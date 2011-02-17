package twetailer.dao;

import javax.jdo.PersistenceManager;

import twetailer.InvalidIdentifierException;
import twetailer.dto.Reseller;

/**
 * Controller defining various methods used for the CRUD operations on Reseller entities
 *
 * @author Dom Derrien
 */
public class ResellerOperations extends BaseOperations {

    /**
     * Create the Reseller instance with the given parameters
     *
     * @param reseller Resource to persist
     * @return Just created resource
     *
     * @see ResellerOperations#createReseller(PersistenceManager, Reseller)
     */
    public Reseller createReseller(Reseller reseller) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createReseller(pm, reseller);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Reseller instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param reseller Resource to persist
     * @return Just created resource
     */
    public Reseller createReseller(PersistenceManager pm, Reseller reseller) {
        return pm.makePersistent(reseller);
    }

    /**
     * Use the given key to get the corresponding Reseller instance
     *
     * @param key Identifier of the Reseller instance
     * @return First Reseller instance matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Reseller record
     *
     * @see ResellerOperations#getReseller(PersistenceManager, Long)
     */
    public Reseller getReseller(Long key) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getReseller(pm, key);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Reseller instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the Reseller instance
     * @return First Reseller instance matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Reseller record
     */
    public Reseller getReseller(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            Reseller defaultReseller = new Reseller();
            defaultReseller.setTokenNb(1000000000L);
            return defaultReseller;
        }
        try {
            return pm.getObjectById(Reseller.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving Reseller instance for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param reseller Resource to update
     * @return Updated resource
     *
     * @see ResellerOperations#updateReseller(PersistenceManager, Reseller)
     */
    public Reseller updateReseller(Reseller reseller) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updateReseller(pm, reseller);
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
     * @param reseller Resource to update
     * @return Updated resource
     */
    public Reseller updateReseller(PersistenceManager pm, Reseller reseller) {
        return pm.makePersistent(reseller);
    }
}
