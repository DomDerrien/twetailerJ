// Adaptation of David Yu's code for dyuproject, made available under
// the Apache licence 2.0. The adaptation mostly introduce more flexibility
// regarding the workflow

package twetailer.j2ee;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.validator.ApplicationSettings;

import com.dyuproject.openid.RelyingParty;

import domderrien.i18n.LocaleController;

/**
 * Home Servlet. If authenticated, goes to the home page. If not, goes to the login page.
 *
 * @author David Yu
 * @maintainer Dom Derrien
 */
@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RelyingParty.getInstance().invalidate(request, response);
        response.sendRedirect(ApplicationSettings.get().getMainPageURL());
    }
}
