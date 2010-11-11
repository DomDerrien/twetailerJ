package twetailer.j2ee.restlet;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.j2ee.MaelzelServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.ConsumerSteps;
import twetailer.validator.CommandSettings.Action;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

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
                consumerKey = LoginServlet.getConsumer(loggedUser, pm).getKey();
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
                    LoginServlet.getConsumer(loggedUser, pm)
            );
            return consumer.toJson();
        }
        finally {
            pm.close();
        }
    }

    /**** Dom: refactoring limit ***/

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws InvalidIdentifierException, ReservedOperationException {
        if (isUserAdmin) {
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                Long consumerKey = Long.valueOf(resourceId);
                delegateResourceDeletion(pm, consumerKey);
                return;
            }
            finally {
                pm.close();
            }
        }
        throw new ReservedOperationException(Action.delete, "Restricted access!");
    }

    /**
     * Delete the Consumer instances based on the specified criteria.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumerKey Identifier of the resource to delete
     * @return Serialized list of the Consumer instances matching the given criteria

     * @throws InvalidIdentifierException If the query to the back-end fails
     *
     * @see SaleAssociateRestlet#delegateResourceDeletion(PersistenceManager, Long)
     */
    protected void delegateResourceDeletion(PersistenceManager pm, Long consumerKey) throws InvalidIdentifierException{
        // Delete the consumer account
        Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, consumerKey);
        BaseSteps.getConsumerOperations().deleteConsumer(pm, consumer);
        // Delete consumer's demands
//        List<Long> demandKeys = BaseSteps.getDemandOperations().getDemandKeys(pm, Demand.OWNER_KEY, consumerKey, 0);
//        for (Long demandKey: demandKeys) {
//            demandRestlet.delegateResourceDeletion(pm, demandKey, consumerKey, false);
//        }
    }

    /**
     * Get the identified and validated attribute out of the request parameters
     *
     * @param parameters Request parameters with the updated Consumer's attributes
     * @param topic Identifier of the attribute to filter out if the validation does not match
     * @param openId Identifier used to cook the validation code
     * @return A validated identifier
     */
    protected static String filterOutInvalidValue(JsonObject parameters, String topic, String openId) {
        String identifier = null;
        if (parameters.containsKey(topic) && parameters.containsKey(topic + "Code")) {
            try {
                identifier = parameters.getString(topic);
                Long code = MaelzelServlet.getCode(topic, identifier, openId);
                if (!code.equals(parameters.getLong(topic + "Code"))) {
                    parameters.remove(topic);
                    identifier = null;
                }
            }
            catch(ClientException ex) {
                parameters.remove(topic);
                identifier = null;
            }
        }
        else {
            parameters.remove(topic);
        }
        return identifier;
    }

    /**
     * Use the identifier of the account to migrate to the current one to select its entity,
     * to neutralize its identifiers, and to schedule tasks that will updates its attached demands
     * to be now attached to the current account
     *
     * @param topic An identifier among {Consumer.EMAIL, Consumer.JABBER_ID, Consumer.TWITTER_ID}
     * @param identifier The value of the identified attribute
     * @param consumerKey The key of the current Consumer account
     *
     * @throws DataSourceException If the Consumer or Demand look-up fails
     */
    protected static void scheduleConsolidationTasks(String topic, String identifier, Long consumerKey) throws DataSourceException {
        if (identifier != null && !"".equals(identifier)) {
            if (!Consumer.EMAIL.equals(topic) && !Consumer.JABBER_ID.equals(topic) && !Consumer.TWITTER_ID.equals(topic)) {
                throw new IllegalArgumentException("Not supported field identifier: " + topic);
            }
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                List<Consumer> consumers = BaseSteps.getConsumerOperations().getConsumers(pm, topic, identifier, 1);
                if (0 < consumers.size()) {
                    // Reset other consumer field
                    Consumer otherConsumer = consumers.get(0);
                    if (!consumerKey.equals(otherConsumer.getKey())) {
                        if (Consumer.EMAIL.equals(topic)) { otherConsumer.setEmail("~" + otherConsumer.getEmail()); }
                        else if (Consumer.JABBER_ID.equals(topic)) { otherConsumer.setJabberId("~" + otherConsumer.getJabberId()); }
                        else /* if (Consumer.TWITTER_ID.equals(topic)) */ { otherConsumer.setTwitterId("~" + otherConsumer.getTwitterId()); }
                        otherConsumer = BaseSteps.getConsumerOperations().updateConsumer(pm, otherConsumer);

                        // Schedule tasks to migrate demands to this new consumer
                        List<Long> demandKeys = BaseSteps.getDemandOperations().getDemandKeys(pm, Demand.OWNER_KEY, otherConsumer.getKey(), 1);
                        Queue queue = BaseSteps.getBaseOperations().getQueue();
                        for (Long demandKey: demandKeys) {
                            queue.add(
                                    url("/_tasks/consolidateConsumerAccounts").
                                    param(Demand.KEY, demandKey.toString()).
                                    param(Demand.OWNER_KEY, consumerKey.toString()).
                                    method(Method.GET)
                            );
                        }
                    }
                }
            }
            finally {
                pm.close();
            }
        }
    }
}
