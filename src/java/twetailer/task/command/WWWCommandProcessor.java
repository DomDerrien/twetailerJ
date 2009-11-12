package twetailer.task.command;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import domderrien.jsontools.JsonObject;

public class WWWCommandProcessor {
    public static void processWWWCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by the resource owner to get the tiny URL that will open the Twetailer Web console
        //
        throw new ClientException("Surfing on the web - Not yet implemented");
    }
}
