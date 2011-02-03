package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.dto.Store.State;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestStore {

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

    Long key = 7654365L;
    Long closedProposalNb = 6453222L;
    String address = "North Pole, H0H 0H0, Canada";
    String email = "d.d@d.dom";
    Double latitude = 45.0D;
    Double longitude = -27.5D;
    String name = "dom";
    String phoneNumber = "514-123-4567 #890";
    Long publishedProposalNb = 645645L;
    Long registrarKey = 65324354L;
    Long reviewSystemKey = 76547095L;
    State state = State.waiting;
    String url = "http://unit-test.org";

    @Test
    public void testAccessors() {
        Store object = new Store();

        object.setKey(key);

        object.setAddress(address);
        object.setClosedProposalNb(closedProposalNb);
        object.setEmail(email);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setRegistrarKey(registrarKey);
        object.setReviewSystemKey(reviewSystemKey);
        object.setState(state);
        object.setState(state.toString());
        object.setUrl(url);

        assertEquals(key, object.getKey());

        assertEquals(address, object.getAddress());
        assertEquals(closedProposalNb, object.getClosedProposalNb());
        assertEquals(email, object.getEmail());
        assertEquals(latitude, object.getLatitude());
        assertEquals(longitude, object.getLongitude());
        assertEquals(name, object.getName());
        assertEquals(phoneNumber, object.getPhoneNumber());
        assertEquals(publishedProposalNb, object.getPublishedProposalNb());
        assertEquals(registrarKey, object.getRegistrarKey());
        assertEquals(reviewSystemKey, object.getReviewSystemKey());
        assertEquals(state, object.getState());
        assertEquals(url, object.getUrl());
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Store object = new Store();

        object.setKey(key);

        object.setAddress(address);
        object.setClosedProposalNb(closedProposalNb);
        object.setEmail(email);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setRegistrarKey(registrarKey);
        object.setReviewSystemKey(reviewSystemKey);
        object.setState(state);
        object.setUrl(url);

        Store clone = new Store();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(address, clone.getAddress());
        assertEquals(closedProposalNb, clone.getClosedProposalNb());
        assertEquals(email, clone.getEmail());
        assertEquals(latitude, clone.getLatitude());
        assertEquals(longitude, clone.getLongitude());
        assertEquals(name, clone.getName());
        assertEquals(phoneNumber, clone.getPhoneNumber());
        assertEquals(publishedProposalNb, clone.getPublishedProposalNb());
        assertEquals(registrarKey, clone.getRegistrarKey());
        assertEquals(reviewSystemKey, clone.getReviewSystemKey());
        assertEquals(state, clone.getState());
        assertEquals(url, clone.getUrl());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Store object = new Store();

        Store clone = new Store();
        clone.fromJson(object.toJson(), true, true);

        assertNull(clone.getAddress());
        assertEquals(0L, clone.getClosedProposalNb().longValue());
        assertNull(clone.getEmail());
        assertEquals(Location.INVALID_COORDINATE, clone.getLatitude());
        assertEquals(Location.INVALID_COORDINATE, clone.getLongitude());
        assertNull(clone.getName());
        assertNull(clone.getPhoneNumber());
        assertEquals(0L, clone.getPublishedProposalNb().longValue());
        assertNull(clone.getRegistrarKey());
        assertNull(clone.getReviewSystemKey());
        assertEquals(State.referenced, clone.getState());
        assertNull(clone.getUrl());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Store object = new Store();

        object.setKey(key);

        object.setAddress(address);
        object.setClosedProposalNb(closedProposalNb);
        object.setEmail(email);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setRegistrarKey(registrarKey);
        object.setReviewSystemKey(reviewSystemKey);
        object.setState(state);
        object.setUrl(url);

        Store clone = new Store();
        clone.fromJson(object.toJson(), true, false);

        assertEquals(key, clone.getKey());

        assertEquals(address, clone.getAddress());
        assertEquals(closedProposalNb, clone.getClosedProposalNb());
        assertEquals(email, clone.getEmail());
        assertEquals(latitude, clone.getLatitude());
        assertEquals(longitude, clone.getLongitude());
        assertEquals(name, clone.getName());
        assertEquals(phoneNumber, clone.getPhoneNumber());
        assertEquals(publishedProposalNb, clone.getPublishedProposalNb());
        assertEquals(registrarKey, clone.getRegistrarKey());
        assertEquals(reviewSystemKey, clone.getReviewSystemKey());
        assertEquals(state, clone.getState());
        assertEquals(url, clone.getUrl());
    }

    @Test
    public void testJsonCommandsIV() {
        //
        // User update (lower)
        //
        Store object = new Store();

        // object.setKey(key);

        object.setAddress(address);
        object.setClosedProposalNb(closedProposalNb);
        object.setEmail(email);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setName(name);
        object.setPhoneNumber(phoneNumber);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setRegistrarKey(registrarKey);
        object.setReviewSystemKey(reviewSystemKey);
        object.setState(state);
        object.setUrl(url);

        Store clone = new Store();
        clone.fromJson(object.toJson());

        assertEquals(address, clone.getAddress());
        assertNull(clone.getClosedProposalNb());
        assertEquals(email, clone.getEmail());
        assertEquals(latitude, clone.getLatitude());
        assertEquals(longitude, clone.getLongitude());
        assertEquals(name, clone.getName());
        assertEquals(phoneNumber, clone.getPhoneNumber());
        assertNull(clone.getPublishedProposalNb());
        assertNull(clone.getRegistrarKey());
        assertEquals(reviewSystemKey, clone.getReviewSystemKey());
        assertEquals(State.referenced, clone.getState());
        assertEquals(url, clone.getUrl());
    }

    @Test
    public void testJsonCommandsV() {
        //
        // User update (lower)
        //
        JsonObject json = new GenericJsonObject();

        Store clone = new Store();
        clone.fromJson(json, true, true);

        assertNull(clone.getClosedProposalNb());
        assertNull(clone.getPublishedProposalNb());
        assertEquals(State.referenced, clone.getState());
    }

    @Test
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Store.STORE_KEY, key);

        assertEquals(key, new Store(parameters).getKey());
    }

    @Test
    public void testGetAddress() {
        Store store = new Store();
        store.setAddress(null);
        assertNull(store.getAddress());
        store.setAddress("");
        assertNull(store.getAddress());
        store.setAddress("test");
        assertEquals("test", store.getAddress());
    }

    @Test
    public void testGetEmail() {
        Store store = new Store();
        store.setEmail(null);
        assertNull(store.getEmail());
        store.setEmail("");
        assertNull(store.getEmail());
        store.setEmail("test");
        assertEquals("test", store.getEmail());
    }

    @Test
    public void testGetName() {
        Store store = new Store();
        store.setName(null);
        assertNull(store.getName());
        store.setName("");
        assertNull(store.getName());
        store.setName("test");
        assertEquals("test", store.getName());
    }

    @Test
    public void testGetPhoneNumber() {
        Store store = new Store();
        store.setPhoneNumber(null);
        assertNull(store.getPhoneNumber());
        store.setPhoneNumber("");
        assertNull(store.getPhoneNumber());
        store.setPhoneNumber("test");
        assertEquals("test", store.getPhoneNumber());
    }

    @Test
    public void testSetLatitude() {
        Store object = new Store();

        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());

        object.setLatitude(null);
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());

        object.setLatitude(90.00001);
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());

        object.setLatitude(-90.00001);
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());
    }

    @Test
    public void testSetLongitude() {
        Store object = new Store();

        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());

        object.setLongitude(null);
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());

        object.setLongitude(180.00001);
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());

        object.setLongitude(-180.00001);
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());
    }
}
