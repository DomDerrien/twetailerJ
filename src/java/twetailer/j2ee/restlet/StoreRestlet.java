package twetailer.j2ee.restlet;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Demand;
import twetailer.dto.Location;
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

    protected BaseOperations _baseOperations = new BaseOperations();
    protected LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected StoreOperations storeOperations = _baseOperations.getStoreOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (loggedUser.getAttribute("info") != null) {
            Map<String, String> info = (Map<String, String>) loggedUser.getAttribute("info");
            if (info.get("email") != null) {
                String email = info.get("email");
                if ("dominique.derrien@gmail.com".equals(email) || "steven.milstein@gmail.com".equals(email)) {
                    return storeOperations.createStore(parameters).toJson();
                }
            }
        }
        throw new ClientException("Restricted access!");
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
        if (parameters.containsKey(Store.LOCATION_KEY)) {
            Long locationKey = parameters.getLong(Store.LOCATION_KEY);
            Double range = !parameters.containsKey(Demand.RANGE) ? 25.0D : parameters.getDouble(Demand.RANGE);
            String rangeUnit = !parameters.containsKey(Demand.RANGE_UNIT) ? LocaleValidator.DEFAULT_RANGE_UNIT : LocaleValidator.checkRangeUnit(parameters.getString(Demand.RANGE_UNIT));

            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                List<Location> locations = locationOperations.getLocations(pm, locationOperations.getLocation(pm, locationKey), range, rangeUnit, 100);
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
