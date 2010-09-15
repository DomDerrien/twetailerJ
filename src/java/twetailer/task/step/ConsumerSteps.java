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
        /*
        boolean isAdminControlled = isAPrivilegedUser(loggedUser);

        // Verify the information about the third party access providers
        String openId = loggedUser.getClaimedId();
        String newEmail = null, newJabberId = null, newTwitterId = null;
        if (!isAdminControlled) {
            newEmail = filterOutInvalidValue(parameters, Consumer.EMAIL, openId);
            newJabberId = filterOutInvalidValue(parameters, Consumer.JABBER_ID, openId);
            newTwitterId = filterOutInvalidValue(parameters, Consumer.TWITTER_ID, openId);
        }

        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        Consumer consumer;
        try {
            // Update the consumer account
            consumer = BaseSteps.getConsumerOperations().getConsumer(pm, consumerKey);
            consumer.fromJson(parameters);
            consumer = BaseSteps.getConsumerOperations().updateConsumer(pm, consumer);
        }
        finally {
            pm.close();
        }

        // Move demands to the updated account
        if (!isAdminControlled && (newEmail != null || newJabberId != null || newTwitterId != null)) {
            / *
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
            scheduleConsolidationTasks(Consumer.EMAIL, newEmail, consumerKey);
            scheduleConsolidationTasks(Consumer.JABBER_ID, newJabberId, consumerKey);
            scheduleConsolidationTasks(Consumer.TWITTER_ID, newTwitterId, consumerKey);
        }
         */

        // Neutralise some updates
        parameters.remove(Consumer.SALE_ASSOCIATE_KEY);

        // Merge updates and persist them
        loggedConsumer.fromJson(parameters);
        loggedConsumer = getConsumerOperations().updateConsumer(pm, loggedConsumer);

        return loggedConsumer;
    }
}
