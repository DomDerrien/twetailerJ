package twetailer.task;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static twetailer.connector.BaseConnector.communicateToCCed;
import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.getCCedCommunicationChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.CommunicationException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.LocationSteps;
import twetailer.validator.CommandSettings;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;

/**
 * Define the task with is invoked by methods in DemandSteps
 * every time a Demand is updated significantly. If the Demand
 * instance is valid, the task "/_tasks/processPublishedDemand"
 * is scheduled to broadcast it to the matching SaleAssociate
 * in the area.
 *
 * @see twetailer.dto.Demand
 * @see twetailer.task.step.DemandSteps
 * @see twetailer.task.DemandProcessor
 *
 * @author Dom Derrien
 */
public class DemandValidator {

    private static Logger log = Logger.getLogger(DemandValidator.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    /**
     * Check the validity of the identified demand
     *
     * @param demandKey Identifier of the demand to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(Long demandKey) throws DataSourceException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            process(pm, demandKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Check the validity of the identified demand
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demandKey Identifier of the demand to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(PersistenceManager pm, Long demandKey) throws DataSourceException, InvalidIdentifierException {
        Demand demand = BaseSteps.getDemandOperations().getDemand(pm, demandKey, null);
        if (CommandSettings.State.opened.equals(demand.getState())) {
            try {
                Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());

                // Temporary filter
                RequestValidator.filterHashTags(pm, consumer, demand, "demand");

                // Check each fields
                String message = RequestValidator.checkWishFields(pm, consumer, demand, "demand");

                RawCommand rawCommand = demand.getRawCommandId() == null ? new RawCommand(demand.getSource()) : BaseSteps.getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId());
                if (message != null) {
                    demand.setState(CommandSettings.State.invalid);
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);

                    if (!Source.api.equals(demand.getSource())) {
                        communicateToConsumer(
                            rawCommand.getSource(),
                            rawCommand.getSubject(),
                            consumer,
                            new String[] { message }
                        );
                    }
                }
                else {
                    demand.setState(CommandSettings.State.published);
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);
                    consumer.setPublishedDemandNb(consumer.getPublishedDemandNb() == null ? 1 : consumer.getPublishedDemandNb() + 1);
                    consumer = BaseSteps.getConsumerOperations().updateConsumer(pm, consumer);

                    // Create a task for that demand
                    Queue queue = BaseSteps.getBaseOperations().getQueue();
                    queue.add(
                            withUrl("/_tasks/processPublishedDemand").
                                param(Demand.KEY, demandKey.toString()).
                                method(Method.GET).
                                countdownMillis(5000)
                    );

                    Influencer influencer = BaseSteps.getInfluencerOperations().getInfluencer(pm, demand.getInfluencerKey());

                    confirmUpdate(rawCommand, demand, LocationSteps.getLocation(pm, demand), consumer, influencer);
                }
            }
            catch (DataSourceException ex) {
                getLogger().warning("Cannot get information for consumer: " + demand.getOwnerKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                getLogger().warning("Cannot communicate with consumer -- ex: " + ex.getMessage());
            }
        }
    }

    /**
     * Prepare the messages sent to the demand owner and to the CC'ed users to notify them
     * about the new demand state.
     *
     * @param rawCommand rawCommand at the origin of the demand creation or update
     * @param demand Demand instance just validate after its creation or update
     * @param location Location attached to the demand
     * @param owner Demand owner
     * @param influencer Descriptor of the entity who helped creating the demand
     *
     * @throws CommunicationException If the communication with the demand owner fails
     */
    public static void confirmUpdate(RawCommand rawCommand, Demand demand, Location location, Consumer owner, Influencer influencer) throws CommunicationException {
        List<String> cc = demand.getCC();
        if (!Source.api.equals(demand.getSource()) || cc != null && 0 < cc.size()) {
            boolean isNewDemand = demand.getCreationDate().getTime() == demand.getModificationDate().getTime();
            Locale locale = owner.getLocale();


            // Send a notification to the Owner
            if (!Source.api.equals(demand.getSource())) {
                MessageGenerator msgGen = new MessageGenerator(demand.getSource(), demand.getHashTags(), locale);
                msgGen.
                    put("demand>owner>name", owner.getName()).
                    fetch(demand).
                    fetch(location, "demand").
                    fetch(influencer).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                    put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                String cancelDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_cancel", msgGen.getParameters(), locale);
                String updateDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_update", msgGen.getParameters(), locale);
                String subject = null;
                if (Source.mail.equals(msgGen.getCommunicationChannel())) {
                    subject = rawCommand.getSubject();
                }
                if (subject == null) {
                    subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                }
                String reSubject = MailConnector.prepareSubjectAsResponse(subject, locale);

                msgGen.
                    put("command>threadSubject", BaseConnector.prepareMailToSubject(reSubject)).
                    put("command>cancelDemand", BaseConnector.prepareMailToBody(cancelDemand)).
                    put("command>updateDemand", BaseConnector.prepareMailToBody(updateDemand));

                String message = msgGen.getMessage(isNewDemand ? MessageId.DEMAND_CREATION_OK_TO_CONSUMER: MessageId.DEMAND_UPDATE_OK_TO_CONSUMER);

                communicateToConsumer(
                        msgGen.getCommunicationChannel(),
                        isNewDemand ? subject : reSubject,
                        owner,
                        new String[] { message }
                );
            }

            // Send a notification to the CC'ed users
            if (cc != null && 0 < cc.size()) {
                MessageGenerator msgGen = null;
                String message = null;
                String subject = null;
                for (String coordinate: cc) {
                    try {
                        Source source = getCCedCommunicationChannel(coordinate);

                        if (msgGen == null || !source.equals(msgGen.getCommunicationChannel())) {
                            //
                            // TODO: cache the MessageGenerator instance per Source value to avoid unnecessary re-creation!
                            //
                            msgGen = new MessageGenerator(source, demand.getHashTags(), locale);
                            msgGen.
                                put("demand>owner>name", owner.getName()).
                                fetch(demand).
                                fetch(location, "demand").
                                fetch(influencer).
                                put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                            message = msgGen.getMessage(isNewDemand ? MessageId.DEMAND_CREATION_OK_TO_CCED: MessageId.DEMAND_UPDATE_OK_TO_CCED);
                        }

                        if (subject == null) {
                            Map<String, Object> cmdPrm = new HashMap<String, Object>();
                            cmdPrm.put("demand>key", demand.getKey());
                            subject = msgGen.getAlternateMessage(MessageId.messageSubject, cmdPrm);
                            subject = MailConnector.prepareSubjectAsForward(subject, locale);
                        }

                        communicateToCCed(
                                source,
                                coordinate,
                                subject,
                                message,
                                locale
                        );
                    }
                    catch (ClientException e) { } // Too bad, cannot contact the CC-ed person... Don't block the next sending!
                }
            }
        }
    }
}
