package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.connector.TwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.Settings;
import twetailer.validator.ApplicationSettings;
import twitter4j.DirectMessage;
import twitter4j.TwitterException;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;

public class TweetLoader {
    private static Logger log = Logger.getLogger(TweetLoader.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();
    protected static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
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
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            Settings settings = settingsOperations.getSettings(pm);
            Long sinceId = settings.getLastProcessDirectMessageId();
            Long lastId = loadDirectMessages(pm, sinceId);
            if (!lastId.equals(sinceId)) {
                settings.setLastProcessDirectMessageId(lastId);
                settings = settingsOperations.updateSettings(pm, settings);
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

    public static final String HARMFULL_D_TWETAILER_PREFIX = "d " + TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME;

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
    @SuppressWarnings("deprecation")
    protected static Long loadDirectMessages(PersistenceManager pm, Long sinceId) throws DataSourceException, TwitterException {
        long lastId = sinceId;

        // Get the list of direct messages
        List<DirectMessage> messages = TwitterConnector.getDirectMessages(sinceId);

        List<RawCommand> extractedCommands = new ArrayList<RawCommand>();
        Map<String, Boolean> nonFollowerIds = new HashMap<String, Boolean>();

        // Process each messages one-by-one
        int idx = messages == null ? 0 : messages.size(); // To start by the end of the message queue
        while (0 < idx) {
            --idx;
            DirectMessage dm = messages.get(idx);
            long dmId = dm.getId();
            String message = dm.getText();
            log.warning("DM id: " + dmId);
            log.warning("DM content: " + message);

            // Get Twetailer account and verify the user is a follower
            twitter4j.User sender = dm.getSender();
            String senderScreenName = sender.getScreenName();
            if (!nonFollowerIds.containsKey(senderScreenName)) {
                Consumer consumer = consumerOperations.createConsumer(pm, sender); // Creation only occurs if the corresponding Consumer instance is not retrieved
                log.warning("DM emitter: " + consumer.getTwitterId());
                Locale senderLocale = consumer.getLocale();
                if (!sender.isFollowing()) {
                    log.warning("Emitter" + consumer.getTwitterId() + " not following Twetailer");
                    TwitterConnector.sendPublicMessage(LabelExtractor.get("tl_inform_dm_sender_no_more_a_follower", new Object[] { senderScreenName }, senderLocale));
                    nonFollowerIds.put(senderScreenName, Boolean.TRUE);
                }
                // else {
                    RawCommand rawCommand = new RawCommand(Source.twitter);
                    rawCommand.setEmitterId(consumer.getTwitterId());
                    rawCommand.setMessageId(dmId);
                    rawCommand.setCommand(message);

                    extractedCommands.add(rawCommand);
                    log.warning("DM added to the queue for the RawCommand creation");
                // }
            }
            if (lastId < dmId) {
                lastId = dmId;
            }
        }

        // Create a task per command
        for(RawCommand rawCommand: extractedCommands) {
            rawCommand = rawCommandOperations.createRawCommand(pm, rawCommand);
            log.warning("RawCommand created: " + rawCommand.getKey().toString());

            Queue queue = _baseOperations.getQueue();
            log.warning("Preparing the task: /maezel/processCommand?key=" + rawCommand.getKey().toString());
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/processCommand").
                        param(Command.KEY, rawCommand.getKey().toString()).
                        method(Method.GET)
            );
            log.warning("Job add to the task queue");
        }

        return Long.valueOf(lastId);
    }
}
