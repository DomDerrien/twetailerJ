package com.twetailer.j2ee;

import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.ClientException;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Settings;

@SuppressWarnings("serial")
public class SettingsServlet extends BaseRestlet {
	private static final Logger log = Logger.getLogger(SettingsServlet.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected String createResource(JsonObject parameters, User loggedUser) throws DataSourceException {
        throw new DataSourceException("Creating new record for application settings is not supported");
	}

	@Override
	protected void deleteResource(String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}
	
	@Override
	protected JsonObject getResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
    	return getSettings().toJson();
	}
	
	@Override
	protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
        throw new DataSourceException("Selecting among application settings is not supported");
	}

	@Override
	protected void updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		throw new RuntimeException("Not yet implemented!");
	}

    /**
     * Use the given key to get the corresponding Settings instance
     * 
     * @return Application settings loaded from the back-end or the default values <code>null</code>
     * @throws ClientException If the retrieved demand does not belong to the specified user
     */
	@SuppressWarnings("unchecked")
    public Settings getSettings() throws DataSourceException {
    	PersistenceManager pm = getPersistenceManager();
    	try {
    	    // FIXME: lookup in the memory cache first
    		// Prepare the query
            Query queryObj = pm.newQuery(Settings.class);
            queryObj.setFilter("name == value");
            queryObj.declareParameters("String value");
			getLogger().warning("Select settings with: " + queryObj.toString());
	    	// Select the corresponding settings
			List<Settings> settingsList = (List<Settings>) queryObj.execute(Settings.APPLICATION_SETTINGS_ID);
	        if (settingsList == null || settingsList.size() == 0) {
                return new Settings();
	        }
	        if (1 < settingsList.size()) {
	            return new Settings();
	        }
	        Settings settings = settingsList.get(0);
	        Long sinceId = settings.getLastProcessDirectMessageId();
            settings.setLastProcessDirectMessageId(1L);
            settings.setLastProcessDirectMessageId(sinceId);
	        return settings;
    	}
    	finally {
    		pm.close();
    	}
    }
	
	public Long updateSettings(Settings update) throws DataSourceException, ParseException {
        getLogger().warning("Update application settings");
        PersistenceManager pm = getPersistenceManager();
        try {
            // Get the saved settings and merge the proposed update
            Settings updated = getSettings();
            updated.fromJson(update.toJson());
            // Push the data to the back-end
            pm.makePersistent(updated);
            // FIXME: save a copy in the memory cache
            // Return the identifier of the just created demand
            return updated.getKey();
        }
        finally {
            pm.close();
        }
	}
}
