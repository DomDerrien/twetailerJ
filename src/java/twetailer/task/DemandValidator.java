package twetailer.task;

import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

public class DemandValidator {

    private static final Logger log = Logger.getLogger(DemandValidator.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();

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
    public static final Double RANGE_MI_MIN = Double.valueOf(3.0D);
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
                if(demand.getCriteria() == null || demand.getCriteria().size() == 0) {
                    message = LabelExtractor.get("dv_report_demand_without_tag", new Object[] { demand.getKey() }, locale);
                }
                else if (demand.getExpirationDate() == null || demand.getExpirationDate().getTime() < nowTime) {
                    message = LabelExtractor.get("dv_report_expiration_in_past", new Object[] { demand.getKey() }, locale);
                }
                else if (LocaleValidator.KILOMETER_UNIT.equals(demand.getRangeUnit()) && (demand.getRange() == null || demand.getRange().doubleValue() < RANGE_KM_MIN.doubleValue())) {
                    message = LabelExtractor.get("dv_report_range_km_too_small", new Object[] { demand.getKey(), demand.getRange() == null ? 0.0D : demand.getRange(), RANGE_KM_MIN }, locale);
                }
                else if (/* LocaleValidator.MILE_UNIT.equals(demand.getRangeUnit()) && */ (demand.getRange() == null || demand.getRange().doubleValue() < RANGE_MI_MIN.doubleValue())) {
                    message = LabelExtractor.get("dv_report_range_mi_too_small", new Object[] { demand.getKey(), demand.getRange() == null ? 0.0D : demand.getRange(), RANGE_MI_MIN }, locale);
                }
                else if (LocaleValidator.MILE_UNIT.equals(demand.getRangeUnit()) && demand.getRange().doubleValue() > RANGE_MI_MAX.doubleValue()) {
                    message = LabelExtractor.get("dv_report_range_mi_too_big", new Object[] { demand.getKey(), demand.getRange(), RANGE_MI_MAX }, locale);
                }
                else if (/* LocaleValidator.KILOMETER_UNIT.equals(demand.getRangeUnit()) && */ demand.getRange().doubleValue() > RANGE_KM_MAX.doubleValue()) {
                    message = LabelExtractor.get("dv_report_range_km_too_big", new Object[] { demand.getKey(), demand.getRange(), RANGE_KM_MAX }, locale);
                }
                else if (demand.getQuantity() == null || demand.getQuantity() == 0L) {
                    message = LabelExtractor.get("dv_report_quantity_zero", new Object[] { demand.getKey() }, locale);
                }
                else {
                    Long locationKey = demand.getLocationKey();
                    if (locationKey == null || locationKey == 0L) {
                        message = LabelExtractor.get("dv_report_missing_locale", new Object[] { demand.getKey() }, locale);
                    }
                    else {
                        try {
                            Location location = locationOperations.getLocation(pm, locationKey);
                            if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                location = LocaleValidator.getGeoCoordinates(location);
                                if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                    message = LabelExtractor.get("dv_report_invalid_locale", new Object[] { demand.getKey(), location.getPostalCode(), location.getCountryCode() }, locale);
                                }
                                else {
                                    location = locationOperations.updateLocation(pm, location);
                                }
                            }
                        }
                        catch (DataSourceException ex) {
                            message = LabelExtractor.get("dv_report_unable_to_get_locale_information", new Object[] { demand.getKey() }, locale);
                        }
                   }
                }
                if (message != null) {
                    log.warning("Invalid state for the demand: " + demand.getKey() + " -- message: " + message);
                    communicateToConsumer(demand.getSource(), consumer, message);
                    demand.setState(CommandSettings.State.invalid);
                }
                else {
                    demand.setState(CommandSettings.State.published);

                    // Create a task for that demand
                    Queue queue = QueueFactory.getDefaultQueue();
                    queue.add(url("/API/maezel/processPublishedDemand").param(Demand.KEY, demandKey.toString()).method(Method.GET));
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
}
