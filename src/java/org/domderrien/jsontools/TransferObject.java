package org.domderrien.jsontools;

import java.text.ParseException;

public interface TransferObject {
	
    JsonObject toJson();

    void fromJson(JsonObject in) throws ParseException;
}
