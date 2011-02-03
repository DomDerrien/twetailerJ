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
import twetailer.dto.ReviewSystem;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestReviewSystemOperations {

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
        new ReviewSystemOperations() {
            @Override
            public ReviewSystem createReviewSystem(PersistenceManager pm, ReviewSystem reviewSystem) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.createReviewSystem(new ReviewSystem());
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ReviewSystemOperations ops = new ReviewSystemOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        ReviewSystem item = new ReviewSystem();
        assertNull(item.getKey());

        item = ops.createReviewSystem(item);
        assertNotNull(item.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetWithFailureI() throws InvalidIdentifierException {
        new ReviewSystemOperations().getReviewSystem(543543L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetWithFailureII() throws InvalidIdentifierException {
        new ReviewSystemOperations() {
            @Override
            public ReviewSystem getReviewSystem(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.getReviewSystem(1234L);
    }

    @Test
    public void testGetI() throws InvalidIdentifierException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ReviewSystemOperations ops = new ReviewSystemOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        String name = "name";
        ReviewSystem item = new ReviewSystem();
        item.setName(name);
        item = ops.createReviewSystem(pm, item);
        assertNotNull(item.getKey());

        ReviewSystem retreived = ops.getReviewSystem(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getName(), retreived.getName());
    }

    @Test
    public void testGetII() throws InvalidIdentifierException {
        ReviewSystem item = new ReviewSystemOperations().getReviewSystem(null);
        assertEquals("AnotherSocialEconomy.com", item.getName());
    }

    @Test
    public void testGetIII() throws InvalidIdentifierException {
        ReviewSystem item = new ReviewSystemOperations().getReviewSystem(0L);
        assertEquals("AnotherSocialEconomy.com", item.getName());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        new ReviewSystemOperations(){
            @Override
            public ReviewSystem updateReviewSystem(PersistenceManager pm, ReviewSystem reviewSystem) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.updateReviewSystem(new ReviewSystem());
    }

    @Test
    public void testUpdateI() throws InvalidIdentifierException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ReviewSystemOperations ops = new ReviewSystemOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        String name = "name";
        ReviewSystem item = new ReviewSystem();
        item.setName(name);
        item = ops.createReviewSystem(pm, item);
        assertNotNull(item.getKey());

        ReviewSystem retreived = ops.getReviewSystem(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getName(), retreived.getName());

        item.setName("updated!");
        item = ops.updateReviewSystem(pm, item);

        assertEquals("updated!", item.getName());
    }

/**** ddd *****
    @Test
    public void testGetsI() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        RegistrarOperations ops = new RegistrarOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        String name = "name";
        Registrar item = new Registrar();
        item.setName(name);
        item = ops.createRegistrar(pm, item);
        assertNotNull(item.getKey());

        List<Long> keys = new ArrayList<Long>();
        keys.add(item.getKey());
        List<Registrar> retreived = ops.getRegistrars(pm, keys);

        assertEquals(1, retreived.size());
        assertEquals(item.getKey(), retreived.get(0).getKey());
        assertEquals(item.getName(), retreived.get(0).getName());
    }

    @Test
    public void testGetsII() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        RegistrarOperations ops = new RegistrarOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        String name = "name";
        Registrar item = new Registrar();
        item.setName(name);
        item = ops.createRegistrar(pm, item);
        assertNotNull(item.getKey());

        List<Long> keys = new ArrayList<Long>();
        keys.add(0L);
        keys.add(item.getKey());
        List<Registrar> retreived = ops.getRegistrars(pm, keys);

        assertEquals(2, retreived.size());
        assertEquals(item.getKey(), retreived.get(0).getKey());
        assertEquals(item.getName(), retreived.get(0).getName());
        assertNull(retreived.get(1).getKey());
        assertEquals("AnotherSocialEconomy.com", retreived.get(1).getName());
    }

    @Test
    public void testGetsIII() throws DataSourceException {
        List<Long> keys = new ArrayList<Long>();
        keys.add(0L);
        List<Registrar> retreived = new RegistrarOperations().getRegistrars(null, keys);

        assertEquals(1, retreived.size());
        assertNull(retreived.get(0).getKey());
        assertEquals("AnotherSocialEconomy.com", retreived.get(0).getName());
    }

    @Test
    public void testGetsIV() throws DataSourceException {
        List<Long> keys = new ArrayList<Long>();
        keys.add(0L);
        List<Registrar> retreived = new RegistrarOperations().getRegistrars(null, new ArrayList<Long>());

        assertEquals(0, retreived.size());
    }
***** ddd *****/
}
