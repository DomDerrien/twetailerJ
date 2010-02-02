package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToEmitter;

import java.text.Collator;
import java.util.Locale;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.RawCommand;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;

public class HelpCommandProcessor {
    /**
     * Use the keyword given as an argument to select an Help text among {prefixes, actions, registered keywords}.
     * If the extracted keyword does not match anything, the default Help text is tweeted.
     *
     * @param rawCommand raw command with emitter coordinates
     * @param prefixes List of localized prefixes for the orginator's locale
     * @param actions List of location actions for the originator's locale
     * @param arguments Sequence submitted in addition to the question mark (?) or to the help command
     * @param locale Originator's locale
     * @param collator for locale-dependent comparison
     *
     * @throws DataSourceException If sending the help message to the originator fails
     * @throws ClientException If the communication back with the user fails
     */
    public static void processHelpCommand(RawCommand rawCommand, String arguments, Locale locale, Collator collator) throws DataSourceException, ClientException {
        // Extract the first keyword
        int limit = arguments.length();
        String keyword = "";
        for(int i = 0; i < limit; i++) {
            char current = arguments.charAt(i);
            if (current == ' ' || current == '\t' || current == ':') {
                if (0 < keyword.length()) {
                    break;
                }
            }
            else {
                keyword += current;
            }
        }
        // Tweet the default help message if there's no keyword
        if (keyword.length() == 0) {
            communicateToEmitter(
                    rawCommand,
                    new String[] { LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, locale) },
                    locale
            );
            return;
        }
        // Try to load the message from the cache
        String message = (String) CommandProcessor.settingsOperations.getFromCache(keyword + locale.toString());
        // Check if the keyword is a prefix
        if (message == null) {
            JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
            for (Prefix prefix: Prefix.values()) {
                if (CommandSettings.isEquivalentTo(prefixes, prefix.toString(), keyword, collator)) {
                    String key = prefix.toString();
                    key = key.substring(0, 1).toUpperCase(locale) + key.substring(1).toLowerCase(locale);
                    message = LabelExtractor.get(ResourceFileId.second, key, locale);
                    break;
                }
            }
        }
        // Check if the keyword is an action
        if (message == null) {
            JsonObject actions = CommandLineParser.localizedActions.get(locale);
            for (Action action: Action.values()) {
                if (CommandSettings.isEquivalentTo(actions, action.toString(), keyword, collator)) {
                    String key = action.toString();
                    key = key.substring(0, 1).toUpperCase(locale) + key.substring(1).toLowerCase(locale);
                    message = LabelExtractor.get(ResourceFileId.second, key, locale);
                    break;
                }
            }
        }
        // Check if the keyword is a state
        if (message == null) {
            JsonObject states = CommandLineParser.localizedStates.get(locale);
            for (State state: State.values()) {
                if (collator.compare(states.getString(state.toString()), keyword) == 0) {
                // if (states.getString(state.toString()).equals(keyword)) {
                    String key = state.toString();
                    key = key.substring(0, 1).toUpperCase(locale) + key.substring(1).toLowerCase(locale);
                    message = LabelExtractor.get(ResourceFileId.second, key, locale);
                    break;
                }
            }
        }
        // Check of the keyword is one registered
        if (message == null) {
            JsonObject helpKeywords = CommandLineParser.localizedHelpKeywords.get(locale);
            for (String helpKeyword: helpKeywords.getMap().keySet()) {
                JsonArray equivalents = helpKeywords.getJsonArray(helpKeyword);
                for (int i = 0; i < equivalents.size(); i++) {
                    if (collator.compare(equivalents.getString(i), keyword) == 0) {
                    // if (equivalents.getString(i).equals(keyword)) {
                        message = LabelExtractor.get(ResourceFileId.second, helpKeyword, locale);
                        break;
                    }
                }
                if (message != null) {
                    break;
                }
            }
        }
        // Tweet the default help message if the given keyword is not recognized
        if (message == null) {
            message = LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, locale);
        }
        // TODO: save the match into the cache for future queries
        CommandProcessor.settingsOperations.setInCache(keyword + locale.toString(), message);
        communicateToEmitter(
                rawCommand,
                new String[] { message },
                locale
        );
    }
}
