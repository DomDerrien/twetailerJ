package twetailer.connector;

import twitter4j.Twitter;

public class MockTwitterConnector extends TwitterConnector {

    public static void injectMockTwitterAccount(Twitter mockTwitterAccount) {
        // Inject the account into the pool
        TwitterConnector.resetAccountLists();
        TwitterConnector.releaseTwetailerAccount(mockTwitterAccount);
    }

    public static void restoreTwitterConnector() {
        TwitterConnector.resetAccountLists();
    }
}
