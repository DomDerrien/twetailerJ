package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import twetailer.dao.ConsumerOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Consumer;
import twetailer.dto.SaleAssociate;
import twetailer.j2ee.LoginServlet;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestSaleAssociateRestlet {

    static final String OPEN_ID = "http://unit.test";
    static final Long CONSUMER_KEY = 12345L;

    static final OpenIdUser user = OpenIdUser.populate(
            "http://www.yahoo.com",
            YadisDiscovery.IDENTIFIER_SELECT,
            LoginServlet.YAHOO_OPENID_SERVER_URL
    );

    SaleAssociateRestlet ops;

    @BeforeClass
    public static void setUpBeforeClass() {
        SaleAssociateRestlet.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
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

        ops = new SaleAssociateRestlet();
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

    @Test(expected=ClientException.class)
    public void testCreateResourceI() throws DataSourceException, ClientException {
        ops.createResource(null, user);
    }

    @Test(expected=ClientException.class)
    public void testCreateResourceII() throws DataSourceException, ClientException {
        user.setAttribute("info", null);
        ops.createResource(null, user);
    }

    @Test(expected=ClientException.class)
    @SuppressWarnings("unchecked")
    public void testCreateResourceIII() throws DataSourceException, ClientException {
        ((Map<String, String>) user.getAttribute("info")).put("email", "unit@test");
        ops.createResource(null, user);
    }

    @Test
    @SuppressWarnings({ "unchecked", "serial" })
    public void testCreateResourceIV() throws DataSourceException, ClientException {
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        new SaleAssociateRestlet() {
            @Override
            protected JsonObject delegateResourceCreation(PersistenceManager pm, JsonObject parameters) {
                return new GenericJsonObject();
            }
        }.createResource(null, user);
    }

    @Test
    @SuppressWarnings({ "unchecked", "serial" })
    public void testCreateResourceV() throws DataSourceException, ClientException {
        ((Map<String, String>) user.getAttribute("info")).put("email", "steven.milstein@gmail.com");
        new SaleAssociateRestlet() {
            @Override
            protected JsonObject delegateResourceCreation(PersistenceManager pm, JsonObject parameters) {
                return new GenericJsonObject();
            }
        }.createResource(null, user);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDelegageResourceCreation0() throws DataSourceException, ClientException {
        //
        // Missing storeKey
        //
        ops.delegateResourceCreation(new MockPersistenceManager(), new GenericJsonObject());
    }

    @Test
    public void testDelegageResourceCreationI() throws DataSourceException, ClientException {
        //
        // Everything must be created
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertNull(consumer.getKey());
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationII() throws DataSourceException, ClientException {
        //
        // One consumerKey for a consumer not yet sale associate
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationIII() throws DataSourceException, ClientException {
        //
        // One consumerKey for a consumer already sale associate for another store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(1L);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationIV() throws DataSourceException, ClientException {
        //
        // One consumerKey for a consumer already sale associate for this store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationV() throws DataSourceException, ClientException {
        //
        // One email for no consumer and no sale associate
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertNull(consumer.getKey());
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationVI() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for another store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertNull(consumer.getKey());
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(1L);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationVII() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for this store but not attached to a Consumer matching the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.EMAIL.equals(key)) {
                    assertEquals(email, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(SaleAssociate.CONSUMER_KEY.equals(key) ? consumerKey : 1L);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationVIII() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for this store but not matching the one attached to the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.EMAIL.equals(key)) {
                    assertEquals(email, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(SaleAssociate.CONSUMER_KEY.equals(key) ? saleAssociateKey : 1L);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationIX() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for this store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                assertEquals(storeKey, saleAssociate.getStoreKey());
                assertEquals(email, saleAssociate.getEmail());
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationX() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for this store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.EMAIL.equals(key)) {
                    assertEquals(email, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                assertEquals(storeKey, saleAssociate.getStoreKey());
                assertEquals(email, saleAssociate.getEmail());
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXI() throws DataSourceException, ClientException {
        //
        // One email for one consumer not matching the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(1L);
                consumer.setEmail(email);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationXII() throws DataSourceException, ClientException {
        //
        // One email for one consumer not yet associate
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setEmail(email);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.EMAIL.equals(key)) {
                    assertEquals(email, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXIII() throws DataSourceException, ClientException {
        //
        // One email for one consumer already associate with another store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                // consumer.setKey(1L);
                consumer.setEmail(email);
                // consumer.setTwitterId(twitterId);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(1L);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXIV() throws DataSourceException, ClientException {
        //
        // One email for one consumer already associate but with another than the one associate with the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                // consumer.setKey(1L);
                consumer.setEmail(email);
                // consumer.setTwitterId(twitterId);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            boolean getSaleAssociatesCalled = false;
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(getSaleAssociatesCalled ? saleAssociateKey : 1L);
                getSaleAssociatesCalled = true;
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    // @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXV() throws DataSourceException, ClientException {
        //
        // One email for one consumer already associate correctly
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setEmail(email);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                assertEquals(storeKey, saleAssociate.getStoreKey());
                assertEquals(email, saleAssociate.getEmail());
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationXVI() throws DataSourceException, ClientException {
        //
        // One email for one consumer already associate correctly, as previously retrieved
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setEmail(email);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                assertEquals(storeKey, saleAssociate.getStoreKey());
                assertEquals(email, saleAssociate.getEmail());
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationXVII() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer and no sale associate
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertNull(consumer.getKey());
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXVIII() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for another store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertNull(consumer.getKey());
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(1L);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXIX() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for this store but not attached to a Consumer matching the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.TWITTER_ID.equals(key)) {
                    assertEquals(twitterId, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(SaleAssociate.CONSUMER_KEY.equals(key) ? consumerKey : 1L);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXX() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for this store but not matching the one attached to the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.TWITTER_ID.equals(key)) {
                    assertEquals(twitterId, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(SaleAssociate.CONSUMER_KEY.equals(key) ? saleAssociateKey : 1L);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationXXI() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for this store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                assertEquals(storeKey, saleAssociate.getStoreKey());
                assertEquals(twitterId, saleAssociate.getTwitterId());
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationXXII() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for this store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.TWITTER_ID.equals(key)) {
                    assertEquals(twitterId, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                assertEquals(storeKey, saleAssociate.getStoreKey());
                assertEquals(twitterId, saleAssociate.getTwitterId());
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXIII() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer not matching the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(1L);
                consumer.setTwitterId(twitterId);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationXXIV() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer not yet associate
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setTwitterId(twitterId);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.TWITTER_ID.equals(key)) {
                    assertEquals(twitterId, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXV() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer already associate with another store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                // consumer.setKey(1L);
                consumer.setTwitterId(twitterId);
                // consumer.setTwitterId(twitterId);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(1L);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXVI() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer already associate but with another than the one associate with the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                // consumer.setKey(1L);
                consumer.setTwitterId(twitterId);
                // consumer.setTwitterId(twitterId);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            boolean getSaleAssociatesCalled = false;
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(getSaleAssociatesCalled ? saleAssociateKey : 1L);
                getSaleAssociatesCalled = true;
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    // @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXVII() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer already associate correctly
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setTwitterId(twitterId);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                assertEquals(storeKey, saleAssociate.getStoreKey());
                assertEquals(twitterId, saleAssociate.getTwitterId());
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    // @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXVIII() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer already associate correctly, as previously retrieved
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        ops.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setTwitterId(twitterId);
                consumers.add(consumer);
                return consumers;
            }
        };
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                assertEquals(storeKey, saleAssociate.getStoreKey());
                assertEquals(twitterId, saleAssociate.getTwitterId());
                return saleAssociate;
            }
        };

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException {
        ops.deleteResource("resourceId", user);
    }

    @Test(expected=RuntimeException.class)
    public void testGetResource() throws DataSourceException {
        ops.getResource(null, "resourceId", user);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResourcesI() throws DataSourceException {
        ops.selectResources(new GenericJsonObject());
    }

    @Test
    public void testSelectResourcesII() throws DataSourceException {
        final Long storeKey = 12345L;
        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        ops.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                assertEquals(100, limit);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
        };
        ops.selectResources(data);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user);
    }
}
