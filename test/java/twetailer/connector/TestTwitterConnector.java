package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import javamocks.util.logging.MockLogger;

import org.junit.Before;
import org.junit.Test;

import twitter4j.DirectMessage;
import twitter4j.MockTwitter;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TestTwitterConnector {

    @Before
    public void setUp() throws Exception {
        TwitterConnector.resetAseHubAccountLists();
        TwitterConnector.resetAsePublicAccountLists();
        TwitterConnector.setMockLogger(new MockLogger("test", null));
        BaseConnector.setMockLogger(new MockLogger("test", null));
    }

    @Test
    public void testConstructor() {
        new TwitterConnector();
    }

    @Test
    public void testTwetailerAccountDistributorI() {
        Twitter first = TwitterConnector.getAseHubAccount();
        Twitter second = TwitterConnector.getAseHubAccount();

        assertNotSame(first, second);

        TwitterConnector.releaseAseHubAccount(first);

        Twitter third = TwitterConnector.getAseHubAccount();

        assertEquals(first, third);
    }

    @Test
    public void testSendPublicMessageI() throws TwitterException {
        // To inject the mock account
        TwitterConnector.releaseAsePublicAccount(new MockTwitter(TwitterConnector.ASE_HUB_USER_SCREEN_NAME));

        Status response = TwitterConnector.sendPublicMessage("test 12345");
        assertEquals("test 12345", response.getText());
    }

    @SuppressWarnings("serial")
    @Test(expected=TwitterException.class)
    public void testSendPublicMessageII() throws TwitterException {
        // To inject the mock account
        TwitterConnector.releaseAsePublicAccount(new MockTwitter(TwitterConnector.ASE_HUB_USER_SCREEN_NAME) {
            @Override
            public Status updateStatus(String text) throws TwitterException {
                throw new TwitterException("Done in purpose!");
            }
        });

        TwitterConnector.sendPublicMessage("test failure");
    }

    @Test
    public void testSendDirectMessageI() throws TwitterException {
        // To inject the mock account
        TwitterConnector.releaseAseHubAccount(new MockTwitter(TwitterConnector.ASE_HUB_USER_SCREEN_NAME));

        DirectMessage response = TwitterConnector.sendDirectMessage("target", "test");
        assertEquals("target", response.getRecipientScreenName());
        assertEquals("test", response.getText());
    }

    @SuppressWarnings("serial")
    @Test(expected=TwitterException.class)
    public void testSendDirectMessageII() throws TwitterException {
        // To inject the mock account
        TwitterConnector.releaseAseHubAccount(new MockTwitter(TwitterConnector.ASE_HUB_USER_SCREEN_NAME) {
            @Override
            public DirectMessage sendDirectMessage(String to, String text) throws TwitterException {
                throw new TwitterException("Done in purpose!");
            }
        });

        TwitterConnector.sendDirectMessage("target", "test");
    }
}
