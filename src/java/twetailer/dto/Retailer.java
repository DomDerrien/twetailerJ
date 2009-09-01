package twetailer.dto;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.validator.LocaleValidator;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Retailer extends Entity {

    /*** Retailer ***/
    @Persistent
    private Long creatorKey;
    
    public final static String CREATOR_KEY = "creatorKey";

    @Persistent
    private Long consumerKey;
    
    public final static String CONSUMER_KEY = "consumerKey";
    
    @Persistent
    private List<String> criteria = new ArrayList<String>();
    
    public final static String  CRITERIA = "criteria";
    
    @Persistent
    private String email;
    
    public final static String EMAIL = "email";
    
    @Persistent
    private String imId;
    
    public final static String IM_ID = "imId";

    @Persistent
    private Boolean isStoreAdmin;
    
    public final static String IS_STORE_ADMIN_KEY = "isStoreAdmin";

    @Persistent
    private String language = LocaleValidator.DEFAULT_LANGUAGE;
    
    public final static String LANGUAGE = "language";

    @Persistent
    private Long locationKey;
    
    public final static String LOCATION_KEY = "locationKey";

    @Persistent
    private String name;
    
    public final static String NAME = "name";

    @Persistent
    private String phoneNumber;
    
    public final static String PHONE_NUMBER = "phoneNb";

    @Persistent
    private Long storeKey;
    
    public final static String STORE_KEY = "storeKey";
    
    // Not persistent
    private Long score;
    
    public final static String SCORE = "score";
    
    @Persistent
    private Long twitterId;
    
    public final static String TWITTER_ID = "twitterId";
    
    /** Default constructor */
    public Retailer() {
        super();
    }
    
    /**
     * Creates a retailer
     * 
     * @param in HTTP request parameters
     */
    public Retailer(JsonObject parameters) {
        this();
        fromJson(parameters);
    }
    
    public Long getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(Long creatorKey) {
        this.creatorKey = creatorKey;
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
        int idx = criteria.size();
        while (0 < idx) {
            --idx;
            this.criteria.remove(idx);
        }
    }

    public void removeCriterion(String criterion) {
        if (criteria == null) {
            return;
        }
        criteria.remove(criterion);
    }

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerKey) {
        this.consumerKey = consumerKey;
    }

    public List<String> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<String> criteria) {
        if (criteria == null) {
            throw new InvalidParameterException("Cannot nullify the attribute 'criteria' of type List<String>");
        }
        this.criteria = criteria;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImId() {
        return imId;
    }

    public void setImId(String imId) {
        this.imId = imId;
    }

    public Boolean isStoreAdmin() {
        return isStoreAdmin;
    }

    public void setIsStoreAdmin(Boolean isStoreAdmin) {
        this.isStoreAdmin = isStoreAdmin;
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
        this.locationKey = locationKey;
    }
    
    public Locale getLocale() {
        return LocaleValidator.getLocale(language);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getStoreKey() {
        return storeKey;
    }

    public void setStoreKey(Long storeKey) {
		this.storeKey = storeKey;
	}

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(Long twitterId) {
        this.twitterId = twitterId;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(CREATOR_KEY, getCreatorKey());
        out.put(CONSUMER_KEY, getConsumerKey());
        if (getCriteria() != null) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
            out.put(CRITERIA, jsonArray);
        }
        out.put(EMAIL, getEmail());
        out.put(IM_ID, getImId());
        out.put(IS_STORE_ADMIN_KEY, isStoreAdmin());
        out.put(LOCATION_KEY, getLocationKey());
        out.put(NAME, getName());
        out.put(PHONE_NUMBER, getPhoneNumber());
        out.put(STORE_KEY, getStoreKey());
        out.put(SCORE, getScore());
        out.put(TWITTER_ID, getTwitterId());
		return out;
	}

	public TransferObject fromJson(JsonObject in) {
	    super.fromJson(in);
        if (in.containsKey(CREATOR_KEY)) { setCreatorKey(in.getLong(CREATOR_KEY)); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(CRITERIA)) {
            boolean additionMode = true;
            JsonArray jsonArray = in.getJsonArray(CRITERIA);
            for (int i=0; i<jsonArray.size(); ++i) {
                if ("+".equals(jsonArray.getString(0))) {
                    additionMode = true;
                }
                else if ("-".equals(jsonArray.getString(0))) {
                    additionMode = false;
                }
                else if (i == 0) {
                    resetCriteria();
                    addCriterion(jsonArray.getString(i));
                }
                else if (additionMode) {
                    addCriterion(jsonArray.getString(i));
                }
                else {
                    removeCriterion(jsonArray.getString(i));
                }
            }
        }
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(IM_ID)) { setImId(in.getString(IM_ID)); }
        if (in.containsKey(IS_STORE_ADMIN_KEY)) { setIsStoreAdmin(in.getBoolean(IS_STORE_ADMIN_KEY)); }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
        if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
        if (in.containsKey(SCORE)) { setScore(in.getLong(SCORE)); }
        if (in.containsKey(TWITTER_ID)) { setTwitterId(in.getLong(TWITTER_ID)); }
		return this;
	}
}
