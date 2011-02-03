package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.WishOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.Wish;
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

public class TestWishRestlet {

    WishRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new WishRestlet();
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
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());

        ops.getResource(parameters, "12345", user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetResourceIc() throws DataSourceException, ClientException {
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, 645432L);

        ops.getResource(parameters, "12345", user, true);
    }

    @Test
    public void testGetResourceII() throws DataSourceException, ClientException {
        // Get just one wish
        final Long wishKey = 5437653L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public Wish getWish(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(wishKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Wish resource = new Wish();
                resource.setKey(wishKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(new GenericJsonObject(), wishKey.toString(), user, false);

        assertEquals(wishKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceIII() throws DataSourceException, ClientException {
        // Get one wish and its related location
        final Long wishKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public Wish getWish(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(wishKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Wish resource = new Wish();
                resource.setKey(wishKey);
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

        JsonObject resource = ops.getResource(parameters, wishKey.toString(), user, false);

        assertEquals(wishKey.longValue(), resource.getLong(Entity.KEY));
        assertEquals(1, resource.getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).size());
        assertEquals(locationKey.longValue(), resource.getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonObject(Location.class.getSimpleName()).getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceIV() throws DataSourceException, ClientException {
        // Get one wish on behalf of a user, and verify that a query of an unsupported related object is safe
        final Long consumerKey = 7609403943L;
        final Long wishKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public Wish getWish(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(wishKey, key);
                assertEquals(consumerKey, ownerKey);
                Wish resource = new Wish();
                resource.setKey(wishKey);
                resource.setOwnerKey(consumerKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Consumer.class.getSimpleName() })));
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, consumerKey);

        JsonObject resource = ops.getResource(parameters, wishKey.toString(), user, true);

        assertEquals(wishKey.longValue(), resource.getLong(Entity.KEY));
        assertFalse(resource.containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIa() throws DataSourceException, ClientException {
        // Missing "on behalf" ids
        // Default point of view: CONSUMER
        ops.selectResources(new GenericJsonObject(), user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIb() throws DataSourceException, ClientException {
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());

        ops.selectResources(parameters, user, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSelectResourceIc() throws DataSourceException, ClientException {
        // With the associate point of view but without the associate id to act on behalf of
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, QueryPointOfView.SALE_ASSOCIATE.toString());
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, 645432L);

        ops.selectResources(parameters, user, true);
    }

    @Test
    public void testSelectResourceIIa() throws DataSourceException, ClientException {
        // Get just one wish
        final Long wishKey = 5437653L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public List<Wish> getWishes(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Wish.OWNER_KEY));
                Wish resource = new Wish();
                resource.setKey(wishKey);
                resource.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return Arrays.asList(new Wish[] { resource });
            }
        });

        JsonArray resources = ops.selectResources(new GenericJsonObject(), user, false);

        assertEquals(1, resources.size());
        assertEquals(wishKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourceIIb() throws DataSourceException, ClientException {
        // Get just one wish KEY
        final Long wishKey = 5437653L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public List<Long> getWishKeys(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Wish.OWNER_KEY));
                return Arrays.asList(new Long[] { wishKey });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, Boolean.TRUE);

        JsonArray resources = ops.selectResources(parameters, user, false);

        assertEquals(1, resources.size());
        assertEquals(wishKey.longValue(), resources.getLong(0));
    }

    @Test
    public void testSelectResourceIIIa() throws DataSourceException, ClientException {
        // Get three wishes and their related locations (which two are the same)
        final Long wishKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public List<Wish> getWishes(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Wish.OWNER_KEY));
                Wish temp1 = new Wish();
                temp1.setKey(1 * wishKey);
                temp1.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                temp1.setLocationKey(1 * locationKey);
                Wish temp2 = new Wish();
                temp2.setKey(2 * wishKey);
                temp2.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                temp2.setLocationKey(2 * locationKey);
                Wish temp3 = new Wish();
                temp3.setKey(3 * wishKey);
                temp3.setOwnerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                temp3.setLocationKey(1 * locationKey); // Same location as temp1
                return Arrays.asList(new Wish[] { temp1, temp2, temp3 });
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
        assertEquals(1 * wishKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
        assertEquals(2 * wishKey.longValue(), resources.getJsonObject(1).getLong(Entity.KEY));
        assertEquals(3 * wishKey.longValue(), resources.getJsonObject(2).getLong(Entity.KEY));
        assertEquals(1, resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).size());
        assertEquals(2, resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).size());
        assertEquals(1 * locationKey.longValue(), resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).getJsonObject(0).getLong(Entity.KEY));
        assertEquals(2 * locationKey.longValue(), resources.getJsonObject(0).getJsonObject(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY).getJsonArray(Location.class.getSimpleName()).getJsonObject(1).getLong(Entity.KEY));
        assertFalse(resources.getJsonObject(1).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
        assertFalse(resources.getJsonObject(2).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
    }

    @Test
    public void testSelectResourceIIIb() throws DataSourceException, ClientException {
        // Get one wish and its related location but not wish matches
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public List<Wish> getWishes(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, params.get(Wish.OWNER_KEY));
                return new ArrayList<Wish>();
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Location.class.getSimpleName() })));

        JsonArray resources = ops.selectResources(parameters, user, false);

        assertEquals(0, resources.size());
    }

    @Test
    public void testSelectResourceIV() throws DataSourceException, ClientException {
        // Get one wish on behalf of a user, and verify that a query of an unsupported related object is safe
        final Long consumerKey = 7609403943L;
        final Long wishKey = 5437653L;
        final Long locationKey = 54645876897L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public List<Wish> getWishes(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertEquals(consumerKey, params.get(Wish.OWNER_KEY));
                Wish resource = new Wish();
                resource.setKey(wishKey);
                resource.setOwnerKey(consumerKey);
                resource.setLocationKey(locationKey);
                return Arrays.asList(new Wish[] { resource });
            }
        });

        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY, new GenericJsonArray(Arrays.asList(new Object[] { Consumer.class.getSimpleName() })));
        parameters.put(BaseRestlet.ON_BEHALF_CONSUMER_KEY, consumerKey);

        JsonArray resources = ops.selectResources(parameters, user, true);

        assertEquals(wishKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
        assertFalse(resources.getJsonObject(0).containsKey(BaseRestlet.RELATED_RESOURCES_ENTRY_POINT_KEY));
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
        final Long wishKey = 12345L;
        final Source source = Source.simulated;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public List<Wish> getWishes(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                // No wish to inherit from
                return new ArrayList<Wish>();
            }
            @Override
            public Wish createWish(PersistenceManager pm, JsonObject parameters, Long ownerKey) throws ClientException {
                assertEquals(Source.api.toString(), parameters.getString(Command.SOURCE));
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Wish resource = new Wish();
                resource.setOwnerKey(ownerKey);
                resource.setKey(wishKey);
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
        final Long wishKey = 12345L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public Wish getWish(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
                assertEquals(wishKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Wish resource = new Wish();
                resource.setOwnerKey(ownerKey);
                resource.setKey(wishKey);
                return resource;
            }
            @Override
            public Wish updateWish(PersistenceManager pm, Wish wish) {
                assertEquals(wishKey, wish.getKey());
                return wish;
            }
        });

        final JsonObject params = new GenericJsonObject();

        JsonObject resource = ops.updateResource(params, wishKey.toString(), user, false);

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
        final Long wishKey = 12345L;
        BaseSteps.setMockWishOperations(new WishOperations() {
            @Override
            public Wish getWish(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(wishKey, key);
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, ownerKey);
                Wish resource = new Wish();
                resource.setOwnerKey(ownerKey);
                resource.setKey(wishKey);
                resource.setState(State.cancelled);
                return resource;
            }
            @Override
            public Wish updateWish(PersistenceManager pm, Wish wish) {
                assertEquals(wishKey, wish.getKey());
                assertEquals(State.markedForDeletion, wish.getState());
                assertTrue(wish.getMarkedForDeletion());
                return wish;
            }
        });

        ops.deleteResource(wishKey.toString(), user, false);
    }
}
