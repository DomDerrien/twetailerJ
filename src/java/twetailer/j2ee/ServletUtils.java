package twetailer.j2ee;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class ServletUtils {
	
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
    public static void configureHttpParameters(HttpServletRequest request, HttpServletResponse response) {
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
    
    /* Injection entry point for tests */
    static UserService userServiceForTest = null;
    public static void setUserService(UserService userService) {
    	userServiceForTest = userService;
    }
    
    protected static UserService getUserService() {
    	if (userServiceForTest != null) {
    		return userServiceForTest;
    	}
    	return UserServiceFactory.getUserService();
    }
    
    /**
     * Return an instance of <code>User</code> class identifying the logged user
     * 
     * @return Instance of the <code>User</code> class representing the logged user
     * 
     * @throws RuntimeException When the request is not associated to an identified user, or if the user is not an administrator as expected
     */
    protected static User getLoggedUser() {
        UserService userService = getUserService();
        User loggedUser = userService.getCurrentUser();
        if (loggedUser == null) {
        	throw new RuntimeException("Query can be posted only by logged users.");
        }
        return loggedUser;
    }
}
