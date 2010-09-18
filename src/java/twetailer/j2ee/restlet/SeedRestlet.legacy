package twetailer.j2ee.restlet;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.Seed;
import twetailer.j2ee.BaseRestlet;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.Action;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

/**
 * Restlet entry point for the Seed entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class SeedRestlet extends BaseRestlet {

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws InvalidIdentifierException, ReservedOperationException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                Seed seed = null;
                try {
                    seed = BaseSteps.getSeedOperations().getSeed(pm, parameters.getString(Seed.COUNTRY), parameters.getString(Seed.REGION), parameters.getString(Seed.CITY));
                    seed.fromJson(parameters);
                    seed = BaseSteps.getSeedOperations().updateSeed(pm, seed);
                }
                catch(InvalidIdentifierException ex) {}
                if (seed == null) {
                    seed = BaseSteps.getSeedOperations().createSeed(pm, new Seed(parameters));
                }
                return seed.toJson();
            }
            finally {
                pm.close();
            }
        }
        throw new ReservedOperationException(Action.list, "Restricted access!");
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        throw new ClientException("Restricted access!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
