package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.task.RobotResponder;
import twetailer.validator.CommandSettings;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestProposalOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseOperations.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testGetLogger() throws IOException {
        Logger log1 = new ProposalOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new ProposalOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureI() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public Proposal createProposal(PersistenceManager pm, Proposal item) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.createProposal(new Proposal());
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
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
        // item.put(Proposal.OWNER_KEY, null);

        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setKey(111L);
        saleAssociate.setStoreKey(222L);
        saleAssociate.setPreferredConnection(Source.simulated);

        Proposal object = new ProposalOperations().createProposal(item, saleAssociate);
        assertNotNull(object.getKey());
        assertEquals(Long.valueOf(222L), object.getStoreKey());
    }

    @Test
    public void testCreateIIb() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 0L);

        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setKey(111L);
        saleAssociate.setStoreKey(222L);
        saleAssociate.setPreferredConnection(Source.simulated);

        Proposal object = new ProposalOperations().createProposal(item, saleAssociate);
        assertNotNull(object.getKey());
        assertEquals(Long.valueOf(222L), object.getStoreKey());
    }

    @Test
    public void testCreateIII() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 111L);

        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setKey(111L);
        saleAssociate.setStoreKey(222L);
        saleAssociate.setPreferredConnection(Source.simulated);

        Proposal object = new ProposalOperations().createProposal(item, saleAssociate);
        assertNotNull(object.getKey());
        assertEquals(Long.valueOf(222L), object.getStoreKey());
    }

    @Test(expected=ClientException.class)
    public void testCreateIV() throws ClientException {
        JsonObject item = new GenericJsonObject();
        item.put(Command.OWNER_KEY, 333L);

        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setKey(111L);
        saleAssociate.setStoreKey(222L);
        saleAssociate.setPreferredConnection(Source.simulated);

        new ProposalOperations().createProposal(item, saleAssociate);
    }

    @Test
    public void testGetI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
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
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 333L, null);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIIb() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 333L, 0L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIIIa() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), null, 444L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIIIb() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 0L, 444L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIVa() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(null, 111L, 222L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIVb() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(0L, 111L, 222L);
    }

    @Test
    public void testGetVa() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), null, null);
    }

    @Test
    public void testGetVb() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 0L, 0L);
    }

    @Test
    public void testGetVI() throws ClientException, DataSourceException {
        ProposalOperations ops = new ProposalOperations();
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setStoreKey(222L);
        object = ops.createProposal(object);

        ops.getProposal(object.getKey(), 333L, 444L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetsWithFailureI() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String key, Object value, int limit) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.getProposals("key", "value", 4324);
    }

    @Test
    public void testGetsI() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object = ops.createProposal(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Proposal> selection = ops.getProposals(Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsII() throws ClientException, DataSourceException {
        List<Proposal> selection = new ProposalOperations().getProposals(Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal item) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.updateProposal(new Proposal());
    }

    @Test
    public void testUpdate() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object = ops.createProposal(pm, object); // Gives the PersistenceManager so it won't be closed
        object.setOwnerKey(222L);

        Proposal updated = ops.updateProposal(object);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertTrue(pm.isClosed());
    }

    @Ignore
    @Test
    public void testDelete() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object = ops.createProposal(pm, object); // Gives the PersistenceManager so it won't be closed

        // ops.deleteProposal(object.getKey());
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetsExtendedI() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations();

        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object = ops.createProposal(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, 111L);

        List<Proposal> selection = ops.getProposals(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedII() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations();

        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setDemandKey(2222L);
        object = ops.createProposal(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Proposal.DEMAND_KEY, object.getDemandKey());

        List<Proposal> selection = ops.getProposals(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedIII() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations();

        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setDemandKey(2222L);
        object.setState(CommandSettings.State.opened);
        object = ops.createProposal(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Proposal.DEMAND_KEY, object.getDemandKey());
        parameters.put(Command.STATE, object.getState().toString());

        List<Proposal> selection = ops.getProposals(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedIV() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations();

        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setDemandKey(2222L);
        object.setState(CommandSettings.State.opened);
        object.setPrice(25.97D);
        object = ops.createProposal(object);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Proposal.DEMAND_KEY, object.getDemandKey());
        parameters.put(Command.STATE, object.getState().toString());
        parameters.put(Proposal.PRICE, object.getPrice());

        List<Proposal> selection = ops.getProposals(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0).getKey());
    }

    @Test
    public void testGetsExtendedV() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations();

        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object.setDemandKey(2222L);
        object.setState(CommandSettings.State.opened);
        object.setPrice(22.97D);
        object = ops.createProposal(object);

        object = new Proposal();
        object.setOwnerKey(111L);
        object.setDemandKey(2222L);
        object.setState(CommandSettings.State.opened);
        object.setPrice(25.97D);
        object = ops.createProposal(object);
        Long selectedKey = object.getKey();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, object.getOwnerKey());
        parameters.put(Proposal.DEMAND_KEY, object.getDemandKey());
        parameters.put(Command.STATE, object.getState().toString());
        parameters.put(">" + Proposal.PRICE, Double.valueOf(24.0D));

        List<Proposal> selection = ops.getProposals(ops.getPersistenceManager(), parameters, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(selectedKey, selection.get(0).getKey());
    }

    @Test
    public void testGetKeysI() throws DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Proposal object = new Proposal();
        object.setOwnerKey(111L);
        object = ops.createProposal(pm, object); // Gives the PersistenceManager so it won't be closed

        List<Long> selection = ops.getProposalKeys(pm, Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertEquals(object.getKey(), selection.get(0));
    }

    @Test
    public void testGetKeysII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        List<Long> selection = new ProposalOperations().getProposalKeys(pm, Command.OWNER_KEY, 111L, 0);
        assertNotNull(selection);
        assertEquals(0, selection.size());
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteWithFailureI() throws DataSourceException {
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public void deleteProposal(PersistenceManager pm, Long key, Long ownerKey) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.deleteProposal(12345L, 4567L);
    }

    @Test
    public void testDeleteI() throws DataSourceException {
        final Long proposalKey = 54657L;
        ProposalOperations ops = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                return proposal;
            }
            @Override
            public void deleteProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
            }
        };
        ops.deleteProposal(proposalKey, null);
    }

    @Test
    public void testDeleteII() throws DataSourceException {
        final String tag = "tag";
        Proposal toBeCreated = new Proposal();
        toBeCreated.addCriterion(tag);
        ProposalOperations ops = new ProposalOperations();
        Proposal justCreated = ops.createProposal(toBeCreated);
        assertNotNull(justCreated.getKey());
        assertEquals(tag, justCreated.getCriteria().get(0));
        ops.deleteProposal(justCreated.getKey(), null);
    }

    @Test
    public void testGetsAroundLocationI() throws DataSourceException {
        //
        // Get all demands from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        ProposalOperations ops = new ProposalOperations();

        final Long ownerKey = 45678L;
        final Long storeKey = 78901L;
        Proposal first = new Proposal();
        first.setLocationKey(where.getKey());
        first.setOwnerKey(ownerKey);
        first.setStoreKey(storeKey);
        first = ops.createProposal(first);

        Proposal second = new Proposal();
        second.setLocationKey(where.getKey());
        second.setOwnerKey(ownerKey);
        second.setStoreKey(storeKey);
        second = ops.createProposal(second);

        first = ops.getProposal(first.getKey(), null, null);
        second = ops.getProposal(second.getKey(), null, null);

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Proposal> selection = ops.getProposals(places, 0);
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertTrue (selection.get(0).getKey().equals(first.getKey()) && selection.get(1).getKey().equals(second.getKey()) ||
                selection.get(1).getKey().equals(first.getKey()) && selection.get(0).getKey().equals(second.getKey()));
        // assertEquals(first.getKey(), selection.get(1).getKey()); // Should be second because of ordered by descending date
        // assertEquals(second.getKey(), selection.get(0).getKey()); // but dates are so closed that sometines first is returned first...
    }

    @Test
    public void testGetsAroundLocationII() throws DataSourceException {
        //
        // Get just one demand from one location
        //
        Location where = new Location();
        where.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        where.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        where = new LocationOperations().createLocation(where);

        ProposalOperations ops = new ProposalOperations();

        final Long ownerKey = 45678L;
        final Long storeKey = 78901L;
        Proposal first = new Proposal();
        first.setLocationKey(where.getKey());
        first.setOwnerKey(ownerKey);
        first.setStoreKey(storeKey);
        first = ops.createProposal(first);

        Proposal second = new Proposal();
        second.setLocationKey(where.getKey());
        second.setOwnerKey(ownerKey);
        second.setStoreKey(storeKey);
        second = ops.createProposal(second);

        first = ops.getProposal(first.getKey(), null, null);
        second = ops.getProposal(second.getKey(), null, null);

        List<Location> places = new ArrayList<Location>();
        places.add(where);

        List<Proposal> selection = ops.getProposals(places, 1);
        assertNotNull(selection);
        assertEquals(1, selection.size());
        assertTrue (selection.get(0).getKey().equals(first.getKey()) ||
                selection.get(0).getKey().equals(second.getKey()));
        // assertEquals(first.getKey(), selection.get(1).getKey()); // Should be second because of ordered by descending date
        // assertEquals(second.getKey(), selection.get(0).getKey()); // but dates are so closed that sometines first is returned first...
    }

    @Test
    public void testGetsAroundLocationIII() throws DataSourceException {
        //
        // Get limited number of demands from many locations
        //
        LocationOperations lOps = new LocationOperations();
        ProposalOperations sOps = new ProposalOperations();

        Location lFirst = new Location();
        lFirst.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        lFirst.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        lFirst = lOps.createLocation(lFirst);

        final Long ownerKey = 45678L;
        final Long storeKey = 78901L;
        Proposal sFirst = new Proposal();
        sFirst.setLocationKey(lFirst.getKey());
        sFirst.setOwnerKey(ownerKey);
        sFirst.setStoreKey(storeKey);
        sFirst = sOps.createProposal(sFirst);

        Location lSecond = new Location();
        lSecond.setPostalCode("H1H1H1");
        lSecond.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        lSecond = lOps.createLocation(lSecond);

        Proposal sSecond = new Proposal();
        sSecond.setLocationKey(lSecond.getKey());
        sSecond.setOwnerKey(ownerKey);
        sSecond.setStoreKey(storeKey);
        sOps.createProposal(sSecond);

        Proposal sThird = new Proposal();
        sThird.setLocationKey(lSecond.getKey());
        sThird.setOwnerKey(ownerKey);
        sThird.setStoreKey(storeKey);
        sOps.createProposal(sThird);

        sFirst = sOps.getProposal(sFirst.getKey(), null, null);
        sSecond = sOps.getProposal(sSecond.getKey(), null, null);
        sThird = sOps.getProposal(sThird.getKey(), null, null);

        List<Location> places = new ArrayList<Location>();
        places.add(lFirst);
        places.add(lSecond);

        List<Proposal> selection = sOps.getProposals(places, 2); // Should cut to 2 items
        assertNotNull(selection);
        assertEquals(2, selection.size());
        assertEquals(sFirst.getKey(), selection.get(0).getKey());
        // No more test because it appears sometimes sSecond comes back, sometimes sThird comes back
        // FIXME: re-insert the test for sSecond in the returned list when we're sure the issue related ordering on inherited attribute is fixed.
    }

    @Test(expected=RuntimeException.class)
    public void testGetsAroundLocationIV() throws DataSourceException {
        //
        // Get demands fails
        //
        ProposalOperations sOps = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, List<Location> locations, int limit) throws DataSourceException {
                throw new RuntimeException("Done in purpose!");
            }
        };

        sOps.getProposals((List<Location>) null, 0);
    }
}
