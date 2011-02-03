package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.dto.SaleAssociate;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.SaleAssociateSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the SaleAssociate entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class SaleAssociateRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(SaleAssociateRestlet.class.getName());

    public Logger getLogger() { return log; }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            SaleAssociate saleAssociate = null;
            if ("current".equals(resourceId)) {
                // Get the sale associate
                saleAssociate = LoginServlet.getSaleAssociate(loggedUser, pm);
                if (saleAssociate == null) {
                    throw new ReservedOperationException("Current user is not a Sale Associate!");
                }
            }
            else if (isUserAdmin) {
                saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, Long.valueOf(resourceId));
            }
            else {
                // TODO: enable a store manager to get information about the sale associates for the store
                throw new ReservedOperationException("Restricted access!");
            }

            return saleAssociate.toJson();
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
            if (!isUserAdmin) {
                SaleAssociate associate = LoginServlet.getSaleAssociate(loggedUser, pm);
                if (!associate.isStoreAdmin()) {
                    throw new ReservedOperationException("Restricted access!");
                }
                parameters.put(SaleAssociate.STORE_KEY, associate.getStoreKey()); // LoginServlet.getStoreKey(loggedUser, pm);
            }

            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) SaleAssociateSteps.getSaleAssociateKeys(pm, parameters));
            }
            else { // full detail
                // Get the demands
                resources = JsonUtils.toJson(SaleAssociateSteps.getSaleAssociates(pm, parameters));
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
            Long saleAssociateKey, storeKey = null;
            if (isUserAdmin) {
                if (!parameters.containsKey(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY)) {
                    throw new ReservedOperationException("Restricted access!");
                }
                saleAssociateKey = parameters.getLong(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY);
            }
            else {
                SaleAssociate updater = LoginServlet.getSaleAssociate(loggedUser, pm);
                if (!updater.isStoreAdmin()) {
                    throw new ReservedOperationException("Restricted access!");
                }
                saleAssociateKey = updater.getKey();
                storeKey = updater.getStoreKey();
            }

            SaleAssociate saleAssociate = SaleAssociateSteps.createSaleAssociate(
                    pm,
                    parameters,
                    saleAssociateKey,
                    storeKey,
                    isUserAdmin
            );
            return saleAssociate.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long saleAssociateKey = null;
            if ("current".equals(resourceId)) {
                // Get the sale associate
                saleAssociateKey = LoginServlet.getSaleAssociateKey(loggedUser, pm);
                if (saleAssociateKey == null) {
                    throw new ReservedOperationException("Current user is not a Sale Associate!");
                }
            }
            else if (isUserAdmin) {
                saleAssociateKey = Long.valueOf(resourceId);
            }
            else {
                SaleAssociate updater = LoginServlet.getSaleAssociate(loggedUser, pm);
                if (!updater.isStoreAdmin()) {
                    throw new ReservedOperationException("Restricted access!");
                }
                saleAssociateKey = Long.valueOf(resourceId);
                SaleAssociate toUpdate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, saleAssociateKey);
                if (!updater.getStoreKey().equals(toUpdate.getStoreKey())) {
                    throw new ReservedOperationException("Restricted access!");
                }
            }

            SaleAssociate saleAssociate = SaleAssociateSteps.updateSaleAssociate(
                    pm,
                    saleAssociateKey,
                    parameters,
                    isUserAdmin
            );
            return saleAssociate.toJson();
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
            Long saleAssociateKey = Long.valueOf(resourceId);

            SaleAssociateSteps.deleteSaleAssociate(
                    pm,
                    saleAssociateKey,
                    isUserAdmin
            );
        }
        finally {
            pm.close();
        }
    }
}
