package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;

/**
 * Define the attribute for the payment operation attached to a confirmed proposal/demand
 *
 * @see twetailer.dto.Demand
 * @see twetailer.dto.Proposal
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")

public class Payment extends Entity {

    @Persistent
    private String authorizationId;

    public final static String AUTHORIZATION_ID = "authorizationId";

    @Persistent
    private String reference;

    public final static String REFERENCE = "reference";

    @Persistent
    private String requestId;

    public final static String REQUEST_ID = "requestId";

    @Persistent
    private String transactionId;

    public final static String TRANSACTION_ID = "transactionId";

    /* legacy *
    @ Persistent
    private TransactionStatus status;

    public final static String STATUS = "status";
    */

    /** Default constructor */
    public Payment() {
        super();
    }

    public String getAuthorizationId() {
        return authorizationId;
    }

    public void setAuthorizationId(String authorizationId) {
        if (authorizationId == null || authorizationId.length() == 0) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'authorizationId'");
        }
        this.authorizationId = authorizationId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        if (reference == null || reference.length() == 0) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'reference'");
        }
        this.reference = reference;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        if (requestId == null || requestId.length() == 0) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'requestId'");
        }
        this.requestId = requestId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'transactionId'");
        }
        this.transactionId = transactionId;
    }

    /* legacy *
    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'status'");
        }
        this.status = status;
    }
    */

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getAuthorizationId() != null) { out.put(AUTHORIZATION_ID, getAuthorizationId()); }
        if (getReference() != null) { out.put(REFERENCE, getReference()); }
        if (getRequestId() != null) { out.put(REQUEST_ID, getRequestId()); }
        if (getTransactionId() != null) { out.put(TRANSACTION_ID, getTransactionId()); }
        /* legacy *
        if (getStatus() != null) { out.put(STATUS, getStatus().value()); }
        */
        return out;
    }

    public static String getReference(Long consumerKey, Long demandKey, Long proposalKey) {
        return consumerKey + "-" + demandKey + "-" + proposalKey;
    }

    public static Long[] getKeys(String sequence) {
        int firstDashIdx = sequence.indexOf('-');
        int secondDashIdx = sequence.indexOf('-', firstDashIdx + 1);
        return new Long[] {
                Long.valueOf(sequence.substring(0, firstDashIdx)),
                Long.valueOf(sequence.substring(firstDashIdx + 1, secondDashIdx)),
                Long.valueOf(sequence.substring(secondDashIdx + 1))
        };
    }

    public static JsonObject keysToJson(String sequence, JsonObject out) {
        Long[] keys = getKeys(sequence);
        out.put("consumerKey", keys[0]);
        out.put("demandKey", keys[1]);
        out.put("proposalKey", keys[2]);
        return out;
    }
}
