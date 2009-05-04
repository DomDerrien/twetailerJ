package com.twetailer;

import java.text.ParseException;

import org.domderrien.jsontools.JsonObject;
import org.domderrien.jsontools.TransferObject;

@SuppressWarnings("serial")
public class DataSourceException extends Exception implements TransferObject {
	
	public DataSourceException(String message) {
		super(message);
	}
	
	public DataSourceException(String message, Exception ex) {
		super(message, ex);
	}
	
	public void fromJson(JsonObject in) throws ParseException {
		throw new RuntimeException("not yet implemented!");
	}

	public JsonObject toJson() {
		throw new RuntimeException("not yet implemented!");
	}
}
