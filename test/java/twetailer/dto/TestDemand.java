package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestDemand {

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

    Action action = Action.cancel;
    Long ownerKey = 12345L;
    Long rawCommandId = 67890L;
    Source source = Source.simulated;
    State state = State.closed;

    List<String> criteria = new ArrayList<String>(Arrays.asList(new String[] {"first", "second"}));
    Date expirationDate = new Date(new Date().getTime() + 65536L);
    Long locationKey = 67890L;
    List<Long> proposalKeys = new ArrayList<Long>(Arrays.asList(new Long[] {12345L, 67890L}));
    Long quantity = 15L;
    Double range = 25.52D;
    String rangeUnit = LocaleValidator.MILE_UNIT;

    @Test
    public void testAccessors() {
        Demand object = new Demand();

        // Command
        object.setAction(action);
        object.setAction(action.toString());
        object.setOwnerKey(ownerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setState(state);
        object.setState(state.toString());

        // Demand
        object.setCriteria(criteria);
        object.setExpirationDate(expirationDate);
        object.setLocationKey(locationKey);
        object.setProposalKeys(proposalKeys);
        object.setQuantity(quantity);
        object.setRange(range);
        object.setRangeUnit(rangeUnit);

        // Command
        assertEquals(action, object.getAction());
        assertEquals(action, object.getAction());
        assertEquals(ownerKey, object.getOwnerKey());
        assertEquals(rawCommandId, object.getRawCommandId());
        assertEquals(source, object.getSource());
        assertEquals(state, object.getState());

        // Demand
        assertEquals(criteria, object.getCriteria());
        assertEquals(expirationDate, object.getExpirationDate());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(proposalKeys, object.getProposalKeys());
        assertEquals(quantity, object.getQuantity());
        assertEquals(range, object.getRange());
        assertEquals(rangeUnit, object.getRangeUnit());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetCriteriaI() {
        Demand object = new Demand();

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
        Demand object = new Demand();

        object.resetLists(); // To force the criteria list creation
        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.resetLists(); // To be sure there's no error
        object.removeCriterion("first"); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetCriteria(); // Reset all
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
    public void testSetAction() {
        Demand object = new Demand();

        object.setAction((Action) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetExpirationDate() {
        Demand object = new Demand();

        object.setExpirationDate(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetExpirationDateInPast() {
        Demand object = new Demand();

        object.setExpirationDate(new Date(12345L));
    }

    @Test
    public void testSetRangeUnit() {
        Demand object = new Demand();

        object.setRangeUnit(LocaleValidator.MILE_UNIT.toLowerCase(Locale.ENGLISH));
        assertEquals(LocaleValidator.MILE_UNIT, object.getRangeUnit());

        object.setRangeUnit(LocaleValidator.MILE_UNIT.toUpperCase(Locale.ENGLISH));
        assertEquals(LocaleValidator.MILE_UNIT, object.getRangeUnit());

        object.setRangeUnit("zzz");
        assertEquals(LocaleValidator.KILOMETER_UNIT, object.getRangeUnit());

        object.setRangeUnit(null);
        assertEquals(LocaleValidator.KILOMETER_UNIT, object.getRangeUnit());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetState() {
        Demand object = new Demand();

        object.setState((State) null);
    }

    @Test
    public void testJsonDemandsI() {
        Demand object = new Demand();

        // Command
        object.setAction(action);
        object.setOwnerKey(ownerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        // Demand
        object.setCriteria(criteria);
        object.setExpirationDate(expirationDate);
        object.setLocationKey(locationKey);
        object.setProposalKeys(proposalKeys);
        object.setQuantity(quantity);
        object.setRange(range);
        object.setRangeUnit(rangeUnit);

        Demand clone = new Demand(object.toJson());

        // Command
        assertEquals(action, clone.getAction());
        assertEquals(ownerKey, clone.getOwnerKey());
        assertEquals(rawCommandId, clone.getRawCommandId());
        assertEquals(source, clone.getSource());
        assertEquals(state, clone.getState());

        // Demand
        assertEquals(criteria, clone.getCriteria());
        assertEquals(DateUtils.dateToISO(expirationDate), DateUtils.dateToISO(clone.getExpirationDate()));
        assertEquals(locationKey, clone.getLocationKey());
        assertEquals(proposalKeys, clone.getProposalKeys());
        assertEquals(quantity, clone.getQuantity());
        assertEquals(range, clone.getRange());
        assertEquals(rangeUnit, clone.getRangeUnit());
    }

    @Test
    public void testJsonDemandsII() {
        Demand object = new Demand();
        object.setSource(source);

        // Command
        assertNull(object.getOwnerKey());
        assertNull(object.getRawCommandId());

        // Demand
        assertNull(object.getLocationKey());
        assertEquals(0, object.getCriteria().size());
        assertEquals(0, object.getProposalKeys().size());

        Demand clone = new Demand(object.toJson());

        // Command
        assertNull(clone.getOwnerKey());
        assertNull(clone.getRawCommandId());

        // Demand
        assertNull(clone.getLocationKey());
        assertEquals(0, clone.getCriteria().size());
        assertEquals(0, clone.getProposalKeys().size());
    }

    @Test
    public void testJsonDemandsIII() {
        Demand object = new Demand();
        object.setSource(source);

        object.resetLists();

        // Demand
        assertNull(object.getCriteria());
        assertNull(object.getProposalKeys());

        Demand clone = new Demand(object.toJson());

        // Demand
        assertEquals(0, clone.getCriteria().size()); // Not null because the clone object creation creates empty List<String>
        assertEquals(0, clone.getProposalKeys().size()); // Not null because the clone object creation creates empty List<Long>
    }

    @Test
    public void testInvalidDateFormat() throws JsonException {
        Demand object = new Demand();
        object.setSource(source);
        Date date = object.getExpirationDate();

        object.fromJson(new JsonParser("{'" + Demand.EXPIRATION_DATE + "':'2009-01-01Tzzz'}").getJsonObject());

        assertEquals(DateUtils.dateToISO(date), DateUtils.dateToISO(object.getExpirationDate())); // Corrupted date did not alter the original date
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetSource() {
        Demand object = new Demand();

        object.setSource((Source) null);
    }

    @Test
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Demand.REFERENCE, key);

        assertEquals(key, new Demand(parameters).getKey());
    }
}
