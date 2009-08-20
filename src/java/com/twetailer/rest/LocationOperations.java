package com.twetailer.rest;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.twetailer.DataSourceException;
import com.twetailer.dto.Location;
import com.twetailer.validator.LocaleValidator;

import domderrien.jsontools.JsonObject;

public class LocationOperations extends BaseOperations {
    private static final Logger log = Logger.getLogger(LocationOperations.class.getName());

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
     * @see LocationOperations#createLocation(Location)
     */
    public Location createLocation(JsonObject parameters) {
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
        if (newLocation.getPostalCode() != null && newLocation.getCountryCode() != null) {
            return createLocation(pm, newLocation);
        }
        return null;
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
            // Try to retrieve the same location
            List<Location> locations = getLocations(pm, Location.POSTAL_CODE, location.getPostalCode(), 1);
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
            throw new InvalidParameterException("Invalid key; cannot retrieve the Location instance");
        }
        getLogger().warning("Get Location instance with id: " + key);
        Location location = pm.getObjectById(Location.class, key);
        if (location == null) {
            throw new DataSourceException("No location for identifier: " + key);
        }
        return location; // FIXME: remove workaround for a bug in DataNucleus
        // return new Location(location.toJson());
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
        prepareQuery(queryObj, attribute, value, limit);
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
        Double latitude = location.getLatitude();
        latitude = 0.0D; // FIXME
        Double topLatitude = latitude + range * 0.001; // TODO: verify the formula
        Double bottomLatitude = latitude - range * 0.001; // TODO: verify the formula
        
        // The horizontal gap is latitude dependent for the meridians
        range = range / Math.cos(latitude); // TODO: verify the formula
        Double longitude = location.getLongitude();
        longitude = 90.0D; // FIXME
        Double leftLongitude = longitude + range * 0.001; // TODO: verify the formula
        Double rightLongitude = longitude - range * 0.001; // TODO: verify the formula
        // FIXME: take into account that the value can be greater than 360° and smaller than 0°
        // FIXME: that means two request have to be done at the limit...
        
        /****************************************************************************************************************
         * Ideal case not feasible because of App Engine limitation:  Only one inequality filter per query is supported.
         * // Prepare the query
         * Query query = pm.newQuery(Location.class);
         * query.setFilter(
         *         Location.LATITUDE + " > bottomLatitude && " +
         *         Location.LATITUDE + " < topLatitude && " +
         *         Location.LONGITUDE + " > rightLongitude && " +
         *         Location.LONGITUDE + " < leftLongitude && " +
         *         Location.HAS_STORE + " != hasStore"
         * );
         * query.declareParameters("Double topLatitude, Double bottomLatitude, Double leftLongitude, Double rightLongitude, Boolean hasStore");
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
                // Location.HAS_STORE + " == hasStore && " + // FIXME: understand why this equality breaks the query!
                Location.LATITUDE + " > bottomLatitude && " +
                Location.LATITUDE + " < topLatitude"
        );
        query.declareParameters("Boolean hasStore, Double topLatitude, Double bottomLatitude");
        if (0 < limit) {
            query.setRange(0, limit);
        }
        getLogger().warning("Select location(s) with: " + query.toString());
        
        // Execute the query
        List<Location> locations = (List<Location>) query.executeWithArray(true, topLatitude, bottomLatitude);
        locations.size(); // FIXME: remove workaround for a bug in DataNucleus
        
        List<Location> selection = new ArrayList<Location>();
        for (Location spot: locations) {
            if (location.hasStore()) {
                if (rightLongitude < location.getLongitude() && location.getLongitude() < leftLongitude) {
                    selection.add(spot);
                }
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