package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;

import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestSaleAssociate {

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    @BeforeClass
    public static void setUpBeforeClass() {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testConstructorI() {
        SaleAssociate object = new SaleAssociate();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        SaleAssociate object = new SaleAssociate(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Long consumerKey = 67890L;
    Long creatorKey = 12345L;
    List<String> criteria = new ArrayList<String>(Arrays.asList(new String[] {"first", "second"}));
    String email = "d.d@d.dom";
    String imId = "ddd";
    Boolean isStoreAdmin = Boolean.TRUE;
    String language = Locale.FRENCH.getLanguage();
    Long locationKey = 12345L;
    String name = "dom";
    String phoneNumber = "514-123-4567 #890";
    Long storeKey = 54321L;
    Long score = 5L;
    String twitterId = "Ryan";

    @Test
    public void testAccessors() {
        SaleAssociate object = new SaleAssociate();

        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        object.setCriteria(criteria);
        object.setEmail(email);
        object.setJabberId(imId);
        object.setIsStoreAdmin(isStoreAdmin);
        object.setLanguage(language);
        object.setLocationKey(locationKey);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);
        object.setStoreKey(storeKey);
        object.setScore(score);
        object.setTwitterId(twitterId);

        assertEquals(consumerKey, object.getConsumerKey());
        assertEquals(creatorKey, object.getCreatorKey());
        assertEquals(criteria, object.getCriteria());
        assertEquals(email, object.getEmail());
        assertEquals(imId, object.getJabberId());
        assertEquals(isStoreAdmin, object.getIsStoreAdmin());
        assertEquals(language, object.getLanguage());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(name, object.getName());
        assertEquals(storeKey, object.getStoreKey());
        assertEquals(score, object.getScore());
        assertEquals(twitterId, object.getTwitterId());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetCriteriaI() {
        SaleAssociate object = new SaleAssociate();

        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("first"); // Add it twice
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("second");
        assertEquals(2, object.getCriteria().size());

        object.removeCriterion("first"); // Remove first
        assertEquals(1, object.getCriteria().size());

        object.resetCriteria(); // Reset all
        assertEquals(0, object.getCriteria().size());

        object.setCriteria(null); // Failure!
    }

    @Test
    public void testResetCriteriaII() {
        SaleAssociate object = new SaleAssociate();

        object.resetLists(); // To force the criteria list creation
        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.resetLists(); // To be sure there's no error
        object.removeCriterion("first"); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetCriteria(); // Reset all
    }

    @Test
    public void testGetLocale() {
        SaleAssociate object = new SaleAssociate();
        object.setLanguage(language);
        assertEquals(Locale.FRENCH, object.getLocale());
    }

    @Test
    public void testJsonCommandsI() {
        SaleAssociate object = new SaleAssociate();

        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        object.setCriteria(criteria);
        object.setEmail(email);
        object.setJabberId(imId);
        object.setIsStoreAdmin(isStoreAdmin);
        object.setLanguage(language);
        object.setLocationKey(locationKey);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);
        object.setStoreKey(storeKey);
        object.setScore(score);
        object.setTwitterId(twitterId);

        SaleAssociate clone = new SaleAssociate(object.toJson());

        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(creatorKey, clone.getCreatorKey());
        assertEquals(criteria, clone.getCriteria());
        assertEquals(email, clone.getEmail());
        assertEquals(imId, clone.getJabberId());
        assertEquals(isStoreAdmin, clone.getIsStoreAdmin());
        assertEquals(language, clone.getLanguage());
        assertEquals(locationKey, clone.getLocationKey());
        assertEquals(name, clone.getName());
        assertEquals(storeKey, clone.getStoreKey());
        assertEquals(score, clone.getScore());
        assertEquals(twitterId, clone.getTwitterId());
    }

    @Test
    public void testJsonCommandsII() {
        SaleAssociate object = new SaleAssociate();

        object.resetLists();

        assertNull(object.getConsumerKey());
        assertNull(object.getCreatorKey());
        assertNull(object.getCriteria());
        assertNull(object.getLocationKey());
        assertNull(object.getStoreKey());
        assertNull(object.getScore());
        assertNull(object.getTwitterId());

        SaleAssociate clone = new SaleAssociate(object.toJson());

        assertNull(clone.getConsumerKey());
        assertNull(clone.getCreatorKey());
        assertEquals(0, clone.getCriteria().size()); // Not null because the clone object creation creates empty List<String>
        assertNull(clone.getLocationKey());
        assertNull(clone.getStoreKey());
        assertNull(clone.getScore());
        assertNull(clone.getTwitterId());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetPreferredConnection() {
        SaleAssociate object = new SaleAssociate();

        object.setPreferredConnection((Source) null);
    }

    @Test
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(SaleAssociate.SALEASSOCIATE_KEY, key);

        assertEquals(key, new SaleAssociate(parameters).getKey());
    }

    @Test
    public void testGetSerialized() {
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.addCriterion("one");
        saleAssociate.addCriterion("two");
        saleAssociate.addCriterion("three");

        assertEquals("one two three", saleAssociate.getSerializedCriteria());
    }
}
