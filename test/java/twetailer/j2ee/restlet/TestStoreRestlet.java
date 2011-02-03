package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javamocks.io.MockInputStream;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.State;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestStoreRestlet {

    StoreRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new StoreRestlet();
        user = MockLoginServlet.buildMockOpenIdUser();
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
    public void testGetResource() throws DataSourceException, ClientException {
        final Long storeKey = 6487643645L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store resource = new Store();
                resource.setKey(key);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(null, storeKey.toString(), user, false);
        assertEquals(storeKey.longValue(), resource.getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourceI() throws DataSourceException, ClientException {
        final Long locationKey = 6988594897869834934L;
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(key);
                return resource;
            }
        });
        final Long storeKey = 6487643645L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> params, List<Location> locations, int limit) {
                assertEquals(locationKey, locations.get(0).getKey());
                Store resource = new Store();
                resource.setKey(storeKey);
                resource.setLocationKey(locationKey);
                return Arrays.asList(new Store[] { resource });
            }
        });

        GenericJsonObject params = new GenericJsonObject();
        params.put(Location.LOCATION_KEY, locationKey);

        JsonArray resources = ops.selectResources(params, user, false);
        assertEquals(storeKey.longValue(), resources.getJsonObject(0).getLong(Entity.KEY));
    }

    @Test
    public void testSelectResourceII() throws DataSourceException, ClientException {
        final Long storeKey = 6487643645L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Long> getStoreKeys(PersistenceManager pm, Map<String, Object> params, int limit) {
                return Arrays.asList(new Long[] { storeKey });
            }
        });

        GenericJsonObject params = new GenericJsonObject();
        params.put(BaseRestlet.ONLY_KEYS_PARAMETER_KEY, Boolean.TRUE);

        JsonArray resources = ops.selectResources(params, user, false);
        assertEquals(storeKey.longValue(), resources.getLong(0));
    }

    @Test(expected=ReservedOperationException.class)
    public void testCreateResourceI() throws DataSourceException, ClientException {
        ops.createResource(null, user, false);
    }

    @Test
    public void testCreateResourceII() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        final Long locationKey = 23456L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store createStore(PersistenceManager pm, JsonObject store) {
                Store resource = new Store();
                resource.setKey(storeKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
            @Override
            public Store updateStore(PersistenceManager pm, Store store) {
                assertEquals(storeKey, store.getKey());
                assertEquals(45.5D, store.getLatitude(), 0.0);
                assertEquals(45.5D, store.getLongitude(), 0.0);
                return store;
            }
        });
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                resource.setHasStore(true);
                return resource;
            }
        });
        LocaleValidator.setMockValidatorStream(new MockInputStream("{'results':[{'geometry':{'location':{'lat':45.5,'lng':45.5}}}]}"));

        JsonObject params = new GenericJsonObject();
        params.put(Store.REGISTRAR_KEY, 0L);

        JsonObject response = ops.createResource(params, user, true);
        assertEquals(storeKey.longValue(), response.getLong(Store.KEY));
        assertEquals(45.5D, response.getDouble(Store.LATITUDE), 0.0);
        assertEquals(45.5D, response.getDouble(Store.LONGITUDE), 0.0);
    }

    @Test
    public void testUpdateResource() throws DataSourceException, ClientException {
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long consumerKey) {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, consumerKey);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                return resource;
            }
        });
        final Long storeKey = 12345L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long saleAssociateKey) {
                assertEquals(MockLoginServlet.DEFAULT_SALE_ASSOCIATE_KEY, saleAssociateKey);
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(saleAssociateKey);
                resource.setIsStoreAdmin(Boolean.TRUE);
                resource.setStoreKey(storeKey);
                return resource;
            }
        });
        final Long locationKey = 5464543L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store resource = new Store();
                resource.setKey(storeKey);
                resource.setLocationKey(locationKey);
                return resource;
            }
            @Override
            public Store updateStore(PersistenceManager pm, Store store) {
                assertEquals(storeKey, store.getKey());
                return store;
            }
        });

        JsonObject resource = ops.updateResource(new GenericJsonObject(), storeKey.toString(), MockLoginServlet.buildMockOpenIdAssociate(), false); // Act as an associate being a store admin
        assertEquals(storeKey.longValue(), resource.getLong(Store.KEY));
    }

    @Test(expected=ReservedOperationException.class)
    public void testDeleteResourceI() throws DataSourceException, ClientException {
        ops.deleteResource("12345", user, false);
    }

    @Test
    public void testDeleteResourceII() throws DataSourceException, ClientException {
        final Long storeKey = 12345L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store resource = new Store();
                resource.setKey(storeKey);
                return resource;
            }
            @Override
            public Store updateStore(PersistenceManager pm, Store store) {
                assertEquals(storeKey, store.getKey());
                assertEquals(State.markedForDeletion, store.getState());
                return store;
            }
        });

        ops.deleteResource(storeKey.toString(), user, true);
    }
}
