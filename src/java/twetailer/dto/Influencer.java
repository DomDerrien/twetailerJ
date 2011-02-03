package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a Twetailer influencer which host a widget or package an application
 * which post consumer's demands to the Twetailer engine on appspot.com
 *
 * @see twetailer.dto.Entity
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Influencer extends Entity {

    @Persistent
    private Long consumerKey;

    public final static String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private String email;

    public final static String EMAIL = Consumer.EMAIL;

    // Shortcut
    public static final String INFLUENCER_KEY = "influencerKey";

    @Persistent
    private String name;

    public final static String NAME = Store.NAME;

    @Persistent
    private String referralId;

    public final static String REFERRAL_ID = "referralId";
    public final static String DEFAULT_REFERRAL_ID = "0";

    @Persistent
    private String url;

    public final static String URL = Store.URL;

    /** Default constructor */
    public Influencer() {
        super();
    }

    /**
     * Creates an influencer
     *
     * @param in HTTP request parameters
     */
    public Influencer(JsonObject in) {
        super();
        fromJson(in);
    }

     public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerKey) {
        this.consumerKey = consumerKey;
    }

     public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // Normalize the email address because it's case insensitive
        this.email = email == null || email.length() == 0 ? null : email.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null || name.length() == 0 ? null : name;
    }

    public String getReferralId() {
        return referralId;
    }

    public void setReferralId(String referralId) {
        this.referralId = referralId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        if (getEmail() != null) { out.put(EMAIL, getEmail()); }
        if (getName() != null) { out.put(NAME, getName()); }
        if (getReferralId() != null) { out.put(REFERRAL_ID, getReferralId()); }
        if (getUrl() != null) { out.put(URL, getUrl()); }
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
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(REFERRAL_ID)) { setReferralId(in.getString(REFERRAL_ID)); }
        if (in.containsKey(URL)) { setUrl(in.getString(URL)); }

        // Shortcut
        if (in.containsKey(INFLUENCER_KEY)) {setKey(in.getLong(INFLUENCER_KEY)); }

        return this;
    }
}
