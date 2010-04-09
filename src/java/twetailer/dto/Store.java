package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

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
    private Long locationKey;

    public final static String LOCATION_KEY = Location.LOCATION_KEY;

    @Persistent
    private String name;

    public final static String NAME = "name";

    // Shortcut
    public static final String STORE_KEY = "storeKey";

    @Persistent
    private String phoneNumber;

    public final static String PHONE_NUMBER = "phoneNb";

    /** Default constructor */
    public Store() {
        super();
    }

    /**
     * Creates a consumer
     *
     * @param in HTTP request parameters
     */
    public Store(JsonObject in) {
        super();
        fromJson(in);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null || address.length() == 0 ? null : address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // Normalize the email address because it's case insensitive
        this.email = email == null || email.length() == 0 ? null : email.toLowerCase();
    }

    public Long getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(Long locationKey) {
        if (locationKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'locationKey'");
        }
        this.locationKey = locationKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null || name.length() == 0 ? null : name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null || phoneNumber.length() == 0 ? null : phoneNumber;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(ADDRESS, getAddress());
        out.put(EMAIL, getEmail());
        if (getLocationKey() != null) {
            out.put(LOCATION_KEY, getLocationKey());
        }
        out.put(NAME, getName());
        out.put(PHONE_NUMBER, getPhoneNumber());
        out.put(STORE_KEY, getKey());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(ADDRESS)) { setAddress(in.getString(ADDRESS)); }
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }

        // Shortcut
        if (in.containsKey(STORE_KEY)) {setKey(in.getLong(STORE_KEY)); }

        return this;
    }
}
