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
import com.twetailer.adapter.TwitterAdapter;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;

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
            log.warning("Path Info: " + pathInfo);
            
            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/processDMs".equals(pathInfo)) {
                Long newSinceId = (new TwitterAdapter()).processDirectMessages();
                out.put("newSinceId", newSinceId);
            }
            else if ("/checkId".equals(pathInfo)) {
            	// Create the consumer
            	String twitterId = in.containsKey("twitterId") ? in.getString("twitterId") : null;
            	if (twitterId == null) {
            	    throw new ClientException("twitterId cannot be null");
            	}
            	Consumer consumer = (new ConsumersServlet()).createConsumer(new User("email", "domain"));
            	// Return the consumer information
                out.put("resource", consumer.toJson());
            }
            else if ("/createDemand".equals(pathInfo)) {
            	// Look for the corresponding consumer account
            	String twitterId = in.containsKey("twitterId") ? in.getString("twitterId") : null;
            	Consumer consumer = (new ConsumersServlet()).getConsumer("twitterId", twitterId);
            	if (consumer == null) {
            		throw new ClientException("Given Twitter identified does not match any exisiting account");
            	}
            	// Create the demand
            	Long demandKey = (new DemandsServlet()).createDemand(in, consumer);
            	// Return request identifier
            	out.put("resourceId", demandKey);
            }
            else if ("/getDemands".equals(pathInfo)) {
                // Select the demands
                List<Demand> demands = (new DemandsServlet()).getDemands(in.getString("qA"), in.getLong("qV"));
                // Return demand list
                out.put("resources", Utils.toJson(demands));
            }
            else if ("/getDemand".equals(pathInfo)) {
                // Select the demands
                Demand demand = (new DemandsServlet()).getDemand(in.getLong("key"), in.getLong("consumerKey"));
                // Return demand
                out.put("resource", demand.toJson());
            }
            else if ("/deleteDemand".equals(pathInfo)) {
                // Select the demands
                (new DemandsServlet()).deleteDemand(in.getLong("key"), in.getLong("consumerKey"));
                // If an error occurred, an exception has been thrown, and the status will be conveyed as is to the client 
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
