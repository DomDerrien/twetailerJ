package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import domderrien.mocks.MockHttpServletRequest;
import domderrien.mocks.MockHttpServletResponse;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;
import domderrien.jsontools.TransferObject;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.dao.BaseOperations;

import com.google.appengine.api.users.User;

public class TestUtils {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        PersistenceManagerFactory pmf = EasyMock.createMock(PersistenceManagerFactory.class);
        BaseOperations.setPersistenceManagerFactory(pmf);
        new ServletUtils();
    }

    @Test
    public void testGetPersistenceManagerFactory() {
        PersistenceManagerFactory pmf = EasyMock.createMock(PersistenceManagerFactory.class);
        BaseOperations.setPersistenceManagerFactory(pmf);
		pmf = BaseOperations.getPersistenceManagerFactory();
		assertNotNull(pmf);
		assertEquals(pmf, BaseOperations.getPersistenceManagerFactory());
	}

	@Test
	public void testGetPersistenceManager() {
        PersistenceManagerFactory pmf = EasyMock.createMock(PersistenceManagerFactory.class);
        EasyMock.expect(pmf.getPersistenceManager()).andReturn(EasyMock.createMock(PersistenceManager.class)).once();
        EasyMock.replay(pmf);
        BaseOperations.setPersistenceManagerFactory(pmf);
        PersistenceManager pm = BaseOperations.getPersistenceManagerFactory().getPersistenceManager();
		assertNotNull(pm);
	}

	@Test
	public void testConfigureHttpParametersI() {
		MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
			@Override
			public void setCharacterEncoding(String encoding) {
				assertEquals("UTF-8", encoding);
			}
		};
		MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
			@Override
			public void setCharacterEncoding(String encoding) {
				assertEquals("UTF-8", encoding);
			}
			@Override
			public void setContentType(String type) {
				assertNotNull(type);
				assertTrue(type.contains("text/javascript"));
			}
		};
		ServletUtils.configureHttpParameters(mockRequest, mockResponse);
	}

	@Test
	public void testConfigureHttpParametersII() {
		MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
			@Override
			public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
				// Should be ignored, silently caught
				throw new UnsupportedEncodingException("done in purpose");
			}
		};
		ServletUtils.configureHttpParameters(mockRequest, new MockHttpServletResponse());
	}

	@Test
	public void testGetUserService() {
		assertNotNull(ServletUtils.getUserService());
	}

	@Test(expected=RuntimeException.class)
	public void testGetLoggedUserI() {
	    ServletUtils.setUserService(new MockUserService());
	    ServletUtils.getLoggedUser();
	}

    @Test
    public void testGetLoggedUserII() {
        final User user = new User("test-email", "test-domain");
        ServletUtils.setUserService(new MockUserService(){
            @Override
            public User getCurrentUser() {
                return user;
            }
        });
        ServletUtils.getLoggedUser();
    }

	@Test
	public void testListToJsonI() {
	    final JsonObject converted = new GenericJsonObject();
	    List<TransferObject> list = new ArrayList<TransferObject>();
	    list.add(new TransferObject() {
            public TransferObject fromJson(JsonObject in) throws ParseException {
                return null;
            }
            public JsonObject toJson() {
                return converted;
            }
        });
	    JsonArray array = JsonUtils.toJson(list);
	    assertEquals(1, array.size());
		assertEquals(converted, array.getJsonObject(0));
    }

    @Test(expected=NullPointerException.class)
    public void testListToJsonII() {
        JsonUtils.toJson((List <?>) null);
	}

	@Test
	public void testMapToJsonI() {
        final JsonObject converted = new GenericJsonObject();
        Map<String, TransferObject> map = new HashMap<String, TransferObject>();
        map.put("test", new TransferObject() {
            public TransferObject fromJson(JsonObject in) throws ParseException {
                return null;
            }
            public JsonObject toJson() {
                return converted;
            }
        });
        JsonObject object = JsonUtils.toJson(map);
        assertEquals(1, object.size());
        assertEquals(converted, object.getJsonObject("test"));
	}

    @Test(expected=NullPointerException.class)
    public void testMapToJsonII() {
        JsonUtils.toJson((Map <String, ?>) null);
    }

}
