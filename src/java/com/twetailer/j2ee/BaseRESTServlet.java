package com.twetailer.j2ee;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonException;
import org.domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.DataSourceException;

@SuppressWarnings("serial")
public abstract class BaseRESTServlet extends HttpServlet {
	/**
	 * Get the logging handler
	 * @return Reference on the local Logger instance
	 */
	abstract protected Logger getLogger();
	
	/**
	 * Create the resource with the given attributes
	 * 
	 * @param parameters HTTP request parameters
	 * @param loggedUser System identity of the logged user
	 * @return Identifier of the newly created resource
	 * 
	 * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
	 */
	abstract protected String createResource(JsonObject parameters, User loggedUser) throws DataSourceException;
	
	/**
	 * Delete the identified resource
	 * 
	 * @param resourceId Identifier of the concerned resource
	 * @param loggedUser System identity of the logged user
	 * 
	 * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
	 */
	abstract protected void deleteResource(String resourceId, User loggedUser) throws DataSourceException;

	/**
	 * Get the detailed information on the identified resource
	 * 
	 * @param parameters HTTP request parameters
	 * @param resourceId Identifier of the concerned resource
	 * @param loggedUser System identity of the logged user
	 * @return ready to be serialized object
	 * 
	 * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
	 */
	abstract protected JsonObject getResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException;
	
	/**
	 * Select the resources with the given search criteria passed as request parameters
	 * 
	 * @param parameters HTTP request parameters
	 * @return ready to be serialized list of object list
	 * 
	 * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
	 */
	abstract protected JsonArray selectResources(JsonObject parameters) throws DataSourceException;
	
	/**
	 * Update the identified resource with the given attributes
	 * 
	 * @param parameters HTTP request parameters
	 * @param resourceId Identifier of the concerned resource
	 * @param loggedUser System identity of the logged user
	 * 
	 * @throws DataSourceException If something goes wrong when getting data from the back-end or if the data are invalid
	 */
	abstract protected void updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException;
	
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
	        User loggedUser = Utils.getLoggedUser(true);
	
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
		        String key = keyMatcher.matches() ? keyMatcher.group(1) : "<nop>";
		        // Get consumer by key
            	out.put("resource", getResource(in, key, loggedUser));
	        }
	        else {
	        	throw new RuntimeException("Unsupported User resource URL format,pathInfo:'"+request.getPathInfo()+"'");
	        }
        
            out.put("success", true);
        }
        catch(Exception ex) {
        	getLogger().warning("doGet().exception: " + ex);
            if (getLogger().getLevel() == Level.FINEST) {
            	ex.printStackTrace();
            }
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during BaseRESTServlet.doGet() operation", ex);
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
	        User loggedUser = Utils.getLoggedUser(true);
	        loggedUser.toString(); // To prevent warnings
	
            String pathInfo = request.getPathInfo();
            getLogger().finer("Path Info: " + pathInfo);
            
            out.put("resourceId", in.getString("key")); // TODO: put the real code
            
            out.put("success", true);
        }
        catch(Exception ex) {
        	getLogger().warning("doGet().exception: " + ex);
            if (getLogger().getLevel() == Level.FINEST) {
            	ex.printStackTrace();
            }
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during BaseRESTServlet.doPost() operation", ex);
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
	        User loggedUser = Utils.getLoggedUser(true);
	        loggedUser.toString(); // To prevent warnings
	
            out.put("resourceId", createResource(in, loggedUser));
            
            out.put("success", true);
        }
        catch(Exception ex) {
        	getLogger().warning("doGet().exception: " + ex);
            if (getLogger().getLevel() == Level.FINEST) {
            	ex.printStackTrace();
            }
            out = (new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during BaseRESTServlet.doPut() operation", ex));
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
	        User loggedUser = Utils.getLoggedUser(true);
	        loggedUser.toString(); // To prevent warnings
	
            String pathInfo = request.getPathInfo();
            getLogger().finer("Path Info: " + pathInfo);
            
            out.put("resourceId", in.getString("key")); // TODO: put the real code
            
            out.put("success", true);
        }
        catch(Exception ex) {
        	getLogger().warning("doGet().exception: " + ex);
            if (getLogger().getLevel() == Level.FINEST) {
            	ex.printStackTrace();
            }
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during BaseRESTServlet.doDelete() operation", ex);
        }
        
        out.toStream(response.getOutputStream(), false);
	}
    
    /**
     * Accessor isolated to facilitate tests by IOP
     * @return Persistence manager instance
     */
    protected PersistenceManager getPersistenceManager() {
    	return Utils.getPersistenceManager();
    }
}
