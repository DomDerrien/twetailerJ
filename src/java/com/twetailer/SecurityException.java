package com.twetailer;

import java.text.ParseException;

import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.TransferObject;

@SuppressWarnings("serial")
public class SecurityException extends Exception implements TransferObject {
	
	public SecurityException(String message) {
		super(message);
	}
	
	public SecurityException(String message, Exception ex) {
		super(message, ex);
	}

	public void fromJson(JsonObject in) throws ParseException {
		throw new RuntimeException("not yet implemented!");
	}

	public JsonObject toJson() {
		throw new RuntimeException("not yet implemented!");
	}
}
