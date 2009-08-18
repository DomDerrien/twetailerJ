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
public class Wish extends Entity {
	
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

    /*** Wish ***/
    
    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public static final String CRITERIA = "criteria";
    
	@Persistent
	private Date expirationDate;

    public static final String EXPIRATION_DATE = "expirationDate";

    @Persistent
    private Long quantity;

    public static final String QUANTITY = "quantity";

	/**
	 * Delay for the default expiration of a demand
	 */
	public final static int DEFAULT_EXPIRATION_DELAY = 7;
	
    /** Default constructor */
    public Wish() {
        super();
    }

    /**
	 * Creates a demand
	 * 
	 * @param in HTTP request parameters
	 */
	public Wish(JsonObject parameters) {
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
    
	public List<String> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<String> criteria) {
		this.criteria = criteria;
	}

	public void addCriterion(String criterion) {
        if (!criteria.contains(criterion)) {
            criteria.add(criterion);
        }
	}
    
    public void resetCriteria() {
        int idx = criteria.size();
        while (0 < idx) {
            --idx;
            this.criteria.remove(idx);
        }
    }

    public void removeCriterion(String criterion) {
        criteria.remove(criterion);
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

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
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
		out.put(CRITERIA, jsonArray);
		out.put(EXPIRATION_DATE, DateUtils.dateToISO(getExpirationDate()));
        out.put(QUANTITY, getQuantity());
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
		if (in.containsKey(EXPIRATION_DATE)) {
            try {
                Date expirationDate = DateUtils.isoToDate(in.getString(EXPIRATION_DATE));
                setExpirationDate(expirationDate);
            }
            catch (ParseException e) {
                setExpirationDate(30); // Default to an expiration 30 days in the future
            }
	    }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }
        
        return this;
	}
}
