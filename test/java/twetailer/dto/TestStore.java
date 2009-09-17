package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.dao.BaseOperations;
import twetailer.dao.MockAppEngineEnvironment;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonParser;

public class TestStore {

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
        Store object = new Store();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Store object = new Store(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    String address = "North Pole, H0H 0H0, Canada";
    String email = "d.d@d.dom";
    Long locationKey = 12345L;
    String name = "dom";
    String phoneNumber = "514-123-4567 #890";

    @Test
    public void testAccessors() {
        Store object = new Store();

        object.setAddress(address);
        object.setEmail(email);
        object.setLocationKey(locationKey);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);

        assertEquals(address, object.getAddress());
        assertEquals(email, object.getEmail());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(name, object.getName());
        assertEquals(phoneNumber, object.getPhoneNumber());
    }

    @Test
    public void testJsonCommandsI() {
        Store object = new Store();

        object.setAddress(address);
        object.setEmail(email);
        object.setLocationKey(locationKey);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);

        Store clone = new Store(object.toJson());

        assertEquals(address, clone.getAddress());
        assertEquals(email, clone.getEmail());
        assertEquals(locationKey, clone.getLocationKey());
        assertEquals(name, clone.getName());
        assertEquals(phoneNumber, clone.getPhoneNumber());
    }

    @Test
    public void testJsonCommandsII() {
        Store object = new Store();

        assertNull(object.getLocationKey());

        Store clone = new Store(object.toJson());

        assertNull(clone.getLocationKey());
    }
}
