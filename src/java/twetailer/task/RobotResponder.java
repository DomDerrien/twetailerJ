package twetailer.task;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.connector.TwitterConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;

public class RobotResponder {

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static StoreOperations storeOperations = _baseOperations.getStoreOperations();
    protected static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    public static Long processDirectMessages() throws TwitterException, DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            Settings settings = settingsOperations.getSettings(pm);
            Long sinceId = settings.getLastRobotDirectMessageId();
            Long lastId = processDirectMessages(pm, sinceId);
            if (!lastId.equals(sinceId)) {
                settings.setLastRobotDirectMessageId(lastId);
                settingsOperations.updateSettings(pm, settings);
            }
            return lastId;
        }
        finally {
            pm.close();
        }
    }
    public static Long processDirectMessages(PersistenceManager pm, Long sinceId) throws TwitterException, DataSourceException {
        Long lastId = sinceId;
        Twitter robotAccount = TwitterConnector.getRobotAccount();
        List<DirectMessage> messages = TwitterConnector.getDirectMessages(robotAccount, sinceId);
        int idx = messages == null ? 0 : messages.size(); // To start by the end of the message queue
        while (0 < idx) {
            --idx;
            DirectMessage message = messages.get(idx);
            String text = message.getText();
            long dmId = message.getId();

            // Get the demand reference
            Pattern pattern = Pattern.compile("reference:(\\d+)");
            Matcher matcher = pattern.matcher(text);
            matcher.find();
            Long reference = Long.valueOf(matcher.group(1));
            // Get the tags
            pattern = Pattern.compile("tags:((?:\\s*\\w+\\s*)+)\\?");
            matcher = pattern.matcher(text);
            matcher.find();
            String tags = matcher.group(1);
            // Get the robot storeKey
            List<Store> stores = storeOperations.getStores(pm, Store.NAME, "Toys Factory", 1);
            Long storeKey = stores.size() == 0 ? 0L : stores.get(0).getKey();
            // Replies with the generic proposal
            TwitterConnector.sendDirectMessage(
                    robotAccount,
                    message.getSenderScreenName(),
                    LabelExtractor.get(
                            "robot_sendDefaultProposal",
                            new Object[] { reference, tags, storeKey },
                            Locale.ENGLISH
                    )
            );

            if (lastId < dmId) {
                lastId = dmId;
            }
        }
        return lastId;
    }
}
