// Adaptation of David Yu's code for dyuproject, made available under
// the Apache licence 2.0. The adaptation mostly introduce more flexibility
// regarding the workflow and to make it unit-test-able

package twetailer.j2ee;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Consumer;
import twetailer.dto.SaleAssociate;
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

    public static final String FROM_PAGE_URL_KEY = "fromPageURL";

    protected static Listener sregExtension = new SRegExtension().addExchange("email").addExchange("country").addExchange("language").addExchange("nickname"); // .addExchange("firstname").addExchange("lastname");

    protected static Listener axSchemaExtension = new AxSchemaExtension().addExchange("email").addExchange("country").addExchange("language").addExchange("firstname").addExchange("lastname").addExchange("nickname");

    protected static Listener relyingPartyListener = new RelyingParty.Listener() {
        public void onDiscovery(OpenIdUser user, HttpServletRequest request) {
        }
        public void onPreAuthenticate(OpenIdUser user, HttpServletRequest request, UrlEncodedParameterMap params) {
        }
        public void onAuthenticate(OpenIdUser user, HttpServletRequest request) {
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

        RelyingParty relyingParty = getRelyingParty();
        String errorMsg = OpenIdServletFilter.DEFAULT_ERROR_MSG;
        try {
            String pageToGo = request.getParameter(FROM_PAGE_URL_KEY);
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
            returnTo += "?" + FROM_PAGE_URL_KEY + "=" + pageToGo;
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
    public static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();

    protected static final String AUTHENTICATED_CONSUMER_TWETAILER_ID = "authConsumer_tId";

    /**
     * Save the Consumer key as attribute of the OpenID user record
     *
     * @param user Descriptor of the logged in OpenID user
     */
    protected static void attachConsumerToSession(OpenIdUser user) {
        // Create only if does not yet exist, otherwise return the existing instance
        Consumer consumer = consumerOperations.createConsumer((OpenIdUser) user);
        // Attached the consumer identifier to the OpenID user record
        user.setAttribute(AUTHENTICATED_CONSUMER_TWETAILER_ID, consumer.getKey());
    }

    /**
     * Return the Consumer key corresponding to the OpenID user
     *
     * @param user OpenID user with the attached Consumer key
     * @return The key of the Consumer instance attached to the OpenID user
     */
    public static Long getConsumerKey(OpenIdUser user) {
        return (Long) user.getAttribute(AUTHENTICATED_CONSUMER_TWETAILER_ID);
    }

    /**
     * Create a Consumer instance for the OpenID user
     *
     * @param user OpenID user to create a Consumer instance for
     * @return Corresponding Consumer instance
     *
     * @throws DataSourceException If the data retrieval fails
     *
     * @see LoginServlet#getConsumer(PersistenceManager, OpenIdUser)
     */
    public static Consumer getConsumer(OpenIdUser user) throws DataSourceException {
        PersistenceManager pm = BaseOperations.getPersistenceManagerHelper();
        try {
            return getConsumer(user, pm);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create a Consumer instance for the OpenID user
     *
     * @param user OpenID user to create a Consumer instance for
     * @param pm PersistenceManager instance used if the OpenID user has not been yet attached;
     *           Should stay open for future usage of the connection
     * @return Corresponding Consumer instance
     *
     * @throws DataSourceException If the data retrieval fails
     */
    public static Consumer getConsumer(OpenIdUser user, PersistenceManager pm) throws DataSourceException {
        return consumerOperations.getConsumer(pm, getConsumerKey(user));
    }

    protected static final String AUTHENTICATED_SALE_ASSOCIATE_ID = "authSA_tId";
    protected static final String NO_SALE_ASSOCIATE_ATTACHED = "authSA_none";

    /**
     * Get the key of the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @return Corresponding SaleAssociate key
     *
     * @throws DataSourceException If the data retrieval fails
     *
     * @see LoginServlet#getSaleAssociateKey(PersistenceManager, OpenIdUser)
     */
    public static Long getSaleAssociateKey(OpenIdUser user) throws DataSourceException {
        // Delay the PersistenceManager instance creation because it's possible it's not required, because the SaleAssociate key has already been retreived for this session!
        return getSaleAssociateKey(user, null);
    }

    /**
     * Get the key of the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @param pm PersistenceManager instance used if the OpenID user has not been yet attached;
     *           Should stay open for future usage of the connection
     * @return Corresponding SaleAssociate key
     *
     * @throws DataSourceException If the data retrieval fails
     */
    public static Long getSaleAssociateKey(OpenIdUser user, PersistenceManager pm) throws DataSourceException {
        boolean noSaleAssociateAttached = user.getAttribute(NO_SALE_ASSOCIATE_ATTACHED) != null; // "attribute != null" means: attribute already set with Boolean.TRUE
        Long saleAssociateKey = (Long) user.getAttribute(AUTHENTICATED_SALE_ASSOCIATE_ID);
        if (!noSaleAssociateAttached && saleAssociateKey == null) {
            // TODO: Use MemCache to limit the number of {consumerKey, saleAssociateKey} association lookup
            PersistenceManager localPM = pm == null ? BaseOperations.getPersistenceManagerHelper() : null;
            try {
                Long consumerKey = getConsumerKey(user);
                List<Long> saleAssociateKeys = saleAssociateOperations.getSaleAssociateKeys(pm == null ? localPM : pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
                if (0 < saleAssociateKeys.size()) {
                    user.setAttribute(AUTHENTICATED_SALE_ASSOCIATE_ID, saleAssociateKeys.get(0));
                    saleAssociateKey = saleAssociateKeys.get(0);
                }
                else {
                    user.setAttribute(NO_SALE_ASSOCIATE_ATTACHED, Boolean.TRUE);
                    saleAssociateKey = null;
                }
            }
            finally {
                if (pm == null) {
                    localPM.close();
                }
            }
        }
        return saleAssociateKey;
    }

    /**
     * Get the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @return Corresponding SaleAssociate instance or <code>null</code> if none is attached to the OpenID user
     *
     * @throws DataSourceException If the data retrieval fails
     *
     * @see LoginServlet#getSaleAssociate(PersistenceManager, OpenIdUser)
     */
    public static SaleAssociate getSaleAssociate(OpenIdUser user) throws DataSourceException {
        PersistenceManager pm = BaseOperations.getPersistenceManagerHelper();
        try {
            return getSaleAssociate(user, pm);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Get the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @param pm PersistenceManager instance used if the OpenID user has not been yet attached;
     *           Should stay open for future usage of the connection
     * @return Corresponding SaleAssociate instance or <code>null</code> if none is attached to the OpenID user
     *
     * @throws DataSourceException If the data retrieval fails
     */
    public static SaleAssociate getSaleAssociate(OpenIdUser user, PersistenceManager pm) throws DataSourceException {
        Long saleAssociateKey = getSaleAssociateKey(user, pm);
        return saleAssociateKey == null ? null : saleAssociateOperations.getSaleAssociate(pm, saleAssociateKey);
    }
}
