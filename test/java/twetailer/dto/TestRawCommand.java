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
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonParser;

public class TestRawCommand {

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
        RawCommand object = new RawCommand();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    String command = "North Pole, H0H 0H0, Canada";
    String emitterId = "emitter";
    Long messageId = 67890L;
    Source source = Source.simulated;

    @Test
    public void testAccessors() {
        RawCommand object = new RawCommand();

        object.setCommand(command);
        object.setEmitterId(emitterId);
        object.setMessageId(messageId);
        object.setSource(source);
        object.setSource(source.toString());

        assertEquals(command, object.getCommand());
        assertEquals(emitterId, object.getEmitterId());
        assertEquals(messageId, object.getMessageId());
        assertEquals(source, object.getSource());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetSource() {
        RawCommand object = new RawCommand();
        object.setSource((Source) null);
    }
}
