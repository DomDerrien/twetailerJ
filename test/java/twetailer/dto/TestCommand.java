package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
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
    Long ownerKey = 12345L;
    Long rawCommandId = 67890L;
    Source source = Source.simulated;
    CommandSettings.State state = CommandSettings.State.closed;

    @Test
    public void testAccessors() {
        Command object = new Command();

        object.setAction(action);
        object.setAction(action.toString());
        object.setOwnerKey(ownerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setState(state);
        object.setState(state.toString());

        assertEquals(action, object.getAction());
        assertEquals(action, object.getAction());
        assertEquals(ownerKey, object.getOwnerKey());
        assertEquals(rawCommandId, object.getRawCommandId());
        assertEquals(source, object.getSource());
        assertEquals(state, object.getState());
    }

    @Test
    public void testJsonCommandsI() {
        Command object = new Command();

        object.setAction(action);
        object.setOwnerKey(ownerKey);
        object.setRawCommandId(rawCommandId);
        object.setSource(source);
        object.setState(state);

        Command clone = new Command(object.toJson());

        assertEquals(action, clone.getAction());
        assertEquals(ownerKey, clone.getOwnerKey());
        assertEquals(rawCommandId, clone.getRawCommandId());
        assertEquals(source, clone.getSource());
        assertEquals(state, clone.getState());
    }

    @Test
    public void testJsonCommandsII() {
        Command object = new Command();
        object.setSource(source);

        assertNull(object.getOwnerKey());
        assertNull(object.getRawCommandId());

        Command clone = new Command(object.toJson());

        assertNull(clone.getOwnerKey());
        assertNull(clone.getRawCommandId());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetAction() {
        Command object = new Command();

        object.setAction((Action) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetSource() {
        Command object = new Command();

        object.setSource((Source) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetState() {
        Command object = new Command();

        object.setState((State) null);
    }
}
