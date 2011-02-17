package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestDemand {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
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
    public void testConstructorI() {
        Demand object = new Demand();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Demand object = new Demand(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Long key = 543452L;
    List<Long> proposalKeys = Arrays.asList(new Long[] {12345L, 67890L});
    List<Long> saleAssociateKeys = Arrays.asList(new Long[] {1111L, 2222L});

    @Test
    public void testAccessors() {
        Demand object = new Demand();

        // Demand
        object.setProposalKeys(proposalKeys);
        object.setSaleAssociateKeys(saleAssociateKeys);

        // Demand
        assertEquals(proposalKeys, object.getProposalKeys());
        assertEquals(saleAssociateKeys, object.getSaleAssociateKeys());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetProposalKeysI() {
        Demand object = new Demand();

        object.addProposalKey(12345L);
        assertEquals(1, object.getProposalKeys().size());

        object.addProposalKey(12345L); // Add it twice
        assertEquals(1, object.getProposalKeys().size());

        object.addProposalKey(67890L);
        assertEquals(2, object.getProposalKeys().size());

        object.removeProposalKey(12345L); // Remove first
        assertEquals(1, object.getProposalKeys().size());

        object.resetProposalKeys(); // Reset all
        assertEquals(0, object.getProposalKeys().size());

        object.setProposalKeys(null); // Failure!
    }

    @Test
    public void testResetProposalKeysII() {
        Demand object = new Demand();

        object.resetLists(); // To force the criteria list creation
        object.addProposalKey(12345L);
        assertEquals(1, object.getProposalKeys().size());

        object.resetLists(); // To be sure there's no error
        object.removeProposalKey(23L); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetProposalKeys(); // Reset all
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetProposalKeysIII() {
        new Demand().addProposalKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetSaleAssociateKeysI() {
        Demand object = new Demand();

        object.addSaleAssociateKey(12345L);
        assertEquals(1, object.getSaleAssociateKeys().size());

        object.addSaleAssociateKey(12345L); // Add it twice
        assertEquals(1, object.getSaleAssociateKeys().size());

        object.addSaleAssociateKey(67890L);
        assertEquals(2, object.getSaleAssociateKeys().size());

        object.removeSaleAssociateKey(12345L); // Remove first
        assertEquals(1, object.getSaleAssociateKeys().size());

        object.resetSaleAssociateKeys(); // Reset all
        assertEquals(0, object.getSaleAssociateKeys().size());

        object.setSaleAssociateKeys(null); // Failure!
    }

    @Test
    public void testResetSaleAssociateKeysII() {
        Demand object = new Demand();

        object.resetLists(); // To force the criteria list creation
        object.addSaleAssociateKey(12345L);
        assertEquals(1, object.getSaleAssociateKeys().size());

        object.resetLists(); // To be sure there's no error
        object.removeSaleAssociateKey(23L); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetSaleAssociateKeys(); // Reset all
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Demand object = new Demand();

        object.setKey(key);

        object.setProposalKeys(proposalKeys);
        object.setSaleAssociateKeys(saleAssociateKeys);

        Demand clone = new Demand();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(2, clone.getProposalKeys().size());
        for (int idx = 0; idx < object.getProposalKeys().size(); idx ++) {
            assertEquals(object.getProposalKeys().get(idx), clone.getProposalKeys().get(idx));
        }
        assertEquals(2, clone.getSaleAssociateKeys().size());
        for (int idx = 0; idx < object.getSaleAssociateKeys().size(); idx ++) {
            assertEquals(object.getSaleAssociateKeys().get(idx), clone.getSaleAssociateKeys().get(idx));
        }
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Demand object = new Demand();

        Demand clone = new Demand();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(0, clone.getProposalKeys().size());
        assertEquals(0, clone.getSaleAssociateKeys().size());

        object.resetLists();

        clone = new Demand();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(0, clone.getProposalKeys().size());
        assertEquals(0, clone.getSaleAssociateKeys().size());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Demand object = new Demand();

        object.setKey(key);

        object.setProposalKeys(proposalKeys);
        object.setSaleAssociateKeys(saleAssociateKeys);

        Demand clone = new Demand();
        clone.fromJson(object.toJson(), true, false);

        assertEquals(key, clone.getKey());

        assertEquals(2, clone.getProposalKeys().size());
        for (int idx = 0; idx < object.getProposalKeys().size(); idx ++) {
            assertEquals(object.getProposalKeys().get(idx), clone.getProposalKeys().get(idx));
        }
        assertEquals(2, clone.getSaleAssociateKeys().size());
        for (int idx = 0; idx < object.getSaleAssociateKeys().size(); idx ++) {
            assertEquals(object.getSaleAssociateKeys().get(idx), clone.getSaleAssociateKeys().get(idx));
        }
    }

    @Test
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        Demand object = new Demand();

        object.setKey(key);

        object.setProposalKeys(proposalKeys);
        object.setSaleAssociateKeys(saleAssociateKeys);

        Demand clone = new Demand();
        clone.fromJson(object.toJson());

        assertEquals(key, clone.getKey());

        assertEquals(0, clone.getProposalKeys().size());
        assertEquals(0, clone.getSaleAssociateKeys().size());
    }

    @Test
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Demand.REFERENCE, key);

        assertEquals(key, new Demand(parameters).getKey());
    }

    @Test
    public void testGetSerializedPoroposalKeys() {
        Demand demand = new Demand();

        String defaultLabel = "test-default";
        assertEquals(defaultLabel, demand.getSerializedProposalKeys(defaultLabel));

        demand.addProposalKey(1111L);
        demand.addProposalKey(2222L);
        demand.addProposalKey(3333L);

        assertEquals("1111 2222 3333", demand.getSerializedProposalKeys(defaultLabel));

        demand.resetLists();
        assertEquals(defaultLabel, demand.getSerializedProposalKeys(defaultLabel));
    }

    @Test
    public void testRemoveProposalKeyI() {
        Demand demand = new Demand();
        demand.addProposalKey(12345L);
        demand.removeProposalKey(null);
        assertEquals(1, demand.getProposalKeys().size());
        assertEquals(Long.valueOf(12345L), demand.getProposalKeys().get(0));
    }

    @Test
    public void testRemoveProposalKeyII() {
        Demand demand = new Demand();
        demand.addProposalKey(12345L);
        demand.removeProposalKey(54321L);
        assertEquals(1, demand.getProposalKeys().size());
        assertEquals(Long.valueOf(12345L), demand.getProposalKeys().get(0));
    }
}
