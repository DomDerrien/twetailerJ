package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import twetailer.dto.Demand;
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

public class TestDemandOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        BaseSteps.resetOperationControllers(false); // Use helper!
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureI() throws DataSourceException {
        DemandOperations ops = new DemandOperations() {
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createDemand(new Demand());
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand item = new Demand();
        assertNull(item.getKey());

        item = ops.createDemand(item);
        assertNotNull(item.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testCreateII() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 111L);

        Demand object = new DemandOperations().createDemand(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test(expected=ClientException.class)
    public void testCreateIII() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 222L);

        Demand object = new DemandOperations().createDemand(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test
    public void testCreateIV() throws ClientException {
        JsonObject item = new GenericJsonObject();
        // Not setting the "OWNER_KEY" attribute will let it as null

        Demand object = new DemandOperations().createDemand(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test
    public void testCreateV() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 0L);

        Demand object = new DemandOperations().createDemand(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test
    public void testGetI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        Demand selected = ops.getDemand(object.getKey(), 111L);
        assertNotNull(selected.getKey());
        assertEquals(object.getKey(), selected.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIIa() throws ClientException, DataSourceException {
        DemandOperations ops = new DemandOperations();
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(object);

        ops.getDemand(object.getKey(), 222L);
    }

    @Test
    public void testGetIIb() throws ClientException, DataSourceException {
        DemandOperations ops = new DemandOperations();
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(object);

        ops.getDemand(object.getKey(), null);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIIc() throws ClientException, DataSourceException {
        DemandOperations ops = new DemandOperations();
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object.setState(State.markedForDeletion);
        object = ops.createDemand(object);

        ops.getDemand(object.getKey(), null);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIII() throws InvalidIdentifierException {
        DemandOperations ops = new DemandOperations();
        ops.getDemand(null, 111L);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIV() throws InvalidIdentifierException {
        DemandOperations ops = new DemandOperations();
        ops.getDemand(0L, 111L);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetV() throws InvalidIdentifierException {
        DemandOperations ops = new DemandOperations();
        ops.getDemand(888L, 111L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetsWithFailureI() throws DataSourceException {
        DemandOperations ops = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.getDemands("key", "value", 4324);
    }

    @Test
    public void testGetsI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Demand> selection = ops.getDemands(Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsII() throws ClientException, DataSourceException {
        List<Demand> selection = new DemandOperations().getDemands(Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        DemandOperations ops = new DemandOperations() {
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.updateDemand(new Demand());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureII() throws InvalidIdentifierException {
        DemandOperations ops = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.updateDemand(new GenericJsonObject(), 543543543L);
    }

    @Test
    public void testUpdateI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed
        object.setOwnerKey(222L);

        Demand updated = ops.updateDemand(object);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testUpdateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        JsonObject item = new GenericJsonObject();
        item.put(Entity.KEY, object.getKey());
        item.put(Command.OWNER_KEY, 111L);

        Demand updated = ops.updateDemand(item, 111L);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteWithFailureI() throws InvalidIdentifierException {
        DemandOperations ops = new DemandOperations() {
            @Override
            public void deleteDemand(PersistenceManager pm, Long key, Long owner) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.deleteDemand(53543L, 76767L);
    }

    @Test
    public void testDeleteI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        ops.deleteDemand(object.getKey(), 111L);
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsExtendedI() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, 111L);

        List<Demand> selection = ops.getDemands(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedII() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object.setOwnerKey(111L);
        object.setRange(25.5D);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Demand.RANGE, object.getRange());

        List<Demand> selection = ops.getDemands(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedIII() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object.setOwnerKey(111L);
        object.setRange(25.5D);
        object.setState(CommandSettings.State.opened);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Demand.RANGE, object.getRange());
        parameters.put(Command.STATE, object.getState().toString());

        List<Demand> selection = ops.getDemands(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedIV() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object.setOwnerKey(111L);
        object.setRange(25.5D);
        object.setState(CommandSettings.State.opened);
        object.setLocationKey(222L);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Demand.RANGE, object.getRange());
        parameters.put(Command.STATE, object.getState().toString());
        parameters.put(Demand.LOCATION_KEY, object.getLocationKey());

        List<Demand> selection = ops.getDemands(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetKeysI() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Long> selection = ops.getDemandKeys(pm, Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetKeysII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        List<Long> selection = new DemandOperations().getDemandKeys(pm, Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test
    public void testGetsAroundLocationI() throws InvalidIdentifierException, DataSourceException {
        //
        // Get all demands from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        DemandOperations ops = new DemandOperations();

        final Long ownerKey = 45678L;
        Demand first = new Demand();
        first.setLocationKey(where.getKey());
        first.setOwnerKey(ownerKey);
        first = ops.createDemand(first);

        Demand second = new Demand();
        second.setLocationKey(where.getKey());
        second.setOwnerKey(ownerKey);
        second = ops.createDemand(second);

        first = ops.getDemand(first.getKey(), null);
        second = ops.getDemand(second.getKey(), null);

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Demand> selection = ops.getDemands(places, 0);
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
        // Get just one demand from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        DemandOperations ops = new DemandOperations();

        final Long ownerKey = 45678L;
        Demand first = new Demand();
        first.setLocationKey(where.getKey());
        first.setOwnerKey(ownerKey);
        first = ops.createDemand(first);

        Demand second = new Demand();
        second.setLocationKey(where.getKey());
        second.setOwnerKey(ownerKey);
        second = ops.createDemand(second);

        first = ops.getDemand(first.getKey(), null);
        second = ops.getDemand(second.getKey(), null);

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Demand> selection = ops.getDemands(places, 1);
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
        // Get limited number of demands from many locations
        //
        LocationOperations lOps = new LocationOperations();
        DemandOperations sOps = new DemandOperations();

        Location lFirst = new Location();
        lFirst.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        lFirst.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        lFirst = lOps.createLocation(lFirst);

        final Long ownerKey = 45678L;
        Demand sFirst = new Demand();
        sFirst.setLocationKey(lFirst.getKey());
        sFirst.setOwnerKey(ownerKey);
        sFirst = sOps.createDemand(sFirst);

        Location lSecond = new Location();
        lSecond.setPostalCode("H1H1H1");
        lSecond.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        lSecond = lOps.createLocation(lSecond);

        Demand sSecond = new Demand();
        sSecond.setLocationKey(lSecond.getKey());
        sSecond.setOwnerKey(ownerKey);
        sOps.createDemand(sSecond);

        Demand sThird = new Demand();
        sThird.setLocationKey(lSecond.getKey());
        sThird.setOwnerKey(ownerKey);
        sOps.createDemand(sThird);

        sFirst = sOps.getDemand(sFirst.getKey(), null);
        sSecond = sOps.getDemand(sSecond.getKey(), null);
        sThird = sOps.getDemand(sThird.getKey(), null);

        List<Location> places = new ArrayList<Location>();
        places.add(lFirst);
        places.add(lSecond);

        List<Demand> selection = sOps.getDemands(places, 2); // Should cut to 2 items
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertEquals(sFirst.getKey(), selection.get(0).getKey());
        // No more test because it appears sometimes sSecond comes back, sometimes sThird comes back
        // FIXME: re-insert the test for sSecond in the returned list when we're sure the issue related ordering on inherited attribute is fixed.
    }

    @Test(expected=RuntimeException.class)
    public void testGetsAroundLocationIV() throws DataSourceException {
        //
        // Get demands fails
        //
        DemandOperations sOps = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                throw new RuntimeException("Done in purpose!");
            }
        };

        sOps.getDemands((List<Location>) null, 0);
    }

    @Test
    public void testGetKeysFromMapI() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object.setOwnerKey(111L);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Demand.OWNER_KEY, object.getOwnerKey());

        List<Long> selection = ops.getDemandKeys(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetsFromKeysI() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object = ops.createDemand(object);

        List<Long> parameters = new ArrayList<Long>();
        parameters.add(object.getKey());

        List<Demand> selection = ops.getDemands(ops.getPersistenceManager(), parameters);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }
}
