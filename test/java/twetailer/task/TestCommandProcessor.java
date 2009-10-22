package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Retailer;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;

public class TestCommandProcessor {

    private String firstLetterToUpper (String key) {
        return key.substring(0, 1).toUpperCase(Locale.ENGLISH) + key.substring(1).toLowerCase(Locale.ENGLISH);
    }

    @Before
    public void setUp() throws Exception {
        // Simplified list of prefixes
        CommandProcessor.localizedPrefixes.clear();
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
        CommandProcessor.localizedPrefixes.put(Locale.ENGLISH, prefixes);

        // Simplified list of actions
        CommandProcessor.localizedActions.clear();
        JsonObject actions = new GenericJsonObject();
        for (Action action: Action.values()) {
            JsonArray equivalents = new GenericJsonArray();
            equivalents.add(action.toString());
            actions.put(action.toString(), equivalents);
        }
        CommandProcessor.localizedActions.put(Locale.ENGLISH, actions);

        // Simplified list of states
        CommandProcessor.localizedStates.clear();
        JsonObject states = new GenericJsonObject();
        for (State state: State.values()) {
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

        CommandProcessor._baseOperations = new BaseOperations();
        CommandProcessor.consumerOperations = CommandProcessor._baseOperations.getConsumerOperations();
        CommandProcessor.demandOperations = CommandProcessor._baseOperations.getDemandOperations();
        CommandProcessor.locationOperations = CommandProcessor._baseOperations.getLocationOperations();
        CommandProcessor.proposalOperations = CommandProcessor._baseOperations.getProposalOperations();
        CommandProcessor.rawCommandOperations = CommandProcessor._baseOperations.getRawCommandOperations();
        CommandProcessor.retailerOperations = CommandProcessor._baseOperations.getRetailerOperations();
        CommandProcessor.settingsOperations = CommandProcessor._baseOperations.getSettingsOperations();
        // CommandProcessor.storeOperations = CommandProcessor._baseOperations.getStoreOperations();

        CommandProcessor.localizedPrefixes = new HashMap<Locale, JsonObject>();
        CommandProcessor.localizedActions = new HashMap<Locale, JsonObject>();
        CommandProcessor.localizedStates = new HashMap<Locale, JsonObject>();
        CommandProcessor.localizedHelpKeywords = new HashMap<Locale, JsonObject>();
        CommandProcessor.localizedPatterns = new HashMap<Locale, Map<String, Pattern>>();
    }

    @Test
    public void testConstructor() {
        new CommandProcessor();
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

        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.facebook); // Unsupported source
        rawCommand.setEmitterId(emitterId);

        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand);
    }

    @Test(expected=DataSourceException.class)
    public void testRetrieveConsumerVI() throws DataSourceException {
        final String emitterId = "emitter";
        final Consumer consumer = new Consumer();

        // Mock RawCommandOperations
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Consumer>();
            }
        };
        CommandProcessor.consumerOperations = consumerOperations;

        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.twitter);
        rawCommand.setEmitterId(emitterId);

        CommandProcessor.retrieveConsumer(new MockPersistenceManager(), rawCommand); // No user...
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
                assertEquals(0L, key.longValue());
                throw new DataSourceException("Done in purpose");
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);
    }

    @Test
    public void testProcessRawCommandWithIncompleteMessageI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final Long commandKey = 12345L;
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(commandKey);
                rawCommand.setSource(Source.simulated);
                // No command
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(commandKey, rawCommand.getKey());
                assertNotNull(rawCommand.getErrorMessage());
                assertNotSame("", rawCommand.getErrorMessage());
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        BaseConnector.resetLastCommunicationInSimulatedMode();

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_unexpected_error", new Object[] { commandKey, "" }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithIncompleteMessageII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final Long commandKey = 12345L;
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(commandKey);
                rawCommand.setCommand("!list test blah-blah-bla");
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(commandKey, rawCommand.getKey());
                assertNotNull(rawCommand.getErrorMessage());
                assertNotSame("", rawCommand.getErrorMessage());
                rawCommand.setSource(Source.simulated);
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
        assertEquals(LabelExtractor.get("cp_unexpected_error", new Object[] { commandKey, "" }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithIncompleteMessageIII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final Long commandKey = 12345L;
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(commandKey);
                rawCommand.setCommand("!list test blah-blah-bla " + CommandProcessor.DEBUG_INFO_SWITCH);
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(commandKey, rawCommand.getKey());
                assertNotNull(rawCommand.getErrorMessage());
                assertNotSame("", rawCommand.getErrorMessage());
                rawCommand.setSource(Source.simulated);
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
        assertEquals(LabelExtractor.get("cp_unexpected_error", new Object[] { commandKey, CommandProcessor.getDebugInfo(exception) }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithSimpleMessage() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
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
        final String action = "grrrrr";
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
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
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("!" + action + " wii console");
                return rawCommand;
            }
        };
        // Mock RetailerOperations
        RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Retailer>();
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_parser_reserved_action", new Object[] { action }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithPrefixI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("help: " + Prefix.action.toString());
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get(ResourceFileId.second, firstLetterToUpper(Prefix.action.toString()), Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithPrefixII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("!help " + Prefix.action.toString());
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get(ResourceFileId.second, firstLetterToUpper(Prefix.action.toString()), Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithPrefixIII() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand("? " + Prefix.action.toString());
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get(ResourceFileId.second, firstLetterToUpper(Prefix.action.toString()), Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithPrefixIV() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand(Prefix.action.toString() + "?");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get(ResourceFileId.second, firstLetterToUpper(Prefix.action.toString()), Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithActionI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand(Action.demand.toString() + "?");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get(ResourceFileId.second, firstLetterToUpper(Action.demand.toString()), Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithStateI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand(State.invalid.toString() + "?");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;
        CommandProcessor.localizedStates.clear();
        CommandProcessor.localizedStates = new HashMap<Locale, JsonObject>();

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get(ResourceFileId.second, firstLetterToUpper(State.invalid.toString()), Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithRegisteredHelpKeywordI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final String helpKeyword = "deposit-test";
        final String helpKeywordEquivalent = "prepay-test";

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
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
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, helpKeyword, Locale.ENGLISH)));
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
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.help.toString()); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
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
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
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
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
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
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
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
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
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
                assertEquals(State.cancelled, demand.getState());
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
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
                assertEquals(State.cancelled, demand.getState());
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
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(Prefix.state.toString()+":"+State.cancelled.toString()));
    }

    @Test
    public void testProcessCommandCancelIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                throw new RuntimeException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_demand_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelIV() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_missing_demand_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelV() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 2222L;
        final Long retailerKey = 3333L;
        final Long proposalKey = 5555L;

        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Retailer.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
                assertEquals(State.cancelled, proposal.getState());
                return proposal;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(Prefix.state.toString()+":"+State.cancelled.toString()));
    }

    @Test
    public void testProcessCommandCancelVI() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 2222L;
        final Long retailerKey = 3333L;
        final Long proposalKey = 5555L;

        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Retailer.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                throw new RuntimeException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCloseI() throws TwitterException, DataSourceException, ClientException {
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
                assertEquals(State.closed, demand.getState());
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
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
    public void testProcessCommandCloseII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State demandState = State.confirmed;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(demandState);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.closed, demand.getState());
                return demand;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                assertTrue(parameters.containsKey(Proposal.DEMAND_KEY));
                assertEquals(demandKey, (Long) parameters.get(Proposal.DEMAND_KEY));
                assertTrue(parameters.containsKey(Command.STATE));
                assertEquals(demandState.toString(), (String) parameters.get(Command.STATE));
                return new ArrayList<Proposal>();
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
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
    public void testProcessCommandCloseIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State demandState = State.confirmed;
        final Long proposalKey = 6666L;
        final Long retailerKey = 7777L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(demandState);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.closed, demand.getState());
                return demand;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                assertTrue(parameters.containsKey(Proposal.DEMAND_KEY));
                assertEquals(demandKey, (Long) parameters.get(Proposal.DEMAND_KEY));
                assertTrue(parameters.containsKey(Command.STATE));
                assertEquals(demandState.toString(), (String) parameters.get(Command.STATE));
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(Source.simulated);
                List<Proposal> proposals = new ArrayList<Proposal>();
                proposals.add(proposal);
                return proposals;
            }
        };
        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public Retailer getRetailer(PersistenceManager pm, Long key) {
                assertEquals(retailerKey, key);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                return retailer;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandCloseIV() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_close_invalid_demand_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCloseV() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final Long consumerKey = 6666L;
        final Long retailerKey = 7777L;
        final State proposalState = State.confirmed;

        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Retailer.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(retailerKey, rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(Source.simulated);
                proposal.setState(proposalState);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.closed, proposal.getState());
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                assertTrue(parameters.containsKey(Demand.PROPOSAL_KEYS));
                assertEquals(proposalKey, (Long) parameters.get(Demand.PROPOSAL_KEYS));
                assertTrue(parameters.containsKey(Command.STATE));
                assertEquals(proposalState.toString(), (String) parameters.get(Command.STATE));
                return new ArrayList<Demand>();
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandCloseVI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final Long consumerKey = 6666L;
        final Long retailerKey = 7777L;
        final Long demandKey = 888888L;
        final State proposalState = State.confirmed;

        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Retailer.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(retailerKey, rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(Source.simulated);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.closed, proposal.getState());
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                assertTrue(parameters.containsKey(Demand.PROPOSAL_KEYS));
                assertEquals(proposalKey, (Long) parameters.get(Demand.PROPOSAL_KEYS));
                assertTrue(parameters.containsKey(Command.STATE));
                assertEquals(proposalState.toString(), (String) parameters.get(Command.STATE));
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(proposalState);
                demand.setOwnerKey(consumerKey);
                demand.setSource(Source.simulated);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        };
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(proposalKey);
                return consumer;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.consumerOperations = consumerOperations;
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandCloseVII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final Long consumerKey = 6666L;
        final Long retailerKey = 7777L;
        final State proposalState = State.confirmed;

        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Retailer.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(retailerKey, rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(Source.simulated);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.closed, proposal.getState());
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                assertTrue(parameters.containsKey(Demand.PROPOSAL_KEYS));
                assertEquals(proposalKey, (Long) parameters.get(Demand.PROPOSAL_KEYS));
                assertTrue(parameters.containsKey(Command.STATE));
                assertEquals(proposalState.toString(), (String) parameters.get(Command.STATE));
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandCloseVIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final Long consumerKey = 6666L;

        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Retailer.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_close_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCloseIX() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_close_invalid_parameters", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandConfirmI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long retailerKey = 6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setState(State.published);
                proposal.setStoreKey(storeKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.confirmed, proposal.getState());
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.addCriterion("test");
                demand.addProposalKey(proposalKey);
                demand.setState(State.published);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.confirmed, demand.getState());
                return demand;
            }
        };
        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public Retailer getRetailer(PersistenceManager pm, Long key) {
                assertEquals(retailerKey, key);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                retailer.setPreferredConnection(Source.simulated);
                return retailer;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText); // Informs the consumer
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(storeKey.toString()));
        assertTrue(sentText.contains("test"));
        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText); // Informs the retailer
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandConfirmII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_confirm_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandConfirmIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long retailerKey = 6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setState(State.published);
                proposal.setStoreKey(storeKey);
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_confirm_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandConfirmIV() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long retailerKey = 6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setState(State.published);
                proposal.setStoreKey(storeKey);
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.addCriterion("test");
                demand.addProposalKey(proposalKey);
                demand.setState(State.invalid); // Blocks the confirmation
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText); // Informs the consumer
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(State.invalid.toString()));
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandDecline() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.decline.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test
    public void testProcessCommandDemandI() throws Exception {
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
        command.put(Command.ACTION, Action.demand.toString());
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandDemandII() throws Exception {
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
            public Demand getDemand(PersistenceManager pm, Long demandKey, Long consumerKey) {
                throw new RuntimeException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.demand.toString());
        command.put(Demand.REFERENCE, demandKey);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_demand_invalid_demand_id", Locale.ENGLISH), sentText);
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
        command.put(Command.ACTION, Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_no_active_demand", Locale.ENGLISH), sentText);
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
        command.put(Command.ACTION, Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
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
        command.put(Command.ACTION, Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
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
        command.put(Command.ACTION, Action.list.toString());
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
        command.put(Command.ACTION, Action.list.toString());
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
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                throw new DataSourceException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_invalid_demand_id", new Object[] { demandKey }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandListVII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long retailerKey = 4444L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertEquals(retailerKey, rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                return proposal;
            }
        };
        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, Retailer.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                retailer.setConsumerKey(consumerKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandListVIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long retailerKey = 4444L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertEquals(retailerKey, rKey);
                throw new DataSourceException("Done in purpose");
            }
        };
        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, Retailer.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                retailer.setConsumerKey(consumerKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_invalid_proposal_id", new Object[] { proposalKey }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandProposeI() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long retailerKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal createProposal(PersistenceManager pm, JsonObject parameters, Retailer retailer) {
                assertEquals(consumerKey, retailer.getConsumerKey());
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                return proposal;
            }
        };
        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, Retailer.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                retailer.setConsumerKey(consumerKey);
                retailer.setStoreKey(storeKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandProposeII() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long retailerKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setState(State.published); // To be able to verify the reset to "open"
                proposal.setStoreKey(storeKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.opened, proposal.getState());
                proposal.setKey(proposalKey);
                return proposal;
            }
        };
        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, Retailer.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                retailer.setConsumerKey(consumerKey);
                retailer.setStoreKey(storeKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandProposeIII() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long retailerKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // RetailerOperations mock
        final RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, Retailer.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                retailer.setConsumerKey(consumerKey);
                retailer.setStoreKey(storeKey);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(retailer);
                return retailers;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.retailerOperations = retailerOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_proposal_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandSupply() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandWish() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.wish.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test(expected=ClientException.class)
    public void testProcessCommandWWW() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.www.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);
    }

    @Test
    public void testGuessActionI() {
        // Command mock
        JsonObject command = new GenericJsonObject();

        assertEquals(Action.demand.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionII() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.demand.toString());

        assertEquals(Action.demand.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionIII() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, "12345");

        assertEquals(Action.list.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionIV() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, "12345");
        command.put(Demand.RANGE, "55.5");

        assertEquals(Action.demand.toString(), CommandProcessor.guessAction(command));
    }

    @Test
    public void testGuessActionV() {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Store.STORE_KEY, "12345");

        assertEquals(Action.list.toString(), CommandProcessor.guessAction(command));
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
    public void testProcessExisitingDemandI() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessExisitingDemandII() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessExisitingDemandIII() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessFirstNewDemandI() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessFirstNewDemandII() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessFirstNewDemandIII() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandI() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandII() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandIII() throws Exception {
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
                assertEquals(State.opened, demand.getState());
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

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandProcessor.localizedPrefixes.get(Locale.ENGLISH), CommandProcessor.localizedActions.get(Locale.ENGLISH));
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }
}
