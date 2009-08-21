package com.twetailer.rest;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.twetailer.DataSourceException;
import com.twetailer.dto.Location;
import com.twetailer.dto.Store;

public class StoreOperations extends BaseOperations {
    private static final Logger log = Logger.getLogger(StoreOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Store instance with the given parameters
     * 
     * @param store Resource to persist
     * @return Just created resource
     * 
     * @see StoresServlet#createStore(PersistenceManager, Store)
     */
    public Store createStore(Store store) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createStore(pm, store);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Store instance with the given parameters
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param store Resource to persist
     * @return Just created resource
     */
    public Store createStore(PersistenceManager pm, Store store) {
        pm.makePersistent(store);
        return store;
    }

    /**
     * Use the given key to get the corresponding Store instance
     * 
     * @param key Identifier of the store
     * @return First store matching the given criteria or <code>null</code>
     * 
     * @throws DataSourceException If the retrieved store does not belong to the specified user
     * 
     * @see StoreOperations#getStore(PersistenceManager, Long)
     */
    public Store getStore(Long key) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getStore(pm, key);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Store instance
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the store
     * @return First store matching the given criteria or <code>null</code>
     * 
     * @throws DataSourceException If the retrieved store does not belong to the specified user
     */
    public Store getStore(PersistenceManager pm, Long key) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new InvalidParameterException("Invalid key; cannot retrieve the Store instance");
        }
        getLogger().warning("Get Store instance with id: " + key);
        try {
            Store store = pm.getObjectById(Store.class, key);
            if (store == null) {
                throw new DataSourceException("No store for identifier: " + key);
            }
            return store; // FIXME: remove workaround for a bug in DataNucleus
            // return new Store(store.toJson());
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving store for identifier: " + key + " -- ex: " + ex.getMessage());
        }
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Store instances
     * 
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of stores matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     * 
     * @see StoresServlet#getStores(PersistenceManager, String, Object)
     */
    public List<Store> getStores(String attribute, Object value, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getStores(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Store instances while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of stores matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Store> getStores(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Store.class);
        prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select stores(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Store> stores = (List<Store>) queryObj.execute(value);
        stores.size(); // FIXME: remove workaround for a bug in DataNucleus
        return stores;
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Store instances
     * 
     * @param locations list of locations where expected stores should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of stores matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     * 
     * @see StoresServlet#getStores(PersistenceManager, String, Object)
     */
    public List<Store> getStores(List<Location> locations, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getStores(pm, locations, limit);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Store instances while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param locations list of locations where expected stores should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of stores matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) throws DataSourceException {
        List<Store> selection = new ArrayList<Store>();
        for (Location location: locations) {
            // Select the corresponding resources
            List<Store> stores = getStores(pm, Store.LOCATION_KEY, location.getKey(), limit);
            // Copy into the list to be returned
            selection.addAll(stores);
        }
        return selection;
    }
}