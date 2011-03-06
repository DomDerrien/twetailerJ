package twetailer.j2ee;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.connector.MailConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.LocationOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.DemandSteps;
import twetailer.task.step.StoreSteps;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.StringUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

/**
 * Third party entry point where the referralId might be
 * compared to the IP address to avoid spam.
 *
 * Possible operations are fairly limited:
 * - post a demand
 * - get number of stores in a region (can filtered with
 * the given parameters)
 *
 * @see twetailer.j2ee.restlet.ConsumerRestlet
 * @see twetailer.j2ee.restlet.DemandRestlet
 * @see twetailer.j2ee.restlet.LocationRestlet
 * @see twetailer.j2ee.restlet.StoreRestlet
 *
 * @author Dom Derrien
 *
 */
@SuppressWarnings("serial")
public class ThirdPartyEntryPointServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(ThirdPartyEntryPointServlet.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    protected final static String DEMAND_PREFIX = "/Demand";
    protected final static String LOCATION_PREFIX = "/Location";
    protected final static String STORE_PREFIX = "/Store";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();

        JsonObject out = new GenericJsonObject();
        out.put("success", true);
        response.setStatus(200);
        JsonObject in = new GenericJsonObject(request);

        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // TODO: verify Content-type = "application/x-www-form-urlencoded"

            if (LOCATION_PREFIX.equals(pathInfo)) {
                verifyReferralId(pm, in, Action.list, Location.class.getName());
                // TODO ...
            }
            else if (STORE_PREFIX.equals(pathInfo)) {
                verifyReferralId(pm, in, Action.list, Store.class.getName());
                List<Store> stores = StoreSteps.getStores(pm, in);
                JsonArray list = new GenericJsonArray();
                for (Store store: stores) {
                    list.add(store.toJson());
                }
                out.put("resources", list);
                Location location = BaseSteps.getLocationOperations().createLocation(pm, in);
                double[] searchBounds = LocationOperations.getLocationBounds(location, in.getDouble(Demand.RANGE), in.getString(Demand.RANGE_UNIT));
                JsonObject bounds = new GenericJsonObject();
                bounds.put("left", searchBounds[0]);
                bounds.put("right", searchBounds[1]);
                bounds.put("bottom", searchBounds[2]);
                bounds.put("top", searchBounds[3]);
                out.put("bounds", bounds);
            }
            else if (DEMAND_PREFIX.equals(pathInfo)) {
                verifyReferralId(pm, in, Action.demand, Demand.class.getName());
                if (!in.containsKey("callback")) {
                    throw new IllegalArgumentException("Invalid JSONP call!");
                }
                createDemand(pm, in, out); // Can be JSONP or HttpMethod.POST
            }
            else if ("/Report".equals(pathInfo)) {
                if (!in.containsKey("callback")) {
                    throw new IllegalArgumentException("Invalid JSONP call!");
                }
                String reportId = in.getString("reportId");
                String message = reportId.length() == 1 ? AuthVerifierFilter.dumpRequest(request) : in.toString();
                getLogger().warning(message);
                String subject = "Landing page visit - Report ";
                if (reportId.length() == 1) {
                    reportId = new java.util.Date().toString();
                }
                else {
                    subject = "Re: " + subject;
                }
                out.put("reportId", reportId);
                MailConnector.reportErrorToAdmins(subject + reportId, message);
            }
            else {
                response.setStatus(404); // Not Found
                out.put("success", false);
                out.put("reason", "URL not supported");
            }
        }
        catch (ReservedOperationException ex) {
            response.setStatus(403); // Forbidden
            out.put("success", false);
            out.put("reason", ex.getMessage());
        }
        catch (Exception ex) {
            response.setStatus(500); // Internal Server Error
            out.put("success", false);
            out.put("reason", ex.getMessage());
            out = BaseRestlet.processException(ex, "doGet", pathInfo, BaseRestlet.debugModeDetected(request));
        }
        finally {
            pm.close();
        }

        if (in.containsKey("callback")) {
            String callbackName = in.getString("callback");
            getLogger().warning("3rd party for JSONP to: " + callbackName + "(" + out.toString() + ")");
            OutputStream stream = response.getOutputStream();
            stream.write(callbackName.getBytes());
            stream.write("(".getBytes());
            out.toStream(stream, false);
            stream.write(")".getBytes());
        }
        else {
            out.toStream(response.getOutputStream(), false);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        JsonObject out = new GenericJsonObject();
        out.put("success", true);
        response.setStatus(200);
        JsonObject in = null;

        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // TODO: verify Content-type == "application/json"
            in = new JsonParser(request.getInputStream(), StringUtils.JAVA_UTF8_CHARSET).getJsonObject();

            if (DEMAND_PREFIX.equals(pathInfo)) {
                verifyReferralId(pm, in, Action.demand, Demand.class.getName());

                createDemand(pm, in, out); // Can be JSONP or HttpMethod.POST
            }
            else {
                response.setStatus(404); // Not Found
                out.put("success", false);
                out.put("reason", "URL not supported");
            }
        }
        catch (ReservedOperationException ex) {
            response.setStatus(403); // Forbidden
            out.put("success", false);
            out.put("reason", ex.getMessage());
        }
        catch (Exception ex) {
            response.setStatus(500); // Internal Server Error
            out.put("success", false);
            out.put("reason", ex.getMessage());
            out = BaseRestlet.processException(ex, "doPost", pathInfo, BaseRestlet.debugModeDetected(in));
        }
        finally {
            pm.close();
        }

        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JsonObject out = new GenericJsonObject();
        response.setStatus(403); // Forbidden
        out.put("success", false);
        out.put("reason", "URL not supported");
        out.toStream(response.getOutputStream(), false);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JsonObject out = new GenericJsonObject();
        response.setStatus(403); // Forbidden
        out.put("success", false);
        out.put("reason", "URL not supported");
        out.toStream(response.getOutputStream(), false);
    }

    public static void verifyReferralId(PersistenceManager pm, JsonObject parameters, Action action, String entityName) throws ReservedOperationException {
        if (!parameters.containsKey(Influencer.REFERRAL_ID)) { // Missing parameter
            getLogger().warning("Missing referralId");
            throw new ReservedOperationException(action, entityName);
        }
        String referralId = parameters.getString(Influencer.REFERRAL_ID).trim();
        if (!BaseSteps.getInfluencerOperations().verifyReferralIdValidity(pm, referralId)) {
            getLogger().warning("Invalid referralId: " + referralId);
            parameters.put(Influencer.REFERRAL_ID, Influencer.DEFAULT_REFERRAL_ID); // Reset the given referral identifier!
        }
        else {
            getLogger().warning("Valid referralId: " + referralId);
        }
    }

    /**
     * Helper verifying the given e-mail address, creating or retrieving the corresponding Consumer
     * record, and creating the specified Demand. The success of the operation pushes a copy of the
     * just created record back to the caller. It doesn't mean that the Demand is valid--the validation
     * will be done asynchronously by the task DemandValidator.
     *
     * @param pm Handles the connection to the back-end storage
     * @param in Request parameters
     * @param out Bag to be sent back to the user, if everything is successful
     *
     * @throws DataSourceException If the Consumer record manipulation fails
     * @throws ClientException If the Demand record creation fails
     */
    protected void createDemand(PersistenceManager pm, JsonObject in, JsonObject out) throws DataSourceException, ClientException {
        String email = in.getString(Consumer.EMAIL);
        if (email == null || email.length() == 0 || !Pattern.matches(Consumer.EMAIL_REGEXP_VALIDATOR, email)) {
            throw new IllegalArgumentException("Invalid sender email address");
        }
        InternetAddress senderAddress;
        try {
            senderAddress = MailConnector.prepareInternetAddress(StringUtils.JAVA_UTF8_CHARSET, email, email);
        }
        catch (AddressException ex) {
            throw new ClientException("Invalid email address", ex);
        }
        Consumer consumer = BaseSteps.getConsumerOperations().createConsumer(pm, senderAddress, false); // Not verified e-mail address yet

        if (consumer.getAutomaticLocaleUpdate()) {
            String language = in.getString(Consumer.LANGUAGE);
            if (language != null && 0 < language.length() && !consumer.getLanguage().equals(LocaleValidator.checkLanguage(language))) {
                consumer.setLanguage(language);
                BaseSteps.getConsumerOperations().updateConsumer(pm, consumer);
            }
        }

        in.put(Demand.SOURCE, Source.widget.toString());
        Demand demand = DemandSteps.createDemand(pm, in, consumer);
        out.put("resource", demand.toJson());
    }
}
