package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twitter4j.MockTwitterUser;
import twitter4j.TwitterException;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;

public class TestConsumerOperations {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testGetsI() throws DataSourceException {
        final String qA = "a";
        final String qV = "b";
        final PersistenceManager pm = new MockPersistenceManager() {
            @Override
            @SuppressWarnings({ "unchecked", "serial" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery() {
                    @Override
                    public void setFilter(String pattern) {
                        assertEquals(qA + " == attributeValue", pattern);
                    }
                    @Override
                    public void setOrdering(String pattern) {
                        assertEquals("creationDate desc", pattern);
                    }
                    @Override
                    public void declareParameters(String pattern) {
                        assertEquals("String attributeValue", pattern);
                    }
                    @Override
                    public List<Object> execute(Object value) {
                        assertEquals(qV, value);
                        return new ArrayList<Object>();
                    }
                };
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        List<Consumer> consumers = ops.getConsumers(qA, qV, 0);
        assertNotNull(consumers);
        assertEquals(0, consumers.size());
        assertTrue(ops.getPersistenceManager().isClosed());
    }

    @Test
    public void testGetsII() throws DataSourceException {
        final String qA = "a";
        final String qV = "b";
        final PersistenceManager pm = new MockPersistenceManager() {
            @SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
                        // Whatever the query is, no Consumer instance match
                        return new ArrayList<Object>();
                    }
                };
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        List<Consumer> consumers = ops.getConsumers(qA, qV, 0);
        assertNotNull(consumers);
        assertEquals(0, consumers.size());
        assertTrue(ops.getPersistenceManager().isClosed());
    }

    @Test
    public void testGetsIII() throws DataSourceException {
        final String qA = "a";
        final String qV = "b";
        final PersistenceManager pm = new MockPersistenceManager() {
            @SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
                        // Whatever the query is, no Consumer instance match
                        return new ArrayList<Object>();
                    }
                };
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        List<Consumer> consumers = ops.getConsumers(qA, qV, 0);
        assertNotNull(consumers);
        assertEquals(0, consumers.size());
        assertTrue(ops.getPersistenceManager().isClosed());
    }

    @Test
    public void testGetsIV() throws DataSourceException {
        final String qA = "a";
        final String qV = "b";
        final Consumer selected = new Consumer();
        final PersistenceManager pm = new MockPersistenceManager() {
            @SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
                        // Whatever the query is, one Consumer instance match
                        List<Object> selection = new ArrayList<Object>();
                        selection.add(selected);
                        return selection;
                    }
                };
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        List<Consumer> consumers = ops.getConsumers(qA, qV, 0);
        assertNotNull(consumers);
        assertEquals(1, consumers.size());
        assertEquals(selected, consumers.get(0));
        assertTrue(ops.getPersistenceManager().isClosed());
    }

    @Test
    public void testGetsV() throws DataSourceException {
        final String qA = "a";
        final String qV = "b";
        final Consumer selected = new Consumer();
        final Consumer spare1 = new Consumer();
        final Consumer spare2 = new Consumer();
        final Consumer spare3 = new Consumer();
        final Consumer spare4 = new Consumer();
        final PersistenceManager pm = new MockPersistenceManager() {
            @SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
                        // Whatever the query is, one Consumer instance match
                        List<Object> selection = new ArrayList<Object>();
                        selection.add(selected);
                        selection.add(spare1);
                        selection.add(spare2);
                        selection.add(spare3);
                        selection.add(spare4);
                        return selection;
                    }
                };
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        List<Consumer> consumers = ops.getConsumers(qA, qV, 0);
        assertNotNull(consumers);
        assertEquals(5, consumers.size());
        assertEquals(selected, consumers.get(0));
        assertEquals(spare1, consumers.get(1));
        assertEquals(spare2, consumers.get(2));
        assertEquals(spare3, consumers.get(3));
        assertEquals(spare4, consumers.get(4));
        assertTrue(ops.getPersistenceManager().isClosed());
    }

    @Test
    public void testGetI() throws DataSourceException {
        final long key = 1234L;
        final PersistenceManager pm = new MockPersistenceManager() {
            @Override
            public <T> T getObjectById(Class<T> arg0, Object arg1) {
                assertEquals(key, ((Long) arg1).longValue());
                return null;
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        try {
            ops.getConsumer(key);
        }
        finally {
            assertTrue(ops.getPersistenceManager().isClosed());
        }
    }

    @Test(expected=DataSourceException.class)
    public void testGetII() throws DataSourceException {
        final long key = 1234L;
        final PersistenceManager pm = new MockPersistenceManager() {
            @Override
            public <T> T getObjectById(Class<T> arg0, Object arg1) {
                assertEquals(key, ((Long) arg1).longValue());
                throw new IllegalArgumentException("done in purpose");
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        try {
            ops.getConsumer(key);
        }
        finally {
            assertTrue(ops.getPersistenceManager().isClosed());
        }
    }

    @Test
    public void testGetIV() throws DataSourceException {
        final long key = 1234L;
        final Consumer selected = new Consumer();
        final PersistenceManager pm = new MockPersistenceManager() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getObjectById(Class<T> arg0, Object arg1) {
                assertEquals(key, ((Long) arg1).longValue());
                return (T) selected;
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        Consumer consumer = ops.getConsumer(key);
        assertNotNull(consumer);
        assertEquals(selected, consumer);
        assertTrue(ops.getPersistenceManager().isClosed());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetV() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations();
        PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ops.getConsumer(pm, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetVI() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations();
        PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ops.getConsumer(pm, 0L);
    }

    @Test
    public void testCreateI() throws DataSourceException {
        final MockPersistenceManager pm = new MockPersistenceManager() {
            @SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
                        // Whatever the query is, no Consumer instance match
                        return new ArrayList<Object>();
                    }
                };
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        Consumer createdConsumer = ops.createConsumer(new com.google.appengine.api.users.User("email", "domain"));
        assertNotNull(createdConsumer);
        assertEquals(createdConsumer, ((MockPersistenceManager) ops.getPersistenceManager()).getPersistedObject());
        assertEquals("email", createdConsumer.getEmail());
        assertEquals("email", createdConsumer.getName());
        assertTrue(ops.getPersistenceManager().isClosed());
    }

    @Test
    public void testCreateII() throws DataSourceException {
        final Consumer existingConsumer = new Consumer();
        final MockPersistenceManager pm = new MockPersistenceManager() {
            @SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
                        List<Object> selection = new ArrayList<Object>();
                        selection.add(existingConsumer);
                        return selection;
                    }
                };
            }
        };
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        Consumer createdConsumer = ops.createConsumer(new com.google.appengine.api.users.User("email", "domain"));
        assertNotNull(createdConsumer);
        assertNull(((MockPersistenceManager) ops.getPersistenceManager()).getPersistedObject());
        assertEquals(existingConsumer, createdConsumer);
        assertTrue(ops.getPersistenceManager().isClosed());
    }

    @Test
    public void testCreateIII() throws DataSourceException {
        com.google.appengine.api.users.User user = new com.google.appengine.api.users.User("test", "test.com");

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return mockAppEngineEnvironment.getPersistenceManager();
            }
        };

        // Verify there's no instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Create the user once
        ops.createConsumer(user);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Tries to recreate it
        ops.createConsumer(user);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("serial")
    public void testCreateIV() throws DataSourceException, TwitterException {
        final String name = "test";
        MockTwitterUser user = new MockTwitterUser() {
            @Override
            public String getName() {
                return name;
            }
        };

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return mockAppEngineEnvironment.getPersistenceManager();
            }
        };

        // Verify there's no instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Create the user once
        Consumer consumer = ops.createConsumer(user);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(name, consumer.getTwitterId());

        // Tries to recreate it
        ops.createConsumer(user);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    public void testCreateV() throws DataSourceException {
        final String jabberId = "unit@test.net";
        com.google.appengine.api.xmpp.JID user = new com.google.appengine.api.xmpp.JID(jabberId);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return mockAppEngineEnvironment.getPersistenceManager();
            }
        };

        // Verify there's no instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Create the user once
        Consumer consumer = ops.createConsumer(user);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(jabberId, consumer.getJabberId());

        // Tries to recreate it
        ops.createConsumer(user);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    public void testCreateVI() throws DataSourceException, UnsupportedEncodingException {
        final String email = "unit@test.net";
        final String name = "Mr Unit Test";
        javax.mail.internet.InternetAddress address = new javax.mail.internet.InternetAddress(email, name);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return mockAppEngineEnvironment.getPersistenceManager();
            }
        };

        // Verify there's no instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Create the user once
        Consumer consumer = ops.createConsumer(address);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(email, consumer.getEmail());
        assertEquals(name, consumer.getName());

        // Tries to recreate it
        ops.createConsumer(address);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    public void testUpdateI() throws DataSourceException {
        final String twitterId = "Katelyn";
        PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ConsumerOperations ops = new ConsumerOperations();

        // Create the user once
        Consumer consumer = ops.createConsumer(pm, new com.google.appengine.api.users.User("test", "domain"));

        // Update it
        consumer.setTwitterId(twitterId);

        // Persist the update
        consumer = ops.updateConsumer(pm, consumer);

        // Close the persistence manager and open a new one for a separate query
        pm.close();
        pm = mockAppEngineEnvironment.getPersistenceManager();

        // Search for the update instance
        List<Consumer> consumers = ops.getConsumers("email", "test", 1);
        assertNotNull(consumers.size());
        assertEquals(1, consumers.size());
        assertEquals(twitterId, consumers.get(0).getTwitterId());
    }

    @Test
    public void testUpdateII() throws DataSourceException {

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        // Create the user once
        Consumer consumer = ops.createConsumer(pm, new com.google.appengine.api.users.User("test", "domain"));

        // Update it
        consumer.setTwitterId("Katelyn");

        // Persist the update
        consumer = ops.updateConsumer(consumer); // This function will close the PersistenceManager instance

        assertTrue(pm.isClosed());
    }

    /* After the upgrade to the Java SDK 1.2.5, the limitation of having to use the same PM even for detached object has been removed
    @Test(expected=javax.jdo.JDOFatalUserException.class)
    public void testUpdateIII() throws DataSourceException {

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return mockAppEngineEnvironment.getPersistenceManager();
            }
        };
        // Create the user once
        Consumer consumer = ops.createConsumer(new User("test", "domain"));

        // Update it
        consumer.setTwitterId(12345L);

        // Persist the update
        consumer = ops.updateConsumer(consumer); // This is going to throw the JDOFatalUserExcepion because the update should be done with the same PersistenceManager instance
    }
    */

    @Test
    public void testGetSimplifiedJabberIdI() {
        String base = "d.d@d.dom";
        String extension = "";
        String jabberId = base + extension;
        assertEquals(base, ConsumerOperations.getSimplifiedJabberId(jabberId));
    }

    @Test
    public void testGetSimplifiedJabberIdII() {
        String base = "d.d@d.dom";
        String extension = "/pidgin3343243";
        String jabberId = base + extension;
        assertEquals(base, ConsumerOperations.getSimplifiedJabberId(jabberId));
    }
}
