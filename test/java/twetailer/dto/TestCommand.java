package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.dao.BaseOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.validator.CommandSettings;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonParser;

public class TestCommand {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();

        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testConstructorI() {
        Command object = new Command();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Command object = new Command(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    CommandSettings.Action action = CommandSettings.Action.cancel;
    Long consumerKey = 12345L;
    CommandSettings.State state = CommandSettings.State.closed;
    Long tweetId = 67890L;

    @Test
    public void testAccessors() {
        Command object = new Command();

        object.setAction(action);
        object.setAction(action.toString());
        object.setConsumerKey(consumerKey);
        object.setState(state);
        object.setState(state.toString());
        object.setTweetId(tweetId);

        assertEquals(action, object.getAction());
        assertEquals(action, object.getAction());
        assertEquals(consumerKey, object.getConsumerKey());
        assertEquals(state, object.getState());
        assertEquals(state, object.getState());
        assertEquals(tweetId, object.getTweetId());
    }

    @Test
    public void testJsonCommandsI() {
        Command object = new Command();

        object.setAction(action);
        object.setConsumerKey(consumerKey);
        object.setState(state);
        object.setTweetId(tweetId);

        Command clone = new Command(object.toJson());

        assertEquals(action, clone.getAction());
        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(state, clone.getState());
        assertEquals(tweetId, clone.getTweetId());
    }

    @Test
    public void testJsonCommandsII() {
        Command object = new Command();

        assertNull(object.getConsumerKey());
        assertNull(object.getTweetId());

        Command clone = new Command(object.toJson());

        assertNull(clone.getConsumerKey());
        assertNull(clone.getTweetId());
    }
}
