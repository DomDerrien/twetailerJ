package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.validator.ApplicationSettings;

import com.dyuproject.openid.MockOpenIdUserManager;
import com.dyuproject.openid.RelyingParty;

public class TestLogoutServlet {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        new LogoutServlet();
    }

    @Test
    public void testGetRelyingParty() {
        LogoutServlet logoutServlet = new LogoutServlet();
        RelyingParty relyingParty = logoutServlet.getRelyingParty();
        assertNotNull(relyingParty);
        assertEquals(relyingParty, logoutServlet.getRelyingParty());
        assertEquals(relyingParty, new LogoutServlet().getRelyingParty());
    }

    @Test
    @SuppressWarnings("serial")
    public void testInvalidateI() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return null;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) throws IOException {
                assertEquals(ApplicationSettings.get().getMainPageURL(), url);
            }
        };

        final RelyingParty relyingParty = RelyingParty.newInstance(TestLoginServlet.propertiesForInjection);;

        new LogoutServlet() {
            @Override
            protected RelyingParty getRelyingParty() {
                return relyingParty;
            }
        }.doGet(request, response);

        assertTrue(((MockOpenIdUserManager) relyingParty.getOpenIdUserManager()).isInvalidated());
    }

    @Test
    @SuppressWarnings("serial")
    public void testInvalidateII() throws IOException, ServletException {
        final String urlToGo = "http://unit.test/ca";
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getParameter(String name) {
                if (LoginServlet.FROM_PAGE_URL_KEY.equals(name)) {
                    return urlToGo;
                }
                fail("Parameter access not expected for: " + name);
                return null;
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) throws IOException {
                assertEquals(urlToGo, url);
            }
        };

        final RelyingParty relyingParty = RelyingParty.newInstance(TestLoginServlet.propertiesForInjection);;

        new LogoutServlet() {
            @Override
            protected RelyingParty getRelyingParty() {
                return relyingParty;
            }
        }.doGet(request, response);

        assertTrue(((MockOpenIdUserManager) relyingParty.getOpenIdUserManager()).isInvalidated());
    }
}
