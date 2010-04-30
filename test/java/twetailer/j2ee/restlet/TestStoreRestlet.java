package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.j2ee.MockLoginServlet;
import twetailer.validator.LocaleValidator;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestStoreRestlet {

    StoreRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
        StoreRestlet.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        ops = new StoreRestlet();
        user = MockLoginServlet.buildMockOpenIdUser();
    }

    @After
    public void tearDown() throws Exception {
        StoreRestlet.saleAssociateRestlet = new SaleAssociateRestlet();

        StoreRestlet._baseOperations = new BaseOperations();
        StoreRestlet.locationOperations = StoreRestlet._baseOperations.getLocationOperations();
        StoreRestlet.saleAssociateOperations = StoreRestlet._baseOperations.getSaleAssociateOperations();
        StoreRestlet.storeOperations = StoreRestlet._baseOperations.getStoreOperations();
    }

    @Test
    public void testGetLogger() {
        ops.getLogger();
        assertTrue(true);
        assertNull(null);
    }

    @Test(expected=ClientException.class)
    public void testCreateResourceI() throws DataSourceException, ClientException {
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        };
        ops.createResource(null, user);
    }

    @Test(expected=ClientException.class)
    public void testCreateResourceII() throws DataSourceException, ClientException {
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        };
        user.setAttribute("info", null);
        ops.createResource(null, user);
    }

    @Test(expected=ClientException.class)
    @SuppressWarnings("unchecked")
    public void testCreateResourceIII() throws DataSourceException, ClientException {
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        };
        ((Map<String, String>) user.getAttribute("info")).put("email", "unit@test");
        ops.createResource(null, user);
    }

    @Test(expected=ClientException.class)
    @SuppressWarnings("unchecked")
    public void testCreateResourceIV() throws DataSourceException, ClientException {
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        };
        ((Map<String, String>) user.getAttribute("info")).put("email", "unit@test");
        ops.createResource(null, user);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateResourceV() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        final Long locationKey = 23456L;
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
        };
        StoreRestlet.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                resource.setHasStore(true);
                return resource;
            }
        };
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        JsonObject response = ops.createResource(null, user);
        assertNotNull(response);
        assertEquals(storeKey.longValue(), response.getLong(Store.KEY));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateResourceVI() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        final Long locationKey = 23456L;
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
        };
        StoreRestlet.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                resource.setHasStore(true);
                return resource;
            }
        };
        ((Map<String, String>) user.getAttribute("info")).put("email", "steven.milstein@gmail.com");
        JsonObject response = ops.createResource(null, user);
        assertNotNull(response);
        assertEquals(storeKey.longValue(), response.getLong(Store.KEY));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateResourceVII() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        final Long locationKey = 23456L;
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
        };
        StoreRestlet.locationOperations = new LocationOperations() {
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
        };
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        JsonObject response = ops.createResource(null, user);
        assertNotNull(response);
        assertEquals(storeKey.longValue(), response.getLong(Store.KEY));
    }

    @Test(expected=ClientException.class)
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        ops.deleteResource("resourceId", user);
    }

    @Test
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        new StoreRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
            }
        }.deleteResource(storeKey.toString(), user);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        new StoreRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.deleteResource(storeKey.toString(), user);
    }

    @Test
    public void testDelegateDeletionResourceI() throws DataSourceException, ClientException {
        //
        // Store without Sale Associates
        //
        final Long storeKey = 12345L;
        StoreRestlet.storeOperations = new StoreOperations() {
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
        };
        StoreRestlet.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                return saleAssociateKeys;
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), storeKey);
    }

    @Test
    @SuppressWarnings("serial")
    public void testDelegateDeletionResourceII() throws DataSourceException, ClientException {
        //
        // Store without Sale Associates
        //
        final Long storeKey = 12345L;
        StoreRestlet.storeOperations = new StoreOperations() {
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
        };
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
        };
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
        ops.getResource(null, "resourceId", user);
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
        StoreRestlet.locationOperations = new LocationOperations() {
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
        };
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                assertEquals(1, locations.size());
                assertEquals(locationKey, locations.get(0).getKey());
                assertEquals(100, limit);
                List<Store> stores = new ArrayList<Store>();
                return stores;
            }
        };
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

        StoreRestlet.locationOperations = new LocationOperations() {
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
        };
        StoreRestlet.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                assertEquals(1, locations.size());
                assertEquals(locationKey, locations.get(0).getKey());
                assertEquals(100, limit);
                List<Store> stores = new ArrayList<Store>();
                return stores;
            }
        };
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

        StoreRestlet.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.selectResources(input, null);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user);
    }
}
