package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestCommand {

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
        Command object = new Command();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Command object = new Command(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Action action = Action.cancel;
    Calendar dueDate = DateUtils.getNowCalendar();
    List<String> cc = new ArrayList<String>(Arrays.asList(new String[] {"first", "second"}));
    List<String> tags = new ArrayList<String>(Arrays.asList(new String[] {"first", "second"}));
    List<String> hashTags = new ArrayList<String>(Arrays.asList(new String[] {"first", "second"}));
    Long locationKey = 87541L;
    Long ownerKey = 12345L;
    Long rawCommandId = 67890L;
    Source source = Source.simulated;
    Long cancelerKey = 76545L;
    State state = State.closed;
    String metadata = "{}";

    @Test
    public void testAccessors() {
        Command object = new Command();

        object.setAction(action);
        object.setAction(action.toString());
        object.setDueDate(dueDate.getTime());
        object.setCancelerKey(cancelerKey);
        object.setCC(cc);
        object.setCriteria(tags);
        object.setHashTags(hashTags);
        object.setLocationKey(locationKey);
        object.setMetadata(metadata);
        object.setOwnerKey(ownerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setState(state);
        object.setState(state.toString());

        assertEquals(action, object.getAction());
        assertEquals(dueDate.getTime(), object.getDueDate());
        assertEquals(cancelerKey, object.getCancelerKey());
        assertEquals(cc, object.getCC());
        assertEquals(tags, object.getCriteria());
        assertEquals(hashTags, object.getHashTags());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(metadata, object.getMetadata());
        assertEquals(ownerKey, object.getOwnerKey());
        assertEquals(rawCommandId, object.getRawCommandId());
        assertEquals(source, object.getSource());
        assertEquals(state, object.getState());
    }

    @Test
    public void testJsonCommandsI() {
        Command object = new Command();

        object.setAction(action);
        object.setDueDate(dueDate.getTime());
        object.setCancelerKey(cancelerKey);
        object.setHashTags(hashTags);
        object.setLocationKey(locationKey);
        object.setMetadata(metadata);
        object.setOwnerKey(ownerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        Command clone = new Command(object.toJson());

        // In the translation, the milliseconds are rounded!
        dueDate.set(Calendar.MILLISECOND, 0);

        assertNull(clone.getAction()); // Cannot be overridden
        assertEquals(dueDate.getTime(), clone.getDueDate());
        assertNull(clone.getCancelerKey()); // Cannot be overridden
        assertEquals(hashTags, clone.getHashTags());
        assertEquals(locationKey, clone.getLocationKey());
        assertEquals(metadata, clone.getMetadata());
        assertNull(clone.getOwnerKey()); // Cannot be overridden
        assertNull(clone.getRawCommandId()); // Cannot be overridden
        assertEquals(source, clone.getSource());
        assertEquals(State.opened, clone.getState()); // Default state, cannot be overridden
    }

    @Test
    public void testJsonCommandsII() {
        Command object = new Command();
        object.setSource(source);
        object.setCC(cc);
        object.setCriteria(tags);
        object.setHashTags(hashTags);

        assertNull(object.getRawCommandId());

        Command clone = new Command(object.toJson());

        assertNull(clone.getRawCommandId());
        assertEquals(object.getCC().size(), clone.getCC().size());
        assertEquals(object.getCC().get(0), clone.getCC().get(0));
        assertEquals(object.getCriteria().size(), clone.getCriteria().size());
        assertEquals(object.getCriteria().get(0), clone.getCriteria().get(0));
        assertEquals(object.getHashTags().size(), clone.getHashTags().size());
        assertEquals(object.getHashTags().get(0), clone.getHashTags().get(0));
    }

    @Test
    public void testJsonDemandsIII() {
        Command object = new Command();
        object.setSource(source);

        // Demand
        assertEquals(0, object.getCC().size());
        assertEquals(0, object.getCriteria().size());
        assertEquals(0, object.getHashTags().size());

        Command clone = new Command(object.toJson());

        // Demand
        assertEquals(0, clone.getCC().size());
        assertEquals(0, clone.getCriteria().size());
        assertEquals(0, clone.getHashTags().size());

        // Demand
        object.resetLists();
        assertNull(object.getCC());
        assertNull(object.getCriteria());
        assertNull(object.getHashTags());

        clone = new Command(object.toJson());

        // Demand
        assertEquals(0, clone.getCC().size()); // Not null because the clone object creation creates empty List<String>
        assertEquals(0, clone.getCriteria().size()); // Not null because the clone object creation creates empty List<String>
        assertEquals(0, clone.getHashTags().size()); // Not null because the clone object creation creates empty List<String>
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetAction() {
        new Command().setAction((Action) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetCancelerKey() {
        new Command().setCancelerKey((Long) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetCC() {
        new Command().setCC((List<String>) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetCriteria() {
        new Command().setCriteria((List<String>) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetOwnerKey() {
        new Command().setOwnerKey((Long) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetQuantity() {
        new Command().setQuantity((Long) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRawCommandId() {
        new Command().setRawCommandId((Long) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetSource() {
        new Command().setSource((Source) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetStateI() {
        new Command().setState((State) null);
    }

    @Test
    public void testSetStateII() {
        Command object = new Command();

        assertEquals(State.opened, object.getState());
        assertTrue(object.getStateCmdList());
        assertFalse(object.getMarkedForDeletion());

        object.setState(State.invalid);
        assertEquals(State.invalid, object.getState());
        assertTrue(object.getStateCmdList());

        object.setState(State.published);
        assertEquals(State.published, object.getState());
        assertTrue(object.getStateCmdList());

        object.setState(State.cancelled);
        assertEquals(State.cancelled, object.getState());
        assertFalse(object.getStateCmdList());

        object.setState(State.confirmed);
        assertEquals(State.confirmed, object.getState());
        assertTrue(object.getStateCmdList());

        object.setState(State.closed);
        assertEquals(State.closed, object.getState());
        assertFalse(object.getStateCmdList());

        object.setState(State.declined);
        assertEquals(State.declined, object.getState());
        assertFalse(object.getStateCmdList());

        object.setState(State.markedForDeletion);
        assertEquals(State.markedForDeletion, object.getState());
        assertFalse(object.getStateCmdList());
        assertTrue(object.getMarkedForDeletion());

        object.setStateCmdList(null);
        assertTrue(object.getStateCmdList());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetHashTagsI() {
        Command object = new Command();

        object.addHashTag(null);
        object.addHashTag("");
        object.addHashTag("first");
        assertEquals(1, object.getHashTags().size());

        object.addHashTag("first"); // Add it twice
        assertEquals(1, object.getHashTags().size());

        object.addHashTag("second");
        assertEquals(2, object.getHashTags().size());

        object.removeHashTag(null);
        object.removeHashTag("");
        object.removeHashTag("first"); // Remove first
        assertEquals(1, object.getHashTags().size());

        object.resetHashTags(); // Reset all
        assertEquals(0, object.getHashTags().size());

        object.setHashTags(null); // Failure!
    }

    @Test
    public void testResetHashTagsII() {
        Command object = new Command();

        object.resetLists(); // To force the hashTags list creation
        object.addHashTag("first");
        assertEquals(1, object.getHashTags().size());

        object.resetLists(); // To be sure there's no error
        object.removeHashTag("first"); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetHashTags(); // Reset all
    }

    @Test
    public void testGetSerializedTagsI() {
        assertEquals(Command.EMPTY_STRING, Command.getSerializedTags(null));
    }

    @Test
    public void testGetSerializedTagsII() {
        assertEquals(Command.EMPTY_STRING, Command.getSerializedTags(new ArrayList<String>()));
    }

    @Test
    public void testGetSerializedTagsIII() {
        assertEquals(Command.EMPTY_STRING, Command.getSerializedTags("Not important!", "Not important!", null));
    }

    @Test
    public void testGetSerializedTagsIV() {
        assertEquals(Command.EMPTY_STRING, Command.getSerializedTags("Not important!", "Not important!", new ArrayList<String>()));
    }

    @Test
    public void testGetSerializedHashTagsI() {
        assertEquals(Command.EMPTY_STRING, new Command().getSerializedHashTags());
    }

    @Test
    public void testGetSerializedHashTagsII() {
        Command command = new Command();
        command.addHashTag("one");
        command.addHashTag("two");
        command.addHashTag("three");

        assertEquals("#one #two #three", command.getSerializedHashTags());
    }

    @Test
    public void testGetSerializedHashTagsIII() {
        Command command = new Command();
        command.addHashTag("one");
        command.addHashTag("two");
        command.addHashTag("three");

        assertEquals("one two three", Command.getSerializedTags(command.getHashTags()));
        assertEquals("#one #two #three", Command.getSerializedTags(Command.HASH, Command.SPACE, command.getHashTags()));
    }

    @Test
    public void testCCJsonI() {
        Command command = new Command();
        command.addCoordinate("one");
        command.addCoordinate("two");
        command.addCoordinate("three");

        JsonObject json = new GenericJsonObject();
        json.put(Command.CC_ADD, new GenericJsonArray());
        json.getJsonArray(Command.CC_ADD).add("four");

        command.fromJson(json);

        assertEquals(4, command.getCC().size());
    }

    @Test
    public void testCCJsonII() {
        Command command = new Command();
        command.addCoordinate("one");
        command.addCoordinate("two");
        command.addCoordinate("three");

        JsonObject json = new GenericJsonObject();
        json.put(Command.CC_ADD, new GenericJsonArray());
        json.getJsonArray(Command.CC_ADD).add("four");

        json.put(Command.CC_REMOVE, new GenericJsonArray());
        json.getJsonArray(Command.CC_REMOVE).add("two");
        json.getJsonArray(Command.CC_REMOVE).add("four");

        command.fromJson(json);

        assertEquals(2, command.getCC().size());
    }

    @Test
    public void testCriteriaJsonI() {
        Command command = new Command();
        command.addCriterion("one");
        command.addCriterion("two");
        command.addCriterion("three");

        JsonObject json = new GenericJsonObject();
        json.put(Command.CRITERIA_ADD, new GenericJsonArray());
        json.getJsonArray(Command.CRITERIA_ADD).add("four");

        command.fromJson(json);

        assertEquals(4, command.getCriteria().size());
    }

    @Test
    public void testCriteriaJsonII() {
        Command command = new Command();
        command.addCriterion("one");
        command.addCriterion("two");
        command.addCriterion("three");

        JsonObject json = new GenericJsonObject();
        json.put(Command.CRITERIA_ADD, new GenericJsonArray());
        json.getJsonArray(Command.CRITERIA_ADD).add("four");

        json.put(Command.CRITERIA_REMOVE, new GenericJsonArray());
        json.getJsonArray(Command.CRITERIA_REMOVE).add("two");
        json.getJsonArray(Command.CRITERIA_REMOVE).add("four");

        command.fromJson(json);

        assertEquals(2, command.getCriteria().size());
    }

    @Test
    public void testHashTagJsonI() {
        Command command = new Command();
        command.addHashTag("one");
        command.addHashTag("two");
        command.addHashTag("three");

        JsonObject json = new GenericJsonObject();
        json.put(Command.HASH_TAGS_ADD, new GenericJsonArray());
        json.getJsonArray(Command.HASH_TAGS_ADD).add("four");

        command.fromJson(json);

        assertEquals(4, command.getHashTags().size());
    }

    @Test
    public void testHashTagJsonII() {
        Command command = new Command();
        command.addHashTag("one");
        command.addHashTag("two");
        command.addHashTag("three");

        JsonObject json = new GenericJsonObject();
        json.put(Command.HASH_TAGS_ADD, new GenericJsonArray());
        json.getJsonArray(Command.HASH_TAGS_ADD).add("four");

        json.put(Command.HASH_TAGS_REMOVE, new GenericJsonArray());
        json.getJsonArray(Command.HASH_TAGS_REMOVE).add("two");
        json.getJsonArray(Command.HASH_TAGS_REMOVE).add("four");

        command.fromJson(json);

        assertEquals(2, command.getHashTags().size());
    }

    @Test
    public void testQueryPointOfViewI() {
        QueryPointOfView defaultValue = QueryPointOfView.ANONYMOUS;
        assertEquals(defaultValue, QueryPointOfView.fromJson(null, defaultValue));
    }

    @Test
    public void testQueryPointOfViewII() {
        QueryPointOfView defaultValue = QueryPointOfView.ANONYMOUS;
        assertEquals(defaultValue, QueryPointOfView.fromJson(new GenericJsonObject(), defaultValue));
    }

    @Test
    public void testQueryPointOfViewIII() {
        QueryPointOfView defaultValue = QueryPointOfView.ANONYMOUS;
        QueryPointOfView otherValue = QueryPointOfView.SALE_ASSOCIATE;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, otherValue.toString());
        assertEquals(otherValue, QueryPointOfView.fromJson(parameters, defaultValue));
    }

    @Test
    public void testQueryPointOfViewIV() {
        QueryPointOfView defaultValue = QueryPointOfView.ANONYMOUS;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, "anything"); // will throw InvalidArgumentException
        assertEquals(defaultValue, QueryPointOfView.fromJson(parameters, defaultValue));
    }

    @Test
    public void testQueryPointOfViewV() {
        QueryPointOfView defaultValue = QueryPointOfView.ANONYMOUS;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.POINT_OF_VIEW, 0L); // will throw ClassCastException
        assertEquals(defaultValue, QueryPointOfView.fromJson(parameters, defaultValue));
    }

    @Test
    public void testGetDefaultSerializedCCI() {
        String defaultValue = "default";
        assertEquals(defaultValue, new Command().getSerializedCC(defaultValue));
    }

    @Test
    public void testGetDefaultSerializedCCII() {
        String defaultValue = "default";
        assertEquals(defaultValue, new Command().resetLists().getSerializedCC(defaultValue));
    }

    @Test
    public void testManageCC() {
        String defaultValue = "default";

        Command command1 = new Command().resetLists();
        command1.addCoordinate("a");
        command1.addCoordinate(null);
        command1.addCoordinate("");
        command1.addCoordinate("b");
        command1.addCoordinate("a");
        assertEquals("a b", command1.getSerializedCC(defaultValue));

        Command command2 = new Command();
        command2.setCC(command1.getCC());
        assertEquals(2, command2.getCC().size());

        command1.removeCoordinate("a");
        command1.removeCoordinate(null);
        command1.removeCoordinate("");
        command1.removeCoordinate("c");

        assertEquals(1, command2.getCC().size());
        assertEquals("b", command2.getSerializedCC(defaultValue));

        new Command().resetLists().removeCoordinate("z"); // No issue reported
    }

    @Test
    public void testResetdCC() {
        Command command = new Command();
        assertEquals(0, command.getCC().size());
        command.addCoordinate("a");
        assertEquals(1, command.getCC().size());
        command.resetCC();
        assertEquals(0, command.getCC().size());
        command.resetLists();
        assertNull(command.getCC());
        command.resetCC(); // No issue reported
        assertNull(command.getCC());
    }

    @Test
    public void testGetDefaultSerializedCriteriaI() {
        String defaultValue = "default";
        assertEquals(defaultValue, new Command().getSerializedCriteria(defaultValue));
    }

    @Test
    public void testGetDefaultSerializedCriteriaII() {
        String defaultValue = "default";
        assertEquals(defaultValue, new Command().resetLists().getSerializedCriteria(defaultValue));
    }

    @Test
    public void testManageCriteria() {
        String defaultValue = "default";

        Command command1 = new Command().resetLists();
        command1.addCriterion("a");
        command1.addCriterion(null);
        command1.addCriterion("");
        command1.addCriterion("b");
        command1.addCriterion("a");
        assertEquals("a b", command1.getSerializedCriteria(defaultValue));

        Command command2 = new Command();
        command2.setCriteria(command1.getCriteria());
        assertEquals(2, command2.getCriteria().size());

        command1.removeCriterion("a");
        command1.removeCriterion(null);
        command1.removeCriterion("");
        command1.removeCriterion("c");

        assertEquals(1, command2.getCriteria().size());
        assertEquals("b", command2.getSerializedCriteria(defaultValue));

        new Command().resetLists().removeCriterion("z"); // No issue reported
    }

    @Test
    public void testResetdCriteria() {
        Command command = new Command();
        assertEquals(0, command.getCriteria().size());
        command.addCriterion("a");
        assertEquals(1, command.getCriteria().size());
        command.resetCriteria();
        assertEquals(0, command.getCriteria().size());
        command.resetLists();
        assertNull(command.getCriteria());
        command.resetCriteria(); // No issue reported
        assertNull(command.getCriteria());
    }

    @Test
    public void testGetDefaultSerializedHashTagsI() {
        String defaultValue = "default";
        assertEquals(defaultValue, new Command().getSerializedHashTags(defaultValue));
    }

    @Test
    public void testGetDefaultSerializedHashTagsII() {
        String defaultValue = "default";
        assertEquals(defaultValue, new Command().resetLists().getSerializedHashTags(defaultValue));
    }

    @Test
    public void testGetDefaultSerializedHashTagsIII() {
        String defaultValue = "default";
        Command command = new Command();
        command.addHashTag("a");
        assertEquals("#a", command.getSerializedHashTags(defaultValue));
    }

    @Test
    public void testDueDateFromJson() {
        Command command = new Command();
        assertNull(command.getDueDate());

        JsonObject json = new GenericJsonObject();
        json.put(Command.DUE_DATE, "2T2");

        command.fromJson(json);
        assertNull(command.getDueDate());
    }
}
