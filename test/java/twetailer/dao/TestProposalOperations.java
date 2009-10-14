package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Proposal;
import twetailer.dto.Retailer;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestProposalOperations {

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
        Logger log1 = new ProposalOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new ProposalOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal item = new Proposal();
        assertNull(item.getKey());

        item = ops.createProposal(item);
        assertNotNull(item.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testCreateIIa() throws ClientException {
        JsonObject item = new GenericJsonObject();
        // item.put(Proposal.CONSUMER_KEY, null);

        Retailer retailer = new Retailer();
        retailer.setKey(111L);
        retailer.setStoreKey(222L);

        Proposal object = new ProposalOperations().createProposal(item, retailer);
        assertNotNull(object.getKey());
        assertEquals(Long.valueOf(222L), object.getStoreKey());
    }

    @Test
    public void testCreateIIb() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Proposal.CONSUMER_KEY, 0L);

        Retailer retailer = new Retailer();
        retailer.setKey(111L);
        retailer.setStoreKey(222L);

        Proposal object = new ProposalOperations().createProposal(item, retailer);
        assertNotNull(object.getKey());
        assertEquals(Long.valueOf(222L), object.getStoreKey());
    }

    @Test
    public void testCreateIII() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Proposal.CONSUMER_KEY, 111L);

        Retailer retailer = new Retailer();
        retailer.setKey(111L);
        retailer.setStoreKey(222L);

        Proposal object = new ProposalOperations().createProposal(item, retailer);
        assertNotNull(object.getKey());
        assertEquals(Long.valueOf(222L), object.getStoreKey());
    }

    @Test(expected=ClientException.class)
    public void testCreateIV() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Proposal.CONSUMER_KEY, 333L);

        Retailer retailer = new Retailer();
        retailer.setKey(111L);
        retailer.setStoreKey(222L);

        new ProposalOperations().createProposal(item, retailer);
    }

    @Test
    public void testGetI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(pm, object); // Gives the PersistenceManager so it won't be closed

        Proposal selected = ops.getProposal(object.getKey(), 111L, 222L);
        assertNotNull(selected.getKey());
        assertEquals(object.getKey(), selected.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=DataSourceException.class)
    public void testGetIIa() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 333L, null);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIIb() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 333L, 0L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIIIa() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), null, 444L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIIIb() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 0L, 444L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIVa() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(null, 111L, 222L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIVb() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(0L, 111L, 222L);
    }

    @Test
    public void testGetVa() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), null, null);
    }

    @Test
    public void testGetVb() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 0L, 0L);
    }

    @Test
    public void testGetVI() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 333L, 444L);
    }

    @Test
    public void testGetsI() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object = ops.createProposal(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Proposal> selection = ops.getProposals(Proposal.CONSUMER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsII() throws ClientException, DataSourceException {
        List<Proposal> selection = new ProposalOperations().getProposals(Proposal.CONSUMER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test
    public void testUpdate() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object = ops.createProposal(pm, object); // Gives the PersistenceManager so it won't be closed
        object.setConsumerKey(222L);

        Proposal updated = ops.updateProposal(object);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertTrue(pm.isClosed());
    }

    @Ignore
    @Test
    public void testDelete() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setConsumerKey(111L);
        object = ops.createProposal(pm, object); // Gives the PersistenceManager so it won't be closed

        // ops.deleteProposal(object.getKey());
        assertTrue(pm.isClosed());
    }
}
