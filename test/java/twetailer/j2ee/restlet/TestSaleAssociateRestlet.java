package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestSaleAssociateRestlet {

    SaleAssociateRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new SaleAssociateRestlet();
        user = MockLoginServlet.buildMockOpenIdAssociate();
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetLogger() throws DataSourceException, ClientException {
        assertNotNull(ops.getLogger());
    }

    @Test
    public void testGetResourceI() throws DataSourceException, ClientException {
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, key);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(null, "current", user, false);
        assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testGetResourceII() throws DataSourceException, ClientException {
        ops.getResource(null, "current", MockLoginServlet.setAsNotAnAssociate(MockLoginServlet.buildMockOpenIdUser()), false); // Just a consumer, not an associate
    }

    @Test
    public void testGetResourceIII() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 4444444444L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(saleAssociateKey, key);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(null, saleAssociateKey.toString(), user, true);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testGetResourceIV() throws DataSourceException, ClientException {
        ops.getResource(null, "1234567890", user, false);
    }

    @Test
    public void testSelectResourceI() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 4444444444L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(saleAssociateKey);
                return Arrays.asList(new SaleAssociate[] { resource });
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, saleAssociateKey);

        JsonArray resources = ops.selectResources(params, user, true);
        assertEquals(saleAssociateKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourceII() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 4444444444L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                return Arrays.asList(new Long[] { saleAssociateKey });
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, saleAssociateKey);
        params.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, Boolean.TRUE);

        JsonArray resources = ops.selectResources(params, user, true);
        assertEquals(saleAssociateKey.longValue(), resources.getLong(0));
    }

    @Test
    public void testSelectResourceIII() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 4444444444L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, key);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.TRUE);
                return resource;
            }
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(saleAssociateKey);
                return Arrays.asList(new SaleAssociate[] { resource });
            }
        });

        JsonArray resources = ops.selectResources(new GenericJsonObject(), user, false);
        assertEquals(saleAssociateKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testSelectResourceIV() throws DataSourceException, ClientException {
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, key);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.FALSE);
                return resource;
            }
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, Map<String, Object> params, int limit) throws DataSourceException {
                fail("Unexpected call");
                return null;
            }
        });

        ops.selectResources(new GenericJsonObject(), user, false);
    }

    @Test
    public void testCreateResourceI() throws DataSourceException, ClientException {
        final Long consumerKey = 434343434343L;
        final Long saleAssociateKey = 4444444444L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key) || consumerKey.equals(key));
                Consumer resource = new Consumer();
                resource.setKey(key);
                if (MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key)) {
                    resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                }
                return resource;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) throws DataSourceException {
                assertEquals(saleAssociateKey, consumer.getSaleAssociateKey());
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject params) {
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(saleAssociateKey);
                return resource;
            }
        });
        final Long locationKey = 86236541221L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_STORE_KEY, key);
                Store resource = new Store();
                resource.setKey(key);
                resource.setLocationKey(locationKey);
                return resource;
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, saleAssociateKey);
        params.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        params.put(SaleAssociate.STORE_KEY, MockLoginServlet.DEFAULT_STORE_KEY);

        JsonObject resource = ops.createResource(params, user, true);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testCreateResourceII() throws DataSourceException, ClientException {
        final Long consumerKey = 434343434343L;
        final Long saleAssociateKey = 4444444444L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key) || consumerKey.equals(key));
                Consumer resource = new Consumer();
                resource.setKey(key);
                if (MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key)) {
                    resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                }
                return resource;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) throws DataSourceException {
                assertEquals(saleAssociateKey, consumer.getSaleAssociateKey());
                return consumer;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.equals(key));
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.TRUE);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject params) {
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(saleAssociateKey);
                return resource;
            }
        });
        final Long locationKey = 86236541221L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_STORE_KEY, key);
                Store resource = new Store();
                resource.setKey(key);
                resource.setLocationKey(locationKey);
                return resource;
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY, saleAssociateKey);
        params.put(SaleAssociate.CONSUMER_KEY, consumerKey);
        params.put(SaleAssociate.STORE_KEY, MockLoginServlet.DEFAULT_STORE_KEY);

        JsonObject resource = ops.createResource(params, user, false);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testCreateResourceIII() throws DataSourceException, ClientException {
        ops.createResource(new GenericJsonObject(), user, true);
    }

    @Test(expected=ReservedOperationException.class)
    public void testCreateResourceIV() throws DataSourceException, ClientException {
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.equals(key));
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.FALSE);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
        });
        ops.createResource(new GenericJsonObject(), user, false);
    }

    @Test(expected=ReservedOperationException.class)
    public void testUpdateResourceI() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key));
                Consumer resource = new Consumer();
                resource.setKey(key);
                // resource.setSaleAssociateKey(null);
                return resource;
            }
        });
        ops.updateResource(new GenericJsonObject(), "current", MockLoginServlet.buildMockOpenIdUser(), false);
    }

    @Test
    public void testUpdateResourceII() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key));
                Consumer resource = new Consumer();
                resource.setKey(key);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, key);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.FALSE);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) throws DataSourceException {
                return saleAssociate;
            }
        });

        JsonObject resource = ops.updateResource(new GenericJsonObject(), "current", user, false);
        assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testUpdateResourceIII() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key));
                Consumer resource = new Consumer();
                resource.setKey(key);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, key);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.FALSE);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) throws DataSourceException {
                return saleAssociate;
            }
        });

        JsonObject resource = ops.updateResource(new GenericJsonObject(), MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.toString(), user, true);
        assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testUpdateResourceIV() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key));
                Consumer resource = new Consumer();
                resource.setKey(key);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, key);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.FALSE);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
        });

        final Long saleAssociateKey = 453645876L;
        JsonObject resource = ops.updateResource(new GenericJsonObject(), saleAssociateKey.toString(), user, false);
        assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testUpdateResourceV() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key));
                Consumer resource = new Consumer();
                resource.setKey(key);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        final Long saleAssociateKey = 453645876L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.equals(key) || saleAssociateKey.equals(key));
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.TRUE);
                if (MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.equals(key)) {
                    resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                }
                else {
                    resource.setStoreKey(2 * MockLoginServlet.DEFAULT_STORE_KEY);
                }
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) throws DataSourceException {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                return saleAssociate;
            }
        });

        JsonObject resource = ops.updateResource(new GenericJsonObject(), saleAssociateKey.toString(), user, false);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testUpdateResourceVI() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_CONSUMER_KEY.equals(key));
                Consumer resource = new Consumer();
                resource.setKey(key);
                resource.setSaleAssociateKey(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY);
                return resource;
            }
        });
        final Long saleAssociateKey = 453645876L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY.equals(key) || saleAssociateKey.equals(key));
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(key);
                resource.setIsStoreAdmin(Boolean.TRUE);
                resource.setStoreKey(MockLoginServlet.DEFAULT_STORE_KEY);
                resource.setConsumerKey(MockLoginServlet.DEFAULT_CONSUMER_KEY);
                return resource;
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) throws DataSourceException {
                assertEquals(saleAssociateKey, saleAssociate.getKey());
                return saleAssociate;
            }
        });

        JsonObject resource = ops.updateResource(new GenericJsonObject(), saleAssociateKey.toString(), user, false);
        assertEquals(saleAssociateKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        ops.deleteResource("12345", user, false);
    }

    @Test
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long saleAssociateKey = 453645876L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public void deleteSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(saleAssociateKey, key);
            }
        });
        ops.deleteResource(saleAssociateKey.toString(), user, true);
    }
}
