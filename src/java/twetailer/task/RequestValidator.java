package twetailer.task;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.HashTag;
import twetailer.dto.Location;
import twetailer.dto.Request;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.LocationSteps;
import twetailer.validator.LocaleValidator;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

/**
 * Core methods used by WishValidator and DemandValidator.
 *
 * @see twetailer.dto.Wish
 * @see twetailer.task.step.WishSteps
 * @see twetailer.task.WishProcessor
 *
 * @author Dom Derrien
 */
public class RequestValidator {

    private static Logger log = Logger.getLogger(RequestValidator.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    public static final Double RANGE_KM_MIN = Double.valueOf(5.0D);
    public static final Double RANGE_KM_MAX = Double.valueOf(40075.0D);
    public static final Double RANGE_MI_MIN = Double.valueOf(2.5D);
    public static final Double RANGE_MI_MAX = Double.valueOf(24906.0D);

    private static final Long oneYear = 365 * 24 * 60 * 60 * 1000L;

    public enum ValidationStatus {
        ok,
        noTagNorHashTag,
        noDueDate,
        dueDateInPast,
        dueDateTooFarInFuture,
        noExpirationDate,
        expirationDateInPast,
        expirationDateTooFarInFuture,
        dueDateBeforeExpiration,
        rangeTooSmall,
        rangeTooLarge,
        noQuantity,
        noLocationKey,
        invalidLocation,
        invalidLocationKey,
        locationUpdateFailed
    }

    protected static ValidationStatus checkRequestFields(PersistenceManager pm, Consumer consumer, Request request) {
        Date nowDate = DateUtils.getNowDate();
        Long nowTime = nowDate.getTime() - 60 * 1000L; // Minus 1 minute

        // getLogger().finest("========================\n now: " + nowTime + "\n exp: " + request.getExpirationDate() + "\n due: " + request.getDueDate() + "\n========================");

        if ((request.getContent().length() == 0) && (request.getHashTags() == null || request.getHashTags().size() == 0)) {
            return ValidationStatus.noTagNorHashTag;
        }
        if (request.getDueDate() == null) {
            return ValidationStatus.noDueDate;
        }
        if (request.getDueDate().getTime() < nowTime) {
            return ValidationStatus.dueDateInPast;
        }
        if (nowTime + oneYear < request.getDueDate().getTime()) {
            return ValidationStatus.dueDateTooFarInFuture;
        }
        if (request.getExpirationDate() == null) {
            return ValidationStatus.noExpirationDate;
        }
        if (request.getExpirationDate().getTime() < nowTime) {
            return ValidationStatus.expirationDateInPast;
        }
        if (nowTime + oneYear < request.getExpirationDate().getTime()) {
            return ValidationStatus.expirationDateTooFarInFuture;
        }
        if (request.getDueDate().getTime() < request.getExpirationDate().getTime()) {
            return ValidationStatus.dueDateBeforeExpiration;
        }
        if (LocaleValidator.KILOMETER_UNIT.equals(request.getRangeUnit()) && (request.getRange() == null || request.getRange().doubleValue() < RANGE_KM_MIN.doubleValue())) {
            return ValidationStatus.rangeTooSmall;
        }
        if (/* LocaleValidator.MILE_UNIT.equals(wish.getRangeUnit()) && */ (request.getRange() == null || request.getRange().doubleValue() < RANGE_MI_MIN.doubleValue())) {
            return ValidationStatus.rangeTooSmall;
        }
        if (LocaleValidator.MILE_UNIT.equals(request.getRangeUnit()) && request.getRange().doubleValue() > RANGE_MI_MAX.doubleValue()) {
            return ValidationStatus.rangeTooLarge;
        }
        if (/* LocaleValidator.KILOMETER_UNIT.equals(wish.getRangeUnit()) && */ request.getRange().doubleValue() > RANGE_KM_MAX.doubleValue()) {
            return ValidationStatus.rangeTooLarge;
        }
        if (request.getQuantity() == null || request.getQuantity() == 0L) {
            return ValidationStatus.noQuantity;
        }
        Long locationKey = request.getLocationKey();
        if (locationKey == null || locationKey == 0L) {
            return ValidationStatus.noLocationKey;
        }
        try {
            Location location = LocationSteps.getLocation(pm, locationKey);
            if (LocaleValidator.DEFAULT_POSTAL_CODE_CA.equals(location.getPostalCode()) ||
                LocaleValidator.DEFAULT_POSTAL_CODE_US.equals(location.getPostalCode()) ||
                LocaleValidator.DEFAULT_POSTAL_CODE_ALT_US.equals(location.getPostalCode())
            ) {
                return ValidationStatus.invalidLocation;
            }
            if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                location = LocaleValidator.getGeoCoordinates(location);
                if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                    return ValidationStatus.invalidLocation;
                }
                location = BaseSteps.getLocationOperations().updateLocation(pm, location);
            }
            // Save the location key as the latest reference used by the consumer
            if (consumer.getAutomaticLocaleUpdate() && !location.getKey().equals(consumer.getLocationKey())) {
                consumer.setLocationKey(location.getKey());
                BaseSteps.getConsumerOperations().updateConsumer(pm, consumer);
            }
        }
        catch (InvalidIdentifierException ex) {
            return ValidationStatus.invalidLocationKey;
        }
        catch (DataSourceException ex) {
            return ValidationStatus.locationUpdateFailed;
        }

        return ValidationStatus.ok;
    }

    protected static String checkRequestFields(PersistenceManager pm, Consumer consumer, Request request, String messageBaseId) {
        Date nowDate = DateUtils.getNowDate();
        Long nowTime = nowDate.getTime() - 60 * 1000L; // Minus 1 minute
        Locale locale = consumer.getLocale();
        String message = null;

        // getLogger().finest("========================\n now: " + nowTime + "\n exp: " + wish.getExpirationDate() + "\n due: " + wish.getDueDate() + "\n========================");

        String wishRef = LabelExtractor.get("cp_tweet_" + messageBaseId + "_reference_part", new Object[] { request.getKey() }, locale);
        if ((request.getContent().length() == 0) && (request.getHashTags() == null || request.getHashTags().size() == 0)) {
            message = LabelExtractor.get("dv_report_" + messageBaseId + "_without_tag", new Object[] { wishRef }, locale);
        }
        else if (request.getDueDate() == null || request.getDueDate().getTime() < nowTime) {
            getLogger().warning(messageBaseId + ": " + request.getKey() + "\nNow: " + nowDate + "\nDue: " + request.getDueDate());
            message = LabelExtractor.get("dv_report_due_in_past", new Object[] { wishRef }, locale);
        }
        else if (nowTime + oneYear < request.getDueDate().getTime()) {
            getLogger().warning(messageBaseId + ": " + request.getKey() + "\nNow: " + nowDate + "\n+1 year: " + new Date(nowTime + oneYear) + "\nDue: " + request.getDueDate());
            message = LabelExtractor.get("dv_report_due_too_far_in_future", new Object[] { wishRef }, locale);
        }
        else if (request.getExpirationDate() == null || request.getExpirationDate().getTime() < nowTime) {
            getLogger().warning(messageBaseId + ": " + request.getKey() + "\nNow: " + nowDate + "\nExpiration: " + request.getExpirationDate());
            message = LabelExtractor.get("dv_report_expiration_in_past", new Object[] { wishRef }, locale);
        }
        else if (nowTime + oneYear < request.getExpirationDate().getTime()) {
            getLogger().warning(messageBaseId + ": " + request.getKey() + "\nNow: " + nowDate + "\n+1 year: " + new Date(nowTime + oneYear) + "\nExpiration: " + request.getExpirationDate());
            message = LabelExtractor.get("dv_report_expiration_too_far_in_future", new Object[] { wishRef }, locale);
        }
        else if (request.getDueDate().getTime() < request.getExpirationDate().getTime()) {
            message = LabelExtractor.get("dv_report_expiration_before_due_date", new Object[] { wishRef }, locale);
        }
        else if (LocaleValidator.KILOMETER_UNIT.equals(request.getRangeUnit()) && (request.getRange() == null || request.getRange().doubleValue() < RANGE_KM_MIN.doubleValue())) {
            String rangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { request.getRange() == null ? 0.0D : request.getRange(), LocaleValidator.KILOMETER_UNIT }, locale);
            String minRangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { RANGE_KM_MIN, LocaleValidator.KILOMETER_UNIT }, locale);
            message = LabelExtractor.get("dv_report_range_too_small", new Object[] { wishRef, rangeDef, minRangeDef, LocaleValidator.KILOMETER_UNIT }, locale);
        }
        else if (/* LocaleValidator.MILE_UNIT.equals(wish.getRangeUnit()) && */ (request.getRange() == null || request.getRange().doubleValue() < RANGE_MI_MIN.doubleValue())) {
            String rangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { request.getRange() == null ? 0.0D : request.getRange(), LocaleValidator.MILE_UNIT }, locale);
            String minRangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { RANGE_MI_MIN, LocaleValidator.MILE_UNIT }, locale);
            message = LabelExtractor.get("dv_report_range_too_small", new Object[] { wishRef, rangeDef, minRangeDef, LocaleValidator.MILE_UNIT }, locale);
        }
        else if (LocaleValidator.MILE_UNIT.equals(request.getRangeUnit()) && request.getRange().doubleValue() > RANGE_MI_MAX.doubleValue()) {
            String rangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { request.getRange(), LocaleValidator.MILE_UNIT }, locale);
            String maxRangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { RANGE_MI_MAX, LocaleValidator.MILE_UNIT }, locale);
            message = LabelExtractor.get("dv_report_range_too_big", new Object[] { wishRef, rangeDef, maxRangeDef, LocaleValidator.MILE_UNIT }, locale);
        }
        else if (/* LocaleValidator.KILOMETER_UNIT.equals(wish.getRangeUnit()) && */ request.getRange().doubleValue() > RANGE_KM_MAX.doubleValue()) {
            String rangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { request.getRange(), LocaleValidator.KILOMETER_UNIT }, locale);
            String maxRangeDef = LabelExtractor.get("cp_tweet_range_part", new Object[] { RANGE_KM_MAX, LocaleValidator.KILOMETER_UNIT }, locale);
            message = LabelExtractor.get("dv_report_range_too_big", new Object[] { wishRef, rangeDef, maxRangeDef, LocaleValidator.KILOMETER_UNIT }, locale);
        }
        else if (request.getQuantity() == null || request.getQuantity() == 0L) {
            message = LabelExtractor.get("dv_report_quantity_zero", new Object[] { wishRef }, locale);
        }
        else {
            Long locationKey = request.getLocationKey();
            if (locationKey == null || locationKey == 0L) {
                message = LabelExtractor.get("dv_report_missing_locale", new Object[] { wishRef }, locale);
            }
            else {
                try {
                    Location location = LocationSteps.getLocation(pm, locationKey);
                    if (LocaleValidator.DEFAULT_POSTAL_CODE_CA.equals(location.getPostalCode()) ||
                        LocaleValidator.DEFAULT_POSTAL_CODE_US.equals(location.getPostalCode()) ||
                        LocaleValidator.DEFAULT_POSTAL_CODE_ALT_US.equals(location.getPostalCode())
                    ) {
                        message = LabelExtractor.get("dv_report_invalid_locale", new Object[] { wishRef, location.getPostalCode(), location.getCountryCode() }, locale);
                    }
                    else if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                        location = LocaleValidator.getGeoCoordinates(location);
                        if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                            // TODO: re-enable the message generation when we're sure the coordinates are really wrong, not just unavailable from Google Maps
                            // message = LabelExtractor.get("dv_report_invalid_locale", new Object[] { wishRef, location.getPostalCode(), location.getCountryCode() }, locale);
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
                    message = LabelExtractor.get("dv_report_unable_to_get_locale_information", new Object[] { wishRef }, locale);
                }
                catch (DataSourceException ex) {
                    message = LabelExtractor.get("dv_report_unable_to_get_locale_information", new Object[] { wishRef }, locale);
                }
           }
        }

        return message;
    }

    // Temporary method filtering out non #demo tags
    protected static void filterHashTags(PersistenceManager pm, Consumer consumer, Command command, String messageBaseId) throws ClientException, DataSourceException {
        if (command.getHashTags() != null) {
            List<String> hashTags = command.getHashTags();
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
                    String wishRef = LabelExtractor.get("cp_tweet_" + messageBaseId + "_reference_part", new Object[] { command.getKey() }, locale);
                    String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { serializedHashTags.trim() }, locale);
                    communicateToConsumer(
                            command.getSource(), // TODO: maybe pass the initial RawCommand to be able to reuse the subject
                            "To be fixed!", // FIXME
                            consumer,
                            new String[] { LabelExtractor.get("dv_report_hashtag_warning", new Object[] { wishRef, tags }, locale) }
                    );
                    for (String tag: serializedHashTags.split(" ")) {
                        command.removeHashTag(tag);
                    }
                }
            }
        }
    }
}
