package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

public class DemandValidator {

    private static Logger log = Logger.getLogger(DemandValidator.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();

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
     */
    public static void process(Long demandKey) throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
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

    /**
     * Check the validity of the identified demand
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demandKey Identifier of the demand to process
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void process(PersistenceManager pm, Long demandKey) throws DataSourceException {
        Demand demand = demandOperations.getDemand(pm, demandKey, null);
        if (CommandSettings.State.opened.equals(demand.getState())) {
            Date nowDate = DateUtils.getNowDate();
            Long nowTime = nowDate.getTime() - 60*1000; // Minus 1 minute
            try {
                Consumer consumer = consumerOperations.getConsumer(pm, demand.getOwnerKey());
                Locale locale = consumer.getLocale();
                String message = null;

                // Temporary filter
                filterHashTags(pm, consumer, demand);

                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                if (demand.getCriteria() == null || demand.getCriteria().size() == 0) {
                    message = LabelExtractor.get("dv_report_demand_without_tag", new Object[] { demandRef }, locale);
                }
                else if (demand.getExpirationDate() == null || demand.getExpirationDate().getTime() < nowTime) {
                    message = LabelExtractor.get("dv_report_expiration_in_past", new Object[] { demandRef }, locale);
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
                            Location location = locationOperations.getLocation(pm, locationKey);
                            if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                location = LocaleValidator.getGeoCoordinates(location);
                                if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                    message = LabelExtractor.get("dv_report_invalid_locale", new Object[] { demandRef, location.getPostalCode(), location.getCountryCode() }, locale);
                                }
                                else {
                                    location = locationOperations.updateLocation(pm, location);
                                }
                            }
                            // Save the location key as the latest reference used by the consumer
                            if (message == null && consumer.getAutomaticLocaleUpdate() && !location.getKey().equals(consumer.getLocationKey())) {
                                consumer.setLocationKey(location.getKey());
                                consumerOperations.updateConsumer(pm, consumer);
                            }
                        }
                        catch (DataSourceException ex) {
                            message = LabelExtractor.get("dv_report_unable_to_get_locale_information", new Object[] { demandRef }, locale);
                        }
                   }
                }
                if (message != null) {
                    log.warning("Invalid state for the demand: " + demand.getKey() + " -- message: " + message);
                    communicateToConsumer(
                            new RawCommand(demand.getSource()),
                            consumer,
                            new String[] { message }
                    );
                    demand.setState(CommandSettings.State.invalid);
                }
                else {
                    demand.setState(CommandSettings.State.published);

                    // Create a task for that demand
                    Queue queue = _baseOperations.getQueue();
                    log.warning("Preparing the task: /maezel/processPublishedDemand?key=" + demandKey.toString());
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maezel/processPublishedDemand").
                                param(Demand.KEY, demandKey.toString()).
                                method(Method.GET)
                    );
                }
                demand = demandOperations.updateDemand(pm, demand);
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
                if (hashTags.size() == 1 && !RobotResponder.ROBOT_DEMO_HASH_TAG.equals(hashTag)) {
                    serializedHashTags = hashTag;
                }
                else { // if (1 < hashTags.size()) {
                    for(int i = 0; i < hashTags.size(); ++i) {
                        hashTag = hashTags.get(i);
                        if (!RobotResponder.ROBOT_DEMO_HASH_TAG.equals(hashTag)) {
                            serializedHashTags += " " + hashTag;
                        }
                    }
                }
                if (0 < serializedHashTags.length()) {
                    Locale locale = consumer.getLocale();
                    String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                    String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { serializedHashTags.trim() }, locale);
                    communicateToConsumer(
                            new RawCommand(demand.getSource()),
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
}
