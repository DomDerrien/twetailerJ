package com.twetailer.j2ee;

import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.ClientException;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;

@SuppressWarnings("serial")
public class DemandsServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(DemandsServlet.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected String createResource(JsonObject parameters, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	protected void deleteResource(String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	protected JsonObject getResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	protected void updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}
	
    /**
     * Create the Demand instance with the given parameters
     * 
     * @param in HTTP demand parameters
     * @param consumer Consumer accout to be associated with the demand
     * @return Identifier of the just created resource
     * 
     * @throws ParseException If the data extraction fails
     * @throws ClientException If the data given by the client are incorrect
     * @throws DataSourceException If error reported when trying to create the demand record
     */
	public Long createDemand(JsonObject parameters, Consumer consumer) throws ParseException, ClientException {
        getLogger().warning("Create demand for consumer id: " + consumer.getKey() + " with: " + parameters.toString());
    	PersistenceManager pm = getPersistenceManager();
    	try {
    		// Creates new demand record and persist it
    		Demand newDemand = new Demand(parameters);
    		Long consumerId = newDemand.getConsumerKey();
    		if (consumerId == null) {
    			newDemand.setConsumerKey(consumer.getKey());
    		}
    		else if (consumerId != consumer.getKey()) {
    			throw new ClientException("Mismatch of consumer identifiers [" + consumerId + "/" + consumer.getKey() + "]");
    		}
    		pm.makePersistent(newDemand);
    		// Return the identifier of the just created demand
        	return newDemand.getKey();
    	}
    	finally {
    		pm.close();
    	}
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instances
     * 
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @return Collection of demands matching the given criteria
     */
    @SuppressWarnings("unchecked")
    public List<Demand> getDemands(String attribute, Object value) throws DataSourceException {
    	PersistenceManager pm = getPersistenceManager();
    	try {
    		// Prepare the query
	    	String queryStr = "select from " + Demand.class.getName();
            if (value instanceof String) {
                queryStr += " where " + attribute + " == '" + value + "'";
            }
            else if (value instanceof Long) {
                queryStr += " where " + attribute + " == " + value;
            }
            else {
                throw new DataSourceException("Unsupported criteruia value type");
            }
            queryStr += " order by creationDate desc";
			Query queryObj = pm.newQuery(queryStr);
			getLogger().warning("Select demand(s) with: " + (queryObj == null ? "null" : queryObj.toString()));
	    	// Select the corresponding users
			List<Demand> demands = queryObj == null ? null : (List<Demand>) queryObj.execute();
			demands.size(); // FIXME: remove workaround for a bug in DataNucleus
	    	return demands;
	    	// return queryObj == null ? null : (List<Consumer>) queryObj.execute();
    	}
    	finally {
    		pm.close();
    	}
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance for the identified consumer
     * 
     * @param key Identifier of the demand
     * @param consumerKey Identifier of the demand owner
     * @return First demand matching the given criteria or <code>null</code>
     * @throws ClientException If the retrieved demand does not belong to the specified user
     * @throws ParseException s
     */
    protected Demand getDemand(Long key, Long consumerKey) throws DataSourceException, ClientException, ParseException {
        getLogger().warning("Get demand with id: " + key);
        PersistenceManager pm = getPersistenceManager();
        try {
            Demand demand = pm.getObjectById(Demand.class, key);
            if (demand == null) {
                throw new ClientException("No demand for identifier: " + key);
            }
            if (consumerKey != demand.getConsumerKey()) {
                throw new ClientException("Mismatch of consumer identifiers [" + consumerKey + "/" + demand.getConsumerKey() + "]");
            }
            // return demand; // FIXME: remove workaround for a bug in DataNucleus
            return new Demand(demand.toJson());
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance for the identified consumer
     * 
     * @param key Identifier of the demand
     * @param consumerKey Identifier of the demand owner
     * @return First demand matching the given criteria or <code>null</code>
     * @throws ClientException If the retrieved demand does not belong to the specified user
     */
    public void deleteDemand(Long key, Long consumerKey) throws DataSourceException, ClientException {
        getLogger().warning("Delete demand with id: " + key);
        PersistenceManager pm = getPersistenceManager();
        try {
            Demand demand = pm.getObjectById(Demand.class, key);
            if (demand == null) {
                throw new ClientException("No demand for identifier: " + key);
            }
            if (consumerKey != demand.getConsumerKey()) {
                throw new ClientException("Mismatch of consumer identifiers [" + consumerKey + "/" + demand.getConsumerKey() + "]");
            }
            pm.deletePersistent(demand);
        }
        finally {
            pm.close();
        }
    }
}
