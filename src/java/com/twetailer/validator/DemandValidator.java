package com.twetailer.validator;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twitter4j.TwitterException;

import com.twetailer.DataSourceException;
import com.twetailer.adapter.TwitterUtils;
import com.twetailer.dto.Consumer;
import com.twetailer.dto.Demand;
import com.twetailer.dto.Location;
import com.twetailer.rest.BaseOperations;
import com.twetailer.rest.ConsumerOperations;
import com.twetailer.rest.DemandOperations;
import com.twetailer.rest.LocationOperations;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

public class DemandValidator {
    
    private static final Logger log = Logger.getLogger(DemandValidator.class.getName());

    private BaseOperations _baseOperations = new BaseOperations();
    private ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    private DemandOperations demandOperations = _baseOperations.getDemandOperations();
    private LocationOperations locationOperations = _baseOperations.getLocationOperations();

    public void process() throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        Date nowDate = DateUtils.getNowDate();
        Long nowTime = nowDate.getTime();
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
                        message = LabelExtractor.get("dv_demandRangeInKMTooSmall", new Object[] { demand.getKey(), demand.getRange() }, locale);
                    }
                    else if (LocaleValidator.MILE_UNIT.equals(demand.getRangeUnit()) && (demand.getRange() == null || demand.getRange().doubleValue() < 3.0D)) {
                        message = LabelExtractor.get("dv_demandRangeInMilesTooSmall", new Object[] { demand.getKey(), demand.getRange() }, locale);
                    }
                    else if (demand.getQuantity() == null || demand.getQuantity() == 0L) {
                        message = LabelExtractor.get("dv_demandShouldConcernAtLeastOneItem", new Object[] { demand.getKey() }, locale);
                    }
                    else {
                        Long locationKey = demand.getLocationKey();
                        if (locationKey == null) {
                            message = LabelExtractor.get("dv_demandShouldHaveALocation", new Object[] { demand.getKey() }, locale);
                        }
                        else {
                            try {
                                Location location = locationOperations.getLocation(pm, locationKey);
                                if (Location.INVALID_COORDINATE.equals(location.getLatitude()) || Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                    location = LocaleValidator.getGeoCoordinates(location);
                                    if (Location.INVALID_COORDINATE.equals(location.getLatitude()) || Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                                        message = LabelExtractor.get("dv_demandShouldHaveAValidLocation", new Object[] { demand.getKey(), location.getPostalCode(), location.getCountryCode() }, locale);
                                    }
                                    else {
                                        locationOperations.updateLocation(pm, location);
                                    }
                                }
                            }
                            catch (DataSourceException ex) {
                                message = LabelExtractor.get("dv_unableToGetLocationInformation", new Object[] { demand.getKey() }, locale);
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