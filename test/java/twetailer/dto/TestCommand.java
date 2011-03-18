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
import domderrien.jsontools.JsonArray;
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

    Long key = 1221L;

    Action action = Action.cancel;
    Long cancelerKey = 43322L;
    List<String> cc = Arrays.asList(new String[] {"first", "second"});
    String content = "first second";
    Calendar dueDate = DateUtils.getNowCalendar();
    List<String> hashTags = Arrays.asList(new String[] {"first", "second"});
    String metadata = "{'name':'value'}";
    Long ownerKey = 12345L;
    Long quantity = 32L;
    Long rawCommandId = 67890L;
    Source source = Source.simulated;
    State state = State.closed;

    @Test
    public void testAccessors() {
        Command object = new Command();

        object.setAction(action);
        object.setAction(action.toString());
        object.setCancelerKey(cancelerKey);
        object.setCC(cc);
        object.setContent(content);
        object.setDueDate(dueDate.getTime());
        object.setHashTags(hashTags);
        object.setMetadata(metadata);
        object.setOwnerKey(ownerKey);
        object.setQuantity(quantity);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setState(state);
        object.setState(state.toString());

        assertEquals(action, object.getAction());
        assertEquals(cancelerKey, object.getCancelerKey());
        assertEquals(cc, object.getCC());
        assertEquals(content, object.getContent());
        assertEquals(dueDate.getTime(), object.getDueDate());
        assertEquals(hashTags, object.getHashTags());
        assertEquals(metadata, object.getMetadata());
        assertEquals(ownerKey, object.getOwnerKey());
        assertEquals(quantity, object.getQuantity());
        assertEquals(rawCommandId, object.getRawCommandId());
        assertEquals(source, object.getSource());
        assertEquals(state, object.getState());
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Command object = new Command();

        object.setKey(key);

        object.setAction(action);
        object.setCancelerKey(cancelerKey);
        object.setCC(cc);
        object.setContent(content);
        object.setDueDate(dueDate.getTime());
        object.setHashTags(hashTags);
        object.setMetadata(metadata);
        object.setOwnerKey(ownerKey);
        object.setQuantity(quantity);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        Command clone = new Command();
        clone.fromJson(object.toJson(), true, true);

        // In the translation, the milliseconds are rounded!
        dueDate.set(Calendar.MILLISECOND, 0);

        assertEquals(key, clone.getKey());

        assertEquals(action, clone.getAction());
        assertEquals(cancelerKey, clone.getCancelerKey());
        assertEquals(cc.size(), clone.getCC().size());
        for (int idx=0; idx < object.getCC().size(); idx++) {
            assertEquals(object.getCC().get(idx), clone.getCC().get(idx));
        }
        assertEquals(content, clone.getContent());
        assertEquals(dueDate.getTime(), clone.getDueDate());
        assertEquals(hashTags.size(), clone.getHashTags().size());
        for (int idx=0; idx < object.getHashTags().size(); idx++) {
            assertEquals(object.getHashTags().get(idx), clone.getHashTags().get(idx));
        }
        assertEquals(metadata, clone.getMetadata());
        assertEquals(ownerKey, clone.getOwnerKey());
        assertEquals(quantity, clone.getQuantity());
        assertEquals(rawCommandId, clone.getRawCommandId());
        assertEquals(source, clone.getSource());
        assertEquals(state, clone.getState());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Command object = new Command();

        Command clone = new Command();
        clone.fromJson(object.toJson(), true, true);

        assertNull(clone.getAction());
        assertNull(clone.getCancelerKey());
        assertEquals(0, clone.getCC().size());
        assertEquals(0, clone.getContent().length());
        assertNull(clone.getDueDate());
        assertEquals(0, clone.getHashTags().size());
        assertNull(clone.getMetadata());
        assertNull(clone.getOwnerKey());
        assertEquals(1L, clone.getQuantity().longValue());
        assertNull(clone.getRawCommandId());
        assertNull(clone.getSource());
        assertEquals(State.opened, clone.getState());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Command object = new Command();

        object.setKey(key);

        object.setAction(action);
        object.setCancelerKey(cancelerKey);
        object.setCC(cc);
        object.setContent(content);
        object.setDueDate(dueDate.getTime());
        object.setHashTags(hashTags);
        object.setMetadata(metadata);
        object.setOwnerKey(ownerKey);
        object.setQuantity(quantity);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        Command clone = new Command();
        clone.fromJson(object.toJson(), true, false);

        // In the translation, the milliseconds are rounded!
        dueDate.set(Calendar.MILLISECOND, 0);

        assertEquals(key, clone.getKey());

        assertNull(clone.getAction());
        assertEquals(cancelerKey, clone.getCancelerKey());
        assertEquals(cc.size(), clone.getCC().size());
        for (int idx=0; idx < object.getCC().size(); idx++) {
            assertEquals(object.getCC().get(idx), clone.getCC().get(idx));
        }
        assertEquals(content, clone.getContent());
        assertEquals(dueDate.getTime(), clone.getDueDate());
        assertEquals(hashTags.size(), clone.getHashTags().size());
        for (int idx=0; idx < object.getHashTags().size(); idx++) {
            assertEquals(object.getHashTags().get(idx), clone.getHashTags().get(idx));
        }
        assertEquals(metadata, clone.getMetadata());
        assertEquals(ownerKey, clone.getOwnerKey());
        assertEquals(quantity, clone.getQuantity());
        assertNull(clone.getRawCommandId());
        assertNull(clone.getSource());
        assertEquals(state, clone.getState());
    }

    @Test
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        Command object = new Command();

        // object.setKey(key);

        object.setAction(action);
        object.setCancelerKey(cancelerKey);
        object.setCC(cc);
        object.setContent(content);
        object.setDueDate(dueDate.getTime());
        object.setHashTags(hashTags);
        object.setMetadata(metadata);
        object.setOwnerKey(ownerKey);
        object.setQuantity(quantity);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        Command clone = new Command();
        clone.fromJson(object.toJson());

        // In the translation, the milliseconds are rounded!
        dueDate.set(Calendar.MILLISECOND, 0);

        // assertNull(clone.getKey());

        assertNull(clone.getAction());
        assertNull(clone.getCancelerKey());
        assertEquals(cc.size(), clone.getCC().size());
        for (int idx=0; idx < object.getCC().size(); idx++) {
            assertEquals(object.getCC().get(idx), clone.getCC().get(idx));
        }
        assertEquals(content, clone.getContent());
        assertEquals(dueDate.getTime(), clone.getDueDate());
        assertEquals(hashTags.size(), clone.getHashTags().size());
        for (int idx=0; idx < object.getHashTags().size(); idx++) {
            assertEquals(object.getHashTags().get(idx), clone.getHashTags().get(idx));
        }
        assertEquals(metadata, clone.getMetadata());
        assertEquals(ownerKey, clone.getOwnerKey());
        assertEquals(quantity, clone.getQuantity());
        assertEquals(rawCommandId, clone.getRawCommandId());
        assertEquals(source, clone.getSource());
        assertEquals(State.opened, clone.getState());
    }

    @Test
    public void testJsonCommandsV() {
        //
        // User update for an existing object (lowest)
        //
        Command object = new Command();

        object.setKey(key);

        object.setAction(action);
        object.setCancelerKey(cancelerKey);
        object.setCC(cc);
        object.setContent(content);
        object.setDueDate(dueDate.getTime());
        object.setHashTags(hashTags);
        object.setMetadata(metadata);
        object.setOwnerKey(ownerKey);
        object.setQuantity(quantity);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        Command clone = new Command();
        clone.fromJson(object.toJson());

        // In the translation, the milliseconds are rounded!
        dueDate.set(Calendar.MILLISECOND, 0);

        assertEquals(key, clone.getKey());

        assertNull(clone.getAction());
        assertNull(clone.getCancelerKey());
        assertEquals(cc.size(), clone.getCC().size());
        for (int idx=0; idx < object.getCC().size(); idx++) {
            assertEquals(object.getCC().get(idx), clone.getCC().get(idx));
        }
        assertEquals(content, clone.getContent());
        assertEquals(dueDate.getTime(), clone.getDueDate());
        assertEquals(hashTags.size(), clone.getHashTags().size());
        for (int idx=0; idx < object.getHashTags().size(); idx++) {
            assertEquals(object.getHashTags().get(idx), clone.getHashTags().get(idx));
        }
        assertEquals(metadata, clone.getMetadata());
        assertNull(clone.getOwnerKey());
        assertEquals(quantity, clone.getQuantity());
        assertNull(clone.getRawCommandId());
        assertNull(clone.getSource());
        assertEquals(State.opened, clone.getState());
    }

    @Test
    public void testJsonCommandsVI() {
        //
        // User update for a new object (lowest) with wrong due date
        //
        Command object = new Command();
        object.setDueDate(dueDate.getTime());

        JsonObject json = new GenericJsonObject();
        json.put(Command.DUE_DATE, "corrupted date");

        object.fromJson(json);
        assertNull(object.getDueDate());
    }

    @Test
    public void testJsonCommandsVII() {
        //
        // User update for a new object (lowest) without state value
        //
        Command object = new Command();
        assertEquals(State.opened, object.getState());

        object.fromJson(new GenericJsonObject(), true, false);
        assertEquals(State.opened, object.getState());
    }

    @Test
    public void testJsonCommandsVIII() {
        //
        // User update for a new object (lowest) with commands to add & remove series of values
        //
        Command object = new Command();
        object.addCoordinate("1");
        object.addHashTag("1");

        JsonObject json = new GenericJsonObject();
        JsonArray add = new GenericJsonArray();
        JsonArray remove = new GenericJsonArray();
        add.add("2");
        remove.add("1");
        json.put(Command.CC_ADD, add);
        json.put(Command.CC_REMOVE, remove);
        json.put(Command.HASH_TAGS_ADD, add);
        json.put(Command.HASH_TAGS_REMOVE, remove);

        object.fromJson(json);
        assertEquals(1, object.getCC().size());
        assertEquals("2", object.getCC().get(0));
        assertEquals(1, object.getHashTags().size());
        assertEquals("2", object.getHashTags().get(0));
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
    public void testResetCC() {
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
        Command object = new Command();
        object.addHashTag("a");
        assertEquals("#a", object.getSerializedHashTags(defaultValue));
    }

    @Test
    public void testtoJsonI() {
        Command object = new Command();
        object.resetLists();
        object.toJson();
    }
}
