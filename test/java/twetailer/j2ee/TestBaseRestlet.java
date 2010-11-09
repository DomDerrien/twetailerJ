package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import twetailer.connector.MailConnector;
import twetailer.task.CommandProcessor;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestBaseRestlet {

    @SuppressWarnings("serial")
    class MockBaseRestlet extends BaseRestlet {
        @Override
        protected Logger getLogger() {
            return new MockLogger(null, null);
        }
        @Override
        protected UserService getUserService() {
            return new UserService() {
                @Override public String createLoginURL(String arg0) { return null; }
                @Override public String createLoginURL(String arg0, String arg1) { return null; }
                @Override public String createLoginURL(String arg0, String arg1, String arg2, Set<String> arg3) { return null; }
                @Override public String createLogoutURL(String arg0) { return null; }
                @Override public String createLogoutURL(String arg0, String arg1) { return null; }
                @Override public User getCurrentUser() { return null; }
                @Override public boolean isUserAdmin() { return false; }
                @Override public boolean isUserLoggedIn() { return false; }
            };
        }
        @Override
        protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
            return null;
        }
        @Override
        protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
        }
        @Override
        protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
            return null;
        }
        @Override
        protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
            return null;
        }
        @Override
        protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
            return null;
        }
    }

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    OpenIdUser user;

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        user = MockLoginServlet.buildMockOpenIdUser();
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
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
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    @Override public boolean hasMoreElements() { return false;}
                    @Override public String nextElement() { return null; }
                };
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
            protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    @Override public boolean hasMoreElements() { return false;}
                    @Override public String nextElement() { return null; }
                };
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
            protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    @Override public boolean hasMoreElements() { return false;}
                    @Override public String nextElement() { return null; }
                };
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
            protected JsonObject getResource(JsonObject parameters, String id, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    @Override public boolean hasMoreElements() { return false;}
                    @Override public String nextElement() { return null; }
                };
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
            protected JsonObject getResource(JsonObject parameters, String id, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
            @Override
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return user;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    @Override public boolean hasMoreElements() { return false;}
                    @Override public String nextElement() { return null; }
                };
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoGetVII() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/";
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
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    @Override public boolean hasMoreElements() { return false;}
                    @Override public String nextElement() { return null; }
                };
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
            protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
            protected JsonObject updateResource(JsonObject parameters, String id, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
    }

    @Test
    public void testdoPutVI() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/";
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
            protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
            protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testdoPostV() throws IOException {
        final Map<String, ?> in = new HashMap<String, Object>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/";
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
            protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
    public void testDoDeleteI() throws IOException {
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
            protected void deleteResource(String id, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();

        mockRestlet.doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
    }

    @Test
    public void testDoDeleteVI() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/";
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
                assertEquals(1L, user.getAttribute(LoginServlet.AUTHENTICATED_CONSUMER_ID));
            }
        };

        BaseRestlet.getLoggedUser(mockRequest);
    }

    @Test
    public void testDoGetFailingInDebugModeI() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return new HashMap<String, Object>();
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
    public void testDoGetFailingInDebugModeII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public Map<String, ?> getParameterMap() {
                throw new DatastoreTimeoutException("Done in purpose");
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
    public void testDoGetFailingInDebugModeIII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public Map<String, ?> getParameterMap() {
                throw new DatastoreTimeoutException("Done in purpose");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MailConnector.foolNextMessagePost(); // Will make CatchAllMailHandlerServlet.composeAndPostMailMessage() throwing a MessagingException!
        new MockBaseRestlet().doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPostFailingInDebugModeI() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                throw new IllegalArgumentException("Done in purpose");
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
        new MockBaseRestlet().doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPostFailingInDebugModeII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                throw new DatastoreTimeoutException("Done in purpose");
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
    public void testDoPostFailingInDebugModeIII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                throw new DatastoreTimeoutException("Done in purpose");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MailConnector.foolNextMessagePost(); // Will make CatchAllMailHandlerServlet.composeAndPostMailMessage() throwing a MessagingException!
        new MockBaseRestlet().doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPutFailingInDebugModeI() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if ("debugMode".equals(name)) {
                    return CommandProcessor.DEBUG_INFO_SWITCH;
                }
                throw new IllegalArgumentException("Done in purpose");
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
        new MockBaseRestlet().doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPutFailingInDebugModeII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                throw new DatastoreTimeoutException("Done in purpose");
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
    public void testDoPutFailingInDebugModeIII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                throw new DatastoreTimeoutException("Done in purpose");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        MailConnector.foolNextMessagePost(); // Will make CatchAllMailHandlerServlet.composeAndPostMailMessage() throwing a MessagingException!
        new MockBaseRestlet().doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoDeleteFailingInDebugModeI() throws IOException {
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
    @SuppressWarnings("serial")
    public void testDoDeleteFailingInDebugModeII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/12345";
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
        new MockBaseRestlet() {
            @Override
            protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
                throw new DatastoreTimeoutException("Done in purpose");
            }
        }.doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoDeleteFailingInDebugModeIII() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/12345";
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
        MailConnector.foolNextMessagePost(); // Will make CatchAllMailHandlerServlet.composeAndPostMailMessage() throwing a MessagingException!
        new MockBaseRestlet() {
            @Override
            protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
                throw new DatastoreTimeoutException("Done in purpose");
            }
        }.doDelete(mockRequest, mockResponse);
        assertTrue(stream.contains("'isException':true"));
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testIsAPrivilegedUserI() {
        assertFalse(new MockBaseRestlet().isUserAdministrator(user));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsAPrivilegedUserII() {
        ((Map<String, String>) user.getAttribute("info")).put("email", "steven.milstein@gmail.com");
        assertFalse(new MockBaseRestlet().isUserAdministrator(user));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsAPrivilegedUserIII() {
        ((Map<String, String>) user.getAttribute("info")).put("email", "dominique.derrien@gmail.com");
        assertTrue(new MockBaseRestlet().isUserAdministrator(user));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsAPrivilegedUserIV() {
        ((Map<String, String>) user.getAttribute("info")).put("email", "toto.rigado@lorient.fr");
        assertFalse(new MockBaseRestlet().isUserAdministrator(user));
    }

    @Test
    public void testIsAPrivilegedUserV() {
        user.setAttribute("info", null);
        assertFalse(new MockBaseRestlet().isUserAdministrator(user));
    }

    @Test
    public void testDoGetUnauthorized() throws IOException {
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
            public Object getAttribute(String key) {
                if (OpenIdUser.ATTR_NAME.equals(key)) {
                    return null;
                }
                fail("No attribute gathering expected for: " + key);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    @Override public boolean hasMoreElements() { return false;}
                    @Override public String nextElement() { return null; }
                };
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

        assertEquals(401, mockResponse.getStatus());
        assertTrue(stream.contains("reason"));
        assertTrue(stream.contains("Unauthorized"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoPostUnauthorized() throws IOException {
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
                    return null;
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet();
        mockRestlet.doPost(mockRequest, mockResponse);

        assertEquals(401, mockResponse.getStatus());
        assertTrue(stream.contains("reason"));
        assertTrue(stream.contains("Unauthorized"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }
}
