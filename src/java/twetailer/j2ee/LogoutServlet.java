// Adaptation of David Yu's code for dyuproject, made available under
// the Apache licence 2.0. The adaptation mostly introduce more flexibility
// regarding the workflow and to make it unit-test-able

package twetailer.j2ee;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.validator.ApplicationSettings;

import com.dyuproject.openid.RelyingParty;

/**
 * Logout Servlet invalidating the session and redirecting to the main application page.
 * Because the user is no more authenticated, the filter will display the login page instead of the main application page.
 *
 * @author David Yu
 * @maintainer Dom Derrien
 */
@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {

    protected static RelyingParty _relyingParty = RelyingParty.getInstance();

    // To allow injection of a mock instance
    protected RelyingParty getRelyingParty() {
        return _relyingParty;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RelyingParty relyingParty = getRelyingParty();
        relyingParty.invalidate(request, response);

        String pageToGo = request.getParameter("fromPageURL");
        pageToGo = pageToGo == null ? ApplicationSettings.get().getMainPageURL() : pageToGo;
        response.sendRedirect(pageToGo);
    }
}
