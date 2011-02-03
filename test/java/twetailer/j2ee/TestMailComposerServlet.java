package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javamocks.io.MockInputStream;
import javamocks.util.logging.MockLogger;

import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.TestMailConnector;
import twetailer.dao.MockBaseOperations;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;

import com.google.appengine.api.mail.dev.LocalMailService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestMailComposerServlet {

    private static LocalServiceTestHelper  helper;
    private static String emailDomain;

    @BeforeClass
    public static void setUpBeforeClass() {
        MailComposerServlet.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        emailDomain = ApplicationSettings.get().getProductEmailDomain();
    }

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testConstructor() {
        new MailComposerServlet();
    }

    @Test
    public void testDoPostI() throws IOException {
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
                fail("Not expected");
                return null;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new MailComposerServlet().doPost(request, response);

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());

        assertTrue(service.getSentMessages().get(0).getTo(0).contains("test-recipient@appspot.com"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("toName"));
        assertEquals("subject", service.getSentMessages().get(0).getSubject());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDoPostII() throws IOException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "Mal formed!!!";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
            @Override
            public String getPathInfo() {
                fail("Not expected");
                return null;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new MailComposerServlet().doPost(request, response);

        assertEquals(500, response.getStatus());
    }

    @Test
    public void testDoPostIII() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() throws IOException {
                throw new IOException("Done in purpose");
            }
            @Override
            public String getPathInfo() {
                fail("Not expected");
                return null;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new MailComposerServlet().doPost(request, response);

        assertEquals(500, response.getStatus());
    }

    @Test
    public void testGerResponderEndPoints() throws IOException {
        assertNotNull(MailComposerServlet.getResponderEndpoints());
        assertNotNull(MailComposerServlet.getResponderEndpoints());
    }
}
