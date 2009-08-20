package com.twetailer.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterUtils {
    private static final Logger log = Logger.getLogger(TwitterUtils.class.getName());

    private static boolean isDebugMode = false;
    private static boolean isDevelopmentMode = false;
    
    private static String twetailerScreenName = "twtlr";
    private static String twetailerPassword = "twetailer@shortcut0";
    
    private static String robotScreenName = "jacktroll";
    private static String robotPassword = "twetailer@robot1";

    public static String getTwetailerScreenName() {
        return twetailerScreenName;
    }

    protected static void setTwetailerScreenName(String screenName) {
        twetailerScreenName = screenName;
    }

    public static String getTwetailerPassword() {
        return twetailerPassword;
    }

    protected static void setTwetailerPassword(String password) {
        twetailerPassword = password;
    }

    public static String getRobotScreenName() {
        return robotScreenName;
    }

    public static void setRobotScreenName(String screenName) {
        robotScreenName = screenName;
    }

    public static String getRobotPassword() {
        return robotPassword;
    }

    public static void setRobotPassword(String password) {
        robotPassword = password;
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
            return new Twitter(TwitterUtils.getTwetailerScreenName(), TwitterUtils.getTwetailerPassword());
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
            return new Twitter(TwitterUtils.getRobotScreenName(), TwitterUtils.getRobotPassword());
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
     * Use the Twetailer account to send public message (to update Twetailer public status)
     * @param message message to be tweeted
     * @throws TwitterException If the message submission fails
     */
    public static void sendPublicMessage(String message) throws TwitterException {
        sendPublicMessage(getTwetailerAccount(), message);
    }
    
    /**
     * Use the given account to send public message
     * @param account identifies the message sender
     * @param message message to be tweeted
     * @throws TwitterException If the message submission fails
     */
    protected static void sendPublicMessage(Twitter account, String message) throws TwitterException {
        try {
            account.updateStatus(message);
        }
        finally {
            releaseTwetailerAccount(account);
        }
    }
    
    /**
     * Use the Twetailer account to send a Direct Message to the identified recipient
     * @param recipientScreenName identifier of the recipient
     * @param message message to be tweeted
     * @throws TwitterException If the message submission fails
     */
    public static void sendDirectMessage(String recipientScreenName, String message) throws TwitterException {
        sendDirectMessage(getTwetailerAccount(), recipientScreenName, message);
    }
    
    /**
     * Use the Twetailer account to send a Direct Message to the identified recipient
     * @param account identifies the message sender
     * @param recipientScreenName identifier of the recipient
     * @param message message to be tweeted
     * @throws TwitterException If the message submission fails
     */
    protected static void sendDirectMessage(Twitter account, String recipientScreenName, String message) throws TwitterException {
        try {
            if (isDevelopmentMode) {
                log.info("Before sending a DM to " + recipientScreenName + ": " + message);
            }
            else if (isDebugMode) {
                log.fine("Before sending a DM to " + recipientScreenName + ": " + message);
                account.sendDirectMessage(recipientScreenName, message);
                log.fine("DM successfully sent!");
            }
            else {
                account.sendDirectMessage(recipientScreenName, message);
            }
        }
        finally {
            releaseTwetailerAccount(account);
        }
    }

    /**
     * Return the Direct Messages received to the Twetailer account, after the identified message 
     * @param sinceId identifier of the last processed Direct Message
     * @return List of Direct Messages not yet processed-can be empty
     * @throws TwitterException If the message retrieval fails
     */
    public static List<DirectMessage> getDirectMessages(long sinceId) throws TwitterException {
        return getDirectMessages(getTwetailerAccount(), sinceId);
    }

    /**
     * Return the Direct Messages received to the Twetailer account, after the identified message 
     * @param account identifies the message sender
     * @param sinceId identifier of the last processed Direct Message
     * @return List of Direct Messages not yet processed-can be empty
     * @throws TwitterException If the message retrieval fails
     */
    public static List<DirectMessage> getDirectMessages(Twitter account, long sinceId) throws TwitterException {
        try {
            log.warning("Before getting new direct messages from Twitter, after the message id: " + sinceId);
            return account.getDirectMessages(new Paging(1, 2, sinceId)); // FIXME: remove the limitation of 2 DMs retrieved at a time
        }
        finally {
            releaseTwetailerAccount(account);
        }
    }
}