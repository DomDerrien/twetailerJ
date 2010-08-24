package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

import javax.jdo.PersistenceManager;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.JabberConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.TwitterConnector;
import twetailer.dao.DemandOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.Settings;
import twetailer.task.CommandProcessor;
import twetailer.task.DemandProcessor;
import twetailer.task.DemandValidator;
import twetailer.task.LocationValidator;
import twetailer.task.ProposalProcessor;
import twetailer.task.ProposalValidator;
import twetailer.task.RobotResponder;
import twetailer.task.TweetLoader;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

/**
 * Entry point used by the system to process tasks asynchronously.
 * This entry point access is restricted to users with the corresponding
 * administrative rights and to the task/cron job schedulers.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class MaezelServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MaezelServlet.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();

        JsonObject out = new GenericJsonObject();
        TaskOptions retryOptions = null;

        try {
            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/deleteMarkedForDeletion".equals(pathInfo)) {
                PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
                try {
                    DemandOperations demandOperations = BaseSteps.getDemandOperations();
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put(Command.STATE, State.markedForDeletion.toString());
                    parameters.put("<" + Entity.MODIFICATION_DATE, DateUtils.getNowDate());
                    List<Demand> demands = demandOperations.getDemands(pm, parameters, 0);
                    for (Demand demand: demands) {
                        demandOperations.deleteDemand(pm, demand);
                    }
                }
                finally {
                    pm.close();
                }
            }
            else if ("/reloadCachedData".equals(pathInfo)) {
                PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
                try {
                    SettingsOperations  settingsOperations = BaseSteps.getSettingsOperations();
                    Settings settings = settingsOperations.getSettings(pm, false);
                    settings.setModificationDate(DateUtils.getNowDate());
                    settingsOperations.updateSettings(pm, settings);
                }
                finally {
                    pm.close();
                }
            }
            else if ("/setupRobotCoordinates".equals(pathInfo)) {
                PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
                try {
                    // Get parameters
                    Long consumerKey = Long.parseLong(request.getParameter("consumerKey"));
                    Long saleAssociateKey = Long.parseLong(request.getParameter("saleAssociateKey"));
                    // Prepare the options for the task to be posted if this one fails because of a timeout
                    retryOptions =
                        url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                        param("consumerKey", consumerKey.toString()).
                        param("saleAssociateKey", saleAssociateKey.toString()).
                        method(Method.GET);
                    // Process the command itself
                    Settings settings = BaseSteps.getSettingsOperations().getSettings(pm);
                    settings.setRobotConsumerKey(consumerKey);
                    settings.setRobotSaleAssociateKey(saleAssociateKey);
                    BaseSteps.getSettingsOperations().updateSettings(settings);
                }
                finally {
                    pm.close();
                }
            }
            else if ("/loadTweets".equals(pathInfo)) {
                //
                // No need to schedule a retry task because this task is regularly run by a cron job
                //
                Long newSinceId = TweetLoader.loadDirectMessages();
                out.put(Settings.LAST_PROCESSED_DIRECT_MESSAGE_ID, newSinceId);
            }
            else if ("/speedUpLoadTweets".equals(pathInfo)) {
                // Get parameters
                String givenDuration = request.getParameter("duration");
                long duration = givenDuration == null ? 300 : Long.parseLong(givenDuration);
                final long defaultDelay = 20;
                // Prepare the loop to post the batch of "loadTweets" requests
                Queue queue = BaseSteps.getBaseOperations().getQueue();
                while (0 < duration) {
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maezel/loadTweets").
                            countdownMillis(duration * 1000).
                            method(Method.GET)
                    );
                    duration -= defaultDelay;
                }
            }
            else if ("/processCommand".equals(pathInfo)) {
                // Get parameters
                Long commandId = Long.parseLong(request.getParameter(Command.KEY));
                // Prepare the options for the task to be posted if this one fails because of a timeout
                retryOptions =
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                    param(Command.KEY, commandId.toString());
                // Process the command itself
                CommandProcessor.processRawCommands(commandId);
            }
            else if ("/validateLocation".equals(pathInfo)) {
                // Get parameters
                String postalCode = request.getParameter(Location.POSTAL_CODE);
                String countryCode = request.getParameter(Location.COUNTRY_CODE);
                Long consumerKey = Long.parseLong(request.getParameter(Consumer.CONSUMER_KEY));
                Long commandKey = Long.parseLong(request.getParameter(Command.KEY));
                // Prepare the options for the task to be posted if this one fails because of a timeout
                retryOptions =
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                    param(Location.POSTAL_CODE, postalCode.toString()).
                    param(Location.COUNTRY_CODE, countryCode.toString()).
                    param(Consumer.CONSUMER_KEY, consumerKey.toString()).
                    param(Command.KEY, commandKey.toString());
                // Process the command itself
                LocationValidator.process(postalCode, countryCode, consumerKey, commandKey);
            }
            else if ("/validateOpenDemand".equals(pathInfo)) {
                // Get parameters
                Long demandId = Long.parseLong(request.getParameter(Demand.KEY));
                // Prepare the options for the task to be posted if this one fails because of a timeout
                retryOptions =
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                    param(Demand.KEY, demandId.toString());
                // Process the command itself
                DemandValidator.process(demandId);
            }
            else if ("/validateOpenProposal".equals(pathInfo)) {
                // Get parameters
                Long proposalId = Long.parseLong(request.getParameter(Proposal.KEY));
                // Prepare the options for the task to be posted if this one fails because of a timeout
                retryOptions =
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                    param(Proposal.KEY, proposalId.toString());
                // Process the command itself
                ProposalValidator.process(proposalId);
            }
            else if ("/processPublishedDemand".equals(pathInfo)) {
                // Get parameters
                Long demandId = Long.parseLong(request.getParameter(Demand.KEY));
                boolean cronJob = "true".equals(request.getParameter("cronJob"));
                // Prepare the options for the task to be posted if this one fails because of a timeout
                retryOptions =
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                    param(Demand.KEY, demandId.toString());
                // Process the command itself
                DemandProcessor.process(demandId, cronJob);
            }
            else if ("/processPublishedDemands".equals(pathInfo)) {
                //
                // No need to schedule a retry task because this task is regularly run by a cron job
                //
                DemandProcessor.batchProcess();
            }
            else if ("/processPublishedProposal".equals(pathInfo)) {
                // Get parameters
                Long proposalId = Long.parseLong(request.getParameter(Proposal.KEY));
                // Prepare the options for the task to be posted if this one fails because of a timeout
                retryOptions =
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                    param(Proposal.KEY, proposalId.toString());
                // Process the command itself
                ProposalProcessor.process(proposalId);
            }
            else if ("/processDemandForRobot".equals(pathInfo)) {
                // Get parameters
                Long demandId = Long.parseLong(request.getParameter(Demand.KEY));
                // Prepare the options for the task to be posted if this one fails because of a timeout
                retryOptions =
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                    param(Demand.KEY, demandId.toString());
                // Process the command itself
                RobotResponder.processDemand(demandId);
            }
            else if ("/consolidateConsumerAccounts".equals(pathInfo)) {
                // Get parameters
                Long demandKey = Long.parseLong(request.getParameter(Demand.KEY));
                Long consumerKey = Long.parseLong(request.getParameter(Demand.OWNER_KEY));
                // Prepare the options for the task to be posted if this one fails because of a timeout
                retryOptions =
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel" + pathInfo).
                    param(Demand.KEY, demandKey.toString()).
                    param(Demand.OWNER_KEY, consumerKey.toString());
                // Process the command itself
                PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
                try {
                    Demand demand = BaseSteps.getDemandOperations().getDemand(pm, demandKey, null);
                    demand.setOwnerKey(consumerKey);
                    BaseSteps.getDemandOperations().updateDemand(pm, demand);
                }
                finally {
                    pm.close();
                }
            }
            /* legacy *
            else if ("/cbuiEndPoint".equals(pathInfo)) {
                if (amazonFPS.verifyCoBrandedServiceResponse(request.getParameterMap())) {
                    Payment payment = new Payment();
                    payment.setAuthorizationId(request.getParameter(AmazonFPS.TOKEN_ID));
                    payment.setReference(request.getParameter(AmazonFPS.CALLER_REFERENCE));

                    BaseSteps.getPaymentOperations().createPayment(payment);
                    out.put("payment", payment.toJson());

                    // Create a task to make the payment with the received token
                    Queue queue = BaseSteps.getBaseOperations().getQueue();
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maezel/makePayment").
                                param(Payment.KEY, payment.getKey().toString()).
                                method(Method.GET)
                    );
                }
                else {
                    throw new RuntimeException("Not yet implemented!");
                }
            }
            else if("/makePayment".equals(pathInfo)) {
                Long paymentKey = Long.parseLong(request.getParameter(Payment.KEY));
                PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
                try {
                    Payment payment = BaseSteps.getPaymentOperations().getPayment(pm, paymentKey);

                    payment = amazonFPS.makePayRequest(payment);

                    BaseSteps.getPaymentOperations().updatePayment(pm, payment);
                    out.put("payment", payment.toJson());

                    if (TransactionStatus.PENDING.equals(payment.getStatus())) {
                        // Validate the payment again in 15 minutes
                        Queue queue = BaseSteps.getBaseOperations().getQueue();
                        queue.add(
                                url(ApplicationSettings.get().getServletApiPath() + "/maezel/makePayment").
                                    param(Payment.KEY, payment.getKey().toString()).
                                    countdownMillis(15*60*1000).
                                    method(Method.GET)
                        );
                    }
                }
                finally {
                    pm.close();
                }
            }
            */
            else {
                throw new ClientException("Unsupported query path: " + pathInfo);
            }

            out.put("success", true);
        }
        catch(Exception ex) {
            // Prepare the exception report
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
            // Reschedule the task if possible
            if(ex instanceof com.google.appengine.api.datastore.DatastoreTimeoutException && retryOptions != null) {
                BaseSteps.getBaseOperations().getQueue().add(retryOptions.method(Method.GET));
            }
            // Send an e-mail to out catch-all list
            MockOutputStream stackTrace = new MockOutputStream();
            ex.printStackTrace(new PrintStream(stackTrace));
            try {
                CatchAllMailHandlerServlet.composeAndPostMailMessage(
                        "error-notifier",
                        "Unexpected error caught in " + MaezelServlet.class.getName(),
                        "Path info: " + pathInfo + "\n\n--\n\nRequest parameters:\n" + new GenericJsonObject(request.getParameterMap()).toString() + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                log.severe("Failure while trying to report an unexpected by e-mail!");
            }
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();

        JsonObject out = new GenericJsonObject();
        JsonObject in = null;

        Locale locale = Locale.ENGLISH;

        try {
            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/processVerificationCode".equals(pathInfo)) {
                //
                // No need to schedule a retry task because this URL is hit by the Web Console which can retry from there
                //

                // TODO: verify Content-type = "application/x-www-form-urlencoded"
                // JsonObject in = new GenericJsonObject(request.getParameterMap());

                // TODO: verify Content-type == "application/json"
                in = new JsonParser(request.getInputStream()).getJsonObject();

                OpenIdUser loggedUser = BaseRestlet.getLoggedUser(request);
                String openId = loggedUser.getClaimedId();

                // Custom fields
                String topic = in.getString("topic");
                Boolean waitForCode = in.getBoolean("waitForCode");
                // Consumer fields
                String language = in.getString(Consumer.LANGUAGE);
                locale = new Locale(language);

                // Verification process
                if (Consumer.EMAIL.equals(topic)) {
                    String email = in.getString(Consumer.EMAIL);
                    long code = getCode(topic, email, openId);
                    if (waitForCode) {
                        // Account with an e-mail address
                        MailConnector.sendMailMessage(
                                false,
                                email,
                                in.getString(Consumer.NAME),
                                LabelExtractor.get(ResourceFileId.third, "consumer_info_verification_notification_title", locale),
                                LabelExtractor.get(ResourceFileId.third, "consumer_info_verification_notification_body", new Object[] { code }, locale),
                                locale
                        );
                    }
                    else {
                        out.put("codeValidity", in.getLong(Consumer.EMAIL + "Code") == code);
                    }
                }
                else if (Consumer.JABBER_ID.equals(topic)) {
                    String jabberId = in.getString(Consumer.JABBER_ID);
                    long code = getCode(topic, jabberId, openId);
                    if (waitForCode) {
                        // Account with a jabber identifier
                        JabberConnector.sendInstantMessage(
                                jabberId,
                                LabelExtractor.get(ResourceFileId.third, "consumer_info_verification_notification_title", locale)
                        );
                        JabberConnector.sendInstantMessage(
                                jabberId,
                                LabelExtractor.get(ResourceFileId.third, "consumer_info_verification_notification_body", new Object[] { code }, locale)
                        );
                    }
                    else {
                        out.put("codeValidity", in.getLong(Consumer.JABBER_ID + "Code") == code);
                    }
                }
                else if (Consumer.TWITTER_ID.equals(topic)) {
                    String twitterId = in.getString(Consumer.TWITTER_ID);
                    long code = getCode(topic, twitterId, openId);
                    if (waitForCode) {
                        // Account with a twitter identifier
                        TwitterConnector.sendDirectMessage(
                                twitterId,
                                LabelExtractor.get(ResourceFileId.third, "consumer_info_verification_notification_title", locale)
                        );
                        TwitterConnector.sendDirectMessage(
                                twitterId,
                                LabelExtractor.get(ResourceFileId.third, "consumer_info_verification_notification_body", new Object[] { code }, locale)
                        );
                    }
                    else {
                        out.put("codeValidity", in.getLong(Consumer.TWITTER_ID + "Code") == code);
                    }

                }
                else {
                    throw new RuntimeException("Unexpected topic: " + topic);
                }
            }
            else {
                throw new ClientException("Unsupported query path: " + pathInfo);
            }

            out.put("success", true);
        }
        catch(TwitterException ex) {
            if (ex.getStatusCode() == 403) { // 403: Forbidden
                if (ex.getMessage().contains("<error>You cannot send messages to users who are not following you.</error>")) {
                    out = new JsonException("TWITTER_USER_NOT_FOLLOWER", LabelExtractor.get(ResourceFileId.third, "error_server_side_twetailer_not_followed", locale), ex);
                }
                else {
                    out = new JsonException("TWITTER_FAILURE", LabelExtractor.get(ResourceFileId.third, "error_server_side_twitter_not_responding", locale), ex);
                }
            }
            else {
                out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doPost() operation", ex);
            }
        }
        catch(ClientException ex) {
            out = new JsonException("CLIENT_EXCEPTION", ex.getMessage(), ex);
        }
        catch(Exception ex) {
            // Prepare the exception report
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doPost() operation", ex);
            // Send an e-mail to out catch-all list
            MockOutputStream stackTrace = new MockOutputStream();
            ex.printStackTrace(new PrintStream(stackTrace));
            try {
                CatchAllMailHandlerServlet.composeAndPostMailMessage(
                        "error-notifier",
                        "Unexpected error caught in " + MaezelServlet.class.getName(),
                        "Path info: " + pathInfo + "\n\n--\n\nRequest parameters:\n" + (in == null ? "null" : in.toString()) + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                log.severe("Failure while trying to report an unexpected by e-mail!");
            }
        }

        out.toStream(response.getOutputStream(), false);
    }

    /**
     * Helper computing the verification code
     *
     * @param topic To be able to choose the verification code algorithm
     * @param identifier User's identifier for the given topic
     * @param openId User's OpenID (accessible because he's logged in)
     * @return Unique verification code
     *
     * @throws ClientException If the given identifier does not respect the standard
     */
    public static long getCode(String topic, String identifier, String openId) throws ClientException {
        long code = 0L;
        if (identifier == null || identifier.length() == 0) {
            code = 9999999999L;
        }
        else if (Consumer.EMAIL.equals(topic)) {
            if (!identifier.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}")) {
                throw new ClientException("Invalid email address: " + identifier);
            }
            code = Math.abs(7 + identifier.hashCode() * 37 - openId.hashCode() / 3);
        }
        else if (Consumer.JABBER_ID.equals(topic)) {
            code = Math.abs(13 + identifier.hashCode() * 29 - openId.hashCode() / 7);
        }
        else if (Consumer.TWITTER_ID.equals(topic)) {
            if (!identifier.matches("[A-Za-z0-9_]+")) {
                throw new ClientException("Invalid Twitter identifier: " + identifier);
            }
            code = Math.abs(23 + identifier.hashCode() * 17 - openId.hashCode() / 5);
        }
        else {
            throw new RuntimeException("Unexpected topic: " + topic);
        }
        log.warning("Code " + code + " for " + topic + ": " + identifier);
        return code;
    }

    public static String getCoBrandedServiceEndPointURL() {
        return "http://twetailer.appspot.com/API/maezel/cbuiEndPoint";
    }

    /**
     * Prepare a task for the workflow step "Command Process".
     *
     * @param rawCommandKey identifier of the raw command to process
     */
    public static void triggerCommandProcessorTask(Long rawCommandKey) {
        // Create a task for that command
        Queue queue = BaseSteps.getBaseOperations().getQueue();
        queue.add(
                url(ApplicationSettings.get().getServletApiPath() + "/maezel/processCommand").
                    param(Command.KEY, rawCommandKey.toString()).
                    method(Method.GET)
        );
    }

    /**
     * Prepare a task for the workflow step "Demand Validation".
     * If this step completes, the step "Demand Process" will be triggered
     * and the demand will be forwarded to the matching sale associates.
     *
     * @param demand Entity to validate
     */
    public static void triggerValidationTask(Demand demand) {
        Long demandKey = demand.getKey();
        // Create a task for that demand validation
        Queue queue = BaseSteps.getBaseOperations().getQueue();
        queue.add(
                url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenDemand").
                    param(Proposal.KEY, demandKey.toString()).
                    method(Method.GET)
        );
    }

    /**
     * Prepare a task for the workflow step "Proposal Validation".
     * If this step completes, the step "Proposal Process" will be triggered
     * and the proposal will be forwarded to the owner of the demand being proposed to.
     *
     * @param proposal Entity to validate
     */
    public static void triggerValidationTask(Proposal proposal) {
        Long proposalKey = proposal.getKey();
        // Create a task for that proposal validation
        Queue queue = BaseSteps.getBaseOperations().getQueue();
        queue.add(
                url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenProposal").
                    param(Proposal.KEY, proposalKey.toString()).
                    method(Method.GET)
        );
    }

    /**
     * Prepare a task for the workflow step "Cancel Proposal".
     *
     * @param proposalKeys List of Proposal identifiers to cancel
     * @param cancellerKey Identifier of the one initiating the operation
     * @param preservedProposalKey Identifier of the Proposal to keep referenced
     */
    public static void triggerProposalCancellationTask(List<Long> proposalKeys, Long cancellerKey, Long preservedProposalKey) {
        /*
        // Create a task per proposal
        Queue queue = BaseSteps.getBaseOperations().getQueue();
        for(Long proposalKey: proposalKeys) {
            if(!proposalKey.equals(preservedProposalKey)) {
                queue.add(
                        url(ApplicationSettings.get().getServletApiPath() + "/maezel/cancelPublishedProposal").
                            param(Proposal.CANCELER_KEY, cancellerKey.toString()).
                            param(Proposal.KEY, proposalKey.toString()).
                            method(Method.GET)
                );
            }
        }
        */

        //
        // TODO: implement the corresponding cancel proposal command
        //

        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            for(Long proposalKey: proposalKeys) {
                if(preservedProposalKey == null || !proposalKey.equals(preservedProposalKey)) {
                    try {
                        Proposal proposal = BaseSteps.getProposalOperations().getProposal(pm, proposalKey, null, null);
                        proposal.setState(State.cancelled);
                        proposal.setCancelerKey(cancellerKey);
                        proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
                    }
                    catch (InvalidIdentifierException e) {
                        // Not an issue, process the next one
                    }
                }
            }
        }
        finally {
            pm.close();
        }
    }
}
