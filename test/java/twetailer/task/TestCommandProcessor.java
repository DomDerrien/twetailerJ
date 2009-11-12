package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

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
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;
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
        BaseConnector.resetLastCommunicationInSimulatedMode();

        CommandProcessor._baseOperations = new BaseOperations();
        CommandProcessor.consumerOperations = CommandProcessor._baseOperations.getConsumerOperations();
        CommandProcessor.demandOperations = CommandProcessor._baseOperations.getDemandOperations();
        CommandProcessor.locationOperations = CommandProcessor._baseOperations.getLocationOperations();
        CommandProcessor.proposalOperations = CommandProcessor._baseOperations.getProposalOperations();
        CommandProcessor.rawCommandOperations = CommandProcessor._baseOperations.getRawCommandOperations();
        CommandProcessor.saleAssociateOperations = CommandProcessor._baseOperations.getSaleAssociateOperations();
        CommandProcessor.settingsOperations = CommandProcessor._baseOperations.getSettingsOperations();
        // CommandProcessor.storeOperations = CommandProcessor._baseOperations.getStoreOperations();

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

        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.twitter);
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

        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.mail);
        rawCommand.setEmitterId(emitterId);

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
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
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
        // Mock SaleAssociateOperations
        SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<SaleAssociate>();
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
}
