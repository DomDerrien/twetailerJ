package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Location;
import twetailer.dto.Store;
import twetailer.task.RobotResponder;

import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestStoreOperations {

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseOperations.setLogger(new MockLogger("test", null));
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testGetLogger() throws IOException {
        Logger log1 = new StoreOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new StoreOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
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
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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
    public void testGetI() throws DataSourceException {
        StoreOperations ops = new StoreOperations();
        Store item = ops.createStore(new Store());

        Store selected = ops.getStore(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetII() throws DataSourceException {
        new StoreOperations().getStore(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIII() throws DataSourceException {
        new StoreOperations().getStore(0L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIV() throws DataSourceException {
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
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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
    public void testGetsExtendedI() throws DataSourceException {
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
    public void testGetsExtendedII() throws DataSourceException {
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
    public void testGetsExtendedIII() throws DataSourceException {
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
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        StoreOperations ops = new StoreOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.getStores(new ArrayList<Location>(), 0);
    }

    @Test
    public void testGetKeysI() throws DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        List<Long> selection = new StoreOperations().getStoreKeys(pm, Store.NAME, "name", 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteWithFailureI() throws DataSourceException {
        StoreOperations ops = new StoreOperations() {
            @Override
            public void deleteStore(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.deleteStore(12345L);
    }

    @Test
    public void testDeleteI() throws DataSourceException {
        final Long storeKey = 54657L;
        StoreOperations ops = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws DataSourceException {
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
    public void testDeleteII() throws DataSourceException {
        final String name = "name";
        Store toBeCreated = new Store();
        toBeCreated.setName(name);
        StoreOperations ops = new StoreOperations();
        Store justCreated = ops.createStore(toBeCreated);
        assertNotNull(justCreated.getKey());
        assertEquals(name, justCreated.getName());
        ops.deleteStore(justCreated.getKey());
    }
}
