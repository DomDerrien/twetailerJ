package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.Calendar;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.dto.HashTag.RegisteredHashTag;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

/**
 * Define the logic that process Demand instances
 * created with the hash tag "#demo".
 *
 * @author Dom Derrien
 */
public class RobotResponder {

    public static final String ROBOT_NAME = "Jack the Troll";
    public static final String ROBOT_POSTAL_CODE = "H0H0H0";
    public static final String ROBOT_COUNTRY_CODE = Locale.CANADA.getCountry();

    public static void processDemand(Long demandKey) throws DataSourceException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            processDemand(pm, demandKey);
        }
        finally {
            pm.close();
        }
    }

    public final static String ROBOT_DEMO_HASH_TAG = RegisteredHashTag.demo.toString();

    public static void processDemand(PersistenceManager pm, Long demandKey) throws DataSourceException, InvalidIdentifierException {
        //
        // TODO: add the robot sale associate key into the setting table
        // TODO: always load the robot from that key
        // TODO: try to put the key in memcached
        //
        Long robotKey = getRobotSaleAssociateKey(pm);
        if (robotKey != null) {
            SaleAssociate robot = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, robotKey);
            Store store = BaseSteps.getStoreOperations().getStore(pm, robot.getStoreKey());

            Demand demand = BaseSteps.getDemandOperations().getDemand(pm, demandKey, null);
            Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
            if (CommandSettings.State.published.equals(demand.getState())) {
                // Create a new and valid proposal
                Proposal proposal = new Proposal();
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(robot.getKey());
                proposal.setPrice(0.01D);
                proposal.setQuantity(4L);
                Calendar tomorrow = DateUtils.getNowCalendar();
                tomorrow.set(Calendar.DATE, tomorrow.get(Calendar.DATE) + 1);
                proposal.setDueDate(tomorrow.getTime());
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
                proposal = BaseSteps.getProposalOperations().createProposal(pm, proposal);
                // Schedule a task to transmit the proposal to the demand owner
                Queue queue = BaseSteps.getBaseOperations().getQueue();
                queue.add(
                        url(ApplicationSettings.get().getServletApiPath() + "/maezel/processPublishedProposal").
                            param(Proposal.KEY, proposal.getKey().toString()).
                            method(Method.GET).
                            countdownMillis(30*1000)
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
            Settings settings = BaseSteps.getSettingsOperations().getSettings(pm);
            robotConsumerKey = settings.getRobotConsumerKey();
        }
        return robotConsumerKey;
    }

    private static Long robotSaleAssociateKey;

    // Just for unit test
    public static void setRobotSaleAssociateKey(Long key) {
        robotSaleAssociateKey = key;
    }

    public static Long getRobotSaleAssociateKey(PersistenceManager pm) throws DataSourceException {
        if (robotSaleAssociateKey == null) {
            Settings settings = BaseSteps.getSettingsOperations().getSettings(pm, true);
            robotSaleAssociateKey = settings.getRobotSaleAssociateKey();
        }
        return robotSaleAssociateKey;
    }
}
