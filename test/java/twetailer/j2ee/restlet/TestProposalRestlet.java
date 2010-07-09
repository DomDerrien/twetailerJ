package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Entity;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestProposalRestlet {

    ProposalRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
        ProposalRestlet.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        ops = new ProposalRestlet();
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

    @Ignore
    @Test(expected=ClientException.class)
    public void testCreateResourceI() throws DataSourceException, ClientException {
        final JsonObject proposedParameters = new GenericJsonObject();
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                return saleAssociateKeys;
            }
        });

        ops.createResource(proposedParameters, user);
    }

    @Test
    @Ignore
    public void testCreateResourceII() throws DataSourceException, ClientException {
        final JsonObject proposedParameters = new GenericJsonObject();
        final Source source = Source.simulated;
        final Long saleAssociateKey = 34567L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return saleAssociate;
            }
        });
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal createProposal(PersistenceManager pm, JsonObject parameters, SaleAssociate saleAssociate) throws ClientException {
                assertFalse(pm.isClosed());
                assertEquals(proposedParameters, parameters);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, saleAssociate.getConsumerKey());
                Proposal temp = new Proposal();
                temp.setOwnerKey(saleAssociate.getKey());
                temp.setKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                temp.setSource(source);
                return temp;
            }
        });

        JsonObject returnedProposal = ops.createResource(proposedParameters, user);
        assertNotNull(returnedProposal);
        assertTrue(returnedProposal.containsKey(Entity.KEY));
        assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY.longValue(), returnedProposal.getLong(Entity.KEY));
        assertTrue(returnedProposal.containsKey(Command.OWNER_KEY));
        assertEquals(saleAssociateKey.longValue(), returnedProposal.getLong(Command.OWNER_KEY));
    }

    @Test(expected=ClientException.class)
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        ops.deleteResource("12345", user);
    }

    /***** ddd
    @Test
    @Ignore
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        final Long proposalKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                saleAssociate.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return saleAssociate;
            }
        });
        ops.deleteResource(proposalKey.toString(), user);
        new ProposalRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key, SaleAssociate saleAssociate, boolean stopRecursion) {
                assertEquals(proposalKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, saleAssociate.getConsumerKey());
            }
        }.deleteResource(proposalKey.toString(), user);
    }

    @Test
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long proposalKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        final Long saleAssociateKey = 34567L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return saleAssociate;
            }
        });
        new ProposalRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key, SaleAssociate saleAssociate, boolean stopRecursion) {
                assertEquals(proposalKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, saleAssociate.getConsumerKey());
            }
        }.deleteResource(proposalKey.toString(), user);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceIII() throws DataSourceException, ClientException {
        final Long proposalKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        new ProposalRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key, SaleAssociate saleAssociate, boolean stopRecursion) {
                assertEquals(proposalKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, saleAssociate.getConsumerKey());
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.deleteResource(proposalKey.toString(), user);
    }

    @Test
    public void testDelegateDeletionResourceI() throws DataSourceException, ClientException {
        //
        // Just Proposal deletion
        //
        final Long proposalKey = 12345L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                return proposal;
            }
            @Override
            public void deleteProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), proposalKey, new SaleAssociate(), true);
    }

    @Test
    public void testDelegateDeletionResourceII() throws DataSourceException, ClientException {
        //
        // Proposal without Demand (already detached)
        //
        final Long proposalKey = 12345L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                return proposal;
            }
            @Override
            public void deleteProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), proposalKey, new SaleAssociate(), false);
    }

    @Test
    public void testDelegateDeletionResourceIII() throws DataSourceException, ClientException {
        //
        // Proposal without Demand (already deleted!)
        //
        final Long proposalKey = 12345L;
        final Long demandKey = 34567L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                return proposal;
            }
            @Override
            public void deleteProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
            }
        };
        ProposalRestlet.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), proposalKey, new SaleAssociate(), false);
    }

    @Test
    public void testDelegateDeletionResourceIV() throws DataSourceException, ClientException {
        //
        // Proposal with Demand
        //
        final Long proposalKey = 12345L;
        final Long demandKey = 34567L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                return proposal;
            }
            @Override
            public void deleteProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
            }
        };
        ProposalRestlet.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.addProposalKey(proposalKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(0, demand.getProposalKeys().size());
                return demand;
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), proposalKey, new SaleAssociate(), false);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResources() throws DataSourceException {
        ops.selectResources(null, null);
    }

    @Ignore
    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException, ClientException {
        ops.updateResource(null, "12345", user);
    }

    @Test
    public void testGetResourceI() throws DataSourceException, ClientException {
        //
        // Proposal queried by the owner (SaleAssociate)
        //
        final Long saleAssociateKey = 65879L;
        final Long proposalKey = 45345L;
        final Long demandKey = 34567L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.simulated);
                proposal.setState(State.published);
                return proposal;
            }
        };
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
        });

        ops.getResource(null, proposalKey.toString(), user);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetResourceII() throws DataSourceException, ClientException {
        //
        // Proposal queried by the Demand owner (Consumer)
        // Demand not yet confirmed!
        //
        final Long saleAssociateKey = MockLoginServlet.DEFAULT_CONSUMER_KEY;
        final Long proposalKey = 45345L;
        final Long demandKey = 34567L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.simulated);
                proposal.setState(State.published);
                return proposal;
            }
        };
        ProposalRestlet.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                return new ArrayList<Long>();
            }
        };
        ProposalRestlet.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(ownerKey); // Because it's a privileged access
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(saleAssociateKey);
                demand.addProposalKey(proposalKey);
                demand.setState(State.published);
                return demand;
            }
        };

        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        ops.getResource(null, proposalKey.toString(), user);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetResourceIII() throws DataSourceException, ClientException {
        //
        // Proposal queried by the Demand owner (Consumer, who is also a SaleAssociate but who did not create the Proposal)
        // Demand not yet confirmed!
        //
        final Long saleAssociateKey = MockLoginServlet.DEFAULT_CONSUMER_KEY;
        final Long proposalKey = 45345L;
        final Long demandKey = 34567L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.simulated);
                proposal.setState(State.published);
                return proposal;
            }
        };
        ProposalRestlet.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(2 * saleAssociateKey); // Ensure the logged user is not the Proposal owner
                return saleAssociateKeys;
            }
        };
        ProposalRestlet.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(ownerKey); // Because it's a privileged access
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(saleAssociateKey);
                demand.addProposalKey(proposalKey);
                demand.setState(State.published);
                return demand;
            }
        };

        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        ops.getResource(null, proposalKey.toString(), user);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetResourceIV() throws DataSourceException, ClientException {
        //
        // Proposal queried by the Demand owner (Consumer)
        // Demand is in confirmed state => "Check out" URL needs to be computed
        //
        final Long saleAssociateKey = MockLoginServlet.DEFAULT_CONSUMER_KEY;
        final Long proposalKey = 45345L;
        final Long demandKey = 34567L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.simulated);
                proposal.setState(State.confirmed);
                return proposal;
            }
        };
        ProposalRestlet.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                return new ArrayList<Long>();
            }
        };
        ProposalRestlet.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(ownerKey); // Because it's a privileged access
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(saleAssociateKey);
                demand.addProposalKey(proposalKey);
                demand.setState(State.confirmed);
                return demand;
            }
        };

        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        ops.getResource(null, proposalKey.toString(), user);
    }

    @SuppressWarnings("unchecked")
    @Test(expected=DataSourceException.class)
    public void testGetResourceV() throws DataSourceException, ClientException {
        //
        // Proposal queried by the Demand owner (Consumer)
        // Demand is in confirmed state => "Check out" URL needs to be computed
        // Simulate error while updating the proposal field with the AWS Co-branded Service URL
        //
        final Long saleAssociateKey = MockLoginServlet.DEFAULT_CONSUMER_KEY;
        final Long proposalKey = 45345L;
        final Long demandKey = 34567L;
        ProposalRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public void setAWSCBUIURL(String url) {
                        throw new IllegalArgumentException("Done in purpose!");
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.simulated);
                proposal.setState(State.confirmed);
                return proposal;
            }
        };
        ProposalRestlet.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, (Long) value);
                return new ArrayList<Long>();
            }
        };
        ProposalRestlet.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(ownerKey); // Because it's a privileged access
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(saleAssociateKey);
                demand.addProposalKey(proposalKey);
                demand.setState(State.confirmed);
                return demand;
            }
        };

        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        ops.getResource(null, proposalKey.toString(), user);
    }
    ddd *****/
}
