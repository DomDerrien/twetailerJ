package com.twetailer.dto;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.domderrien.i18n.DateUtils;
import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.TransferObject;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Consumer implements TransferObject {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long key;
	
	@Persistent
	private String address;
	
	public final static String ADDRESS = "address";
	
	@Persistent
	private Date creationDate;
	
	public final static String CREATION_DATE = "creationDate";
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
    
    /** Default constructor */
    public Consumer() {
        setCreationDate(getNowDate());
    }
    
    /**
     * Creates a consumer
     * 
     * @param in HTTP request parameters
     * @throws ParseException If the parameter extraction fails
     */
    public Consumer(JsonObject parameters) throws ParseException {
        this();
        fromJson(parameters);
    }
    
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	protected Calendar getNowCalendar() {
		return DateUtils.getNowCalendar();
	}

	protected Date getNowDate() {
		return getNowCalendar().getTime();
	}
	
	public Date getCreationDate() {
		if (creationDate == null) {
			setCreationDate(getNowDate());
		}
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		if (creationDate == null) {
			throw new IllegalArgumentException("Non null Date instance required");
		}
		this.creationDate = creationDate;
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


    public JsonObject toJson() {
		JsonObject out = new GenericJsonObject();
		out.put("key", getKey());
		out.put(ADDRESS, getAddress());
		out.put(CREATION_DATE, DateUtils.dateToISO(getCreationDate()));
		out.put(EMAIL, getEmail());
		out.put(IM_ID, getImId());
		out.put(NAME, getName());
		out.put(PHONE_NUMBER, getPhoneNumber());
		// out.put(SYSTEM_USER, getSystemUser());
        out.put(TWITTER_ID, getTwitterId());
		return out;
	}

	public void fromJson(JsonObject in) throws ParseException {
		if (in.containsKey("key")) { setKey(in.getLong("key")); }
		if (in.containsKey(ADDRESS)) { setAddress(in.getString(ADDRESS)); }
		// if (in.containsKey(CREATION_DATE)) { setCreationDate(DateUtil.isoToDate(in.getString(CREATION_DATE))); }
		if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
		if (in.containsKey(IM_ID)) { setImId(in.getString(IM_ID)); }
		if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
		if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
		// if (in.containsKey(SYSTEM_USER)) { setSystemUser(in.getObject(SYSTEM_USER)); }
        if (in.containsKey(TWITTER_ID)) { setTwitterId(in.getLong(TWITTER_ID)); }
	}
}
