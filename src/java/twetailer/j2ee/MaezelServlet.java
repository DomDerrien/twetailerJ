package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

import javax.jdo.PersistenceManager;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.connector.JabberConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.TwitterConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
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
import twetailer.validator.ApplicationSettings;
import twitter4j.TwitterException;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

@SuppressWarnings("serial")
public class MaezelServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MaezelServlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    protected SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();
    protected StoreOperations storeOperations = _baseOperations.getStoreOperations();

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        JsonObject out = new GenericJsonObject();
        TaskOptions retryOptions = null;

        try {
            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/setupRobotCoordinates".equals(pathInfo)) {
                PersistenceManager pm = _baseOperations.getPersistenceManager();
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
                    Settings settings = settingsOperations.getSettings(pm);
                    settings.setRobotConsumerKey(consumerKey);
                    settings.setRobotSaleAssociateKey(saleAssociateKey);
                    settingsOperations.updateSettings(settings);
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
                PersistenceManager pm = _baseOperations.getPersistenceManager();
                try {
                    Demand demand = demandOperations.getDemand(pm, demandKey, null);
                    demand.setOwnerKey(consumerKey);
                    demandOperations.updateDemand(pm, demand);
                }
                finally {
                    pm.close();
                }
            }
            else {
                throw new ClientException("Unsupported query path: " + pathInfo);
            }

            out.put("success", true);
        }
        catch(Exception ex) {
            // Prepare the exception report
            log.warning("doGet().exception: " + ex);
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
            // Reschedule the task if possible
            if(ex instanceof com.google.appengine.api.datastore.DatastoreTimeoutException && retryOptions != null) {
                log.warning("Schedule another attempt for the task: /maezel/" + pathInfo);
                _baseOperations.getQueue().add(retryOptions.method(Method.GET));
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
        log.warning("Path Info: " + pathInfo);

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
                    out = new JsonException("TWITTER_USER_NOT_FOLLOWER", LabelExtractor.get("error_server_side_twetailer_not_followed", locale), ex);
                }
                else {
                    out = new JsonException("TWITTER_FAILURE", LabelExtractor.get("error_server_side_twitter_not_responding", locale), ex);
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
            log.warning("doPost().exception: " + ex);
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
}
