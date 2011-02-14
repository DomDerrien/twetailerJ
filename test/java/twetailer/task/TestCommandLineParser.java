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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.connector.BaseConnector;
import twetailer.dao.MockBaseOperations;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.Store;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestCommandLineParser {

    @Before
    public void setUp() throws Exception {
        // Simplified list of prefixes
        CommandLineParser.localizedPrefixes.clear();
        JsonObject prefixes = new GenericJsonObject();
        for (Prefix prefix: Prefix.values()) {
            JsonArray equivalents = new GenericJsonArray();
            if (Prefix.reference.equals(prefix)) {
                equivalents.add("demand"); // To insert this label before "reference" ;)
            }
            equivalents.add(prefix.toString());
            if (Prefix.action.equals(prefix)) {
                equivalents.add("!");
            }
            else if (Prefix.help.equals(prefix)) {
                equivalents.add("?");
            }
            else if (Prefix.hash.equals(prefix)) {
                equivalents.add("#");
            }
            else if (Prefix.expiration.equals(prefix)) {
                equivalents.add("expires");
            }
            prefixes.put(prefix.toString(), equivalents);
        }
        CommandLineParser.localizedPrefixes.put(Locale.ENGLISH, prefixes);

        // Very simplified list of prefixes in FRENCH
        prefixes = new GenericJsonObject();
        prefixes.put(Prefix.reference.toString(), new GenericJsonArray());
        prefixes.getJsonArray(Prefix.reference.toString()).add("demande");
        prefixes.getJsonArray(Prefix.reference.toString()).add("référence");
        prefixes.put(Prefix.price.toString(), new GenericJsonArray());
        prefixes.getJsonArray(Prefix.price.toString()).add("prix");
        prefixes.put(Prefix.help.toString(), new GenericJsonArray());
        prefixes.getJsonArray(Prefix.help.toString()).add("aide");
        CommandLineParser.localizedPrefixes.put(Locale.FRENCH, prefixes);

        // Simplified list of actions
        CommandLineParser.localizedActions.clear();
        JsonObject actions = new GenericJsonObject();
        for (Action action: Action.values()) {
            JsonArray equivalents = new GenericJsonArray();
            equivalents.add(action.toString());
            if (Action.www.equals(action)) {
                equivalents.add("url");
            }
            actions.put(action.toString(), equivalents);
        }
        CommandLineParser.localizedActions.put(Locale.ENGLISH, actions);

        // Very simplified list of actions in FRENCH
        actions = new GenericJsonObject();
        actions.put(Action.demand.toString(), new GenericJsonArray());
        actions.getJsonArray(Action.demand.toString()).add("demande");
        CommandLineParser.localizedActions.put(Locale.FRENCH, actions);

        // Simplified list of states
        CommandLineParser.localizedStates.clear();
        JsonObject states = new GenericJsonObject();
        for (State state: State.values()) {
            states.put(state.toString(), state.toString());
        }
        CommandLineParser.localizedStates.put(Locale.ENGLISH, states);

        // Invoke the defined logic to build the list of RegEx patterns for the simplified list of prefixes
        CommandLineParser.localizedPatterns.clear();
        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
    }

    @After
    public void tearDown() throws Exception {
        BaseConnector.resetLastCommunicationInSimulatedMode();

        CommandLineParser.localizedPrefixes = new HashMap<Locale, JsonObject>();
        CommandLineParser.localizedActions = new HashMap<Locale, JsonObject>();
        CommandLineParser.localizedStates = new HashMap<Locale, JsonObject>();
        CommandLineParser.localizedHelpKeywords = new HashMap<Locale, JsonObject>();
        CommandLineParser.localizedPatterns = new HashMap<Locale, Map<String, Pattern>>();
    }

    @Test
    public void testConstructor() {
        new CommandLineParser();
    }

    @Test
    public void testLoadLocalizedSettings() {
        CommandLineParser.localizedPrefixes.clear();
        CommandLineParser.localizedActions.clear();
        CommandLineParser.localizedStates.clear();
        CommandLineParser.localizedHelpKeywords.clear();
        CommandLineParser.localizedPatterns.clear();

        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);
        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);

        assertNotSame(0, CommandLineParser.localizedPrefixes.size());
        assertNotSame(0, CommandLineParser.localizedActions.size());
        assertNotSame(0, CommandLineParser.localizedStates.size());
        assertNotSame(0, CommandLineParser.localizedHelpKeywords.size());
        assertNotSame(0, CommandLineParser.localizedPatterns.size());
    }

    @Test(expected=NullPointerException.class)
    public void testParseNull() throws ClientException, ParseException {
        // Cannot pass a null reference
        CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), null, Locale.ENGLISH);
    }

    @Test(expected=twetailer.ClientException.class)
    public void testParseEmpty() throws ClientException, ParseException {
        // At least the twitter identifier of the sender is required
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "", Locale.ENGLISH);
        assertEquals(0, data.size());
    }

    @Test(expected=twetailer.ClientException.class)
    public void testParseWithOnlySeparators() throws ClientException, ParseException {
        // At least the twitter identifier of the sender is required
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), " \t \r\n ", Locale.ENGLISH);
        assertEquals(0, data.size());
    }

    @Test
    public void testParseReferenceI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "reference:21", Locale.ENGLISH);
        assertEquals(21, data.getLong(Demand.REFERENCE));
    }

    @Test
    public void testParseReferenceII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "reference: 21", Locale.ENGLISH);
        assertEquals(21, data.getLong(Demand.REFERENCE));
    }

    @Test
    public void testParseReferenceShort() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21", Locale.ENGLISH);
        assertEquals(21, data.getLong(Demand.REFERENCE));
    }

    @Test
    public void testParseOneWordTag() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 product", Locale.ENGLISH);
        assertEquals("product", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
    }

    @Test
    public void testParseOneWordTagPrefixed() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 tags:product", Locale.ENGLISH);
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(0));
    }

    @Test
    public void testParseMultipleWordsTag() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 brand product part", Locale.ENGLISH);
        assertEquals("brand", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA_ADD).getString(1));
        assertEquals("part", data.getJsonArray(Demand.CRITERIA_ADD).getString(2));
    }

    @Test
    public void testParseMultipleWordsTagPrefixed() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 tags:brand product part", Locale.ENGLISH);
        assertEquals("brand", data.getJsonArray(Demand.CRITERIA).getString(0));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA).getString(1));
        assertEquals("part", data.getJsonArray(Demand.CRITERIA).getString(2));
    }

    @Test
    public void testParseExpirationI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 expiration:2050-01-01", Locale.ENGLISH);
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 expiration: 2050-01-01", Locale.ENGLISH);
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 expiration: 20500101", Locale.ENGLISH);
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseExpirationShort() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 exp:2050-01-01", Locale.ENGLISH);
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseRangeI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:1mi", Locale.ENGLISH);
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 range: 1mi", Locale.ENGLISH);
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 range: 1 mi", Locale.ENGLISH);
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeIV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:1234567mi", Locale.ENGLISH);
        assertEquals(1234567, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:1km", Locale.ENGLISH);
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.KILOMETER_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeVI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:100 km", Locale.ENGLISH);
        assertEquals(100, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.KILOMETER_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeVII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:1mile", Locale.ENGLISH);
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals("mile", data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeVIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 range:10miles", Locale.ENGLISH);
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.ALTERNATE_MILE_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeShortI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 ran:1mi", Locale.ENGLISH);
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseRangeShortII() throws ClientException, ParseException {
        // Add an equivalent to "range" and rebuild the RegEx patterns
        CommandLineParser.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(Prefix.range.toString()).add("rng");
        CommandLineParser.localizedPatterns.clear();
        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 rng:1mi", Locale.ENGLISH);
        assertEquals(1, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseLocaleI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:h3c2n6 ca", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale: h3c 2n6 ca", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:h3c2n6-ca", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:97323 us", Locale.ENGLISH);
        assertEquals("97323", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:97323-1234 us", Locale.ENGLISH);
        assertEquals("97323-1234", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleVI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale:97323-1234-us", Locale.ENGLISH);
        assertEquals("97323-1234", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleVII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale: h3c 2n6 ca ", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleVIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale: h3c 2n6 ca manger des pommes", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleIX() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 locale: h3c 2n6 ca range:2km", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleShortI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 loc:97343-us", Locale.ENGLISH);
        assertEquals("97343", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleShortII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 loc:97343us", Locale.ENGLISH);
        assertEquals("97343", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParsePriceI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 price:25.99", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.PRICE), 0.0);
    }

    @Test
    public void testParsePriceII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 price:  25.99", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.PRICE), 0.0);
    }

    @Test
    public void testParsePriceIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 price: $ 25.99", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.PRICE), 0.0);
    }

    @Test
    public void testParsePriceIV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 price:  25.99€ ", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.PRICE), 0.0);
    }

    @Test
    public void testParsePriceV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 price: € 25.3243", Locale.ENGLISH);
        assertEquals(25.3243, data.getDouble(Proposal.PRICE), 0.0);
    }

    @Test
    public void testParsePriceVI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 price:  25$", Locale.ENGLISH);
        assertEquals(25, data.getDouble(Proposal.PRICE), 0.0);
    }

    @Test
    public void testParsePriceShort() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 pri: 25.99", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.PRICE), 0.0);
    }

    @Test
    public void testParseProposalI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 proposal:1234567890", Locale.ENGLISH);
        assertEquals(1234567890L, data.getLong(Proposal.PROPOSAL_KEY));
    }

    @Test
    public void testParseProposalII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 proposal: \t 1234567890", Locale.ENGLISH);
        assertEquals(1234567890L, data.getLong(Proposal.PROPOSAL_KEY));
    }

    @Test
    public void testParseProposalShort() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 pro: 1234567890", Locale.ENGLISH);
        assertEquals(1234567890L, data.getLong(Proposal.PROPOSAL_KEY));
    }

    @Test
    public void testParseQuantityI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 quantity:21", Locale.ENGLISH);
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 quantity: 21", Locale.ENGLISH);
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 quantity: 21", Locale.ENGLISH);
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityShortI() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandLineParser.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(Prefix.quantity.toString()).add("qty");
        CommandLineParser.localizedPatterns.clear();
        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 qty:21", Locale.ENGLISH);
        assertEquals(21, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseQuantityShortII() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandLineParser.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(Prefix.quantity.toString()).add("qty");
        CommandLineParser.localizedPatterns.clear();
        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:  21    qty:  \t 50   ", Locale.ENGLISH);
        assertEquals(21, data.getLong(Demand.REFERENCE));
        assertEquals(50, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseTotalI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 total:25.99", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.TOTAL), 0.0);
    }

    @Test
    public void testParseTotalII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 toTAL:  25.99", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.TOTAL), 0.0);
    }

    @Test
    public void testParseTotalIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 toTAL: € 25.99", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.TOTAL), 0.0);
    }

    @Test
    public void testParseTotalIV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 toTAL:  25.99£", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.TOTAL), 0.0);
    }

    @Test
    public void testParseTotalShort() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 tot: 25.99", Locale.ENGLISH);
        assertEquals(25.99, data.getDouble(Proposal.TOTAL), 0.0);
    }

    @Test
    public void testParseMixedCase() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 RaNge: 25 kM", Locale.ENGLISH);
        assertEquals(25, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.KILOMETER_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseCompositeI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:1234 exp:2050-12-31", Locale.ENGLISH);
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
    }

    @Test
    public void testParseCompositeII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:1234 range: 10 mi exp:2050-12-31", Locale.ENGLISH);
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
    }

    @Test
    public void testParseCompositeIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:1234 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca", Locale.ENGLISH);
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
        assertEquals(RobotResponder.ROBOT_POSTAL_CODE, data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseCompositeIV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:1234 quantity:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca", Locale.ENGLISH);
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
        assertEquals(RobotResponder.ROBOT_POSTAL_CODE, data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
    }

    @Test
    public void testParseCompositeV() throws ClientException, ParseException {
        String keywords = "Wii  console\tremote \t control";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "qua:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca " + keywords, Locale.ENGLISH);
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
        assertEquals(RobotResponder.ROBOT_POSTAL_CODE, data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA_ADD).getString(i));
        }
    }

    @Test
    public void testParseCompositeVI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "qua:12 range: 10 mi exp:2050-12-31 " + keywords + " locale: h0h 0h0 ca", Locale.ENGLISH);
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
        assertEquals(RobotResponder.ROBOT_POSTAL_CODE, data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA_ADD).getString(i));
        }
    }

    @Test
    public void testParseCompositeVII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "qua:12 range: 10 mi " + keywords + " exp:2050-12-31 locale: h0h 0h0 ca", Locale.ENGLISH);
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
        assertEquals(RobotResponder.ROBOT_POSTAL_CODE, data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA_ADD).getString(i));
        }
    }

    @Test
    public void testParseCompositeVIII() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "" + keywords + " quant:12 range: 10 mi exp:2050-12-31 locale: h0h 0h0 ca", Locale.ENGLISH);
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
        assertEquals(RobotResponder.ROBOT_POSTAL_CODE, data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA_ADD).getString(i));
        }
    }

    @Test
    public void testParseCompositeIX() throws ClientException, ParseException {
        String keywords = "Wii console remote control et des épices sèches de suiße";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "quant:12 range: 10 mi " + keywords + " exp:2050-12-31 locale: h0h 0h0 ca", Locale.ENGLISH);
        assertEquals("2050-12-31T23:59:59", data.getString(Demand.EXPIRATION_DATE));
        assertEquals(10, data.getLong(Demand.RANGE));
        assertEquals(LocaleValidator.MILE_UNIT, data.getString(Demand.RANGE_UNIT));
        assertEquals(RobotResponder.ROBOT_POSTAL_CODE, data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertEquals(12, data.getLong(Demand.QUANTITY));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA_ADD).getString(i));
        }
    }

    @Test
    public void testParseActionI() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "action:demand ref:1234 " + keywords, Locale.ENGLISH);
        assertEquals(Action.demand.toString(), data.getString(Command.ACTION));
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA_ADD).getString(i));
        }
    }

    @Test
    public void testParseActionIIa() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "!demand ref:1234 " + keywords, Locale.ENGLISH);
        assertEquals(Action.demand.toString(), data.getString(Command.ACTION));
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA_ADD).getString(i));
        }
    }

    @Test
    public void testParseActionIIb() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand ref:1234 " + keywords, Locale.ENGLISH);
        assertEquals(Action.demand.toString(), data.getString(Command.ACTION));
        assertEquals(1234, data.getLong(Demand.REFERENCE));
        String[] parts = keywords.split("\\s+");
        for (int i = 0; i < parts.length; i ++) {
            assertEquals(parts[i], data.getJsonArray(Demand.CRITERIA_ADD).getString(i));
        }
    }

    @Test
    public void testParseActionIIc() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "list", Locale.ENGLISH);
        assertEquals(Action.list.toString(), data.getString(Command.ACTION));
    }

    @Test
    public void testParseActionIId() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), " propose ", Locale.ENGLISH);
        assertEquals(Action.propose.toString(), data.getString(Command.ACTION));
    }

    @Test
    public void testParseActionIIe() throws ClientException, ParseException {
        String url = ApplicationSettings.get().getProductWebsite();
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), " www " + url, Locale.ENGLISH);
        assertEquals(Action.www.toString(), data.getString(Command.ACTION));
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertEquals(url, data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), " url " + url, Locale.ENGLISH);
        assertEquals("url", data.getString(Command.ACTION)); // as "url" is an equivalent to "www"
    }

    @Test
    public void testParseIncompleteMessage() throws ClientException, ParseException {
        String keywords = "Wii console remote control";
        CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "action:demand " + keywords, Locale.ENGLISH);
        // Now, the function consuming the incomplete tweet does the checking
    }

    @Test
    public void testParseActionIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "action:list ref:1234", Locale.ENGLISH);
        assertEquals("list", data.getString(Command.ACTION));
        assertEquals(1234, data.getLong(Demand.REFERENCE));
    }

    @Test
    public void testParseHelpI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "help:", Locale.ENGLISH);
        assertEquals(1, data.size());
        assertNotNull(data.getString(Command.NEED_HELP));
        assertEquals(0, data.getString(Command.NEED_HELP).length());
    }

    @Test
    public void testParseHelpII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "help:action:", Locale.ENGLISH);
        assertEquals(1, data.size());
        assertNotNull(data.getString(Command.NEED_HELP));
        assertEquals("action:", data.getString(Command.NEED_HELP));
    }

    @Test
    public void testParseHelpShortI() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandLineParser.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(Prefix.help.toString()).add("?");
        CommandLineParser.localizedPatterns.clear();
        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "?", Locale.ENGLISH);
        assertEquals(1, data.size());
        assertTrue(data.containsKey(Command.NEED_HELP));
        assertNotNull(data.getString(Command.NEED_HELP));
    }
    @Test
    public void testParseHelpShortII() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandLineParser.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(Prefix.help.toString()).add("?");
        CommandLineParser.localizedPatterns.clear();
        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), " action ? ", Locale.ENGLISH);
        assertEquals(1, data.size());
        assertTrue(data.containsKey(Command.NEED_HELP));
        assertNotNull(data.getString(Command.NEED_HELP));
        assertEquals("action", data.getString(Command.NEED_HELP));
    }

    @Test
    public void testParseHelpShortIII() throws ClientException, ParseException {
        // Add an equivalent to "quantity" and rebuild the RegEx patterns
        CommandLineParser.localizedPrefixes.get(Locale.ENGLISH).getJsonArray(Prefix.help.toString()).add("?");
        CommandLineParser.localizedPatterns.clear();
        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);

        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), " action: ? exp:", Locale.ENGLISH);
        assertEquals(1, data.size());
        assertTrue(data.containsKey(Command.NEED_HELP));
        assertNotNull(data.getString(Command.NEED_HELP));
        assertEquals("action:  exp:", data.getString(Command.NEED_HELP));
    }

    @Test
    @Ignore
    @SuppressWarnings("deprecation")
    public void testGenerateFullTweetI() {
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
        demand.setState(State.published);

        Location location = new Location();
        location.setPostalCode("zzz".toUpperCase());
        location.setCountryCode(Locale.CANADA.getCountry());

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(demand, location, false, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.reference.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "1"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.tags.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "first second"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.expiration.toString()).getString(1) + CommandLineParser.PREFIX_SEPARATOR + "2025-01-01"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.quantity.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "3"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.range.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "4.0" + LocaleValidator.KILOMETER_UNIT));
        // State.published: not printed anymore ;)
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.locale.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "ZZZ " + Locale.CANADA.getCountry()));
    }

    @Test
    @Ignore
    public void testGenerateFullTweetII() {
        List<String> criteria = new ArrayList<String>();
        criteria.add("first");
        criteria.add("second");

        Proposal proposal = new Proposal();
        proposal.setKey(1L);
        proposal.setCriteria(criteria);
        proposal.setDemandKey(12345L);
        proposal.setPrice(25.99D);
        proposal.setQuantity(3L);
        proposal.setState(State.published);
        proposal.setTotal(35.33D);

        Store store = new Store();
        store.setKey(67890L);
        store.setName("sgrognegneu");

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(proposal, store, false, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.proposal.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "1"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.tags.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "first second"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.reference.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "12345"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.price.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "$25.99"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.quantity.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "3"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.store.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "67890"));
        assertTrue(response.contains("sgrognegneu"));
        // State.published: not printed anymore ;)
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.total.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "$35.33"));
    }

    @Test
    @Ignore
    @SuppressWarnings("deprecation")
    public void testGeneratePartialTweetI() {
        Demand demand = new Demand();
        demand.setExpirationDate(new Date(2025-1900, 0, 1, 0, 0, 0));
        demand.setQuantity(3L);
        demand.setRange(4.0D);
        demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
        demand.setState(State.published);
        demand.addHashTag("demo");

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(demand, null, false, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        assertFalse(response.contains(prefixes.getJsonArray(Prefix.reference.toString()).getString(0)));
        assertFalse(response.contains(prefixes.getJsonArray(Prefix.tags.toString()).getString(0)));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.expiration.toString()).getString(1) + CommandLineParser.PREFIX_SEPARATOR + "2025-01-01"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.quantity.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "3"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.range.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "4.0" + LocaleValidator.KILOMETER_UNIT));
        // State.published: not printed anymore ;)
        assertFalse(response.contains(prefixes.getJsonArray(Prefix.locale.toString()).getString(0)));
        assertTrue(response.contains("#demo"));
    }

    @Test
    @Ignore
    public void testGeneratePartialTweetII() {
        Demand demand = new Demand();

        Location location = new Location();
        location.setCountryCode(Locale.CANADA.getCountry());

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(demand, location, false, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        assertFalse(response.contains(prefixes.getJsonArray(Prefix.locale.toString()).getString(0)));
    }

    @Test
    @Ignore
    public void testGeneratePartialTweetIV() {

        Proposal proposal = new Proposal();
        proposal.setPrice(25.99D);
        proposal.setQuantity(3L);
        proposal.setState(State.published);
        proposal.setTotal(35.33D);
        proposal.addHashTag("demo");

        Store store = null;

        Locale locale = Locale.ENGLISH;

        String response = CommandProcessor.generateTweet(proposal, store, false, locale);

        assertNotNull(response);
        assertNotSame(0, response.length());
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        assertFalse(response.contains(prefixes.getJsonArray(Prefix.proposal.toString()).getString(0)));
        assertFalse(response.contains(prefixes.getJsonArray(Prefix.tags.toString()).getString(0)));
        assertFalse(response.contains(prefixes.getJsonArray(Prefix.reference.toString()).getString(0)));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.price.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "$25.99"));
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.quantity.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "3"));
        // State.published: not printed anymore ;)
        assertTrue(response.contains(prefixes.getJsonArray(Prefix.total.toString()).getString(0) + CommandLineParser.PREFIX_SEPARATOR + "$35.33"));
        assertTrue(response.contains("#demo"));
    }

    @Test
    public void testParseAddTag() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "+tags:product", Locale.ENGLISH);
        assertNull(data.getJsonArray(Demand.CRITERIA));
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertEquals("product", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
    }

    @Test
    public void testParseRemoveTag() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "-tags:product", Locale.ENGLISH);
        assertNull(data.getJsonArray(Demand.CRITERIA));
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_REMOVE).size());
        assertEquals("product", data.getJsonArray(Demand.CRITERIA_REMOVE).getString(0));
    }

    @Test
    public void testParseMixedTagsI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "-tags:product +tags:service tags:excellence", Locale.ENGLISH);
        assertEquals(1, data.getJsonArray(Demand.CRITERIA).size());
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_REMOVE).size());
        assertEquals("excellence", data.getJsonArray(Demand.CRITERIA).getString(0));
        assertEquals("service", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA_REMOVE).getString(0));
    }

    @Test
    public void testParseMixedTagsII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "tags:excellence -tags:product price:25.80£ +tags:service quantity:12", Locale.ENGLISH);
        assertEquals(1, data.getJsonArray(Demand.CRITERIA).size());
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_REMOVE).size());
        assertEquals("excellence", data.getJsonArray(Demand.CRITERIA).getString(0));
        assertEquals("service", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA_REMOVE).getString(0));
    }

    @Test
    public void testParseMixedTagsIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "tags:excellence excellency royal royalty +tags:product thing toy price:$25.80 -tags:service help quantity:12",
                Locale.ENGLISH
        );
        assertEquals(4, data.getJsonArray(Demand.CRITERIA).size());
        assertEquals(3, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertEquals(2, data.getJsonArray(Demand.CRITERIA_REMOVE).size());
        assertEquals("excellence", data.getJsonArray(Demand.CRITERIA).getString(0));
        assertEquals("excellency", data.getJsonArray(Demand.CRITERIA).getString(1));
        assertEquals("royal", data.getJsonArray(Demand.CRITERIA).getString(2));
        assertEquals("royalty", data.getJsonArray(Demand.CRITERIA).getString(3));
        assertEquals("product", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("thing", data.getJsonArray(Demand.CRITERIA_ADD).getString(1));
        assertEquals("toy", data.getJsonArray(Demand.CRITERIA_ADD).getString(2));
        assertEquals("service", data.getJsonArray(Demand.CRITERIA_REMOVE).getString(0));
        assertEquals("help", data.getJsonArray(Demand.CRITERIA_REMOVE).getString(1));
    }

    @Test
    public void testParseMixedTagsIV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "tags: total:45.3222 +tags: price:$25.80 -tags: quantity:12",
                Locale.ENGLISH
        );
        assertEquals(1, data.getJsonArray(Demand.CRITERIA).size());
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_REMOVE).size());
    }

    @Test
    public void testParseMixedTagsV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "action:demand tags:one two three four +tags:four five six price:$25.80 -tags:three four six quantity:12",
                Locale.ENGLISH
        );
        Demand demand = new Demand(data);
        assertNotNull(demand.getCriteria());
        assertEquals(4, demand.getCriteria().size());
        assertTrue(demand.getCriteria().contains("one"));
        assertTrue(demand.getCriteria().contains("two"));
        assertTrue(demand.getCriteria().contains("four"));
        assertTrue(demand.getCriteria().contains("five"));
    }

    @Test
    public void testParseMixedTagsVI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "action:propose proposal:21 tags:one two three four +tags:four five six price:$25.80 -tags:three four six quantity:12",
                Locale.ENGLISH
        );
        Proposal proposal = new Proposal(data);
        assertNotNull(proposal.getCriteria());
        assertEquals(4, proposal.getCriteria().size());
        assertTrue(proposal.getCriteria().contains("one"));
        assertTrue(proposal.getCriteria().contains("two"));
        assertTrue(proposal.getCriteria().contains("four"));
        assertTrue(proposal.getCriteria().contains("five"));
    }

    @Test
    public void testParseMixedTagsVIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "action:demand +tags:test price:$25.80 quantity:12",
                Locale.ENGLISH
        );
        assertEquals(1, new Demand(data).getCriteria().size());
        assertNull(data.getJsonArray(Demand.CRITERIA));
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertNull(data.getJsonArray(Demand.CRITERIA_REMOVE));
        assertEquals("test", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
    }

    @Test
    public void testParseMixedTagsIX() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "action:demand -tags:test price:$25.80 quantity:12",
                Locale.ENGLISH
        );
        assertEquals(0, new Demand(data).getCriteria().size());
        assertNull(data.getJsonArray(Demand.CRITERIA));
        assertNull(data.getJsonArray(Demand.CRITERIA_ADD));
        assertEquals(1, data.getJsonArray(Demand.CRITERIA_REMOVE).size());
        assertEquals("test", data.getJsonArray(Demand.CRITERIA_REMOVE).getString(0));
    }

    @Test
    public void testParseMixedTagsX() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "action:demand price:$25.80 quantity:12",
                Locale.ENGLISH
        );
        new Demand(data);
        assertEquals(0, new Demand(data).getCriteria().size());
        assertNull(data.getJsonArray(Demand.CRITERIA));
        assertNull(data.getJsonArray(Demand.CRITERIA_ADD));
        assertNull(data.getJsonArray(Demand.CRITERIA_REMOVE));
    }

    @Test
    public void testParseAddressI() throws ClientException, ParseException {
        final String address = "12345, Lamb Street, Montreal, Qc, Canada";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "address: " + address, Locale.ENGLISH);
        assertEquals(address, data.getString(Store.ADDRESS));
    }

    @Test
    public void testParseAddressII() throws ClientException, ParseException {
        final String address = "12345, Appt. 123, Lamb Street, Montreal, Qc, Canada";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "wii address: " + address + " ref:12345 console", Locale.ENGLISH);
        assertEquals(address, data.getString(Store.ADDRESS));
        assertEquals(12345, data.getLong(Demand.REFERENCE));
        assertEquals("wii", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("console", data.getJsonArray(Demand.CRITERIA_ADD).getString(1));
    }

    @Test
    public void testParseNameI() throws ClientException, ParseException {
        final String name = "Grumb LLC inc.";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "name: " + name, Locale.ENGLISH);
        assertEquals(name, data.getString(Store.NAME));
    }

    @Test
    public void testParseNameII() throws ClientException, ParseException {
        final String name = "Grumb LLC inc.";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "wii name: " + name + " ref:12345 console", Locale.ENGLISH);
        assertEquals(name, data.getString(Store.NAME));
        assertEquals(12345, data.getLong(Demand.REFERENCE));
        assertEquals("wii", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("console", data.getJsonArray(Demand.CRITERIA_ADD).getString(1));
    }

    @Test
    public void testParsePhoneNumberI() throws ClientException, ParseException {
        final String phoneNumber = "+1 (222) 333 4455 #666-777";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "phoneNumber: " + phoneNumber, Locale.ENGLISH);
        assertEquals(phoneNumber, data.getString(Store.PHONE_NUMBER));
    }

    @Test
    public void testParsePhoneNumberII() throws ClientException, ParseException {
        final String phoneNumber = "+1 (222) 333 4455 #666-777";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "wii phoneNumber: " + phoneNumber + " ref:12345 console", Locale.ENGLISH);
        assertEquals(phoneNumber, data.getString(Store.PHONE_NUMBER));
        assertEquals(12345, data.getLong(Demand.REFERENCE));
        assertEquals("wii", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("console", data.getJsonArray(Demand.CRITERIA_ADD).getString(1));
    }

    @Test
    public void testParsePointOfViewI() throws ClientException, ParseException {
        final QueryPointOfView pointOfView = QueryPointOfView.ANONYMOUS;
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "pointOfView: " + pointOfView.toString(), Locale.ENGLISH);
        assertEquals(pointOfView.toString(), data.getString(Command.POINT_OF_VIEW));
    }

    @Test
    public void testParsePointOfViewII() throws ClientException, ParseException {
        final QueryPointOfView pointOfView = QueryPointOfView.ANONYMOUS;
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "wii pointOfView: " + pointOfView.toString() + " ref:12345 console", Locale.ENGLISH);
        assertEquals(pointOfView.toString(), data.getString(Command.POINT_OF_VIEW));
        assertEquals(12345, data.getLong(Demand.REFERENCE));
        assertEquals("wii", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("console", data.getJsonArray(Demand.CRITERIA_ADD).getString(1));
    }

    @Test
    public void testParseStoreI() throws ClientException, ParseException {
        final long storeKey = 1234567890L;
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "store: " + storeKey, Locale.ENGLISH);
        assertEquals(storeKey, data.getLong(Store.STORE_KEY));
    }

    @Test
    public void testParseStoreII() throws ClientException, ParseException {
        final long storeKey = 1234567890L;
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "wii store: " + storeKey + " ref:12345 console", Locale.ENGLISH);
        assertEquals(storeKey, data.getLong(Store.STORE_KEY));
        assertEquals(12345, data.getLong(Demand.REFERENCE));
        assertEquals("wii", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("console", data.getJsonArray(Demand.CRITERIA_ADD).getString(1));
    }

    @Test
    public void testParseStoreIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "store: *", Locale.ENGLISH);
        assertEquals(-1, data.getLong(Store.STORE_KEY));
    }

    @Test
    public void testManyHashTagsI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "ref:249 wii #game locale:h0h0h0 mario range:25 km kart #DeMo",
                Locale.ENGLISH
        );

        assertEquals(2, data.getJsonArray(Command.HASH_TAGS).size());
        assertFalse(data.containsKey(Command.HASH_TAGS_ADD));
        assertFalse(data.containsKey(Command.HASH_TAGS_REMOVE));

        assertEquals("game", data.getJsonArray(Command.HASH_TAGS).getString(0));
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, data.getJsonArray(Command.HASH_TAGS).getString(1));
    }

    @Test
    public void testManyHashTagsII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "ref:249 wii +hash:game locale:h0h0h0 mario range:25 km kart +hash:DeMo",
                Locale.ENGLISH
        );

        assertFalse(data.containsKey(Command.HASH_TAGS));
        assertEquals(2, data.getJsonArray(Command.HASH_TAGS_ADD).size());
        assertFalse(data.containsKey(Command.HASH_TAGS_REMOVE));

        assertEquals("game", data.getJsonArray(Command.HASH_TAGS_ADD).getString(0));
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, data.getJsonArray(Command.HASH_TAGS_ADD).getString(1));
    }

    @Test
    public void testManyHashTagsIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(
                CommandLineParser.localizedPatterns.get(Locale.ENGLISH),
                "ref:249 wii -hash:game locale:h0h0h0 mario range:25 km kart -hash:DeMo",
                Locale.ENGLISH
        );

        assertFalse(data.containsKey(Command.HASH_TAGS));
        assertFalse(data.containsKey(Command.HASH_TAGS_ADD));
        assertEquals(2, data.getJsonArray(Command.HASH_TAGS_REMOVE).size());

        assertEquals("game", data.getJsonArray(Command.HASH_TAGS_REMOVE).getString(0));
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, data.getJsonArray(Command.HASH_TAGS_REMOVE).getString(1));
    }

    @Test
    public void testGuessActionI() {
        // Command mock
        JsonObject command = new GenericJsonObject();

        assertEquals(Action.demand.toString(), CommandLineParser.guessAction(command, CommandLineParser.localizedActions.get(Locale.ENGLISH)));
    }

    @Test
    public void testGuessActionII() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, "12345");

        assertEquals(Action.list.toString(), CommandLineParser.guessAction(command, CommandLineParser.localizedActions.get(Locale.ENGLISH)));
    }

    @Test
    public void testGuessActionIII() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, "12345");
        command.put(Proposal.PROPOSAL_KEY, "12345"); // Will be ignored
        command.put(Store.STORE_KEY, "12345"); // Will be ignored
        command.put(Demand.RANGE, "55.5");

        assertEquals(Action.demand.toString(), CommandLineParser.guessAction(command, CommandLineParser.localizedActions.get(Locale.ENGLISH)));
    }

    @Test
    public void testGuessActionIV() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Proposal.PROPOSAL_KEY, "12345");

        assertEquals(Action.list.toString(), CommandLineParser.guessAction(command, CommandLineParser.localizedActions.get(Locale.ENGLISH)));
    }

    @Test
    public void testGuessActionV() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Proposal.PROPOSAL_KEY, "12345");
        command.put(Proposal.PRICE, "55.5");

        assertEquals(Action.propose.toString(), CommandLineParser.guessAction(command, CommandLineParser.localizedActions.get(Locale.ENGLISH)));
    }

    public void testGuessActionVI() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Store.STORE_KEY, "12345");

        assertEquals(Action.list.toString(), CommandLineParser.guessAction(command, CommandLineParser.localizedActions.get(Locale.ENGLISH)));
    }

    @Test
    public void testGuessActionVII() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Store.STORE_KEY, "12345");
        command.put(Store.ADDRESS, "address");

        assertNull(CommandLineParser.guessAction(command, CommandLineParser.localizedActions.get(Locale.ENGLISH))); // Cannot update Store instance from Twitter
    }

    @Test
    public void testParseDueDateI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 due:2050-01-01", Locale.ENGLISH);
        assertEquals("2050-01-01T23:59:59", data.getString(Demand.DUE_DATE));
    }

    @Test
    public void testParseDueDateII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 dueDate:2050-01-01T08:31", Locale.ENGLISH);
        assertEquals("2050-01-01T08:31:00", data.getString(Demand.DUE_DATE));
    }

    @Test
    public void testParseDueDateIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 dueDate:2050-01-01T08:32 AM", Locale.ENGLISH);
        assertEquals("2050-01-01T08:32:00", data.getString(Demand.DUE_DATE));
    }

    @Test
    public void testParseDueDateIV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 dueDate:2050-01-01T08:33PM", Locale.ENGLISH);
        assertEquals("2050-01-01T20:33:00", data.getString(Demand.DUE_DATE));
    }

    @Test
    public void testParseDueDateV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 dueDate:2050-01-01T08:34:45", Locale.ENGLISH);
        assertEquals("2050-01-01T08:34:45", data.getString(Demand.DUE_DATE));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testParseDueDateVI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 dueDate:T08:35:46", Locale.ENGLISH);
        Date today = new Date();
        assertEquals(
                (today.getYear() + 1900) +
                "-" + (today.getMonth() < 9 ? "0" : "") + (today.getMonth() + 1) +
                "-" + (today.getDate() < 10 ? "0" : "") + today.getDate() +
                "T08:35:46",
                data.getString(Demand.DUE_DATE)
        );
    }

    @Test
    public void testParseDueDateVII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 dueDate:20101112", Locale.ENGLISH);
        assertEquals("2010-11-12T23:59:59", data.getString(Demand.DUE_DATE));
    }

    @Test
    public void testParseCCI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 cc:a.a@a.aa", Locale.ENGLISH);
        assertEquals("a.a@a.aa", data.getJsonArray(Demand.CC).getString(0));
    }

    @Test
    public void testParseCCII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 cc:a.a@a.aa\t", Locale.ENGLISH);
        assertEquals("a.a@a.aa", data.getJsonArray(Demand.CC).getString(0));
    }

    @Test
    public void testParseCCIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 cc:a.a@a.aa #test", Locale.ENGLISH);
        assertEquals("a.a@a.aa", data.getJsonArray(Demand.CC).getString(0));
    }

    @Test
    public void testParseCCVI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 cc:a.a@a.aa #test cc:b.b@b.bb cc:d.d@d.dd", Locale.ENGLISH);
        assertEquals("a.a@a.aa", data.getJsonArray(Demand.CC).getString(0));
        assertEquals("b.b@b.bb", data.getJsonArray(Demand.CC).getString(1));
        assertEquals("d.d@d.dd", data.getJsonArray(Demand.CC).getString(2));
    }

    @Test
    public void testParseMetadataI() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 metadata:{'name':'marcelus'} qty:1", Locale.ENGLISH);
        assertEquals("{'name':'marcelus'}", data.getString(Command.META_DATA));
    }

    @Test
    public void testParseMetadataII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 metadata:{'age':12.43} qty:1", Locale.ENGLISH);
        assertEquals("{'age':12.43}", data.getString(Command.META_DATA));
    }

    @Test
    public void testParseMetadataIII() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 metadata: { 'name' : 'marcelus junior','age' : 12.43 } qty:1", Locale.ENGLISH);
        assertEquals("{ 'name' : 'marcelus junior','age' : 12.43 }", data.getString(Command.META_DATA));
    }

    @Test
    public void testParseMetadataIV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 metadata:{} qty:1", Locale.ENGLISH);
        assertEquals("{}", data.getString(Command.META_DATA));
    }

    @Test
    public void testParseCommentI() throws ClientException, ParseException {
        String comment = "bravo pour les tests";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 comment:" + comment, Locale.ENGLISH);
        assertEquals(comment, data.getString(Proposal.COMMENT));
    }

    @Test
    public void testParseCommentII() throws ClientException, ParseException {
        String comment = "\t   \t merci   ";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 comment:" + comment, Locale.ENGLISH);
        assertEquals(comment.trim(), data.getString(Proposal.COMMENT));
    }

    @Test
    public void testParseCommentIII() throws ClientException, ParseException {
        String comment = "";
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "ref:21 comment:" + comment, Locale.ENGLISH);
        assertNull(data.getString(Proposal.COMMENT));
        assertEquals(Proposal.COMMENT + CommandLineParser.PREFIX_SEPARATOR, data.getJsonArray(Command.CRITERIA_ADD).getString(0));
    }

    @Test
    public void testParseScoreI() throws ClientException, ParseException {
        Map<String, Pattern> patterns = CommandLineParser.localizedPatterns.get(Locale.ENGLISH);
        JsonObject data;
        String value;
        value = ":)";       data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(5, data.getLong(Proposal.SCORE));
        value = ":-)";      data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(5, data.getLong(Proposal.SCORE));
        value = " \t :-) "; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(5, data.getLong(Proposal.SCORE));
    }

    @Test
    public void testParseScoreII() throws ClientException, ParseException {
        Map<String, Pattern> patterns = CommandLineParser.localizedPatterns.get(Locale.ENGLISH);
        JsonObject data;
        String value;
        value = ":|";  data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(3, data.getLong(Proposal.SCORE));
        value = ":-|"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(3, data.getLong(Proposal.SCORE));
    }

    @Test
    public void testParseScoreIII() throws ClientException, ParseException {
        Map<String, Pattern> patterns = CommandLineParser.localizedPatterns.get(Locale.ENGLISH);
        JsonObject data;
        String value;
        value = ":(";  data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(1, data.getLong(Proposal.SCORE));
        value = ":-("; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(1, data.getLong(Proposal.SCORE));
    }

    @Test
    public void testParseScoreIV() throws ClientException, ParseException {
        Map<String, Pattern> patterns = CommandLineParser.localizedPatterns.get(Locale.ENGLISH);
        JsonObject data;
        String value;
        value = "0"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(0, data.getLong(Proposal.SCORE));
        value = "1"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(1, data.getLong(Proposal.SCORE));
        value = "2"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(2, data.getLong(Proposal.SCORE));
        value = "3"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(3, data.getLong(Proposal.SCORE));
        value = "4"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(4, data.getLong(Proposal.SCORE));
        value = "5"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(5, data.getLong(Proposal.SCORE));
    }

    @Test
    public void testParseScoreV() throws ClientException, ParseException {
        Map<String, Pattern> patterns = CommandLineParser.localizedPatterns.get(Locale.ENGLISH);
        JsonObject data;
        String value;
        value = "000"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(0, data.getLong(Proposal.SCORE));
        value = "01";  data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(0, data.getLong(Proposal.SCORE));
        value = "6";   data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(0, data.getLong(Proposal.SCORE));
        value = "132"; data = CommandLineParser.parseCommand(patterns, "ref:21 score:" + value, Locale.ENGLISH); assertEquals(0, data.getLong(Proposal.SCORE));
    }

    @Test
    public void testParseScoreVI() throws ClientException, ParseException {
        Map<String, Pattern> patterns = CommandLineParser.localizedPatterns.get(Locale.ENGLISH);
        String comment = "no comment sir";
        JsonObject data;
        data = CommandLineParser.parseCommand(patterns, "rate proposal:59 score::) comment:" + comment, Locale.ENGLISH);
        assertEquals(5, data.getLong(Proposal.SCORE));
        assertEquals(comment, data.getString(Proposal.COMMENT));
        data = CommandLineParser.parseCommand(patterns, "rate proposal:59 comment:" + comment + " score::)", Locale.ENGLISH);
        assertEquals(5, data.getLong(Proposal.SCORE));
        assertEquals(comment, data.getString(Proposal.COMMENT));
    }

    @Test
    public void testParseLocaleMoreCA_Ia() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c2n6 CA", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleMoreCA_Ib() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c2n6 Ca any tag", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreCA_Ic() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c2n6 ca range:5km", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreCA_Id() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c ca range:5km", Locale.ENGLISH);
        assertEquals("H0H0H0", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreCA_Ie() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c2n6t5y ca range:5km", Locale.ENGLISH);
        assertEquals("H0H0H0", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreCA_IIa() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c2n6 ", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleMoreCA_IIb() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c2n6 any tag", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreCA_IIc() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c2n6 range:5km", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreCA_IId() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c range:5km", Locale.ENGLISH);
        assertEquals("H0H0H0", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreCA_IIe() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:h3c2n6t5y range:5km", Locale.ENGLISH);
        assertEquals("H0H0H0", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreCA_IIIa() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: h3c \t 2n6 \t ca any tag", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreCA_IIIb() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: h3 c2 n6 ca range:5km", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreCA_IIIc() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: h3c-2n6 ca any tag", Locale.ENGLISH);
        assertEquals("H3C2N6", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreCA_IV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: h3 c2 n6 range:5km", Locale.ENGLISH);
        assertEquals("H0H0H0", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
        assertTrue(data.containsKey(Demand.CRITERIA_ADD));
        assertEquals(2L, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertEquals("c2", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
        assertEquals("n6", data.getJsonArray(Demand.CRITERIA_ADD).getString(1));
    }

    @Test
    public void testParseLocaleMoreCA_Va() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: ca any tag", Locale.ENGLISH);
        assertEquals("H0H0H0", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreCA_Vb() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:ca any tag", Locale.ENGLISH);
        assertEquals("H0H0H0", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.CANADA.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreUS_Ia() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:12345 US", Locale.ENGLISH);
        assertEquals("12345", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleMoreUS_Ib() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:12345 Us any tag", Locale.ENGLISH);
        assertEquals("12345", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreUS_Ic() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:12345-6789 us range:5km", Locale.ENGLISH);
        assertEquals("12345-6789", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreUS_Id() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:1234 us range:5km", Locale.ENGLISH);
        assertEquals("00000", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreUS_Ie() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:12345 67 us range:5km", Locale.ENGLISH);
        assertEquals("00000-0000", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreUS_IIa() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:12345 ", Locale.ENGLISH);
        assertEquals("12345", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
    }

    @Test
    public void testParseLocaleMoreUS_IIb() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:12345 any tag", Locale.ENGLISH);
        assertEquals("12345", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreUS_IIc() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:12345 range:5km", Locale.ENGLISH);
        assertEquals("12345", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreUS_IId() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:1234 range:5km", Locale.ENGLISH);
        assertEquals("00000", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreUS_IIe() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:12345-67 range:5km", Locale.ENGLISH);
        assertEquals("00000-0000", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreUS_IIIa() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: 123 \t 45 \t us any tag", Locale.ENGLISH);
        assertEquals("12345", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreUS_IIIb() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: 123 45-6789 us range:5km", Locale.ENGLISH);
        assertEquals("12345-6789", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
    }

    @Test
    public void testParseLocaleMoreUS_IIIc() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: 12345 6789 us any tag", Locale.ENGLISH);
        assertEquals("00000-0000", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreUS_IV() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: 12345 6789 range:5km", Locale.ENGLISH);
        assertEquals("12345", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Demand.RANGE));
        assertTrue(data.containsKey(Demand.CRITERIA_ADD));
        assertEquals(1L, data.getJsonArray(Demand.CRITERIA_ADD).size());
        assertEquals("6789", data.getJsonArray(Demand.CRITERIA_ADD).getString(0));
    }

    @Test
    public void testParseLocaleMoreUS_Va() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale: us any tag", Locale.ENGLISH);
        assertEquals("00000", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    @Test
    public void testParseLocaleMoreUS_Vb() throws ClientException, ParseException {
        JsonObject data = CommandLineParser.parseCommand(CommandLineParser.localizedPatterns.get(Locale.ENGLISH), "demand:21 locale:us any tag", Locale.ENGLISH);
        assertEquals("00000", data.getString(Location.POSTAL_CODE));
        assertEquals(Locale.US.getCountry(), data.getString(Location.COUNTRY_CODE));
        assertTrue(data.containsKey(Command.CRITERIA_ADD));
    }

    private int[] todayNumbers;

    @SuppressWarnings("deprecation")
    private int[] getTodayNumbers() {
        if (todayNumbers == null) {
            Date today = new Date();
            todayNumbers = new int[] { today.getYear() + 1900, today.getMonth() + 1, today.getDate() };
        }
        return todayNumbers;
    }
    @Test
    public void testGetDateIa() throws ParseException {
        String expected = getTodayNumbers()[0] + "-" + (getTodayNumbers()[1] < 10 ? "0" : "") + getTodayNumbers()[1] + "-" + (getTodayNumbers()[2] < 10 ? "0" : "") + getTodayNumbers()[2] + "T23:59:59";
        String in = "due:";
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIa() throws ParseException {
        String expected = "2011-05-07" + "T23:59:59";
        String in = "due:" + expected;
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIb() throws ParseException {
        String expected = "2011-05-07" + "T23:59:59";
        String in = "due:201105-07";
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIc() throws ParseException {
        String expected = "2011-05-07" + "T23:59:59";
        String in = "due:2011-0507";
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIId() throws ParseException {
        String expected = "2011-05-07" + "T23:59:59";
        String in = "due:20110507";
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIe() throws ParseException {
        String expected = "2011-05-07" + "T23:59:59";
        String in = "due:" + expected;
        String out = CommandLineParser.getDate(in.replace('-', 'a'));
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIf() throws ParseException {
        String expected = "2011-05-07" + "T23:59:59";
        String in = "due:2011-0507";
        String out = CommandLineParser.getDate(in.replace('-', 'a'));
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIIa() throws ParseException {
        String expected = "T08:00:00";
        expected = getTodayNumbers()[0] + "-" + (getTodayNumbers()[1] < 10 ? "0" : "") + getTodayNumbers()[1] + "-" + (getTodayNumbers()[2] < 10 ? "0" : "") + getTodayNumbers()[2] + expected;
        String in = "due:T08";
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIIb() throws ParseException {
        String expected = "T08:07:00";
        expected = getTodayNumbers()[0] + "-" + (getTodayNumbers()[1] < 10 ? "0" : "") + getTodayNumbers()[1] + "-" + (getTodayNumbers()[2] < 10 ? "0" : "") + getTodayNumbers()[2] + expected;
        String in = "due:T08:07";
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIIc() throws ParseException {
        String expected = "T08:07:06";
        expected = getTodayNumbers()[0] + "-" + (getTodayNumbers()[1] < 10 ? "0" : "") + getTodayNumbers()[1] + "-" + (getTodayNumbers()[2] < 10 ? "0" : "") + getTodayNumbers()[2] + expected;
        String in = "due:T08:07:06";
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIIId() throws ParseException {
        String expected = "T08:00:00";
        expected = getTodayNumbers()[0] + "-" + (getTodayNumbers()[1] < 10 ? "0" : "") + getTodayNumbers()[1] + "-" + (getTodayNumbers()[2] < 10 ? "0" : "") + getTodayNumbers()[2] + expected;
        String in = "due:T080706";
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIVa() throws ParseException {
        String expected = "2011-01-01T01:01:01";
        String in = "due:" + expected;
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetDateIVb() throws ParseException {
        String expected = "2011-11-11T11:11:11";
        String in = "due:" + expected;
        String out = CommandLineParser.getDate(in);
        assertEquals(expected, out);
    }

    @Test
    public void testGetCountrycodeIa() {
        String in = "locale:h CA";
        assertEquals(Locale.CANADA.getCountry(), CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCountrycodeIb() {
        String in = "locale:0 US";
        assertEquals(Locale.US.getCountry(), CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCountrycodeIIa() {
        String in = "locale:h";
        // Because it starts by a letter
        assertEquals(Locale.CANADA.getCountry(), CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCountrycodeIIb() {
        String in = "locale:H";
        // Because it starts by a letter
        assertEquals(Locale.CANADA.getCountry(), CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCountrycodeIIc() {
        String in = "locale:0";
        // Because it starts by a digit
        assertEquals(Locale.US.getCountry(), CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCountrycodeIIIa() {
        String in = "locale:&"; // Before the digits
        // Default country
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCountrycodeIIIb() {
        String in = "locale:="; // Between digits and capital letters
        // Default country
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCountrycodeIIIc() {
        String in = "locale:_"; // Between the capital and the lower case letters
        // Default country
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCountrycodeIIId() {
        String in = "locale:|"; // After the lower case letters
        // Default country
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, CommandLineParser.getCountryCode(in));
    }

    @Test
    public void testGetCleanNumber() {
        String in = "qty:-1,2.3a";
        assertEquals("-1,2.3", CommandLineParser.getCleanNumber(in));
    }
}
