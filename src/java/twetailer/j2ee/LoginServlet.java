// Adaptation of David Yu's code for dyuproject, made available under
// the Apache licence 2.0. The adaptation mostly introduce more flexibility
// regarding the workflow and to make it unit-test-able

package twetailer.j2ee;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twetailer.validator.ApplicationSettings;

import com.dyuproject.openid.OpenIdServletFilter;
import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.RelyingParty;
import com.dyuproject.openid.YadisDiscovery;
import com.dyuproject.openid.RelyingParty.Listener;
import com.dyuproject.openid.ext.AxSchemaExtension;
import com.dyuproject.openid.ext.SRegExtension;
import com.dyuproject.util.http.UrlEncodedParameterMap;

/**
 * Home Servlet. If authenticated, goes to the home page. If not, goes to the login page.
 *
 * @author David Yu
 * @maintainer Dom Derrien
 */
@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

    protected static Listener sregExtension = new SRegExtension().addExchange("email").addExchange("country").addExchange("language").addExchange("nickname"); // .addExchange("firstname").addExchange("lastname");

    protected static Listener axSchemaExtension = new AxSchemaExtension().addExchange("email").addExchange("country").addExchange("language").addExchange("firstname").addExchange("lastname").addExchange("nickname");

    protected static Listener relyingPartyListener = new RelyingParty.Listener() {
        public void onDiscovery(OpenIdUser user, HttpServletRequest request) {
        }
        public void onPreAuthenticate(OpenIdUser user, HttpServletRequest request, UrlEncodedParameterMap params) {
        }
        public void onAuthenticate(OpenIdUser user, HttpServletRequest request) {
            System.err.println("******** newly authenticated user: " + user.getIdentity());
            Map<String, String> sreg = SRegExtension.remove(user);
            Map<String, String> axschema = AxSchemaExtension.remove(user);
            if (sreg != null && !sreg.isEmpty()) {
                user.setAttribute("info", sreg);
            }
            else if (axschema != null && !axschema.isEmpty()) {
                user.setAttribute("info", axschema);
            }
        }
        public void onAccess(OpenIdUser user, HttpServletRequest request) {
            System.err.println("******** user access: " + user.getIdentity());
            System.err.println("******** info: " + user.getAttribute("info"));
        }
    };

    protected static RelyingParty _relyingParty;
    static {
        _relyingParty = RelyingParty.getInstance().
            addListener(sregExtension).
            addListener(axSchemaExtension).
            addListener(relyingPartyListener);
    }

    // To allow injection of a mock instance
    protected RelyingParty getRelyingParty() {
        return _relyingParty;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }

    public final static String LOGIN_WITH_PARAMETER_KEY = "loginWith";
    public final static String LOGIN_WITH_GOOGLE = "google";
    public final static String LOGIN_WITH_YAHOO = "yahoo";

    public final static String GOOGLE_OPENID_SERVER_URL = "https://www.google.com/accounts/o8/ud";
    public final static String YAHOO_OPENID_SERVER_URL = "https://open.login.yahooapis.com/openid/op/auth";

    protected void preselectOpendIdServer(HttpServletRequest request) {
        String identifier = null;
        String openIdServer = null;

        // If the ui supplies a LoginWith=google or LoginWith=yahoo link/button,
        // this will speed up the openid process by skipping discovery.
        // The override is done by adding the OpenIdUser to the request attribute.

        String loginWith = request.getParameter(LOGIN_WITH_PARAMETER_KEY);
        String openidIdentifier = request.getParameter(RelyingParty.DEFAULT_IDENTIFIER_PARAMETER);
        if (loginWith != null) {
            if (loginWith.equals(LOGIN_WITH_GOOGLE)) {
                identifier = "https://www.google.com/accounts/o8/id";
                openIdServer = GOOGLE_OPENID_SERVER_URL;
            }
            else if (loginWith.equals(LOGIN_WITH_YAHOO)) {
                identifier = "http://yahoo.com/";
                openIdServer = YAHOO_OPENID_SERVER_URL;
            }
        }
        else if (openidIdentifier != null) {
            if (openidIdentifier.contains("@gmail")) {
                identifier = "https://www.google.com/accounts/o8/id";
                openIdServer = GOOGLE_OPENID_SERVER_URL;
            }
            if (openidIdentifier.contains("@yahoo")) {
                identifier = "http://yahoo.com/";
                openIdServer = YAHOO_OPENID_SERVER_URL;
            }
        }
        if (identifier != null) { // && openIdServer != null) {
            request.setAttribute(
                    OpenIdUser.ATTR_NAME,
                    OpenIdUser.populate(
                            identifier,
                            YadisDiscovery.IDENTIFIER_SELECT, // claimedId,
                            openIdServer
                    )
            );
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        preselectOpendIdServer(request);

        //
        // FIXME:
        //   1. Get the URL for the initial request
        //   2. If this URL is ApplicationSettings.get().getLoginPageURL(), change it to ApplicationSettings.get().getMainPageURL()
        //   3. Save the URL in the session
        //   4. When the servlet is invoked again with the user being authenticated. jump to the URL saved in the session
        //   5. Note that URL parameters should be saved and restored later
        //

        RelyingParty relyingParty = getRelyingParty();
        String errorMsg = OpenIdServletFilter.DEFAULT_ERROR_MSG;
        try {
            String pageToGo = request.getParameter("fromPageURL");
            pageToGo = pageToGo == null ? ApplicationSettings.get().getMainPageURL() : pageToGo;

            OpenIdUser user = relyingParty.discover(request);
            if (user == null) {
                if (RelyingParty.isAuthResponse(request)) {
                    // authentication timeout
                    response.sendRedirect(request.getRequestURI()); // TODO: verify if parameters should be added: "?" + request.getQueryString()
                }
                else {
                    // set error msg if the openid_identifier is not resolved.
                    if (request.getParameter(relyingParty.getIdentifierParameter()) != null) {
                        request.setAttribute(OpenIdServletFilter.ERROR_MSG_ATTR, errorMsg);
                    }
                    // new user
                    request.getRequestDispatcher(pageToGo).forward(request, response);
                }
                return;
            }

            if (user.isAuthenticated()) {
                // user already authenticated
                attachConsumerToSession(user);
                request.getRequestDispatcher(pageToGo).forward(request, response);
                return;
            }

            // if (user.isAssociated() && RelyingParty.isAuthResponse(request)) { // Temporary shortcut, waiting for David Yu's response
            if (RelyingParty.isAuthResponse(request)) {
                // verify authentication
                if (relyingParty.verifyAuth(user, request, response)) {
                    // authenticated redirect to home to remove the query params instead of doing:
                    // request.setAttribute("user", user); request.getRequestDispatcher("/home.jsp").forward(request, response);
                    attachConsumerToSession(user);
                    response.sendRedirect(pageToGo);
                }
                else {
                    // failed verification
                    request.getRequestDispatcher(pageToGo).forward(request, response);
                }
                return;
            }

            // associate and authenticate user
            StringBuffer url = request.getRequestURL();
            String trustRoot = url.substring(0, url.indexOf("/", 9));
            String realm = url.substring(0, url.lastIndexOf("/"));
            String returnTo = url.toString();
            returnTo += "?fromPageURL=" + pageToGo;
            if (relyingParty.associateAndAuthenticate(user, request, response, trustRoot, realm, returnTo)) {
                // successful association
                return;
            }
        }
        catch (UnknownHostException uhe) {
            errorMsg = OpenIdServletFilter.ID_NOT_FOUND_MSG;
        }
        catch (FileNotFoundException fnfe) {
            errorMsg = OpenIdServletFilter.DEFAULT_ERROR_MSG;
        }
        catch (Exception e) {
            // e.printStackTrace();
            errorMsg = OpenIdServletFilter.DEFAULT_ERROR_MSG;
        }
        request.setAttribute(OpenIdServletFilter.ERROR_MSG_ATTR, errorMsg);
        request.getRequestDispatcher(ApplicationSettings.get().getLoginPageURL()).forward(request, response);
    }

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();

    public static final String AUTHENTICATED_USER_TWETAILER_ID = "authUser_twetailerId";

    protected static void attachConsumerToSession(OpenIdUser user) {
        Long consumerKey = (Long) user.getAttribute(AUTHENTICATED_USER_TWETAILER_ID);
        if (consumerKey == null) {
            // Create only if does not yet exist, otherwise return the existing instance
            Consumer consumer = consumerOperations.createConsumer((OpenIdUser) user);
            // Attached the consumer identifier to the OpenID user record
            user.setAttribute(AUTHENTICATED_USER_TWETAILER_ID, consumer.getKey());
        }
    }
}
