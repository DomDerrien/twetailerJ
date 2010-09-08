package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;
import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.TestMailConnector;
import twetailer.connector.TwitterConnector;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dto.Consumer;
import twetailer.task.step.BaseSteps;
import twitter4j.MockTwitter;
import twitter4j.TwitterException;
import twitter4j.User;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestTwitterMailNotifHandlerServlet {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        TwitterMailNotificationHandlerServlet.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostI() throws IOException, TwitterException {
        //
        // New follower not registered yet
        //
        final String followerName = "Test Twitter";
        final String followerScreenName = "Test_Twitter";
        final String from = "twitter@postmaster.twitter.com";
        final String twitterName = "Twitter";
        final String subject = followerName + " is now following you on Twitter!";
        final String message = "Hi Twetailer. " + followerName + " (" + followerScreenName + ") is now following your tweets on Twitter. A little information on " + followerName + ": ...";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, twitterName, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public User createFriendship(String screenName, boolean follow) throws TwitterException {
                assertEquals(followerScreenName, screenName);
                assertTrue(follow);
                return null; // Not important
            }
        });

        TwitterMailNotificationHandlerServlet servlet = new TwitterMailNotificationHandlerServlet();

        final Long consumerKey = 8888L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(followerScreenName, (String) value);
                return new ArrayList<Consumer>();
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(followerName, consumer.getName());
                assertEquals(followerScreenName, consumer.getTwitterId());
                consumer.setKey(consumerKey);
                return consumer;
            }
        });

        servlet.doPost(request, null);

        TwitterConnector.getTwetailerAccount(); // To remove the injected account from the connector account pool
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostII() throws IOException, TwitterException {
        //
        // New follower already registered
        //
        final String followerName = "Test Twitter";
        final String followerScreenName = "Test_Twitter";
        final String from = "twitter@postmaster.twitter.com";
        final String twitterName = "Twitter";
        final String subject = followerName + " is now following you on Twitter!";
        final String message = "Hi Twetailer. " + followerName + " (" + followerScreenName + ") is now following your tweets on Twitter. A little information on " + followerName + ": ...";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, twitterName, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public User createFriendship(String screenName, boolean follow) throws TwitterException {
                assertEquals(followerScreenName, screenName);
                assertTrue(follow);
                return null; // Not important
            }
        });

        TwitterMailNotificationHandlerServlet servlet = new TwitterMailNotificationHandlerServlet();

        final Long consumerKey = 8888L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(followerScreenName, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setTwitterId(followerScreenName);
                consumers.add(consumer);
                return consumers;
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                fail("Call not expected!");
                return consumer;
            }
        });

        servlet.doPost(request, null);

        TwitterConnector.getTwetailerAccount(); // To remove the injected account from the connector account pool
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostIII() throws IOException, TwitterException {
        //
        // Error while communicating with Twitter
        //
        final String followerName = "Test Twitter";
        final String followerScreenName = "Test_Twitter";
        final String from = "twitter@postmaster.twitter.com";
        final String twitterName = "Twitter";
        final String subject = followerName + " is now following you on Twitter!";
        final String message = "Hi Twetailer. " + followerName + " (" + followerScreenName + ") is now following your tweets on Twitter. A little information on " + followerName + ": ...";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, twitterName, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public User createFriendship(String screenName, boolean follow) throws TwitterException {
                assertEquals(followerScreenName, screenName);
                assertTrue(follow);
                throw new TwitterException("done in purpose!");
            }
        });

        TwitterMailNotificationHandlerServlet servlet = new TwitterMailNotificationHandlerServlet();

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(followerScreenName, (String) value);
                return new ArrayList<Consumer>();
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                fail("Call not expected!");
                return consumer;
            }
        });

        servlet.doPost(request, null);

        TwitterConnector.getTwetailerAccount(); // To remove the injected account from the connector account pool
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostIV() throws IOException, TwitterException {
        //
        // Error while getting consumer information
        //
        final String followerName = "Test Twitter";
        final String followerScreenName = "Test_Twitter";
        final String from = "twitter@postmaster.twitter.com";
        final String twitterName = "Twitter";
        final String subject = followerName + " is now following you on Twitter!";
        final String message = "Hi Twetailer. " + followerName + " (" + followerScreenName + ") is now following your tweets on Twitter. A little information on " + followerName + ": ...";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, twitterName, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public User createFriendship(String screenName, boolean follow) throws TwitterException {
                assertEquals(followerScreenName, screenName);
                assertTrue(follow);
                return null; // Not important
            }
        });

        TwitterMailNotificationHandlerServlet servlet = new TwitterMailNotificationHandlerServlet();

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(followerScreenName, (String) value);
                throw new DataSourceException("done in purpose!");
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                fail("Call not expected!");
                return consumer;
            }
        });

        servlet.doPost(request, null);

        TwitterConnector.getTwetailerAccount(); // To remove the injected account from the connector account pool
    }

    @Test
    public void testDoPostV() throws IOException, TwitterException {
        //
        // Unexpected notification body
        //
        final String followerName = "Test Twitter";
        final String followerScreenName = "Test_Twitter";
        final String from = "twitter@postmaster.twitter.com";
        final String twitterName = "Twitter";
        final String subject = followerName + " is now following you on Twitter!";
        final String message = "Hi Twetailer. " + followerName + " (" + followerScreenName + ") is now a magic Twitter user. A little information on " + followerName + ": ...";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, twitterName, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        TwitterMailNotificationHandlerServlet servlet = new TwitterMailNotificationHandlerServlet();
        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostVI() throws IOException, TwitterException {
        //
        // Unexpected notification subject
        //
        final String followerName = "Test Twitter";
        final String followerScreenName = "Test_Twitter";
        final String from = "twitter@postmaster.twitter.com";
        final String twitterName = "Twitter";
        final String subject = followerName + " is now a magic Twitter user!";
        final String message = "Hi Twetailer. " + followerName + " (" + followerScreenName + ") is now a magic Twitter user. A little information on " + followerName + ": ...";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, twitterName, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        TwitterMailNotificationHandlerServlet servlet = new TwitterMailNotificationHandlerServlet();
        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostVII() throws IOException, TwitterException {
        //
        // No notification subject
        //
        final String followerName = "Test Twitter";
        final String followerScreenName = "Test_Twitter";
        final String from = "twitter@postmaster.twitter.com";
        final String twitterName = "Twitter";
        final String subject = null;
        final String message = "Hi Twetailer. " + followerName + " (" + followerScreenName + ") is now a magic Twitter user. A little information on " + followerName + ": ...";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, twitterName, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        TwitterMailNotificationHandlerServlet servlet = new TwitterMailNotificationHandlerServlet();
        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostVIII() throws IOException, TwitterException {
        //
        // To generate a MailMessageException
        //
        final String followerName = "Test Twitter";
        final String followerScreenName = "Test_Twitter";
        final String from = "";
        final String twitterName = "";
        final String subject = null;
        final String message = "Hi Twetailer. " + followerName + " (" + followerScreenName + ") is now a magic Twitter user. A little information on " + followerName + ": ...";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, twitterName, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        TwitterMailNotificationHandlerServlet servlet = new TwitterMailNotificationHandlerServlet();
        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostIX() throws IOException, TwitterException {
        //
        // Corrupted stream
        //
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() throws IOException {
                throw new IOException("Done in purpose!");
            }
        };

        new TwitterMailNotificationHandlerServlet().doPost(request, null);
    }
}
