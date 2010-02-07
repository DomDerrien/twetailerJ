package twetailer.j2ee.restlet;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import javamocks.util.logging.MockLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.LocationOperations;
import twetailer.dto.Location;
import twetailer.j2ee.TestBaseRestlet;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestLocationRestlet {

    LocationRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
        LocationRestlet.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        ops = new LocationRestlet();
        user = TestBaseRestlet.setupOpenIdUser();
    }

    @After
    public void tearDown() throws Exception {
        LocationRestlet.locationOperations = new LocationOperations();
    }

    @Test
    public void testGetLogger() {
        ops.getLogger();
        assertTrue(true);
        assertNull(null);
    }

    @Test
    public void testCreateResource() throws DataSourceException, ClientException {
        LocationRestlet.locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(JsonObject location) {
                return new Location();
            }
        };
        ops.createResource(null, user);
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
    public void testSelectResources() throws DataSourceException {
        ops.selectResources(new GenericJsonObject(), null);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user);
    }
}
