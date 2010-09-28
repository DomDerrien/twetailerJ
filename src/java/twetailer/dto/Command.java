package twetailer.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Base class for all Twetailer commands consumers and sale associates can produce
 *
 * @see twetailer.dto.Demand
 * @see twetailer.dto.Proposal
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public class Command extends Entity {

    public static final String POINT_OF_VIEW = "pointOfView";

    public enum QueryPointOfView {
        ANONYMOUS,
        CONSUMER,
        SALE_ASSOCIATE;

        /**
         * Returns the enum constant extracted from the given JSON bag
         *
         * @param parameters JSON bag to scan
         * @param defaultValue default value to apply if the JSON bag does not contain the expected information
         * @return Corresponding enum constant if found, <code>consumer</code> value otherwise
         */
        public static QueryPointOfView fromJson(JsonObject parameters, QueryPointOfView defaultValue) {
            try {
                if (parameters != null && parameters.containsKey(POINT_OF_VIEW)) {
                    String value = parameters.getString(POINT_OF_VIEW).toUpperCase();
                    return QueryPointOfView.valueOf(value);
                }
                return defaultValue;
            }
            catch(Exception ex) {
                // Will fall-back on the default value ;)
                Logger.getLogger(QueryPointOfView.class.getName()).warning("Cannot get a QueryPointOfView value from: " + parameters.toString() + " -- message: " + ex.getMessage());
                return defaultValue;
            }
        }
    }

    @Persistent
    private Action action;

    public static final String ACTION = "action";

    @Persistent
    private Long cancelerKey;

    public static final String CANCELER_KEY = "cancelerKey";

    @Persistent
    private List<String> cc = new ArrayList<String>();

    public static final String CC = "cc";
    public static final String CC_ADD = "\\+cc";
    public static final String CC_REMOVE = "\\-cc";

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public static final String CRITERIA = "criteria";
    public static final String CRITERIA_ADD = "\\+criteria";
    public static final String CRITERIA_REMOVE = "\\-criteria";

    @Persistent
    private Date dueDate;

    public static final String DUE_DATE = "dueDate";

    @Persistent
    private List<String> hashTags = new ArrayList<String>();

    public static final String HASH_TAGS = "hashTags";
    public static final String HASH_TAGS_ADD = "\\+hashTags";
    public static final String HASH_TAGS_REMOVE = "\\-hashTags";

    @Persistent
    private String metadata;

    public static final String META_DATA = "metadata";

    @Persistent
    private Long ownerKey;

    public static final String OWNER_KEY = "ownerKey";

    @Persistent
    private Long quantity = 1L;

    public static final String QUANTITY = "quantity";

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
     *
     * @return Object instance for chaining
     */
    protected Command resetLists() {
        cc = null;
        criteria = null;
        hashTags = null;
        return this;
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

    public String getSerializedCC(String defaultLabel) {
        if (getCC() == null || getCC().size() == 0) {
            return defaultLabel;
        }
        return getSerializedCC();
    }

    public String getSerializedCC() {
        return getSerializedTags(cc);
    }

    public List<String> getCC() {
        return cc;
    }

    public void setCC(List<String> cc) {
        if (cc == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'cc' of type List<String>");
        }
        this.cc = cc;
    }

    public void addCoordinate(String coordinates) {
        if (coordinates == null || coordinates.length() == 0) {
            return;
        }
        if (cc == null) {
            cc = new ArrayList<String>();
        }
        if (!cc.contains(coordinates)) {
            cc.add(coordinates);
        }
    }

    public Command resetCC() {
        if (cc == null) {
            return this;
        }
        cc = new ArrayList<String>();
        return this;
    }

    public void removeCoordinate(String coordinates) {
        if (cc == null || coordinates == null || coordinates.length() == 0) {
            return;
        }
        cc.remove(coordinates);
    }

    public String getSerializedCriteria(String defaultLabel) {
        if (getCriteria() == null || getCriteria().size() == 0) {
            return defaultLabel;
        }
        return getSerializedCriteria();
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
        if (criterion == null || criterion.length() == 0) {
            return;
        }
        if (criteria == null) {
            criteria = new ArrayList<String>();
        }
        if (!criteria.contains(criterion)) {
            criteria.add(criterion);
        }
    }

    public Command resetCriteria() {
        if (criteria == null) {
            return this;
        }
        criteria = new ArrayList<String>();
        return this;
    }

    public void removeCriterion(String criterion) {
        if (criteria == null || criterion == null || criterion.length() == 0) {
            return;
        }
        criteria.remove(criterion);
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getSerializedHashTags(String defaultLabel) {
        if (hashTags == null || hashTags.size() == 0) {
            return defaultLabel;
        }
        return getSerializedHashTags();
    }

    public String getSerializedHashTags() {
        return getSerializedTags(HASH, SPACE, hashTags);
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
        if (hashTag == null || hashTag.length() == 0) {
            return;
        }
        if (hashTags == null) {
            hashTags = new ArrayList<String>();
        }
        if (!hashTags.contains(hashTag)) {
            hashTags.add(hashTag);
        }
    }

    public Command resetHashTags() {
        if (hashTags == null) {
            return this;
        }
        hashTags = new ArrayList<String>();
        return this;
    }

    public void removeHashTag(String hashTag) {
        if (hashTags == null || hashTag == null || hashTag.length() == 0) {
            return;
        }
        hashTags.remove(hashTag);
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
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

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'quantity'");
        }
        this.quantity = quantity;
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
        setStateCmdList(
            !State.cancelled.equals(state) &&
            !State.closed.equals(state) &&
            !State.declined.equals(state) &&
            !State.markedForDeletion.equals(state)
        );
        setMarkedForDeletion(State.markedForDeletion.equals(state));
    }

    public void setState(String state) {
        setState(State.valueOf(state));
    }

    public Boolean getStateCmdList() {
        return stateCmdList == null ? Boolean.TRUE : stateCmdList;
    }

    protected void setStateCmdList(Boolean  stateCmdList) {
        this.stateCmdList = stateCmdList;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getAction() != null) { out.put(ACTION, getAction().toString()); }
        if (getCancelerKey() != null) { out.put(CANCELER_KEY, getCancelerKey()); }
        if (getCC() != null && 0 < getCC().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String coordinate: getCC()) {
                jsonArray.add(coordinate);
            }
            out.put(CC, jsonArray);
        }
        if (getCriteria() != null && 0 < getCriteria().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
            out.put(CRITERIA, jsonArray);
        }
        if (getDueDate() != null) { out.put(DUE_DATE, DateUtils.dateToISO(getDueDate())); }
        if (getHashTags() != null && 0 < getHashTags().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String hashTag: getHashTags()) {
                jsonArray.add(hashTag);
            }
            out.put(HASH_TAGS, jsonArray);
        }
        if (getMetadata() != null) { out.put(META_DATA, getMetadata()); }
        out.put(OWNER_KEY, getOwnerKey());
        out.put(QUANTITY, getQuantity());
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
        if (in.containsKey(CC)) {
            JsonArray jsonArray = in.getJsonArray(CC);
            resetCC();
            for (int i=0; i<jsonArray.size(); ++i) {
                addCoordinate(jsonArray.getString(i));
            }
        }
        removeDuplicates(in, CC_ADD, CC_REMOVE);
        if (in.containsKey(CC_REMOVE)) {
            JsonArray jsonArray = in.getJsonArray(CC_REMOVE);
            for (int i=0; i<jsonArray.size(); ++i) {
                removeCoordinate(jsonArray.getString(i));
            }
        }
        if (in.containsKey(CC_ADD)) {
            JsonArray jsonArray = in.getJsonArray(CC_ADD);
            for (int i=0; i<jsonArray.size(); ++i) {
                addCoordinate(jsonArray.getString(i));
            }
        }
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
        if (in.containsKey(DUE_DATE)) {
            try {
                Date dueDate = DateUtils.isoToDate(in.getString(DUE_DATE));
                setDueDate(dueDate);
            }
            catch (ParseException ex) {
                Logger.getLogger(Command.class.getName()).warning("Invalid format in due date: " + in.getString(DUE_DATE) + ", for command.key=" + getKey() + " -- message: " + ex.getMessage());
                setDueDate(null);
            }
        }
        if (in.containsKey(HASH_TAGS)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAGS);
            resetHashTags();
            for (int i=0; i<jsonArray.size(); ++i) {
                addHashTag(jsonArray.getString(i));
            }
        }
        removeDuplicates(in, HASH_TAGS_ADD, HASH_TAGS_REMOVE);
        if (in.containsKey(HASH_TAGS_REMOVE)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAGS_REMOVE);
            for (int i=0; i<jsonArray.size(); ++i) {
                removeHashTag(jsonArray.getString(i));
            }
        }
        if (in.containsKey(HASH_TAGS_ADD)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAGS_ADD);
            for (int i=0; i<jsonArray.size(); ++i) {
                addHashTag(jsonArray.getString(i));
            }
        }
        if (in.containsKey(META_DATA)) { setMetadata(in.getString(META_DATA)); }
        if (in.containsKey(OWNER_KEY)) { setOwnerKey(in.getLong(OWNER_KEY)); }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }
        if (in.containsKey(RAW_COMMAND_ID)) { setRawCommandId(in.getLong(RAW_COMMAND_ID)); }
        if (in.containsKey(SOURCE)) { setSource(in.getString(SOURCE)); }
        if (in.containsKey(STATE)) { setState(in.getString(STATE)); }
        if (in.containsKey(STATE_COMMAND_LIST)) { in.getBoolean(STATE_COMMAND_LIST); }
        return this;
    }

    public final static String EMPTY_STRING = "";
    public final static String SPACE = " ";
    public final static String HASH = "#";
    public final static String SEMICOLON = ";";

    public static String getSerializedTags(List<?> keywords) {
        return getSerializedTags(null, SPACE, keywords);
    }

    public static String getSerializedTags(String prefix, String spacer, List<?> keywords) {
        if (keywords == null || keywords.size() == 0) {
            return EMPTY_STRING;
        }
        StringBuilder out = new StringBuilder();
        if (prefix == null) {
            for(Object keyword: keywords) {
                out.append(spacer).append(keyword.toString());
            }
        }
        else {
            for(Object keyword: keywords) {
                out.append(spacer).append(prefix).append(keyword.toString());
            }
        }
        return out.substring(spacer.length()); // Leading spacer excluded
    }

    public static void removeDuplicates(JsonObject in, String addLabel, String removeLabel) {
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
