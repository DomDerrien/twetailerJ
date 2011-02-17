package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.validator.LocaleValidator;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestProposal {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testConstructorI() {
        Proposal object = new Proposal();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Proposal object = new Proposal(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Long key = 98760L;
    String AWSCBUIURL = "Very long long text!";
    Long consumerKey = 876432L;
    String comment = "Super génial";
    String currencyCode = "EUR";
    Long demandKey = 54321L;
    Double price = 25.99D;
    Long score = 5L;
    Long storeKey = 45678L;
    Double total = 32.36D;

    @Test
    public void testAccessors() {
        Proposal object = new Proposal();

        object.setAWSCBUIURL(AWSCBUIURL);
        object.setConsumerKey(consumerKey);
        object.setComment(comment);
        object.setCurrencyCode(currencyCode);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setScore(score);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        assertEquals(AWSCBUIURL, object.getAWSCBUIURL());
        assertEquals(consumerKey, object.getConsumerKey());
        assertEquals(comment, object.getComment());
        assertEquals(currencyCode, object.getCurrencyCode());
        assertEquals(demandKey, object.getDemandKey());
        assertEquals(price, object.getPrice());
        assertEquals(score, object.getScore());
        assertEquals(storeKey, object.getStoreKey());
        assertEquals(total, object.getTotal());
    }

    @Test
    public void testJsonProposalsI() {
        //
        // Cache related copy (highest)
        //
        Proposal object = new Proposal();

        object.setKey(key);

        object.setAWSCBUIURL(AWSCBUIURL);
        object.setConsumerKey(consumerKey);
        object.setComment(comment);
        object.setCurrencyCode(currencyCode);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setScore(score);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        Proposal clone = new Proposal();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(AWSCBUIURL, clone.getAWSCBUIURL());
        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(comment, clone.getComment());
        assertEquals(currencyCode, clone.getCurrencyCode());
        assertEquals(demandKey, clone.getDemandKey());
        assertEquals(price, clone.getPrice());
        assertEquals(score, clone.getScore());
        assertEquals(storeKey, clone.getStoreKey());
        assertEquals(total, clone.getTotal());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Proposal object = new Proposal();

        Proposal clone = new Proposal();
        clone.fromJson(object.toJson(), true, true);

        assertNull(clone.getAWSCBUIURL());
        assertNull(clone.getConsumerKey());
        assertNull(clone.getComment());
        assertEquals(LocaleValidator.DEFAULT_CURRENCY_CODE, clone.getCurrencyCode());
        assertNull(clone.getDemandKey());
        assertEquals(0.0D, clone.getPrice().doubleValue(), 0);
        assertEquals(0L, clone.getScore().longValue());
        assertNull(clone.getStoreKey());
        assertEquals(0.0D, clone.getTotal().doubleValue(), 0);
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Proposal object = new Proposal();

        object.setKey(key);

        object.setAWSCBUIURL(AWSCBUIURL);
        object.setConsumerKey(consumerKey);
        object.setComment(comment);
        object.setCurrencyCode(currencyCode);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setScore(score);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        Proposal clone = new Proposal();
        clone.fromJson(object.toJson(), true, false);

        assertNull(clone.getAWSCBUIURL());
        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(comment, clone.getComment());
        assertEquals(currencyCode, clone.getCurrencyCode());
        assertEquals(demandKey, clone.getDemandKey());
        assertEquals(price, clone.getPrice());
        assertEquals(score, clone.getScore());
        assertEquals(storeKey, clone.getStoreKey());
        assertEquals(total, clone.getTotal());
    }

    @Test
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        Proposal object = new Proposal();

        // object.setKey(key);

        object.setAWSCBUIURL(AWSCBUIURL);
        object.setConsumerKey(consumerKey);
        object.setComment(comment);
        object.setCurrencyCode(currencyCode);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setScore(score);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        Proposal clone = new Proposal();
        clone.fromJson(object.toJson());

        assertNull(clone.getAWSCBUIURL());
        assertEquals(consumerKey, clone.getConsumerKey());
        assertNull(clone.getComment());
        assertEquals(currencyCode, clone.getCurrencyCode());
        assertEquals(demandKey, clone.getDemandKey());
        assertEquals(price, clone.getPrice());
        assertEquals(0L, clone.getScore().longValue());
        assertNull(clone.getStoreKey());
        assertEquals(total, clone.getTotal());
    }

    @Test
    public void testJsonCommandsV() {
        //
        // User update for an existing object (lowest)
        //
        Proposal object = new Proposal();

        object.setKey(key);

        object.setAWSCBUIURL(AWSCBUIURL);
        object.setConsumerKey(consumerKey);
        object.setComment(comment);
        object.setCurrencyCode(currencyCode);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setScore(score);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        Proposal clone = new Proposal();
        clone.fromJson(object.toJson());

        assertNull(clone.getAWSCBUIURL());
        assertNull(clone.getConsumerKey());
        assertNull(clone.getComment());
        assertEquals(currencyCode, clone.getCurrencyCode());
        assertNull(clone.getDemandKey());
        assertEquals(price, clone.getPrice());
        assertEquals(0L, clone.getScore().longValue());
        assertNull(clone.getStoreKey());
        assertEquals(total, clone.getTotal());
    }

    @Test
    public void testJsonCommandsVI() {
        Proposal object = new Proposal();
        JsonObject json = new GenericJsonObject();
        object.fromJson(json, true, true);
    }

    @Test
    public void testShortcutI() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Proposal.PROPOSAL_KEY, key);

        Proposal proposal = new Proposal(parameters);
        assertEquals(key, proposal.getKey());
        assertNull(proposal.getDemandKey()); // Can only be set with Proposal to be created
    }

    @Test
    public void testSetAWSCBUIURL() {
        Proposal object = new Proposal();

        object.setAWSCBUIURL(null);
        assertNull(object.getAWSCBUIURL());
        object.setAWSCBUIURL("");
        assertNull(object.getAWSCBUIURL());
        object.setAWSCBUIURL(AWSCBUIURL);
        assertEquals(AWSCBUIURL, object.getAWSCBUIURL());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetConsumerKey() {
        new Proposal().setConsumerKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetDemandKey() {
        new Proposal().setDemandKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetStoreKey() {
        new Proposal().setStoreKey(null);
    }

    @Test
    public void testResetList() throws JsonException {
        Proposal object = new Proposal();
        object.resetLists(); // Inherited, tests done by parent objects
    }

    @Test
    public void testSetCurrencyCode() throws JsonException {
        Proposal object = new Proposal();
        object.setCurrencyCode(null);
        assertEquals(LocaleValidator.DEFAULT_CURRENCY_CODE, object.getCurrencyCode());
    }

    @Test
    public void testSetScore() throws JsonException {
        Proposal object = new Proposal();

        object.setScore(null);
        assertEquals(0L, object.getScore().longValue());

        object.setScore(0L);
        assertEquals(0L, object.getScore().longValue());

        object.setScore(2L);
        assertEquals(2L, object.getScore().longValue());

        object.setScore(10L);
        assertEquals(0L, object.getScore().longValue());
    }
}
