package com.twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.domderrien.i18n.DateUtils;
import org.domderrien.jsontools.GenericJsonArray;
import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.TransferObject;

import com.twetailer.settings.CommandSettings;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Demand extends Command implements TransferObject {
	
	@Persistent
	private List<String> criteria;

    public static final String CRITERIA = "criteria";
	
	@Persistent
	private String countryCode;

    public static final String COUNTRY_CODE = "countryCode";
	
	@Persistent
	private Date expirationDate;
	
	@Persistent
	private String latitude;
	
	@Persistent
	private String longitude;
	
    @Persistent
    private String postalCode;

    public static final String POSTAL_CODE = "postalCode";

    @Persistent
    private Long quantity;

    public static final String QUANTITY = "quantity";
    
    @Persistent
    private Double range;

    public static final String RANGE = "range";
    
    @Persistent
    private String rangeUnit;

    public static final String RANGE_UNIT = "rangeUnit";

    public static final String EXPIRATION_DATE = "expirationDate";

	/**
	 * Delay for the default expiration of a demand
	 */
	public final static int DEFAULT_EXPIRATION_DELAY = 7;
	
	/**
	 * Creates a demand
	 * 
	 * @param in HTTP request parameters
	 * @throws ParseException If the parameter extraction fails
	 */
	public Demand(JsonObject parameters) throws ParseException {
	    super();
		fromJson(parameters);
		if (getExpirationDate() == null) {
			// Default expiration in one week
			setExpirationDate(DEFAULT_EXPIRATION_DELAY);
		}
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

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public String getRangeUnit() {
        return rangeUnit;
    }

    public void setRangeUnit(String rangeUnit) {
        this.rangeUnit = rangeUnit;
    }
    
    public static String getAttributeLabel(CommandSettings.Prefix prefix) {
        String inherited = Command.getAttributeLabel(prefix);
        if (inherited != null) return inherited;
        if (prefix == CommandSettings.Prefix.expiration) return EXPIRATION_DATE;
        if (prefix == CommandSettings.Prefix.location) return POSTAL_CODE;
        if (prefix == CommandSettings.Prefix.quantity) return QUANTITY;
        if (prefix == CommandSettings.Prefix.range) return RANGE;
        if (prefix == CommandSettings.Prefix.tags) return CRITERIA;
        return null;
    }

	public JsonObject toJson() {
	    // TODO: finish the constant definition for the serialization
		JsonObject out = super.toJson();
		JsonArray jsonArray = new GenericJsonArray();
		for(String criterion: getCriteria()) {
			jsonArray.add(criterion);
		}
		out.put(CRITERIA, jsonArray);
		out.put(COUNTRY_CODE, getCountryCode());
		out.put(EXPIRATION_DATE, DateUtils.dateToISO(getExpirationDate()));
		out.put("latitude", getLatitude());
		out.put("longitude", getLongitude());
		out.put(POSTAL_CODE, getPostalCode());
        out.put(QUANTITY, getQuantity());
        out.put(RANGE, getRange());
        out.put(RANGE_UNIT, getRangeUnit());
		return out;
	}

	public void fromJson(JsonObject in) throws ParseException {
        // TODO: finish the constant definition for the de-serialization
	    super.fromJson(in);
		if (in.containsKey(CRITERIA)) {
			JsonArray jsonArray = in.getJsonArray(CRITERIA);
			for (int i=0; i<jsonArray.size(); ++i) {
				addCriterion((String) jsonArray.getString(i));
			}
		}
		if (in.containsKey(COUNTRY_CODE)) { setCountryCode(in.getString(COUNTRY_CODE)); }
		if (in.containsKey(EXPIRATION_DATE)) { setExpirationDate(DateUtils.isoToDate(in.getString(EXPIRATION_DATE))); }
		if (in.containsKey("latitude")) { setLatitude(in.getString("latitude")); }
		if (in.containsKey("longitude")) { setLongitude(in.getString("longitude")); }
        if (in.containsKey(POSTAL_CODE)) { setPostalCode(in.getString(POSTAL_CODE)); }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }
        if (in.containsKey(RANGE)) { setRange(in.getDouble(RANGE)); }
        if (in.containsKey(RANGE_UNIT)) { setRangeUnit(in.getString(RANGE_UNIT)); }
	}
}
