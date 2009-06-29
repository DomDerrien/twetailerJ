package com.twetailer.adapter;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.domderrien.i18n.DateUtils;
import org.domderrien.jsontools.JsonException;
import org.domderrien.jsontools.JsonObject;
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
import com.twetailer.dto.Command;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.j2ee.ConsumersServlet;
import com.twetailer.j2ee.DemandsServlet;

public class TestTwitterAdapter {

    private TwitterAdapter adapter;
    
    @Before
    public void setUp() throws Exception {
        adapter = new TwitterAdapter();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected=java.lang.NullPointerException.class)
    public void testParseNull() throws ClientException, ParseException {
        // Cannot pass a null reference
        adapter.parseMessage(null);
    }

    @Test(expected=com.twetailer.ClientException.class)
    public void testParseEmpty() throws ClientException, ParseException {
        // At least the twitter identifier of the sender is required
        JsonObject data = adapter.parseMessage("");
        assertEquals(0, data.size());
    }

    @Test(expected=com.twetailer.ClientException.class)
    public void testParseWithOnlySeparators() throws ClientException, ParseException {
        // At least the twitter identifier of the sender is required
        JsonObject data = adapter.parseMessage(" \t \r\n ");
        assertEquals(0, data.size());
    }

    @Test
    public void testParseReferenceI() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("reference:21");
        assertEquals(21, data.getLong(Demand.KEY));
    }

    @Test
    public void testParseReferenceII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("reference: 21");
        assertEquals(21, data.getLong(Demand.KEY));
    }

    @Test
    public void testParseReferenceShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21");
        assertEquals(21, data.getLong(Demand.KEY));
    }

    @Test
    public void testParseOneWordTag() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 product");
        assertEquals("product", data.getString(Demand.CRITERIA));
    }

    @Test
    public void testParseOneWordTagPrefixed() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 tags:product");
        assertEquals("product", data.getString(Demand.CRITERIA));
    }

    @Test
    public void testParseMultipleWordsTag() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 brand product part");
        assertEquals("brand product part", data.getString(Demand.CRITERIA));
    }

    @Test
    public void testParseMultipleWordsTagPrefixed() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 tags:brand product part");
        assertEquals("brand product part", data.getString(Demand.CRITERIA));
    }

    @Test
    public void testParseExpirationI() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 expires:2050-01-01");
        assertEquals(DateUtils.isoToMilliseconds("2050-01-01T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 expires: 2050-01-01");
        assertEquals(DateUtils.isoToMilliseconds("2050-01-01T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 expires: 20500101");
        assertEquals(DateUtils.isoToMilliseconds("2050-01-01T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationIV() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 expires:50-01-01");
        assertEquals(DateUtils.isoToMilliseconds("2050-01-01T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testParseExpirationShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 exp:2050-01-01");
        assertEquals(DateUtils.isoToMilliseconds("2050-01-01T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseRangeI() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 range:1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 range: 1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 range: 1 mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeIV() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 range:1234567mi");
        assertEquals(1234567, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeV() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 range:1km");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeVI() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 range:100 km");
        assertEquals(100, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 rng:1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseLocaleI() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 locale:h3c2n6 ca");
        assertEquals("H3C2N6", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 locale: h3c 2n6 ca");
        assertEquals("H3C 2N6", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 locale:h3c2n6-ca");
        assertEquals("H3C2N6", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIV() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 locale:97323 us");
        assertEquals("97323", data.getString(Demand.POSTAL_CODE));
        assertEquals("US", data.getString(Demand.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleV() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 locale:97323-12345 us");
        assertEquals("97323-12345", data.getString(Demand.POSTAL_CODE));
        assertEquals("US", data.getString(Demand.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleVI() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 locale:97323-12345-us");
        assertEquals("97323-12345", data.getString(Demand.POSTAL_CODE));
        assertEquals("US", data.getString(Demand.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 loc:97343-us");
        assertEquals("97343", data.getString(Demand.POSTAL_CODE));
        assertEquals("US", data.getString(Demand.COUNTRY_CODE));
    }

    @Test
    public void testParseQuantityI() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 quantity:21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 quantity: 21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 quantity: 21 qty: 12");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityShort() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 qty:21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseMixedCase() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:21 RaNge: 25 kM");
        assertEquals(25, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseCompositeI() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:1234 exp:2050-12-31");
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals(DateUtils.isoToMilliseconds("2050-12-31T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseCompositeII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:1234 range: 10 mi exp:2050-12-31");
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals(DateUtils.isoToMilliseconds("2050-12-31T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseCompositeIII() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:1234 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals(DateUtils.isoToMilliseconds("2050-12-31T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
    }

    @Test
    public void testParseCompositeIV() throws ClientException, ParseException {
        JsonObject data = adapter.parseMessage("ref:1234 qty:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals(DateUtils.isoToMilliseconds("2050-12-31T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseCompositeV() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseMessage("qty:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca " + keywords);
        assertEquals(DateUtils.isoToMilliseconds("2050-12-31T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        assertEquals(keywords, data.getString(Demand.CRITERIA));
    }

    @Test
    public void testParseCompositeVI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseMessage("qty:12 range: 10 mi exp:2050-12-31 " + keywords + " locale: h0h 0h0 ca");
        assertEquals(DateUtils.isoToMilliseconds("2050-12-31T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        assertEquals(keywords, data.getString(Demand.CRITERIA));
    }

    @Test
    public void testParseCompositeVII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseMessage("qty:12 range: 10 mi " + keywords + " exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals(DateUtils.isoToMilliseconds("2050-12-31T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        assertEquals(keywords, data.getString(Demand.CRITERIA));
    }

    @Test
    public void testParseCompositeVIII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseMessage("" + keywords + " qty:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals(DateUtils.isoToMilliseconds("2050-12-31T23:59:59"), data.getLong(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Demand.POSTAL_CODE));
        assertEquals("CA", data.getString(Demand.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        assertEquals(keywords, data.getString(Demand.CRITERIA));
    }

    @Test
    public void testParseActionI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseMessage("action:demand ref:1234" + keywords);
        assertEquals("demand", data.getString(Demand.ACTION));
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals(keywords, data.getString(Demand.CRITERIA));
    }

    @Test(expected=com.twetailer.ClientException.class)
    public void testParseIncompleteMessageI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        adapter.parseMessage("action:demand " + keywords);
    }

    @Test(expected=com.twetailer.ClientException.class)
    public void testParseIncompleteMessageII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        adapter.parseMessage("action:demand loc:h0h0h0 ca " + keywords);
    }

    @Test(expected=com.twetailer.ClientException.class)
    public void testParseIncompleteMessageIII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        adapter.parseMessage("action:demand exp:2050-01-01" + keywords);
    }

    @Test
    public void testParseActionII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = adapter.parseMessage("!update ref:1234" + keywords);
        assertEquals("update", data.getString(Demand.ACTION));
        assertEquals(1234, data.getLong(Demand.KEY));
        assertEquals(keywords, data.getString(Demand.CRITERIA));
    }

    @Test
    public void testProcessDirectMessageWithNoMessageI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        TwitterAdapter adapter = new TwitterAdapter() {
            @SuppressWarnings("serial")
            public Twitter getTwitterAccount() {
                return new Twitter() {
                    public List<DirectMessage> getDirectMessages(Paging paging) {
                        return null;
                    }
                };
            }
        };
        adapter.processDirectMessages();
    }

    @Test
    public void testProcessDirectMessageWithNoMessageII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        TwitterAdapter adapter = new TwitterAdapter() {
            @SuppressWarnings("serial")
            public Twitter getTwitterAccount() {
                return new Twitter() {
                    public List<DirectMessage> getDirectMessages(Paging paging) {
                        return new ArrayList<DirectMessage>();
                    }
                };
            }
        };
        adapter.processDirectMessages();
    }

    @Test
    public void testAccessorI() {
        adapter.getTwitterAccount();
    }

    @Test
    public void testAccessorII() {
        adapter.getConsumersServlet();
    }

    @Test
    public void testAccessorIII() {
        adapter.getDemandsServlet();
    }

    @Test
    public void testAccessorIV() {
        Twitter twitterAccount = adapter.getTwitterAccount();
        assertEquals(twitterAccount, adapter.getTwitterAccount());
    }

    @Test
    public void testAccessorV() {
        ConsumersServlet servlet = adapter.getConsumersServlet();
        assertEquals(servlet, adapter.getConsumersServlet());
    }

    @Test
    public void testAccessorVI() {
        DemandsServlet servlet = adapter.getDemandsServlet();
        assertEquals(servlet, adapter.getDemandsServlet());
    }

    private User createUser(int id, boolean isFollowing, String screenName) {
        User user = EasyMock.createMock(User.class);
        EasyMock.expect(user.getId()).andReturn(id).once();
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
    @SuppressWarnings("serial")
    public void testProcessDirectMessageFromNewSenderNotFollowingTwetailer() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
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
                Assert.assertTrue(status.contains("follow @twetailer"));
                return null;
            }
            @Override
            public User follow(String id) {
                assertEquals(id, String.valueOf(senderId));
                return null;
            }
        };
        // ConsumersServlet mock
        final ConsumersServlet consumersServlet = new ConsumersServlet() {
            @Override
            public Consumer getConsumer(String key, Object value) {
                assertEquals("twitterId", key);
                assertEquals(Long.valueOf(senderId), (Long) value);
                return null;                                        // <-- Unknown consumer who needs to be created
            }
            @Override
            public Consumer createConsumer(User twitterAccount) {
                int twitterId = twitterAccount.getId();
                assertEquals(senderId, twitterId);         // <-- Verify the correct creation submission
                Consumer consumer = new Consumer();
                consumer.setTwitterId(Long.valueOf(twitterId));
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // TwitterAdapter mock 
        TwitterAdapter adapter = new TwitterAdapter() {
            @Override
            public Twitter getTwitterAccount() {
                return twitterAccount;
            }
            @Override
            public ConsumersServlet getConsumersServlet() {
                return consumersServlet;
            }
        };
        
        // Test itself
        Long newSinceId = adapter.processDirectMessages(1L);
        assertEquals(Long.valueOf(dmId), newSinceId);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageFromExistingSenderNotFollowingTwetailer() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
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
                Assert.assertTrue(status.contains("follow @twetailer"));
                return null;
            }
        };
        // ConsumersServlet mock
        final ConsumersServlet consumersServlet = new ConsumersServlet() {
            @Override
            public Consumer getConsumer(String key, Object value) {
                assertEquals("twitterId", key);                     // <-- Sender is already known
                assertEquals(Long.valueOf(senderId), (Long) value);
                Consumer consumer = new Consumer();
                consumer.setTwitterId((Long) value);
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // TwitterAdapter mock 
        TwitterAdapter adapter = new TwitterAdapter() {
            @Override
            public Twitter getTwitterAccount() {
                return twitterAccount;
            }
            @Override
            public ConsumersServlet getConsumersServlet() {
                return consumersServlet;
            }
        };
        
        // Test itself
        Long newSinceId = adapter.processDirectMessages(1L);
        assertEquals(Long.valueOf(dmId), newSinceId);
    }
    
    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithOneCorrectMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
        final long demandKey = 4444;
        // Sender mock
        User sender = createUser(senderId, true, null);
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, String.valueOf(senderId), sender, "!demand wii console qty:1 loc:h0h0h0 ca exp:2050-01-01");
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
                Assert.assertTrue(text.contains("ref:" + String.valueOf(demandKey)));
                return dm;
            }
        };
        // ConsumersServlet mock
        final ConsumersServlet consumersServlet = new ConsumersServlet() {
            @Override
            public Consumer getConsumer(String key, Object value) {
                assertEquals("twitterId", key);
                assertEquals(Long.valueOf(senderId), (Long) value);
                Consumer consumer = new Consumer();
                consumer.setTwitterId((Long) value);
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // DemandsServlet mock
        final DemandsServlet demandsServlet = new DemandsServlet() {
            @Override
            public Long createDemand(JsonObject parameters, Consumer consumer) {
                assertEquals("H0H0H0", parameters.getString(Demand.POSTAL_CODE));
                return demandKey;
            }
        };
        // TwitterAdapter mock 
        TwitterAdapter adapter = new TwitterAdapter() {
            @Override
            public Twitter getTwitterAccount() {
                return twitterAccount;
            }
            @Override
            public ConsumersServlet getConsumersServlet() {
                return consumersServlet;
            }
            @Override
            public DemandsServlet getDemandsServlet() {
                return demandsServlet;
            }
        };
        
        // Test itself
        Long newSinceId = adapter.processDirectMessages(1L);
        assertEquals(Long.valueOf(dmId), newSinceId);
    }
    
    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithIncorrectMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
        final long demandKey = 4444;
        // Sender mock
        User sender = createUser(senderId, true, null);
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, String.valueOf(senderId), sender, "!demand wii console qty:10");
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
                Assert.assertTrue(text.contains("Error"));
                return dm;
            }
        };
        // ConsumersServlet mock
        final ConsumersServlet consumersServlet = new ConsumersServlet() {
            @Override
            public Consumer getConsumer(String key, Object value) {
                assertEquals("twitterId", key);
                assertEquals(Long.valueOf(senderId), (Long) value);
                Consumer consumer = new Consumer();
                consumer.setTwitterId((Long) value);
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // TwitterAdapter mock 
        TwitterAdapter adapter = new TwitterAdapter() {
            @Override
            public Twitter getTwitterAccount() {
                return twitterAccount;
            }
            @Override
            public ConsumersServlet getConsumersServlet() {
                return consumersServlet;
            }
        };
        
        // Test itself
        Long newSinceId = adapter.processDirectMessages(1L);
        assertEquals(Long.valueOf(dmId), newSinceId);
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessDirectMessageWithUnsupportedAction() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final int senderId = 1111;
        final int dmId = 2222;
        final long consumerKey = 333;
        final long demandKey = 4444;
        // Sender mock
        User sender = createUser(senderId, true, null);
        // DirectMessage mock
        final DirectMessage dm = createDM(dmId, senderId, String.valueOf(senderId), sender, "!supply wii console qty:1 loc:h0h0h0 ca exp:2050-01-01");
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
        // ConsumersServlet mock
        final ConsumersServlet consumersServlet = new ConsumersServlet() {
            @Override
            public Consumer getConsumer(String key, Object value) {
                assertEquals("twitterId", key);
                assertEquals(Long.valueOf(senderId), (Long) value);
                Consumer consumer = new Consumer();
                consumer.setTwitterId((Long) value);
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // TwitterAdapter mock 
        TwitterAdapter adapter = new TwitterAdapter() {
            @Override
            public Twitter getTwitterAccount() {
                return twitterAccount;
            }
            @Override
            public ConsumersServlet getConsumersServlet() {
                return consumersServlet;
            }
        };
        
        // Test itself
        Long newSinceId = adapter.processDirectMessages(1L);
        assertEquals(Long.valueOf(dmId), newSinceId);
    }
}