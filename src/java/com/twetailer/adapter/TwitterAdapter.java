package com.twetailer.adapter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.domderrien.i18n.DateUtils;
import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonException;
import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.JsonParser;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.twetailer.ClientException;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.j2ee.ConsumersServlet;
import com.twetailer.j2ee.DemandsServlet;

public class TwitterAdapter {
   
    private Map<String, Pattern> patterns;

    /**
     * Default constructor using English keywords for the tweet parser and the response generator
     */
    public TwitterAdapter() throws JsonException {
        this((new JsonParser(
                "{" +
                "'act':['action:','!']," +
                "'exp':['expires:','exp:']," +
                "'loc':['locale:','loc:']," +
                "'qty':['quantity:','qty:']," +
                "'ref':['reference:','ref:']," +
                "'rng':['range:','rng:']," +
                "'tag':['tags:','']" +
                "}"
        )).getJsonObject());
    }
    
    private JsonObject localizedKeywords;
    
    /**
     * Extract the localized prefix matching the keywords (long and short versions)
     * and insert the corresponding regular expression pattern in the pattern list
     * 
     * @param keyword identifier of the prefix to process
     * @param expression part of the regular expression that extracts the parameters for the identified prefix
     */
    private void preparePattern(String keyword, String expression) {
        String longPrefix = localizedKeywords.getJsonArray(keyword).getString(0);
        String shortPrefix = localizedKeywords.getJsonArray(keyword).getString(1);
        patterns.put(keyword, Pattern.compile("((?:" + longPrefix + "|" + shortPrefix + ")" + expression + ")", Pattern.CASE_INSENSITIVE));
    }

    /**
     * Initialize the adapter for a given set of localized command prefixes
     */
    public TwitterAdapter(JsonObject keywords) throws JsonException {
        localizedKeywords = keywords;
        
        patterns = new HashMap<String, Pattern>();
        
        preparePattern("act", "\\s*\\w+");
        preparePattern("exp", "\\s*[\\d\\-]+");
        preparePattern("loc", "\\s*(?:\\w+(?:\\s|-|\\.)+)+(?:ca|us)");
        preparePattern("qty", "\\s*\\d+");
        preparePattern("ref", "\\s*\\d+");
        preparePattern("rng", "\\s*\\d+\\s*(?:mi|km)");
        preparePattern("tag", "?(?:\\w\\s*)+");
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
        // Reset some fields of the previous demand
        demand.put(Demand.KEY, 0L);
        demand.put(Demand.ACTION, "demamd");
        demand.put(Demand.CRITERIA, (String) null);
        demand.put(Demand.QUANTITY, 1L);
        demand.put(Demand.RANGE, 25);
        demand.put(Demand.RANGE_UNIT, "km");
        // Action
        try {
            matcher = patterns.get("act").matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.ACTION, getAction(currentGroup.toLowerCase()));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        // Expiration
        try {
            matcher = patterns.get("exp").matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.EXPIRATION_DATE, getDate(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        // Locale
        try {
            matcher = patterns.get("loc").matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.COUNTRY_CODE, getCountryCode(currentGroup).toUpperCase());
            demand.put(Demand.POSTAL_CODE, getPostalCode(currentGroup, demand.getString(Demand.COUNTRY_CODE)).toUpperCase());
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        // Quantity
        try {
            matcher = patterns.get("qty").matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.QUANTITY, getQuantity(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        // Reference
        try {
            matcher = patterns.get("ref").matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.KEY, getQuantity(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        // Range
        try {
            matcher = patterns.get("rng").matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.RANGE_UNIT, getRangeUnit(currentGroup).toLowerCase());
            demand.put(Demand.RANGE, getRange(currentGroup, demand.getString(Demand.RANGE_UNIT)));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        // Tags
        try {
            matcher = patterns.get("tag").matcher(message);
            matcher.find(); // Runs the matcher once
            String currentGroup = matcher.group(1).trim();
            demand.put(Demand.CRITERIA, getTags(currentGroup));
            message = matcher.replaceFirst("");
            oneFieldOverriden = true;
        }
        catch(IllegalStateException ex) {
            // Not a mandatory attribute
        }
        if (!oneFieldOverriden) {
            throw new ClientException("No query field has been correctly extracted");
        }
        if (demand.getLong(Demand.KEY) == 0L && (demand.getLong(Demand.EXPIRATION_DATE) == 0L || demand.getString(Demand.POSTAL_CODE) == null)) {
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
    private static Long getDate(String pattern) throws ParseException {
        // TODO: ensure the date is in the future
        String date = pattern.substring(pattern.indexOf(":") + 1).trim();
        if (date.indexOf('-') == -1) {
            String day = date.substring(date.length() - 2);
            String month = date.substring(date.length() - 4, date.length() - 2);
            String year = date.substring(0, date.length() - 4);
            date = year + "-" + month + "-" + day;
            System.out.println(pattern + " --> " + date);
        }
        if (date.length() == 8) {
            date = "20" + date;
            System.out.println(pattern + " --> " + date);
        }
        return DateUtils.isoToMilliseconds(date + "T23:59:59");
    }
    
    /**
     * Helper extracting a distance
     * @param pattern Parameters extracted by a regular expression
     * @return valid distance
     */
    private static Long getRange(String pattern, String rangeUnit) {
        // Extract the number after the prefix, taking care of excluding the part with the range unit
        return Long.parseLong(pattern.substring(pattern.indexOf(":") + 1, pattern.length() - rangeUnit.length()).trim());
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
    private static String getTags(String pattern) {
        if (pattern.startsWith("tags:")) {
            return pattern.substring("tags:".length());
        }
        return pattern;
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
    
    public void processDirectMessages() throws TwitterException, DataSourceException, ParseException, ClientException {
        processDirectMessages(1L);
    }
    
    public Long processDirectMessages(Long sinceId) throws TwitterException, DataSourceException, ParseException, ClientException {
        Long lastId = sinceId;
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
                    if ("demand".equals(newCommand.getString(Demand.ACTION))) {
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