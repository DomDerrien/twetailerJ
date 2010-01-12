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

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.LocationOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Store;
import twetailer.j2ee.TestBaseRestlet;
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

    @Test(expected=ClientException.class)
    public void testCreateResourceI() throws DataSourceException, ClientException {
        ops.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(JsonObject store) {
                fail("Unexpected call");
                return null;
            }
        };
        ops.createResource(null, user);
    }

    @Test(expected=ClientException.class)
    public void testCreateResourceII() throws DataSourceException, ClientException {
        ops.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(JsonObject store) {
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
        ops.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(JsonObject store) {
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
        ops.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(JsonObject store) {
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
        final long storeKey = 12345L;
        ops.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
                return resource;
            }
        };
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        JsonObject response = ops.createResource(null, user);
        assertNotNull(response);
        assertEquals(storeKey, response.getLong(Store.KEY));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateResourceVI() throws DataSourceException, ClientException {
        final long storeKey = 12345L;
        ops.storeOperations = new StoreOperations() {
            @Override
            public Store createStore(JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
                return resource;
            }
        };
        ((Map<String, String>) user.getAttribute("info")).put("email", "steven.milstein@gmail.com");
        JsonObject response = ops.createResource(null, user);
        assertNotNull(response);
        assertEquals(storeKey, response.getLong(Store.KEY));
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException {
        ops.deleteResource("resourceId", user);
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
        ops.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                assertEquals(locationKey, location.getKey());
                assertEquals(100, limit);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        };
        ops.storeOperations = new StoreOperations() {
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

        ops.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double givenRange, String givenRangeUnit, int limit) {
                assertEquals(locationKey, location.getKey());
                assertEquals(range, givenRange);
                assertEquals(rangeUnit, givenRangeUnit);
                assertEquals(100, limit);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        };
        ops.storeOperations = new StoreOperations() {
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
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user);
    }
}
