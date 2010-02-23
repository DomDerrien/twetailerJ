package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javamocks.util.logging.MockLogger;

import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;

import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MockXMPPService;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestJabberConnector {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseConnector.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JabberConnector.injectMockXMPPService(null);
        helper.tearDown();
    }

    @Test
    public void testConstructor() {
        new JabberConnector();
    }

    public static void prepareStream(MockServletInputStream stream, String boundary, String jabberId, String message) {
        stream.setData(
            "--" + boundary + "\n" +
            "Content-Disposition: form-data; name=\"from\"\n" +
            "\n" +
            jabberId + "\n" +
            "--" + boundary + "\n" +
            "Content-Disposition: form-data; name=\"to\"\n" +
            "\n" +
            "twetailer@appspot.com\n" +
            "--" + boundary + "\n" +
            "Content-Disposition: form-data; name=\"body\"\n" +
            "\n" +
            message + "\n" +
            "--" + boundary + "\n" +
            "Content-Disposition: form-data; name=\"stanza\"\n" +
            "\n" +
            "<message from=\"" + jabberId + "\" to=\"twetailer@appspot.com\">\n" +
            "<body>" + message + "</body>\n" +
            "</message>"
        );
    }

    @Test
    public void testGetInstantMessageI() throws IOException {
        final String jabberId = "test-emitter@appspot.com";
        final String message = "wii console Mario Kart -- àéüôç";
        final String boundary = "B";
        final MockServletInputStream stream = new MockServletInputStream();
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getContentType() {
                return "multipart/form-data; boundary=" + boundary;
            }
            @Override
            public ServletInputStream getInputStream() {
                prepareStream(stream, boundary, jabberId, message);
                return stream;
            }
        };

        Message instantMessage = JabberConnector.getInstantMessage(request);

        assertNotNull(instantMessage);
        assertEquals(jabberId, instantMessage.getFromJid().getId());
        assertEquals(message, instantMessage.getBody());
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    @Test(expected=ClientException.class)
    public void testSendInstantMessageI() throws ClientException {
        JabberConnector.sendInstantMessage("recipient", "content");
        // No mock means no way to verify the behavior but it should work without complaining
    }

    @Test(expected=ClientException.class)
    public void testSendInstantMessagII() throws ClientException {
        MockXMPPService xmppService = new MockXMPPService();
        JabberConnector.injectMockXMPPService(xmppService);

        JabberConnector.sendInstantMessage("recipient", "content");

        // Because recipient not available!
        assertNull(xmppService.getLastSentMessage());
    }

    @Test
    public void testSendInstantMessagIII() throws ClientException {
        MockXMPPService xmppService = new MockXMPPService();
        xmppService.setPresence("recipient", true);
        JabberConnector.injectMockXMPPService(xmppService);

        JabberConnector.sendInstantMessage("recipient", "content");

        assertNotNull(xmppService.getLastSentMessage());
        assertEquals("content", xmppService.getLastSentMessage().getBody());
    }

    @Test(expected=ClientException.class)
    public void testSendInstantMessageIV() throws ClientException {
        MockXMPPService xmppService = new MockXMPPService();
        xmppService.setPresence("recipient", true);
        xmppService.injectResponseStatus(SendResponse.Status.OTHER_ERROR);

        JabberConnector.injectMockXMPPService(xmppService);
        JabberConnector.sendInstantMessage("recipient", "content");

        assertNull(xmppService.getLastSentMessage());
    }
}
