package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Location;
import twetailer.dto.Store;
import twetailer.task.RobotResponder;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestStoreOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        BaseSteps.resetOperationControllers(false); // Use helper!
        CacheHandler.injectCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        CacheHandler.injectCacheFactory(null);
        CacheHandler.injectCache(null);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureI() throws DataSourceException {
        StoreOperations ops = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, Store item) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createStore(new Store());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureII() throws ClientException {
        StoreOperations ops = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, Store item) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createStore(new GenericJsonObject());
    }

    @Test
    public void testCreateI() {
        Store item = new StoreOperations().createStore(new Store());
        assertNotNull(item.getKey());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        StoreOperations ops = new StoreOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public Store createStore(PersistenceManager pm, Store store) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.createStore(new Store());
    }

    @Test
    public void testCreateIII() throws ClientException {
        String name="test";
        JsonObject item = new GenericJsonObject();
        item.put(Store.NAME, name);

        Store store = new StoreOperations().createStore(item);
        assertNotNull(store.getKey());
        assertEquals(name, store.getName());
    }

    @Test
    public void testGetI() throws InvalidIdentifierException {
        StoreOperations ops = new StoreOperations();
        Store item = ops.createStore(new Store());

        Store selected = ops.getStore(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetII() throws InvalidIdentifierException {
        new StoreOperations().getStore(null);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIII() throws InvalidIdentifierException {
        new StoreOperations().getStore(0L);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIV() throws InvalidIdentifierException {
        new StoreOperations().getStore(888L);
    }

    @Test
    public void testGetsI() throws DataSourceException {
        Store item = new Store();
        item.setLocationKey(12345L);

        StoreOperations ops = new StoreOperations();
        item = ops.createStore(item);

        List<Store> selection = ops.getStores(Store.LOCATION_KEY, 12345L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(item.getKey(), selection.get(0).getKey());
    }

    @Test(expected=RuntimeException.class)
    public void testGetsII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        StoreOperations ops = new StoreOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public List<Store> getStores(PersistenceManager pm, String key, Object value, int limit) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.getStores("test", null, 0);
    }

    @Test
    public void testUpdateI() throws DataSourceException {
        Store item = new Store();
        item.setLocationKey(12345L);

        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        StoreOperations ops = new StoreOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        item = ops.createStore(pm, item); // Gives the PersistenceManager so it won't be closed

        item.setEmail("test@test.com");

        Store updated = ops.updateStore(item);
        assertNotNull(updated);
        assertEquals(item.getKey(), updated.getKey());
        assertEquals(item.getEmail(), updated.getEmail());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        StoreOperations ops = new StoreOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public Store updateStore(PersistenceManager pm, Store store) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.updateStore(new Store());
    }

    @Test
    public void testGetsExtendedI() throws InvalidIdentifierException, DataSourceException {
        //
        // Get all stores from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        StoreOperations ops = new StoreOperations();

        Store first = new Store();
        first.setLocationKey(where.getKey());
        first = ops.createStore(first);

        Store second = new Store();
        second.setLocationKey(where.getKey());
        second = ops.createStore(second);

        first = ops.getStore(first.getKey());
        second = ops.getStore(second.getKey());

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Store> selection = ops.getStores(places, 0);
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertTrue (selection.get(0).getKey().equals(first.getKey()) && selection.get(1).getKey().equals(second.getKey()) ||
                selection.get(1).getKey().equals(first.getKey()) && selection.get(0).getKey().equals(second.getKey()));
        // assertEquals(first.getKey(), selection.get(1).getKey()); // Should be second because of ordered by descending date
        // assertEquals(second.getKey(), selection.get(0).getKey()); // but dates are so closed that sometimes first is returned first...
    }

    @Test
    public void testGetsExtendedII() throws DataSourceException, InvalidIdentifierException {
        //
        // Get just one store from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        StoreOperations ops = new StoreOperations();

        Store first = new Store();
        first.setLocationKey(where.getKey());
        first = ops.createStore(first);
        first.getKey();

        Store second = new Store();
        second.setLocationKey(where.getKey());
        second = ops.createStore(second);

        first = ops.getStore(first.getKey());
        second = ops.getStore(second.getKey());

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Store> selection = ops.getStores(places, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        // assertEquals(first.getKey(), selection.get(0).getKey()); // Should be second because of ordered by descending date
        // assertEquals(second.getKey(), selection.get(0).getKey()); // but dates are so closed that sometimes first is returned first...
    }

    @Test
    public void testGetsExtendedIII() throws DataSourceException, InvalidIdentifierException {
        //
        // Get limited number of stores from many locations
        //
        LocationOperations lOps = new LocationOperations();
        StoreOperations sOps = new StoreOperations();

        Location lFirst = new Location();
        lFirst.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        lFirst.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        lFirst = lOps.createLocation(lFirst);

        Store sFirst = new Store();
        sFirst.setLocationKey(lFirst.getKey());
        sFirst = sOps.createStore(sFirst);

        Location lSecond = new Location();
        lSecond.setPostalCode("H1H1H1");
        lSecond.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        lSecond = lOps.createLocation(lSecond);

        Store sSecond = new Store();
        sSecond.setLocationKey(lSecond.getKey());
        sOps.createStore(sSecond);

        Store sThird = new Store();
        sThird.setLocationKey(lSecond.getKey());
        sOps.createStore(sThird);

        sFirst = sOps.getStore(sFirst.getKey());
        sSecond = sOps.getStore(sSecond.getKey());
        sThird = sOps.getStore(sThird.getKey());

        List<Location> places = new ArrayList<Location>();
        places.add(lFirst);
        places.add(lSecond);

        List<Store> selection = sOps.getStores(places, 2); // Should cut to 2 items
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertEquals(sFirst.getKey(), selection.get(0).getKey());
        // No more test because it appears sometimes sSecond comes back, sometimes sThird comes back
        // FIXME: re-insert the test for sSecond in the returned list when we're sure the issue related ordering on inherited attribute is fixed.
    }

    @Test(expected=RuntimeException.class)
    public void testGetsExtendedIV() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        StoreOperations ops = new StoreOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.getStores(new ArrayList<Location>(), 0);
    }

    @Test
    public void testGetKeysI() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        StoreOperations ops = new StoreOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Store object = new Store();
        object.setName("name");
        object = ops.createStore(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Long> selection = ops.getStoreKeys(pm, Store.NAME, "name", 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetKeysII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        List<Long> selection = new StoreOperations().getStoreKeys(pm, Store.NAME, "name", 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteWithFailureI() throws DataSourceException, InvalidIdentifierException {
        StoreOperations ops = new StoreOperations() {
            @Override
            public void deleteStore(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.deleteStore(12345L);
    }

    @Test
    public void testDeleteI() throws DataSourceException, InvalidIdentifierException {
        final Long storeKey = 54657L;
        StoreOperations ops = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
            @Override
            public void deleteStore(PersistenceManager pm, Store item) {
                assertEquals(storeKey, item.getKey());
            }
        };
        ops.deleteStore(storeKey);
    }

    @Test
    public void testDeleteII() throws DataSourceException, InvalidIdentifierException {
        final String name = "name";
        Store toBeCreated = new Store();
        toBeCreated.setName(name);
        StoreOperations ops = new StoreOperations();
        Store justCreated = ops.createStore(toBeCreated);
        assertNotNull(justCreated.getKey());
        assertEquals(name, justCreated.getName());
        ops.deleteStore(justCreated.getKey());
    }

    @Test
    public void testGetsFromMapI() throws DataSourceException {
        StoreOperations ops = new StoreOperations();

        Store object = new Store();
        object.setLocationKey(12345L);
        object = ops.createStore(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Store.LOCATION_KEY, object.getLocationKey());

        List<Store> selection = ops.getStores(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetKeysFromMapI() throws DataSourceException {
        StoreOperations ops = new StoreOperations();

        Store object = new Store();
        object.setLocationKey(12345L);
        object = ops.createStore(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Store.LOCATION_KEY, object.getLocationKey());

        List<Long> selection = ops.getStoreKeys(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetsFromKeysI() throws DataSourceException {
        StoreOperations ops = new StoreOperations();

        Store object = new Store();
        object.setLocationKey(12345L);
        object = ops.createStore(object);

        List<Long> parameters = new ArrayList<Long>();
        parameters.add(object.getKey());

        List<Store> selection = ops.getStores(ops.getPersistenceManager(), parameters);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetKeysExtendedI() throws InvalidIdentifierException, DataSourceException {
        //
        // Get all stores from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        StoreOperations ops = new StoreOperations();

        Store first = new Store();
        first.setLocationKey(where.getKey());
        first = ops.createStore(first);

        Store second = new Store();
        second.setLocationKey(where.getKey());
        second = ops.createStore(second);

        first = ops.getStore(first.getKey());
        second = ops.getStore(second.getKey());

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Long> selection = ops.getStoreKeys(ops.getPersistenceManager(), new HashMap<String, Object>(), places, 0);
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertTrue (selection.get(0).equals(first.getKey()) && selection.get(1).equals(second.getKey()) ||
                selection.get(1).equals(first.getKey()) && selection.get(0).equals(second.getKey()));
        // assertEquals(first.getKey(), selection.get(1).getKey()); // Should be second because of ordered by descending date
        // assertEquals(second.getKey(), selection.get(0).getKey()); // but dates are so closed that sometimes first is returned first...
    }

    @Test
    public void testGetKeysExtendedII() throws InvalidIdentifierException, DataSourceException {
        //
        // Get all stores from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        StoreOperations ops = new StoreOperations();

        Store first = new Store();
        first.setLocationKey(where.getKey());
        first = ops.createStore(first);

        Location also = new Location();
        also.setPostalCode("A0A0A0");
        also.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        also = new LocationOperations().createLocation(also);

        Store second = new Store();
        second.setLocationKey(also.getKey());
        second = ops.createStore(second);

        first = ops.getStore(first.getKey());
        second = ops.getStore(second.getKey());

        List<Location> places = new ArrayList<Location>();
        places.add(where);
        places.add(also);

        List<Long> selection = ops.getStoreKeys(ops.getPersistenceManager(), new HashMap<String, Object>(), places, 2);
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertTrue (selection.get(0).equals(first.getKey()) && selection.get(1).equals(second.getKey()) ||
                selection.get(1).equals(first.getKey()) && selection.get(0).equals(second.getKey()));
        // assertEquals(first.getKey(), selection.get(1).getKey()); // Should be second because of ordered by descending date
        // assertEquals(second.getKey(), selection.get(0).getKey()); // but dates are so closed that sometimes first is returned first...
    }
}
