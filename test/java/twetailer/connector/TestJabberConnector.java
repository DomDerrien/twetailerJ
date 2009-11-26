package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.xmpp.Message;
import com.google.apphosting.api.MockAppEngineEnvironment;

public class TestJabberConnector {

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
    public void testGetInstantMessage() throws IOException {
        final String jabberId = "test-emitter@appspot.com";
        final String message = "wii console Mario Kart";
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

    @Test
    public void testSendInstantMessage() {
        JabberConnector.sendInstantMessage("test", "test");
    }
}
