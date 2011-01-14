package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import twetailer.ReservedOperationException;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dto.Location;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

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
    public void testCreateResource() throws DataSourceException, ClientException {
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(JsonObject location) {
                return new Location();
            }
        });
        ops.createResource(null, user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException {
        ops.deleteResource("resourceId", user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testGetResource() throws InvalidIdentifierException {
        ops.getResource(null, "resourceId", user, false);
    }

    @Test
    public void testSelectResourcesI() throws DataSourceException, ClientException {
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                fail("Call not expected");
                return null;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                fail("Call not expected");
                return null;
            }
        });
        JsonArray response = ops.selectResources(new GenericJsonObject(), null, false);
        assertEquals(0, response.size());
    }

    @Test
    public void testSelectResourcesII() throws DataSourceException, ClientException {
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                fail("Call not expected");
                return null;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                fail("Call not expected");
                return null;
            }
        });
        JsonObject parameters = new GenericJsonObject();
        parameters.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, Boolean.TRUE);
        JsonArray response = ops.selectResources(parameters, null, false);
        assertEquals(0, response.size());
    }

    @Test(expected=ReservedOperationException.class)
    public void testUpdateResource() throws DataSourceException, NumberFormatException, ReservedOperationException, InvalidIdentifierException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user, false);
    }
}
