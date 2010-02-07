package twetailer.j2ee;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javamocks.io.MockOutputStream;

import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.task.CommandProcessor;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.RelyingParty;
import com.dyuproject.openid.YadisDiscovery;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

@SuppressWarnings("serial")
public abstract class BaseRestlet extends HttpServlet {
    /**
     * Get the logging handler
     *
     * @return Reference on the local Logger instance
     */
    abstract protected Logger getLogger();

    /**
     * Create the resource with the given attributes
     *
     * @param parameters HTTP request parameters
     * @param loggedUser System identity of the logged user
     * @return Newly created resource
     *
     * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
     * @throws ClientException If the proposed data are invalid
     */
    abstract protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException;

    /**
     * Delete the identified resource
     *
     * @param resourceId Identifier of the concerned resource
     * @param loggedUser System identity of the logged user
     *
     * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
     * @throws ClientException If the proposed data are invalid
     */
    abstract protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException;

    /**
     * Get the detailed information on the identified resource
     *
     * @param parameters HTTP request parameters
     * @param resourceId Identifier of the concerned resource
     * @param loggedUser System identity of the logged user
     * @return ready to be serialized object
     *
     * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
     * @throws ClientException If the proposed data are invalid
     */
    abstract protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser)
            throws DataSourceException, ClientException;

    /**
     * Select the resources with the given search criteria passed as request parameters
     *
     * @param parameters HTTP request parameters
     * @param loggedUser System identity of the logged user
     * @return ready to be serialized list of object list
     *
     * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
     * @throws ClientException If the proposed data are invalid
     */
    abstract protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException;

    /**
     * Update the identified resource with the given attributes
     *
     * @param parameters HTTP request parameters
     * @param resourceId Identifier of the concerned resource
     * @param loggedUser System identity of the logged user
     * @return Updated resource
     *
     * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
     * @throws ClientException If the proposed data are invalid
     */
    abstract protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser)
            throws DataSourceException, ClientException;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Helper made available to be able to inject a mock OpenIdUser from the unit tests
     *
     * @param parameters HTTP request parameters
     * @return OpenIdUser instance extracted from the session
     *
     * @throws Exception If the OpendIdUser un-marshaling fails
     */
    public static OpenIdUser getLoggedUser(HttpServletRequest request) throws Exception {
        if (debugModeDetected(request)) {
            injectMockOpenUser(request);
        }
        return RelyingParty.getInstance().discover(request);
    }

    /**
     * Helper returning <code>true if the logged user is one with top privileges
     *
     * @param loggedUser authenticated user descriptor
     * @return <code>true</code> if the user has top privileges
     */
    @SuppressWarnings("unchecked")
    public static boolean isAPrivilegedUser(OpenIdUser loggedUser) {
        //
        // TODO: replace this verification by checking if the user is an administrator for App Engine?
        //
        if (loggedUser.getAttribute("info") != null) {
            Map<String, String> info = (Map<String, String>) loggedUser.getAttribute("info");
            if (info.get("email") != null) {
                String email = info.get("email");
                return ("dominique.derrien@gmail.com".equals(email) || "steven.milstein@gmail.com".equals(email));
            }
        }
        return false;
    }

    /**
     * Study the request parameter list and checks if there a recognized debug mode switch
     *
     * @param parameters HTTP request parameters
     * @return <code>true</code> if the debug mode is detected, <code>false</code> otherwise.
     */
    protected static boolean debugModeDetected(HttpServletRequest request) {
        return CommandProcessor.DEBUG_INFO_SWITCH.equals(request.getParameter("debugMode"));
    }

    /**
     * Inject in the request a mock OpenUser instance built with values found among the request parameters
     * with fallback on default values
     *
     * @param parameters HTTP request parameters
     */
    protected static void injectMockOpenUser(HttpServletRequest request) {
        // Get the consumer information
        String proposeConsumerKey = request.getParameter("debugConsumerKey");
        Long consumerKey = proposeConsumerKey == null ? 1L : Long.valueOf(proposeConsumerKey);
        String proposedOpenId = request.getParameter("debugConsumerOpenId");
        String openId = proposedOpenId == null ? "http://open.id" : proposedOpenId;

        // Inject the data in an OpenIdUser and inject this one in the request
        injectMockOpenUser(request, openId, consumerKey);
    }

    /**
     * Inject in the request a mock OpenUser instance built with values found among the request parameters
     * with fallback on default values
     *
     * @param parameters HTTP request parameters
     * @param openId openId of the user to fake
     * @param consumerKey Key of the Consumer instance to attach to the fake user
     */
    protected static void injectMockOpenUser(HttpServletRequest request, String openId, Long consumerKey) {
        // Create fake user
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );

        // Inject the result in the request
        injectMockOpenUser(request, user, openId, consumerKey);
    }

    /**
     * Inject in the request a mock OpenUser instance built with values found among the request parameters
     * with fallback on default values
     *
     * @param parameters HTTP request parameters
     * @param user Basic fake user record
     * @param openId openId of the user to fake
     * @param consumerKey Key of the Consumer instance to attach to the fake user
     */
    protected static void injectMockOpenUser(HttpServletRequest request, OpenIdUser user, String openId, Long consumerKey) {
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("a", openId);
        user.fromJSON(json);

        // Inject a defined Consumer key
        user.setAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID, consumerKey);

        // Inject the result in the request
        injectMockOpenUser(request, user);
    }

    /**
     * Inject in the request a mock OpenUser instance built with values found among the request parameters
     * with fallback on default values
     *
     * @param parameters HTTP request parameters
     * @param user Fetched fake user record
     */
    protected static void injectMockOpenUser(HttpServletRequest request, OpenIdUser user) {
        // Save the fake user as the request attribute
        request.setAttribute(OpenIdUser.ATTR_NAME, user);
    }

    private static final String ROOT = "/";

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();
        getLogger().fine("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        JsonObject in = null;

        try {
            // TODO: verify Content-type = "application/x-www-form-urlencoded"
            in = new GenericJsonObject(request.getParameterMap());

            OpenIdUser loggedUser = getLoggedUser(request);

            if (pathInfo == null || pathInfo.length() == 0 || ROOT.equals(pathInfo)) {
                // Get selected resources
                out.put("resources", selectResources(in, loggedUser));
            }
            else if ("/current".equals(pathInfo)) {
                // Get current resource
                out.put("resource", getResource(in, "current", loggedUser));
            }
            else if (Pattern.matches("/(\\w+)", pathInfo)) {
                // Get the key
                Matcher keyMatcher = ServletUtils.uriKeyPattern.matcher(pathInfo);
                keyMatcher.matches();
                String key = keyMatcher.group(1);
                // Get resource by key
                out.put("resource", getResource(in, key, loggedUser));
            }
            else {
                throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
            }

            out.put("success", true);
        }
        catch (Exception ex) {
            String message = "Unexpected exception during BaseRESTServlet.doGet() operation";
            // Special case
            if (ex instanceof com.google.appengine.api.datastore.DatastoreTimeoutException) {
                // http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreTimeoutException.html
                message = "Google App Engine datastore is overloaded or having trouble. Please, submit the same request in few minutes.";
            }
            // Prepare the exception report
            getLogger().warning("doGet().exception: " + ex);
            out = new JsonException("UNEXPECTED_EXCEPTION", message, ex);
            // Send an e-mail to out catch-all list
            MockOutputStream stackTrace = new MockOutputStream();
            ex.printStackTrace(new PrintStream(stackTrace));
            try {
                CatchAllMailHandlerServlet.composeAndPostMailMessage(
                        "error-notifier",
                        "Unexpected error caught in " + this.getClass().getName() + ".doGet()",
                        "Path info: " + pathInfo + "\n\nRequest parameters:\n" + (in == null ? "null" : in.toString()) + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                getLogger().severe("Failure while trying to report an unexpected by e-mail!");
            }
            // Log the stack trace
            if (debugModeDetected(request) || getLogger().getLevel() == Level.FINEST) {
                ex.printStackTrace();
            }
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();
        getLogger().finer("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        JsonObject in = null;

        try {
            // TODO: verify Content-type == "application/json"
            in = new JsonParser(request.getInputStream()).getJsonObject();

            OpenIdUser loggedUser = getLoggedUser(request);

            if (pathInfo == null || pathInfo.length() == 0 || ROOT.equals(pathInfo)) {
                // Create the resource
                out.put("resource", createResource(in, loggedUser));
            }
            else {
                throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
            }

            out.put("success", true);
        }
        catch (Exception ex) {
            String message = "Unexpected exception during BaseRESTServlet.doPost() operation";
            // Special case
            if (ex instanceof com.google.appengine.api.datastore.DatastoreTimeoutException) {
                // http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreTimeoutException.html
                message = "Google App Engine datastore is overloaded or having trouble. Please, submit the same request in few minutes.";
            }
            // Prepare the exception report
            getLogger().warning("doPost().exception: " + ex);
            out = new JsonException("UNEXPECTED_EXCEPTION", message, ex);
            // Send an e-mail to out catch-all list
            MockOutputStream stackTrace = new MockOutputStream();
            ex.printStackTrace(new PrintStream(stackTrace));
            try {
                CatchAllMailHandlerServlet.composeAndPostMailMessage(
                        "error-notifier",
                        "Unexpected error caught in " + this.getClass().getName() + ".doPost()",
                        "Path info: " + pathInfo + "\n\nRequest parameters:\n" + (in == null ? "null" : in.toString()) + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                getLogger().severe("Failure while trying to report an unexpected by e-mail!");
            }
            // Log the stack trace
            if (debugModeDetected(request) || getLogger().getLevel() == Level.FINEST) {
                ex.printStackTrace();
            }
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();
        getLogger().finer("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        JsonObject in = null;

        try {
            // TODO: verify Content-type == "application/json"
            in = new JsonParser(request.getInputStream()).getJsonObject();

            OpenIdUser loggedUser = getLoggedUser(request);

            if (pathInfo == null || pathInfo.length() == 0 || ROOT.equals(pathInfo)) {
                throw new RuntimeException("Required path info for resource update");
            }
            if (Pattern.matches("/(\\w+)", pathInfo)) {
                // Get the key
                Matcher keyMatcher = ServletUtils.uriKeyPattern.matcher(pathInfo);
                keyMatcher.matches();
                String key = keyMatcher.group(1);
                // Update the identified resource
                out.put("resource", updateResource(in, key, loggedUser));
            }
            else {
                throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
            }

            out.put("success", true);
        }
        catch (Exception ex) {
            String message = "Unexpected exception during BaseRESTServlet.doPut() operation";
            // Special case
            if (ex instanceof com.google.appengine.api.datastore.DatastoreTimeoutException) {
                // http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreTimeoutException.html
                message = "Google App Engine datastore is overloaded or having trouble. Please, submit the same request in few minutes.";
            }
            // Prepare the exception report
            getLogger().warning("doPut().exception: " + ex);
            out = new JsonException("UNEXPECTED_EXCEPTION", message, ex);
            // Send an e-mail to out catch-all list
            MockOutputStream stackTrace = new MockOutputStream();
            ex.printStackTrace(new PrintStream(stackTrace));
            try {
                CatchAllMailHandlerServlet.composeAndPostMailMessage(
                        "error-notifier",
                        "Unexpected error caught in " + this.getClass().getName() + ".doPut()",
                        "Path info: " + pathInfo + "\n\nRequest parameters:\n" + (in == null ? "null" : in.toString()) + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                getLogger().severe("Failure while trying to report an unexpected by e-mail!");
            }
            // Log the stack trace
            if (debugModeDetected(request) || getLogger().getLevel() == Level.FINEST) {
                ex.printStackTrace();
            }
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        getLogger().finer("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();

        try {
            OpenIdUser loggedUser = getLoggedUser(request);

            if (pathInfo == null || pathInfo.length() == 0 || ROOT.equals(pathInfo)) {
                throw new RuntimeException("Required path info for resource deletion");
            }
            if (Pattern.matches("/(\\w+)", pathInfo)) {
                // Get the key
                Matcher keyMatcher = ServletUtils.uriKeyPattern.matcher(pathInfo);
                keyMatcher.matches();
                String key = keyMatcher.group(1);
                // Delete the resource
                deleteResource(key, loggedUser);
            }
            else {
                throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
            }

            out.put("success", true);
        }
        catch (Exception ex) {
            String message = "Unexpected exception during BaseRESTServlet.doDelete() operation";
            // Special case
            if (ex instanceof com.google.appengine.api.datastore.DatastoreTimeoutException) {
                // http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreTimeoutException.html
                message = "Google App Engine datastore is overloaded or having trouble. Please, submit the same request in few minutes.";
            }
            // Prepare the exception report
            getLogger().warning("doDelete().exception: " + ex);
            out = new JsonException("UNEXPECTED_EXCEPTION", message, ex);
            // Send an e-mail to out catch-all list
            MockOutputStream stackTrace = new MockOutputStream();
            ex.printStackTrace(new PrintStream(stackTrace));
            try {
                CatchAllMailHandlerServlet.composeAndPostMailMessage(
                        "error-notifier",
                        "Unexpected error caught in " + this.getClass().getName() + ".doDelete()",
                        "Path info: " + pathInfo + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                getLogger().severe("Failure while trying to report an unexpected by e-mail!");
            }
            // Log the stack trace
            if (debugModeDetected(request) || getLogger().getLevel() == Level.FINEST) {
                ex.printStackTrace();
            }
        }

        out.toStream(response.getOutputStream(), false);
    }
}
