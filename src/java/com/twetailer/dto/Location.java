package com.twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Location extends Entity {
    
    @Persistent
    private String countryCode;

    public static final String COUNTRY_CODE = "countryCode";
    
    public static final Double INVALID_COORDINATE = Double.valueOf(-1.0D);

    @Persistent
    private Double latitude = INVALID_COORDINATE;

    public static final String LATITUDE = "latitude";
    
    @Persistent
    private Double longitude = INVALID_COORDINATE;

    public static final String LONGITUDE = "longitude";

    @Persistent
    private String postalCode;

    public static final String POSTAL_CODE = "postalCode";

    @Persistent
    private Boolean hasStore = Boolean.FALSE;

    public static final String HAS_STORE = "hasStore";
    
    /** Default constructor */
    public Location() {
        super();
    }

    /**
     * Creates a demand
     * 
     * @param in HTTP request parameters
     */
    public Location(JsonObject parameters) {
        this();
        fromJson(parameters);
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        if (90.0D < latitude || latitude < -90.0D) {
            latitude = INVALID_COORDINATE;
        }
        this.latitude = latitude;
    }

    public Double getLongitude() {
        if (180.0D < longitude || longitude < -180.0D) {
            longitude = INVALID_COORDINATE;
        }
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
        if(this.postalCode != null) {
            this.postalCode = this.postalCode.replaceAll("\\s", "");
        }
    }

    public Boolean hasStore() {
        return hasStore;
    }

    public void setHasStore(Boolean hasStore) {
        this.hasStore = hasStore;
    }

    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject(); // super.toJson();
        out.put(COUNTRY_CODE, getCountryCode());
        out.put(LATITUDE, getLatitude());
        out.put(LONGITUDE, getLongitude());
        out.put(POSTAL_CODE, getPostalCode());
        out.put(HAS_STORE, hasStore());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        // super.fromJson(in);

        boolean resetCurrentLocation = false;
        resetCurrentLocation = resetCurrentLocation || in.containsKey(COUNTRY_CODE) && !in.getString(COUNTRY_CODE).equals(getCountryCode());
        resetCurrentLocation = resetCurrentLocation || in.containsKey(POSTAL_CODE) && !in.getString(POSTAL_CODE).equals(getPostalCode());
        resetCurrentLocation = resetCurrentLocation || in.containsKey(LATITUDE) && !in.getString(LATITUDE).equals(getLatitude());
        resetCurrentLocation = resetCurrentLocation || in.containsKey(LONGITUDE) && !in.getString(LONGITUDE).equals(getLongitude());
        if (resetCurrentLocation) {
            setCountryCode(null);
            setPostalCode(null);
            setLatitude(INVALID_COORDINATE);
            setLongitude(INVALID_COORDINATE);
        }
        
        if (in.containsKey(COUNTRY_CODE)) { setCountryCode(in.getString(COUNTRY_CODE)); }
        if (in.containsKey(LATITUDE)) { setLatitude(in.getDouble(LATITUDE)); }
        if (in.containsKey(LONGITUDE)) { setLongitude(in.getDouble(LONGITUDE)); }
        if (in.containsKey(POSTAL_CODE)) { setPostalCode(in.getString(POSTAL_CODE)); }
        if (in.containsKey(HAS_STORE)) { setHasStore(in.getBoolean(HAS_STORE)); }
        
        return this;
    }

}