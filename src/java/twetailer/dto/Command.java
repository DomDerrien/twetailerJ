package twetailer.dto;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public class Command extends Entity {

    @Persistent
    private Action action;

    public static final String ACTION = "action";

    @Persistent
    private List<String> hashTags = new ArrayList<String>();

    public static final String HASH_TAG = "hasTag";
    public static final String HASH_TAG_ADD = "\\+hasTag";
    public static final String HASH_TAG_REMOVE = "\\-hasTag";

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

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     */
    protected void resetLists() {
        hashTags = null;
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

    public String getSerializedHashTags() {
        return getSerializedTags(HASH, hashTags);
    }

    public List<String> getHashTags() {
        return hashTags;
    }

    public void setHashTags(List<String> hashTags) {
        if (hashTags == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'hashTags' of type List<String>");
        }
        this.hashTags = hashTags;
    }

    public void addHashTag(String hashTag) {
        if (hashTags == null) {
            hashTags = new ArrayList<String>();
        }
        if (!hashTags.contains(hashTag)) {
            hashTags.add(hashTag);
        }
    }

    public void resetHashTags() {
        if (hashTags == null) {
            return;
        }
        hashTags = new ArrayList<String>();
    }

    public void removeHashTag(String hashTag) {
        if (hashTags == null) {
            return;
        }
        hashTags.remove(hashTag);
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
        if (getHashTags() != null && 0 < getHashTags().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String hashTag: getHashTags()) {
                jsonArray.add(hashTag);
            }
            out.put(HASH_TAG, jsonArray);
        }
        if (getOwnerKey() != null) { out.put(OWNER_KEY, getOwnerKey()); }
        if (getRawCommandId() != null) { out.put(RAW_COMMAND_ID, getRawCommandId()); }
        out.put(SOURCE, getSource().toString());
        out.put(STATE, getState().toString());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        if (in.containsKey(HASH_TAG)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAG);
            resetHashTags();
            for (int i=0; i<jsonArray.size(); ++i) {
                addHashTag(jsonArray.getString(i));
            }
        }
        Demand.removeDuplicates(in, HASH_TAG_ADD, HASH_TAG_REMOVE);
        if (in.containsKey(HASH_TAG_REMOVE)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAG_REMOVE);
            for (int i=0; i<jsonArray.size(); ++i) {
                removeHashTag(jsonArray.getString(i));
            }
        }
        if (in.containsKey(HASH_TAG_ADD)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAG_ADD);
            for (int i=0; i<jsonArray.size(); ++i) {
                addHashTag(jsonArray.getString(i));
            }
        }
        if (in.containsKey(OWNER_KEY)) { setOwnerKey(in.getLong(OWNER_KEY)); }
        if (in.containsKey(RAW_COMMAND_ID)) { setRawCommandId(in.getLong(RAW_COMMAND_ID)); }
        if (in.containsKey(SOURCE)) { setSource(in.getString(SOURCE)); }
        if (in.containsKey(STATE)) { setState(in.getString(STATE)); }
        return this;
    }

    public final static String EMPTY_STRING = "";
    public final static String SPACE = " ";
    public final static String HASH = "#";

    public static String getSerializedTags(List<?> keywords) {
        return getSerializedTags(null, keywords);
    }

    public static String getSerializedTags(String prefix, List<?> keywords) {
        if (keywords.size() == 0) {
            return EMPTY_STRING;
        }
        StringBuilder out = new StringBuilder();
        if (prefix == null) {
            for(Object keyword: keywords) {
                out.append(keyword.toString()).append(SPACE);
            }
        }
        else {
            for(Object keyword: keywords) {
                out.append(prefix).append(keyword.toString()).append(SPACE);
            }
        }
        out.setLength(out.length() - 1); // To remove the trailing space
        return out.toString();
    }
}
