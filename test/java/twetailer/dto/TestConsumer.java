package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.dao.BaseOperations;
import twetailer.dao.MockAppEngineEnvironment;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestConsumer {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();

        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testConstructorI() {
        Consumer object = new Consumer();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Consumer object = new Consumer(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    String address = "North Pole, H0H 0H0, Canada";
    String email = "d.d@d.dom";
    String imId = "ddd";
    String language = Locale.FRENCH.getLanguage();
    Long locationKey = 12345L;
    String name = "dom";
    String phoneNumber = "514-123-4567 #890";
    String twitterId = "Katelyn";

    @Test
    public void testAccessors() {
        Consumer object = new Consumer();

        object.setAddress(address);
        object.setEmail(email);
        object.setJabberId(imId);
        object.setLanguage(language);
        object.setLocationKey(locationKey);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);
        object.setTwitterId(twitterId);

        assertEquals(address, object.getAddress());
        assertEquals(email, object.getEmail());
        assertEquals(imId, object.getJabberId());
        assertEquals(language, object.getLanguage());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(name, object.getName());
        assertEquals(phoneNumber, object.getPhoneNumber());
        assertEquals(twitterId, object.getTwitterId());
    }

    @Test
    public void testGetLocale() {
        Consumer object = new Consumer();
        object.setLanguage(language);
        assertEquals(Locale.FRENCH, object.getLocale());
    }

    @Test
    public void testJsonCommandsI() {
        Consumer object = new Consumer();

        object.setAddress(address);
        object.setEmail(email);
        object.setJabberId(imId);
        object.setLanguage(language);
        object.setLocationKey(locationKey);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);
        object.setTwitterId(twitterId);

        Consumer clone = new Consumer(object.toJson());

        assertEquals(address, clone.getAddress());
        assertEquals(email, clone.getEmail());
        assertEquals(imId, clone.getJabberId());
        assertEquals(language, clone.getLanguage());
        assertEquals(locationKey, clone.getLocationKey());
        assertEquals(name, clone.getName());
        assertEquals(phoneNumber, clone.getPhoneNumber());
        assertEquals(twitterId, clone.getTwitterId());
    }

    @Test
    public void testJsonCommandsII() {
        Consumer object = new Consumer();

        assertNull(object.getLocationKey());
        assertNull(object.getTwitterId());

        Consumer clone = new Consumer(object.toJson());

        assertNull(clone.getLocationKey());
        assertNull(clone.getTwitterId());
    }

    @Test
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.CONSUMER_KEY, key);

        assertEquals(key, new Consumer(parameters).getKey());
    }
}
