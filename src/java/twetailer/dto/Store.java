package twetailer.dto;

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
    private Long locationKey;
    
    public final static String LOCATION_KEY = "locationKey";
    
    @Persistent
    private String name;
    
    public final static String NAME = "name";
    
    public static final String STORE_KEY = "storeKey";

    @Persistent
    private String phoneNumber;
    
    public final static String PHONE_NUMBER = "phoneNb";
    
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

    public Long getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(Long locationKey) {
        this.locationKey = locationKey;
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

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(ADDRESS, getAddress());
        out.put(EMAIL, getEmail());
        out.put(LOCATION_KEY, getLocationKey());
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
        if (in.containsKey(STORE_KEY)) {setKey(in.getLong(STORE_KEY)); }
        return this;
    }
}
