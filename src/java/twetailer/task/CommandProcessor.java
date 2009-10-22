package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToEmitter;
import static twetailer.connector.BaseConnector.communicateToRetailer;

import java.text.Collator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Retailer;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

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
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected static RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();
    protected static RetailerOperations retailerOperations = _baseOperations.getRetailerOperations();
    protected static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    protected static Map<Locale, JsonObject> localizedPrefixes = new HashMap<Locale, JsonObject>();
    protected static Map<Locale, JsonObject> localizedActions = new HashMap<Locale, JsonObject>();
    protected static Map<Locale, JsonObject> localizedStates = new HashMap<Locale, JsonObject>();
    protected static Map<Locale, JsonObject> localizedHelpKeywords = new HashMap<Locale, JsonObject>();
    protected static Map<Locale, Map<String, Pattern>> localizedPatterns = new HashMap<Locale, Map<String, Pattern>>();

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

        Map<String, Pattern> patterns = localizedPatterns.get(locale);
        if (patterns == null) {
            patterns = new HashMap<String, Pattern>();

            final String separatorFromOtherPrefix = "(?:\\s+(?:\\-?\\+?\\w+:)|$)";
            final String separatorFromNonDigit = "(?:\\D|$)";
            final String separatorFromNonAlpha = "(?:\\W|$)";

            preparePattern(prefixes, patterns, Prefix.action, "\\s*\\w+", separatorFromNonAlpha);
            preparePattern(prefixes, patterns, Prefix.expiration, "[\\d- ]+", separatorFromNonDigit);
            preparePattern(prefixes, patterns, Prefix.help, "", ""); // Given keywords considered as tags
            preparePattern(prefixes, patterns, Prefix.locale, "[\\w- ]+(?:ca|us)", separatorFromNonAlpha);
            // FIXME: use DecimalFormatSymbols.getInstance(locale).getCurrencySymbol() in the following expression
            preparePattern(prefixes, patterns, Prefix.price, "[ $€£¥\\d\\.,]+", separatorFromNonDigit);
            preparePattern(prefixes, patterns, Prefix.proposal, "\\s*\\d+", separatorFromNonDigit);
            preparePattern(prefixes, patterns, Prefix.quantity, "[\\s\\d\\.,]+", separatorFromNonDigit);
            preparePattern(prefixes, patterns, Prefix.reference, "\\s*\\d+", separatorFromNonDigit);
            preparePattern(prefixes, patterns, Prefix.range, "[\\s\\d\\.,]+(?:miles|mile|mi|km)", ".*" + separatorFromOtherPrefix);
            preparePattern(prefixes, patterns, Prefix.state, "\\s*\\w+", separatorFromNonAlpha);
            preparePattern(prefixes, patterns, Prefix.tags, "?.+", "");
            preparePattern(prefixes, patterns, Prefix.total, "[\\s$€£\\d\\.,]+", separatorFromNonDigit);

            String modifiedPrefix = "\\+" + prefixes.getJsonArray(Prefix.tags.toString()).getString(0);
            patterns.put(modifiedPrefix, Pattern.compile("((?:" + modifiedPrefix + ":)[^\\:]+)(?: +[\\w\\+\\-]+:|$)", Pattern.CASE_INSENSITIVE));

            modifiedPrefix = "\\-" + prefixes.getJsonArray(Prefix.tags.toString()).getString(0);
            patterns.put(modifiedPrefix, Pattern.compile("((?:" + modifiedPrefix + ":)[^\\:]+)(?: +[\\w\\+\\-]+:|$)", Pattern.CASE_INSENSITIVE));

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
     * @param separator part of the regular expression separating the entity from the rest of the phrase
     */
    private static void preparePattern(JsonObject keywords, Map<String, Pattern> patterns, Prefix keyword, String expression, String separator) {
        String prefix = keywords.getJsonArray(keyword.toString()).getString(0);
        String pattern = createPatternWithOptionalEndingCharacters(prefix);
        for(int i = 1; i < keywords.getJsonArray(keyword.toString()).size(); i++) {
            prefix = keywords.getJsonArray(keyword.toString()).getString(i);
            pattern += "|" + createPatternWithOptionalEndingCharacters(prefix);
        }
        patterns.put(keyword.toString(), Pattern.compile("((?:" + pattern + ")" + expression + ")" + separator, Pattern.CASE_INSENSITIVE));
    }

    /**
     * Parse the given tweet with the set of given localized prefixes
     * @param patterns list of registered RegEx patterns preset for the user's locale
     * @param message message are returned by the communication provider
     * @param locale emitter preferred language
     *
     * @return JsonObject with all message attributes correctly extracted from the given message
     *
     * @throws ClientException If the query is malformed
     * @throws ParseException if a date format is invalid
     */
    public static JsonObject parseCommand (Map<String, Pattern> patterns, String message, Locale locale) throws ClientException, ParseException {
        Matcher matcher;
        boolean oneFieldOverriden = false;
        log.warning("Message to parse: " + message);
        JsonObject command = new GenericJsonObject();
        // Help
        try {
            matcher = patterns.get(Prefix.help.toString()).matcher(message);
            if (matcher.find()) { // Runs the matcher once
                message = matcher.replaceFirst("");
                // No need to continue parsing: grab the rest of the tweet that will be used for lookups in the TMX
                command.put(Command.NEED_HELP, message.trim());
                return command;
            }
        }
        catch(IllegalStateException ex) {}
        StringBuilder messageCopy = new StringBuilder(message);
        // Action
        try {
            matcher = patterns.get(Prefix.action.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Command.ACTION, getAction(currentGroup.toLowerCase(locale)));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Expiration
        try {
            matcher = patterns.get(Prefix.expiration.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.EXPIRATION_DATE, getDate(currentGroup));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Locale
        try {
            matcher = patterns.get(Prefix.locale.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Location.COUNTRY_CODE, getCountryCode(currentGroup).toUpperCase(locale));
                command.put(Location.POSTAL_CODE, getPostalCode(currentGroup, command.getString(Location.COUNTRY_CODE)).toUpperCase(locale));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Price
        try {
            matcher = patterns.get(Prefix.price.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Proposal.PRICE, getDoubleValue(currentGroup, locale));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Proposal
        try {
            matcher = patterns.get(Prefix.proposal.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Proposal.PROPOSAL_KEY, getLongValue(currentGroup, locale));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Quantity
        try {
            matcher = patterns.get(Prefix.quantity.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.QUANTITY, getLongValue(currentGroup, locale));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Reference
        try {
            matcher = patterns.get(Prefix.reference.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.REFERENCE, getLongValue(currentGroup, locale));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Range
        try {
            matcher = patterns.get(Prefix.range.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Demand.RANGE_UNIT, getRangeUnit(currentGroup).toLowerCase(locale));
                command.put(Demand.RANGE, getDoubleValue(currentGroup, locale));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Total
        try {
            matcher = patterns.get(Prefix.total.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put(Proposal.TOTAL, getDoubleValue(currentGroup, locale));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // \+Tags
        try {
            matcher = patterns.get("\\+" + Prefix.tags.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put("\\+" + Demand.CRITERIA, new GenericJsonArray(getTags(currentGroup)));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // \-Tags
        try {
            matcher = patterns.get("\\-" + Prefix.tags.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                command.put("\\-" + Demand.CRITERIA, new GenericJsonArray(getTags(currentGroup)));
                messageCopy = extractPart(messageCopy, currentGroup);
                oneFieldOverriden = true;
            }
        }
        catch(IllegalStateException ex) {}
        // Tags
        try {
            matcher = patterns.get(Prefix.tags.toString()).matcher(messageCopy);
            if (matcher.find()) { // Runs the matcher once
                String currentGroup = matcher.group(1).trim();
                if (0 < currentGroup.length()) {
                    command.put(Demand.CRITERIA, new GenericJsonArray(getTags(currentGroup)));
                    messageCopy = extractPart(messageCopy, currentGroup);
                    oneFieldOverriden = true;
                }
            }
        }
        catch(IllegalStateException ex) {}

        if (!oneFieldOverriden) {
            throw new ClientException("No query field has been correctly extracted");
        }
        return command;
    }

    /**
     * Helper extracting the specified part from the input stream
     *
     * @param master input stream to process
     * @param excerpt part to extract
     * @return cleaned-up stream
     */
    private static StringBuilder extractPart(StringBuilder master, String excerpt) {
        int start = master.indexOf(excerpt);
        int end = start + excerpt.length();
        return master.replace(start, end, "");
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
     *
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
     * Helper extracting distance unit
     *
     * @param pattern Parameters extracted by a regular expression
     * @return valid distance unit
     */
    private static String getRangeUnit(String pattern) {
        int indexSpace = pattern.lastIndexOf(' ');
        if (indexSpace != -1 && !Character.isDigit(pattern.charAt(indexSpace + 1))) {
            return pattern.substring(indexSpace + 1);
        }
        int idx = pattern.length();
        while(true) { // while(0 < idx) { // No need to check because the pattern matches the regexp \\d+\\s*(miles|mi|km)
            --idx;
            if (!Character.isLetter(pattern.charAt(idx))) {
                break;
            }
        }
        return pattern.substring(idx + 1);
    }

    /**
     * Helper extracting country codes
     *
     * @param pattern Parameters extracted by a regular expression
     * @return valid country cod
     */
    private static String getCountryCode(String pattern) {
        int indexSpace = pattern.lastIndexOf(' ');
        if (indexSpace != -1) {
            return pattern.substring(indexSpace + 1);
        }
        int indexDash = pattern.lastIndexOf('-');
        if (indexDash != -1) {
            return pattern.substring(indexDash + 1);
        }
        return LocaleValidator.DEFAULT_COUNTRY_CODE;
    }

    /**
     * Helper extracting postal code
     *
     * @param pattern Parameters extracted by a regular expression
     * @return valid postal code
     */
    private static String getPostalCode(String pattern, String countryCode) {
        String postalCode = pattern.substring(pattern.indexOf(":") + 1, pattern.length() - countryCode.length()).replaceAll("\\s", "");
        if (postalCode.charAt(postalCode.length() - 1) == '-') {
            return postalCode.substring(0, postalCode.length() - 1);
        }
        return postalCode;
    }

    /**
     * Helper returning only number related characters
     *
     * @param pattern Parameters extracted by a regular expression
     * @return valid number representation, ready to be transformed
     */
    private static String getCleanNumber(String pattern) {
        StringBuilder value = new StringBuilder(pattern.substring(pattern.indexOf(":") + 1).trim());
        int idx = value.length();
        while (0 < idx) {
            -- idx;
            char scannedChar = value.charAt(idx);
            // Look for excluding the currency symbols
            if (!Character.isDigit(scannedChar) && scannedChar != '.' && scannedChar != ',') {
                value = value.replace(idx, idx + 1, "");
            }
        }
        return value.toString();
    }

    /**
     * Helper extracting Double values
     *
     * @param pattern Parameters extracted by a regular expression
     * @param locale Locale used to detect the decimal separator
     * @return valid Double value
     *
     * @throws ParseException if the parsing fails
     */
    private static double getDoubleValue(String pattern, Locale locale) throws ParseException {
        NumberFormat extractor = DecimalFormat.getInstance(locale);
        return extractor.parse(getCleanNumber(pattern)).doubleValue();
    }

    /**
     * Helper extracting Long values
     *
     * @param pattern Parameters extracted by a regular expression
     * @param locale Locale used to detect the decimal separator
     * @return valid Long value
     *
     * @throws ParseException if the parsing fails
     */
    private static long getLongValue(String pattern, Locale locale) throws ParseException {
        NumberFormat extractor = DecimalFormat.getInstance(locale);
        return extractor.parse(getCleanNumber(pattern)).longValue();
    }

    /**
     * Helper extracting tags
     *
     * @param pattern Parameters extracted by a regular expression
     * @return tags
     */
    private static String[] getTags(String pattern) {
        String keywords = pattern;
        if (pattern.indexOf(":") != -1) {
            keywords = pattern.substring(pattern.indexOf(":") + 1).trim();
        }
        return keywords.split("\\s+");
    }

    /**
     * Prepare a message to be submit a user
     *
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
        tweet.append(prefixes.getJsonArray(Prefix.action.toString()).getString(0)).append(":").append(actions.getJsonArray(demand.getAction().toString()).getString(0)).append(space);
        if (demand.getKey() != null) {
            tweet.append(prefixes.getJsonArray(Prefix.reference.toString()).getString(0)).append(":").append(demand.getKey()).append(space);
        }
        JsonObject states = localizedStates.get(locale);
        tweet.append(prefixes.getJsonArray(Prefix.state.toString()).getString(0)).append(":").append(states.getString(demand.getState().toString())).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.expiration.toString()).getString(0)).append(":").append(DateUtils.dateToYMD(demand.getExpirationDate())).append(space);
        if (location != null && location.getPostalCode() != null && location.getCountryCode() != null) {
            tweet.append(prefixes.getJsonArray(Prefix.locale.toString()).getString(0)).append(":").append(location.getPostalCode()).append(space).append(location.getCountryCode()).append(space);
        }
        tweet.append(prefixes.getJsonArray(Prefix.range.toString()).getString(0)).append(":").append(demand.getRange()).append(demand.getRangeUnit()).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.quantity.toString()).getString(0)).append(":").append(demand.getQuantity()).append(space);
        if (0 < demand.getCriteria().size()) {
            tweet.append(prefixes.getJsonArray(Prefix.tags.toString()).getString(0)).append(":");
            for (String tag: demand.getCriteria()) {
                tweet.append(tag).append(space);
            }
        }
        return tweet.toString();
    }

    /**
     * Prepare a message to be submit a user
     *
     * @param command Command to convert
     * @param location Place where the command starts
     * @param prefixes List of localized prefix labels
     * @param actions List of localized action labels
     * @return Serialized command
     */
    public static String generateTweet(Proposal proposal, Locale locale) {
        //
        // TODO: use the storeKey to send the store information?
        // TODO: with the demand containing a list of proposal keys, use of the proposal key index in that table as a proposal key equivalent
        //
        final String space = " ";
        StringBuilder tweet = new StringBuilder();
        JsonObject prefixes = localizedPrefixes.get(locale);
        JsonObject actions = localizedActions.get(locale);
        tweet.append(prefixes.getJsonArray(Prefix.action.toString()).getString(0)).append(":").append(actions.getJsonArray(proposal.getAction().toString()).getString(0)).append(space);
        if (proposal.getKey() != null) {
            tweet.append(prefixes.getJsonArray(Prefix.proposal.toString()).getString(0)).append(":").append(proposal.getKey()).append(space);
        }
        if (proposal.getDemandKey() != null) {
            tweet.append(prefixes.getJsonArray(Prefix.reference.toString()).getString(0)).append(":").append(proposal.getDemandKey()).append(space);
        }
        JsonObject states = localizedStates.get(locale);
        tweet.append(prefixes.getJsonArray(Prefix.state.toString()).getString(0)).append(":").append(states.getString(proposal.getState().toString())).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.store.toString()).getString(0)).append(":").append(proposal.getStoreKey()).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.quantity.toString()).getString(0)).append(":").append(proposal.getQuantity()).append(space);
        if (0 < proposal.getCriteria().size()) {
            tweet.append(prefixes.getJsonArray(Prefix.tags.toString()).getString(0)).append(":");
            for (String tag: proposal.getCriteria()) {
                tweet.append(tag).append(space);
            }
        }
        tweet.append(prefixes.getJsonArray(Prefix.price.toString()).getString(0)).append(":").append(proposal.getPrice()).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.total.toString()).getString(0)).append(":").append(proposal.getTotal()).append(space);
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

    public final static String DEBUG_INFO_SWITCH = "-lusanga-debug-info";

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
        Map<String, Pattern> patterns = localizedPatterns.get(senderLocale);

        try {
            // Extract information from the short message and process the information
            JsonObject command = parseCommand(patterns, rawCommand.getCommand(), consumer.getLocale());
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            processCommand(pm, consumer, rawCommand, command);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            String additionalInfo = getDebugInfo(ex);
            boolean exposeInfo = rawCommand.getCommand() != null && rawCommand.getCommand().contains(DEBUG_INFO_SWITCH);
            // Report the error to the raw command emitter
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get("cp_unexpected_error", new Object[] { rawCommand.getKey(), exposeInfo ? additionalInfo : "" }, Locale.ENGLISH)
            );
            // Save the error information for further debugging
            rawCommand.setErrorMessage(additionalInfo);
            rawCommandOperations.updateRawCommand(pm, rawCommand);
        }
    }

    protected static String getDebugInfo(Exception ex) {
        return ex.getClass().getName() + " / " + ex.getMessage();
    }

    /**
     * Helper querying the data store to retrieve the Consumer who matches the command emitter's information.
     * Because the raw command exists, it's assumed the emitter's record exists.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand command as received by the system
     * @return The emitter Consumer record

     * @throws DataSourceException If no Consumer record match the raw command emitter
     */
    protected static Consumer retrieveConsumer(PersistenceManager pm, RawCommand rawCommand) throws DataSourceException {
        if (Source.simulated.equals(rawCommand.getSource())) {
            Consumer consumer = new Consumer();
            consumer.setName(rawCommand.getEmitterId());
            return consumer;
        }
        List<Consumer> consumers = null;
        if (Source.twitter.equals(rawCommand.getSource())) {
            consumers = consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, rawCommand.getEmitterId(), 1);
        }
        else if (Source.jabber.equals(rawCommand.getSource())) {
            consumers = consumerOperations.getConsumers(pm, Consumer.JABBER_ID, rawCommand.getEmitterId(), 1);
        }
        else {
            throw new DataSourceException("Provider " + rawCommand.getSource() + " not yet supported");
        }
        if (consumers.size() == 0) {
            throw new DataSourceException("Record for " + rawCommand.getSource().toString() + ": '" + rawCommand.getEmitterId() + "' not found!");
        }
        return consumers.get(0);
    }

    /**
     * Helper querying the data store to retrieve the Retailer account attached to the given Consumer one.
     * If no Retailer account is found, the error is reported that the corresponding action which required a Retailer account failed.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer Consumer account to consider
     * @param action Action involved for that lookup
     * @return The Retailer account
     *
     * @throws DataSourceException If the data retrieval fails
     * @throws ReservedOperationException If no account is returned
     */
    protected static Retailer retrieveRetailer(PersistenceManager pm, Consumer consumer, Action action) throws DataSourceException, ReservedOperationException {
        List<Retailer> retailers = retailerOperations.getRetailers(pm, Retailer.CONSUMER_KEY, consumer.getKey(), 1);
        if (retailers.size() == 0) {
            throw new ReservedOperationException(action);
        }
        return retailers.get(0);
    }

    /**
     * Dispatch the tweeted command according to the corresponding action
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer originator of the raw command
     * @param rawCommand raw command with emitter coordinates
     * @param command parsed command
     *
     * @throws DataSourceException If the communication with the back-end fails
     * @throws ClientException If information extracted from the tweet are incorrect
     */
    public static void processCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws DataSourceException, ClientException {
        Locale locale = consumer.getLocale();
        JsonObject prefixes = localizedPrefixes.get(locale);
        JsonObject actions = localizedActions.get(locale);
        // Prepare for locale dependent comparisons
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY);
        // Clear case of help being requested at the prefix level
        if (command.containsKey(Command.NEED_HELP)) {
            processHelpCommand(
                    rawCommand,
                    command.getString(Command.NEED_HELP),
                    locale,
                    collator
            );
            return;
        }
        String action = guessAction(command);
        try {
            // Alternate case of the help being asked as an action...
            if (CommandSettings.isEquivalentTo(prefixes, Prefix.help.toString(), action, collator)) {
                processHelpCommand(
                        rawCommand,
                        command.containsKey(Demand.CRITERIA) ? command.getJsonArray(Demand.CRITERIA).getString(0) : "",
                        locale,
                        collator
                );
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.cancel.toString(), action, collator)) {
                processCancelCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.close.toString(), action, collator)) {
                processCloseCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.confirm.toString(), action, collator)) {
                processConfirmCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.decline.toString(), action, collator)) {
                processDeclineCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.demand.toString(), action, collator)) {
                processDemandCommand(pm, consumer, rawCommand, command, prefixes, actions);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.list.toString(), action, collator)) {
                processListCommand(pm, consumer, rawCommand, command, prefixes, actions);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.propose.toString(), action, collator)) {
                processProposeCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.supply.toString(), action, collator)) {
                processSupplyCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.wish.toString(), action, collator)) {
                processWishCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.www.toString(), action, collator)) {
                processWWWCommand(pm, consumer, rawCommand, command);
            }
            else {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_parser_unsupported_action", new Object[] { action }, locale)
                );
            }
        }
        catch(ReservedOperationException ex) {
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get("cp_command_parser_reserved_action", new Object[] { action }, locale)
            );
        }
    }

    /**
     * Utility function extracting the action, even if the attribute is not present, by looking at all given parameters
     * @param command Set of command attributes
     * @return Specified or guessed action
     */
    protected static String guessAction(JsonObject command) {
        String action = command.getString(Command.ACTION);
        if (action == null) {
            if (command.containsKey(Demand.REFERENCE)) {
                action = command.size() == 1 ? Action.list.toString() : Action.demand.toString();
            }
            else if (command.containsKey(Store.STORE_KEY)) {
                action = command.size() == 1 ? Action.list.toString() : null; // No possibility to create/update/delete Store instance from Twitter
            }
            /* TODO: implement other listing variations
            else if (command.containsKey(Product.PRODUCT_KEY)) {
                action = command.size() == 1 ? Action.list.toString() : null; // No possibility to create/update/delete Store instance from Twitter
            }
            */
            else {
                action = Action.demand.toString();
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
     * @param collator for locale-dependent comparison
     *
     * @throws DataSourceException If sending the help message to the originator fails
     * @throws ClientException If the communication back with the user fails
     */
    protected static void processHelpCommand(RawCommand rawCommand, String arguments, Locale locale, Collator collator) throws DataSourceException, ClientException {
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
            JsonObject actions = localizedActions.get(locale);
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
            JsonObject states = localizedStates.get(locale);
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
            JsonObject helpKeywords = localizedHelpKeywords.get(locale);
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
        // getCache().put(keyword + locale.toString(), message);
        communicateToEmitter(rawCommand, message);
    }

    protected static void processCancelCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
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
                demand.setState(State.cancelled);
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
        if (command.containsKey(Demand.REFERENCE)) {
            Demand demand = null;
            try {
                demand = demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
                demand.setState(State.closed);
                demandOperations.updateDemand(pm, demand);
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_close_acknowledge_demand_closing", new Object[] { demand.getKey() }, consumer.getLocale())
                );
            }
            catch(Exception ex) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_close_invalid_demand_id", consumer.getLocale())
                );
            }
            if (demand !=  null) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(Proposal.DEMAND_KEY, demand.getKey());
                parameters.put(Command.STATE, State.confirmed.toString());
                try {
                    List<Proposal> proposals = proposalOperations.getProposals(pm, parameters, 1);
                    if (0 < proposals.size()) {
                        Proposal proposal = proposals.get(0);
                        Retailer retailer = retailerOperations.getRetailer(pm, proposal.getOwnerKey());
                        communicateToRetailer(
                                proposal.getSource(),
                                retailer,
                                LabelExtractor.get("cp_command_close_demand_closed_proposal_to_close", new Object[] { demand.getKey(), proposal.getKey() }, consumer.getLocale())
                        );
                    }
                }
                catch(Exception ex) {
                    // Too bad, the proposal owner can be informed about the demand closing...
                    // He/she can still do it without notification
                }
            }
        }
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Proposal proposal = null;
            try {
                Retailer retailer = retrieveRetailer(pm, consumer, Action.close);
                proposal = proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), retailer.getKey(), null);
                proposal.setState(State.closed);
                proposalOperations.updateProposal(pm, proposal);
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_close_acknowledge_proposal_closing", new Object[] { proposal.getKey() }, consumer.getLocale())
                );
            }
            catch(Exception ex) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_close_invalid_proposal_id", consumer.getLocale())
                );
            }
            if (proposal != null) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(Demand.PROPOSAL_KEYS, proposal.getKey());
                parameters.put(Command.STATE, State.confirmed.toString());
                try {
                    List<Demand> demands = demandOperations.getDemands(pm, parameters, 1);
                    if (0 < demands.size()) {
                        Demand demand = demands.get(0);
                        Consumer demandOwner = consumerOperations.getConsumer(pm, demand.getOwnerKey());
                        communicateToConsumer(
                                demand.getSource(),
                                demandOwner,
                                LabelExtractor.get("cp_command_close_proposal_closed_demand_to_close", new Object[] { demand.getKey(), proposal.getKey() }, consumer.getLocale())
                        );
                    }
                }
                catch(Exception ex) {
                    // Too bad, the demand owner can be informed about the proposal closing...
                    // He/she can still do it without notification
                }
            }
        }
        else {
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get("cp_command_close_invalid_parameters", consumer.getLocale())
            );
        }
    }

    protected static void processConfirmCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by the consumer to accept a proposal
        // Note that the proposal should refer to a demand owned by the consumer
        //
        // The consumer receives a notification with the store location
        // The retailer receives a notification with the confirmation and the suggestion to put the product aside for the consumer
        //
        Proposal proposal = null;
        Demand demand = null;
        try {
            // If there's no PROPOSAL_KEY attribute, it's going to generate an exception as the desired side-effect
            proposal = proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, null);
            // If the proposal is not for a demand the consumer owns, it's going to generate an exception as the desired side-effect
            demand = demandOperations.getDemand(pm, proposal.getDemandKey(), consumer.getKey());
        }
        catch(Exception ex) {
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get("cp_command_confirm_invalid_proposal_id", consumer.getLocale())
            );
        }
        if (demand != null) {
            if (!State.published.equals(demand.getState())) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_confirm_invalid_state_demand", new Object[] { proposal.getKey(), demand.getKey(), demand.getState().toString() }, consumer.getLocale())
                );
            }
            else {
                // Inform the consumer of the successful confirmation
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get(
                                "cp_command_confirm_acknowledge_confirmation",
                                new Object[] {
                                        proposal.getKey(),
                                        demand.getKey(),
                                        demand.getSerializedCriteria(),
                                        proposal.getStoreKey()
                                },
                                consumer.getLocale()
                        )
                );
                // Inform the retailer of the successful confirmation
                Retailer retailer = retailerOperations.getRetailer(pm, proposal.getOwnerKey());
                communicateToRetailer(
                        retailer.getPreferredConnection(),
                        retailer,
                        LabelExtractor.get(
                                "cp_command_confirm_inform_about_confirmation",
                                new Object[] {
                                        proposal.getKey(),
                                        proposal.getSerializedCriteria(),
                                        demand.getKey()
                                },
                                consumer.getLocale()
                        )
                );
                // Update the proposal and the demand states
                proposal.setState(State.confirmed);
                proposalOperations.updateProposal(pm, proposal);
                demand.setState(State.confirmed);
                demandOperations.updateDemand(pm, demand);
            }
        }
    }

    protected static void processDeclineCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by a consumer to refuse a proposal
        //
        throw new ClientException("Declining proposals - Not yet implemented");
    }

    public static void processDemandCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {
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
                demand.setState(State.opened); // Will force the re-validation of the entire demand
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
            List<Demand> demands = demandOperations.getDemands(pm, Command.OWNER_KEY, consumer.getKey(), 1);
            Demand latestDemand = null;
            if (0 < demands.size()) {
                latestDemand = demands.get(0);
                // Transfer the demand into a new object
                latestDemand = new Demand(latestDemand.toJson()); // To avoid attempts to persist the object
                // Reset sensitive fields
                latestDemand.resetKey();
                latestDemand.resetCoreDates();
                latestDemand.setAction(Action.demand);
                latestDemand.resetCriteria();
                latestDemand.setDefaultExpirationDate();
                latestDemand.setState(State.opened);
            }
            else {
                latestDemand = new Demand();
                // Set fields with default values
                latestDemand.setAction(Action.demand);
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
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Proposal proposal = null;
            Retailer retailer = retrieveRetailer(pm, consumer, Action.list);
            try {
                proposal = proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), retailer.getKey(), null);
            }
            catch(Exception ex) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_list_invalid_proposal_id", consumer.getLocale())
                );
            }
            if (proposal != null) {
                // Echo back the specified proposal
                communicateToEmitter(rawCommand, generateTweet(proposal, retailer.getLocale()));
            }
        }
        /* TODO: implement other listing variations
        else if (command.getString(Product.PRODUCT_KEY) != null) {
            throw new ClientException("Listing Stores - Not yet implemented");
        }
        else if (command.getString(Store.STORE_KEY) != null) {
            throw new ClientException("Listing Stores - Not yet implemented");
        }
        */
        else {
            // FIXME: select only {invalid, open, published, proposed} demands -- {canceled, closed} demands can be only listed with the Web console
            List<Demand> demands = demandOperations.getDemands(pm, Command.OWNER_KEY, consumer.getKey(), 0);
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

    protected static void processProposeCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by a retailer to:
        //
        // 1. create a new proposal
        // 2. update the identified proposal
        //
        Long proposalKey = 0L;
        Retailer retailer = retrieveRetailer(pm, consumer, Action.propose);
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            // Update the proposal attributes
            Proposal proposal = null;
            try {
                proposal = proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, retailer.getStoreKey());
            }
            catch(Exception ex) {
                communicateToEmitter(
                        rawCommand,
                        LabelExtractor.get("cp_command_proposal_invalid_proposal_id", consumer.getLocale())
                );
            }
            if (proposal != null) {
                proposal.fromJson(command);
                proposal.setState(State.opened); // Will force the re-validation of the entire proposal
                proposalOperations.updateProposal(pm, proposal);
                // Echo back the updated proposal
                communicateToEmitter(rawCommand, generateTweet(proposal, consumer.getLocale()));
                // Get the proposalKey for the task scheduling
                proposalKey = proposal.getKey();
            }
        }
        else {
            // Get the proposal attributes
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            // Persist the new proposal
            Proposal newProposal = proposalOperations.createProposal(pm, command, retailer);
            communicateToEmitter(
                    rawCommand,
                    LabelExtractor.get(
                            "cp_command_proposal_acknowledge_creation",
                            new Object[] { newProposal.getKey() },
                            consumer.getLocale()
                    )
            );
            communicateToEmitter(rawCommand, generateTweet(newProposal, consumer.getLocale()));
            // Get the proposalKey for the task scheduling
            proposalKey = newProposal.getKey();
        }

        // Create a task for that proposal
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(url("/API/maezel/validateOpenProposal").param(Proposal.KEY, proposalKey.toString()).method(Method.GET));
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
