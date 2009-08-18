package com.twetailer.dto;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Settings {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    public static final String KEY = "key";

    @Persistent
    private String name;
    
    public static final String NAME = "name";
    
    @Persistent
    private Long lastProcessDirectMessageId;
    
    public static final String LAST_PROCESSED_DIRECT_MESSAGE_ID = "lastProcessDirectMessageId";
    
    public static final String APPLICATION_SETTINGS_ID = "appSettings";

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLastProcessDirectMessageId() {
        return lastProcessDirectMessageId;
    }

    public void setLastProcessDirectMessageId(Long lastProcessDirectMessageId) {
        this.lastProcessDirectMessageId = lastProcessDirectMessageId;
    }
    
    public Settings() {
        setName(APPLICATION_SETTINGS_ID);
        setLastProcessDirectMessageId(1L);
    }

    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        out.put(KEY, getKey());
        out.put(NAME, getName());
        out.put(LAST_PROCESSED_DIRECT_MESSAGE_ID, getLastProcessDirectMessageId());
        return out;
    }

    public void fromJson(JsonObject in) {
        // if (in.containsKey(KEY)) { setKey(in.getLong(KEY)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(LAST_PROCESSED_DIRECT_MESSAGE_ID)) { setLastProcessDirectMessageId(in.getLong(LAST_PROCESSED_DIRECT_MESSAGE_ID)); }
    }
}
