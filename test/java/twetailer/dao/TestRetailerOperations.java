package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.users.User;

import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.Retailer;

public class TestRetailerOperations {

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
        Logger log1 = new RetailerOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new RetailerOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
    }

    @Test
    public void testCreate() {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));

        Retailer item = new RetailerOperations().createRetailer(consumer, 111L);
        assertNotNull(item.getKey());
        assertEquals(consumer.getKey(), item.getConsumerKey());
        assertEquals(Long.valueOf(111L), item.getStoreKey());
    }

    @Test
    public void testGetI() throws DataSourceException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));

        RetailerOperations ops = new RetailerOperations();
        Retailer item = ops.createRetailer(consumer, 111L);
        
        Retailer selected = ops.getRetailer(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
    }

    @Test(expected=InvalidParameterException.class)
    public void testGetII() throws DataSourceException {
        new RetailerOperations().getRetailer(null);
    }

    @Test(expected=InvalidParameterException.class)
    public void testGetIII() throws DataSourceException {
        new RetailerOperations().getRetailer(0L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIV() throws DataSourceException {
        new RetailerOperations().getRetailer(888L);
    }

    @Test
    public void testGets() throws DataSourceException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));
        consumer.setTwitterId(12345L);

        RetailerOperations ops = new RetailerOperations();
        Retailer item = ops.createRetailer(consumer, 111L);
        
        List<Retailer> selection = ops.getRetailers(Retailer.TWITTER_ID, 12345L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(item.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testUpdate() throws DataSourceException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));
        consumer.setTwitterId(12345L);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        RetailerOperations ops = new RetailerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Retailer item = ops.createRetailer(pm, consumer, 111L); // Gives the PersistenceManager so it won't be closed
        
        item.setEmail("test@test.com");

        Retailer updated = ops.updateRetailer(item);
        assertNotNull(updated);
        assertEquals(item.getKey(), updated.getKey());
        assertEquals(item.getEmail(), updated.getEmail());
    }


    @Test
    public void testGetExtendedI() throws DataSourceException {
        Consumer consumer = new ConsumerOperations().createConsumer(new User("test", "domain"));

        RetailerOperations ops = new RetailerOperations();
        Retailer item = ops.createRetailer(consumer, 111L);

        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        
        Retailer selected = ops.getRetailer(pm, item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
        assertNull(selected.getCriteria()); // The empty List<String> has been nullified by the JPO by creation process

        selected.addCriterion("first");
        selected.addCriterion("second");
        assertNotNull(selected.getCriteria());
        assertNotSame(0, selected.getCriteria().size());

        Retailer updated = ops.updateRetailer(pm, selected);
        assertNotNull(updated.getCriteria());
        assertNotSame(0, updated.getCriteria().size());
        assertEquals("first", updated.getCriteria().get(0));
        assertEquals("second", updated.getCriteria().get(1));
        
        pm.close();
        
        selected = ops.getRetailer(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
        assertNotNull(selected.getCriteria());
        assertNotSame(0, selected.getCriteria().size());
        assertEquals("first", selected.getCriteria().get(0));
        assertEquals("second", selected.getCriteria().get(1));
    }
}
