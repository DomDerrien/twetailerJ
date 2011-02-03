package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.MockCache;
import javax.cache.MockCacheFactory;
import javax.jdo.PersistenceManager;
import javax.servlet.MockServletInputStream;
import javax.servlet.MockServletOutputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;
import javax.servlet.http.MockHttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.InvalidIdentifierException;
import twetailer.connector.ChannelConnector;
import twetailer.dao.CacheHandler;
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twetailer.task.CommandProcessor;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;

public class TestChannelServlet {

    @Before
    public void setUpBeforeClass() throws Exception {
        ChannelServlet.setMockLogger(new MockLogger("test", null));
        ChannelConnector.setMockLogger(new MockLogger("test", null));
        CacheHandler.setMockLogger(new MockLogger("test", null));
    }

    OpenIdUser user;

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        user = MockLoginServlet.buildMockOpenIdUser();
    }

    @After
    public void tearDown() throws Exception {
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @Test
    public void testConstructor() {
        new ChannelServlet();
    }

    @Test
    public void testDoGetIa() throws IOException {
        //
        // No logged user, no session
        //
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public HttpSession getSession(boolean createSession) {
                assertEquals(false, createSession);
                return null;
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return null;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ChannelServlet().doPost(mockRequest, mockResponse);
        assertEquals(401, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoGetIb() throws IOException {
        //
        // Logged user, one session
        //
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public HttpSession getSession(boolean createSession) {
                assertEquals(false, createSession);
                return new MockHttpSession();
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ChannelServlet().doPost(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoGetIc() throws IOException {
        //
        // Logged user, one session, debug mode, error while accessing cache
        //
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public HttpSession getSession(boolean createSession) {
                assertEquals(false, createSession);
                return new MockHttpSession();
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{'" + CommandProcessor.DEBUG_INFO_SWITCH + "':'yes','action':'register'}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        CacheHandler.injectMockCacheFactory(new MockCacheFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public Cache createCache(Map arg0) throws CacheException {
                throw new CacheException("Done in purpose!");
            }

        });

        new ChannelServlet().doPost(mockRequest, mockResponse);
        assertEquals(500, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoGetII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return null;
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ChannelServlet().doPost(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoGetIII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{'action':'getToken'}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer consumer = new Consumer();
                consumer.setKey(key);
                return consumer;
            }
        });

        final String token = "ytrlkvfdskjltrlekjds-7665-43";
        ChannelConnector.injectMockChannelService(new ChannelService() {
            @Override public String createChannel(String arg0) { return token; }
            @Override public ChannelMessage parseMessage(HttpServletRequest arg0) { return null; }
            @Override public void sendMessage(ChannelMessage arg0) { }
        });

        new ChannelServlet().doPost(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("token"));
        assertTrue(stream.contains(token));
    }

    @Test
    public void testDoGetIV() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{'action':'register'}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer consumer = new Consumer();
                consumer.setKey(key);
                return consumer;
            }
        });

        CacheHandler.injectMockCache(new MockCache(null));

        new ChannelServlet().doPost(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoGetV() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{'action':'unregister'}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(MockLoginServlet.DEFAULT_CONSUMER_KEY, key);
                Consumer consumer = new Consumer();
                consumer.setKey(key);
                return consumer;
            }
        });

        CacheHandler.injectMockCache(new MockCache(null));

        new ChannelServlet().doPost(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }
}
