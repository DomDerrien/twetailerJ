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

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestRegistrar {

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
        Registrar object = new Registrar();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorII() throws JsonException {
        new Registrar(new JsonParser("{}").getJsonObject());
    }

    Long key = 1221L;
    Long consumerKey = 3243L;
    String email = "a@a.aa";
    String name = "name";
    String url = "http://www.org";

    @Test
    public void testAccessors() {
        Registrar object = new Registrar();

        object.setConsumerKey(consumerKey);
        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        assertEquals(consumerKey, object.getConsumerKey());
        assertEquals(email, object.getEmail());
        assertEquals(name, object.getName());
        assertEquals(url, object.getUrl());
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Registrar object = new Registrar();

        object.setKey(key);

        object.setConsumerKey(consumerKey);
        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        Registrar clone = new Registrar();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(email, clone.getEmail());
        assertEquals(name, clone.getName());
        assertEquals(url, clone.getUrl());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Registrar object = new Registrar();

        Registrar clone = new Registrar();
        clone.fromJson(object.toJson(), true, true);

        assertNull(clone.getConsumerKey());
        assertNull(clone.getEmail());
        assertNull(clone.getName());
        assertNull(clone.getUrl());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Registrar object = new Registrar();

        object.setKey(key);

        object.setConsumerKey(consumerKey);
        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        Registrar clone = new Registrar();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(email, clone.getEmail());
        assertEquals(name, clone.getName());
        assertEquals(url, clone.getUrl());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        Registrar object = new Registrar();

        // object.setKey(key);

        object.setConsumerKey(consumerKey);
        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        Registrar clone = new Registrar();
        clone.fromJson(object.toJson());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsV() {
        //
        // User update for an existing object (lowest)
        //
        Registrar object = new Registrar();

        object.setKey(key);

        object.setConsumerKey(consumerKey);
        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        Registrar clone = new Registrar();
        clone.fromJson(object.toJson());
    }

    @Test
    public void testSetShortcut() {
        JsonObject json = new GenericJsonObject();
        json.put(Registrar.REGISTRAR_KEY, key);

        Registrar object = new Registrar();
        object.fromJson(json, true, true);

        assertEquals(key, object.getKey());
    }

    @Test
    public void testSetEmail() {
        Registrar object = new Registrar();

        object.setEmail(null);
        assertNull(object.getEmail());

        object.setEmail("");
        assertNull(object.getEmail());

        object.setEmail(email.toUpperCase());
        assertEquals(email, object.getEmail());
    }

    @Test
    public void testSetName() {
        Registrar object = new Registrar();

        object.setName(null);
        assertNull(object.getName());

        object.setName("");
        assertNull(object.getName());

        object.setName(name);
        assertEquals(name, object.getName());
    }
}
