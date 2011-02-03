package twetailer.j2ee.restlet;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.Location;
import twetailer.j2ee.BaseRestlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.LocationSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
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
    private static Logger log = Logger.getLogger(LocationRestlet.class.getName());

    public Logger getLogger() { return log; }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws InvalidIdentifierException {
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
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);
            boolean centerOnly = parameters.containsKey(BaseRestlet.CENTER_ONLY_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // As the selection needs the entire Location instances to filter within a geo-box,
                // the corresponding instances are asked even if only their identifiers is forwarded to the users
                resources = new GenericJsonArray();
                for (Location location: LocationSteps.getLocations(pm, parameters, !centerOnly)) {
                    resources.add(location.getKey());
                }
            }
            else { // full detail
                // Get the locations
                resources = JsonUtils.toJson(LocationSteps.getLocations(pm, parameters, !centerOnly));
            }
            return resources;
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        return BaseSteps.getLocationOperations().createLocation(parameters).toJson();
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ReservedOperationException, NumberFormatException, InvalidIdentifierException {
        if (!isUserAdmin) {
            throw new ReservedOperationException("Only Admins can update Location records!");
        }

        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Location location = LocationSteps.updateLocation(
                    pm,
                    Long.valueOf(resourceId),
                    parameters,
                    isUserAdmin
            );
            return location.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
