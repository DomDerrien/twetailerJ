package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonParser;

public class TestReseller {

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
        Reseller object = new Reseller();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorII() throws JsonException {
        new Reseller(new JsonParser("{}").getJsonObject());
    }

    Long key = 1221L;
    Long consumerKey = 3243L;
    Long tokenNb = 543543L;

    @Test
    public void testAccessors() {
        Reseller object = new Reseller();

        object.setConsumerKey(consumerKey);
        object.setTokenNb(tokenNb);

        assertEquals(consumerKey, object.getConsumerKey());
        assertEquals(tokenNb, object.getTokenNb());
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Reseller object = new Reseller();

        object.setKey(key);

        object.setConsumerKey(consumerKey);
        object.setTokenNb(tokenNb);

        Reseller clone = new Reseller();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(tokenNb, clone.getTokenNb());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Reseller object = new Reseller();

        Reseller clone = new Reseller();
        clone.fromJson(object.toJson(), true, true);

        assertNull(clone.getConsumerKey());
        assertNull(clone.getTokenNb());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Reseller object = new Reseller();

        object.setKey(key);

        object.setConsumerKey(consumerKey);
        object.setTokenNb(tokenNb);

        Reseller clone = new Reseller();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(tokenNb, clone.getTokenNb());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        Reseller object = new Reseller();

        // object.setKey(key);

        object.setConsumerKey(consumerKey);
        object.setTokenNb(tokenNb);

        Reseller clone = new Reseller();
        clone.fromJson(object.toJson());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsV() {
        //
        // User update for an existing object (lowest)
        //
        Reseller object = new Reseller();

        object.setKey(key);

        object.setConsumerKey(consumerKey);
        object.setTokenNb(tokenNb);

        Reseller clone = new Reseller();
        clone.fromJson(object.toJson());
    }
}
