package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void testInvalidate() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public void sendRedirect(String url) throws IOException {
                assertEquals(ApplicationSettings.DEFAULT_MAIN_PAGE_URL, url);
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
