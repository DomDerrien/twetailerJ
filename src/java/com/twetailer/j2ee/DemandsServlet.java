package com.twetailer.j2ee;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.ClientException;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.validator.LocaleValidator;

@SuppressWarnings("serial")
public class DemandsServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(DemandsServlet.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected JsonObject createResource(JsonObject parameters, User loggedUser) throws DataSourceException, ClientException {
	    Consumer consumer = new ConsumersServlet().getConsumer("email", loggedUser.getEmail());
	    Demand demand = createDemand(parameters, consumer.getKey());
		return demand.toJson();
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
     * @param parameters HTTP demand parameters
     * @param consumerKey Identifier of the demand owner
     * @return Just created resource
     * 
     * @throws ClientException If the data given by the client are incorrect
     * 
     * @see DemandsServlet#createDemand(Demand)
     */
	public Demand createDemand(JsonObject parameters, Long consumerKey) throws ClientException {
        getLogger().warning("Create demand for consumer id: " + consumerKey + " with: " + parameters.toString());
        // Creates new demand record and persist it
        Demand newDemand = new Demand(parameters);
        // Validate the location
        LocaleValidator.getGeoCoordinates(newDemand);
        // Validate the state
        newDemand.checkForCompletion();
        // Updates the identifier of the creator consumer
        Long consumerId = newDemand.getConsumerKey();
        if (consumerId == null || consumerId == 0L) {
            newDemand.setConsumerKey(consumerKey);
        }
        else if (!consumerKey.equals(consumerId)) {
            throw new ClientException("Mismatch of consumer identifiers [" + consumerId + "/" + consumerKey + "]");
        }
        // Persist it
        return createDemand(newDemand);
    }

    /**
     * Create the Demand instance with the given parameters
     * 
     * @param demand Resource to persist
     * @return Just created resource
     */
    public Demand createDemand(Demand demand) {
        PersistenceManager pm = getPersistenceManager();
        try {
            pm.makePersistent(demand);
            return demand;
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
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of demands matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     * 
     * @see DemandsServlet#getDemands(PersistenceManager, String, Object)
     */
    public List<Demand> getDemands(String attribute, Object value, int limit) throws DataSourceException {
    	PersistenceManager pm = getPersistenceManager();
        try {
            return getDemands(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instances while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of demands matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Demand> getDemands(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
		// Prepare the query
        Query queryObj = pm.newQuery(Demand.class);
        prepareQuery(queryObj, attribute, value, limit);
    	// Select the corresponding resources
		List<Demand> demands = (List<Demand>) queryObj.execute(value);
		demands.size(); // FIXME: remove workaround for a bug in DataNucleus
    	return demands;
    }
    
    @SuppressWarnings("unchecked")
    public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        log.warning("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*  111");
        StringBuilder filterDefinition = new StringBuilder();
        StringBuilder parameterDefinitions = new StringBuilder();
        List<Object> values = new ArrayList<Object>(parameters.size());
        log.warning("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*  222");
        for(String parameterName: parameters.keySet()) {
            filterDefinition.append(" && " + parameterName + " == " + parameterName + "Value");
            parameterDefinitions.append(", ");
            Object parameterValue = parameters.get(parameterName);
            if (parameterValue instanceof String) { parameterDefinitions.append("String " + parameterName + "Value"); }
            else if (parameterValue instanceof Long) { parameterDefinitions.append("Long " + parameterName + "Value"); }
            else if (parameterValue instanceof Integer) {
                parameterDefinitions.append("Long " + parameterName + "Value");
                parameterValue = Long.valueOf((Integer) parameterValue);
            }
            else if (parameterValue instanceof Date) { parameterDefinitions.append("Date " + parameterName + "Value"); }
            values.add(parameterValue);
        }
        log.warning("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*  333");
        Query queryObj = pm.newQuery(Demand.class);
        queryObj.setFilter(filterDefinition.substring(" && ".length()));
        queryObj.declareParameters(parameterDefinitions.substring(", ".length()));
        if (0 < limit) {
            queryObj.setRange(0, limit);
        }
        log.warning("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*  444 -- " + queryObj.toString());
        getLogger().warning("Select demand(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Demand> demands = (List<Demand>) queryObj.executeWithArray(values);
        demands.size(); // FIXME: remove workaround for a bug in DataNucleus
        return demands;
    }
    
    /**
     * Use the given reference to get the corresponding Demand instance for the identified consumer
     * 
     * @param key Identifier of the demand
     * @param consumerKey Identifier of the demand owner
     * @return First demand matching the given criteria or <code>null</code>
     * 
     * @throws ClientException If the retrieved demand does not belong to the specified user
     * 
     * @see DemandsServlet#getDemand(PersistenceManager, Long, Long)
     */
    public Demand getDemand(Long key, Long consumerKey) throws ClientException {
        getLogger().warning("Get demand with id: " + key);
        PersistenceManager pm = getPersistenceManager();
        try {
            return getDemand(pm, key, consumerKey);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Use the given reference to get the corresponding Demand instance for the identified consumer while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the demand
     * @param consumerKey Identifier of the demand owner
     * @return First demand matching the given criteria or <code>null</code>
     * 
     * @throws ClientException If the retrieved demand does not belong to the specified user
     */
    public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws ClientException {
        Demand demand = pm.getObjectById(Demand.class, key);
        if (demand == null) {
            throw new ClientException("No demand for identifier: " + key);
        }
        if (!consumerKey.equals(demand.getConsumerKey())) {
            throw new ClientException("Mismatch of consumer identifiers [" + consumerKey + "/" + demand.getConsumerKey() + "]");
        }
        demand.getCriteria().size();
        return demand;
    }
    
    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance and to delete it
     * 
     * @param key Identifier of the demand
     * @param consumerKey Identifier of the demand owner
     * 
     * @throws ClientException If the retrieved demand does not belong to the specified user
     * 
     * @see DemandsServlet#getDemands(PersistenceManager, Long, Long)
     * @see DemandsServlet#deleteDemand(PersistenceManager, Demand, Long)
     */
    public void deleteDemand(Long key, Long consumerKey) throws ClientException {
        getLogger().warning("Delete demand with id: " + key);
        PersistenceManager pm = getPersistenceManager();
        try {
            Demand demand = getDemand(pm, key, consumerKey);
            deleteDemand(pm, demand, consumerKey);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Delete the given demand while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the demand
     * @param consumerKey Identifier of the demand owner
     */
    public void deleteDemand(PersistenceManager pm, Demand demand, Long consumerKey) {
        pm.deletePersistent(demand);
    }
    
    /**
     * Load the demand matching the given parameters and persist the result of the merge
     * 
     * @param parameters List of updated attributes, plus the resource identifier (cannot be changed)
     * @param consumerKey Identifier of the consumer issuing the operation
     * @return Updated resource
     * 
     * @throws ParseException If the given parameters cannot be transfered to the resource
     * @throws ClientException If the identified resource does not belong to the issuing consumer
     * 
     * @see DemandsServlet#updateDemand(PersistenceManager, Demand)
     */
    public Demand updateDemand(JsonObject parameters, Long consumerKey) throws ParseException, ClientException {
        PersistenceManager pm = getPersistenceManager();
        try {
            Demand updatedDemand = getDemand(pm, parameters.getLong(Demand.KEY), consumerKey);
            updatedDemand.fromJson(parameters);
            Long consumerId = updatedDemand.getConsumerKey();
            if (!consumerKey.equals(consumerId)) {
                throw new ClientException("Mismatch of consumer identifiers [" + consumerId + "/" + consumerKey + "]");
            }
            // Validate the state
            updatedDemand.checkForCompletion();
            // Persist updated demand
            return updateDemand(pm, updatedDemand);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Persist the given (probably updated) resource
     * 
     * @param demand Resource to update
     * @return Updated resource
     * 
     * @see DemandsServlet#updateDemand(PersistenceManager, Demand)
     */
    public Demand updateDemand(Demand demand) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Validate the state
            demand.checkForCompletion();
            // Persist updated demand
            return updateDemand(pm, demand);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demand Resource to update
     * @return Updated resource
     */
    public Demand updateDemand(PersistenceManager pm, Demand demand) {
        getLogger().warning("Updating demand with id: " + demand.getKey());
        pm.makePersistent(demand);
        return demand;
    }
}
