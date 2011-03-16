package twetailer.connector;

import twitter4j.Twitter;

public class MockTwitterConnector extends TwitterConnector {

    public static void injectMockTwitterAccount(Twitter mockTwitterAccount) {
        // Inject the account into the pool
        TwitterConnector.resetAseHubAccountLists();
        TwitterConnector.releaseAseHubAccount(mockTwitterAccount);
    }

    public static void restoreTwitterConnector() {
        TwitterConnector.resetAseHubAccountLists();
    }
}
