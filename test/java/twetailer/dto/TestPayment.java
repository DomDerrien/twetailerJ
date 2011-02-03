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
import domderrien.jsontools.JsonObject;

public class TestPayment {

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
        Payment object = new Payment();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Long key = 54423453L;
    String authorizationId = "<trelkjdjsahrs/sdadse/wq>";
    String reference = "4435243.4343.65765";
    String requestId = "64654";
    String transactionId = "7653454";

    @Test
    public void testAccessors() {
        Payment object = new Payment();

        object.setAuthorizationId(authorizationId);
        object.setReference(reference);
        object.setRequestId(requestId);
        object.setTransactionId(transactionId);

        assertEquals(authorizationId, object.getAuthorizationId());
        assertEquals(reference, object.getReference());
        assertEquals(requestId, object.getRequestId());
        assertEquals(transactionId, object.getTransactionId());
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Payment object = new Payment();

        object.setKey(key);

        object.setAuthorizationId(authorizationId);
        object.setReference(reference);
        object.setRequestId(requestId);
        object.setTransactionId(transactionId);

        Payment clone = new Payment();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, object.getKey());

        assertEquals(authorizationId, object.getAuthorizationId());
        assertEquals(reference, object.getReference());
        assertEquals(requestId, object.getRequestId());
        assertEquals(transactionId, object.getTransactionId());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Payment object = new Payment();

        Payment clone = new Payment();
        clone.fromJson(object.toJson(), true, true);

        assertNull(object.getAuthorizationId());
        assertNull(object.getReference());
        assertNull(object.getRequestId());
        assertNull(object.getTransactionId());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Payment object = new Payment();

        object.setKey(key);

        object.setAuthorizationId(authorizationId);
        object.setReference(reference);
        object.setRequestId(requestId);
        object.setTransactionId(transactionId);

        Payment clone = new Payment();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, object.getKey());

        assertEquals(authorizationId, object.getAuthorizationId());
        assertEquals(reference, object.getReference());
        assertEquals(requestId, object.getRequestId());
        assertEquals(transactionId, object.getTransactionId());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsIV() {
        //
        // User update (lower)
        //
        new Payment().fromJson(null);
    }

    @Test
    public void testTransactionIdGeneration() {
        Long consumerKey = 1L;
        Long demandKey = 2L;
        Long proposalKey = 3L;

        String transactionId = Payment.getReference(1L, 2L, 3L);
        assertNotNull(transactionId);

        Long[] keys = Payment.getKeys(transactionId);
        assertEquals(consumerKey, keys[0]);
        assertEquals(demandKey, keys[1]);
        assertEquals(proposalKey, keys[2]);

        JsonObject json = Payment.keysToJson(transactionId, new GenericJsonObject());

        assertEquals(consumerKey.longValue(), json.getLong("consumerKey"));
        assertEquals(demandKey.longValue(), json.getLong("demandKey"));
        assertEquals(proposalKey.longValue(), json.getLong("proposalKey"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullifyAuthorizationIdI() {
        new Payment().setAuthorizationId(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullifyAuthorizationIdII() {
        new Payment().setAuthorizationId("");
    }

    @Test
    public void testNullifyAuthorizationIdIII() {
        new Payment().setAuthorizationId("good!");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullifyReferenceI() {
        new Payment().setReference(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullifyReferenceII() {
        new Payment().setReference("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullifyRequestIdI() {
        new Payment().setRequestId(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullifyRequestIdII() {
        new Payment().setRequestId("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullifyTransactionIdI() {
        new Payment().setTransactionId(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullifyTransactionIdII() {
        new Payment().setTransactionId("");
    }
}
