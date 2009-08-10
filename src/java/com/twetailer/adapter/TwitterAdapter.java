package com.twetailer.adapter;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.twetailer.ClientException;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.dto.Settings;
import com.twetailer.j2ee.ConsumersServlet;
import com.twetailer.j2ee.DemandsServlet;
import com.twetailer.j2ee.SettingsServlet;
import com.twetailer.validator.CommandSettings;

public class TwitterAdapter {

    private static final Logger log = Logger.getLogger(TwitterAdapter.class.getName());

    /**
     * Default constructor using English keywords for the tweet parser and the response generator
     * @deprecated
     */
    public TwitterAdapter() {
        this(Locale.US);
    }

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
            command.put(Demand.COUNTRY_CODE, getCountryCode(currentGroup).toUpperCase());
            command.put(Demand.POSTAL_CODE, getPostalCode(currentGroup, command.getString(Demand.COUNTRY_CODE)).toUpperCase());
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
    public String generateTweet(JsonObject command) {
        final String space = " ";
        StringBuilder tweet = new StringBuilder();
        if (command.containsKey(Demand.ACTION)) {
            tweet.append(CommandSettings.Prefix.action).append(":").append(command.getString(Demand.ACTION)).append(space);
        }
        if (command.containsKey(Demand.KEY)) {
            tweet.append(CommandSettings.Prefix.reference).append(":").append(command.getLong(Demand.KEY)).append(space);
        }
        if (command.containsKey(Demand.STATE)) {
            tweet.append(CommandSettings.Prefix.state).append(":").append(command.getString(Demand.STATE)).append(space);
        }
        if (command.containsKey(Demand.EXPIRATION_DATE)) {
            tweet.append(CommandSettings.Prefix.expiration).append(":").append(command.getString(Demand.EXPIRATION_DATE).substring(0, 10)).append(space);
        }
        if (command.containsKey(Demand.POSTAL_CODE) && command.containsKey(Demand.COUNTRY_CODE)) {
            tweet.append(CommandSettings.Prefix.location).append(":").append(command.getString(Demand.POSTAL_CODE)).append(space).append(command.getString(Demand.COUNTRY_CODE)).append(space);
        }
        if (command.containsKey(Demand.RANGE_UNIT) && command.containsKey(Demand.RANGE)) {
            tweet.append(CommandSettings.Prefix.range).append(":").append(command.getDouble(Demand.RANGE)).append(command.getString(Demand.RANGE_UNIT)).append(space);
        }
        if (command.containsKey(Demand.QUANTITY)) {
            tweet.append(CommandSettings.Prefix.quantity).append(":").append(command.getLong(Demand.QUANTITY)).append(space);
        }
        if (command.containsKey(Demand.CRITERIA)) {
            tweet.append(CommandSettings.Prefix.tags).append(":");
            JsonArray keywords = command.getJsonArray(Demand.CRITERIA);
            int limit = keywords.size();
            for (int i=0; i<limit; i++) {
                tweet.append(keywords.getString(i)).append(space);
            }
        }
        return tweet.toString();
    }

    private Twitter _twitterAccount;

    /**
     * Accessor provided for unit tests
     * @return Twitter account controller
     */
    public Twitter getTwitterAccount() {
        if (_twitterAccount == null) {
            _twitterAccount = new Twitter("twtlr", "twetailer@shortcut0");
        }
        return _twitterAccount;
    }

    public void sendDirectMessage(Twitter twitterAccount, String recipientScreenName, String message) throws TwitterException {
        log.warning("Before sending a DM to " + recipientScreenName + ": " + message);
        twitterAccount.sendDirectMessage(recipientScreenName, message);
        log.warning("DM successfully sent!");
    }

    private ConsumersServlet _consumersServlet;

    /**
     * Accessor provided for unit tests
     * @return Consumer servlet instance
     */
    public ConsumersServlet getConsumersServlet() {
        if (_consumersServlet == null) {
            _consumersServlet = new ConsumersServlet();
        }
        return _consumersServlet;
    }

    private DemandsServlet _demandsServlet;

    /**
     * Accessor provided for unit tests
     * @return Demand servlet instance
     */
    public DemandsServlet getDemandsServlet() {
        if (_demandsServlet == null) {
            _demandsServlet = new DemandsServlet();
        }
        return _demandsServlet;
    }

    private SettingsServlet _settingsServlet;

    /**
     * Accessor provided for unit tests
     * @return Settings servlet instance
     */
    public SettingsServlet getSettingsServlet() {
        if (_settingsServlet == null) {
            _settingsServlet = new SettingsServlet();
        }
        return _settingsServlet;
    }

    public Long processDirectMessages() throws TwitterException, DataSourceException, ParseException, ClientException {
        SettingsServlet settingsServlet = getSettingsServlet();
        Settings settings = settingsServlet.getSettings();
        Long sinceId = settings.getLastProcessDirectMessageId();
        log.warning("Last process DM id: " + sinceId);
        Long lastId = processDirectMessages(sinceId);
        if (!lastId.equals(sinceId)) {
            log.warning("New value for the process DM id: " + lastId);
            settings.setLastProcessDirectMessageId(lastId);
            settingsServlet.updateSettings(settings);
        }
        return lastId;
    }

    /**
     * Extract commands from the pending Direct Messages and acts accordingly
     *
     * @param sinceId identifier of the last process direct message
     * @return Updated direct message identifier if new DMs have been processed, or the given one if none has been processed
     *
     * @throws TwitterException
     * @throws DataSourceException
     * @throws ParseException
     * @throws ClientException
     */
    protected Long processDirectMessages(Long sinceId) throws TwitterException, DataSourceException, ParseException, ClientException {
        return processDirectMessages(sinceId, Locale.ENGLISH);
    }

    @SuppressWarnings("deprecation")
    protected Long processDirectMessages(Long sinceId, Locale locale) throws TwitterException, DataSourceException, ParseException, ClientException {
        long lastId = sinceId;
        // Get the list of direct messages
        Twitter twitterAccount = getTwitterAccount();
        log.warning("Before getting new direct messages from Twitter, after the message id: " + sinceId);
        List<DirectMessage> messages = twitterAccount.getDirectMessages(new Paging(1, 2, sinceId));
        String referenceLabel = possiblePrefixes.getJsonArray(CommandSettings.Prefix.reference.toString()).getString(0);
        // Process each messages one-by-one
        int idx = messages == null ? 0 : messages.size(); // To start by the end of the message queue
        log.warning(idx + " direct messages to process (temporary limited to 1 DM per operation)");
        while (0 < idx) {
            --idx;
            DirectMessage dm = messages.get(idx);
            long dmId = dm.getId();
            // Get Twetailer account
            twitter4j.User sender = dm.getSender();
            Consumer consumer = checkConsumer(sender);
            String senderScreenName = dm.getSenderScreenName();
            if (!sender.isFollowing()) {
                twitterAccount.updateStatus(LabelExtractor.get("ta_messageToNonFollower", new Object[] { senderScreenName }, locale));
                break;
            }
            // Evaluate the new demand
            try {
                JsonObject newCommand = parseTweet(dm.getText());
                String newAction = newCommand.getString(Demand.ACTION);
                Long refAttr = newCommand.getLong(Demand.KEY);
                if (CommandSettings.isAction(CommandSettings.Action.help, newAction, locale)) {
                    JsonArray tags = newCommand.getJsonArray(Demand.CRITERIA);
                    if (tags.size() == 0) {
                        sendDirectMessage(twitterAccount, senderScreenName, "Help: type \"!help <action>\" to get contextuel help per action one verb at a time. Or \"!help help\" to get the list of supported commands.");
                    }
                    else if (CommandSettings.Action.demand.toString().equals(tags.getString(0))) {
                        sendDirectMessage(twitterAccount, senderScreenName, "Help: \"demand\" is the default action, used to create or update a demand.");
                    }
                    // FIXME: implement all variations
                    else {
                        sendDirectMessage(twitterAccount, senderScreenName, "Help: command not supported. Current set: {demand, list, cancel}.");
                    }
                }
                else if (refAttr == 0L) {
                    newCommand.put(Demand.TWEET_ID, dmId);
                    if(CommandSettings.isAction(CommandSettings.Action.demand, newAction, locale)) {
                        DemandsServlet demandsServlet = getDemandsServlet();
                        // Get the latest demand or the default one
                        List<Demand> demands = demandsServlet.getDemands(Demand.CONSUMER_KEY, consumer.getKey(), 1);
                        Demand latestDemand = null;
                        if (0 < demands.size()) {
                            latestDemand = demands.get(0);
                            // Reset sensitive fields
                            latestDemand.setAction(CommandSettings.Action.demand);
                            latestDemand.setCreationDate(DateUtils.getNowDate());
                            latestDemand.setCriteria(null);
                            Date latestCreationDate = latestDemand.getCreationDate();
                            Date latestExpirationDate = latestDemand.getExpirationDate();
                            long latestExpirationDelay = (latestExpirationDate.getTime() - latestCreationDate.getTime()) / 24 / 60 / 60 / 1000;
                            latestDemand.setExpirationDate((int) latestExpirationDelay);
                            latestDemand.resetKey();
                            latestDemand.setModificationDate(DateUtils.getNowDate());
                            latestDemand.setState(CommandSettings.State.open);
                        }
                        else {
                            latestDemand = new Demand();
                            // Set fields with default values
                            latestDemand.setAction(CommandSettings.Action.demand);
                            latestDemand.setExpirationDate(30);
                            latestDemand.setQuantity(1L);
                            latestDemand.setRange(25.0D);
                            latestDemand.setRangeUnit("km");
                        }
                        // Update of the latest command (can be the default one) with the just extracted parameters
                        newCommand = latestDemand.fromJson(newCommand).toJson();
                        // Persist the new demand
                        Demand newDemand = demandsServlet.createDemand(newCommand, consumer.getKey());
                        if (newDemand.getState() == CommandSettings.State.open) {
                            sendDirectMessage(
                                    twitterAccount,
                                    senderScreenName,
                                    LabelExtractor.get(
                                            "ta_acknowledgeIncompleteDemandCreated",
                                            new Object[] { newDemand.getKey() },
                                            locale
                                    )
                            );
                        }
                        else {
                            sendDirectMessage(
                                    twitterAccount,
                                    senderScreenName,
                                    LabelExtractor.get(
                                            "ta_acknowledgeDemandCreated",
                                            new Object[] { newDemand.getKey() },
                                            locale
                                    )
                            );
                        }
                        sendDirectMessage(twitterAccount, senderScreenName, generateTweet(newDemand.toJson()));
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.list, newAction, locale)) {
                        // FIXME: list only active demands
                        DemandsServlet demandsServlet = getDemandsServlet();
                        List<Demand> demands = demandsServlet.getDemands(Demand.CONSUMER_KEY, consumer.getKey(), 0);
                        if (demands.size() == 0) {
                            sendDirectMessage(
                                    twitterAccount,
                                    senderScreenName,
                                    LabelExtractor.get(
                                            "ta_responseToListCommandForNoResult",
                                            locale
                                    )
                            );
                        }
                        else {
                            sendDirectMessage(
                                    twitterAccount,
                                    senderScreenName,
                                    LabelExtractor.get(
                                            "ta_introductionToResponseToListCommandWithManyResults",
                                            new Object[] { demands.size() },
                                            locale
                                    )
                            );
                            for (Demand demand: demands) {
                                sendDirectMessage(twitterAccount, senderScreenName, generateTweet(demand.toJson()));
                            }
                        }
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.shop, newAction, locale)) {
                        throw new ClientException("Shop creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.supply, newAction, locale)) {
                        throw new ClientException("Supply creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.wish, newAction, locale)) {
                        throw new ClientException("Wish creation: not yet implemented");
                    }
                    else {
                        throw new ClientException("Command " + newAction + " might be applied to existing resource with the required " + referenceLabel + " parameter.");
                    }
                }
                else { // if (_refAttr != 0L) {
                    if (CommandSettings.isAction(CommandSettings.Action.cancel, newAction, locale)) {
                        DemandsServlet demandsServlet = getDemandsServlet();
                        PersistenceManager pm = demandsServlet.getPersistenceManager();
                        try {
                            // Update demand state
                            Demand demand = demandsServlet.getDemand(pm, refAttr, consumer.getKey());
                            demand.setState(CommandSettings.State.canceled);
                            demandsServlet.updateDemand(pm, demand);
                            // Echo back the updated demand
                            sendDirectMessage(twitterAccount, senderScreenName, generateTweet(demand.toJson()));
                        }
                        finally {
                            pm.close();
                        }
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.close, newAction, locale)) {
                        throw new ClientException("Closing identified command: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.confirm, newAction, locale)) {
                        throw new ClientException("Confirming identified command: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.decline, newAction, locale)) {
                        throw new ClientException("Declining identified command: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.demand, newAction, locale)) {
                        DemandsServlet demandsServlet = getDemandsServlet();
                        PersistenceManager pm = demandsServlet.getPersistenceManager();
                        try {
                            // Update the demand attributes
                            Demand demand = demandsServlet.getDemand(pm, refAttr, consumer.getKey());
                            demand.fromJson(newCommand);
                            demandsServlet.updateDemand(pm, demand);
                            // Echo back the updated demand
                            sendDirectMessage(twitterAccount, senderScreenName, generateTweet(demand.toJson()));
                        }
                        finally {
                            pm.close();
                        }
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.list, newAction, locale)) {
                        DemandsServlet demandsServlet = getDemandsServlet();
                        Demand demand = demandsServlet.getDemand(refAttr, consumer.getKey());
                        if (demand != null) {
                            // Echo back the specified demand
                            sendDirectMessage(twitterAccount, senderScreenName, generateTweet(demand.toJson()));
                        }
                        else {
                            sendDirectMessage(
                                    twitterAccount,
                                    senderScreenName,
                                    LabelExtractor.get(
                                            "ta_responseToSpecificListCommandForNoResult",
                                            new Object[] { refAttr },
                                            locale
                                    )
                            );
                        }
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.propose, newAction, locale)) {
                        throw new ClientException("Proposing identified command: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.shop, newAction, locale)) {
                        throw new ClientException("Shop creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.supply, newAction, locale)) {
                        throw new ClientException("Supply creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.wish, newAction, locale)) {
                        throw new ClientException("Wish creation: not yet implemented");
                    }
                    else if (CommandSettings.isAction(CommandSettings.Action.www, newAction, locale)) {
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
                sendDirectMessage(twitterAccount, senderScreenName, "Error: " + ex.getMessage());
            }
            if (lastId < dmId) {
                lastId = dmId;
            }
            break; // FIXME: temporary limitation to process one DM at a time :(
        }
        return Long.valueOf(lastId);
    }

    /**
     * Return the Consumer instance representing the Twitter user, an instance that may have been just created if the Twitter user is a new one
     * @param user Twitter user
     * @return Consumer instance that represents the Twitter user
     * @throws DataSourceException If the Consumer cannot be retrieved or created
     */
    protected Consumer checkConsumer(twitter4j.User user) throws DataSourceException {
        ConsumersServlet consumersServlet = getConsumersServlet();
        Consumer consumer = consumersServlet.getConsumer(Consumer.TWITTER_ID, Long.valueOf(user.getId()));
        if (consumer == null) {
            consumer = consumersServlet.createConsumer(user);
        }
        return consumer;
    }
}