package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
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
import twetailer.CommunicationException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.j2ee.CatchAllMailHandlerServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.LocationSteps;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

/**
 * Define the task with is invoked by the task "/maelzel/validateOpenDemand"
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
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Get the date 9 hours (8 hours as the cron job plus 1 hour for security) in the past
            Calendar now = DateUtils.getNowCalendar();
            Calendar past = now;
            past.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) - 9);
            // Prepare the query with: state == published && modificationDate > past
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("=" + Command.STATE, State.published.toString());
            parameters.put("<" + Entity.MODIFICATION_DATE, past.getTime());
            List<Demand> demands = BaseSteps.getDemandOperations().getDemands(pm, parameters, 0);
            // Add the corresponding task in the queue
            if (0 < demands.size()) {
                Queue queue = BaseSteps.getBaseOperations().getQueue();
                for (Demand demand: demands) {
                    // Create a task for that demand
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maelzel/processPublishedDemand").
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
        int initialNumberOfSaleAssociatesContacted = demand.getSaleAssociateKeys() == null ? 0 : demand.getSaleAssociateKeys().size();
        if (CommandSettings.State.published.equals(demand.getState())) {
            Consumer owner = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
            // Try to contact regular sale associates
            List<SaleAssociate> saleAssociates = identifySaleAssociates(pm, demand, owner);
            for(SaleAssociate saleAssociate: saleAssociates) {
                Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                // Communicate with the sale associate
                try {
                    notifyAvailability(demand, saConsumerRecord);

                    // Keep track of the notification to not ping him/her another time
                    demand.addSaleAssociateKey(saleAssociate.getKey());
                }
                catch (ClientException ex) {
                    // Send an e-mail to out catch-all list
                    MockOutputStream stackTrace = new MockOutputStream();
                    ex.printStackTrace(new PrintStream(stackTrace));
                    try {
                        CatchAllMailHandlerServlet.composeAndPostMailMessage(
                                "error-notifier",
                                "Unexpected error caught in " + DemandProcessor.class.getName(),
                                "Path info: /processPublishedDemand?key=" + demand.getKey().toString() + "\n\n--\n\nSale associate: " + saConsumerRecord.getName() + " (" + saleAssociate.getKey() + ")\n\n--\n\n" + stackTrace.toString()
                        );
                    }
                    catch (MessagingException e) {
                        log.severe("Failure while trying to report an unexpected by e-mail!");
                    }
                }
            }

            // Special treatment for demand with a tag #demo
            if (demand.getHashTags().contains(RobotResponder.ROBOT_DEMO_HASH_TAG)) {
                if (!hasRobotAlreadyContacted(pm, demand)) {
                    // Schedule a task to transmit the proposal to the demand owner
                    Queue queue = BaseSteps.getBaseOperations().getQueue();
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maelzel/processDemandForRobot").
                                param(Demand.KEY, demand.getKey().toString()).
                                method(Method.GET)
                    );
                    // Keep track of the notification to not ping him/her another time
                    demand.addSaleAssociateKey(RobotResponder.getRobotSaleAssociateKey(pm));
                }
            }

            // Push the updated demand into the datastore
            demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);

            // Notify the consumer about how many sale associates have been successfully contacted this time
            boolean isNewDemand = demand.getCreationDate().getTime() == demand.getModificationDate().getTime();
            if (!isNewDemand && !Source.api.equals(demand.getSource())) {
                try {
                    int newNumberOfSaleAssociatesContacted = demand.getSaleAssociateKeys() == null ? 0 : demand.getSaleAssociateKeys().size();
                    Locale locale = owner.getLocale();
                    if (newNumberOfSaleAssociatesContacted != initialNumberOfSaleAssociatesContacted) {
                        RawCommand rawCommand = demand.getRawCommandId() == null ? new RawCommand(demand.getSource()) : BaseSteps.getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId());
                        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                        communicateToConsumer(
                                rawCommand.getSource(),
                                rawCommand.getSubject(),
                                owner,
                                new String[] {
                                    LabelExtractor.get(
                                        "dp_inform_consumer_about_new_sale_associates_contacted",
                                        new Object[] {
                                            demandRef,
                                            newNumberOfSaleAssociatesContacted,
                                            newNumberOfSaleAssociatesContacted - initialNumberOfSaleAssociatesContacted
                                        },
                                        locale
                                    )
                                }
                        );
                    }
                    else if (cronJob && initialNumberOfSaleAssociatesContacted == 0) {
                        RawCommand rawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId());
                        communicateToConsumer(
                                rawCommand.getSource(),
                                rawCommand.getSubject(),
                                owner,
                                new String[] {
                                    LabelExtractor.get(
                                        "dp_inform_consumer_about_no_store",
                                        locale
                                    )
                                }
                        );
                    }
                }
                catch (ClientException ex) {
                    // Send an e-mail to out catch-all list
                    MockOutputStream stackTrace = new MockOutputStream();
                    ex.printStackTrace(new PrintStream(stackTrace));
                    try {
                        CatchAllMailHandlerServlet.composeAndPostMailMessage(
                                "error-notifier",
                                "Unexpected error caught in " + DemandProcessor.class.getName(),
                                "Path info: /processPublishedDemand?key=" + demand.getKey().toString() + "\n\n--\n\nConsumer: " + owner.getName() + " (" + owner.getKey() + ")\n\n--\n\n" + stackTrace.toString()
                        );
                    }
                    catch (MessagingException e) {
                        log.severe("Failure while trying to report an unexpected by e-mail!");
                    }
                }
            }
        }
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
        Long robotSaleAssociateKey = RobotResponder.getRobotSaleAssociateKey(pm);
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
     * @param owner Demand owner
     * @return List of sale associates listening for the demand tags, within the area specified by the consumer
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the extraction of the Location for the given Demand fails
     */
    protected static List<SaleAssociate> identifySaleAssociates(PersistenceManager pm, Demand demand, Consumer owner) throws InvalidIdentifierException, DataSourceException {
        List<SaleAssociate> selectedSaleAssociates = new ArrayList<SaleAssociate>();
        // Get the stores around the demanded location
        Location location = LocationSteps.getLocation(pm, demand);
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
        // Verifies that the sale associates supply the demanded tags and that he/she has not been yet contacted
        Locale locale = owner.getLocale();
        for (SaleAssociate saleAssociate: saleAssociates) {
            // Verifies that the sale associate has not been yet informed about this demand
            if (demand.getSaleAssociateKeys() == null || !demand.getSaleAssociateKeys().contains(saleAssociate.getKey())) {
                List<String> suppliedHashTags = saleAssociate.getHashTags();
                long score = 0;
                if (suppliedHashTags != null) {
                    if (demand.getHashTags() != null) {
                        for (String tag: demand.getHashTags()) {
                            if (suppliedHashTags.contains(tag.toLowerCase(locale))) {
                                ++ score;
                                break; // One common hash tag is enough
                            }
                        }
                    }
                }
                if (score == 0) {
                    List<String> suppliedTags = saleAssociate.getCriteria();
                    if (suppliedTags != null) {
                        if (demand.getCriteria() != null) {
                            for (String tag: demand.getCriteria()) {
                                if (suppliedTags.contains(tag.toLowerCase(locale))) {
                                    ++ score;
                                    break; // TODO: use the sale associate required score before accepting the match
                                }
                            }
                        }
                    }
                }
                if (0 < score) {
                    saleAssociate.setScore(score);
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
     * @param associate Associate record of the sale associate to be contacted
     *
     * @throws CommunicationException If the communication with the demand owner fails
     */
    public static void notifyAvailability(Demand demand, Consumer associate) throws CommunicationException {

        // TODO: check if it's worth caching the MessageGenerator instance for the associate's locale, so we can save on the time to serialize the demand and the commands -- Note the associate name field will have to be updated

        Locale locale = associate.getLocale();
        final String proposedDate = DateUtils.dateToYMD(demand.getDueDate());
        final String subject = "ezToff Notification about Request:" + demand.getKey();
        final String automatedResponseFooter = "%0A--%0AThis email will be sent to ezToff's automated mail reader.";
        final String createProposal = "propose demand:" + demand.getKey().toString() + " players:" + demand.getQuantity().toString() + " due:" + proposedDate + "T??:?? price:$? total:$? infos:? meta:{pull:?,buggy:?}";
        final String declineDemand = "decline demand:" + demand.getKey().toString();

        MessageGenerator msgGen = new MessageGenerator(demand.getSource(), demand.getHashTags(), locale);
        msgGen.
            put("proposal>owner>name", associate.getName()).
            fetch(demand).
            put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
            put("control>threadSubject", msgGen.put("control.threadSubject", subject)).
            put("control>createProposal", (createProposal + automatedResponseFooter).replaceAll(" ", "%20").replaceAll("\n", "%0A")).
            put("control>declineDemand", (declineDemand + automatedResponseFooter).replaceAll(" ", "%20").replaceAll("\n", "%0A"));

        // TODO: place 'automatedResponseFooter' loaded by msgGen.getRawMessage()
        // TODO: load default 'threadSubject' and inject a the demand key
        // TODO: add 'long_command_create_proposal' in the TMX
        // TODO: add 'long_command_decline_demand' in the TMX

        RawCommand rawCommand = new RawCommand(associate.getPreferredConnection());
        rawCommand.setSubject(subject);
        communicateToConsumer(
                associate.getPreferredConnection(),
                subject,
                associate,
                new String[] { msgGen.getMessage(MessageId.demandCreationNot) }
        );
    }
}
