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

/**
 * Definition of the methods specific to communication via Twitter
 *
 * @author Dom Derrien
 */
public class TwitterConnector {

    private static Logger log = Logger.getLogger(TwitterConnector.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    /* Information for basic authentication -- to be depreciated on June 30, 2010
     * Username: twetailer
     * Password: 6GmeM3FJzPOy-zNJ
     */
    // public static final String TWETAILER_USER_SCREEN_NAME = "twetailer";
    // private static final String TWETAILER_USER_PASSWORD = "6GmeM3FJzPOy-zNJ";

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
    // public static final String TWETAILER_USER_KEY = "Kxo0DAP1ImQ1PV1pAAZkQ";
    // public static final String TWETAILER_USER_SECRET = "8Ia1q1Eag6VZ0JoW2XT0APDwvxMMLmY1WAkBf32Y9o";

    // public static final String TWETAILER_ACCESS_KEY = "18082942-4TqNSEbiRc2kQs8EeYT2nJvQ4aVdPyQDVwar4ldf4";
    // public static final String TWETAILER_ACCESS_SECRET = "GJsExv4iVdjmNsJS4uq6WSvyZbm9K9GuN7vckaq1Xk";

    /* Information for basic authentication
     * Username: ASEconomy
     * Password: s4kprgX2QFp7SQ
     */
    public static final String ASE_PUBLIC_USER_SCREEN_NAME = "ASEconomy";
    // private static final String ASE_PUBLIC_USER_PASSWORD = "s4kprgX2QFp7SQ";

    /* Information for OAuth authentication
     * Consumer key:          Yiyh4b0BJpAGcEP8f08ug
     * Consumer secret:       S4lA7EN0AwneFSDuGGGBuSxd8kfDvxNhQ2REoljZs
     *
     * Request token key:     Twitter instance dependent
     * Request token secret:  Twitter instance dependent
     *
     * Access token key:      190003819-0BKtTpmBLd9CzODopwEltO4RO177OTlhm5eOaDE
     * Access token secret:   5xRkfJeoCQrwrNez8UWIUJWFZUXAYwfLx5iwpqK7Mw
     */
    public static final String ASE_PUBLIC_USER_KEY = "Yiyh4b0BJpAGcEP8f08ug";
    public static final String ASE_PUBLIC_USER_SECRET = "S4lA7EN0AwneFSDuGGGBuSxd8kfDvxNhQ2REoljZs";

    public static final String ASE_PUBLIC_ACCESS_KEY = "190003819-0BKtTpmBLd9CzODopwEltO4RO177OTlhm5eOaDE";
    public static final String ASE_PUBLIC_ACCESS_SECRET = "5xRkfJeoCQrwrNez8UWIUJWFZUXAYwfLx5iwpqK7Mw";

    /* Information for basic authentication -- to be depreciated on June 30, 2010
     * Username: ASEconomyHub
     * Password: 6j2rwEzqrHFQKi
     */
    public static final String ASE_HUB_USER_SCREEN_NAME = "ASEconomyHub";
    // private static final String ASE_HUB_USER_PASSWORD = "6j2rwEzqrHFQKi";

    /* Information for OAuth authentication
     * Consumer key:          Yiyh4b0BJpAGcEP8f08ug
     * Consumer secret:       S4lA7EN0AwneFSDuGGGBuSxd8kfDvxNhQ2REoljZs
     *
     * Request token key:     Twitter instance dependent
     * Request token secret:  Twitter instance dependent
     *
     * Access token key:      190053886-Gum2P28u55Mz4QG5P8up6RizzMYmkRuUvO7shJKs
     * Access token secret:   oWb6781Tg9sAucrvBV5KLVSTSFmpd7RCkejX0rgNU8
     */
    public static final String ASE_HUB_USER_KEY = "Yiyh4b0BJpAGcEP8f08ug";
    public static final String ASE_HUB_USER_SECRET = "S4lA7EN0AwneFSDuGGGBuSxd8kfDvxNhQ2REoljZs";

    public static final String ASE_HUB_ACCESS_KEY = "190053886-Gum2P28u55Mz4QG5P8up6RizzMYmkRuUvO7shJKs";
    public static final String ASE_HUB_ACCESS_SECRET = "oWb6781Tg9sAucrvBV5KLVSTSFmpd7RCkejX0rgNU8";

    private static List<Twitter> _aseHubAccounts = new ArrayList<Twitter>();
    private static List<Twitter> _asePublicAccounts = new ArrayList<Twitter>();

    /**
     * Accessor provided for unit tests
     * @return Twitter account controller
     *
     * @see TwitterUtils#releaseAseHubAccount(Twitter)
     */
    @SuppressWarnings("deprecation")
    public static Twitter getAseHubAccount() {
        int size = _aseHubAccounts.size();
        if (size == 0) {
            return new TwitterFactory().getInstance(
                    new OAuthAuthorization(
                            ConfigurationContext.getInstance(),
                            TwitterConnector.ASE_HUB_USER_KEY,
                            TwitterConnector.ASE_HUB_USER_SECRET,
                            new AccessToken(
                                    TwitterConnector.ASE_HUB_ACCESS_KEY,
                                    TwitterConnector.ASE_HUB_ACCESS_SECRET
                            )
                    )
            );
        }
        return _aseHubAccounts.remove(size - 1);
    }

    /**
     * Accessor provided for unit tests
     * @return Twitter account controller
     *
     * @see TwitterUtils#releaseAseHubAccount(Twitter)
     */
    @SuppressWarnings("deprecation")
    public static Twitter getAsePublicAccount() {
        int size = _asePublicAccounts.size();
        if (size == 0) {
            return new TwitterFactory().getInstance(
                    new OAuthAuthorization(
                            ConfigurationContext.getInstance(),
                            TwitterConnector.ASE_PUBLIC_USER_KEY,
                            TwitterConnector.ASE_PUBLIC_USER_SECRET,
                            new AccessToken(
                                    TwitterConnector.ASE_PUBLIC_ACCESS_KEY,
                                    TwitterConnector.ASE_PUBLIC_ACCESS_SECRET
                            )
                    )
            );
        }
        return _asePublicAccounts.remove(size - 1);
    }

    /**
     * Allow to return the Twitter account object to the pool
     *
     * @see TwitterUtils#getTwetailerAccount(Twitter)
     */
    public static void releaseAseHubAccount(Twitter account) {
        _aseHubAccounts.add(account);
    }

    /**
     * Allow to return the Twitter account object to the pool
     *
     * @see TwitterUtils#getTwetailerAccount(Twitter)
     */
    public static void releaseAsePublicAccount(Twitter account) {
        _asePublicAccounts.add(account);
    }

    /**
     * Accessor provided for the unit tests
     */
    protected static void resetAseHubAccountLists() {
        _aseHubAccounts.clear();
    }

    /**
     * Accessor provided for the unit tests
     */
    protected static void resetAsePublicAccountLists() {
        _asePublicAccounts.clear();
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
        Twitter account = getAsePublicAccount();
        try {
            return sendPublicMessage(account, message);
        }
        finally {
            releaseAsePublicAccount(account);
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
        Twitter account = getAseHubAccount();
        try {
            return sendDirectMessage(account, recipientScreenName, message);
        }
        finally {
            releaseAseHubAccount(account);
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
    protected static DirectMessage sendDirectMessage(Twitter account, String recipientScreenName, String message) throws TwitterException {
        getLogger().fine("Before sending a DM to " + recipientScreenName + ": " + message);
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
        Twitter account = getAseHubAccount();
        try {
            return getDirectMessages(account, sinceId);
        }
        finally {
            releaseAseHubAccount(account);
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
    protected static List<DirectMessage> getDirectMessages(Twitter account, long sinceId) throws TwitterException {
        getLogger().warning("Before getting new direct messages from Twitter, after the message id: " + sinceId);
        return account.getDirectMessages(new Paging(1, sinceId));
    }
}
