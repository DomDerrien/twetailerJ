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

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Proposal extends Entity {

    /*** Command ***/

    @Persistent
    private Action action;

    public static final String ACTION = "action";

    @Persistent
    private Long consumerKey;

    public static final String CONSUMER_KEY = Consumer.CONSUMER_KEY;

    @Persistent
    private Long rawCommandId;

    public static final String RAW_COMMAND_ID = "rawCommandId";

    @Persistent
    private Source source;

    public static final String SOURCE = "source";

    @Persistent
    private State state = State.open;

    public static final String STATE = "state";

    /*** Demand ***/

    @Persistent
    private List<String> criteria = new ArrayList<String>();

    public static final String CRITERIA = "criteria";

    @Persistent
    private Long demandKey;

    public static final String DEMAND_KEY = Demand.REFERENCE;

    // Shortcut
    public static final String PROPOSAL_KEY = "proposal";

    @Persistent
    private Double price = 0.0D;

    public static final String PRICE = "price";

    @Persistent
    private Long quantity = 1L;

    public static final String QUANTITY = "quantity";

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
     * Creates a demand
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

    /*** Command ***/

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

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerId) {
        this.consumerKey = consumerId;
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

    /*** Demand ***/

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
        /*** Command ***/
        out.put(ACTION, getAction().toString());
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        if (getRawCommandId() != null) { out.put(RAW_COMMAND_ID, getRawCommandId()); }
        out.put(SOURCE, getSource().toString());
        out.put(STATE, getState().toString());
        /*** Demand ***/
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
        if (getStoreKey() != null) { out.put(STORE_KEY, getStoreKey()); }
        out.put(TOTAL, getTotal());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        /*** Command ***/
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(RAW_COMMAND_ID)) { setRawCommandId(in.getLong(RAW_COMMAND_ID)); }
        if (in.containsKey(SOURCE)) { setSource(in.getString(SOURCE)); }
        if (in.containsKey(STATE)) { setState(in.getString(STATE)); }
        /*** Demand ***/
        if (in.containsKey(CRITERIA)) {
            JsonArray jsonArray = in.getJsonArray(CRITERIA);
            resetCriteria();
            for (int i=0; i<jsonArray.size(); ++i) {
                addCriterion(jsonArray.getString(i));
            }
        }
        if (in.containsKey(DEMAND_KEY)) { setDemandKey(in.getLong(DEMAND_KEY)); }
        if (in.containsKey(PRICE)) { setPrice(in.getDouble(PRICE)); }
        if (in.containsKey(QUANTITY)) { setQuantity(in.getLong(QUANTITY)); }
        if (in.containsKey(STORE_KEY)) { setStoreKey(in.getLong(STORE_KEY)); }
        if (in.containsKey(TOTAL)) { setTotal(in.getDouble(TOTAL)); }

        // Shortcut
        if (in.containsKey(PROPOSAL_KEY)) { setKey(in.getLong(PROPOSAL_KEY)); }

        return this;
    }
}
