package com.twetailer.dto;

import java.util.Locale;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.twetailer.validator.LocaleValidator;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Consumer extends Entity {
    
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
    private String language = LocaleValidator.DEFAULT_LANGUAGE;
    
    public final static String LANGUAGE = "language";

    @Persistent
    private Long locationKey = 0L;
    
    public final static String LOCATION_KEY = "locationKey";

	@Persistent
	private String name;
	
    public final static String NAME = "name";

	@Persistent
	private String phoneNumber;
    
    public final static String PHONE_NUMBER = "phoneNb";
    
    @Persistent
    private Long twitterId;
    
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

    public Long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(Long twitterId) {
        this.twitterId = twitterId;
    }

    public JsonObject toJson() {
		JsonObject out = super.toJson();
		out.put(ADDRESS, getAddress());
		out.put(EMAIL, getEmail());
		out.put(IM_ID, getImId());
        out.put(LOCATION_KEY, getLocationKey());
        out.put(LANGUAGE, getLanguage());
		out.put(NAME, getName());
		out.put(PHONE_NUMBER, getPhoneNumber());
        out.put(TWITTER_ID, getTwitterId());
		return out;
	}

	public TransferObject fromJson(JsonObject in) {
	    super.fromJson(in);
		if (in.containsKey(ADDRESS)) { setAddress(in.getString(ADDRESS)); }
		if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
		if (in.containsKey(IM_ID)) { setImId(in.getString(IM_ID)); }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(LANGUAGE)) { setLanguage(in.getString(LANGUAGE)); }
		if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
		if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
        if (in.containsKey(TWITTER_ID)) { setTwitterId(in.getLong(TWITTER_ID)); }
        return this;
	}
}
