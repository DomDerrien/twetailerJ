package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Future;

import javamocks.util.logging.MockLogger;

import javax.servlet.ServletException;
import javax.servlet.http.MockHttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.MockHTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.JsonObject;

public class TestFacebookConnector {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseConnector.setMockLogger(new MockLogger("test", null));
        FacebookConnector.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        FacebookConnector.injectMockURLFetchService(null);
    }

    @Test
    public void testConstructor() {
        new FacebookConnector();
    }

    @Test
    public void testGetAppUrl() {
        String localhostData = FacebookConnector.getAppUrl(new MockHttpServletRequest() {
            @Override public String getServerName() {
                return "localhost";
            }
        });
        assertNotNull(localhostData);

        String localIPData = FacebookConnector.getAppUrl(new MockHttpServletRequest() {
            @Override public String getServerName() {
                return "127.0.0.1";
            }
        });
        assertEquals(localhostData, localIPData);

        String virtualIPData = FacebookConnector.getAppUrl(new MockHttpServletRequest() {
            @Override public String getServerName() {
                return "10.0.2.2";
            }
        });
        assertNotSame(localhostData, virtualIPData);

        String whateverData = FacebookConnector.getAppUrl(new MockHttpServletRequest() {
            @Override public String getServerName() {
                return "where-ever-else.com";
            }
        });
        assertNotSame(localhostData, whateverData);
        assertNotSame(virtualIPData, whateverData);
    }

    @Test
    public void testGetAccessTokenI() throws MalformedURLException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override
            public HTTPResponse fetch(URL arg0) throws IOException {
                assertEquals("https", arg0.getProtocol());
                assertEquals("graph.facebook.com", arg0.getHost());
                assertEquals("/oauth/access_token", arg0.getPath());
                return null;
            }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "name=value&othername=othervalue");
                response.addHeader("content-type", "text/plain; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });

        FacebookConnector.bootstrapAuthUrl(new MockHttpServletRequest() {
            @Override public String getServerName() {
                return "localhost";
            }
        });
        JsonObject response = FacebookConnector.getAccessToken("source", "code");
        assertEquals("value", response.getString("name"));
        assertEquals("othervalue", response.getString("othername"));

        FacebookConnector.bootstrapAuthUrl(new MockHttpServletRequest() {
            @Override public String getServerName() {
                return "127.0.0.1";
            }
        });
        response = FacebookConnector.getAccessToken("source", "code");
        assertEquals("value", response.getString("name"));
        assertEquals("othervalue", response.getString("othername"));

        FacebookConnector.bootstrapAuthUrl(new MockHttpServletRequest() {
            @Override public String getServerName() {
                return "10.0.2.2";
            }
        });
        response = FacebookConnector.getAccessToken("source", "code");
        assertEquals("value", response.getString("name"));
        assertEquals("othervalue", response.getString("othername"));

        FacebookConnector.bootstrapAuthUrl(new MockHttpServletRequest() {
            @Override public String getServerName() {
                return "where-ever-else.com";
            }
        });
        response = FacebookConnector.getAccessToken("source", "code");
        assertEquals("value", response.getString("name"));
        assertEquals("othervalue", response.getString("othername"));
    }

    @Test
    public void testGetAccessTokenII() throws MalformedURLException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override
            public HTTPResponse fetch(URL arg0) throws IOException {
                assertEquals("https", arg0.getProtocol());
                assertEquals("graph.facebook.com", arg0.getHost());
                assertEquals("/oauth/access_token", arg0.getPath());
                return null;
            }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "{'name':'value','othername':'othervalue'}");
                response.addHeader("content-type", "application/json; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });

        JsonObject response = FacebookConnector.getAccessToken("source", "code");
        assertEquals("value", response.getString("name"));
        assertEquals("othervalue", response.getString("othername"));
    }

    @Test(expected=IOException.class)
    public void testGetAccessTokenIII() throws MalformedURLException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override public HTTPResponse fetch(URL arg0) throws IOException {
                assertEquals("https", arg0.getProtocol());
                assertEquals("graph.facebook.com", arg0.getHost());
                assertEquals("/oauth/access_token", arg0.getPath());
                return null;
            }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "{'broken-json");
                response.addHeader("content-type", "application/json; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });

        FacebookConnector.getAccessToken("source", "code");
    }

    @Test
    public void testGetUserInfoI() throws MalformedURLException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override public HTTPResponse fetch(URL arg0) throws IOException {
                assertEquals("https", arg0.getProtocol());
                assertEquals("graph.facebook.com", arg0.getHost());
                assertEquals("/oauth/access_token", arg0.getPath());
                return null;
            }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "name=value&othername=othervalue");
                response.addHeader("content-type", "text/plain; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });

        JsonObject response = FacebookConnector.getUserInfo("accessToken");
        assertEquals("value", response.getString("name"));
        assertEquals("othervalue", response.getString("othername"));
    }

    @Test
    public void testGetUserInfoII() throws MalformedURLException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override public HTTPResponse fetch(URL arg0) throws IOException {
                assertEquals("https", arg0.getProtocol());
                assertEquals("graph.facebook.com", arg0.getHost());
                assertEquals("/oauth/access_token", arg0.getPath());
                return null;
            }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "{'name':'value','othername':'othervalue'}");
                response.addHeader("content-type", "application/json; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });

        JsonObject response = FacebookConnector.getUserInfo("accessToken");
        assertEquals("value", response.getString("name"));
        assertEquals("othervalue", response.getString("othername"));
    }

    @Test(expected=IOException.class)
    public void testGetUserInfoIII() throws MalformedURLException, IOException {
        FacebookConnector.injectMockURLFetchService(new URLFetchService() {
            @Override public HTTPResponse fetch(URL arg0) throws IOException {
                assertEquals("https", arg0.getProtocol());
                assertEquals("graph.facebook.com", arg0.getHost());
                assertEquals("/oauth/access_token", arg0.getPath());
                return null;
            }
            @Override
            public HTTPResponse fetch(HTTPRequest arg0) throws IOException {
                MockHTTPResponse response = new MockHTTPResponse(0, "{'broken-json");
                response.addHeader("content-type", "application/json; charset=UTF-8");
                return response;
            }
            @Override public Future<HTTPResponse> fetchAsync(URL arg0) { return null; }
            @Override public Future<HTTPResponse> fetchAsync(HTTPRequest arg0) { return null; }

        });

        FacebookConnector.getUserInfo("accessToken");
    }

    @Test
    public void testGetURLFetchService() {
        assertNotNull(FacebookConnector.getURLFetchService());
    }

    @Test
    public void testGetContentTypeI() {
        assertEquals("", FacebookConnector.getContentType(new MockHTTPResponse(0, "")));
    }

    @Test
    public void testGetContentTypeII() {
        MockHTTPResponse response = new MockHTTPResponse(0, "");
        response.addHeader("content-length", "0");
        assertEquals("", FacebookConnector.getContentType(response));
    }

    @Test
    public void testProcessUrlEncodedResponse() {
       Map<String, Object> data = FacebookConnector.processURLEncodedResponse(new MockHTTPResponse(0, "one=two&three"));
       assertEquals(2, data.size());
       assertEquals("two", data.get("one"));
       assertTrue(data.containsKey("three"));
       assertNull(data.get("three"));
    }

    @Test
    public void testProcessSignedRequestI() throws ServletException {
        JsonObject data = FacebookConnector.processSignedRequest(new MockHttpServletRequest() {
            @Override public String getParameter(String name) {;
                // Valid signature:
                //
                // signed_request: ef=bookmarks&count=0&signed_request=ckQgOGXtswDCrc-NprGtaM8RCGKbqkYXBdCD2mSSHLk.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImV4cGlyZXMiOjEyOTUyODcyMDAsImlzc3VlZF9hdCI6MTI5NTI4MjE5NSwib2F1dGhfdG9rZW4iOiIxNjEzNTU3ODA1NTIwNDJ8Mi42UjhLZllQXzlfUUF0QVkwaHhxX193X18uMzYwMC4xMjk1Mjg3MjAwLTYyMDAwMTMyMXxsejNrTzV5RmNTQXpOai1vd3V1TDBHLVY0VGciLCJ1c2VyIjp7ImxvY2FsZSI6ImVuX1VTIiwiY291bnRyeSI6ImNhIn0sInVzZXJfaWQiOiI2MjAwMDEzMjEifQ
                //
                // Extracted payload: JsonObject: {
                //    issued_at: 1.295282195E9,
                //    expires: 1.2952872E9,
                //    oauth_token: String: "161355780552042|2.6R8KfYP_9_QAtAY0hxq__w__.3600.1295287200-620001321|lz3kO5yFcSAzNj-owuuL0G-V4Tg",
                //    user_id: String: "620001321",
                //    user: JsonObject: {
                //      locale: String: "en_US",
                //      country: String: "ca"
                //    },
                //    algorithm: String: "HMAC-SHA256"
                //    }
                //
                if ("signed_request".equals(name)) {
                    return "ckQgOGXtswDCrc-NprGtaM8RCGKbqkYXBdCD2mSSHLk.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImV4cGlyZXMiOjEyOTUyODcyMDAsImlzc3VlZF9hdCI6MTI5NTI4MjE5NSwib2F1dGhfdG9rZW4iOiIxNjEzNTU3ODA1NTIwNDJ8Mi42UjhLZllQXzlfUUF0QVkwaHhxX193X18uMzYwMC4xMjk1Mjg3MjAwLTYyMDAwMTMyMXxsejNrTzV5RmNTQXpOai1vd3V1TDBHLVY0VGciLCJ1c2VyIjp7ImxvY2FsZSI6ImVuX1VTIiwiY291bnRyeSI6ImNhIn0sInVzZXJfaWQiOiI2MjAwMDEzMjEifQ";
                }
                fail("getting parameter " + name + " not planned");
                return null;
            }
        });
        assertNotNull(data);
        assertEquals(FacebookConnector.ENCRYPTION_ALGORITHM_FACEBOOK_NAME, data.getString(FacebookConnector.ATTR_ALGORITHM));
        assertEquals("620001321", data.getString(FacebookConnector.ATTR_USER_ID));
    }

    @Test(expected=ServletException.class)
    public void testProcessSignedRequestII() throws ServletException {
        FacebookConnector.processSignedRequest(new MockHttpServletRequest() {
            @Override public String getParameter(String name) {
                if ("signed_request".equals(name)) {
                    // Invalid data:
                    return "fdsjkhtoire-ytjrksnmdkfmnds.709efdskmjewk";
                }
                fail("getting parameter " + name + " not planned");
                return null;
            }
        });
    }

    @Test(expected=ServletException.class)
    public void testProcessSignedRequestIII() throws ServletException {
        FacebookConnector.processSignedRequest(new MockHttpServletRequest() {
            @Override public String getParameter(String name) {;
                if ("signed_request".equals(name)) {
                    String payload = "{\"" + FacebookConnector.ATTR_ALGORITHM + "\":\"unknown\"}";
                    return "ckQgOGXtswDCrc-NprGtaM8RCGKbqkYXBdCD2mSSHLk." + new String(Base64.encodeBase64URLSafeString(payload.getBytes()));
                }
                fail("getting parameter " + name + " not planned");
                return null;
            }
        });
    }

    @Test(expected=ServletException.class)
    public void testProcessSignedRequestIVa() throws ServletException {
        FacebookConnector.processSignedRequest(new MockHttpServletRequest() {
            @Override public String getServerName() { return "localhost"; }
            @Override public String getParameter(String name) {;
                if ("signed_request".equals(name)) {
                    String payload = "{\"" + FacebookConnector.ATTR_ALGORITHM + "\":\"" + FacebookConnector.ENCRYPTION_ALGORITHM_FACEBOOK_NAME + "\"}";
                    return "ckQgOGXtswDCrc-NprGtaM8RCGKbqkYXBdCD2mSSHLk." + new String(Base64.encodeBase64URLSafeString(payload.getBytes()));
                }
                fail("getting parameter " + name + " not planned");
                return null;
            }
        });
    }

    @Test(expected=ServletException.class)
    public void testProcessSignedRequestIVb() throws ServletException {
        FacebookConnector.processSignedRequest(new MockHttpServletRequest() {
            @Override public String getServerName() { return "127.0.0.1"; }
            @Override public String getParameter(String name) {;
                if ("signed_request".equals(name)) {
                    String payload = "{\"" + FacebookConnector.ATTR_ALGORITHM + "\":\"" + FacebookConnector.ENCRYPTION_ALGORITHM_FACEBOOK_NAME + "\"}";
                    return "ckQgOGXtswDCrc-NprGtaM8RCGKbqkYXBdCD2mSSHLk." + new String(Base64.encodeBase64URLSafeString(payload.getBytes()));
                }
                fail("getting parameter " + name + " not planned");
                return null;
            }
        });
    }

    @Test(expected=ServletException.class)
    public void testProcessSignedRequestIVc() throws ServletException {
        FacebookConnector.processSignedRequest(new MockHttpServletRequest() {
            @Override public String getServerName() { return "10.0.2.2"; }
            @Override public String getParameter(String name) {;
                if ("signed_request".equals(name)) {
                    String payload = "{\"" + FacebookConnector.ATTR_ALGORITHM + "\":\"" + FacebookConnector.ENCRYPTION_ALGORITHM_FACEBOOK_NAME + "\"}";
                    return "ckQgOGXtswDCrc-NprGtaM8RCGKbqkYXBdCD2mSSHLk." + new String(Base64.encodeBase64URLSafeString(payload.getBytes()));
                }
                fail("getting parameter " + name + " not planned");
                return null;
            }
        });
    }

    @Test(expected=ServletException.class)
    public void testProcessSignedRequestIVd() throws ServletException {
        FacebookConnector.processSignedRequest(new MockHttpServletRequest() {
            @Override public String getServerName() { return "where-ever-else.com"; }
            @Override public String getParameter(String name) {;
                if ("signed_request".equals(name)) {
                    String payload = "{\"" + FacebookConnector.ATTR_ALGORITHM + "\":\"" + FacebookConnector.ENCRYPTION_ALGORITHM_FACEBOOK_NAME + "\"}";
                    return "ckQgOGXtswDCrc-NprGtaM8RCGKbqkYXBdCD2mSSHLk." + new String(Base64.encodeBase64URLSafeString(payload.getBytes()));
                }
                fail("getting parameter " + name + " not planned");
                return null;
            }
        });
    }
}
