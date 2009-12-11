package twetailer.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Location;
import twetailer.validator.LocaleValidator;
import domderrien.jsontools.JsonObject;

public class LocationOperations extends BaseOperations {
    private static Logger log = Logger.getLogger(LocationOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Location instance with the given parameters
     *
     * @param parameters HTTP location parameters
     * @return Just created resource
     *
     * @throws ClientException If mandatory attributes are missing
     *
     * @see LocationOperations#createLocation(Location)
     */
    public Location createLocation(JsonObject parameters) throws ClientException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createLocation(pm, parameters);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Location instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters HTTP location parameters
     * @return Just created resource
     *
     * @see LocationOperations#createLocation(PersistenceManager, Location)
     */
    public Location createLocation(PersistenceManager pm, JsonObject parameters) {
        getLogger().warning("Create location with: " + parameters.toString());
        // Creates new location record and persist it
        Location newLocation = new Location(parameters);
        // Persist it
        return createLocation(pm, newLocation);
    }

    /**
     * Create the Location instance with the given parameters
     *
     * @param location Resource to persist
     * @return Just created resource
     *
     * @see LocationOperations#createLocation(PersistenceManager, Location)
     */
    public Location createLocation(Location location) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createLocation(pm, location);
        }
        finally {
            pm.close();
        }
    }


    /**
     * Create the Location instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param location Resource to persist
     * @return Just created resource
     */
    public Location createLocation(PersistenceManager pm, Location location) {
        try {
            List<Location> locations = null;
            // Check the location attributes
            if (location.getPostalCode() != null) { // && location.getCountryCode() != null) { // A country code being automatically created, so no need to verify the null value
                // Try to retrieve from its postal and country codes
                locations = getLocations(pm, location.getPostalCode(), location.getCountryCode());
            }
            else if (location.getLatitude() != Location.INVALID_COORDINATE && location.getLongitude() != Location.INVALID_COORDINATE) {
                // Try to retrieve from the geo-coordinates
                locations = getLocations(pm, location.getLatitude(), location.getLongitude());
            }
            else {
                throw new IllegalArgumentException("Location object should have a valid pair of {postal; country} or {latitude; longitude}.");
            }
            if (0 < locations.size()) {
                return locations.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Create an entry for that new location
        pm.makePersistent(location);
        return location;
    }

    /**
     * Use the given key to get the corresponding Location instance
     *
     * @param key Identifier of the location
     * @return First location matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved location does not belong to the specified user
     *
     * @see LocationOperations#getLocation(PersistenceManager, Long)
     */
    public Location getLocation(Long key) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getLocation(pm, key);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Location instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the location
     * @return First location matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the location cannot be retrieved
     */
    public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new IllegalArgumentException("Invalid key; cannot retrieve the Location instance");
        }
        getLogger().warning("Get Location instance with id: " + key);
        try {
            Location location = pm.getObjectById(Location.class, key);
            return location;
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving location for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances
     *
     * @param attribute Name of the location attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of locations matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see LocationsServlet#getLocations(PersistenceManager, String, Object)
     */
    public List<Location> getLocations(String attribute, Object value, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getLocations(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the location attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of locations matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Location.class);
        value = prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select location(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Location> locations = (List<Location>) queryObj.execute(value);
        locations.size(); // FIXME: remove workaround for a bug in DataNucleus
        return locations;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param postalCode postal code of the searched location
     * @param countryCode country code of the searched location
     * @return Collection of locations matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Location.class);
        queryObj.setFilter(Location.POSTAL_CODE + " == postal && " + Location.COUNTRY_CODE + " == country");
        queryObj.declareParameters("String postal, String country");
        getLogger().warning("Select location(s) with: " + queryObj.toString());
        queryObj.setOrdering("creationDate desc");
        // Select the corresponding resources
        List<Location> locations = (List<Location>) queryObj.execute(postalCode, countryCode);
        locations.size(); // FIXME: remove workaround for a bug in DataNucleus
        return locations;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param latitude latitude of the searched location
     * @param longitude longitude of the searched location
     * @return Collection of locations matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, Double latitude, Double longitude) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Location.class);
        queryObj.setFilter(Location.LATITUDE + " == lat && " + Location.LONGITUDE + " == long");
        queryObj.declareParameters("Double lat, Double long");
        queryObj.setOrdering("creationDate desc");
        getLogger().warning("Select location(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Location> locations = (List<Location>) queryObj.execute(latitude, longitude);
        locations.size(); // FIXME: remove workaround for a bug in DataNucleus
        return locations;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param location place where to start the search
     * @param range distance around the location
     * @param rangeUnit unit of the distance around the search
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of locations matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) throws DataSourceException {
        // The vertical gap is consistent between parallels
        if (LocaleValidator.MILE_UNIT.equals(rangeUnit)) {
            range = range * 1.8;
        }
        range = range * 0.53 / 59;
        Double latitude = location.getLatitude();
        Double topLatitude = latitude + range;
        Double bottomLatitude = latitude - range;

        // The horizontal gap is latitude dependent for the meridians
        Double leftLongitude = -180.0D;
        Double rightLongitude = +180.0D;
        if(latitude < 89.9D) {
            range = range / Math.abs(Math.cos(latitude));
            Double longitude = location.getLongitude();
            leftLongitude = longitude - range;
            rightLongitude = longitude + range;
        }
        // FIXME: take into account that the value can be greater than 360° and smaller than 0°
        // FIXME: that means two request have to be done at the limit...

        log.finest("Box limits [left; rigth] / [bottom; top] : [" + leftLongitude + "; " + rightLongitude + "] / [" + bottomLatitude + "; " + topLatitude + "]");

        /****************************************************************************************************************
         * Ideal case not feasible because of App Engine limitation:  Only one inequality filter per query is supported.
         * // Prepare the query
         * Query query = pm.newQuery(Location.class);
         * query.setFilter(
         *         Location.LATITUDE + " > bottomLatitude && " +
         *         Location.LATITUDE + " < topLatitude && " +
         *         Location.LONGITUDE + " > leftLongitude && " +
         *         Location.LONGITUDE + " < rightLongitude && " +
         *         Location.HAS_STORE + " == hasStoreRegistered"
         * );
         * query.declareParameters("Double topLatitude, Double bottomLatitude, Double leftLongitude, Double rightLongitude, Boolean hasStoreRegistered");
         * if (0 < limit) {
         *     query.setRange(0, limit);
         * }
         *
         * // Execute the query
         * List<Location> locations = (List<Location>) query.executeWithArray(topLatitude, bottomLatitude, leftLongitude, rightLongitude, location.hasStore());
         ****************************************************************************************************************/
        // Prepare the query
        Query query = pm.newQuery(Location.class);
        query.setFilter(
                Location.HAS_STORE + " == hasStoreRegistered && " +
                Location.LATITUDE + " > bottomLatitude && " +
                Location.LATITUDE + " < topLatitude"
        );
        query.declareParameters("Boolean hasStoreRegistered, Double topLatitude, Double bottomLatitude");
        if (0 < limit) {
            query.setRange(0, limit);
        }
        getLogger().warning("Select location(s) with: " + query.toString());

        // Execute the query
        List<Location> locations = (List<Location>) query.executeWithArray(Boolean.TRUE, topLatitude, bottomLatitude);
        locations.size(); // FIXME: remove workaround for a bug in DataNucleus

        List<Location> selection = new ArrayList<Location>();
        for (Location spot: locations) {
            if (leftLongitude < spot.getLongitude() && spot.getLongitude() < rightLongitude) {
                selection.add(spot);
            }
        }

        return selection;
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param location Resource to update
     * @return Updated resource
     *
     * @see LocationOperations#updateLocation(PersistenceManager, Location)
     */
    public Location updateLocation(Location location) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated location
            return updateLocation(pm, location);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param location Resource to update
     * @return Updated resource
     */
    public Location updateLocation(PersistenceManager pm, Location location) {
        getLogger().warning("Updating location with id: " + location.getKey());
        pm.makePersistent(location);
        return location;
    }
}
