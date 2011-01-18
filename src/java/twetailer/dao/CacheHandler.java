package twetailer.dao;

import java.util.Collections;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import twetailer.dto.Entity;
import domderrien.jsontools.JsonObject;

public class CacheHandler<T extends Entity> {

    private static Logger log = Logger.getLogger(CacheHandler.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    private static Cache localCache = null;
    private static CacheFactory localCacheFactory = null;

    /**
     * Accessor for the unit tests
     */
    public static void injectCache(Cache mockCache) {
        localCache = mockCache;
    }

    /**
     * Accessor for the unit tests
     */
    public static void injectCacheFactory(CacheFactory mockCacheFactory) {
        localCacheFactory = mockCacheFactory;
    }

    /// Accessor dealing with the previously retrieved or inject Cache related instances
    protected static Cache getCache() throws CacheException {
        if (localCache == null) {
            if (localCacheFactory == null) {
                localCache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
            }
            else {
                localCache = localCacheFactory.createCache(Collections.emptyMap());
            }
        }
        return localCache;
    }

    /**
     * Return the settings object that have been saved into the cache
     *
     * @param entryId identifier of the cached data
     * @return Cached settings if any
     *
     * @see twetailer.dao.CacheHandler#setInCache(String, Object)
     */
    public static Object getFromCache(String entryId) {
        try {
            Object data = getCache().get(entryId); // Can be null
            getLogger().finest("Cache " + (data == null ? "MISS" : "HIT") + " for: " + entryId);
            return data;
        }
        catch(CacheException ex) {
            getLogger().warning("Cannot get entry: " + entryId + " -- message: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Update the cache with the given value
     *
     * @param entryId identifier of the cached data
     * @param object data to store--if the data is <code>null</code>, the cache entry is removed
     * @return The value to which was stored in the cache, or <code>null</code>
     *
     * @see twetailer.dao.CacheHandler#getFromCache(String)
     */
    @SuppressWarnings("unchecked")
    public static Object setInCache(String entryId, Object data) {
        try {
            if (data == null) {
                getLogger().finest("Clearing : " + entryId);
                data = getCache().remove(entryId);
            }
            else {
                getLogger().finest("Caching : " + entryId);
                data = getCache().put(entryId, data);
            }
        }
        catch(CacheException ex) {
            getLogger().warning("Cache addition failed with the data identified by: " + entryId + " -- message: " + ex.getMessage());
        }
        return data;
    }

    /**
     * Reset the corresponding entry from the cache
     *
     * @param entryId identifier of the cached data
     * @return The value to which was stored in the cache, or <code>null</code>
     *
     * @see twetailer.dao.CacheHandler#setInCache(String, Object)
     */
    public static Object resetInCache(String entryId) {
        return setInCache(entryId, null);
    }

    /**
     * Reset the entire cache.
     *
     * Reserved for test and the Maelzel entry '/flushMemCache'
     */
    public static void clearCache() {
        try {
            Cache cache = getCache();
            cache.clear();
        }
        catch (CacheException ex) {
            getLogger().warning("Cache flushing failed -- message: " + ex.getMessage());
        }

    }

    private String className;
    private String keyPrefix;
    private String[] keys = new String[1];

    private CacheHandler(String className) {
        this.className = className;
        keyPrefix = className.substring(className.lastIndexOf('.')) + "_";
    }

    /**
     * Constructor for an instance which is always going to save the given class instance in the cache under one entry.
     *
     * @param clazz Class of the instance to be retrieved--needed as we cannot use to operator 'new' for the template...
     * @param key The identifier which is going to used, with the associated value, to store the instance into the cache.
     *
     * @see twetailer.dao.CacheHandler#CacheHandler(String[])
     */
    public CacheHandler(String className, String key) {
        this(className);
        keys[0] = key;
    }

    /**
     * Constructor for an instance which is always going to save the given class instance in the cache with many entries.
     *
     * @param clazz Class of the instance to be retrieved--needed as we cannot use to operator 'new' for the template...
     * @param keys List of identifiers which are going to used, with the associated values, to store the instance into the cache.
     *
     * @see twetailer.dao.CacheHandler#CacheHandler(String)
     */
    public CacheHandler(String className, String[] keys) {
        this(className);
        this.keys = keys;
    }

    /**
     * Helper storing the given instance under each entry specified when the CacheHandler instance has been initialized
     *
     * @param instance Data to be inserted in the cache, if the values associated to the initial keys are not null.
     * @return Given instance for chaining purposes
     */
    public T cacheInstance(T instance) {
        if (instance != null) {
            JsonObject data = instance.toJson();
            for (int idx = 0; idx < keys.length; idx ++) {
                String key = keys[idx];
                if (data.containsKey(key)) {
                    Object value = data.getMap().get(key); // Use the map because we don't know the type of value, then it can stay as an Object instance
                    if (value != null) {
                        setInCache(keyPrefix + key + "_" + value, data);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Helper removing the given instance from the cache under each entry specified when the CacheHandler instance has been initialized
     *
     * @param instance Data to be removed from the cache.
     * @return Given instance for chaining purposes
     */
    public T decacheInstance(T instance) {
        if (instance != null) {
            JsonObject data = instance.toJson();
            for (int idx = 0; idx < keys.length; idx ++) {
                String key = keys[idx];
                if (data.containsKey(key)) {
                    Object value = data.getMap().get(key); // Use the map because we don't know the type of value, then it can stay as an Object instance
                    if (value != null) {
                        resetInCache(keyPrefix + key + "_" + value);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Return the identified instance if found into the cache
     *
     * @param key Identifier of the attribute used for the cache key
     * @param value Value used for the cache key
     * @return Valid class instance or <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public T getCachedInstance(String key, Object value) {
        if (value != null) {
            String cacheKey = keyPrefix + key + "_" + value;
            Object cached = getFromCache(cacheKey);
            if (cached != null) {
                try {
                    T instance = (T) Class.forName(className).newInstance();
                    return (T) instance.fromJson((JsonObject) cached, true, true);
                }
                catch(Exception ex) {
                    resetInCache(cacheKey);
                }
            }
        }
        return null;
    }
}
