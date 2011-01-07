package twetailer.task.step;

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
import domderrien.jsontools.JsonObject;

public class ConsumerSteps extends BaseSteps {

    public static Consumer getConsumer(PersistenceManager pm, Long consumerKey) throws InvalidIdentifierException {
        return getConsumerOperations().getConsumer(consumerKey);
    }

    public static List<Consumer> getConsumers(PersistenceManager pm, JsonObject parameters) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Consumer> output = getConsumerOperations().getConsumers(pm, queryParameters, maximumResults);
        return output;
    }

    public static List<Long> getConsumerKeys(PersistenceManager pm, JsonObject parameters) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

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

        // Date fields
        processDateFilter(Entity.MODIFICATION_DATE, parameters, queryFilters);

        // String fields
        processStringFilter(Consumer.EMAIL, parameters, queryFilters);
        processStringFilter(Consumer.JABBER_ID, parameters, queryFilters);
        processStringFilter(Consumer.NAME, parameters, queryFilters);
        processStringFilter(Consumer.TWITTER_ID, parameters, queryFilters);

        // Long fields
        processLongFilter(Consumer.FACEBOOK_ID, parameters, queryFilters);

        return queryFilters;
    }

    public static Consumer updateConsumer(PersistenceManager pm, Long consumerKey, JsonObject parameters, Consumer loggedConsumer, boolean isUserAdmin) throws DataSourceException, InvalidIdentifierException, InvalidStateException {

        Consumer actualConsumer = loggedConsumer;
        if (isUserAdmin || !loggedConsumer.getKey().equals(consumerKey)) {
            // Redirection in case the action is triggered by an administrator
            actualConsumer = getConsumerOperations().getConsumer(pm, consumerKey);
        }

        // Verify the information about the third party access providers
        String newEmail = null, newFacebookId = null, newJabberId = null, newTwitterId = null;
        newEmail = filterOutInvalidValue(parameters, Consumer.EMAIL, actualConsumer, isUserAdmin);
        newFacebookId = filterOutInvalidValue(parameters, Consumer.FACEBOOK_ID, actualConsumer, isUserAdmin);
        newJabberId = filterOutInvalidValue(parameters, Consumer.JABBER_ID, actualConsumer, isUserAdmin);
        newTwitterId = filterOutInvalidValue(parameters, Consumer.TWITTER_ID, actualConsumer, isUserAdmin);

        // Merge updates and persist them
        actualConsumer.fromJson(parameters, isUserAdmin);
        actualConsumer = getConsumerOperations().updateConsumer(pm, actualConsumer);

        // Move demands to the updated account
        if (newEmail != null || newJabberId != null || newTwitterId != null) {
            scheduleConsolidationTasks(Consumer.EMAIL, newEmail, consumerKey);
            scheduleConsolidationTasks(Consumer.FACEBOOK_ID, newFacebookId, consumerKey);
            scheduleConsolidationTasks(Consumer.JABBER_ID, newJabberId, consumerKey);
            scheduleConsolidationTasks(Consumer.TWITTER_ID, newTwitterId, consumerKey);
        }

        return actualConsumer;
    }

    protected static String filterOutInvalidValue(JsonObject parameters, String parameterName, Consumer consumer, boolean isUserAdmin) {
        // Not yet implemented
        // See ConsumerRestlet.filterOutInvalidValue()
        if (isUserAdmin) {
            if (parameters.containsKey(Consumer.EMAIL)) { consumer.setEmail(parameters.getString(Consumer.EMAIL)); }
            if (parameters.containsKey(Consumer.FACEBOOK_ID)) { consumer.setFacebookId(parameters.getString(Consumer.FACEBOOK_ID)); }
            if (parameters.containsKey(Consumer.JABBER_ID)) { consumer.setJabberId(parameters.getString(Consumer.JABBER_ID)); }
            if (parameters.containsKey(Consumer.OPEN_ID)) { consumer.setOpenID(parameters.getString(Consumer.OPEN_ID)); }
            if (parameters.containsKey(Consumer.TWITTER_ID)) { consumer.setTwitterId(parameters.getString(Consumer.TWITTER_ID)); }
        }
        return null;
    }

    protected static void scheduleConsolidationTasks(String parameterName, String parameterValue, Long consumerKey) {
        // Not yet implemented
        // See ConsumerRestlet.scheduleConsolidationTasks()
    }
}
