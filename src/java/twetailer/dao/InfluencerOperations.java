package twetailer.dao;

import javax.jdo.PersistenceManager;

import twetailer.InvalidIdentifierException;
import twetailer.dto.Influencer;

/**
 * Controller defining various methods used for the CRUD operations on Influencer entities
 *
 * @author Dom Derrien
 */
public class InfluencerOperations extends BaseOperations {

    /**
     * Create the Influencer instance with the given parameters
     *
     * @param influencer Resource to persist
     * @return Just created resource
     *
     * @see InfluencerOperations#createInfluencer(PersistenceManager, Influencer)
     */
    public Influencer createInfluencer(Influencer influencer) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createInfluencer(pm, influencer);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Influencer instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param influencer Resource to persist
     * @return Just created resource
     */
    public Influencer createInfluencer(PersistenceManager pm, Influencer influencer) {
        return pm.makePersistent(influencer);
    }

    /**
     * Use the given key to get the corresponding Influencer instance
     *
     * @param key Identifier of the Influencer instance
     * @return First Influencer instance matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Influencer record
     *
     * @see InfluencerOperations#getInfluencer(PersistenceManager, Long)
     */
    public Influencer getInfluencer(Long key) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getInfluencer(pm, key);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Influencer instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the Influencer instance
     * @return First Influencer instance matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Influencer record
     */
    public Influencer getInfluencer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            Influencer defaultInfluencer = new Influencer();
            defaultInfluencer.setEmail("influencer-program@anothersocialeconomy.com");
            defaultInfluencer.setName("AnotherSocialEconomy.com");
            defaultInfluencer.setUrl("http://anothersocialeconomy.com/");
            return defaultInfluencer;
        }
        try {
            return pm.getObjectById(Influencer.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving Influencer instance for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param influencer Resource to update
     * @return Updated resource
     *
     * @see InfluencerOperations#updateInfluencer(PersistenceManager, Influencer)
     */
    public Influencer updateInfluencer(Influencer influencer) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updateInfluencer(pm, influencer);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param influencer Resource to update
     * @return Updated resource
     */
    public Influencer updateInfluencer(PersistenceManager pm, Influencer influencer) {
        return pm.makePersistent(influencer);
    }
}
