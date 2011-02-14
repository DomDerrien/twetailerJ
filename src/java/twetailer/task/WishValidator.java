package twetailer.task;

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
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.dto.Wish;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.LocationSteps;
import twetailer.validator.CommandSettings;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;

/**
 * Define the task with is invoked by methods in WishSteps
 * every time a Wish is updated significantly. If the Wish
 * instance is valid, the task "/_tasks/processPublishedWish"
 * is scheduled to broadcast it to the matching SaleAssociate
 * in the area.
 *
 * @see twetailer.dto.Wish
 * @see twetailer.task.step.WishSteps
 * @see twetailer.task.WishProcessor
 *
 * @author Dom Derrien
 */
public class WishValidator {

    private static Logger log = Logger.getLogger(WishValidator.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    /**
     * Check the validity of the identified wish
     *
     * @param wishKey Identifier of the wish to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(Long wishKey) throws DataSourceException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            process(pm, wishKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Check the validity of the identified wish
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param wishKey Identifier of the wish to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(PersistenceManager pm, Long wishKey) throws DataSourceException, InvalidIdentifierException {
        Wish wish = BaseSteps.getWishOperations().getWish(pm, wishKey, null);
        if (CommandSettings.State.opened.equals(wish.getState())) {
            try {
                Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, wish.getOwnerKey());

                // Temporary filter
                RequestValidator.filterHashTags(pm, consumer, wish, "wish");

                // Check each fields
                String message = RequestValidator.checkRequestFields(pm, consumer, wish, "wish");

                RawCommand rawCommand = wish.getRawCommandId() == null ? new RawCommand(wish.getSource()) : BaseSteps.getRawCommandOperations().getRawCommand(pm, wish.getRawCommandId());
                if (message != null) {
                    wish.setState(CommandSettings.State.invalid);
                    wish = BaseSteps.getWishOperations().updateWish(pm, wish);

                    if (!Source.api.equals(wish.getSource())) {
                        communicateToConsumer(
                            rawCommand.getSource(),
                            rawCommand.getSubject(),
                            consumer,
                            new String[] { message }
                        );
                    }
                }
                else {
                    wish.setState(CommandSettings.State.published);
                    wish = BaseSteps.getWishOperations().updateWish(pm, wish);

                    Influencer influencer = BaseSteps.getInfluencerOperations().getInfluencer(pm, wish.getInfluencerKey());

                    confirmUpdate(rawCommand, wish, LocationSteps.getLocation(pm, wish), consumer, influencer);
                }
            }
            catch (DataSourceException ex) {
                getLogger().warning("Cannot get information for consumer: " + wish.getOwnerKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                getLogger().warning("Cannot communicate with consumer -- ex: " + ex.getMessage());
            }
        }
    }

    /**
     * Prepare the messages sent to the wish owner and to the CC'ed users to notify them
     * about the new wish state.
     *
     * @param rawCommand rawCommand at the origin of the wish creation or update
     * @param wish Wish instance just validate after its creation or update
     * @param location Location attached to the wish
     * @param owner Wish owner
     * @param influencer Descriptor of the entity who helped creating the wish
     *
     * @throws CommunicationException If the communication with the wish owner fails
     */
    public static void confirmUpdate(RawCommand rawCommand, Wish wish, Location location, Consumer owner, Influencer influencer) throws CommunicationException {
        List<String> cc = wish.getCC();
        if (!Source.api.equals(wish.getSource()) || cc != null && 0 < cc.size()) {
            boolean isNewWish = wish.getCreationDate().getTime() == wish.getModificationDate().getTime();
            Locale locale = owner.getLocale();


            // Send a notification to the Owner
            if (!Source.api.equals(wish.getSource())) {
                MessageGenerator msgGen = new MessageGenerator(wish.getSource(), wish.getHashTags(), locale);
                msgGen.
                    put("wish>owner>name", owner.getName()).
                    fetch(wish).
                    fetch(location, "wish").
                    fetch(influencer).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                    put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                String cancelWish = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_wish_cancel", msgGen.getParameters(), locale);
                String updateWish = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_wish_update", msgGen.getParameters(), locale);
                String subject = null;
                if (Source.mail.equals(msgGen.getCommunicationChannel())) {
                    subject = rawCommand.getSubject();
                }
                if (subject == null) {
                    subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                }
                subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                msgGen.
                    put("command>threadSubject", BaseConnector.prepareMailToSubject(subject)).
                    put("command>cancelWish", BaseConnector.prepareMailToBody(cancelWish)).
                    put("command>updateWish", BaseConnector.prepareMailToBody(updateWish));

                String message = msgGen.getMessage(isNewWish ? MessageId.WISH_CREATION_OK_TO_CONSUMER: MessageId.WISH_UPDATE_OK_TO_CONSUMER);

                communicateToConsumer(
                        msgGen.getCommunicationChannel(),
                        subject,
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
                            msgGen = new MessageGenerator(source, wish.getHashTags(), locale);
                            msgGen.
                                put("wish>owner>name", owner.getName()).
                                fetch(wish).
                                fetch(location, "wish").
                                put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                            message = msgGen.getMessage(isNewWish ? MessageId.WISH_CREATION_OK_TO_CCED: MessageId.WISH_UPDATE_OK_TO_CCED);
                        }

                        if (subject == null) {
                            Map<String, Object> cmdPrm = new HashMap<String, Object>();
                            cmdPrm.put("wish>key", wish.getKey());
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
