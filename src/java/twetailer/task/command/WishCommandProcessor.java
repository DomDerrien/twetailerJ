package twetailer.task.command;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import domderrien.jsontools.JsonObject;

public class WishCommandProcessor {
    public static void processWishCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException {
        //
        // Used by a consumer to:
        //
        // 1. Create a wish
        // 2. Update a wish
        //
        throw new ClientException("Wishing - Not yet implemented");
    }
}
