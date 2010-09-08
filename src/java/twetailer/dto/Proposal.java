package twetailer.dto;

import java.text.DecimalFormatSymbols;

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

    public static final String CONSUMER_KEY = "consumerKey";

    @Persistent
    private String currencyCode = DecimalFormatSymbols.getInstance(LocaleValidator.DEFAULT_LOCALE).getCurrencySymbol();

    public static final String CURRENCY_CODE = "currencyCode";

    @Persistent
    private Long demandKey;

    public static final String DEMAND_KEY = "demandKey";

    public static final String DEMAND_REFERENCE = Demand.REFERENCE;

    // Shortcut
    public static final String PROPOSAL_KEY = "proposal";

    @Persistent
    private Double price = 0.0D;

    public static final String PRICE = "price";

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

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerKey) {
        if (consumerKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'consumerKey'");
        }
        this.consumerKey = consumerKey;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
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

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (AWSCBUIURL != null) {
            out.put(AWSCBUIURL_KEY, getAWSCBUIURL());
        }
        out.put(CURRENCY_CODE, getCurrencyCode());
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        if (getDemandKey() != null) { out.put(DEMAND_KEY, getDemandKey()); }
        out.put(PRICE, getPrice());
        if (getStoreKey() != null) { out.put(STORE_KEY, getStoreKey()); }
        out.put(TOTAL, getTotal());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(AWSCBUIURL_KEY)) { setAWSCBUIURL(in.getString(AWSCBUIURL_KEY)); }
        if (in.containsKey(CURRENCY_CODE)) { setCurrencyCode(in.getString(CURRENCY_CODE)); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (getKey() == null && in.containsKey(DEMAND_KEY)) {
            setDemandKey(in.getLong(DEMAND_KEY)); // Can only be set at creation time
        }
        if (in.containsKey(PRICE)) { setPrice(in.getDouble(PRICE)); }
        // if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); } // Set by the system from the SaleAssociate own storeKey
        if (in.containsKey(TOTAL)) { setTotal(in.getDouble(TOTAL)); }

        // Shortcut
        if (in.containsKey(PROPOSAL_KEY)) { setKey(in.getLong(PROPOSAL_KEY)); }
        if (getKey() == null && in.containsKey(DEMAND_REFERENCE)) {
            setDemandKey(in.getLong(DEMAND_REFERENCE)); // Shortcut; can only be set at creation time
        }

        return this;
    }
}
