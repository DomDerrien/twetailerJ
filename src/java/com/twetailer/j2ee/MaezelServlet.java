package com.twetailer.j2ee;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonException;
import org.domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.ClientException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Request;

@SuppressWarnings("serial")
public class MaezelServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(MaezelServlet.class.getName());

	@Override
    @SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	Utils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();
        
        try {
        	// FIXME: covers the Maezel servlet with an "admin" security role 
        	// http://code.google.com/appengine/docs/java/config/webxml.html#Security_and_Authentication
	        User loggedUser = Utils.getLoggedUser();
	        loggedUser.toString(); // To prevent warnings
	
            String pathInfo = request.getPathInfo();
            log.finer("Path Info: " + pathInfo);
            
            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/checkId".equals(pathInfo)) {
            	// Create the consumer
            	String twitterId = in.containsKey("twitterId") ? in.getString("twitterId") : null;
            	if (twitterId == null) {
            	    throw new ClientException("twitterId cannot be null");
            	}
            	Consumer consumer = (new ConsumersServlet()).createConsumer(null, null, twitterId);
            	// Return the consumer information
                out.put("resource", consumer.toJson());
            }
            else if ("/createRequest".equals(pathInfo)) {
            	// Look for the corresponding consumer account
            	String twitterId = in.containsKey("twitterId") ? in.getString("twitterId") : null;
            	Consumer consumer = (new ConsumersServlet()).getConsumer("twitterId", twitterId);
            	if (consumer == null) {
            		throw new ClientException("Given Twitter identified does not match any exisiting account");
            	}
            	// Create the request
            	Long requestKey = (new RequestsServlet()).createRequest(in, consumer);
            	// Return request identifier
            	out.put("resourceId", requestKey);
            }
            else if ("/getRequests".equals(pathInfo)) {
            	// Select the requests
            	List<Request> requests = (new RequestsServlet()).getRequests(in.getString("qA"), in.getString("qV"));
            	// Return request list
            	out.put("resources", Utils.toJson(requests));
            }
            
            out.put("success", true);
        }
        catch(Exception ex) {
        	log.warning("doGet().exception: " + ex);
            if (log.getLevel() == Level.FINE) {
            	ex.printStackTrace();
            	if (ex.getCause() != null) {
            		ex.getCause().printStackTrace();
            	}
            }
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
	}
}
