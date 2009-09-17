package twetailer.j2ee;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.users.User;
import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class DemandsRestlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(DemandsRestlet.class.getName());

    private BaseOperations _baseOperations = new BaseOperations();
    private DemandOperations demandOperations = _baseOperations.getDemandOperations();
    private ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
        
	@Override
	protected Logger getLogger() {
		return log;
	}

    /**
     * Setter for the unit tests
     * @param ops instance of a mock class
     */
    protected void setConsumerOperations(ConsumerOperations cOps) {
        consumerOperations = cOps;
    }

    /**
     * Setter for the unit tests
     * @param ops instance of a mock class
     */
    protected void setDemandOperations(DemandOperations dOps) {
        demandOperations = dOps;
    }

	@Override
	protected JsonObject createResource(JsonObject parameters, User loggedUser) throws DataSourceException, ClientException {
	    PersistenceManager pm = demandOperations.getPersistenceManager();
	    try {
    	    List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.EMAIL, loggedUser.getEmail(), 1);
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
	protected JsonObject updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}
}
