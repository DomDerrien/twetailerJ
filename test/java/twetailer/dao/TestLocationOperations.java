package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Location;
import twetailer.validator.LocaleValidator;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestLocationOperations {

    private MockAppEngineEnvironment mockAppEngineEnvironment;
    
	@Before
	public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
        mockAppEngineEnvironment.setUp();
        
        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
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
        input.setCountryCode("CA");
        input.setPostalCode("H0H0H0");
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

    @Test(expected=InvalidParameterException.class)
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
        input.put(Location.POSTAL_CODE, "H0H0H0");
        input.put(Location.COUNTRY_CODE, "CA");
        
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

    @Test(expected=InvalidParameterException.class)
    public void testCreateVI() throws ClientException {
        JsonObject input = new GenericJsonObject();
        input.put(Location.LATITUDE, 45.0D);
        
        new LocationOperations().createLocation(input);
    }

    @Test(expected=InvalidParameterException.class)
    public void testCreateVII() throws ClientException {
        JsonObject input = new GenericJsonObject();
        input.put(Location.COUNTRY_CODE, "CA");
        input.put(Location.LONGITUDE, -27.5D);
        
        new LocationOperations().createLocation(input);
    }

    @Test
    public void testCreateVIII() throws ClientException {
        JsonObject input = new GenericJsonObject();
        input.put(Location.POSTAL_CODE, "H0H0H0");
        input.put(Location.COUNTRY_CODE, "CA");
        
        Location first = new LocationOperations().createLocation(input);
        assertNotNull(first.getKey());
        
        Location second = new LocationOperations().createLocation(input);
        assertNotNull(second.getKey());
        
        // Object not created twice
        assertEquals(first.getKey(), second.getKey());
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
        object.setPostalCode("H0H0H0");
        object.setCountryCode("CA");
        object = ops.createLocation(pm, object); // Gives the PersistenceManager so it won't be closed
        
        Location selected = ops.getLocation(object.getKey());
        assertNotNull(selected.getKey());
        assertEquals(object.getKey(), selected.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=InvalidParameterException.class)
    public void testGetIII() throws ClientException, DataSourceException {
        LocationOperations ops = new LocationOperations();
        ops.getLocation(null);
    }

    @Test(expected=InvalidParameterException.class)
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
        object.setPostalCode("H0H0H0");
        object.setCountryCode("CA");
        object = ops.createLocation(pm, object); // Gives the PersistenceManager so it won't be closed
        
        List<Location> selection = ops.getLocations(Location.COUNTRY_CODE, "CA", 0);
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
    
    @Test
    public void testGetsExtendedI() throws DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations();
        
        Location source = new Location();
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setLatitude(45.5D);
        target.setLongitude(-27.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);
        
        List<Location> selection = ops.getLocations(pm, source, 1000.0D, LocaleValidator.KILOMETER_UNIT, 0);
        pm.close();
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(target.getKey(), selection.get(0).getKey());
    }
    
    @Test
    public void testGetsExtendedII() throws DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations();
        
        Location source = new Location();
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setLatitude(45.5D);
        target.setLongitude(-27.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);
        
        List<Location> selection = ops.getLocations(pm, source, 1000.0D, LocaleValidator.MILE_UNIT, 50);
        pm.close();
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(target.getKey(), selection.get(0).getKey());
    }
    
    @Test
    public void testGetsExtendedIII() throws DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations();
        
        Location source = new Location();
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setLatitude(0.0D);
        target.setLongitude(-27.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);
        
        List<Location> selection = ops.getLocations(pm, source, 1000.0D, LocaleValidator.MILE_UNIT, 50);
        pm.close();
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }
    
    @Test
    public void testGetsExtendedIV() throws DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations();
        
        Location source = new Location();
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setLatitude(45.0D);
        target.setLongitude(-55.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);
        
        List<Location> selection = ops.getLocations(pm, source, 1000.0D, LocaleValidator.MILE_UNIT, 50);
        pm.close();
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }
    
    @Test
    public void testGetsExtendedV() throws DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        LocationOperations ops = new LocationOperations();
        
        Location source = new Location();
        source.setLatitude(45.0D);
        source.setLongitude(-27.5D);
        source = ops.createLocation(source);

        Location target = new Location();
        target.setLatitude(45.0D);
        target.setLongitude(10.0D);
        target.setHasStore(Boolean.TRUE);
        target = ops.createLocation(target);
        
        List<Location> selection = ops.getLocations(pm, source, 1000.0D, LocaleValidator.MILE_UNIT, 50);
        pm.close();
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }
}
