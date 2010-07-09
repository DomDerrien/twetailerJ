package twetailer.task.command;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.RawCommand;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestLanguageCommandProcessor {

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
        new LanguageCommandProcessor();
    }

    @Test
    public void testProcessCommandLanguageDisplay() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.language.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(
                "cp_command_language_missing_language_code",
                new Object[] { LocaleValidator.DEFAULT_LANGUAGE, LocaleValidator.DEFAULT_DISPLAY_LANGUAGE },
                Locale.ENGLISH
        )));
    }

    @Test
    public void testProcessCommandLanguageSetSame() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.language.toString());
        JsonArray criteria = new GenericJsonArray();
        criteria.add("en");
        command.put(Demand.CRITERIA_ADD, criteria);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(
                "cp_command_language_given_value_as_current",
                new Object[] { "en", "English" },
                Locale.ENGLISH
        )));
    }

    @Test
    public void testProcessCommandLanguageSetIncorrect() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.language.toString());
        JsonArray criteria = new GenericJsonArray();
        criteria.add("zzzzzz");
        command.put(Demand.CRITERIA_ADD, criteria);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(
                "cp_command_language_given_value_as_current",
                new Object[] { "en", "English" },
                Locale.ENGLISH
        )));
    }

    @Test
    public void testProcessCommandLanguageSetNew() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.language.toString());
        JsonArray criteria = new GenericJsonArray();
        criteria.add(Locale.FRENCH.getLanguage());
        command.put(Demand.CRITERIA_ADD, criteria);

        // ConsumerOperations mock
        final Long consumerKey = 76325L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(consumerKey, consumer.getKey());
                assertEquals(Locale.FRENCH.getLanguage(), consumer.getLanguage());
                return consumer;
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(LabelExtractor.get(
                "cp_command_language_given_value_accepted",
                new Object[] { "fr", LocaleValidator.toUnicode("Français"), "en" },
                Locale.FRENCH
        )));
    }
}
