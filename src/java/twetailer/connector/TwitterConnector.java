package twetailer.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.AccessToken;
import twitter4j.http.OAuthAuthorization;

public class TwitterConnector {
    private static Logger log = Logger.getLogger(TwitterConnector.class.getName());

    /* Information for basic authentication -- to be depreciated on June 30, 2010
     * Username: twetailer
     * Password: 6GmeM3FJzPOy-zNJ
     */
    public static final String TWETAILER_TWITTER_SCREEN_NAME = "twetailer";
    // private static final String TWETAILER_TWITTER_PASSWORD = "6GmeM3FJzPOy-zNJ";

    /* Information for OAuth authentication
     * Consumer key:          Kxo0DAP1ImQ1PV1pAAZkQ
     * Consumer secret:       8Ia1q1Eag6VZ0JoW2XT0APDwvxMMLmY1WAkBf32Y9o
     *
     * Request token key:     Twitter instance dependent
     * Request token secret:  Twitter instance dependent
     *
     * Access token key:      18082942-4TqNSEbiRc2kQs8EeYT2nJvQ4aVdPyQDVwar4ldf4
     * Access token secret:   GJsExv4iVdjmNsJS4uq6WSvyZbm9K9GuN7vckaq1Xk
     */
    public static final String TWETAILER_TWITTER_CONSUMER_KEY = "Kxo0DAP1ImQ1PV1pAAZkQ";
    public static final String TWETAILER_TWITTER_CONSUMER_SECRET = "8Ia1q1Eag6VZ0JoW2XT0APDwvxMMLmY1WAkBf32Y9o";

    public static final String TWETAILER_TWITTER_ACCESS_KEY = "18082942-4TqNSEbiRc2kQs8EeYT2nJvQ4aVdPyQDVwar4ldf4";
    public static final String TWETAILER_TWITTER_ACCESS_SECRET = "GJsExv4iVdjmNsJS4uq6WSvyZbm9K9GuN7vckaq1Xk";

    private static List<Twitter> _twetailerAccounts = new ArrayList<Twitter>();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Accessor provided for unit tests
     * @return Twitter account controller
     *
     * @see TwitterUtils#releaseTwetailerAccount(Twitter)
     */
    public static Twitter getTwetailerAccount() {
        int size = _twetailerAccounts.size();
        if (size == 0) {
            // Depreciated: return new Twitter(TWETAILER_TWITTER_SCREEN_NAME, TWETAILER_TWITTER_PASSWORD);
            // Depreciated: return new TwitterFactory().getInstance(TWETAILER_TWITTER_SCREEN_NAME, TWETAILER_TWITTER_PASSWORD);
            return new TwitterFactory().getInstance(
                    new OAuthAuthorization(
                            ConfigurationContext.getInstance(),
                            TwitterConnector.TWETAILER_TWITTER_CONSUMER_KEY,
                            TwitterConnector.TWETAILER_TWITTER_CONSUMER_SECRET,
                            new AccessToken(
                                    TwitterConnector.TWETAILER_TWITTER_ACCESS_KEY,
                                    TwitterConnector.TWETAILER_TWITTER_ACCESS_SECRET
                            )
                    )
            );
        }
        return _twetailerAccounts.remove(size - 1);
    }

    /**
     * Allow to return the Twitter account object to the pool
     *
     * @see TwitterUtils#getTwetailerAccount(Twitter)
     */
    public static void releaseTwetailerAccount(Twitter account) {
        _twetailerAccounts.add(account);
    }

    /**
     * Accessor provided for the unit tests
     */
    protected static void resetAccountLists() {
        _twetailerAccounts.clear();
    }

    /**
     * Use the Twetailer account to send public message (to update Twetailer public status)
     *
     * @param message message to be tweeted
     * @return Status of the operation
     *
     * @throws TwitterException If the message submission fails
     *
     * @see TwitterUtils#sendPublicMessage(Twitter, String)
     */
    public static Status sendPublicMessage(String message) throws TwitterException {
        Twitter account = getTwetailerAccount();
        try {
            return sendPublicMessage(account, message);
        }
        finally {
            releaseTwetailerAccount(account);
        }
    }

    /**
     * Use the given account to send public message
     *
     * @param account identifies the message sender
     * @param message message to be tweeted
     * @return Status of the operation
     *
     * @throws TwitterException If the message submission fails
     */
    protected static Status sendPublicMessage(Twitter account, String message) throws TwitterException {
        return account.updateStatus(message);
    }

    /**
     * Use the Twetailer account to send a Direct Message to the identified recipient
     *
     * @param recipientScreenName identifier of the recipient
     * @param message message to be tweeted
     * @return Corresponding DirectMessage instance
     *
     * @throws TwitterException If the message submission fails
     *
     * @see TwitterUtils#sendDirectMessage(Twitter, String, String)
     */
    public static DirectMessage sendDirectMessage(String recipientScreenName, String message) throws TwitterException {
        Twitter account = getTwetailerAccount();
        try {
            return sendDirectMessage(account, recipientScreenName, message);
        }
        finally {
            releaseTwetailerAccount(account);
        }
    }

    /**
     * Use the Twetailer account to send a Direct Message to the identified recipient
     *
     * @param account identifies the message sender
     * @param recipientScreenName identifier of the recipient
     * @param message message to be tweeted
     * @return Corresponding DirectMessage instance
     *
     * @throws TwitterException If the message submission fails
     */
    public static DirectMessage sendDirectMessage(Twitter account, String recipientScreenName, String message) throws TwitterException {
        log.fine("Before sending a DM to " + recipientScreenName + ": " + message);
        return account.sendDirectMessage(recipientScreenName, message);
    }

    /**
     * Return the Direct Messages received to the Twetailer account, after the identified message
     *
     * @param sinceId identifier of the last processed Direct Message
     * @return List of Direct Messages not yet processed-can be empty
     *
     * @throws TwitterException If the message retrieval fails
     *
     * @see TwitterUtils#getDirectMessages(Twitter, Long)
     */
    public static List<DirectMessage> getDirectMessages(long sinceId) throws TwitterException {
        Twitter account = getTwetailerAccount();
        try {
            return getDirectMessages(account, sinceId);
        }
        finally {
            releaseTwetailerAccount(account);
        }
    }

    /**
     * Return the Direct Messages received to the Twetailer account, after the identified message
     *
     * @param account identifies the message sender
     * @param sinceId identifier of the last processed Direct Message
     * @return List of Direct Messages not yet processed-can be empty
     *
     * @throws TwitterException If the message retrieval fails
     */
    public static List<DirectMessage> getDirectMessages(Twitter account, long sinceId) throws TwitterException {
        log.warning("Before getting new direct messages from Twitter, after the message id: " + sinceId);
        return account.getDirectMessages(new Paging(1, sinceId));
    }
}
