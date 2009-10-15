package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestProposal {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();

        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
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

    CommandSettings.Action action = CommandSettings.Action.cancel;
    Long OwnerKey = 12345L;
    Long rawCommandId = 67890L;
    Source source = Source.simulated;
    CommandSettings.State state = CommandSettings.State.closed;

    List<String> criteria = new ArrayList<String>(Arrays.asList(new String[] {"first", "second"}));
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
        object.setOwnerKey(OwnerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setState(state);
        object.setState(state.toString());

        // Proposal
        object.setCriteria(criteria);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setQuantity(quantity);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        // Command
        assertEquals(action, object.getAction());
        assertEquals(action, object.getAction());
        assertEquals(OwnerKey, object.getOwnerKey());
        assertEquals(rawCommandId, object.getRawCommandId());
        assertEquals(source, object.getSource());
        assertEquals(state, object.getState());

        // Proposal
        assertEquals(criteria, object.getCriteria());
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

        object.setAction((CommandSettings.Action) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetState() {
        Proposal object = new Proposal();

        object.setState((CommandSettings.State) null);
    }

    @Test
    public void testJsonProposalsI() {
        Proposal object = new Proposal();

        // Command
        object.setAction(action);
        object.setOwnerKey(OwnerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        // Proposal
        object.setCriteria(criteria);
        object.setDemandKey(demandKey);
        object.setPrice(price);
        object.setQuantity(quantity);
        object.setStoreKey(storeKey);
        object.setTotal(total);

        Proposal clone = new Proposal(object.toJson());

        // Command
        assertEquals(action, clone.getAction());
        assertEquals(OwnerKey, clone.getOwnerKey());
        assertEquals(rawCommandId, clone.getRawCommandId());
        assertEquals(source, clone.getSource());
        assertEquals(state, clone.getState());

        // Proposal
        assertEquals(criteria, clone.getCriteria());
        assertEquals(demandKey, clone.getDemandKey());
        assertEquals(quantity, clone.getQuantity());
        assertEquals(storeKey, clone.getStoreKey());
        assertEquals(total, clone.getTotal());
    }

    @Test
    public void testJsonProposalsII() {
        Proposal object = new Proposal();
        object.setSource(source);

        // Command
        assertNull(object.getOwnerKey());
        assertNull(object.getRawCommandId());

        // Proposal
        assertNull(object.getDemandKey());
        assertEquals(0, object.getCriteria().size());
        assertNull(object.getStoreKey());

        Proposal clone = new Proposal(object.toJson());

        // Command
        assertNull(clone.getOwnerKey());
        assertNull(clone.getRawCommandId());

        // Proposal
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
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Proposal.PROPOSAL_KEY, key);
        parameters.put(Proposal.DEMAND_REFERENCE, key);

        Proposal proposal = new Proposal(parameters);
        assertEquals(key, proposal.getKey());
        assertEquals(key, proposal.getDemandKey());
    }
}
