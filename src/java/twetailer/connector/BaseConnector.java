package twetailer.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import twetailer.ClientException;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twitter4j.TwitterException;

public class BaseConnector {
    private static Logger log = Logger.getLogger(BaseConnector.class.getName());

    public enum Source {
        simulated,
        twitter,
        jabber,
        facebook,
        mail
    }

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Send the specified message to the RawCommand emitter, using the same communication channel
     *
     * @param rawCommand Command as received by the system, from an IM, in a tweet, an e-mail, etc.
     * @param message Message to send back
     * @param locale recipient's locale
     *
     * @throws ClientException If the communication fails
     */
    public static void communicateToEmitter(RawCommand rawCommand, String message, Locale locale) throws ClientException {
        communicateToUser(rawCommand.getSource(), rawCommand.getEmitterId(), null, rawCommand.getSubject(), message, locale);
    }

    /**
     * Send the specified message to the identified consumer, using the suggested communication channel.
     * If the suggested communication fails, the System can try to use another channel if the Consumer profile contains alternatives.
     *
     * @param rawCommand Message triggering this response
     * @param consumer targeted user
     * @param message Message to send back
     *
     * @throws ClientException If all communication attempts fail
     */
    public static void communicateToConsumer(RawCommand rawCommand, Consumer consumer, String message) throws ClientException {
        // TODO: implement the fallback mechanism
        Source source = rawCommand.getSource();
        String userId =
            Source.twitter.equals(source) ? consumer.getTwitterId() :
                Source.jabber.equals(source) ? consumer.getJabberId() :
                    Source.mail.equals(source) ? consumer.getEmail() :
                        null;
        String userName = consumer.getName();
        if (userId != null || Source.simulated.equals(source)) {
            communicateToUser(source, userId, userName, rawCommand.getSubject(), message, consumer.getLocale());
        }
    }

    /**
     * Send the specified message to the identified sale associate, using the suggested communication channel.
     * If the suggested communication fails, the System can try to use another channel if the SaleAssociate profile contains alternatives.
     *
     * @param rawCommand Message triggering this response
     * @param saleAssociate targeted user
     * @param message Message to send back
     *
     * @throws ClientException If all communication attempts fail
     */
    public static void communicateToSaleAssociate(RawCommand rawCommand, SaleAssociate saleAssociate, String message) throws ClientException {
        // TODO: implement the fallback mechanism
        Source source = rawCommand.getSource();
        String userId =
            Source.twitter.equals(source) ? saleAssociate.getTwitterId() :
                Source.jabber.equals(source) ? saleAssociate.getJabberId() :
                    Source.mail.equals(source) ? saleAssociate.getEmail() :
                        null;
        String userName = saleAssociate.getName();
        if (userId != null || Source.simulated.equals(source)) {
            communicateToUser(source, userId, userName, rawCommand.getSubject(), message, saleAssociate.getLocale());
        }
    }

    /** Buffer for the last messages sent with the source being set to <code>Source.simulated</code>, made available only for test purposes */
    protected static List<String> lastCommunications = new ArrayList<String>();

    /**
     * Send the specified message to the identified sale associate, using the suggested communication channel.
     * If the suggested communication fails, the System can try to use another channel if the SaleAssociate profile contains alternatives.
     *
     * @param source Identifier of the suggested communication channel
     * @param userId User identifier (can be Jabber ID, Twitter screen name, etc.)
     * @param userName User display name
     * @param subject TODO
     * @param message Message to send back
     * @param locale recipient's locale
     * @throws ClientException If all communication attempts fail
     */
    protected static void communicateToUser(Source source, String userId, String userName, String subject, String message, Locale locale) throws ClientException {
        log.warning("Communicating with " + userId + " (medium: " + (source == null ? "null" : source.toString()) + ") -- message: " + message);
        if (Source.simulated.equals(source)) {
            lastCommunications.add(message);
        }
        else if (Source.twitter.equals(source)) {
            try {
                List<String> messageParts = checkMessageLength(message, 140);
                for (String part: messageParts) {
                    TwitterConnector.sendDirectMessage(userId, part);
                }
            }
            catch (TwitterException ex) {
                throw new ClientException("Cannot communicate with Twitter to the consumer: " + userId, ex);
            }
        }
        else if (Source.jabber.equals(source)) {
            List<String> messageParts = checkMessageLength(message, 512);
            for (String part: messageParts) {
                JabberConnector.sendInstantMessage(userId, part);
            }
        }
        else if (Source.mail.equals(source)) {
            try {
                MailConnector.sendMailMessage(userId, userName, subject, message, locale);
            }
            catch(Exception ex) {
                throw new ClientException("Cannot communicate by E-mail to the consumer: " + userId, ex);
            }
        }
        else if (Source.facebook.equals(source)) {
            throw new RuntimeException("Not yet implemented");
        }
        else {
            throw new ClientException("Provider " + source + " not yet supported");
        }
    }

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

    /** Minimal size of the messages to be sent */
    public static final int MINIMAL_MESSAGE_LENGTH = 8;

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
            message = message.trim();
            int separatorIdx = message.indexOf(SUGGESTED_MESSAGE_SEPARATOR);
            while (0 < message.length()) {
                String head = separatorIdx == -1 ? message : message.substring(0, separatorIdx);
                if (limit <= head.length()) {
                    int endIdx = limit;
                    while (head.charAt(endIdx) != ' ' && head.charAt(endIdx) != '\t') {
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
