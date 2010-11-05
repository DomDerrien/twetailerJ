package twetailer.j2ee.restlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.DemandSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the Demand entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class DemandRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(DemandRestlet.class.getName());

    public Logger getLogger() { return log; }


    @Override
    public JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long demandKey = Long.valueOf(resourceId);
            Long ownerKey = LoginServlet.getConsumerKey(loggedUser);
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.CONSUMER);
            Long saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? LoginServlet.getSaleAssociateKey(loggedUser, pm) : null;
            Demand demand = DemandSteps.getDemand(pm, demandKey, ownerKey, pointOfView, saleAssociateKey);

            JsonObject out = DemandSteps.anonymizeDemand(pm, pointOfView, demand.toJson(), saleAssociateKey);

            if (parameters.containsKey(RELATED_RESOURCE_NAMES)) {
                JsonArray relatedResourceNames = parameters.getJsonArray(RELATED_RESOURCE_NAMES);
                JsonObject relatedResources = new GenericJsonObject();
                int idx = relatedResourceNames.size();
                while (0 < idx) {
                    --idx;
                    String relatedResourceName = relatedResourceNames.getString(idx);
                    if (Location.class.getName().contains(relatedResourceName)) {
                        Location location = BaseSteps.getLocationOperations().getLocation(pm, demand.getLocationKey());
                        relatedResources.put(relatedResourceName, location.toJson());
                    }
                }
                if (0 < relatedResources.size()) {
                    out.put(RELATED_RESOURCE_NAMES, relatedResources);
                }
            }

            return out;
        }
        finally {
            pm.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long ownerKey = LoginServlet.getConsumerKey(loggedUser);
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.CONSUMER);
            Long saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? LoginServlet.getSaleAssociateKey(loggedUser, pm) : null;
            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) DemandSteps.getDemandKeys(pm, parameters, ownerKey, pointOfView, saleAssociateKey));
            }
            else { // full detail
                // Get the demands
                List<Demand> demands = DemandSteps.getDemands(pm, parameters, ownerKey, pointOfView, saleAssociateKey);
                resources = JsonUtils.toJson(demands);
                resources = DemandSteps.anonymizeDemands(pointOfView, resources, saleAssociateKey);

                if (parameters.containsKey(RELATED_RESOURCE_NAMES) && 0 < demands.size()) {
                    JsonArray relatedResourceNames = parameters.getJsonArray(RELATED_RESOURCE_NAMES);
                    JsonObject relatedResources = new GenericJsonObject();
                    int idx = relatedResourceNames.size();
                    while (0 < idx) {
                        --idx;
                        String relatedResourceName = relatedResourceNames.getString(idx);
                        if (Location.class.getName().contains(relatedResourceName)) {
                            List<Long> locationKeys = new ArrayList<Long>();
                            for(int i=0; i<demands.size(); i++) {
                                Long locationKey = demands.get(i).getLocationKey();
                                if (!locationKeys.contains(locationKey)) {
                                    locationKeys.add(locationKey);
                                }
                            }
                            List<Location> locations = BaseSteps.getLocationOperations().getLocations(pm, locationKeys);
                            relatedResources.put(relatedResourceName, JsonUtils.toJson(locations));
                        }
                    }
                    if (0 < relatedResources.size()) {
                        resources.getJsonObject(0).put(RELATED_RESOURCE_NAMES, relatedResources);
                    }
                }
            }
            return resources;
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Create the Demand
            parameters.put(Command.SOURCE, Source.api.toString());

            Demand demand = DemandSteps.createDemand(pm, parameters, LoginServlet.getConsumer(loggedUser, pm));

            return demand.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Update the Demand
            Long demandKey = Long.valueOf(resourceId);
            Demand demand = DemandSteps.updateDemand(pm, null, demandKey, parameters, LoginServlet.getConsumer(loggedUser, pm));

            return demand.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Update the Demand
            Long demandKey = Long.valueOf(resourceId);
            DemandSteps.deleteDemand(pm, demandKey, LoginServlet.getConsumer(loggedUser, pm));
        }
        finally {
            pm.close();
        }
    }
}
