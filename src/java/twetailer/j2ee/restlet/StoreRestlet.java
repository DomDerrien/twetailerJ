package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Location;
import twetailer.dto.Store;
import twetailer.j2ee.BaseRestlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.LocationSteps;
import twetailer.task.step.StoreSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the Store entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class StoreRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(StoreRestlet.class.getName());

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Store store = StoreSteps.getStore(pm, Long.valueOf(resourceId));
            return StoreSteps.anonymizeStore(store.toJson());
        }
        finally {
            pm.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws InvalidIdentifierException, DataSourceException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) StoreSteps.getStoreKeys(pm, parameters));
            }
            else { // full detail
                // Get the locations
                resources = JsonUtils.toJson(StoreSteps.getStores(pm, parameters));
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
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                Store store = BaseSteps.getStoreOperations().createStore(pm, parameters);
                Location location = LocationSteps.getLocation(pm, store);
                if (Boolean.FALSE.equals(location.hasStore())) {
                    location.setHasStore(Boolean.TRUE);
                    BaseSteps.getLocationOperations().updateLocation(pm, location);
                }
                return store.toJson();
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                Long storeKey = Long.valueOf(resourceId);
                delegateResourceDeletion(pm, storeKey);
                return;
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
    }

    /**
     * Delete the Store instances based on the specified criteria.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param storeKey Identifier of the resource to delete
     * @return Serialized list of the Store instances matching the given criteria

     * @throws DataSourceException If the query to the back-end fails
     *
     * @see SaleAssociateRestlet#delegateResourceDeletion(PersistenceManager, Long)
     */
    protected void delegateResourceDeletion(PersistenceManager pm, Long storeKey) throws InvalidIdentifierException{
        // Delete the store account
        Store store = BaseSteps.getStoreOperations().getStore(pm, storeKey);
        BaseSteps.getStoreOperations().deleteStore(pm, store);
        // Delete attached sale associates
//        List<Long> saleAssociateKeys = BaseSteps.getSaleAssociateOperations().getSaleAssociateKeys(pm, SaleAssociate.STORE_KEY, storeKey, 0);
//        for (Long key: saleAssociateKeys) {
//            new SaleAssociateRestlet().delegateResourceDeletion(pm, key);
//        }
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
