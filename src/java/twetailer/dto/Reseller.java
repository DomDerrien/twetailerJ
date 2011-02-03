package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a Twetailer reseller which takes care of the
 * retailer registration, training, support, etc., with the system
 *
 * @see twetailer.dto.Entity
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Reseller extends Entity {

    @Persistent
    private Long consumerKey;

    public final static String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private Long tokenNb;

    public final static String TOKEN_NB = "tokenNb";

    /** Default constructor */
    public Reseller() {
        super();
    }

    /**
     * Creates an Reseller
     *
     * @param in HTTP request parameters
     */
    public Reseller(JsonObject in) {
        super();
        fromJson(in);
    }

     public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerKey) {
        this.consumerKey = consumerKey;
    }

    public Long getTokenNb() {
        return tokenNb;
    }

    public void setTokenNb(Long tokenNb) {
        this.tokenNb = tokenNb;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        if (getTokenNb() != null) { out.put(TOKEN_NB, getTokenNb()); }
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        return fromJson(in, false, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin, boolean isCacheRelated) {
        if (isCacheRelated) { isUserAdmin = isCacheRelated; }
        if (!isUserAdmin) {
            throw new IllegalArgumentException("Reserved operation");
        }

        super.fromJson(in, isUserAdmin, isCacheRelated);

        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(TOKEN_NB)) { setTokenNb(in.getLong(TOKEN_NB)); }

        return this;
    }
}
