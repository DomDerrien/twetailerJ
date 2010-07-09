package twetailer;

import java.text.ParseException;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * To report issues thrown by the back-end infrastructure
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class DataSourceException extends Exception implements TransferObject {

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Exception ex) {
        super(message, ex);
    }

    public TransferObject fromJson(JsonObject in) throws ParseException {
        throw new RuntimeException("not yet implemented!");
    }

    public JsonObject toJson() {
        throw new RuntimeException("not yet implemented!");
    }
}
