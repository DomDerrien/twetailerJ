package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestProposal {

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
        Proposal object = new Proposal();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Proposal object = new Proposal(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    String AWSCBUIURL = "Very long long text!";
    Action action = Action.cancel;
    Long ownerKey = 12345L;
    Long rawCommandId = 67890L;
    Source source = Source.simulated;
    State state = State.closed;

    List<String> criteria = new ArrayList<String>(Arrays.asList(new String[] {"first", "second"}));
    Long consumerKey = 876432L;
    Long demandKey = 54321L;
    Long proposalKey = 98760L;
    Double price = 25.99D;
    Long quantity = 15L;
    Long storeKey = 45678L;
    Double total = 32.36D;

    @Test
    public void testAccessors() {
        Proposal object = new Proposal();

        // Command
        object.setAction(action);
        object.setAction(action.toString());
        object.setOwnerKey(ownerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setState(state);
        object.setState(state.toString());

        // Proposal
        object.setAWSCBUIURL(AWSCBUIURL);
        object.setConsumerKey(consumerKey);
        object.setCriteria(criteria);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setQuantity(quantity);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        // Command
        assertEquals(action, object.getAction());
        assertEquals(action, object.getAction());
        assertEquals(ownerKey, object.getOwnerKey());
        assertEquals(rawCommandId, object.getRawCommandId());
        assertEquals(source, object.getSource());
        assertEquals(state, object.getState());

        // Proposal
        assertEquals(AWSCBUIURL, object.getAWSCBUIURL());
        assertEquals(criteria, object.getCriteria());
        assertEquals(consumerKey, object.getConsumerKey());
        assertEquals(demandKey, object.getDemandKey());
        assertEquals(quantity, object.getQuantity());
        assertEquals(storeKey, object.getStoreKey());
        assertEquals(total, object.getTotal());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetCriteriaI() {
        Proposal object = new Proposal();

        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("first"); // Add it twice
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("second");
        assertEquals(2, object.getCriteria().size());

        object.removeCriterion("first"); // Remove first
        assertEquals(1, object.getCriteria().size());

        object.resetCriteria(); // Reset all
        assertEquals(0, object.getCriteria().size());

        object.setCriteria(null); // Failure!
    }

    @Test
    public void testResetCriteriaII() {
        Proposal object = new Proposal();

        object.resetLists(); // To force the criteria list creation
        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.resetLists(); // To be sure there's no error
        object.removeCriterion("first"); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetCriteria(); // Reset all
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetAction() {
        Proposal object = new Proposal();

        object.setAction((Action) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetState() {
        Proposal object = new Proposal();

        object.setState((State) null);
    }

    @Test
    public void testJsonProposalsI() {
        Proposal object = new Proposal();

        // Command
        object.setAction(action);
        object.setOwnerKey(ownerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        // Proposal
        object.setAWSCBUIURL(AWSCBUIURL);
        object.setCriteria(criteria);
        object.setConsumerKey(consumerKey);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setQuantity(quantity);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        Proposal clone = new Proposal(object.toJson());

        // Command
        assertEquals(action, clone.getAction());
        assertEquals(ownerKey, clone.getOwnerKey());
        assertEquals(rawCommandId, clone.getRawCommandId());
        assertEquals(source, clone.getSource());
        assertEquals(state, clone.getState());

        // Proposal
        assertEquals(AWSCBUIURL, clone.getAWSCBUIURL());
        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(criteria, clone.getCriteria());
        assertEquals(demandKey, clone.getDemandKey());
        assertEquals(quantity, clone.getQuantity());
        assertNull(clone.getStoreKey()); // Set automatically by the system with the SaleAssociate storeKey
        assertEquals(total, clone.getTotal());
        assertFalse(clone.getStateCmdList());
    }

    @Test
    public void testJsonProposalsII() {
        Proposal object = new Proposal();
        object.setSource(source);

        // Command
        assertNull(object.getRawCommandId());

        // Proposal
        assertNull(object.getConsumerKey());
        assertNull(object.getDemandKey());
        assertEquals(0, object.getCriteria().size());
        assertNull(object.getStoreKey());

        Proposal clone = new Proposal(object.toJson());

        // Command
        assertNull(clone.getRawCommandId());

        // Proposal
        assertNull(clone.getConsumerKey());
        assertNull(clone.getDemandKey());
        assertEquals(0, clone.getCriteria().size());
        assertNull(clone.getStoreKey());
    }

    @Test
    public void testJsonProposalsIII() {
        Proposal object = new Proposal();
        object.setSource(source);

        object.resetLists();

        // Proposal
        assertNull(object.getCriteria());

        Proposal clone = new Proposal(object.toJson());

        // Proposal
        assertEquals(0, clone.getCriteria().size()); // Not null because the clone object creation creates empty List<String>
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetSource() {
        Proposal object = new Proposal();

        object.setSource((Source) null);
    }

    @Test
    public void testShortcutI() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Proposal.PROPOSAL_KEY, key);
        parameters.put(Proposal.DEMAND_REFERENCE, key);

        Proposal proposal = new Proposal(parameters);
        assertEquals(key, proposal.getKey());
        assertNull(proposal.getDemandKey()); // Can only be set with Proposal to be created
    }

    @Test
    public void testShortcutII() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Proposal.DEMAND_REFERENCE, key);

        Proposal proposal = new Proposal(parameters);
        assertNull(proposal.getKey());
        assertEquals(key, proposal.getDemandKey());
    }

    @Test
    public void testSetStateCommandList() {
        Proposal proposal = new Proposal();
        // proposal.getState() == State.opened by default
        assertTrue(proposal.getStateCmdList());

        proposal.setState(State.cancelled);
        assertFalse(proposal.getStateCmdList());

        proposal.setState(State.invalid);
        assertTrue(proposal.getStateCmdList());

        proposal.setState(State.declined);
        assertFalse(proposal.getStateCmdList());

        proposal.setState(State.published);
        assertTrue(proposal.getStateCmdList());

        proposal.setState(State.markedForDeletion);
        assertFalse(proposal.getStateCmdList());

        proposal.setState(State.opened);
        assertTrue(proposal.getStateCmdList());

        proposal.setState(State.closed);
        assertFalse(proposal.getStateCmdList());
    }

    @Test
    public void testGetSerialized() {
        Proposal proposal = new Proposal();
        proposal.addCriterion("one");
        proposal.addCriterion("two");
        proposal.addCriterion("three");

        assertEquals("one two three", proposal.getSerializedCriteria());
    }

    @Test
    public void testGetStateCmdList() {
        Proposal proposal = new Proposal();
        assertTrue(proposal.getStateCmdList());
        proposal.setStateCmdList(null);
        assertTrue(proposal.getStateCmdList());
        proposal.setStateCmdList(false);
        assertFalse(proposal.getStateCmdList());
        proposal.setStateCmdList(true);
        assertTrue(proposal.getStateCmdList());
    }

    @Test
    public void testGetAWSCBUIURL() {
        assertNull(new Proposal().getAWSCBUIURL());
    }

    @Test
    public void testSetAWSCBUIURL() {
        Proposal object = new Proposal();

        object.setAWSCBUIURL(null);
        assertNull(object.getAWSCBUIURL());
        object.setAWSCBUIURL("");
        assertNull(object.getAWSCBUIURL());
        object.setAWSCBUIURL(AWSCBUIURL);
        assertEquals(AWSCBUIURL, object.getAWSCBUIURL());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetConsumerKey() {
        new Proposal().setConsumerKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetDemandKey() {
        new Proposal().setDemandKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetStoreKey() {
        new Proposal().setStoreKey(null);
    }

    @Test
    public void testFromJson() {
        Proposal object = new Proposal();
        object.setKey(12345L);

        JsonObject json = new GenericJsonObject();
        json.put(Proposal.DEMAND_KEY, 545456L);

        object.fromJson(json);
        assertNull(object.getDemandKey());
    }
}
