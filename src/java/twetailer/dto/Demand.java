package twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a consumer request
 *
 * @see twetailer.dto.Command
 * @see twetailer.dto.Consumer
 * @see twetailer.dto.Payment
 * @see twetailer.dto.Proposal
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Demand extends Command {

    @Persistent
    private Date expirationDate;

    public static final String EXPIRATION_DATE = "expirationDate";

    @Persistent
    private Long influencerKey;

    public final static String INFLUENCER_KEY = Influencer.INFLUENCER_KEY;

    @Persistent
    private List<Long> proposalKeys = new ArrayList<Long>();

    public static final String PROPOSAL_KEYS = "proposalKeys";

    @Persistent
    private Double range = LocaleValidator.DEFAULT_RANGE;

    public static final String RANGE = "range";

    @Persistent
    private String rangeUnit = LocaleValidator.DEFAULT_RANGE_UNIT;

    public static final String RANGE_UNIT = "rangeUnit";

    // Shortcut
    public static final String REFERENCE = "reference";

    @Persistent
    private List<Long> saleAssociateKeys = new ArrayList<Long>();

    public static final String SALE_ASSOCIATE_KEYS = "saleAssociateKeys";

    /** Default constructor */
    public Demand() {
        super();
        setAction(Action.demand);
        setDefaultExpirationDate();
        setDueDate(getExpirationDate());
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
     *
     * @return Object instance for chaining
     */
    protected Demand resetLists() {
        super.resetLists();
        proposalKeys = null;
        saleAssociateKeys = null;
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

    public Long getInfluencerKey() {
        return influencerKey;
    }

    public void setInfluencerKey(Long influencerKey) {
        this.influencerKey = influencerKey;
    }

    public List<Long> getProposalKeys() {
        return proposalKeys;
    }

    public String getSerializedProposalKeys(String defaultLabel) {
        if (getProposalKeys() == null || getProposalKeys().size() == 0) {
            return defaultLabel;
        }
        return getSerializedProposalKeys();
    }

    public String getSerializedProposalKeys() {
        return getSerializedTags(proposalKeys);
    }

    public void setProposalKeys(List<Long> proposalKeys) {
        if (proposalKeys == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'proposalKeys' of type List<Long>");
        }
        updateModificationDate(); // To highlight the demand update
        this.proposalKeys = proposalKeys;
    }

    public void addProposalKey(Long proposalKey) {
        if (proposalKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'proposalKeys' of type List<Long>");
        }
        if (proposalKeys == null) {
            proposalKeys = new ArrayList<Long>();
        }
        if (!proposalKeys.contains(proposalKey)) {
            updateModificationDate(); // To highlight the demand update
            proposalKeys.add(proposalKey);
        }
    }

    public Demand resetProposalKeys() {
        if (proposalKeys == null) {
            return this;
        }
        updateModificationDate(); // To highlight the demand update
        proposalKeys = new ArrayList<Long>();
        return this;
    }

    public void removeProposalKey(Long proposalKey) {
        if (proposalKeys == null) {
            return;
        }
        if (proposalKeys.contains(proposalKey)) {
            updateModificationDate(); // To highlight the demand update
            proposalKeys.remove(proposalKey);
        }
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

    public List<Long> getSaleAssociateKeys() {
        return saleAssociateKeys;
    }

    public void setSaleAssociateKeys(List<Long> saleAssociateKeys) {
        if (saleAssociateKeys == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'saleAssociateKeys' of type List<Long>");
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

    public Demand resetSaleAssociateKeys() {
        if (saleAssociateKeys == null) {
            return this;
        }
        saleAssociateKeys = new ArrayList<Long>();
        return this;
    }

    public void removeSaleAssociateKey(Long saleAssociateKey) {
        if (saleAssociateKeys == null) {
            return;
        }
        saleAssociateKeys.remove(saleAssociateKey);
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(EXPIRATION_DATE, DateUtils.dateToISO(getExpirationDate()));
        if (getInfluencerKey() != null) {
            out.put(INFLUENCER_KEY, getInfluencerKey());
        }
        if (getProposalKeys() != null && 0 < getProposalKeys().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(Long key: getProposalKeys()) {
                jsonArray.add(key);
            }
            out.put(PROPOSAL_KEYS, jsonArray);
        }
        out.put(RANGE, getRange());
        out.put(RANGE_UNIT, getRangeUnit());
        if (getSaleAssociateKeys() != null && 0 < getSaleAssociateKeys().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(Long key: getSaleAssociateKeys()) {
                jsonArray.add(key);
            }
            out.put(SALE_ASSOCIATE_KEYS, jsonArray);
        }
        return out;
    }

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
        if (in.containsKey(PROPOSAL_KEYS)) {
            resetProposalKeys();
            JsonArray jsonArray = in.getJsonArray(PROPOSAL_KEYS);
            for (int i=0; i<jsonArray.size(); ++i) {
                addProposalKey(jsonArray.getLong(i));
            }
        }
        if (in.containsKey(RANGE)) { setRange(in.getDouble(RANGE)); }
        if (in.containsKey(RANGE_UNIT)) { setRangeUnit(in.getString(RANGE_UNIT)); }
        if (in.containsKey(SALE_ASSOCIATE_KEYS)) {
            resetSaleAssociateKeys();
            JsonArray jsonArray = in.getJsonArray(SALE_ASSOCIATE_KEYS);
            for (int i=0; i<jsonArray.size(); ++i) {
                addSaleAssociateKey(jsonArray.getLong(i));
            }
        }

        // Shortcut
        if (in.containsKey(REFERENCE)) { setKey(in.getLong(REFERENCE)); }

        return this;
    }
}
