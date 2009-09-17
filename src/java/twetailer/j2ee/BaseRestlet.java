package twetailer.j2ee;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import twetailer.ClientException;
import twetailer.DataSourceException;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;

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
    abstract protected JsonObject createResource(JsonObject parameters, User loggedUser) throws DataSourceException, ClientException;

    /**
     * Delete the identified resource
     * 
     * @param resourceId Identifier of the concerned resource
     * @param loggedUser System identity of the logged user
     * 
     * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
     * @throws ClientException If the proposed data are invalid
     */
    abstract protected void deleteResource(String resourceId, User loggedUser) throws DataSourceException, ClientException;

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
    abstract protected JsonObject getResource(JsonObject parameters, String resourceId, User loggedUser)
            throws DataSourceException, ClientException;

    /**
     * Select the resources with the given search criteria passed as request parameters
     * 
     * @param parameters HTTP request parameters
     * @return ready to be serialized list of object list
     * 
     * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
     * @throws ClientException If the proposed data are invalid
     */
    abstract protected JsonArray selectResources(JsonObject parameters) throws DataSourceException, ClientException;

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
    abstract protected JsonObject updateResource(JsonObject parameters, String resourceId, User loggedUser)
            throws DataSourceException, ClientException;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            User loggedUser = ServletUtils.getLoggedUser();

            String pathInfo = request.getPathInfo();
            getLogger().fine("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
                // Get selected resources
                out.put("resources", selectResources(in));
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
            getLogger().warning("doGet().exception: " + ex);
            if (getLogger().getLevel() == Level.FINEST) {
                ex.printStackTrace();
            }
            out = new JsonException("UNEXPECTED_EXCEPTION",
                    "Unexpected exception during BaseRESTServlet.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            User loggedUser = ServletUtils.getLoggedUser();
            loggedUser.toString(); // To prevent warnings

            String pathInfo = request.getPathInfo();
            getLogger().finer("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
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
            getLogger().warning("doGet().exception: " + ex);
            if (getLogger().getLevel() == Level.FINEST) {
                ex.printStackTrace();
            }
            out = new JsonException("UNEXPECTED_EXCEPTION",
                    "Unexpected exception during BaseRESTServlet.doPost() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            User loggedUser = ServletUtils.getLoggedUser();
            loggedUser.toString(); // To prevent warnings

            String pathInfo = request.getPathInfo();
            getLogger().finer("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
                // Create the resource
                out.put("resource", createResource(in, loggedUser));
            }
            else {
                throw new RuntimeException("Unsupported URL format, pathInfo: " + request.getPathInfo());
            }

            out.put("success", true);
        }
        catch (Exception ex) {
            getLogger().warning("doGet().exception: " + ex);
            if (getLogger().getLevel() == Level.FINEST) {
                ex.printStackTrace();
            }
            out = (new JsonException("UNEXPECTED_EXCEPTION",
                    "Unexpected exception during BaseRESTServlet.doPut() operation", ex));
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JsonObject out = new GenericJsonObject();

        try {
            User loggedUser = ServletUtils.getLoggedUser();
            loggedUser.toString(); // To prevent warnings

            String pathInfo = request.getPathInfo();
            getLogger().finer("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
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
            getLogger().warning("doGet().exception: " + ex);
            if (getLogger().getLevel() == Level.FINEST) {
                ex.printStackTrace();
            }
            out = new JsonException("UNEXPECTED_EXCEPTION",
                    "Unexpected exception during BaseRESTServlet.doDelete() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
    }
}
