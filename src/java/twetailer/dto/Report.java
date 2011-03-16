package twetailer.dto;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a Twetailer reseller which takes care of the
 * retailer registration, training, support, etc., with the system
 *
 * @see twetailer.dto.Entity
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Report extends Entity {

    @Persistent
    private Long consumerKey;

    public final static String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private String content;

    public final static String CONTENT = Command.CONTENT;

    @Persistent
    private Long demandKey;

    public final static String DEMAND_KEY = Demand.DEMAND_KEY;

    @Persistent
    private List<String> hashTags;

    public final static String HASH_TAGS = Command.HASH_TAGS;

    @Persistent
    private String ipAddress;

    public final static String IP_ADDRESS = "ipAddress";

    @Persistent
    private String language;

    public final static String LANGUAGE = Consumer.LANGUAGE;

    @Persistent
    private Long locationKey;

    public final static String LOCATION_KEY = Location.LOCATION_KEY;

    @Persistent
    private String metadata;

    public final static String META_DATA = Command.META_DATA;

    @Persistent
    private Double range;

    public final static String RANGE = Demand.RANGE;

    private static final Text defaultReferrerUrl = new Text("unknown");

    @Persistent
    private Text referrerUrl = defaultReferrerUrl;

    public final static String REFERRER_URL = "referrerUrl";

    @Persistent
    private String reporterUrl;

    public final static String REPORTER_URL = "reporterUrl";

    @Persistent
    private String userAgent;

    public final static String USER_AGENT = "userAgent";

    /** Default constructor */
    public Report() {
        super();
    }

    /**
     * Creates an Reseller
     *
     * @param in HTTP request parameters
     */
    public Report(JsonObject in) {
        super();
        fromJson(in);
    }

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getDemandKey() {
        return demandKey;
    }

    public void setDemandKey(Long demandKey) {
        this.demandKey = demandKey;
    }

    public String getSerializedHashTags(String defaultLabel) {
        if (hashTags == null || hashTags.size() == 0) {
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

    public Report resetHashTags() {
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(Long locationKey) {
        this.locationKey = locationKey;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public Text getReferrerUrl() {
        return referrerUrl;
    }

    public void setReferrerUrl(String referrerUrl) {
        if (referrerUrl == null) {
            setReferrerUrl((Text) null);
        }
        else {
            setReferrerUrl(new Text(referrerUrl));
        }
    }

    public void setReferrerUrl(Text referrerUrl) {
        if (referrerUrl == null) {
            if (this.referrerUrl == defaultReferrerUrl) {
                return;
            }
            referrerUrl = defaultReferrerUrl;
        }
        this.referrerUrl = referrerUrl;
    }

    public String getReporterUrl() {
        return reporterUrl;
    }

    public void setReporterUrl(String reporter) {
        this.reporterUrl = reporter;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        if (getContent() != null) { out.put(CONTENT, getContent()); }
        if (getDemandKey() != null) { out.put(DEMAND_KEY, getDemandKey()); }
        if (getHashTags() != null && 0 < getHashTags().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String hashTag: getHashTags()) {
                jsonArray.add(hashTag);
            }
            out.put(HASH_TAGS, jsonArray);
        }
        if (getIpAddress() != null) { out.put(IP_ADDRESS, getIpAddress()); }
        if (getLanguage() != null) { out.put(LANGUAGE, getLanguage()); }
        if (getLocationKey() != null) { out.put(LOCATION_KEY, getLocationKey()); }
        if (getMetadata() != null) { out.put(META_DATA, getMetadata()); }
        if (getRange() != null) { out.put(RANGE, getRange()); }
        if (getReferrerUrl() != null) { out.put(REFERRER_URL, getReferrerUrl().getValue()); }
        if (getReporterUrl() != null) { out.put(REPORTER_URL, getReporterUrl()); }
        if (getUserAgent() != null) { out.put(USER_AGENT, getUserAgent()); }

        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        return fromJson(in, false, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin, boolean isCacheRelated) {
        if (isCacheRelated) { isUserAdmin = isCacheRelated; }
        if (!isUserAdmin) {
            throw new IllegalArgumentException("Reserved operation");
        }

        super.fromJson(in, isUserAdmin, isCacheRelated);

        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(CONTENT)) { setContent(in.getString(CONTENT)); }
        if (in.containsKey(DEMAND_KEY)) { setDemandKey(in.getLong(DEMAND_KEY)); }
        if (in.containsKey(HASH_TAGS)) {
            JsonArray jsonArray = in.getJsonArray(HASH_TAGS);
            resetHashTags();
            for (int i=0; i<jsonArray.size(); ++i) {
                addHashTag(jsonArray.getString(i));
            }
        }
        if (in.containsKey(IP_ADDRESS)) { setIpAddress(in.getString(IP_ADDRESS)); }
        if (in.containsKey(LANGUAGE)) { setLanguage(in.getString(LANGUAGE)); }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (in.containsKey(META_DATA)) { setMetadata(in.getString(META_DATA)); }
        if (in.containsKey(RANGE)) { setRange(in.getDouble(RANGE)); }
        if (in.containsKey(REFERRER_URL)) { setReferrerUrl(new Text(in.getString(REFERRER_URL))); }
        if (in.containsKey(REPORTER_URL)) { setReporterUrl(in.getString(REPORTER_URL)); }
        if (in.containsKey(USER_AGENT)) { setUserAgent(in.getString(USER_AGENT)); }

        return this;
    }
}
