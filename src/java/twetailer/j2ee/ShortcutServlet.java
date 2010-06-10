package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.Store;
import twetailer.task.CommandProcessor;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings.State;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;
import domderrien.jsontools.JsonUtils;

@SuppressWarnings("serial")
public class ShortcutServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MaezelServlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected StoreOperations storeOperations = _baseOperations.getStoreOperations();
    /*
    protected SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    protected SeedOperations seedOperations = _baseOperations.getSeedOperations();
    protected SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();
    */

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    public static OpenIdUser buildMockOpenIdUser(Long consumerKey) {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );

        Map<String, Object> json = new HashMap<String, Object>();

        json.put("a", "http://shortcut.twetailer.com/" + consumerKey);

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", new HashMap<String, String>());
        json.put("g", attributes);

        user.fromJSON(json);

        user.setAttribute(LoginServlet.AUTHENTICATED_CONSUMER_TWETAILER_ID, consumerKey);

        return user;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Process the given parameters
        Long consumerKey = null;
        if (request.getParameter(Command.OWNER_KEY) != null) {
            consumerKey = Long.valueOf(request.getParameter(Command.OWNER_KEY));
        }
        if (consumerKey == null || consumerKey == 0L) {
            throw new IllegalArgumentException("Invalid owner Key");
        }
        Date lastModificationDate = null;
        if (request.getParameter(Entity.MODIFICATION_DATE) != null) {
            try {
                lastModificationDate = DateUtils.isoToDate(request.getParameter(Entity.MODIFICATION_DATE));
            }
            catch (ParseException e) { } // Date not set, too bad.
        }
        int maximumResults = request.getParameter("maximumResults") == null ? 0 : Integer.valueOf(request.getParameter("maximumResults"));

        // Fetch the map with the query parameters
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, consumerKey);
        parameters.put(Demand.STATE_COMMAND_LIST, Boolean.TRUE);
        if (lastModificationDate != null) {
            parameters.put(">" + Entity.MODIFICATION_DATE, lastModificationDate);
        }

        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        // Dispatch the call per Entity
        JsonObject out = new GenericJsonObject();
        if ("/Demand".equals(pathInfo)) {
            out.put("success", true);
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                // new DemandRestlet().selectResources(null, buildMockOpenIdUser(consumerKey));
                List<Demand> demands = demandOperations.getDemands(pm, parameters, maximumResults);
                out.put("resources", JsonUtils.toJson((List<?>) demands));
                if (Boolean.valueOf(request.getParameter("includeLocaleCodes")) && 0 < demands.size()) {
                    List<Long> locationKeys = new ArrayList<Long>();
                    for (Demand demand: demands) {
                        Long locationKey = demand.getLocationKey();
                        if (locationKey != null && !locationKeys.contains(locationKey)) {
                            locationKeys.add(locationKey);
                        }
                    }
                    List<Location> locations = locationOperations.getLocations(pm, locationKeys);
                    out.put("relatedResources", JsonUtils.toJson((List<?>) locations));
                }
            }
            catch(Exception ex) {
                out.put("success", false);
                out.put("reason", ex.getMessage());
                ex.printStackTrace();
            }
            finally {
                pm.close();
            }
        }
        else if (Pattern.matches("/Proposal/(\\w+)", pathInfo)) {
            out.put("success", true);
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                Matcher keyMatcher = Pattern.compile("/Proposal/(\\w+)").matcher(pathInfo);
                keyMatcher.matches();
                Long key = Long.valueOf(keyMatcher.group(1));
                Proposal proposal = proposalOperations.getProposal(pm, key, null, null);
                demandOperations.getDemand(pm, proposal.getDemandKey(), consumerKey); // To verify the right to see the Proposal
                out.put("resource", proposal.toJson());
                Store store = storeOperations.getStore(pm, proposal.getStoreKey());
                out.put("relatedResource", store.toJson());
            }
            catch(Exception ex) {
                out.put("success", false);
                out.put("reason", ex.getMessage());
                ex.printStackTrace();
            }
            finally {
                pm.close();
            }
        }
        else {
            out.put("success", false);
            out.put("reason", "URL not supported");
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Long consumerKey = null;
        if (request.getParameter(Command.OWNER_KEY) != null) {
            consumerKey = Long.valueOf(request.getParameter(Command.OWNER_KEY));
        }
        if (consumerKey == null || consumerKey == 0L) {
            throw new IllegalArgumentException("Invalid owner Key");
        }

        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        if ("/Demand".equals(pathInfo)) {
            out.put("success", true);
            JsonObject in = null;
            try {
                // TODO: verify Content-type == "application/json"
                in = new JsonParser(request.getInputStream()).getJsonObject();

                // Create the Demand
                in.put(Command.SOURCE, Source.api.toString());
                Demand demand = demandOperations.createDemand(in, consumerKey);
                out.put("resource", demand.toJson());

                // Create a task for that demand validation
                Queue queue = CommandProcessor._baseOperations.getQueue();
                log.warning("Preparing the task: /maezel/validateOpenDemand?key=" + demand.getKey().toString());
                queue.add(
                        url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenDemand").
                            param(Proposal.KEY, demand.getKey().toString()).
                            method(Method.GET)
                );
            }
            catch(Exception ex) {
                out.put("success", false);
                out.put("reason", ex.getMessage());
                ex.printStackTrace();
            }
        }
        else {
            out.put("success", false);
            out.put("reason", "URL not supported");
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Long consumerKey = null;
        if (request.getParameter(Command.OWNER_KEY) != null) {
            consumerKey = Long.valueOf(request.getParameter(Command.OWNER_KEY));
        }
        if (consumerKey == null || consumerKey == 0L) {
            throw new IllegalArgumentException("Invalid owner Key");
        }

        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        if (pathInfo == null || pathInfo.length() == 0) {
            out.put("success", false);
            out.put("reason", "URL not supported");
        }
        else if (Pattern.matches("/Demand/(\\w+)", pathInfo)) {
            out.put("success", true);
            JsonObject in = null;
            try {
                // TODO: verify Content-type == "application/json"
                in = new JsonParser(request.getInputStream()).getJsonObject();

                // Get the key
                Matcher keyMatcher = Pattern.compile("/Demand/(\\w+)").matcher(pathInfo);
                keyMatcher.matches();
                Long key = Long.valueOf(keyMatcher.group(1));
                if (!in.containsKey(Entity.KEY) || !key.equals(in.getLong(Entity.KEY))) {
                    throw new IllegalArgumentException("Incomplete parameters");
                }

                // Update the Demand
                in.put(Command.SOURCE, Source.api.toString());
                Demand demand = demandOperations.updateDemand(in, consumerKey);
                out.put("resource", demand.toJson());

                // Create a task for that demand validation
                Queue queue = CommandProcessor._baseOperations.getQueue();
                log.warning("Preparing the task: /maezel/validateOpenDemand?key=" + demand.getKey().toString());
                queue.add(
                        url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenDemand").
                            param(Proposal.KEY, demand.getKey().toString()).
                            method(Method.GET)
                );
            }
            catch(Exception ex) {
                out.put("success", false);
                out.put("reason", ex.getMessage());
                ex.printStackTrace();
            }
        }
        else {
            out.put("success", false);
            out.put("reason", "URL not supported");
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Long consumerKey = null;
        if (request.getParameter(Command.OWNER_KEY) != null) {
            consumerKey = Long.valueOf(request.getParameter(Command.OWNER_KEY));
        }
        if (consumerKey == null || consumerKey == 0L) {
            throw new IllegalArgumentException("Invalid owner Key");
        }

        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        if (pathInfo == null || pathInfo.length() == 0) {
            out.put("success", false);
            out.put("reason", "URL not supported");
        }
        else if (Pattern.matches("/Demand/(\\w+)", pathInfo)) {
            out.put("success", true);
            try {
                // Get the key
                Matcher keyMatcher = Pattern.compile("/Demand/(\\w+)").matcher(pathInfo);
                keyMatcher.matches();
                Long key = Long.valueOf(keyMatcher.group(1));

                // Delete the Demand => Cancel it!
                PersistenceManager pm = _baseOperations.getPersistenceManager();
                try {
                    Demand demand = demandOperations.getDemand(pm, key, consumerKey);
                    demand.setState(State.cancelled);
                    demandOperations.updateDemand(pm, demand);
                }
                finally {
                    pm.close();
                }
                out.put("resourceId", key);
            }
            catch(Exception ex) {
                out.put("success", false);
                out.put("reason", ex.getMessage());
                ex.printStackTrace();
            }
        }
        else {
            out.put("success", false);
            out.put("reason", "URL not supported");
        }

        out.toStream(response.getOutputStream(), false);
    }
}
