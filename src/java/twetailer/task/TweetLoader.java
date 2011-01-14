package twetailer.task;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.connector.TwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.Settings;
import twetailer.task.step.BaseSteps;
import twitter4j.DirectMessage;
import twitter4j.TwitterException;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

/**
 * Define the <code>cron</code> job logic which load
 * the new direct messages (DM) sent to the Twetailer
 * account on Twitter.
 *
 * Each DM is used to create a RawCommand that is
 * scheduled for the task "/_tasks/processCommand".
 *
 * @author Dom Derrien
 */
public class TweetLoader {

    private static Logger log = Logger.getLogger(TweetLoader.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    /**
     * Extract commands from the pending Direct Messages and save them into the command table
     *
     * @return Updated direct message identifier if new DMs have been processed, or the given one if none has been processed
     *
     * @throws TwitterException
     * @throws DataSourceException
     */
    public static Long loadDirectMessages() {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Settings settings = BaseSteps.getSettingsOperations().getSettings(pm);
            Long sinceId = settings.getLastProcessDirectMessageId();
            Long lastId = loadDirectMessages(pm, sinceId);
            if (!lastId.equals(sinceId)) {
                settings.setLastProcessDirectMessageId(lastId);
                settings = BaseSteps.getSettingsOperations().updateSettings(pm, settings);
            }
            return lastId;
        }
        catch(DataSourceException ex) {
            ex.printStackTrace();
        }
        catch (TwitterException ex) {
            ex.printStackTrace();
        }
        finally {
            pm.close();
        }
        return -1L;
    }

    public static final String HARMFULL_D_TWETAILER_PREFIX = "d " + TwitterConnector.ASE_TWITTER_SCREEN_NAME.toLowerCase(Locale.ENGLISH);

    /**
     * Extract commands from the pending Direct Messages and save them into the command table
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param sinceId identifier of the last process direct message
     * @return Updated direct message identifier if new DMs have been processed, or the given one if none has been processed
     *
     * @throws TwitterException
     * @throws DataSourceException
     */
    protected static Long loadDirectMessages(PersistenceManager pm, Long sinceId) throws DataSourceException, TwitterException {
        long lastId = sinceId;

        // Get the list of direct messages
        List<DirectMessage> messages = TwitterConnector.getDirectMessages(sinceId);

        List<RawCommand> extractedCommands = new ArrayList<RawCommand>();

        // Process each messages one-by-one
        int idx = messages == null ? 0 : messages.size(); // To start by the end of the message queue
        while (0 < idx) {
            --idx;
            DirectMessage dm = messages.get(idx);
            long dmId = dm.getId();
            String message = dm.getText();
            getLogger().warning("DM id: " + dmId + " -- DM content: " + message);

            // Get Twetailer account
            twitter4j.User sender = dm.getSender();
            Consumer consumer = BaseSteps.getConsumerOperations().createConsumer(pm, sender); // Creation only occurs if the corresponding Consumer instance is not retrieved

            getLogger().warning("DM emitter: " + consumer.getTwitterId());
            RawCommand rawCommand = new RawCommand(Source.twitter);
            rawCommand.setCommandId(String.valueOf(dm.getId()));
            rawCommand.setEmitterId(consumer.getTwitterId());
            rawCommand.setMessageId(dmId);
            rawCommand.setCommand(message);

            extractedCommands.add(rawCommand);

            if (lastId < dmId) {
                lastId = dmId;
            }
        }

        // Create a task per command
        for(RawCommand rawCommand: extractedCommands) {
            rawCommand = BaseSteps.getRawCommandOperations().createRawCommand(pm, rawCommand);

            Queue queue = BaseSteps.getBaseOperations().getQueue();
            queue.add(
                    withUrl("/_tasks/processCommand").
                        param(Command.KEY, rawCommand.getKey().toString()).
                        method(Method.GET)
            );
        }

        return Long.valueOf(lastId);
    }
}
