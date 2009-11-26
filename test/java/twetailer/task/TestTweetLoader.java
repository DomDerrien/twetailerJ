package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.MockSettingsOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.jsontools.JsonException;

public class TestTweetLoader {

    @SuppressWarnings("deprecation")
    protected static User createUser(int id, boolean isFollowing, String screenName) {
        User user = EasyMock.createMock(User.class);
        EasyMock.expect(user.getId()).andReturn(id).atLeastOnce();
        EasyMock.expect(user.isFollowing()).andReturn(isFollowing).once();
        EasyMock.expect(user.getScreenName()).andReturn(screenName).atLeastOnce();
        EasyMock.replay(user);
        return user;
    }

    protected static DirectMessage createDM(int id, int senderId, String screenName, User sender, String message) {
        DirectMessage dm = EasyMock.createMock(DirectMessage.class);
        EasyMock.expect(dm.getSenderScreenName()).andReturn(screenName).once();
        EasyMock.expect(dm.getSenderId()).andReturn(senderId).once();
        EasyMock.expect(dm.getSender()).andReturn(sender).once();
        EasyMock.expect(dm.getId()).andReturn(id).once();
        if (true || message != null) {
            EasyMock.expect(dm.getText()).andReturn(message).once();
        }
        EasyMock.replay(dm);
        return dm;
    }

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    @BeforeClass
    public static void setUpBeforeClass() {
        TweetLoader.setLogger(new MockLogger("test", null));
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();

        TweetLoader._baseOperations = new BaseOperations();
        TweetLoader.consumerOperations = TweetLoader._baseOperations.getConsumerOperations();
        TweetLoader.rawCommandOperations = TweetLoader._baseOperations.getRawCommandOperations();
        TweetLoader.settingsOperations = TweetLoader._baseOperations.getSettingsOperations();
    }

    @Test
    public void testConstructor() {
        new TweetLoader();
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithNoMessageI() throws TwitterException, DataSourceException {
        // Inject a fake Twitter account
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
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
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                return new ArrayList<DirectMessage>();
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
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) throws TwitterException {
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
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
            }
            @Override
            public Status updateStatus(String status) {
                assertTrue(status.startsWith("@" + senderScreenName));
                assertTrue(status.contains("Follow Twetailer"));
                return null;
            }
            @Override
            public User follow(String id) {
                assertEquals(id, String.valueOf(senderId));
                return null;
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
        // TweetLoader mock
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.consumerOperations = consumerOperations;

        // Test itself
        Long newSinceId = TweetLoader.loadDirectMessages(new MockPersistenceManager(), 1L);
        assertNotSame(Long.valueOf(dmId), newSinceId); // Because the nessage is not processed

        // Remove the fake Twitter account
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

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
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm1);
                messages.add(dm2);
                return messages;
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
            @Override
            public User follow(String id) {
                assertEquals(id, String.valueOf(senderId));
                return null;
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
        // TweetLoader mock
        TweetLoader._baseOperations = new MockBaseOperations();
        TweetLoader.consumerOperations = consumerOperations;

        // Test itself
        Long newSinceId = TweetLoader.loadDirectMessages(new MockPersistenceManager(), 1L);
        assertNotSame(Long.valueOf(dmId), newSinceId); // Because the nessage is not processed

        // Remove the fake Twitter account
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    @SuppressWarnings({ "serial" })
    public void testProcessDirectMessageWithOneCorrectMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
        final long rawCommandKey = 4444;
        final String senderScreenName = "Katelyn";

        // Sender mock
        User sender = createUser(senderId, true, senderScreenName);
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, senderScreenName, sender, "action:demand tags:wii console quantity:1 loc:h0h0h0 ca exp:2050-01-01");
        // Twitter mock
        final Twitter mockTwitterAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
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
        User sender = createUser(senderId, true, senderScreenName);
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, senderScreenName, sender, "action:demand tags:wii console quantity:1 loc:h0h0h0 ca exp:2050-01-01");
        // Twitter mock
        final Twitter mockTwitterAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
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
}
