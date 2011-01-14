package twetailer.dto;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestConsumer {

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
    String facebookId = "64554364532";
    String jabberId = "ddd";
    String language = Locale.FRENCH.getLanguage();
    Long locationKey = 12345L;
    String name = "dom";
    String openID = "http://dom.my-openid.org";
    String phoneNumber = "514-123-4567 #890";
    Source preferredConnection = Source.jabber;
    Long saleAssociateKey = 76543453L;
    String twitterId = "Katelyn";

    @Test
    public void testAccessors() {
        Consumer object = new Consumer();

        object.setAddress(address);
        object.setEmail(email);
        object.setJabberId(jabberId);
        object.setLanguage(language);
        object.setLocationKey(locationKey);
        object.setName(name);
        object.setOpenID(openID);
        object.setPhoneNumber(phoneNumber);
        object.setPreferredConnection(preferredConnection);
        object.setSaleAssociateKey(saleAssociateKey);
        object.setTwitterId(twitterId);

        assertEquals(address, object.getAddress());
        assertEquals(email, object.getEmail());
        assertEquals(jabberId, object.getJabberId());
        assertEquals(language, object.getLanguage());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(name, object.getName());
        assertEquals(openID, object.getOpenID());
        assertEquals(phoneNumber, object.getPhoneNumber());
        assertEquals(preferredConnection, object.getPreferredConnection());
        assertEquals(saleAssociateKey, object.getSaleAssociateKey());
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
        object.setJabberId(jabberId);
        object.setFacebookId(facebookId);
        object.setLanguage(language);
        object.setLocationKey(locationKey);
        object.setName(name);
        object.setOpenID(openID);
        object.setPhoneNumber(phoneNumber);
        object.setPreferredConnection(preferredConnection);
        object.setSaleAssociateKey(saleAssociateKey);
        object.setTwitterId(twitterId);

        Consumer clone = new Consumer(object.toJson());

        assertEquals(address, clone.getAddress());
        assertNull(clone.getClosedDemandNb()); // Cannot be overridden
        assertEquals(email, clone.getEmail());
        assertEquals(facebookId, clone.getFacebookId());
        assertEquals(jabberId, clone.getJabberId());
        assertEquals(language, clone.getLanguage());
        assertEquals(locationKey, clone.getLocationKey());
        assertEquals(name, clone.getName());
        assertNull(clone.getOpenID()); // Cannot be overridden
        assertEquals(phoneNumber, clone.getPhoneNumber());
        assertEquals(preferredConnection, clone.getPreferredConnection());
        assertNull(clone.getPublishedDemandNb()); // Cannot be overridden
        assertEquals(saleAssociateKey, clone.getSaleAssociateKey()); // Cannot be overridden
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

    @Test
    public void testSetAddress() {
        Consumer object = new Consumer();
        assertNull(object.getAddress());
        object.setAddress(address);
        assertEquals(address, object.getAddress());
        object.setAddress("");
        assertNull(object.getAddress());
        object.setAddress(address);
        assertEquals(address, object.getAddress());
        object.setAddress(null);
        assertNull(object.getAddress());
    }

    @Test
    public void testSetEmailId() {
        String emailId = "SuperGénial";
        Consumer object = new Consumer();
        assertNull(object.getEmail());
        object.setEmail(emailId);
        assertEquals(emailId.toLowerCase(), object.getEmail());
        object.setEmail("");
        assertNull(object.getEmail());
        object.setEmail(emailId);
        assertEquals(emailId.toLowerCase(), object.getEmail());
        object.setEmail(null);
        assertNull(object.getEmail());
    }

    @Test
    public void testSetJabberId() {
        String jabberId = "SuperGénial";
        Consumer object = new Consumer();
        assertNull(object.getJabberId());
        object.setJabberId(jabberId);
        assertEquals(jabberId.toLowerCase(), object.getJabberId());
        object.setJabberId("");
        assertNull(object.getJabberId());
        object.setJabberId(jabberId);
        assertEquals(jabberId.toLowerCase(), object.getJabberId());
        object.setJabberId(null);
        assertNull(object.getJabberId());
    }

    @Test
    public void testSetName() {
        String name = "SuperGénial";
        Consumer object = new Consumer();
        assertNull(object.getName());
        object.setName(name);
        assertEquals(name, object.getName());
        object.setName("");
        assertNull(object.getName());
        object.setName(name);
        assertEquals(name, object.getName());
        object.setName(null);
        assertNull(object.getName());
    }

    @Test
    public void testSetOpenID() {
        String openID = "SuperGénial";
        Consumer object = new Consumer();
        assertNull(object.getOpenID());
        object.setOpenID(openID);
        assertEquals(openID, object.getOpenID());
        object.setOpenID("");
        assertNull(object.getOpenID());
        object.setOpenID(openID);
        assertEquals(openID, object.getOpenID());
        object.setOpenID(null);
        assertNull(object.getOpenID());
    }

    @Test
    public void testSetPhoneNumber() {
        String phoneNumber = "SuperGénial";
        Consumer object = new Consumer();
        assertNull(object.getPhoneNumber());
        object.setPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, object.getPhoneNumber());
        object.setPhoneNumber("");
        assertNull(object.getPhoneNumber());
        object.setPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, object.getPhoneNumber());
        object.setPhoneNumber(null);
        assertNull(object.getPhoneNumber());
    }

    @Test
    public void testSetTwitterId() {
        String twitterId = "SuperGénial";
        Consumer object = new Consumer();
        assertNull(object.getTwitterId());
        object.setTwitterId(twitterId);
        assertEquals(twitterId, object.getTwitterId());
        object.setTwitterId("");
        assertNull(object.getTwitterId());
        object.setTwitterId(twitterId);
        assertEquals(twitterId, object.getTwitterId());
        object.setTwitterId(null);
        assertNull(object.getTwitterId());
    }

    @Test
    public void testGetAutomaticLocaleUpdate() {
        Consumer consumer = new Consumer();
        assertTrue(consumer.getAutomaticLocaleUpdate());
        consumer.setAutomaticLocaleUpdate(null);
        assertFalse(consumer.getAutomaticLocaleUpdate());
        consumer.setAutomaticLocaleUpdate(false);
        assertFalse(consumer.getAutomaticLocaleUpdate());
        consumer.setAutomaticLocaleUpdate(true);
        assertTrue(consumer.getAutomaticLocaleUpdate());
    }

    @Test
    public void testSetPreferredConnection() {
        Consumer object = new Consumer();
        assertEquals(Source.mail, object.getPreferredConnection());
        object.setPreferredConnection(Source.jabber);
        assertEquals(Source.jabber, object.getPreferredConnection());
        object.setPreferredConnection((Source) null);
        assertEquals(Source.mail, object.getPreferredConnection());
    }

    @Test
    public void testLimitedFromJson() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Entity.KEY, key);
        parameters.put(Consumer.OPEN_ID, "test");
        parameters.put(Consumer.SALE_ASSOCIATE_KEY, 4365443L);

        Consumer object = new Consumer(parameters);
        assertEquals(key, object.getKey());
        assertNull(object.getOpenID());
        assertNull(object.getSaleAssociateKey());
    }
}
