package com.twetailer.settings;

import java.util.Locale;

import org.domderrien.jsontools.JsonException;
import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.JsonParser;

public class CommandSettings {
    
    public enum Prefix {
        action,
        expiration,
        location,
        quantity,
        reference,
        range,
        tags
    }

    public static JsonObject getPrefixes(Locale locale) throws JsonException {
        String definition = "{}";
        if (Locale.US.equals(locale)) {
            // TODO: load the definition from a localized resource bundle
            definition = "{" +
                "'" + Prefix.action + "':['action:','!']," +
                "'" + Prefix.expiration + "':['expires:','exp:']," +
                "'" + Prefix.location + "':['locale:','loc:']," +
                "'" + Prefix.quantity + "':['quantity:','qty:']," +
                "'" + Prefix.reference + "':['reference:','ref:']," +
                "'" + Prefix.range + "':['range:','rng:']," +
                "'" + Prefix.tags + "':['tags:','']" +
                "}";
        }
        else if (Locale.FRENCH.equals(locale)) {
            // TODO: load the definition from a localized resource bundle
            definition = "{" +
                "'" + Prefix.action + "':['action:','!']," +
                "'" + Prefix.expiration + "':['expire:','exp:']," +
                "'" + Prefix.location + "':['localisation:','loc:']," +
                "'" + Prefix.quantity + "':['quantité:','qté:']," +
                "'" + Prefix.reference + "':['réference:','réf:']," +
                "'" + Prefix.range + "':['distance:','dst:']," +
                "'" + Prefix.tags + "':['mots-clés:','']" +
                "}";
        }
        JsonObject prefixes = (new JsonParser(definition)).getJsonObject();
        return prefixes;
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

    public static JsonObject getActions(Locale locale) throws JsonException {
        String definition = "{}";
        if (Locale.US.equals(locale)) {
            // TODO: load the definition from a localized resource bundle
            definition = "{" +
                "'" + Action.cancel + "':['cancel','can']," +
                "'" + Action.close + "':['close','clo']," +
                "'" + Action.confirm + "':['confirm','con']," +
                "'" + Action.decline + "':['decline:','dec']," +
                "'" + Action.demand + "':['demand','dem']," +
                "'" + Action.help + "':['help','?']," +
                "'" + Action.list + "':['list','lis']," +
                "'" + Action.propose + "':['propose','pro']," +
                "'" + Action.shop + "':['shop','sho']," +
                "'" + Action.supply + "':['supply','sup']," +
                "'" + Action.wish + "':['wish','wis']," +
                "'" + Action.www + "':['www','web']" +
                "}";
        }
        else if (Locale.FRENCH.equals(locale)) {
            // TODO: load the definition from a localized resource bundle
            definition = "{" +
                "'" + Action.cancel + "':['annule','anu']," +
                "'" + Action.close + "':['ferme','fer']," +
                "'" + Action.confirm + "':['confirme','con']," +
                "'" + Action.decline + "':['décline','déc']," +
                "'" + Action.demand + "':['demande','dem']," +
                "'" + Action.list + "':['aide','?']," +
                "'" + Action.list + "':['liste','lis']," +
                "'" + Action.propose + "':['propose','pro']," +
                "'" + Action.shop + "':['magazine','mag']," +
                "'" + Action.supply + "':['fournit','fou']," +
                "'" + Action.wish + "':['voeux','voe']," +
                "'" + Action.www + "':['www','web']" +
                "}";
        }
        JsonObject actions = (new JsonParser(definition)).getJsonObject();
        return actions;
    }
}