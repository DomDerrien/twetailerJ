package twetailer.dto;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.i18n.StringUtils;
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
    private Long closedProposalNb;

    public final static String CLOSED_PROPOSAL_NB = "closedProposalNb";

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

    @Persistent
    private Long publishedProposalNb;

    public final static String PUBLISHED_PROPOSAL_NB = "publishedProposalNb";

    // Shortcut
    public final static String SALEASSOCIATE_KEY = "saleAssociateKey";

    @Persistent
    private Long storeKey;

    public final static String STORE_KEY = Store.STORE_KEY;

    // Not persistent
    private String score;

    public final static String SCORE = "score";

    public final static String DEFAULT_SCORE = "1:0.1";

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
     * Provided to reproduce the JDO behavior with Unit tests
     *
     * @return Object instance for chaining
     */
    protected SaleAssociate resetLists() {
        criteria = null;
        hashTags = null;
        return this;
    }

    public Long getClosedProposalNb() {
        return closedProposalNb;
    }

    public void setClosedProposalNb(Long closedProposalNb) {
        this.closedProposalNb = closedProposalNb;
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
        if (criterion == null || criterion.length() == 0) {
            return;
        }
        if (criteria == null) {
            criteria = new ArrayList<String>();
        }
        if (collator != null) { // Skip the comparison as it's cache related and data can be transfered safely
            String normalizedCriterion = StringUtils.toUnicode(criterion.getBytes()); // TODO: I don't think it does make a difference to convert the tags here!
            for(String item: criteria) {
                String normalizedItem = StringUtils.toUnicode(item.getBytes()); // TODO: I don't think it does make a difference to convert the tags here!
                if (collator.compare(normalizedCriterion, normalizedItem) == 0) {
                    return;
                }
            }
        }
        criteria.add(criterion);
    }

    public SaleAssociate resetCriteria() {
        if (criteria == null) {
            return this;
        }
        criteria = new ArrayList<String>();
        return this;
    }

    public void removeCriterion(String criterion, Collator collator) {
        if (criteria == null || criterion == null || criterion.length() == 0) {
            return;
        }
        String normalizedCriterion = StringUtils.toUnicode(criterion.getBytes()); // TODO: I don't think it does make a difference to convert the tags here!
        for(String item: criteria) {
            String normalizedItem = StringUtils.toUnicode(item.getBytes()); // TODO: I don't think it does make a difference to convert the tags here!
            if (collator.compare(normalizedCriterion, normalizedItem) == 0) {
                criteria.remove(item);
                break;
            }
        }
    }

    public String getSerializedCriteria(String defaultLabel) {
        if (getCriteria() == null || getCriteria().size() == 0) {
            return defaultLabel;
        }
        return getSerializedCriteria();
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

    public String getSerializedHashTags(String defaultLabel) {
        if (getHashTags() == null || getHashTags().size() == 0) {
            return defaultLabel;
        }
        return getSerializedHashTags();
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

    public SaleAssociate resetHashTags() {
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

    public Boolean getIsStoreAdmin() {
        return isStoreAdmin == null ? Boolean.FALSE : isStoreAdmin;
    }

    public void setIsStoreAdmin(Boolean isStoreAdmin) {
        this.isStoreAdmin = isStoreAdmin;
    }

    public boolean isStoreAdmin() {
        return Boolean.TRUE.equals(isStoreAdmin);
    }

    public Long getPublishedProposalNb() {
        return publishedProposalNb;
    }

    public void setPublishedProposalNb(Long publishedProposalNb) {
        this.publishedProposalNb = publishedProposalNb;
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

    public String getScore() {
        return score == null ? DEFAULT_SCORE : score;
    }

    public void setScore(String score) {
        this.score = DEFAULT_SCORE.equals(score) ? null : score;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        out.put(CLOSED_PROPOSAL_NB, getClosedProposalNb() == null ? 0L : getClosedProposalNb());
        if (getCreatorKey() != null) { out.put(CREATOR_KEY, getCreatorKey()); }
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
        out.put(PUBLISHED_PROPOSAL_NB, getPublishedProposalNb() == null ? 0L : getPublishedProposalNb());
        if (getStoreKey() != null) { out.put(STORE_KEY, getStoreKey()); }
        out.put(SCORE, getScore());
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        return fromJson(in, false, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin, boolean isCacheRelated) {
        if (isCacheRelated) { isUserAdmin = isCacheRelated; }
        super.fromJson(in, isUserAdmin, isCacheRelated);

        if (isUserAdmin && in.containsKey(CLOSED_PROPOSAL_NB)) { setClosedProposalNb(in.getLong(CLOSED_PROPOSAL_NB)); } // Cannot be updated remotely
        if ((getKey() == null || isUserAdmin) && in.containsKey(CONSUMER_KEY)) {
            // Cannot change once set at creation time
            setConsumerKey(in.getLong(CONSUMER_KEY));
        }
        if ((getKey() == null || isUserAdmin) && in.containsKey(CREATOR_KEY)) {
            // Cannot change once set at creation time
            setCreatorKey(in.getLong(CREATOR_KEY));
        }
        if (isCacheRelated) {
            if (in.containsKey(CRITERIA)) {
                JsonArray jsonArray = in.getJsonArray(CRITERIA);
                resetCriteria();
                for (int i=0; i<jsonArray.size(); ++i) {
                    addCriterion(jsonArray.getString(i), null);
                }
            }
        }
        else if (in.containsKey(CRITERIA) || in.containsKey(CRITERIA_ADD) || in.containsKey(CRITERIA_REMOVE)) {
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
        if (isUserAdmin && in.containsKey(IS_STORE_ADMIN)) { setIsStoreAdmin(in.getBoolean(IS_STORE_ADMIN)); }
        if (isUserAdmin && in.containsKey(PUBLISHED_PROPOSAL_NB)) { setPublishedProposalNb(in.getLong(PUBLISHED_PROPOSAL_NB)); } // Cannot be updated remotely
        if (isUserAdmin && in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
        if (in.containsKey(SCORE)) { setScore(in.getString(SCORE)); }

        // Shortcut
        if (in.containsKey(SALEASSOCIATE_KEY)) { setKey(in.getLong(SALEASSOCIATE_KEY)); }

        return this;
    }
}
