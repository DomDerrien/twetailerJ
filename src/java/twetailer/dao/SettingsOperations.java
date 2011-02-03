package twetailer.dao;

import javax.jdo.JDOHelper;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.DataSourceException;
import twetailer.dto.Settings;

/**
 * Controller defining various methods used for the CRUD operations on Settings entities
 *
 * @author Dom Derrien
 */
public class SettingsOperations extends BaseOperations {

    private static final CacheHandler<Settings> cacheHandler = new CacheHandler<Settings>(Settings.class.getName(), Settings.NAME);

    private static Settings cacheSettings(Settings settings) {
        return cacheHandler.cacheInstance(settings);
    }

    private static Settings getCachedSettings() {
        return cacheHandler.getCachedInstance(Settings.NAME, Settings.APPLICATION_SETTINGS_ID);
    }

    /**
     * Retrieve the application saved settings
     *
     * @return Application settings loaded from the back-end or the default values <code>null</code>
     *
     * @throws DataSourceException If the data exchange with the data store fails
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
     * Retrieve the application saved settings
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @return Application settings loaded from the back-end or the default values <code>null</code>
     *
     * @throws DataSourceException If the data exchange with the data store fails
     *
     * @see SettingsOperations#getSettings(PersistenceManager)
     */
    public Settings getSettings(PersistenceManager pm) throws DataSourceException {
        return getSettings(pm, true);
    }

    /**
     * Retrieve the application saved settings
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param useCache can be used to bypass the cache
     * @return Application settings loaded from the back-end or the default values <code>null</code>
     *
     * @throws DataSourceException If the data exchange with the data store fails
     *
     * @see SettingsOperations#getSettings(PersistenceManager)
     */
    protected Settings getSettings(PersistenceManager pm, boolean useCache) throws DataSourceException {
        // Try to get a copy from the cache
        Settings settings = useCache ? getCachedSettings() : null;
        if (settings != null) {
            return settings;
        }
        // Get it from the data store
        Query query = pm.newQuery(Settings.class);
        try {
            // Select the corresponding settings
            query.setFilter("name == value");
            query.declareParameters("String value");
            query.setUnique(true);
            settings = (Settings) query.execute(Settings.APPLICATION_SETTINGS_ID);
            if (settings == null) {
                // Create the settings instance on the fly
                settings = pm.makePersistent(new Settings());
            }
            if (useCache) {
                cacheSettings(settings);
            }
            return settings;
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
     *
     * @throws DataSourceException If the data exchange with the data store fails
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
     * @throws DataSourceException If the data exchange with the data store fails
     */
    public Settings updateSettings(PersistenceManager pm, Settings update) throws DataSourceException {
        ObjectState state = JDOHelper.getObjectState(update);
        if (ObjectState.TRANSIENT.equals(state)) {
            // Get a fresh user copy from the data store
            Settings transientConsumer = update;
            update = getSettings(pm, false);
            // Note: no need to remove the transient copy from the cache as it's going to be overridden
            // Merge the attribute of the old copy into the fresh one
            update.fromJson(transientConsumer.toJson(), true, true);
        }
        // Update the cached instance
        cacheSettings(update);
        // Persist updated settings
        return pm.makePersistent(update);
    }
}
