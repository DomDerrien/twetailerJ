package twetailer.j2ee.restlet;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.ArrayList;
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
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.j2ee.MaezelServlet;
import twetailer.validator.ApplicationSettings;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

@SuppressWarnings("serial")
public class ConsumerRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(ConsumerRestlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected DemandOperations demandOperations = _baseOperations.getDemandOperations();

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
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        Consumer consumer = null;
        if ("current".equals(resourceId)) {
            consumer = consumerOperations.getConsumer((Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID));
        }
        else {
            consumer = consumerOperations.getConsumer(Long.valueOf(resourceId));
        }
        return consumer.toJson();
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters) throws DataSourceException{
        // Get search criteria
        String queryAttribute = parameters.getString("qA");
        String queryValue = parameters.getString("qV");
        if (queryAttribute == null || queryAttribute.length() == 0) {
            queryAttribute = Consumer.EMAIL;
            if (queryValue == null) {
                queryValue = parameters.getString("q");
            }
        } // FIXME: verify the specified attribute name belongs to a list of authorized attributes
        // Select and return the corresponding consumers
        return JsonUtils.toJson(consumerOperations.getConsumers(queryAttribute, queryValue, 0));
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            // Get the identified consumer
            Long consumerKey = (Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID);
            if (!consumerKey.toString().equals(resourceId)) {
                throw new IllegalArgumentException("Consumer records can only be updated by the consumer themselves");
            }
            Consumer consumer = consumerOperations.getConsumer(pm, consumerKey);
            String openId = loggedUser.getClaimedId();
            if (!openId.equals(consumer.getOpenID())) {
                throw new IllegalArgumentException("Mismatch between the given OpenID and the one associated to the identified consumer");
            }

            // Verify the information about the third party access providers
            String newEmail = filterOutInvalidValue(parameters, Consumer.EMAIL, openId);
            String newJabberId = filterOutInvalidValue(parameters, Consumer.JABBER_ID, openId);
            String newTwitterId = filterOutInvalidValue(parameters, Consumer.TWITTER_ID, openId);

            // Update the consumer account
            consumer.fromJson(parameters);
            consumer = consumerOperations.updateConsumer(pm, consumer);


            // Move demands to the updated account
            if (newEmail != null || newJabberId != null || newTwitterId != null) {
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
                scheduleConsolidationTasks(Consumer.EMAIL, newEmail, consumer.getKey());
                scheduleConsolidationTasks(Consumer.JABBER_ID, newJabberId, consumer.getKey());
                scheduleConsolidationTasks(Consumer.TWITTER_ID, newTwitterId, consumer.getKey());
            }

            return consumer.toJson();
        }
        finally {
            pm.close();
        }
    }

    /**
     * Get the identified and validated attribute out of the request parameters
     *
     * @param parameters Request parameters with the updated Consumer's attributes
     * @param topic Identifier of the attribute to filter out if the validation does not match
     * @param openId Identifier used to cook the validation code
     * @return A validated identifier
     */
    protected String filterOutInvalidValue(JsonObject parameters, String topic, String openId) {
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
    protected void scheduleConsolidationTasks(String topic, String identifier, Long consumerKey) throws DataSourceException {
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
                    if (Consumer.EMAIL.equals(topic)) { otherConsumer.setEmail("~" + otherConsumer.getEmail()); }
                    else if (Consumer.JABBER_ID.equals(topic)) { otherConsumer.setJabberId("~" + otherConsumer.getJabberId()); }
                    else /* if (Consumer.TWITTER_ID.equals(topic)) */ { otherConsumer.setTwitterId("~" + otherConsumer.getTwitterId()); }
                    otherConsumer = consumerOperations.updateConsumer(pm, otherConsumer);

                    // Schedule tasks to migrate demands to this new consumer
                    List<Long> demandKeys = demandOperations.getDemandKeys(pm, Demand.OWNER_KEY, otherConsumer.getKey(), 1);
                    Queue queue = _baseOperations.getQueue();
                    for (Long demandKey: demandKeys) {
                        queue.add(
                                url(ApplicationSettings.get().getServletApiPath() + "/maezel/consolidateConsumerAccounts").
                                param(Demand.KEY, demandKey.toString()).
                                param(Demand.OWNER_KEY, consumerKey.toString()).
                                method(Method.GET)
                        );
                    }
                }
            }
            finally {
                pm.close();
            }
        }
    }
}
