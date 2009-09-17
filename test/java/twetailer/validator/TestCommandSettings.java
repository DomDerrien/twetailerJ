package twetailer.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import domderrien.i18n.MockLabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestCommandSettings {

    @Test
    public void testConstructor() {
        new CommandSettings();
    }

    @Test
    public void testGetPrefixes_EN() {
        JsonObject prefixes = CommandSettings.getPrefixes(Locale.ENGLISH);
        for(CommandSettings.Prefix prefix: CommandSettings.Prefix.values()) {
            assertTrue("The prefix definition for '" + prefix + "' is not in the resource bundle", prefixes.containsKey(prefix.toString()));
            assertNotNull(prefixes.getJsonArray(prefix.toString()));
            assertNotSame(0, prefixes.getJsonArray(prefix.toString()).size());
        }
        assertEquals(prefixes, CommandSettings.getPrefixes(Locale.ENGLISH));
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
        assertEquals(actions, CommandSettings.getActions(Locale.ENGLISH));
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

    @Test
    public void testGetStates_EN() {
        JsonObject states = CommandSettings.getStates(Locale.ENGLISH);
        for(CommandSettings.State state: CommandSettings.State.values()) {
            assertTrue("The state value for '" + state + "' is not in the resource bundle", states.containsKey(state.toString()));
            assertNotNull(states.getString(state.toString()));
            assertNotSame(0, states.getString(state.toString()).length());
        }
        assertEquals(states, CommandSettings.getStates(Locale.ENGLISH));
    }

    @Test
    public void testGetStates_FR() {
        JsonObject states = CommandSettings.getStates(Locale.FRENCH);
        for(CommandSettings.State state: CommandSettings.State.values()) {
            assertTrue("The state value for '" + state + "' is not in the resource bundle", states.containsKey(state.toString()));
            assertNotNull(states.getString(state.toString()));
            assertNotSame(0, states.getString(state.toString()).length());
        }
    }

    @Test
    public void testGetHelpKeywords() {
        MockLabelExtractor.init(new Object[][]{
            {CommandSettings.HELP_KEYWORD_LIST_ID, "one,two"},
            {CommandSettings.HELP_KEYWORD_EQUIVALENTS_PREFIX + "one", "one help message"},
            {CommandSettings.HELP_KEYWORD_EQUIVALENTS_PREFIX + "two", "two help message"}
        });

        JsonObject helpKeywords = CommandSettings.getHelpKeywords(null);
        assertNotNull(helpKeywords);
        assertNotSame(0, helpKeywords.size());
        assertEquals(helpKeywords, CommandSettings.getHelpKeywords(null));

        MockLabelExtractor.close();
    }
}
