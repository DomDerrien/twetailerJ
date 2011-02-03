package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Future;

import javamocks.util.logging.MockLogger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;
import javax.servlet.http.MockHttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.FacebookConnector;
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.MockHTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;

import domderrien.jsontools.JsonObject;

public class TestAuthVerifierFilter {

    @BeforeClass
    public static void setUpBeforeClass() {
        AuthVerifierFilter.setMockLogger(new MockLogger("test", null));
        FacebookConnector.setMockLogger(new MockLogger("test", null));
    }

    OpenIdUser user;

    @Before
    public void setUp() throws Exception {
        FacebookConnector.injectMockURLFetchService(null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        new AuthVerifierFilter();
    }

    @Test
    public void testInit() throws ServletException {
        new AuthVerifierFilter().init(null);
    }

    @Test
    public void testDestroy() throws ServletException {
        new AuthVerifierFilter().destroy();
    }

    @Test
    public void testDoFilterI() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String key) {
                if ("code".equals(key)) {
                    return null;
                }
                fail("Unexpected parameter retrieval");
                return null;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                assertNull(((HttpServletRequest) request).getParameter("code"));
                assertNotNull(response);
                ((HttpServletResponse) response).setStatus(200);
            }
        };
        new AuthVerifierFilter().doFilter(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDoFilterII() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String key) {
                if ("code".equals(key)) {
                    return "";
                }
                fail("Unexpected parameter retrieval");
                return null;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                assertEquals("", ((HttpServletRequest) request).getParameter("code"));
                assertNotNull(response);
                ((HttpServletResponse) response).setStatus(200);
            }
        };
        new AuthVerifierFilter().doFilter(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDoFilterIII() throws ServletException, IOException {
        final Long consumerKey = 6547634L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(JsonObject info) {
                assertEquals("token", info.getString(FacebookConnector.ATTR_ACCESS_TOKEN));
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        });
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override public HTTPResponse fetch(URL arg0) throws IOException { return null; }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "{'" + FacebookConnector.ATTR_ACCESS_TOKEN + "':'token'}");
                response.addHeader("content-type", "application/json; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });
        final MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String key) {
                if ("code".equals(key)) {
                    return "test-code";
                }
                fail("Unexpected parameter retrieval");
                return null;
            }
            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer("http://unit-test.org/");
            }
            @Override
            public HttpSession getSession(boolean create) {
                assertTrue(create);
                return session;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) {
                assertEquals("http://unit-test.org/", url);
                setStatus(200);
            }
        };
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                fail("Unexpected filtering");
            }
        };
        new AuthVerifierFilter().doFilter(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDoFilterIV() throws ServletException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override public HTTPResponse fetch(URL arg0) throws IOException { return null; }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "{'" + FacebookConnector.ATTR_ACCESS_TOKEN + "':null}");
                response.addHeader("content-type", "application/json; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String key) {
                if ("code".equals(key)) {
                    return "test-code";
                }
                fail("Unexpected parameter retrieval");
                return null;
            }
            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer("http://unit-test.org/");
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) {
                assertTrue(url.startsWith(FacebookConnector.FB_GRAPH_AUTH_URL));
                setStatus(200);
            }
        };
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                fail("Unexpected filtering");
            }
        };
        new AuthVerifierFilter().doFilter(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDoFilterV() throws ServletException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override public HTTPResponse fetch(URL arg0) throws IOException { return null; }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "{'" + FacebookConnector.ATTR_ACCESS_TOKEN + "':''}");
                response.addHeader("content-type", "application/json; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String key) {
                if ("code".equals(key)) {
                    return "test-code";
                }
                fail("Unexpected parameter retrieval");
                return null;
            }
            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer("http://unit-test.org/");
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) {
                assertTrue(url.startsWith(FacebookConnector.FB_GRAPH_AUTH_URL));
                setStatus(200);
            }
        };
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                fail("Unexpected filtering");
            }
        };
        new AuthVerifierFilter().doFilter(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test(expected=IOException.class)
    public void testDoFilterVI() throws ServletException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override public HTTPResponse fetch(URL arg0) throws IOException { return null; }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                throw new IOException("Done in purpose!");
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String key) {
                if ("".equals(key)) {
                    // For the dump, @see override of getParameterNames() below
                    return "";
                }
                if ("code".equals(key)) {
                    return "test-code";
                }
                fail("Unexpected parameter retrieval");
                return null;
            }
            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer("http://unit-test.org/");
            }
            @Override public Enumeration<?> getAttributeNames() { Vector<String> names = new Vector<String>(1); names.add(""); return names.elements(); }
            @Override public Enumeration<?> getHeaderNames() { Vector<String> names = new Vector<String>(1); names.add(""); return names.elements(); }
            @Override public Enumeration<?> getParameterNames() { Vector<String> names = new Vector<String>(1); names.add(""); return names.elements(); }
            @Override public String getAttribute(String ame) { return ""; }
            @Override public String getHeader(String name) { return ""; }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) {
                fail("Unexpected call");
            }
        };
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                fail("Unexpected filtering");
            }
        };
        new AuthVerifierFilter().doFilter(request, response, filterChain);
    }
}
