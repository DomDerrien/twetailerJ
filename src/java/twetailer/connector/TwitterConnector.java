package twetailer.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterConnector {
    private static final Logger log = Logger.getLogger(TwitterConnector.class.getName());

    private static String twetailerScreenName = "twetailer";
    private static String twetailerPassword = "ec0n0m1cs";

    private static String robotScreenName = "jacktroll";
    private static String robotPassword = "twetailer@robot1";

    public static String getTwetailerScreenName() {
        return twetailerScreenName;
    }

    private static List<Twitter> _twetailerAccounts = new ArrayList<Twitter>();

    /**
     * Accessor provided for unit tests
     * @return Twitter account controller
     *
     * @see TwitterUtils#releaseTwetailerAccount(Twitter)
     */
    public synchronized static Twitter getTwetailerAccount() {
        int size = _twetailerAccounts.size();
        if (size == 0) {
            return new Twitter(twetailerScreenName, twetailerPassword);
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

    private static List<Twitter> _robotAccounts = new ArrayList<Twitter>();

    /**
     * Accessor provided for unit tests
     * @return Twitter account controller
     *
     * @see TwitterUtils#releaseRobotAccount(Twitter)
     */
    public synchronized static Twitter getRobotAccount() {
        int size = _robotAccounts.size();
        if (size == 0) {
            return new Twitter(robotScreenName, robotPassword);
        }
        return _robotAccounts.remove(size - 1);
    }


    /**
     * Allow to return the Twitter account object to the pool
     *
     * @see TwitterUtils#getRobotAccount(Twitter)
     */
    public static void releaseRobotAccount(Twitter account) {
        _robotAccounts.add(account);
    }

    /**
     * Accessor provided for the unit tests
     */
    protected static void resetAccountLists() {
        _twetailerAccounts.clear();
        _robotAccounts.clear();
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
