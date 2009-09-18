package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;

import com.google.appengine.api.users.User;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestConsumersRestlet {

    static final User user = new User("test-email", "test-domain");
    ConsumersRestlet ops;

    @Before
    public void setUp() throws Exception {
        ops = new ConsumersRestlet();
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
        ops.setConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(Long key) {
                assertEquals(resourceId, key);
                Consumer temp = new Consumer();
                temp.setKey(resourceId);
                return temp;
            }
        });
        JsonObject resource = ops.getResource(null, resourceId.toString(), user);
        assertEquals(resourceId.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testGetResourceII() throws DataSourceException {
        final Long resourceId = 12345L;
        ops.setConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(Long key) {
                assertEquals(resourceId, key);
                return null;
            }
        });
        ops.getResource(null, resourceId.toString(), user);
    }

    @Test
    public void testGetResourceIII() throws DataSourceException {
        final Long resourceId = 12345L;
        ops.setConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(String attribute, Object value, int limit) {
                assertEquals(Consumer.EMAIL, attribute);
                assertEquals(user.getEmail(), (String) value);
                Consumer temp = new Consumer();
                temp.setKey(resourceId);
                List<Consumer> list = new ArrayList<Consumer>();
                list.add(temp);
                return list;
            }
        });
        JsonObject resource = ops.getResource(null, "current", user);
        assertEquals(resourceId.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testGetResourceIV() throws DataSourceException {
        ops.setConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(String attribute, Object value, int limit) {
                assertEquals(Consumer.EMAIL, attribute);
                assertEquals(user.getEmail(), (String) value);
                return new ArrayList<Consumer>();
            }
        });

        ops.getResource(null, "current", user);
    }

    @Test
    public void testSelectResourcesI() throws DataSourceException {
        final String email = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put("qA", Consumer.EMAIL);
        parameters.put("qV", email);
        final Long resourceId = 12345L;
        ops.setConsumerOperations(new ConsumerOperations() {
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
        });
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
        ops.setConsumerOperations(new ConsumerOperations() {
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
        });
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
        ops.setConsumerOperations(new ConsumerOperations() {
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
        });
        JsonArray resources = ops.selectResources(parameters);
        assertEquals(1, resources.size());
        assertEquals(resourceId.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testSelectResourcesv() throws DataSourceException {
        final String email = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put("q", email);
        ops.setConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(String key, Object value, int index) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        });
        ops.selectResources(parameters);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user);
    }
}
