package twetailer;

import java.text.ParseException;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Base class reporting issues related to incorrect submitted data
 * or incorrect access rights
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class ClientException extends Exception implements TransferObject {

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Exception ex) {
        super(message, ex);
    }

    public TransferObject fromJson(JsonObject in) throws ParseException {
        throw new RuntimeException("not yet implemented!");
    }

    public JsonObject toJson() {
        throw new RuntimeException("not yet implemented!");
    }
}
