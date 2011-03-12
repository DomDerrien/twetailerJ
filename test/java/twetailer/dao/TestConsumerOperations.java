package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManager;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.MockQuery;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.FacebookConnector;
import twetailer.dto.Consumer;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
import twitter4j.MockUser;
import twitter4j.TwitterException;

import com.dyuproject.openid.YadisDiscovery;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestConsumerOperations {

    private static LocalServiceTestHelper helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        BaseSteps.resetOperationControllers(false); // Use helper!
        CacheHandler.injectMockCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @Test(expected = RuntimeException.class)
    public void testGetsWithFailureI() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String attribute, Object value, int limit) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.getConsumers("key", "value", 12345);
    }

    @Test
    public void testGetsI() throws DataSourceException {
        final String qA = "a";
        final String qV = "b";
        final PersistenceManager pm = new MockPersistenceManager() {
            @Override
            @SuppressWarnings( { "serial", "rawtypes" })
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
            @SuppressWarnings( { "serial", "rawtypes" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery() {
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
            @SuppressWarnings( { "serial", "rawtypes" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery() {
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
            @SuppressWarnings( { "serial", "rawtypes" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery() {
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
            @SuppressWarnings( { "serial", "rawtypes" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery() {
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
    public void testGetI() throws InvalidIdentifierException {
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

    @Test(expected = InvalidIdentifierException.class)
    public void testGetII() throws InvalidIdentifierException {
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
    public void testGetIV() throws InvalidIdentifierException {
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

    @Test(expected = InvalidIdentifierException.class)
    public void testGetV() throws InvalidIdentifierException {
        ConsumerOperations ops = new ConsumerOperations();
        ops.getConsumer(new MockPersistenceManager(), null);
    }

    @Test(expected = InvalidIdentifierException.class)
    public void testGetVI() throws InvalidIdentifierException {
        ConsumerOperations ops = new ConsumerOperations();
        ops.getConsumer(new MockPersistenceManager(), 0L);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateWithFailureI() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, com.google.appengine.api.users.User loggedUser) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createConsumer(new com.google.appengine.api.users.User("email", "domain"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateWithFailureII() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, com.google.appengine.api.xmpp.JID jabberId) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createConsumer(new com.google.appengine.api.xmpp.JID("jabberId"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateWithFailureIII() throws DataSourceException, TwitterException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, twitter4j.User twitterUser) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createConsumer(new twitter4j.MockUser());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateWithFailureIV() throws DataSourceException, UnsupportedEncodingException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, javax.mail.internet.InternetAddress senderAddress, boolean isVerified) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createConsumer(new javax.mail.internet.InternetAddress("unit@test.ca", "name"), true);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateWithFailureV() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, com.dyuproject.openid.OpenIdUser user) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createConsumer(new com.dyuproject.openid.OpenIdUser());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateWithFailureVI() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer user) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createConsumer(new Consumer());
    }

    @Test
    public void testCreateWithWorkedAroundFailure() throws DataSourceException, TwitterException,
            UnsupportedEncodingException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit)
                    throws DataSourceException {
                throw new DataSourceException("Done in purpoe");
            }

            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer user) {
                return null; // Not important
            }
        };
        ops.createConsumer(new com.google.appengine.api.users.User("email", "domain"));
        ops.createConsumer(new com.google.appengine.api.xmpp.JID("jabberId"));
        ops.createConsumer(new twitter4j.MockUser());
        ops.createConsumer(new javax.mail.internet.InternetAddress("unit@test.ca", "name"), true);
        ops.createConsumer(new com.dyuproject.openid.OpenIdUser());
        ops.createConsumer(new Consumer());
    }

    @Test
    public void testCreateI() throws DataSourceException {
        final MockPersistenceManager pm = new MockPersistenceManager() {
            @SuppressWarnings( { "serial", "rawtypes" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery() {
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
            @SuppressWarnings( { "serial", "rawtypes" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery() {
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
    @SuppressWarnings("deprecation")
    public void testCreateIII() throws DataSourceException {
        com.google.appengine.api.users.User user = new com.google.appengine.api.users.User("test", "test.com");

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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
    @SuppressWarnings("deprecation")
    public void testCreateIVa() throws DataSourceException, TwitterException {
        final String name = "displayName";
        final String screenName = "screenName";
        MockUser user = new MockUser(0, screenName, name);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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

        assertEquals(screenName, consumer.getTwitterId());
        assertEquals(name, consumer.getName());

        // Tries to recreate it
        ops.createConsumer(user);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateIVb() throws DataSourceException, TwitterException {
        final String screenName = "screenName";
        MockUser user = new MockUser(0, screenName, null);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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

        assertEquals(screenName, consumer.getTwitterId());
        assertEquals(screenName, consumer.getName());

        // Tries to recreate it
        ops.createConsumer(user);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateV() throws DataSourceException {
        final String jabberId = "unit@test.net";
        com.google.appengine.api.xmpp.JID user = new com.google.appengine.api.xmpp.JID(jabberId);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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
    @SuppressWarnings("deprecation")
    public void testCreateVIa() throws DataSourceException, UnsupportedEncodingException {
        final String email = "unit@test.net";
        final String name = "Mr Unit Test";
        javax.mail.internet.InternetAddress address = new javax.mail.internet.InternetAddress(email, name);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
        };

        // Verify there's no instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Create the user once
        Consumer consumer = ops.createConsumer(address, true);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(email, consumer.getEmail());
        assertEquals(name, consumer.getName());

        // Tries to recreate it
        ops.createConsumer(address, true);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateVIb() throws DataSourceException, UnsupportedEncodingException {
        final String email = "unit@test.net";
        javax.mail.internet.InternetAddress address = new javax.mail.internet.InternetAddress(email, null);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
        };

        // Verify there's no instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Create the user once
        Consumer consumer = ops.createConsumer(address, true);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(email, consumer.getEmail());
        assertEquals(email, consumer.getName());

        // Tries to recreate it
        ops.createConsumer(address, true);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateVII() throws DataSourceException, UnsupportedEncodingException {
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        final String openId = "unit@test";
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        user.fromJSON(json);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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

        assertEquals(openId, consumer.getOpenID());

        // Tries to recreate it
        ops.createConsumer(user);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateVIII() throws DataSourceException, UnsupportedEncodingException {
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        final String openId = "unit@test";
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", new HashMap<String, String>());
        json.put("g", attributes);
        user.fromJSON(json);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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

        assertEquals(openId, consumer.getOpenID());

        // Tries to recreate it
        ops.createConsumer(user);

        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateIX() throws DataSourceException, UnsupportedEncodingException {
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        final String openId = "unit@test";
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        Map<String, String> info = new HashMap<String, String>();
        info.put("language", "FR");
        info.put("firstname", "unit");
        info.put("lastname", "test");
        info.put("email", openId);
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", info);
        json.put("g", attributes);
        user.fromJSON(json);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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

        assertEquals(openId, consumer.getOpenID());
        assertEquals(Locale.FRENCH.getLanguage(), consumer.getLanguage());
        assertEquals(LabelExtractor.get("display_name_pattern", new Object[] { "unit", "test" }, Locale.ENGLISH),
                consumer.getName());
        assertEquals(openId, consumer.getEmail());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateX() throws DataSourceException, UnsupportedEncodingException {
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        final String openId = "unit@test";
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        Map<String, String> info = new HashMap<String, String>();
        info.put("nickname", "unit_test");
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", info);
        json.put("g", attributes);
        user.fromJSON(json);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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

        assertEquals(openId, consumer.getOpenID());
        assertEquals("unit_test", consumer.getName());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXI() throws DataSourceException, UnsupportedEncodingException {
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        final String openId = "unit@test";
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        Map<String, String> info = new HashMap<String, String>();
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", info);
        json.put("g", attributes);
        user.fromJSON(json);

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
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

        assertEquals(openId, consumer.getOpenID());
        assertEquals(openId, consumer.getName());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXII() throws DataSourceException, UnsupportedEncodingException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public Consumer updateConsumer(PersistenceManager  pm, Consumer consumer) {
                return consumer;
            }
        };

        // Create the user once
        final String openId = "unit@test";
        Consumer consumer = new Consumer();
        consumer.setEmail(openId);
        consumer = ops.createConsumer(consumer);

        // Verify there's one instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Creates the data for a user identified by its OpenID
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        Map<String, String> info = new HashMap<String, String>();
        info.put("email", openId);
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", info);
        json.put("g", attributes);
        user.fromJSON(json);

        // Create the second user
        Consumer secondConsumer = ops.createConsumer(user);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(consumer.getKey(), secondConsumer.getKey());
        assertEquals(openId, secondConsumer.getOpenID());
        assertEquals(openId, secondConsumer.getEmail());
        assertEquals(openId, secondConsumer.getName());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXIII() throws DataSourceException, UnsupportedEncodingException {
        ConsumerOperations ops = new ConsumerOperations();

        // Create the user once
        final String openId = "unit@test";
        Consumer consumer = new Consumer();
        consumer.setEmail(openId);
        consumer = ops.createConsumer(consumer);

        // Verify there's one instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Creates the data for a user identified by its OpenID
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        Map<String, String> info = new HashMap<String, String>();
        info.put("email", openId);
        info.put("nickname", "marcelus");
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", info);
        json.put("g", attributes);
        user.fromJSON(json);

        // Create the second user
        Consumer secondConsumer = ops.createConsumer(user);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(consumer.getKey(), secondConsumer.getKey());
        assertEquals(openId, secondConsumer.getOpenID());
        assertEquals(openId, secondConsumer.getEmail());
        assertEquals("marcelus", secondConsumer.getName());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXIV() throws DataSourceException, UnsupportedEncodingException {
        ConsumerOperations ops = new ConsumerOperations();

        // Create the user once
        final String openId = "unit@test";
        Consumer consumer = new Consumer();
        consumer.setEmail(openId);
        consumer.setName("initial name");
        consumer = ops.createConsumer(consumer);

        // Verify there's one instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Creates the data for a user identified by its OpenID
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        Map<String, String> info = new HashMap<String, String>();
        info.put("email", openId);
        info.put("nickname", "marcelus");
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", info);
        json.put("g", attributes);
        user.fromJSON(json);

        // Create the second user
        Consumer secondConsumer = ops.createConsumer(user);

        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(consumer.getKey(), secondConsumer.getKey());
        assertEquals(openId, secondConsumer.getOpenID());
        assertEquals(openId, secondConsumer.getEmail());
        assertEquals("initial name", secondConsumer.getName());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXV() throws DataSourceException, UnsupportedEncodingException {
        final String openId = "unit@test";
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }

            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit)
                    throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };

        // Create the user once
        Consumer consumer = new Consumer();
        consumer.setEmail(openId);
        consumer.setName("unit test");
        consumer = ops.createConsumer(consumer);

        // Verify there's one instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Creates the data for a user identified by its OpenID
        com.dyuproject.openid.OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate("http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT, LoginServlet.YAHOO_OPENID_SERVER_URL);
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate",
        // g: attributes, h: "identifier"}
        json.put("a", openId);
        Map<String, String> info = new HashMap<String, String>();
        info.put("email", openId);
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", info);
        json.put("g", attributes);
        user.fromJSON(json);

        // Create the second user
        ops.createConsumer(user);

        // Verify there's 2 instances because the attempts to get the first one failed...
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(2, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer user) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.updateConsumer(new Consumer());
    }

    @Test
    public void testUpdateI() throws DataSourceException {
        final String twitterId = "Katelyn";
        PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ConsumerOperations ops = new ConsumerOperations();

        // Create the user once
        Consumer consumer = ops.createConsumer(pm, new com.google.appengine.api.users.User("test", "domain"));

        // Update it
        consumer.setTwitterId(twitterId);

        // Persist the update
        consumer = ops.updateConsumer(pm, consumer);

        // Close the persistence manager and open a new one for a separate query
        pm.close();
        pm = new MockPersistenceManagerFactory().getPersistenceManager();

        // Search for the update instance
        List<Consumer> consumers = ops.getConsumers("email", "test", 1);
        assertNotNull(consumers.size());
        assertEquals(1, consumers.size());
        assertEquals(twitterId, consumers.get(0).getTwitterId());

        pm.close();
    }

    @Test
    public void testUpdateII() throws DataSourceException {

        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
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

    @Test(expected = RuntimeException.class)
    public void testDeleteWithFailureI() throws InvalidIdentifierException, DataSourceException {
        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public void deleteConsumer(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.deleteConsumer(12345L);
    }

    @Test
    public void testDeleteI() throws InvalidIdentifierException, DataSourceException {
        final Long consumerKey = 54657L;
        ConsumerOperations ops = new ConsumerOperations() {
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
        ops.deleteConsumer(consumerKey);
    }

    @Test
    public void testDeleteII() throws InvalidIdentifierException, DataSourceException {
        final String name = "name";
        Consumer toBeCreated = new Consumer();
        toBeCreated.setName(name);
        Consumer justCreated = new ConsumerOperations().createConsumer(toBeCreated);
        assertNotNull(justCreated.getKey());
        assertEquals(name, justCreated.getName());
        new ConsumerOperations().deleteConsumer(justCreated.getKey());
    }

    @Test
    public void testGetsFromKeysI() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations();

        Consumer object = new Consumer();
        object = ops.createConsumer(object);

        List<Long> parameters = new ArrayList<Long>();
        parameters.add(object.getKey());

        List<Consumer> selection = ops.getConsumers(ops.getPersistenceManager(), parameters);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsFromKeysII() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations();

        List<Long> parameters = new ArrayList<Long>();
        parameters.add(12345L);

        List<Consumer> selection = ops.getConsumers(ops.getPersistenceManager(), parameters);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test
    public void testGetsFromMapI() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations();

        Consumer object = new Consumer();
        object.setEmail("d.d.@d.dom");
        object = ops.createConsumer(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Consumer.EMAIL, object.getEmail());

        List<Consumer> selection = ops.getConsumers(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsFromMapII() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Consumer.EMAIL, "unit@test.org");

        List<Consumer> selection = ops.getConsumers(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test
    public void testGetKeysFromMapI() throws DataSourceException {
        ConsumerOperations ops = new ConsumerOperations();

        Consumer object = new Consumer();
        object.setEmail("d.d.@d.dom");
        object = ops.createConsumer(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Consumer.EMAIL, object.getEmail());

        List<Long> selection = ops.getConsumerKeys(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXVI() throws DataSourceException, UnsupportedEncodingException {
        final Long consumerKey = 12345L;
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                List<Consumer> consumers = new ArrayList<Consumer>();
                if (Consumer.FACEBOOK_ID.equals(key)) {
                    Consumer consumer = new Consumer();
                    consumer.setKey(consumerKey);
                    consumer.setFacebookId(facebookId);
                    consumers.add(consumer);
                }
                return consumers;
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);
        assertEquals(consumerKey, consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());

        // No creation occurred as it has been serve by the injected mock
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXVII() throws DataSourceException, UnsupportedEncodingException {
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(Consumer.NAME, "name");
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);

        // Verify creation occurred
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Verify attributes
        assertNotNull(consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());
        assertEquals("name", consumer.getName());
        assertNull(consumer.getEmail());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXVIII() throws DataSourceException, UnsupportedEncodingException {
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(FacebookConnector.ATTR_EMAIL, "");
        consumerData.put(FacebookConnector.ATTR_NAME, "name");
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);

        // Verify creation occurred
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Verify attributes
        assertNotNull(consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());
        assertEquals("name", consumer.getName());
        assertNull(consumer.getEmail());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXIX() throws DataSourceException, UnsupportedEncodingException {
        final Long consumerKey = 12345L;
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                List<Consumer> consumers = new ArrayList<Consumer>();
                if (Consumer.EMAIL.equals(key)) {
                    Consumer consumer = new Consumer();
                    consumer.setKey(consumerKey);
                    consumer.setEmail("email");
                    consumer.setName("initial name");
                    consumers.add(consumer);
                }
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(facebookId, consumer.getFacebookId());
                assertEquals("initial name", consumer.getName());
                return consumer;
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(FacebookConnector.ATTR_EMAIL, "email");
        consumerData.put(FacebookConnector.ATTR_NAME, "name");
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);

        // Verify no update occurred
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Verify attributes
        assertNotNull(consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());
        assertEquals("initial name", consumer.getName());
        assertEquals("email", consumer.getEmail());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXX() throws DataSourceException, UnsupportedEncodingException {
        final Long consumerKey = 12345L;
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                List<Consumer> consumers = new ArrayList<Consumer>();
                if (Consumer.EMAIL.equals(key)) {
                    Consumer consumer = new Consumer();
                    consumer.setKey(consumerKey);
                    consumer.setEmail("email");
                    // No initial name
                    consumers.add(consumer);
                }
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(facebookId, consumer.getFacebookId());
                assertEquals("new name", consumer.getName());
                return consumer;
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(FacebookConnector.ATTR_EMAIL, "email");
        consumerData.put(FacebookConnector.ATTR_NAME, "new name");
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);

        // Verify no update occurred
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Verify attributes
        assertNotNull(consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());
        assertEquals("new name", consumer.getName());
        assertEquals("email", consumer.getEmail());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXXI() throws DataSourceException, UnsupportedEncodingException {
        final Long consumerKey = 12345L;
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                List<Consumer> consumers = new ArrayList<Consumer>();
                if (Consumer.EMAIL.equals(key)) {
                    Consumer consumer = new Consumer();
                    consumer.setKey(consumerKey);
                    consumer.setEmail("email");
                    // No initial name
                    consumers.add(consumer);
                }
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(facebookId, consumer.getFacebookId());
                assertEquals("email", consumer.getName());
                return consumer;
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(FacebookConnector.ATTR_EMAIL, "email");
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);

        // Verify no update occurred
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Verify attributes
        assertNotNull(consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());
        assertEquals("email", consumer.getName());
        assertEquals("email", consumer.getEmail());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXXII() throws DataSourceException, UnsupportedEncodingException {
        final Long consumerKey = 12345L;
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                List<Consumer> consumers = new ArrayList<Consumer>();
                if (Consumer.EMAIL.equals(key)) {
                    Consumer consumer = new Consumer();
                    consumer.setKey(consumerKey);
                    consumer.setEmail("email");
                    // No initial name
                    consumers.add(consumer);
                }
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(facebookId, consumer.getFacebookId());
                assertEquals("email", consumer.getName());
                return consumer;
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(FacebookConnector.ATTR_EMAIL, "email");
        consumerData.put(FacebookConnector.ATTR_NAME, "");
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);

        // Verify no update occurred
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Verify attributes
        assertNotNull(consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());
        assertEquals("email", consumer.getName());
        assertEquals("email", consumer.getEmail());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXXIII() throws DataSourceException, UnsupportedEncodingException {
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(FacebookConnector.ATTR_EMAIL, "email");
        consumerData.put(FacebookConnector.ATTR_NAME, "new name");
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);

        // Verify creation occurred
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Verify attributes
        assertNotNull(consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());
        assertEquals("new name", consumer.getName());
        assertEquals("email", consumer.getEmail());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCreateXXIV() throws DataSourceException, UnsupportedEncodingException {
        final String facebookId = "54657687";

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                throw new DataSourceException("done in purpose!");
            }
        };

        JsonObject consumerData = new GenericJsonObject();
        consumerData.put(FacebookConnector.ATTR_EMAIL, "email");
        consumerData.put(FacebookConnector.ATTR_NAME, "new name");
        consumerData.put(FacebookConnector.ATTR_UID, facebookId);

        // Create the user
        Consumer consumer = ops.createConsumer(consumerData);

        // Verify creation occurred
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Verify attributes
        assertNotNull(consumer.getKey());
        assertEquals(facebookId, consumer.getFacebookId());
        assertEquals("new name", consumer.getName());
        assertEquals("email", consumer.getEmail());
    }

    @Test(expected=DataSourceException.class)
    public void testUpdateTransientConsumer() throws DataSourceException {
        final Long consumerKey = 23456L;

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key, boolean useCache) throws InvalidIdentifierException {
                assertFalse(useCache);
                assertEquals(consumerKey, key);
                throw new InvalidIdentifierException("done in purpose!");
            }
        };

        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        ops.updateConsumer(null, consumer);
    }

    @Test
    public void testDeleteTransientConsumer() throws DataSourceException, InvalidIdentifierException {
        Consumer consumer = new Consumer();

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
        };

        consumer = ops.createConsumer(consumer);

        PersistenceManager pm = ops.getPersistenceManager();
        try {
            consumer = ops.getConsumer(pm, consumer.getKey(), false);
            ops.deleteConsumer(pm, consumer);
        }
        finally {
            pm.close();
        }
    }

    @Test
    public void testDeleteTransientConsumerII() throws DataSourceException, InvalidIdentifierException {
        Consumer consumer = new Consumer();

        ConsumerOperations ops = new ConsumerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return new MockPersistenceManagerFactory().getPersistenceManager();
            }
        };

        consumer = ops.createConsumer(consumer);

        PersistenceManager pm = ops.getPersistenceManager();
        try {
            ops.deleteConsumer(pm, consumer.getKey());
        }
        finally {
            pm.close();
        }
    }
}
