package twetailer.task.step;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.j2ee.BaseRestlet;
import twetailer.validator.LocaleValidator;
import domderrien.jsontools.JsonObject;

public class LocationSteps extends BaseSteps {

    /**
     * Utility method extracting the Location information attached to the specified entity
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param entity One of the entity managed by Twetailer
     * @return Identified location or <code>null</code>
     *
     * @throws InvalidIdentifierException If the resource retrieval on the back-end fails
     */
    public static Location getLocation(PersistenceManager pm, Entity entity) throws InvalidIdentifierException {
        if (entity.getLocationKey() == null) {
            return null;
        }
        return getLocation(pm, entity.getLocationKey());
    }

    /**
     * Utility method extracting the Location information
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param locationKey Identifier of the demand to retrieve
     * @return Identified location
     *
     * @throws InvalidIdentifierException If the resource retrieval on the back-end fails
     */
    public static Location getLocation(PersistenceManager pm, Long locationKey) throws InvalidIdentifierException {
        return getLocationOperations().getLocation(pm, locationKey);
    }

    /**
     * Utility method extracting the selected Location information
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Filters of the selection
     * @param withBounds if <code>true</code>, all locations in the specified are searched, otherwise it's limited to the center
     * @return List of matching locations, can be empty
     *
     * @throws InvalidIdentifierException If the resource retreival on the back-end fails
     * @throws DataSourceException If the resource selection on the back-end fails
     */
    public static List<Location> getLocations(PersistenceManager pm, JsonObject parameters, boolean withBounds) throws InvalidIdentifierException, DataSourceException {

        // Map<String, Object> queryParameters = prepareQueryForSelection(parameters); // Parameters processed individually
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        // Get the center
        Location center = null;
        if (parameters.containsKey(Location.LOCATION_KEY)) {
            center = getLocation(pm, parameters.getLong(Location.LOCATION_KEY));
        }
        else if (parameters.containsKey(Location.POSTAL_CODE) && parameters.containsKey(Location.COUNTRY_CODE)) {
            String postalCode = LocaleValidator.standardizePostalCode(parameters.getString(Location.POSTAL_CODE));
            String countryCode = LocaleValidator.checkCountryCode(parameters.getString(Location.COUNTRY_CODE));
            List<Location> possibleCenters = getLocationOperations().getLocations(pm, postalCode, countryCode);
            if (possibleCenters.size() == 0) {
                parameters.put(Location.POSTAL_CODE, postalCode); // Inject normalize postal code
                center = getLocationOperations().createLocation(pm, parameters);
            }
            else {
                center = possibleCenters.get(0);
            }
        }
        else if (parameters.containsKey(Location.LATITUDE) && parameters.containsKey(Location.LONGITUDE) && parameters.containsKey(Location.COUNTRY_CODE)) {
            center = new Location(); // Transient entity
            center.setCountryCode(parameters.getString(Location.COUNTRY_CODE));
            center.setLatitude(parameters.getDouble(Location.LATITUDE));
            center.setLongitude(parameters.getDouble(Location.LONGITUDE));
        }

        // Get location around
        List<Location> output = null;
        if (center == null) {
            output = new ArrayList<Location>();
        }
        else if (!withBounds || !parameters.containsKey(Demand.RANGE)) {
            output = new ArrayList<Location>(1);
            output.add(center);
        }
        else {
            Double range = parameters.getDouble(Demand.RANGE);
            String rangeUnit = parameters.getString(Demand.RANGE_UNIT);
            boolean hasStore = !parameters.containsKey(Location.HAS_STORE) || parameters.getBoolean(Location.HAS_STORE);

            output = getLocationOperations().getLocations(pm, center, range, rangeUnit, hasStore, maximumResults);
            output.add(center);
        }

        return output;
    }

    public static Location updateLocation(PersistenceManager pm, Long locationKey, JsonObject parameters, boolean isUserAdmin) throws InvalidIdentifierException {

        // Get identified location
        Location location = getLocationOperations().getLocation(pm, locationKey);

        // Merge updates and persist them
        location.fromJson(parameters, isUserAdmin);
        location = getLocationOperations().updateLocation(pm, location);

        return location;
    }
}
