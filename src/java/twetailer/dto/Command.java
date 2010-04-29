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
    private Long cancelerKey;

    public static final String CANCELER_KEY = "cancelerKey";

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public static final String CRITERIA = "criteria";
    public static final String CRITERIA_ADD = "\\+criteria";
    public static final String CRITERIA_REMOVE = "\\-criteria";

    @Persistent
    private Long locationKey;

    public final static String LOCATION_KEY = Location.LOCATION_KEY;

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

    @Persistent
    private Boolean stateCmdList = Boolean.TRUE;

    public static final String STATE_COMMAND_LIST = "stateCmdList";

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
        criteria = null;
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

    public Long getCancelerKey() {
        return cancelerKey;
    }

    public void setCancelerKey(Long cancelerKey) {
        if (cancelerKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'cancelerKey'");
        }
        this.cancelerKey = cancelerKey;
    }

    public String getSerializedCriteria() {
        return getSerializedTags(criteria);
    }

    public List<String> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<String> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'criteria' of type List<String>");
        }
        this.criteria = criteria;
    }

    public void addCriterion(String criterion) {
        if (criteria == null) {
            criteria = new ArrayList<String>();
        }
        if (!criteria.contains(criterion)) {
            criteria.add(criterion);
        }
    }

    public void resetCriteria() {
        if (criteria == null) {
            return;
        }
        criteria = new ArrayList<String>();
    }

    public void removeCriterion(String criterion) {
        if (criteria == null) {
            return;
        }
        criteria.remove(criterion);
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
        if (ownerKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'ownerKey'");
        }
        this.ownerKey = ownerKey;
    }

    public Long getRawCommandId() {
        return rawCommandId;
    }

    public void setRawCommandId(Long rawCommandId) {
        if (rawCommandId == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'rawCommandId'");
        }
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
        stateCmdList =
            !State.cancelled.equals(state) &&
            !State.closed.equals(state) &&
            !State.declined.equals(state) &&
            !State.markedForDeletion.equals(state);
    }

    public Boolean getStateCmdList() {
        return stateCmdList == null ? Boolean.TRUE : stateCmdList;
    }

    protected void setStateCmdList(Boolean  stateCmdList) {
        this.stateCmdList = stateCmdList;
    }
    public void setState(String state) {
        setState(State.valueOf(state));
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getAction() != null) { out.put(ACTION, getAction().toString()); }
        if (getCancelerKey() != null) { out.put(CANCELER_KEY, getCancelerKey()); }
        if (getCriteria() != null && 0 < getCriteria().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
            out.put(CRITERIA, jsonArray);
        }
        if (getLocationKey() != null) { out.put(LOCATION_KEY, getLocationKey()); }
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
        out.put(STATE_COMMAND_LIST, getStateCmdList());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        if (in.containsKey(CANCELER_KEY)) { setCancelerKey(in.getLong(CANCELER_KEY)); }
        if (in.containsKey(CRITERIA)) {
            JsonArray jsonArray = in.getJsonArray(CRITERIA);
            resetCriteria();
            for (int i=0; i<jsonArray.size(); ++i) {
                addCriterion(jsonArray.getString(i));
            }
        }
        removeDuplicates(in, CRITERIA_ADD, CRITERIA_REMOVE);
        if (in.containsKey(CRITERIA_REMOVE)) {
            JsonArray jsonArray = in.getJsonArray(CRITERIA_REMOVE);
            for (int i=0; i<jsonArray.size(); ++i) {
                removeCriterion(jsonArray.getString(i));
            }
        }
        if (in.containsKey(CRITERIA_ADD)) {
            JsonArray jsonArray = in.getJsonArray(CRITERIA_ADD);
            for (int i=0; i<jsonArray.size(); ++i) {
                addCriterion(jsonArray.getString(i));
            }
        }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(HASH_TAG)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAG);
            resetHashTags();
            for (int i=0; i<jsonArray.size(); ++i) {
                addHashTag(jsonArray.getString(i));
            }
        }
        removeDuplicates(in, HASH_TAG_ADD, HASH_TAG_REMOVE);
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
        if (in.containsKey(STATE_COMMAND_LIST)) { in.getBoolean(STATE_COMMAND_LIST); }
        return this;
    }

    public final static String EMPTY_STRING = "";
    public final static String SPACE = " ";
    public final static String HASH = "#";

    public static String getSerializedTags(List<?> keywords) {
        return getSerializedTags(null, keywords);
    }

    public static String getSerializedTags(String prefix, List<?> keywords) {
        if (keywords == null || keywords.size() == 0) {
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

    protected static void removeDuplicates(JsonObject in, String addLabel, String removeLabel) {
        if (in.containsKey(addLabel) && in.containsKey(removeLabel)) {
            JsonArray inAdd = in.getJsonArray(addLabel);
            JsonArray inRemove = in.getJsonArray(removeLabel);
            int idx = inRemove.size();
            while (0 < idx) {
                --idx;
                String tag = inRemove.getString(idx);
                if (inAdd.getList().contains(tag)) {
                    inAdd.remove(tag);
                    inRemove.remove(tag);
                }
            }
        }
    }
}
