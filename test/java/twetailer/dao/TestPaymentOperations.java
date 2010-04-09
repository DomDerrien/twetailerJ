package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.logging.Logger;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.dto.Payment;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestPaymentOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseOperations.setLogger(new MockLogger("test", null));
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
    public void testGetLogger() throws IOException {
        Logger log1 = new PaymentOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new PaymentOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureI() throws DataSourceException {
        new PaymentOperations() {
            @Override
            public Payment createPayment(PersistenceManager pm, Payment payment) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.createPayment(new Payment());
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        PaymentOperations ops = new PaymentOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Payment item = new Payment();
        assertNull(item.getKey());

        item = ops.createPayment(item);
        assertNotNull(item.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=DataSourceException.class)
    public void testGetWithFailureI() throws DataSourceException {
        new PaymentOperations().getPayment(543543L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetWithFailureII() throws DataSourceException {
        new PaymentOperations().getPayment(null);
    }

    @Test(expected=RuntimeException.class)
    public void testGetWithFailureIII() throws DataSourceException {
        new PaymentOperations().getPayment(0L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetWithFailureIV() throws DataSourceException {
        new PaymentOperations() {
            @Override
            public Payment getPayment(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.getPayment(1234L);
    }

    @Test
    public void testGetI() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        PaymentOperations ops = new PaymentOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        String authorizationId = "<trejxkjfeouyt/ugxhqwkgiet/jcxbdsrj4y65876i8fjdsqle4TRTYTRGFdsdmsad>";
        Payment item = new Payment();
        item.setAuthorizationId(authorizationId);
        item = ops.createPayment(pm, item);
        assertNotNull(item.getKey());

        Payment retreived = ops.getPayment(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getAuthorizationId(), retreived.getAuthorizationId());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        new PaymentOperations(){
            @Override
            public Payment updatePayment(PersistenceManager pm, Payment payment) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.updatePayment(new Payment());
    }

    @Test
    public void testUpdateI() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        PaymentOperations ops = new PaymentOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        String authorizationId = "<trejxkjfeouyt/ugxhqwkgiet/jcxbdsrj4y65876i8fjdsqle4TRTYTRGFdsdmsad>";
        Payment item = new Payment();
        item.setAuthorizationId(authorizationId);
        item = ops.createPayment(pm, item);
        assertNotNull(item.getKey());

        Payment retreived = ops.getPayment(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getAuthorizationId(), retreived.getAuthorizationId());

        item.setAuthorizationId("good!");
        item = ops.updatePayment(pm, item);

        assertEquals("good!", item.getAuthorizationId());
    }
}
