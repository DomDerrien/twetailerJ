package twetailer.j2ee;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.connector.JabberConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.TwitterConnector;
import twetailer.connector.BaseConnector.Source;
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
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
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
            ex.printStackTrace();
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        JsonObject in = new GenericJsonObject(request.getParameterMap());
        JsonObject out = new GenericJsonObject();

        try {
            String pathInfo = request.getPathInfo();
            log.warning("Path Info: " + pathInfo);

            if (pathInfo == null || pathInfo.length() == 0) {
            }
            else if ("/createLocation".equals(pathInfo)) {
                Location location = new Location();
                location.setPostalCode(request.getParameter(Location.POSTAL_CODE));
                location.setCountryCode(request.getParameter(Location.COUNTRY_CODE));
                location = locationOperations.createLocation(location);
                out.put(Location.KEY, location.getKey());
            }
            else if ("/createStore".equals(pathInfo)) {
                // Supported formats:
                //   http:<host:port>/@servletApiPath@/maezel/createStore?postalCode=H0H0H0&address=number, street, city, postal code, country&name=store name
                //   http:<host:port>/@servletApiPath@/maezel/createStore?postalCode=H0H0H0&address=1, Frozen street, North Pole, H0H 0H0, Canada&name=Toys Factory

                PersistenceManager pm = _baseOperations.getPersistenceManager();
                try {
                    Long locationKey = Long.parseLong(request.getParameter(Store.LOCATION_KEY));

                    Location location = locationOperations.getLocation(pm, locationKey);
                    location.setHasStore(Boolean.TRUE);
                    location = locationOperations.updateLocation(pm, location);

                    Store store = new Store();
                    store.setLocationKey(locationKey);
                    store.setAddress(request.getParameter(Store.ADDRESS));
                    store.setName(request.getParameter(Store.NAME));
                    store.setPhoneNumber(request.getParameter(Store.PHONE_NUMBER));
                    store = storeOperations.createStore(pm, store);
                    out.put(Store.KEY, store.getKey());
                }
                finally {
                    pm.close();
                }
            }
            else if ("/createSaleAssociate".equals(pathInfo)) {
                // Supported formats:
                //   http:<host:port>/@servletApiPath/maezel/createSaleAssociate?store=11&name=Jack the Troll&supplies=wii console xbox gamecube

                PersistenceManager pm = _baseOperations.getPersistenceManager();
                try {
                    Consumer consumer = null;
                    if (request.getParameter(SaleAssociate.CONSUMER_KEY) != null) {
                        consumer = consumerOperations.getConsumer(pm, Long.parseLong(request.getParameter(SaleAssociate.CONSUMER_KEY)));
                    }
                    else if (request.getParameter(SaleAssociate.TWITTER_ID) != null) {
                        // Assume the sale associate candidate has already submitted an account
                        List<Consumer> consumers = consumerOperations.getConsumers(SaleAssociate.TWITTER_ID, request.getParameter(SaleAssociate.TWITTER_ID), 1);
                        if (0 < consumers.size()) {
                            consumer = consumers.get(0);
                        }
                        else {
                            consumer = new Consumer();
                            consumer.setName(request.getParameter(Consumer.NAME));
                            consumer.setEmail(request.getParameter(Consumer.EMAIL));
                            consumer.setTwitterId(request.getParameter(Consumer.TWITTER_ID));
                            consumer.setLanguage(request.getParameter(Consumer.LANGUAGE));
                            consumer = consumerOperations.createConsumer(consumer);
                        }
                    }
                    Long storeKey = Long.valueOf(request.getParameter(Store.STORE_KEY));
                    String name = request.getParameter(SaleAssociate.NAME);

                    SaleAssociate saleAssociate = new SaleAssociate();

                    saleAssociate.setName(name == null ? consumer.getName() : name);
                    saleAssociate.setConsumerKey(consumer.getKey());

                    // Copy the user's attribute
                    saleAssociate.setJabberId(consumer.getJabberId());
                    saleAssociate.setEmail(consumer.getEmail());
                    saleAssociate.setTwitterId(consumer.getTwitterId());
                    saleAssociate.setLanguage(consumer.getLanguage());
                    saleAssociate.setPreferredConnection(request.getParameter(SaleAssociate.PREFERRED_CONNECTION));

                    // Attach to the store
                    saleAssociate.setStoreKey(storeKey);

                    // Set the supplied keywords
                    if (request.getParameter("supplies") != null) {
                        String[] supplies = request.getParameter("supplies").split(" ");
                        for (int i = 0; i < supplies.length; i++) {
                            saleAssociate.addCriterion(supplies[i]);
                        }
                    }

                    // Persist the account
                    saleAssociateOperations.createSaleAssociate(pm, saleAssociate);
                }
                finally {
                    pm.close();
                }
            }
            else if ("/processVerificationCode".equals(pathInfo)) {
                // Custom fields
                String topic = in.getString("topic");
                Boolean waitForCode = in.getBoolean("waitForCode");
                // Consumer fields
                String language = in.getString(Consumer.LANGUAGE);
                Locale locale = new Locale(language);
                // Verification process
                if (Consumer.EMAIL.equals(topic)) {
                    String email = in.getString(Consumer.EMAIL);
                    long code = getCode(topic, email);
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
                        out.put("codeValidity", in.getString(Consumer.EMAIL + "Code").equals("" + code));
                    }
                }
                else if (Consumer.JABBER_ID.equals(topic)) {
                    String jabberId = in.getString(Consumer.JABBER_ID);
                    long code = getCode(topic, jabberId);
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
                        out.put("codeValidity", in.getString(Consumer.JABBER_ID + "Code").equals("" + code));
                    }
                }
                else if (Consumer.TWITTER_ID.equals(topic)) {
                    String twitterId = in.getString(Consumer.TWITTER_ID);
                    long code = getCode(topic, twitterId);
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
                        out.put("codeValidity", in.getString(Consumer.TWITTER_ID + "Code").equals("" + code));
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
            ex.printStackTrace();
            out = new JsonException("UNEXPECTED_EXCEPTION", "Unexpected exception during Maezel.doGet() operation", ex);
        }

        out.toStream(response.getOutputStream(), false);

        // FIXME: remove this trace
        MockOutputStream outS = new MockOutputStream();
        out.toStream(outS, false);
        System.err.println("******************** " + outS.getStream());
    }

    protected long getCode(String topic, String identifier) {
        long code = 0L;
        if (Consumer.EMAIL.equals(topic)) {
            code = Math.abs(7 + identifier.hashCode() * 37);
        }
        else if (Consumer.JABBER_ID.equals(topic)) {
            code = Math.abs(13 + identifier.hashCode() * 29);
        }
        else if (Consumer.TWITTER_ID.equals(topic)) {
            code = Math.abs(23 + identifier.hashCode() * 17);
        }
        else {
            throw new RuntimeException("Unexpected topic: " + topic);
        }
        log.warning("Code " + code + " for " + topic + ": " + identifier);
        return code;
    }
}
