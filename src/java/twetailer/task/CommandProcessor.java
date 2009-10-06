package twetailer.task;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToEmitter;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings;
import twitter4j.TwitterException;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class CommandProcessor {
    private static final Logger log = Logger.getLogger(CommandProcessor.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();
    protected static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    protected static Map<Locale, JsonObject> localizedPrefixes = new HashMap<Locale, JsonObject>();
    protected static Map<Locale, JsonObject> localizedActions = new HashMap<Locale, JsonObject>();
    protected static Map<Locale, JsonObject> localizedStates = new HashMap<Locale, JsonObject>();
    protected static Map<Locale, JsonObject> localizedHelpKeywords = new HashMap<Locale, JsonObject>();
    protected static Map<Locale, Map<CommandSettings.Prefix, Pattern>> localizedPatterns = new HashMap<Locale, Map<CommandSettings.Prefix, Pattern>>();

    /**
     * Load the command processor parameters for the specified locale
     *
     * @param locale user's preferred locale
     */
    protected static void loadLocalizedSettings(Locale locale) {
        JsonObject prefixes = localizedPrefixes.get(locale);
        if (prefixes == null) {
            prefixes = CommandSettings.getPrefixes(locale);
            localizedPrefixes.put(locale, prefixes);
        }

        JsonObject actions = localizedActions.get(locale);
        if (actions == null) {
            actions = CommandSettings.getActions(locale);
            localizedActions.put(locale, actions);
        }

        JsonObject states = localizedStates.get(locale);
        if (states == null) {
            states = CommandSettings.getStates(locale);
            localizedStates.put(locale, states);
        }

        JsonObject helpKeywords = localizedHelpKeywords.get(locale);
        if (helpKeywords == null) {
            helpKeywords = CommandSettings.getHelpKeywords(locale);
            localizedHelpKeywords.put(locale, helpKeywords);
        }

        Map<CommandSettings.Prefix, Pattern> patterns = localizedPatterns.get(locale);
        if (patterns == null) {
            patterns = new HashMap<CommandSettings.Prefix, Pattern>();

            preparePattern(prefixes, patterns, CommandSettings.Prefix.action, "\\s*\\w+");
            preparePattern(prefixes, patterns, CommandSettings.Prefix.expiration, "\\s*[\\d\\-]+");
            preparePattern(prefixes, patterns, CommandSettings.Prefix.help, ""); // Given keywords considered as tags
            preparePattern(prefixes, patterns, CommandSettings.Prefix.locale, "\\s*(?:\\w+(?:\\s|-|\\.)+)+(?:ca|us)");
            preparePattern(prefixes, patterns, CommandSettings.Prefix.quantity, "\\s*\\d+");
            preparePattern(prefixes, patterns, CommandSettings.Prefix.reference, "\\s*\\d+");
            preparePattern(prefixes, patterns, CommandSettings.Prefix.range, "\\s*\\d+\\s*(?:mi|km)");
            preparePattern(prefixes, patterns, CommandSettings.Prefix.state, "\\s*\\w+");
            preparePattern(prefixes, patterns, CommandSettings.Prefix.tags, "?(?:\\+\\s*|\\-\\s*|\\w\\s*)+");

            localizedPatterns.put(locale, patterns);
        }
    }

    /**
     * Builds a pattern for a regular expression which consider all characters after the third one as optional
     *
     * @param prefix character sequence to consider
     * @return Pattern for a regular expression with optional characters at the end, in form such as tes(?:t(?:e(?:r)?)?)? for the keyword tester
     */
    private static String createPatternWithOptionalEndingCharacters(String prefix) {
        if (prefix.length() == 1) {
            return "\\" + prefix; // To be sure to escape potential RegExp commands
        }
        String pattern = "";
        int prefixEnd = prefix.length();
        while(3 < prefixEnd) {
            prefixEnd --;
            pattern = "(?:" + prefix.charAt(prefixEnd) + pattern + ")?";
        }
        return prefix.subSequence(0, prefixEnd) + pattern + ":";
    }

    /**
     * Extract the localized prefix matching the keywords (long and short versions)
     * and insert the corresponding regular expression pattern in the pattern list
     *
     * @param keywords set of localized prefix labels
     * @param patterns list of registered RegEx patterns preset for the user's locale
     * @param keyword identifier of the prefix to process
     * @param expression part of the regular expression that extracts the parameters for the identified prefix
     */
    private static void preparePattern(JsonObject keywords, Map<CommandSettings.Prefix, Pattern> patterns, CommandSettings.Prefix keyword, String expression) {
        String prefix = keywords.getJsonArray(keyword.toString()).getString(0);
        String pattern = createPatternWithOptionalEndingCharacters(prefix);
        for(int i = 1; i < keywords.getJsonArray(keyword.toString()).size(); i++) {
            prefix = keywords.getJsonArray(keyword.toString()).getString(i);
            pattern += "|" + createPatternWithOptionalEndingCharacters(prefix);
        }
        patterns.put(keyword, Pattern.compile("((?:" + pattern + ")" + expression + ")", Pattern.CASE_INSENSITIVE));
    }

    /**
     * Parse the given tweet with the set of given localized prefixes
     *
     * @param message message are returned by the Twitter page
     * @param patterns list of registered RegEx patterns preset for the user's locale
     * @return JsonObject with all message attributes correctly extracted from the given message
     *
     * @throws ClientException If the query is malformed
     * @throws ParseException if a date format is invalid
     */
    public static JsonObject parseCommand (Map<CommandSettings.Prefix, Pattern> patterns, String message) throws ClientException, ParseException {
        Matcher matcher;
        boolean oneFieldOverriden = false;
        log.warning("Message to parse: " + message);
        JsonObject command = new GenericJsonObject();
        // Help
        try {
            matcher = patterns.get(CommandSettings.Prefix.help).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                message = matcher.replaceFirst("");
                // No need to continue parsing: grab the rest of the tweet that will be used for lookups in the TMX
                command.put(Command.NEED_HELP, message.trim());
                return command;
            }
        }
        catch(IllegalStateException ex) {}
        // Action
        try {
            matcher = patterns.get(CommandSettings.Prefix.action).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.ACTION, getAction(currentGroup.toLowerCase()));
                message = matcher.replaceFirst("");
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Expiration
        try {
            matcher = patterns.get(CommandSettings.Prefix.expiration).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.EXPIRATION_DATE, getDate(currentGroup));
                message = matcher.replaceFirst("");
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Locale
        try {
            matcher = patterns.get(CommandSettings.Prefix.locale).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Location.COUNTRY_CODE, getCountryCode(currentGroup).toUpperCase());
                command.put(Location.POSTAL_CODE, getPostalCode(currentGroup, command.getString(Location.COUNTRY_CODE)).toUpperCase());
                message = matcher.replaceFirst("");
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Quantity
        try {
            matcher = patterns.get(CommandSettings.Prefix.quantity).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.QUANTITY, getQuantity(currentGroup));
                message = matcher.replaceFirst("");
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Reference
        try {
            matcher = patterns.get(CommandSettings.Prefix.reference).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.REFERENCE, getQuantity(currentGroup));
                message = matcher.replaceFirst("");
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Range
        try {
            matcher = patterns.get(CommandSettings.Prefix.range).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.RANGE_UNIT, getRangeUnit(currentGroup).toLowerCase());
                command.put(Demand.RANGE, getRange(currentGroup, command.getString(Demand.RANGE_UNIT)));
                message = matcher.replaceFirst("");
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Tags
        try {
            matcher = patterns.get(CommandSettings.Prefix.tags).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.CRITERIA, new GenericJsonArray(getTags(currentGroup)));
                message = matcher.replaceFirst("");
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}

        if (!oneFieldOverriden) {
            throw new ClientException("No query field has been correctly extracted");
        }
        return command;
    }

    /**
     * Helper extracting commands
     * @param pattern Parameters extracted by a regular expression
     * @return valid command
     */
    private static String getAction(String pattern) {
        String command;
        if (pattern.charAt(0) == '!') {
            command = pattern.substring(1);
        }
        else {
            command = pattern.substring(pattern.indexOf(":") + 1);
        }
        // TODO: validate the command among a list
        return command;
    }

    /**
     * Helper extracting an expiration date
     * @param pattern Parameters extracted by a regular expression
     * @return valid expiration date
     * @throws ParseException if the date format is invalid
     */
    private static String getDate(String pattern) throws ParseException {
        // TODO: ensure the date is in the future
        String date = pattern.substring(pattern.indexOf(":") + 1).trim();
        if (date.indexOf('-') == -1) {
            String day = date.substring(date.length() - 2);
            String month = date.substring(date.length() - 4, date.length() - 2);
            String year = date.substring(0, date.length() - 4);
            date = year + "-" + month + "-" + day;
        }
        if (date.length() == 8) {
            date = "20" + date;
        }
        return date + "T23:59:59";
    }

    /**
     * Helper extracting a distance
     * @param pattern Parameters extracted by a regular expression
     * @return valid distance
     */
    private static Double getRange(String pattern, String rangeUnit) {
        // Extract the number after the prefix, taking care of excluding the part with the range unit
        return Double.parseDouble(pattern.substring(pattern.indexOf(":") + 1, pattern.length() - rangeUnit.length()).trim());
    }

    /**
     * Helper extracting distance unit
     * @param pattern Parameters extracted by a regular expression
     * @return valid distance unit
     */
    private static String getRangeUnit(String pattern) {
        // Range unit can be {km; mi}, just 2-character wide
        return pattern.substring(pattern.length() - 2);
    }

    /**
     * Helper extracting country codes
     * @param pattern Parameters extracted by a regular expression
     * @return valid country cod
     */
    private static String getCountryCode(String pattern) {
        // Range unit can be {us; ca}, just 2-character wide
        return pattern.substring(pattern.length() - 2);
    }

    /**
     * Helper extracting postal code
     * @param pattern Parameters extracted by a regular expression
     * @return valid postal code
     */
    private static String getPostalCode(String pattern, String countryCode) {
        return pattern.substring(pattern.indexOf(":") + 1, pattern.length() - countryCode.length() - 1).trim();
    }

    /**
     * Helper extracting quantities
     * @param pattern Parameters extracted by a regular expression
     * @return valid quantity
     */
    private static long getQuantity(String pattern) {
        return Long.parseLong(pattern.substring(pattern.indexOf(":") + 1).trim());
    }

    /**
     * Helper extracting tags
     * @param pattern Parameters extracted by a regular expression
     * @return tags
     */
    private static String[] getTags(String pattern) {
        String keywords = pattern;
        if (pattern.startsWith("tags:")) {
            keywords = pattern.substring("tags:".length());
        }
        return keywords.split("\\s+");
    }

    /**
     * Prepare a message to be submit Twitter
     * @param command Command to convert
     * @param location Place where the command starts
     * @param prefixes List of localized prefix labels
     * @param actions List of localized action labels
     * @return Serialized command
     */
    public static String generateTweet(Demand demand, Location location, Locale locale) {
        final String space = " ";
        StringBuilder tweet = new StringBuilder();
        JsonObject prefixes = localizedPrefixes.get(locale);
        JsonObject actions = localizedActions.get(locale);
        tweet.append(prefixes.getJsonArray(CommandSettings.Prefix.action.toString()).getString(0)).append(":").append(actions.getJsonArray(demand.getAction().toString()).getString(0)).append(space);
        if (demand.getKey() != null) {
            tweet.append(prefixes.getJsonArray(CommandSettings.Prefix.reference.toString()).getString(0)).append(":").append(demand.getKey()).append(space);
        }
        JsonObject states = localizedStates.get(locale);
        tweet.append(prefixes.getJsonArray(CommandSettings.Prefix.state.toString()).getString(0)).append(":").append(states.getString(demand.getState().toString())).append(space);
        tweet.append(prefixes.getJsonArray(CommandSettings.Prefix.expiration.toString()).getString(0)).append(":").append(DateUtils.dateToYMD(demand.getExpirationDate())).append(space);
        if (location != null && location.getPostalCode() != null && location.getCountryCode() != null) {
            tweet.append(prefixes.getJsonArray(CommandSettings.Prefix.locale.toString()).getString(0)).append(":").append(location.getPostalCode()).append(space).append(location.getCountryCode()).append(space);
        }
        tweet.append(prefixes.getJsonArray(CommandSettings.Prefix.range.toString()).getString(0)).append(":").append(demand.getRange()).append(demand.getRangeUnit()).append(space);
        tweet.append(prefixes.getJsonArray(CommandSettings.Prefix.quantity.toString()).getString(0)).append(":").append(demand.getQuantity()).append(space);
        if (0 < demand.getCriteria().size()) {
            tweet.append(prefixes.getJsonArray(CommandSettings.Prefix.tags.toString()).getString(0)).append(":");
            for (String tag: demand.getCriteria()) {
                tweet.append(tag).append(space);
            }
        }
        return tweet.toString();
    }

    /**
     * Extract commands from the tables of raw commands and acts accordingly
     *
     * @param rawCommandKey Identifier of the raw command to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws ClientException If the communication back with the user fails
     */
    public static void processRawCommands(Long rawCommandKey) throws DataSourceException, ClientException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            processRawCommands(pm, rawCommandKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Extract commands from the tables of raw commands and acts accordingly
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommandKey Identifier of the raw command to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws ClientException If the communication back with the user fails
     */
    protected static void processRawCommands(PersistenceManager pm, Long rawCommandKey) throws DataSourceException, ClientException {
        // Get the identified raw command
        RawCommand rawCommand = rawCommandOperations.getRawCommand(pm, rawCommandKey);

        // Get the record of the command emitter
        Consumer consumer = retrieveConsumer(pm, rawCommand);
        Locale senderLocale = consumer.getLocale();

        // Load the definitions for the sender locale
        loadLocalizedSettings(senderLocale);
        Map<CommandSettings.Prefix, Pattern> patterns = localizedPatterns.get(senderLocale);

        try {
            // Extract information from the tweet and process the information
            JsonObject command = parseCommand(patterns, rawCommand.getCommand());
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            processCommand(pm, consumer, rawCommand, command);
        }
        catch(Exception ex) {
            // TODO: use localized message
            ex.printStackTrace();
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get("cp_unexpected_error", new Object[] { rawCommand.getKey() }, Locale.ENGLISH)
            );
        }
    }

    protected static Consumer retrieveConsumer(PersistenceManager pm, RawCommand rawCommand) throws DataSourceException {
        if (Source.simulated.equals(rawCommand.getSource())) {
            Consumer consumer = new Consumer();
            consumer.setName(rawCommand.getEmitterId());
            return consumer;
        }
        if (Source.twitter.equals(rawCommand.getSource())) {
            return consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, rawCommand.getEmitterId(), 1).get(0);
        }
        if (Source.jabber.equals(rawCommand.getSource())) {
            return consumerOperations.getConsumers(pm, Consumer.JABBER_ID, rawCommand.getEmitterId(), 1).get(0);
        }
        if (Source.facebook.equals(rawCommand.getSource())) {
            throw new RuntimeException("Not yet implemented");
        }
        throw new DataSourceException("Provider " + rawCommand.getSource() + " not yet supported");
    }

    /**
     * Dispatch the tweeted command according to the corresponding action
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer originator of the raw command
     * @param rawCommand raw command with emitter coordinates
     * @param command parsed command
     *
     * @throws TwitterException If sending the help message to the originator fails
     * @throws DataSourceException If the communication with the back-end fails
     * @throws ClientException If information extracted from the tweet are incorrect
     */
    public static void processCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws TwitterException, DataSourceException, ClientException {
        Locale locale = consumer.getLocale();
        JsonObject prefixes = localizedPrefixes.get(locale);
        JsonObject actions = localizedActions.get(locale);
        // Clear case of help being requested at the prefix level
        if (command.containsKey(Command.NEED_HELP)) {
            processHelpCommand(rawCommand, command.getString(Command.NEED_HELP), locale);
            return;
        }
        String action = guessAction(command);
        // Alternate case of the help being asked as an action...
        if (CommandSettings.isEquivalentTo(prefixes, CommandSettings.Prefix.help.toString(), action)) {
            processHelpCommand(rawCommand, command.containsKey(Demand.CRITERIA) ? command.getJsonArray(Demand.CRITERIA).getString(0) : "", locale);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.cancel.toString(), action)) {
            processCancelCommand(pm, consumer, rawCommand, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.close.toString(), action)) {
            processCloseCommand(pm, consumer, rawCommand, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.confirm.toString(), action)) {
            processConfirmCommand(pm, consumer, rawCommand, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.decline.toString(), action)) {
            processDeclineCommand(pm, consumer, rawCommand, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.demand.toString(), action)) {
            processDemandCommand(pm, consumer, rawCommand, command, prefixes, actions);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.list.toString(), action)) {
            processListCommand(pm, consumer, rawCommand, command, prefixes, actions);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.propose.toString(), action)) {
            processProposeCommand(pm, consumer, rawCommand, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.supply.toString(), action)) {
            processSupplyCommand(pm, consumer, rawCommand, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.wish.toString(), action)) {
            processWishCommand(pm, consumer, rawCommand, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.www.toString(), action)) {
            processWWWCommand(pm, consumer, rawCommand, command);
        }
        else {
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get("cp_command_parser_unsupported_action", new Object[] { action }, locale)
            );
        }
    }

    /**
     * Utility function extracting the action, even if the attribute is not present, by looking at all given parameters
     * @param command Set of command attributes
     * @return Specified or guessed action
     */
    protected static String guessAction(JsonObject command) {
        String action = command.getString(Demand.ACTION);
        if (action == null) {
            if (command.containsKey(Demand.REFERENCE)) {
                action = command.size() == 1 ? CommandSettings.Action.list.toString() : CommandSettings.Action.demand.toString();
            }
            else if (command.containsKey(Store.STORE_KEY)) {
                action = command.size() == 1 ? CommandSettings.Action.list.toString() : null; // No possibility to create/update/delete Store instance from Twitter
            }
            /* TODO: implement other listing variations
            else if (command.containsKey(Product.PRODUCT_KEY)) {
                action = command.size() == 1 ? CommandSettings.Action.list.toString() : null; // No possibility to create/update/delete Store instance from Twitter
            }
            */
            else {
                action = CommandSettings.Action.demand.toString();
            }
        }
        return action;
    }

    /**
     * Use the keyword given as an argument to select an Help text among {prefixes, actions, registered keywords}.
     * If the extracted keyword does not match anything, the default Help text is tweeted.
     *
     * @param rawCommand raw command with emitter coordinates
     * @param prefixes List of localized prefixes for the orginator's locale
     * @param actions List of location actions for the originator's locale
     * @param arguments Sequence submitted in addition to the question mark (?) or to the help command
     * @param locale Originator's locale
     *
     * @throws DataSourceException If sending the help message to the originator fails
     * @throws ClientException If the communication back with the user fails
     */
    protected static void processHelpCommand(RawCommand rawCommand, String arguments, Locale locale) throws DataSourceException, ClientException {
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
                    LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, locale)
            );
            return;
        }
        String message = null;
        // TODO: lookup into the cache to see if the given keyword has already been resolved
        // if (getCache().containsKey(keyword + locale.toString()) {
        //     message = (String) getCache().get(keyword + locale.toString());
        // }
        // Check if the keyword is a prefix
        if (true) { // if (message == null) {
            JsonObject prefixes = localizedPrefixes.get(locale);
            for (CommandSettings.Prefix prefix: CommandSettings.Prefix.values()) {
                if (CommandSettings.isEquivalentTo(prefixes, prefix.toString(), keyword)) {
                    message = LabelExtractor.get(ResourceFileId.second, prefix.toString(), locale);
                    break;
                }
            }
        }
        // Check if the keyword is an action
        if (message == null) {
            JsonObject actions = localizedActions.get(locale);
            for (CommandSettings.Action action: CommandSettings.Action.values()) {
                if (CommandSettings.isEquivalentTo(actions, action.toString(), keyword)) {
                    message = LabelExtractor.get(ResourceFileId.second, action.toString(), locale);
                    break;
                }
            }
        }
        // Check if the keyword is a state
        if (message == null) {
            JsonObject states = localizedStates.get(locale);
            for (CommandSettings.State state: CommandSettings.State.values()) {
                if (states.getString(state.toString()).equals(keyword)) {
                    message = LabelExtractor.get(ResourceFileId.second, state.toString(), locale);
                    break;
                }
            }
        }
        // Check of the keyword is one registered
        if (message == null) {
            JsonObject helpKeywords = localizedHelpKeywords.get(locale);
            for (String helpKeyword: helpKeywords.getMap().keySet()) {
                JsonArray equivalents = helpKeywords.getJsonArray(helpKeyword);
                for (int i = 0; i < equivalents.size(); i++) {
                    if (equivalents.getString(i).equals(keyword)) {
                        message = LabelExtractor.get(ResourceFileId.second, helpKeyword, locale);
                        break;
                    }
                }
            }
        }
        // Tweet the default help message if the given keyword is not recognized
        if (message == null) {
            message = LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, locale);
        }
        // TODO: save the match into the cache for future queries
        // getCache().put(keyword + locale.toString(), message);
        communicateToEmitter(rawCommand, message);
    }

    protected static void processCancelCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException, TwitterException {
        //
        // Used by resource owner to stop the process of his resource
        //
        // 1. Cancel the identified demand
        // 2. Cancel the identified proposal
        // 3. Cancel the identified wish
        //
        if (command.containsKey(Demand.REFERENCE)) {
            // Update demand state
            Demand demand = null;
            try {
                demand = demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
            }
            catch(Exception ex) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_cancel_invalid_demand_id", consumer.getLocale())
                );
            }
            if (demand != null) {
                demand.setState(CommandSettings.State.canceled);
                demandOperations.updateDemand(pm, demand);
                // Echo back the updated demand
                Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                communicateToEmitter(rawCommand, generateTweet(demand, location, consumer.getLocale()));
            }
        }
        /* TODO: implement other variations
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            throw new ClientException("Canceling proposals - Not yet implemented");
        }
        else if (command.containsKey(Wish.REFERENCE)) {
            throw new ClientException("Canceling proposals - Not yet implemented");
        }
        */
        else {
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get("cp_command_cancel_missing_demand_id", consumer.getLocale())
            );
        }
    }

    protected static void processCloseCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by the resource owner to report that the expected product has been delivered
        //
        // 1. Close the identified demand
        // 2. Close the identified proposal
        throw new ClientException("Closing demands - Not yet implemented");
    }

    protected static void processConfirmCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by the consumer to accept a proposal
        //
        throw new ClientException("Confirming proposals - Not yet implemented");
    }

    protected static void processDeclineCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by a consumer to refuse a proposal
        //
        throw new ClientException("Declining proposals - Not yet implemented");
    }

    public static void processDemandCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException, TwitterException {
        //
        // Used by a consumer to:
        //
        // 1. create a new demand
        // 2. update the identified demand
        //
        Long demandKey = 0L;
        Location newLocation = Location.hasAttributeForANewLocation(command) ? locationOperations.createLocation(pm, command) : null;
        if (command.containsKey(Demand.REFERENCE)) {
            // Extracts the new location
            if (newLocation != null) {
                command.put(Demand.LOCATION_KEY, newLocation.getKey());
            }
            // Update the demand attributes
            Demand demand = null;
            try {
                demand = demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
            }
            catch(Exception ex) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_demand_invalid_demand_id", consumer.getLocale())
                );
            }
            if (demand != null) {
                demand.fromJson(command);
                demand.setState(CommandSettings.State.open); // Will force the re-validation of the entire demand
                demand.resetProposalKeys(); // All existing proposals are removed
                demandOperations.updateDemand(pm, demand);
                // Echo back the updated demand
                Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                communicateToEmitter(rawCommand, generateTweet(demand, location, consumer.getLocale()));
                // Get the demandKey for the task scheduling
                demandKey = demand.getKey();
            }
        }
        else {
            // Extracts the new location
            Long newLocationKey = consumer.getLocationKey();
            if (newLocation != null) {
                newLocationKey = newLocation.getKey();
            }
            // Get the latest demand or the default one
            List<Demand> demands = demandOperations.getDemands(pm, Demand.CONSUMER_KEY, consumer.getKey(), 1);
            Demand latestDemand = null;
            if (0 < demands.size()) {
                latestDemand = demands.get(0);
                // Transfer the demand into a new object
                latestDemand = new Demand(latestDemand.toJson()); // To avoid attempts to persist the object
                // Reset sensitive fields
                latestDemand.resetKey();
                latestDemand.resetCoreDates();
                latestDemand.setAction(CommandSettings.Action.demand);
                latestDemand.resetCriteria();
                latestDemand.setDefaultExpirationDate();
                latestDemand.setState(CommandSettings.State.open);
            }
            else {
                latestDemand = new Demand();
                // Set fields with default values
                latestDemand.setAction(CommandSettings.Action.demand);
            }
            latestDemand.setSource(rawCommand.getSource());
            // Update of the latest command (can be the default one) with the just extracted parameters
            command = latestDemand.fromJson(command).toJson();
            if (newLocationKey != null && !newLocationKey.equals(command.getLong(Demand.LOCATION_KEY))) {
                command.put(Demand.LOCATION_KEY, newLocationKey);
            }
            // Persist the new demand
            Demand newDemand = demandOperations.createDemand(pm, command, consumer.getKey());
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get(
                            "cp_command_demand_acknowledge_creation",
                            new Object[] { newDemand.getKey() },
                            consumer.getLocale()
                    )
            );
            Location location = newDemand.getLocationKey() == null ? null : locationOperations.getLocation(pm, newDemand.getLocationKey());
            communicateToEmitter(rawCommand, generateTweet(newDemand, location, consumer.getLocale()));
            // Get the demandKey for the task scheduling
            demandKey = newDemand.getKey();
        }

        // Create a task for that demand
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(url("/API/maezel/validateOpenDemand").param(Demand.KEY, demandKey.toString()).method(Method.GET));
    }

    protected static void processListCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {
        //
        // Used by actors to:
        //
        // 1. Get the details about the identified demand
        // 2. Get the details about the identified proposal
        // 3. Get the details about the identified product
        // 4. Get the details about the identified store
        //
        if (command.containsKey(Demand.REFERENCE)) {
            Demand demand = null;
            try {
                demand = demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
            }
            catch(Exception ex) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_list_invalid_demand_id", consumer.getLocale())
                );
            }
            if (demand != null) {
                // Echo back the specified demand
                Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                communicateToEmitter(rawCommand, generateTweet(demand, location, consumer.getLocale()));
            }
        }
        /* TODO: implement other listing variations
        else if (command.getString(Proposal.PROPOSAL_KEY) != null) {
            throw new ClientException("Listing proposals - Not yet implemented");
        }
        else if (command.getString(Product.PRODUCT_KEY) != null) {
            throw new ClientException("Listing Stores - Not yet implemented");
        }
        else if (command.getString(Store.STORE_KEY) != null) {
            throw new ClientException("Listing Stores - Not yet implemented");
        }
        */
        else {
            // FIXME: select only {invalid, open, published, proposed} demands -- {canceled, closed} demands can be only listed with the Web console
            List<Demand> demands = demandOperations.getDemands(pm, Demand.CONSUMER_KEY, consumer.getKey(), 0);
            if (demands.size() == 0) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get(
                                "cp_command_list_no_active_demand",
                                consumer.getLocale()
                        )
                );
            }
            else {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get(
                                "cp_command_list_series_introduction",
                                new Object[] { demands.size() },
                                consumer.getLocale()
                        )
                );
                for (Demand demand: demands) {
                    Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                    communicateToEmitter(rawCommand, generateTweet(demand, location, consumer.getLocale()));
                }
            }
        }
    }

    protected static void processProposeCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by a retailer to propose a product for a demand
        //
        throw new ClientException("Proposing proposals - Not yet implemented");
    }

    protected static void processSupplyCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by a retailer to add/remove tags to his supply list
        //
        throw new ClientException("Supplying tags - Not yet implemented");
    }

    protected static void processWishCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by a consumer to:
        //
        // 1. Create a wish
        // 2. Update a wish
        //
        throw new ClientException("Wishing - Not yet implemented");
    }

    protected static void processWWWCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by the resource owner to get the tiny URL that will open the Twetailer Web console
        //
        throw new ClientException("Surfing on the web - Not yet implemented");
    }
}
