package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;

import com.google.appengine.api.users.User;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestDemandsRestlet {

    static final User user = new User("test-email", "test-domain");
    DemandsRestlet ops;

    @Before
    public void setUp() throws Exception {
        ops = new DemandsRestlet();
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
        final Long resourceId = 12345L;
        ops.setConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(proposedPM, pm);
                assertFalse(pm.isClosed());
                assertEquals(Consumer.EMAIL, key);
                assertTrue(value instanceof String);
                assertEquals(user.getEmail(), (String) value);
                assertEquals(1, limit);
                Consumer temp = new Consumer();
                temp.setKey(resourceId);
                temp.setEmail(user.getEmail());
                List<Consumer> temps = new ArrayList<Consumer>();
                temps.add(temp);
                return temps;
            }
        });
        ops.setDemandOperations(new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return proposedPM;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long consumerKey) throws ClientException {
                assertEquals(proposedPM, pm);
                assertFalse(pm.isClosed());
                assertEquals(proposedParameters, parameters);
                assertEquals(resourceId, consumerKey);
                Demand temp = new Demand();
                temp.setConsumerKey(consumerKey);
                temp.setKey(resourceId);
                return temp;
            }
        });

        JsonObject returnedDemand = ops.createResource(proposedParameters, user);
        assertTrue(proposedPM.isClosed());
        assertNotNull(returnedDemand);
        assertTrue(returnedDemand.containsKey(Entity.KEY));
        assertEquals(resourceId.longValue(), returnedDemand.getLong(Entity.KEY));
        assertTrue(returnedDemand.containsKey(Demand.CONSUMER_KEY));
        assertEquals(resourceId.longValue(), returnedDemand.getLong(Demand.CONSUMER_KEY));
    }

    @Test
    public void testCreateResourceII() throws DataSourceException, ClientException {
        final PersistenceManager proposedPM = new MockPersistenceManager();
        final JsonObject proposedParameters = new GenericJsonObject();
        ops.setConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(proposedPM, pm);
                assertFalse(pm.isClosed());
                assertEquals(Consumer.EMAIL, key);
                assertTrue(value instanceof String);
                assertEquals(user.getEmail(), (String) value);
                assertEquals(1, limit);
                return new ArrayList<Consumer>();
            }
        });
        ops.setDemandOperations(new DemandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return proposedPM;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long consumerKey) throws ClientException {
                fail("Should not be called!");
                return null;
            }
        });

        JsonObject returnedDemand = ops.createResource(proposedParameters, user);
        assertTrue(proposedPM.isClosed());
        assertNull(returnedDemand);
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
