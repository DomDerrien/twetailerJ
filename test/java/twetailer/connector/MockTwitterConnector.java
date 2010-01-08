package twetailer.connector;

import twitter4j.Twitter;

public class MockTwitterConnector extends TwitterConnector {

    public static void injectMockTwitterAccount(Twitter mockTwitterAccount) {
        // Inject the account into the pool
        TwitterConnector.releaseTwetailerAccount(mockTwitterAccount);
    }

    public static void restoreTwitterConnector(Twitter mockTwitterAccount, Twitter mockRobotAccount) {
        // Get the injected account
        if (mockTwitterAccount != null) {
            TwitterConnector.getTwetailerAccount();
        }
    }
}
