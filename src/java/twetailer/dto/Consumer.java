package twetailer.dto;

import java.util.Locale;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.LocaleValidator;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a Twetailer consumer
 *
 * @see twetailer.dto.SaleAssociate
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Consumer extends Entity {

    // Check http://code.google.com/appengine/docs/java/datastore/dataclasses.html for the various data types
    //
    // Type                             Java class
    // ------------------------------   ---------------------------------------------------------------
    // short text string, < 500 bytes   java.lang.String
    // short byte string, < 500 bytes   com.google.appengine.api.datastore.ShortBlob
    // Boolean value                    boolean or java.lang.Boolean
    // integer                          short, java.lang.Short, int, java.lang.Integer, long, java.lang.Long
    // floating point number            float, java.lang.Float, double, java.lang.Double
    // date-time                        java.util.Date
    // Google account                   com.google.appengine.api.users.User
    // long text string                 com.google.appengine.api.datastore.Text
    // long byte string                 com.google.appengine.api.datastore.Blob
    // entity key                       com.google.appengine.api.datastore.Key, or the referenced object (as a child)
    // a category                       com.google.appengine.api.datastore.Category
    // an email address                 com.google.appengine.api.datastore.Email
    // a geographical point             com.google.appengine.api.datastore.GeoPt
    // an instant messaging handle      com.google.appengine.api.datastore.IMHandle
    // a URL                            com.google.appengine.api.datastore.Link
    // a phone number                   com.google.appengine.api.datastore.PhoneNumber
    // a postal address                 com.google.appengine.api.datastore.PostalAddress
    // a user-provided rating, [0; 100] com.google.appengine.api.datastore.Rating

    @Persistent
    private String address;

    public final static String ADDRESS = "address";

    @Persistent
    private Boolean automaticLocaleUpdate = Boolean.TRUE;

    public final static String AUTOMATIC_LOCALE_UPDATE = "automaticLocaleUpdate";

    public enum Autonomy {
        BLOCKED,
        UNCONFIRMED,
        MODERATED,
        AUTONOMOUS
    }

    @Persistent
    private Autonomy autonomy; // null == Autonomy.MODERATED;

    public final static String AUTONOMY = "autonomy";

    @Persistent
    private Long closedDemandNb;

    public final static String CLOSED_DEMAND_NB = "closedDemandNb";

    // Shortcut
    public final static String CONSUMER_KEY = "consumerKey";

    @Persistent
    private String email;

    public final static String EMAIL = "email";

    public final static String EMAIL_REGEXP_VALIDATOR = "[\\w\\._%+-]+@[\\w\\.-]+\\.\\w{2,4}";

    @Persistent
    private String facebookId;

    public final static String FACEBOOK_ID = "facebookId";

    @Persistent
    private String jabberId;

    public final static String JABBER_ID = "jabberId";

    @Persistent
    private String language = LocaleValidator.DEFAULT_LANGUAGE;

    public final static String LANGUAGE = "language";

    @Persistent
    private String name;

    public final static String NAME = "name";

    @Persistent
    private String openID;

    public final static String OPEN_ID = "openID";

    @Persistent
    private String phoneNumber;

    public final static String PHONE_NUMBER = "phoneNb";

    @Persistent
    private Source preferredConnection = Source.mail;

    public final static String PREFERRED_CONNECTION = "preferredConnection";

    @Persistent
    private Long publishedDemandNb;

    public final static String PUBLISHED_DEMAND_NB = "publishedDemandNb";

    @Persistent
    private Long saleAssociateKey;

    public final static String SALE_ASSOCIATE_KEY = SaleAssociate.SALEASSOCIATE_KEY;

    @Persistent
    private String twitterId;

    public final static String TWITTER_ID = "twitterId";

    /** Default constructor */
    public Consumer() {
        super();
    }

    /**
     * Creates a consumer
     *
     * @param in HTTP request parameters
     */
    public Consumer(JsonObject parameters) {
        this();
        fromJson(parameters);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null || address.length() == 0 ? null : address;
    }

    public Boolean getAutomaticLocaleUpdate() {
        return automaticLocaleUpdate == null ? Boolean.FALSE : automaticLocaleUpdate;
    }

    public void setAutomaticLocaleUpdate(Boolean automaticLocaleUpdate) {
        this.automaticLocaleUpdate = automaticLocaleUpdate;
    }

    public Autonomy getAutonomy() {
        return autonomy == null ? Autonomy.MODERATED : autonomy;
    }

    public void setAutonomy(Autonomy autonomy) {
        this.autonomy = autonomy == Autonomy.MODERATED ? null : autonomy;
    }

    public void setAutonomy(String autonomy) {
        setAutonomy(Autonomy.valueOf(autonomy));
    }

    public Long getClosedDemandNb() {
        return closedDemandNb;
    }

    public void setClosedDemandNb(Long closedDemandNb) {
        this.closedDemandNb = closedDemandNb == 0L ? null : closedDemandNb;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // Normalize the email address because it's case insensitive
        this.email = email == null || email.length() == 0 ? null : email.toLowerCase();
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        // Note: no normalization because the Facebook identifier is case sensitive!
        this.facebookId = facebookId == null || facebookId.length() == 0 ? null : facebookId;
    }

    public String getJabberId() {
        return jabberId;
    }

    public void setJabberId(String jabberId) {
        // Normalize the Jabber identifier because it's case insensitive
        this.jabberId = jabberId == null || jabberId.length() == 0 ? null : jabberId.toLowerCase();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = LocaleValidator.checkLanguage(language);
    }

    public Locale getLocale() {
        return LocaleValidator.getLocale(language);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null || name.length() == 0 ? null : name;
    }

    public String getOpenID() {
        return openID;
    }

    public void setOpenID(String openID) {
        // Note: no normalization because the OpenID identifier is case sensitive!
        this.openID = openID == null || openID.length() == 0 ? null : openID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null || phoneNumber.length() == 0 ? null : phoneNumber;
    }

    public Source getPreferredConnection() {
        if (preferredConnection == null) {
            return Source.mail;
        }
        return preferredConnection;
    }

    public void setPreferredConnection(Source preferredConnection) {
        this.preferredConnection = preferredConnection;
    }

    public void setPreferredConnection(String preferredConnection) {
        this.preferredConnection = Source.valueOf(preferredConnection);
    }

    public Long getPublishedDemandNb() {
        return publishedDemandNb;
    }

    public void setPublishedDemandNb(Long publishedDemandNb) {
        this.publishedDemandNb = publishedDemandNb == 0L ? null : publishedDemandNb;
    }

    public Long getSaleAssociateKey() {
        return saleAssociateKey;
    }

    public void setSaleAssociateKey(Long saleAssociateKey) {
        this.saleAssociateKey = saleAssociateKey;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        // Note: no normalization because the Twitter identifier is case sensitive!
        this.twitterId = twitterId == null || twitterId.length() == 0 ? null : twitterId;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getAddress() != null) { out.put(ADDRESS, getAddress()); }
        out.put(AUTOMATIC_LOCALE_UPDATE, getAutomaticLocaleUpdate());
        out.put(AUTONOMY, getAutonomy().toString());
        out.put(CLOSED_DEMAND_NB, getClosedDemandNb() == null ? 0L : getClosedDemandNb());
        if (getEmail() != null) { out.put(EMAIL, getEmail()); }
        if (getFacebookId() != null) { out.put(FACEBOOK_ID, getFacebookId()); }
        if (getJabberId() != null) { out.put(JABBER_ID, getJabberId()); }
        out.put(LANGUAGE, getLanguage());
        if (getName() != null) { out.put(NAME, getName()); }
        if (getOpenID() != null) { out.put(OPEN_ID, getOpenID()); }
        if (getPhoneNumber() != null) { out.put(PHONE_NUMBER, getPhoneNumber()); }
        out.put(PREFERRED_CONNECTION, getPreferredConnection().toString());
        out.put(PUBLISHED_DEMAND_NB, getPublishedDemandNb() == null ? 0L : getPublishedDemandNb());
        if (getSaleAssociateKey() != null) { out.put(SALE_ASSOCIATE_KEY, getSaleAssociateKey()); }
        if (getTwitterId() != null) { out.put(TWITTER_ID, getTwitterId()); }
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        return fromJson(in, false, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin, boolean isCacheRelated) {
        if (isCacheRelated) { isUserAdmin = isCacheRelated; }
        super.fromJson(in, isUserAdmin, isCacheRelated);

        if (in.containsKey(ADDRESS)) { setAddress(in.getString(ADDRESS)); }
        if (in.containsKey(AUTOMATIC_LOCALE_UPDATE)) { setAutomaticLocaleUpdate(in.getBoolean(AUTOMATIC_LOCALE_UPDATE)); }
        if (isUserAdmin && in.containsKey(AUTONOMY)) { setAutonomy(in.getString(AUTONOMY)); }
        if (isUserAdmin && in.containsKey(CLOSED_DEMAND_NB)) { setClosedDemandNb(in.getLong(CLOSED_DEMAND_NB)); }
        if ((isUserAdmin || getKey() == null) && in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if ((isUserAdmin || getKey() == null) && in.containsKey(FACEBOOK_ID)) { setFacebookId(in.getString(FACEBOOK_ID)); }
        if ((isUserAdmin || getKey() == null) && in.containsKey(JABBER_ID)) { setJabberId(in.getString(JABBER_ID)); }
        if (in.containsKey(LANGUAGE)) { setLanguage(in.getString(LANGUAGE)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if ((isUserAdmin || getKey() == null) && in.containsKey(OPEN_ID)) { setOpenID(in.getString(OPEN_ID)); }
        if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
        if (in.containsKey(PREFERRED_CONNECTION)) { setPreferredConnection(in.getString(PREFERRED_CONNECTION)); }
        if (isUserAdmin && in.containsKey(PUBLISHED_DEMAND_NB)) { setPublishedDemandNb(in.getLong(PUBLISHED_DEMAND_NB)); }
        if ((isUserAdmin || getKey() == null) && in.containsKey(SALE_ASSOCIATE_KEY)) {
            // Cannot change once set at creation time
            setSaleAssociateKey(in.getLong(SALE_ASSOCIATE_KEY));
        }
        if ((isUserAdmin || getKey() == null) && in.containsKey(TWITTER_ID)) { setTwitterId(in.getString(TWITTER_ID)); }

        // Shortcut
        if (in.containsKey(CONSUMER_KEY)) { setKey(in.getLong(CONSUMER_KEY)); }

        return this;
    }
}
