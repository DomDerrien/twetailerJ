package org.domderrien.jsontools;

import java.text.ParseException;

public interface TransferObject {
	
    JsonObject toJson();

    TransferObject fromJson(JsonObject in) throws ParseException;
}
