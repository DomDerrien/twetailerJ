package twetailer.j2ee.restlet;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.j2ee.MaezelServlet;
import twetailer.validator.ApplicationSettings;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

@SuppressWarnings("serial")
public class ConsumerRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(ConsumerRestlet.class.getName());

    protected static DemandRestlet demandRestlet = new DemandRestlet();

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
        // Consumer instances are created automatically when users log in
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                Long consumerKey = Long.valueOf(resourceId);
                delegateResourceDeletion(pm, consumerKey);
                return;
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
    }

    /**
     * Delete the Consumer instances based on the specified criteria.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumerKey Identifier of the resource to delete
     * @return Serialized list of the Consumer instances matching the given criteria

     * @throws DataSourceException If the query to the back-end fails
     *
     * @see SaleAssociateRestlet#delegateResourceDeletion(PersistenceManager, Long)
     */
    protected void delegateResourceDeletion(PersistenceManager pm, Long consumerKey) throws DataSourceException{
        // Delete the consumer account
        Consumer consumer = consumerOperations.getConsumer(pm, consumerKey);
        consumerOperations.deleteConsumer(pm, consumer);
        // Delete consumer's demands
        List<Long> demandKeys = demandOperations.getDemandKeys(pm, Demand.OWNER_KEY, consumerKey, 0);
        for (Long demandKey: demandKeys) {
            demandRestlet.delegateResourceDeletion(pm, demandKey, consumerKey, false);
        }
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        Consumer consumer = null;
        if ("current".equals(resourceId)) {
            consumer = consumerOperations.getConsumer(LoginServlet.getConsumerKey(loggedUser));
        }
        else if (isAPrivilegedUser(loggedUser)) {
            consumer = consumerOperations.getConsumer(Long.valueOf(resourceId));
        }
        else {
            throw new ClientException("Restricted access!");
        }
        return consumer.toJson();
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                return delegateResourceSelection(pm, parameters);
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
    }

    /**
     * Retrieve the Consumer instances based on the specified criteria.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Attribute coming from a client over HTTP
     * @return Serialized list of the Consumer instances matching the given criteria

     * @throws DataSourceException If the query to the back-end fails
     */
    protected JsonArray delegateResourceSelection(PersistenceManager pm, JsonObject parameters) throws DataSourceException {
        if (parameters.containsKey(Consumer.EMAIL)) {
            // Expects only one Consumer record matching the given e-mail address
            return JsonUtils.toJson(consumerOperations.getConsumers(pm, Consumer.EMAIL, parameters.getString(Consumer.EMAIL), 1));
        }
        if (parameters.containsKey(Consumer.JABBER_ID)) {
            // Expects only one Consumer record matching the given Jabber/XMPP identifier
            return JsonUtils.toJson(consumerOperations.getConsumers(pm, Consumer.JABBER_ID, parameters.getString(Consumer.JABBER_ID), 1));
        }
        if (parameters.containsKey(Consumer.TWITTER_ID)) {
            // Expects only one Consumer record matching the given Twitter name
            return JsonUtils.toJson(consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, parameters.getString(Consumer.TWITTER_ID), 1));
        }
        return new GenericJsonArray();
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        boolean isAdminControlled = isAPrivilegedUser(loggedUser);

        // Get the logged user information
        Long consumerKey = null;
        if (isAdminControlled) {
            consumerKey = Long.valueOf(resourceId);
        }
        else {
            consumerKey = LoginServlet.getConsumerKey(loggedUser);
            if (!resourceId.equals(consumerKey.toString())) {
                throw new ClientException("Consumer records can only be updated by the consumers themselves");
            }
        }

        // Verify the information about the third party access providers
        String openId = loggedUser.getClaimedId();
        String newEmail = null, newJabberId = null, newTwitterId = null;
        if (!isAdminControlled) {
            newEmail = filterOutInvalidValue(parameters, Consumer.EMAIL, openId);
            newJabberId = filterOutInvalidValue(parameters, Consumer.JABBER_ID, openId);
            newTwitterId = filterOutInvalidValue(parameters, Consumer.TWITTER_ID, openId);
        }

        PersistenceManager pm = _baseOperations.getPersistenceManager();
        Consumer consumer;
        try {
            // Update the consumer account
            consumer = consumerOperations.getConsumer(pm, consumerKey);
            consumer.fromJson(parameters);
            consumer = consumerOperations.updateConsumer(pm, consumer);
        }
        finally {
            pm.close();
        }

        // Move demands to the updated account
        if (!isAdminControlled && (newEmail != null || newJabberId != null || newTwitterId != null)) {
            /*
            Warning:
            --------
            Cannot pass the connection to the following operations because they are
            possibly going to affect different Consumer entities! And the JDO layer
            will throw an exception like the following one:
                Exception thrown: javax.jdo.JDOFatalUserException: Illegal argument
                NestedThrowables: java.lang.IllegalArgumentException: can't operate on multiple entity groups in a single transaction.
                    Found both Element {
                      type: "Consumer"
                      id: 425
                    }
                    and Element {
                      type: "Consumer"
                      id: 512
                    }
             */
            scheduleConsolidationTasks(Consumer.EMAIL, newEmail, consumerKey);
            scheduleConsolidationTasks(Consumer.JABBER_ID, newJabberId, consumerKey);
            scheduleConsolidationTasks(Consumer.TWITTER_ID, newTwitterId, consumerKey);
        }

        return consumer.toJson();
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
                Long code = MaezelServlet.getCode(topic, identifier, openId);
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
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                List<Consumer> consumers = consumerOperations.getConsumers(pm, topic, identifier, 1);
                if (0 < consumers.size()) {
                    // Reset other consumer field
                    Consumer otherConsumer = consumers.get(0);
                    if (!consumerKey.equals(otherConsumer.getKey())) {
                        if (Consumer.EMAIL.equals(topic)) { otherConsumer.setEmail("~" + otherConsumer.getEmail()); }
                        else if (Consumer.JABBER_ID.equals(topic)) { otherConsumer.setJabberId("~" + otherConsumer.getJabberId()); }
                        else /* if (Consumer.TWITTER_ID.equals(topic)) */ { otherConsumer.setTwitterId("~" + otherConsumer.getTwitterId()); }
                        otherConsumer = consumerOperations.updateConsumer(pm, otherConsumer);

                        // Schedule tasks to migrate demands to this new consumer
                        List<Long> demandKeys = demandOperations.getDemandKeys(pm, Demand.OWNER_KEY, otherConsumer.getKey(), 1);
                        Queue queue = _baseOperations.getQueue();
                        for (Long demandKey: demandKeys) {
                            log.warning("Preparing the task: /maezel/consolidateConsumerAccounts?key=" + demandKey.toString() + "&ownerKey=" + consumerKey.toString());
                            queue.add(
                                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/consolidateConsumerAccounts").
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
