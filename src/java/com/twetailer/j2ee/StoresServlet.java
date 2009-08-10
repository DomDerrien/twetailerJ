package com.twetailer.j2ee;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Store;

@SuppressWarnings("serial")
public class StoresServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(StoresServlet.class.getName());

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
     * Create the Store instance with the given parameters
     * 
     * @param store Resource to persist
     * @return Just created resource
     */
    public Store createStore(Store store) {
        PersistenceManager pm = getPersistenceManager();
        try {
            pm.makePersistent(store);
            return store;
        }
        finally {
            pm.close();
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
        // Select the corresponding resources
        List<Store> stores = (List<Store>) queryObj.execute(value);
        stores.size(); // FIXME: remove workaround for a bug in DataNucleus
        return stores;
    }
}
