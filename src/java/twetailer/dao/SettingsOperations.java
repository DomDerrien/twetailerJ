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

/**
 * Controller defining various methods used for the CRUD operations on Settings entities
 *
 * @author Dom Derrien
 */
public class SettingsOperations extends BaseOperations {

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
        if (checkCache) {
            Settings settings = getSettingsFromCache();
            if (settings != null) {
                return settings;
            }
        }
        PersistenceManager pm = getPersistenceManager();
        try {
            return getSettings(pm); // Skip getSettings(pm, checkCache) because the cache has already been checked.
        }
        finally {
            pm.close();
        }
    }

    /**
     * Retrieve the application saved settings
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param useCache can be used to bypass the cache
     * @return Application settings loaded from the back-end or the default values <code>null</code>
     *
     * @throws ClientException If the retrieved demand does not belong to the specified user
     *
     * @see SettingsOperations#getSettings(PersistenceManager)
     */
    public Settings getSettings(PersistenceManager pm, boolean checkCache) throws DataSourceException {
        if (checkCache) {
            Settings settings = getSettingsFromCache();
            if (settings != null) {
                return settings;
            }
        }
        return getSettings(pm);
    }

    private Cache localCache;

    /**
     * Accessor for the unit tests
     *
     * @return A working cache instance
     *
     * @throws CacheException If the cache instance creation fails
     */
    protected Cache getCache() throws CacheException {
        if (localCache == null) {
            localCache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        }
        return localCache;
    }

    /**
     * Return the settings object that have been saved into the cache
     *
     * @return Cached settings if any
     */
    public Settings getSettingsFromCache() {
        return (Settings) getFromCache(Settings.APPLICATION_SETTINGS_ID);
    }

    /**
     * Return the settings object that have been saved into the cache
     *
     * @param entryId identifier of the cached data
     * @return Cached settings if any
     */
    public Object getFromCache(String entryId) {
        try {
            return getCache().get(entryId); // Can be null
        }
        catch(CacheException ex) {
            Logger.getLogger(SettingsOperations.class.getName()).warning("Cannot get entry: " + entryId + " -- message: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Update the cache with the given value
     *
     * @param entryId identifier of the cached data
     * @param object data to store
     * @return The object itself, for operation chaining
     */
    @SuppressWarnings("unchecked")
    public SettingsOperations setInCache(String entryId, Object data) {
        try {
            getCache().put(entryId, data);
        }
        catch(CacheException ex) {
            Logger.getLogger(SettingsOperations.class.getName()).warning("Cache addition failed with the data identified by: " + entryId + " -- message: " + ex.getMessage());
        }
        return this;
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
        Query query = pm.newQuery(Settings.class);
        try {
            query.setFilter("name == value");
            query.declareParameters("String value");
            // Select the corresponding settings
            List<Settings> settingsList = (List<Settings>) query.execute(Settings.APPLICATION_SETTINGS_ID);
            if (settingsList.size() == 0) {
                Settings settings = new Settings();
                updateSettings(pm, settings);
                updateSettingsInCache(settings);
                return settings;
            }
            return settingsList.get(0);
        }
        finally {
            query.closeAll();
        }
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
     *
     * @param settings updated settings
     * @return The object itself, for operation chaining
     */
    public SettingsOperations updateSettingsInCache(Settings update) {
        return setInCache(Settings.APPLICATION_SETTINGS_ID, update);
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
        updateSettingsInCache(update);
        return pm.makePersistent(update);
    }
}
