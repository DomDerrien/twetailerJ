package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToCCed;
import static twetailer.connector.BaseConnector.getCCedCommunicationChannel;

import java.util.Date;
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
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.HashTag;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.LocationSteps;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;

/**
 * Define the task with is invoked by methods in DemandSteps
 * every time a Demand is updated significantly. If the Demand
 * instance is valid, the task "/maelzel/processPublishedDemand"
 * is scheduled to broadcast it to the matching SaleAssociate
 * in the area.
 *
 * @see twetailer.dto.Demand
 * @see twetailer.task.step.DemandSteps
 * @see twetailer.task.DemandProcessor
 *
 * @author Dom Derrien
 */
public class DemandValidator {

    private static Logger log = Logger.getLogger(DemandValidator.class.getName());

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Check the validity of the identified demand
     *
     * @param demandKey Identifier of the demand to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(Long demandKey) throws DataSourceException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            process(pm, demandKey);
        }
        finally {
            pm.close();
        }
    }

    public static final Double RANGE_KM_MIN = Double.valueOf(5.0D);
    public static final Double RANGE_KM_MAX = Double.valueOf(40075.0D);
    public static final Double RANGE_MI_MIN = Double.valueOf(2.5D);
    public static final Double RANGE_MI_MAX = Double.valueOf(24906.0D);

    private static final Long oneYear = 365 * 24 * 60 * 60 * 1000L;

    /**
     * Check the validity of the identified demand
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demandKey Identifier of the demand to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(PersistenceManager pm, Long demandKey) throws DataSourceException, InvalidIdentifierException {
        Demand demand = BaseSteps.getDemandOperations().getDemand(pm, demandKey, null);
        if (CommandSettings.State.opened.equals(demand.getState())) {
            Date nowDate = DateUtils.getNowDate();
            Long nowTime = nowDate.getTime() - 60 * 1000L; // Minus 1 minute
            try {
                Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
                Locale locale = consumer.getLocale();
                String message = null;

                // Temporary filter
                filterHashTags(pm, consumer, demand);

                // System.err.println("========================\n now: " + nowTime + "\n exp: " + demand.getExpirationDate() + "\n due: " + demand.getDueDate() + "\n========================");

                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                if ((demand.getCriteria() == null || demand.getCriteria().size() == 0) && (demand.getHashTags() == null || demand.getHashTags().size() == 0)) {
                    message = LabelExtractor.get("dv_report_demand_without_tag", new Object[] { demandRef }, locale);
                }
                else if (demand.getDueDate() == null || demand.getDueDate().getTime() < nowTime) {
                    log.warning("Demand: " + demand.getKey() + "\nNow: " + nowDate + "\nDue: " + demand.getDueDate());
                    message = LabelExtractor.get("dv_report_due_in_past", new Object[] { demandRef }, locale);
                }
                else if (nowTime + oneYear < demand.getDueDate().getTime()) {
                    log.warning("Demand: " + demand.getKey() + "\nNow: " + nowDate + "\n+1 year: " + new Date(nowTime + oneYear) + "\nDue: " + demand.getDueDate());
                    message = LabelExtractor.get("dv_report_due_too_far_in_future", new Object[] { demandRef }, locale);
                }
                else if (demand.getExpirationDate() == null || demand.getExpirationDate().getTime() < nowTime) {
                    log.warning("Demand: " + demand.getKey() + "\nNow: " + nowDate + "\nExpiration: " + demand.getExpirationDate());
                    message = LabelExtractor.get("dv_report_expiration_in_past", new Object[] { demandRef }, locale);
                }
                else if (nowTime + oneYear < demand.getExpirationDate().getTime()) {
                    log.warning("Demand: " + demand.getKey() + "\nNow: " + nowDate + "\n+1 year: " + new Date(nowTime + oneYear) + "\nExpiration: " + demand.getExpirationDate());
                    message = LabelExtractor.get("dv_report_expiration_too_far_in_future", new Object[] { demandRef }, locale);
                }
                else if (demand.getDueDate().getTime() < demand.getExpirationDate().getTime()) {
                    message = LabelExtractor.get("dv_report_expiration_before_due_date", new Object[] { demandRef }, locale);
                }
                else if (LocaleValidator.KILOMETER_UNIT.equals(demand.getRangeUnit()) && (demand.getRange() == null || demand.getRange().doubleValue() < RANGE_KM_MIN.doubleValue())) {
                    String rangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { demand.getRange() == null ? 0.0D : demand.getRange(), LocaleValidator.KILOMETER_UNIT }, locale);
                    String minRangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { RANGE_KM_MIN, LocaleValidator.KILOMETER_UNIT }, locale);
                    message = LabelExtractor.get("dv_report_range_too_small", new Object[] { demandRef, rangeDef, minRangeDef, LocaleValidator.KILOMETER_UNIT }, locale);
                }
                else if (/* LocaleValidator.MILE_UNIT.equals(demand.getRangeUnit()) && */ (demand.getRange() == null || demand.getRange().doubleValue() < RANGE_MI_MIN.doubleValue())) {
                    String rangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { demand.getRange() == null ? 0.0D : demand.getRange(), LocaleValidator.MILE_UNIT }, locale);
                    String minRangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { RANGE_MI_MIN, LocaleValidator.MILE_UNIT }, locale);
                    message = LabelExtractor.get("dv_report_range_too_small", new Object[] { demandRef, rangeDef, minRangeDef, LocaleValidator.MILE_UNIT }, locale);
                }
                else if (LocaleValidator.MILE_UNIT.equals(demand.getRangeUnit()) && demand.getRange().doubleValue() > RANGE_MI_MAX.doubleValue()) {
                    String rangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { demand.getRange(), LocaleValidator.MILE_UNIT }, locale);
                    String maxRangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { RANGE_MI_MAX, LocaleValidator.MILE_UNIT }, locale);
                    message = LabelExtractor.get("dv_report_range_too_big", new Object[] { demandRef, rangeDef, maxRangeDef, LocaleValidator.MILE_UNIT }, locale);
                }
                else if (/* LocaleValidator.KILOMETER_UNIT.equals(demand.getRangeUnit()) && */ demand.getRange().doubleValue() > RANGE_KM_MAX.doubleValue()) {
                    String rangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { demand.getRange(), LocaleValidator.KILOMETER_UNIT }, locale);
                    String maxRangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { RANGE_KM_MAX, LocaleValidator.KILOMETER_UNIT }, locale);
                    message = LabelExtractor.get("dv_report_range_too_big", new Object[] { demandRef, rangeDef, maxRangeDef, LocaleValidator.KILOMETER_UNIT }, locale);
                }
                else if (demand.getQuantity() == null || demand.getQuantity() == 0L) {
                    message = LabelExtractor.get("dv_report_quantity_zero", new Object[] { demandRef }, locale);
                }
                else {
                    Long locationKey = demand.getLocationKey();
                    if (locationKey == null || locationKey == 0L) {
                        message = LabelExtractor.get("dv_report_missing_locale", new Object[] { demandRef }, locale);
                    }
                    else {
                        try {
                            //
                            // At this step, it should be possible to call delegate the locale validation
                            // as done in ListCommandProcess.getLocation()
                            // ** It might be overkill to create 2 additional tasks if everything can be done here **
                            //
                            Location location = LocationSteps.getLocation(pm, locationKey);
                            if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                location = LocaleValidator.getGeoCoordinates(location);
                                if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                    message = LabelExtractor.get("dv_report_invalid_locale", new Object[] { demandRef, location.getPostalCode(), location.getCountryCode() }, locale);
                                }
                                else {
                                    location = BaseSteps.getLocationOperations().updateLocation(pm, location);
                                }
                            }
                            // Save the location key as the latest reference used by the consumer
                            if (message == null && consumer.getAutomaticLocaleUpdate() && !location.getKey().equals(consumer.getLocationKey())) {
                                consumer.setLocationKey(location.getKey());
                                BaseSteps.getConsumerOperations().updateConsumer(pm, consumer);
                            }
                        }
                        catch (InvalidIdentifierException ex) {
                            message = LabelExtractor.get("dv_report_unable_to_get_locale_information", new Object[] { demandRef }, locale);
                        }
                   }
                }
                RawCommand rawCommand = demand.getRawCommandId() == null ? new RawCommand(demand.getSource()) : BaseSteps.getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId());
                if (message != null) {
                    demand.setState(CommandSettings.State.invalid);
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);

                    if (!Source.api.equals(demand.getSource())) {
                        communicateToConsumer(
                            rawCommand.getSource(),
                            rawCommand.getSubject(),
                            consumer,
                            new String[] { message }
                        );
                    }
                }
                else {
                    demand.setState(CommandSettings.State.published);
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);

                    // Create a task for that demand
                    Queue queue = BaseSteps.getBaseOperations().getQueue();
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maelzel/processPublishedDemand").
                                param(Demand.KEY, demandKey.toString()).
                                method(Method.GET).
                                countdownMillis(5000)
                    );

                    confirmUpdate(rawCommand, demand, LocationSteps.getLocation(pm, demand), consumer);
                }
            }
            catch (DataSourceException ex) {
                log.warning("Cannot get information for consumer: " + demand.getOwnerKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                log.warning("Cannot communicate with consumer -- ex: " + ex.getMessage());
            }
        }
    }

    // Temporary method filtering out non #demo tags
    protected static void filterHashTags(PersistenceManager pm, Consumer consumer, Demand demand) throws ClientException, DataSourceException {
        if (demand.getHashTags() != null) {
            List<String> hashTags = demand.getHashTags();
            if (hashTags.size() != 0) {
                String serializedHashTags = "";
                String hashTag = hashTags.get(0);
                if (hashTags.size() == 1 && !HashTag.isSupportedHashTag(hashTag)) {
                    serializedHashTags = hashTag;
                }
                else { // if (1 < hashTags.size()) {
                    for(int i = 0; i < hashTags.size(); ++i) {
                        hashTag = hashTags.get(i);
                        if (!HashTag.isSupportedHashTag(hashTag)) {
                            serializedHashTags += " " + hashTag;
                        }
                    }
                }
                if (0 < serializedHashTags.length()) {
                    Locale locale = consumer.getLocale();
                    String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                    String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { serializedHashTags.trim() }, locale);
                    communicateToConsumer(
                            demand.getSource(), // TODO: maybe pass the initial RawCommand to be able to reuse the subject
                            "To be fixed!", // FIXME
                            consumer,
                            new String[] { LabelExtractor.get("dv_report_hashtag_warning", new Object[] { demandRef, tags }, locale) }
                    );
                    for (String tag: serializedHashTags.split(" ")) {
                        demand.removeHashTag(tag);
                    }
                }
            }
        }
    }

    /**
     * Prepare the messages sent to the demand owner and to the CC'ed users to notify them
     * about the new demand state.
     *
     * @param rawCommand rawCommand at the origin of the demand creation or update
     * @param demand Demand instance just validate after its creation or update
     * @param location Location attached to the demand
     * @param owner Demand owner
     *
     * @throws CommunicationException If the communication with the demand owner fails
     */
    public static void confirmUpdate(RawCommand rawCommand, Demand demand, Location location, Consumer owner) throws CommunicationException {

        List<String> cc = demand.getCC();
        if (!Source.api.equals(demand.getSource()) || cc != null && 0 < cc.size()) {
            boolean isNewDemand = demand.getCreationDate().getTime() == demand.getModificationDate().getTime();
            Locale locale = owner.getLocale();

            MessageGenerator msgGen = null;
            String message = null;
            String subject = null;

            if (!Source.api.equals(demand.getSource())) {
                msgGen = new MessageGenerator(demand.getSource(), demand.getHashTags(), locale);
                msgGen.
                    put("demand>owner>name", owner.getName()).
                    fetch(demand).
                    fetch(location, "demand").
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                Map<String, Object> cmdPrm = new HashMap<String, Object>();
                cmdPrm.put("demand>key", demand.getKey());
                cmdPrm.put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));
                String cancelDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_cancel", cmdPrm, locale);
                // String updateDemand = "update demand:" + demand.getKey().toString();

                if (Source.mail.equals(demand.getSource()) && rawCommand.getSubject() != null) {
                    subject = rawCommand.getSubject();
                }
                else {
                    subject = msgGen.getAlternateMessage(MessageId.messageSubject, cmdPrm);
                }

                msgGen.
                    put("control>threadSubject", subject.replaceAll(" ", "%20")).
                    put("control>cancelDemand", cancelDemand.replaceAll(" ", "%20").replaceAll("\n", "%0A"));
                    // put("control>updateDemand", updateDemand.replaceAll(" ", "%20").replaceAll("\n", "%0A"));
                if (Source.mail.equals(demand.getSource()) && rawCommand.getSubject() != null) {
                    msgGen.put("control>threadSubject", rawCommand.getSubject());
                }

                message = msgGen.getMessage(isNewDemand ? MessageId.demandCreationAck: MessageId.demandUpdateAck);

                communicateToConsumer(
                        rawCommand.getSource(),
                        subject,
                        owner,
                        new String[] { message }
                );
            }

            // Send a notification to the CC'ed users
            if (cc != null && 0 < cc.size()) {
                for (String coordinate: cc) {
                    try {
                        Source source = getCCedCommunicationChannel(coordinate);

                        if (msgGen == null || !source.equals(msgGen.getCommunicationChannel())) {
                            //
                            // TODO: cache the MessageGenerator instance per Source value to avoid unnecessary re-creation!
                            //
                            msgGen = new MessageGenerator(Source.mail, demand.getHashTags(), locale);
                            msgGen.
                                put("demand>owner>name", owner.getName()).
                                fetch(demand).
                                fetch(location, "demand").
                                put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                            message = msgGen.getMessage(isNewDemand ? MessageId.demandCreationCpy: MessageId.demandUpdateCpy);
                        }

                        if (subject == null) {
                            Map<String, Object> cmdPrm = new HashMap<String, Object>();
                            cmdPrm.put("demand>key", demand.getKey());
                            subject = msgGen.getAlternateMessage(MessageId.messageSubject, cmdPrm).replaceAll(" ", "%20");
                        }

                        communicateToCCed(
                                source,
                                coordinate,
                                subject,
                                message,
                                locale
                        );
                    }
                    catch (ClientException e) { } // Too bad, cannot contact the CC-ed person... Don't block the next sending!
                }
            }
        }
    }
}
