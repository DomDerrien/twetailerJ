package twetailer.j2ee;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Text;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MailConnector;
import twetailer.dao.InfluencerOperations;
import twetailer.dao.LocationOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Consumer.Autonomy;
import twetailer.dto.Demand;
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.Report;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.ConsumerSteps;
import twetailer.task.step.DemandSteps;
import twetailer.task.step.StoreSteps;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.LocaleValidator;
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

    protected static final String LOCATION_PREFIX = "/" + Location.class.getSimpleName();
    protected static final String STORE_PREFIX = "/" + Store.class.getSimpleName();
    protected static final String CONSUMER_PREFIX = "/" + Consumer.class.getSimpleName();
    protected static final String DEMAND_PREFIX = "/" + Demand.class.getSimpleName();
    protected static final String REPORT_PREFIX = "/" + Report.class.getSimpleName();

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
            // Get the Store instances around the described Location
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
            // Verify if the given email address is attached to a known Consumer
            else if (CONSUMER_PREFIX.equals(pathInfo)) {
                verifyReferralId(pm, in, Action.list, Consumer.class.getName());
                if (!in.containsKey("callback")) {
                    throw new IllegalArgumentException("Invalid JSONP call!");
                }
                Consumer consumer = lookupConsumer(pm, in, out); // Can be JSONP or HttpMethod.POST

                String reportId = in.getString("reportId");
                boolean newReport = reportId.length() == 1;
                if (!newReport) {
                    Report report = BaseSteps.getReportOperations().getReport(pm, Long.valueOf(reportId));
                    report.setConsumerKey(consumer.getKey());
                    BaseSteps.getReportOperations().updateReport(pm, report);
                }
            }
            // Create the described Demand
            else if (DEMAND_PREFIX.equals(pathInfo)) {
                verifyReferralId(pm, in, Action.demand, Demand.class.getName());
                if (!in.containsKey("callback")) {
                    throw new IllegalArgumentException("Invalid JSONP call!");
                }
                Demand demand = createDemand(pm, in, out); // Can be JSONP or HttpMethod.POST

                String reportId = in.getString("reportId");
                boolean newReport = reportId == null || reportId.length() == 1;
                if (!newReport) {
                    Report report = BaseSteps.getReportOperations().getReport(pm, Long.valueOf(reportId));
                    report.setDemandKey(demand.getKey());
                    report.setConsumerKey(demand.getOwnerKey());
                    BaseSteps.getReportOperations().updateReport(pm, report);
                }
            }
            // Create or update a Report
            else if (REPORT_PREFIX.equals(pathInfo)) {
                if (!in.containsKey("callback")) {
                    throw new IllegalArgumentException("Invalid JSONP call!");
                }
                Report report = null;
                String reportId = in.getString("reportId");
                boolean newReport = reportId.length() == 1;
                if (newReport) {
                    report = createReport(pm, request, in);
                    out.put("reportDelay", 4000); // Only 4 seconds after the initial call, 15 seconds between all other calls
                    out.put("reportId", report.getKey().toString());
                }
                else {
                    report = updateReport(pm, in);
                    out.put("reportDelay", 15000); // Only 4 seconds after the initial call, 15 seconds between all other calls
                    out.put("reportId", reportId);
                }
                BaseSteps.getReportOperations().trackRecentReport(report.getKey());
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
            response.setStatus(200); // To be able to report possible exception
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

            if (CONSUMER_PREFIX.equals(pathInfo)) {
                verifyReferralId(pm, in, Action.demand, Demand.class.getName());

                lookupConsumer(pm, in, out); // Can be JSONP or HttpMethod.POST
            }
            else if (DEMAND_PREFIX.equals(pathInfo)) {
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

    /**
     * Helper verifying the validity of the passed referralId
     *
     * @param pm Handles the connection to the back-end storage
     * @param in Request parameters
     * @param action Action associated to the verification
     * @param entityName Entity concerned by the verification
     *
     * @throws ReservedOperationException If the referralId is invalid
     */
    public static void verifyReferralId(PersistenceManager pm, JsonObject in, Action action, String entityName) throws ReservedOperationException {
        if (!in.containsKey(Influencer.REFERRAL_ID)) { // Missing parameter
            getLogger().warning("Missing referralId");
            throw new ReservedOperationException(action, entityName);
        }
        String referralId = in.getString(Influencer.REFERRAL_ID).trim();
        if (!BaseSteps.getInfluencerOperations().verifyReferralIdValidity(pm, referralId)) {
            getLogger().warning("Invalid referralId: " + referralId);
            in.put(Influencer.REFERRAL_ID, Influencer.DEFAULT_REFERRAL_ID); // Reset the given referral identifier!
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
     * @return Just created Demand record
     *
     * @throws DataSourceException If the Consumer record manipulation fails
     * @throws ClientException If the Demand record creation fails
     */
    protected Demand createDemand(PersistenceManager pm, JsonObject in, JsonObject out) throws DataSourceException, ClientException {
        Consumer consumer = createConsumer(pm, in);

        in.put(Demand.SOURCE, Source.widget.toString());
        Demand demand = DemandSteps.createDemand(pm, in, consumer);

        // TODO: anonymize the demand so it's not possible to use the Consumer key
        out.put("resource", demand.toJson());

        return demand;
    }

    /**
     * Create a Consumer record with the given email address
     *
     * @param pm Handles the connection to the back-end storage
     * @param in Request parameters
     * @return Consumer record just created
     *
     * @throws ClientException If the email is invalid
     * @throws DataSourceException If the Consumer record management fails
     */
    protected Consumer createConsumer(PersistenceManager pm, JsonObject in) throws ClientException, DataSourceException {
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
                consumer = BaseSteps.getConsumerOperations().updateConsumer(pm, consumer);
            }
        }

        return consumer;
    }

    /**
     * Helper verifying the presence of a Consumer record with the given identifier
     *
     * @param pm Handles the connection to the back-end storage
     * @param in Request parameters
     * @param out Bag to be sent back to the user, if everything is successful
     * @return the retrieved or created Consumer record
     *
     * @throws DataSourceException If the Consumer record manipulation fails
     * @throws InvalidIdentifierException If the Influencer record cannot be retrieved
     * @throws ClientException If the given email address is mal-formed
     */
    protected Consumer lookupConsumer(PersistenceManager pm, JsonObject in, JsonObject out) throws DataSourceException, InvalidIdentifierException, ClientException {
        String email = in.getString(Consumer.EMAIL);
        if (email == null || email.length() == 0 || !Pattern.matches(Consumer.EMAIL_REGEXP_VALIDATOR, email)) {
            throw new IllegalArgumentException("Invalid sender email address");
        }

        List<Consumer> consumers = BaseSteps.getConsumerOperations().getConsumers(pm, Consumer.EMAIL, email, 1);
        Consumer consumer = consumers.size() == 0 ? null : consumers.get(0);
        if (consumer == null || Autonomy.UNCONFIRMED.equals(consumer.getAutonomy())) {
            out.put("status", false);

            if (consumer == null) {
                consumer = createConsumer(pm, in);
                out.put("recordCreated", true);
            }

            String referralId = in.getString(Influencer.REFERRAL_ID).trim();
            Long influencerKey = InfluencerOperations.getInfluencerKey(referralId);
            Influencer influencer = BaseSteps.getInfluencerOperations().getInfluencer(pm, influencerKey);

            List<String> hashTags = null;
            if (in.containsKey(Command.HASH_TAGS)) {
                hashTags = new ArrayList<String>(in.getJsonArray(Command.HASH_TAGS).size());
                for (Object hashTag: in.getJsonArray(Command.HASH_TAGS).getList()) {
                    hashTags.add((String) hashTag);
                }
            }

            boolean notified = ConsumerSteps.notifyUnconfirmedConsumer(consumer, hashTags, null, influencer, getLogger());
            out.put("notified", notified);
        }
        else {
            out.put("status", true);
            out.put("name", consumer.getName());
        }

        return consumer;
    }

    /**
     * Helper creating a new report and saving it into the data store
     *
     * @param pm Handles the connection to the back-end storage
     * @param request Descriptor of the HTTP request
     * @param in Request parameters
     * @return The newly created Report instance
     */
    protected Report createReport(PersistenceManager pm, HttpServletRequest request, JsonObject in) {
        Report report = new Report();

        report.setLanguage(LocaleValidator.checkLanguage(in.getString(Report.LANGUAGE)));

        if (in.containsKey(Report.CONTENT)) {
            String content = in.getString(Report.CONTENT);
            if (0 < content.length()) {
                report.setContent(content);
            }
        }
        report.setIpAddress(request.getRemoteAddr());
        try {
            report.setRange(in.getDouble(Report.RANGE));
        }
        catch(ClassCastException ex) {
            // Invalid range, just ignored
        }
        if (in.containsKey(Report.REFERRER_URL)) {
            report.setReferrerUrl(new Text(in.getString(Report.REFERRER_URL))); // Referrer of the landing page
        }
        report.setReporterUrl(request.getHeader("Referer")); // Landing page URL
        report.setUserAgent(request.getHeader("User-Agent"));

        Location location = new Location();
        location.setCountryCode(in.getString(Location.COUNTRY_CODE));
        location.setPostalCode(in.getString(Location.POSTAL_CODE), location.getCountryCode());
        location = BaseSteps.getLocationOperations().createLocation(pm, location);

        report.setLocationKey(location.getKey());

        report = BaseSteps.getReportOperations().createReport(pm, report);

        getLogger().warning("New report created: " + report.getKey());
        return report;
    }

    /**
     * Helper updating an existing report and saving it into the data store
     *
     * @param pm Handles the connection to the back-end storage
     * @param in Request parameters
     * @return The just updated Report instance
     *
     * @throws InvalidIdentifierException If the resource identifiers are invalid
     * @throws DataSourceException If the data could not be persisted into the data store
     */
    protected Report updateReport(PersistenceManager pm, JsonObject in) throws InvalidIdentifierException, DataSourceException {
        Report report = BaseSteps.getReportOperations().getReport(pm, Long.valueOf(in.getString("reportId")));

        String email = in.getString(Consumer.EMAIL);
        if (email != null && 0 < email.length() && report.getConsumerKey() != null) {
            Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, report.getConsumerKey());
            if (!consumer.getEmail().equals(email)) {
                // TODO: should the consumerKey field be reset?
            }
        }

        String countryCode = LocaleValidator.checkCountryCode(in.getString(Location.COUNTRY_CODE));
        String postalCode = LocaleValidator.standardizePostalCode(in.getString(Location.POSTAL_CODE), countryCode);
        // TODO: use the countryCode and verify the postalCode against all default values!
        if (!LocaleValidator.DEFAULT_POSTAL_CODE_CA.equals(postalCode)) {
            Location location = BaseSteps.getLocationOperations().getLocation(pm, report.getLocationKey());
            if (!location.getCountryCode().equals(countryCode) || !location.getPostalCode().equals(postalCode)) {
                location = new Location();
                location.setCountryCode(in.getString(Location.COUNTRY_CODE));
                location.setPostalCode(in.getString(Location.POSTAL_CODE), location.getCountryCode());
                location = BaseSteps.getLocationOperations().createLocation(pm, location);

                report.setLocationKey(location.getKey());
            }
        }

        try {
            Double range = in.getDouble(Demand.RANGE);
            if (!range.equals(report.getRange())) {
                report.setRange(range);
            }
        }
        catch(ClassCastException ex) {
            // Invalid range, just ignored
        }

        String content = in.getString(Demand.CONTENT);
        if (content != null && 0 < content.length() && !content.equals(report.getContent())) {
            report.setContent(content);
        }

        report.updateModificationDate(); // The updated lastModificationDate value will allow to track the time spend in the page
        report = BaseSteps.getReportOperations().updateReport(pm, report);

        return report;
    }
}
