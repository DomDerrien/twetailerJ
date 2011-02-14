package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.InvalidIdentifierException;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dto.Demand;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestDataMigration {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        DataMigration.setMockLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() {
        MockTwitterConnector.restoreTwitterConnector();
    }

    @Test
    public void testConstructor() {
        new DataMigration();
    }

    @Test
    public void testGetLogger() {
        DataMigration.getLogger();
    }

    @Test
    public void testUpdateSourceI() throws InvalidIdentifierException {

        BaseSteps.setMockDemandOperations(new DemandOperations());
        PersistenceManager pm  = BaseOperations.getPersistenceManagerHelper();
        Demand demand1 = BaseSteps.getDemandOperations().createDemand(pm, new Demand());
        Demand demand2 = BaseSteps.getDemandOperations().createDemand(pm, new Demand());
        Demand demand3 = BaseSteps.getDemandOperations().createDemand(pm, new Demand());
        Demand demand4 = BaseSteps.getDemandOperations().createDemand(pm, new Demand());
        demand4.setSource(Source.simulated);
        demand4 = BaseSteps.getDemandOperations().updateDemand(pm, demand4);
        pm.close();

        assertNotNull(demand1.getKey());
        assertNotNull(demand2.getKey());
        assertNotNull(demand3.getKey());
        assertNotNull(demand4.getKey());

        assertNull(demand1.getSource());
        assertNull(demand2.getSource());
        assertNull(demand3.getSource());
        assertEquals(Source.simulated, demand4.getSource());

        pm  = BaseOperations.getPersistenceManagerHelper();
        String cursor = DataMigration.updateSource(pm, null, 1, Source.robot);
        assertNotNull(cursor);
        pm.close();

        pm  = BaseOperations.getPersistenceManagerHelper();
        demand1 = BaseSteps.getDemandOperations().getDemand(pm, demand1.getKey(), null);
        demand2 = BaseSteps.getDemandOperations().getDemand(pm, demand2.getKey(), null);
        demand3 = BaseSteps.getDemandOperations().getDemand(pm, demand3.getKey(), null);
        demand4 = BaseSteps.getDemandOperations().getDemand(pm, demand4.getKey(), null);
        pm.close();

        assertEquals(Source.robot, demand1.getSource());
        assertNull(demand2.getSource());
        assertNull(demand3.getSource());
        assertEquals(Source.simulated, demand4.getSource());

        pm  = BaseOperations.getPersistenceManagerHelper();
        cursor = DataMigration.updateSource(pm, cursor, 1, Source.robot);
        assertNotNull(cursor);
        pm.close();

        pm  = BaseOperations.getPersistenceManagerHelper();
        demand1 = BaseSteps.getDemandOperations().getDemand(pm, demand1.getKey(), null);
        demand2 = BaseSteps.getDemandOperations().getDemand(pm, demand2.getKey(), null);
        demand3 = BaseSteps.getDemandOperations().getDemand(pm, demand3.getKey(), null);
        demand4 = BaseSteps.getDemandOperations().getDemand(pm, demand4.getKey(), null);
        pm.close();

        assertEquals(Source.robot, demand1.getSource());
        assertEquals(Source.robot, demand2.getSource());
        assertNull(demand3.getSource());
        assertEquals(Source.simulated, demand4.getSource());

        pm  = BaseOperations.getPersistenceManagerHelper();
        cursor = DataMigration.updateSource(pm, cursor, 1, Source.robot);
        assertNotNull(cursor);
        pm.close();

        pm  = BaseOperations.getPersistenceManagerHelper();
        demand1 = BaseSteps.getDemandOperations().getDemand(pm, demand1.getKey(), null);
        demand2 = BaseSteps.getDemandOperations().getDemand(pm, demand2.getKey(), null);
        demand3 = BaseSteps.getDemandOperations().getDemand(pm, demand3.getKey(), null);
        demand4 = BaseSteps.getDemandOperations().getDemand(pm, demand4.getKey(), null);
        pm.close();

        assertEquals(Source.robot, demand1.getSource());
        assertEquals(Source.robot, demand2.getSource());
        assertEquals(Source.robot, demand3.getSource());
        assertEquals(Source.simulated, demand4.getSource());

        pm  = BaseOperations.getPersistenceManagerHelper();
        cursor = DataMigration.updateSource(pm, cursor, 1, Source.robot);
        assertNotNull(cursor);
        pm.close();

        pm  = BaseOperations.getPersistenceManagerHelper();
        demand1 = BaseSteps.getDemandOperations().getDemand(pm, demand1.getKey(), null);
        demand2 = BaseSteps.getDemandOperations().getDemand(pm, demand2.getKey(), null);
        demand3 = BaseSteps.getDemandOperations().getDemand(pm, demand3.getKey(), null);
        demand4 = BaseSteps.getDemandOperations().getDemand(pm, demand4.getKey(), null);
        pm.close();

        assertEquals(Source.robot, demand1.getSource());
        assertEquals(Source.robot, demand2.getSource());
        assertEquals(Source.robot, demand3.getSource());
        assertEquals(Source.simulated, demand4.getSource());

        pm  = BaseOperations.getPersistenceManagerHelper();
        assertNull(DataMigration.updateSource(pm, cursor, 1, Source.robot));
        pm.close();
    }
}
