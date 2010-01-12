package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.mail.internet.InternetAddress;
import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.TestMailConnector;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;

import com.google.apphosting.api.MockAppEngineEnvironment;

public class TestMailResponderServlet {

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    @BeforeClass
    public static void setUpBeforeClass() {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
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
        final String subject = "Not important!";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
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
        servlet._baseOperations = new MockBaseOperations();
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
        servlet._baseOperations = new MockBaseOperations();
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
        servlet._baseOperations = new MockBaseOperations();
        servlet.rawCommandOperations = rawCommandOperations;

        servlet.doPost(request, null);
    }

    public static MockServletInputStream prepareCorruptedStream(String from, String name) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: multipart/alternative; boundary=BBBBBB\n" +
                "\n" +
                "--BBBBBB\n" +
                "\n" +
                "--BBBBBB--"
        );
        return stream;
    }

    @Test
    public void testDoPostIV() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return prepareCorruptedStream(from, name);
            }
        };

        final Long consumerKey = 12345L;
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(InternetAddress address) {
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setEmail(from);
                return consumer;
            }
        };

        final Long rawCommandKey = 12345L;
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertNotNull(from, rawCommand.getEmitterId());
                assertNull(rawCommand.getCommand());
                assertNotNull(rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };

        MailResponderServlet servlet = new MailResponderServlet();
        servlet._baseOperations = new MockBaseOperations();
        servlet.consumerOperations = consumerOperations;
        servlet.rawCommandOperations = rawCommandOperations;

        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostV() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return prepareCorruptedStream(from, name);
            }
        };

        final Long consumerKey = 12345L;
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(InternetAddress address) {
                Consumer consumer = new Consumer() {
                    int accessNbAllowed = 1;
                    @Override
                    public String getEmail() {
                        if (0 < accessNbAllowed) {
                            -- accessNbAllowed;
                            return from;
                        }
                        // To generate UnsupportedEncodingException in MailConnector.setMailMessage()
                        return "@@@@";
                    }
                };
                consumer.setKey(consumerKey);
                return consumer;
            }
        };

        final Long rawCommandKey = 12345L;
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertNotNull(from, rawCommand.getEmitterId());
                assertNull(rawCommand.getCommand());
                assertNotNull(rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };

        MailResponderServlet servlet = new MailResponderServlet();
        servlet._baseOperations = new MockBaseOperations();
        servlet.consumerOperations = consumerOperations;
        servlet.rawCommandOperations = rawCommandOperations;

        servlet.doPost(request, null);
    }
}
