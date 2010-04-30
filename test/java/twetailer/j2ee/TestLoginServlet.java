package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twetailer.validator.ApplicationSettings;

import com.dyuproject.openid.Constants;
import com.dyuproject.openid.MockAssociation;
import com.dyuproject.openid.MockOpenIdUserManager;
import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.RelyingParty;
import com.dyuproject.openid.YadisDiscovery;
import com.dyuproject.openid.ext.AxSchemaExtension;
import com.dyuproject.openid.ext.SRegExtension;
import com.dyuproject.util.http.UrlEncodedParameterMap;

@SuppressWarnings("serial")
public class TestLoginServlet {

    class MockRequestDispatcher implements RequestDispatcher {
        public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException { }
        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException { }

    };

    public static Properties propertiesForInjection = new Properties();
    static {
        propertiesForInjection.setProperty("openid.discovery", "com.dyuproject.openid.MockDiscovery");
        propertiesForInjection.setProperty("openid.association", "com.dyuproject.openid.MockAssociation");
        propertiesForInjection.setProperty("openid.httpconnector", "com.dyuproject.util.http.MockHttpConnector");
        propertiesForInjection.setProperty("openid.user.manager", "com.dyuproject.openid.MockOpenIdUserManager");
        propertiesForInjection.setProperty("openid.user.cache", "com.dyuproject.openid.MockUserCache");
        // propertiesForInjection.setProperty("openid.automatic_redirect", "true"); // Same as default
        propertiesForInjection.setProperty("openid.authredirection", "com.dyuproject.openid.MockAuthRedirection");
        // propertiesForInjection.setProperty("openid.identifier.parameter", RelyingParty.DEFAULT_IDENTIFIER_PARAMETER); // Same as default
        // propertiesForInjection.setProperty("openid.relyingparty.listeners", null); // Same as default
        // propertiesForInjection.setProperty("openid.identifier.resolvers", null); // Same as default
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        LoginServlet.consumerOperations = new ConsumerOperations();
    }

    @Test
    public void testConstructor() {
        new LoginServlet();
    }

    @Test
    public void testGetRelyingParty() {
        LoginServlet loginServlet = new LoginServlet();
        RelyingParty relyingParty = loginServlet.getRelyingParty();
        assertNotNull(relyingParty);
        assertEquals(relyingParty, loginServlet.getRelyingParty());
        assertEquals(relyingParty, new LoginServlet().getRelyingParty());
    }

    @Test
    public void testPreselectOpendIdServerI() throws IOException, ServletException {
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.LOGIN_WITH_PARAMETER_KEY.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String key, Object value) {
                fail("No attribute should be set now");
            }
        };

        new LoginServlet().preselectOpendIdServer(request);
    }

    @Test
    public void testPreselectOpendIdServerII() throws IOException, ServletException {
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.LOGIN_WITH_PARAMETER_KEY.equals(name)) {
                    return "unknown";
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String key, Object value) {
                fail("No attribute should be set now");
            }
        };

        new LoginServlet().preselectOpendIdServer(request);
    }

    @Test
    public void testPreselectOpendIdServerIII() throws IOException, ServletException {
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.LOGIN_WITH_PARAMETER_KEY.equals(name)) {
                    return "google";
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String key, Object value) {
                assertTrue(value instanceof OpenIdUser);
                assertEquals(LoginServlet.GOOGLE_OPENID_SERVER_URL, ((OpenIdUser) value).getOpenIdServer());
            }
        };

        new LoginServlet().preselectOpendIdServer(request);
    }

    @Test
    public void testPreselectOpendIdServerIV() throws IOException, ServletException {
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.LOGIN_WITH_PARAMETER_KEY.equals(name)) {
                    return "yahoo";
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String key, Object value) {
                assertTrue(value instanceof OpenIdUser);
                assertEquals(LoginServlet.YAHOO_OPENID_SERVER_URL, ((OpenIdUser) value).getOpenIdServer());
            }
        };

        new LoginServlet().preselectOpendIdServer(request);
    }

    @Test
    public void testPreselectOpendIdServerV() throws IOException, ServletException {
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.LOGIN_WITH_PARAMETER_KEY.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return "unknown";
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String key, Object value) {
                fail("No attribute should be set now");
            }
        };

        new LoginServlet().preselectOpendIdServer(request);
    }

    @Test
    public void testPreselectOpendIdServerVI() throws IOException, ServletException {
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.LOGIN_WITH_PARAMETER_KEY.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return "test@gmail.com";
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String key, Object value) {
                assertTrue(value instanceof OpenIdUser);
                assertEquals(LoginServlet.GOOGLE_OPENID_SERVER_URL, ((OpenIdUser) value).getOpenIdServer());
            }
        };

        new LoginServlet().preselectOpendIdServer(request);
    }

    @Test
    public void testPreselectOpendIdServerVII() throws IOException, ServletException {
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.LOGIN_WITH_PARAMETER_KEY.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return "test@yahoo.com";
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public void setAttribute(String key, Object value) {
                assertTrue(value instanceof OpenIdUser);
                assertEquals(LoginServlet.YAHOO_OPENID_SERVER_URL, ((OpenIdUser) value).getOpenIdServer());
            }
        };

        new LoginServlet().preselectOpendIdServer(request);
    }

    @Test
    public void testLoginI() throws IOException, ServletException {
        //
        // Fresh call, without any authentication information
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                return RelyingParty.newInstance(propertiesForInjection);
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginII() throws IOException, ServletException {
        //
        // Response coming from authentication but without the expected user information
        // => Redirection to where we come from
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return "anyID";
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                return RelyingParty.newInstance(propertiesForInjection);
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginIII() throws IOException, ServletException {
        //
        // Response coming from authentication but without the expected user information
        // => Redirection to where we come from
        //
        final String requestUrl = "http://unit/test/";
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return Constants.Mode.ID_RES;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public String getRequestURI() {
                return requestUrl;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) throws IOException {
                assertEquals(requestUrl, url);
            }
        };

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                return RelyingParty.newInstance(propertiesForInjection);
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginVIa() throws IOException, ServletException {
        //
        // An OpendIdUser instance is stored in the request
        // The user is authenticated
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("b", "identity"); // Means user.isAuthenticated() == true
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url); // Because of authentication verified
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        final Long consumerKey = 12345L;
        LoginServlet.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(OpenIdUser user) {
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                return RelyingParty.newInstance(propertiesForInjection);
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginVIb() throws IOException, ServletException {
        //
        // An OpendIdUser instance is stored in the request
        // The user is authenticated
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("b", "identity"); // Means user.isAuthenticated() == true
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url); // Authenticated, but not yet in our data store
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        final Long consumerKey = 12345L;
        LoginServlet.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(OpenIdUser user) {
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                return RelyingParty.newInstance(propertiesForInjection);
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginVII() throws IOException, ServletException {
        //
        // An OpendIdUser instance is stored in the request
        // The user is NOT authenticated
        // The user is associated and the response is authenticated
        // At the end, the verification FAILS
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return Constants.Mode.ID_RES;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    public boolean hasMoreElements() { return false; }
                    public String nextElement() { return null; }
                };
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("c", "assocHandle");
                    json.put("d", new HashMap<String, Object>());
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                return RelyingParty.newInstance(propertiesForInjection);
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginVIIIa() throws IOException, ServletException {
        //
        // An OpendIdUser instance is stored in the request
        // The user is NOT authenticated
        // The user is associated and the response is authenticated
        // At the end, the verification SUCCEED
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return Constants.Mode.ID_RES;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    public boolean hasMoreElements() { return false; }
                    public String nextElement() { return null; }
                };
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("c", "assocHandle");
                    json.put("d", new HashMap<String, Object>());
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url);
            }
        };

        final Long consumerKey = 12345L;
        LoginServlet.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(OpenIdUser user) {
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                RelyingParty instance = RelyingParty.newInstance(propertiesForInjection);
                ((MockAssociation) instance.getOpenIdContext().getAssociation()).makeItSuccessful();
                return instance;
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginVIIIb() throws IOException, ServletException {
        //
        // An OpendIdUser instance is stored in the request
        // The user is NOT authenticated
        // The user is associated and the response is authenticated
        // At the end, the verification SUCCEED
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return Constants.Mode.ID_RES;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    public boolean hasMoreElements() { return false; }
                    public String nextElement() { return null; }
                };
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("c", "assocHandle");
                    json.put("d", new HashMap<String, Object>());
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url);
            }
        };

        final Long consumerKey = 12345L;
        LoginServlet.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(OpenIdUser user) {
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                RelyingParty instance = RelyingParty.newInstance(propertiesForInjection);
                ((MockAssociation) instance.getOpenIdContext().getAssociation()).makeItSuccessful();
                return instance;
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginX() throws IOException, ServletException {
        //
        // An OpendIdUser instance is stored in the request
        // The user is NOT authenticated, just associated
        // Association and authentication required and successful
        //
        final String requestUrl = "http://unit/test/";
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    public boolean hasMoreElements() { return false; }
                    public String nextElement() { return null; }
                };
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("c", "assocHandle");
                    json.put("d", new HashMap<String, Object>());
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer(requestUrl);
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                RelyingParty instance = RelyingParty.newInstance(propertiesForInjection);
                ((MockAssociation) instance.getOpenIdContext().getAssociation()).makeItSuccessful();
                return instance;
            }
        }.doGet(request, response);
    }

    @Test
    @Ignore
    public void testLoginXI() throws IOException, ServletException {
        //
        // An OpendIdUser instance is stored in the request
        // The user is NOT authenticated NOR associated, just from a response of an authenticated server
        // Association and authentication required and successful
        //
        final String requestUrl = "http://unit/test/";
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return Constants.Mode.ID_RES;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    public boolean hasMoreElements() { return false; }
                    public String nextElement() { return null; }
                };
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer(requestUrl);
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                RelyingParty instance = RelyingParty.newInstance(propertiesForInjection);
                ((MockAssociation) instance.getOpenIdContext().getAssociation()).makeItSuccessful();
                return instance;
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginXII() throws IOException, ServletException {
        //
        // An OpendIdUser instance is stored in the request
        // The user is NOT authenticated, just associated
        // Association and authentication required
        //
        final String requestUrl = "http://unit/test/";
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                return new Enumeration<String>() {
                    public boolean hasMoreElements() { return false; }
                    public String nextElement() { return null; }
                };
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("c", "assocHandle");
                    json.put("d", new HashMap<String, Object>());
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer(requestUrl);
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getLoginPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                return RelyingParty.newInstance(propertiesForInjection);
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginXV() throws IOException, ServletException {
        //
        // Fresh call and failure during discovering information
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getLoginPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                RelyingParty instance = RelyingParty.newInstance(propertiesForInjection);
                ((MockOpenIdUserManager) instance.getOpenIdUserManager()).makeItUnknownHostException();
                return instance;
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginXVI() throws IOException, ServletException {
        //
        // Fresh call and failure during discovering information
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getLoginPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                RelyingParty instance = RelyingParty.newInstance(propertiesForInjection);
                ((MockOpenIdUserManager) instance.getOpenIdUserManager()).makeItFileNotFoundException();
                return instance;
            }
        }.doGet(request, response);
    }

    @Test
    public void testLoginXVII() throws IOException, ServletException {
        //
        // Fresh call and failure during discovering information
        //
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (Constants.OPENID_MODE.equals(name)) {
                    return null;
                }
                if (RelyingParty.DEFAULT_IDENTIFIER_PARAMETER.equals(name)) {
                    return null;
                }
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
            @Override
            public RequestDispatcher getRequestDispatcher(String url) {
                assertEquals(ApplicationSettings.get().getLoginPageURL(), url);
                return new MockRequestDispatcher();
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LoginServlet() {
            @Override
            protected void preselectOpendIdServer(HttpServletRequest request) { }
            @Override
            protected RelyingParty getRelyingParty() {
                RelyingParty instance = RelyingParty.newInstance(propertiesForInjection);
                ((MockOpenIdUserManager) instance.getOpenIdUserManager()).makeItIllegalArgumentException();
                return instance;
            }
        }.doGet(request, response);
    }

    @Test
    public void testListenerOnDiscovery() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );

        LoginServlet.relyingPartyListener.onDiscovery(user, new MockHttpServletRequest());
    }

    @Test
    public void testListenerOnPreAuthenticate() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );

        LoginServlet.relyingPartyListener.onPreAuthenticate(user, new MockHttpServletRequest(), new UrlEncodedParameterMap());
    }

    @Test
    public void testListenerOnAuthenticateI() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("b", "unit@test");
        user.fromJSON(json);

        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public void setAttribute(String key, Object value) {
                fail("No attribute expected");
            }
        };

        LoginServlet.relyingPartyListener.onAuthenticate(user, request);
    }

    @Test
    public void testListenerOnAuthenticateII() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("b", "unit@test");
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(SRegExtension.ATTR_NAME, new HashMap<String, String>());
        json.put("g", attributes);
        user.fromJSON(json);

        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public void setAttribute(String key, Object value) {
                fail("No attribute expected");
            }
        };

        LoginServlet.relyingPartyListener.onAuthenticate(user, request);
    }

    @Test
    public void testListenerOnAuthenticateIII() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("b", "unit@test");
        final Map<String,String> sregExtension = new HashMap<String, String>();;
        sregExtension.put("unit", "test");
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(SRegExtension.ATTR_NAME, sregExtension);
        json.put("g", attributes);
        user.fromJSON(json);

        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public void setAttribute(String key, Object value) {
                assertEquals("info", key);
                assertEquals(sregExtension, value);
            }
        };

        LoginServlet.relyingPartyListener.onAuthenticate(user, request);
    }

    @Test
    public void testListenerOnAuthenticateIV() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("b", "unit@test");
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(AxSchemaExtension.ATTR_NAME, new HashMap<String, String>());
        json.put("g", attributes);
        user.fromJSON(json);

        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public void setAttribute(String key, Object value) {
                fail("No attribute expected");
            }
        };

        LoginServlet.relyingPartyListener.onAuthenticate(user, request);
    }

    @Test
    public void testListenerOnAuthenticateV() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("b", "unit@test");
        final Map<String,String> axSchemaExtension = new HashMap<String, String>();;
        axSchemaExtension.put("unit", "test");
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(AxSchemaExtension.ATTR_NAME, axSchemaExtension);
        json.put("g", attributes);
        user.fromJSON(json);

        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public void setAttribute(String key, Object value) {
                assertEquals("info", key);
                assertEquals(axSchemaExtension, value);
            }
        };

        LoginServlet.relyingPartyListener.onAuthenticate(user, request);
    }

    @Test
    public void testListenerOnAccess() {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("b", "unit@test");
        user.fromJSON(json);

        LoginServlet.relyingPartyListener.onAccess(user, new MockHttpServletRequest());
    }

    @Test
    public void testAttachConsumerToSessionI() throws ServletException, IOException {
        final Long consumerKey = 12345L;
        LoginServlet.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer createConsumer(OpenIdUser user) {
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );

        // Authenticated user already known
        assertNull(user.getAttribute(LoginServlet.AUTHENTICATED_CONSUMER_TWETAILER_ID));
        LoginServlet.attachConsumerToSession(user);
        assertEquals(consumerKey, user.getAttribute(LoginServlet.AUTHENTICATED_CONSUMER_TWETAILER_ID));
    }
}
