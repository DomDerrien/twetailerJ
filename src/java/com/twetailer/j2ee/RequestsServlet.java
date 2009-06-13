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
import com.twetailer.dto.Request;

@SuppressWarnings("serial")
public class RequestsServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(RequestsServlet.class.getName());

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
     * Create the Request instance with the given parameters
     * 
     * @param in HTTP request parameters
     * @param consumer Consumer accout to be associated with the request
     * @return Identifier of the just created resource
     * 
     * @throws ParseException If the data extraction fails
     * @throws ClientException If the data given by the client are incorrect
     * @throws DataSourceException If error reported when trying to create the request record
     */
	protected Long createRequest(JsonObject parameters, Consumer consumer) throws ParseException, ClientException {
        getLogger().warning("Create request for consumer id: " + consumer.getKey() + " with: " + parameters.toString());
    	PersistenceManager pm = getPersistenceManager();
    	try {
    		// Creates new request record and persist it
    		Request newRequest = new Request(parameters);
    		Long consumerId = newRequest.getConsumerKey();
    		if (consumerId == null) {
    			newRequest.setConsumerKey(consumer.getKey());
    		}
    		else if (consumerId != consumer.getKey()) {
    			throw new ClientException("Mismatch of consumer identifiers [" + consumerId + "/" + consumer.getKey() + "]");
    		}
    		pm.makePersistent(newRequest);
    		// Return the identifier of the just created request
        	return newRequest.getKey();
    	}
    	finally {
    		pm.close();
    	}
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Request instances
     * 
     * @param attribute Name of the request attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @return Collection of requests matching the given criteria
     */
    protected List<Request> getRequests(String attribute, String value) throws DataSourceException {
        return getRequests(attribute + " == \"" + value + "\"");
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Request instances
     * 
     * @param attribute Name of the request attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @return Collection of requests matching the given criteria
     */
    protected List<Request> getRequests(String attribute, Long value) throws DataSourceException {
        return getRequests(attribute + " == " + value);
    }
    
    @SuppressWarnings("unchecked")
    private List<Request> getRequests(String whereClause) throws DataSourceException {
    	PersistenceManager pm = getPersistenceManager();
    	try {
    		// Prepare the query
	    	String queryStr = "select from " + Request.class.getName();
            queryStr += " where " + whereClause;
            queryStr += " order by creationDate desc";
			Query queryObj = pm.newQuery(queryStr);
			getLogger().warning("Select request(s) with: " + (queryObj == null ? "null" : queryObj.toString()));
	    	// Select the corresponding users
			List<Request> requests = queryObj == null ? null : (List<Request>) queryObj.execute();
			requests.size(); // FIXME: remove workaround for a bug in DataNucleus
	    	return requests;
	    	// return queryObj == null ? null : (List<Consumer>) queryObj.execute();
    	}
    	finally {
    		pm.close();
    	}
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Request instance for the identified consumer
     * 
     * @param key Identifier of the request
     * @param consumerKey Identifier of the request owner
     * @return First request matching the given criteria or <code>null</code>
     * @throws ClientException If the retrieved request does not belong to the specified user
     * @throws ParseException s
     */
    protected Request getRequest(Long key, Long consumerKey) throws DataSourceException, ClientException, ParseException {
        getLogger().warning("Get request with id: " + key);
        PersistenceManager pm = getPersistenceManager();
        try {
            Request request = pm.getObjectById(Request.class, key);
            if (request == null) {
                throw new ClientException("No request for identifier: " + key);
            }
            if (consumerKey != request.getConsumerKey()) {
                throw new ClientException("Mismatch of consumer identifiers [" + consumerKey + "/" + request.getConsumerKey() + "]");
            }
            // return request; // FIXME: remove workaround for a bug in DataNucleus
            return new Request(request.toJson());
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Request instance for the identified consumer
     * 
     * @param key Identifier of the request
     * @param consumerKey Identifier of the request owner
     * @return First request matching the given criteria or <code>null</code>
     * @throws ClientException If the retrieved request does not belong to the specified user
     */
    protected void deleteRequest(Long key, Long consumerKey) throws DataSourceException, ClientException {
        getLogger().warning("Delete request with id: " + key);
        PersistenceManager pm = getPersistenceManager();
        try {
            Request request = pm.getObjectById(Request.class, key);
            if (request == null) {
                throw new ClientException("No request for identifier: " + key);
            }
            if (consumerKey != request.getConsumerKey()) {
                throw new ClientException("Mismatch of consumer identifiers [" + consumerKey + "/" + request.getConsumerKey() + "]");
            }
            pm.deletePersistent(request);
        }
        finally {
            pm.close();
        }
    }
}
