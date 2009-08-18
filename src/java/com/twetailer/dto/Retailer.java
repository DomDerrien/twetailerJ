package com.twetailer.dto;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Retailer extends Entity {

    /*** Consumer ***/
    @Persistent
    private String address;
    
    public final static String ADDRESS = "address";
    
    @Persistent
    private String email;
    
    public final static String EMAIL = "email";
    
    @Persistent
    private String imId;
    
    public final static String IM_ID = "imId";

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
    private User systemUser;
    
    public final static String SYSTEM_USER = "sysUser";
    
    @Persistent
    private Long twitterId;
    
    public final static String TWITTER_ID = "twitterId";

    /*** Retailer ***/
    @Persistent
    private Long creatorKey;
    
    public final static String CREATOR_KEY = "creatorKey";

    @Persistent
    private Boolean isStoreAdmin;
    
    public final static String IS_STORE_ADMIN_KEY = "isStoreAdmin";

    @Persistent
    private Long storeKey;
    
    public final static String STORE_KEY = "storeKey";
    
    // Not persistent
    private Long score;
    
    public final static String SCORE = "score";
    
    @Persistent
    private List<String> supplies = new ArrayList<String>();

    public static final String SUPPLIES = "supplies";
    
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
    
    /*** Consumer ***/
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Long getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(Long locationKey) {
        this.locationKey = locationKey;
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

    public User getSystemUser() {
        return systemUser;
    }

    public void setSystemUser(User systemUser) {
        this.systemUser = systemUser;
    }

    public Long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(Long twitterId) {
        this.twitterId = twitterId;
    }

    /*** Retailer ***/
	public Long getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(Long creatorKey) {
        this.creatorKey = creatorKey;
    }

    public Boolean isStoreAdmin() {
        return isStoreAdmin;
    }

    public void setIsStoreAdmin(Boolean isStoreAdmin) {
        this.isStoreAdmin = isStoreAdmin;
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

    public List<String> getSupplies() {
        return supplies;
    }

    public void setSupplies(List<String> supplies) {
        this.supplies = supplies;
    }

    public void addSupply(String supply) {
        if (!supplies.contains(supply)) {
            supplies.add(supply);
        }
    }
    
    public void resetSupplies() {
        int idx = supplies.size();
        while (0 < idx) {
            --idx;
            this.supplies.remove(idx);
        }
    }

    public void removeSupply(String supply) {
        supplies.remove(supply);
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        /*** Consumer ***/
        out.put(ADDRESS, getAddress());
        out.put(EMAIL, getEmail());
        out.put(IM_ID, getImId());
        out.put(LOCATION_KEY, getLocationKey());
        out.put(NAME, getName());
        out.put(PHONE_NUMBER, getPhoneNumber());
        // out.put(SYSTEM_USER, getSystemUser());
        out.put(TWITTER_ID, getTwitterId());
        /*** Retailer ***/
        out.put(CREATOR_KEY, getCreatorKey());
        out.put(IS_STORE_ADMIN_KEY, isStoreAdmin());
        out.put(STORE_KEY, getStoreKey());
        out.put(SCORE, getScore());
        if (getSupplies() != null) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String supply: getSupplies()) {
                jsonArray.add(supply);
            }
            out.put(SUPPLIES, jsonArray);
        }
		return out;
	}

	public TransferObject fromJson(JsonObject in) {
	    super.fromJson(in);
	    /*** Consumer ***/
        if (in.containsKey(ADDRESS)) { setAddress(in.getString(ADDRESS)); }
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(IM_ID)) { setImId(in.getString(IM_ID)); }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
        // if (in.containsKey(SYSTEM_USER)) { setSystemUser(in.getObject(SYSTEM_USER)); }
        if (in.containsKey(TWITTER_ID)) { setTwitterId(in.getLong(TWITTER_ID)); }
        /*** Retailer ***/
        if (in.containsKey(CREATOR_KEY)) { setCreatorKey(in.getLong(CREATOR_KEY)); }
        if (in.containsKey(IS_STORE_ADMIN_KEY)) { setIsStoreAdmin(in.getBoolean(IS_STORE_ADMIN_KEY)); }
        if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
        if (in.containsKey(SCORE)) { setScore(in.getLong(SCORE)); }
        if (in.containsKey(SUPPLIES)) {
            boolean additionMode = true;
            JsonArray jsonArray = in.getJsonArray(SUPPLIES);
            for (int i=0; i<jsonArray.size(); ++i) {
                if ("+".equals(jsonArray.getString(0))) {
                    additionMode = true;
                }
                else if ("-".equals(jsonArray.getString(0))) {
                    additionMode = false;
                }
                else if (i == 0) {
                    resetSupplies();
                    addSupply(jsonArray.getString(i));
                }
                else if (additionMode) {
                    addSupply(jsonArray.getString(i));
                }
                else {
                    removeSupply(jsonArray.getString(i));
                }
            }
        }
		return this;
	}
}
