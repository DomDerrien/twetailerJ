package twetailer.adapter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import twitter4j.DirectMessage;
import twitter4j.TwitterException;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.SettingsOperations;
import twetailer.validator.CommandSettings;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TwitterAdapter {
    private static final Logger log = Logger.getLogger(TwitterAdapter.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    static Map<Locale, JsonObject> localizedPrefixes = new HashMap<Locale, JsonObject>();
    static Map<Locale, JsonObject> localizedActions = new HashMap<Locale, JsonObject>();
    static Map<Locale, JsonObject> localizedStates = new HashMap<Locale, JsonObject>();
    static Map<Locale, JsonObject> localizedHelpKeywords = new HashMap<Locale, JsonObject>();
    static Map<Locale, Map<CommandSettings.Prefix, Pattern>> localizedPatterns = new HashMap<Locale, Map<CommandSettings.Prefix, Pattern>>();
    
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
            preparePattern(prefixes, patterns, CommandSettings.Prefix.tags, "?(?:\\+\\s*|\\-\\s*|\\w\\s*)+");

            localizedPatterns.put(locale, patterns);
        }
    }

    private static String createPatternWithOptionalEndingCharacters(String prefix) {
        if (prefix.length() == 1) {
            if ("?".equals(prefix)) {
                return "\\?";
            }
            return prefix;
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
    public static JsonObject parseTweet (Map<CommandSettings.Prefix, Pattern> patterns, String message) throws ClientException, ParseException {
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
    public static String generateTweet(Demand demand, Location location, JsonObject prefixes, JsonObject actions, Locale locale) {
        final String space = " ";
        StringBuilder tweet = new StringBuilder();
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
     * Extract commands from the pending Direct Messages and acts accordingly
     *
     * @return Updated direct message identifier if new DMs have been processed, or the given one if none has been processed
     *
     * @throws TwitterException
     * @throws DataSourceException
     */
    public static Long processDirectMessages() throws TwitterException, DataSourceException, ParseException, ClientException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            Settings settings = settingsOperations.getSettings(pm);
            Long sinceId = settings.getLastProcessDirectMessageId();
            Long lastId = processDirectMessages(pm, sinceId);
            if (!lastId.equals(sinceId)) {
                settings.setLastProcessDirectMessageId(lastId);
                settingsOperations.updateSettings(pm, settings);
            }
            return lastId;
        }
        finally {
            pm.close();
        }
    }

    /**
     * Extract commands from the pending Direct Messages and acts accordingly
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param sinceId identifier of the last process direct message
     * @return Updated direct message identifier if new DMs have been processed, or the given one if none has been processed
     *
     * @throws TwitterException
     * @throws DataSourceException
     */
    @SuppressWarnings("deprecation")
    protected static Long processDirectMessages(PersistenceManager pm, Long sinceId) throws DataSourceException, TwitterException {
        long lastId = sinceId;

        // Get the list of direct messages
        List<DirectMessage> messages = null;
        try {
            messages = TwitterUtils.getDirectMessages(sinceId);
        }
        catch(TwitterException ex) {
            log.info("Cannot get the Direct Messages (DM) for the account " + TwitterUtils.getTwetailerScreenName());
        }
        
        // Process each messages one-by-one
        int idx = messages == null ? 0 : messages.size(); // To start by the end of the message queue
        log.warning(idx + " direct messages to process (temporary limited to 1 DM per operation)"); // FIXME: remove the limitation of 1 DM processed at a time
        while (0 < idx) {
            --idx;
            DirectMessage dm = messages.get(idx);
            long dmId = dm.getId();
            
            // Get Twetailer account and verify the user is a follower
            twitter4j.User sender = dm.getSender();
            Consumer consumer = consumerOperations.createConsumer(pm, sender); // Creation only occurs if the corresponding Consumer instance is not retrieved
            Locale senderLocale = consumer.getLocale();
            if (!sender.isFollowing()) {
                TwitterUtils.sendPublicMessage(LabelExtractor.get("ta_messageToNonFollower", new Object[] { sender.getScreenName() }, senderLocale));
                break;
            }
            
            // Evaluate the new demand
            try {
                // Load the definitions for the sender locale
                loadLocalizedSettings(senderLocale);
                JsonObject prefixes = localizedPrefixes.get(senderLocale);
                Map<CommandSettings.Prefix, Pattern> patterns = localizedPatterns.get(senderLocale);

                // Extract information from the tweet and process the information
                JsonObject command = parseTweet(patterns, dm.getText());
                command.put(Demand.TWEET_ID, dmId);
                processCommand(pm, sender, consumer, prefixes, command);
            }
            catch(Exception ex) {
                // TODO: use localized message
                ex.printStackTrace();
                TwitterUtils.sendDirectMessage(sender.getScreenName(), "Error: " + ex.getMessage());
            }
            
            if (lastId < dmId) {
                lastId = dmId;
            }
            break; // FIXME: remove the limitation of 1 DM processed at a time
        }

        return Long.valueOf(lastId);
    }
    
    /**
     * Dispatch the tweeted command according to the corresponding action
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param sender Twitter message originator
     * @param prefixes List of localized prefixes for the orginator's locale
     * @param arguments Sequence submitted in addition to the question mark (?) or to the help command
     * 
     * @throws TwitterException If sending the help message to the originator fails
     * @throws DataSourceException If the communication with the back-end fails
     * @throws ClientException If information extracted from the tweet are incorrect
     */
    protected static void processCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject prefixes, JsonObject command) throws TwitterException, DataSourceException, ClientException {
        Locale locale = consumer.getLocale();
        JsonObject actions = localizedActions.get(locale);
        // Clear case of help being requested at the prefix level
        if (command.containsKey(Command.NEED_HELP)) {
            processHelpCommand(sender, prefixes, actions, command.getString(Command.NEED_HELP), locale);
            return;
        }
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
        // Alternate case of the help being asked as an action...
        if (CommandSettings.isEquivalentTo(prefixes, CommandSettings.Prefix.help.toString(), action)) {
            processHelpCommand(sender, prefixes, actions, command.containsKey(Demand.CRITERIA) ? command.getJsonArray(Demand.CRITERIA).getString(0) : "", locale);
            return;
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.cancel.toString(), action)) {
            processCancelCommand(pm, sender, consumer, command, prefixes, actions);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.close.toString(), action)) {
            processCloseCommand(pm, sender, consumer, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.confirm.toString(), action)) {
            processConfirmCommand(pm, sender, consumer, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.decline.toString(), action)) {
            processDeclineCommand(pm, sender, consumer, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.demand.toString(), action)) {
            processDemandCommand(pm, sender, consumer, command, prefixes, actions);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.list.toString(), action)) {
            processListCommand(pm, sender, consumer, command, prefixes, actions);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.propose.toString(), action)) {
            processProposeCommand(pm, sender, consumer, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.supply.toString(), action)) {
            processSupplyCommand(pm, sender, consumer, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.wish.toString(), action)) {
            processWishCommand(pm, sender, consumer, command);
        }
        else if (CommandSettings.isEquivalentTo(actions, CommandSettings.Action.www.toString(), action)) {
            processWWWCommand(pm, sender, consumer, command);
        }
        else {
            TwitterUtils.sendDirectMessage(sender.getScreenName(), LabelExtractor.get("error_unsupported_action", locale));
        }
    }
    
    /**
     * Use the keyword given as an argument to select an Help text among {prefixes, actions, registered keywords}.
     * If the extracted keyword does not match anything, the default Help text is tweeted.
     * 
     * @param sender Twitter message originator
     * @param prefixes List of localized prefixes for the orginator's locale
     * @param actions List of location actions for the originator's locale
     * @param arguments Sequence submitted in addition to the question mark (?) or to the help command
     * @param locale Originator's locale
     * 
     * @throws TwitterException If sending the help message to the originator fails
     */
    protected static void processHelpCommand(twitter4j.User sender, JsonObject prefixes, JsonObject actions, String arguments, Locale locale) throws TwitterException {
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
            TwitterUtils.sendDirectMessage(sender.getScreenName(), LabelExtractor.get(CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, locale));
            return;
        }
        // Check if the keyword is a prefix
        for (CommandSettings.Prefix prefix: CommandSettings.Prefix.values()) {
            if (CommandSettings.isEquivalentTo(prefixes, prefix.toString(), keyword)) {
                TwitterUtils.sendDirectMessage(sender.getScreenName(), LabelExtractor.get(CommandSettings.HELP_DEFINITION_PREFIX_PREFIX + prefix, locale));
                return;
            }
        }
        // Check if the keyword is an action
        for (CommandSettings.Action action: CommandSettings.Action.values()) {
            if (CommandSettings.isEquivalentTo(actions, action.toString(), keyword)) {
                TwitterUtils.sendDirectMessage(sender.getScreenName(), LabelExtractor.get(CommandSettings.HELP_DEFINITION_ACTION_PREFIX + action, locale));
                return;
            }
        }
        // Check of the keyword is one registered
        JsonObject helpKeywords = localizedHelpKeywords.get(locale);
        for (String helpKeyword: helpKeywords.getMap().keySet()) {
            if (helpKeyword.equals(keyword)) {
                TwitterUtils.sendDirectMessage(sender.getScreenName(), LabelExtractor.get(CommandSettings.HELP_DEFINITION_KEYWORD_PREFIX + helpKeyword, locale));
                return;
            }
        }
        // Tweet the default help message if the given keyword is not recognized
        TwitterUtils.sendDirectMessage(sender.getScreenName(), LabelExtractor.get(CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, locale));
    }
    
    protected static void processCancelCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command, JsonObject prefixes, JsonObject actions) throws ClientException, DataSourceException, TwitterException {
        //
        // Used by resource owner to stop the process of his resource
        //
        // 1. Cancel the identified demand
        // 2. Cancel the identified proposal
        // 3. Cancel the identified wish
        //
        if (command.containsKey(Demand.REFERENCE)) {
            // Update demand state
            Demand demand = demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
            demand.setState(CommandSettings.State.canceled);
            demandOperations.updateDemand(pm, demand);
            // Echo back the updated demand
            Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
            TwitterUtils.sendDirectMessage(sender.getScreenName(), generateTweet(demand, location, prefixes, actions, consumer.getLocale()));
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
            TwitterUtils.sendDirectMessage(sender.getScreenName(), LabelExtractor.get("error_cancel_without_resource_id", consumer.getLocale()));
        }
    }
    
    protected static void processCloseCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command) throws ClientException {
        //
        // Used by the resource owner to report that the expected product has been delivered
        //
        // 1. Close the identified demand
        // 2. Close the identified proposal
        throw new ClientException("Closing demands - Not yet implemented");
    }
    
    protected static void processConfirmCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command) throws ClientException {
        //
        // Used by the consumer to accept a proposal
        //
        throw new ClientException("Confirming proposals - Not yet implemented");
    }
    
    protected static void processDeclineCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command) throws ClientException {
        //
        // Used by a consumer to refuse a proposal
        //
        throw new ClientException("Declining proposals - Not yet implemented");
    }
    
    protected static void processDemandCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException, TwitterException {
        //
        // Used by a consumer to:
        //
        // 1. create a new demand
        // 2. update the identified demand
        //
        Long reference = command.getLong(Demand.REFERENCE);
        if (reference != 0) {
            // Extracts the new location
            Location newLocation = locationOperations.createLocation(pm, command);
            if (newLocation != null) {
                command.put(Demand.LOCATION_KEY, newLocation.getKey());
            }
            // Update the demand attributes
            Demand demand = demandOperations.getDemand(pm, reference, consumer.getKey());
            demand.fromJson(command);
            demand.setState(CommandSettings.State.open); // Will force the re-validation of the entire demand
            demand.resetProposalKeys(); // All existing proposals are removed
            demandOperations.updateDemand(pm, demand);
            // Echo back the updated demand
            Location location = demand.getLocationKey() == null || demand.getLocationKey() == 0L ? null : locationOperations.getLocation(pm, demand.getLocationKey());
            TwitterUtils.sendDirectMessage(sender.getScreenName(), generateTweet(demand, location, prefixes, actions, consumer.getLocale()));
        }
        else {
            // Extracts the new location
            Location newLocation = locationOperations.createLocation(pm, command);
            Long newLocationKey = newLocation == null ? consumer.getLocationKey() : newLocation.getKey();
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
            // Update of the latest command (can be the default one) with the just extracted parameters
            command = latestDemand.fromJson(command).toJson();
            if (newLocationKey != null && newLocationKey != 0L && newLocationKey != command.getLong(Demand.LOCATION_KEY)) {
                command.put(Demand.LOCATION_KEY, newLocationKey);
            }
            // Persist the new demand
            Demand newDemand = demandOperations.createDemand(pm, command, consumer.getKey());
            String screenName = sender.getScreenName();
            TwitterUtils.sendDirectMessage(
                    screenName,
                    LabelExtractor.get(
                            "ta_acknowledgeDemandCreated",
                            new Object[] { newDemand.getKey() },
                            consumer.getLocale()
                    )
            );
            Location location = newDemand.getLocationKey() == null || newDemand.getLocationKey() == 0L ? null : locationOperations.getLocation(pm, newDemand.getLocationKey());
            TwitterUtils.sendDirectMessage(screenName, generateTweet(newDemand, location, prefixes, actions, consumer.getLocale()));
        }
    }

    protected static void processListCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, TwitterException {
        //
        // Used by actors to:
        //
        // 1. Get the details about the identified demand
        // 2. Get the details about the identified proposal
        // 3. Get the details about the identified product
        // 4. Get the details about the identified store
        //
        if (command.containsKey(Demand.REFERENCE)) {
            Long reference = command.getLong(Demand.REFERENCE);
            Demand demand = demandOperations.getDemand(pm, reference, consumer.getKey());
            if (demand != null) {
                // Echo back the specified demand
                Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                TwitterUtils.sendDirectMessage(sender.getScreenName(), generateTweet(demand, location, prefixes, actions, consumer.getLocale()));
            }
            else {
                TwitterUtils.sendDirectMessage(
                        sender.getScreenName(),
                        LabelExtractor.get(
                                "ta_responseToSpecificListCommandForNoResult",
                                new Object[] { reference },
                                consumer.getLocale()
                        )
                );
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
            List<Demand> demands = demandOperations.getDemands(pm, Demand.CONSUMER_KEY, consumer.getKey(), 0);
            if (demands.size() == 0) {
                TwitterUtils.sendDirectMessage(
                        sender.getScreenName(),
                        LabelExtractor.get(
                                "ta_responseToListCommandForNoResult",
                                consumer.getLocale()
                        )
                );
            }
            else {
                String screenName = sender.getScreenName();
                TwitterUtils.sendDirectMessage(
                        screenName,
                        LabelExtractor.get(
                                "ta_introductionToResponseToListCommandWithManyResults",
                                new Object[] { demands.size() },
                                consumer.getLocale()
                        )
                );
                for (Demand demand: demands) {
                    Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                    TwitterUtils.sendDirectMessage(screenName, generateTweet(demand, location, prefixes, actions, consumer.getLocale()));
                }
            }
        }
    }
    
    protected static void processProposeCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command) throws ClientException {
        //
        // Used by a retailer to propose a product for a demand
        //
        throw new ClientException("Proposing proposals - Not yet implemented");
    }
    
    protected static void processShopCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command) throws ClientException {
        throw new ClientException("Shopping - Not yet implemented");
    }
    
    protected static void processSupplyCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command) throws ClientException {
        //
        // Used by a retailer to add/remove tags to his supply list
        //
        throw new ClientException("Supplying tags - Not yet implemented");
    }
    
    protected static void processWishCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command) throws ClientException {
        //
        // Used by a consumer to:
        //
        // 1. Create a wish
        // 2. Update a wish
        //
        throw new ClientException("Wishing - Not yet implemented");
    }
    
    protected static void processWWWCommand(PersistenceManager pm, twitter4j.User sender, Consumer consumer, JsonObject command) throws ClientException {
        //
        // Used by the resource owner to get the tiny URL that will open the Twetailer Web console
        //
        throw new ClientException("Surfing on the web - Not yet implemented");
    }
    
}