package com.twetailer.dto;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.domderrien.i18n.DateUtils;
import org.domderrien.jsontools.GenericJsonObject;
import org.domderrien.jsontools.JsonObject;

public class Command {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    public static final String KEY = "key";
    
    @Persistent
    private Long consumerKey;

    public static final String CONSUMER_KEY = "consumerKey";
    
    @Persistent
    private Date creationDate;

    public static final String CREATION_DATE = "creationDate";

    public enum Action {
        demand,
        supply,
        shop,
        wish,
        propose,
        confirm,
        decline,
        cancel,
        close,
        list,
        www
    }

    private Action action;
    
    public static final String ACTION = "action";
    
    public Command() {
        setCreationDate(getNowDate());
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
    
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setAction(String action) {
        this.action = Action.valueOf(action);
    }

    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        out.put(KEY, getKey());
        out.put(ACTION, getAction().toString());
        out.put(CREATION_DATE, DateUtils.dateToISO(getCreationDate()));
        out.put(CONSUMER_KEY, getConsumerKey());
        return out;
    }

    public void fromJson(JsonObject in) throws ParseException {
        if (in.containsKey(KEY)) { setKey(in.getLong(KEY)); }
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        // if (in.containsKey(CREATION_DATE)) { setCreationDate(DateUtil.isoToDate(in.getString(CREATION_DATE))); }
    }
}
