package twetailer.dto;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a consumer request,
 * which is really a wish or a demand
 *
 * @see twetailer.dto.Command
 * @see twetailer.dto.Consumer
 * @see twetailer.dto.Wish
 * @see twetailer.dto.Demand
 * @see twetailer.dto.Payment
 * @see twetailer.dto.Proposal
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public class Request extends Command {

    @Persistent
    private Date expirationDate;

    public static final String EXPIRATION_DATE = "expirationDate";

    @Persistent
    private Long influencerKey;

    public final static String INFLUENCER_KEY = Influencer.INFLUENCER_KEY;

    @Persistent
    private Double range = LocaleValidator.DEFAULT_RANGE;

    public static final String RANGE = "range";

    @Persistent
    private String rangeUnit = LocaleValidator.DEFAULT_RANGE_UNIT;

    public static final String RANGE_UNIT = "rangeUnit";

    // Shortcut
    public static final String REFERENCE = "reference";

    /** Default constructor */
    public Request() {
        super();
        setAction(Action.demand);
        setDefaultExpirationDate();
        setDueDate(getExpirationDate());
    }

    /**
     * Creates a wish
     *
     * @param in HTTP request parameters
     */
    public Request(JsonObject in) {
        this();
        fromJson(in);
    }

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     *
     * @return Object instance for chaining
     */
    protected Request resetLists() {
        super.resetLists();
        return this;
    }

    @Override
    public Date getDueDate() {
        Date dueDate = super.getDueDate();
        if (dueDate == null) {
            dueDate = getExpirationDate();
        }
        return dueDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        if (expirationDate == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'expirationDate' of type Date reference");
        }
        /** Relaxed validation because user's can give invalid dates up-front!
        if (expirationDate.getTime() < DateUtils.getNowDate().getTime()) {
            throw new IllegalArgumentException("Expiration date cannot be in the past");
        }
        */
        this.expirationDate = expirationDate;
    }

    /**
     * Delay for the default expiration of a wish
     */
    public final static int DEFAULT_EXPIRATION_DELAY = 30; // In 1 month

    /**
     * Push the expiration to a defined default in the future
     */
    public void setDefaultExpirationDate() {
        setExpirationDate(DEFAULT_EXPIRATION_DELAY);
    }

    public void setExpirationDate(int delayInDays) {
        Calendar limit = DateUtils.getNowCalendar();
        limit.set(Calendar.DAY_OF_MONTH, limit.get(Calendar.DAY_OF_MONTH) + delayInDays);
        this.expirationDate = limit.getTime();
    }

    public Long getInfluencerKey() {
        return influencerKey;
    }

    public void setInfluencerKey(Long influencerKey) {
        this.influencerKey = influencerKey;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        if (range == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'range'");
        }
        this.range = range;
    }

    public String getRangeUnit() {
        return rangeUnit;
    }

    public void setRangeUnit(String rangeUnit) {
        this.rangeUnit = LocaleValidator.checkRangeUnit(rangeUnit);
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(EXPIRATION_DATE, DateUtils.dateToISO(getExpirationDate()));
        if (getInfluencerKey() != null) {
            out.put(INFLUENCER_KEY, getInfluencerKey());
        }
        out.put(RANGE, getRange());
        out.put(RANGE_UNIT, getRangeUnit());
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(EXPIRATION_DATE)) {
            try {
                Date expirationDate = DateUtils.isoToDate(in.getString(EXPIRATION_DATE));
                setExpirationDate(expirationDate);
                if (!in.containsKey(KEY) && !in.containsKey(DUE_DATE)) {
                    // To push the given expirationDate as the default dueDate if the exchange is about a demand to be created
                    setDueDate(getExpirationDate());
                }
            }
            catch (ParseException ex) {
                Logger.getLogger(Demand.class.getName()).warning("Invalid format in expiration date: " + in.getString(EXPIRATION_DATE) + ", for command.key=" + getKey() + " -- message: " + ex.getMessage());
                setExpirationDate(DEFAULT_EXPIRATION_DELAY); // Default to an expiration 30 days in the future
            }
        }
        else if (getKey() == null) { //  && getDueDate() != null) { // getDueDate() cannot be null as it falls back on getExpirationDate() which cannot be nullified
            // To push the given dueDate as the default expirationDate if the exchange is about a demand to be created
            setExpirationDate(getDueDate());
        }
        // if (in.containsKey(INFLUENCER_KEY)) { setInfluencerKey(in.getLong(INFLUENCER_KEY)); } // Cannot be changed transparently
        if (in.containsKey(RANGE)) { setRange(in.getDouble(RANGE)); }
        if (in.containsKey(RANGE_UNIT)) { setRangeUnit(in.getString(RANGE_UNIT)); }

        // Shortcut
        if (in.containsKey(REFERENCE)) { setKey(in.getLong(REFERENCE)); }

        return this;
    }
}
