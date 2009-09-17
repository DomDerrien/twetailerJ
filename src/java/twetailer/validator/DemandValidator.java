package twetailer.validator;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twitter4j.TwitterException;

import twetailer.DataSourceException;
import twetailer.adapter.TwitterUtils;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

public class DemandValidator {
    
    private static final Logger log = Logger.getLogger(DemandValidator.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();

    public static void process() throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        Date nowDate = DateUtils.getNowDate();
        Long nowTime = nowDate.getTime() - 60*1000; // Minus 1 minute
        try {
            List<Demand> demands = demandOperations.getDemands(pm, Demand.STATE, CommandSettings.State.open.toString(), 0);
            for(Demand demand: demands) {
                try {
                    Consumer consumer = consumerOperations.getConsumer(pm, demand.getConsumerKey());
                    Locale locale = consumer.getLocale();
                    String message = null;
                    if(demand.getCriteria() == null || demand.getCriteria().size() == 0) {
                        message = LabelExtractor.get("dv_demandShouldHaveAtLeastOneTag", new Object[] { demand.getKey() }, locale);
                    }
                    else if (demand.getExpirationDate() == null || demand.getExpirationDate().getTime() < nowTime) {
                        message = LabelExtractor.get("dv_demandExpirationShouldBeInFuture", new Object[] { demand.getKey() }, locale);
                    }
                    else if (LocaleValidator.KILOMETER_UNIT.equals(demand.getRangeUnit()) && (demand.getRange() == null || demand.getRange().doubleValue() < 5.0D)) {
                        message = LabelExtractor.get("dv_demandRangeInKMTooSmall", new Object[] { demand.getKey(), demand.getRange() == null ? 0.0D : demand.getRange() }, locale);
                    }
                    else if (/* LocaleValidator.MILE_UNIT.equals(demand.getRangeUnit()) && */ (demand.getRange() == null || demand.getRange().doubleValue() < 3.0D)) {
                        message = LabelExtractor.get("dv_demandRangeInMilesTooSmall", new Object[] { demand.getKey(), demand.getRange() == null ? 0.0D : demand.getRange() }, locale);
                    }
                    else if (demand.getQuantity() == null || demand.getQuantity() == 0L) {
                        message = LabelExtractor.get("dv_demandShouldConcernAtLeastOneItem", new Object[] { demand.getKey() }, locale);
                    }
                    else {
                        Long locationKey = demand.getLocationKey();
                        if (locationKey == null || locationKey == 0L) {
                            message = LabelExtractor.get("dv_demandShouldHaveALocale", new Object[] { demand.getKey() }, locale);
                        }
                        else {
                            try {
                                Location location = locationOperations.getLocation(pm, locationKey);
                                if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                    location = LocaleValidator.getGeoCoordinates(location);
                                    if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                        message = LabelExtractor.get("dv_demandShouldHaveAValidLocale", new Object[] { demand.getKey(), location.getPostalCode(), location.getCountryCode() }, locale);
                                    }
                                    else {
                                        locationOperations.updateLocation(pm, location);
                                    }
                                }
                            }
                            catch (DataSourceException ex) {
                                message = LabelExtractor.get("dv_unableToGetLocaleInformation", new Object[] { demand.getKey() }, locale);
                            }
                       }
                    }
                    if (message != null) {
                        log.warning("Invalid state for the demand: " + demand.getKey());
                        TwitterUtils.sendDirectMessage(consumer.getTwitterId().toString(), message);
                        demand.setState(CommandSettings.State.invalid);
                    }
                    else {
                        demand.setState(CommandSettings.State.published);
                    }
                    demandOperations.updateDemand(pm, demand);
                }
                catch (DataSourceException ex) {
                    log.warning("Cannot get information for consumer: " + demand.getConsumerKey() + " -- ex: " + ex.getMessage());
                }
                catch (TwitterException ex) {
                    log.warning("Cannot tweet error message to consumer: " + demand.getConsumerKey() + " -- ex: " + ex.getMessage());
                }
            }
        }
        finally {
            pm.close();
        }
    }
}