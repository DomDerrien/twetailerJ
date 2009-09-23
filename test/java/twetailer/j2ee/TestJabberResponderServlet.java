package twetailer.j2ee;

import java.io.IOException;

import javax.servlet.ServletInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.connector.TestJabberConnector;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.RawCommand;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;

import domderrien.mocks.MockHttpServletRequest;
import domderrien.mocks.MockServletInputStream;

public class TestJabberResponderServlet {

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
        new JabberResponderServlet();
    }

    @Test
    public void testDoPost() throws IOException {
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
                TestJabberConnector.prepareStream(stream, boundary, jabberId, message);
                return stream;
            }
        };

        final Long rawCommandKey = 12345L;
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        };

        JabberResponderServlet servlet = new JabberResponderServlet();
        servlet.rawCommandOperations = rawCommandOperations;

        servlet.doPost(request, null);
    }
}
