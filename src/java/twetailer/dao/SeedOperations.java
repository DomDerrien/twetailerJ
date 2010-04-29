package twetailer.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.DataSourceException;
import twetailer.dto.Seed;

public class SeedOperations extends BaseOperations {
    private static Logger log = Logger.getLogger(ProductOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Seed instance with the given parameters
     *
     * @param seed Resource to persist
     * @return Just created resource
     *
     * @throws DataSourceException If the given seed that is read from the object or created from its data is invalid
     *
     * @see SeedOperations#createSeed(PersistenceManager, Seed)
     */
    public Seed createSeed(Seed seed) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createSeed(pm, seed);
        }
        finally {
            pm.close();
        }
    }


    /**
     * Create the Seed instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param seed Resource to persist
     * @return Just created resource
     *
     * @throws DataSourceException If the given seed that is read from the object or created from its data is invalid
     *
     * @see SeedOperations#getSeed(PersistenceManager, String)
     */
    public Seed createSeed(PersistenceManager pm, Seed seed) throws DataSourceException {
        // Check if the seed already exists
        if (seed.getKey() != null) {
            return getSeed(pm, seed.getKey());
        }
        // Create an entry for that new seed
        seed.setKey(seed.buildQueryString());
        return pm.makePersistent(seed);
    }

    /**
     * Use the given key to get the corresponding Seed instance
     *
     * @param country Country identifier
     * @param region Region identifier
     * @city City name
     * @return First location matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved location does not belong to the specified user
     *
     * @see SeedOperations#getSeed(PersistenceManager, String, String, String)
     */
    public Seed getSeed(String country, String region, String city) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getSeed(pm, country, region, city);
        }
        finally {
            pm.close();
        }
    }


    /**
     * Use the given key to get the corresponding Seed instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param country Country identifier
     * @param region Region identifier
     * @city City name
     * @return First location matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved location does not belong to the specified user
     *
     * @see SeedOperations#getSeed(String)
     */
    public Seed getSeed(PersistenceManager pm, String country, String region, String city) throws DataSourceException {
        return getSeed(Seed.generateKey(Seed.buildQueryString(country, region, city)));
    }

    /**
     * Use the given key to get the corresponding Seed instance
     *
     * @param key Identifier of the location
     * @return First location matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved location does not belong to the specified user
     *
     * @see SeedOperations#getSeed(PersistenceManager, String)
     */
    public Seed getSeed(String key) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getSeed(pm, key);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Seed instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the location
     * @return First location matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the location cannot be retrieved
     */
    public Seed getSeed(PersistenceManager pm, String key) throws DataSourceException {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Invalid key; cannot retrieve the Seed instance");
        }
        getLogger().warning("Get Seed instance with id: " + key);
        try {
            return pm.getObjectById(Seed.class, key);
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving seed for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Get as much as possible Seed instances
     *
     * @return List of Seed instances
     */
    public List<Seed> getAllSeeds() {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getAllSeeds(pm);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Get as much as possible Seed instances
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @return List of Seed instances
     */
    @SuppressWarnings("unchecked")
    public List<Seed> getAllSeeds(PersistenceManager pm) {
        Query queryObj = pm.newQuery(Seed.class);
        List<Seed> seeds = (List<Seed>) queryObj.execute();
        seeds.size(); // FIXME: remove workaround for a bug in DataNucleus
        return seeds;
    }

    /**
     * Persist the updated Seed instance
     *
     * @param seed Resource to persist
     * @return Just created resource
     *
     * @throws DataSourceException If the given seed that is read from the object or created from its data is invalid
     *
     * @see SeedOperations#updateSeed(PersistenceManager, Seed)
     */
    public Seed updateSeed(Seed seed) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createSeed(pm, seed);
        }
        finally {
            pm.close();
        }
    }


    /**
     * Persist the updated Seed instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param seed Resource to persist
     * @return Just updated resource
     */
    public Seed updateSeed(PersistenceManager pm, Seed seed) {
        return pm.makePersistent(seed);
    }
}
