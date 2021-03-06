package twetailer.task.step;

import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.ReservedOperationException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.j2ee.BaseRestlet;
import twetailer.validator.LocaleValidator;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class SaleAssociateSteps extends BaseSteps {

    public static SaleAssociate getSaleAssociate(PersistenceManager pm, Long saleAssociateKey) throws InvalidIdentifierException {
        return getSaleAssociateOperations().getSaleAssociate(saleAssociateKey);
    }

    public static List<SaleAssociate> getSaleAssociates(PersistenceManager pm, JsonObject parameters) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<SaleAssociate> output = getSaleAssociateOperations().getSaleAssociates(pm, queryParameters, maximumResults);
        return output;
    }

    public static List<Long> getSaleAssociateKeys(PersistenceManager pm, JsonObject parameters) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Long> output = getSaleAssociateOperations().getSaleAssociateKeys(pm, queryParameters, maximumResults);
        return output;
    }

    /**
     * Helper fetching a list of parameters for a query
     *
     * @param parameters bag of parameters proposed by a connector
     * @return Prefetch list of query parameters
     */
    protected static Map<String, Object> prepareQueryForSelection(JsonObject parameters) {
        Map<String, Object> queryFilters = new HashMap<String, Object>();

        // Date fields
        processDateFilter(Entity.CREATION_DATE, parameters, queryFilters);
        processDateFilter(Entity.MODIFICATION_DATE, parameters, queryFilters);

        // Long fields
        processLongFilter(SaleAssociate.STORE_KEY, parameters, queryFilters);

        return queryFilters;
    }

    public static SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters, Long creatorKey, Long storeKey, boolean isUserAdmin) throws DataSourceException, ClientException {

        parameters.put(SaleAssociate.CREATOR_KEY, creatorKey);

        // Verify the consumerKey existence
        if (!parameters.containsKey(SaleAssociate.CONSUMER_KEY)) {
            throw new ClientException("Parameter '" + SaleAssociate.CONSUMER_KEY + "' missing!");
        }
        Long consumerKey = parameters.getLong(SaleAssociate.CONSUMER_KEY);
        Consumer associatedConsumerRecord = getConsumerOperations().getConsumer(pm, consumerKey); // Will throw ClientException is the record cannot be found!

        // Verify the consumer is still free
        if(associatedConsumerRecord.getSaleAssociateKey() != null) {
            throw new ClientException("Consumer " + SaleAssociate.CONSUMER_KEY + ":" + consumerKey + " already associated!");
        }

        // Adjust the storeKey
        if (!isUserAdmin) {
            parameters.put(SaleAssociate.STORE_KEY, storeKey);
        }

        // Propagate the store location
        Store store = getStoreOperations().getStore(pm, parameters.getLong(SaleAssociate.STORE_KEY));
        parameters.put(SaleAssociate.LOCATION_KEY, store.getLocationKey());

        SaleAssociate justCreated = getSaleAssociateOperations().createSaleAssociate(pm, parameters);

        // Link the just created account with the Consumer record
        associatedConsumerRecord.setSaleAssociateKey(justCreated.getKey());
        getConsumerOperations().updateConsumer(pm, associatedConsumerRecord);

        return justCreated;
    }

    public static SaleAssociate updateSaleAssociate(PersistenceManager pm, Long saleAssociateKey, JsonObject parameters, boolean isUserAdmin) throws DataSourceException, InvalidIdentifierException, InvalidStateException, ReservedOperationException {

        SaleAssociate saleAssociate = getSaleAssociateOperations().getSaleAssociate(pm, saleAssociateKey);
        Consumer saConsumerRecord = getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());

        // Handle manually the supplied tags update
        if (parameters.containsKey(SaleAssociate.CRITERIA) || parameters.containsKey(SaleAssociate.CRITERIA_ADD) || parameters.containsKey(SaleAssociate.CRITERIA_REMOVE)) {
            Locale locale = saConsumerRecord.getLocale();
            Collator collator = LocaleValidator.getCollator(locale);
            if (parameters.containsKey(SaleAssociate.CRITERIA)) {
                saleAssociate.resetCriteria();
                JsonArray jsonArray = parameters.getJsonArray(SaleAssociate.CRITERIA);
                for (int i=0; i<jsonArray.size(); ++i) {
                    saleAssociate.addCriterion(jsonArray.getString(i).toLowerCase(locale), collator);
                }
            }
            Command.removeDuplicates(parameters, SaleAssociate.CRITERIA_ADD, SaleAssociate.CRITERIA_REMOVE);
            if (parameters.containsKey(SaleAssociate.CRITERIA_REMOVE)) {
                JsonArray jsonArray = parameters.getJsonArray(SaleAssociate.CRITERIA_REMOVE);
                for (int i=0; i<jsonArray.size(); ++i) {
                    saleAssociate.removeCriterion(jsonArray.getString(i).toLowerCase(locale), collator);
                }
            }
            if (parameters.containsKey(SaleAssociate.CRITERIA_ADD)) {
                JsonArray jsonArray = parameters.getJsonArray(SaleAssociate.CRITERIA_ADD);
                for (int i=0; i<jsonArray.size(); ++i) {
                    saleAssociate.addCriterion(jsonArray.getString(i).toLowerCase(locale), collator);
                }
            }
            parameters.remove(SaleAssociate.CRITERIA);
            parameters.remove(SaleAssociate.CRITERIA_ADD);
            parameters.remove(SaleAssociate.CRITERIA_REMOVE);
        }

        // Merge updates and persist them
        saleAssociate.fromJson(parameters, isUserAdmin, false);
        saleAssociate = getSaleAssociateOperations().updateSaleAssociate(pm, saleAssociate);

        return saleAssociate;
    }

    public static void deleteSaleAssociate(PersistenceManager pm, Long saleAssociateKey, boolean isUserAdmin) throws ReservedOperationException, InvalidIdentifierException {

        // Verify the logged user rights
        if (!isUserAdmin) {
            throw new ReservedOperationException("SaleAssociate instances can only be deleted by Store admins");
        }

        getSaleAssociateOperations().deleteSaleAssociate(pm, saleAssociateKey);
    }
}
