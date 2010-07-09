package twetailer.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Location;
import twetailer.dto.Store;
import domderrien.jsontools.JsonObject;

/**
 * Controller defining various methods used for the CRUD operations on Store entities
 *
 * @author Dom Derrien
 */
public class StoreOperations extends BaseOperations {
    private static Logger log = Logger.getLogger(StoreOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Store instance with the given parameters
     *
     * @param parameters HTTP store parameters
     * @return Just created resource
     *
     * @throws ClientException If mandatory attributes are missing
     *
     * @see StoreOperations#createStore(Store)
     */
    public Store createStore(JsonObject parameters) throws ClientException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createStore(pm, parameters);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Store instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters HTTP store parameters
     * @return Just created resource
     *
     * @see StoreOperations#createStore(PersistenceManager, Store)
     */
    public Store createStore(PersistenceManager pm, JsonObject parameters) {
        getLogger().warning("Create store with: " + parameters.toString());
        // Creates new store record and persist it
        Store newStore = new Store(parameters);
        // Persist it
        return createStore(pm, newStore);
    }

    /**
     * Create the Store instance with the given parameters
     *
     * @param store Resource to persist
     * @return Just created resource
     *
     * @see StoreOperations#createStore(PersistenceManager, Store)
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
        return pm.makePersistent(store);
    }

    /**
     * Use the given key to get the corresponding Store instance
     *
     * @param key Identifier of the store
     * @return First store matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Store record
     *
     * @see StoreOperations#getStore(PersistenceManager, Long)
     */
    public Store getStore(Long key) throws InvalidIdentifierException {
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
     * @throws InvalidIdentifierException If the given identifier does not match a valid Store record
     */
    public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the Store instance");
        }
        getLogger().warning("Get Store instance with id: " + key);
        try {
            return pm.getObjectById(Store.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving store for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Store instances
     *
     * @param attribute Name of the store attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of stores matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see StoreOperations#getStores(PersistenceManager, String, Object)
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
     * @param attribute Name of the store attribute used a the search criteria
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
        value = prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select stores(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Store> stores = (List<Store>) queryObj.execute(value);
        stores.size(); // FIXME: remove workaround for a bug in DataNucleus
        return stores;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Store identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the Store attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of Store identifiers matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getStoreKeys(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery("select " + Store.KEY + " from " + Store.class.getName());
        value = prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select store(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Long> storeKeys = (List<Long>) queryObj.execute(value);
        storeKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
        return storeKeys;
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Store instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of stores matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Store.class);
        Object[] values = prepareQuery(query, parameters, limit);
        getLogger().warning("Select store(s) with: " + query.toString());
        // Select the corresponding resources
        List<Store> stores = (List<Store>) query.executeWithArray(values);
        stores.size(); // FIXME: remove workaround for a bug in DataNucleus
        return stores;
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Store identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of store keys matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getStoreKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery("select " + Store.KEY + " from " + Store.class.getName());
        Object[] values = prepareQuery(queryObj, parameters, limit);
        getLogger().warning("Select store(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Long> storeKeys = (List<Long>) queryObj.executeWithArray(values);
        storeKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
        return storeKeys;
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
     * @see StoreOperations#getStores(PersistenceManager, String, Object)
     */
    public List<Store> getStores(List<Location> locations, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getStores(pm, new HashMap<String, Object>(), locations, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Store instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param queryParameters Map of attributes and values to match
     * @param locations list of locations where expected stores should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of stores matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public List<Store> getStores(PersistenceManager pm, Map<String, Object> queryParameters, List<Location> locations, int limit) throws DataSourceException {
        List<Store> selection = new ArrayList<Store>();
        for (Location location: locations) {
            // Select the corresponding resources
            queryParameters.put(Store.LOCATION_KEY, location.getKey());
            List<Store> stores = getStores(pm, queryParameters, limit);
            // Copy into the list to be returned
            selection.addAll(stores);
            if (limit != 0) {
                if (limit <= selection.size()) {
                    break;
                }
                limit = limit - selection.size();
            }
        }
        return selection;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Store identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param queryParameters Map of attributes and values to match
     * @param locations list of locations where expected stores should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of store identifiers matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public List<Long> getStoreKeys(PersistenceManager pm, Map<String, Object> queryParameters, List<Location> locations, int limit) throws DataSourceException {
        List<Long> selection = new ArrayList<Long>();
        for (Location location: locations) {
            // Select the corresponding resources
            queryParameters.put(Store.LOCATION_KEY, location.getKey());
            List<Long> stores = getStoreKeys(pm, queryParameters, limit);
            // Copy into the list to be returned
            selection.addAll(stores);
            if (limit != 0) {
                if (limit <= selection.size()) {
                    break;
                }
                limit = limit - selection.size();
            }
        }
        return selection;
    }

    /**
     * Get the identified Store instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param storeKeys list of Store instance identifiers
     * @return Collection of stores matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Store> getStores(PersistenceManager pm, List<Long> storeKeys) throws DataSourceException {
        // Select the corresponding resources
        Query query = pm.newQuery(Store.class, ":p.contains(key)"); // Reported as being more efficient than pm.getObjectsById()
        List<Store> stores = (List<Store>) query.execute(storeKeys);
        stores.size(); // FIXME: remove workaround for a bug in DataNucleus
        return stores;
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param store Resource to update
     * @return Updated resource
     *
     * @see StoreOperations#updateStore(PersistenceManager, Store)
     */
    public Store updateStore(Store store) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated store
            return updateStore(pm, store);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param store Resource to update
     * @return Updated resource
     */
    public Store updateStore(PersistenceManager pm, Store store) {
        return pm.makePersistent(store);
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Store instance and to delete it
     *
     * @param storeKey Identifier of the store
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Store record
     *
     * @see StoreOperations#deleteStore(PersistenceManager, Long)
     */
    public void deleteStore(Long storeKey) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            deleteStore(pm, storeKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Store instance and to delete it
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param storeKey Identifier of the store
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Store record
     *
     * @see StoreOperations#getStores(PersistenceManager, Long)
     * @see StoreOperations#deleteStore(PersistenceManager, Store)
     */
    public void deleteStore(PersistenceManager pm, Long storeKey) throws InvalidIdentifierException {
        Store store = getStore(pm, storeKey);
        deleteStore(pm, store);
    }

    /**
     * Delete the given store while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param store Object to delete
     */

    public void deleteStore(PersistenceManager pm, Store store) {
        getLogger().warning("Delete store with id: " + store.getKey());
        pm.deletePersistent(store);
    }
}
