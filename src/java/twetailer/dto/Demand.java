package twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import twetailer.validator.LocaleValidator;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Demand extends Entity {

    /*** Command ***/

    @Persistent
    private Action action;

    public static final String ACTION = "action";

    @Persistent
    private Long consumerKey;

    public static final String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private Long rawCommandId;

    public static final String RAW_COMMAND_ID = "rawCommandId";

    @Persistent
    private Source source;

    public static final String SOURCE = "source";

    @Persistent
    private State state = State.open;

    public static final String STATE = "state";

    /*** Demand ***/

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public static final String CRITERIA = "criteria";

    @Persistent
    private Date expirationDate;

    public static final String EXPIRATION_DATE = "expirationDate";

    @Persistent
    private Long locationKey;

    public final static String LOCATION_KEY = Location.LOCATION_KEY;

    @Persistent
    private List<Long> proposalKeys = new ArrayList<Long>();

    public static final String PROPOSAL_KEYS = "proposalKeys";

    @Persistent
    private Long quantity = 1L;

    public static final String QUANTITY = "quantity";

    @Persistent
    private Double range = 25.0D;

    public static final String RANGE = "range";

    // Shortcut
    public static final String REFERENCE = "reference";

    @Persistent
    private String rangeUnit = LocaleValidator.KILOMETER_UNIT;

    public static final String RANGE_UNIT = "rangeUnit";

    /** Default constructor */
    public Demand() {
        super();
        setAction(Action.demand);
        setDefaultExpirationDate();
    }

    /**
     * Creates a demand
     *
     * @param in HTTP request parameters
     */
    public Demand(JsonObject in) {
        this();
        fromJson(in);
    }

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     */
    protected void resetLists() {
        criteria = null;
        proposalKeys = null;
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

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerId) {
        this.consumerKey = consumerId;
    }

    public Long getRawCommandId() {
        return rawCommandId;
    }

    public void setRawCommandId(Long rawCommandId) {
        this.rawCommandId = rawCommandId;
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

    public Long getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(Long locationKey) {
        this.locationKey = locationKey;
    }

    public List<Long> getProposalKeys() {
        return proposalKeys;
    }

    public void setProposalKeys(List<Long> proposalKeys) {
        if (proposalKeys == null) {
            throw new IllegalArgumentException("Cannot nuulify the attribute 'proposalKeys' of type List<Long>");
        }
        this.proposalKeys = proposalKeys;
    }

    public void addProposalKey(Long proposalKey) {
        if (proposalKeys == null) {
            proposalKeys = new ArrayList<Long>();
        }
        if (!proposalKeys.contains(proposalKey)) {
            proposalKeys.add(proposalKey);
        }
    }

    public void resetProposalKeys() {
        if (proposalKeys == null) {
            return;
        }
        proposalKeys = new ArrayList<Long>();
    }

    public void removeProposalKey(Long proposalKey) {
        if (proposalKeys == null) {
            return;
        }
        proposalKeys.remove(proposalKey);
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public String getRangeUnit() {
        return rangeUnit;
    }

    public void setRangeUnit(String rangeUnit) {
        if (LocaleValidator.MILE_UNIT.equalsIgnoreCase(rangeUnit)) {
            this.rangeUnit = LocaleValidator.MILE_UNIT;
        }
        else {
            this.rangeUnit = LocaleValidator.KILOMETER_UNIT;
        }
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        /*** Command ***/
        out.put(ACTION, getAction().toString());
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        if (getRawCommandId() != null) { out.put(RAW_COMMAND_ID, getRawCommandId()); }
        out.put(SOURCE, getSource().toString());
        out.put(STATE, getState().toString());
        /*** Demand ***/
        if (getCriteria() != null && 0 < getCriteria().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
            out.put(CRITERIA, jsonArray);
        }
        out.put(EXPIRATION_DATE, DateUtils.dateToISO(getExpirationDate()));
        if (getLocationKey() != null) { out.put(LOCATION_KEY, getLocationKey()); }
        if (getProposalKeys() != null && 0 < getProposalKeys().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(Long key: getProposalKeys()) {
                jsonArray.add(key);
            }
            out.put(PROPOSAL_KEYS, jsonArray);
        }
        out.put(QUANTITY, getQuantity());
        out.put(RANGE, getRange());
        out.put(RANGE_UNIT, getRangeUnit());
        out.put(REFERENCE, getKey());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        /*** Command ***/
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(RAW_COMMAND_ID)) { setRawCommandId(in.getLong(RAW_COMMAND_ID)); }
        if (in.containsKey(SOURCE)) { setSource(in.getString(SOURCE)); }
        if (in.containsKey(STATE)) { setState(in.getString(STATE)); }
        /*** Demand ***/
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
                setExpirationDate(DEFAULT_EXPIRATION_DELAY); // Default to an expiration 30 days in the future
            }
        }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(PROPOSAL_KEYS)) {
            resetProposalKeys();
            JsonArray jsonArray = in.getJsonArray(PROPOSAL_KEYS);
            for (int i=0; i<jsonArray.size(); ++i) {
                addProposalKey(jsonArray.getLong(i));
            }
        }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }
        if (in.containsKey(RANGE)) { setRange(in.getDouble(RANGE)); }
        if (in.containsKey(RANGE_UNIT)) { setRangeUnit(in.getString(RANGE_UNIT)); }

        // Shortcut
        if (in.containsKey(REFERENCE)) { setKey(in.getLong(REFERENCE)); }

        return this;
    }
}
