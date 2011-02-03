package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestLocationRestlet {

    LocationRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new LocationRestlet();
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

    @Test
    public void testGetResource() throws InvalidIdentifierException {
        final Long locationKey = 54645434L;
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(new GenericJsonObject(), locationKey.toString(), user, false);
        assertEquals(locationKey.longValue(), resource.getLong(Location.KEY));
    }

    @Test
    public void testSelectResourcesIa() throws DataSourceException, ClientException {
        final Long locationKey = 54645434L;
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.CENTER_ONLY_KEY, Boolean.TRUE);
        params.put(Location.LOCATION_KEY, locationKey);

        JsonArray resources = ops.selectResources(params, user, false);
        assertEquals(locationKey.longValue(), resources.getJsonObject(0).getLong(Location.KEY));
    }

    @Test
    public void testSelectResourcesIb() throws DataSourceException, ClientException {
        final Long locationKey = 54645434L;
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                assertEquals(3.14159D, range.doubleValue(), 0.0);
                assertEquals(LocaleValidator.ALTERNATE_MILE_UNIT, rangeUnit);
                return Arrays.asList(new Location[] { new Location() });
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(Location.LOCATION_KEY, locationKey);
        params.put(Demand.RANGE, 3.14159D);
        params.put(Demand.RANGE_UNIT, "miles");

        JsonArray resources = ops.selectResources(params, user, false);
        assertEquals(2, resources.size());
    }

    @Test
    public void testSelectResourcesIIa() throws DataSourceException, ClientException {
        final Long locationKey = 54645434L;
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, Boolean.TRUE);
        params.put(BaseRestlet.CENTER_ONLY_KEY, Boolean.TRUE);
        params.put(Location.LOCATION_KEY, locationKey);

        JsonArray resources = ops.selectResources(params, user, false);
        assertEquals(locationKey.longValue(), resources.getLong(0));
    }

    @Test
    public void testSelectResourcesIIb() throws DataSourceException, ClientException {
        final Long locationKey = 54645434L;
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                assertEquals(3.14159D, range.doubleValue(), 0.0);
                assertEquals(LocaleValidator.ALTERNATE_MILE_UNIT, rangeUnit);
                return Arrays.asList(new Location[] { new Location() });
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, Boolean.TRUE);
        params.put(Location.LOCATION_KEY, locationKey);
        params.put(Demand.RANGE, 3.14159D);
        params.put(Demand.RANGE_UNIT, "miles");

        JsonArray resources = ops.selectResources(params, user, false);
        assertEquals(2, resources.size());
    }

    @Test
    public void testCreateResource() throws DataSourceException, ClientException {
        final Long locationKey = 54645434L;
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject params) {
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
        });

        JsonObject resource = ops.createResource(new GenericJsonObject(), user, false);
        assertEquals(locationKey.longValue(), resource.getLong(Location.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testUpdateResourceI() throws DataSourceException, ClientException {
        ops.updateResource(new GenericJsonObject(), "not important!", user, false);
    }

    @Test
    public void testUpdateResourceII() throws DataSourceException, ClientException {
        final Long locationKey = 54645434L;
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(Location.LOCATION_KEY, locationKey);
        params.put(Location.POSTAL_CODE, "h0h 0h0");

        JsonObject resource = ops.updateResource(params, locationKey.toString(), user, true);
        assertEquals(locationKey.longValue(), resource.getLong(Location.KEY));
        assertEquals("H0H0H0", resource.getString(Location.POSTAL_CODE));
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException, ClientException {
        ops.deleteResource("not important!", user, false);
    }
}
