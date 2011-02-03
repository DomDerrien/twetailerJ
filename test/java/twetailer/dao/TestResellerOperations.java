package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Reseller;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestResellerOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        BaseSteps.resetOperationControllers(false); // Use helper!
        CacheHandler.injectMockCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureI() throws DataSourceException {
        new ResellerOperations() {
            @Override
            public Reseller createReseller(PersistenceManager pm, Reseller reseller) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.createReseller(new Reseller());
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ResellerOperations ops = new ResellerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Reseller item = new Reseller();
        assertNull(item.getKey());

        item = ops.createReseller(item);
        assertNotNull(item.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetWithFailureI() throws InvalidIdentifierException {
        new ResellerOperations().getReseller(543543L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetWithFailureII() throws InvalidIdentifierException {
        new ResellerOperations() {
            @Override
            public Reseller getReseller(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.getReseller(1234L);
    }

    @Test
    public void testGetI() throws InvalidIdentifierException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ResellerOperations ops = new ResellerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        Reseller item = new Reseller();
        item.setTokenNb(2132L);
        item = ops.createReseller(pm, item);
        assertNotNull(item.getKey());

        Reseller retreived = ops.getReseller(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getTokenNb(), retreived.getTokenNb());
    }

    @Test
    public void testGetII() throws InvalidIdentifierException {
        Reseller item = new ResellerOperations().getReseller(null);
        assertEquals(1000000000L, item.getTokenNb().longValue());
    }

    @Test
    public void testGetIII() throws InvalidIdentifierException {
        Reseller item = new ResellerOperations().getReseller(0L);
        assertEquals(1000000000L, item.getTokenNb().longValue());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        new ResellerOperations(){
            @Override
            public Reseller updateReseller(PersistenceManager pm, Reseller reseller) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.updateReseller(new Reseller());
    }

    @Test
    public void testUpdateI() throws InvalidIdentifierException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ResellerOperations ops = new ResellerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        Reseller item = new Reseller();
        item.setTokenNb(12345L);
        item = ops.createReseller(pm, item);
        assertNotNull(item.getKey());

        Reseller retreived = ops.getReseller(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getTokenNb(), retreived.getTokenNb());

        item.setTokenNb(45678L);
        item = ops.updateReseller(pm, item);

        assertEquals(45678L, item.getTokenNb().longValue());
    }
}
