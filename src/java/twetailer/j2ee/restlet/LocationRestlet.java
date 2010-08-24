package twetailer.j2ee.restlet;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.j2ee.BaseRestlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.LocationSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the Location entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class LocationRestlet extends BaseRestlet {

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long locationKey = Long.valueOf(resourceId);

            return LocationSteps.getLocation(pm, locationKey).toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                throw new ClientException("Not implemented!");
            }
            else { // full detail
                // Get the locations
                resources = JsonUtils.toJson(LocationSteps.getLocations(pm, parameters, true));
            }
            return resources;
        }
        finally {
            pm.close();
        }
    }

    /**** Dom: refactoring limit ***/

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        return BaseSteps.getLocationOperations().createLocation(parameters).toJson();
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
