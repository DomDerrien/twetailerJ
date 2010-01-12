package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Consumer;
import twetailer.dto.SaleAssociate;
import twetailer.j2ee.BaseRestlet;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

@SuppressWarnings("serial")
public class SaleAssociateRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(SaleAssociateRestlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                return delegateResourceCreation(pm, parameters);
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
    }

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
            Consumer consumer = consumerOperations.getConsumer(pm, parameters.getLong(SaleAssociate.CONSUMER_KEY));
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

        if (email != null) {
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

        if (jabberId != null) {
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

        if (twitterId != null) {
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
            consumer.setEmail(parameters.getString(Consumer.EMAIL));
            consumer.setTwitterId(parameters.getString(Consumer.TWITTER_ID));
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
            if (twitterId != null) { candidateSaleAssociate.setTwitterId(twitterId); updateRequired = true; }
            if (updateRequired) {
                candidateSaleAssociate = saleAssociateOperations.updateSaleAssociate(pm, candidateSaleAssociate);
            }
        }

        return candidateSaleAssociate.toJson();
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
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
    protected JsonArray delegateResourceSelection(PersistenceManager pm, JsonObject parameters) throws DataSourceException{
        if (parameters.containsKey(SaleAssociate.STORE_KEY)) {
            Long storeKey = parameters.getLong(SaleAssociate.STORE_KEY);
            List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.STORE_KEY, storeKey, 100);
            return JsonUtils.toJson(saleAssociates);
        }

        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
