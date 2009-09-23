package twetailer.connector;

import java.util.ArrayList;
import java.util.List;

import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.Retailer;
import twitter4j.TwitterException;

public class BaseConnector {
    public enum Source {
        simulated,
        twitter,
        jabber,
        facebook,
        imap
    }

    public static void communicateToEmitter(RawCommand rawCommand, String message) throws DataSourceException {
        communicateToUser(rawCommand.getSource(), rawCommand.getEmitterId(), rawCommand.getEmitterId(), rawCommand.getEmitterId(), message);
    }

    public static void communicateToConsumer(Source source, Consumer consumer, String message) throws DataSourceException {
        communicateToUser(source, consumer.getTwitterId(), consumer.getJabberId(), null, message);
    }

    public static void communicateToRetailer(Source source, Retailer consumer, String message) throws DataSourceException {
        communicateToUser(source, consumer.getTwitterId(), consumer.getJabberId(), null, message);
    }

    private static List<String> lastCommunications = new ArrayList<String>();

    protected static void communicateToUser(Source source, String twitterId, String jabberId, String facebookId, String message) throws DataSourceException {
        if (Source.simulated.equals(source)) {
            lastCommunications.add(message);
        }
        else if (Source.twitter.equals(source)) {
            try {
                TwitterConnector.sendDirectMessage(twitterId, message);
            }
            catch (TwitterException ex) {
                throw new DataSourceException("Cannot communicate with Twitter to the consumer: " + twitterId);
            }
        }
        else if (Source.jabber.equals(source)) {
            JabberConnector.sendInstantMessage(jabberId, message);
        }
        else if (Source.facebook.equals(source)) {
            throw new RuntimeException("Not yet implemented");
        }
        else {
            throw new DataSourceException("Provider " + source + " not yet supported");
        }
    }

    public static void resetLastCommunicationInSimulatedMode() {
        lastCommunications.clear();
    }

    public static String getLastCommunicationInSimulatedMode() {
        if (lastCommunications.isEmpty()) {
            return null;
        }
        return lastCommunications.get(lastCommunications.size() - 1);
    }

    public static String getCommunicationForRetroIndexInSimulatedMode(int retroIndex) {
        if (lastCommunications.isEmpty() || lastCommunications.size() <= retroIndex) {
            return null;
        }
        return lastCommunications.get(lastCommunications.size() - 1 - retroIndex);
    }
}
