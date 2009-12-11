package twetailer.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.HashMap;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import domderrien.i18n.MockLabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestCommandSettings {

    @Before
    public void setUp() {
        CommandSettings.localizedHelpKeywords = new HashMap<Locale, JsonObject>();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() {
        new CommandSettings();
    }

    @Test
    public void testGetPrefixes_EN() {
        JsonObject prefixes = CommandSettings.getPrefixes(Locale.ENGLISH);
        for(Prefix prefix: Prefix.values()) {
            assertTrue("The prefix definition for '" + prefix + "' is not in the resource bundle", prefixes.containsKey(prefix.toString()));
            assertNotNull(prefixes.getJsonArray(prefix.toString()));
            assertNotSame(0, prefixes.getJsonArray(prefix.toString()).size());
        }
        assertEquals(prefixes, CommandSettings.getPrefixes(Locale.ENGLISH));
    }

    @Test
    public void testGetPrefixes_FR() {
        JsonObject prefixes = CommandSettings.getPrefixes(Locale.FRENCH);
        for(Prefix prefix: Prefix.values()) {
            assertTrue("The prefix definition for '" + prefix + "' is not in the resource bundle", prefixes.containsKey(prefix.toString()));
            assertNotNull(prefixes.getJsonArray(prefix.toString()));
            assertNotSame(0, prefixes.getJsonArray(prefix.toString()).size());
        }
    }

    @Test
    public void testGetActions_EN() {
        JsonObject actions = CommandSettings.getActions(Locale.ENGLISH);
        for(Action action: Action.values()) {
            assertTrue("The action value for '" + action + "' is not in the resource bundle", actions.containsKey(action.toString()));
            assertNotNull(actions.getJsonArray(action.toString()));
            assertNotSame(0, actions.getJsonArray(action.toString()).size());
        }
        assertEquals(actions, CommandSettings.getActions(Locale.ENGLISH));
    }

    @Test
    public void testGetActions_FR() {
        JsonObject actions = CommandSettings.getActions(Locale.FRENCH);
        for(Action action: Action.values()) {
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
        actions.put(Action.cancel.toString(), equivalents);

        Collator collator = Collator.getInstance(Locale.US);
        collator.setStrength(Collator.PRIMARY);

        assertTrue(CommandSettings.isEquivalentTo(actions, Action.cancel.toString(), "cancel", collator));
        assertTrue(CommandSettings.isEquivalentTo(actions, Action.cancel.toString(), "delete", collator));
        assertFalse(CommandSettings.isEquivalentTo(actions, Action.cancel.toString(), "destroy", collator));
    }

    @Test
    public void testVariousActionsII() {
        JsonArray equivalents = new GenericJsonArray();
        equivalents.add("help");
        equivalents.add("?");
        JsonObject actions = new GenericJsonObject();
        actions.put(Action.help.toString(), equivalents);

        Collator collator = Collator.getInstance(Locale.US);
        collator.setStrength(Collator.PRIMARY);

        assertTrue(CommandSettings.isEquivalentTo(actions, Action.help.toString(), "help", collator));
        assertTrue(CommandSettings.isEquivalentTo(actions, Action.help.toString(), "?", collator));
        assertFalse(CommandSettings.isEquivalentTo(actions, Action.help.toString(), "sos", collator));
    }

    @Test
    public void testGetStates_EN() {
        JsonObject states = CommandSettings.getStates(Locale.ENGLISH);
        for(State state: State.values()) {
            assertTrue("The state value for '" + state + "' is not in the resource bundle", states.containsKey(state.toString()));
            assertNotNull(states.getString(state.toString()));
            assertNotSame(0, states.getString(state.toString()).length());
        }
        assertEquals(states, CommandSettings.getStates(Locale.ENGLISH));
    }

    @Test
    public void testGetStates_FR() {
        JsonObject states = CommandSettings.getStates(Locale.FRENCH);
        for(State state: State.values()) {
            assertTrue("The state value for '" + state + "' is not in the resource bundle", states.containsKey(state.toString()));
            assertNotNull(states.getString(state.toString()));
            assertNotSame(0, states.getString(state.toString()).length());
        }
    }

    @Test
    public void testGetHelpKeywords() {
        Locale locale = null;
        ResourceFileId fileId = ResourceFileId.master;
        MockLabelExtractor.setup(fileId, new Object[][]{
            {CommandSettings.HELP_KEYWORD_LIST_ID, "one,two"},
            {CommandSettings.HELP_KEYWORD_EQUIVALENTS_PREFIX + "one", "one help message"},
            {CommandSettings.HELP_KEYWORD_EQUIVALENTS_PREFIX + "two", "two help message"}
        }, locale);

        JsonObject helpKeywords = CommandSettings.getHelpKeywords(locale);
        assertNotNull(helpKeywords);
        assertNotSame(0, helpKeywords.size());
        assertEquals(helpKeywords, CommandSettings.getHelpKeywords(locale));

        MockLabelExtractor.cleanup(fileId, locale);
    }

    @Test
    public void testHandlingEquivalentListI() {
        Locale locale = null;
        ResourceFileId fileId = ResourceFileId.master;
        MockLabelExtractor.setup(fileId, new Object[][]{
            {"entry", "one , \n TWO, 333.3333E+333 \t\r\n,\r\n\t four"}
        }, locale);

        JsonArray keywords = CommandSettings.getCleanKeywords(fileId, "entry", locale);
        assertNotNull(keywords);
        assertEquals(4, keywords.size());
        assertEquals("one", keywords.getString(0));
        assertEquals("TWO", keywords.getString(1));
        assertEquals("333.3333E+333", keywords.getString(2));
        assertEquals("four", keywords.getString(3));
    }

    @Test
    public void testHandlingEquivalentListII() {
        Locale locale = null;
        ResourceFileId fileId = ResourceFileId.master;
        MockLabelExtractor.setup(fileId, new Object[][]{
            {CommandSettings.HELP_KEYWORD_LIST_ID, " one , \t two \r\n, three "}, // With additional separator
            {CommandSettings.HELP_KEYWORD_EQUIVALENTS_PREFIX + "one", "one , \n ONE, 1 \t"},
            {CommandSettings.HELP_KEYWORD_EQUIVALENTS_PREFIX + "two", "2,TWO,two"},
            {CommandSettings.HELP_KEYWORD_EQUIVALENTS_PREFIX + "three", "three     ,     3\t\t\t,\t\t\tTHREE"}
        }, locale);

        JsonObject helpKeywords = CommandSettings.getHelpKeywords(locale);

        assertNotNull(helpKeywords);
        assertNotSame(0, helpKeywords.size());
        assertEquals(3, helpKeywords.size());
        assertEquals(3, helpKeywords.getJsonArray("one").size());
        assertEquals(3, helpKeywords.getJsonArray("two").size());
        assertEquals(3, helpKeywords.getJsonArray("three").size());
        assertEquals("one", helpKeywords.getJsonArray("one").getString(0));
        assertEquals("ONE", helpKeywords.getJsonArray("one").getString(1));
        assertEquals("1", helpKeywords.getJsonArray("one").getString(2));

        MockLabelExtractor.cleanup(fileId, locale);
    }
}
