package com.twetailer.j2ee;

import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.users.User;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;
import com.twetailer.rest.BaseOperations;
import com.twetailer.rest.ConsumerOperations;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

@SuppressWarnings("serial")
public class ConsumersServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(ConsumersServlet.class.getName());

    private BaseOperations _baseOperations = new BaseOperations();
    private ConsumerOperations consumerOperations = _baseOperations.getConsumerOperation();
        
	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected JsonObject createResource(JsonObject parameters, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	protected void deleteResource(String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}
	
	@Override
	protected JsonObject getResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
    	Consumer consumer = null;
    	if ("/current".equals(resourceId)) {
	        List<Consumer> consumers = consumerOperations.getConsumers(Consumer.EMAIL, loggedUser.getEmail(), 1);
	        if (0 < consumers.size()) {
	            consumer = consumers.get(0);
	        }
    	}
    	else {
    	    consumer = consumerOperations.getConsumer(Long.valueOf(resourceId));
    	}
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
    	return JsonUtils.toJson(consumerOperations.getConsumers(queryAttribute, queryValue, 0));
	}

	@Override
	protected void updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}
}
