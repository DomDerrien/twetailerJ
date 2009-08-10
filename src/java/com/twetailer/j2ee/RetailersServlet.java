package com.twetailer.j2ee;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Retailer;
import com.twetailer.dto.Store;

@SuppressWarnings("serial")
public class RetailersServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(RetailersServlet.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected JsonObject createResource(JsonObject parameters, User loggedUser) throws DataSourceException {
		return null;
	}

	@Override
	protected void deleteResource(String resourceId, User loggedUser) throws DataSourceException {
	}

	@Override
	protected JsonObject getResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		return null;
	}

	@Override
	protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
		return null;
	}

	@Override
	protected void updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
	}

    /**
     * Create the Retailer instance with the given parameters
     * 
     * @param consumer Existing consumer account to extend
     * @param store Existing store where the retailer works
     * @return Just created resource
     */
    public Retailer createRetailer(Consumer consumer, Store store) {
        Retailer retailer = new Retailer();
        
        // Copy the user's attribute
        retailer.setAddress(consumer.getAddress());
        retailer.setEmail(consumer.getEmail());
        retailer.setImId(consumer.getImId());
        retailer.setName(consumer.getName());
        retailer.setPhoneNumber(consumer.getPhoneNumber());
        retailer.setSystemUser(consumer.getSystemUser());
        retailer.setTwitterId(consumer.getTwitterId());
        
        // Attach to the store
        retailer.setStoreKey(store.getKey());
        
        // Persist the account
        return createRetailer(retailer);
    }

    /**
     * Create the Retailer instance with the given parameters
     * 
     * @param retailer Resource to persist
     * @return Just created resource
     */
    public Retailer createRetailer(Retailer retailer) {
        PersistenceManager pm = getPersistenceManager();
        try {
            pm.makePersistent(retailer);
            return retailer;
        }
        finally {
            pm.close();
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
        // Select the corresponding resources
        List<Retailer> retailers = (List<Retailer>) queryObj.execute(value);
        retailers.size(); // FIXME: remove workaround for a bug in DataNucleus
        return retailers;
    }
}
