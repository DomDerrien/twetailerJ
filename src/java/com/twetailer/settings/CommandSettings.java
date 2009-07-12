package com.twetailer.settings;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.domderrien.i18n.LabelExtractor;
import org.domderrien.jsontools.GenericJsonArray;
import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonObject;

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
                                LabelExtractor.get("ta_prefix_" + prefix.toString(), locale).split(",")
                        )
                );
            }
            localizedPrefixes.put(locale, prefixes);
        }
        return localizedPrefixes.get(locale);
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
                                LabelExtractor.get("ta_action_" + action.toString(), locale).split(",")
                        )
                );
            }
            localizedActions.put(locale, actions);
        }
        return localizedActions.get(locale);
    }
}