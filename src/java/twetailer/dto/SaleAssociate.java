package twetailer.dto;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.validator.LocaleValidator;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a Twetailer sale associate, in addition to the ones associated to his consumer profile
 *
 * @see twetailer.dto.Consumer
 * @see twetailer.dto.Store
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class SaleAssociate extends Entity {

    /*** SaleAssociate ***/
    @Persistent
    private Long consumerKey;

    public final static String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private Long creatorKey;

    public final static String CREATOR_KEY = "creatorKey";

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public final static String  CRITERIA = Command.CRITERIA;
    public static final String CRITERIA_ADD = Command.CRITERIA_ADD;
    public static final String CRITERIA_REMOVE = Command.CRITERIA_REMOVE;

    @Persistent
    private List<String> hashTags = new ArrayList<String>();

    public static final String HASH_TAGS = Command.HASH_TAGS;
    public static final String HASH_TAGS_ADD = Command.HASH_TAGS_ADD;
    public static final String HASH_TAGS_REMOVE = Command.HASH_TAGS_REMOVE;

    @Persistent
    private Boolean isStoreAdmin;

    public final static String IS_STORE_ADMIN = "isStoreAdmin";

    // Shortcut
    public final static String SALEASSOCIATE_KEY = "saleAssociateKey";

    @Persistent
    private Long storeKey;

    public final static String STORE_KEY = Store.STORE_KEY;

    // Not persistent
    private Long score = 0L;

    public final static String SCORE = "score";

    /** Default constructor */
    public SaleAssociate() {
        super();
    }

    /**
     * Creates a sale associate
     *
     * @param in HTTP request parameters
     */
    public SaleAssociate(JsonObject in) {
        this();
        fromJson(in);
    }

    /**
     * Provided to reproduce the JDO behaviour with Unit tests
     */
    protected void resetLists() {
        criteria = null;
        hashTags = null;
    }

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerKey) {
        if (consumerKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'consumerKey'");
        }
        this.consumerKey = consumerKey;
    }

    public Long getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(Long creatorKey) {
        if (creatorKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'creatorKey'");
        }
        this.creatorKey = creatorKey;
    }

    public void addCriterion(String criterion, Collator collator) {
        removeCriterion(criterion, collator);
        if (criterion == null || criterion.length() == 0) {
            return;
        }
        if (criteria == null) {
            criteria = new ArrayList<String>();
        }
        criteria.add(criterion);
    }

    public void resetCriteria() {
        if (criteria == null) {
            return;
        }
        criteria = new ArrayList<String>();
    }

    public void removeCriterion(String criterion, Collator collator) {
        if (criteria == null|| criterion == null || criterion.length() == 0) {
            return;
        }
        String normalizedCriterion = LocaleValidator.toUnicode(criterion);
        for(String item: criteria) {
            String normalizedItem = LocaleValidator.toUnicode(item);
            if (collator.compare(normalizedCriterion, normalizedItem) == 0) {
                criteria.remove(item);
                break;
            }
        }
    }

    public String getSerializedCriteria() {
        return Command.getSerializedTags(criteria);
    }

    public List<String> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<String> criteria, Collator collator) {
        if (criteria == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'criteria' of type List<String>");
        }
        this.criteria = null;
        for (String criterion: criteria) {
            addCriterion(criterion, collator);
        }
    }

    public String getSerializedHashTags() {
        return Command.getSerializedTags(Command.HASH, Command.SPACE, hashTags);
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

    public Boolean getIsStoreAdmin() {
        return isStoreAdmin == null ? Boolean.FALSE : isStoreAdmin;
    }

    public void setIsStoreAdmin(Boolean isStoreAdmin) {
        this.isStoreAdmin = isStoreAdmin;
    }

    public boolean isStoreAdmin() {
        return Boolean.TRUE.equals(isStoreAdmin);
    }

    public Long getStoreKey() {
        return storeKey;
    }

    public void setStoreKey(Long storeKey) {
        if (storeKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'storeKey'");
        }
        this.storeKey = storeKey;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        if (score == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'score'");
        }
        this.score = score;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(CONSUMER_KEY, getConsumerKey());
        out.put(CREATOR_KEY, getCreatorKey());
        if (getCriteria() != null && 0 < getCriteria().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
            out.put(CRITERIA, jsonArray);
        }
        if (getHashTags() != null && 0 < getHashTags().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String hashTag: getHashTags()) {
                jsonArray.add(hashTag);
            }
            out.put(HASH_TAGS, jsonArray);
        }
        if (isStoreAdmin()) {
            out.put(IS_STORE_ADMIN, Boolean.TRUE);
        }
        out.put(STORE_KEY, getStoreKey());
        out.put(SCORE, getScore());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (getKey() == null && in.containsKey(CONSUMER_KEY)) {
            // Cannot change once set at creation time
            setConsumerKey(in.getLong(CONSUMER_KEY)); 
        }
        if (getKey() == null && in.containsKey(CREATOR_KEY)) {
            // Cannot change once set at creation time
            setCreatorKey(in.getLong(CREATOR_KEY));
        }
        if (in.containsKey(CRITERIA) || in.containsKey(CRITERIA_ADD) || in.containsKey(CRITERIA_REMOVE)) {
            throw new IllegalArgumentException("Supplied tags should be updated manually to ensure there's no locale-dependend duplicate");
        }
        if (in.containsKey(HASH_TAGS)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAGS);
            resetHashTags();
            for (int i=0; i<jsonArray.size(); ++i) {
                addHashTag(jsonArray.getString(i));
            }
        }
        Command.removeDuplicates(in, HASH_TAGS_ADD, HASH_TAGS_REMOVE);
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
        if (in.containsKey(IS_STORE_ADMIN)) { setIsStoreAdmin(in.getBoolean(IS_STORE_ADMIN)); }
        if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
        if (in.containsKey(SCORE)) { setScore(in.getLong(SCORE)); }

        // Shortcut
        if (in.containsKey(SALEASSOCIATE_KEY)) { setKey(in.getLong(SALEASSOCIATE_KEY)); }

        return this;
    }
}
