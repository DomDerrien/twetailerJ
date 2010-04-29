package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.validator.LocaleValidator;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Location extends Entity {

    public static final Double INVALID_COORDINATE = Double.valueOf(-1000.0D);

    @Persistent
    private String countryCode;

    public static final String COUNTRY_CODE = "countryCode";

    @Persistent
    private Boolean hasStore = Boolean.FALSE;

    public static final String HAS_STORE = "hasStore";

    @Persistent
    private Double latitude = INVALID_COORDINATE;

    public static final String LATITUDE = "latitude";

    // Shortcut
    public final static String LOCATION_KEY = "locationKey";

    @Persistent
    private Double longitude = INVALID_COORDINATE;

    public static final String LONGITUDE = "longitude";

    @Persistent
    private String postalCode;

    public static final String POSTAL_CODE = "postalCode";

    /** Default constructor */
    public Location() {
        super();
    }

    /**
     * Creates a demand
     *
     * @param in HTTP request parameters
     */
    public Location(JsonObject in) {
        this();
        fromJson(in);
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = LocaleValidator.checkCountryCode(countryCode);
    }

    public boolean hasStore() {
        return hasStore == null ? Boolean.FALSE : hasStore;
    }

    public Boolean getHasStore() {
        return hasStore;
    }

    public void setHasStore(Boolean hasStore) {
        this.hasStore = hasStore;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        if (latitude == null || 90.0D < latitude || latitude < -90.0D) {
            latitude = INVALID_COORDINATE;
        }
        this.latitude = latitude;
    }

    public Double getLongitude() {
        if (longitude == null || 180.0D < longitude || longitude < -180.0D) {
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
        //
        // FIXME: Be sure that the given postal is in upper case for the country's locale?
        //
        this.postalCode = postalCode;
        if(this.postalCode != null) {
            this.postalCode = this.postalCode.replaceAll("\\s", "").replaceAll("\\-", "").toUpperCase();
        }
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(COUNTRY_CODE, getCountryCode());
        out.put(HAS_STORE, getHasStore());
        out.put(LATITUDE, getLatitude());
        out.put(LONGITUDE, getLongitude());
        out.put(POSTAL_CODE, getPostalCode());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);

        if (hasNewAttributes(in)) {
            setCountryCode(null);
            setPostalCode(null);
            setLatitude(INVALID_COORDINATE);
            setLongitude(INVALID_COORDINATE);
        }

        if (in.containsKey(COUNTRY_CODE)) { setCountryCode(in.getString(COUNTRY_CODE)); }
        if (in.containsKey(HAS_STORE)) { setHasStore(in.getBoolean(HAS_STORE)); }
        if (in.containsKey(LATITUDE)) { setLatitude(in.getDouble(LATITUDE)); }
        if (in.containsKey(LONGITUDE)) { setLongitude(in.getDouble(LONGITUDE)); }
        if (in.containsKey(POSTAL_CODE)) { setPostalCode(in.getString(POSTAL_CODE)); }

        // Shortcut
        if (in.containsKey(LOCATION_KEY)) {setKey(in.getLong(LOCATION_KEY)); }

        return this;
    }

    public boolean hasNewAttributes(JsonObject in) {
        boolean hasNewAttribute = false;
        // Country code is an ISO 2-letters code
        hasNewAttribute = in.containsKey(COUNTRY_CODE) && !in.getString(COUNTRY_CODE).equals(getCountryCode());
        // Postal code has been upper cased and comparison can be done bit-to-bit
        hasNewAttribute = hasNewAttribute || in.containsKey(POSTAL_CODE) && !in.getString(POSTAL_CODE).equals(getPostalCode());
        hasNewAttribute = hasNewAttribute || in.containsKey(LATITUDE) && !getLatitude().equals(in.getDouble(LATITUDE));
        hasNewAttribute = hasNewAttribute || in.containsKey(LONGITUDE) && !getLongitude().equals(in.getDouble(LONGITUDE));
        return hasNewAttribute;
    }

    private static Location cleanBase = new Location();

    public static boolean hasAttributeForANewLocation(JsonObject in) {
        return cleanBase.hasNewAttributes(in);
    }
}
