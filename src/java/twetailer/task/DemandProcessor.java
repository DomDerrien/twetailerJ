package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

public class DemandProcessor {

    private static Logger log = Logger.getLogger(DemandProcessor.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    protected static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();
    protected static StoreOperations storeOperations = _baseOperations.getStoreOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Load the published demand that has not been updated during the last 8 hours and reinsert them into the task queue
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void batchProcess() throws DataSourceException {
        //
        // Warning: Keep the frequency specified in the cron.xml.tmpl file in sync with the delay used in the function body
        //
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            // Get the date 9 hours (8 hours as the cron job plus 1 hour for security) in the past
            Calendar now = DateUtils.getNowCalendar();
            Calendar past = now;
            past.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) - 9);
            // Prepare the query with: state == published && modificationDate > past
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("=" + Command.STATE, State.published.toString());
            parameters.put("<" + Entity.MODIFICATION_DATE, past.getTime());
            List<Demand> demands = demandOperations.getDemands(pm, parameters, 0);
            // Add the corresponding task in the queue
            if (0 < demands.size()) {
                Queue queue = _baseOperations.getQueue();
                for (Demand demand: demands) {
                    // Create a task for that demand
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maezel/processPublishedDemand").
                                param(Demand.KEY, demand.getKey().toString()).
                                method(Method.GET)
                    );
                }
            }
        }
        finally {
            pm.close();
        }
    }

    /**
     * Forward the identified demand to listening sale associates
     *
     * @param demandKey Identifier of the demand to process
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void process(Long demandKey) throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            process(pm, demandKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Forward the identified demand to listening sale associates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demandKey Identifier of the demand to process
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void process(PersistenceManager pm, Long demandKey) throws DataSourceException {
        Demand demand = demandOperations.getDemand(pm, demandKey, null);
        if (CommandSettings.State.published.equals(demand.getState())) {
            try {
                // Try to contact regular sale associates
                List<SaleAssociate> saleAssociates = identifySaleAssociates(pm, demand);
                for(SaleAssociate saleAssociate: saleAssociates) {
                    // Communicate with the sale associate
                    communicateToSaleAssociate(
                            new RawCommand(saleAssociate.getPreferredConnection()),
                            saleAssociate,
                            LabelExtractor.get(
                                    demand.getQuantity() == 1 ? "dp_inform_saleAssociate_about_demand_one_item" : "dp_inform_saleAssociate_about_demand_many_items",
                                    new Object[] {
                                        demand.getKey(),
                                        demand.getSerializedCriteria(),
                                        demand.getExpirationDate(),
                                        demand.getQuantity()
                                    },
                                    saleAssociate.getLocale()
                            )
                    );
                    // Keep track of the notification to not ping him/her another time
                    demand.addSaleAssociateKey(saleAssociate.getKey());
                    demandOperations.updateDemand(pm, demand);
                }
                // Special treatment for demand with a tag #demo
                if (demand.getHashTags().contains(RobotResponder.ROBOT_DEMO_HASH_TAG)) {
                    if (!hasRobotAlreadyContacted(pm, demand)) {
                        // Schedule a task to transmit the proposal to the demand owner
                        Queue queue = _baseOperations.getQueue();
                        queue.add(
                                url(ApplicationSettings.get().getServletApiPath() + "/maezel/processDemandForRobot").
                                    param(Demand.KEY, demand.getKey().toString()).
                                    method(Method.GET)
                        );
                        // Keep track of the notification to not ping him/her another time
                        demand.addSaleAssociateKey(robotSaleAssociateKey);
                        demandOperations.updateDemand(pm, demand);
                    }
                }
            }
            catch (DataSourceException ex) {
                log.warning("Cannot get information retaled to demand: " + demand.getKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                log.warning("Cannot communicate with sale associate -- ex: " + ex.getMessage());
            }
        }
    }

    private static Long robotSaleAssociateKey;

    // Just for unit test
    protected static void setRobotKey(Long key) {
        robotSaleAssociateKey = key;
    }

    /**
     * Helper verifying that the robot has not yet been contacted
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demand Consumer's demand to evaluate
     * @return <code>true</code> if the robot key is among the list of contacted sale associate keys or
     *         the robot key is not in the Settings; <code>false</code> otherwise.
     *
     * @throws DataSourceException If the attempt to read the robot key from the data store fails
     */
    protected static boolean hasRobotAlreadyContacted(PersistenceManager pm, Demand demand) throws DataSourceException {
        if (robotSaleAssociateKey == null) {
            Settings settings = settingsOperations.getSettings(pm);
            robotSaleAssociateKey = settings.getRobotSaleAssociateKey();
        }
        if (robotSaleAssociateKey != null) {
            return demand.getSaleAssociateKeys().contains(robotSaleAssociateKey);
        }
        return true;
    }

    /**
     * For the given location, get stores around and return the employees listening for at least one of the demand tags
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demand Consumer demand to consider
     * @return List of sale associates listening for the demand tags, within the area specified by the consumer
     *
     * @throws DataSourceException If the data manipulation fails
     */
    protected static List<SaleAssociate> identifySaleAssociates(PersistenceManager pm, Demand demand) throws DataSourceException {
        List<SaleAssociate> selectedSaleAssociates = new ArrayList<SaleAssociate>();
        // Get the stores around the demanded location
        Location location = locationOperations.getLocation(pm, demand.getLocationKey());
        List<Location> locations = locationOperations.getLocations(pm, location, demand.getRange(), demand.getRangeUnit(), 0);
        if (locations.size() == 0) {
            return selectedSaleAssociates;
        }
        List<Store> stores = storeOperations.getStores(pm, locations, 0);
        if (stores.size() == 0) {
            return selectedSaleAssociates;
        }
        // Extracts all sale associates
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        for (Store store: stores) {
            List<SaleAssociate> employees = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.STORE_KEY, store.getKey(), 0);
            saleAssociates.addAll(employees);
        }
        if (saleAssociates.size() == 0) {
            return selectedSaleAssociates;
        }
        // Verifies that the sale associates supply the demanded tags and that he/she has not been yet contacted
        for (SaleAssociate saleAssociate: saleAssociates) {
            if (demand.getSaleAssociateKeys() == null || !demand.getSaleAssociateKeys().contains(saleAssociate.getKey())) {
                long score = 0;
                for (String tag: demand.getCriteria()) {
                    if (saleAssociate.getCriteria() != null && saleAssociate.getCriteria().contains(tag)) {
                        ++ score;
                        break; // TODO: check if it's useful to continue counting
                    }
                }
                if (0 < score) {
                    log.warning("Sale Asssociate " + saleAssociate.getKey() + " selected for the demand: " + demand.getKey());
                    saleAssociate.setScore(score);
                    selectedSaleAssociates.add(saleAssociate);
                }
            }
        }
        return selectedSaleAssociates;
    }
}
