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
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

public class DemandProcessor {

    private static final Logger log = Logger.getLogger(DemandProcessor.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    protected static StoreOperations storeOperations = _baseOperations.getStoreOperations();

    /**
     * Load the published demand that has not been updated during the last 8 hours and reinsert them into the task queue
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void batchProcess() throws DataSourceException {
        //
        // Warning: Keep the frequency specified in the cron.xml.tmpl file with the delay used in the function body
        //
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            // Get the date 4 hours in the past
            Calendar now = DateUtils.getNowCalendar();
            Calendar past = now;
            past.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) - 8);
            // Prepare the query with: state == published && modificationDate > past
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("=" + Command.STATE, State.published.toString());
            // FIXME: enable the additional filtering when the issue 25 is solved and delivered
            // parameters.put("<" + Entity.MODIFICATION_DATE, past.getTime());
            List<Demand> demands = demandOperations.getDemands(pm, parameters, 0);
            // Add the corresponding task in the queue
            if (0 < demands.size()) {
                Queue queue = QueueFactory.getDefaultQueue();
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
                List<Proposal> proposals = proposalOperations.getProposals(pm, Proposal.DEMAND_KEY, demand.getKey(), 0);
                List<SaleAssociate> saleAssociates = identifySaleAssociates(pm, demand);
                for(SaleAssociate saleAssociate: saleAssociates) {
                    boolean contactSaleAssociateJustOnce = true;
                    for (Proposal proposal: proposals) {
                        if (proposal.getOwnerKey().equals(saleAssociate.getKey())) {
                            contactSaleAssociateJustOnce = false;
                            break;
                        }
                    }
                    if (contactSaleAssociateJustOnce) {
                        //
                        // Special treatment for demand posted to "locale:H0H H0H CA"
                        //
                        if (RobotResponder.ROBOT_NAME.equals(saleAssociate.getName())) {
                            // Schedule a task to transmit the proposal to the demand owner
                            Queue queue = QueueFactory.getDefaultQueue();
                            queue.add(
                                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/processDemandForRobot").
                                        param(Demand.KEY, demand.getKey().toString()).
                                        method(Method.GET)
                            );
                        }
                        else {
                            communicateToSaleAssociate(
                                    saleAssociate.getPreferredConnection(),
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
                        }
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
        // Get the stores around the demanded location
        Location location = locationOperations.getLocation(pm, demand.getLocationKey());
        List<Location> locations = locationOperations.getLocations(pm, location, demand.getRange(), demand.getRangeUnit(), 0);
        List<Store> stores = storeOperations.getStores(pm, locations, 0);
        // Extracts all sale associates
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        for (Store store: stores) {
            List<SaleAssociate> employees = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.STORE_KEY, store.getKey(), 0);
            saleAssociates.addAll(employees);
        }
        // Verifies that the sale associates supply the demanded tags
        List<SaleAssociate> selectedSaleAssociates = new ArrayList<SaleAssociate>();
        for (SaleAssociate saleAssociate: saleAssociates) {
            long score = 0;
            for (String tag: demand.getCriteria()) {
                if (saleAssociate.getCriteria() != null && saleAssociate.getCriteria().contains(tag)) {
                    ++ score;
                    break; // TODO: check if it's useful to continue counting
                }
            }
            if (0 < score) {
                log.warning("Sale sssociate " + saleAssociate.getKey() + " selected for the demand: " + demand.getKey());
                saleAssociate.setScore(score);
                selectedSaleAssociates.add(saleAssociate);
            }
        }
        return selectedSaleAssociates;
    }
}
