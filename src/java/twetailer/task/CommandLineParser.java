package twetailer.task;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twetailer.ClientException;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Prefix;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class CommandLineParser {
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

            String tagKey = Prefix.tags.toString();
            String tagPattern = assembleModularPrefixes(prefixes.getJsonArray(tagKey), tagKey).toString();
            patterns.put("\\+" + tagKey, Pattern.compile("((?:\\+(?:" + tagPattern + "))[^\\:]+)(?: +[\\w\\+\\-]+:|$)", Pattern.CASE_INSENSITIVE));
            patterns.put("\\-" + tagKey, Pattern.compile("((?:\\-(?:" + tagPattern + "))[^\\:]+)(?: +[\\w\\+\\-]+:|$)", Pattern.CASE_INSENSITIVE));

            localizedPatterns.put(locale, patterns);
        }
    }

    /**
     * Builds a pattern for a regular expression which consider all characters after the third one as optional
     *
     * @param prefix character sequence to consider
     * @return Pattern for a regular expression with optional characters at the end, in form such as <code>tes(?:t(?:e(?:r)?)?)?</code> for the keyword tester
     */
    private static StringBuilder createPatternWithOptionalEndingCharacters(String prefix) {
        StringBuilder pattern = new StringBuilder();
        if (prefix.length() == 1) {
            return pattern.append("\\").append(prefix); // To be sure to escape potential RegExp commands
        }
        int prefixEnd = prefix.length();
        while(3 < prefixEnd) {
            prefixEnd --;
            pattern.insert(0, prefix.charAt(prefixEnd)).insert(0, "(?:").append(")?");
        }
        return pattern.insert(0, prefix.subSequence(0, prefixEnd)).append(":");
    }

    /**
     * Build a series of shorten-able patterns with the keyword equivalents
     *
     * @param equivalents List of equivalents accepted for the given keyword
     * @param keyword Keyword to process
     * @return Pattern for a regular expression a series of <code>tes(?:t(?:e(?:r)?)?)?<code> sequences separated by the pipe (<code>|</code>) sign
     */
    private static StringBuilder assembleModularPrefixes(JsonArray equivalents, String keyword) {
        StringBuilder pattern = createPatternWithOptionalEndingCharacters(equivalents.getString(0));
        for(int i = 1; i < equivalents.size(); i++) {
            pattern.append("|").append(createPatternWithOptionalEndingCharacters(equivalents.getString(i)));
        }
        return pattern;
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
        String keywordKey = keyword.toString();
        StringBuilder pattern = assembleModularPrefixes(keywords.getJsonArray(keywordKey), keywordKey);
        pattern = pattern.insert(0, "((?:").append(")").append(expression).append(")").append(separator);
        patterns.put(keywordKey, Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE));
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
                command.put(Demand.CRITERIA_ADD, new GenericJsonArray(getTags(currentGroup)));
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
                command.put(Demand.CRITERIA_REMOVE, new GenericJsonArray(getTags(currentGroup)));
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

}
