package com.twetailer.rest;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Retailer;
import com.twetailer.dto.Store;

public class RetailerOperations extends BaseOperations {
    private static final Logger log = Logger.getLogger(RetailerOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Retailer instance with the given parameters
     * 
     * @param consumer Existing consumer account to extend
     * @param store storeKey identifier of the store where the retailer works
     * @return Just created resource
     * 
     * @see RetailerOperations#createRetailer(PersistenceManager, Consumer, Store)
     */
    public Retailer createRetailer(Consumer consumer, Long storeKey) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createRetailer(pm, consumer, storeKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Retailer instance with the given parameters
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer Existing consumer account to extend
     * @param store storeKey identifier of the store where the retailer works
     * @return Just created resource
     * 
     * @see RetailerOperations#createRetailer(PersistenceManager, Retailer)
     */
    public Retailer createRetailer(PersistenceManager pm, Consumer consumer, Long storeKey) {
        Retailer retailer = new Retailer();
        
        retailer.setName(consumer.getName());
        retailer.setConsumerKey(consumer.getKey());
        
        // Copy the user's attribute
        retailer.setImId(consumer.getImId());
        retailer.setEmail(consumer.getEmail());
        retailer.setTwitterId(consumer.getTwitterId());
        
        // Attach to the store
        retailer.setStoreKey(storeKey);

        // Persist the account
        return createRetailer(pm, retailer);
    }

    /**
     * Create the Retailer instance with the given parameters
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param retailer Resource to persist
     * @return Just created resource
     */
    public Retailer createRetailer(PersistenceManager pm, Retailer retailer) {
        pm.makePersistent(retailer);
        return retailer;
    }

    /**
     * Use the given key to get the corresponding Retailer instance
     * 
     * @param key Identifier of the retailer
     * @return First retailer matching the given criteria or <code>null</code>
     * 
     * @throws DataSourceException If the retrieved retailer does not belong to the specified user
     * 
     * @see RetailerOperations#getRetailer(PersistenceManager, Long)
     */
    public Retailer getRetailer(Long key) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getRetailer(pm, key);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Retailer instance
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the retailer
     * @return First retailer matching the given criteria or <code>null</code>
     * 
     * @throws DataSourceException If the retailer cannot be retrieved
     */
    public Retailer getRetailer(PersistenceManager pm, Long key) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new InvalidParameterException("Invalid key; cannot retrieve the Retailer instance");
        }
        getLogger().warning("Get Retailer instance with id: " + key);
        try {
            Retailer retailer = pm.getObjectById(Retailer.class, key);
            if (retailer == null) {
                throw new DataSourceException("No retailer for identifier: " + key);
            }
            if (retailer.getCriteria() != null) {
                retailer.getCriteria().size();
            }
            return retailer;
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving retailer for identifier: " + key + " -- ex: " + ex.getMessage());
        }
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Retailer instances
     * 
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of retailers matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data retailer type
     * 
     * @see RetailersServlet#getRetailers(PersistenceManager, String, Object)
     */
    public List<Retailer> getRetailers(String attribute, Object value, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getRetailers(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Retailer instances while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of retailers matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data retailer type
     */
    @SuppressWarnings("unchecked")
    public List<Retailer> getRetailers(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Retailer.class);
        prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select retailer(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Retailer> retailers = (List<Retailer>) queryObj.execute(value);
        retailers.size(); // FIXME: remove workaround for a bug in DataNucleus
        return retailers;
    }
    
    /**
     * Persist the given (probably updated) resource
     * 
     * @param retailer Resource to update
     * @return Updated resource
     * 
     * @see RetailerOperations#updateRetailer(PersistenceManager, Retailer)
     */
    public Retailer updateRetailer(Retailer retailer) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated retailer
            return updateRetailer(pm, retailer);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param retailer Resource to update
     * @return Updated resource
     */
    public Retailer updateRetailer(PersistenceManager pm, Retailer retailer) {
        getLogger().warning("Updating retailer with id: " + retailer.getKey());
        retailer.setCriteriaKack(retailer.getCriteria()); // FIXME: if the array is not copied, the updated array won't be persisted!
        pm.makePersistent(retailer);
        return retailer;
    }
}