package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.State;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestProposalRestlet {

    ProposalRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new ProposalRestlet();
        user = MockLoginServlet.buildMockOpenIdAssociate(); // Associate
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetLogger() throws DataSourceException, ClientException {
        assertNotNull(ops.getLogger());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetResourceIa() throws DataSourceException, ClientException {
        // Missing "on behalf" ids
        // Default point of view: SALE_ASSOCIATE
        ops.getResource(new GenericJsonObject(), "12345", user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetResourceIb() throws DataSourceException, ClientException {
        // On behalf of an associate but without the associate point of view (default is associate point of view)
        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, 645432L);

        ops.getResource(parameters, "12345", user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetResourceIc() throws DataSourceException, ClientException {
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());

        ops.getResource(parameters, "12345", user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetResourceId() throws DataSourceException, ClientException {
        // With the consumer point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, 645432L);
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());

        ops.getResource(parameters, "12345", user, true);
    }

    @Test
    public void testGetResourceII() throws DataSourceException, ClientException {
        // Get just one proposal
        final Long proposalKey = 5437653L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, ownerKey);
                assertEquals(MockLoginServlet.DEFAULT_STORE_KEY, sKey);
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(new GenericJsonObject(), proposalKey.toString(), user, false);

        assertEquals(proposalKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceIII() throws DataSourceException, ClientException {
        // Get one proposal and its related location
        final Long proposalKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, ownerKey);
                assertEquals(MockLoginServlet.DEFAULT_STORE_KEY, sKey);
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setLocationKey(locationKey);
                return resource;
            }
        });
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(MockLoginServlet.DEFAULT_STORE_KEY, key);
                Store resource = new Store();
                resource.setKey(MockLoginServlet.DEFAULT_STORE_KEY);
                return resource;
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Location.class.getSimpleName(), Store.class.getSimpleName() })));

        JsonObject resource = ops.getResource(parameters, proposalKey.toString(), user, false);

        assertEquals(proposalKey.longValue(), resource.getLong(Entity.KEY));
        assertEquals(2, resource.getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).size());
        assertEquals(locationKey.longValue(), resource.getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonObject(Location.class.getSimpleName()).getLong(Entity.KEY));
        assertEquals(MockLoginServlet.DEFAULT_STORE_KEY.longValue(), resource.getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonObject(Store.class.getSimpleName()).getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceIV() throws DataSourceException, ClientException {
        // Get one proposal on behalf of an associate, and verify that a query of an unsupported related object is safe
        final Long saleAssociateKey = 5555L;
        final Long proposalKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, ownerKey);
                assertNull(sKey);
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setOwnerKey(saleAssociateKey);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setLocationKey(locationKey);
                return resource;
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Consumer.class.getSimpleName() })));
        parameters.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, saleAssociateKey);

        JsonObject resource = ops.getResource(parameters, proposalKey.toString(), user, true);

        assertEquals(proposalKey.longValue(), resource.getLong(Entity.KEY));
        assertFalse(resource.containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
    }

    @Test
    public void testGetResourceV() throws DataSourceException, ClientException {
        // Get one proposal as a consumer, who must have sent the demand
        final Long demandKey = 5437653L;
        final Long proposalKey = 9874321L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(ownerKey);
                assertNull(sKey);
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setDemandKey(demandKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                resource.setLocationKey(locationKey);
                resource.addSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                resource.addProposalKey(proposalKey);
                return resource;
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());

        JsonObject resource = ops.getResource(parameters, proposalKey.toString(), MockLoginServlet.buildMockOpenIdUser(), false); // logged user is just a consumer

        assertEquals(proposalKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceVI() throws DataSourceException, ClientException {
        // Get one proposal on behalf of a consumer, who must have sent the demand
        final Long consumerKey = 7609403943L;
        final Long demandKey = 5437653L;
        final Long proposalKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(ownerKey);
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setDemandKey(demandKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertEquals(consumerKey, ownerKey);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setOwnerKey(consumerKey);
                resource.setLocationKey(locationKey);
                resource.addSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                resource.addProposalKey(proposalKey);
                return resource;
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, consumerKey);
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());

        JsonObject resource = ops.getResource(parameters, proposalKey.toString(), user, true);

        assertEquals(proposalKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIa() throws DataSourceException, ClientException {
        // Missing "on behalf" ids
        // Default point of view: SALE_ASSOCIATE
        ops.selectResources(new GenericJsonObject(), user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIb() throws DataSourceException, ClientException {
        // On behalf of an associate but without the associate point of view
        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, 645432L);

        ops.selectResources(parameters, user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIc() throws DataSourceException, ClientException {
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());

        ops.selectResources(parameters, user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceId() throws DataSourceException, ClientException {
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());
        parameters.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, 645432L);

        ops.selectResources(parameters, user, true);
    }

    @Test
    public void testSelectResourceIIa() throws DataSourceException, ClientException {
        // Get just one proposal
        final Long proposalKey = 5437653L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, params.get(Proposal.OWNER_KEY));
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return Arrays.asList(new Proposal[] { resource });
            }
        });

        JsonArray resources = ops.selectResources(new GenericJsonObject(), user, false);

        assertEquals(1, resources.size());
        assertEquals(proposalKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourceIIb() throws DataSourceException, ClientException {
        // Get just one proposal KEY
        final Long proposalKey = 5437653L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Long> getProposalKeys(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, params.get(Proposal.OWNER_KEY));
                return Arrays.asList(new Long[] { proposalKey });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, Boolean.TRUE);

        JsonArray resources = ops.selectResources(parameters, user, false);

        assertEquals(1, resources.size());
        assertEquals(proposalKey.longValue(), resources.getLong(0));
    }

    @Test
    public void testSelectResourceIIIa() throws DataSourceException, ClientException {
        // Get three proposals and their related locations (which two are the same)
        final Long proposalKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, params.get(Proposal.OWNER_KEY));
                Proposal temp1 = new Proposal();
                temp1.setKey(1 * proposalKey);
                temp1.setOwnerKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                temp1.setLocationKey(1 * locationKey);
                temp1.setStoreKey(1 * MockLoginServlet.DEFAULT_STORE_KEY);
                Proposal temp2 = new Proposal();
                temp2.setKey(2 * proposalKey);
                temp2.setOwnerKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                temp2.setLocationKey(2 * locationKey);
                temp2.setStoreKey(2 * MockLoginServlet.DEFAULT_STORE_KEY);
                Proposal temp3 = new Proposal();
                temp3.setKey(3 * proposalKey);
                temp3.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                temp3.setLocationKey(1 * locationKey); // Same location as temp1
                temp3.setStoreKey(1 * MockLoginServlet.DEFAULT_STORE_KEY); // Same store as temp1
                return Arrays.asList(new Proposal[] { temp1, temp2, temp3 });
            }
        });
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, List<Long> keys) {
                assertEquals(2, keys.size());
                assertEquals(locationKey, keys.get(0));
                Location temp1 = new Location();
                temp1.setKey(1 * locationKey);
                Location temp2 = new Location();
                temp2.setKey(2 * locationKey);
                return Arrays.asList(new Location[] { temp1, temp2 });
            }
        });
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, List<Long> keys) {
                assertEquals(2, keys.size());
                assertEquals(1 * locationKey, keys.get(0).longValue());
                assertEquals(2 * locationKey, keys.get(1).longValue());
                Location temp1 = new Location();
                temp1.setKey(1 * locationKey);
                Location temp2 = new Location();
                temp2.setKey(2 * locationKey);
                return Arrays.asList(new Location[] { temp1, temp2 });
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Long> keys) {
                assertEquals(2, keys.size());
                assertEquals(1 * MockLoginServlet.DEFAULT_STORE_KEY, keys.get(0).longValue());
                assertEquals(2 * MockLoginServlet.DEFAULT_STORE_KEY, keys.get(1).longValue());
                Store temp1 = new Store();
                temp1.setKey(1 * MockLoginServlet.DEFAULT_STORE_KEY);
                Store temp2 = new Store();
                temp2.setKey(2 * MockLoginServlet.DEFAULT_STORE_KEY);
                return Arrays.asList(new Store[] { temp1, temp2 });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Location.class.getSimpleName(), Store.class.getSimpleName() })));

        JsonArray resources = ops.selectResources(parameters, user, false);

        assertEquals(3, resources.size());
        assertEquals(1 * proposalKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
        assertEquals(2 * proposalKey.longValue(), resources.getJsonObject(1).getLong(Entity.KEY));
        assertEquals(3 * proposalKey.longValue(), resources.getJsonObject(2).getLong(Entity.KEY));
        assertEquals(2, resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).size());
        assertEquals(2, resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).size());
        assertEquals(1 * locationKey.longValue(), resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).getJsonObject(0).getLong(Entity.KEY));
        assertEquals(2 * locationKey.longValue(), resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).getJsonObject(1).getLong(Entity.KEY));
        assertEquals(2, resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Store.class.getSimpleName()).size());
        assertEquals(1 * MockLoginServlet.DEFAULT_STORE_KEY.longValue(), resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Store.class.getSimpleName()).getJsonObject(0).getLong(Entity.KEY));
        assertEquals(2 * MockLoginServlet.DEFAULT_STORE_KEY.longValue(), resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Store.class.getSimpleName()).getJsonObject(1).getLong(Entity.KEY));
        assertFalse(resources.getJsonObject(1).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
        assertFalse(resources.getJsonObject(2).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
    }

    @Test
    public void testSelectResourceIIIb() throws DataSourceException, ClientException {
        // Get one proposal and its related location but not proposal matches
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, params.get(Proposal.OWNER_KEY));
                return new ArrayList<Proposal>();
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Location.class.getSimpleName() })));

        JsonArray resources = ops.selectResources(parameters, user, false);

        assertEquals(0, resources.size());
    }

    @Test
    public void testSelectResourceIV() throws DataSourceException, ClientException {
        // Get one proposal on behalf of a consumer, and verify that a query of an unsupported related object is safe
        final Long saleAssociateKey = 7609403943L;
        final Long proposalKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(saleAssociateKey, params.get(Proposal.OWNER_KEY));
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setOwnerKey(saleAssociateKey);
                resource.setLocationKey(locationKey);
                return Arrays.asList(new Proposal[] { resource });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { SaleAssociate.class.getSimpleName() })));
        parameters.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, saleAssociateKey);

        JsonArray resources = ops.selectResources(parameters, user, true);

        assertEquals(proposalKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
        assertFalse(resources.getJsonObject(0).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
    }

    @Test
    public void testSelectResourceV() throws DataSourceException, ClientException {
        // Get one proposal as a consumer, who must have sent a proposal
        final Long proposalKey = 5437653L;
        final Long consumerKey = 7654322143L;
        final Long demandKey = 9874321L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertNull(params.get(Proposal.OWNER_KEY));
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Proposal.CONSUMER_KEY));
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setLocationKey(locationKey);
                resource.setConsumerKey(consumerKey);
                resource.setDemandKey(demandKey);
                return Arrays.asList(new Proposal[] { resource });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());

        JsonArray resources = ops.selectResources(parameters, MockLoginServlet.buildMockOpenIdUser(), false); // logged user is just a consumer

        assertEquals(proposalKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelecttResourceVI() throws DataSourceException, ClientException {
        // Get one proposal on behalf of a consumer, who must have sent the demand
        final Long consumerKey = 7609403943L;
        final Long proposalKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertNull(params.get(Proposal.OWNER_KEY));
                assertEquals(consumerKey, params.get(Proposal.CONSUMER_KEY));
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setLocationKey(locationKey);
                resource.setDemandKey(proposalKey);
                return Arrays.asList(new Proposal[] { resource });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, consumerKey);
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());

        JsonArray resources = ops.selectResources(parameters, user, true);

        assertEquals(proposalKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testCreateResource() throws DataSourceException, ClientException {
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long saleAssociateKey) {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, saleAssociateKey);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(saleAssociateKey);
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        final Long demandKey = 12345L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(ownerKey);
                Demand resource = new Demand();
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                resource.setKey(demandKey);
                return resource;
            }
        });
        final Source source = Source.simulated;
        final Long proposalKey = 654876098232L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal createProposal(PersistenceManager pm, JsonObject parameters, SaleAssociate associate) throws ClientException {
                assertEquals(Source.api.toString(), parameters.getString(Command.SOURCE));
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, associate.getKey());
                Proposal resource = new Proposal();
                resource.setOwnerKey(associate.getKey());
                resource.setKey(proposalKey);
                resource.setSource(source);
                resource.setDemandKey(demandKey);
                return resource;
            }
        });

        final JsonObject params = new GenericJsonObject();
        params.put(Proposal.DEMAND_KEY, demandKey);

        JsonObject resource = ops.createResource(params, user, false);

        assertTrue(resource.containsKey(Entity.KEY));
    }

    @Test
    public void testUpdateResourceI() throws DataSourceException, ClientException {
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long saleAssociateKey) {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, saleAssociateKey);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(saleAssociateKey);
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        final Long demandKey = 12345L;
        final Long proposalKey = 654876098232L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long sK) {
                assertEquals(proposalKey, key);
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, ownerKey);
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setOwnerKey(ownerKey);
                resource.setDemandKey(demandKey);
                return resource;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
                return proposal;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(ownerKey);
                Demand resource = new Demand();
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                resource.setKey(demandKey);
                resource.addProposalKey(proposalKey);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(0, demand.getProposalKeys().size());
                return demand;
            }
        });

        final JsonObject params = new GenericJsonObject();

        JsonObject resource = ops.updateResource(params, proposalKey.toString(), user, false);

        assertTrue(resource.containsKey(Entity.KEY));
    }

    @Test
    public void testUpdateResourceII() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        final Long demandKey = 12345L;
        final Long proposalKey = 654876098232L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long sK) {
                assertEquals(proposalKey, key);
                assertNull(ownerKey);
                Proposal resource = new Proposal();
                resource.setKey(proposalKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                resource.setDemandKey(demandKey);
                resource.setState(State.published);
                return resource;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
                assertEquals(State.declined, proposal.getState());
                return proposal;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Demand resource = new Demand();
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                resource.setKey(demandKey);
                resource.addProposalKey(proposalKey);
                resource.setState(State.published);
                return resource;
            }
        });

        final JsonObject params = new GenericJsonObject();
        params.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());
        params.put(Command.STATE, State.declined.toString());

        JsonObject resource = ops.updateResource(params, proposalKey.toString(), MockLoginServlet.buildMockOpenIdUser(), false); // Update as the consumer owning the demand

        assertTrue(resource.containsKey(Entity.KEY));
    }

    @Test
    public void testDeleteResource() throws DataSourceException, ClientException {
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long saleAssociateKey) {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, saleAssociateKey);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(saleAssociateKey);
                return resource;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        final Long proposalKey = 12345L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long sK) {
                assertEquals(proposalKey, key);
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, ownerKey);
                Proposal resource = new Proposal();
                resource.setOwnerKey(ownerKey);
                resource.setKey(proposalKey);
                resource.setState(State.cancelled);
                return resource;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
                assertEquals(State.markedForDeletion, proposal.getState());
                assertTrue(proposal.getMarkedForDeletion());
                return proposal;
            }
        });

        ops.deleteResource(proposalKey.toString(), user, false);
    }
}
