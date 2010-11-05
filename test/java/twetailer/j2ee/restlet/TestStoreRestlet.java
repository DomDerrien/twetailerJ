package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Store;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonObject;

public class TestStoreRestlet {

    StoreRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new StoreRestlet();
        user = MockLoginServlet.buildMockOpenIdUser();
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected=ClientException.class)
    public void testCreateResourceI() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer consumer = new Consumer();
                consumer.setKey(key);
                return consumer;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        });
        ops.createResource(null, user, false);
    }

    @Test(expected=ClientException.class)
    public void testCreateResourceII() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer consumer = new Consumer();
                consumer.setKey(key);
                return consumer;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        });
        user.setAttribute("info", null);
        ops.createResource(null, user, false);
    }

    @Test(expected=ClientException.class)
    @SuppressWarnings("unchecked")
    public void testCreateResourceIII() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer consumer = new Consumer();
                consumer.setKey(key);
                return consumer;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        });
        ((Map<String, String>) user.getAttribute("info")).put("email", "unit@test");
        ops.createResource(null, user, false);
    }

    @Test(expected=ClientException.class)
    @SuppressWarnings("unchecked")
    public void testCreateResourceIV() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer consumer = new Consumer();
                consumer.setKey(key);
                return consumer;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        });
        ((Map<String, String>) user.getAttribute("info")).put("email", "unit@test");
        ops.createResource(null, user, false);
    }

    /**** ddd
    @Test
    public void testCreateResourceV() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        final Long locationKey = 23456L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
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
                resource.setHasStore(true);
                return resource;
            }
        });
        JsonObject response = ops.createResource(null, user, true);
        assertNotNull(response);
        assertEquals(storeKey.longValue(), response.getLong(Store.KEY));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateResourceVI() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        final Long locationKey = 23456L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
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
                resource.setHasStore(true);
                return resource;
            }
        });
        ((Map<String, String>) user.getAttribute("info")).put("email", "steven.milstein@gmail.com");
        JsonObject response = ops.createResource(null, user, false);
        assertNotNull(response);
        assertEquals(storeKey.longValue(), response.getLong(Store.KEY));
    }

    @Test
    public void testCreateResourceVII() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        final Long locationKey = 23456L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
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
                resource.setHasStore(false);
                return resource;
            }
            @Override
            public Location updateLocation(PersistenceManager pm, Location location) {
                assertEquals(locationKey, location.getKey());
                assertTrue(location.hasStore());
                return location;
            }
        });
        JsonObject response = ops.createResource(null, user, true);
        assertNotNull(response);
        assertEquals(storeKey.longValue(), response.getLong(Store.KEY));
    }

    @Test(expected=ClientException.class)
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        ops.deleteResource("resourceId", user, false);
    }

    @Test
    @SuppressWarnings({ "serial" })
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        new StoreRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
            }
        }.deleteResource(storeKey.toString(), user, true);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "serial" })
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        new StoreRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.deleteResource(storeKey.toString(), user, true);
    }

    @Test
    public void testDelegateDeletionResourceI() throws DataSourceException, ClientException {
        //
        // Store without Sale Associates
        //
        final Long storeKey = 12345L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
            @Override
            public void deleteStore(PersistenceManager pm, Store store) {
                assertEquals(storeKey, store.getKey());
            }
        });
        StoreRestlet.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                return saleAssociateKeys;
            }
        });

        ops.delegateResourceDeletion(new MockPersistenceManager(), storeKey);
    }

    @Test
    @SuppressWarnings("serial")
    public void testDelegateDeletionResourceII() throws DataSourceException, ClientException {
        //
        // Store without Sale Associates
        //
        final Long storeKey = 12345L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
            @Override
            public void deleteStore(PersistenceManager pm, Store store) {
                assertEquals(storeKey, store.getKey());
            }
        });
        final Long saleAssociateKey1 = 2222L;
        final Long saleAssociateKey2 = 3333L;
        StoreRestlet.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey1);
                saleAssociateKeys.add(saleAssociateKey2);
                return saleAssociateKeys;
            }
        });
        StoreRestlet.saleAssociateRestlet = new SaleAssociateRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long sAKey) throws DataSourceException{
                assertTrue(sAKey == saleAssociateKey1 || sAKey == saleAssociateKey2);
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), storeKey);
    }

    @Test(expected=RuntimeException.class)
    public void testGetResource() throws DataSourceException {
        ops.getResource(null, "resourceId", user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResourcesI() throws DataSourceException {
        ops.selectResources(new GenericJsonObject(), null);
    }

    @Test
    public void testSelectResourcesII() throws DataSourceException {
        final Long locationKey = 12345L;
        JsonObject input = new GenericJsonObject();
        input.put(Store.LOCATION_KEY, locationKey);
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(locationKey, location.getKey());
                assertEquals(100, limit);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(1, locations.size());
                assertEquals(locationKey, locations.get(0).getKey());
                assertEquals(100, limit);
                List<Store> stores = new ArrayList<Store>();
                return stores;
            }
        });
        ops.selectResources(input, null);
    }

    @Test
    public void testSelectResourcesIII() throws DataSourceException {
        final Long locationKey = 12345L;
        final Double range = 6.7890D;
        final String rangeUnit = LocaleValidator.MILE_UNIT;
        JsonObject input = new GenericJsonObject();
        input.put(Store.LOCATION_KEY, locationKey);
        input.put(Demand.RANGE, range);
        input.put(Demand.RANGE_UNIT, rangeUnit);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double givenRange, String givenRangeUnit, boolean withStore, int limit) {
                assertEquals(locationKey, location.getKey());
                assertEquals(range, givenRange);
                assertEquals(rangeUnit, givenRangeUnit);
                assertEquals(100, limit);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(1, locations.size());
                assertEquals(locationKey, locations.get(0).getKey());
                assertEquals(100, limit);
                List<Store> stores = new ArrayList<Store>();
                return stores;
            }
        });
        ops.selectResources(input, null);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResourcesIV() throws DataSourceException, ClientException {
        final Long locationKey = 12345L;
        final Double range = 6.7890D;
        final String rangeUnit = LocaleValidator.MILE_UNIT;
        JsonObject input = new GenericJsonObject();
        input.put(Store.LOCATION_KEY, locationKey);
        input.put(Demand.RANGE, range);
        input.put(Demand.RANGE_UNIT, rangeUnit);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        });
        ops.selectResources(input, null);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user, false);
    }
    ddd ****/
}
