package twetailer.j2ee.restlet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.cache.MockCacheFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.dao.CacheHandler;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MaelzelServlet;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestConsumerRestlet {

    ConsumerRestlet ops;
    OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new ConsumerRestlet();
        user = MockLoginServlet.buildMockOpenIdUser();
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
        CacheHandler.injectMockCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @Test
    public void testGetLogger() throws DataSourceException, ClientException {
        assertNotNull(ops.getLogger());
    }

    @Test(expected=ReservedOperationException.class)
    public void testCreateResource() throws DataSourceException, ReservedOperationException {
        ops.createResource(new GenericJsonObject(), user, false);
    }

    @Test
    public void testGetResourceI() throws DataSourceException, ClientException {
        final Long resourceId = 12345L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(resourceId, key);
                Consumer resource = new Consumer();
                resource.setKey(resourceId);
                return resource;
            }
        });
        JsonObject resource = ops.getResource(null, resourceId.toString(), user, true);
        assertEquals(resourceId.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceII() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer resource = new Consumer();
                resource.setKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
        });
        JsonObject resource = ops.getResource(null, "current", user, false);
        assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testGetResourceIII() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(12345L, key.longValue());
                Consumer resource = new Consumer();
                resource.setKey(12345L);
                return resource;
            }
        });
        JsonObject resource = ops.getResource(null, "12345", user, true);
        assertEquals(12345L, resource.getLong(Entity.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testGetResourceIV() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                fail("Unexpected call");
                return null;
            }
        });
        ops.getResource(null, "12345", user, false);
    }

    @Test(expected=ReservedOperationException.class)
    public void testGetResourcesI() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                fail("Unexpected call");
                return null;
            }
            @Override
            public List<Long> getConsumerKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                fail("Unexpected call");
                return null;
            }
        });
        ops.selectResources(null, user, false);
    }

    @Test
    public void testGetResourcesII() throws DataSourceException, ClientException {
        final Date date = new Date(123456000L); // 3 last digit being null because milliseconds will be rounded
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                assertTrue(parameters.containsKey('>' + Entity.MODIFICATION_DATE));
                assertEquals(date, parameters.get('>' + Entity.MODIFICATION_DATE));
                return new ArrayList<Consumer>();
            }
            @Override
            public List<Long> getConsumerKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                fail("Unexpected call");
                return null;
            }
        });
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Entity.MODIFICATION_DATE, DateUtils.dateToISO(date));
        ops.selectResources(parameters, user, true);
    }

    @Test
    public void testGetResourcesIII() throws DataSourceException, ClientException {
        final Date date = new Date(123456000L); // 3 last digit being null because milliseconds will be rounded
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                fail("Unexpected call");
                return null;
            }
            @Override
            public List<Long> getConsumerKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                assertTrue(parameters.containsKey('>' + Entity.MODIFICATION_DATE));
                assertEquals(date, parameters.get('>' + Entity.MODIFICATION_DATE));
                return new ArrayList<Long>();
            }
        });
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Entity.MODIFICATION_DATE, DateUtils.dateToISO(date));
        parameters.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, true);
        ops.selectResources(parameters, user, true);
    }

    @Test(expected=ClientException.class)
    public void testUpdateResourceI() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        MockLoginServlet.updateConsumerKey(user, consumerKey);
        ops.updateResource(null, "0", user, false);
    }

    @Test
    public void testUpdateResourceII() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        MockLoginServlet.updateConsumerKey(user, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key, boolean useCache) {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });
        ops.updateResource(new GenericJsonObject(), "current", user, false);
    }

    @Test
    public void testUpdateResourceIII() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        MockLoginServlet.updateConsumerKey(user, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
                return consumer;
            }
        });
        ops.updateResource(new GenericJsonObject(), "current", user, false);
    }

    @Test
    public void testUpdateResourceIVa() throws DataSourceException, ClientException {
        final String email = "unit@test.ca";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.EMAIL, email);
        parameters.put(Consumer.EMAIL + "Code", MaelzelServlet.getCode(Consumer.EMAIL, email, user.getClaimedId()));
        final Long consumerKey = 12345L;
        MockLoginServlet.updateConsumerKey(user, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                return new ArrayList<Consumer>();
            }
       });
       ops.updateResource(parameters, "current", user, false);
    }

    @Test
    public void testUpdateResourceIVb() throws DataSourceException, ClientException {
        final String jabberId = "unit@test.ca";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.JABBER_ID, jabberId);
        parameters.put(Consumer.JABBER_ID + "Code", MaelzelServlet.getCode(Consumer.JABBER_ID, jabberId, user.getClaimedId()));
        final Long consumerKey = 12345L;
        MockLoginServlet.updateConsumerKey(user, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                return new ArrayList<Consumer>();
            }
       });
       ops.updateResource(parameters, "current", user, false);
    }

    @Test
    public void testUpdateResourceIVc() throws DataSourceException, ClientException {
        final String twitterId = "unit_test_ca";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.TWITTER_ID, twitterId);
        parameters.put(Consumer.TWITTER_ID + "Code", MaelzelServlet.getCode(Consumer.TWITTER_ID, twitterId, user.getClaimedId()));
        final Long consumerKey = 12345L;
        MockLoginServlet.updateConsumerKey(user, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                return new ArrayList<Consumer>();
            }
       });
       ops.updateResource(parameters, "current", user, false);
    }

    @Test
    public void testUpdateResourceV() throws DataSourceException, ClientException {
        final String email = "unit@test.net";
        final String jabberId = "unit@test.net";
        final String facebookId = "6547566978797";
        final String twitterId = "unit_test_ca";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.EMAIL, email);
        parameters.put(Consumer.JABBER_ID, jabberId);
        parameters.put(Consumer.FACEBOOK_ID, facebookId);
        parameters.put(Consumer.TWITTER_ID, twitterId);

        final Long consumerKey = 12345L;
        MockLoginServlet.updateConsumerKey(user, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                // Because of consolidation
                return new ArrayList<Consumer>();
            }
       });

       JsonObject resource = ops.updateResource(parameters, "12345", user, true);
       assertEquals(email, resource.getString(Consumer.EMAIL));
       assertEquals(jabberId, resource.getString(Consumer.JABBER_ID));
       assertEquals(facebookId, resource.getString(Consumer.FACEBOOK_ID));
       assertEquals(twitterId, resource.getString(Consumer.TWITTER_ID));
    }

    @Test(expected=ClientException.class)
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        ops.deleteResource("resourceId", user, false);
    }
}
