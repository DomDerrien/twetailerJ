package twetailer.j2ee.restlet;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SeedOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Seed;
import twetailer.j2ee.BaseRestlet;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class SeedRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(StoreRestlet.class.getName());

    protected static SaleAssociateRestlet saleAssociateRestlet = new SaleAssociateRestlet();

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    protected static SeedOperations seedOperations = _baseOperations.getSeedOperations();
    protected static StoreOperations storeOperations = _baseOperations.getStoreOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                Seed seed = null;
                try {
                    seed = seedOperations.getSeed(pm, parameters.getString(Seed.COUNTRY), parameters.getString(Seed.REGION), parameters.getString(Seed.CITY));
                    seed.fromJson(parameters);
                    seed = seedOperations.updateSeed(pm, seed);
                }
                catch(DataSourceException ex) {}
                if (seed == null) {
                    seed = seedOperations.createSeed(pm, new Seed(parameters));
                }
                return seed.toJson();
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
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
