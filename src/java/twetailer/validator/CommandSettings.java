package twetailer.validator;

import java.text.Collator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

/**
 * Define various enumerations and methods used to understand
 * and process the text-based commands.
 *
 * @author Dom Derrien
 */
public class CommandSettings {

    public enum Prefix {
        action,
        address,
        cc,
        dueDate,
        expiration,
        hash,
        help,
        locale,
        metadata,
        name,
        phoneNumber,
        pointOfView,
        price,
        proposal,
        quantity,
        reference,
        range,
        state,
        store,
        tags,
        total
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
                // Extraction
                JsonArray equivalents = getCleanKeywords(ResourceFileId.master, "cl_prefix_" + prefix.toString(), locale);
                // Caching
                prefixes.put(
                        prefix.toString(),
                        equivalents
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
        delete,
        demand,
        help,
        language,
        list,
        propose,
        supply,
        wish,
        www
    }

    private static Map<Locale, JsonObject> localizedActions = new HashMap<Locale, JsonObject>();

    /**
     * Loads the labels for the action commands for the specified locale
     *
     * @param locale Used to access the localized resource bundle
     * @return A JsonObject with the localized labels, one JsonArray of values per defined action
     */
    public static JsonObject getActions(Locale locale) {
        if (!localizedActions.containsKey(locale)) {
            JsonObject actions = new GenericJsonObject();
            for(Action action: Action.values()) {
                // Extraction
                JsonArray equivalents = getCleanKeywords(ResourceFileId.master, "cl_action_" + action.toString(), locale);
                // Caching
                actions.put(
                        action.toString(),
                        equivalents
                );
            }
            localizedActions.put(locale, actions);
        }
        return localizedActions.get(locale);
    }


    public enum State {
        opened,
        invalid,
        published,
        confirmed,
        closed,
        declined,
        cancelled,
        markedForDeletion
    }

    private static Map<Locale, JsonObject> localizedStates = new HashMap<Locale, JsonObject>();

    /**
     * Loads the labels for the command states for the specified locale
     *
     * @param locale Used to access the localized resource bundle
     * @return A JsonObject with the localized labels, one label per state
     */
    public static JsonObject getStates(Locale locale) {
        if (!localizedStates.containsKey(locale)) {
            JsonObject states = new GenericJsonObject();
            for(State state: State.values()) {
                states.put(
                        state.toString(),
                        LabelExtractor.get("cl_state_" + state.toString(), locale)
                );
            }
            localizedStates.put(locale, states);
        }
        return localizedStates.get(locale);
    }

    protected final static String HELP_KEYWORD_LIST_ID = "help_keyword_list";
    protected final static String HELP_KEYWORD_EQUIVALENTS_PREFIX = "help_keyword_equivalents_";
    public final static String HELP_INTRODUCTION_MESSAGE_ID = "_introduction_";

    protected static Map<Locale, JsonObject> localizedHelpKeywords = new HashMap<Locale, JsonObject>();

    /**
     * Loads the list of registered labels and the list of keyword list themselves for the specified locale
     *
     * @param locale Used to access the localized resource bundle
     * @return A JsonObject with the localized labels, one JsonArray of values per defined help keyword
     */
   public static JsonObject getHelpKeywords(Locale locale) {
        JsonObject helpKeywords = localizedHelpKeywords.get(locale);
        if (helpKeywords == null) {
            helpKeywords = new GenericJsonObject();
            // Extraction
            String[] keywords = LabelExtractor.get(HELP_KEYWORD_LIST_ID , locale).split(",");
            for(String keyword: keywords) {
                keyword = keyword.trim();
                // Extraction
                JsonArray equivalents = getCleanKeywords(ResourceFileId.master, HELP_KEYWORD_EQUIVALENTS_PREFIX + keyword, locale);
                // Caching
                helpKeywords.put(
                        keyword,
                        equivalents
                );
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
    * @param collator for locale-dependent comparisons
    * @return <code>true</code> if both values match, <code>false</code> otherwise.
    */
   public static boolean isEquivalentTo(JsonObject equivalentList, String expectedValue, String actualValue, Collator collator) {
       JsonArray acceptedValues = equivalentList.getJsonArray(expectedValue);
       int acceptedValueNb = acceptedValues.size();
       int acceptedValueIdx = 0;
       while (acceptedValueIdx < acceptedValueNb) {
           // String acceptedValue = LocaleValidator.toUnicode(acceptedValues.getString(acceptedValueIdx));
           String acceptedValue = acceptedValues.getString(acceptedValueIdx);
           if (collator != null && collator.compare(acceptedValue, actualValue) == 0 ||
               collator == null && acceptedValue.equals(actualValue)) {
           // if (acceptedValue.equals(actualValue)) {
               return true;
           }
           acceptedValueIdx++;
       }
       return false;
   }

   /**
    * Helper getting a list of comma-separated keywords from a TMX and return them trimed in a JsonArray
    *
    * @param fileId Identifier of the TMX file to process
    * @param tmxKey TMX entry key
    * @param locale Used to access the localized resource bundle
    * @return Clean list of keywords
    */
   protected static JsonArray getCleanKeywords(ResourceFileId fileId, String tmxKey, Locale locale) {
       // Extraction
       JsonArray equivalents = new GenericJsonArray(
               LabelExtractor.get(fileId, tmxKey, locale).split(",")
       );
       // Clean-up
       int idx = equivalents.size();
       while (0 < idx) {
           --idx;
           String equivalent = equivalents.getString(idx);
           equivalents.set(idx, equivalent.trim());
       }
       return equivalents;
   }
}
