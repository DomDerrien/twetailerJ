package twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Wish extends Entity {

    /*** Command ***/

    @Persistent
    private Action action;

    @Persistent
    private String hastTag;

    @Persistent
    private Long ownerKey;

    @Persistent
    private Source source;

    @Persistent
    private State state = State.opened;

    /*** Wish ***/

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

    /*** Command ***/

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'action'");
        }
        this.action = action;
    }

    public void setAction(String action) {
        setAction(Action.valueOf(action));
    }

    public String getHastTag() {
        return hastTag;
    }

    public void setHastTag(String hastTag) {
        this.hastTag = hastTag;
    }

    public Long getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(Long ownerKey) {
        this.ownerKey = ownerKey;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'source'");
        }
        this.source = source;
    }

    public void setSource(String source) {
        setSource(Source.valueOf(source));
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        if (state == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'state'");
        }
        this.state = state;
    }

    public void setState(String state) {
        setState(State.valueOf(state));
    }

    /*** Demand ***/

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
        /*** Command ***/
        out.put(Command.ACTION, getAction().toString());
        out.put(Command.HASH_TAG, getHastTag());
        if (getOwnerKey() != null) { out.put(Command.OWNER_KEY, getOwnerKey()); }
        out.put(Command.SOURCE, getSource().toString());
        out.put(Command.STATE, getState().toString());
        /*** Demand ***/
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
        /*** Command ***/
        if (in.containsKey(Command.ACTION)) { setAction(in.getString(Command.ACTION)); }
        if (in.containsKey(Command.HASH_TAG)) { setHastTag(in.getString(Command.HASH_TAG)); }
        if (in.containsKey(Command.OWNER_KEY)) { setOwnerKey(in.getLong(Command.OWNER_KEY)); }
        if (in.containsKey(Command.SOURCE)) { setSource(in.getString(Command.SOURCE)); }
        if (in.containsKey(Command.STATE)) { setState(in.getString(Command.STATE)); }
        /*** Wish ***/
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
