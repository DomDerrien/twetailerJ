package twetailer.dao;

import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import domderrien.jsontools.JsonObject;

/**
 * Controller defining various methods used for the CRUD operations on SaleAssociate entities
 *
 * @author Dom Derrien
 */
public class SaleAssociateOperations extends BaseOperations {

    private static final CacheHandler<SaleAssociate> cacheHandler = new CacheHandler<SaleAssociate>(SaleAssociate.class.getName(), Entity.KEY);

    private static SaleAssociate cacheSaleAssociate(SaleAssociate saleAssociate) {
        return cacheHandler.cacheInstance(saleAssociate);
    }

    private static SaleAssociate decacheSaleAssociate(SaleAssociate saleAssociate) {
        return cacheHandler.decacheInstance(saleAssociate);
    }

    private static SaleAssociate getCachedSaleAssociate(Long key) {
        return cacheHandler.getCachedInstance(Entity.KEY, key);
    }

    /**
     * Create the SaleAssociate instance with the given parameters
     *
     * @param parameters HTTP sale associate parameters
     * @return Just created resource
     *
     * @see SaleAssociateOperations#createSaleAssociate(SaleAssociate)
     */
    public SaleAssociate createSaleAssociate(JsonObject parameters) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createSaleAssociate(pm, parameters);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the SaleAssociate instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters HTTP sale associate parameters
     * @return Just created resource
     *
     * @see SaleAssociateOperations#createSaleAssociate(PersistenceManager, SaleAssociate)
     */
    public SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters) {
        // Creates new sale associate record and persist it
        SaleAssociate newSaleAssociate = new SaleAssociate(parameters);
        // Persist it
        return createSaleAssociate(pm, newSaleAssociate);
    }

    /**
     * Create the SaleAssociate instance with the given parameters
     *
     * @param consumer Existing consumer account to extend
     * @param store storeKey identifier of the store where the sale associate works
     * @return Just created resource
     *
     * @see SaleAssociateOperations#createSaleAssociate(PersistenceManager, Consumer, Store)
     */
    public SaleAssociate createSaleAssociate(Consumer consumer, Long storeKey) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createSaleAssociate(pm, consumer, storeKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the SaleAssociate instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer Existing consumer account to extend
     * @param store storeKey identifier of the store where the sale associate works
     * @return Just created resource
     *
     * @see SaleAssociateOperations#createSaleAssociate(PersistenceManager, SaleAssociate)
     */
    public SaleAssociate createSaleAssociate(PersistenceManager pm, Consumer consumer, Long storeKey) {
        SaleAssociate saleAssociate = new SaleAssociate();

        saleAssociate.setConsumerKey(consumer.getKey());

        // Attach to the store
        saleAssociate.setStoreKey(storeKey);

        // Persist the account
        return createSaleAssociate(pm, saleAssociate);
    }

    /**
     * Create the SaleAssociate instance with the given parameters
     *
     * @param saleAssociate Resource to persist
     * @return Just created resource
     */
    public SaleAssociate createSaleAssociate(SaleAssociate saleAssociate) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createSaleAssociate(pm, saleAssociate);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the SaleAssociate instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param saleAssociate Resource to persist
     * @return Just created resource
     */
    public SaleAssociate createSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
        // Persist new sale associate
        saleAssociate = pm.makePersistent(saleAssociate);
        // Cache the new instance
        cacheSaleAssociate(saleAssociate);
        return saleAssociate;
    }

    /**
     * Use the given key to get the corresponding SaleAssociate instance
     *
     * @param key Identifier of the sale associate
     * @return First sale associate matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid SaleAssociate record
     *
     * @see SaleAssociateOperations#getSaleAssociate(PersistenceManager, Long)
     */
    public SaleAssociate getSaleAssociate(Long key) throws InvalidIdentifierException{
        PersistenceManager pm = getPersistenceManager();
        try {
            return getSaleAssociate(pm, key);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding SaleAssociate instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the sale associate
     * @return First sale associate matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid SaleAssociate record
     */
    public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        return getSaleAssociate(pm, key, true);
    }

    /**
     * Use the given key to get the corresponding SaleAssociate instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the sale associate
     * @param useCache If <code>true</code> the SaleAssociate record might come from the cache, otherwise it's loaded from the data store
     * @return First sale associate matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid SaleAssociate record
     */
    public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key, boolean useCache) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the SaleAssociate instance");
        }
        // Try to get a copy from the cache
        SaleAssociate saleAssociate = useCache ? getCachedSaleAssociate(key) : null;
        if (saleAssociate != null) {
            return saleAssociate;
        }
        try {
            // Get it from the data store
            saleAssociate = pm.getObjectById(SaleAssociate.class, key);
            saleAssociate.getCriteria().size(); // FIXME: remove workaround for a bug in DataNucleus
            // Cache the instance
            if (useCache) {
                cacheSaleAssociate(saleAssociate);
            }
            return saleAssociate;
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving sale associate for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding SaleAssociate instances
     *
     * @param attribute Name of the demand attribute used a the search filter
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of sale associates matching the given filter
     *
     * @throws DataSourceException If the data exchange with the data store fails
     *
     * @see SaleAssociatesOperations#getSaleAssociates(PersistenceManager, String, Object)
     */
    public List<SaleAssociate> getSaleAssociates(String attribute, Object value, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getSaleAssociates(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding SaleAssociate instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the demand attribute used a the search filter
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of sale associates matching the given filter
     *
     * @throws DataSourceException If the data exchange with the data store fails
     */
    @SuppressWarnings("unchecked")
    public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(SaleAssociate.class);
        try {
            value = prepareQuery(query, attribute, value, limit);
            // Select the corresponding resources
            List<SaleAssociate> saleAssociates = (List<SaleAssociate>) query.execute(value);
            // Cache the data if only one instance is returned
            if (saleAssociates.size() == 1) {
                cacheSaleAssociate(saleAssociates.get(0));
            }
            return saleAssociates;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding SaleAssociate identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the SaleAssociate attribute used a the search filter
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of SaleAssociate identifiers matching the given filter
     *
     * @throws DataSourceException If the data exchange with the data store fails
     */
    @SuppressWarnings("unchecked")
    public List<Long> getSaleAssociateKeys(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery("select " + SaleAssociate.KEY + " from " + SaleAssociate.class.getName());
        try {
            value = prepareQuery(query, attribute, value, limit);
            // Select the corresponding resources
            List<Long> saleAssociateKeys = (List<Long>) query.execute(value);
            saleAssociateKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
            return saleAssociateKeys;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Get the identified SaleAssociate instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param saleAssociateKeys list of SaleAssociate instance identifiers
     * @return Collection of sale associates matching the given filter
     *
     * @throws DataSourceException If the data exchange with the data store fails
     */
    @SuppressWarnings("unchecked")
    public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, List<Long> saleAssociateKeys) throws DataSourceException {
        // Select the corresponding resources
        Query query = pm.newQuery(SaleAssociate.class, ":p.contains(key)"); // Reported as being more efficient than pm.getObjectsById()
        try {
            List<SaleAssociate> saleAssociates = (List<SaleAssociate>) query.execute(saleAssociateKeys);
            saleAssociates.size(); // FIXME: remove workaround for a bug in DataNucleus
            return saleAssociates;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding SaleAssociate instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of sale associates matching the given filter
     *
     * @throws DataSourceException If the data exchange with the data store fails
     */
    @SuppressWarnings("unchecked")
    public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(SaleAssociate.class);
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<SaleAssociate> saleAssociates = (List<SaleAssociate>) query.executeWithArray(values);
            // Cache the data if only one instance is returned
            if (saleAssociates.size() == 1) {
                cacheSaleAssociate(saleAssociates.get(0));
            }
            return saleAssociates;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding SaleAssociate identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of sale associate keys matching the given filter
     *
     * @throws DataSourceException If the data exchange with the data store fails
     */
    @SuppressWarnings("unchecked")
    public List<Long> getSaleAssociateKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery("select " + SaleAssociate.KEY + " from " + SaleAssociate.class.getName());
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<Long> saleAssociateKeys = (List<Long>) query.executeWithArray(values);
            saleAssociateKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
            return saleAssociateKeys;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param saleAssociate Resource to update
     * @return Updated resource
     *
     * @throws DataSourceException If the data exchange with the data store fails
     *
     * @see SaleAssociateOperations#updateSaleAssociate(PersistenceManager, SaleAssociate)
     */
    public SaleAssociate updateSaleAssociate(SaleAssociate saleAssociate) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated sale associate
            return updateSaleAssociate(pm, saleAssociate);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param saleAssociate Resource to update
     * @return Updated resource
     *
     * @throws DataSourceException If the data exchange with the data store fails
     */
    public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) throws DataSourceException {
        ObjectState state = JDOHelper.getObjectState(saleAssociate);
        if (ObjectState.TRANSIENT.equals(state)) {
            // Get a fresh user copy from the data store
            SaleAssociate transientSaleAssociate = saleAssociate;
            try {
                saleAssociate = getSaleAssociate(pm, saleAssociate.getKey(), false);
            }
            catch (InvalidIdentifierException ex) {
                throw new DataSourceException("Cannot retreive a fresh copy of the consumer key:" + saleAssociate.getKey(), ex);
            }
            // Remove the previous copy from the cache
            decacheSaleAssociate(transientSaleAssociate); // To handle the possibility of an attribute used as a cache key being updated and leaving a wrong entry into the cache
            // Merge the attribute of the old copy into the fresh one
            saleAssociate.fromJson(transientSaleAssociate.toJson(), true, true);
        }
        // Persist new sale associate
        saleAssociate = pm.makePersistent(saleAssociate);
        // Cache the new instance
        cacheSaleAssociate(saleAssociate);
        return pm.makePersistent(saleAssociate);
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance and to delete it
     *
     * @param saleAssociateKey Identifier of the sale associate
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid SaleAssociate record
     *
     * @see SaleAssociateOperations#deleteSaleAssociate(PersistenceManager, Long)
     */
    public void deleteSaleAssociate(Long saleAssociateKey) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            deleteSaleAssociate(pm, saleAssociateKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance and to delete it
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param saleAssociateKey Identifier of the sale associate
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid SaleAssociate record
     *
     * @see SaleAssociateOperations#getSaleAssociates(PersistenceManager, Long)
     * @see SaleAssociateOperations#deleteSaleAssociate(PersistenceManager, SaleAssociate)
     */
    public void deleteSaleAssociate(PersistenceManager pm, Long saleAssociateKey) throws InvalidIdentifierException {
        SaleAssociate saleAssociate = getSaleAssociate(pm, saleAssociateKey);
        deleteSaleAssociate(pm, saleAssociate);
    }

    /**
     * Delete the given demand while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param saleAssociate Object to delete
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid SaleAssociate record
     */

    public void deleteSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) throws InvalidIdentifierException {
        ObjectState state = JDOHelper.getObjectState(saleAssociate);
        if (ObjectState.TRANSIENT.equals(state)) {
            saleAssociate = getSaleAssociate(pm, saleAssociate.getKey(), false);
        }
        decacheSaleAssociate(saleAssociate);
        pm.deletePersistent(saleAssociate);
    }
}
