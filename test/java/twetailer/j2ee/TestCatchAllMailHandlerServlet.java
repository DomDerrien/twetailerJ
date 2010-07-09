package twetailer.j2ee;

import java.io.IOException;

import javamocks.util.logging.MockLogger;

import javax.mail.MessagingException;
import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.TestMailConnector;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestCatchAllMailHandlerServlet {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        CatchAllMailHandlerServlet.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
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
        CatchAllMailHandlerServlet.composeAndPostMailMessage(
                "unit@test.ca",
                "subject",
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************"
        );
    }

    @Test
    public void testDoPostI() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return MailResponderServlet.responderEndpoints.get(0);
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet();

        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostII() throws IOException {
        final String from = null;
        final String name = null;
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return MailResponderServlet.responderEndpoints.get(0);
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet();

        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostIII() throws IOException {
        final String from = null;
        final String name = "not important";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return MailResponderServlet.responderEndpoints.get(0);
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet();

        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostIV() throws IOException {
        final String from = "";
        final String name = "";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                return MailResponderServlet.responderEndpoints.get(0);
            }
        };

        CatchAllMailHandlerServlet servlet = new CatchAllMailHandlerServlet();

        servlet.doPost(request, null);
    }
}
