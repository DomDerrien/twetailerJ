// Adaptation of David Yu's code for dyuproject, made available under
// the Apache licence 2.0. The adaptation mostly introduce more flexibility
// regarding the workflow and to make it unit-test-able

package twetailer.j2ee;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dao.BaseOperations;
import twetailer.dto.Consumer;
import twetailer.dto.SaleAssociate;
import twetailer.task.step.BaseSteps;
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
 * Login servlet which associates the just authenticated user
 * to the session
 *
 * @author David Yu
 * @maintainer Dom Derrien
 */
@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(LoginServlet.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    public static final String FROM_PAGE_URL_KEY = "fromPageURL";

    protected static Listener sregExtension =
        new SRegExtension().
        addExchange("email").
        addExchange("country").
        addExchange("language").
        addExchange("nickname");
        // addExchange("firstname").
        // addExchange("lastname");

    protected static Listener axSchemaExtension =
        new AxSchemaExtension().
        addExchange("email").
        addExchange("country").
        addExchange("language").
        addExchange("firstname").
        addExchange("lastname").
        addExchange("nickname");

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
            getLogger().finest("Authentication listener:\n- user access: " + user.getIdentity() + "\n- info: " + user.getAttribute("info"));
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
            if (pageToGo == null) {
                pageToGo = ApplicationSettings.get().getMainPageURL();
            }

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
            getLogger().warning("Issue while trying to contact the authentication server -- message " + uhe.getMessage());
            errorMsg = OpenIdServletFilter.ID_NOT_FOUND_MSG;
            uhe.printStackTrace();
        }
        catch (FileNotFoundException fnfe) {
            getLogger().warning("Issue while accessing a file (?) -- message " + fnfe.getMessage());
            errorMsg = OpenIdServletFilter.DEFAULT_ERROR_MSG;
            fnfe.printStackTrace();
        }
        catch (Exception ex) {
            getLogger().warning("Unexpected error -- message " + ex.getMessage());
            errorMsg = OpenIdServletFilter.DEFAULT_ERROR_MSG;
            ex.printStackTrace();
        }
        request.setAttribute(OpenIdServletFilter.ERROR_MSG_ATTR, errorMsg);
        request.getRequestDispatcher(ApplicationSettings.get().getLoginPageURL()).forward(request, response);
    }

    protected static final String AUTHENTICATED_CONSUMER_ID = "authConsumer_tId";

    /**
     * Save the Consumer key as attribute of the OpenID user record
     *
     * @param user Descriptor of the logged in OpenID user
     */
    protected static void attachConsumerToSession(OpenIdUser user) {
        // Create only if does not yet exist, otherwise return the existing instance
        Consumer consumer = BaseSteps.getConsumerOperations().createConsumer(user);
        // Attached the consumer identifier to the OpenID user record
        user.setAttribute(AUTHENTICATED_CONSUMER_ID, consumer.getKey());
    }

    /**
     * Return the Consumer key corresponding to the OpenID user
     *
     * @param user OpenID user with the attached Consumer key
     * @return The key of the Consumer instance attached to the OpenID user
     */
    public static Long getConsumerKey(OpenIdUser user) {
        return user == null ? null : (Long) user.getAttribute(AUTHENTICATED_CONSUMER_ID);
    }

    /**
     * Create a Consumer instance for the OpenID user
     *
     * @param user OpenID user to create a Consumer instance for
     * @return Corresponding Consumer instance
     *
     * @throws InvalidIdentifierException If the Consumer instance retrieval fails
     *
     * @see LoginServlet#getConsumer(PersistenceManager, OpenIdUser)
     */
    public static Consumer getConsumer(OpenIdUser user) throws InvalidIdentifierException {
        if (user == null) {
            return null;
        }
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
     * @throws InvalidIdentifierException If the Consumer instance retrieval fails
     */
    public static Consumer getConsumer(OpenIdUser user, PersistenceManager pm) throws InvalidIdentifierException {
        return user == null ? null : BaseSteps.getConsumerOperations().getConsumer(pm, getConsumerKey(user));
    }

    protected static final String SALE_ASSOCIATION_ALREADY_CHECKED = "authSA_alreadyChecked";
    protected static final String AUTHENTICATED_SALE_ASSOCIATE_ID = "authSA_tId";
    protected static final String AUTHENTICATED_STORE_ID = "authStore_tId";

    /**
     * Get the key of the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @return Corresponding SaleAssociate key
     *
     * @throws InvalidIdentifierException If the SaleAssociate instance retrieval fails
     *
     * @see LoginServlet#getSaleAssociateKey(PersistenceManager, OpenIdUser)
     */
    public static Long getSaleAssociateKey(OpenIdUser user) throws InvalidIdentifierException {
        if (user == null) {
            return null;
        }
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            return getSaleAssociateKey(user, pm);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Get the key of the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @param pm PersistenceManager instance used if the OpenID user has not been yet attached;
     *           Should stay open for future usage of the connection
     * @return Corresponding SaleAssociate key
     *
     * @throws InvalidIdentifierException If the Consumer instance retrieval fails
     */
    public static Long getSaleAssociateKey(OpenIdUser user, PersistenceManager pm) throws InvalidIdentifierException {
        if (user == null) {
            return null;
        }
        if (user.getAttribute(SALE_ASSOCIATION_ALREADY_CHECKED) != null) {
            return (Long) user.getAttribute(AUTHENTICATED_SALE_ASSOCIATE_ID);
        }
        Long saleAssociateKey = getConsumer(user, pm).getSaleAssociateKey();
        user.setAttribute(AUTHENTICATED_SALE_ASSOCIATE_ID, saleAssociateKey);
        user.setAttribute(SALE_ASSOCIATION_ALREADY_CHECKED, Boolean.TRUE); // Won't be null at the next request
        return saleAssociateKey;
    }

    /**
     * Get the Store key of the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @return Corresponding store key
     *
     * @throws InvalidIdentifierException If the SaleAssociate instance retrieval fails
     * @throws ReservedOperationException If the logged user is not an associate
     *
     * @see LoginServlet#getStoreKey(PersistenceManager, OpenIdUser)
     */
    public static Long getStoreKey(OpenIdUser user) throws InvalidIdentifierException, ReservedOperationException {
        if (user == null) {
            return null;
        }
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            return getStoreKey(user, pm);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Get the Store key of the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @param pm PersistenceManager instance used if the OpenID user has not been yet attached;
     *           Should stay open for future usage of the connection
     * @return Corresponding store key
     *
     * @throws InvalidIdentifierException If the Consumer instance retrieval fails
     * @throws ReservedOperationException If the logged user is not an associate
     */
    public static Long getStoreKey(OpenIdUser user, PersistenceManager pm) throws InvalidIdentifierException, ReservedOperationException {
        if (user == null) {
            return null;
        }
        if (user.getAttribute(SALE_ASSOCIATION_ALREADY_CHECKED) != null) {
            Long storeKey = (Long) user.getAttribute(AUTHENTICATED_STORE_ID);
            if (storeKey != null) {
                return storeKey;
            }
        }
        Long storeKey = getSaleAssociate(user, pm).getStoreKey();
        user.setAttribute(AUTHENTICATED_STORE_ID, storeKey);
        return storeKey;
    }

    /**
     * Get the SaleAssociate instance for the OpenID user
     *
     * @param user OpenID user to create a SaleAssociate instance for
     * @return Corresponding SaleAssociate instance or <code>null</code> if none is attached to the OpenID user
     *
     * @throws InvalidIdentifierException If the SaleAssociate instance retrieval fails
     * @throws ReservedOperationException If the logged user is not an associate
     *
     * @see LoginServlet#getSaleAssociate(PersistenceManager, OpenIdUser)
     */
    public static SaleAssociate getSaleAssociate(OpenIdUser user) throws InvalidIdentifierException, ReservedOperationException {
        if (user == null) {
            return null;
        }
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
     * @throws InvalidIdentifierException If the SaleAssociate instance retrieval fails
     * @throws ReservedOperationException
     */
    public static SaleAssociate getSaleAssociate(OpenIdUser user, PersistenceManager pm) throws InvalidIdentifierException, ReservedOperationException {
        if (user == null) {
            return null;
        }
        try {
            Long saleAssociateKey = getSaleAssociateKey(user, pm);
            if (saleAssociateKey == null) {
                return null;
            }
            return BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, saleAssociateKey);
        }
        catch (Exception ex) {
            throw new ReservedOperationException("Cannot get the SaleAssociate record!", ex);
        }
    }
}
