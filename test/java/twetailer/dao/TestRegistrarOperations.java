package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Registrar;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestRegistrarOperations {

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
        new RegistrarOperations() {
            @Override
            public Registrar createRegistrar(PersistenceManager pm, Registrar registrar) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.createRegistrar(new Registrar());
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        RegistrarOperations ops = new RegistrarOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Registrar item = new Registrar();
        assertNull(item.getKey());

        item = ops.createRegistrar(item);
        assertNotNull(item.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetWithFailureI() throws InvalidIdentifierException {
        new RegistrarOperations().getRegistrar(543543L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetWithFailureII() throws InvalidIdentifierException {
        new RegistrarOperations() {
            @Override
            public Registrar getRegistrar(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.getRegistrar(1234L);
    }

    @Test
    public void testGetI() throws InvalidIdentifierException {
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

        Registrar retreived = ops.getRegistrar(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getName(), retreived.getName());
    }

    @Test
    public void testGetII() throws InvalidIdentifierException {
        Registrar item = new RegistrarOperations().getRegistrar(null);
        assertEquals("AnotherSocialEconomy.com", item.getName());
    }

    @Test
    public void testGetIII() throws InvalidIdentifierException {
        Registrar item = new RegistrarOperations().getRegistrar(0L);
        assertEquals("AnotherSocialEconomy.com", item.getName());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        new RegistrarOperations(){
            @Override
            public Registrar updateRegistrar(PersistenceManager pm, Registrar registrar) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.updateRegistrar(new Registrar());
    }

    @Test
    public void testUpdateI() throws InvalidIdentifierException {
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

        Registrar retreived = ops.getRegistrar(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getName(), retreived.getName());

        item.setName("updated!");
        item = ops.updateRegistrar(pm, item);

        assertEquals("updated!", item.getName());
    }

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
}
