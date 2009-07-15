package com.twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.domderrien.i18n.DateUtils;
import org.domderrien.jsontools.GenericJsonArray;
import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonObject;

import com.twetailer.ClientException;
import com.twetailer.settings.CommandSettings.State;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Demand extends Command {
	
    @Persistent
    private Long consumerKey = 0L;

    public static final String CONSUMER_KEY = "consumerKey";
    
	@Persistent
	private String countryCode;

    public static final String COUNTRY_CODE = "countryCode";
	
    @Persistent
    private List<String> criteria;

    public static final String CRITERIA = "criteria";
    
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
	
    
    /** Default constructor */
    public Demand() {
        super();
    }

    /**
	 * Creates a demand
	 * 
	 * @param in HTTP request parameters
	 * @throws ParseException If the parameter extraction fails
	 */
	public Demand(JsonObject parameters) throws ParseException {
	    this();
        try {
            fromJson(parameters);
        }
        catch (ClientException e) {
            // No risk to override an existing value because this is a newly (an empty) object instace
        }
	}

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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

	public JsonObject toJson() {
	    // TODO: finish the constant definition for the serialization
		JsonObject out = super.toJson();
		JsonArray jsonArray = new GenericJsonArray();
		for(String criterion: getCriteria()) {
			jsonArray.add(criterion);
		}
        out.put(COUNTRY_CODE, getCountryCode());
        out.put(CONSUMER_KEY, getConsumerKey());
		out.put(CRITERIA, jsonArray);
		out.put(EXPIRATION_DATE, DateUtils.dateToISO(getExpirationDate()));
		out.put("latitude", getLatitude());
		out.put("longitude", getLongitude());
		out.put(POSTAL_CODE, getPostalCode());
        out.put(QUANTITY, getQuantity());
        out.put(RANGE, getRange());
        out.put(RANGE_UNIT, getRangeUnit());
		return out;
	}

	public void fromJson(JsonObject in) throws ParseException, ClientException {
        // TODO: finish the constant definition for the de-serialization
	    super.fromJson(in);
		if (in.containsKey(CRITERIA)) {
			JsonArray jsonArray = in.getJsonArray(CRITERIA);
			for (int i=0; i<jsonArray.size(); ++i) {
				addCriterion((String) jsonArray.getString(i));
			}
		}
		if (in.containsKey(COUNTRY_CODE)) { setCountryCode(in.getString(COUNTRY_CODE)); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
		if (in.containsKey(EXPIRATION_DATE)) { setExpirationDate(DateUtils.isoToDate(in.getString(EXPIRATION_DATE))); }
		if (in.containsKey("latitude")) { setLatitude(in.getString("latitude")); }
		if (in.containsKey("longitude")) { setLongitude(in.getString("longitude")); }
        if (in.containsKey(POSTAL_CODE)) { setPostalCode(in.getString(POSTAL_CODE)); }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }
        if (in.containsKey(RANGE)) { setRange(in.getDouble(RANGE)); }
        if (in.containsKey(RANGE_UNIT)) { setRangeUnit(in.getString(RANGE_UNIT)); }

        checkForCompletion();
	}
	
	protected void checkForCompletion() {
	    if (getState() == State.incomplete) {
	        if (getCriteria() != null && 0 < getCriteria().size()) {
	            if (getExpirationDate() != null) {
	                if (getRange() != null && 5.0D <= getRange().doubleValue()) {
	                    if (getCountryCode() != null && getPostalCode() != null || getLatitude() != null && getLongitude() != null) {
	                        if (getQuantity() != null && 0L < getQuantity().longValue()) {
	                            setState(State.completed);
	                        }
	                    }
	                }
	            }
	        }
	    }
	}
}
