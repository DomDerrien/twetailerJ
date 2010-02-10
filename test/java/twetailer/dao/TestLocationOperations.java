package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Location;
import twetailer.task.RobotResponder;
import twetailer.validator.LocaleValidator;

import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestLocationOperations {

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseOperations.setLogger(new MockLogger("test", null));
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testGetLogger() throws IOException {
        Logger log1 = new LocationOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new LocationOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
    }

    @Test
    public void testCreateI() throws ClientException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Location input = new Location();
        input.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        input.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        assertNull(input.getKey());

        input = ops.createLocation(input);
        assertNotNull(input.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testCreateII() throws ClientException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Location input = new Location();
        input.setLatitude(45.0D);
        input.setLongitude(-27.5D);
        assertNull(input.getKey());

        input = ops.createLocation(input);
        assertNotNull(input.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateIII() throws ClientException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Location input = new Location();
        assertNull(input.getKey());

        input = ops.createLocation(input);
    }

    @Test
    public void testCreateIV() throws ClientException {
        JsonObject input = new GenericJsonObject();
        input.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        input.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        Location object = new LocationOperations().createLocation(input);
        assertNotNull(object.getKey());
    }

    @Test
    public void testCreateV() throws ClientException {
        JsonObject input = new GenericJsonObject();
        input.put(Location.LATITUDE, 45.0D);
        input.put(Location.LONGITUDE, -27.5D);

        Location object = new LocationOperations().createLocation(input);
        assertNotNull(object.getKey());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateVI() throws ClientException {
        JsonObject input = new GenericJsonObject();
        input.put(Location.LATITUDE, 45.0D);

        new LocationOperations().createLocation(input);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateVII() throws ClientException {
        JsonObject input = new GenericJsonObject();
        input.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);
        input.put(Location.LONGITUDE, -27.5D);

        new LocationOperations().createLocation(input);
    }

    @Test
    public void testCreateVIII() throws ClientException {
        JsonObject input = new GenericJsonObject();
        input.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        input.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        Location first = new LocationOperations().createLocation(input);
        assertNotNull(first.getKey());

        Location second = new LocationOperations().createLocation(input);
        assertNotNull(second.getKey());

        // Object not created twice
        assertEquals(first.getKey(), second.getKey());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateIX() throws ClientException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                throw new RuntimeException("Done in purpose");
            }
        };
        Location input = new Location();
        input.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        input.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        assertNull(input.getKey());

        input = ops.createLocation(input);
    }

    @Test
    public void testCreateX() throws ClientException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };
        Location input = new Location();
        input.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        input.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        assertNull(input.getKey());

        input = ops.createLocation(input);
        assertNotNull(input.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Location object = new Location();
        object.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        object.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        object = ops.createLocation(pm, object); // Gives the PersistenceManager so it won't be closed

        Location selected = ops.getLocation(object.getKey());
        assertNotNull(selected.getKey());
        assertEquals(object.getKey(), selected.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIII() throws ClientException, DataSourceException {
        LocationOperations ops = new LocationOperations();
        ops.getLocation(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIV() throws ClientException, DataSourceException {
        LocationOperations ops = new LocationOperations();
        ops.getLocation(0L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetV() throws ClientException, DataSourceException {
        LocationOperations ops = new LocationOperations();
        ops.getLocation(888L);
    }

    @Test
    public void testGetsI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Location object = new Location();
        object.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        object.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        object = ops.createLocation(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Location> selection = ops.getLocations(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsII() throws ClientException, DataSourceException {
        List<Location> selection = new LocationOperations().getLocations(Location.LATITUDE, 45.0D, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test(expected=RuntimeException.class)
    public void testGetsIII() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, String key, Object value, int limit) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.getLocations(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE, 0);
    }

    /*
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };
     */
    @Test
    public void testUpdateI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Location object = new Location();
        object.setLatitude(45.0D);
        object.setLongitude(-27.5);
        object = ops.createLocation(pm, object); // Gives the PersistenceManager so it won't be closed

        object.setLatitude(-27.5D);

        Location updated = ops.updateLocation(object);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertEquals(Double.valueOf(-27.5D), updated.getLatitude());
        assertTrue(pm.isClosed());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateII() throws ClientException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public Location updateLocation(PersistenceManager pm, Location location) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.updateLocation(new Location());
    }

    @Test
    public void testGetsExtendedI() throws DataSourceException {
        LocationOperations ops = new LocationOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return mockAppEngineEnvironment.getPersistenceManager();
            }
        };

        Location source = new Location();
        source.setPostalCode("H8P3R8");
        source.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setPostalCode("H8P3R0");
        target.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        target.setLatitude(45.5D);
        target.setLongitude(-27.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        try {
            List<Location> selection = ops.getLocations(pm, source, 100.0D, LocaleValidator.KILOMETER_UNIT, 0);
            assertNotNull(selection);
            assertEquals(1, selection.size());
            assertEquals(target.getKey(), selection.get(0).getKey());
        }
        finally {
            pm.close();
        }
    }

    @Test
    public void testGetsExtendedII() throws DataSourceException {
        LocationOperations ops = new LocationOperations();

        Location source = new Location();
        source.setPostalCode("H8P3R8");
        source.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setPostalCode("H8P3R0");
        target.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        target.setLatitude(45.5D);
        target.setLongitude(-27.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        try {
            List<Location> selection = ops.getLocations(pm, source, 52.2D, LocaleValidator.MILE_UNIT, 0);
            assertNotNull(selection);
            assertEquals(1, selection.size());
            assertEquals(target.getKey(), selection.get(0).getKey());
        }
        finally {
            pm.close();
        }
    }

    @Test
    public void testGetsExtendedIII() throws DataSourceException {
        LocationOperations ops = new LocationOperations();

        Location source = new Location();
        source.setPostalCode("H8P3R8");
        source.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setLatitude(0.0D);
        target.setPostalCode("H8P3R0");
        target.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        target.setLongitude(-27.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        try {
            List<Location> selection = ops.getLocations(pm, source, 100.0D, LocaleValidator.MILE_UNIT, 50);
            assertNotNull(selection);
            assertEquals(0, selection.size());
        }
        finally {
            pm.close();
        }
    }

    @Test
    public void testGetsExtendedIV() throws DataSourceException {
        LocationOperations ops = new LocationOperations();

        Location source = new Location();
        source.setPostalCode("H8P3R8");
        source.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setPostalCode("H8P3R0");
        target.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        target.setLatitude(45.0D);
        target.setLongitude(-55.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        try {
            List<Location> selection = ops.getLocations(pm, source, 100.0D, LocaleValidator.MILE_UNIT, 50);
            assertNotNull(selection);
            assertEquals(0, selection.size());
        }
        finally {
            pm.close();
        }
    }

    @Test
    public void testGetsExtendedV() throws DataSourceException {
        LocationOperations ops = new LocationOperations();

        Location source = new Location();
        source.setPostalCode("H8P3R8");
        source.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setPostalCode("H8P3R0");
        target.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        target.setLatitude(45.0D);
        target.setLongitude(10.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        try {
            List<Location> selection = ops.getLocations(pm, source, 100.0D, LocaleValidator.MILE_UNIT, 50);
            assertNotNull(selection);
            assertEquals(0, selection.size());
        }
        finally {
            pm.close();
        }
    }

    @Test
    public void testGetsExtendedVI() throws DataSourceException {
        LocationOperations ops = new LocationOperations();

        Location source = new Location();
        source.setPostalCode("H8P3R8");
        source.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        source.setLatitude(90.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setPostalCode("H8P3R0");
        target.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        target.setLatitude(45.0D);
        target.setLongitude(10.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        try {
            List<Location> selection = ops.getLocations(pm, source, 100.0D, LocaleValidator.MILE_UNIT, 50);
            assertNotNull(selection);
            assertEquals(0, selection.size());
        }
        finally {
            pm.close();
        }
    }
}
