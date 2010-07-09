package twetailer.task.step;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Entity;
import twetailer.dto.Location;
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
        int maximumResults = (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY);

        List<Location> locations = LocationSteps.getLocations(pm, parameters, true);

        return getStoreOperations().getStores(pm, queryParameters, locations, maximumResults);
    }

    public static List<Long> getStoreKeys(PersistenceManager pm, JsonObject parameters) throws InvalidIdentifierException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY);

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

}
