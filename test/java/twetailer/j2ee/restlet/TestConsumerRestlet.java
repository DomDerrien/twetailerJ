package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
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
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.j2ee.LoginServlet;
import twetailer.j2ee.MaezelServlet;
import twetailer.j2ee.TestBaseRestlet;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.labs.taskqueue.MockQueue;
import com.google.appengine.api.labs.taskqueue.Queue;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestConsumerRestlet {

    ConsumerRestlet ops;
    OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
        ConsumerRestlet.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        ops = new ConsumerRestlet();
        user = TestBaseRestlet.setupOpenIdUser();
    }

    @After
    public void tearDown() throws Exception {
        ConsumerRestlet.demandRestlet = new DemandRestlet();

        ConsumerRestlet._baseOperations = new BaseOperations();
        ConsumerRestlet.consumerOperations = ConsumerRestlet._baseOperations.getConsumerOperations();
        ConsumerRestlet.demandOperations = ConsumerRestlet._baseOperations.getDemandOperations();
        ConsumerRestlet.proposalOperations = ConsumerRestlet._baseOperations.getProposalOperations();
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

    @Test
    public void testGetResourceI() throws DataSourceException, ClientException {
        final Long resourceId = 12345L;
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
    public void testGetResourceII() throws DataSourceException, ClientException {
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(Long key) {
                assertEquals(TestBaseRestlet.LOGGED_USER_CONSUMER_KEY, key);
                Consumer temp = new Consumer();
                temp.setKey(TestBaseRestlet.LOGGED_USER_CONSUMER_KEY);
                return temp;
            }
        };
        JsonObject resource = ops.getResource(null, "current", user);
        assertEquals(TestBaseRestlet.LOGGED_USER_CONSUMER_KEY.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testDelegateResourcesSelectionI() throws DataSourceException {
        final String email = "d.d@d.dom";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.EMAIL, email);
        final Long resourceId = 12345L;
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
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
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
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
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        JsonArray resources = ops.delegateResourceSelection(new MockPersistenceManager(), parameters);
        assertEquals(1, resources.size());
        assertEquals(resourceId.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testDelegateResourcesSelectionIV() throws DataSourceException {
        JsonObject parameters = new GenericJsonObject();
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                fail("Call not expected!");
                return null;
            }
        };
        JsonArray resources = ops.delegateResourceSelection(new MockPersistenceManager(), parameters);
        assertEquals(0, resources.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSelectResourcesI() throws DataSourceException, ClientException {
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        JsonObject parameters = new GenericJsonObject();
        JsonArray resources = ops.selectResources(parameters, user);
        assertEquals(0, resources.size());
    }

    @Test(expected=ClientException.class)
    public void testSelectResourcesII() throws DataSourceException, ClientException {
        JsonObject parameters = new GenericJsonObject();
        ops.selectResources(parameters, user);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "serial", "unchecked" })
    public void testSelectResourcesIII() throws DataSourceException, ClientException {
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        JsonObject parameters = new GenericJsonObject();
        new ConsumerRestlet() {
            @Override
            protected JsonArray delegateResourceSelection(PersistenceManager pm, JsonObject parameters) throws DataSourceException {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.selectResources(parameters, user);
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
        parameters.put(topic + "Code", MaezelServlet.getCode(topic, value, openId)); // valid code

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
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIIa() throws DataSourceException, ClientException {
        final String email = "unit@test.ca";
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(email, (String) value);
                return new ArrayList<Consumer>();
            }
        };
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIIb() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String email = "unit@test.ca";
        final Consumer consumer = new Consumer();
        consumer.setEmail(email);
        consumer.setKey(consumerKey);
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        ConsumerRestlet.demandOperations = new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                return new ArrayList<Long>();
            }
        };
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIIIa() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String email = "unit@test.ca";
        final Consumer consumer = new Consumer();
        consumer.setEmail(email);
        consumer.setKey(consumerKey);
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        ConsumerRestlet.demandOperations = new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                return new ArrayList<Long>();
            }
        };
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, consumerKey);
    }

    @Test
    public void testScheduleConsolidationTasksIIIb() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String jabberId = "unit@test.ca";
        final Consumer consumer = new Consumer();
        consumer.setJabberId(jabberId);
        consumer.setKey(consumerKey);
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        ConsumerRestlet.demandOperations = new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                return new ArrayList<Long>();
            }
        };
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.JABBER_ID, jabberId, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIIIc() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String twitterId = "unit_test_ca";
        final Consumer consumer = new Consumer();
        consumer.setTwitterId(twitterId);
        consumer.setKey(consumerKey);
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        ConsumerRestlet.demandOperations = new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                return new ArrayList<Long>();
            }
        };
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.TWITTER_ID, twitterId, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIV() throws DataSourceException, ClientException {
        final Long consumerKey = 67890L;
        final String email = "unit@test.ca";
        final Consumer consumer = new Consumer();
        consumer.setEmail(email);
        consumer.setKey(consumerKey);
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        final Long demandKey = 12345L;;
        ConsumerRestlet.demandOperations = new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int index) {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> keys = new ArrayList<Long>();
                keys.add(demandKey);
                return keys;
            }
        };
        final MockQueue queue = new MockQueue();
        ConsumerRestlet._baseOperations = new BaseOperations() {
            @Override
            public Queue getQueue() {
                return queue;
            }
        };
        ConsumerRestlet.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
        assertEquals(1, queue.getHistory().size());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testUpdateResourceI() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, consumerKey);
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ops.updateResource(null, "0", user);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testUpdateResourceII() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                return consumer;
            }
        };
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ops.updateResource(new GenericJsonObject(), consumerKey.toString(), user);
    }

    @Test
    public void testUpdateResourceIII() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        ConsumerRestlet._baseOperations = new MockBaseOperations();
        ops.updateResource(new GenericJsonObject(), consumerKey.toString(), user);
    }

    @Test
    public void testUpdateResourceIVa() throws DataSourceException, ClientException {
        final String email = "unit@test.ca";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.EMAIL, email);
        parameters.put(Consumer.EMAIL + "Code", MaezelServlet.getCode(Consumer.EMAIL, email, user.getClaimedId()));
        final Long consumerKey = 12345L;
        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
       };
       ConsumerRestlet._baseOperations = new MockBaseOperations();
       ops.updateResource(parameters, consumerKey.toString(), user);
    }

    @Test
    public void testUpdateResourceIVb() throws DataSourceException, ClientException {
        final String jabberId = "unit@test.ca";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.JABBER_ID, jabberId);
        parameters.put(Consumer.JABBER_ID + "Code", MaezelServlet.getCode(Consumer.JABBER_ID, jabberId, user.getClaimedId()));
        final Long consumerKey = 12345L;
        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
       };
       ConsumerRestlet._baseOperations = new MockBaseOperations();
       ops.updateResource(parameters, consumerKey.toString(), user);
    }

    @Test
    public void testUpdateResourceIVc() throws DataSourceException, ClientException {
        final String twitterId = "unit_test_ca";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Consumer.TWITTER_ID, twitterId);
        parameters.put(Consumer.TWITTER_ID + "Code", MaezelServlet.getCode(Consumer.TWITTER_ID, twitterId, user.getClaimedId()));
        final Long consumerKey = 12345L;
        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, consumerKey);
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setOpenID(user.getClaimedId());
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
       };
       ConsumerRestlet._baseOperations = new MockBaseOperations();
       ops.updateResource(parameters, consumerKey.toString(), user);
    }

    @Test(expected=ClientException.class)
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        ops.deleteResource("resourceId", user);
    }

    @Test
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        new ConsumerRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
            }
        }.deleteResource(consumerKey.toString(), user);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "unchecked", "serial" })
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        new ConsumerRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.deleteResource(consumerKey.toString(), user);
    }

    @Test
    public void testDelegateDeletionResourceI() throws DataSourceException, ClientException {
        //
        // Consumer without Demands
        //
        final Long consumerKey = 12345L;
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        ConsumerRestlet.demandOperations = new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> demandKeys = new ArrayList<Long>();
                return demandKeys;
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), consumerKey);
    }

    @Test
    @SuppressWarnings("serial")
    public void testDelegateDeletionResourceII() throws DataSourceException, ClientException {
        //
        // Consumer with Demands, themselves without Proposals
        //
        final Long consumerKey = 12345L;
        ConsumerRestlet.consumerOperations = new ConsumerOperations() {
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
        };
        final Long demandKey1 = 2222L;
        final Long demandKey2 = 3333L;
        ConsumerRestlet.demandOperations = new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Demand.OWNER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> demandKeys = new ArrayList<Long>();
                demandKeys.add(demandKey1);
                demandKeys.add(demandKey2);
                return demandKeys;
            }
        };
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
}
