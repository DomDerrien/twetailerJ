package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonParser;

public class TestSeed {

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

    static String key = "/c/r/c";
    static String city = "city";
    static String region = "region";
    static String country = "country";
    static String label = "label";
    static Long locationKey = 76543L;
    static Long storeKey = 5465L;

    @Test
    public void testConstructorI() {
        new Seed(city, region, country, label, storeKey);
    }

    @Test
    public void testConstructorII() throws JsonException {
        new Seed(new JsonParser("{}").getJsonObject());
    }

    @Test
    public void testAccessors() {
        Seed object = new Seed("c", "r", "c", "l", 0L); // Local attributes to be sure global will override them

        object.setKey(key);
        object.setCity(city);
        object.setCountry(country);
        object.setLabel(label);
        object.setLocationKey(locationKey);
        object.setRegion(region);
        object.setStoreKey(storeKey);

        assertEquals(key, object.getKey());
        assertEquals(city, object.getCity());
        assertEquals(country, object.getCountry());
        assertEquals(label, object.getLabel());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(region, object.getRegion());
        assertEquals(storeKey, object.getStoreKey());
    }

    @Test
    public void testJsonCommands() {
        Seed object = new Seed("c", "r", "c", "l", 0L); // Local attributes to be sure global will override them

        object.setKey(key);
        object.setCity(city);
        object.setCountry(country);
        object.setLabel(label);
        object.setLocationKey(locationKey);
        object.setRegion(region);
        object.setStoreKey(storeKey);

        Seed clone = new Seed(object.toJson());

        assertEquals(key, clone.getKey());
        assertEquals(city, clone.getCity());
        assertEquals(country, clone.getCountry());
        assertEquals(label, clone.getLabel());
        assertEquals(locationKey, clone.getLocationKey());
        assertEquals(region, clone.getRegion());
        assertEquals(storeKey, clone.getStoreKey());
    }

    @Test
    public void testSetCity() {
        Seed object = new Seed(city, region, country, label, storeKey);
        assertNull(object.getKey());

        object.setCity(null);
        assertNull(object.getCity());
        object.setCity("");
        assertNull(object.getCity());
    }

    @Test
    public void testSetCountry() {
        Seed object = new Seed(city, region, country, label, storeKey);
        assertNull(object.getKey());

        object.setCountry(null);
        assertNull(object.getCountry());
        object.setCountry("");
        assertNull(object.getCountry());
    }

    @Test
    public void testSetLabel() {
        Seed object = new Seed(city, region, country, label, storeKey);
        assertNull(object.getKey());

        object.setLabel(null);
        assertNull(object.getLabel());
        object.setLabel("");
        assertNull(object.getLabel());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetKey() {
        Seed object = new Seed(city, region, country, label, storeKey);
        assertNull(object.getKey());

        object.setKey(null);
    }

    @Test
    public void testSetRegion() {
        Seed object = new Seed(city, region, country, label, storeKey);
        assertNull(object.getKey());

        object.setRegion(null);
        assertNull(object.getRegion());
        object.setRegion("");
        assertNull(object.getRegion());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetStoreKey() {
        Seed object = new Seed(city, region, country, label, storeKey);
        assertNull(object.getKey());

        object.setStoreKey(null);
    }

    @Test
    public void testGenerateKey() {
        Seed object = new Seed(city, region, country, label, storeKey);
        assertNull(object.getKey());
        object.generateKey();
        assertTrue(KeyFactory.stringToKey(object.getKey()).toString().contains(object.buildQueryString()));
    }
}
