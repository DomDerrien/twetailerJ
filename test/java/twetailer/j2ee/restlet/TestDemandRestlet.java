package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Proposal;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.State;

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
        user = MockLoginServlet.buildMockOpenIdUser();
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
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
    @Ignore
    public void testCreateResourceI() throws DataSourceException, ClientException {
        final PersistenceManager proposedPM = new MockPersistenceManager();
        final JsonObject proposedParameters = new GenericJsonObject();
        final Source source = Source.simulated;
        final Long resourceId = 12345L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return proposedPM;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long OwnerKey) throws ClientException {
                assertEquals(proposedPM, pm);
                assertFalse(pm.isClosed());
                assertEquals(proposedParameters, parameters);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, OwnerKey);
                Demand temp = new Demand();
                temp.setOwnerKey(OwnerKey);
                temp.setKey(resourceId);
                temp.setSource(source);
                return temp;
            }
        });

        JsonObject returnedDemand = ops.createResource(proposedParameters, user);
        assertTrue(proposedPM.isClosed());
        assertNotNull(returnedDemand);
        assertTrue(returnedDemand.containsKey(Entity.KEY));
        assertEquals(resourceId.longValue(), returnedDemand.getLong(Entity.KEY));
        assertTrue(returnedDemand.containsKey(Command.OWNER_KEY));
        assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY.longValue(), returnedDemand.getLong(Command.OWNER_KEY));
    }

    /**** ddd
    @Test(expected=RuntimeException.class)
    public void testCreateResourceIII() throws DataSourceException, ClientException {
        final JsonObject proposedParameters = new GenericJsonObject();
        DemandRestlet.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                throw new RuntimeException("done in purpose");
            }
        };
        ops.createResource(proposedParameters, user);
    }
    ddd *******/

    @Test
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long demandKey, Long ownerKey) {
                assertEquals(Long.valueOf("12345"), demandKey);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Demand demand = new Demand();
                demand.setOwnerKey(ownerKey);
                demand.setKey(demandKey);
                demand.setState(State.cancelled);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(Long.valueOf("12345"), demand.getKey());
                assertEquals(State.markedForDeletion, demand.getState());
                assertTrue(demand.getMarkedForDeletion());
                return demand;
            }
        });
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal >getProposals(PersistenceManager pm, String name, Object value, int limit) {
                assertEquals(Proposal.DEMAND_KEY, name);
                assertEquals(Long.valueOf("12345"), (Long) value);
                return new ArrayList<Proposal>();
            }
        });

        ops.deleteResource("12345", user);
    }

    @Ignore
    @Test(expected=RuntimeException.class)
    public void testGetResource() throws DataSourceException, ClientException {
        ops.getResource(null, "12345", user);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResources() throws DataSourceException, ClientException {
        ops.selectResources(null, null);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException, ClientException {
        ops.updateResource(null, "12345", user);
    }
}
