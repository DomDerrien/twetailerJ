package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.Store;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
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

    public Logger getLogger() { return log; }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws InvalidIdentifierException {
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
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws InvalidIdentifierException, DataSourceException {
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

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Store store = StoreSteps.createStore(
                    pm,
                    parameters,
                    LoginServlet.getConsumer(loggedUser, pm),
                    LoginServlet.getSaleAssociate(loggedUser, pm),
                    isUserAdmin
            );
            return store.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, NumberFormatException, ReservedOperationException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Store store = StoreSteps.updateStore(
                    pm,
                    Long.valueOf(resourceId),
                    parameters,
                    LoginServlet.getConsumer(loggedUser, pm),
                    LoginServlet.getSaleAssociate(loggedUser, pm),
                    isUserAdmin
            );
            return store.toJson();
        }
        finally {
            pm.close();
        }
    }

    /**** Dom: refactoring limit ***/

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        if (isUserAdmin) {
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
}
