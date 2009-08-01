package com.twetailer.settings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Locale;

import domderrien.jsontools.JsonObject;
import org.junit.Test;

import com.twetailer.validator.CommandSettings;

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
        assertTrue(CommandSettings.isAction(CommandSettings.Action.cancel, "cancel", Locale.ENGLISH));
        assertTrue(CommandSettings.isAction(CommandSettings.Action.cancel, "delete", Locale.ENGLISH));
        assertFalse(CommandSettings.isAction(CommandSettings.Action.cancel, "destroy", Locale.ENGLISH));
    }

    @Test
    public void testVariousActionsII() {
        assertTrue(CommandSettings.isAction(CommandSettings.Action.help, "help", Locale.ENGLISH));
        assertTrue(CommandSettings.isAction(CommandSettings.Action.help, "?", Locale.ENGLISH));
        assertFalse(CommandSettings.isAction(CommandSettings.Action.help, "sos", Locale.ENGLISH));
    }
}
