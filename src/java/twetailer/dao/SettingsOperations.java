package twetailer.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
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
        return getSettings(true);
    }

    /**
     * Retrieve the application saved settings
     * 
     * @param useCache can be used to bypass the cache
     * @return Application settings loaded from the back-end or the default values <code>null</code>
     * 
     * @throws ClientException If the retrieved demand does not belong to the specified user
     * 
     * @see SettingsOperations#getSettings(PersistenceManager)
     */
    public Settings getSettings(boolean checkCache) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            if (checkCache) {
                Settings settings = getSettingsFromCache();
                if (settings != null) {
                    return settings;
                }
            }
            return getSettings(pm);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Accessor for the unit tests
     * 
     * @return A working cache instance
     * 
     * @throws CacheException If the cache instance creation fails
     */
    protected Cache getCache() throws CacheException {
        return CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
    }
    
    /**
     * Return the settings object that have been saved into the cache
     * 
     * @return Cached settings if any
     */
    public Settings getSettingsFromCache() {
        try {
            return (Settings) getCache().get(Settings.APPLICATION_SETTINGS_ID);
        }
        catch(CacheException ex) {}
        return null;
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
        // Prepare the query
        Query queryObj = pm.newQuery(Settings.class);
        queryObj.setFilter("name == value");
        queryObj.declareParameters("String value");
        getLogger().warning("Select settings with: " + queryObj.toString());
        // Select the corresponding settings
        List<Settings> settingsList = (List<Settings>) queryObj.execute(Settings.APPLICATION_SETTINGS_ID);
        if (settingsList.size() == 0) {
            Settings settings = new Settings();
            updateSettingsInCache(settings);
            return settings;
        }
        return settingsList.get(0);
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
            Settings settings = getSettings(pm);
            settings.fromJson(update.toJson()); // Merge the update
            return updateSettings(pm, settings);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Update transparently the cache with the new settings
     */
    @SuppressWarnings("unchecked")
    public Settings updateSettingsInCache(Settings update) {
        try {
            getCache().put(Settings.APPLICATION_SETTINGS_ID, update);
        }
        catch(CacheException ex) {}
        return update;
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
        updateSettingsInCache(update);
        pm.makePersistent(update);
        return update;
    }
}