package twetailer.rest;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Settings;

public class SettingsOperations extends BaseOperations {
    private static final Logger log = Logger.getLogger(SettingsOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Retrieve the application saved settings
     * 
     * @return Application settings loaded from the back-end or the default values <code>null</code>
     * @throws ClientException If the retrieved demand does not belong to the specified user
     * 
     * @see SettingsOperations#getSettings(PersistenceManager)
     */
    public Settings getSettings() throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getSettings(pm);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Settings instance
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @return Application settings loaded from the back-end or the default values <code>null</code>
     * 
     * @throws ClientException If the retrieved demand does not belong to the specified user
     */
    @SuppressWarnings("unchecked")
    public Settings getSettings(PersistenceManager pm) throws DataSourceException {
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
    
    /**
     * Save the updated application settings
     * 
     * @param update Resource with the updated values
     * @return The just update resource instance
     * @throws ClientException If the retrieved demand does not belong to the specified user
     * 
     * @see SettingsOperations#updateSettings(PersistenceManager, Settings)
     */
    public Settings updateSettings(Settings update) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updateSettings(pm, update);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Save the updated application settings
     * 
     * @param update Resource with the updated values
     * @return The just update resource instance
     * 
     * @throws ClientException If the retrieved demand does not belong to the specified user
     */
    public Settings updateSettings(PersistenceManager pm, Settings update) throws DataSourceException {
        getLogger().warning("Update application settings");
        pm.makePersistent(update);
        // FIXME: save a copy in the memory cache
        return update;
    }
}