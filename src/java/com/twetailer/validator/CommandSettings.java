package com.twetailer.validator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class CommandSettings {
    
    public enum Prefix {
        action,
        expiration,
        location,
        quantity,
        reference,
        range,
        state,
        tags
    }
    
    private static Map<Locale, JsonObject> localizedPrefixes = new HashMap<Locale, JsonObject>();

    /**
     * Loads the labels for the attribute prefixes for the specified locale
     * 
     * @param locale Used to access the localized resource bundle
     * @return A JsonObject with the localized labels, one JsonArray of values per defined prefix
     */
    public static JsonObject getPrefixes(Locale locale) {
        if (!localizedPrefixes.containsKey(locale)) {
            JsonObject prefixes = new GenericJsonObject();
            for(Prefix prefix: Prefix.values()) {
                prefixes.put(
                        prefix.toString(),
                        new GenericJsonArray(
                                LabelExtractor.get("cmdLine_prefix_" + prefix.toString(), locale).split(",")
                        )
                );
            }
            localizedPrefixes.put(locale, prefixes);
        }
        return localizedPrefixes.get(locale);
    }
    
    /**
     * Verify if the given value matches the given command action
     * 
     * @param expectedPrefix prefix to consider for the match
     * @param actualValue value submitted for a command prefix
     * @param locale Identifies the language to consider
     * @return <code>true</code> if both values match, <code>false</code> otherwise.
     */
    public static boolean isPrefix(CommandSettings.Prefix expectedPrefix, String actualValue, Locale locale) {
        return isPrefix(getPrefixes(locale), expectedPrefix, actualValue);
    }
    
    /**
     * Verify if the given value matches the given command action
     * 
     * @param supportedPrefixes list of localized action labels
     * @param expectedPrefix prefix to consider for the match
     * @param actualValue value submitted for a command prefix
     * @return <code>true</code> if both values match, <code>false</code> otherwise.
     */
    protected static boolean isPrefix(JsonObject supportedPrefixes, CommandSettings.Prefix expectedPrefix, String actualValue) {
        JsonArray acceptedValues = supportedPrefixes.getJsonArray(expectedPrefix.toString());
        int acceptedValueNb = acceptedValues.size();
        int acceptedValueIdx = 0;
        while (acceptedValueIdx < acceptedValueNb) {
            if (acceptedValues.getString(acceptedValueIdx).equals(actualValue)) {
                return true;
            }
            acceptedValueIdx++;
        }
        return false;
    }

    public enum Action {
        cancel,
        close,
        confirm,
        decline,
        demand,
        help,
        list,
        propose,
        shop,
        supply,
        wish,
        www
    }

    private static Map<Locale, JsonObject> localizedActions = new HashMap<Locale, JsonObject>();

    /**
     * Loads the labels for the action commands for the specified locale
     * @param locale Used to access the localized resource bundle
     * @return A JsonObject with the localized labels, one JsonArray of values per defined action
     */
    public static JsonObject getActions(Locale locale) {
        if (!localizedActions.containsKey(locale)) {
            JsonObject actions = new GenericJsonObject();
            for(Action action: Action.values()) {
                actions.put(
                        action.toString(),
                        new GenericJsonArray(
                                LabelExtractor.get("cmdLine_action_" + action.toString(), locale).split(",")
                        )
                );
            }
            localizedActions.put(locale, actions);
        }
        return localizedActions.get(locale);
    }
    
    /**
     * Verify if the given value matches the given command action
     * 
     * @param expectedAction command action to consider for the match
     * @param actualValue value submitted for a command action
     * @param locale Identifies the language to consider
     * @return <code>true</code> if both values match, <code>false</code> otherwise.
     */
    public static boolean isAction(CommandSettings.Action expectedAction, String actualValue, Locale locale) {
        return isAction(getActions(locale), expectedAction, actualValue);
    }
    
    /**
     * Verify if the given value matches the given command action
     * 
     * @param supportedActions list of localized action labels
     * @param expectedAction command action to consider for the match
     * @param actualValue value submitted for a command action
     * @return <code>true</code> if both values match, <code>false</code> otherwise.
     */
    protected static boolean isAction(JsonObject supportedActions, CommandSettings.Action expectedAction, String actualValue) {
        JsonArray acceptedValues = supportedActions.getJsonArray(expectedAction.toString());
        int acceptedValueNb = acceptedValues.size();
        int acceptedValueIdx = 0;
        while (acceptedValueIdx < acceptedValueNb) {
            if (acceptedValues.getString(acceptedValueIdx).equals(actualValue)) {
                return true;
            }
            acceptedValueIdx++;
        }
        return false;
    }

    public enum State {
        open,
        invalid,
        published,
        forwarded,
        proposed,
        confirmed,
        closed,
        canceled
    }

    /**
     * Loads the labels for the command states for the specified locale
     * @param locale Used to access the localized resource bundle
     * @return A JsonObject with the localized labels, one label per state
     */
    public static JsonObject getStates(Locale locale) {
        if (!localizedActions.containsKey(locale)) {
            JsonObject actions = new GenericJsonObject();
            for(Action action: Action.values()) {
                actions.put(
                        action.toString(),
                        LabelExtractor.get("command_state_" + action.toString(), locale)
                );
            }
            localizedActions.put(locale, actions);
        }
        return localizedActions.get(locale);
    }
}