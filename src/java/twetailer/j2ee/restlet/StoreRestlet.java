package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.SaleAssociate;
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

    @Override
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
        if (!isUserAdmin) {
            throw new ReservedOperationException("Only Admins can create Store records!");
        }

        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            return StoreSteps.createStore(pm, parameters, isUserAdmin).toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, NumberFormatException, ReservedOperationException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long storeKey = Long.valueOf(resourceId);

            if (!isUserAdmin) {
                SaleAssociate saleAssociate = LoginServlet.getSaleAssociate(loggedUser, pm);
                if (!saleAssociate.getStoreKey().equals(storeKey)) {
                    throw new ReservedOperationException("Only Admins can create Store records!");
                }
            }

            Store store = StoreSteps.updateStore(
                    pm,
                    storeKey,
                    parameters,
                    isUserAdmin
            );
            return store.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        if (!isUserAdmin) {
            throw new ReservedOperationException("Restricted access!");
        }

        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long storeKey = Long.valueOf(resourceId);
            StoreSteps.deleteStore(pm, storeKey, isUserAdmin);
        }
        finally {
            pm.close();
        }
    }
}
