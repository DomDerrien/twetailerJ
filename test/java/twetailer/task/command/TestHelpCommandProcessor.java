package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;

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
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
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

public class TestHelpCommandProcessor {

    private String firstLetterToUpper (String key) {
        return key.substring(0, 1).toUpperCase(Locale.ENGLISH) + key.substring(1).toLowerCase(Locale.ENGLISH);
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        TestCommandProcessor.setUpBeforeClass();
    }

    @Before
    public void setUp() throws Exception {
        new TestCommandProcessor().setUp();
    }

    @After
    public void tearDown() throws Exception {
        new TestCommandProcessor().tearDown();
    }

    @Test
    public void testConstructor() {
        new HelpCommandProcessor();
    }

    @Test
    public void testProcessRawCommandWithDataFromCache() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final String data = "test";
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return data;
            }
            @Override
            public Object setInCache(String key, Object value) {
                assertEquals(data, (String) value);
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setCommand("? squizzzzzzy");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(data, sentText);
    }

    @Test
    public void testProcessRawCommandWithPrefixI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
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
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
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
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
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
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
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
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
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
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
                rawCommand.setCommand(State.invalid.toString() + "?");
                return rawCommand;
            }
        };
        CommandProcessor.rawCommandOperations = rawCommandOperations;
        CommandLineParser.localizedStates.clear();
        CommandLineParser.localizedStates = new HashMap<Locale, JsonObject>();

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get(ResourceFileId.second, firstLetterToUpper(State.invalid.toString()), Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessRawCommandWithRegisteredHelpKeywordI() throws JsonException, TwitterException, DataSourceException, ParseException, ClientException {
        final String helpKeyword = "deposit-test";
        final String helpKeywordEquivalent = "prepay-test";

        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Mock RawCommandOperations
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(0L, key.longValue());
                RawCommand rawCommand = new RawCommand(Source.simulated);
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
        CommandLineParser.localizedHelpKeywords.clear();
        CommandLineParser.localizedHelpKeywords.put(Locale.ENGLISH, helpKeywords);

        CommandProcessor.processRawCommands(0L);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, helpKeyword, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpI() throws TwitterException, DataSourceException, ClientException {
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, ""); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpII() throws TwitterException, DataSourceException, ClientException {
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.help.toString()); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpIII() throws TwitterException, DataSourceException, ClientException {
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "\t : zzz"); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpIV() throws TwitterException, DataSourceException, ClientException {
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "zzz:"); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpV() throws TwitterException, DataSourceException, ClientException {
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "zzz\t"); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }

    @Test
    public void testProcessCommandHelpVI() throws TwitterException, DataSourceException, ClientException {
        // SettingsOperations mock
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Object getFromCache(String key) {
                return null;
            }
            @Override
            public Object setInCache(String key, Object value) {
                return value;
            }
        };
        CommandProcessor.settingsOperations = ops;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.NEED_HELP, "zzz "); // No keyword, just the help system call

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(ResourceFileId.second, CommandSettings.HELP_INTRODUCTION_MESSAGE_ID, Locale.ENGLISH)));
    }
}
