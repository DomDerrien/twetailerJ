package com.twetailer.j2ee;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.users.User;
import com.twetailer.ClientException;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.rest.BaseOperations;
import com.twetailer.rest.ConsumerOperations;
import com.twetailer.rest.DemandOperations;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class DemandsServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(DemandsServlet.class.getName());

    private BaseOperations _baseOperations = new BaseOperations();
    private DemandOperations demandOperations = _baseOperations.getDemandOperation();
    private ConsumerOperations consumerOperations = _baseOperations.getConsumerOperation();
        
	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected JsonObject createResource(JsonObject parameters, User loggedUser) throws DataSourceException, ClientException {
	    PersistenceManager pm = _baseOperations.getPersistenceManager();
	    try {
    	    List<Consumer> consumers = consumerOperations.getConsumers(pm, "email", loggedUser.getEmail(), 1);
    	    if (0 < consumers.size()) {
    	        Demand demand = demandOperations.createDemand(pm, parameters, consumers.get(0).getKey());
    	        return demand.toJson();
    	    }
    	    return null;
	    }
	    finally {
	        pm.close();
	    }
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
}
