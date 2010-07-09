package twetailer;

/**
 * To report issues related to invalid resource identifier
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class InvalidIdentifierException extends ClientException {

    public InvalidIdentifierException(String message) {
        super(message);
    }

    public InvalidIdentifierException(String message, Exception ex) {
        super(message, ex);
    }
}
