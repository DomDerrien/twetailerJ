package com.twetailer.j2ee;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.twetailer.ClientException;
import com.twetailer.DataSourceException;

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
     * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
     * @throws ClientException If the proposed data are invalid
     */
    abstract protected void updateResource(JsonObject parameters, String resourceId, User loggedUser)
            throws DataSourceException, ClientException;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Utils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            User loggedUser = Utils.getLoggedUser();

            String pathInfo = request.getPathInfo();
            getLogger().fine("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
                // Get selected consumers
                out.put("resources", selectResources(in));
            }
            else if ("/current".equals(pathInfo)) {
                // Get current consumer
                out.put("resource", getResource(in, "current", loggedUser));
            }
            else if (Pattern.matches("/(\\w+)", pathInfo)) {
                // Get the key
                Matcher keyMatcher = Utils.uriKeyPattern.matcher(pathInfo);
                keyMatcher.matches();
                String key = keyMatcher.group(1);
                // Get consumer by key
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
        Utils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            User loggedUser = Utils.getLoggedUser();
            loggedUser.toString(); // To prevent warnings

            String pathInfo = request.getPathInfo();
            getLogger().finer("Path Info: " + pathInfo);

            out.put("resourceId", in.getString("key")); // TODO: put the real code

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
        Utils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            User loggedUser = Utils.getLoggedUser();
            loggedUser.toString(); // To prevent warnings

            out.put("resourceId", createResource(in, loggedUser));

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
    @SuppressWarnings("unchecked")
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Utils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            User loggedUser = Utils.getLoggedUser();
            loggedUser.toString(); // To prevent warnings

            String pathInfo = request.getPathInfo();
            getLogger().finer("Path Info: " + pathInfo);

            out.put("resourceId", in.getString("key")); // TODO: put the real code

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

    /**
     * Accessor isolated to facilitate tests by IOP
     * 
     * @return Persistence manager instance
     */
    public PersistenceManager getPersistenceManager() {
        PersistenceManager pm = Utils.getPersistenceManager();
        pm.setDetachAllOnCommit(true);
        pm.setCopyOnAttach(false);
        return pm;
    }
    
    /**
     * Prepare the query with the given parameters
     * 
     * @param query Object to prepare
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Updated query
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public Query prepareQuery(Query query, String attribute, Object value, int limit) throws DataSourceException {
        query.setFilter(attribute + " == value");
        query.setOrdering("creationDate desc");
        if (value instanceof String) {
            query.declareParameters("String value");
        }
        else if (value instanceof Long) {
            query.declareParameters("Long value");
        }
        else if (value instanceof Integer) {
            query.declareParameters("Long value");
            value = Long.valueOf((Integer) value);
        }
        else if (value instanceof Date) {
            query.declareParameters("Date value");
        }
        else {
            throw new DataSourceException("Unsupported criteria value type: " + value.getClass());
        }
        if (0 < limit) {
            query.setRange(0, limit);
        }
        getLogger().warning("Select demand(s) with: " + query.toString());

        return query;
    }
}
