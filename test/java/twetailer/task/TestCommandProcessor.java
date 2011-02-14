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

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.MockSettingsOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
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
        // CommandProcessor.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
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
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();

        BaseConnector.resetLastCommunicationInSimulatedMode();

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
    public void testRetrieveConsumerI() throws InvalidIdentifierException, DataSourceException {
        RawCommand rawCommand = new RawCommand(Source.simulated);

        assertNotNull(CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test
    public void testRetrieveConsumerII() throws DataSourceException, InvalidIdentifierException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(emitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        });

        RawCommand rawCommand = new RawCommand(Source.twitter);
        rawCommand.setEmitterId(emitterId);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test
    public void testRetrieveConsumerIII() throws DataSourceException, InvalidIdentifierException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.JABBER_ID, key);
                assertEquals(emitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        });

        RawCommand rawCommand = new RawCommand(Source.jabber);
        rawCommand.setEmitterId(emitterId);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test(expected=DataSourceException.class)
    public void testRetrieveConsumerIV() throws DataSourceException, InvalidIdentifierException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                // assertEquals(Consumer.FACEBOOK_ID, key);
                assertEquals(emitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        });

        RawCommand rawCommand = new RawCommand(Source.facebook); // Unsupported source
        rawCommand.setEmitterId(emitterId);

        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand);
    }

    @Test(expected=DataSourceException.class)
    public void testRetrieveConsumerV() throws DataSourceException, InvalidIdentifierException {
        final String emitterId = "emitter";

        // Mock RawCommandOperations
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Consumer>();
            }
        });

        RawCommand rawCommand = new RawCommand(Source.twitter);
        rawCommand.setEmitterId(emitterId);

        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand); // No user...
    }

    @Test(expected=DataSourceException.class)
    public void testRetrieveConsumerVI() throws DataSourceException, InvalidIdentifierException {
        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), new RawCommand());
    }

    @Test
    public void testRetrieveConsumerVII() throws DataSourceException, InvalidIdentifierException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.EMAIL, key);
                assertEquals(emitterId, (String) value);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
        });

        RawCommand rawCommand = new RawCommand(Source.mail);
        rawCommand.setEmitterId(emitterId);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test
    public void testRetrieveConsumerVIII() throws DataSourceException, InvalidIdentifierException {
        final Long robotKey = 12345L;
        RobotResponder.setRobotConsumerKey(robotKey);

        final Consumer consumer = new Consumer();
        consumer.setKey(robotKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(robotKey, key);
                return consumer;
            }
        });

        RawCommand rawCommand = new RawCommand(Source.robot);

        assertEquals(consumer, CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand));
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testProcessRawCommandWithNoMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(0L, key.longValue());
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        CommandProcessor.processRawCommands(0L);
    }

    @Test(expected=ClientException.class)
    public void testProcessRawCommandWithIncompleteMessageI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final Long commandKey = 12345L;
        // Mock RawCommandOperations
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
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
        });

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
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
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
        });
        // Mock DemandOperations
        final IllegalArgumentException exception = new IllegalArgumentException("Done in purpose");
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String attribute, Object value, int limit) {
                throw exception;
            }
        });

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
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
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
        });
        // Mock DemandOperations
        final IllegalArgumentException exception = new IllegalArgumentException("Done in purpose");
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                throw exception;
            }
        });

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
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
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
        });
        BaseSteps.setMockSettingsOperations(new MockSettingsOperations());

        CommandProcessor.processRawCommands(rawCommandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
    }

    @Test
    public void testProcessRawCommandWithUnsupportedAction() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final String action = "grrrrr";
        // Mock RawCommandOperations
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setCommand("!" + action + " ref:10 wii console quantity:1 loc:h0h0h0 ca exp:2050-01-01");
                return rawCommand;
            }
        });

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_parser_unsupported_action", new Object[] { action }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithUnallowedAction() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final String action = Action.propose.toString();

        // Mock RawCommandOperations
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setCommand("!" + action + " wii console");
                return rawCommand;
            }
        });
        // Mock SaleAssociateOperations
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Long>();
            }
        });

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_parser_reserved_action", new Object[] { action }, Locale.ENGLISH), sentText);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGenerateTweetForStoreI() {
        Store store = new Store();
        store.setKey(12345L);

        String tweet = CommandProcessor.generateTweet(store, null, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertFalse(tweet.contains(Prefix.phoneNumber.toString()));
        assertFalse(tweet.contains(Prefix.locale.toString()));
    }

    @Test
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
    public void testGenerateTweetForAnonimizedProposal() {
        Proposal proposal = new Proposal();
        proposal.setKey(12345L);
        proposal.setDemandKey(23456L);

        String tweet = CommandProcessor.generateTweet(proposal, null, true, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertFalse(tweet.contains("12345"));
        assertFalse(tweet.contains("23456"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGenerateTweetProposalI() {
        Proposal proposal = new Proposal();
        proposal.setKey(12345L);
        proposal.setDemandKey(23456L);
        proposal.setState(State.published);
        proposal.setDueDate(new Date(2011 - 1900, 0, 1, 0, 0, 0));
        proposal.addCriterion("just-one-tag");
        proposal.addHashTag("just-one-hash-tag");
        proposal.addCoordinate("unit@test.org");

        String tweet = CommandProcessor.generateTweet(proposal, null, false, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains("12345"));
        assertTrue(tweet.contains("23456"));
        assertTrue(tweet.contains("2011-01-01T00:00:00"));
        assertTrue(tweet.contains("just-one-tag"));
        assertTrue(tweet.contains("just-one-hash-tag"));
        assertTrue(tweet.contains("unit@test.org"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGenerateTweetProposalII() {
        Proposal proposal = new Proposal();
        proposal.setState(State.cancelled);

        Store store = new Store();
        store.setKey(23456L);
        store.setName("store-name");

        String tweet = CommandProcessor.generateTweet(proposal, store, false, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains("cancelled"));
        assertTrue(tweet.contains("23456"));
        assertTrue(tweet.contains("store-name"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGenerateTweetForDemandI() {
        Demand demand = new Demand();
        demand.setKey(12345L);
        demand.setState(State.published);

        String tweet = CommandProcessor.generateTweet(demand, new Location(), false, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains("12345"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGenerateTweetForDemandII() {
        Demand demand = new Demand();
        demand.setKey(12345L);
        demand.addProposalKey(23456L);
        demand.addProposalKey(34567L);
        demand.setState(State.cancelled);
        demand.setExpirationDate(new Date());
        demand.setDueDate(new Date(demand.getExpirationDate().getTime() - 10000));
        demand.addCriterion("just-one-tag");

        Location location = new Location();
        location.setPostalCode("H0H0H0");

        String tweet = CommandProcessor.generateTweet(demand, location, false, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains("12345"));
        assertTrue(tweet.contains("23456"));
        assertTrue(tweet.contains("34567"));
        assertTrue(tweet.contains("cancelled"));
        assertTrue(tweet.contains("H0H0H0 CA"));
        assertTrue(tweet.contains("just-one-tag"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGenerateTweetForDemandIII() {
        Demand demand = new Demand();
        demand.setKey(12345L);
        demand.addCoordinate("unit@test.org");

        String tweet = CommandProcessor.generateTweet(demand, new Location(), false, Locale.ENGLISH);
        assertNotSame(0, tweet.length());
        assertTrue(tweet.contains("unit@test.org"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSerializedDateIa() {
        Date date = new Date(2011 - 1900, 0, 1, 0, 0, 0);
        assertEquals("2011-01-01T00:00:00", CommandProcessor.serializeDate(date));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSerializedDateIb() {
        Date date = new Date(2011 - 1900, 0, 1, 23, 0, 0);
        assertEquals("2011-01-01T23:00:00", CommandProcessor.serializeDate(date));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSerializedDateIc() {
        Date date = new Date(2011 - 1900, 0, 1,23, 59, 0);
        assertEquals("2011-01-01T23:59:00", CommandProcessor.serializeDate(date));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSerializedDateII() {
        Date date = new Date(2011 - 1900, 0, 1, 23, 59, 59);
        assertEquals("2011-01-01", CommandProcessor.serializeDate(date));
    }

    @Test
    public void testProcessCommandI() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "");

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandII() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.help.toString());

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandIII() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.help.toString());
        command.put(Command.CRITERIA_ADD, new GenericJsonArray());
        command.getJsonArray(Command.CRITERIA_ADD).add("");

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandIV() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandV() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandVI() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandVII() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.decline.toString());

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandVIII() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandIX() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.demand.toString());
        command.put(Demand.DEMAND_KEY, 12345L);

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertNull(key);
                throw new InvalidIdentifierException("Done in purpose!");
            }
        });

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandX() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.language.toString());

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandXI() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.rate.toString());

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }

    @Test
    public void testProcessCommandXII() throws DataSourceException, ClientException {
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertNull(key);
                throw new InvalidIdentifierException("Done in purpose!");
            }
        });

        Consumer consumer = new Consumer();
        CommandLineParser.loadLocalizedSettings(consumer.getLocale());

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, new RawCommand(Source.simulated), command);
        assertNotSame(0, BaseConnector.getLastCommunicationInSimulatedMode().length());
    }
}
