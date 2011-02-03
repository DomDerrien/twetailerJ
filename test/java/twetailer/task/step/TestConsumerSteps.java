package twetailer.task.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.taskqueue.MockQueue;
import com.google.appengine.api.taskqueue.Queue;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.j2ee.MaelzelServlet;
import twetailer.task.step.ConsumerSteps;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestConsumerSteps {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorI() {
        new ConsumerSteps();
    }

    @Test
    public void testFilterOutInvalidValueI() throws DataSourceException {
        String topic = "zzz";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        // No attribute
        // No validation code
        Consumer consumer = new Consumer();
        consumer.setOpenID(openId);

        String attribute = ConsumerSteps.filterOutInvalidValue(parameters, topic, consumer, false);

        assertNull(attribute);
    }

    @Test
    public void testFilterOutInvalidValueIIa() throws DataSourceException {
        String topic = "zzz";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        parameters.put(topic, "yyy");
        // No validation code
        Consumer consumer = new Consumer();
        consumer.setOpenID(openId);

        String attribute = ConsumerSteps.filterOutInvalidValue(parameters, topic, consumer, false);

        assertNull(attribute);
    }

    @Test
    public void testFilterOutInvalidValueIIb() throws DataSourceException {
        String topic = "zzz";
        String openId = "http://open.id";
        JsonObject parameters = new GenericJsonObject();
        // No attribute
        parameters.put(topic + "Code", 0L); // Value not important
        Consumer consumer = new Consumer();
        consumer.setOpenID(openId);

        String attribute = ConsumerSteps.filterOutInvalidValue(parameters, topic, consumer, false);

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
        Consumer consumer = new Consumer();
        consumer.setOpenID(openId);

        String attribute = ConsumerSteps.filterOutInvalidValue(parameters, topic, consumer, false);

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
        Consumer consumer = new Consumer();
        consumer.setOpenID(openId);

        String attribute = ConsumerSteps.filterOutInvalidValue(parameters, topic, consumer, false);

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
        Consumer consumer = new Consumer();
        consumer.setOpenID(openId);

        String attribute = ConsumerSteps.filterOutInvalidValue(parameters, topic, consumer, false);

        assertEquals(value, attribute);
    }

    @Test
    public void testScheduleConsolidationTasksIa() throws DataSourceException, ClientException {
        ConsumerSteps.scheduleConsolidationTasks("<don't care>", null, 0L);
    }

    @Test
    public void testScheduleConsolidationTasksIb() throws DataSourceException, ClientException {
        ConsumerSteps.scheduleConsolidationTasks("<don't care>", "", 0L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testScheduleConsolidationTasksIc() throws DataSourceException, ClientException {
        ConsumerSteps.scheduleConsolidationTasks("<don't care>", "zzz", 0L);
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
        ConsumerSteps.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
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
        ConsumerSteps.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
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
        ConsumerSteps.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
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
        ConsumerSteps.scheduleConsolidationTasks(Consumer.EMAIL, email, consumerKey);
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
        ConsumerSteps.scheduleConsolidationTasks(Consumer.JABBER_ID, jabberId, 0L);
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
        ConsumerSteps.scheduleConsolidationTasks(Consumer.TWITTER_ID, twitterId, 0L);
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
        ConsumerSteps.scheduleConsolidationTasks(Consumer.EMAIL, email, 0L);
        assertEquals(1, queue.getHistory().size());
    }
}
