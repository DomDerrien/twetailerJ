package twetailer.j2ee;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Retailer;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.task.CommandProcessor;
import twetailer.task.DemandProcessor;
import twetailer.task.DemandValidator;
import twetailer.task.RobotResponder;
import twetailer.task.TweetLoader;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class MaezelServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(MaezelServlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected RetailerOperations retailerOperations = _baseOperations.getRetailerOperations();
    protected StoreOperations storeOperations = _baseOperations.getStoreOperations();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        // JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            String pathInfo = request.getPathInfo();
            log.warning("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/loadTweets".equals(pathInfo)) {
                Long newSinceId = TweetLoader.loadDirectMessages();
                out.put(Settings.LAST_PROCESSED_DIRECT_MESSAGE_ID, newSinceId);
            }
            else if ("/processCommand".equals(pathInfo)) {
                Long commandId = Long.parseLong(request.getParameter(Command.KEY));
                CommandProcessor.processRawCommands(commandId);
            }
            else if ("/validateOpenDemand".equals(pathInfo)) {
                Long demandId = Long.parseLong(request.getParameter(Demand.KEY));
                DemandValidator.process(demandId);
            }
            else if ("/processPublishedDemand".equals(pathInfo)) {
                Long demandId = Long.parseLong(request.getParameter(Demand.KEY));
                DemandProcessor.process(demandId);
            }
            else if ("/processRobotMessages".equals(pathInfo)) {
                RobotResponder.processDirectMessages();
            }
            else if ("/processProposals".equals(pathInfo)) {
                /// Long proposalId = Long.parseLong(request.getParameter(Proposal.KEY));
                throw new IllegalArgumentException("Not yet implemented");
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
                    Consumer consumer = consumerOperations.getConsumer(pm, Long.parseLong(request.getParameter(Retailer.CONSUMER_KEY)));
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

                PersistenceManager pm = _baseOperations.getPersistenceManager();
                try {
                    List<Location> locations = locationOperations.getLocations(pm, Location.POSTAL_CODE, request.getParameter(Location.POSTAL_CODE), 0);

                    Demand demand = new Demand();
                    demand.setLocationKey(locations.get(0).getKey());
                    demand.setConsumerKey(Long.valueOf(request.getParameter(Demand.CONSUMER_KEY)));
                    String[] tags = request.getParameter("tags").split(" ");
                    for (int i = 0; i < tags.length; i++) {
                        demand.addCriterion(tags[i]);
                    }

                    demandOperations.createDemand(pm, demand);
                }
                finally {
                    pm.close();
                }
            }
            else if ("/updateConsumer".equals(pathInfo)) {
                // Supported formats:
                //   http:<host:port>/API/maezel/updateConsumer?key=###&jabberId=[name@domain]&twitterId=[screen-name]&locationKey=###

                PersistenceManager pm = _baseOperations.getPersistenceManager();
                try {
                    List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, request.getParameter(Consumer.TWITTER_ID), 0);
                    if (0 < consumers.size()) {
                        Consumer consumer = consumers.get(0);

                        boolean oneUpdate = false;
                        if (request.getParameter(Consumer.ADDRESS) != null) { oneUpdate = true; consumer.setAddress(request.getParameter(Consumer.ADDRESS)); }
                        if (request.getParameter(Consumer.EMAIL) != null) { oneUpdate = true; consumer.setEmail(request.getParameter(Consumer.EMAIL)); }
                        if (request.getParameter(Consumer.JABBER_ID) != null) { oneUpdate = true; consumer.setJabberId(request.getParameter(Consumer.JABBER_ID)); }
                        if (request.getParameter(Consumer.LOCATION_KEY) != null) { oneUpdate = true; consumer.setLocationKey(Long.valueOf(request.getParameter(Consumer.LOCATION_KEY))); }
                        if (request.getParameter(Consumer.LANGUAGE) != null) { oneUpdate = true; consumer.setLanguage(request.getParameter(Consumer.LANGUAGE)); }
                        if (request.getParameter(Consumer.NAME) != null) { oneUpdate = true; consumer.setName(request.getParameter(Consumer.NAME)); }
                        if (request.getParameter(Consumer.PHONE_NUMBER) != null) { oneUpdate = true; consumer.setPhoneNumber(request.getParameter(Consumer.PHONE_NUMBER)); }
                        // if (request.getParameter(Consumer.TWITTER_ID) != null) { oneUpdate = true; consumer.setTwitterId(request.getParameter(Consumer.TWITTER_ID)); }

                        if (oneUpdate) {
                            consumerOperations.updateConsumer(pm, consumer);
                        }
                        else {
                            throw new ClientException("No attribute recognized for the consumer with " + Consumer.TWITTER_ID + " attribute == " + request.getParameter(Consumer.TWITTER_ID));
                        }
                    }
                    else {
                        throw new ClientException("No consumer found for the " + Consumer.TWITTER_ID + " attribute == " + request.getParameter(Consumer.TWITTER_ID));
                    }
                }
                finally {
                    pm.close();
                }
            }
            else {
                throw new ClientException("Unsupported query path: " + pathInfo);
            }

            out.put("success", true);
        }
        catch(Exception ex) {
            log.warning("doGet().exception: " + ex);
            // ex.printStackTrace();
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
    }
}
