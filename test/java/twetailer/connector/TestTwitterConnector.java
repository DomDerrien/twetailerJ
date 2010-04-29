package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import javamocks.util.logging.MockLogger;

import org.junit.Before;
import org.junit.Test;

import twitter4j.DirectMessage;
import twitter4j.MockDirectMessage;
import twitter4j.MockTwitter;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TestTwitterConnector {

    @Before
    public void setUp() throws Exception {
        TwitterConnector.resetAccountLists();
        TwitterConnector.setLogger(new MockLogger("test", null));
    }

    @Test
    public void testConstructor() {
        new TwitterConnector();
    }

    @Test
    public void testTwetailerAccountDistributorI() {
        Twitter first = TwitterConnector.getTwetailerAccount();
        Twitter second = TwitterConnector.getTwetailerAccount();

        assertNotSame(first, second);

        TwitterConnector.releaseTwetailerAccount(first);

        Twitter third = TwitterConnector.getTwetailerAccount();

        assertEquals(first, third);
    }

    @Test
    public void testSendPublicMessageI() throws TwitterException {
        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME));

        Status response = TwitterConnector.sendPublicMessage("test 12345");
        assertEquals("test 12345", response.getText());
    }

    @SuppressWarnings("serial")
    @Test(expected=TwitterException.class)
    public void testSendPublicMessageII() throws TwitterException {
        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
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
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME));

        DirectMessage response = TwitterConnector.sendDirectMessage("target", "test");
        assertEquals("target", response.getRecipientScreenName());
        assertEquals("test", response.getText());
    }

    @SuppressWarnings("serial")
    @Test(expected=TwitterException.class)
    public void testSendDirectMessageII() throws TwitterException {
        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public DirectMessage sendDirectMessage(String to, String text) throws TwitterException {
                throw new TwitterException("Done in purpose!");
            }
        });

        TwitterConnector.sendDirectMessage("target", "test");
    }
}
