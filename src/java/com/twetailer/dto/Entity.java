package com.twetailer.dto;

import java.text.ParseException;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Entity implements TransferObject {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    public static final String KEY = "key";

    @Persistent
    private Date creationDate = DateUtils.getNowDate();

    public static final String CREATION_DATE = "creationDate";
    
    @Persistent
    private Date modificationDate = DateUtils.getNowDate();

    public static final String MODIFICATION_DATE = "modificationDate";
    
    public Entity() {
        setCreationDate(DateUtils.getNowDate());
        setModificationDate(DateUtils.getNowDate());
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        if (this.key == null) {
            this.key = key;
        }
        else if (!this.key.equals(key)) {
            throw new IllegalArgumentException("Cannot override the key of an object with a new one");
        }
    }

    public void resetKey() {
        this.key = null;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        if (creationDate == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute “creationDate” of type Date reference");
        }
        this.creationDate = creationDate;
    }
    
    public void resetCoreDates() {
        setCreationDate(DateUtils.getNowDate());
        setModificationDate(getCreationDate());
    }
    
    public Date getModificationDate() {
        return modificationDate;
    }

    public void updateModificationDate() {
        setModificationDate(DateUtils.getNowDate());
    }

    public void setModificationDate(Date modificationDate) {
        if (modificationDate == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute “modificationDate” of type Date reference");
        }
        this.modificationDate = modificationDate;
    }
    
    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        out.put(KEY, getKey());
        out.put(CREATION_DATE, DateUtils.dateToISO(getCreationDate()));
        out.put(MODIFICATION_DATE, DateUtils.dateToISO(getModificationDate()));
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        if (in.containsKey(KEY)) {
            Long inKey = in.getLong(KEY);
            setKey(inKey == 0L ? null : inKey);
        }
        if (in.containsKey(CREATION_DATE)) {
            Date creationDate;
            try {
                creationDate = DateUtils.isoToDate(in.getString(CREATION_DATE));
                setCreationDate(creationDate);
            }
            catch (ParseException e) {
                // Ignored error, the date stays not set
            }
        }
        updateModificationDate();
        return this;
    }
}
