package com.twetailer.j2ee;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.ClientException;
import com.twetailer.adapter.TwitterAdapter;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.dto.Retailer;
import com.twetailer.dto.Store;
import com.twetailer.task.DemandProcessor;

@SuppressWarnings("serial")
public class MaezelServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(MaezelServlet.class.getName());

    @Override
	@SuppressWarnings("deprecation")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	Utils.configureHttpParameters(request, response);

        // JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();
        
        try {
	        User loggedUser = Utils.getLoggedUser();
	        loggedUser.toString(); // To prevent warnings
	
            String pathInfo = request.getPathInfo();
            log.warning("Path Info: " + pathInfo);
            
            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/processDMs".equals(pathInfo)) {
                Long newSinceId = new TwitterAdapter().processDirectMessages();
                out.put("newSinceId", newSinceId);
            }
            else if ("/processPubDemands".equals(pathInfo)) {
                new DemandProcessor().process(Locale.ENGLISH);
            }
            else if ("/processProposals".equals(pathInfo)) {
                new DemandProcessor().process(Locale.ENGLISH);
            }
            else if ("/createHohoho".equals(pathInfo)) {
                Store northPole = new Store();
                northPole.setCountryCode("CA");
                northPole.setPostalCode("H0H 0H0");
                northPole.setAddress("North Pole / Pôle nord");
                northPole.setLatitude(Demand.INVALID_COORDINATE);
                northPole.setLongitude(Demand.INVALID_COORDINATE);
                northPole.setEmail("robot@twetailer.com");
                new StoresServlet().createStore(northPole);

                User jackTroll = new User("jacktroll@twetailer.com", "twetailer.com");
                Consumer temp = new ConsumersServlet().createConsumer(jackTroll);
                temp.setName("Jack the Troll");
                temp.setTwitterId(62414620L);
                Retailer temp2 = new RetailersServlet().createRetailer(temp, northPole);
                log.warning("Jack created -- " + temp2.getKey());
            }
            else {
                throw new ClientException("Unsupported query path");
            }
            
            out.put("success", true);
        }
        catch(Exception ex) {
        	log.warning("doGet().exception: " + ex);
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
	}
}
