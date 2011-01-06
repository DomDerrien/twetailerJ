package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a Twetailer registrator which takes care of the
 * retailer registration, training, support, etc., with the system
 *
 * @see twetailer.dto.Entity
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Registrar extends Entity {

    @Persistent
    private Long consumerKey;

    public final static String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private String email;

    public final static String EMAIL = Consumer.EMAIL;

    @Persistent
    private String name;

    public final static String NAME = Store.NAME;

    // Shortcut
    public static final String REGISTRAR_KEY = "registrarKey";

    @Persistent
    private String url;

    public final static String URL = Store.URL;

    /** Default constructor */
    public Registrar() {
        super();
    }

    /**
     * Creates an Registrar
     *
     * @param in HTTP request parameters
     */
    public Registrar(JsonObject in) {
        super();
        fromJson(in);
    }

     public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerKey) {
        this.consumerKey = consumerKey;
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
        out.put(CONSUMER_KEY, getConsumerKey());
        out.put(EMAIL, getEmail());
        out.put(NAME, getName());
        out.put(URL, getUrl());
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        return fromJson(in, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin) {
        if (!isUserAdmin) {
            // No external update allowed
            return this;
        }

        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(URL)) { setUrl(in.getString(URL)); }

        // Shortcut
        if (in.containsKey(REGISTRAR_KEY)) {setKey(in.getLong(REGISTRAR_KEY)); }

        return this;
    }
}
