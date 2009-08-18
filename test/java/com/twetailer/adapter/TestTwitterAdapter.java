package com.twetailer.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import junit.framework.Assert;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import com.twetailer.ClientException;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.dto.Location;
import com.twetailer.dto.Settings;
import com.twetailer.rest.BaseOperations;
import com.twetailer.rest.ConsumerOperations;
import com.twetailer.rest.DemandOperations;
import com.twetailer.rest.LocationOperations;
import com.twetailer.rest.MockPersistenceManager;
import com.twetailer.rest.SettingsOperations;
import com.twetailer.validator.CommandSettings;

import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;

public class TestTwitterAdapter {

    private TwitterAdapter adapter;
    
    private class MockBaseOperations extends BaseOperations {
        @Override
        public PersistenceManager getPersistenceManager() {
            return new MockPersistenceManager();
        }
    };
    
    private class MockSettingsOperations extends SettingsOperations {
        @Override
        public Settings getSettings() {
            return new Settings();
        }
        @Override
        public Settings getSettings(PersistenceManager pm) {
            return new Settings();
        }
        @Override
        public Settings updateSettings(Settings settings) {
            return settings;
        }
        @Override
        public Settings updateSettings(PersistenceManager pm, Settings settings) {
            return settings;
        }
    };
    
    @Before
    public void setUp() throws Exception {
        adapter = new TwitterAdapter(Locale.ENGLISH);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected=java.lang.NullPointerException.class)
    public void testParseNull() throws ClientException, ParseException {
        // Cannot pass a null reference
        adapter.parseTweet(null);
    }

    @Test(expected=com.twetailer.ClientException.class)
    public void testParseEmpty() throws ClientException, ParseException {
        // At least the twitter identifier of the sender is required
        JsonObject data = adapter.parseTweet("");
        assertEquals(0, data.size());
    }

    @Test(expected=com.twetailer.ClientException.class)
    public void testParseWithOnlySeparators() throws ClientException, ParseException {
        // At least the twitter identifier of the sender is required
        JsonObject data = adapter.parseTweet(" \t \r\n ");
        assertEquals(0, data.size());
    }

    @Test
    public void testParseReferenceI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("reference:21");
        assertEquals(21, data.getLong(Demand.KEY));
    }

    @Test
    public void testParseReferenceII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("reference: 21");
        assertEquals(21, data.getLong(Demand.KEY));
    }

    @Test
    public void testParseReferenceShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21");
        assertEquals(21, data.getLong(Demand.KEY));
    }

    @Test
    public void testParseOneWordTag() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 product");
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(0));
    }

    @Test
    public void testParseOneWordTagPrefixed() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 tags:product");
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(0));
    }

    @Test
    public void testParseMultipleWordsTag() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 brand product part");
        assertEquals("brand", data.getJsonArray(Demand.CRITERIA).getString(0));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(1));
        assertEquals("part", data.getJsonArray(Demand.CRITERIA).getString(2));
    }

    @Test
    public void testParseMultipleWordsTagPrefixed() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 tags:brand product part");
        assertEquals("brand", data.getJsonArray(Demand.CRITERIA).getString(0));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(1));
        assertEquals("part", data.getJsonArray(Demand.CRITERIA).getString(2));
    }

    @Test
    public void testParseExpirationI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 expires:2050-01-01");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 expires: 2050-01-01");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 expires: 20500101");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationIV() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 expires:50-01-01");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 exp:2050-01-01");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseRangeI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 range:1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 range: 1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 range: 1 mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeIV() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 range:1234567mi");
        assertEquals(1234567, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeV() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 range:1km");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeVI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 range:100 km");
        assertEquals(100, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 rng:1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseLocaleI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 locale:h3c2n6 ca");
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 locale: h3c 2n6 ca");
        assertEquals("H3C 2N6", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 locale:h3c2n6-ca");
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIV() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 locale:97323 us");
        assertEquals("97323", data.getString(Location.POSTAL_CODE));
        assertEquals("US", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleV() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 locale:97323-12345 us");
        assertEquals("97323-12345", data.getString(Location.POSTAL_CODE));
        assertEquals("US", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleVI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 locale:97323-12345-us");
        assertEquals("97323-12345", data.getString(Location.POSTAL_CODE));
        assertEquals("US", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 loc:97343-us");
        assertEquals("97343", data.getString(Location.POSTAL_CODE));
        assertEquals("US", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseQuantityI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 quantity:21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 quantity: 21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 quantity: 21 qty: 12");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityShortI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 qty:21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }
    
    @Test
    public void testParseQuantityShortII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:  21    qty:  \t 50   ");
        System.out.println(data.toString());
        assertEquals(21, data.getLong(Demand.KEY));
        assertEquals(50, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseMixedCase() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:21 RaNge: 25 kM");
        assertEquals(25, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseCompositeI() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:1234 exp:2050-12-31");
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseCompositeII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:1234 range: 10 mi exp:2050-12-31");
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseCompositeIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:1234 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseCompositeIV() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("ref:1234 qty:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseCompositeV() throws ClientException, ParseException {
        String keywords = "Wii  console\tremote \t control";
        JsonObject data = adapter.parseTweet("qty:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca " + keywords);
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseCompositeVI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseTweet("qty:12 range: 10 mi exp:2050-12-31 " + keywords + " locale: h0h 0h0 ca");
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseCompositeVII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseTweet("qty:12 range: 10 mi " + keywords + " exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseCompositeVIII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseTweet("" + keywords + " qty:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseActionI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseTweet("action:demand ref:1234 " + keywords);
        assertEquals("demand", data.getString(Demand.ACTION));
        assertEquals(1234, data.getLong(Demand.KEY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseIncompleteMessage() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        adapter.parseTweet("action:demand " + keywords);
        // Now, the function consuming the incomplete tweet does the checking
    }

    @Test
    public void testParseActionII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseTweet("!update ref:1234 " + keywords);
        assertEquals("update", data.getString(Demand.ACTION));
        assertEquals(1234, data.getLong(Demand.KEY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseActionIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseTweet("action:list ref:1234");
        assertEquals("list", data.getString(Demand.ACTION));
        assertEquals(1234, data.getLong(Demand.KEY));
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithNoMessageI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Inject a fake Twitter account
        TwitterUtils.releaseTwitterAccount(new Twitter() {
            public List<DirectMessage> getDirectMessages(Paging paging) {
                return null;
            }
        });
        TwitterAdapter adapter = new TwitterAdapter(Locale.ENGLISH);
        adapter._baseOperations = new MockBaseOperations();
        adapter.settingsOperations = new MockSettingsOperations();
        
        adapter.processDirectMessages();
        
        // Remove the fake Twitter account
        TwitterUtils.getTwitterAccount();
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithNoMessageII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Inject a fake Twitter account
        TwitterUtils.releaseTwitterAccount(new Twitter() {
            public List<DirectMessage> getDirectMessages(Paging paging) {
                return new ArrayList<DirectMessage>();
            }
        });
        TwitterAdapter adapter = new TwitterAdapter(Locale.ENGLISH);
        adapter._baseOperations = new MockBaseOperations();
        adapter.settingsOperations = new MockSettingsOperations();
        
        adapter.processDirectMessages();
        
        // Remove the fake Twitter account
        TwitterUtils.getTwitterAccount();
    }

    @SuppressWarnings("deprecation")
    private User createUser(int id, boolean isFollowing, String screenName) {
        User user = EasyMock.createMock(User.class);
        EasyMock.expect(user.getId()).andReturn(id).atLeastOnce();
        EasyMock.expect(user.isFollowing()).andReturn(isFollowing).once();
        if (screenName != null) {
            EasyMock.expect(user.getScreenName()).andReturn(screenName).once();
        }
        EasyMock.replay(user);
        return user;
    }
    
    private DirectMessage createDM(int id, int senderId, String screenName, User sender, String message) {
        DirectMessage dm = EasyMock.createMock(DirectMessage.class);
        EasyMock.expect(dm.getSenderScreenName()).andReturn(screenName).once();
        EasyMock.expect(dm.getSenderId()).andReturn(senderId).once();
        EasyMock.expect(dm.getSender()).andReturn(sender).once();
        EasyMock.expect(dm.getId()).andReturn(id).once();
        if (message != null) {
            EasyMock.expect(dm.getText()).andReturn(message).once();
        }
        EasyMock.replay(dm);
        return dm;
    }
    
    @Test
    @SuppressWarnings({ "serial" })
    public void testProcessDirectMessageFromNewSenderNotFollowingTwetailer() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 3333L;
        // Sender mock
        User sender = createUser(senderId, false, null); // <-- The sender does not follow @twetailer
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, String.valueOf(senderId), sender, null);
        // Twitter mock
        final Twitter twitterAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
            }
            @Override
            public Status updateStatus(String status) {
                Assert.assertTrue(status.startsWith("@" + String.valueOf(senderId)));
                Assert.assertTrue(status.contains("Follow Twetailer"));
                return null;
            }
            @Override
            public User follow(String id) {
                assertEquals(id, String.valueOf(senderId));
                return null;
            }
        };
        // Inject the fake Twitter account
        TwitterUtils.releaseTwitterAccount(twitterAccount);
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, User twitterAccount) {
                int twitterId = twitterAccount.getId();
                assertEquals(senderId, twitterId);         // <-- Verify the correct creation submission
                Consumer consumer = new Consumer();
                consumer.setTwitterId(Long.valueOf(twitterId));
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // TwitterAdapter mock 
        TwitterAdapter adapter = new TwitterAdapter(Locale.ENGLISH);
        adapter._baseOperations = new MockBaseOperations();
        adapter.consumerOperations = consumerOperations;
        
        // Test itself
        Long newSinceId = adapter.processDirectMessages(new MockPersistenceManager(), 1L);
        assertNotSame(Long.valueOf(dmId), newSinceId); // Because the nessage is not processed
        
        // Remove the fake Twitter account
        TwitterUtils.getTwitterAccount();
    }
    
    static String referenceLabel = CommandSettings.getPrefixes(Locale.ENGLISH).getJsonArray(CommandSettings.Prefix.reference.toString()).getString(0);

    @Test
    @SuppressWarnings({ "serial" })
    public void testProcessDirectMessageWithOneCorrectMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
        final long demandKey = 4444;
        final long locationKey = 55555;
        // Sender mock
        User sender = createUser(senderId, true, null);
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, String.valueOf(senderId), sender, "!demand tags:wii console qty:1 loc:h0h0h0 ca exp:2050-01-01");
        // Twitter mock
        final Twitter twitterAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
            }
            @Override
            public DirectMessage sendDirectMessage(String id, String text) {
                assertEquals(String.valueOf(senderId), id);
                Assert.assertTrue(text.contains(referenceLabel + ":" + String.valueOf(demandKey)));
                return dm;
            }
        };
        // Inject the fake Twitter account
        TwitterUtils.releaseTwitterAccount(twitterAccount);
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, User twitterAccount) {
                int twitterId = twitterAccount.getId();
                assertEquals(senderId, twitterId);         // <-- Verify the correct creation submission
                Consumer consumer = new Consumer();
                consumer.setTwitterId(Long.valueOf(twitterId));
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals("consumerKey", key);                     // <-- Sender is already known
                assertEquals(Long.valueOf(consumerKey), (Long) value);
                return new ArrayList<Demand>();
            }
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long consumerKey) {
                assertEquals(2, parameters.getJsonArray(Demand.CRITERIA).size());
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long givenLocationKey) {
                assertEquals(locationKey, givenLocationKey.longValue());
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject parameters) {
                assertEquals("H0H0H0", parameters.getString(Location.POSTAL_CODE));
                Location location = new Location();
                location.setPostalCode("H0H0H0");
                location.setCountryCode("CA");
                location.setKey(locationKey);
                return location;
            }
        };
        // TwitterAdapter mock 
        TwitterAdapter adapter = new TwitterAdapter(Locale.ENGLISH);
        adapter._baseOperations = new MockBaseOperations();
        adapter.consumerOperations = consumerOperations;
        adapter.demandOperations = demandOperations;
        adapter.locationOperations = locationOperations;
        
        // Test itself
        Long newSinceId = adapter.processDirectMessages(new MockPersistenceManager(), 1L);
        assertEquals(Long.valueOf(dmId), newSinceId);
        
        // Remove the fake Twitter account
        TwitterUtils.getTwitterAccount();
    }

    @Test
    @SuppressWarnings({ "serial" })
    public void testProcessDirectMessageWithUnsupportedAction() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
        // Sender mock
        User sender = createUser(senderId, true, null);
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, String.valueOf(senderId), sender, "!grrrr ref:10 wii console qty:1 loc:h0h0h0 ca exp:2050-01-01");
        // Twitter mock
        final Twitter twitterAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                List<DirectMessage> messages = new ArrayList<DirectMessage>();
                messages.add(dm);
                return messages;
            }
            @Override
            public DirectMessage sendDirectMessage(String id, String text) {
                assertEquals(String.valueOf(senderId), id);
                Assert.assertTrue(text.contains("not supported"));
                return dm;
            }
        };
        // Inject the fake Twitter account
        TwitterUtils.releaseTwitterAccount(twitterAccount);
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, User twitterAccount) {
                int twitterId = twitterAccount.getId();
                assertEquals(senderId, twitterId);         // <-- Verify the correct creation submission
                Consumer consumer = new Consumer();
                consumer.setTwitterId(Long.valueOf(twitterId));
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        /*
        // DemandsServlet mock
        final DemandsServlet demandsServlet = new DemandsServlet() {
            @Override
            public List<Demand> getDemands(String key, Object value, int limit) {
                assertEquals("consumerKey", key);                     // <-- Sender is already known
                assertEquals(Long.valueOf(consumerKey), (Long) value);
                return new ArrayList<Demand>();
            }
        };
        */
        // TwitterAdapter mock 
        TwitterAdapter adapter = new TwitterAdapter(Locale.ENGLISH);
        adapter._baseOperations = new MockBaseOperations();
        adapter.consumerOperations = consumerOperations;
        
        // Test itself
        Long newSinceId = adapter.processDirectMessages(new MockPersistenceManager(), 1L);
        assertEquals(Long.valueOf(dmId), newSinceId);
        
        // Remove the fake Twitter account
        TwitterUtils.getTwitterAccount();
    }
}