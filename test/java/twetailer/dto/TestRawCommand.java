package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestRawCommand {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testConstructorI() {
        RawCommand object = new RawCommand();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() {
        RawCommand object = new RawCommand(Source.mail);
        assertEquals(Source.mail, object.getSource());
    }

    Long key = 43645243L;
    String command = "North Pole, H0H 0H0, Canada";
    String commandId = "command";
    String emitterId = "emitter";
    String errorMessage = "no error to report!";
    Long messageId = 67890L;
    Source source = Source.simulated;
    String subject = "subject";
    String toId = "to";

    @Test
    public void testAccessors() {
        RawCommand object = new RawCommand();

        object.setCommand(command);
        object.setCommandId(commandId);
        object.setEmitterId(emitterId);
        object.setErrorMessage(errorMessage);
        object.setMessageId(messageId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setSubject(subject);
        object.setToId(toId);

        assertEquals(command, object.getCommand());
        assertEquals(commandId, object.getCommandId());
        assertEquals(emitterId, object.getEmitterId());
        assertEquals(errorMessage, object.getErrorMessage());
        assertEquals(messageId, object.getMessageId());
        assertEquals(source, object.getSource());
        assertEquals(subject, object.getSubject());
        assertEquals(toId, object.getToId());
    }

    @Test
    public void testSetCommand() {
        RawCommand object = new RawCommand();
        object.setCommand(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetSource() {
        RawCommand object = new RawCommand();
        object.setSource((Source) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetEmitterId() {
        RawCommand object = new RawCommand();
        object.setEmitterId(null);
    }

    @Test
    public void testSetErrorMessage() {
        RawCommand object = new RawCommand();
        object.setErrorMessage(null);
        assertNull(object.getErrorMessage());
        object.setErrorMessage("");
        assertNull(object.getErrorMessage());
        object.setErrorMessage("something");
        assertNotNull(object.getErrorMessage());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetMessageId() {
        RawCommand object = new RawCommand();
        object.setMessageId(null);
    }

    @Test
    public void testSetSubject() {
        RawCommand object = new RawCommand();
        object.setSubject(null);
        assertNull(object.getSubject());
        object.setSubject("");
        assertNull(object.getSubject());
        object.setSubject("something");
        assertNotNull(object.getSubject());
    }

    @Test
    public void testExclusionOfDTwetailer() {
        RawCommand command = new RawCommand();
        String given, expected;

        expected = "d";
        given = expected;
        command.setCommand(given);
        assertEquals(expected, command.getCommand());

        expected = "d test";
        given = expected;
        command.setCommand(given);
        assertEquals(expected, command.getCommand());

        expected = "";
        given = "d aseconomyhub " + expected;
        command.setCommand(given);
        assertEquals(expected, command.getCommand());

        expected = "";
        given = "   d aseconomyhub   " + expected;
        command.setCommand(given);
        assertEquals(expected, command.getCommand());

        expected = "wii console";
        given = "   d ASEconomyHub   " + expected;
        command.setCommand(given);
        assertEquals(expected, command.getCommand());
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        RawCommand object = new RawCommand();

        object.setKey(key);

        object.setCommand(command);
        object.setCommandId(commandId);
        object.setEmitterId(emitterId);
        object.setErrorMessage(errorMessage);
        object.setMessageId(messageId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setSubject(subject);
        object.setToId(toId);

        RawCommand clone = new RawCommand();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(command, clone.getCommand());
        assertEquals(commandId, clone.getCommandId());
        assertEquals(emitterId, clone.getEmitterId());
        assertEquals(errorMessage, clone.getErrorMessage());
        assertEquals(messageId, clone.getMessageId());
        assertEquals(source, clone.getSource());
        assertEquals(subject, clone.getSubject());
        assertEquals(toId, clone.getToId());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        RawCommand object = new RawCommand();

        RawCommand clone = new RawCommand();
        clone.fromJson(object.toJson(), true, true);

        assertNull(clone.getCommand());
        assertNull(clone.getCommandId());
        assertNull(clone.getEmitterId());
        assertNull(clone.getErrorMessage());
        assertNull(clone.getMessageId());
        assertNull(clone.getSource());
        assertNull(clone.getSubject());
        assertNull(clone.getToId());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        RawCommand object = new RawCommand();

        object.setKey(key);

        object.setCommand(command);
        object.setCommandId(commandId);
        object.setEmitterId(emitterId);
        object.setErrorMessage(errorMessage);
        object.setMessageId(messageId);
        object.setSource(source);
        object.setSource(source.toString());
        object.setSubject(subject);
        object.setToId(toId);

        RawCommand clone = new RawCommand();
        clone.fromJson(object.toJson(), true, false);

        assertEquals(key, clone.getKey());

        assertEquals(command, clone.getCommand());
        assertEquals(commandId, clone.getCommandId());
        assertEquals(emitterId, clone.getEmitterId());
        assertEquals(errorMessage, clone.getErrorMessage());
        assertEquals(messageId, clone.getMessageId());
        assertEquals(source, clone.getSource());
        assertEquals(subject, clone.getSubject());
        assertEquals(toId, clone.getToId());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsIV() {
        //
        // User update (lower)
        //
        new RawCommand().fromJson(null);
    }
}
