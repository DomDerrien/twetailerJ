package twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")

public class Demand extends Command {

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public static final String CRITERIA = "criteria";
    public static final String CRITERIA_ADD = "\\+criteria";
    public static final String CRITERIA_REMOVE = "\\-criteria";

    @Persistent
    private Date expirationDate;

    public static final String EXPIRATION_DATE = "expirationDate";

    @Persistent
    private List<Long> proposalKeys = new ArrayList<Long>();

    public static final String PROPOSAL_KEYS = "proposalKeys";

    @Persistent
    private Long quantity = 1L;

    public static final String QUANTITY = "quantity";

    @Persistent
    private Double range = LocaleValidator.DEFAULT_RANGE;

    public static final String RANGE = "range";

    // Shortcut
    public static final String REFERENCE = "reference";

    @Persistent
    private String rangeUnit = LocaleValidator.DEFAULT_RANGE_UNIT;

    public static final String RANGE_UNIT = "rangeUnit";

    @Persistent
    private List<Long> saleAssociateKeys = new ArrayList<Long>();

    public static final String SALE_ASSOCIATE_KEYS = "saleAssociateKeys";

    @Persistent
    private Boolean stateCmdList = Boolean.TRUE;

    public static final String STATE_COMMAND_LIST = "stateCmdList";

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
        saleAssociateKeys = null;
    }

    public String getSerializedCriteria() {
        return getSerializedTags(criteria);
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
        /** Relaxed validation because user's can give invalid dates up-front!
        if (expirationDate.getTime() < DateUtils.getNowDate().getTime()) {
            throw new IllegalArgumentException("Expiration date cannot be in the past");
        }
        */
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
        this.rangeUnit = LocaleValidator.checkRangeUnit(rangeUnit);
    }

    public List<Long> getSaleAssociateKeys() {
        return saleAssociateKeys;
    }

    public void setSaleAssociateKeys(List<Long> saleAssociateKeys) {
        if (saleAssociateKeys == null) {
            throw new IllegalArgumentException("Cannot nuulify the attribute 'saleAssociateKeys' of type List<Long>");
        }
        this.saleAssociateKeys = saleAssociateKeys;
    }

    public void addSaleAssociateKey(Long saleAssociateKey) {
        if (saleAssociateKeys == null) {
            saleAssociateKeys = new ArrayList<Long>();
        }
        if (!saleAssociateKeys.contains(saleAssociateKey)) {
            saleAssociateKeys.add(saleAssociateKey);
        }
    }

    public void resetSaleAssociateKeys() {
        if (saleAssociateKeys == null) {
            return;
        }
        saleAssociateKeys = new ArrayList<Long>();
    }

    public void removeSaleAssociateKey(Long saleAssociateKey) {
        if (saleAssociateKeys == null) {
            return;
        }
        saleAssociateKeys.remove(saleAssociateKey);
    }

    @Override
    public void setState(State state) {
        super.setState(state);
        stateCmdList =
            !State.cancelled.equals(state) &&
            !State.closed.equals(state) &&
            !State.declined.equals(state) &&
            !State.markedForDeletion.equals(state);
    }

    public Boolean getStateCmdList() {
        return stateCmdList == null ? Boolean.TRUE : stateCmdList;
    }

    protected void setStateCmdList(Boolean  stateCmdList) {
        this.stateCmdList = stateCmdList;
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
        if (getSaleAssociateKeys() != null && 0 < getSaleAssociateKeys().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(Long key: getSaleAssociateKeys()) {
                jsonArray.add(key);
            }
            out.put(SALE_ASSOCIATE_KEYS, jsonArray);
        }
        out.put(STATE_COMMAND_LIST, getStateCmdList());
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
        removeDuplicates(in, CRITERIA_ADD, CRITERIA_REMOVE);
        if (in.containsKey(CRITERIA_REMOVE)) {
            JsonArray jsonArray = in.getJsonArray(CRITERIA_REMOVE);
            for (int i=0; i<jsonArray.size(); ++i) {
                removeCriterion(jsonArray.getString(i));
            }
        }
        if (in.containsKey(CRITERIA_ADD)) {
            JsonArray jsonArray = in.getJsonArray(CRITERIA_ADD);
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
        if (in.containsKey(SALE_ASSOCIATE_KEYS)) {
            resetSaleAssociateKeys();
            JsonArray jsonArray = in.getJsonArray(SALE_ASSOCIATE_KEYS);
            for (int i=0; i<jsonArray.size(); ++i) {
                addSaleAssociateKey(jsonArray.getLong(i));
            }
        }
        if (in.containsKey(STATE_COMMAND_LIST)) { in.getBoolean(STATE_COMMAND_LIST); }

        // Shortcut
        if (in.containsKey(REFERENCE)) { setKey(in.getLong(REFERENCE)); }

        return this;
    }

    protected static void removeDuplicates(JsonObject in, String addLabel, String removeLabel) {
        if (in.containsKey(addLabel) && in.containsKey(removeLabel)) {
            JsonArray inAdd = in.getJsonArray(addLabel);
            JsonArray inRemove = in.getJsonArray(removeLabel);
            int idx = inRemove.size();
            while (0 < idx) {
                --idx;
                String tag = inRemove.getString(idx);
                if (inAdd.getList().contains(tag)) {
                    inAdd.remove(tag);
                    inRemove.remove(tag);
                }
            }
        }
    }
}
