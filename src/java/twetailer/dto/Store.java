package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a Twetailer store, which has at least a sale associate as administrator
 *
 * @see twetailer.dto.Location
 * @see twetailer.dto.SaleAssociate
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Store extends Entity {

    @Persistent
    private String address;

    public final static String ADDRESS = "address";

    @Persistent
    private Long closedProposalNb;

    public final static String CLOSED_PROPOSAL_NB = "closedProposalNb";

    @Persistent
    private String email;

    public final static String EMAIL = Consumer.EMAIL;

    @Persistent
    private String name;

    public final static String NAME = "name";

    @Persistent
    private String phoneNumber;

    public final static String PHONE_NUMBER = "phoneNb";

    @Persistent
    private Long publishedProposalNb;

    public final static String PUBLISHED_PROPOSAL_NB = "publishedProposalNb";

    @Persistent
    private Long registrarKey;

    public final static String REGISTRAR_KEY = "registrarKey";

    @Persistent
    private Long reviewSystemKey;

    public final static String REVIEW_SYSTEM_KEY = ReviewSystem.REVIEW_SYSTEM_KEY;

    // Shortcut
    public static final String STORE_KEY = "storeKey";

    @Persistent
    private String url;

    public final static String URL = "url";

    /** Default constructor */
    public Store() {
        super();
    }

    /**
     * Creates a consumer
     *
     * @param in HTTP request parameters
     */
    public Store(JsonObject in) {
        super();
        fromJson(in);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null || address.length() == 0 ? null : address;
    }

    public Long getClosedProposalNb() {
        return closedProposalNb;
    }

    public void setClosedProposalNb(Long closedProposalNb) {
        this.closedProposalNb = closedProposalNb;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null || phoneNumber.length() == 0 ? null : phoneNumber;
    }

    public Long getPublishedProposalNb() {
        return publishedProposalNb;
    }

    public void setPublishedProposalNb(Long publishedProposalNb) {
        this.publishedProposalNb = publishedProposalNb;
    }

    public Long getRegistrarKey() {
        return registrarKey;
    }

    public void setRegistrarKey(Long registrarKey) {
        this.registrarKey = registrarKey;
    }

    public Long getReviewSystemKey() {
        return reviewSystemKey;
    }

    public void setReviewSystemKey(Long reviewSystemKey) {
        this.reviewSystemKey = reviewSystemKey;
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
        out.put(ADDRESS, getAddress());
        out.put(CLOSED_PROPOSAL_NB, getClosedProposalNb() == null ? 0L : getClosedProposalNb());
        out.put(EMAIL, getEmail());
        out.put(NAME, getName());
        out.put(PHONE_NUMBER, getPhoneNumber());
        out.put(PUBLISHED_PROPOSAL_NB, getPublishedProposalNb() == null ? 0L : getPublishedProposalNb());
        if (getRegistrarKey() != null) {
            out.put(REGISTRAR_KEY, getRegistrarKey());
        }
        if (getReviewSystemKey() != null) {
            out.put(REVIEW_SYSTEM_KEY, getReviewSystemKey());
        }
        out.put(URL, getUrl());
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(ADDRESS)) { setAddress(in.getString(ADDRESS)); }
        // if (in.containsKey(CLOSED_PROPOSAL_NB)) { setClosedProposalNb(in.getLong(CLOSED_PROPOSAL_NB)); } // Cannot be updated remotely
        if (in.containsKey(EMAIL)) { setEmail(in.getString(EMAIL)); }
        if (in.containsKey(NAME)) { setName(in.getString(NAME)); }
        if (in.containsKey(PHONE_NUMBER)) { setPhoneNumber(in.getString(PHONE_NUMBER)); }
        // if (in.containsKey(PUBLISHED_PROPOSAL_NB)) { setPublishedProposalNb(in.getLong(PUBLISHED_PROPOSAL_NB)); } // Cannot be updated remotely
        // if (in.containsKey(REGISTRAR_KEY)) { setRegistrarKey(in.getLong(REGISTRAR_KEY)); } // Cannot be changed transparently
        if (in.containsKey(REVIEW_SYSTEM_KEY)) { setReviewSystemKey(in.getLong(REVIEW_SYSTEM_KEY)); } // Store administrators can change it
        if (in.containsKey(URL)) { setUrl(in.getString(URL)); }

        // Shortcut
        if (in.containsKey(STORE_KEY)) {setKey(in.getLong(STORE_KEY)); }

        return this;
    }
}
