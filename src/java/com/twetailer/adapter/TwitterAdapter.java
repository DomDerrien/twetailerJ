package com.twetailer.adapter;

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
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.twetailer.ClientException;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.dto.Location;
import com.twetailer.dto.Settings;
import com.twetailer.rest.BaseOperations;
import com.twetailer.rest.ConsumerOperations;
import com.twetailer.rest.DemandOperations;
import com.twetailer.rest.LocationOperations;
import com.twetailer.rest.SettingsOperations;
import com.twetailer.validator.CommandSettings;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TwitterAdapter {
    private static final Logger log = Logger.getLogger(TwitterAdapter.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperation();
    protected DemandOperations demandOperations = _baseOperations.getDemandOperation();
    protected LocationOperations locationOperations = _baseOperations.getLocationOperation();
    protected SettingsOperations settingsOperations = _baseOperations.getSettingsOperation();

    private JsonObject possiblePrefixes;

    /**
     * Initialize the tweet parser and the response generator for the given locale
     * @param locale
     */
    public TwitterAdapter(Locale locale) {
        possiblePrefixes = CommandSettings.getPrefixes(locale);
        patterns = new HashMap<CommandSettings.Prefix, Pattern>();

        preparePattern(possiblePrefixes, CommandSettings.Prefix.action, "\\s*\\w+");
        preparePattern(possiblePrefixes, CommandSettings.Prefix.expiration, "\\s*[\\d\\-]+");
        preparePattern(possiblePrefixes, CommandSettings.Prefix.location, "\\s*(?:\\w+(?:\\s|-|\\.)+)+(?:ca|us)");
        preparePattern(possiblePrefixes, CommandSettings.Prefix.quantity, "\\s*\\d+");
        preparePattern(possiblePrefixes, CommandSettings.Prefix.reference, "\\s*\\d+");
        preparePattern(possiblePrefixes, CommandSettings.Prefix.range, "\\s*\\d+\\s*(?:mi|km)");
        preparePattern(possiblePrefixes, CommandSettings.Prefix.tags, "?(?:\\w\\s*)+");
    }

    private Map<CommandSettings.Prefix, Pattern> patterns;

    private String createPatternWithOptionalEndingCharacters(String prefix) {
        if (prefix.length() == 1) {
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
     * @param keyword identifier of the prefix to process
     * @param expression part of the regular expression that extracts the parameters for the identified prefix
     */
    private void preparePattern(JsonObject keywords, CommandSettings.Prefix keyword, String expression) {
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
     * @return JsonObject with all message attributes correctly extracted from the given message
     * @throws ClientException If the query is malformed
     * @throws ParseException if a date format is invalid
     */
    public JsonObject parseTweet (String message) throws ClientException, ParseException {
        Matcher matcher;
        boolean oneFieldOverriden = false;
        log.warning("Message to parse: " + message);
        JsonObject command = new GenericJsonObject();
        // Action
        try {
            matcher = patterns.get(CommandSettings.Prefix.action).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            command.put(Demand.ACTION, getAction(currentGroup.toLowerCase()));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Default action is !demand
            command.put(Demand.ACTION, CommandSettings.Action.demand.toString());
        }
        // Expiration
        try {
            matcher = patterns.get(CommandSettings.Prefix.expiration).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            command.put(Demand.EXPIRATION_DATE, getDate(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {}
        // Locale
        try {
            matcher = patterns.get(CommandSettings.Prefix.location).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            command.put(Location.COUNTRY_CODE, getCountryCode(currentGroup).toUpperCase());
            command.put(Location.POSTAL_CODE, getPostalCode(currentGroup, command.getString(Location.COUNTRY_CODE)).toUpperCase());
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {}
        // Quantity
        try {
            matcher = patterns.get(CommandSettings.Prefix.quantity).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            command.put(Demand.QUANTITY, getQuantity(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {}
        // Reference
        try {
            matcher = patterns.get(CommandSettings.Prefix.reference).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            command.put(Demand.KEY, getQuantity(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {}
        // Range
        try {
            matcher = patterns.get(CommandSettings.Prefix.range).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            command.put(Demand.RANGE_UNIT, getRangeUnit(currentGroup).toLowerCase());
            command.put(Demand.RANGE, getRange(currentGroup, command.getString(Demand.RANGE_UNIT)));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {}
        // Tags
        try {
            matcher = patterns.get(CommandSettings.Prefix.tags).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            command.put(Demand.CRITERIA, new GenericJsonArray(getTags(currentGroup)));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
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
     * @return Serialized command
     */
    public String generateTweet(Demand demand, Location location, Locale locale) {
        final String space = " ";
        StringBuilder tweet = new StringBuilder();
        // FIXME: use the given locale to generate localized messages!
        tweet.append(CommandSettings.Prefix.action).append(":").append(demand.getAction()).append(space);
        if (demand.getKey() != null) {
            tweet.append(CommandSettings.Prefix.reference).append(":").append(demand.getKey()).append(space);
        }
        tweet.append(CommandSettings.Prefix.state).append(":").append(demand.getState()).append(space);
        // FIXME: update DateUtils with a good formatter ;)
        tweet.append(CommandSettings.Prefix.expiration).append(":").append(DateUtils.dateToISO(demand.getExpirationDate()).substring(0, 10)).append(space);
        if (location != null && location.getPostalCode() != null && location.getCountryCode() != null) {
            tweet.append(CommandSettings.Prefix.location).append(":").append(location.getPostalCode()).append(space).append(location.getCountryCode()).append(space);
        }
        tweet.append(CommandSettings.Prefix.range).append(":").append(demand.getRange()).append(demand.getRangeUnit()).append(space);
        tweet.append(CommandSettings.Prefix.quantity).append(":").append(demand.getQuantity()).append(space);
        if (demand.getCriteria() != null && 0 < demand.getCriteria().size()) {
            tweet.append(CommandSettings.Prefix.tags).append(":");
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
     * @throws ParseException
     * @throws ClientException
     */
    public Long processDirectMessages() throws TwitterException, DataSourceException, ParseException, ClientException {
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
     * @throws ParseException
     * @throws ClientException
     */
    @SuppressWarnings("deprecation")
    protected Long processDirectMessages(PersistenceManager pm, Long sinceId) throws DataSourceException, TwitterException {
        long lastId = sinceId;
        // Get the list of direct messages
        List<DirectMessage> messages = TwitterUtils.getDirectMessages(sinceId);
        String referenceLabel = possiblePrefixes.getJsonArray(CommandSettings.Prefix.reference.toString()).getString(0);
        // Process each messages one-by-one
        int idx = messages == null ? 0 : messages.size(); // To start by the end of the message queue
        log.warning(idx + " direct messages to process (temporary limited to 1 DM per operation)"); // FIXME: remove the limitation of 1 DM processed at a time
        while (0 < idx) {
            --idx;
            DirectMessage dm = messages.get(idx);
            long dmId = dm.getId();
            // Get Twetailer account
            twitter4j.User sender = dm.getSender();
            Consumer consumer = consumerOperations.createConsumer(pm, sender); // Creation only occurs if the corresponding Consumer instance is not retrieved
            Locale senderLocale = consumer.getLocale();
            String senderScreenName = dm.getSenderScreenName();
            if (!sender.isFollowing()) {
                TwitterUtils.sendPublicMessage(LabelExtractor.get("ta_messageToNonFollower", new Object[] { senderScreenName }, senderLocale));
                break;
            }
            // Evaluate the new demand
            try {
                JsonObject newCommand = parseTweet(dm.getText());
                String newAction = newCommand.getString(Demand.ACTION);
                Long refAttr = newCommand.getLong(Demand.KEY);
                if (CommandSettings.isAction(CommandSettings.Action.help, newAction, senderLocale)) {
                    JsonArray tags = newCommand.getJsonArray(Demand.CRITERIA);
                    if (tags.size() == 0) {
                        TwitterUtils.sendDirectMessage(senderScreenName, "Help: type \"!help <action>\" to get contextuel help per action one verb at a time. Or \"!help help\" to get the list of supported commands.");
                    }
                    else if (CommandSettings.Action.demand.toString().equals(tags.getString(0))) {
                        TwitterUtils.sendDirectMessage(senderScreenName, "Help: \"demand\" is the default action, used to create or update a demand.");
                    }
                    // FIXME: implement all variations
                    else {
                        TwitterUtils.sendDirectMessage(senderScreenName, "Help: command not supported. Current set: {demand, list, cancel}.");
                    }
                }
                else if (refAttr == 0L) {
                    newCommand.put(Demand.TWEET_ID, dmId);
                    if(CommandSettings.isAction(CommandSettings.Action.demand, newAction, senderLocale)) {
                        // Extracts the new location
                        Location newLocation = locationOperations.createLocation(pm, newCommand);
                        Long newLocationKey = newLocation == null ? consumer.getLocationKey() : newLocation.getKey();
                        // Get the latest demand or the default one
                        List<Demand> demands = demandOperations.getDemands(pm, Demand.CONSUMER_KEY, consumer.getKey(), 1);
                        Demand latestDemand = null;
                        if (0 < demands.size()) {
                            latestDemand = demands.get(0);
                            // Reset sensitive fields
                            latestDemand.resetKey();
                            latestDemand.resetCoreDates();
                            latestDemand.setAction(CommandSettings.Action.demand);
                            latestDemand.setCriteria(null);
                            latestDemand.setDefaultExpirationDate();
                            latestDemand.setState(CommandSettings.State.open);
                        }
                        else {
                            latestDemand = new Demand();
                            // Set fields with default values
                            latestDemand.setAction(CommandSettings.Action.demand);
                        }
                        // Update of the latest command (can be the default one) with the just extracted parameters
                        newCommand = latestDemand.fromJson(newCommand).toJson();
                        if (newLocationKey != null && newLocationKey != newCommand.getLong(Demand.LOCATION_KEY)) {
                            newCommand.put(Demand.LOCATION_KEY, newLocationKey);
                        }
                        // Persist the new demand
                        Demand newDemand = demandOperations.createDemand(pm, newCommand, consumer.getKey());
                        TwitterUtils.sendDirectMessage(
                                senderScreenName,
                                LabelExtractor.get(
                                        "ta_acknowledgeDemandCreated",
                                        new Object[] { newDemand.getKey() },
                                        senderLocale
                                )
                        );
                        Location location = newDemand.getLocationKey() == null ? null : locationOperations.getLocation(pm, newDemand.getLocationKey());
                        TwitterUtils.sendDirectMessage(senderScreenName, generateTweet(newDemand, location, senderLocale));
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.list, newAction, senderLocale)) {
                        // FIXME: list only active demands
                        List<Demand> demands = demandOperations.getDemands(pm, Demand.CONSUMER_KEY, consumer.getKey(), 0);
                        if (demands.size() == 0) {
                            TwitterUtils.sendDirectMessage(
                                    senderScreenName,
                                    LabelExtractor.get(
                                            "ta_responseToListCommandForNoResult",
                                            senderLocale
                                    )
                            );
                        }
                        else {
                            TwitterUtils.sendDirectMessage(
                                    senderScreenName,
                                    LabelExtractor.get(
                                            "ta_introductionToResponseToListCommandWithManyResults",
                                            new Object[] { demands.size() },
                                            senderLocale
                                    )
                            );
                            for (Demand demand: demands) {
                                Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                                TwitterUtils.sendDirectMessage(senderScreenName, generateTweet(demand, location, senderLocale));
                            }
                        }
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.shop, newAction, senderLocale)) {
                        throw new ClientException("Shop creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.supply, newAction, senderLocale)) {
                        throw new ClientException("Supply creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.wish, newAction, senderLocale)) {
                        throw new ClientException("Wish creation: not yet implemented");
                    }
                    else {
                        throw new ClientException("Command " + newAction + " might be applied to existing resource with the required " + referenceLabel + " parameter.");
                    }
                }
                else { // if (_refAttr != 0L) {
                    if (CommandSettings.isAction(CommandSettings.Action.cancel, newAction, senderLocale)) {
                        // Update demand state
                        Demand demand = demandOperations.getDemand(pm, refAttr, consumer.getKey());
                        demand.setState(CommandSettings.State.canceled);
                        demandOperations.updateDemand(pm, demand);
                        // Echo back the updated demand
                        Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                        TwitterUtils.sendDirectMessage(senderScreenName, generateTweet(demand, location, senderLocale));
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.close, newAction, senderLocale)) {
                        throw new ClientException("Closing identified command: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.confirm, newAction, senderLocale)) {
                        throw new ClientException("Confirming identified command: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.decline, newAction, senderLocale)) {
                        throw new ClientException("Declining identified command: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.demand, newAction, senderLocale)) {
                        // Update the demand attributes
                        Demand demand = demandOperations.getDemand(pm, refAttr, consumer.getKey());
                        demand.fromJson(newCommand);
                        demand.setState(CommandSettings.State.open); // Will force the re-validation of the entire demand
                        demand.resetProposalKeys(); // All existing proposals are removed
                        demandOperations.updateDemand(pm, demand);
                        // Echo back the updated demand
                        Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                        TwitterUtils.sendDirectMessage(senderScreenName, generateTweet(demand, location, senderLocale));
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.list, newAction, senderLocale)) {
                        Demand demand = demandOperations.getDemand(pm, refAttr, consumer.getKey());
                        if (demand != null) {
                            // Echo back the specified demand
                            Location location = demand.getLocationKey() == null ? null : locationOperations.getLocation(pm, demand.getLocationKey());
                            TwitterUtils.sendDirectMessage(senderScreenName, generateTweet(demand, location, senderLocale));
                        }
                        else {
                            TwitterUtils.sendDirectMessage(
                                    senderScreenName,
                                    LabelExtractor.get(
                                            "ta_responseToSpecificListCommandForNoResult",
                                            new Object[] { refAttr },
                                            senderLocale
                                    )
                            );
                        }
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.propose, newAction, senderLocale)) {
                        throw new ClientException("Proposing identified command: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.shop, newAction, senderLocale)) {
                        throw new ClientException("Shop creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.supply, newAction, senderLocale)) {
                        throw new ClientException("Supply creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.wish, newAction, senderLocale)) {
                        throw new ClientException("Wish creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.www, newAction, senderLocale)) {
                        throw new ClientException("www/web: not yet implemented");
                    }
                    else {
                        throw new ClientException("Command " + newAction + " is not supported.");
                    }
                }
            }
            catch(Exception ex) {
                // TODO: use localized message
                // ex.printStackTrace();
                TwitterUtils.sendDirectMessage(senderScreenName, "Error: " + ex.getMessage());
            }
            if (lastId < dmId) {
                lastId = dmId;
            }
            break; // FIXME: remove the limitation of 1 DM processed at a time
        }
        return Long.valueOf(lastId);
    }
}