package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;

public class TestCommandProcessor {

    @Before
    public void setUp() throws Exception {
        // Simplified list of prefixes
        CommandProcessor.localizedPrefixes.clear();
        JsonObject prefixes = new GenericJsonObject();
        for (CommandSettings.Prefix prefix: CommandSettings.Prefix.values()) {
            JsonArray equivalents = new GenericJsonArray();
            equivalents.add(prefix.toString());
            if (CommandSettings.Prefix.action.equals(prefix)) {
                equivalents.add("!");
            }
            else if (CommandSettings.Prefix.help.equals(prefix)) {
                equivalents.add("?");
            }
            prefixes.put(prefix.toString(), equivalents);
        }
        CommandProcessor.localizedPrefixes.put(Locale.ENGLISH, prefixes);

        // Simplified list of actions
        CommandProcessor.localizedActions.clear();
        JsonObject actions = new GenericJsonObject();
        for (CommandSettings.Action action: CommandSettings.Action.values()) {
            JsonArray equivalents = new GenericJsonArray();
            equivalents.add(action.toString());
            actions.put(action.toString(), equivalents);
        }
        CommandProcessor.localizedActions.put(Locale.ENGLISH, actions);

        // Simplified list of states
        CommandProcessor.localizedStates.clear();
        JsonObject states = new GenericJsonObject();
        for (CommandSettings.State state: CommandSettings.State.values()) {
            states.put(state.toString(), state.toString());
        }
        CommandProcessor.localizedStates.put(Locale.ENGLISH, states);

        // Invoke the defined logic to build the list of RegEx patterns for the simplified list of prefixes
        CommandProcessor.localizedPatterns.clear();
        CommandProcessor.loadLocalizedSettings(Locale.ENGLISH);
    }

    @After
    public void tearDown() throws Exception {
        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @Test
    public void testConstructor() {
        new CommandProcessor();
    }

    @Test
    public void testLoadLocalizedSettings() {
        CommandProcessor.localizedPrefixes.clear();
        CommandProcessor.localizedActions.clear();
        CommandProcessor.localizedStates.clear();
        CommandProcessor.localizedHelpKeywords.clear();
        CommandProcessor.localizedPatterns.clear();

        CommandProcessor.loadLocalizedSettings(Locale.ENGLISH);

        assertNotSame(0, CommandProcessor.localizedPrefixes.size());
        assertNotSame(0, CommandProcessor.localizedActions.size());
        assertNotSame(0, CommandProcessor.localizedStates.size());
        assertNotSame(0, CommandProcessor.localizedHelpKeywords.size());
        assertNotSame(0, CommandProcessor.localizedPatterns.size());
    }

    @Test(expected=java.lang.NullPointerException.class)
    public void testParseNull() throws ClientException, ParseException {
        // Cannot pass a null reference
        CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), null);
    }

    @Test(expected=twetailer.ClientException.class)
    public void testParseEmpty() throws ClientException, ParseException {
        // At least the twitter identifier of the sender is required
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "");
        assertEquals(0, data.size());
    }

    @Test(expected=twetailer.ClientException.class)
    public void testParseWithOnlySeparators() throws ClientException, ParseException {
        // At least the twitter identifier of the sender is required
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), " \t \r\n ");
        assertEquals(0, data.size());
    }

    @Test
    public void testParseReferenceI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "reference:21");
        assertEquals(21, data.getLong(Demand.REFERENCE));
    }

    @Test
    public void testParseReferenceII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "reference: 21");
        assertEquals(21, data.getLong(Demand.REFERENCE));
    }

    @Test
    public void testParseReferenceShort() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21");
        assertEquals(21, data.getLong(Demand.REFERENCE));
    }

    @Test
    public void testParseOneWordTag() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 product");
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(0));
    }

    @Test
    public void testParseOneWordTagPrefixed() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 tags:product");
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(0));
    }

    @Test
    public void testParseMultipleWordsTag() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 brand product part");
        assertEquals("brand", data.getJsonArray(Demand.CRITERIA).getString(0));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(1));
        assertEquals("part", data.getJsonArray(Demand.CRITERIA).getString(2));
    }

    @Test
    public void testParseMultipleWordsTagPrefixed() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 tags:brand product part");
        assertEquals("brand", data.getJsonArray(Demand.CRITERIA).getString(0));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(1));
        assertEquals("part", data.getJsonArray(Demand.CRITERIA).getString(2));
    }

    @Test
    public void testParseExpirationI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 expiration:2050-01-01");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 expiration: 2050-01-01");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationIII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 expiration: 20500101");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationIV() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 expiration:50-01-01");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationShort() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 exp:2050-01-01");
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseRangeI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 range: 1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeIII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 range: 1 mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeIV() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:1234567mi");
        assertEquals(1234567, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeV() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:1km");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeVI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:100 km");
        assertEquals(100, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeShortI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 ran:1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeShortII() throws ClientException, ParseException {
        // Add an equivalent to "range" and rebuild the RegEx patterns
        CommandProcessor.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(CommandSettings.Prefix.range.toString()).add("rng");
        CommandProcessor.localizedPatterns.clear();
        CommandProcessor.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 rng:1mi");
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseLocaleI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:h3c2n6 ca");
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale: h3c 2n6 ca");
        assertEquals("H3C 2N6", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:h3c2n6-ca");
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIV() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:97323 us");
        assertEquals("97323", data.getString(Location.POSTAL_CODE));
        assertEquals("US", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleV() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:97323-12345 us");
        assertEquals("97323-12345", data.getString(Location.POSTAL_CODE));
        assertEquals("US", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleVI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:97323-12345-us");
        assertEquals("97323-12345", data.getString(Location.POSTAL_CODE));
        assertEquals("US", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleShort() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 loc:97343-us");
        assertEquals("97343", data.getString(Location.POSTAL_CODE));
        assertEquals("US", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseQuantityI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 quantity:21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 quantity: 21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityIII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 quantity: 21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityShortI() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandProcessor.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(CommandSettings.Prefix.quantity.toString()).add("qty");
        CommandProcessor.localizedPatterns.clear();
        CommandProcessor.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 qty:21");
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityShortII() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandProcessor.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(CommandSettings.Prefix.quantity.toString()).add("qty");
        CommandProcessor.localizedPatterns.clear();
        CommandProcessor.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:  21    qty:  \t 50   ");
        assertEquals(21, data.getLong(Demand.REFERENCE));
        assertEquals(50, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseMixedCase() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:21 RaNge: 25 kM");
        assertEquals(25, data.getLong(Demand.RANGE));
        assertEquals("km", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseCompositeI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:1234 exp:2050-12-31");
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseCompositeII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:1234 range: 10 mi exp:2050-12-31");
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseCompositeIII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:1234 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseCompositeIV() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "ref:1234 quantity:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseCompositeV() throws ClientException, ParseException {
        String keywords = "Wii  console\tremote \t control";
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "qua:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca " + keywords);
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseCompositeVI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "qua:12 range: 10 mi exp:2050-12-31 " + keywords + " locale: h0h 0h0 ca");
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseCompositeVII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "qua:12 range: 10 mi " + keywords + " exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseCompositeVIII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "" + keywords + " quant:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca");
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals("mi", data.getString(Demand.RANGE_UNIT));
        assertEquals("H0H 0H0", data.getString(Location.POSTAL_CODE));
        assertEquals("CA", data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseActionI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "action:demand ref:1234 " + keywords);
        assertEquals("demand", data.getString(Demand.ACTION));
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA).getString(i));
        }
    }

    @Test
    public void testParseIncompleteMessage() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "action:demand " + keywords);
        // Now, the function consuming the incomplete tweet does the checking
    }

    @Test
    public void testParseActionIII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "action:list ref:1234");
        assertEquals("list", data.getString(Demand.ACTION));
        assertEquals(1234, data.getLong(Demand.REFERENCE));
    }

    @Test
    public void testParseHelpI() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "help:");
        assertEquals(1, data.size());
        assertNotNull(data.getString(Command.NEED_HELP));
        assertEquals(0, data.getString(Command.NEED_HELP).length());
    }

    @Test
    public void testParseHelpII() throws ClientException, ParseException {
        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "help:action:");
        assertEquals(1, data.size());
        assertNotNull(data.getString(Command.NEED_HELP));
        assertEquals("action:", data.getString(Command.NEED_HELP));
    }

    @Test
    public void testParseHelpShortI() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandProcessor.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(CommandSettings.Prefix.help.toString()).add("?");
        CommandProcessor.localizedPatterns.clear();
        CommandProcessor.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), "?");
        assertEquals(1, data.size());
        assertTrue(data.containsKey(Command.NEED_HELP));
        assertNotNull(data.getString(Command.NEED_HELP));
    }
    @Test
    public void testParseHelpShortII() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandProcessor.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(CommandSettings.Prefix.help.toString()).add("?");
        CommandProcessor.localizedPatterns.clear();
        CommandProcessor.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), " action ? ");
        assertEquals(1, data.size());
        assertTrue(data.containsKey(Command.NEED_HELP));
        assertNotNull(data.getString(Command.NEED_HELP));
        assertEquals("action", data.getString(Command.NEED_HELP));
    }

    @Test
    public void testParseHelpShortIII() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandProcessor.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(CommandSettings.Prefix.help.toString()).add("?");
        CommandProcessor.localizedPatterns.clear();
        CommandProcessor.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandProcessor.parseTweet(CommandProcessor.localizedPatterns.get(Locale.ENGLISH), " action: ? exp:");
        assertEquals(1, data.size());
        assertTrue(data.containsKey(Command.NEED_HELP));
        assertNotNull(data.getString(Command.NEED_HELP));
        assertEquals("action:  exp:", data.getString(Command.NEED_HELP));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGenerateFullTweet() {
        List<String> criteria = new ArrayList<String>();
        criteria.add("first");
        criteria.add("second");

        Demand demand = new Demand();
        demand.setKey(1L);
        demand.setCriteria(criteria);
        demand.setExpirationDate(new Date(2025-1900, 0, 1, 0, 0, 0));
        demand.setQuantity(3L);
        demand.setRange(4.0D);
        demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
        demand.setState(CommandSettings.State.published);

        Location location = new Location();
        location.setPostalCode("zzz");
        location.setCountryCode(Locale.CANADA.getCountry());

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(demand, location, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandProcessor.localizedPrefixes.get(locale);
        JsonObject states = CommandProcessor.localizedStates.get(locale);
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.reference.toString()).getString(0) + ":1"));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.tags.toString()).getString(0) + ":first second"));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.expiration.toString()).getString(0) + ":2025-01-01"));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.quantity.toString()).getString(0) + ":3"));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.range.toString()).getString(0) + ":4.0" + LocaleValidator.KILOMETER_UNIT));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.state.toString()).getString(0) + ":" + states.getString(CommandSettings.State.published.toString())));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.locale.toString()).getString(0) + ":ZZZ " + Locale.CANADA.getCountry()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGeneratePartialTweetI() {
        Demand demand = new Demand();
        demand.setExpirationDate(new Date(2025-1900, 0, 1, 0, 0, 0));
        demand.setQuantity(3L);
        demand.setRange(4.0D);
        demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
        demand.setState(CommandSettings.State.published);

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(demand, null, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandProcessor.localizedPrefixes.get(locale);
        JsonObject states = CommandProcessor.localizedStates.get(locale);
        assertFalse(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.reference.toString()).getString(0)));
        assertFalse(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.tags.toString()).getString(0)));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.expiration.toString()).getString(0) + ":2025-01-01"));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.quantity.toString()).getString(0) + ":3"));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.range.toString()).getString(0) + ":4.0" + LocaleValidator.KILOMETER_UNIT));
        assertTrue(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.state.toString()).getString(0) + ":" + states.getString(CommandSettings.State.published.toString())));
        assertFalse(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.locale.toString()).getString(0)));
    }

    @Test
    public void testGeneratePartialTweetII() {
        Demand demand = new Demand();

        Location location = new Location();
        location.setCountryCode(Locale.CANADA.getCountry());

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(demand, location, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandProcessor.localizedPrefixes.get(locale);
        assertFalse(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.locale.toString()).getString(0)));
    }

    @Test
    public void testGeneratePartialTweetIII() {
        Demand demand = new Demand();

        Location location = new Location();
        location.setPostalCode("zzz");

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(demand, location, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandProcessor.localizedPrefixes.get(locale);
        assertFalse(response.contains(prefixes.getJsonArray(CommandSettings.Prefix.locale.toString()).getString(0)));
    }

    @Test
    public void testRetrieveConsumerI() throws DataSourceException {
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        assertNotNull(CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test
    public void testRetrieveConsumerII() throws DataSourceException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(emitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        };
        CommandProcessor.consumerOperations = consumerOperations;

        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.twitter);
        rawCommand.setEmitterId(emitterId);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test
    public void testRetrieveConsumerIII() throws DataSourceException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.JABBER_ID, key);
                assertEquals(emitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        };
        CommandProcessor.consumerOperations = consumerOperations;

        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.jabber);
        rawCommand.setEmitterId(emitterId);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test(expected=RuntimeException.class)
    public void testRetrieveConsumerIV() throws DataSourceException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                // assertEquals(Consumer.FACEBOOK_ID, key);
                assertEquals(emitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        };
        CommandProcessor.consumerOperations = consumerOperations;

        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.facebook);
        rawCommand.setEmitterId(emitterId);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test(expected=DataSourceException.class)
    public void testRetrieveConsumerV() throws DataSourceException {
        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), new RawCommand());
    }

    @Test(expected=DataSourceException.class)
    public void testProcessRawCommandWithNoMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);
    }

    @Test
    public void testProcessRawCommandWithIncompleteMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        BaseConnector.resetLastCommunicationInSimulatedMode();

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains("Error"));
    }

    @Test
    public void testProcessRawCommandWithSimpleMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("? demand");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
    }

    @Test
    public void testProcessRawCommandWithUnsupportedAction() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("!grrrr ref:10 wii console quantity:1 loc:h0h0h0 ca exp:2050-01-01");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get("error_unsupported_action", Locale.ENGLISH)));
    }

    @Test
    public void testProcessRawCommandWithPrefixI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("help: " + CommandSettings.Prefix.action.toString());
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_DEFINITION_PREFIX_PREFIX + CommandSettings.Prefix.action.toString(), Locale.ENGLISH)));
    }

    @Test
    public void testProcessRawCommandWithPrefixII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("!help " + CommandSettings.Prefix.action.toString());
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_DEFINITION_PREFIX_PREFIX + CommandSettings.Prefix.action.toString(), Locale.ENGLISH)));
    }

    @Test
    public void testProcessRawCommandWithPrefixIII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("? " + CommandSettings.Prefix.action.toString());
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_DEFINITION_PREFIX_PREFIX + CommandSettings.Prefix.action.toString(), Locale.ENGLISH)));
    }

    @Test
    public void testProcessRawCommandWithPrefixIV() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand(CommandSettings.Prefix.action.toString() + "?");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_DEFINITION_PREFIX_PREFIX + CommandSettings.Prefix.action.toString(), Locale.ENGLISH)));
    }

    @Test
    public void testProcessRawCommandWithActionI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand(CommandSettings.Action.demand.toString() + "?");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_DEFINITION_ACTION_PREFIX + CommandSettings.Action.demand.toString(), Locale.ENGLISH)));
    }

    @Test
    public void testProcessRawCommandWithRegisteredHelpKeywordI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final String helpKeyword = "deposit-test";
        final String helpKeywordEquivalent = "prepay-test";

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("help: " + helpKeywordEquivalent);
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        // Simplified list of registered help keywords
        JsonArray equivalents = new GenericJsonArray();
        equivalents.add(helpKeyword);
        equivalents.add(helpKeywordEquivalent);
        JsonObject helpKeywords = new GenericJsonObject();
        helpKeywords.put(helpKeyword, equivalents);
        CommandProcessor.localizedHelpKeywords.clear();
        CommandProcessor.localizedHelpKeywords.put(Locale.ENGLISH, helpKeywords);

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_DEFINITION_KEYWORD_PREFIX + helpKeyword, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpI() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, ""); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.help.toString()); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpIII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "\t : zzz"); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpIV() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "zzz:"); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpV() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "zzz\t"); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpVI() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "zzz "); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandCancelI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandCancelII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandCancelIII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.cancel.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("error_cancel_without_resource_id", new Consumer().getLocale()), sentText);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandClose() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.close.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandConfirm() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.confirm.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandDecline() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.decline.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test
    public void testProcessCommandDemand() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 3333L;
        final Long locationKey = 4444L;
        final Long demandKey = 5555L;

        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject parameters) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                List<Demand> demands = new ArrayList<Demand>();
                return demands;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long consumerKey) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.demand.toString());
        command.put(Location.POSTAL_CODE, "H0H0H0");
        command.put(Location.COUNTRY_CODE, "CA");

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListI() throws TwitterException, DataSourceException, ClientException {
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Demand>();
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("ta_responseToListCommandForNoResult", new Consumer().getLocale()), sentText);
    }

    @Test
    public void testProcessCommandListII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("ta_introductionToResponseToListCommandWithManyResults", new Object[] { 1 }, new Consumer().getLocale()), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("ta_introductionToResponseToListCommandWithManyResults", new Object[] { 1 }, new Consumer().getLocale()), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListIV() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.list.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListV() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.list.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListVI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                return null;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.list.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("ta_responseToSpecificListCommandForNoResult", new Object[] { demandKey }, new Consumer().getLocale()), sentText);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandPropose() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.propose.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandSupply() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.supply.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandWish() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.wish.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandWWW() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.www.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test
    public void testGuessActionI() {
        // Command mock
        JsonObject command = new GenericJsonObject();

        assertEquals(CommandSettings.Action.demand.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionII() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, CommandSettings.Action.demand.toString());

        assertEquals(CommandSettings.Action.demand.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionIII() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, "12345");

        assertEquals(CommandSettings.Action.list.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionIV() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, "12345");
        command.put(Demand.RANGE, "55.5");

        assertEquals(CommandSettings.Action.demand.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionV() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Store.STORE_KEY, "12345");

        assertEquals(CommandSettings.Action.list.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionVI() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Store.STORE_KEY, "12345");
        command.put(Store.ADDRESS, "address");

        assertNull(CommandProcessor.guessAction(command)); // Cannot update Store instance from Twitter
    }

    @Test
    public void testProcessExisitingDemandI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(CommandSettings.State.open, demand.getState());
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);
        command.put(Location.POSTAL_CODE, "zzz");

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessExisitingDemandII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(CommandSettings.State.open, demand.getState());
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessExisitingDemandIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(CommandSettings.State.open, demand.getState());
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                return null;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessFirstNewDemandI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Demand>();
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(CommandSettings.State.open, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);
        command.put(Location.POSTAL_CODE, "zzz");

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessFirstNewDemandII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Demand>();
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(CommandSettings.State.open, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessFirstNewDemandIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Demand>();
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(CommandSettings.State.open, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.LOCATION_KEY, locationKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setSource(Source.twitter); // Setup to verify it will be reset with the Source.simulated of the rawCommand
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(Source.simulated, demand.getSource()); // Verify the source attribute reset with the raw Command one
                assertEquals(CommandSettings.State.open, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                demand.setSource(Source.twitter); // Setup to verify it will be reset with the Source.simulated of the rawCommand
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(Source.simulated, demand.getSource()); // Verify the source attribute reset with the raw Command one
                assertEquals(CommandSettings.State.open, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                demand.setSource(Source.twitter); // Setup to verify it will be reset with the Source.simulated of the rawCommand
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(Source.simulated, demand.getSource()); // Verify the source attribute reset with the raw Command one
                assertEquals(CommandSettings.State.open, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setLocationKey(locationKey);

        CommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }
}
