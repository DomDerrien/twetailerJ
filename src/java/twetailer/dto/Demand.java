package twetailer.dto;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Define the attributes of a consumer request
 *
 * @see twetailer.dto.Command
 * @see twetailer.dto.Request
 * @see twetailer.dto.Wish
 * @see twetailer.dto.Consumer
 * @see twetailer.dto.Payment
 * @see twetailer.dto.Proposal
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Demand extends Request {

    @Persistent
    private List<Long> proposalKeys = new ArrayList<Long>();

    public static final String PROPOSAL_KEYS = "proposalKeys";

    @Persistent
    private List<Long> saleAssociateKeys = new ArrayList<Long>();

    public static final String SALE_ASSOCIATE_KEYS = "saleAssociateKeys";

    /** Default constructor */
    public Demand() {
        super();
    }

    /**
     * Creates a demand
     *
     * @param in HTTP request parameters
     */
    public Demand(JsonObject in) {
        this();
        fromJson(in);
    }

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     *
     * @return Object instance for chaining
     */
    protected Demand resetLists() {
        super.resetLists();
        proposalKeys = null;
        saleAssociateKeys = null;
        return this;
    }

    public List<Long> getProposalKeys() {
        return proposalKeys;
    }

    public String getSerializedProposalKeys(String defaultLabel) {
        if (getProposalKeys() == null || getProposalKeys().size() == 0) {
            return defaultLabel;
        }
        return getSerializedProposalKeys();
    }

    public String getSerializedProposalKeys() {
        return getSerializedTags(proposalKeys);
    }

    public void setProposalKeys(List<Long> proposalKeys) {
        if (proposalKeys == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'proposalKeys' of type List<Long>");
        }
        updateModificationDate(); // To highlight the demand update
        this.proposalKeys = proposalKeys;
    }

    public void addProposalKey(Long proposalKey) {
        if (proposalKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'proposalKeys' of type List<Long>");
        }
        if (proposalKeys == null) {
            proposalKeys = new ArrayList<Long>();
        }
        if (!proposalKeys.contains(proposalKey)) {
            updateModificationDate(); // To highlight the demand update
            proposalKeys.add(proposalKey);
        }
    }

    public Demand resetProposalKeys() {
        if (proposalKeys == null) {
            return this;
        }
        updateModificationDate(); // To highlight the demand update
        proposalKeys = new ArrayList<Long>();
        return this;
    }

    public void removeProposalKey(Long proposalKey) {
        if (proposalKeys == null) {
            return;
        }
        if (proposalKeys.contains(proposalKey)) {
            updateModificationDate(); // To highlight the demand update
            proposalKeys.remove(proposalKey);
        }
    }

    public List<Long> getSaleAssociateKeys() {
        return saleAssociateKeys;
    }

    public void setSaleAssociateKeys(List<Long> saleAssociateKeys) {
        if (saleAssociateKeys == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'saleAssociateKeys' of type List<Long>");
        }
        this.saleAssociateKeys = saleAssociateKeys;
    }

    public void addSaleAssociateKey(Long saleAssociateKey) {
        if (saleAssociateKeys == null) {
            saleAssociateKeys = new ArrayList<Long>();
        }
        if (!saleAssociateKeys.contains(saleAssociateKey)) {
            saleAssociateKeys.add(saleAssociateKey);
        }
    }

    public Demand resetSaleAssociateKeys() {
        if (saleAssociateKeys == null) {
            return this;
        }
        saleAssociateKeys = new ArrayList<Long>();
        return this;
    }

    public void removeSaleAssociateKey(Long saleAssociateKey) {
        if (saleAssociateKeys == null) {
            return;
        }
        saleAssociateKeys.remove(saleAssociateKey);
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getProposalKeys() != null && 0 < getProposalKeys().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(Long key: getProposalKeys()) {
                jsonArray.add(key);
            }
            out.put(PROPOSAL_KEYS, jsonArray);
        }
        if (getSaleAssociateKeys() != null && 0 < getSaleAssociateKeys().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(Long key: getSaleAssociateKeys()) {
                jsonArray.add(key);
            }
            out.put(SALE_ASSOCIATE_KEYS, jsonArray);
        }
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(PROPOSAL_KEYS)) {
            resetProposalKeys();
            JsonArray jsonArray = in.getJsonArray(PROPOSAL_KEYS);
            for (int i=0; i<jsonArray.size(); ++i) {
                addProposalKey(jsonArray.getLong(i));
            }
        }
        if (in.containsKey(SALE_ASSOCIATE_KEYS)) {
            resetSaleAssociateKeys();
            JsonArray jsonArray = in.getJsonArray(SALE_ASSOCIATE_KEYS);
            for (int i=0; i<jsonArray.size(); ++i) {
                addSaleAssociateKey(jsonArray.getLong(i));
            }
        }
        return this;
    }
}
