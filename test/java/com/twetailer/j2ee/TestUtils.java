package com.twetailer.j2ee;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.domderrien.MockHttpServletRequest;
import org.domderrien.MockHttpServletResponse;
import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.TransferObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        new Utils();
    }

    @Test
    public void testGetPersistenceManagerFactory() {
		PersistenceManagerFactory pmf = Utils.getPersistenceManagerFactory();
		assertNotNull(pmf);
		assertEquals(pmf, Utils.getPersistenceManagerFactory());
	}

	@Test
	public void testGetPersistenceManager() {
		PersistenceManager pm = Utils.getPersistenceManager();
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
		Utils.configureHttpParameters(mockRequest, mockResponse);
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
		Utils.configureHttpParameters(mockRequest, new MockHttpServletResponse());
	}

	@Test
	public void testGetUserService() {
		assertNotNull(Utils.getUserService());
	}

	@Test(expected=RuntimeException.class)
	public void testGetLoggedUserI() {
		Utils.setUserService(new MockUserService());
		Utils.getLoggedUser();
	}

	@Test
	public void testGetLoggedUserII() {
		final User user = new User("test-email", "test-domain");
		Utils.setUserService(new MockUserService(){
			@Override
			public User getCurrentUser() {
				return user;
			}
		});
		assertEquals(user, Utils.getLoggedUser());
	}

	@Test
	public void testListToJsonI() {
	    final JsonObject converted = new GenericJsonObject();
	    List<TransferObject> list = new ArrayList<TransferObject>();
	    list.add(new TransferObject() {
            public void fromJson(JsonObject in) throws ParseException {
            }
            public JsonObject toJson() {
                return converted;
            }
        });
	    JsonArray array = Utils.toJson(list);
	    assertEquals(1, array.size());
		assertEquals(converted, array.getJsonObject(0));
    }

    @Test(expected=NullPointerException.class)
    public void testListToJsonII() {
        Utils.toJson((List <?>) null);
	}

	@Test
	public void testMapToJsonI() {
        final JsonObject converted = new GenericJsonObject();
        Map<String, TransferObject> map = new HashMap<String, TransferObject>();
        map.put("test", new TransferObject() {
            public void fromJson(JsonObject in) throws ParseException {
            }
            public JsonObject toJson() {
                return converted;
            }
        });
        JsonObject object = Utils.toJson(map);
        assertEquals(1, object.size());
        assertEquals(converted, object.getJsonObject("test"));
	}

    @Test(expected=NullPointerException.class)
    public void testMapToJsonII() {
        Utils.toJson((Map <String, ?>) null);
    }

}
