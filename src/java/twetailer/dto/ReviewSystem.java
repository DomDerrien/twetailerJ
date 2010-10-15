package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a third-party review system
 *
 * @see twetailer.dto.Entity
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class ReviewSystem extends Entity {

    @Persistent
    private String email;

    public final static String EMAIL = Consumer.EMAIL;

    @Persistent
    private String name;

    public final static String NAME = Store.NAME;

    // Shortcut
    public static final String REVIEW_SYSTEM_KEY = "reviewSystemKey";

    @Persistent
    private String url;

    public final static String URL = Store.URL;

    /** Default constructor */
    public ReviewSystem() {
        super();
    }

    /**
     * Creates an ReviewSystem
     *
     * @param in HTTP request parameters
     */
    public ReviewSystem(JsonObject in) {
        super();
        fromJson(in);
    }

     public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // Normalize the email address because it's case insensitive
        this.email = email == null || email.length() == 0 ? null : email.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null || name.length() == 0 ? null : name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(EMAIL, getEmail());
        out.put(NAME, getName());
        out.put(URL, getUrl());
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(URL)) { setUrl(in.getString(URL)); }

        // Shortcut
        if (in.containsKey(REVIEW_SYSTEM_KEY)) {setKey(in.getLong(REVIEW_SYSTEM_KEY)); }

        return this;
    }
}
