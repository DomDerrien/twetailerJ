package twetailer.j2ee.restlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Location;
import twetailer.dto.Wish;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.WishSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the Wish entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class WishRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(WishRestlet.class.getName());

    public Logger getLogger() { return log; }


    @Override
    public JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.CONSUMER);
            Long ownerKey;
            if (isUserAdmin) {
                if (!QueryPointOfView.CONSUMER.equals(pointOfView) || !parameters.containsKey(BaseRestlet.ON_BEHALF_CONSUMER_KEY)) {
                    throw new IllegalArgumentException("Missing one of the identity identifiers!");
                }
                ownerKey = parameters.getLong(BaseRestlet.ON_BEHALF_CONSUMER_KEY);
            }
            else {
                ownerKey = LoginServlet.getConsumerKey(loggedUser);
            }

            Long wishKey = Long.valueOf(resourceId);
            Wish wish = WishSteps.getWish(pm, wishKey, ownerKey, pointOfView);

            JsonObject out = WishSteps.anonymizeWish(pointOfView, wish.toJson());

            if (parameters.containsKey(RELATED_RESOURCES_ENTRY_POINT_KEY)) {
                JsonArray relatedResourceNames = parameters.getJsonArray(RELATED_RESOURCES_ENTRY_POINT_KEY);
                JsonObject relatedResources = new GenericJsonObject();
                int idx = relatedResourceNames.size();
                while (0 < idx) {
                    --idx;
                    String relatedResourceName = relatedResourceNames.getString(idx);
                    if (Location.class.getName().contains(relatedResourceName)) {
                        Location location = BaseSteps.getLocationOperations().getLocation(pm, wish.getLocationKey());
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
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.CONSUMER);
            Long ownerKey;
            if (isUserAdmin) {
                if (!QueryPointOfView.CONSUMER.equals(pointOfView) || !parameters.containsKey(BaseRestlet.ON_BEHALF_CONSUMER_KEY)) {
                    throw new IllegalArgumentException("Missing one of the identity identifiers!");
                }
                ownerKey = parameters.getLong(BaseRestlet.ON_BEHALF_CONSUMER_KEY);
            }
            else {
                ownerKey = LoginServlet.getConsumerKey(loggedUser);
            }

            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) WishSteps.getWishKeys(pm, parameters, ownerKey, pointOfView));
            }
            else { // full detail
                // Get the wishes
                List<Wish> wishes = WishSteps.getWishes(pm, parameters, ownerKey, pointOfView);
                resources = JsonUtils.toJson(wishes);
                resources = WishSteps.anonymizeWishes(pointOfView, resources);

                if (parameters.containsKey(RELATED_RESOURCES_ENTRY_POINT_KEY) && 0 < wishes.size()) {
                    JsonArray relatedResourceNames = parameters.getJsonArray(RELATED_RESOURCES_ENTRY_POINT_KEY);
                    JsonObject relatedResources = new GenericJsonObject();
                    int idx = relatedResourceNames.size();
                    while (0 < idx) {
                        --idx;
                        String relatedResourceName = relatedResourceNames.getString(idx);
                        if (Location.class.getName().contains(relatedResourceName)) {
                            List<Long> locationKeys = new ArrayList<Long>();
                            for(int i=0; i<wishes.size(); i++) {
                                Long locationKey = wishes.get(i).getLocationKey();
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
            // Create the Wish
            parameters.put(Command.SOURCE, Source.api.toString());

            Wish wish = WishSteps.createWish(pm, parameters, LoginServlet.getConsumer(loggedUser, pm));

            return wish.toJson();
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

            // Update the Wish
            Long wishKey = Long.valueOf(resourceId);
            Wish wish = WishSteps.updateWish(pm, null, wishKey, parameters, ownerKey);

            return wish.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Update the Wish
            Long wishKey = Long.valueOf(resourceId);
            WishSteps.deleteWish(pm, wishKey, LoginServlet.getConsumer(loggedUser, pm));
        }
        finally {
            pm.close();
        }
    }
}
