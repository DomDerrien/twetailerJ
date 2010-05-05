package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Proposal;
import twetailer.j2ee.MockLoginServlet;

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
    }

    @After
    public void tearDown() throws Exception {
        DemandRestlet._baseOperations = new BaseOperations();
        DemandRestlet.consumerOperations = DemandRestlet._baseOperations.getConsumerOperations();
        DemandRestlet.demandOperations = DemandRestlet._baseOperations.getDemandOperations();
        DemandRestlet.proposalOperations = DemandRestlet._baseOperations.getProposalOperations();
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
        DemandRestlet.demandOperations = new DemandOperations() {
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
        };

        JsonObject returnedDemand = ops.createResource(proposedParameters, user);
        assertTrue(proposedPM.isClosed());
        assertNotNull(returnedDemand);
        assertTrue(returnedDemand.containsKey(Entity.KEY));
        assertEquals(resourceId.longValue(), returnedDemand.getLong(Entity.KEY));
        assertTrue(returnedDemand.containsKey(Command.OWNER_KEY));
        assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY.longValue(), returnedDemand.getLong(Command.OWNER_KEY));
    }

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

    @Test(expected=ClientException.class)
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        ops.deleteResource("12345", user);
    }

    @Test
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        final Long demandKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        new DemandRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key, Long ownerKey, boolean stopRecursion) {
                assertEquals(demandKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
            }
        }.deleteResource(demandKey.toString(), user);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long demandKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        new DemandRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key, Long ownerKey, boolean stopRecursion) {
                assertEquals(demandKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.deleteResource(demandKey.toString(), user);
    }

    @Test
    public void testDelegateDeletionResourceI() throws DataSourceException, ClientException {
        //
        // Just Demand deletion
        //
        final Long demandKey = 12345L;
        DemandRestlet.demandOperations = new DemandOperations() {
            @Override
            public void deleteDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                assertEquals(demandKey, key);
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), demandKey, MockLoginServlet.DEFAULT_CONSUMER_KEY, true);
    }

    @Test
    public void testDelegateDeletionResourceII() throws DataSourceException, ClientException {
        //
        // Demand without Proposals
        //
        final Long demandKey = 12345L;
        DemandRestlet.demandOperations = new DemandOperations() {
            @Override
            public void deleteDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                assertEquals(demandKey, key);
            }
        };
        DemandRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Proposal.DEMAND_KEY, key);
                assertEquals(demandKey, (Long) value);
                List<Proposal> proposals = new ArrayList<Proposal>();
                return proposals;
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), demandKey, MockLoginServlet.DEFAULT_CONSUMER_KEY, false);
    }

    @Test
    public void testDelegateDeletionResourceIII() throws DataSourceException, ClientException {
        //
        // Demand without Proposals
        //
        final Long demandKey = 12345L;
        DemandRestlet.demandOperations = new DemandOperations() {
            @Override
            public void deleteDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                assertEquals(demandKey, key);
            }
        };
        final Long proposalKey1 = 2222L;
        final Long proposalKey2 = 33333L;
        DemandRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Proposal.DEMAND_KEY, key);
                assertEquals(demandKey, (Long) value);
                List<Proposal> proposals = new ArrayList<Proposal>();
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey1);
                proposals.add(proposal);
                proposal = new Proposal();
                proposal.setKey(proposalKey2);
                proposals.add(proposal);
                return proposals;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertTrue(proposal.getKey() == proposalKey1 || proposal.getKey() == proposalKey2);
                return proposal;
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), demandKey, MockLoginServlet.DEFAULT_CONSUMER_KEY, false);
    }

    @Ignore
    @Test(expected=RuntimeException.class)
    public void testGetResource() throws DataSourceException {
        ops.getResource(null, "12345", user);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResources() throws DataSourceException, ClientException {
        ops.selectResources(null, null);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(null, "12345", user);
    }
}
