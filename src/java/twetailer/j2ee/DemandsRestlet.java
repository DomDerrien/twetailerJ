package twetailer.j2ee;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;

import com.google.appengine.api.users.User;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class DemandsRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(DemandsRestlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
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
