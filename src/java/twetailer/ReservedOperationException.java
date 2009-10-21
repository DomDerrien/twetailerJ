package twetailer;

import twetailer.validator.CommandSettings.Action;

@SuppressWarnings("serial")
public class ReservedOperationException extends ClientException {

    public ReservedOperationException(String message) {
        super(message);
    }

    public ReservedOperationException(String message, Exception ex) {
        super(message, ex);
    }

    private Action action;

    public ReservedOperationException(Action action) {
        super("Concerned action: " + action.toString());
        this.action = action;
    }

    public Action getAction() {
        return action;
    }
}
