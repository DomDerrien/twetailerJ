package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.validator.CommandSettings;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestDemandOperations {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
        mockAppEngineEnvironment.setUp();

        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testGetLogger() throws IOException {
        Logger log1 = new DemandOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new DemandOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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
        item.put(Demand.CONSUMER_KEY, 111L);

        Demand object = new DemandOperations().createDemand(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test(expected=ClientException.class)
    public void testCreateIII() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Demand.CONSUMER_KEY, 222L);

        Demand object = new DemandOperations().createDemand(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test
    public void testCreateIV() throws ClientException {
        JsonObject item = new GenericJsonObject();
        // Not setting the "CONSUMER_KEY" attribute will let it as null

        Demand object = new DemandOperations().createDemand(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test
    public void testCreateV() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Demand.CONSUMER_KEY, 0L);

        Demand object = new DemandOperations().createDemand(item, 111L);
        assertNotNull(object.getKey());
    }

    @Test
    public void testGetI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setConsumerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        Demand selected = ops.getDemand(object.getKey(), 111L);
        assertNotNull(selected.getKey());
        assertEquals(object.getKey(), selected.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=DataSourceException.class)
    public void testGetIIa() throws ClientException, DataSourceException {
        DemandOperations ops = new DemandOperations();
        Demand object = new Demand();
        object.setConsumerKey(111L);
        object = ops.createDemand(object);

        ops.getDemand(object.getKey(), 222L);
    }

    @Test
    public void testGetIIb() throws ClientException, DataSourceException {
        DemandOperations ops = new DemandOperations();
        Demand object = new Demand();
        object.setConsumerKey(111L);
        object = ops.createDemand(object);

        ops.getDemand(object.getKey(), null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIII() throws ClientException, DataSourceException {
        DemandOperations ops = new DemandOperations();
        ops.getDemand(null, 111L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIV() throws ClientException, DataSourceException {
        DemandOperations ops = new DemandOperations();
        ops.getDemand(0L, 111L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetV() throws ClientException, DataSourceException {
        DemandOperations ops = new DemandOperations();
        ops.getDemand(888L, 111L);
    }

    @Test
    public void testGetsI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setConsumerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Demand> selection = ops.getDemands(Demand.CONSUMER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsII() throws ClientException, DataSourceException {
        List<Demand> selection = new DemandOperations().getDemands(Demand.CONSUMER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test
    public void testUpdateI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setConsumerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed
        object.setConsumerKey(222L);

        Demand updated = ops.updateDemand(object);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testUpdateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setConsumerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        JsonObject item = new GenericJsonObject();
        item.put(Entity.KEY, object.getKey());
        item.put(Demand.CONSUMER_KEY, 111L);

        Demand updated = ops.updateDemand(item, 111L);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testDeleteI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        DemandOperations ops = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Demand object = new Demand();
        object.setConsumerKey(111L);
        object = ops.createDemand(pm, object); // Gives the PersistenceManager so it won't be closed

        ops.deleteDemand(object.getKey(), 111L);
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsExtendedI() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object.setConsumerKey(111L);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Demand.CONSUMER_KEY, 111L);

        List<Demand> selection = ops.getDemands(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedII() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object.setConsumerKey(111L);
        object.setRange(25.5D);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Demand.CONSUMER_KEY, object.getConsumerKey());
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
        object.setConsumerKey(111L);
        object.setRange(25.5D);
        object.setState(CommandSettings.State.open);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Demand.CONSUMER_KEY, object.getConsumerKey());
        parameters.put(Demand.RANGE, object.getRange());
        parameters.put(Demand.STATE, object.getState().toString());

        List<Demand> selection = ops.getDemands(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedIV() throws DataSourceException {
        DemandOperations ops = new DemandOperations();

        Demand object = new Demand();
        object.setConsumerKey(111L);
        object.setRange(25.5D);
        object.setState(CommandSettings.State.open);
        object.setLocationKey(222L);
        object = ops.createDemand(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Demand.CONSUMER_KEY, object.getConsumerKey());
        parameters.put(Demand.RANGE, object.getRange());
        parameters.put(Demand.STATE, object.getState().toString());
        parameters.put(Demand.LOCATION_KEY, object.getLocationKey());

        List<Demand> selection = ops.getDemands(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }
}
