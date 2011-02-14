package twetailer.task;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

import javax.jdo.PersistenceManager;
import javax.mail.MessagingException;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.ChannelConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

/**
 * Define the task with is invoked by the task "/_tasks/validateOpenDemand"
 * in order to broadcast the valid Demand to matching sale associates
 * in the area.
 *
 * When everything is OK, a message is sent to sale associates according
 * to their preferred communication channel (IMAP, XMPP, Twitter, etc.).
 *
 * @see twetailer.task.DemandValidator
 *
 * @author Dom Derrien
 */
public class DemandProcessor {

    private static Logger log = Logger.getLogger(DemandProcessor.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    /**
     * Load the published demand that has not been updated during the last 8 hours and reinsert them into the task queue
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void batchProcess() throws DataSourceException {
        //
        // Warning: Keep the frequency specified in the src/resources/cron.xml file in sync with the delay used in the function body
        //
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Get the date 25 hours (24 hours as the cron job plus 1 hour for security) in the past
            Calendar now = DateUtils.getNowCalendar();
            Calendar past = now;
            past.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) - 25);
            // Prepare the query with: state == published && modificationDate > past
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("=" + Command.STATE, State.published.toString());
            parameters.put("<" + Entity.MODIFICATION_DATE, past.getTime());
            List<Long> demandKeys = BaseSteps.getDemandOperations().getDemandKeys(pm, parameters, 0);
            // Add the corresponding task in the queue
            if (0 < demandKeys.size()) {
                Queue queue = BaseSteps.getBaseOperations().getQueue();
                for (Long key: demandKeys) {
                    // Create a task for that demand
                    queue.add(
                            withUrl("/_tasks/processPublishedDemand").
                                param(Demand.KEY, key.toString()).
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
     * @param cronJob <code>true</code> if the process has been commanded by the cron job
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(Long demandKey, boolean cronJob) throws DataSourceException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            process(pm, demandKey, cronJob);
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
     * @param cronJob <code>true</code> if the process has been commanded by the cron job
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(PersistenceManager pm, Long demandKey, boolean cronJob) throws InvalidIdentifierException, DataSourceException {
        Demand demand = BaseSteps.getDemandOperations().getDemand(pm, demandKey, null);
        if (CommandSettings.State.published.equals(demand.getState())) {
            Consumer owner = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());

            // Special treatment for demand with a tag #demo
            if (demand.getHashTags().contains(RobotResponder.ROBOT_DEMO_HASH_TAG)) {
                if (!hasRobotAlreadyContacted(pm, demand)) {
                    // Schedule a task to transmit the proposal to the demand owner
                    Queue queue = BaseSteps.getBaseOperations().getQueue();
                    queue.add(
                            withUrl("/_tasks/processDemandForRobot").
                                param(Demand.KEY, demand.getKey().toString()).
                                method(Method.GET)
                    );
                    // Keep track of the notification to not ping him/her another time
                    demand.addSaleAssociateKey(RobotResponder.getRobotSaleAssociateKey(pm));
                }
            }
            else {
                boolean isInfluencerIdentifiedAsAMatchingSaleAssociate = false;
                Influencer influencer = BaseSteps.getInfluencerOperations().getInfluencer(pm, demand.getInfluencerKey());
                // Try to contact regular sale associates
                List<SaleAssociate> saleAssociates = identifySaleAssociates(pm, demand, owner);
                for(SaleAssociate saleAssociate: saleAssociates) {
                    if (!isInfluencerIdentifiedAsAMatchingSaleAssociate) {
                        isInfluencerIdentifiedAsAMatchingSaleAssociate = saleAssociate.getConsumerKey().equals(influencer.getConsumerKey());
                    }
                    Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                    // Communicate with the sale associate
                    notifyAvailability(demand, owner, saConsumerRecord, influencer);

                    // Keep track of the notification to not ping him/her another time
                    demand.addSaleAssociateKey(saleAssociate.getKey());
                }
                // Inform the influencer even if he was not already concerned by the demand
                if (!isInfluencerIdentifiedAsAMatchingSaleAssociate) {
                    Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, influencer.getConsumerKey());
                    if (!demand.getSaleAssociateKeys().contains(saConsumerRecord.getSaleAssociateKey())) {
                        // Communicate with the sale associate
                        notifyAvailability(demand, owner, saConsumerRecord, influencer);

                        // Keep track of the notification to not ping him/her another time
                        demand.addSaleAssociateKey(saConsumerRecord.getSaleAssociateKey());
                    }
                }
            }

            // Push the updated demand into the data store
            demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);
        }
    }

    /**
     * Helper verifying that the robot has not yet been contacted
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demand Consumer's demand to evaluate
     * @return <code>true</code> if the robot key is among the list of contacted sale associate keys; <code>false</code> otherwise.
     *
     * @throws DataSourceException If the attempt to read the robot key from the data store fails
     */
    protected static boolean hasRobotAlreadyContacted(PersistenceManager pm, Demand demand) throws DataSourceException {
        Long robotSaleAssociateKey = RobotResponder.getRobotSaleAssociateKey(pm);
        if (robotSaleAssociateKey != null) {
            return demand.getSaleAssociateKeys().contains(robotSaleAssociateKey);
        }
        return false;
    }

    /**
     * For the given location, get stores around and return the employees listening for at least one of the demand tags
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demand Consumer demand to consider
     * @param owner Demand owner
     * @return List of sale associates listening for the demand tags, within the area specified by the consumer
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the extraction of the Location for the given Demand fails
     */
    protected static List<SaleAssociate> identifySaleAssociates(PersistenceManager pm, Demand demand, Consumer owner) throws InvalidIdentifierException, DataSourceException {
        List<SaleAssociate> selectedSaleAssociates = new ArrayList<SaleAssociate>();
        // Get the stores around the demanded location
        Location location = BaseSteps.getLocationOperations().getLocation(pm, demand.getLocationKey());
        List<Location> locations = BaseSteps.getLocationOperations().getLocations(pm, location, demand.getRange(), demand.getRangeUnit(), true, 0);
        if (locations.size() == 0) {
            return selectedSaleAssociates;
        }
        List<Store> stores = BaseSteps.getStoreOperations().getStores(pm, new HashMap<String, Object>(), locations, 0);
        if (stores.size() == 0) {
            return selectedSaleAssociates;
        }
        // Extracts all sale associates
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        for (Store store: stores) {
            List<SaleAssociate> employees = BaseSteps.getSaleAssociateOperations().getSaleAssociates(pm, SaleAssociate.STORE_KEY, store.getKey(), 0);
            saleAssociates.addAll(employees);
        }
        if (saleAssociates.size() == 0) {
            return selectedSaleAssociates;
        }
        // Filter out non matching Sale Associates
        return filterSaleAssociates(saleAssociates, demand, owner);
    }

    /**
     * Filter out Sale Associates who don't supply a tag or a hash tag (depends on each one's score) for the given demand
     *
     * @param saleAssociates Array of pre-selected Sale Associates
     * @param demand Consumer demand to consider
     * @param owner Demand owner
     * @return List of sale associates listening for the demand tags
     */
    protected static List<SaleAssociate> filterSaleAssociates(List<SaleAssociate> saleAssociates, Demand demand, Consumer owner) {
        List<SaleAssociate> selectedSaleAssociates = new ArrayList<SaleAssociate>();

        // Verifies that the sale associates supply the demanded tags and that he/she has not been yet contacted
        Locale locale = owner.getLocale();
        for (SaleAssociate saleAssociate: saleAssociates) {
            // Verifies that the sale associate has not been yet informed about this demand
            if (demand.getSaleAssociateKeys() == null || !demand.getSaleAssociateKeys().contains(saleAssociate.getKey())) {
                String score = saleAssociate.getScore();
                long hashTagScore = 0, normalTagScore = 1, floatingScore = 0; // Default values
                if (score.startsWith("1:")) {
                    String[] scores = score.substring(2).split("\\.");
                    hashTagScore = Long.valueOf(scores[0]);
                    normalTagScore = Long.valueOf(scores[1]);
                }
                if (0 < hashTagScore) {
                    List<String> suppliedHashTags = saleAssociate.getHashTags();
                    if (suppliedHashTags != null) {
                        if (demand.getHashTags() != null) {
                            for (String tag: demand.getHashTags()) {
                                if (suppliedHashTags.contains(tag.toLowerCase(locale))) {
                                    ++ floatingScore;
                                    if (hashTagScore == floatingScore) {
                                        break; // Number of hash tags reached
                                    }
                                }
                            }
                        }
                    }
                }
                if (0 < normalTagScore) {
                    List<String> suppliedTags = saleAssociate.getCriteria();
                    if (suppliedTags != null) {
                        if (demand.getCriteria() != null) {
                            for (String tag: demand.getCriteria()) {
                                if (suppliedTags.contains(tag.toLowerCase(locale))) {
                                    ++ floatingScore;
                                    if (hashTagScore + normalTagScore == floatingScore) {
                                        break; // Number of normal tags reached
                                    }
                                }
                            }
                        }
                    }
                }
                if (hashTagScore + normalTagScore == floatingScore) {
                    selectedSaleAssociates.add(saleAssociate);
                }
            }
        }
        return selectedSaleAssociates;
    }

    /**
     * Send a message to the identified sale associate about the demand ready to be proposed
     *
     * @param demand New or updated demand to be presented to the sale associate
     * @param demandOwner Record of the demand owner, used to expose the closing rate
     * @param saConsumerRecord Associate record of the sale associate to be contacted
     * @param influencer Descriptor of the entity who helped creating the demand
     */
    public static void notifyAvailability(Demand demand, Consumer demandOwner, Consumer saConsumerRecord, Influencer influencer) {

        try {
            // Try to communicate to the logged sale associate
            JsonObject tmp = new GenericJsonObject();
            tmp.put("resource", demand.toJson());
            ChannelConnector.sendMessage(saConsumerRecord, tmp);

            // Send a formal notification
            if (!Source.api.equals(saConsumerRecord.getPreferredConnection())) {
                Locale locale = saConsumerRecord.getLocale();

                MessageGenerator msgGen = new MessageGenerator(saConsumerRecord.getPreferredConnection(), demand.getHashTags(), locale);
                msgGen.
                    put("proposal>owner>name", saConsumerRecord.getName()).
                    fetch(demand).
                    fetch(influencer).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                MessageGenerator cmdGen = new MessageGenerator(Source.jabber, demand.getHashTags(), locale). // "jabber" is a used to "source" to be able to generate short command by e-mail
                    fetch(demand).
                    put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));
                String createProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_create", cmdGen.getParameters(), locale);
                String declineDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_decline", cmdGen.getParameters(), locale);
                String subject = msgGen.getAlternateMessage(MessageId.messageSubject, cmdGen.getParameters());

                msgGen.
                    put("command>threadSubject", BaseConnector.prepareMailToSubject(MailConnector.prepareSubjectAsResponse(subject, locale))).
                    put("command>declineDemand", BaseConnector.prepareMailToBody(declineDemand)).
                    put("command>createProposal", BaseConnector.prepareMailToBody(createProposal));

                double publishedNb = demandOwner.getPublishedDemandNb() == null ? 1 : demandOwner.getPublishedDemandNb(); // Can't be null with new demands, but can still be null with the old ones without this field
                double closedNb = demandOwner.getClosedDemandNb() == null ? 0 : demandOwner.getClosedDemandNb();
                msgGen.
                    put("demand>owner>publishedDemandNb", (long) publishedNb).
                    put("demand>owner>closedDemandNb", (long) closedNb).
                    put(
                            "demand>owner>closedDemandPercentage",
                            LocaleValidator.formatFloatWith2Digits(100.0D * closedNb / publishedNb, locale)
                    );

                communicateToConsumer(
                        msgGen.getCommunicationChannel(),
                        subject,
                        saConsumerRecord,
                        new String[] { msgGen.getMessage(MessageId.DEMAND_CREATION_OK_TO_ASSOCIATE) }
                );
            }
        }
        catch (ClientException ex) {
            // Send an e-mail to out catch-all list
            MockOutputStream stackTrace = new MockOutputStream();
            ex.printStackTrace(new PrintStream(stackTrace));
            try {
                MailConnector.reportErrorToAdmins(
                        "Unexpected error caught in " + DemandProcessor.class.getName(),
                        "Path info: /processPublishedDemand?key=" + demand.getKey().toString() + "\n\n--\n\nSale associate: " + saConsumerRecord.getName() + " (" + saConsumerRecord.getSaleAssociateKey() + ")\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                getLogger().severe("Failure while trying to report an unexpected by e-mail!");
            }
        }
    }
}
