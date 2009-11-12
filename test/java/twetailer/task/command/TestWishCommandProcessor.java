package twetailer.task.command;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.MockPersistenceManager;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twitter4j.TwitterException;

public class TestWishCommandProcessor {

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
        new WishCommandProcessor();
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
}
