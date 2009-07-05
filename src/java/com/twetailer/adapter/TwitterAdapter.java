package com.twetailer.adapter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.domderrien.i18n.DateUtils;
import org.domderrien.jsontools.GenericJsonArray;
import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonException;
import org.domderrien.jsontools.JsonObject;

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
import com.twetailer.settings.CommandSettings;

public class TwitterAdapter {
   
    private static final Logger log = Logger.getLogger(TwitterAdapter.class.getName());

    /**
     * Default constructor using English keywords for the tweet parser and the response generator
     */
    public TwitterAdapter() throws JsonException {
        this(Locale.US);
    }

    private JsonObject possibleActions;

    /**
     * Initialize the tweet parser and the response generator for the given locale
     * @param locale
     * @throws JsonException
     */
    public TwitterAdapter(Locale locale) throws JsonException {
        this(CommandSettings.getPrefixes(locale));
        possibleActions = CommandSettings.getActions(locale);
    }
    
    private Map<CommandSettings.Prefix, Pattern> patterns;

    /**
     * Extract the localized prefix matching the keywords (long and short versions)
     * and insert the corresponding regular expression pattern in the pattern list
     * 
     * @param keywords set of localized prefix labels
     * @param keyword identifier of the prefix to process
     * @param expression part of the regular expression that extracts the parameters for the identified prefix
     */
    private void preparePattern(JsonObject keywords, CommandSettings.Prefix keyword, String expression) {
        String longPrefix = keywords.getJsonArray(keyword.toString()).getString(0);
        String shortPrefix = keywords.getJsonArray(keyword.toString()).getString(1);
        patterns.put(keyword, Pattern.compile("((?:" + longPrefix + "|" + shortPrefix + ")" + expression + ")", Pattern.CASE_INSENSITIVE));
    }

    /**
     * Initialize the adapter for a given set of localized command prefixes
     * 
     * @param keywords set of localized prefix labels
     */
    protected TwitterAdapter(JsonObject keywords) throws JsonException {
        patterns = new HashMap<CommandSettings.Prefix, Pattern>();
        
        preparePattern(keywords, CommandSettings.Prefix.action, "\\s*\\w+");
        preparePattern(keywords, CommandSettings.Prefix.expiration, "\\s*[\\d\\-]+");
        preparePattern(keywords, CommandSettings.Prefix.location, "\\s*(?:\\w+(?:\\s|-|\\.)+)+(?:ca|us)");
        preparePattern(keywords, CommandSettings.Prefix.quantity, "\\s*\\d+");
        preparePattern(keywords, CommandSettings.Prefix.reference, "\\s*\\d+");
        preparePattern(keywords, CommandSettings.Prefix.range, "\\s*\\d+\\s*(?:mi|km)");
        preparePattern(keywords, CommandSettings.Prefix.tags, "?(?:\\w\\s*)+");
    }

    /**
     * Parse the given tweet with the set of given localized prefixes
     * 
     * @param message message are returned by the Twitter page
     * @return JsonObject with all message attributes correctly extracted from the given message
     * @throws ClientException If the query is malformed
     * @throws ParseException if a date format is invalid
     */
    public JsonObject parseMessage (String message) throws ClientException, ParseException {
        JsonObject demand = new GenericJsonObject();
        return parseMessage(message, demand);
    }

    /**
     * Parse the given tweet with the set of given localized prefixes
     * 
     * @param message message are returned by the Twitter page
     * @param demand can be fresh object or one with default values
     * @return JsonObject with all message attributes correctly extracted from the given message
     * @throws ClientException If the query is malformed
     * @throws ParseException if a date format is invalid
     */
    public JsonObject parseMessage (String message, JsonObject demand) throws ClientException, ParseException {
        Matcher matcher;
        boolean oneFieldOverriden = false;
        log.warning("Message to parse: " + message);
        // Action
        try {
            matcher = patterns.get(CommandSettings.Prefix.action).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.action), getAction(currentGroup.toLowerCase()));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Default settings
            String actionLabelForDemand = possibleActions.getJsonArray(CommandSettings.Action.demand.toString()).getString(0);
            demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.action), actionLabelForDemand);
        }
        // Expiration
        try {
            matcher = patterns.get(CommandSettings.Prefix.expiration).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.expiration), getDate(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Default settings
            if (!demand.containsKey(Demand.getAttributeLabel(CommandSettings.Prefix.expiration))) {
                Calendar now = DateUtils.getNowCalendar();
                now.set(Calendar.MONTH, now.get(Calendar.MONTH) + 1);
                demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.expiration), DateUtils.dateToISO(now.getTime()));
            }
        }
        // Locale
        try {
            matcher = patterns.get(CommandSettings.Prefix.location).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.COUNTRY_CODE, getCountryCode(currentGroup).toUpperCase());
            demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.location), getPostalCode(currentGroup, demand.getString(Demand.COUNTRY_CODE)).toUpperCase());
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Mandatory attribute that cannot be defaulted
        }
        // Quantity
        try {
            matcher = patterns.get(CommandSettings.Prefix.quantity).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.quantity), getQuantity(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            String label = Demand.getAttributeLabel(CommandSettings.Prefix.quantity);
            if (!demand.containsKey(label) || demand.getLong(label) == 0L) {
                demand.put(label, 1L);
            }
        }
        // Reference
        demand.remove(Demand.getAttributeLabel(CommandSettings.Prefix.reference)); // Reset
        try {
            matcher = patterns.get(CommandSettings.Prefix.reference).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.reference), getQuantity(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        // Range
        try {
            matcher = patterns.get(CommandSettings.Prefix.range).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.RANGE_UNIT, getRangeUnit(currentGroup).toLowerCase());
            demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.range), getRange(currentGroup, demand.getString(Demand.RANGE_UNIT)));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Default settings
            if (!demand.containsKey(Demand.getAttributeLabel(CommandSettings.Prefix.range))) { demand.put(Demand.RANGE, 25.0D); }
            if (!demand.containsKey(Demand.RANGE_UNIT)) { demand.put(Demand.RANGE_UNIT, "km"); }
        }
        // Tags
        demand.remove(Demand.getAttributeLabel(CommandSettings.Prefix.tags)); // Reset
        try {
            matcher = patterns.get(CommandSettings.Prefix.tags).matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.getAttributeLabel(CommandSettings.Prefix.tags), new GenericJsonArray(getTags(currentGroup)));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        if (!oneFieldOverriden) {
            throw new ClientException("No query field has been correctly extracted");
        }
        if ((!demand.containsKey(Demand.getAttributeLabel(CommandSettings.Prefix.reference)) || demand.getLong(Demand.getAttributeLabel(CommandSettings.Prefix.reference)) == 0L) && (!demand.containsKey(Demand.getAttributeLabel(CommandSettings.Prefix.location)) || demand.getString(Demand.getAttributeLabel(CommandSettings.Prefix.location)) == null)) {
            log.finest(demand.toString());
            throw new ClientException("New demand must have an expiration date and a location");
        }
        return demand;
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
     * @return valid country code
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
        if (lastId != sinceId) {
            log.warning("New value for the process DM id: " + lastId);
            settings.setLastProcessDirectMessageId(lastId);
            settingsServlet.updateSettings(settings);
        }
        return lastId;
    }
    
    public Long processDirectMessages(Long sinceId) throws TwitterException, DataSourceException, ParseException, ClientException {
        Long lastId = sinceId;
        log.warning("Identifier of the last processed DM: " + sinceId);
        log.fine("Identifier of the last processed DM: " + sinceId);
        log.finer("Identifier of the last processed DM: " + sinceId);
        log.finest("Identifier of the last processed DM: " + sinceId);
        // Get the list of direct messages
        Twitter twitterAccount = getTwitterAccount();
        List<DirectMessage> messages = twitterAccount.getDirectMessages(new Paging(sinceId));
        // Transfer the message content
        int limit = messages == null ? 0 : messages.size();
        for (int i=0; i<limit; i++) {
            DirectMessage dm = messages.get(i);
            twitter4j.User sender = dm.getSender();
            ConsumersServlet consumersServlet = getConsumersServlet();
            Consumer consumer = consumersServlet.getConsumer("twitterId", Long.valueOf(dm.getSenderId()));
            String senderScreenName = dm.getSenderScreenName();
            if (consumer == null) {
                consumer = consumersServlet.createConsumer(sender);
                // TODO: issue an invite to follow the new sender
                twitterAccount.follow(senderScreenName);
            }
            if (!sender.isFollowing()) {
                // TODO: use localized message
                twitterAccount.updateStatus("@" + senderScreenName + " You must follow @twetailer and then we can privately talk over DMs.");
            }
            else {
                // TODO: get latest demand submitted by this consumer
                JsonObject latestDemand = new GenericJsonObject();
                try {
                    JsonObject newCommand = parseMessage(dm.getText(), latestDemand);
                    String demandLongLabel = possibleActions.getJsonArray(CommandSettings.Action.demand.toString()).getString(0);
                    String demandShortLabel = possibleActions.getJsonArray(CommandSettings.Action.demand.toString()).getString(1);
                    String demandGivenLabel = newCommand.getString(Demand.ACTION);
                    if (demandLongLabel.equals(demandGivenLabel) || demandShortLabel.equals(demandGivenLabel)) {
                        Long persistedDemandKey = getDemandsServlet().createDemand(newCommand, consumer);
                        // TODO: use localized message
                        twitterAccount.sendDirectMessage(senderScreenName, "Demand ref:" + persistedDemandKey + " saved. Use this reference to update the demand.");
                    }
                    else {
                        // TODO: use localized message
                        twitterAccount.sendDirectMessage(senderScreenName, "Command not supported yet.");
                    }
                }
                catch(ClientException ex) {
                    // TODO: use localized message
                    twitterAccount.sendDirectMessage(senderScreenName, "Error: " + ex.getLocalizedMessage());
                }
            }
            lastId = Long.valueOf(dm.getId());
        }
        return lastId;
    }
}