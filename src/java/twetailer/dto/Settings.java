package twetailer.dto;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Settings implements Serializable {

    /** Required to be able to save the settings into the cache */
    private static final long serialVersionUID = -7877518547014483177L;

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    public static final String KEY = "key";

    @Persistent
    private String name;

    public static final String NAME = "name";

    @Persistent
    private Long lastProcessDirectMessageId = 1L;

    public static final String LAST_PROCESSED_DIRECT_MESSAGE_ID = "lastProcessDirectMessageId";

    @Persistent
    private Long lastRobotDirectMessageId = 1L;

    public static final String LAST_ROBOT_DIRECT_MESSAGE_ID = "lastRobotDirectMessageId";

    public static final String APPLICATION_SETTINGS_ID = "appSettings";

    /** Default constructor */
    public Settings() {
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

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
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

    public Long getLastRobotDirectMessageId() {
        return lastRobotDirectMessageId;
    }

    public void setLastRobotDirectMessageId(Long lastRobotProcessDirectMessageId) {
        if (lastRobotProcessDirectMessageId == null) {
            throw new IllegalArgumentException("Cannot nullify the message identifier");
        }
        if (lastRobotProcessDirectMessageId < 1L) {
            throw new IllegalArgumentException("Cannot assign 0L or a negative value to the message identifier");
        }
        this.lastRobotDirectMessageId = lastRobotProcessDirectMessageId;
    }

    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        if (getKey() != null) {
            out.put(KEY, getKey());
        }
        out.put(NAME, getName());
        out.put(LAST_PROCESSED_DIRECT_MESSAGE_ID, getLastProcessDirectMessageId());
        out.put(LAST_ROBOT_DIRECT_MESSAGE_ID, getLastRobotDirectMessageId());
        return out;
    }

    public void fromJson(JsonObject in) {
        if (in.containsKey(KEY)) { setKey(in.getLong(KEY)); }
        setName(in.getString(NAME));
        setLastProcessDirectMessageId(in.getLong(LAST_PROCESSED_DIRECT_MESSAGE_ID));
        setLastRobotDirectMessageId(in.getLong(LAST_ROBOT_DIRECT_MESSAGE_ID));
    }
}
