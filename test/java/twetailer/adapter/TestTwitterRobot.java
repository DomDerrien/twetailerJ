package twetailer.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import domderrien.i18n.LabelExtractor;

public class TestTwitterRobot {

    private class MockBaseOperations extends BaseOperations {
        @Override
        public PersistenceManager getPersistenceManager() {
            return new MockPersistenceManager();
        }
    };

    @Test
    public void testContructor() {
        new TwitterRobot();
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessagesI() throws TwitterException, DataSourceException {
        // Twitter mock
        final Twitter robotAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                return null;
            }
        };
        MockTwitterUtils.injectMockRobotTwitterAccount(robotAccount);

        TwitterRobot.processDirectMessages(new MockPersistenceManager(), 1L);

        MockTwitterUtils.releaseRobotAccount(robotAccount);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessagesII() throws TwitterException, DataSourceException {
        // Twitter mock
        final Twitter robotAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                return messages;
            }
        };
        MockTwitterUtils.injectMockRobotTwitterAccount(robotAccount);

        TwitterRobot.processDirectMessages(new MockPersistenceManager(), 1L);

        MockTwitterUtils.releaseRobotAccount(robotAccount);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessagesIII() throws TwitterException, DataSourceException {
        final int senderId = 1111;
        final int dmId = 2222;
        final Long demandKey = 3333L;
        final Long storeKey = 4444L;
        final String senderScreenName = "Tom";
        final String tags = "one two three";
        final String message = LabelExtractor.get( // Same message as generated by DemandProcessor.process()
                "dp_informNewDemand",
                new Object[] { demandKey, tags, new Date() },
                Locale.ENGLISH
        );

        // Sender mock
        User sender = TestTwitterAdapter.createUser(senderId, false, senderScreenName);
        // DirectMessage mock
        final DirectMessage dm = TestTwitterAdapter.createDM(dmId, senderId, String.valueOf(senderId), sender, message);
        // Twitter mock
        final Twitter robotAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
            }
            @Override
            public DirectMessage sendDirectMessage(String id, String text) {
                assertEquals(String.valueOf(senderId), id);
                assertNotSame(0, text.length());
                assertTrue(text.contains(LabelExtractor.get("robot_sendDefaultProposal", new Object[] { demandKey, tags, storeKey}, Locale.ENGLISH)));
                return dm;
            }
        };
        MockTwitterUtils.injectMockRobotTwitterAccount(robotAccount);

        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, String key, Object value, int limit) {
                Store store = new Store();
                store.setKey(storeKey);
                List<Store> stores = new ArrayList<Store>();
                stores.add(store);
                return stores;
            }
        };
        // TwitterAdapter mock
        TwitterRobot._baseOperations = new MockBaseOperations();
        TwitterRobot.storeOperations = storeOperations;

        TwitterRobot.processDirectMessages(new MockPersistenceManager(), 1L);

        MockTwitterUtils.releaseRobotAccount(robotAccount);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessagesIV() throws TwitterException, DataSourceException {
        final int senderId = 1111;
        final int dmId = 2222;
        final Long demandKey = 3333L;
        final Long storeKey = 0L; // Default store key
        final String senderScreenName = "Tom";
        final String tags = "one two three";
        final String message = LabelExtractor.get( // Same message as generated by DemandProcessor.process()
                "dp_informNewDemand",
                new Object[] { demandKey, tags, new Date() },
                Locale.ENGLISH
        );

        // Sender mock
        User sender = TestTwitterAdapter.createUser(senderId, false, senderScreenName);
        // DirectMessage mock
        final DirectMessage dm = TestTwitterAdapter.createDM(dmId, senderId, String.valueOf(senderId), sender, message);
        // Twitter mock
        final Twitter robotAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
            }
            @Override
            public DirectMessage sendDirectMessage(String id, String text) {
                assertEquals(String.valueOf(senderId), id);
                assertNotSame(0, text.length());
                assertTrue(text.contains(LabelExtractor.get("robot_sendDefaultProposal", new Object[] { demandKey, tags, storeKey}, Locale.ENGLISH)));
                return dm;
            }
        };
        MockTwitterUtils.injectMockRobotTwitterAccount(robotAccount);

        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, String key, Object value, int limit) {
                List<Store> stores = new ArrayList<Store>();
                return stores;
            }
        };
        // TwitterAdapter mock
        TwitterRobot._baseOperations = new MockBaseOperations();
        TwitterRobot.storeOperations = storeOperations;

        TwitterRobot.processDirectMessages(new MockPersistenceManager(), 1L);

        MockTwitterUtils.releaseRobotAccount(robotAccount);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessagesV() throws TwitterException, DataSourceException {
        final int senderId = 1111;
        final int dmId = 2222;
        final Long demandKey = 3333L;
        final Long storeKey = 4444L;
        final String senderScreenName = "Tom";
        final String tags = "one two three";
        final String message = LabelExtractor.get( // Same message as generated by DemandProcessor.process()
                "dp_informNewDemand",
                new Object[] { demandKey, tags, new Date() },
                Locale.ENGLISH
        );

        // Sender mock
        User sender = TestTwitterAdapter.createUser(senderId, false, senderScreenName);
        // DirectMessage mock
        final DirectMessage dm = TestTwitterAdapter.createDM(dmId, senderId, String.valueOf(senderId), sender, message);
        // Twitter mock
        final Twitter robotAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
            }
            @Override
            public DirectMessage sendDirectMessage(String id, String text) {
                assertEquals(String.valueOf(senderId), id);
                assertNotSame(0, text.length());
                assertTrue(text.contains(LabelExtractor.get("robot_sendDefaultProposal", new Object[] { demandKey, tags, storeKey}, Locale.ENGLISH)));
                return dm;
            }
        };
        MockTwitterUtils.injectMockRobotTwitterAccount(robotAccount);

        // SettingsOperations mock
        SettingsOperations settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
            @Override
            public Settings updateSettings(PersistenceManager pm, Settings settings) {
                return settings;
            }
        };
        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, String key, Object value, int limit) {
                Store store = new Store();
                store.setKey(storeKey);
                List<Store> stores = new ArrayList<Store>();
                stores.add(store);
                return stores;
            }
        };
        // TwitterAdapter mock
        TwitterRobot._baseOperations = new MockBaseOperations();
        TwitterRobot.settingsOperations = settingsOperations;
        TwitterRobot.storeOperations = storeOperations;

        TwitterRobot.processDirectMessages();

        MockTwitterUtils.releaseRobotAccount(robotAccount);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessagesVI() throws TwitterException, DataSourceException {
        final int senderId = 1111;
        final int dmId = 0; // To force the setting being not updated
        final Long demandKey = 3333L;
        final Long storeKey = 4444L;
        final String senderScreenName = "Tom";
        final String tags = "one two three";
        final String message = LabelExtractor.get( // Same message as generated by DemandProcessor.process()
                "dp_informNewDemand",
                new Object[] { demandKey, tags, new Date() },
                Locale.ENGLISH
        );

        // Sender mock
        User sender = TestTwitterAdapter.createUser(senderId, false, senderScreenName);
        // DirectMessage mock
        final DirectMessage dm = TestTwitterAdapter.createDM(dmId, senderId, String.valueOf(senderId), sender, message);
        // Twitter mock
        final Twitter robotAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
            }
            @Override
            public DirectMessage sendDirectMessage(String id, String text) {
                assertEquals(String.valueOf(senderId), id);
                assertNotSame(0, text.length());
                assertTrue(text.contains(LabelExtractor.get("robot_sendDefaultProposal", new Object[] { demandKey, tags, storeKey}, Locale.ENGLISH)));
                return dm;
            }
        };
        MockTwitterUtils.injectMockRobotTwitterAccount(robotAccount);

        // SettingsOperations mock
        SettingsOperations settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
        };
        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, String key, Object value, int limit) {
                Store store = new Store();
                store.setKey(storeKey);
                List<Store> stores = new ArrayList<Store>();
                stores.add(store);
                return stores;
            }
        };
        // TwitterAdapter mock
        TwitterRobot._baseOperations = new MockBaseOperations();
        TwitterRobot.settingsOperations = settingsOperations;
        TwitterRobot.storeOperations = storeOperations;

        TwitterRobot.processDirectMessages();

        MockTwitterUtils.releaseRobotAccount(robotAccount);
    }
}
