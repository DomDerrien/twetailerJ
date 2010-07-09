package twetailer;

/**
 * To report issues related to invalid resource state
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class InvalidStateException extends ClientException {

    public InvalidStateException(String message) {
        super(message);
    }

    public InvalidStateException(String message, Exception ex) {
        super(message, ex);
    }

    private String entityState;
    private String proposedState;

    public InvalidStateException(String message, String entityState, String proposedState) {
        super(message);
        setEntityState(entityState);
        setProposedState(proposedState);
    }

    public String getEntityState() {
        return entityState;
    }

    public void setEntityState(String entityState) {
        this.entityState = entityState;
    }

    public String getProposedState() {
        return proposedState;
    }

    public void setProposedState(String proposedState) {
        this.proposedState = proposedState;
    }
}
