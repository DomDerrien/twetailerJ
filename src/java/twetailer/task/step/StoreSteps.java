package twetailer.task.step;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.j2ee.BaseRestlet;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonObject;

public class StoreSteps extends BaseSteps {

    public static Store getStore(PersistenceManager pm, Long storeKey) throws InvalidIdentifierException {
        return getStoreOperations().getStore(pm, storeKey);
    }

    public static List<Store> getStores(PersistenceManager pm, JsonObject parameters) throws InvalidIdentifierException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Location> locations = LocationSteps.getLocations(pm, parameters, true);

        return getStoreOperations().getStores(pm, queryParameters, locations, maximumResults);
    }

    public static List<Long> getStoreKeys(PersistenceManager pm, JsonObject parameters) throws InvalidIdentifierException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Location> locations = LocationSteps.getLocations(pm, parameters, true);

        return getStoreOperations().getStoreKeys(pm, queryParameters, locations, maximumResults);
    }

    /**
     * Remove nominative information from the Store record
     *
     * @param store Entity to purge
     * @return Cleaned up Store instance
     */
    public static JsonObject anonymizeStore(JsonObject store) {
        return store;
    }

    /**
     * Helper fetching a list of parameters for a query
     *
     * @param parameters bag of parameters proposed by a connector
     * @return Prefetch list of query parameters
     */
    protected static Map<String, Object> prepareQueryForSelection(JsonObject parameters) {
        Map<String, Object> queryFilters = new HashMap<String, Object>();

        Date lastModificationDate = null;
        if (parameters.containsKey(Entity.MODIFICATION_DATE)) {
            try {
                lastModificationDate = DateUtils.isoToDate(parameters.getString(Entity.MODIFICATION_DATE));
                queryFilters.put(">" + Entity.MODIFICATION_DATE, lastModificationDate);
            }
            catch (ParseException e) { } // Date not set, too bad.
        }

        return queryFilters;
    }

    public static Store createStore(PersistenceManager pm, JsonObject parameters, Consumer loggedConsumer, SaleAssociate loggedSaleAssociate, boolean isPrivileged) throws ClientException {
        // Verify the logged user rights
        if (!isPrivileged) {
            throw new ReservedOperationException("Store instances can only be created by admins");
        }
        if (!parameters.containsKey(Store.REGISTRAR_KEY)) {
            throw new ClientException("Missing registrar key");
        }

        Store store = getStoreOperations().createStore(pm, parameters);

        Location location = getLocationOperations().getLocation(pm, store.getLocationKey());
        if (Boolean.FALSE.equals(location.hasStore())) {
            location.setHasStore(Boolean.TRUE);
            getLocationOperations().updateLocation(pm, location);
        }

        return store;
    }

    public static Store updateStore(PersistenceManager pm, Long storeKey, JsonObject parameters, Consumer loggedConsumer, SaleAssociate loggedSaleAssociate, boolean isPrivileged) throws ReservedOperationException, InvalidIdentifierException {
        // Verify the logged user rights
        if (!isPrivileged && !loggedSaleAssociate.isStoreAdmin() && !loggedSaleAssociate.getStoreKey().equals(storeKey)) {
            throw new ReservedOperationException("Store instances can only be created by Store admins");
        }
        if (!isPrivileged) {
            // Prevent any unexpected update
            parameters.remove(Store.REGISTRAR_KEY);
        }

        Store store = getStoreOperations().getStore(pm, storeKey);
        Long initialLocationKey = store.getLocationKey();

        store.fromJson(parameters);
        store = getStoreOperations().updateStore(pm, store);

        Long newLocationKey = store.getLocationKey();
        if (!newLocationKey.equals(initialLocationKey)) {
            Location location = getLocationOperations().getLocation(pm, newLocationKey);
            if (Boolean.FALSE.equals(location.hasStore())) {
                location.setHasStore(Boolean.TRUE);
                getLocationOperations().updateLocation(pm, location);
            }
        }

        return store;
    }
}
