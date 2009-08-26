package twetailer;

import java.text.ParseException;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@SuppressWarnings("serial")
public class SecurityException extends Exception implements TransferObject {
	
	public SecurityException(String message) {
		super(message);
	}
	
	public SecurityException(String message, Exception ex) {
		super(message, ex);
	}

	public TransferObject fromJson(JsonObject in) throws ParseException {
		throw new RuntimeException("not yet implemented!");
	}

	public JsonObject toJson() {
		throw new RuntimeException("not yet implemented!");
	}
}
