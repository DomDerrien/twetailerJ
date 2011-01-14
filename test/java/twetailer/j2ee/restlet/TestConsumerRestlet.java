package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.dao.BaseOperations;
import twetailer.dao.CacheHandler;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.j2ee.MaelzelServlet;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.taskqueue.MockQueue;
import com.google.appengine.api.taskqueue.Queue;

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
        CacheHandler.injectCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        CacheHandler.injectCacheFactory(null);
        CacheHandler.injectCache(null);
    }

    @Test(expected=ReservedOperationException.class)
    public void testCreateResource() throws DataSourceException, ReservedOperationException {
        ops.createResource(new GenericJsonObject(), user, false);
    }

    /**** ddd
    @Test
    public void testGetResourceI() throws DataSourceException, ClientException {
        final Long resourceId = 12345L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(resourceId, key);
                Consumer temp = new Consumer();
                temp.setKey(resourceId);
                return temp;
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
                Consumer temp = new Consumer();
                temp.setKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return temp;
            }
        });
        JsonObject resource = ops.getResource(null, "current", user, false);
        assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testDelegateResourcesSelectionI() throws DataSourceException {
        final String email = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.EMAIL, email);
        final Long resourceId = 12345L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                Consumer consumer = new Consumer();
                consumer.setKey(resourceId);
                consumer.setEmail(email);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        });
        JsonArray resources = ops.delegateResourceSelection(new MockPersistenceManager(), parameters);
        assertEquals(1, resources.size());
        assertEquals(resourceId.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testDelegateResourcesSelectionII() throws DataSourceException {
        final String jabberId = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.JABBER_ID, jabberId);
        final Long resourceId = 12345L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                Consumer consumer = new Consumer();
                consumer.setKey(resourceId);
                consumer.setEmail(jabberId);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        });
        JsonArray resources = ops.delegateResourceSelection(new MockPersistenceManager(), parameters);
        assertEquals(1, resources.size());
        assertEquals(resourceId.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testDelegateResourcesSelectionIII() throws DataSourceException {
        final String twitterId = "d_d";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.TWITTER_ID, twitterId);
        final Long resourceId = 12345L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                Consumer consumer = new Consumer();
                consumer.setKey(resourceId);
                consumer.setEmail(twitterId);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        });
        JsonArray resources = ops.delegateResourceSelection(new MockPersistenceManager(), parameters);
        assertEquals(1, resources.size());
        assertEquals(resourceId.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testDelegateResourcesSelectionIV() throws DataSourceException {
        JsonObject parameters = new GenericJsonObject();
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                fail("Call not expected!");
                return null;
            }
        });
        JsonArray resources = ops.delegateResourceSelection(new MockPersistenceManager(), parameters);
        assertEquals(0, resources.size());
    }

    @Test
    public void testSelectResourcesI() throws DataSourceException, ClientException {
        JsonObject parameters = new GenericJsonObject();
        JsonArray resources = ops.selectResources(parameters, user, true);
        assertEquals(0, resources.size());
    }

    @Test(expected=ClientException.class)
    public void testSelectResourcesII() throws DataSourceException, ClientException {
        JsonObject parameters = new GenericJsonObject();
        ops.selectResources(parameters, user);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "serial" })
    public void testSelectResourcesIII() throws DataSourceException, ClientException {
        JsonObject parameters = new GenericJsonObject();
        new ConsumerRestlet() {
            @Override
            protected JsonArray delegateResourceSelection(PersistenceManager pm, JsonObject parameters) throws DataSourceException {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.selectResources(parameters, user, true);
    }

    @Test
    public void testFilterOutInvalidValueI() throws DataSourceException {
        String topic = "zzz";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        // No attribute
        // No validation code

        String attribute = ConsumerRestlet.filterOutInvalidValue(parameters, topic, openId);

        assertNull(attribute);
    }
    ***/

    @Test
    public void testFilterOutInvalidValueIIa() throws DataSourceException {
        String topic = "zzz";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(topic, "yyy");
        // No validation code

        String attribute = ConsumerRestlet.filterOutInvalidValue(parameters, topic, openId);

        assertNull(attribute);
    }

    @Test
    public void testFilterOutInvalidValueIIb() throws DataSourceException {
        String topic = "zzz";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        // No attribute
        parameters.put(topic + "Code", 0L); // Value not important

        String attribute = ConsumerRestlet.filterOutInvalidValue(parameters, topic, openId);

        assertNull(attribute);
    }

    @Test
    public void testFilterOutInvalidValueIII() throws DataSourceException {
        String topic = Consumer.EMAIL;
        String value = "unit@test.ca";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(topic, value);
        parameters.put(topic + "Code", 0L); // invalid code

        String attribute = ConsumerRestlet.filterOutInvalidValue(parameters, topic, openId);

        assertNull(attribute);
    }

    @Test
    public void testFilterOutInvalidValueIV() throws DataSourceException {
        String topic = Consumer.EMAIL;
        String value = "invalid e-mail address";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(topic, value);
        parameters.put(topic + "Code", 0L); // invalid code

        String attribute = ConsumerRestlet.filterOutInvalidValue(parameters, topic, openId);

        assertNull(attribute);
    }

    @Test
    public void testFilterOutInvalidValueV() throws DataSourceException, ClientException {
        String topic = Consumer.EMAIL;
        String value = "unit@test.ca";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(topic, value);
        parameters.put(topic + "Code", MaelzelServlet.getCode(topic, value, openId)); // valid code

        String attribute = ConsumerRestlet.filterOutInvalidValue(parameters, topic, openId);

        assertEquals(value, attribute);
    }

    @Test
    public void testScheduleConsolidationTasksIa() throws DataSourceException, ClientException {
        ConsumerRestlet.scheduleConsolidationTasks("<don't care>", null, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIb() throws DataSourceException, ClientException {
        ConsumerRestlet.scheduleConsolidationTasks("<don't care>", "", 0L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testScheduleConsolidationTasksIc() throws DataSourceException, ClientException {
        ConsumerRestlet.scheduleConsolidationTasks("<don't care>", "zzz", 0L);
    }

    @Test(expected=RuntimeException.class)
    public void testScheduleConsolidationTasksII() throws DataSourceException, ClientException {
        final String email = "unit@test.ca";
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        });
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIIa() throws DataSourceException, ClientException {
        final String email = "unit@test.ca";
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                return new ArrayList<Consumer>();
            }
        });
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIIb() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String email = "unit@test.ca";
        final Consumer consumer = new Consumer();
        consumer.setEmail(email);
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
                assertEquals("~" + email, consumer.getEmail());
                return consumer;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                return new ArrayList<Long>();
            }
        });
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIIIa() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String email = "unit@test.ca";
        final Consumer consumer = new Consumer();
        consumer.setEmail(email);
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
                assertEquals("~" + email, consumer.getEmail());
                return consumer;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                return new ArrayList<Long>();
            }
        });
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, consumerKey);
    }

    @Test
    public void testScheduleConsolidationTasksIIIb() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String jabberId = "unit@test.ca";
        final Consumer consumer = new Consumer();
        consumer.setJabberId(jabberId);
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals("~" + jabberId, consumer.getJabberId());
                return consumer;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                return new ArrayList<Long>();
            }
        });
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.JABBER_ID, jabberId, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIIIc() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String twitterId = "unit_test_ca";
        final Consumer consumer = new Consumer();
        consumer.setTwitterId(twitterId);
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals("~" + twitterId, consumer.getTwitterId());
                return consumer;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                return new ArrayList<Long>();
            }
        });
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.TWITTER_ID, twitterId, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIV() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String email = "unit@test.ca";
        final Consumer consumer = new Consumer();
        consumer.setEmail(email);
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals("~" + email, consumer.getEmail());
                return consumer;
            }
        });
        final Long demandKey = 12345L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> keys = new ArrayList<Long>();
                keys.add(demandKey);
                return keys;
            }
        });
        final MockQueue queue = new MockQueue();
        BaseSteps.setMockBaseOperations(new BaseOperations() {
            @Override
            public Queue getQueue() {
                return queue;
            }
        });
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
        assertEquals(1, queue.getHistory().size());
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

    @Test(expected=ClientException.class)
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        ops.deleteResource("resourceId", user, false);
    }

    @Test
    @SuppressWarnings({ "serial" })
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        new ConsumerRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
            }
        }.deleteResource(consumerKey.toString(), user, true);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "serial" })
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        new ConsumerRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.deleteResource(consumerKey.toString(), user, true);
    }

    @Test
    public void testDelegateDeletionResourceI() throws DataSourceException, ClientException {
        //
        // Consumer without Demands
        //
        final Long consumerKey = 12345L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public void deleteConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> demandKeys = new ArrayList<Long>();
                return demandKeys;
            }
        });

        ops.delegateResourceDeletion(new MockPersistenceManager(), consumerKey);
    }

    /******* dd
    @Test
    @SuppressWarnings("serial")
    public void testDelegateDeletionResourceII() throws DataSourceException, ClientException {
        //
        // Consumer with Demands, themselves without Proposals
        //
        final Long consumerKey = 12345L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public void deleteConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
            }
        });
        final Long demandKey1 = 2222L;
        final Long demandKey2 = 3333L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> demandKeys = new ArrayList<Long>();
                demandKeys.add(demandKey1);
                demandKeys.add(demandKey2);
                return demandKeys;
            }
        });
        ConsumerRestlet.demandRestlet = new DemandRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long dKey, Long cKey, boolean stopRecursion) throws DataSourceException{
                assertTrue(dKey == demandKey1 || dKey == demandKey2);
                assertEquals(consumerKey, cKey);
                assertEquals(false, stopRecursion);
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), consumerKey);
    }
    ddd ********/
}
