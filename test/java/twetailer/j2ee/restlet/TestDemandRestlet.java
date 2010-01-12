package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.j2ee.TestBaseRestlet;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestDemandRestlet {

    DemandRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
        DemandRestlet.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        ops = new DemandRestlet();
        user = TestBaseRestlet.setupOpenIdUser();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetLogger() {
        ops.getLogger();
        assertTrue(true);
        assertNull(null);
    }

    @Test
    public void testCreateResourceI() throws DataSourceException, ClientException {
        final PersistenceManager proposedPM = new MockPersistenceManager();
        final JsonObject proposedParameters = new GenericJsonObject();
        final Source source = Source.simulated;
        final Long resourceId = 12345L;
        ops.demandOperations = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return proposedPM;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long OwnerKey) throws ClientException {
                assertEquals(proposedPM, pm);
                assertFalse(pm.isClosed());
                assertEquals(proposedParameters, parameters);
                assertEquals(TestBaseRestlet.LOGGED_USER_CONSUMER_KEY, OwnerKey);
                Demand temp = new Demand();
                temp.setOwnerKey(OwnerKey);
                temp.setKey(resourceId);
                temp.setSource(source);
                return temp;
            }
        };

        JsonObject returnedDemand = ops.createResource(proposedParameters, user);
        assertTrue(proposedPM.isClosed());
        assertNotNull(returnedDemand);
        assertTrue(returnedDemand.containsKey(Entity.KEY));
        assertEquals(resourceId.longValue(), returnedDemand.getLong(Entity.KEY));
        assertTrue(returnedDemand.containsKey(Command.OWNER_KEY));
        assertEquals(TestBaseRestlet.LOGGED_USER_CONSUMER_KEY.longValue(), returnedDemand.getLong(Command.OWNER_KEY));
    }

    @Test(expected=RuntimeException.class)
    public void testCreateResourceIII() throws DataSourceException, ClientException {
        final JsonObject proposedParameters = new GenericJsonObject();
        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                throw new RuntimeException("done in purpose");
            }
        };

        ops.createResource(proposedParameters, user);
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException {
        ops.deleteResource("12345", user);
    }

    @Test(expected=RuntimeException.class)
    public void testGetResource() throws DataSourceException {
        ops.getResource(null, "12345", user);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResources() throws DataSourceException {
        ops.selectResources(null, null);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(null, "12345", user);
    }
}
