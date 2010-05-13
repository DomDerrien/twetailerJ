package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;

public class TestCommandProcessor {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        // CommandProcessor.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();

        // Simplified list of prefixes
        CommandLineParser.localizedPrefixes.clear();
        JsonObject prefixes = new GenericJsonObject();
        for (Prefix prefix: Prefix.values()) {
            JsonArray equivalents = new GenericJsonArray();
            equivalents.add(prefix.toString());
            if (Prefix.action.equals(prefix)) {
                equivalents.add("!");
            }
            else if (Prefix.help.equals(prefix)) {
                equivalents.add("?");
            }
            prefixes.put(prefix.toString(), equivalents);
        }
        CommandLineParser.localizedPrefixes.put(Locale.ENGLISH, prefixes);

        // Simplified list of actions
        CommandLineParser.localizedActions.clear();
        JsonObject actions = new GenericJsonObject();
        for (Action action: Action.values()) {
            JsonArray equivalents = new GenericJsonArray();
            equivalents.add(action.toString());
            actions.put(action.toString(), equivalents);
        }
        CommandLineParser.localizedActions.put(Locale.ENGLISH, actions);

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
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();

        BaseConnector.resetLastCommunicationInSimulatedMode();

        CommandProcessor._baseOperations = new BaseOperations();
        CommandProcessor.consumerOperations = CommandProcessor._baseOperations.getConsumerOperations();
        CommandProcessor.demandOperations = CommandProcessor._baseOperations.getDemandOperations();
        CommandProcessor.locationOperations = CommandProcessor._baseOperations.getLocationOperations();
        CommandProcessor.proposalOperations = CommandProcessor._baseOperations.getProposalOperations();
        CommandProcessor.rawCommandOperations = CommandProcessor._baseOperations.getRawCommandOperations();
        CommandProcessor.saleAssociateOperations = CommandProcessor._baseOperations.getSaleAssociateOperations();
        CommandProcessor.settingsOperations = CommandProcessor._baseOperations.getSettingsOperations();
        CommandProcessor.storeOperations = CommandProcessor._baseOperations.getStoreOperations();

        CommandLineParser.localizedPrefixes = new HashMap<Locale, JsonObject>();
        CommandLineParser.localizedActions = new HashMap<Locale, JsonObject>();
        CommandLineParser.localizedStates = new HashMap<Locale, JsonObject>();
        CommandLineParser.localizedHelpKeywords = new HashMap<Locale, JsonObject>();
        CommandLineParser.localizedPatterns = new HashMap<Locale, Map<String, Pattern>>();
    }

    @Test
    public void testConstructor() {
        new CommandProcessor();
    }

    @Test
    public void testRetrieveConsumerI() throws DataSourceException {
        RawCommand rawCommand = new RawCommand(Source.simulated);

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

        RawCommand rawCommand = new RawCommand(Source.twitter);
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

        RawCommand rawCommand = new RawCommand(Source.jabber);
        rawCommand.setEmitterId(emitterId);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test(expected=DataSourceException.class)
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

        RawCommand rawCommand = new RawCommand(Source.facebook); // Unsupported source
        rawCommand.setEmitterId(emitterId);

        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand);
    }

    @Test(expected=DataSourceException.class)
    public void testRetrieveConsumerV() throws DataSourceException {
        final String emitterId = "emitter";

        // Mock RawCommandOperations
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Consumer>();
            }
        };
        CommandProcessor.consumerOperations = consumerOperations;

        RawCommand rawCommand = new RawCommand(Source.twitter);
        rawCommand.setEmitterId(emitterId);

        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand); // No user...
    }

    @Test(expected=DataSourceException.class)
    public void testRetrieveConsumerVI() throws DataSourceException {
        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), new RawCommand());
    }

    @Test
    public void testRetrieveConsumerVII() throws DataSourceException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(emitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        };
        CommandProcessor.consumerOperations = consumerOperations;

        RawCommand rawCommand = new RawCommand(Source.mail);
        rawCommand.setEmitterId(emitterId);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test
    public void testRetrieveConsumerVIII() throws DataSourceException {
        final Long robotKey = 12345L;
        RobotResponder.setRobotConsumerKey(robotKey);

        final Consumer consumer = new Consumer();
        consumer.setKey(robotKey);
        CommandProcessor.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(robotKey, key);
                return consumer;
            }
        };

        RawCommand rawCommand = new RawCommand(Source.robot);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test(expected=DataSourceException.class)
    public void testProcessRawCommandWithNoMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(0L, key.longValue());
                throw new DataSourceException("Done in purpose");
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);
    }

    @Test(expected=ClientException.class)
    public void testProcessRawCommandWithIncompleteMessageI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final Long commandKey = 12345L;
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setKey(commandKey);
                // No command
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(commandKey, rawCommand.getKey());
                assertNotNull(rawCommand.getErrorMessage());
                assertNotSame("", rawCommand.getErrorMessage());
                // rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        BaseConnector.resetLastCommunicationInSimulatedMode();

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("error_unexpected", new Object[] { commandKey, "" }, Locale.ENGLISH), sentText);
    }

    @Test(expected=ClientException.class)
    public void testProcessRawCommandWithIncompleteMessageII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final Long commandKey = 12345L;
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setKey(commandKey);
                rawCommand.setCommand("!list test blah-blah-bla");
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(commandKey, rawCommand.getKey());
                assertNotNull(rawCommand.getErrorMessage());
                assertNotSame("", rawCommand.getErrorMessage());
                // rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;
        // Mock DemandOperations
        final IllegalArgumentException exception = new IllegalArgumentException("Done in purpose");
        DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String attribute, Object value, int limit) {
                throw exception;
            }
        };
        CommandProcessor.demandOperations = demandOperations;

        BaseConnector.resetLastCommunicationInSimulatedMode();

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("error_unexpected", new Object[] { commandKey, "" }, Locale.ENGLISH), sentText);
    }

    @Test(expected=ClientException.class)
    public void testProcessRawCommandWithIncompleteMessageIII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final Long commandKey = 12345L;
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setKey(commandKey);
                rawCommand.setCommand("!list test blah-blah-bla " + CommandProcessor.DEBUG_INFO_SWITCH);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(commandKey, rawCommand.getKey());
                assertNotNull(rawCommand.getErrorMessage());
                assertNotSame("", rawCommand.getErrorMessage());
                // rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;
        // Mock DemandOperations
        final IllegalArgumentException exception = new IllegalArgumentException("Done in purpose");
        DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                throw exception;
            }
        };
        CommandProcessor.demandOperations = demandOperations;

        BaseConnector.resetLastCommunicationInSimulatedMode();

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("error_unexpected", new Object[] { commandKey, CommandProcessor.getDebugInfo(exception) }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithSimpleMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final Long rawCommandKey = 1345L;
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setKey(key);
                rawCommand.setCommand("? demand");
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(rawCommandKey, rawCommand.getKey());
                assertEquals("? demand", rawCommand.getCommand());
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(rawCommandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
    }

    @Test
    public void testProcessRawCommandWithUnsupportedAction() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final String action = "grrrrr";
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setCommand("!" + action + " ref:10 wii console quantity:1 loc:h0h0h0 ca exp:2050-01-01");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_parser_unsupported_action", new Object[] { action }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithUnallowedAction() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final String action = Action.propose.toString();

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setCommand("!" + action + " wii console");
                return rawCommand;
            }
        };
        // Mock SaleAssociateOperations
        SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Long>();
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_parser_reserved_action", new Object[] { action }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testGenerateTweetForStoreI() {
        Store store = new Store();
        store.setKey(12345L);
        String name = "Grumb LLC inc.";
        store.setName(name);
        store.setAddress("432 Lane W, Montreal, Qc, Canada");

        String tweet = CommandProcessor.generateTweet(store, null, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains(name));
        assertFalse(tweet.contains(Prefix.phoneNumber.toString()));
        assertFalse(tweet.contains(Prefix.locale.toString()));
    }

    @Test
    public void testGenerateTweetForStoreII() {
        Store store = new Store();
        String name = "Grumb LLC inc.";
        store.setName(name);
        store.setAddress("432 Lane W, Montreal, Qc, Canada");
        String phoneNumber = "+1 (514) 111-2233 #444-555";
        store.setPhoneNumber(phoneNumber);

        String tweet = CommandProcessor.generateTweet(store, null, Locale.ENGLISH);

        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains(name));
        assertTrue(tweet.contains("phone")); // Prefix.phoneNumber.toString()));
        assertTrue(tweet.contains(phoneNumber));
        assertFalse(tweet.contains(Prefix.locale.toString()));
    }

    @Test
    public void testGenerateTweetForStoreIII() {
        Store store = new Store();
        store.setKey(12345L);
        String name = "Grumb LLC inc.";
        store.setName(name);
        store.setAddress("432 Lane W, Montreal, Qc, Canada");
        String phoneNumber = "+1 (514) 111-2233 #444-555";
        store.setPhoneNumber(phoneNumber);
        Location location = new Location();
        location.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        location.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);

        String tweet = CommandProcessor.generateTweet(store, location, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains(name));
        assertTrue(tweet.contains(phoneNumber));
        assertTrue(tweet.contains(Prefix.locale.toString()));
        assertTrue(tweet.contains(RobotResponder.ROBOT_POSTAL_CODE));
        assertTrue(tweet.contains(RobotResponder.ROBOT_COUNTRY_CODE));
    }

    @Test
    public void testGenerateTweetForStoreIV() {
        Store store = new Store();
        store.setKey(12345L);
        String name = "Grumb LLC inc.";
        store.setName(name);
        store.setAddress("432 Lane W, Montreal, Qc, Canada");
        String phoneNumber = "+1 (514) 111-2233 #444-555";
        store.setPhoneNumber(phoneNumber);
        Location location = new Location();
        location.setLatitude(45.5D);
        location.setLongitude(-73.3D);

        String tweet = CommandProcessor.generateTweet(store, location, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains(name));
        assertTrue(tweet.contains(phoneNumber));
        assertFalse(tweet.contains(Prefix.locale.toString()));
    }

    @Test
    public void testGenerateTweetForDemandWithHashtag() {
        Demand demand = new Demand();
        demand.addHashTag("one");
        demand.addHashTag("two");
        demand.addHashTag("three");

        String tweet = CommandProcessor.generateTweet(demand, null, false, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains("#one"));
        assertTrue(tweet.contains("#two"));
        assertTrue(tweet.contains("#three"));
    }

    @Test
    public void testGenerateTweetForAnonimizedDemand() {
        Demand demand = new Demand();
        demand.setKey(12345L);
        demand.addProposalKey(23456L);
        demand.addProposalKey(34567L);

        String tweet = CommandProcessor.generateTweet(demand, null, true, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertFalse(tweet.contains("12345"));
        assertFalse(tweet.contains("23456"));
        assertFalse(tweet.contains("34567"));
    }

    @Test
    public void testGenerateTweetForAnonimizedProposal() {
        Proposal demand = new Proposal();
        demand.setKey(12345L);
        demand.setDemandKey(23456L);

        String tweet = CommandProcessor.generateTweet(demand, null, true, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertFalse(tweet.contains("12345"));
        assertFalse(tweet.contains("23456"));
    }

    @Test
    public void testGenerateTweetForDemandI() {
        Demand demand = new Demand();
        demand.setKey(12345L);

        String tweet = CommandProcessor.generateTweet(demand, null, false, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains("12345"));
    }

    @Test
    public void testGenerateTweetForDemandII() {
        Demand demand = new Demand();
        demand.setKey(12345L);
        demand.addProposalKey(23456L);
        demand.addProposalKey(34567L);

        String tweet = CommandProcessor.generateTweet(demand, null, false, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains("12345"));
        assertTrue(tweet.contains("23456"));
        assertTrue(tweet.contains("34567"));
    }
}
