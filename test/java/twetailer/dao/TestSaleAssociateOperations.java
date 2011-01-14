package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Consumer;
import twetailer.dto.SaleAssociate;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;

import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestSaleAssociateOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        BaseSteps.resetOperationControllers(false); // Use helper!
        CacheHandler.injectCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        CacheHandler.injectCacheFactory(null);
        CacheHandler.injectCache(null);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureI() throws DataSourceException {
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, SaleAssociate item) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createSaleAssociate(new SaleAssociate());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureII() throws ClientException {
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, SaleAssociate item) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createSaleAssociate(new GenericJsonObject());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureIII() {
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, Consumer item, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createSaleAssociate(new Consumer(), 12345L);
    }

    @Test
    public void testCreateI() {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));
        SaleAssociate item = new SaleAssociateOperations().createSaleAssociate(consumer, 111L);

        assertNotNull(item.getKey());
        assertEquals(consumer.getKey(), item.getConsumerKey());
        assertEquals(Long.valueOf(111L), item.getStoreKey());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.createSaleAssociate(new Consumer(), 0L);
    }

    @Test
    public void testCreateIII() {
        SaleAssociate item = new SaleAssociate();
        item.setConsumerKey(111L);
        assertNull(item.getKey());

        item = new SaleAssociateOperations().createSaleAssociate(item);

        assertNotNull(item.getKey());
        assertEquals((Long) 111L, item.getConsumerKey());
    }

    @Test
    public void testCreateIV() {
        JsonObject parameters = new GenericJsonObject();
        parameters.put(SaleAssociate.CONSUMER_KEY, (Long) 111L);

        SaleAssociate item = new SaleAssociateOperations().createSaleAssociate(parameters);

        assertNotNull(item.getKey());
        assertEquals((Long) 111L, item.getConsumerKey());
    }

    @Test
    public void testGetI() throws InvalidIdentifierException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));

        SaleAssociateOperations ops = new SaleAssociateOperations();
        SaleAssociate item = ops.createSaleAssociate(consumer, 111L);

        SaleAssociate selected = ops.getSaleAssociate(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetII() throws InvalidIdentifierException {
        new SaleAssociateOperations().getSaleAssociate(null);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIII() throws InvalidIdentifierException {
        new SaleAssociateOperations().getSaleAssociate(0L);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIV() throws InvalidIdentifierException {
        new SaleAssociateOperations().getSaleAssociate(888L);
    }

    @Test
    public void testGetsI() throws ClientException, DataSourceException {
        SaleAssociateOperations ops = new SaleAssociateOperations();

        SaleAssociate object = new SaleAssociate();
        object.setConsumerKey(111L);
        object = ops.createSaleAssociate(object);

        List<SaleAssociate> objects = ops.getSaleAssociates(SaleAssociate.CONSUMER_KEY, object.getConsumerKey(), 1);
        assertNotSame(0, objects.size());
        assertEquals(object.getKey(), objects.get(0).getKey());
    }

    @Test(expected=RuntimeException.class)
    public void testGetsII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.getSaleAssociates("test", null, 0);
    }

    @Test
    public void testUpdateI() throws ClientException, DataSourceException {
        SaleAssociateOperations ops = new SaleAssociateOperations();
        SaleAssociate object = ops.createSaleAssociate(new SaleAssociate());
        assertNull(object.getConsumerKey());
        object.setConsumerKey(111L);
        SaleAssociate updated = ops.updateSaleAssociate(object);
        assertEquals(object.getKey(), updated.getKey());
        assertEquals(object.getConsumerKey(), updated.getConsumerKey());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.updateSaleAssociate(new SaleAssociate());
    }

    @Test
    public void testGetExtendedI() throws DataSourceException, InvalidIdentifierException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));

        SaleAssociateOperations ops = new SaleAssociateOperations();
        SaleAssociate item = ops.createSaleAssociate(consumer, 111L);

        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();

        SaleAssociate selected = ops.getSaleAssociate(pm, item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
        assertNotNull(selected.getCriteria()); // No more nullified by the JPO by creation process - appengine 1.2.8

        Collator collator = LocaleValidator.getCollator(Locale.ENGLISH);

        selected.addCriterion("first", collator);
        selected.addCriterion("second", collator);
        assertNotNull(selected.getCriteria());
        assertNotSame(0, selected.getCriteria().size());

        SaleAssociate updated = ops.updateSaleAssociate(pm, selected);
        assertNotNull(updated.getCriteria());
        assertNotSame(0, updated.getCriteria().size());
        assertEquals("first", updated.getCriteria().get(0));
        assertEquals("second", updated.getCriteria().get(1));

        pm.close();

        selected = ops.getSaleAssociate(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
        assertNotNull(selected.getCriteria());
        assertNotSame(0, selected.getCriteria().size());
        assertEquals("first", selected.getCriteria().get(0));
        assertEquals("second", selected.getCriteria().get(1));
    }

    @Test
    public void testGetKeysI() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        SaleAssociate object = new SaleAssociate();
        object.setConsumerKey(111L);
        object = ops.createSaleAssociate(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Long> selection = ops.getSaleAssociateKeys(pm, SaleAssociate.CONSUMER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetKeysII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        List<Long> selection = new SaleAssociateOperations().getSaleAssociateKeys(pm, SaleAssociate.CONSUMER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteWithFailureI() throws InvalidIdentifierException, DataSourceException {
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public void deleteSaleAssociate(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.deleteSaleAssociate(12345L);
    }

    @Test
    public void testDeleteI() throws InvalidIdentifierException, DataSourceException {
        final Long saleAssociateKey = 54657L;
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
            @Override
            public void deleteSaleAssociate(PersistenceManager pm, SaleAssociate item) {
                assertEquals(saleAssociateKey, item.getKey());
            }
        };
        ops.deleteSaleAssociate(saleAssociateKey);
    }

    @Test
    public void testGetsFromMapI() throws DataSourceException {
        SaleAssociateOperations ops = new SaleAssociateOperations();

        SaleAssociate object = new SaleAssociate();
        object.setConsumerKey(111L);
        object = ops.createSaleAssociate(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(SaleAssociate.CONSUMER_KEY, object.getConsumerKey());

        List<SaleAssociate> selection = ops.getSaleAssociates(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetKeysFromMapI() throws DataSourceException {
        SaleAssociateOperations ops = new SaleAssociateOperations();

        SaleAssociate object = new SaleAssociate();
        object.setConsumerKey(111L);
        object = ops.createSaleAssociate(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(SaleAssociate.CONSUMER_KEY, object.getConsumerKey());

        List<Long> selection = ops.getSaleAssociateKeys(ops.getPersistenceManager(), parameters, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetsFromKeysI() throws DataSourceException {
        SaleAssociateOperations ops = new SaleAssociateOperations();

        SaleAssociate object = new SaleAssociate();
        object.setConsumerKey(111L);
        object = ops.createSaleAssociate(object);

        List<Long> parameters = new ArrayList<Long>();
        parameters.add(object.getKey());

        List<SaleAssociate> selection = ops.getSaleAssociates(ops.getPersistenceManager(), parameters);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testDeleteII() throws ClientException, DataSourceException {
        SaleAssociateOperations ops = new SaleAssociateOperations();
        SaleAssociate object = ops.createSaleAssociate(new SaleAssociate());
        assertNull(object.getConsumerKey());
        object.setConsumerKey(111L);
        ops.deleteSaleAssociate(ops.getPersistenceManager(), object);
    }
}
