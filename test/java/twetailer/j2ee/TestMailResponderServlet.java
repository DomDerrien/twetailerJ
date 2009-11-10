package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.mail.internet.InternetAddress;
import javax.servlet.ServletInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.connector.TestMailConnector;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import domderrien.mocks.MockHttpServletRequest;
import domderrien.mocks.MockServletInputStream;

public class TestMailResponderServlet {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testConstructor() {
        new MailResponderServlet();
    }

    @Test
    public void testDoPostI() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        final Long rawCommandKey = 12345L;
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertEquals(message, rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };

        MailResponderServlet servlet = new MailResponderServlet();
        servlet.rawCommandOperations = rawCommandOperations;

        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostII() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream();
            }
        };

        final Long rawCommandKey = 12345L;
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertNull(rawCommand.getEmitterId());
                assertNull(rawCommand.getCommand());
                assertNotNull(rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };

        MailResponderServlet servlet = new MailResponderServlet();
        servlet.rawCommandOperations = rawCommandOperations;

        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostIII() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final MockServletInputStream stream = TestMailConnector.prepareEmptyStream(from, name);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        final Long rawCommandKey = 12345L;
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertEquals("", rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };

        MailResponderServlet servlet = new MailResponderServlet();
        servlet.rawCommandOperations = rawCommandOperations;

        servlet.doPost(request, null);
    }
}
