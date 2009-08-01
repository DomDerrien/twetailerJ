package com.twetailer.j2ee;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import domderrien.mocks.MockHttpServletRequest;
import domderrien.mocks.MockHttpServletResponse;
import domderrien.mocks.MockLogger;
import domderrien.mocks.MockServletOutputStream;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.appengine.api.users.User;
import com.twetailer.DataSourceException;

public class TestBaseRestlet {

	@SuppressWarnings("serial")
	class MockBaseRestlet extends BaseRestlet {
        @Override
        protected Logger getLogger() {
            return new MockLogger(MockBaseRestlet.class.getName(), null);
        }
		@Override
		protected String createResource(JsonObject parameters, User loggedUser) throws DataSourceException {
			return null;
		}
		@Override
		protected void deleteResource(String resourceId, User loggedUser) throws DataSourceException {
		}
		@Override
		protected JsonObject getResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
			return null;
		}
		@Override
		protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
			return null;
		}
		@Override
		protected void updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		}
	}

	static final User user = new User("test-email", "test-domain");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Utils.setUserService(new MockUserService(){
            @Override
            public User getCurrentUser() {
                return user;
            }
        });
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
        final JsonObject resource = new GenericJsonObject();
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonObject getResource(JsonObject parameters, String id, User loggedUser) throws DataSourceException {
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
        final JsonObject resource = new GenericJsonObject();
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected JsonObject getResource(JsonObject parameters, String id, User loggedUser) throws DataSourceException {
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
        /*
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
        */
    }

    @Test
    @SuppressWarnings("serial")
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
        MockBaseRestlet mockRestlet = new MockBaseRestlet() {
            @Override
            protected Logger getLogger() {
                return new MockLogger(MockBaseRestlet.class.getName(), null) {
                    @Override
                    public Level getLevel() {
                        return Level.WARNING;
                    }
                };
            }
        };
        mockRestlet.doGet(mockRequest, mockResponse);
    }

    @Test
    public void testDoPost() throws IOException {
    }

    @Test
    public void testDoPut() throws IOException {
    }

    @Test
    public void testDoDelete() throws IOException {
    }

    @Test(expected=java.lang.NoClassDefFoundError.class)
    public void testGetPersistenceManager() throws IOException {
        new MockBaseRestlet().getPersistenceManager();
    }
}
