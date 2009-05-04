package com.twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.domderrien.i18n.DateUtils;
import org.domderrien.jsontools.GenericJsonArray;
import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Request implements TransferObject {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long key;
	
	@Persistent
	private Date creationDate;
	
	@Persistent
	private Long consumerKey;
	
	@Persistent
	private List<String> criteria;
	
	@Persistent
	private String countryCode;
	
	@Persistent
	private Date expirationDate;
	
	@Persistent
	private String latitude;
	
	@Persistent
	private String longitude;
	
	@Persistent
	private String postalCode;

	/**
	 * Delay for the default expiration of a request
	 */
	public final static int DEFAULT_EXPIRATION_DELAY = 7;
	
	/**
	 * Creates a request
	 * 
	 * @param in HTTP request parameters
	 * @throws ParseException If the parameter extraction fails
	 */
	public Request(JsonObject parameters) throws ParseException {
		fromJson(parameters);
		setCreationDate(getNowDate());
		if (getExpirationDate() == null) {
			// Default expiration in one week
			setExpirationDate(DEFAULT_EXPIRATION_DELAY);
		}
	}
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
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

	public Long getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(Long consumerId) {
		this.consumerKey = consumerId;
	}

	public List<String> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<String> criteria) {
		this.criteria = criteria;
	}

	public void addCriterion(String criterion) {
		if (criteria == null) {
			criteria = new ArrayList<String>();
		}
		criteria.add(criterion);
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		if (expirationDate == null) {
			throw new IllegalArgumentException("Non null Date instance required");
		}
		this.expirationDate = expirationDate;
	}

	public void setExpirationDate(int delayInDays) {
		Calendar limit = getNowCalendar();
		limit.set(Calendar.DAY_OF_MONTH, limit.get(Calendar.DAY_OF_MONTH) + delayInDays);
		this.expirationDate = limit.getTime();
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public JsonObject toJson() {
		JsonObject out = new GenericJsonObject();
		out.put("key", getKey());
		out.put("creationDate", DateUtils.toISO(getCreationDate().getTime()));
		out.put("consumerKey", getConsumerKey());
		JsonArray jsonArray = new GenericJsonArray();
		for(String criterion: getCriteria()) {
			jsonArray.add(criterion);
		}
		out.put("criteria", jsonArray);
		out.put("countryCode", getCountryCode());
		if (getExpirationDate() == null) {
			setExpirationDate(DEFAULT_EXPIRATION_DELAY);
		}
		out.put("expirationDate", DateUtils.toISO(getExpirationDate().getTime()));
		out.put("latitude", getLatitude());
		out.put("longitude", getLongitude());
		out.put("postalCode", getPostalCode());
		return out;
	}

	public void fromJson(JsonObject in) throws ParseException {
		if (in.containsKey("key")) { setKey(in.getLong("key")); }
		if (in.containsKey("consumerKey")) { setConsumerKey(in.getLong("consumerKey")); }
		// if (in.containsKey("creationDate")) { setCreationDate(DateUtil.fromISOToDate(in.getString("creationDate"))); }
		if (in.containsKey("criteria")) {
			JsonArray jsonArray = in.getJsonArray("criteria");
			for (int i=0; i<jsonArray.size(); ++i) {
				addCriterion((String) jsonArray.getString(i));
			}
		}
		if (in.containsKey("countryCode")) { setCountryCode(in.getString("countryCode")); }
		if (in.containsKey("expirationDate")) { setExpirationDate(DateUtils.fromISOToDate(in.getString("expirationDate"))); }
		if (in.containsKey("latitude")) { setLatitude(in.getString("latitude")); }
		if (in.containsKey("longitude")) { setLongitude(in.getString("longitude")); }
		if (in.containsKey("postalCode")) { setPostalCode(in.getString("postalCode")); }
	}
}
