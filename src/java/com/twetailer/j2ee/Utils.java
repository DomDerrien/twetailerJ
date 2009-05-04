package com.twetailer.j2ee;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.domderrien.jsontools.GenericJsonArray;
import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.TransferObject;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class Utils {
	
	private static final PersistenceManagerFactory pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	/**
	 * Singleton accessor
	 * 
	 * @return Initial instance of the <code>PersistenceManagerFactory</code> class
	 */
	public static PersistenceManagerFactory getPersistenceManagerFactory() {
			return pmfInstance;
	}

	/**
	 * Use the <code>PersistenceManagerFactory</code> singleton to generate a new persistence layer manager
	 * 
	 * @return New instance of the <code>PersistenceManager</code> class
	 */
	public static PersistenceManager getPersistenceManager() {
			return getPersistenceManagerFactory().getPersistenceManager();
	}

	/**
	 * Fixed pattern for the regular expression extracting the resource keys from the URIs
	 */
    public static final Pattern uriKeyPattern = Pattern.compile("/(\\w+)");

    /**
     * Set default parameters for the HTTP request/response objects
     * 
     * @param request Container of the HTTP request parameters
     * @param response Container for the request output stream
     */
    protected static void configureHttpParameters(HttpServletRequest request, HttpServletResponse response) {
        // Set httpRequest encoding
        try {
            request.setCharacterEncoding("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            // Ignore the exception
        }

        // Set httpResponse format
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/javascript;charset=UTF-8");
        
        // FIXME: introduce a strategy that is going to detect future expiration dates
        response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
        response.setHeader("Pragma","no-cache"); // HTTP 1.0
        response.setDateHeader ("Expires", 0); // prevents caching at the proxy server
    }
    
    /**
     * Return an instance of <code>User</code> class identifying the logged user
     * 
     * @param isAdminRequired If set to <code>true</code>, the <code>admin</code> attribute of the logged user is checked
     * @return Instance of the <code>User</code> class representing the logged user
     * 
     * @throws RuntimeException When the request is not associated to an identified user, or if the user is not an administrator as expected
     */
    protected static User getLoggedUser(boolean isAdminRequired) {
        UserService userService = UserServiceFactory.getUserService();
        User loggedUser = userService.getCurrentUser();
        if (loggedUser == null) {
        	throw new RuntimeException("Query can be posted only by logged users.");
        }
        if (false && isAdminRequired) { // FIXME: verify the "admin" flag of the logged user
        	throw new RuntimeException("Query can be posted only by identified administrators.");
        }
        return loggedUser;
    }

	/**
	 * Serialize the given list of <code>TransfertObject</code> instance to be send on the wire
	 * 
	 * @param objects List of serialize-able objects
	 * return object ready to be serialized
	 */
    protected static JsonArray toJson(List<?> objects) {
    	JsonArray out = new GenericJsonArray();
    	for (Object object: objects) {
    		out.add(((TransferObject) object).toJson());
    	}
    	return out;
    }

	/**
	 * Serialize the given map of <code>TransfertObject</code> instance to be send on the wire
	 * 
	 * @param objects Map of serialize-able objects
	 * return object ready to be serialized
	 */
    protected static JsonObject toJson(Map<String, ?> objects) {
    	JsonObject out = new GenericJsonObject();
    	for (String key: objects.keySet()) {
    		out.put(key, ((TransferObject) objects.get(key)).toJson());
    	}
    	return out;
    }
}
