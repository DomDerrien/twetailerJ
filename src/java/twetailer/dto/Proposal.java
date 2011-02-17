package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;

import com.google.appengine.api.datastore.Text;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a sale associate proposal
 *
 * @see twetailer.dto.Command
 * @see twetailer.dto.Demamd
 * @see twetailer.dto.Payment
 * @see twetailer.dto.SaleAssociate
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Proposal extends Command {

    private Text AWSCBUIURL;

    public static final String AWSCBUIURL_KEY = "AWSCBUIURL";

    @Persistent
    private Long consumerKey;

    public static final String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private String comment;

    public static final String COMMENT = "comment";

    @Persistent
    private String currencyCode = LocaleValidator.DEFAULT_CURRENCY_CODE;

    public static final String CURRENCY_CODE = "currencyCode";

    @Persistent
    private Long demandKey;

    public static final String DEMAND_KEY = Demand.DEMAND_KEY;

    // Shortcut
    public static final String PROPOSAL_KEY = "proposalKey";

    @Persistent
    private Double price = 0.0D;

    public static final String PRICE = "price";

    @Persistent
    private Long score;

    public static final String SCORE = "score";

    @Persistent
    private Long storeKey;

    public static final String STORE_KEY = Store.STORE_KEY;

    @Persistent
    private Double total = 0.0D;

    public static final String TOTAL = "total";

    /** Default constructor */
    public Proposal() {
        super();
        setAction(Action.propose);
    }

    /**
     * Creates a proposal
     *
     * @param in HTTP request parameters
     */
    public Proposal(JsonObject in) {
        this();
        fromJson(in);
    }

    /**
     * Provided to reproduce the JDO behavior with Unit tests     *
     * @return Object instance for chaining

     */
    protected Proposal resetLists() {
        super.resetLists();
        return this;
    }

    public String getAWSCBUIURL() {
        if (AWSCBUIURL == null) {
            return null;
        }
        return AWSCBUIURL.getValue();
    }

    public void setAWSCBUIURL(String aWSCBUIURL) {
        AWSCBUIURL = aWSCBUIURL == null || aWSCBUIURL.length() == 0 ? null : new Text(aWSCBUIURL);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerKey) {
        if (consumerKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'consumerKey'");
        }
        this.consumerKey = consumerKey;
    }

    /**
     * Returns the ISO 4217 currency code of the currency for this proposal
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        if (currencyCode == null) {
            currencyCode = LocaleValidator.DEFAULT_CURRENCY_CODE;
        }
        this.currencyCode = currencyCode;
    }

    public Long getDemandKey() {
        return demandKey;
    }

    public void setDemandKey(Long demandKey) {
        if (demandKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'demandKey'");
        }
        this.demandKey = demandKey;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getScore() {
        if (score == null) {
            return 0L;
        }
        return score;
    }

    public void setScore(Long score) {
        this.score = score != null && (score < 1L || 5L < score) ? null : score;
    }

    public Long getStoreKey() {
        return storeKey;
    }

    public void setStoreKey(Long storeKey) {
        if (storeKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'storeKey'");
        }
        this.storeKey = storeKey;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (AWSCBUIURL != null) {
            out.put(AWSCBUIURL_KEY, getAWSCBUIURL());
        }
        if (comment != null) {
            out.put(COMMENT, getComment());
        }
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        out.put(CURRENCY_CODE, getCurrencyCode());
        if (getDemandKey() != null) { out.put(DEMAND_KEY, getDemandKey()); }
        out.put(PRICE, getPrice());
        if (getStoreKey() != null) { out.put(STORE_KEY, getStoreKey()); }
        out.put(SCORE, getScore());
        out.put(TOTAL, getTotal());
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        return fromJson(in, false, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin, boolean isCacheRelated) {
        if (isCacheRelated) { isUserAdmin = isCacheRelated; }
        super.fromJson(in, isUserAdmin, isCacheRelated);

        if (isCacheRelated && in.containsKey(AWSCBUIURL_KEY)) { setAWSCBUIURL(in.getString(AWSCBUIURL_KEY)); }
        if (isUserAdmin && in.containsKey(COMMENT)) { setComment(in.getString(COMMENT)); } // Set by the system from the Consumer !rate action
        if ((getKey() == null || isUserAdmin) && in.containsKey(CONSUMER_KEY)) {
            setConsumerKey(in.getLong(CONSUMER_KEY)); // Can only be set at creation time
        }
        if (in.containsKey(CURRENCY_CODE)) { setCurrencyCode(in.getString(CURRENCY_CODE)); }
        if ((getKey() == null || isUserAdmin) && in.containsKey(DEMAND_KEY)) {
            setDemandKey(in.getLong(DEMAND_KEY)); // Can only be set at creation time
        }
        if (in.containsKey(PRICE)) { setPrice(in.getDouble(PRICE)); }
        if (isUserAdmin && in.containsKey(SCORE)) { setScore(in.getLong(SCORE)); } // Set by the system from the Consumer !rate action
        if (isUserAdmin && in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); } // Set by the system from the SaleAssociate own storeKey
        if (in.containsKey(TOTAL)) { setTotal(in.getDouble(TOTAL)); }

        // Shortcut
        if (in.containsKey(PROPOSAL_KEY)) { setKey(in.getLong(PROPOSAL_KEY)); }
        if (getKey() == null && in.containsKey(DEMAND_KEY)) {
            setDemandKey(in.getLong(DEMAND_KEY)); // Shortcut; can only be set at creation time
        }

        return this;
    }
}
