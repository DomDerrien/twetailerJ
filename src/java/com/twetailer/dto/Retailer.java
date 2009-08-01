package com.twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

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
	private Long storeKey;
	
	public final static String STORE_KEY = "storeKey";
	
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
	public Long getStoreKey() {
		return storeKey;
	}

	public void setStoreKey(Long storeKey) {
		this.storeKey = storeKey;
	}

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        /*** Consumer ***/
        out.put(ADDRESS, getAddress());
        out.put(EMAIL, getEmail());
        out.put(IM_ID, getImId());
        out.put(NAME, getName());
        out.put(PHONE_NUMBER, getPhoneNumber());
        // out.put(SYSTEM_USER, getSystemUser());
        out.put(TWITTER_ID, getTwitterId());
        /*** Retailer ***/
		out.put(STORE_KEY, getStoreKey());
		return out;
	}

	public TransferObject fromJson(JsonObject in) {
	    super.fromJson(in);
	    /*** Consumer ***/
        if (in.containsKey(ADDRESS)) { setAddress(in.getString(ADDRESS)); }
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(IM_ID)) { setImId(in.getString(IM_ID)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
        // if (in.containsKey(SYSTEM_USER)) { setSystemUser(in.getObject(SYSTEM_USER)); }
        if (in.containsKey(TWITTER_ID)) { setTwitterId(in.getLong(TWITTER_ID)); }
        /*** Retailer ***/
		if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
		return this;
	}
}
