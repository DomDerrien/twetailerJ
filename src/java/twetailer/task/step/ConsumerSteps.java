package twetailer.task.step;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.CommunicationException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MailConnector;
import twetailer.connector.MessageGenerator;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dao.CacheHandler;
import twetailer.dto.Consumer;
import twetailer.dto.Consumer.Autonomy;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Influencer;
import twetailer.dto.Report;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MaelzelServlet;
import twetailer.validator.ApplicationSettings;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
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

    public static Consumer updateConsumer(PersistenceManager pm, Long consumerKey, JsonObject parameters, boolean isUserAdmin) throws DataSourceException, InvalidIdentifierException, InvalidStateException {

        Consumer actualConsumer = getConsumerOperations().getConsumer(pm, consumerKey);

        // Verify the information about the third party access providers
        String newEmail = null, newFacebookId = null, newJabberId = null, newTwitterId = null;
        newEmail = filterOutInvalidValue(parameters, Consumer.EMAIL, actualConsumer, isUserAdmin);
        newFacebookId = filterOutInvalidValue(parameters, Consumer.FACEBOOK_ID, actualConsumer, isUserAdmin);
        newJabberId = filterOutInvalidValue(parameters, Consumer.JABBER_ID, actualConsumer, isUserAdmin);
        newTwitterId = filterOutInvalidValue(parameters, Consumer.TWITTER_ID, actualConsumer, isUserAdmin);

        // Merge updates and persist them
        boolean hadUnconfirmedAccount = Autonomy.UNCONFIRMED.equals(actualConsumer.getAutonomy());
        actualConsumer.fromJson(parameters, isUserAdmin, false);
        actualConsumer = getConsumerOperations().updateConsumer(pm, actualConsumer);

        // Move demands to the updated account
        if (newEmail != null || newJabberId != null || newTwitterId != null) {
            scheduleConsolidationTasks(Consumer.EMAIL, newEmail, consumerKey);
            scheduleConsolidationTasks(Consumer.FACEBOOK_ID, newFacebookId, consumerKey);
            scheduleConsolidationTasks(Consumer.JABBER_ID, newJabberId, consumerKey);
            scheduleConsolidationTasks(Consumer.TWITTER_ID, newTwitterId, consumerKey);
        }

        // Trigger the validation of the Demands owned by the Consumer if his 'autonomy' status allows it
        if (hadUnconfirmedAccount && (Autonomy.MODERATED.equals(actualConsumer.getAutonomy()) || Autonomy.AUTONOMOUS.equals(actualConsumer.getAutonomy()))) {
            PersistenceManager secondPM = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                List<Long> demandKeys = getDemandOperations().getDemandKeys(secondPM, Demand.OWNER_KEY, consumerKey, 0);
                for (Long demandKey: demandKeys) {
                    MaelzelServlet.triggerValidationTask(demandKey);
                }
            }
            finally {
                secondPM.close();
            }
        }

        return actualConsumer;
    }


    /**
     * Get the identified and validated attribute out of the request parameters
     *
     * @param parameters Request parameters with the updated Consumer's attributes
     * @param parameterName Identifier of the attribute to filter out if the validation does not match
     * @param consumer User record to update
     * @return A validated identifier
     */
    protected static String filterOutInvalidValue(JsonObject parameters, String parameterName, Consumer consumer, boolean isUserAdmin) {
        String acceptedValue = null;
        if (isUserAdmin) {
            if (parameters.containsKey(parameterName)) {
                if (parameters.getString(parameterName).equals(consumer.getEmail())) {
                    parameters.remove(parameterName);
                }
                else {
                    acceptedValue = parameters.getString(parameterName);
                }
            }
        }
        else {
            if (parameters.containsKey(parameterName) && parameters.containsKey(parameterName + "Code")) {
                try {
                    acceptedValue = parameters.getString(parameterName);
                    Long code = MaelzelServlet.getCode(parameterName, acceptedValue, consumer.getOpenID());
                    if (!code.equals(parameters.getLong(parameterName + "Code"))) {
                        parameters.remove(parameterName);
                        acceptedValue = null;
                    }
                }
                catch(ClientException ex) {
                    parameters.remove(parameterName);
                    acceptedValue = null;
                }
            }
            else {
                parameters.remove(parameterName);
            }
        }
        return acceptedValue;
    }

    /**
     * Use the identifier of the account to migrate to the current one to select its entity,
     * to neutralize its identifiers, and to schedule tasks that will updates its attached demands
     * to be now attached to the current account
     *
     * @param parameterName An identifier among {Consumer.EMAIL, Consumer.JABBER_ID, Consumer.TWITTER_ID}
     * @param parameterValue The value of the identified attribute
     * @param consumerKey The key of the current Consumer account
     *
     * @throws DataSourceException If the Consumer or Demand look-up fails
     */
    protected static void scheduleConsolidationTasks(String parameterName, String parameterValue, Long consumerKey) throws DataSourceException {
        if (parameterValue != null && 0 < parameterValue.length()) {
            if (!Consumer.EMAIL.equals(parameterName) && !Consumer.JABBER_ID.equals(parameterName) && !Consumer.FACEBOOK_ID.equals(parameterName) && !Consumer.TWITTER_ID.equals(parameterName)) {
                throw new IllegalArgumentException("Not supported field identifier: " + parameterName);
            }
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                List<Consumer> consumers = BaseSteps.getConsumerOperations().getConsumers(pm, parameterName, parameterValue, 1);
                if (0 < consumers.size()) {
                    // Reset other consumer field
                    Consumer otherConsumer = consumers.get(0);
                    if (!consumerKey.equals(otherConsumer.getKey())) {
                        if (Consumer.EMAIL.equals(parameterName)) { otherConsumer.setEmail("~" + otherConsumer.getEmail()); }
                        else if (Consumer.JABBER_ID.equals(parameterName)) { otherConsumer.setJabberId("~" + otherConsumer.getJabberId()); }
                        else if (Consumer.FACEBOOK_ID.equals(parameterName)) { otherConsumer.setFacebookId("~" + otherConsumer.getFacebookId()); }
                        else /* if (Consumer.TWITTER_ID.equals(topic)) */ { otherConsumer.setTwitterId("~" + otherConsumer.getTwitterId()); }
                        otherConsumer = BaseSteps.getConsumerOperations().updateConsumer(pm, otherConsumer);

                        // Schedule tasks to migrate demands to this new consumer
                        List<Long> demandKeys = BaseSteps.getDemandOperations().getDemandKeys(pm, Demand.OWNER_KEY, otherConsumer.getKey(), 1);
                        Queue queue = BaseSteps.getBaseOperations().getQueue();
                        for (Long demandKey: demandKeys) {
                            queue.add(
                                    withUrl("/_tasks/consolidateConsumerAccounts").
                                    param(Demand.KEY, demandKey.toString()).
                                    param(Demand.OWNER_KEY, consumerKey.toString()).
                                    method(Method.GET).
                                    countdownMillis(2000)
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

    /**
     * Send a message to the Unconfirmed Consumer
     *
     * @param consumer   Consumer to communicate with
     * @param hashTags   Domain identifier
     * @param demand     One of the consumer demand
     * @param influencer Influencer who allowed to create the consumer record
     * @param report     Report with the indication of the page who sent us the consumer
     * @param logger     To identify the source of the notification
     * @throws CommunicationException If the email sending fails
     */
    public static boolean notifyUnconfirmedConsumer(Consumer consumer, List<String> hashTags, Demand demand, Influencer influencer, Report report, Logger logger) throws CommunicationException {
        Long lastNotificationDate = (Long) CacheHandler.getFromCache("unconfirmed_" + consumer.getEmail());
        Long nowDate = DateUtils.getNowDate().getTime();
        if (lastNotificationDate != null && nowDate < lastNotificationDate + 30 * 60 * 1000) { // Less than 30 minutes ago
            return false;
        }
        CacheHandler.setInCache("unconfirmed_" + consumer.getEmail(), nowDate);

        Locale locale = consumer.getLocale();
        MessageGenerator msgGen = new MessageGenerator(Source.mail, hashTags == null ? demand.getHashTags() : hashTags, locale);
        msgGen.
            put("landingPage>title", report != null ? report.getReporterTitle() : ApplicationSettings.get().getProductName()).
            put("landingPage>url", report != null ? report.getReporterUrl() : ApplicationSettings.get().getProductWebsite()).
            put("initiator>name", consumer.getName()).
            fetch(demand).
            fetch(influencer);

        String subject = LabelExtractor.get(ResourceFileId.fourth, "common_welcome_message_subject_default", locale);
        try {
            String message = msgGen.getMessage(demand == null ? MessageId.INVITATION_TO_CONFIRM_EMAIL_ADDRESS : MessageId.INVITATION_TO_CONFIRM_EMAIL_ADDRESS_WITH_DEMAND);
            logger.warning("Communicating with " + consumer.getEmail() + " (medium: mail) -- message: " + message);
            try {
                MailConnector.sendMailMessage(
                        false,
                        true,
                        consumer.getEmail(),
                        consumer.getName(),
                        subject,
                        message,
                        locale
                );
            }
            finally {
                // It's possible the message sent to  userId failed... anyway, send the copy to admins
                MailConnector.sendCopyToAdmins(Source.mail, consumer, subject, new String[] { message });
            }
            return true;
        }
        catch (Exception ex) {
            throw new CommunicationException("Cannot send message asking to confirm the new Consumer email address");
        }
    }
}
