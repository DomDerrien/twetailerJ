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

import com.twetailer.settings.CommandSettings;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Command {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    public static final String KEY = "key";

    @Persistent
    private CommandSettings.Action action;
    
    public static final String ACTION = "action";
    
    @Persistent
    private Long consumerKey = 0L;

    public static final String CONSUMER_KEY = "consumerKey";
    
    @Persistent
    private Date creationDate = DateUtils.getNowDate();

    public static final String CREATION_DATE = "creationDate";
    
    @Persistent
    private Date modificationDate = DateUtils.getNowDate();

    public static final String MODIFICATION_DATE = "modificationDate";

    public enum State {
        incomplete,
        completed,
        processed,
        cancelled
    }

    @Persistent
    private State state = State.incomplete;
    
    public static final String STATE = "state";

    @Persistent
    private Long tweetId;
    
    public static final String TWEET_ID = "tweetId";
    
    public Command() {
        setCreationDate(getNowDate());
        setModificationDate(getNowDate());
    }

    protected Calendar getNowCalendar() {
        return DateUtils.getNowCalendar();
    }

    protected Date getNowDate() {
        return getNowCalendar().getTime();
    }
    
    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerId) {
        this.consumerKey = consumerId;
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
    
    public Date getModificationDate() {
        if (modificationDate == null) {
            setModificationDate(getNowDate());
        }
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        if (modificationDate == null) {
            throw new IllegalArgumentException("Non null Date instance required");
        }
        this.modificationDate = modificationDate;
    }
    
    public CommandSettings.Action getAction() {
        return action;
    }

    public void setAction(CommandSettings.Action action) {
        this.action = action;
    }

    public void setAction(String action) {
        this.action = CommandSettings.Action.valueOf(action);
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

    public static String getAttributeLabel(CommandSettings.Prefix prefix) {
        if (prefix == CommandSettings.Prefix.action) return ACTION;
        if (prefix == CommandSettings.Prefix.reference) return KEY;
        return null;
    }

    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        out.put(KEY, getKey());
        out.put(ACTION, getAction().toString());
        out.put(CONSUMER_KEY, getConsumerKey());
        out.put(CREATION_DATE, DateUtils.dateToISO(getCreationDate()));
        out.put(MODIFICATION_DATE, DateUtils.dateToISO(getModificationDate()));
        out.put(STATE, getState().toString());
        out.put(TWEET_ID, getTweetId());
        return out;
    }

    public void fromJson(JsonObject in) throws ParseException {
        if (in.containsKey(KEY)) { setKey(in.getLong(KEY)); }
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        // if (in.containsKey(CREATION_DATE)) { setCreationDate(DateUtils.isoToDate(in.getString(CREATION_DATE))); }
        // if (in.containsKey(MODIFICATION_DATE)) { setModificationDate(DateUtils.isoToDate(in.getString(MODIFICATION_DATE))); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(STATE)) { setState(in.getString(STATE)); }
        // if (in.containsKey(TWEET_ID)) { setTweetId(in.getLong(TWEET_ID)); }
    }
}
