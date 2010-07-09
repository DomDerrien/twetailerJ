package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

public class TestSaleAssociate {

    private static LocalServiceTestHelper helper;
    private static Collator collator = LocaleValidator.getCollator(Locale.ENGLISH);

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
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
    Boolean isStoreAdmin = Boolean.TRUE;
    Long locationKey = 12345L;
    Long storeKey = 54321L;
    Long score = 5L;

    @Test
    public void testAccessors() {
        SaleAssociate object = new SaleAssociate();

        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        object.setCriteria(criteria, collator);
        object.setIsStoreAdmin(isStoreAdmin);
        object.setLocationKey(locationKey);
        object.setStoreKey(storeKey);
        object.setScore(score);

        assertEquals(consumerKey, object.getConsumerKey());
        assertEquals(creatorKey, object.getCreatorKey());
        assertEquals(criteria, object.getCriteria());
        assertEquals(isStoreAdmin, object.getIsStoreAdmin());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(storeKey, object.getStoreKey());
        assertEquals(score, object.getScore());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetCriteriaI() {
        SaleAssociate object = new SaleAssociate();

        object.addCriterion(null, collator);
        assertEquals(0, object.getCriteria().size());

        object.addCriterion("", collator);
        assertEquals(0, object.getCriteria().size());

        object.addCriterion("first", collator);
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("first", collator); // Add it twice
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("FiRsT", collator); // Add it twice, mixed case
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("second", collator);
        assertEquals(2, object.getCriteria().size());

        object.removeCriterion("first", collator); // Remove first
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("Troisième", collator);
        assertEquals(2, object.getCriteria().size());

        object.addCriterion("TROISIÈME", collator);
        assertEquals(2, object.getCriteria().size());

        object.removeCriterion("TROISIÈME", collator); // Remove mixed case and disparate accents
        assertEquals(1, object.getCriteria().size());

        object.removeCriterion(null, collator);
        assertEquals(1, object.getCriteria().size());

        object.removeCriterion("", collator);
        assertEquals(1, object.getCriteria().size());

        object.resetCriteria(); // Reset all
        assertEquals(0, object.getCriteria().size());

        object.setCriteria(null, collator); // Failure!
    }

    @Test
    public void testResetCriteriaII() {
        SaleAssociate object = new SaleAssociate();

        object.resetLists(); // To force the criteria list creation
        object.addCriterion("first", collator);
        assertEquals(1, object.getCriteria().size());

        object.resetLists(); // To be sure there's no error
        object.removeCriterion("first", collator); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetCriteria(); // Reset all
    }

    @Test
    public void testJsonCommandsI() {
        SaleAssociate object = new SaleAssociate();

        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        object.setCriteria(new ArrayList<String>(), collator); // Only null or empty list can be transfered, actual supplied keywords must be transfered manually
        object.setIsStoreAdmin(isStoreAdmin);
        object.setLocationKey(locationKey);
        object.setStoreKey(storeKey);
        object.setScore(score);

        SaleAssociate clone = new SaleAssociate(object.toJson());

        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(creatorKey, clone.getCreatorKey());
        assertEquals(0, clone.getCriteria().size());
        assertEquals(isStoreAdmin, clone.getIsStoreAdmin());
        assertEquals(locationKey, clone.getLocationKey());
        assertEquals(storeKey, clone.getStoreKey());
        assertEquals(score, clone.getScore());
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

        SaleAssociate clone = new SaleAssociate(object.toJson());

        assertNull(clone.getConsumerKey());
        assertNull(clone.getCreatorKey());
        assertEquals(0, clone.getCriteria().size()); // Not null because the clone object creation creates empty List<String>
        assertNull(clone.getLocationKey());
        assertNull(clone.getStoreKey());
        assertNull(clone.getScore());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsIII() {
        SaleAssociate object = new SaleAssociate();

        object.setCriteria(criteria, collator); // Supplied keywords must be transfered manually

        new SaleAssociate(object.toJson());
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
        saleAssociate.addCriterion("one", collator);
        saleAssociate.addCriterion("two", collator);
        saleAssociate.addCriterion("three", collator);

        assertEquals("one two three", saleAssociate.getSerializedCriteria());
    }

    @Test
    public void testGetIsStoreAdmin() {
        SaleAssociate saleAssociate = new SaleAssociate();
        assertFalse(saleAssociate.getIsStoreAdmin());
        saleAssociate.setIsStoreAdmin(null);
        assertFalse(saleAssociate.getIsStoreAdmin());
        saleAssociate.setIsStoreAdmin(false);
        assertFalse(saleAssociate.getIsStoreAdmin());
        saleAssociate.setIsStoreAdmin(true);
        assertTrue(saleAssociate.getIsStoreAdmin());
    }
}
