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
        Payment object = new Payment();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

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
    public void testToJsonI() {
        Payment object = new Payment();

        object.setAuthorizationId(authorizationId);
        object.setReference(reference);
        object.setRequestId(requestId);
        object.setTransactionId(transactionId);

        JsonObject json = object.toJson();

        assertEquals(authorizationId, json.getString(Payment.AUTHORIZATION_ID));
        assertEquals(reference, json.getString(Payment.REFERENCE));
        assertEquals(requestId, json.getString(Payment.REQUEST_ID));
        assertEquals(transactionId, json.getString(Payment.TRANSACTION_ID));
    }

    @Test
    public void testToJsonII() {
        Payment object = new Payment();

        JsonObject json = object.toJson();

        assertNull(json.getString(Payment.AUTHORIZATION_ID));
        assertNull(json.getString(Payment.REFERENCE));
        assertNull(json.getString(Payment.REQUEST_ID));
        assertNull(json.getString(Payment.TRANSACTION_ID));
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
}
