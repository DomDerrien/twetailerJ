package twetailer.dto;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the application settings as stored in the back-end storage.
 * Has only one instance!
 *
 * @see twetailer.j2ee.MaelzelServlet
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Settings extends Entity implements Serializable, TransferObject {

    /** Required to be able to save the settings into the cache */
    private static final long serialVersionUID = -7877518547014483177L;

    @Persistent
    private String name;

    public static final String NAME = "name";

    @Persistent
    private Long lastProcessDirectMessageId = 1L;

    public static final String LAST_PROCESSED_DIRECT_MESSAGE_ID = "lastProcessDirectMessageId";

    public static final String APPLICATION_SETTINGS_ID = "appSettings";

    @Persistent
    private Long robotConsumerKey = null;

    @Persistent
    private Long robotSaleAssociateKey = null;

    /** Default constructor */
    public Settings() {
        super();
        setName(APPLICATION_SETTINGS_ID);
    }

    /**
     * Creates a retailer
     *
     * @param in HTTP request parameters
     */
    public Settings(JsonObject in) {
        this();
        fromJson(in);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'name'");
        }
        this.name = name;
    }

    public Long getLastProcessDirectMessageId() {
        return lastProcessDirectMessageId;
    }

    public void setLastProcessDirectMessageId(Long lastProcessDirectMessageId) {
        if (lastProcessDirectMessageId == null) {
            throw new IllegalArgumentException("Cannot nullify the message identifier");
        }
        if (lastProcessDirectMessageId < 1L) {
            throw new IllegalArgumentException("Cannot assign 0L or a negative value to the message identifier");
        }
        this.lastProcessDirectMessageId = lastProcessDirectMessageId;
    }

    public Long getRobotConsumerKey() {
        return robotConsumerKey;
    }

    // Just made available for the unit tests
    public void setRobotConsumerKey(Long robotConsumerKey) {
        this.robotConsumerKey = robotConsumerKey;
    }

    public Long getRobotSaleAssociateKey() {
        return robotSaleAssociateKey;
    }

    // Just made available for the unit tests
    public void setRobotSaleAssociateKey(Long robotSaleAssociateKey) {
        this.robotSaleAssociateKey = robotSaleAssociateKey;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();

        out.put(NAME, getName());
        out.put(LAST_PROCESSED_DIRECT_MESSAGE_ID, getLastProcessDirectMessageId());

        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        return fromJson(in, false, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin, boolean isCacheRelated) {
        isUserAdmin = isUserAdmin || isCacheRelated;
        super.fromJson(in, isUserAdmin, isCacheRelated);

        setName(in.getString(NAME));
        setLastProcessDirectMessageId(in.getLong(LAST_PROCESSED_DIRECT_MESSAGE_ID));

        return this;
    }
}
