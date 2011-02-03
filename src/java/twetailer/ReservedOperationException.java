package twetailer;

import twetailer.validator.CommandSettings.Action;

/**
 * To report issues related to inadequate access rights
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class ReservedOperationException extends ClientException {

    public ReservedOperationException(String message) {
        super(message);
    }

    public ReservedOperationException(String message, Exception ex) {
        super(message, ex);
    }

    private Action action;
    private String entityClassName;

    public ReservedOperationException(Action action, String entityClassName) {
        super("Reserved operation -- Cannot use action \"" + action.toString() + "\"" + (entityClassName == null ? "" : " on entity \"" + entityClassName + "\""));
        this.action = action;
        this.entityClassName = entityClassName;
    }

    public ReservedOperationException(Action action, String entityClassName, Exception ex) {
        super("Reserved operation -- Cannot use action \"" + action.toString() + "\"" + (entityClassName == null ? "" : " on entity \"" + entityClassName + "\""), ex);
        this.action = action;
        this.entityClassName = entityClassName;
    }

    public Action getAction() {
        return action;
    }

    public String getEntityClassName() {
        return entityClassName;
    }
}
