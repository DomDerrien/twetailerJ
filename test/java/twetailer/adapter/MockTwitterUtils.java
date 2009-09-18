package twetailer.adapter;

import twitter4j.Twitter;

public class MockTwitterUtils extends TwitterUtils {

    public static void injectMockTwitterAccount(Twitter mockTwitterAccount) {
        // Inject the account into the pool
        TwitterUtils.releaseTwetailerAccount(mockTwitterAccount);
    }

    public static void injectMockRobotTwitterAccount(Twitter mockTwitterAccount) {
        // Inject the account into the pool
        TwitterUtils.releaseRobotAccount(mockTwitterAccount);
    }

    public static void restoreTwitterUtils(Twitter mockTwitterAccount) {
        // Get the injected account
        TwitterUtils.getTwetailerAccount();
        TwitterUtils.getRobotAccount();
    }
}
