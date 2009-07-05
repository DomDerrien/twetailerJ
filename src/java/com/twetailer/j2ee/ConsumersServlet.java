package com.twetailer.j2ee;

import java.text.ParseException;
import java.util.Date;
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

@SuppressWarnings("serial")
public class ConsumersServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(ConsumersServlet.class.getName());

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
    	Consumer consumer = "/current".equals(resourceId) ?
    			getConsumer("email", loggedUser.getEmail()) :
				getConsumer("key", resourceId);
    	if (consumer == null) {
    		throw new DataSourceException("No Consumer resource matches the criteria");
    	}
    	return consumer.toJson();
	}
	
	@Override
	protected JsonArray selectResources(JsonObject parameters) throws DataSourceException{
    	// Get search criteria
    	String queryAttribute = parameters.getString("qA");
    	String queryValue = parameters.getString("qV");
    	if (queryAttribute == null || queryAttribute.length() == 0) {
    		queryAttribute = "email";
    		if (queryValue == null) {
    			queryValue = parameters.getString("q");
    		}
    	} // FIXME: verify the specified attribute name belongs to a list of authorized attributes
    	// Select and return the corresponding consumers
    	return Utils.toJson(getConsumers(queryAttribute, queryValue));
	}

	@Override
	protected void updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     * 
     * @param loggedUser System entity to attach with the just created user
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     * 
     * @throws DataSourceException Forward error reported when trying to get a consumer record
     */
    public Consumer createConsumer(User loggedUser) throws DataSourceException {
    	Consumer existingConsumer = getConsumer("email", loggedUser.getEmail());
        PersistenceManager pm = getPersistenceManager();
    	if (existingConsumer == null) {
	    	try {
	    		// Creates new consumer record and persist it
	    		existingConsumer = new Consumer();
	    		existingConsumer.setSystemUser(loggedUser);
    		    existingConsumer.setName(loggedUser.getNickname());
    		    existingConsumer.setEmail(loggedUser.getEmail());
        		pm.makePersistent(existingConsumer);
	    	}
	    	finally {
	    		pm.close();
	    	}
    	}
    	else {
    		if (existingConsumer.getSystemUser() == null) {
    	    	try {
    	    		// Update existing consumer with system user record
    	    		existingConsumer.setSystemUser(loggedUser);
            		pm.makePersistent(existingConsumer);
    	    	}
    	    	finally {
    	    		pm.close();
    	    	}
    		}
    		else if (existingConsumer.getSystemUser() != loggedUser) {
        		throw new DataSourceException("Just retrieved a Consumer instance that does not correspond to the logged user");
    		}
    	}
    	return existingConsumer;
    }

    /**
     * Create the Consumer instance
     * 
     * @param twitterId Twitter identifier to be used to identify the new consumer account
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     * 
     * @throws DataSourceException Forward error reported when trying to get a consumer record
     */
    public Consumer createConsumer(twitter4j.User twitterUser) throws DataSourceException {
        Consumer existingConsumer = getConsumer("twitterId", twitterUser.getId());
        if (existingConsumer == null) {
            getLogger().warning("Create consumer account for: " + twitterUser.getScreenName() + " [" + twitterUser.getId() + "]");
            PersistenceManager pm = getPersistenceManager();
            try {
                existingConsumer = new Consumer();
                existingConsumer.setName(twitterUser.getName());
                existingConsumer.setAddress(twitterUser.getLocation());
                existingConsumer.setTwitterId(Long.valueOf(twitterUser.getId()));
                pm.makePersistent(existingConsumer);
            }
            finally {
                pm.close();
            }
        }
        return existingConsumer;
    }

    /**
     * Use the given key to get the corresponding Consumer instance
     * 
     * @param key Identifier of the consumer
     * @return First demand matching the given criteria or <code>null</code>
     * @throws ClientException If the retrieved demand does not belong to the specified user
     * @throws ParseException s
     */
    public Consumer getConsumer(Long key) throws DataSourceException, ClientException, ParseException {
        getLogger().warning("Get consumer with id: " + key);
        PersistenceManager pm = getPersistenceManager();
        try {
            Consumer consumer = pm.getObjectById(Consumer.class, key);
            if (consumer == null) {
                throw new ClientException("No consumer for identifier: " + key);
            }
            // return consumer; // FIXME: remove workaround for a bug in DataNucleus
            return new Consumer(consumer.toJson());
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Consumer instance
     * 
     * @param attribute Name of the consumer attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @return Consumer matching the search criteria
     * 
     * @throws DataSourceException if the expected consumer is not found, or if too many consumers match the criteria
     */
    public Consumer getConsumer(String attribute, Object value) throws DataSourceException {
        // Select the corresponding consumers
        List<Consumer> consumers = getConsumers(attribute, value);
        // Report the possible problems
        if (consumers == null || consumers.size() == 0) {
            return null;
        }
        if (1 < consumers.size()) {
            throw new DataSourceException("Abnormal number of returned Consumer resources: " + consumers.size());
        }
        return consumers.get(0);
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Consumer instances
     * 
     * @param attribute Name of the consumer attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @return Collection of consumers matching the given criteria
     */
    @SuppressWarnings("unchecked")
	public List<Consumer> getConsumers(String attribute, Object value) throws DataSourceException {
    	PersistenceManager pm = getPersistenceManager();
    	try {
    		// Prepare the query
            Query queryObj = pm.newQuery(Consumer.class);
            queryObj.setFilter(attribute + " == value");
            queryObj.setOrdering("creationDate desc");
            if (value instanceof String) {
                queryObj.declareParameters("String value");
            }
            else if (value instanceof Long) {
                queryObj.declareParameters("Long value");
            }
            else if (value instanceof Date) {
                queryObj.declareParameters("Date value");
            }
            else {
                throw new DataSourceException("Unsupported criteria value type: " + value.getClass());
            }
			getLogger().warning("Select consumer(s) with: " + (queryObj == null ? "null" : queryObj.toString()));
	    	// Select the corresponding consumers
			List<Consumer> consumers = (List<Consumer>) queryObj.execute(value);
			if (consumers != null) {
				consumers.size(); // FIXME: remove workaround for a bug in DataNucleus
			}
	    	return consumers;
	    	// return queryObj == null ? null : (List<Consumer>) queryObj.execute();
    	}
    	finally {
    		pm.close();
    	}
    }
}
