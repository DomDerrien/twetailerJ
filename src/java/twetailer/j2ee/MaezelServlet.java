package twetailer.j2ee;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import twetailer.ClientException;
import twetailer.adapter.TwitterAdapter;
import twetailer.adapter.TwitterRobot;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Retailer;
import twetailer.dto.Store;
import twetailer.rest.BaseOperations;
import twetailer.rest.ConsumerOperations;
import twetailer.rest.DemandOperations;
import twetailer.rest.LocationOperations;
import twetailer.rest.RetailerOperations;
import twetailer.rest.StoreOperations;
import twetailer.task.DemandProcessor;
import twetailer.validator.DemandValidator;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class MaezelServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(MaezelServlet.class.getName());

    private BaseOperations _baseOperations = new BaseOperations();
    private ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    private DemandOperations demandOperations = _baseOperations.getDemandOperations();
    private LocationOperations locationOperations = _baseOperations.getLocationOperations();
    private RetailerOperations retailerOperations = _baseOperations.getRetailerOperations();
    private StoreOperations storeOperations = _baseOperations.getStoreOperations();
        
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        // JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();
        
        try {
	        User loggedUser = ServletUtils.getLoggedUser();
	        loggedUser.toString(); // To prevent warnings
	
            String pathInfo = request.getPathInfo();
            log.warning("Path Info: " + pathInfo);
            
            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/processDMs".equals(pathInfo)) {
                Long newSinceId = TwitterAdapter.processDirectMessages();
                out.put("newSinceId", newSinceId);
            }
            else if ("/validateOpenDemands".equals(pathInfo)) {
                DemandValidator.process();
            }
            else if ("/processPubDemands".equals(pathInfo)) {
                DemandProcessor.process();
            }
            else if ("/processRobotMessages".equals(pathInfo)) {
                TwitterRobot.processDirectMessages();
            }
            else if ("/processProposals".equals(pathInfo)) {
                throw new InvalidParameterException("Not yet implemented");
            }
            else if ("/createLocation".equals(pathInfo)) {
                // Supported formats:
                //   http:<host:port>/API/maezel/createLocation?postalCode=H0H0H0&countryCode=CA
                //   http:<host:port>/API/maezel/createLocation?postalCode=H0H0H0&countryCode=CA&latitude=45.0&longitude=30.0
                
                Location somewhere = new Location();
                somewhere.setCountryCode(request.getParameter("countryCode"));
                somewhere.setPostalCode(request.getParameter("postalCode"));
                if (request.getParameter("latitude") !=  null) somewhere.setLatitude(Double.valueOf(request.getParameter("latitude")));
                if (request.getParameter("longitude") != null) somewhere.setLongitude(Double.valueOf(request.getParameter("longitude")));
                
                locationOperations.createLocation(somewhere);
            }
            else if ("/createStore".equals(pathInfo)) {
                // Supported formats:
                //   http:<host:port>/API/maezel/createStore?postalCode=H0H0H0&address=number, street, city, postal code, country&name=store name
                //   http:<host:port>/API/maezel/createStore?postalCode=H0H0H0&address=1, Frozen street, North Pole, H0H 0H0, Canada&name=Toys Factory
                
                PersistenceManager pm = _baseOperations.getPersistenceManager();
                try {
                    Long locationKey = Long.parseLong(request.getParameter("locationKey"));
                    
                    Location location = locationOperations.getLocation(pm, locationKey);
                    location.setHasStore(Boolean.TRUE);
                    locationOperations.updateLocation(pm, location);
                    
                    Store santaFactory = new Store();
                    santaFactory.setLocationKey(locationKey);
                    santaFactory.setAddress(request.getParameter("address"));
                    santaFactory.setName(request.getParameter("name"));
                    storeOperations.createStore(pm, santaFactory);
                }
                finally {
                    pm.close();
                }
            }
            else if ("/createRetailer".equals(pathInfo)) {
                // Supported formats:
                //   http:<host:port>/API/maezel/createDemand?storeKey=11&name=Jack the Troll&supplies=wii console xbox gamecube
    
                PersistenceManager pm = _baseOperations.getPersistenceManager();
                try {
                    Consumer consumer = consumerOperations.getConsumer(pm, Long.parseLong(request.getParameter("consumerKey")));
                    Long storeKey = Long.valueOf(request.getParameter("storeKey"));
                    
                    Retailer retailer = retailerOperations.createRetailer(pm, consumer, storeKey);

                    pm.close();
                    pm = _baseOperations.getPersistenceManager();
                    
                    Retailer reload = retailerOperations.getRetailer(pm, retailer.getKey());

                    String[] supplies = request.getParameter("supplies").split(" ");
                    for (int i = 0; i < supplies.length; i++) {
                        retailer.addCriterion(supplies[i]);
                    }
                    retailerOperations.updateRetailer(pm, reload);
                }
                finally {
                    pm.close();
                }
                
            }
            else if ("/createDemand".equals(pathInfo)) {
                // Supported formats:
                //   http:<host:port>/API/maezel/createDemand?consumerKey=11&tags=wii console&postalCode=H0H0H0

                List<Location> locations = locationOperations.getLocations(Location.POSTAL_CODE, request.getParameter("postalCode"), 0);

                Demand demand = new Demand();
                demand.setLocationKey(locations.get(0).getKey());
                demand.setConsumerKey(Long.valueOf(request.getParameter("consumerKey")));
                String[] tags = request.getParameter("tags").split(" ");
                for (int i = 0; i < tags.length; i++) {
                    demand.addCriterion(tags[i]);
                }
                
                demandOperations.createDemand(demand);
            }
            else {
                throw new ClientException("Unsupported query path: " + pathInfo);
            }
            
            out.put("success", true);
        }
        catch(Exception ex) {
        	log.warning("doGet().exception: " + ex);
        	ex.printStackTrace();
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
	}
}
