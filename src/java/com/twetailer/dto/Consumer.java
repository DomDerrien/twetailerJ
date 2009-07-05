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
	
	@Persistent
	private Date creationDate;
	
	@Persistent
	private String email;
	
	@Persistent
	private String imId;
	
	@Persistent
	private String name;
	
	@Persistent
	private String phoneNumber;
	
	@Persistent
	private User systemUser;
    
    @Persistent
    private Long twitterId;
    
    // @Persistent
    // private String twitterScreenName;

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

    // public String getTwitterScreenName() {
    //     return twitterScreenName;
    // }

    // public void setTwitterScreenName(String twitterScreenName) {
    //     this.twitterScreenName = twitterScreenName;
    // }

    public JsonObject toJson() {
		JsonObject out = new GenericJsonObject();
		out.put("key", getKey());
		out.put("address", getAddress());
		out.put("creationDate", DateUtils.dateToISO(getCreationDate()));
		out.put("email", getEmail());
		out.put("imId", getImId());
		out.put("name", getName());
		out.put("phoneNumber", getPhoneNumber());
		// out.put("systemUser", getSystemUser());
        out.put("twitterId", getTwitterId());
        // out.put("twitterScreenName", getTwitterScreenName());
		return out;
	}

	public void fromJson(JsonObject in) throws ParseException {
		if (in.containsKey("key")) { setKey(in.getLong("key")); }
		if (in.containsKey("address")) { setAddress(in.getString("address")); }
		// if (in.containsKey("creationDate")) { setCreationDate(DateUtil.isoToDate(in.getString("creationDate"))); }
		if (in.containsKey("email")) { setEmail(in.getString("email")); }
		if (in.containsKey("imId")) { setImId(in.getString("imId")); }
		if (in.containsKey("name")) { setName(in.getString("name")); }
		if (in.containsKey("phoneNumber")) { setPhoneNumber(in.getString("phoneNumber")); }
		// if (in.containsKey("systemUser")) { setSystemUser(in.getObject("systemUser")); }
        if (in.containsKey("twitterId")) { setTwitterId(in.getLong("twitterId")); }
        // if (in.containsKey("twitterScreenName")) { setTwitterScreenName(in.getString("twitterScreenName")); }
	}
}
