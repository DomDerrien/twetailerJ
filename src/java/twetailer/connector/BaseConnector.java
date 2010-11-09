package twetailer.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import twetailer.CommunicationException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twitter4j.TwitterException;

/**
 * Definition of general purpose methods to communicate with Twetailer users
 * over the various connectors
 *
 * @author Dom Derrien
 */
public class BaseConnector {
    private static Logger log = Logger.getLogger(BaseConnector.class.getName());

    public enum Source {
        simulated,
        robot,
        twitter,
        jabber,
        facebook,
        mail,
        api,
        widget
    }

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Send the specified message to the RawCommand emitter, using the same communication channel
     *
     * @param source (Optional) Type of communication channel to use
     * @param coordinate Identifier of a CC-ed contact
     * @param message Text to forward to the CC-ed contact
     * @param locale recipient's locale (expects it's good for the CC-ed contact too)
     *
     * @param e-mail subject, for thread-aware mail readers
     * @throws CommunicationException If the communication fails
     */
    public static void communicateToCCed(Source source, String coordinate, String subject, String message, Locale locale) throws CommunicationException {
        if (source == null) {
            source = getCCedCommunicationChannel(coordinate);
        }
        communicateToUser(source, true, coordinate, null, subject, new String[] { message }, locale);
    }

    /**
     * Determine the communication channel to use for the given coordinate
     *
     * @param coordinate Communication identifier (e-mail address, Twitter Id, etc.)
     * @return The communication channel identifier, with <code>Source.mail</code> as default
     *
     * @throws CommunicationException If the coordinate is invalid
     */
    public static Source getCCedCommunicationChannel(String coordinate) throws CommunicationException {
        if (coordinate == null || coordinate.length() < 2) {
            throw new CommunicationException("Invalid coordinate!");
        }
        int arobasIdx = coordinate.indexOf('@');
        if (arobasIdx == -1 || arobasIdx == 0) { // No arobas, or at the leading character
            return Source.twitter;
        }
        return Source.mail;
    }

    /**
     * Send the specified message to the RawCommand emitter, using the same communication channel
     *
     * @param rawCommand Command as received by the system, from an IM, in a tweet, an e-mail, etc.
     * @param messages Array of messages to send back
     * @param locale recipient's locale
     *
     * @throws CommunicationException If the communication fails
     */
    public static void communicateToEmitter(RawCommand rawCommand, String[] messages, Locale locale) throws CommunicationException {
        communicateToUser(rawCommand.getSource(), false, rawCommand.getEmitterId(), null, rawCommand.getSubject(), messages, locale);
    }

    /**
     * Send the specified message to the identified consumer, using the suggested communication channel.
     * If the suggested communication fails, the System can try to use another channel if the Consumer profile contains alternatives.
     *
     * @param source Identifier of the suggested communication channel
     * @param subject Subject of the discussion, will be prefixed by "Re:" for a message to owner, by "Fwd:" for a message to a CC'ed, by nothing when it's a notification
     * @param consumer targeted user
     * @param messages Array of messages to send back
     *
     * @throws CommunicationException If all communication attempts fail
     */
    public static void communicateToConsumer(Source source, String subject, Consumer consumer, String[] messages) throws CommunicationException {
        //
        // Pass "source" & "subject" as arguments in place of "rawCommand", especially because the rawCommand mainly convey the source!
        // Then the caller will have to rely decide which subject to send, in the user's locale and with possibly arguments related to the order
        //

        // TODO: implement the fall back mechanism
        String userId =
            Source.twitter.equals(source) ? consumer.getTwitterId() :
                Source.jabber.equals(source) ? consumer.getJabberId() :
                    Source.mail.equals(source) ? consumer.getEmail() :
                        Source.widget.equals(source) ? consumer.getEmail() :
                            null;
        String userName = consumer.getName();
        if (userId != null || Source.simulated.equals(source)) {
            communicateToUser(source, false, userId, userName, subject, messages, consumer.getLocale());
            MailConnector.sendCopyToAdmins(source, consumer, subject, messages);
        }
    }

    /** Buffer for the last messages sent with the source being set to <code>Source.simulated</code>, made available only for test purposes */
    protected static List<String> lastCommunications = new ArrayList<String>();

    /**
     * Send the specified message to the identified sale associate, using the suggested communication channel.
     * If the suggested communication fails, the System can try to use another channel if the SaleAssociate profile contains alternatives.
     *
     * @param source Identifier of the suggested communication channel
     * @param useCcAccount Indicates that the message should be sent from a CC account, which is going to ignore unexpected replies
     * @param userId User identifier (can be Jabber ID, Twitter screen name, etc.)
     * @param userName User display name
     * @param subject Subject of the discussion, will be prefixed by "Re:" for a message to owner, by "Fwd:" for a message to a CC'ed, by nothing when it's a notification
     * @param messages Array of messages to send back
     * @param locale recipient's locale
     *
     * @throws CommunicationException If all communication attempts fail
     */
    protected static void communicateToUser(Source source, boolean useCcAccount, String userId, String userName, String subject, String[] messages, Locale locale) throws CommunicationException {
        log.warning("Communicating with " + userId + " (medium: " + (source == null ? "null" : source.toString()) + ") -- message: " + Arrays.toString(messages));
        if (Source.simulated.equals(source)) {
            for (String message: messages) {
                lastCommunications.add(message);
            }
        }
        else if (Source.twitter.equals(source)) {
            try {
                for (String message: messages) {
                    List<String> messageParts = checkMessageLength(message, 140);
                    for (String part: messageParts) {
                        TwitterConnector.sendDirectMessage(userId, part);
                    }
                }
            }
            catch (TwitterException ex) {
                /*****
                try {
                    // FIXME: verify that the error is really related to a non following issue!
                    log.warning("Emitter" + userId + " not following Twetailer");
                    TwitterConnector.sendPublicMessage(LabelExtractor.get("tl_inform_dm_sender_no_more_a_follower", new Object[] { userId }, locale));
                }
                catch(TwitterException nestedEx) {
                *****/
                   throw new CommunicationException("Cannot communicate with Twitter to the consumer: " + userId, Source.twitter, ex);
                //// }
            }
        }
        else if (Source.jabber.equals(source)) {
            try {
                for (String message: messages) {
                    List<String> messageParts = checkMessageLength(message, 512);
                    for (String part: messageParts) {
                        JabberConnector.sendInstantMessage(userId, part);
                    }
                }
            }
            catch(Exception ex) {
                throw new CommunicationException("Cannot communicate by IM to the consumer: " + userId, Source.jabber, ex);
            }
        }
        else if (Source.mail.equals(source) || Source.widget.equals(source)) {
            try {
                StringBuilder mailMessage = new StringBuilder();
                for (String message: messages) {
                    List<String> messageParts = checkMessageLength(message, 16394);
                    for (String part: messageParts) {
                        mailMessage.append(part).append(MESSAGE_SEPARATOR);
                    }
                }
                MailConnector.sendMailMessage(useCcAccount, userId, userName, subject, mailMessage.toString(), locale);
            }
            catch(Exception ex) {
                throw new CommunicationException("Cannot communicate by E-mail to the consumer: " + userId, Source.mail, ex);
            }
        }
        else if (Source.facebook.equals(source)) {
            throw new RuntimeException("Not yet implemented");
        }
        else {
            throw new CommunicationException("Provider " + source + " not yet supported", source);
        }
    }

    public final static String MESSAGE_SEPARATOR = "\n";

    /** Provided only for test purpose, when communication are done with <code>Source.simulated</code> */
    public static void resetLastCommunicationInSimulatedMode() {
        lastCommunications.clear();
    }

    /** Provided only for test purpose, when communication are done with <code>Source.simulated</code> */
    public static String getLastCommunicationInSimulatedMode() {
        return getCommunicationForRetroIndexInSimulatedMode(0);
    }

    /** Provided only for test purpose, when communication are done with <code>Source.simulated</code> */
    public static String getCommunicationForRetroIndexInSimulatedMode(int retroIndex) {
        if (lastCommunications.isEmpty() || lastCommunications.size() <= retroIndex) {
            return null;
        }
        return lastCommunications.get(lastCommunications.size() - 1 - retroIndex);
    }

    /** If found into a message, this separator is used as a suggestion to break a message in many parts */
    public static final char SUGGESTED_MESSAGE_SEPARATOR = '|';
    public static final String SUGGESTED_MESSAGE_SEPARATOR_STR = "|";
    public static final String ESCAPED_SUGGESTED_MESSAGE_SEPARATOR_STR = "\\|";
    public static final String SENTENCE_SEPARATOR_STR = ".";
    public static final String MANY_SPACES_REGEXP = "\\s+";

    /** Minimal size of the messages to be sent */
    public static final int MINIMAL_MESSAGE_LENGTH = 8;

    private static final char SPACE_CHAR = ' ';
    private static final String SPACE_STR = Command.SPACE;

    /**
     * Split the message in many parts as suggested and when the different parts are larger than the specified limit.
     *
     * @param message Message to process
     * @param limit Size of part length not to exceed
     * @return Array of message parts extracted as suggested, or splitted because the initial parts were too long
     */
    protected static List<String> checkMessageLength(String message, int limit) {
        if (limit < MINIMAL_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Cannot accept the specified limit (" + limit + ") under the expected " + MINIMAL_MESSAGE_LENGTH + " characters...");
        }
        List<String> output = new ArrayList<String>();
        if (message != null) {
            // Initial conditions
            message =
                message.trim().
                replaceAll(MANY_SPACES_REGEXP, SPACE_STR).
                replaceAll(MANY_SPACES_REGEXP + "\\" + SENTENCE_SEPARATOR_STR, SENTENCE_SEPARATOR_STR).
                replaceAll(MANY_SPACES_REGEXP + "\\" + SUGGESTED_MESSAGE_SEPARATOR, SUGGESTED_MESSAGE_SEPARATOR_STR);
            int separatorIdx = message.indexOf(SUGGESTED_MESSAGE_SEPARATOR);
            while (0 < message.length()) {
                String head = separatorIdx == -1 ? message : message.substring(0, separatorIdx);
                if (limit < head.length()) {
                    int endIdx = limit;
                    while (head.charAt(endIdx) != SPACE_CHAR) { // All separators have been replaced by SPACE_STR!
                        -- endIdx;
                    }
                    head = head.substring(0, endIdx);
                }
                output.add(head.trim());
                // Variants
                message = message.substring(head.length() + (separatorIdx == -1 ? 0 : 1));
                separatorIdx = message.indexOf(SUGGESTED_MESSAGE_SEPARATOR);
            }
        }
        return output;
    }
}
