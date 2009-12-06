package twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Wish extends Command {

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public static final String CRITERIA = "criteria";

    @Persistent
    private Date expirationDate;

    public static final String EXPIRATION_DATE = "expirationDate";

    @Persistent
    private Long quantity = 1L;

    public static final String QUANTITY = "quantity";

    /** Default constructor */
    public Wish() {
        super();
        setAction(Action.wish);
        setDefaultExpirationDate();
    }

    /**
     * Creates a demand
     *
     * @param in HTTP request parameters
     */
    public Wish(JsonObject parameters) {
        this();
        fromJson(parameters);
    }

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     */
    protected void resetLists() {
        criteria = null;
    }

    public List<String> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<String> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'criteria' of type List<String>");
        }
        this.criteria = criteria;
    }

    public void addCriterion(String criterion) {
        if (criteria == null) {
            criteria = new ArrayList<String>();
        }
        if (!criteria.contains(criterion)) {
            criteria.add(criterion);
        }
    }

    public void resetCriteria() {
        if (criteria == null) {
            return;
        }
        criteria = new ArrayList<String>();
    }

    public void removeCriterion(String criterion) {
        if (criteria == null) {
            return;
        }
        criteria.remove(criterion);
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        if (expirationDate == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'expirationDate' of type Date reference");
        }
        if (expirationDate.getTime() < DateUtils.getNowDate().getTime()) {
            throw new IllegalArgumentException("Expiration date cannot be in the past");
        }
        this.expirationDate = expirationDate;
    }

    /**
     * Delay for the default expiration of a demand
     */
    public final static int DEFAULT_EXPIRATION_DELAY = 183; // In six months

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

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getCriteria() != null && 0 < getCriteria().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
            out.put(CRITERIA, jsonArray);
        }
        out.put(EXPIRATION_DATE, DateUtils.dateToISO(getExpirationDate()));
        out.put(QUANTITY, getQuantity());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(CRITERIA)) {
            JsonArray jsonArray = in.getJsonArray(CRITERIA);
            resetCriteria();
            for (int i=0; i<jsonArray.size(); ++i) {
                addCriterion(jsonArray.getString(i));
            }
        }
        if (in.containsKey(EXPIRATION_DATE)) {
            try {
                Date expirationDate = DateUtils.isoToDate(in.getString(EXPIRATION_DATE));
                setExpirationDate(expirationDate);
            }
            catch (ParseException e) {
                setExpirationDate(DEFAULT_EXPIRATION_DELAY); // Default to an expiration 6 months in the future
            }
        }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }

        return this;
    }
}
