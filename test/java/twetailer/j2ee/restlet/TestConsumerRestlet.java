package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.j2ee.LoginServlet;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestConsumerRestlet {

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

    ConsumerRestlet ops;

    @BeforeClass
    public static void setUpBeforeClass() {
        ConsumerRestlet.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        ops = new ConsumerRestlet();
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

    @Test(expected=RuntimeException.class)
    public void testCreateResource() throws DataSourceException {
        ops.createResource(new GenericJsonObject(), user);
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException {
        ops.deleteResource("resourceId", user);
    }

    @Test
    public void testGetResourceI() throws DataSourceException {
        final Long resourceId = 12345L;
        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(Long key) {
                assertEquals(resourceId, key);
                Consumer temp = new Consumer();
                temp.setKey(resourceId);
                return temp;
            }
        };
        JsonObject resource = ops.getResource(null, resourceId.toString(), user);
        assertEquals(resourceId.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceII() throws DataSourceException {
        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(Long key) {
                assertEquals(CONSUMER_KEY, key);
                Consumer temp = new Consumer();
                temp.setKey(CONSUMER_KEY);
                return temp;
            }
        };
        JsonObject resource = ops.getResource(null, "current", user);
        assertEquals(CONSUMER_KEY.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourcesI() throws DataSourceException {
        final String email = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put("qA", Consumer.EMAIL);
        parameters.put("qV", email);
        final Long resourceId = 12345L;
        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertTrue(value instanceof String);
                assertEquals(email, (String) value);
                Consumer temp = new Consumer();
                temp.setKey(resourceId);
                temp.setEmail(email);
                List<Consumer> temps = new ArrayList<Consumer>();
                temps.add(temp);
                return temps;
            }
        };
        JsonArray resources = ops.selectResources(parameters);
        assertEquals(1, resources.size());
        assertEquals(resourceId.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourcesII() throws DataSourceException {
        final String email = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put("q", email);
        final Long resourceId = 12345L;
        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertTrue(value instanceof String);
                assertEquals(email, (String) value);
                Consumer temp = new Consumer();
                temp.setKey(resourceId);
                temp.setEmail(email);
                List<Consumer> temps = new ArrayList<Consumer>();
                temps.add(temp);
                return temps;
            }
        };
        JsonArray resources = ops.selectResources(parameters);
        assertEquals(1, resources.size());
        assertEquals(resourceId.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourcesIII() throws DataSourceException {
        final String email = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put("qA", ""); // To fall back on "qA = Consumer.EMAIL"
        parameters.put("qV", email);
        final Long resourceId = 12345L;
        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertTrue(value instanceof String);
                assertEquals(email, (String) value);
                Consumer temp = new Consumer();
                temp.setKey(resourceId);
                temp.setEmail(email);
                List<Consumer> temps = new ArrayList<Consumer>();
                temps.add(temp);
                return temps;
            }
        };
        JsonArray resources = ops.selectResources(parameters);
        assertEquals(1, resources.size());
        assertEquals(resourceId.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testSelectResourcesv() throws DataSourceException {
        final String email = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put("q", email);
        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(String key, Object value, int index) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };
        ops.selectResources(parameters);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user);
    }
}
