package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.SaleAssociate;

import com.google.appengine.api.users.User;

public class TestSaleAssociateOperations {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
        mockAppEngineEnvironment.setUp();

        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testGetLogger() throws IOException {
        Logger log1 = new SaleAssociateOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new SaleAssociateOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
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
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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
    public void testGetI() throws DataSourceException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));

        SaleAssociateOperations ops = new SaleAssociateOperations();
        SaleAssociate item = ops.createSaleAssociate(consumer, 111L);

        SaleAssociate selected = ops.getSaleAssociate(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetII() throws DataSourceException {
        new SaleAssociateOperations().getSaleAssociate(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIII() throws DataSourceException {
        new SaleAssociateOperations().getSaleAssociate(0L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIV() throws DataSourceException {
        new SaleAssociateOperations().getSaleAssociate(888L);
    }

    @Test
    public void testGetsI() throws DataSourceException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));
        consumer.setTwitterId("Ryan");

        SaleAssociateOperations ops = new SaleAssociateOperations();
        SaleAssociate item = ops.createSaleAssociate(consumer, 111L);

        List<SaleAssociate> selection = ops.getSaleAssociates(SaleAssociate.TWITTER_ID, "Ryan", 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(item.getKey(), selection.get(0).getKey());
    }

    @Test(expected=RuntimeException.class)
    public void testGetsII() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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
    public void testUpdateI() throws DataSourceException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));
        consumer.setTwitterId("Ryan");

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        SaleAssociateOperations ops = new SaleAssociateOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        SaleAssociate item = ops.createSaleAssociate(pm, consumer, 111L); // Gives the PersistenceManager so it won't be closed

        item.setEmail("test@test.com");

        SaleAssociate updated = ops.updateSaleAssociate(item);
        assertNotNull(updated);
        assertEquals(item.getKey(), updated.getKey());
        assertEquals(item.getEmail(), updated.getEmail());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
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
    public void testGetExtendedI() throws DataSourceException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));

        SaleAssociateOperations ops = new SaleAssociateOperations();
        SaleAssociate item = ops.createSaleAssociate(consumer, 111L);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();

        SaleAssociate selected = ops.getSaleAssociate(pm, item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
        assertNull(selected.getCriteria()); // The empty List<String> has been nullified by the JPO by creation process

        selected.addCriterion("first");
        selected.addCriterion("second");
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
}
