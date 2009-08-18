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

    private static List<Twitter> _twitterAccounts = new ArrayList<Twitter>();

    /**
     * Accessor provided for unit tests
     * @return Twitter account controller
     * 
     * @see TwitterUtils#releaseTwitterAccount(Twitter)
     */
    public synchronized static Twitter getTwitterAccount() {
        int size = _twitterAccounts.size();
        if (size == 0) {
            return new Twitter(TwitterUtils.getTwetailerScreenName(), TwitterUtils.getTwetailerPassword());
        }
        return _twitterAccounts.remove(size - 1);
    }
    

    /**
     * Allow to return the Twitter account object to the pool
     * 
     * @see TwitterUtils#getTwitterAccount(Twitter)
     */
    public static void releaseTwitterAccount(Twitter account) {
        _twitterAccounts.add(account);
    }
    
    /**
     * Use the Twetailer account to send public message (to update Twetailer public status)
     * @param message message to be tweeted
     * @throws TwitterException If the message submission fails
     */
    public static void sendPublicMessage(String message) throws TwitterException {
        Twitter twitterAccount = getTwitterAccount();
        try {
            twitterAccount.updateStatus(message);
        }
        finally {
            releaseTwitterAccount(twitterAccount);
        }
    }
    
    /**
     * Use the Twetailer account to send a Direct Message to the identified recipient
     * @param recipientScreenName identifier of the recipient
     * @param message message to be tweeted
     * @throws TwitterException If the message submission fails
     */
    public static void sendDirectMessage(String recipientScreenName, String message) throws TwitterException {
        Twitter twitterAccount = getTwitterAccount();
        try {
            if (isDevelopmentMode) {
                log.info("Before sending a DM to " + recipientScreenName + ": " + message);
            }
            else if (isDebugMode) {
                log.fine("Before sending a DM to " + recipientScreenName + ": " + message);
                twitterAccount.sendDirectMessage(recipientScreenName, message);
                log.fine("DM successfully sent!");
            }
            else {
                twitterAccount.sendDirectMessage(recipientScreenName, message);
            }
        }
        finally {
            releaseTwitterAccount(twitterAccount);
        }
    }

    /**
     * Return the Direct Messages received to the Twetailer account, after the identified message 
     * @param sinceId identifier of the last processed Direct Message
     * @return List of Direct Messages not yet processed-can be empty
     * @throws TwitterException If the message retrieval fails
     */
    public static List<DirectMessage> getDirectMessages(long sinceId) throws TwitterException {
        Twitter twitterAccount = getTwitterAccount();
        try {
            log.warning("Before getting new direct messages from Twitter, after the message id: " + sinceId);
            return twitterAccount.getDirectMessages(new Paging(1, 2, sinceId)); // FIXME: remove the limitation of 2 DMs retrieved at a time
        }
        finally {
            releaseTwitterAccount(twitterAccount);
        }
    }
}