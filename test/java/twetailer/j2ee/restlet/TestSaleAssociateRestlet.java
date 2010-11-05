package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Consumer;
import twetailer.dto.SaleAssociate;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

public class TestSaleAssociateRestlet {

    SaleAssociateRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new SaleAssociateRestlet();
        user = MockLoginServlet.buildMockOpenIdUser();
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected=ReservedOperationException.class)
    public void testCreateResourceI() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 45354L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer consumer = new Consumer();
                consumer.setKey(key);
                consumer.setSaleAssociateKey(saleAssociateKey);
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                return new SaleAssociate();
            }
        });
        ops.createResource(null, user, false);
    }

    /***** ddd
    @Test(expected=ClientException.class)
    public void testCreateResourceII() throws DataSourceException, ClientException {
        user.setAttribute("info", null);
        ops.createResource(null, user, false);

    @Test(expected=ClientException.class)
    @SuppressWarnings("unchecked")
    public void testCreateResourceIII() throws DataSourceException, ClientException {
        ((Map<String, String>) user.getAttribute("info")).put("email", "unit@test");
        ops.createResource(null, user, false);
    }

    @Test
    @SuppressWarnings({ "serial" })
    public void testCreateResourceIV() throws DataSourceException, ClientException {
        JsonObject parameters = new GenericJsonObject();
        new SaleAssociateRestlet() {
            @Override
            protected JsonObject delegateResourceCreation(PersistenceManager pm, JsonObject parameters) {
                return new GenericJsonObject();
            }
        }.createResource(parameters, user, true);
        assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY.longValue(), parameters.getLong(SaleAssociate.CREATOR_KEY));
    }

    @Test
    @SuppressWarnings({ "unchecked", "serial" })
    public void testCreateResourceV() throws DataSourceException, ClientException {
        JsonObject parameters = new GenericJsonObject();
        ((Map<String, String>) user.getAttribute("info")).put("email", "steven.milstein@gmail.com");
        new SaleAssociateRestlet() {
            @Override
            protected JsonObject delegateResourceCreation(PersistenceManager pm, JsonObject parameters) {
                return new GenericJsonObject();
            }
        }.createResource(parameters, user, false);
        assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY.longValue(), parameters.getLong(SaleAssociate.CREATOR_KEY));
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "serial" })
    public void testCreateResourceVI() throws DataSourceException, ClientException {
        new SaleAssociateRestlet() {
            @Override
            protected JsonObject delegateResourceCreation(PersistenceManager pm, JsonObject parameters) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.createResource(null, user, true);
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
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertNull(consumer.getKey());
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
                assertEquals(consumerKey.longValue(), parameters.getLong(SaleAssociate.CONSUMER_KEY));
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationII() throws DataSourceException, ClientException {
        //
        // One consumerKey for a consumer not yet sale associate
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationIII() throws DataSourceException, ClientException {
        //
        // One consumerKey for a consumer already sale associate for another store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationIV() throws DataSourceException, ClientException {
        //
        // One consumerKey for a consumer already sale associate for this store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationV() throws DataSourceException, ClientException {
        //
        // One email for no consumer and no sale associate
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationVI() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for another store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationVII() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for this store but not attached to a Consumer matching the given consumerKey
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationVIII() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for this store but not matching the one attached to the given consumerKey
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationIX() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for this store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.EMAIL, key);
                assertEquals(email, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationX() throws DataSourceException, ClientException {
        //
        // One email for no consumer but one sale associate for this store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXI() throws DataSourceException, ClientException {
        //
        // One email for one consumer not matching the given consumerKey
        //
        final Long storeKey = 11111L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationXII() throws DataSourceException, ClientException {
        //
        // One email for one consumer not yet associate
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXIII() throws DataSourceException, ClientException {
        //
        // One email for one consumer already associate with another store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXIV() throws DataSourceException, ClientException {
        //
        // One email for one consumer already associate but with another than the one associate with the given consumerKey
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    // @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXV() throws DataSourceException, ClientException {
        //
        // One email for one consumer already associate correctly
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationXVI() throws DataSourceException, ClientException {
        //
        // One email for one consumer already associate correctly, as previously retrieved
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String email = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.EMAIL, email);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationXVII() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer and no sale associate
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXVIII() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for another store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXIX() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for this store but not attached to a Consumer matching the given consumerKey
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXX() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for this store but not matching the one attached to the given consumerKey
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationXXI() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for this store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationXXII() throws DataSourceException, ClientException {
        //
        // One twitterId for no consumer but one sale associate for this store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXIII() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer not matching the given consumerKey
        //
        final Long storeKey = 11111L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationXXIV() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer not yet associate
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXV() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer already associate with another store
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXVI() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer already associate but with another than the one associate with the given consumerKey
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    // @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXVII() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer already associate correctly
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    // @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXVIII() throws DataSourceException, ClientException {
        //
        // One twitterId for one consumer already associate correctly, as previously retrieved
        //
        final Long storeKey = 11111L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String twitterId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.TWITTER_ID, twitterId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
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
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=ClientException.class)
    public void testDeleteResourceForNonAuthorized() throws DataSourceException, ClientException {
        ops.deleteResource("resourceId", user, false);
    }

    @Test
    @SuppressWarnings({ "serial" })
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 11111L;
        new SaleAssociateRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
            }
        }.deleteResource(saleAssociateKey.toString(), user, true);
    }

    @Test(expected=RuntimeException.class)
    @SuppressWarnings({ "serial" })
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 11111L;
        new SaleAssociateRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.deleteResource(saleAssociateKey.toString(), user, true);
    }

    @Test
    @SuppressWarnings("serial")
    public void testDelegateDeletionResourceI() throws DataSourceException, ClientException {
        //
        // SaleAssociate without Proposals
        //
        final Long saleAssociateKey = 11111L;
        final Long consumerKey = 2222L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
            @Override
            public void deleteSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
            }
        };
        SaleAssociateRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public List<Long> getProposalKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Proposal.OWNER_KEY, key);
                assertEquals(saleAssociateKey, (Long) value);
                List<Long> proposals = new ArrayList<Long>();
                return proposals;
            }
        };
        SaleAssociateRestlet.consumerRestlet = new ConsumerRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) throws DataSourceException{
                assertEquals(consumerKey, key);
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), saleAssociateKey);
    }

    @Test
    @SuppressWarnings("serial")
    public void testDelegateDeletionResourceII() throws DataSourceException, ClientException {
        //
        // SaleAssociate with Proposals
        //
        final Long saleAssociateKey = 11111L;
        final Long consumerKey = 22222L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
            @Override
            public void deleteSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
            }
        };
        final Long proposalKey = 33333L;
        SaleAssociateRestlet.proposalOperations = new ProposalOperations() {
            @Override
            public List<Long> getProposalKeys(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Proposal.OWNER_KEY, key);
                assertEquals(saleAssociateKey, (Long) value);
                List<Long> proposals = new ArrayList<Long>();
                proposals.add(proposalKey);
                return proposals;
            }
        };
        SaleAssociateRestlet.proposalRestlet = new ProposalRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key, SaleAssociate owner, boolean stopRecursion) throws DataSourceException{
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, owner.getKey());
            }
        };
        SaleAssociateRestlet.consumerRestlet = new ConsumerRestlet() {
            @Override
            protected void delegateResourceDeletion(PersistenceManager pm, Long key) throws DataSourceException{
                assertEquals(consumerKey, key);
            }
        };

        ops.delegateResourceDeletion(new MockPersistenceManager(), saleAssociateKey);
    }

    @Ignore
    @Test(expected=RuntimeException.class)
    public void testGetResource() throws DataSourceException, ClientException {
        ops.getResource(null, "resourceId", user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResourcesI() throws DataSourceException {
        ops.delegateResourceSelection(new MockPersistenceManager(), new GenericJsonObject());
    }

    @Test
    public void testSelectResourcesII() throws DataSourceException {
        final Long storeKey = 12345L;
        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                assertEquals(100, limit);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
        };
        ops.delegateResourceSelection(new MockPersistenceManager(), data);
    }

    @Test(expected=ClientException.class)
    public void testSelectResourcesIII() throws DataSourceException, ClientException {
        ops.selectResources(new GenericJsonObject(), user);
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResourcesIV() throws DataSourceException, ClientException {
        JsonObject parameters = new GenericJsonObject(); // STORE_KEY is missing
        ops.selectResources(parameters, user, true);
    }

    @Test
    public void testSelectResourcesV() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(SaleAssociate.STORE_KEY, saleAssociateKey);

        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(saleAssociateKey, (Long) value);
                return new ArrayList<SaleAssociate>();
            }
        };

        JsonArray resources = ops.selectResources(parameters, user, true);
        assertEquals(0, resources.size());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException, ClientException {
        ops.updateResource(new GenericJsonObject(), "resourceId", user, false);
    }

    @Test
    public void testDelegageResourceCreationXXX() throws DataSourceException, ClientException {
        //
        // One jabberId for no consumer and no sale associate
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertNull(consumer.getKey());
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXXI() throws DataSourceException, ClientException {
        //
        // One jabberId for no consumer but one sale associate for another store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
            @Override
            public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
                assertNull(consumer.getKey());
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXXII() throws DataSourceException, ClientException {
        //
        // One jabberId for no consumer but one sale associate for this store but not attached to a Consumer matching the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.JABBER_ID.equals(key)) {
                    assertEquals(jabberId, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(SaleAssociate.CONSUMER_KEY.equals(key) ? consumerKey : 1L);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXXIII() throws DataSourceException, ClientException {
        //
        // One jabberId for no consumer but one sale associate for this store but not matching the one attached to the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.JABBER_ID.equals(key)) {
                    assertEquals(jabberId, (String) value);
                }
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(SaleAssociate.CONSUMER_KEY.equals(key) ? saleAssociateKey : 1L);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationXXXIV() throws DataSourceException, ClientException {
        //
        // One jabberId for no consumer but one sale associate for this store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
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
                assertEquals(jabberId, saleAssociate.getJabberId());
                return saleAssociate;
            }
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    public void testDelegageResourceCreationXXXV() throws DataSourceException, ClientException {
        //
        // One jabberId for no consumer but one sale associate for this store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.JABBER_ID.equals(key)) {
                    assertEquals(jabberId, (String) value);
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
                assertEquals(jabberId, saleAssociate.getJabberId());
                return saleAssociate;
            }
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXXVI() throws DataSourceException, ClientException {
        //
        // One jabberId for one consumer not matching the given consumerKey
        //
        final Long storeKey = 12345L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(1L);
                consumer.setJabberId(jabberId);
                consumers.add(consumer);
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    public void testDelegageResourceCreationXXXVII() throws DataSourceException, ClientException {
        //
        // One jabberId for one consumer not yet associate
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setJabberId(jabberId);
                consumers.add(consumer);
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) {
                    assertEquals(consumerKey, (Long) value);
                }
                if (SaleAssociate.JABBER_ID.equals(key)) {
                    assertEquals(jabberId, (String) value);
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
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXXVIII() throws DataSourceException, ClientException {
        //
        // One jabberId for one consumer already associate with another store
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                // consumer.setKey(1L);
                consumer.setJabberId(jabberId);
                // consumer.setjabberId(jabberId);
                consumers.add(consumer);
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXXXIX() throws DataSourceException, ClientException {
        //
        // One jabberId for one consumer already associate but with another than the one associate with the given consumerKey
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                // consumer.setKey(1L);
                consumer.setJabberId(jabberId);
                // consumer.setjabberId(jabberId);
                consumers.add(consumer);
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
        });

        ops.delegateResourceCreation(new MockPersistenceManager(), data);
    }

    @Test
    // @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXC() throws DataSourceException, ClientException {
        //
        // One jabberId for one consumer already associate correctly
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setJabberId(jabberId);
                consumers.add(consumer);
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
                assertEquals(jabberId, saleAssociate.getJabberId());
                return saleAssociate;
            }
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }

    @Test
    // @Test(expected=DataSourceException.class)
    public void testDelegageResourceCreationXCI() throws DataSourceException, ClientException {
        //
        // One jabberId for one consumer already associate correctly, as previously retrieved
        //
        final Long storeKey = 12345L;
        final Long saleAssociateKey = 54321L;

        final Long consumerKey = 67890L;
        final String jabberId = "unit@test";

        JsonObject data = new GenericJsonObject();
        data.put(SaleAssociate.STORE_KEY, storeKey);
        data.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        data.put(SaleAssociate.JABBER_ID, jabberId);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.JABBER_ID, key);
                assertEquals(jabberId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setJabberId(jabberId);
                consumers.add(consumer);
                return consumers;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
                assertEquals(jabberId, saleAssociate.getJabberId());
                return saleAssociate;
            }
        });

        JsonObject resource = ops.delegateResourceCreation(new MockPersistenceManager(), data);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(SaleAssociate.KEY));
    }
    ddd ***/
}
