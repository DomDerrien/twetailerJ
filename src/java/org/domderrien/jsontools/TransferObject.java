package org.domderrien.jsontools;

import java.text.ParseException;

import com.twetailer.ClientException;

public interface TransferObject {
	
    JsonObject toJson();

    void fromJson(JsonObject in) throws ParseException, ClientException;
}
