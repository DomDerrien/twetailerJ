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
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
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

public class TestDemandRestlet {

    DemandRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
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
    public void testGetLogger() throws DataSourceException, ClientException {
        assertNotNull(ops.getLogger());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetResourceIa() throws DataSourceException, ClientException {
        // Missing "on behalf" ids
        // Default point of view: CONSUMER
        ops.getResource(new GenericJsonObject(), "12345", user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetResourceIb() throws DataSourceException, ClientException {
        // On behalf of an associate but without the associate point of view (default is consumer point of view)
        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, 645432L);

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
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, 645432L);

        ops.getResource(parameters, "12345", user, true);
    }

    @Test
    public void testGetResourceII() throws DataSourceException, ClientException {
        // Get just one demand
        final Long demandKey = 5437653L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(new GenericJsonObject(), demandKey.toString(), user, false);

        assertEquals(demandKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceIII() throws DataSourceException, ClientException {
        // Get one demand and its related location
        final Long demandKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
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

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Location.class.getSimpleName() })));

        JsonObject resource = ops.getResource(parameters, demandKey.toString(), user, false);

        assertEquals(demandKey.longValue(), resource.getLong(Entity.KEY));
        assertEquals(1, resource.getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).size());
        assertEquals(locationKey.longValue(), resource.getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonObject(Location.class.getSimpleName()).getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceIV() throws DataSourceException, ClientException {
        // Get one demand on behalf of a user, and verify that a query of an unsupported related object is safe
        final Long consumerKey = 7609403943L;
        final Long demandKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertEquals(consumerKey, ownerKey);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setOwnerKey(consumerKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Consumer.class.getSimpleName() })));
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, consumerKey);

        JsonObject resource = ops.getResource(parameters, demandKey.toString(), user, true);

        assertEquals(demandKey.longValue(), resource.getLong(Entity.KEY));
        assertFalse(resource.containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
    }

    @Test
    public void testGetResourceV() throws DataSourceException, ClientException {
        // Get one demand as an associate, who must have sent a proposal
        final Long demandKey = 5437653L;
        final Long proposalKey = 9874321L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(ownerKey);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setLocationKey(locationKey);
                resource.addSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                resource.addProposalKey(proposalKey);
                return resource;
            }
        });
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Long> getProposalKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, parameters.get(Command.OWNER_KEY));
                assertEquals(Boolean.TRUE, parameters.get(Command.STATE_COMMAND_LIST));
                return Arrays.asList(new Long[] { proposalKey });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());

        JsonObject resource = ops.getResource(parameters, demandKey.toString(), MockLoginServlet.buildMockOpenIdAssociate(), false); // logged user is an associate

        assertEquals(demandKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceVI() throws DataSourceException, ClientException {
        // Get one demand on behalf of an associate, who must have sent a proposal
        final Long saleAssociateKey = 7609403943L;
        final Long demandKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(ownerKey);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setLocationKey(locationKey);
                resource.addSaleAssociateKey(saleAssociateKey);
                return resource;
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, saleAssociateKey);
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());

        JsonObject resource = ops.getResource(parameters, demandKey.toString(), user, true);

        assertEquals(demandKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIa() throws DataSourceException, ClientException {
        // Missing "on behalf" ids
        // Default point of view: CONSUMER
        ops.selectResources(new GenericJsonObject(), user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIb() throws DataSourceException, ClientException {
        // On behalf of an associate but without the associate point of view
        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, 645432L);

        ops.selectResources(parameters, user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIc() throws DataSourceException, ClientException {
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());

        ops.selectResources(parameters, user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceId() throws DataSourceException, ClientException {
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, 645432L);

        ops.selectResources(parameters, user, true);
    }

    @Test
    public void testSelectResourceIIa() throws DataSourceException, ClientException {
        // Get just one demand
        final Long demandKey = 5437653L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Demand.OWNER_KEY));
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return Arrays.asList(new Demand[] { resource });
            }
        });

        JsonArray resources = ops.selectResources(new GenericJsonObject(), user, false);

        assertEquals(1, resources.size());
        assertEquals(demandKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourceIIb() throws DataSourceException, ClientException {
        // Get just one demand KEY
        final Long demandKey = 5437653L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Demand.OWNER_KEY));
                return Arrays.asList(new Long[] { demandKey });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, Boolean.TRUE);

        JsonArray resources = ops.selectResources(parameters, user, false);

        assertEquals(1, resources.size());
        assertEquals(demandKey.longValue(), resources.getLong(0));
    }

    @Test
    public void testSelectResourceIIIa() throws DataSourceException, ClientException {
        // Get three demands and their related locations (which two are the same)
        final Long demandKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Demand.OWNER_KEY));
                Demand temp1 = new Demand();
                temp1.setKey(1 * demandKey);
                temp1.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                temp1.setLocationKey(1 * locationKey);
                Demand temp2 = new Demand();
                temp2.setKey(2 * demandKey);
                temp2.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                temp2.setLocationKey(2 * locationKey);
                Demand temp3 = new Demand();
                temp3.setKey(3 * demandKey);
                temp3.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                temp3.setLocationKey(1 * locationKey); // Same location as temp1
                return Arrays.asList(new Demand[] { temp1, temp2, temp3 });
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

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Location.class.getSimpleName() })));

        JsonArray resources = ops.selectResources(parameters, user, false);

        assertEquals(3, resources.size());
        assertEquals(1 * demandKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
        assertEquals(2 * demandKey.longValue(), resources.getJsonObject(1).getLong(Entity.KEY));
        assertEquals(3 * demandKey.longValue(), resources.getJsonObject(2).getLong(Entity.KEY));
        assertEquals(1, resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).size());
        assertEquals(2, resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).size());
        assertEquals(1 * locationKey.longValue(), resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).getJsonObject(0).getLong(Entity.KEY));
        assertEquals(2 * locationKey.longValue(), resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).getJsonObject(1).getLong(Entity.KEY));
        assertFalse(resources.getJsonObject(1).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
        assertFalse(resources.getJsonObject(2).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
    }

    @Test
    public void testSelectResourceIIIb() throws DataSourceException, ClientException {
        // Get one demand and its related location but not demand matches
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Demand.OWNER_KEY));
                return new ArrayList<Demand>();
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Location.class.getSimpleName() })));

        JsonArray resources = ops.selectResources(parameters, user, false);

        assertEquals(0, resources.size());
    }

    @Test
    public void testSelectResourceIV() throws DataSourceException, ClientException {
        // Get one demand on behalf of a user, and verify that a query of an unsupported related object is safe
        final Long consumerKey = 7609403943L;
        final Long demandKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(consumerKey, params.get(Demand.OWNER_KEY));
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setOwnerKey(consumerKey);
                resource.setLocationKey(locationKey);
                return Arrays.asList(new Demand[] { resource });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Consumer.class.getSimpleName() })));
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, consumerKey);

        JsonArray resources = ops.selectResources(parameters, user, true);

        assertEquals(demandKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
        assertFalse(resources.getJsonObject(0).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
    }

    @Test
    public void testSelectResourceV() throws DataSourceException, ClientException {
        // Get one demand as an associate, who must have sent a proposal
        final Long demandKey = 5437653L;
        final Long proposalKey = 9874321L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertNull(params.get(Demand.OWNER_KEY));
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, params.get(Demand.SALE_ASSOCIATE_KEYS));
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setLocationKey(locationKey);
                resource.addSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                resource.addProposalKey(proposalKey);
                return Arrays.asList(new Demand[] { resource });
            }
        });
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Long> getProposalKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, parameters.get(Command.OWNER_KEY));
                assertEquals(Boolean.TRUE, parameters.get(Command.STATE_COMMAND_LIST));
                return Arrays.asList(new Long[] { proposalKey });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());

        JsonArray resources = ops.selectResources(parameters, MockLoginServlet.buildMockOpenIdAssociate(), false); // logged user is an associate

        assertEquals(demandKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelecttResourceVI() throws DataSourceException, ClientException {
        // Get one demand on behalf of an associate, who must have sent a proposal
        final Long saleAssociateKey = 7609403943L;
        final Long demandKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertNull(params.get(Demand.OWNER_KEY));
                assertEquals(saleAssociateKey, params.get(Demand.SALE_ASSOCIATE_KEYS));
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setLocationKey(locationKey);
                resource.addSaleAssociateKey(saleAssociateKey);
                return Arrays.asList(new Demand[] { resource });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, saleAssociateKey);
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());

        JsonArray resources = ops.selectResources(parameters, user, true);

        assertEquals(demandKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testCreateResource() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        final Long demandKey = 12345L;
        final Source source = Source.simulated;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long ownerKey) throws ClientException {
                assertEquals(Source.api.toString(), parameters.getString(Command.SOURCE));
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Demand resource = new Demand();
                resource.setOwnerKey(ownerKey);
                resource.setKey(demandKey);
                resource.setSource(source);
                return resource;
            }
        });

        final JsonObject params = new GenericJsonObject();

        JsonObject resource = ops.createResource(params, user, false);

        assertTrue(resource.containsKey(Entity.KEY));
    }

    @Test
    public void testUpdateResource() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                return resource;
            }
        });
        final Long demandKey = 12345L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Demand resource = new Demand();
                resource.setOwnerKey(ownerKey);
                resource.setKey(demandKey);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                return demand;
            }
        });

        final JsonObject params = new GenericJsonObject();

        JsonObject resource = ops.updateResource(params, demandKey.toString(), user, false);

        assertTrue(resource.containsKey(Entity.KEY));
    }

    @Test
    public void testDeleteResource() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                return resource;
            }
        });
        final Long demandKey = 12345L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Demand resource = new Demand();
                resource.setOwnerKey(ownerKey);
                resource.setKey(demandKey);
                resource.setState(State.cancelled);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.markedForDeletion, demand.getState());
                assertTrue(demand.getMarkedForDeletion());
                return demand;
            }
        });
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Proposal.DEMAND_KEY, key);
                assertEquals(demandKey, value);
                return new ArrayList<Proposal>();
            }
        });

        ops.deleteResource(demandKey.toString(), user, false);
    }
}
