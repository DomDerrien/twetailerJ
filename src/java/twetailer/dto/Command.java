package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * @author Dom Derrien
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Command extends Entity {

    @Persistent
    private Action action;

    public static final String ACTION = "action";

    @Persistent
    private String hashTag;

    public static final String HASH_TAG = "hasTag";

    @Persistent
    private Long ownerKey;

    public static final String OWNER_KEY = "ownerKey";

    public static final String NEED_HELP = "needHelp";

    @Persistent
    private Long rawCommandId;

    public static final String RAW_COMMAND_ID = "rawCommandId";

    @Persistent
    private Source source;

    public static final String SOURCE = "source";

    @Persistent
    private State state = State.opened;

    public static final String STATE = "state";

    /** Default constructor */
    public Command() {
        super();
    }

    /**
     * Creates a consumer
     *
     * @param in HTTP request parameters
     */
    public Command(JsonObject in) {
        this();
        fromJson(in);
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'action'");
        }
        this.action = action;
    }

    public void setAction(String action) {
        setAction(Action.valueOf(action));
    }

    public String getHashTag() {
        return hashTag;
    }

    public void setHashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    public Long getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(Long ownerKey) {
        this.ownerKey = ownerKey;
    }

    public Long getRawCommandId() {
        return rawCommandId;
    }

    public void setRawCommandId(Long rawCommandId) {
        this.rawCommandId = rawCommandId;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'source'");
        }
        this.source = source;
    }

    public void setSource(String source) {
        setSource(Source.valueOf(source));
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        if (state == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'state'");
        }
        this.state = state;
    }

    public void setState(String state) {
        setState(State.valueOf(state));
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getAction() != null) { out.put(ACTION, getAction().toString()); }
        if (getHashTag() != null) { out.put(HASH_TAG, getHashTag()); }
        if (getOwnerKey() != null) { out.put(OWNER_KEY, getOwnerKey()); }
        if (getRawCommandId() != null) { out.put(RAW_COMMAND_ID, getRawCommandId()); }
        out.put(SOURCE, getSource().toString());
        out.put(STATE, getState().toString());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        if (in.containsKey(HASH_TAG)) { setHashTag(in.getString(HASH_TAG)); }
        if (in.containsKey(OWNER_KEY)) { setOwnerKey(in.getLong(OWNER_KEY)); }
        if (in.containsKey(RAW_COMMAND_ID)) { setRawCommandId(in.getLong(RAW_COMMAND_ID)); }
        if (in.containsKey(SOURCE)) { setSource(in.getString(SOURCE)); }
        if (in.containsKey(STATE)) { setState(in.getString(STATE)); }
        return this;
    }
}
