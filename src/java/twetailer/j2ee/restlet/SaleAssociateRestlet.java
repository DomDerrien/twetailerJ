package twetailer.j2ee.restlet;

import java.util.List;
import java.util.Map;
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

    private void reportExisitingSaleAssociate(List<SaleAssociate> saleAssociates, Long storeKey) throws DataSourceException {
        if (0 < saleAssociates.size()) {
            SaleAssociate saleAssociate = saleAssociates.get(0);
            if (saleAssociate.getStoreKey() != storeKey) {
                throw new DataSourceException("A Sale Associate for one or many given identifiers is already attached to the Store:" + saleAssociate.getStoreKey());
            }
        }
    }

    private void checkEmailUnicity() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (loggedUser.getAttribute("info") != null) {
            Map<String, String> info = (Map<String, String>) loggedUser.getAttribute("info");
            if (info.get("email") != null) {
                String email = info.get("email");
                if ("dominique.derrien@gmail.com".equals(email) || "steven.milstein@gmail.com".equals(email)) {
                    PersistenceManager pm = _baseOperations.getPersistenceManager();
                    try {
                        Long consumerKey = 0L;
                        Long storeKey = parameters.getLong(SaleAssociate.STORE_KEY);
                        if (parameters.containsKey(SaleAssociate.CONSUMER_KEY)) {
                            // Get the corresponding
                            Consumer consumer = consumerOperations.getConsumer(pm, parameters.getLong(SaleAssociate.CONSUMER_KEY));
                            consumerKey = consumer.getKey();
                        }
                        else if (parameters.containsKey(SaleAssociate.TWITTER_ID)) {
                            // Assume the sale associate candidate has already submitted an account
                            List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, parameters.getString(SaleAssociate.TWITTER_ID), 1);
                            if (0 < consumers.size()) {
                                consumerKey = consumers.get(0).getKey();
                            }
                            else {
                                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.TWITTER_ID, parameters.getString(SaleAssociate.TWITTER_ID), 1);
                                reportExisitingSaleAssociate(saleAssociates, storeKey);
                            }
                        }
                        else if (parameters.containsKey(SaleAssociate.EMAIL)) {
                            // Assume the sale associate candidate has already submitted an account
                            List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.EMAIL, parameters.getString(SaleAssociate.EMAIL), 1);
                            if (0 < consumers.size()) {
                                consumerKey = consumers.get(0).getKey();
                            }
                            else {
                                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.EMAIL, parameters.getString(SaleAssociate.EMAIL), 1);
                                reportExisitingSaleAssociate(saleAssociates, storeKey);
                            }
                        }
                        if (consumerKey != 0L) {
                            List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
                            reportExisitingSaleAssociate(saleAssociates, storeKey);
                        }
                        else {
                            Consumer consumer = new Consumer();
                            consumer.setName(parameters.getString(Consumer.NAME));
                            consumer.setEmail(parameters.getString(Consumer.EMAIL));
                            consumer.setTwitterId(parameters.getString(Consumer.TWITTER_ID));
                            consumer.setLanguage(parameters.getString(Consumer.LANGUAGE));
                            consumer = consumerOperations.createConsumer(pm, consumer);
                            consumerKey = consumer.getKey();
                        }
                        parameters.put(SaleAssociate.CONSUMER_KEY, consumerKey);
                        return saleAssociateOperations.createSaleAssociate(pm, parameters).toJson();
                    }
                    finally {
                        pm.close();
                    }
                }
            }
        }
        throw new RuntimeException("Restricted access!");
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
    protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
        if (parameters.containsKey(SaleAssociate.STORE_KEY)) {
            Long storeKey = parameters.getLong(SaleAssociate.STORE_KEY);
            List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(SaleAssociate.STORE_KEY, storeKey, 100);
            return JsonUtils.toJson(saleAssociates);
        }

        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
