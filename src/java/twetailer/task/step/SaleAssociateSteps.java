package twetailer.task.step;

import java.text.Collator;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import domderrien.i18n.DateUtils;
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

        if (parameters.containsKey(SaleAssociate.STORE_KEY)) {
            queryFilters.put(SaleAssociate.STORE_KEY, parameters.getLong(SaleAssociate.STORE_KEY));
        }

        Date lastModificationDate = null;
        if (parameters.containsKey(Entity.MODIFICATION_DATE)) {
            try {
                lastModificationDate = DateUtils.isoToDate(parameters.getString(Entity.MODIFICATION_DATE));
                queryFilters.put(">" + Entity.MODIFICATION_DATE, lastModificationDate);
            }
            catch (ParseException e) { } // Date not set, too bad.
        }

        return queryFilters;
    }

    public static SaleAssociate createSaleAssociate(PersistenceManager pm, JsonObject parameters, Consumer loggedConsumer, SaleAssociate loggedSaleAssociate, boolean isPrivileged) throws DataSourceException, ClientException {

        // Verify the logged user rights
        if (!isPrivileged && !loggedSaleAssociate.isStoreAdmin()) {
            throw new ReservedOperationException("SaleAssociate instances can only be created by Store admins");
        }
        parameters.put(SaleAssociate.CREATOR_KEY, isPrivileged ? 0L : loggedConsumer.getKey());

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
        if (!isPrivileged) {
            parameters.put(SaleAssociate.STORE_KEY, loggedSaleAssociate.getStoreKey());
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

    public static SaleAssociate updateSaleAssociate(PersistenceManager pm, Long saleAssociateKey, JsonObject parameters, Consumer loggedConsumer, SaleAssociate loggedSaleAssociate, boolean isPrivileged) throws DataSourceException, InvalidIdentifierException, InvalidStateException, ReservedOperationException {

        // Verify the logged user rights
        if (!isPrivileged && !loggedSaleAssociate.isStoreAdmin() && !loggedSaleAssociate.getKey().equals(saleAssociateKey)) {
            throw new ReservedOperationException("SaleAssociate instances can only be updated by Store admins or the user himself");
        }

        if (!loggedSaleAssociate.getKey().equals(saleAssociateKey)) {
            // Redirection in case the action is triggered by an administrator
            loggedSaleAssociate = getSaleAssociateOperations().getSaleAssociate(pm, saleAssociateKey);
        }

        // Handle manually the supplied tags update
        if (parameters.containsKey(SaleAssociate.CRITERIA) || parameters.containsKey(SaleAssociate.CRITERIA_ADD) || parameters.containsKey(SaleAssociate.CRITERIA_REMOVE)) {
            Collator collator = LocaleValidator.getCollator(loggedConsumer.getLocale());
            if (parameters.containsKey(SaleAssociate.CRITERIA)) {
                loggedSaleAssociate.resetCriteria();
                JsonArray jsonArray = parameters.getJsonArray(SaleAssociate.CRITERIA);
                for (int i=0; i<jsonArray.size(); ++i) {
                    loggedSaleAssociate.addCriterion(jsonArray.getString(i), collator);
                }
            }
            Command.removeDuplicates(parameters, SaleAssociate.CRITERIA_ADD, SaleAssociate.CRITERIA_REMOVE);
            if (parameters.containsKey(SaleAssociate.CRITERIA_REMOVE)) {
                JsonArray jsonArray = parameters.getJsonArray(SaleAssociate.CRITERIA_REMOVE);
                for (int i=0; i<jsonArray.size(); ++i) {
                    loggedSaleAssociate.removeCriterion(jsonArray.getString(i), collator);
                }
            }
            if (parameters.containsKey(SaleAssociate.CRITERIA_ADD)) {
                JsonArray jsonArray = parameters.getJsonArray(SaleAssociate.CRITERIA_ADD);
                for (int i=0; i<jsonArray.size(); ++i) {
                    loggedSaleAssociate.addCriterion(jsonArray.getString(i), collator);
                }
            }
            parameters.remove(SaleAssociate.CRITERIA);
            parameters.remove(SaleAssociate.CRITERIA_ADD);
            parameters.remove(SaleAssociate.CRITERIA_REMOVE);
        }

        // Neutralize some updates
        parameters.remove(SaleAssociate.CONSUMER_KEY);
        if (!isPrivileged) {
            parameters.remove(SaleAssociate.LOCATION_KEY);
            parameters.remove(SaleAssociate.STORE_KEY);
        }

        // Merge updates and persist them
        loggedSaleAssociate.fromJson(parameters);
        loggedSaleAssociate = getSaleAssociateOperations().updateSaleAssociate(pm, loggedSaleAssociate);

        return loggedSaleAssociate;
    }
}
