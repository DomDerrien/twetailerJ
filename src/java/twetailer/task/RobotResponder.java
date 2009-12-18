package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;

public class RobotResponder {

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    protected static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();
    protected static StoreOperations storeOperations = _baseOperations.getStoreOperations();

    public static final String ROBOT_NAME = "Jack the Troll";
    public static final String ROBOT_POSTAL_CODE = "H0H0H0";
    public static final String ROBOT_COUNTRY_CODE = Locale.CANADA.getCountry();

    public static void processDemand(Long demandKey) throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            processDemand(pm, demandKey);
        }
        finally {
            pm.close();
        }
    }

    public final static String ROBOT_DEMO_HASH_TAG = "demo";

    public static void processDemand(PersistenceManager pm, Long demandKey) throws DataSourceException {
        //
        // TODO: add the robot sale associate key into the setting table
        // TODO: always load the robot from that key
        // TODO: try to put the key in memcached
        //
        Long robotKey = getRobotSaleAssociateKey(pm);
        if (robotKey != null) {
            SaleAssociate robot = saleAssociateOperations.getSaleAssociate(pm, robotKey);
            Store store = storeOperations.getStore(pm, robot.getStoreKey());

            Demand demand = demandOperations.getDemand(pm, demandKey, null);
            Consumer consumer = consumerOperations.getConsumer(pm, demand.getOwnerKey());
            if (CommandSettings.State.published.equals(demand.getState())) {
                // Create a new and valid proposal
                Proposal proposal = new Proposal();
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(robot.getKey());
                proposal.setPrice(0.01D);
                proposal.setQuantity(100L);
                proposal.setSource(Source.simulated);
                proposal.addHashTag(ROBOT_DEMO_HASH_TAG);
                proposal.setState(State.published);
                proposal.setStoreKey(store.getKey());
                proposal.setTotal(1.15D);
                // Prepare the automated response
                String message = LabelExtractor.get("rr_robot_automatic_proposition", consumer.getLocale());
                String[] parts = message.split(" ");
                for (String part: parts) {
                    proposal.addCriterion(part);
                }
                // Persist the newly created proposal
                proposal = proposalOperations.createProposal(pm, proposal);
                // Schedule a task to transmit the proposal to the demand owner
                Queue queue = _baseOperations.getQueue();
                queue.add(
                        url(ApplicationSettings.get().getServletApiPath() + "/maezel/processPublishedProposal").
                            param(Proposal.KEY, proposal.getKey().toString()).
                            method(Method.GET)
                );
            }
        }
    }

    private static Long robotConsumerKey;

    // Just for unit test
    protected static void setRobotConsumerKey(Long key) {
        robotConsumerKey = key;
    }

    public static Long getRobotConsumerKey(PersistenceManager pm) throws DataSourceException {
        if (robotConsumerKey == null) {
            Settings settings = settingsOperations.getSettings(pm);
            robotConsumerKey = settings.getRobotConsumerKey();
        }
        return robotConsumerKey;
    }

    private static Long robotSaleAssociateKey;

    // Just for unit test
    protected static void setRobotSaleAssociateKey(Long key) {
        robotSaleAssociateKey = key;
    }

    public static Long getRobotSaleAssociateKey(PersistenceManager pm) throws DataSourceException {
        if (robotSaleAssociateKey == null) {
            Settings settings = settingsOperations.getSettings(pm);
            robotSaleAssociateKey = settings.getRobotSaleAssociateKey();
        }
        return robotSaleAssociateKey;
    }
}
