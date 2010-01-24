package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javamocks.util.logging.MockLogger;

import javax.servlet.MockServletInputStream;
import javax.servlet.MockServletOutputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.task.CommandProcessor;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;
import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestBaseRestlet {

    @SuppressWarnings("serial")
    class MockBaseRestlet extends BaseRestlet {
        private Logger _logger;
        @Override
        protected Logger getLogger() {
            if(_logger == null) {
                _logger = new MockLogger(MockBaseRestlet.class.getName(), null);
            }
            return _logger;
        }
        protected void setLogger(Logger logger) {
            _logger = logger;
        }
        @Override
        protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
            return null;
        }
        @Override
        protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        }
        @Override
        protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
            return null;
        }
        @Override
        protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
            return null;
        }
        @Override
        protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
            return null;
        }
    }

    public static final String LOGGED_USER_OPEN_ID = "http://unit.test";
    public static final Long LOGGED_USER_CONSUMER_KEY = 12345L;

    public static OpenIdUser setupOpenIdUser() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );

        //  {
        //      a: "claimId",
        //      b: "identity",
        //      c: "assocHandle",
        //      d: associationData,
        //      e: "openIdServer",
        //      f: "openIdDelegate",
        //      g: attributes,
        //      h: "identifier"
        //  }
        Map<String, Object> json = new HashMap<String, Object>();

        json.put("a", LOGGED_USER_OPEN_ID);

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", new HashMap<String, String>());
        json.put("g", attributes);

        user.fromJSON(json);

        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, LOGGED_USER_CONSUMER_KEY);

        return user;
    }

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
        user = setupOpenIdUser();
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testInit() throws ServletException {
        (new MockBaseRestlet()).init(null);
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoGetI() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return null;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        final JsonArray resources = new GenericJsonArray();
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
                assertEquals(in, parameters.getMap());
                return resources;
            }
        };
        mockRestlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("resources"));
        assertTrue(stream.contains("[]"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoGetII() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "";
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        final JsonArray resources = new GenericJsonArray();
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
                assertEquals(in, parameters.getMap());
                return resources;
            }
        };
        mockRestlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("resources"));
        assertTrue(stream.contains("[]"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoGetIII() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/current";
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
            public Map<String, ?> getParameterMap() {
                return in;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonObject getResource(JsonObject parameters, String id, OpenIdUser loggedUser) throws DataSourceException {
                JsonObject resource = new GenericJsonObject();
                assertEquals(in, parameters.getMap());
                assertEquals("current", id);
                assertEquals(user, loggedUser);
                resource.put("id", id);
                return resource;
            }
        };
        mockRestlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("resource"));
        assertTrue(stream.contains("{"));
        assertTrue(stream.contains("id"));
        assertTrue(stream.contains("current"));
        assertTrue(stream.contains("}"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoGetIV() throws IOException {
        final String uid = "uid1212";
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonObject getResource(JsonObject parameters, String id, OpenIdUser loggedUser) throws DataSourceException {
                JsonObject resource = new GenericJsonObject();
                assertEquals(in, parameters.getMap());
                assertEquals(uid, id);
                assertEquals(user, loggedUser);
                resource.put("id", id);
                return resource;
            }
        };
        mockRestlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("resource"));
        assertTrue(stream.contains("{"));
        assertTrue(stream.contains("id"));
        assertTrue(stream.contains(uid));
        assertTrue(stream.contains("}"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoGetV() throws IOException {
        final String uid = "<!uid1212:>";
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Unsupported URL format"));
        assertTrue(stream.contains(uid));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoGetVI() throws IOException {
        final String uid = "<!uid1212:>";
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet();
        mockRestlet.setLogger(new MockLogger("Not important", null) {
            @Override
            public Level getLevel() {
                return Level.INFO;
            }
        });

        mockRestlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
    }

    @Test
    public void testdoPutI() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return null;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Required path info"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testdoPutII() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "";
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Required path info"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testdoPutIII() throws IOException {
        final String uid = "uid1212";
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonObject updateResource(JsonObject parameters, String id, OpenIdUser loggedUser) throws DataSourceException {
                JsonObject resource = new GenericJsonObject();
                assertEquals(in, parameters.getMap());
                assertEquals(uid, id);
                assertEquals(user, loggedUser);
                resource.put("id", id);
                return resource;
            }
        };

        mockRestlet.doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("resource"));
        assertTrue(stream.contains("{"));
        assertTrue(stream.contains("id"));
        assertTrue(stream.contains(uid));
        assertTrue(stream.contains("}"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testdoPutIV() throws IOException {
        final String uid = "<!uid1212:>";
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Unsupported URL format"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testdoPutV() throws IOException {
        final String uid = "<!uid1212:>";
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();
        mockRestlet.setLogger(new MockLogger("Not important", null) {
            @Override
            public Level getLevel() {
                return Level.INFO;
            }
        });

        mockRestlet.doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testdoPostI() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return null;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        final String uid = "uid1212";
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
                JsonObject resource = new GenericJsonObject();
                assertEquals(in, parameters.getMap());
                assertEquals(user, loggedUser);
                resource.put("id", uid);
                return resource;
            }
        };

        mockRestlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("resource"));
        assertTrue(stream.contains("{"));
        assertTrue(stream.contains("id"));
        assertTrue(stream.contains(uid));
        assertTrue(stream.contains("}"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testdoPostII() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "";
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        final String uid = "uid1212";
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
                JsonObject resource = new GenericJsonObject();
                assertEquals(in, parameters.getMap());
                assertEquals(user, loggedUser);
                resource.put("id", uid);
                return resource;
            }
        };

        mockRestlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("resource"));
        assertTrue(stream.contains("{"));
        assertTrue(stream.contains("id"));
        assertTrue(stream.contains(uid));
        assertTrue(stream.contains("}"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testdoPostIII() throws IOException {
        final String uid = "uid1212";
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Unsupported URL format"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testdoPostIV() throws IOException {
        final String uid = "<!uid1212:>";
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return in;
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();
        mockRestlet.setLogger(new MockLogger("Not important", null) {
            @Override
            public Level getLevel() {
                return Level.INFO;
            }
        });

        mockRestlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
    }

    @Test
    public void testDoDeleteI() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Required path info"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoDeleteII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "";
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Required path info"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoDeleteIII() throws IOException {
        final String uid = "uid1212";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected void deleteResource(String id, OpenIdUser loggedUser) throws DataSourceException {
                assertEquals(uid, id);
                assertEquals(user, loggedUser);
            }
        };

        mockRestlet.doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoDeleteIV() throws IOException {
        final String uid = "<!uid1212:>";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Unsupported URL format"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoDeleteV() throws IOException {
        final String uid = "<!uid1212:>";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/" + uid;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MockBaseRestlet mockRestlet = new MockBaseRestlet();
        mockRestlet.setLogger(new MockLogger("Not important", null) {
            @Override
            public Level getLevel() {
                return Level.INFO;
            }
        });

        mockRestlet.doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
    }

    @Test
    public void testGetLoggedUser() throws Exception {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        assertEquals(user, BaseRestlet.getLoggedUser(mockRequest));
    }

    @Test
    public void testInjectMockOpenUserI() {
        final Long consumerKey = 12345L;
        final String openId = "http://unit.test";
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                if ("debugConsumerKey".equals(name)) {
                    return consumerKey.toString();
                }
                if ("debugConsumerOpenId".equals(name)) {
                    return openId;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String name, Object value) {
                assertEquals(OpenIdUser.ATTR_NAME, name);
                OpenIdUser user = (OpenIdUser) value;
                assertEquals(openId, user.getClaimedId());
                assertEquals(consumerKey, user.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID));
            }
        };

        BaseRestlet.injectMockOpenUser(mockRequest);
    }

    @Test
    public void testInjectMockOpenUserII() {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                if ("debugConsumerKey".equals(name)) {
                    return null;
                }
                if ("debugConsumerOpenId".equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String name, Object value) {
                assertEquals(OpenIdUser.ATTR_NAME, name);
                OpenIdUser user = (OpenIdUser) value;
                assertEquals("http://open.id", user.getClaimedId());
                assertEquals(1L, user.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID));
            }
        };

        BaseRestlet.injectMockOpenUser(mockRequest);
    }

    @Test
    public void testGetLoggedUserWithInjection() throws Exception {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                if ("debugConsumerKey".equals(name)) {
                    return null;
                }
                if ("debugConsumerOpenId".equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String name, Object value) {
                assertEquals(OpenIdUser.ATTR_NAME, name);
                OpenIdUser user = (OpenIdUser) value;
                assertEquals("http://open.id", user.getClaimedId());
                assertEquals(1L, user.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID));
            }
        };

        BaseRestlet.getLoggedUser(mockRequest);
    }

    @Test
    public void testDoGetFailingInDebugMode() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        new MockBaseRestlet().doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPostFailingInDebugMode() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        new MockBaseRestlet().doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPutFailingInDebugMode() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        new MockBaseRestlet().doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoDeleteFailingInDebugMode() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        new MockBaseRestlet().doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testIsAPrivilegedUserI() {
        assertFalse(BaseRestlet.isAPrivilegedUser(user));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsAPrivilegedUserII() {
        ((Map<String, String>) user.getAttribute("info")).put("email", "steven.milstein@gmail.com");
        assertTrue(BaseRestlet.isAPrivilegedUser(user));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsAPrivilegedUserIII() {
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        assertTrue(BaseRestlet.isAPrivilegedUser(user));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsAPrivilegedUserIV() {
        ((Map<String, String>) user.getAttribute("info")).put("email", "toto.rigado@lorient.fr");
        assertFalse(BaseRestlet.isAPrivilegedUser(user));
    }

    @Test
    public void testIsAPrivilegedUserV() {
        user.setAttribute("info", null);
        assertFalse(BaseRestlet.isAPrivilegedUser(user));
    }
}
