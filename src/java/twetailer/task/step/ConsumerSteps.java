package twetailer.task.step;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.ReservedOperationException;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.j2ee.BaseRestlet;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonObject;

public class ConsumerSteps extends BaseSteps {

    public static Consumer getConsumer(PersistenceManager pm, Long consumerKey) throws InvalidIdentifierException {
        return getConsumerOperations().getConsumer(consumerKey);
    }

    public static List<Consumer> getConsumers(PersistenceManager pm, JsonObject parameters) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY);

        List<Consumer> output = getConsumerOperations().getConsumers(pm, queryParameters, maximumResults);
        return output;
    }

    public static List<Long> getConsumerKeys(PersistenceManager pm, JsonObject parameters) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY);

        List<Long> output = getConsumerOperations().getConsumerKeys(pm, queryParameters, maximumResults);
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

        if (parameters.containsKey(Consumer.EMAIL)) {
            queryFilters.put(Consumer.EMAIL, parameters.getString(Consumer.EMAIL));
        }

        if (parameters.containsKey(Consumer.JABBER_ID)) {
            queryFilters.put(Consumer.JABBER_ID, parameters.getString(Consumer.JABBER_ID));
        }

        if (parameters.containsKey(Consumer.TWITTER_ID)) {
            queryFilters.put(Consumer.TWITTER_ID, parameters.getString(Consumer.TWITTER_ID));
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

    public static Consumer updateConsumer(PersistenceManager pm, Long consumerKey, JsonObject parameters, Consumer loggedConsumer) throws DataSourceException, InvalidIdentifierException, InvalidStateException {

        if (!loggedConsumer.getKey().equals(consumerKey)) {
            // Redirection in case the action is triggered by an administrator
            loggedConsumer = getConsumerOperations().getConsumer(pm, consumerKey);
        }

        //
        // TODO: handle the account consolidation !
        //

        // Neutralise some updates
        parameters.remove(Consumer.SALE_ASSOCIATE_KEY);

        // Merge updates and persist them
        loggedConsumer.fromJson(parameters);
        loggedConsumer = getConsumerOperations().updateConsumer(pm, loggedConsumer);

        return loggedConsumer;
    }
}
