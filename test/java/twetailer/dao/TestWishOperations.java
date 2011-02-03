package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import twetailer.dto.Command;
import twetailer.dto.Wish;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.task.RobotResponder;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestWishOperations {

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
        WishOperations ops = new WishOperations() {
            @Override
            public Wish createWish(PersistenceManager pm, Wish wish) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createWish(new Wish());
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        WishOperations ops = new WishOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Wish item = new Wish();
        assertNull(item.getKey());

        item = ops.createWish(item);
        assertNotNull(item.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testCreateII() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 111L);

        Wish object = new WishOperations().createWish(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test(expected=ClientException.class)
    public void testCreateIII() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 222L);

        new WishOperations().createWish(item, 111L);
    }

    @Test
    public void testCreateIV() throws ClientException {
        JsonObject item = new GenericJsonObject();
        // Not setting the "OWNER_KEY" attribute will let it as null

        Wish object = new WishOperations().createWish(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test
    public void testCreateV() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 0L);

        Wish object = new WishOperations().createWish(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test
    public void testGetI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        WishOperations ops = new WishOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(pm, object); // Gives the PersistenceManager so it won't be closed

        Wish selected = ops.getWish(object.getKey(), 111L);
        assertNotNull(selected.getKey());
        assertEquals(object.getKey(), selected.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIIa() throws ClientException, DataSourceException {
        WishOperations ops = new WishOperations();
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(object);

        ops.getWish(object.getKey(), 222L);
    }

    @Test
    public void testGetIIb() throws ClientException, DataSourceException {
        WishOperations ops = new WishOperations();
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(object);

        ops.getWish(object.getKey(), null);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIIc() throws ClientException, DataSourceException {
        WishOperations ops = new WishOperations();
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object.setState(State.markedForDeletion);
        object = ops.createWish(object);

        ops.getWish(object.getKey(), null);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIII() throws InvalidIdentifierException {
        WishOperations ops = new WishOperations();
        ops.getWish(null, 111L);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIV() throws InvalidIdentifierException {
        WishOperations ops = new WishOperations();
        ops.getWish(0L, 111L);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetV() throws InvalidIdentifierException {
        WishOperations ops = new WishOperations();
        ops.getWish(888L, 111L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetsWithFailureI() throws DataSourceException {
        WishOperations ops = new WishOperations() {
            @Override
            public List<Wish> getWishes(PersistenceManager pm, String key, Object value, int limit) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.getWishes("key", "value", 4324);
    }

    @Test
    public void testGetsI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        WishOperations ops = new WishOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Wish> selection = ops.getWishes(Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsII() throws ClientException, DataSourceException {
        List<Wish> selection = new WishOperations().getWishes(Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        WishOperations ops = new WishOperations() {
            @Override
            public Wish updateWish(PersistenceManager pm, Wish wish) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.updateWish(new Wish());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureII() throws InvalidIdentifierException {
        WishOperations ops = new WishOperations() {
            @Override
            public Wish getWish(PersistenceManager pm, Long key, Long ownerKey) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.updateWish(new GenericJsonObject(), 543543543L);
    }

    @Test
    public void testUpdateI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        WishOperations ops = new WishOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(pm, object); // Gives the PersistenceManager so it won't be closed
        object.setOwnerKey(222L);

        Wish updated = ops.updateWish(object);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testUpdateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        WishOperations ops = new WishOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(pm, object); // Gives the PersistenceManager so it won't be closed

        JsonObject item = new GenericJsonObject();
        item.put(Entity.KEY, object.getKey());
        item.put(Command.OWNER_KEY, 111L);

        Wish updated = ops.updateWish(item, 111L);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteWithFailureI() throws InvalidIdentifierException {
        WishOperations ops = new WishOperations() {
            @Override
            public void deleteWish(PersistenceManager pm, Long key, Long owner) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.deleteWish(53543L, 76767L);
    }

    @Test
    public void testDeleteI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        WishOperations ops = new WishOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(pm, object); // Gives the PersistenceManager so it won't be closed

        ops.deleteWish(object.getKey(), 111L);
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsExtendedI() throws DataSourceException {
        WishOperations ops = new WishOperations();

        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, 111L);

        List<Wish> selection = ops.getWishes(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedII() throws DataSourceException {
        WishOperations ops = new WishOperations();

        Wish object = new Wish();
        object.setOwnerKey(111L);
        object.setRange(25.5D);
        object = ops.createWish(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Wish.RANGE, object.getRange());

        List<Wish> selection = ops.getWishes(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedIII() throws DataSourceException {
        WishOperations ops = new WishOperations();

        Wish object = new Wish();
        object.setOwnerKey(111L);
        object.setRange(25.5D);
        object.setState(CommandSettings.State.opened);
        object = ops.createWish(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Wish.RANGE, object.getRange());
        parameters.put(Command.STATE, object.getState().toString());

        List<Wish> selection = ops.getWishes(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedIV() throws DataSourceException {
        WishOperations ops = new WishOperations();

        Wish object = new Wish();
        object.setOwnerKey(111L);
        object.setRange(25.5D);
        object.setState(CommandSettings.State.opened);
        object.setLocationKey(222L);
        object = ops.createWish(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Wish.RANGE, object.getRange());
        parameters.put(Command.STATE, object.getState().toString());
        parameters.put(Wish.LOCATION_KEY, object.getLocationKey());

        List<Wish> selection = ops.getWishes(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetKeysI() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        WishOperations ops = new WishOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Long> selection = ops.getWishKeys(pm, Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetKeysII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        List<Long> selection = new WishOperations().getWishKeys(pm, Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test
    public void testGetsAroundLocationI() throws InvalidIdentifierException, DataSourceException {
        //
        // Get all Wishes from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        WishOperations ops = new WishOperations();

        final Long ownerKey = 45678L;
        Wish first = new Wish();
        first.setLocationKey(where.getKey());
        first.setOwnerKey(ownerKey);
        first = ops.createWish(first);

        Wish second = new Wish();
        second.setLocationKey(where.getKey());
        second.setOwnerKey(ownerKey);
        second = ops.createWish(second);

        first = ops.getWish(first.getKey(), null);
        second = ops.getWish(second.getKey(), null);

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Wish> selection = ops.getWishes(places, 0);
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertTrue (selection.get(0).getKey().equals(first.getKey()) && selection.get(1).getKey().equals(second.getKey()) ||
                selection.get(1).getKey().equals(first.getKey()) && selection.get(0).getKey().equals(second.getKey()));
        // assertEquals(first.getKey(), selection.get(1).getKey()); // Should be second because of ordered by descending date
        // assertEquals(second.getKey(), selection.get(0).getKey()); // but dates are so closed that sometines first is returned first...
    }

    @Test
    public void testGetsAroundLocationII() throws InvalidIdentifierException, DataSourceException {
        //
        // Get just one Wish from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        WishOperations ops = new WishOperations();

        final Long ownerKey = 45678L;
        Wish first = new Wish();
        first.setLocationKey(where.getKey());
        first.setOwnerKey(ownerKey);
        first = ops.createWish(first);

        Wish second = new Wish();
        second.setLocationKey(where.getKey());
        second.setOwnerKey(ownerKey);
        second = ops.createWish(second);

        first = ops.getWish(first.getKey(), null);
        second = ops.getWish(second.getKey(), null);

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Wish> selection = ops.getWishes(places, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertTrue (selection.get(0).getKey().equals(first.getKey()) ||
                selection.get(0).getKey().equals(second.getKey()));
        // assertEquals(first.getKey(), selection.get(1).getKey()); // Should be second because of ordered by descending date
        // assertEquals(second.getKey(), selection.get(0).getKey()); // but dates are so closed that sometines first is returned first...
    }

    @Test
    public void testGetsAroundLocationIII() throws DataSourceException, InvalidIdentifierException {
        //
        // Get limited number of Wishes from many locations
        //
        LocationOperations lOps = new LocationOperations();
        WishOperations sOps = new WishOperations();

        Location lFirst = new Location();
        lFirst.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        lFirst.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        lFirst = lOps.createLocation(lFirst);

        final Long ownerKey = 45678L;
        Wish sFirst = new Wish();
        sFirst.setLocationKey(lFirst.getKey());
        sFirst.setOwnerKey(ownerKey);
        sFirst = sOps.createWish(sFirst);

        Location lSecond = new Location();
        lSecond.setPostalCode("H1H1H1");
        lSecond.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        lSecond = lOps.createLocation(lSecond);

        Wish sSecond = new Wish();
        sSecond.setLocationKey(lSecond.getKey());
        sSecond.setOwnerKey(ownerKey);
        sOps.createWish(sSecond);

        Wish sThird = new Wish();
        sThird.setLocationKey(lSecond.getKey());
        sThird.setOwnerKey(ownerKey);
        sOps.createWish(sThird);

        sFirst = sOps.getWish(sFirst.getKey(), null);
        sSecond = sOps.getWish(sSecond.getKey(), null);
        sThird = sOps.getWish(sThird.getKey(), null);

        List<Location> places = new ArrayList<Location>();
        places.add(lFirst);
        places.add(lSecond);

        List<Wish> selection = sOps.getWishes(places, 2); // Should cut to 2 items
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertEquals(sFirst.getKey(), selection.get(0).getKey());
        // No more test because it appears sometimes sSecond comes back, sometimes sThird comes back
        // FIXME: re-insert the test for sSecond in the returned list when we're sure the issue related ordering on inherited attribute is fixed.
    }

    @Test(expected=RuntimeException.class)
    public void testGetsAroundLocationIV() throws DataSourceException {
        //
        // Get Wishes fails
        //
        WishOperations sOps = new WishOperations() {
            @Override
            public List<Wish> getWishes(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                throw new RuntimeException("Done in purpose!");
            }
        };

        sOps.getWishes((List<Location>) null, 0);
    }

    @Test
    public void testGetKeysFromMapI() throws DataSourceException {
        WishOperations ops = new WishOperations();

        Wish object = new Wish();
        object.setOwnerKey(111L);
        object = ops.createWish(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Wish.OWNER_KEY, object.getOwnerKey());

        List<Long> selection = ops.getWishKeys(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetsFromKeysI() throws DataSourceException {
        WishOperations ops = new WishOperations();

        Wish object = new Wish();
        object = ops.createWish(object);

        List<Long> parameters = new ArrayList<Long>();
        parameters.add(object.getKey());

        List<Wish> selection = ops.getWishes(ops.getPersistenceManager(), parameters);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }
}
