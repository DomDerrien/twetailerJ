package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;
import javax.mail.MessagingException;
import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.MailConnector;
import twetailer.connector.TestMailConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;

import com.google.appengine.api.mail.dev.LocalMailService;
import com.google.appengine.api.taskqueue.MockQueue;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestCatchAllMailHandlerServlet {

    private static LocalServiceTestHelper  helper;
    private static String emailDomain;

    @BeforeClass
    public static void setUpBeforeClass() {
        TestMailConnector.setUpBeforeClass();
        CatchAllMailHandlerServlet.setMockLogger(new MockLogger("test", null));
        TwitterMailNotificationHandlerServlet.setMockLogger(new MockLogger("test", null));
        MailResponderServlet.setMockLogger(new MockLogger("test", null));
        MailComposerServlet.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        emailDomain = ApplicationSettings.get().getProductEmailDomain();
    }

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testComposeAndPostMailMessage() throws MessagingException {
        MailConnector.reportErrorToAdmins(
                "unit@test.ca",
                "subject",
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************"
        );
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostI() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return "/" + MailResponderServlet.getResponderEndpoints().get(0);
            }
        };

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                rawCommand.setKey(12345L);
                return rawCommand;
            }
        });

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet() {
            @Override
            protected void forwardUnexpectedMailMessage(HttpServletRequest request, HttpServletResponse response) {
                fail("Unexpected call");
            }
        };

        servlet.doPost(request, null);

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(2, service.getSentMessages().size());

        assertTrue(service.getSentMessages().get(0).getTo(0).contains(from));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains(name));
        assertEquals(subject, service.getSentMessages().get(0).getSubject());

        try {
            assertNull(service.getSentMessages().get(1).getTo(0));
            fail("Message should not have a To field, as it has been sent to 'admins'");
        }
        catch(NullPointerException ex) { } // Expected as no To record is added when a message is sent to "admins"
        assertTrue(service.getSentMessages().get(1).getSubject().startsWith("Unexpected error"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostII() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "[[[toName---test-recipient@appspot.com---subject]]]\nwii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return "/" + MailComposerServlet.getResponderEndpoints().get(0);
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet() {
            @Override
            protected void forwardUnexpectedMailMessage(HttpServletRequest request, HttpServletResponse response) {
                fail("Unexpected call");
            }
        };

        servlet.doPost(request, new MockHttpServletResponse());

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());

        assertTrue(service.getSentMessages().get(0).getTo(0).contains("test-recipient@appspot.com"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("toName"));
        assertEquals("subject", service.getSentMessages().get(0).getSubject());
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostIII() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not a new follower notification";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return "/" + TwitterMailNotificationHandlerServlet.getResponderEndpoints().get(0);
            }
        };

        BaseSteps.setMockBaseOperations(new BaseOperations() {
            @Override
            public Queue getQueue() {
                return new MockQueue();
            }
        });

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet() {
            @Override
            protected void forwardUnexpectedMailMessage(HttpServletRequest request, HttpServletResponse response) {
                fail("Unexpected call");
            }
        };

        servlet.doPost(request, null);

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());

        try {
            assertNull(service.getSentMessages().get(0).getTo(0));
            fail("Message should not have a To field, as it has been sent to 'admins'");
        }
        catch(NullPointerException ex) { } // Expected as no To record is added when a message is sent to "admins"
        assertTrue(service.getSentMessages().get(0).getSubject().startsWith("Fwd:"));
        assertTrue(service.getSentMessages().get(0).getSubject().contains("Not a new follower notification"));
    }

    @Test
    public void testDoPostIVa() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return "/anything-else@" + emailDomain;
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet();

        servlet.doPost(request, null);

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());

        try {
            assertNull(service.getSentMessages().get(0).getTo(0));
            fail("Message should not have a To field, as it has been sent to 'admins'");
        }
        catch(NullPointerException ex) { } // Expected as no To record is added when a message is sent to "admins"
        assertTrue(service.getSentMessages().get(0).getSubject().startsWith("Fwd:"));
        assertTrue(service.getSentMessages().get(0).getSubject().contains(from));
        assertTrue(service.getSentMessages().get(0).getSubject().contains(name));
        assertTrue(service.getSentMessages().get(0).getSubject().contains("Unexpected e-mail"));
    }

    @Test
    public void testDoPostIVb() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return "";
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet();

        servlet.doPost(request, null);

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());

        try {
            assertNull(service.getSentMessages().get(0).getTo(0));
            fail("Message should not have a To field, as it has been sent to 'admins'");
        }
        catch(NullPointerException ex) { } // Expected as no To record is added when a message is sent to "admins"
        assertTrue(service.getSentMessages().get(0).getSubject().startsWith("Fwd:"));
        assertTrue(service.getSentMessages().get(0).getSubject().contains(from));
        assertTrue(service.getSentMessages().get(0).getSubject().contains(name));
        assertTrue(service.getSentMessages().get(0).getSubject().contains("Unexpected e-mail"));
    }

    @Test
    public void testDoPostIVc() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return null;
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet();

        servlet.doPost(request, null);

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());

        try {
            assertNull(service.getSentMessages().get(0).getTo(0));
            fail("Message should not have a To field, as it has been sent to 'admins'");
        }
        catch(NullPointerException ex) { } // Expected as no To record is added when a message is sent to "admins"
        assertTrue(service.getSentMessages().get(0).getSubject().startsWith("Fwd:"));
        assertTrue(service.getSentMessages().get(0).getSubject().contains(from));
        assertTrue(service.getSentMessages().get(0).getSubject().contains(name));
        assertTrue(service.getSentMessages().get(0).getSubject().contains("Unexpected e-mail"));
    }

    @Test
    public void testDoPostIVd() throws IOException {
        final String from = null;
        final String name = null;
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return null;
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet();

        servlet.doPost(request, null);

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(0, service.getSentMessages().size());
    }
}
