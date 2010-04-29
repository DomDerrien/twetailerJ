package twetailer.task;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.TwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.MockSettingsOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.Settings;
import twitter4j.DirectMessage;
import twitter4j.MockDirectMessage;
import twitter4j.MockHttpResponse;
import twitter4j.MockResponseList;
import twitter4j.MockTwitter;
import twitter4j.MockUser;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.JsonException;

public class TestTweetLoader {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        TweetLoader.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();

        TweetLoader._baseOperations = new BaseOperations();
        TweetLoader.consumerOperations = TweetLoader._baseOperations.getConsumerOperations();
        TweetLoader.rawCommandOperations = TweetLoader._baseOperations.getRawCommandOperations();
        TweetLoader.settingsOperations = TweetLoader._baseOperations.getSettingsOperations();
    }

    @Test
    public void testConstructor() {
        new TweetLoader();
    }

    @Test(expected=RuntimeException.class)
    public void testProcessBatchWithFailure() throws DataSourceException {
        TweetLoader.settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };

        TweetLoader.loadDirectMessages();
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithNoMessageI() throws TwitterException, DataSourceException {
        // Inject a fake Twitter account
        final Twitter mockTwitterAccount = (new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public ResponseList<DirectMessage> getDirectMessages(Paging paging) {
                return null;
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.settingsOperations = new MockSettingsOperations();

        TweetLoader.loadDirectMessages();

        // Remove the fake Twitter account
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithNoMessageII() throws TwitterException, DataSourceException {
        // Inject a fake Twitter account
        final Twitter mockTwitterAccount = (new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
                try {
                    return new MockResponseList<DirectMessage>(0, new MockHttpResponse(null));
                }
                catch (IOException ex) {
                    throw new TwitterException("Relay IOException: " + ex.getMessage());
                }
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.settingsOperations = new MockSettingsOperations();

        TweetLoader.loadDirectMessages();

        // Remove the fake Twitter account
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithNoMessageIII() throws TwitterException, DataSourceException {
        // Inject a fake Twitter account
        final Twitter mockTwitterAccount = (new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.settingsOperations = new MockSettingsOperations();

        try {
            TweetLoader.loadDirectMessages();
        }
        finally {
            // Remove the fake Twitter account
            MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
        }
    }

    /*****
    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageFromNewSenderNotFollowingTwetailer() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 3333L;
        final String senderScreenName = "Katelyn";

        // Sender mock
        User sender = createUser(senderId, false, senderScreenName); // <-- The sender does not follow @twetailer
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, senderScreenName, sender, null);
        // Twitter mock
        final Twitter mockTwitterAccount = new Twitter() {
            @Override
            public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
                try {
                    ResponseList<DirectMessage> messages = new MockResponseList<DirectMessage>(0, new MockHttpResponse(null));
                    messages.add(dm);
                    return messages;
                }
                catch (IOException ex) {
                    throw new TwitterException("Relay IOException: " + ex.getMessage());
                }
            }
            @Override
            public Status updateStatus(String status) {
                assertTrue(status.startsWith("@" + senderScreenName));
                assertTrue(status.contains("Follow Twetailer"));
                return null;
            }
            / ***
            @Override
            public User follow(String id) {
                assertEquals(id, String.valueOf(senderId));
                return null;
            }
            *** /
        };
        // Inject the fake Twitter account
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, User twitterAccount) {
                int twitterId = twitterAccount.getId();
                assertEquals(senderId, twitterId);         // <-- Verify the correct creation submission
                Consumer consumer = new Consumer();
                consumer.setTwitterId(senderScreenName);
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // RawCommandOperations mock
        final Long rawCommandKey = 8888L;
        final RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(senderScreenName, rawCommand.getEmitterId());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };
        // TweetLoader mock
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.consumerOperations = consumerOperations;
        TweetLoader.rawCommandOperations = rawCommandOperations;

        // Test itself
        Long newSinceId = TweetLoader.loadDirectMessages(new MockPersistenceManager(), 1L);
        assertNotSame(Long.valueOf(dmId), newSinceId); // Because the nessage is not processed

        // Remove the fake Twitter account
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }
    ****/

    /*****
    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageFromNewSenderNotFollowingTwetailerWithManyTweets() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 3333L;
        final String senderScreenName = "Katelyn";

        // Sender mock
        User sender = createUser(senderId, false, senderScreenName); // <-- The sender does not follow @twetailer
        // DirectMessage mock
        final DirectMessage dm1 = createDM(dmId, senderId, senderScreenName, sender, "first");
        final DirectMessage dm2 = createDM(dmId + 1, senderId, senderScreenName, sender, "second");
        // Twitter mock
        final Twitter mockTwitterAccount = new Twitter() {
            @Override
            public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
                try {
                    ResponseList<DirectMessage> messages = new MockResponseList<DirectMessage>(0, new MockHttpResponse(null));
                    messages.add(dm1);
                    messages.add(dm2);
                    return messages;
                }
                catch (IOException ex) {
                    throw new TwitterException("Relay IOException: " + ex.getMessage());
                }
            }
            private boolean firstNotification = true;
            @Override
            public Status updateStatus(String status) {
                assertTrue(firstNotification);
                assertTrue(status.startsWith("@" + senderScreenName));
                assertTrue(status.contains("Follow Twetailer"));
                firstNotification = false;
                return null;
            }
            / ***
            @Override
            public User follow(String id) {
                assertEquals(id, String.valueOf(senderId));
                return null;
            }
            *** /
        };
        // Inject the fake Twitter account
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, User twitterAccount) {
                int twitterId = twitterAccount.getId();
                assertEquals(senderId, twitterId);         // <-- Verify the correct creation submission
                Consumer consumer = new Consumer();
                consumer.setTwitterId(senderScreenName);
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // RawCommandOperations mock
        final Long rawCommandKey = 8888L;
        final RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(senderScreenName, rawCommand.getEmitterId());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };
        // TweetLoader mock
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.consumerOperations = consumerOperations;
        TweetLoader.rawCommandOperations = rawCommandOperations;

        // Test itself
        Long newSinceId = TweetLoader.loadDirectMessages(new MockPersistenceManager(), 1L);
        assertNotSame(Long.valueOf(dmId), newSinceId); // Because the nessage is not processed

        // Remove the fake Twitter account
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }
    ****/

    @Test
    @SuppressWarnings({ "serial" })
    public void testProcessDirectMessageWithOneCorrectMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
        final long rawCommandKey = 4444;
        final String senderScreenName = "Katelyn";

        // Sender mock
        // User sender = createUser(senderId, true, senderScreenName);
        User sender = new MockUser(senderId, senderScreenName, senderScreenName);
        User receiver = new MockUser(senderId * 3232, TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME, TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME);
        // DirectMessage mock
        // final DirectMessage dm = createDM(dmId, senderId, senderScreenName, sender, );
        final DirectMessage dm = new MockDirectMessage(dmId, sender, receiver, "action:demand tags:wii console quantity:1 loc:h0h0h0 ca exp:2050-01-01");
        // Twitter mock
        final Twitter mockTwitterAccount = new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
                try {
                    ResponseList<DirectMessage> messages = new MockResponseList<DirectMessage>(0, new MockHttpResponse(null));
                    messages.add(dm);
                    return messages;
                }
                catch (IOException ex) {
                    throw new TwitterException("Relay IOException: " + ex.getMessage());
                }
            }
        };
        // Inject the fake Twitter account
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, User twitterAccount) {
                int twitterId = twitterAccount.getId();
                assertEquals(senderId, twitterId);         // <-- Verify the correct creation submission
                Consumer consumer = new Consumer();
                consumer.setTwitterId(senderScreenName);
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // RawCommandOperations mock
        final RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(Source.twitter, rawCommand.getSource());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };
        // TweetLoader mock
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.consumerOperations = consumerOperations;
        TweetLoader.rawCommandOperations = rawCommandOperations;
        TweetLoader.settingsOperations = new MockSettingsOperations();

        // Test itself
        Long newSinceId = TweetLoader.loadDirectMessages();
        assertEquals(Long.valueOf(dmId), newSinceId);

        // Remove the fake Twitter account
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    @SuppressWarnings({ "serial" })
    public void testProcessDirectMessageWithOldTweetId() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 0;
        final long consumerKey = 333;
        final long rawCommandKey = 4444;
        final String senderScreenName = "Katelyn";

        // Sender mock
        // Sender mock
        // User sender = createUser(senderId, true, senderScreenName);
        User sender = new MockUser(senderId, senderScreenName, senderScreenName);
        User receiver = new MockUser(senderId * 3232, TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME, TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME);
        // DirectMessage mock
        // final DirectMessage dm = createDM(dmId, senderId, senderScreenName, sender, );
        final DirectMessage dm = new MockDirectMessage(dmId, sender, receiver, "action:demand tags:wii console quantity:1 loc:h0h0h0 ca exp:2050-01-01");
        // Twitter mock
        final Twitter mockTwitterAccount = new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
                try {
                    ResponseList<DirectMessage> messages = new MockResponseList<DirectMessage>(0, new MockHttpResponse(null));
                    messages.add(dm);
                    return messages;
                }
                catch (IOException ex) {
                    throw new TwitterException("Relay IOException: " + ex.getMessage());
                }
            }
        };
        // Inject the fake Twitter account
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, User twitterAccount) {
                int twitterId = twitterAccount.getId();
                assertEquals(senderId, twitterId);         // <-- Verify the correct creation submission
                Consumer consumer = new Consumer();
                consumer.setTwitterId(senderScreenName);
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // RawCommandOperations mock
        final RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(Source.twitter, rawCommand.getSource());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };
        // TweetLoader mock
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.consumerOperations = consumerOperations;
        TweetLoader.rawCommandOperations = rawCommandOperations;
        TweetLoader.settingsOperations = new MockSettingsOperations();

        // Test itself
        Long newSinceId = TweetLoader.loadDirectMessages();
        assertEquals(Long.valueOf(1), newSinceId); // The tweet has previously an higher number, so the same higher number is returned

        // Remove the fake Twitter account
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testFailingLoadDirectMessagesI() {
        TweetLoader.settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };

        assertEquals(new Long(-1L), TweetLoader.loadDirectMessages());

    }

    @Test
    @SuppressWarnings("serial")
    public void testFailingLoadDirectMessagesII() throws TwitterException {
        TweetLoader.settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                return new Settings();
            }
        };

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(new MockTwitter(TwitterConnector.TWETAILER_TWITTER_SCREEN_NAME) {
            @Override
            public ResponseList<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
                throw new TwitterException("Done in purpose!");
            }
        });

        assertEquals(new Long(-1L), TweetLoader.loadDirectMessages());

    }
}
