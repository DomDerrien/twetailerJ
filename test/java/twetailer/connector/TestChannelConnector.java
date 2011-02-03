package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import javax.cache.MockCacheFactory;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.dao.CacheHandler;
import twetailer.dto.Consumer;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestChannelConnector {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseConnector.setMockLogger(new MockLogger("test", null));
        ChannelConnector.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        CacheHandler.injectMockCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        JabberConnector.injectMockXMPPService(null);
        MockTwitterConnector.restoreTwitterConnector();
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @Test
    public void testConstructor() {
        new ChannelConnector();
    }

    @Test
    public void testRegisterConsumer() {
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);
        ChannelConnector.register(consumer); // Will trigger the cache creation
        ChannelConnector.register(consumer); // Will skip the creation path
    }

    @Test
    public void testUnregisterConsumer() {
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);
        ChannelConnector.unregister(consumer); // Nothing done as cache has not been created yet
        ChannelConnector.register(consumer);
        ChannelConnector.unregister(consumer); // Remove the instance from the cache
    }

    @Test
    public void testGetService() {
        assertNotNull(ChannelConnector.getService());
    }

    @Test
    public void testGetUserToken() {
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);

        ChannelConnector.injectMockChannelService(new ChannelService() {
            @Override public void sendMessage(ChannelMessage msg) { }
            @Override public ChannelMessage parseMessage(HttpServletRequest arg0) { return null; }
            @Override public String createChannel(String arg0) { return "channel created!"; }
        });

        assertNotNull(ChannelConnector.getUserToken(consumer));
    }

    @Test
    public void testSendMessageI() {
        //
        // Normal sending with the verification in Channel.sendMessage()
        //
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);

        final JsonObject data = new GenericJsonObject();
        data.put("1", "one");
        data.put("two", 2L);

        ChannelConnector.injectMockChannelService(new ChannelService() {
            @Override
            public void sendMessage(ChannelMessage msg) {
                assertEquals("{'two':2,'1':'one'}", msg.getMessage());
            }
            @Override public ChannelMessage parseMessage(HttpServletRequest arg0) { return null; }
            @Override public String createChannel(String arg0) { return "channel created!"; }
        });

        ChannelConnector.sendMessage(consumer, data); // Nothing cached yet
        ChannelConnector.register(consumer);
        ChannelConnector.sendMessage(consumer, data); // Cache available
    }

    @Test
    @SuppressWarnings("serial")
    public void testSendMessageII() {
        //
        // Verify that wrong JSON serialization is not pushed client-side
        //
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);

        final JsonObject data = new GenericJsonObject() {
            @Override
            public OutputStream toStream(OutputStream out, boolean isFollowed) throws IOException {
                throw new IOException("done in purpose");
            }
        };
        data.put("1", "one");
        data.put("two", "2");

        ChannelConnector.injectMockChannelService(new ChannelService() {
            @Override
            public void sendMessage(ChannelMessage msg) {
                fail("Unexpected call as a thrown exception should stop the sending");
            }
            @Override public ChannelMessage parseMessage(HttpServletRequest arg0) { return null; }
            @Override public String createChannel(String arg0) { return "channel created!"; }
        });

        ChannelConnector.register(consumer);
        ChannelConnector.sendMessage(consumer, data); // Will fail silently
    }

    @Test
    public void testSendMessageIII() {
        //
        // Verify the safeness of a Channel.sendMessage() failure
        //
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);

        final JsonObject data = new GenericJsonObject();
        data.put("1", "one");
        data.put("two", "2");

        ChannelConnector.injectMockChannelService(new ChannelService() {
            @Override
            public void sendMessage(ChannelMessage msg) {
                throw new ChannelFailureException("done in purpose");
            }
            @Override public ChannelMessage parseMessage(HttpServletRequest arg0) { return null; }
            @Override public String createChannel(String arg0) { return null; }
        });

        ChannelConnector.register(consumer);
        ChannelConnector.sendMessage(consumer, data); // Will fail silently
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageIV() {
        //
        // Verify that nothing is sent on an expired channel
        //
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);

        final JsonObject data = new GenericJsonObject();
        data.put("1", "one");
        data.put("two", 2L);

        ChannelConnector.injectMockChannelService(new ChannelService() {
            @Override
            public void sendMessage(ChannelMessage msg) {
                fail("Unexpected call as a thrown exception should stop the sending");
           }
            @Override public ChannelMessage parseMessage(HttpServletRequest arg0) { return null; }
            @Override public String createChannel(String arg0) {
                fail("Unexpected call as a thrown exception should stop the sending");
                return null;
            }
        });

        ChannelConnector.register(consumer);

        Map<Long, Long> activeChannels = (Map<Long, Long>) CacheHandler.getFromCache(ChannelConnector.MEMCACHE_IDENTIFIER);
        activeChannels.put(consumer.getKey(), 0L); // Reset the expiration date!

        ChannelConnector.sendMessage(consumer, data); // Cache available
    }
}
