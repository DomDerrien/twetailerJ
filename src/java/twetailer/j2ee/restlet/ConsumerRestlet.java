package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.Consumer;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.ConsumerSteps;
import twetailer.validator.CommandSettings.Action;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the Consumer entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class ConsumerRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(ConsumerRestlet.class.getName());

    public Logger getLogger() { return log; }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Consumer consumer = null;
            if ("current".equals(resourceId)) {
                consumer = LoginServlet.getConsumer(loggedUser, pm);
            }
            else if (isUserAdmin) {
                consumer = ConsumerSteps.getConsumer(pm, Long.valueOf(resourceId));
            }
            else {
                throw new ReservedOperationException("Restricted access!");
            }

            return consumer.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            if (!isUserAdmin) {
                throw new ReservedOperationException("Restricted access!");
            }

            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) ConsumerSteps.getConsumerKeys(pm, parameters));
            }
            else { // full detail
                // Get the demands
                resources = JsonUtils.toJson(ConsumerSteps.getConsumers(pm, parameters));
            }
            return resources;
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ReservedOperationException {
        // Consumer instances are created automatically when users log in
        throw new ReservedOperationException("Consumer instances are created automatically by the connectors");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long consumerKey = null;
            if ("current".equals(resourceId)) {
                // Get the sale associate
                consumerKey = LoginServlet.getConsumerKey(loggedUser);
            }
            else if (isUserAdmin) {
                consumerKey = Long.valueOf(resourceId);
            }
            else {
                throw new ReservedOperationException("Restricted access!");
            }

            Consumer consumer = ConsumerSteps.updateConsumer(
                    pm,
                    consumerKey,
                    parameters,
                    isUserAdmin
            );

            return consumer.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws InvalidIdentifierException, ReservedOperationException, DataSourceException {
        throw new ReservedOperationException(Action.delete, "Restricted access!");
    }
}
