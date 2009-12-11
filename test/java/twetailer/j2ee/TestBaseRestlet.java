package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestBaseRestlet {

    static final OpenIdUser user = OpenIdUser.populate(
            "http://www.yahoo.com",
            YadisDiscovery.IDENTIFIER_SELECT,
            LoginServlet.YAHOO_OPENID_SERVER_URL
    );

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
        protected OpenIdUser getLoggedUser(HttpServletRequest request) {
            return user;
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
        protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
            return null;
        }
        @Override
        protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
            return null;
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
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
            protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
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
            protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
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
    public void testDoPostI() throws IOException {
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

        mockRestlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Required path info"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoPostII() throws IOException {
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

        mockRestlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Required path info"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostIII() throws IOException {
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
    public void testDoPostIV() throws IOException {
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

        mockRestlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Unsupported URL format"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoPostV() throws IOException {
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
    @SuppressWarnings("serial")
    public void testDoPutI() throws IOException {
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
    @SuppressWarnings("serial")
    public void testDoPutII() throws IOException {
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
    public void testDoPutIII() throws IOException {
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

        mockRestlet.doPut(mockRequest, mockResponse);
        assertTrue(stream.contains("isException"));
        assertTrue(stream.contains("true"));
        assertTrue(stream.contains("exceptionMessage"));
        assertTrue(stream.contains("Unsupported URL format"));
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoPutIV() throws IOException {
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
}
