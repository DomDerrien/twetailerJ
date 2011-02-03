package twetailer.j2ee;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Locale;

import javamocks.util.logging.MockLogger;

import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.BaseConnector;
import twetailer.connector.JabberConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.TestJabberConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.MockXMPPService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.LabelExtractor;

public class TestJabberResponderServlet {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        JabberResponderServlet.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
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
        JabberConnector.injectMockXMPPService(null);
    }

    @Test
    public void testConstructor() {
        new JabberResponderServlet();
    }

    @Test
    public void testDoPostI() throws IOException {
        //
        // Normal process without trouble
        //
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

        final Long consumerKey = 56645L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(JID address) {
                assertEquals(jabberId, address.getId());
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setJabberId(jabberId);
                return consumer;
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(jabberId, rawCommand.getEmitterId());
                assertEquals(message, rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        });

        JabberResponderServlet servlet = new JabberResponderServlet();

        servlet.doPost(request, null);
    }

    @Test
    public void testDoPostII() throws IOException {
        //
        // Exception while getting the consumer information (unexpected)
        // So communicate the error directly via the JabberConnector
        //
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

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(JID address) {
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(jabberId, rawCommand.getEmitterId());
                assertEquals(message, rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        });

        JabberResponderServlet servlet = new JabberResponderServlet();

        MockXMPPService mock = new MockXMPPService();
        mock.setPresence(jabberId, true);
        JabberConnector.injectMockXMPPService(mock);

        servlet.doPost(request, null);

        assertNotNull(mock.getLastSentMessage());
        assertEquals(LabelExtractor.get("error_unexpected", new Object[] { 0L, "" }, Locale.ENGLISH), mock.getLastSentMessage().getBody());
    }

    @Test
    public void testDoPostIII() throws IOException {
        //
        // Exception while getting the consumer information (datastore timeout)
        // So communicate the error directly via the JabberConnector
        //
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

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(JID address) {
                throw new DatastoreTimeoutException("Done in purpose");
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(jabberId, rawCommand.getEmitterId());
                assertEquals(message, rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        });

        JabberResponderServlet servlet = new JabberResponderServlet();

        MockXMPPService mock = new MockXMPPService();
        mock.setPresence(jabberId, true);
        JabberConnector.injectMockXMPPService(mock);

        servlet.doPost(request, null);

        assertNotNull(mock.getLastSentMessage());
        assertEquals(LabelExtractor.get("error_datastore_timeout", Locale.ENGLISH), mock.getLastSentMessage().getBody());
    }

    @Test
    public void testDoPostIV() throws IOException {
        //
        // Exception while posting the task to the queue
        // So communicate the error via the BaseConnector which has the fall-back mechanism
        //
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

        BaseSteps.setMockBaseOperations(new MockBaseOperations() {
            @Override
            public Queue getQueue() {
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        final Long consumerKey = 56645L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(JID address) {
                assertEquals(jabberId, address.getId());
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setJabberId(jabberId);
                return consumer;
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(jabberId, rawCommand.getEmitterId());
                assertEquals(message, rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                assertEquals(rawCommandKey, rawCommand.getKey());
                rawCommand.setSource(Source.simulated); // Redirection to allow the capture of the output sent via BaseConnector.communicateToConsumer()
                return rawCommand;
            }
        });

        JabberResponderServlet servlet = new JabberResponderServlet();

        servlet.doPost(request, null);

        String sentMessage = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentMessage);
        assertEquals(LabelExtractor.get("error_unexpected", new Object[] { rawCommandKey, "" }, Locale.ENGLISH), sentMessage);
    }

    @Test
    public void testDoPostV() throws IOException {
        //
        // Exception while getting the consumer information (unexpected)
        // And another exception while communicate directly on Jabber
        //
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

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(JID address) {
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(jabberId, rawCommand.getEmitterId());
                assertEquals(message, rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
        });

        JabberResponderServlet servlet = new JabberResponderServlet();

        MockXMPPService mock = new MockXMPPService();
        mock.setPresence(jabberId, false);
        JabberConnector.injectMockXMPPService(mock);

        servlet.doPost(request, null);

        assertNull(mock.getLastSentMessage());
    }

    @Test
    public void testDoPostVI() throws IOException {
        //
        // Exception while posting the task to the queue
        // And another exception while communicate with BaseConnector
        //
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

        BaseSteps.setMockBaseOperations(new MockBaseOperations() {
            @Override
            public Queue getQueue() {
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        final Long consumerKey = 56645L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(JID address) {
                assertEquals(jabberId, address.getId());
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setJabberId(jabberId);
                return consumer;
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(jabberId, rawCommand.getEmitterId());
                assertEquals(message, rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                assertEquals(rawCommandKey, rawCommand.getKey());
                rawCommand.setSource(Source.jabber); // Redirection because the MockXMPPService allows to generate an exception during the communication
                return rawCommand;
            }
        });

        JabberResponderServlet servlet = new JabberResponderServlet();

        MockXMPPService mock = new MockXMPPService();
        mock.setPresence(jabberId, false);
        JabberConnector.injectMockXMPPService(mock);

        servlet.doPost(request, null);

        assertNull(mock.getLastSentMessage());
    }

    @Test
    public void testDoPostVII() throws IOException {
        //
        // Exception while posting the task to the queue
        // And another exception while communicate with BaseConnector
        // And another exception while sending the error report to "catch-all" list
        //
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

        BaseSteps.setMockBaseOperations(new MockBaseOperations() {
            @Override
            public Queue getQueue() {
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        final Long consumerKey = 56645L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(JID address) {
                assertEquals(jabberId, address.getId());
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setJabberId(jabberId);
                return consumer;
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(RawCommand rawCommand) {
                assertEquals(jabberId, rawCommand.getEmitterId());
                assertEquals(message, rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                assertEquals(rawCommandKey, rawCommand.getKey());
                rawCommand.setSource(Source.jabber); // Redirection because the MockXMPPService allows to generate an exception during the communication
                return rawCommand;
            }
        });

        JabberResponderServlet servlet = new JabberResponderServlet();

        MockXMPPService mock = new MockXMPPService();
        mock.setPresence(jabberId, false);
        JabberConnector.injectMockXMPPService(mock);

        MailConnector.foolNextMessagePost();

        servlet.doPost(request, null);

        assertNull(mock.getLastSentMessage());
    }
}
