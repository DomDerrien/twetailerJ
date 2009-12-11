package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
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
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.j2ee.LoginServlet;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestDemandRestlet {

    static final String OPEN_ID = "http://unit.test";
    static final Long CONSUMER_KEY = 12345L;

    static final OpenIdUser user = OpenIdUser.populate(
            "http://www.yahoo.com",
            YadisDiscovery.IDENTIFIER_SELECT,
            LoginServlet.YAHOO_OPENID_SERVER_URL
    );
    static {
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("a", OPEN_ID);
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", new HashMap<String, String>());
        Map<String, String> info = new HashMap<String, String>();
        attributes.put("info", info);
        json.put("g", attributes);
        user.fromJSON(json);
        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, CONSUMER_KEY);
    }

    DemandRestlet ops;

    @BeforeClass
    public static void setUpBeforeClass() {
        DemandRestlet.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        ops = new DemandRestlet();
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

    @Test
    public void testCreateResourceI() throws DataSourceException, ClientException {
        final PersistenceManager proposedPM = new MockPersistenceManager();
        final JsonObject proposedParameters = new GenericJsonObject();
        final Source source = Source.simulated;
        final Long resourceId = 12345L;
        ops.demandOperations = new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return proposedPM;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long OwnerKey) throws ClientException {
                assertEquals(proposedPM, pm);
                assertFalse(pm.isClosed());
                assertEquals(proposedParameters, parameters);
                assertEquals(CONSUMER_KEY, OwnerKey);
                Demand temp = new Demand();
                temp.setOwnerKey(OwnerKey);
                temp.setKey(resourceId);
                temp.setSource(source);
                return temp;
            }
        };

        JsonObject returnedDemand = ops.createResource(proposedParameters, user);
        assertTrue(proposedPM.isClosed());
        assertNotNull(returnedDemand);
        assertTrue(returnedDemand.containsKey(Entity.KEY));
        assertEquals(resourceId.longValue(), returnedDemand.getLong(Entity.KEY));
        assertTrue(returnedDemand.containsKey(Command.OWNER_KEY));
        assertEquals(CONSUMER_KEY.longValue(), returnedDemand.getLong(Command.OWNER_KEY));
    }

    @Test(expected=RuntimeException.class)
    public void testCreateResourceIII() throws DataSourceException, ClientException {
        final JsonObject proposedParameters = new GenericJsonObject();
        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                throw new RuntimeException("done in purpose");
            }
        };

        ops.createResource(proposedParameters, user);
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException {
        ops.deleteResource("12345", user);
    }

    @Test(expected=RuntimeException.class)
    public void testGetResource() throws DataSourceException {
        ops.getResource(null, "12345", user);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResources() throws DataSourceException {
        ops.selectResources(null);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(null, "12345", user);
    }
}
