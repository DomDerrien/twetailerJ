package twetailer.dto;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Proposal extends Command {

    private Text AWSCBUIURL;

    public static final String AWSCBUIURL_KEY = "AWSCBUIURL";

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public static final String CRITERIA = Demand.CRITERIA;
    public static final String CRITERIA_ADD = Demand.CRITERIA_ADD;
    public static final String CRITERIA_REMOVE = Demand.CRITERIA_REMOVE;

    @Persistent
    private Long demandKey;

    public static final String DEMAND_KEY = "demandKey";

    public static final String DEMAND_REFERENCE = Demand.REFERENCE;

    // Shortcut
    public static final String PROPOSAL_KEY = "proposal";

    @Persistent
    private Double price = 0.0D;

    public static final String PRICE = "price";

    @Persistent
    private Long quantity = 1L;

    public static final String QUANTITY = Demand.QUANTITY;

    @Persistent
    private Boolean stateCmdList = Boolean.TRUE;

    public static final String STATE_COMMAND_LIST = Demand.STATE_COMMAND_LIST;

    @Persistent
    private Long storeKey;

    public static final String STORE_KEY = Store.STORE_KEY;

    @Persistent
    private Double total = 0.0D;

    public static final String TOTAL = "total";

    /** Default constructor */
    public Proposal() {
        super();
        setAction(Action.propose);
    }

    /**
     * Creates a proposal
     *
     * @param in HTTP request parameters
     */
    public Proposal(JsonObject in) {
        this();
        fromJson(in);
    }

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     */
    protected void resetLists() {
        criteria = null;
    }

    public String getAWSCBUIURL() {
        return AWSCBUIURL.getValue();
    }

    public void setAWSCBUIURL(String aWSCBUIURL) {
        AWSCBUIURL = new Text(aWSCBUIURL);
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

    public Long getDemandKey() {
        return demandKey;
    }

    public void setDemandKey(Long demandKey) {
        this.demandKey = demandKey;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    @Override
    public void setState(State state) {
        super.setState(state);
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

    public Long getStoreKey() {
        return storeKey;
    }

    public void setStoreKey(Long storeKey) {
        this.storeKey = storeKey;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (AWSCBUIURL != null) {
            out.put(AWSCBUIURL_KEY, getAWSCBUIURL());
        }
        if (getCriteria() != null && 0 < getCriteria().size()) {
            JsonArray jsonArray = new GenericJsonArray();
            for(String criterion: getCriteria()) {
                jsonArray.add(criterion);
            }
            out.put(CRITERIA, jsonArray);
        }
        if (getDemandKey() != null) { out.put(DEMAND_KEY, getDemandKey()); }
        out.put(PRICE, getPrice());
        out.put(QUANTITY, getQuantity());
        out.put(STATE_COMMAND_LIST, getStateCmdList());
        if (getStoreKey() != null) { out.put(STORE_KEY, getStoreKey()); }
        out.put(TOTAL, getTotal());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(AWSCBUIURL_KEY)) { setAWSCBUIURL(in.getString(AWSCBUIURL_KEY)); }
        if (in.containsKey(CRITERIA)) {
            JsonArray jsonArray = in.getJsonArray(CRITERIA);
            resetCriteria();
            for (int i=0; i<jsonArray.size(); ++i) {
                addCriterion(jsonArray.getString(i));
            }
        }
        Demand.removeDuplicates(in, CRITERIA_ADD, CRITERIA_REMOVE);
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
        if (in.containsKey(DEMAND_KEY)) { setDemandKey(in.getLong(DEMAND_KEY)); }
        if (in.containsKey(PRICE)) { setPrice(in.getDouble(PRICE)); }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }
        if (in.containsKey(STATE_COMMAND_LIST)) { in.getBoolean(STATE_COMMAND_LIST); }
        if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
        if (in.containsKey(TOTAL)) { setTotal(in.getDouble(TOTAL)); }

        // Shortcut
        if (in.containsKey(PROPOSAL_KEY)) { setKey(in.getLong(PROPOSAL_KEY)); }
        if (in.containsKey(DEMAND_REFERENCE)) { setDemandKey(in.getLong(DEMAND_REFERENCE)); }

        return this;
    }
}
