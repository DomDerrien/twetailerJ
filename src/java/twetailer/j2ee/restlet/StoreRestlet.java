package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.j2ee.BaseRestlet;
import twetailer.validator.LocaleValidator;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

@SuppressWarnings("serial")
public class StoreRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(StoreRestlet.class.getName());

    protected static SaleAssociateRestlet saleAssociateRestlet = new SaleAssociateRestlet();

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
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
                Store store = storeOperations.createStore(pm, parameters);
                Location location = locationOperations.getLocation(pm, store.getLocationKey());
                if (Boolean.FALSE.equals(location.hasStore())) {
                    location.setHasStore(Boolean.TRUE);
                    locationOperations.updateLocation(pm, location);
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
            PersistenceManager pm = _baseOperations.getPersistenceManager();
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
    protected void delegateResourceDeletion(PersistenceManager pm, Long storeKey) throws DataSourceException{
        // Delete the store account
        Store store = storeOperations.getStore(pm, storeKey);
        storeOperations.deleteStore(pm, store);
        // Delete attached sale associates
        List<Long> saleAssociateKeys = saleAssociateOperations.getSaleAssociateKeys(pm, SaleAssociate.STORE_KEY, storeKey, 0);
        for (Long key: saleAssociateKeys) {
            saleAssociateRestlet.delegateResourceDeletion(pm, key);
        }
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        return storeOperations.getStore(Long.valueOf(resourceId)).toJson();
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
        if (parameters.containsKey(Store.LOCATION_KEY)) {
            Long locationKey = parameters.getLong(Store.LOCATION_KEY);
            Double range = !parameters.containsKey(Demand.RANGE) ? 25.0D : parameters.getDouble(Demand.RANGE);
            String rangeUnit = !parameters.containsKey(Demand.RANGE_UNIT) ? LocaleValidator.DEFAULT_RANGE_UNIT : LocaleValidator.checkRangeUnit(parameters.getString(Demand.RANGE_UNIT));

            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                List<Location> locations = locationOperations.getLocations(pm, locationOperations.getLocation(pm, locationKey), range, rangeUnit, true, 100);
                List<Store> stores = storeOperations.getStores(locations, 100);
                return JsonUtils.toJson(stores);
            }
            finally {
                pm.close();
            }
        }

        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
