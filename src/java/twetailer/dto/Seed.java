package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.KeyFactory;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Seed implements TransferObject {

    @Persistent
    String city;

    public static final String CITY = "city";

    @Persistent
    String country;

    public static final String COUNTRY = "country";

    @Persistent
    @PrimaryKey
    String key;

    public static final String KEY = "key";

    @Persistent
    String label;

    public static final String LABEL = "label";

    @Persistent
    Long locationKey;

    public static final String LOCATION_KEY = Location.LOCATION_KEY;

    public static final String QUERY_KEY = "queryKey";

    @Persistent
    String region;

    public static final String REGION = "region";

    @Persistent
    Long storeKey;

    public static final String STORE_KEY = Store.STORE_KEY;

    /** Default constructor */
    public Seed(String country, String region, String city, String label, Long storeKey) {
        setCountry(country);
        setRegion(region);
        setCity(city);
        setLabel(label);
        setStoreKey(storeKey);
    }

    public Seed(JsonObject in) {
        fromJson(in);
    }

    public String buildQueryString() {
        return buildQueryString(country, region, city);
    }

    public static String buildQueryString(String country, String region, String city) {
        return "/" + country + "/" + region + "/" + city;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null || city.length() == 0 ? null : city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null || country.length() == 0 ? null : country;
    }

    public String getKey() {
        return key;
    }

    public void generateKey() {
        setKey(generateKey(buildQueryString()));
    }

    public static String generateKey(String queryString) {
        return KeyFactory.createKeyString(Seed.class.getSimpleName(), queryString);
    }

    public void setKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'key'");
        }
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label == null || label.length() == 0 ? null : label;
    }

    public Long getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(Long locationKey) {
        this.locationKey = locationKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region == null || region.length() == 0 ? null : region;
    }

    public Long getStoreKey() {
        return storeKey;
    }

    public void setStoreKey(Long storeKey) {
        if (storeKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'storeKey'");
        }
        this.storeKey = storeKey;
    }

    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        out.put(CITY, getCity());
        out.put(COUNTRY, getCountry());
        out.put(KEY, getKey());
        out.put(LABEL, getLabel());
        out.put(LOCATION_KEY, getLocationKey());
        out.put(QUERY_KEY, buildQueryString());
        out.put(REGION, getRegion());
        out.put(STORE_KEY, getStoreKey());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        if (in.containsKey(CITY)) { setCity(in.getString(CITY)); }
        if (in.containsKey(COUNTRY)) { setCountry(in.getString(COUNTRY)); }
        if (in.containsKey(KEY)) { setKey(in.getString(KEY)); }
        if (in.containsKey(LABEL)) { setLabel(in.getString(LABEL)); }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(REGION)) { setRegion(in.getString(REGION)); }
        if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
        return this;
    }
}
