package twetailer.dto;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.LocaleValidator;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class SaleAssociate extends Entity {

    /*** SaleAssociate ***/
    private Collator collator;

    @Persistent
    private Long consumerKey;

    public final static String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private Long creatorKey;

    public final static String CREATOR_KEY = "creatorKey";

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public final static String  CRITERIA = Demand.CRITERIA;
    public static final String CRITERIA_ADD = Demand.CRITERIA_ADD;
    public static final String CRITERIA_REMOVE = Demand.CRITERIA_REMOVE;

    @Persistent
    private String email;

    public final static String EMAIL = Consumer.EMAIL;

    @Persistent
    private String jabberId;

    public final static String JABBER_ID = Consumer.JABBER_ID;

    @Persistent
    private Boolean isStoreAdmin;

    public final static String IS_STORE_ADMIN = "isStoreAdmin";

    @Persistent
    private String language = LocaleValidator.DEFAULT_LANGUAGE;

    public final static String LANGUAGE = Consumer.LANGUAGE;

    @Persistent
    private Long locationKey;

    public final static String LOCATION_KEY = Location.LOCATION_KEY;

    @Persistent
    private String name;

    public final static String NAME = Consumer.NAME;

    @Persistent
    private String openID;

    public final static String OPEN_ID = Consumer.OPEN_ID;

    @Persistent
    private String phoneNumber;

    public final static String PHONE_NUMBER = Consumer.PHONE_NUMBER;

    @Persistent
    private Source preferredConnection = Source.twitter;

    public final static String PREFERRED_CONNECTION = "preferredConnection";

    // Shortcut
    public final static String SALEASSOCIATE_KEY = "saleAssociateKey";

    @Persistent
    private Long storeKey;

    public final static String STORE_KEY = Store.STORE_KEY;

    // Not persistent
    private Long score;

    public final static String SCORE = "score";

    @Persistent
    private String twitterId;

    public final static String TWITTER_ID = Consumer.TWITTER_ID;

    /** Default constructor */
    public SaleAssociate() {
        super();
    }

    /**
     * Creates a sale associate
     *
     * @param in HTTP request parameters
     */
    public SaleAssociate(JsonObject in) {
        this();
        fromJson(in);
    }

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     */
    protected void resetLists() {
        criteria = null;
    }

    protected Collator getCollator() {
        if (collator == null) {
            collator = LocaleValidator.getCollator(getLocale());
        }
        return collator;
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

    public Long getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(Long creatorKey) {
        if (creatorKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'creatorKey'");
        }
        this.creatorKey = creatorKey;
    }

    public void addCriterion(String criterion) {
        removeCriterion(criterion);
        if (criterion == null || criterion.length() == 0) {
            return;
        }
        if (criteria == null) {
            criteria = new ArrayList<String>();
        }
        criteria.add(criterion);
    }

    public void resetCriteria() {
        if (criteria == null) {
            return;
        }
        criteria = new ArrayList<String>();
    }

    public void removeCriterion(String criterion) {
        if (criteria == null|| criterion == null || criterion.length() == 0) {
            return;
        }
        String normalizedCriterion = LocaleValidator.toUnicode(criterion);
        for(String item: criteria) {
            String normalizedItem = LocaleValidator.toUnicode(item);
            if (getCollator().compare(normalizedCriterion, normalizedItem) == 0) {
                criteria.remove(item);
                break;
            }
        }
    }

    public String getSerializedCriteria() {
        return Command.getSerializedTags(criteria);
    }

    public List<String> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<String> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'criteria' of type List<String>");
        }
        this.criteria = null;
        for (String criterion: criteria) {
            addCriterion(criterion);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // Normalize the email address because it's case unsensitive
        this.email = email == null || email.length() == 0 ? null : email.toLowerCase();
    }

    public String getJabberId() {
        return jabberId;
    }

    public void setJabberId(String jabberId) {
        // Normalize the Jabber identifier because it's case unsensitive
        this.jabberId = jabberId == null || jabberId.length() == 0 ? null : jabberId.toLowerCase();
    }

    public Boolean getIsStoreAdmin() {
        return isStoreAdmin == null ? Boolean.FALSE : isStoreAdmin;
    }

    public void setIsStoreAdmin(Boolean isStoreAdmin) {
        this.isStoreAdmin = isStoreAdmin;
    }

    public boolean isStoreAdmin() {
        return Boolean.TRUE.equals(isStoreAdmin);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = LocaleValidator.checkLanguage(language);
    }

    public Long getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(Long locationKey) {
        if (locationKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'locationKey'");
        }
        this.locationKey = locationKey;
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
        return preferredConnection;
    }

    public void setPreferredConnection(Source preferredConnection) {
        if (preferredConnection == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'preferredConnection'");
        }
        this.preferredConnection = preferredConnection;
    }

    public void setPreferredConnection(String preferredConnection) {
        setPreferredConnection(Source.valueOf(preferredConnection));
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

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        if (score == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'score'");
        }
        this.score = score;
    }

    public String getTwitterId() {
        return twitterId == null || twitterId.length() == 0 ? null : twitterId;
    }

    public void setTwitterId(String twitterId) {
        // Note: no normalization because the Twitter identifier is case sensitive!
        this.twitterId = twitterId;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        if (getCreatorKey() != null) { out.put(CREATOR_KEY, getCreatorKey()); }
        if (getCriteria() != null) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
            out.put(CRITERIA, jsonArray);
        }
        out.put(EMAIL, getEmail());
        out.put(JABBER_ID, getJabberId());
        out.put(IS_STORE_ADMIN, isStoreAdmin());
        out.put(LANGUAGE, getLanguage());
        if (getLocationKey() != null) { out.put(LOCATION_KEY, getLocationKey()); }
        out.put(NAME, getName());
        out.put(OPEN_ID, getOpenID());
        out.put(PHONE_NUMBER, getPhoneNumber());
        out.put(PREFERRED_CONNECTION, getPreferredConnection().toString());
        if (getStoreKey() != null) { out.put(STORE_KEY, getStoreKey()); }
        if (getScore() != null) { out.put(SCORE, getScore()); }
        out.put(TWITTER_ID, getTwitterId());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(CREATOR_KEY)) { setCreatorKey(in.getLong(CREATOR_KEY)); }
        if (in.containsKey(CRITERIA)) {
            resetCriteria();
            JsonArray jsonArray = in.getJsonArray(CRITERIA);
            for (int i=0; i<jsonArray.size(); ++i) {
                addCriterion(jsonArray.getString(i));
            }
        }
        Demand.removeDuplicates(in, CRITERIA_ADD, CRITERIA_REMOVE);
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
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(JABBER_ID)) { setJabberId(in.getString(JABBER_ID)); }
        if (in.containsKey(IS_STORE_ADMIN)) { setIsStoreAdmin(in.getBoolean(IS_STORE_ADMIN)); }
        if (in.containsKey(LANGUAGE)) { setLanguage(in.getString(LANGUAGE)); }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(OPEN_ID)) { setOpenID(in.getString(OPEN_ID)); }
        if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
        if (in.containsKey(PREFERRED_CONNECTION)) { setPreferredConnection(in.getString(PREFERRED_CONNECTION)); }
        if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
        if (in.containsKey(SCORE)) { setScore(in.getLong(SCORE)); }
        if (in.containsKey(TWITTER_ID)) { setTwitterId(in.getString(TWITTER_ID)); }

        // Shortcut
        if (in.containsKey(SALEASSOCIATE_KEY)) { setKey(in.getLong(SALEASSOCIATE_KEY)); }

        return this;
    }
}
