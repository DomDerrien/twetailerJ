package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import twetailer.ReservedOperationException;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Entity;
import twetailer.dto.RawCommand;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestRawCommandRestlet {

    RawCommandRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new RawCommandRestlet();
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

    @Test(expected=ReservedOperationException.class)
    public void testGetResourceI() throws InvalidIdentifierException, ReservedOperationException {
        ops.getResource(new GenericJsonObject(), "not important!", user, false);
    }

    @Test
    public void testGetResourceII() throws InvalidIdentifierException, ReservedOperationException {
        final Long rawCommandKey = 54645434L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(rawCommandKey, key);
                RawCommand resource = new RawCommand();
                resource.setKey(rawCommandKey);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(new GenericJsonObject(), rawCommandKey.toString(), user, true);
        assertEquals(rawCommandKey.longValue(), resource.getLong(RawCommand.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testSelectResourcesI() throws DataSourceException, ClientException {
        ops.selectResources(new GenericJsonObject(), user, false);
    }

    @Test(expected=ReservedOperationException.class)
    public void testSelectResourcesII() throws DataSourceException, ClientException {
        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, true);
        ops.selectResources(params, user, false);
    }

    @Test(expected=ReservedOperationException.class)
    public void testSelectResourcesIII() throws DataSourceException, ClientException {
        ops.selectResources(new GenericJsonObject(), user, true);
    }

    @Test(expected=DataSourceException.class)
    public void testSelectResourcesIV() throws DataSourceException, ClientException {
        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, true);
        ops.selectResources(params, user, true);
    }

    @Test
    public void testSelectResourcesV() throws DataSourceException, ClientException {
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public List<Long> getRawCommandKeys(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                assertTrue(params.containsKey(']' + Entity.CREATION_DATE));
                return Arrays.asList(new Long[] { 1111L, 22222L, 33333L });
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, true);
        params.put(Entity.CREATION_DATE, "2000-01-01T00:00:00");
        JsonArray resources = ops.selectResources(params, user, true);
        assertEquals(3, resources.size());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateResource() throws DataSourceException, ClientException {
        ops.createResource(new GenericJsonObject(), user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException, ClientException {
        ops.updateResource(new GenericJsonObject(), "not important!", user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException, ClientException {
        ops.deleteResource("not important!", user, false);
    }
}
