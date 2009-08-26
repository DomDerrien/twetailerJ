package twetailer.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Locale;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import org.junit.Test;

public class TestCommandSettings {

    @Test
    public void testGetPrefixes_EN() {
        JsonObject prefixes = CommandSettings.getPrefixes(Locale.ENGLISH);
        for(CommandSettings.Prefix prefix: CommandSettings.Prefix.values()) {
            assertTrue("The prefix definition for '" + prefix + "' is not in the resource bundle", prefixes.containsKey(prefix.toString()));
            assertNotNull(prefixes.getJsonArray(prefix.toString()));
            assertNotSame(0, prefixes.getJsonArray(prefix.toString()).size());
        }
    }

    @Test
    public void testGetPrefixes_FR() {
        JsonObject prefixes = CommandSettings.getPrefixes(Locale.FRENCH);
        for(CommandSettings.Prefix prefix: CommandSettings.Prefix.values()) {
            assertTrue("The prefix definition for '" + prefix + "' is not in the resource bundle", prefixes.containsKey(prefix.toString()));
            assertNotNull(prefixes.getJsonArray(prefix.toString()));
            assertNotSame(0, prefixes.getJsonArray(prefix.toString()).size());
        }
    }

    @Test
    public void testGetActions_EN() {
        JsonObject actions = CommandSettings.getActions(Locale.ENGLISH);
        for(CommandSettings.Action action: CommandSettings.Action.values()) {
            assertTrue("The action value for '" + action + "' is not in the resource bundle", actions.containsKey(action.toString()));
            assertNotNull(actions.getJsonArray(action.toString()));
            assertNotSame(0, actions.getJsonArray(action.toString()).size());
        }
    }

    @Test
    public void testGetActions_FR() {
        JsonObject actions = CommandSettings.getActions(Locale.FRENCH);
        for(CommandSettings.Action action: CommandSettings.Action.values()) {
            assertTrue("The action value for '" + action + "' is not in the resource bundle", actions.containsKey(action.toString()));
            assertNotNull(actions.getJsonArray(action.toString()));
            assertNotSame(0, actions.getJsonArray(action.toString()).size());
        }
    }

    @Test
    public void testVariousActionsI() {
        JsonArray equivalents = new GenericJsonArray();
        equivalents.add("cancel");
        equivalents.add("delete");
        equivalents.add("stop");
        JsonObject actions = new GenericJsonObject();
        actions.put(CommandSettings.Action.cancel.toString(), equivalents);

        assertTrue(CommandSettings.isEquivalentTo(actions, CommandSettings.Action.cancel.toString(), "cancel"));
        assertTrue(CommandSettings.isEquivalentTo(actions, CommandSettings.Action.cancel.toString(), "delete"));
        assertFalse(CommandSettings.isEquivalentTo(actions, CommandSettings.Action.cancel.toString(), "destroy"));
    }

    @Test
    public void testVariousActionsII() {
        JsonArray equivalents = new GenericJsonArray();
        equivalents.add("help");
        equivalents.add("?");
        JsonObject actions = new GenericJsonObject();
        actions.put(CommandSettings.Action.help.toString(), equivalents);

        assertTrue(CommandSettings.isEquivalentTo(actions, CommandSettings.Action.help.toString(), "help"));
        assertTrue(CommandSettings.isEquivalentTo(actions, CommandSettings.Action.help.toString(), "?"));
        assertFalse(CommandSettings.isEquivalentTo(actions, CommandSettings.Action.help.toString(), "sos"));
    }
}
