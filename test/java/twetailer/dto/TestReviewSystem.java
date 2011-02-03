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

public class TestReviewSystem {

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
        ReviewSystem object = new ReviewSystem();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorII() throws JsonException {
        new ReviewSystem(new JsonParser("{}").getJsonObject());
    }

    Long key = 1221L;
    String email = "a@a.aa";
    String name = "name";
    String url = "http://www.org";

    @Test
    public void testAccessors() {
        ReviewSystem object = new ReviewSystem();

        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        assertEquals(email, object.getEmail());
        assertEquals(name, object.getName());
        assertEquals(url, object.getUrl());
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        ReviewSystem object = new ReviewSystem();

        object.setKey(key);

        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        ReviewSystem clone = new ReviewSystem();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(email, clone.getEmail());
        assertEquals(name, clone.getName());
        assertEquals(url, clone.getUrl());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        ReviewSystem object = new ReviewSystem();

        ReviewSystem clone = new ReviewSystem();
        clone.fromJson(object.toJson(), true, true);

        assertNull(clone.getEmail());
        assertNull(clone.getName());
        assertNull(clone.getUrl());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        ReviewSystem object = new ReviewSystem();

        object.setKey(key);

        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        ReviewSystem clone = new ReviewSystem();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(email, clone.getEmail());
        assertEquals(name, clone.getName());
        assertEquals(url, clone.getUrl());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        ReviewSystem object = new ReviewSystem();

        // object.setKey(key);

        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        ReviewSystem clone = new ReviewSystem();
        clone.fromJson(object.toJson());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsV() {
        //
        // User update for an existing object (lowest)
        //
        ReviewSystem object = new ReviewSystem();

        object.setKey(key);

        object.setEmail(email);
        object.setName(name);
        object.setUrl(url);

        ReviewSystem clone = new ReviewSystem();
        clone.fromJson(object.toJson());
    }

    @Test
    public void testSetEmail() {
        ReviewSystem object = new ReviewSystem();

        object.setEmail(null);
        assertNull(object.getEmail());

        object.setEmail("");
        assertNull(object.getEmail());

        object.setEmail(email.toUpperCase());
        assertEquals(email, object.getEmail());
    }

    @Test
    public void testSetName() {
        ReviewSystem object = new ReviewSystem();

        object.setName(null);
        assertNull(object.getName());

        object.setName("");
        assertNull(object.getName());

        object.setName(name);
        assertEquals(name, object.getName());
    }

    @Test
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(ReviewSystem.REVIEW_SYSTEM_KEY, key);

        ReviewSystem object = new ReviewSystem();
        object.fromJson(parameters, true, true);

        assertEquals(key, object.getKey());
    }
}
