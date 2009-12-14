package twetailer.j2ee;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.RelyingParty;

import twetailer.ClientException;
import twetailer.connector.JabberConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.TwitterConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.SaleAssociateOperations;
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
    protected StoreOperations storeOperations = _baseOperations.getStoreOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        JsonObject out = new GenericJsonObject();

        try {
            String pathInfo = request.getPathInfo();
            log.warning("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/loadTweets".equals(pathInfo)) {
                Long newSinceId = TweetLoader.loadDirectMessages();
                out.put(Settings.LAST_PROCESSED_DIRECT_MESSAGE_ID, newSinceId);
            }
            else if ("/processCommand".equals(pathInfo)) {
                Long commandId = Long.parseLong(request.getParameter(Command.KEY));
                CommandProcessor.processRawCommands(commandId);
            }
            else if ("/validateLocation".equals(pathInfo)) {
                String postalCode = request.getParameter(Location.POSTAL_CODE);
                String countryCode = request.getParameter(Location.COUNTRY_CODE);
                Long consumerKey = Long.parseLong(request.getParameter(Consumer.CONSUMER_KEY));
                Long commandKey = Long.parseLong(request.getParameter(Command.KEY));
                LocationValidator.process(postalCode, countryCode, consumerKey, commandKey);
            }
            else if ("/validateOpenDemand".equals(pathInfo)) {
                Long demandId = Long.parseLong(request.getParameter(Demand.KEY));
                DemandValidator.process(demandId);
            }
            else if ("/validateOpenProposal".equals(pathInfo)) {
                Long proposalId = Long.parseLong(request.getParameter(Proposal.KEY));
                ProposalValidator.process(proposalId);
            }
            else if ("/processPublishedDemand".equals(pathInfo)) {
                Long demandId = Long.parseLong(request.getParameter(Demand.KEY));
                DemandProcessor.process(demandId);
            }
            else if ("/processPublishedDemands".equals(pathInfo)) {
                DemandProcessor.batchProcess();
            }
            else if ("/processPublishedProposal".equals(pathInfo)) {
                Long proposalId = Long.parseLong(request.getParameter(Proposal.KEY));
                ProposalProcessor.process(proposalId);
            }
            else if ("/processDemandForRobot".equals(pathInfo)) {
                Long demandId = Long.parseLong(request.getParameter(Demand.KEY));
                RobotResponder.processDemand(demandId);
            }
            else {
                throw new ClientException("Unsupported query path: " + pathInfo);
            }

            out.put("success", true);
        }
        catch(Exception ex) {
            log.warning("doGet().exception: " + ex);
            // ex.printStackTrace();
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        JsonObject out = new GenericJsonObject();

        try {
            String pathInfo = request.getPathInfo();
            log.warning("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/processVerificationCode".equals(pathInfo)) {
                // TODO: verify Content-type = "application/x-www-form-urlencoded"
                // JsonObject in = new GenericJsonObject(request.getParameterMap());

                // TODO: verify Content-type == "application/json"
                JsonObject in = new JsonParser(request.getInputStream()).getJsonObject();

                OpenIdUser loggedUser = getLoggedUser(request);

                // Custom fields
                String topic = in.getString("topic");
                Boolean waitForCode = in.getBoolean("waitForCode");
                // Consumer fields
                String language = in.getString(Consumer.LANGUAGE);
                Locale locale = new Locale(language);

                // Verification process
                if (Consumer.EMAIL.equals(topic)) {
                    String email = in.getString(Consumer.EMAIL);
                    long code = getCode(topic, email, loggedUser.getClaimedId());
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
                    long code = getCode(topic, jabberId, loggedUser.getClaimedId());
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
                    long code = getCode(topic, twitterId, loggedUser.getClaimedId());
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
        catch(Exception ex) {
            log.warning("doPost().exception: " + ex);
            // ex.printStackTrace();
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);

        /*
        // FIXME: remove this trace
        MockOutputStream outS = new MockOutputStream();
        out.toStream(outS, false);
        System.err.println("******************** " + outS.getStream());
        */
    }

    /**
     * Helper computing the verification code
     *
     * @param openId User's OpenID (accessible because he's logged in)
     * @param topic To be able to choose the verification code algorithm
     * @param identifier User's identifier for the given topic
     * @return Unique verification code
     */
    protected static long getCode(String topic, String identifier, String openId) {
        long code = 0L;
        if (Consumer.EMAIL.equals(topic)) {
            code = Math.abs(7 + identifier.hashCode() * 37 - openId.hashCode() / 3);
        }
        else if (Consumer.JABBER_ID.equals(topic)) {
            code = Math.abs(13 + identifier.hashCode() * 29 - openId.hashCode() / 7);
        }
        else if (Consumer.TWITTER_ID.equals(topic)) {
            code = Math.abs(23 + identifier.hashCode() * 17 - openId.hashCode() / 5);
        }
        else {
            throw new RuntimeException("Unexpected topic: " + topic);
        }
        log.warning("Code " + code + " for " + topic + ": " + identifier);
        return code;
    }

    /**
     * Helper made available to be able to inject a mock OpenIdUser from the unit tests
     *
     * @param parameters HTTP request parameters
     * @return OpenIdUser instance extracted from the session
     *
     * @throws Exception If the OpendIdUser un-marshaling fails
     */
    protected static OpenIdUser getLoggedUser(HttpServletRequest request) throws Exception {
        return RelyingParty.getInstance().discover(request);
    }
}
