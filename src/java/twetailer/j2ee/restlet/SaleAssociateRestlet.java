package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.SaleAssociate;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.SaleAssociateSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the SaleAssociate entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class SaleAssociateRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(SaleAssociateRestlet.class.getName());

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            SaleAssociate saleAssociate = null;
            if ("current".equals(resourceId)) {
                // Get the sale associate
                saleAssociate = LoginServlet.getSaleAssociate(loggedUser, pm);
                if (saleAssociate == null) {
                    throw new ReservedOperationException("Current user is not a Sale Associate!");
                }
            }
            else if (isAPrivilegedUser(loggedUser)) {
                saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, Long.valueOf(resourceId));
            }
            else {
                // TODO: enable a store manager to get information about the sale associates for the store
                throw new ReservedOperationException("Restricted access!");
            }

            return saleAssociate.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            if (!isAPrivilegedUser(loggedUser)) {
                throw new ReservedOperationException("Restricted access!");
            }

            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) SaleAssociateSteps.getSaleAssociateKeys(pm, parameters));
            }
            else { // full detail
                // Get the demands
                resources = JsonUtils.toJson(SaleAssociateSteps.getSaleAssociates(pm, parameters));
            }
            return resources;
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            boolean isAPrivilegedUser = isAPrivilegedUser(loggedUser);
            SaleAssociate saleAssociate = SaleAssociateSteps.createSaleAssociate(
                    pm,
                    parameters,
                    LoginServlet.getConsumer(loggedUser),
                    isAPrivilegedUser ? null : LoginServlet.getSaleAssociate(loggedUser),
                    isAPrivilegedUser
            );
            return saleAssociate.toJson();
        }
        finally {
            pm.close();
        }
    }

    /**** Dom: refactoring limit ***/

    /**
     * Create Consumer and SaleAssociate instances for the given parameters if
     * it's sure there no such existing object with identical parameters.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Attribute coming from a client over HTTP
     * @return Serialized SaleAssociate instance

     * @throws DataSourceException If there is already some associate with one or many identical attributes
     * or if the found sale associate instance is already attached to another store
     */
    protected JsonObject delegateResourceCreation(PersistenceManager pm, JsonObject parameters) throws DataSourceException {
        return null;
    }
    /**********
        Long storeKey = parameters.getLong(SaleAssociate.STORE_KEY);
        if (storeKey == 0L) {
            throw new IllegalArgumentException("StoreKey for the SaleAssociate is mandatory");
        }

        Long consumerKey = parameters.containsKey(SaleAssociate.CONSUMER_KEY) ? parameters.getLong(SaleAssociate.CONSUMER_KEY) : null;
        String email = parameters.containsKey(SaleAssociate.EMAIL) ? parameters.getString(SaleAssociate.EMAIL) : null;
        String jabberId = parameters.containsKey(SaleAssociate.JABBER_ID) ? parameters.getString(SaleAssociate.JABBER_ID) : null;
        String twitterId = parameters.containsKey(SaleAssociate.TWITTER_ID) ? parameters.getString(SaleAssociate.TWITTER_ID) : null;

        SaleAssociate candidateSaleAssociate = null;

        if (consumerKey != null) {
            Consumer consumer = consumerOperations.getConsumer(pm, consumerKey);
            consumerKey = consumer.getKey();
            List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
            if (0 < saleAssociates.size()) {
                SaleAssociate saleAssociate = saleAssociates.get(0);
                if (!saleAssociate.getStoreKey().equals(storeKey)) {
                    throw new DataSourceException("Sale Associate already attached to another store");
                }
                candidateSaleAssociate = saleAssociate;
            }
        }

        if (email != null && 0 < email.length()) {
            List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.EMAIL, email, 1);
            if (0 < consumers.size()) {
                if (consumerKey != null && !consumerKey.equals(consumers.get(0).getKey())) {
                    throw new DataSourceException("At least two different Consumer instances match the given criteria");
                }
                consumerKey = consumers.get(0).getKey();
                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
                if (0 < saleAssociates.size()) {
                    SaleAssociate saleAssociate = saleAssociates.get(0);
                    if (!saleAssociate.getStoreKey().equals(storeKey)) {
                        throw new DataSourceException("Sale Associate already attached to another store");
                    }
                    if (candidateSaleAssociate != null && !candidateSaleAssociate.getKey().equals(saleAssociate.getKey())) {
                        throw new DataSourceException("At least two different Sale Associate instances match the given criteria");
                    }
                    candidateSaleAssociate = saleAssociate;
                }
            }
            else {
                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.EMAIL, email, 1);
                if (0 < saleAssociates.size()) {
                    SaleAssociate saleAssociate = saleAssociates.get(0);
                    if (!saleAssociate.getStoreKey().equals(storeKey)) {
                        throw new DataSourceException("Sale Associate already attached to another store");
                    }
                    if (consumerKey != null && !consumerKey.equals(saleAssociate.getConsumerKey())) {
                        throw new DataSourceException("Retreived Sale Associate instance attached to another Consumer than the one identified by the given criteria");
                    }
                    if (candidateSaleAssociate != null && !candidateSaleAssociate.getKey().equals(saleAssociate.getKey())) {
                        throw new DataSourceException("At least two different Sale Associate instances match the given criteria");
                    }
                    candidateSaleAssociate = saleAssociate;
                    consumerKey = candidateSaleAssociate.getConsumerKey();
                }
            }
        }

        if (jabberId != null && 0 < jabberId.length()) {
            List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.JABBER_ID, jabberId, 1);
            if (0 < consumers.size()) {
                if (consumerKey != null && !consumerKey.equals(consumers.get(0).getKey())) {
                    throw new DataSourceException("At least two different Consumer instances match the given criteria");
                }
                consumerKey = consumers.get(0).getKey();
                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
                if (0 < saleAssociates.size()) {
                    SaleAssociate saleAssociate = saleAssociates.get(0);
                    if (!saleAssociate.getStoreKey().equals(storeKey)) {
                        throw new DataSourceException("Sale Associate already attached to another store");
                    }
                    if (candidateSaleAssociate != null && !candidateSaleAssociate.getKey().equals(saleAssociate.getKey())) {
                        throw new DataSourceException("At least two different Sale Associate instances match the given criteria");
                    }
                    candidateSaleAssociate = saleAssociate;
                }
            }
            else {
                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.JABBER_ID, jabberId, 1);
                if (0 < saleAssociates.size()) {
                    SaleAssociate saleAssociate = saleAssociates.get(0);
                    if (!saleAssociate.getStoreKey().equals(storeKey)) {
                        throw new DataSourceException("Sale Associate already attached to another store");
                    }
                    if (consumerKey != null && !consumerKey.equals(saleAssociate.getConsumerKey())) {
                        throw new DataSourceException("Retreived Sale Associate instance attached to another Consumer than the one identified by the given criteria");
                    }
                    if (candidateSaleAssociate != null && !candidateSaleAssociate.getKey().equals(saleAssociate.getKey())) {
                        throw new DataSourceException("At least two different Sale Associate instances match the given criteria");
                    }
                    candidateSaleAssociate = saleAssociate;
                    consumerKey = candidateSaleAssociate.getConsumerKey();
                }
            }
        }

        if (twitterId != null && 0 < twitterId.length()) {
            List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, twitterId, 1);
            if (0 < consumers.size()) {
                if (consumerKey != null && !consumerKey.equals(consumers.get(0).getKey())) {
                    throw new DataSourceException("At least two different Consumer instances match the given criteria");
                }
                consumerKey = consumers.get(0).getKey();
                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
                if (0 < saleAssociates.size()) {
                    SaleAssociate saleAssociate = saleAssociates.get(0);
                    if (!saleAssociate.getStoreKey().equals(storeKey)) {
                        throw new DataSourceException("Sale Associate already attached to another store");
                    }
                    if (candidateSaleAssociate != null && !candidateSaleAssociate.getKey().equals(saleAssociate.getKey())) {
                        throw new DataSourceException("At least two different Sale Associate instances match the given criteria");
                    }
                    candidateSaleAssociate = saleAssociate;
                }
            }
            else {
                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.TWITTER_ID, twitterId, 1);
                if (0 < saleAssociates.size()) {
                    SaleAssociate saleAssociate = saleAssociates.get(0);
                    if (!saleAssociate.getStoreKey().equals(storeKey)) {
                        throw new DataSourceException("Sale Associate already attached to another store");
                    }
                    if (consumerKey != null && !consumerKey.equals(saleAssociate.getConsumerKey())) {
                        throw new DataSourceException("Retreived Sale Associate instance attached to another Consumer than the one identified by the given criteria");
                    }
                    if (candidateSaleAssociate != null && !candidateSaleAssociate.getKey().equals(saleAssociate.getKey())) {
                        throw new DataSourceException("At least two different Sale Associate instances match the given criteria");
                    }
                    candidateSaleAssociate = saleAssociate;
                    consumerKey = candidateSaleAssociate.getConsumerKey();
                }
            }
        }

        if (consumerKey == null) {
            Consumer consumer = new Consumer();
            consumer.setName(parameters.getString(Consumer.NAME));
            consumer.setEmail(email);
            consumer.setJabberId(jabberId);
            consumer.setTwitterId(twitterId);
            consumer.setLanguage(parameters.getString(Consumer.LANGUAGE));
            consumer = consumerOperations.createConsumer(pm, consumer);
            consumerKey = consumer.getKey();
        }

        if (candidateSaleAssociate == null) {
            parameters.put(SaleAssociate.CONSUMER_KEY, consumerKey);
            candidateSaleAssociate = saleAssociateOperations.createSaleAssociate(pm, parameters);
        }
        else {
            boolean updateRequired = false;
            if (email != null) { candidateSaleAssociate.setEmail(email); updateRequired = true; }
            if (jabberId != null) { candidateSaleAssociate.setJabberId(jabberId); updateRequired = true; }
            if (twitterId != null) { candidateSaleAssociate.setTwitterId(twitterId); updateRequired = true; }
            if (updateRequired) {
                candidateSaleAssociate = saleAssociateOperations.updateSaleAssociate(pm, candidateSaleAssociate);
            }
        }

        return candidateSaleAssociate.toJson();
    }
    *****/

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                Long saleAssociateKey = Long.valueOf(resourceId);
                delegateResourceDeletion(pm, saleAssociateKey);
                return;
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
    }

    /**
     * Delete the SaleAssociate instances based on the specified criteria.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param saleAssociateKey Identifier of the resource to delete
     * @return Serialized list of the Consumer instances matching the given criteria

     * @throws InvalidIdentifierException If the query to the back-end fails
     *
     * @see ConsumerRestlet#delegateResourceDeletion(PersistenceManager, Long)
     * @see StoreRestlet#delegateResourceDeletion(PersistenceManager, Long)
     */
    protected void delegateResourceDeletion(PersistenceManager pm, Long saleAssociateKey) throws InvalidIdentifierException{
        // Delete the sale associate account
        SaleAssociate saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, saleAssociateKey);
        BaseSteps.getSaleAssociateOperations().deleteSaleAssociate(pm, saleAssociate);
        // Delete sale associate's proposals
//        List<Long> proposalKeys = BaseSteps.getProposalOperations().getProposalKeys(pm, Proposal.OWNER_KEY, saleAssociateKey, 0);
//        for (Long proposalKey: proposalKeys) {
//            proposalRestlet.delegateResourceDeletion(pm, proposalKey, saleAssociate, false);
//        }
        // Delete the attached consumer
//        Long consumerKey = saleAssociate.getConsumerKey();
//        consumerRestlet.delegateResourceDeletion(pm, consumerKey);
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        /*
        boolean isAdminControlled = isAPrivilegedUser(loggedUser);
        String newEmail = null, newJabberId = null, newTwitterId = null;
        SaleAssociate saleAssociate = null;
        Long saleAssociateKey = null;
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Get the sale associate key
            if (isAdminControlled) {
                saleAssociateKey = Long.valueOf(resourceId);
            }
            else {
                saleAssociateKey = LoginServlet.getSaleAssociateKey(loggedUser, pm);
                if (!resourceId.equals(saleAssociateKey.toString())) {
                    throw new ClientException("SaleAssociate records can only be updated by the associates themselves");
                }
            }

            // Verify the information about the third party access providers
            String openId = loggedUser.getClaimedId();
            if (!isAdminControlled) {
                newEmail = ConsumerRestlet.filterOutInvalidValue(parameters, Consumer.EMAIL, openId);
                newJabberId = ConsumerRestlet.filterOutInvalidValue(parameters, Consumer.JABBER_ID, openId);
                newTwitterId = ConsumerRestlet.filterOutInvalidValue(parameters, Consumer.TWITTER_ID, openId);
            }

            // Update the consumer account
            saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, saleAssociateKey);
            saleAssociate.fromJson(parameters);
            saleAssociate = BaseSteps.getSaleAssociateOperations().updateSaleAssociate(pm, saleAssociate);
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
             * /
            scheduleConsolidationTasks(SaleAssociate.EMAIL, newEmail, saleAssociateKey);
            scheduleConsolidationTasks(SaleAssociate.JABBER_ID, newJabberId, saleAssociateKey);
            scheduleConsolidationTasks(SaleAssociate.TWITTER_ID, newTwitterId, saleAssociateKey);
        }
    */

        return null;
    }

    /**
     * Use the identifier of the account to migrate to the current one to select its entity,
     * to neutralize its identifiers, and to schedule tasks that will updates its attached demands
     * to be now attached to the current account
     *
     * @param topic An identifier among {SaleAssociate.EMAIL, SaleAssociate.JABBER_ID, SaleAssociate.TWITTER_ID}
     * @param identifier The value of the identified attribute
     * @param SaleAssociateKey The key of the current SaleAssociate account
     *
     * @throws DataSourceException If the SaleAssociate or Demand look-up fails
     * /
    protected static void scheduleConsolidationTasks(String topic, String identifier, Long saleAssociateKey) throws DataSourceException {
        if (identifier != null && !"".equals(identifier)) {
            if (!SaleAssociate.EMAIL.equals(topic) && !SaleAssociate.JABBER_ID.equals(topic) && !SaleAssociate.TWITTER_ID.equals(topic)) {
                throw new IllegalArgumentException("Not supported field identifier: " + topic);
            }
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                List<SaleAssociate> saleAssociates = BaseSteps.getSaleAssociateOperations().getSaleAssociates(pm, topic, identifier, 1);
                if (0 < saleAssociates.size()) {
                    // Reset other consumer field
                    SaleAssociate otherSaleAssociate = saleAssociates.get(0);
                    if (!saleAssociateKey.equals(otherSaleAssociate.getKey())) {
                        if (SaleAssociate.EMAIL.equals(topic)) { otherSaleAssociate.setEmail("~" + otherSaleAssociate.getEmail()); }
                        else if (SaleAssociate.JABBER_ID.equals(topic)) { otherSaleAssociate.setJabberId("~" + otherSaleAssociate.getJabberId()); }
                        else /* if (SaleAssociate.TWITTER_ID.equals(topic)) * / { otherSaleAssociate.setTwitterId("~" + otherSaleAssociate.getTwitterId()); }
                        otherSaleAssociate = BaseSteps.getSaleAssociateOperations().updateSaleAssociate(pm, otherSaleAssociate);

                        // Schedule tasks to migrate demands to this new sale associate
                        List<Long> demandKeys = demandOperations.getDemandKeys(pm, Demand.OWNER_KEY, otherSaleAssociate.getKey(), 1);
                        Queue queue = BaseSteps.getBaseOperations().getQueue();
                        for (Long demandKey: demandKeys) {
                            log.warning("Preparing the task: /maezel/consolidateSaleAssociateAccounts?key=" + demandKey.toString() + "&ownerKey=" + saleAssociateKey.toString());
                            queue.add(
                                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/consolidateSaleAssociateAccounts").
                                    param(Demand.KEY, demandKey.toString()).
                                    param(Demand.OWNER_KEY, saleAssociateKey.toString()).
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
    */
}
