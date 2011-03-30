package twetailer.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.validator.LocaleValidator;
import domderrien.jsontools.JsonObject;

/**
 * Controller defining various methods used for the CRUD operations on Location entities
 *
 * @author Dom Derrien
 */
public class LocationOperations extends BaseOperations {

    // Add entries for JabberId & TwitterId when these connectors start to be heavily used
    private static final CacheHandler<Location> cacheHandler = new CacheHandler<Location>(Location.class.getName(), new String[] { Entity.KEY, Location.POSTAL_CODE });

    private static Location cacheLocation(Location location) {
        return cacheHandler.cacheInstance(location);
    }

    private static Location decacheLocation(Location location) {
        return cacheHandler.decacheInstance(location);
    }

    private static Location getCachedLocation(Long key) {
        return cacheHandler.getCachedInstance(Location.KEY, key);
    }

    private static List<Location> getCachedLocations(String key, Object value) {
        Location location = cacheHandler.getCachedInstance(key, value);
        if (location != null) {
            List<Location> locations = new ArrayList<Location>();
            locations.add(location);
            return locations;
        }
        return null;
    }

    private static List<Location> getCachedLocations(String postalCode, String countryCode) {
        Location location = cacheHandler.getCachedInstance(Location.POSTAL_CODE, postalCode);
        if (location != null && location.getCountryCode().equals(countryCode)) {
            List<Location> locations = new ArrayList<Location>();
            locations.add(location);
            return locations;
        }
        return null;
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
            if (location.getKey() != null) {
                try {
                    return getLocation(pm, location.getKey());
                }
                catch (InvalidIdentifierException ex) {
                    // Erase the reference to be able to create a new Location instance
                    location.setKey(null, true);
                }
            }
            if (location.getPostalCode() != null) { // && location.getCountryCode() != null) { // A country code being automatically created, so no need to verify the null value
                // Try to retrieve from its postal and country codes
                locations = getLocations(pm, location.getPostalCode(), location.getCountryCode());
            }
            else if (location.getLatitude() == Location.INVALID_COORDINATE || location.getLongitude() == Location.INVALID_COORDINATE) {
                throw new IllegalArgumentException("Location object should have a valid pair of {postal; country} or {latitude; longitude}.");
            }
            if (locations != null && 0 < locations.size()) {
                return locations.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Create an entry for that new location
        if (location.getLatitude() == Location.INVALID_COORDINATE) { // || location.getLongitude() == Location.INVALID_COORDINATE) {
            location = LocaleValidator.getGeoCoordinates(location);
        }
        // Persist new location
        location = pm.makePersistent(location);
        // Cache the new instance
        cacheLocation(location);
        return location;
    }

    /**
     * Use the given key to get the corresponding Location instance
     *
     * @param key Identifier of the location
     * @return First location matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Location record
     *
     * @see LocationOperations#getLocation(PersistenceManager, Long)
     */
    public Location getLocation(Long key) throws InvalidIdentifierException {
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
     * @return First location matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Location record
     */
    public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        return getLocation(pm, key, true);
    }

    /**
     * Use the given key to get the corresponding Location instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the location
     * @param useCache If <code>true</code> the Location record might come from the cache, otherwise it's loaded from the data store
     * @return First location matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Location record
     */
    public Location getLocation(PersistenceManager pm, Long key, boolean useCache) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the Location instance");
        }
        // Try to get a copy from the cache
        Location location = useCache ? getCachedLocation(key) : null;
        if (location != null) {
            return location;
        }
        try {
            // Get it from the data store
            location = pm.getObjectById(Location.class, key);
            // Cache the instance
            if (useCache) {
                cacheLocation(location);
            }
            return location;
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving location for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances
     *
     * @param attribute Name of the location attribute used a the search filter
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of locations matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see LocationsOperations#getLocations(PersistenceManager, String, Object)
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
     * @param attribute Name of the location attribute used a the search filter
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of locations matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Location.class);
        try {
            // Try to get a copy from the cache
            List<Location> locations = getCachedLocations(attribute, value);
            if (locations != null) {
                return locations;
            }
            value = prepareQuery(query, attribute, value, limit);
            // Select the corresponding resources
            locations = (List<Location>) query.execute(value);
            // Cache the data if only one instance is returned
            if (locations.size() == 1) {
                cacheLocation(locations.get(0));
            }
            return locations;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of locations matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Location.class);
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<Location> locations = (List<Location>) query.executeWithArray(values);
            // Cache the data if only one instance is returned
            if (locations.size() == 1) {
                cacheLocation(locations.get(0));
            }
            return locations;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Location identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of location identifiers matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getLocationKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery("select " + Location.KEY + " from " + Location.class.getName());
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<Long> locationKeys = (List<Long>) query.executeWithArray(values);
            locationKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
            return locationKeys;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param postalCode postal code of the searched location
     * @param countryCode country code of the searched location
     * @return Collection of locations matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Location.class);
        try {
            // Try to get a copy from the cache
            List<Location> locations = getCachedLocations(postalCode, countryCode);
            if (locations != null) {
                return locations;
            }
            // Get it from the data store
            query.setFilter(Location.POSTAL_CODE + " == postal && " + Location.COUNTRY_CODE + " == country");
            query.declareParameters("String postal, String country");
            query.setOrdering("creationDate desc");
            // Select the corresponding resources
            locations = (List<Location>) query.execute(postalCode, countryCode);
            // Cache the data if only one instance is returned
            if (locations.size() == 1) {
                cacheLocation(locations.get(0));
            }
            return locations;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param latitude latitude of the searched location
     * @param longitude longitude of the searched location
     * @return Collection of locations matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     * /
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, Double latitude, Double longitude) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Location.class);
        queryObj.setFilter(Location.LATITUDE + " == lat && " + Location.LONGITUDE + " == long");
        queryObj.declareParameters("Double lat, Double long");
        queryObj.setOrdering("creationDate desc");
        // Select the corresponding resources
        List<Location> locations = (List<Location>) queryObj.execute(latitude, longitude);
        locations.size(); // FIXME: remove workaround for a bug in DataNucleus
        return locations;
    }
    */

    private static final double ONE_MI_IN_KM = 1.609344d;
    private static final double EQUATORIAL_CIRCUMFERENCE_IN_KM = 40075.11d;
    private static final double ONE_EQUATORIAL_DEGREE_IN_KM = 360.0d / EQUATORIAL_CIRCUMFERENCE_IN_KM;
    private static final double EQUATORIAL_RADIUS_IN_KM = EQUATORIAL_CIRCUMFERENCE_IN_KM / 2.0d / Math.PI;

    /**
     * Get the geo-coordinates of the box defined by its centers and a radius
     *
     * @param location place where to start the search
     * @param range distance around the location
     * @param rangeUnit unit of the distance around the search
     * @return Array of coordinates
     */
    public static double[] getLocationBounds(Location location, Double range, String rangeUnit) {
        // The vertical gap is consistent between parallels
        if (LocaleValidator.MILE_UNIT.equals(rangeUnit)) {
            range = range * ONE_MI_IN_KM;
        }
        double latitudeDelta = range * ONE_EQUATORIAL_DEGREE_IN_KM;
        double latitude = location.getLatitude();
        double topLatitude = latitude + latitudeDelta;
        double bottomLatitude = latitude - latitudeDelta;

        // The horizontal gap is latitude dependent for the meridians
        double leftLongitude = -180.0D;
        double rightLongitude = +180.0D;
        if(latitude < 89.9D) {
            double longitudeDelta = Math.abs(range * Math.toDegrees(Math.asin(1 / (Math.cos(Math.toRadians(latitude)) * EQUATORIAL_RADIUS_IN_KM))));

            double longitude = location.getLongitude();
            leftLongitude = longitude - longitudeDelta;
            rightLongitude = longitude + longitudeDelta;
        }
        // FIXME: take into account that the value can be greater than 360° and smaller than 0°
        // FIXME: that means two requests have to be done at the limit...

        Logger.getLogger(LocationOperations.class.getName()).finest("Box limits [left; right] / [bottom; top] : [" + leftLongitude + "; " + rightLongitude + "] / [" + bottomLatitude + "; " + topLatitude + "]");

        return new double[] { leftLongitude, rightLongitude, bottomLatitude, topLatitude };
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param location place where to start the search
     * @param range distance around the location
     * @param rangeUnit unit of the distance around the search
     * @param withStore if <code>true</code>, only locations with at least a store will be returned, otherwise there's no limitation
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of locations matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
        double[] bounds = getLocationBounds(location, range, rangeUnit);
        double leftLongitude = bounds[0];
        double rightLongitude = bounds[1];
        double bottomLatitude = bounds[2];
        double topLatitude = bounds[3];
        /****************************************************************************************************************
         * Ideal case not feasible because of App Engine limitation:  Only _one_ inequality filter per query!
         * // Prepare the query
         * Query query = pm.newQuery(Location.class);
         * query.setFilter(
         *         Location.COUNTRY_CODE + " == givenCountryCode && " +
         *         Location.LATITUDE + " > bottomLatitude && " +
         *         Location.LATITUDE + " < topLatitude && " +
         *         Location.LONGITUDE + " > leftLongitude && " +
         *         Location.LONGITUDE + " < rightLongitude && " +
         *         Location.HAS_STORE + " == hasStoreRegistered"
         * );
         * query.declareParameters("String givenCountryCode, Double topLatitude, Double bottomLatitude, Double leftLongitude, Double rightLongitude, Boolean hasStoreRegistered");
         * if (0 < limit) {
         *     query.setRange(0, limit);
         * }
         *
         * // Execute the query
         * List<Location> locations = (List<Location>) query.executeWithArray(location.getCountryCode(), topLatitude, bottomLatitude, leftLongitude, rightLongitude, location.hasStore());
         ****************************************************************************************************************/
        // Prepare the query
        Query query = pm.newQuery(Location.class);
        try {
            if (withStore) {
                query.setFilter(
                        Location.COUNTRY_CODE + " == givenCountryCode && " +
                        Location.HAS_STORE + " == hasStoreRegistered && " +
                        Location.LATITUDE + " > bottomLatitude && " +
                        Location.LATITUDE + " < topLatitude"
                );
                query.declareParameters("String givenCountryCode, Boolean hasStoreRegistered, Double topLatitude, Double bottomLatitude");
            }
            else {
                query.setFilter(
                        Location.COUNTRY_CODE + " == givenCountryCode && " +
                        Location.LATITUDE + " > bottomLatitude && " +
                        Location.LATITUDE + " < topLatitude"
                );
                query.declareParameters("String givenCountryCode, Double topLatitude, Double bottomLatitude");
            }
            if (0 < limit) {
                query.setRange(0, limit);
            }

            // Execute the query
            List<Location> locations = (List<Location>) (withStore ?
                    query.executeWithArray(location.getCountryCode(), Boolean.valueOf(withStore), topLatitude, bottomLatitude) :
                    query.executeWithArray(location.getCountryCode(), topLatitude, bottomLatitude));
            locations.size(); // FIXME: remove workaround for a bug in DataNucleus

            List<Location> selection = new ArrayList<Location>();
            for (Location spot: locations) {
                if (leftLongitude < spot.getLongitude() && spot.getLongitude() < rightLongitude) {
                    selection.add(spot);
                }
            }

            return selection;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Get the identified Location instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param locationKeys list of Location instance identifiers
     * @return Collection of locations matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Location> getLocations(PersistenceManager pm, List<Long> locationKeys) throws DataSourceException {
        // Select the corresponding resources
        Query query = pm.newQuery(Location.class, ":p.contains(key)"); // Reported as being more efficient than pm.getObjectsById()
        try {
            List<Location> locations = (List<Location>) query.execute(locationKeys);
            // Cache the data if only one instance is returned
            if (locations.size() == 1) {
                cacheLocation(locations.get(0));
            }
            return locations;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param location Resource to update
     * @return Updated resource
     *
     * @throws DataSourceException If the data management failed data store side
     *
     * @see LocationOperations#updateLocation(PersistenceManager, Location)
     */
    public Location updateLocation(Location location) throws DataSourceException {
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
     *
     * @throws DataSourceException If the data management failed data store side
     *
     * @throws DataSourceException
     */
    public Location updateLocation(PersistenceManager pm, Location location) throws DataSourceException {
        ObjectState state = JDOHelper.getObjectState(location);
        if (ObjectState.TRANSIENT.equals(state)) {
            // Get a fresh user copy from the data store
            Location transientLocation = location;
            try {
                location = getLocation(pm, location.getKey(), false);
            }
            catch (InvalidIdentifierException ex) {
                throw new DataSourceException("Cannot retreive a fresh copy of the location key:" + location.getKey(), ex);
            }
            // Remove the previous copy from the cache
            decacheLocation(transientLocation); // To handle the possibility of an attribute used as a cache key being updated and leaving a wrong entry into the cache
            // Merge the attribute of the old copy into the fresh one
            location.fromJson(transientLocation.toJson(), true, true);
        }
        // Persist updated location
        location = pm.makePersistent(location);
        // Cache the new instance
        cacheLocation(location);
        return location;
    }
}
