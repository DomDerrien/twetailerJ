package com.twetailer.dto;

import java.util.Calendar;
import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Store extends Entity {
    
    @Persistent
    private String address;
    
    public final static String ADDRESS = "address";
    
    @Persistent
    private String email;
    
    public final static String EMAIL = "email";
    
    @Persistent
    private String name;
    
    public final static String NAME = "name";

    @Persistent
    private String phoneNumber;
    
    public final static String PHONE_NUMBER = "phoneNb";
    
    @Persistent
    private Double latitude;

    public static final String LATITUDE = "latitude";
    
    @Persistent
    private Double longitude;

    public static final String LONGITUDE = "longitude";
    
    @Persistent
    private String countryCode;

    public static final String COUNTRY_CODE = "countryCode";
    
    @Persistent
    private String postalCode;

    public static final String POSTAL_CODE = "postalCode";
    
    public Store() {
        super();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(ADDRESS, getAddress());
        out.put(EMAIL, getEmail());
        out.put(NAME, getName());
        out.put(PHONE_NUMBER, getPhoneNumber());
        out.put(LATITUDE, getLatitude());
        out.put(LONGITUDE, getLongitude());
        out.put(COUNTRY_CODE, getCountryCode());
        out.put(POSTAL_CODE, getPostalCode());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(ADDRESS)) { setAddress(in.getString(ADDRESS)); }
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
        if (in.containsKey(LATITUDE)) { setLatitude(in.getDouble(LATITUDE)); }
        if (in.containsKey(LONGITUDE)) { setLongitude(in.getDouble(LONGITUDE)); }
        if (in.containsKey(COUNTRY_CODE)) { setCountryCode(in.getString(COUNTRY_CODE)); }
        if (in.containsKey(POSTAL_CODE)) { setPostalCode(in.getString(POSTAL_CODE)); }
        return this;
    }
}
