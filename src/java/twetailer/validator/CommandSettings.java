package twetailer.validator;

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
        help,
        locale,
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

    public enum Action {
        cancel,
        close,
        confirm,
        decline,
        demand,
        help,
        list,
        propose,
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


    public enum State {
        open,
        invalid,
        published,
        confirmed,
        settled,
        closed,
        canceled
    }

    private static Map<Locale, JsonObject> localizedStates = new HashMap<Locale, JsonObject>();

    /**
     * Loads the labels for the command states for the specified locale
     * @param locale Used to access the localized resource bundle
     * @return A JsonObject with the localized labels, one label per state
     */
    public static JsonObject getStates(Locale locale) {
        if (!localizedStates.containsKey(locale)) {
            JsonObject states = new GenericJsonObject();
            for(State state: State.values()) {
                states.put(
                        state.toString(),
                        LabelExtractor.get("command_state_" + state.toString(), locale)
                );
            }
            localizedStates.put(locale, states);
        }
        return localizedStates.get(locale);
    }

    public final static String HELP_INTRODUCTION_MESSAGE_ID = "help_introduction";
    protected final static String HELP_KEYWORD_LIST_ID = "help_keyword_list";
    protected final static String HELP_KEYWORD_EQUIVALENTS_PREFIX = "help_keyword_equivalents_";
    public final static String HELP_DEFINITION_PREFIX_PREFIX = "help_definition_prefix_";
    public final static String HELP_DEFINITION_ACTION_PREFIX = "help_definition_action_";
    public final static String HELP_DEFINITION_KEYWORD_PREFIX = "help_definition_keyword_";

    private static Map<Locale, JsonObject> localizedHelpKeywords = new HashMap<Locale, JsonObject>();

    /**
     * Loads the list of registered labels and the list of keyword list themselves for the specified locale
     * @param locale Used to access the localized resource bundle
     * @return A JsonObject with the localized labels, one JsonArray of values per defined help keyword
     */
   public static JsonObject getHelpKeywords(Locale locale) {
        JsonObject helpKeywords = localizedHelpKeywords.get(locale);
        if (helpKeywords == null) {
            helpKeywords = new GenericJsonObject();
            String keywordList = LabelExtractor.get(HELP_KEYWORD_LIST_ID , locale);
            String[] keywords = keywordList.split(",");
            for(String keyword: keywords) {
                helpKeywords.put(keyword, LabelExtractor.get(HELP_KEYWORD_EQUIVALENTS_PREFIX + keyword, locale));
            }
            localizedHelpKeywords.put(locale, helpKeywords);
        }
        return helpKeywords;
    }

   /**
    * Verify if the given value is an equivalent of the expected value
    *
    * @param equivalentList list of localized keywords with their equivalents
    * @param expectedValue command action to consider for the match
    * @param actualValue value submitted for a command action
    * @return <code>true</code> if both values match, <code>false</code> otherwise.
    */
   public static boolean isEquivalentTo(JsonObject equivalentList, String expectedValue, String actualValue) {
       JsonArray acceptedValues = equivalentList.getJsonArray(expectedValue);
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
}
