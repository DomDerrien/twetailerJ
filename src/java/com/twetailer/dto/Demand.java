package com.twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

import com.twetailer.validator.CommandSettings;
import com.twetailer.validator.CommandSettings.State;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Demand extends Entity {
	
    /*** Command ***/
    
    @Persistent
    private CommandSettings.Action action;
    
    public static final String ACTION = "action";
    
    @Persistent
    private Long consumerKey = 0L;

    public static final String CONSUMER_KEY = "consumerKey";
    
    @Persistent
    private State state = State.open;
    
    public static final String STATE = "state";

    @Persistent
    private Long tweetId;
    
    public static final String TWEET_ID = "tweetId";

    /*** Demand ***/
    
    @Persistent
	private String countryCode;

    public static final String COUNTRY_CODE = "countryCode";
	
    @Persistent
    private List<String> criteria;

    public static final String CRITERIA = "criteria";
    
	@Persistent
	private Date expirationDate;

    public static final String EXPIRATION_DATE = "expirationDate";
	
	@Persistent
	private Double latitude = Double.valueOf(-1.0D);

    public static final String LATITUDE = "latitude";
	
	@Persistent
	private Double longitude = Double.valueOf(-1.0D);

    public static final String LONGITUDE = "longitude";
	
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
	 */
	public Demand(JsonObject parameters) {
	    this();
        fromJson(parameters);
	}

	/*** Command ***/
	
    public CommandSettings.Action getAction() {
        return action;
    }

    public void setAction(CommandSettings.Action action) {
        this.action = action;
    }

    public void setAction(String action) {
        this.action = CommandSettings.Action.valueOf(action);
    }
    
    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerId) {
        this.consumerKey = consumerId;
    }
    
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setState(String state) {
        this.state = State.valueOf(state);
    }
    
    public Long getTweetId() {
        return tweetId;
    }

    public void setTweetId(Long tweetId) {
        this.tweetId = tweetId;
    }

    /*** Demand ***/
    
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
        if (expirationDate.getTime() < DateUtils.getNowDate().getTime()) {
            throw new IllegalArgumentException("Expiration date cannot be in the past");
        }
		this.expirationDate = expirationDate;
	}

	public void setExpirationDate(int delayInDays) {
		Calendar limit = DateUtils.getNowCalendar();
		limit.set(Calendar.DAY_OF_MONTH, limit.get(Calendar.DAY_OF_MONTH) + delayInDays);
		this.expirationDate = limit.getTime();
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
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
        /*** Command ***/
        out.put(ACTION, getAction().toString());
        out.put(CONSUMER_KEY, getConsumerKey());
        out.put(STATE, getState().toString());
        out.put(TWEET_ID, getTweetId());
		/*** Demand ***/
        if (getCriteria() != null) {
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
		}
        out.put(COUNTRY_CODE, getCountryCode());
		out.put(CRITERIA, jsonArray);
		out.put(EXPIRATION_DATE, DateUtils.dateToISO(getExpirationDate()));
		out.put(LATITUDE, getLatitude());
		out.put(LONGITUDE, getLongitude());
		out.put(POSTAL_CODE, getPostalCode());
        out.put(QUANTITY, getQuantity());
        out.put(RANGE, getRange());
        out.put(RANGE_UNIT, getRangeUnit());
		return out;
	}

	public TransferObject fromJson(JsonObject in) {
	    super.fromJson(in);
	    /*** Command ***/
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(STATE)) { setState(in.getString(STATE)); }
        if (in.containsKey(TWEET_ID)) { setTweetId(in.getLong(TWEET_ID)); }
        /*** Demand ***/
		if (in.containsKey(CRITERIA)) {
			JsonArray jsonArray = in.getJsonArray(CRITERIA);
			for (int i=0; i<jsonArray.size(); ++i) {
				addCriterion((String) jsonArray.getString(i));
			}
		}
		boolean resetCurrentLocation = false;
        resetCurrentLocation = resetCurrentLocation || in.containsKey(COUNTRY_CODE) && in.getString(COUNTRY_CODE).equals(getCountryCode());
        resetCurrentLocation = resetCurrentLocation || in.containsKey(POSTAL_CODE) && in.getString(POSTAL_CODE).equals(getPostalCode());
        resetCurrentLocation = resetCurrentLocation || in.containsKey(LATITUDE) && in.getString(LATITUDE).equals(getLatitude());
        resetCurrentLocation = resetCurrentLocation || in.containsKey(LONGITUDE) && in.getString(LONGITUDE).equals(getLongitude());
        if (resetCurrentLocation) {
            setCountryCode(null);
            setPostalCode(null);
            setLatitude(-1.0D);
            setLongitude(-1.0D);
        }
        if (in.containsKey(COUNTRY_CODE)) { setCountryCode(in.getString(COUNTRY_CODE)); }
		if (in.containsKey(EXPIRATION_DATE)) {
            try {
                Date expirationDate = DateUtils.isoToDate(in.getString(EXPIRATION_DATE));
                setExpirationDate(expirationDate);
            }
            catch (ParseException e) {
                setExpirationDate(7); // Default to an expiration 7 days in the future
            }
	    }
		if (in.containsKey(LATITUDE)) { setLatitude(in.getDouble(LATITUDE)); }
		if (in.containsKey(LONGITUDE)) { setLongitude(in.getDouble(LONGITUDE)); }
        if (in.containsKey(POSTAL_CODE)) { setPostalCode(in.getString(POSTAL_CODE)); }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }
        if (in.containsKey(RANGE)) { setRange(in.getDouble(RANGE)); }
        if (in.containsKey(RANGE_UNIT)) { setRangeUnit(in.getString(RANGE_UNIT)); }
        
        return this;
	}
	
	public void checkForCompletion() {
	    if (getState() == State.open) {
	        if (getCriteria() != null && 0 < getCriteria().size()) {
	            if (getExpirationDate() != null) {
	                if (getRange() != null && 5.0D <= getRange().doubleValue()) {
	                    if (getCountryCode() != null && getPostalCode() != null || getLatitude() != null && getLongitude() != null) {
	                        if (getQuantity() != null && 0L < getQuantity().longValue()) {
	                            setState(State.open);
	                        }
	                    }
	                }
	            }
	        }
	    }
	}
}
