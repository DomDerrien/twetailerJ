package twetailer.j2ee;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javamocks.io.MockOutputStream;

import javax.mail.MessagingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.task.CommandProcessor;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.RelyingParty;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

/**
 * Base class for all Twetailer REST servlet interaction model.
 *
 * Define the common servlet entry points and delegate the command
 * process to the derived classes.
 *
 * Only the derived classes appear as servlet entry points (as defined
 * in the <code>web.xml</code> configuration file). The path for the
 * derived class should be determined by the entity each one controls
 * (like "/Consumer" for the ConsumerRestlet).
 *
 * @see twetailer.j2ee.restlet.*
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public abstract class BaseRestlet extends HttpServlet {

    public static final String ANY_STATE_PARAMETER_KEY = "anyState";
    public static final String ONLY_KEYS_PARAMETER_KEY = "onlyKeys";
    public static final String MAXIMUM_RESULTS_PARAMETER_KEY = "maximumResults";

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

    private static final String ROOT = "/";

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();
        getLogger().fine("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        out.put("success", true);
        JsonObject in = null;

        try {
            // TODO: verify Content-type = "application/x-www-form-urlencoded"
            in = new GenericJsonObject(request.getParameterMap());

            OpenIdUser loggedUser = getLoggedUser(request);
            if (loggedUser == null) {
                response.setStatus(401); // Unauthorized
                out.put("success", false);
                out.put("reason", "Unauthorized");
            }
            else if (pathInfo == null || pathInfo.length() == 0 || ROOT.equals(pathInfo)) {
                // Get selected resources
                out.put("resources", selectResources(in, loggedUser));
            }
            else if ("/current".equals(pathInfo)) {
                // Get current resource
                out.put("resource", getResource(in, "current", loggedUser));
            }
            else {
                Matcher keyMatcher = ServletUtils.uriKeyPattern.matcher(pathInfo);
                if (keyMatcher.matches()) {
                    // Get the key
                    String key = keyMatcher.group(1);
                    // Get resource by key
                    out.put("resource", getResource(in, key, loggedUser));
                }
                else {
                    throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
                }
            }
        }
        catch (ReservedOperationException ex) {
            response.setStatus(403); // Forbidden
            out.put("success", false);
            out.put("reason", ex.getMessage());
        }
        catch (Exception ex) {
            response.setStatus(500); // Internal Server Error
            out = processException(getLogger(), ex, "doGet", pathInfo);
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();
        getLogger().finer("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        out.put("success", true);
        JsonObject in = null;

        try {
            // TODO: verify Content-type == "application/json"
            in = new JsonParser(request.getInputStream()).getJsonObject();

            OpenIdUser loggedUser = getLoggedUser(request);
            if (loggedUser == null) {
                response.setStatus(401); // Unauthorized
                out.put("success", false);
                out.put("reason", "Unauthorized");
            }
            else if (pathInfo == null || pathInfo.length() == 0 || ROOT.equals(pathInfo)) {
                // Create the resource
                out.put("resource", createResource(in, loggedUser));
            }
            else {
                throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
            }
        }
        catch (ReservedOperationException ex) {
            response.setStatus(403); // Forbidden
            out.put("success", false);
            out.put("reason", ex.getMessage());
        }
        catch (Exception ex) {
            response.setStatus(500); // Internal Server Error
            out = processException(getLogger(), ex, "doPost", pathInfo);
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();
        getLogger().finer("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        out.put("success", true);
        JsonObject in = null;

        try {
            // TODO: verify Content-type == "application/json"
            in = new JsonParser(request.getInputStream()).getJsonObject();

            OpenIdUser loggedUser = getLoggedUser(request);
            if (loggedUser == null) {
                response.setStatus(401); // Unauthorized
                out.put("success", false);
                out.put("reason", "Unauthorized");
            }
            else if (pathInfo == null || pathInfo.length() == 0 || ROOT.equals(pathInfo)) {
                throw new RuntimeException("Required path info for resource update");
            }
            Matcher keyMatcher = ServletUtils.uriKeyPattern.matcher(pathInfo);
            if (keyMatcher.matches()) {
                // Get the key
                String key = keyMatcher.group(1);
                // Update the identified resource
                out.put("resource", updateResource(in, key, loggedUser));
            }
            else {
                throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
            }
        }
        catch (ReservedOperationException ex) {
            response.setStatus(403); // Forbidden
            out.put("success", false);
            out.put("reason", ex.getMessage());
        }
        catch (Exception ex) {
            response.setStatus(500); // Internal Server Error
            out = processException(getLogger(), ex, "doPut", pathInfo);
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        getLogger().finer("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        out.put("success", true);

        try {
            OpenIdUser loggedUser = getLoggedUser(request);
            if (loggedUser == null) {
                response.setStatus(401); // Unauthorized
                out.put("success", false);
                out.put("reason", "Unauthorized");
            }
            else if (pathInfo == null || pathInfo.length() == 0 || ROOT.equals(pathInfo)) {
                throw new RuntimeException("Required path info for resource deletion");
            }
            else {
                Matcher keyMatcher = ServletUtils.uriKeyPattern.matcher(pathInfo);
                if (keyMatcher.matches()) {
                    // Get the key
                    String key = keyMatcher.group(1);
                    // Delete the resource
                    deleteResource(key, loggedUser);
                    out.put("resourceId", key);
                }
                else {
                    throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
                }
            }
        }
        catch (ReservedOperationException ex) {
            response.setStatus(403); // Forbidden
            out.put("success", false);
            out.put("reason", ex.getMessage());
        }
        catch (Exception ex) {
            response.setStatus(500); // Internal Server Error
            out = processException(getLogger(), ex, "doDelete", pathInfo);
        }

        out.toStream(response.getOutputStream(), false);
    }

    protected static JsonObject processException(Logger logger, Exception ex, String methodName, String pathInfo) {
        String message = "Unexpected exception during BaseRESTServlet." + methodName + "() operation";
        // Special case
        if (ex instanceof com.google.appengine.api.datastore.DatastoreTimeoutException) {
            // http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreTimeoutException.html
            message = "Google App Engine datastore is overloaded or having trouble. Please, submit the same request in few minutes.";
        }
        // Prepare the exception report
        logger.warning(methodName + "().exception: " + ex);
        JsonObject out = new JsonException("UNEXPECTED_EXCEPTION", message, ex);
        // Send an e-mail to out catch-all list
        MockOutputStream stackTrace = new MockOutputStream();
        ex.printStackTrace(new PrintStream(stackTrace));
        try {
            CatchAllMailHandlerServlet.composeAndPostMailMessage(
                    "error-notifier",
                    "Unexpected error caught in BaseRestlet." + methodName + "()",
                    "Path info: " + pathInfo + "\n\n--\n\n" + stackTrace.toString()
            );
        }
        catch (MessagingException e) {
            logger.severe("Failure while trying to report an unexpected by e-mail!");
        }
        return out;
    }
}
