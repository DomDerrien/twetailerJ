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
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.CONSUMER);
            Long ownerKey, saleAssociateKey;
            if (isUserAdmin) {
                if (QueryPointOfView.CONSUMER.equals(pointOfView) && !parameters.containsKey(BaseRestlet.ON_BEHALF_CONSUMER_KEY) ||
                    QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) && !parameters.containsKey(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY)
                ) {
                    throw new IllegalArgumentException("Missing one of the identity identifiers!");
                }
                ownerKey = parameters.getLong(BaseRestlet.ON_BEHALF_CONSUMER_KEY);
                saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? parameters.getLong(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY) : null;
            }
            else {
                ownerKey = LoginServlet.getConsumerKey(loggedUser);
                saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? LoginServlet.getSaleAssociateKey(loggedUser, pm) : null;
            }
            Long demandKey = Long.valueOf(resourceId);
            Demand demand = DemandSteps.getDemand(pm, demandKey, ownerKey, pointOfView, saleAssociateKey);

            JsonObject out = isUserAdmin ? demand.toJson() : DemandSteps.anonymizeDemand(pm, pointOfView, demand.toJson(), saleAssociateKey);

            if (parameters.containsKey(RELATED_RESOURCES_ENTRY_POINT_KEY)) {
                JsonArray relatedResourceNames = parameters.getJsonArray(RELATED_RESOURCES_ENTRY_POINT_KEY);
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
                    out.put(RELATED_RESOURCES_ENTRY_POINT_KEY, relatedResources);
                }
            }

            return out;
        }
        finally {
            pm.close();
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.CONSUMER);
            Long ownerKey, saleAssociateKey;
            if (isUserAdmin) {
                if (QueryPointOfView.CONSUMER.equals(pointOfView) && !parameters.containsKey(BaseRestlet.ON_BEHALF_CONSUMER_KEY) ||
                    QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) && !parameters.containsKey(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY)
                ) {
                    throw new IllegalArgumentException("Missing one of the identity identifiers!");
                }
                ownerKey = parameters.getLong(BaseRestlet.ON_BEHALF_CONSUMER_KEY);
                saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? parameters.getLong(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY) : null;
            }
            else {
                ownerKey = LoginServlet.getConsumerKey(loggedUser);
                saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? LoginServlet.getSaleAssociateKey(loggedUser, pm) : null;
            }
            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) DemandSteps.getDemandKeys(pm, parameters, ownerKey, pointOfView, saleAssociateKey));
            }
            else { // full detail
                // Get the demands
                List<Demand> demands = DemandSteps.getDemands(pm, parameters, ownerKey, pointOfView, saleAssociateKey);
                resources = isUserAdmin ? JsonUtils.toJson(demands) : DemandSteps.anonymizeDemands(pointOfView, JsonUtils.toJson(demands), saleAssociateKey);

                if (parameters.containsKey(RELATED_RESOURCES_ENTRY_POINT_KEY) && 0 < demands.size()) {
                    JsonArray relatedResourceNames = parameters.getJsonArray(RELATED_RESOURCES_ENTRY_POINT_KEY);
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
                        resources.getJsonObject(0).put(RELATED_RESOURCES_ENTRY_POINT_KEY, relatedResources);
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
            // Create the Demand for the logged user
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
            // Get the demand owner key
            Long ownerKey = null;
            if (isUserAdmin) {
                if (!parameters.containsKey(BaseRestlet.ON_BEHALF_CONSUMER_KEY)) {
                    throw new IllegalArgumentException("Missing one of the identity identifiers!");
                }
                ownerKey = parameters.getLong(BaseRestlet.ON_BEHALF_CONSUMER_KEY);
            }
            else {
                ownerKey = LoginServlet.getConsumerKey(loggedUser);
            }

            // Update the Demand
            Long demandKey = Long.valueOf(resourceId);
            Demand demand = DemandSteps.updateDemand(pm, null, demandKey, parameters, ownerKey, isUserAdmin);

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
            DemandSteps.deleteDemand(pm, demandKey, LoginServlet.getConsumerKey(loggedUser));
        }
        finally {
            pm.close();
        }
    }
}
