package twetailer.dao;

import javax.jdo.PersistenceManager;

import twetailer.InvalidIdentifierException;
import twetailer.dto.ReviewSystem;

/**
 * Controller defining various methods used for the CRUD operations on ReviewSystem entities
 *
 * @author Dom Derrien
 */
public class ReviewSystemOperations extends BaseOperations {

    /**
     * Create the ReviewSystem instance with the given parameters
     *
     * @param reviewSystem Resource to persist
     * @return Just created resource
     *
     * @see ReviewSystemOperations#createReviewSystem(PersistenceManager, ReviewSystem)
     */
    public ReviewSystem createReviewSystem(ReviewSystem reviewSystem) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createReviewSystem(pm, reviewSystem);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the ReviewSystem instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param reviewSystem Resource to persist
     * @return Just created resource
     */
    public ReviewSystem createReviewSystem(PersistenceManager pm, ReviewSystem reviewSystem) {
        return pm.makePersistent(reviewSystem);
    }

    /**
     * Use the given key to get the corresponding ReviewSystem instance
     *
     * @param key Identifier of the ReviewSystem instance
     * @return First ReviewSystem instance matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid ReviewSystem record
     *
     * @see ReviewSystemOperations#getReviewSystem(PersistenceManager, Long)
     */
    public ReviewSystem getReviewSystem(Long key) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getReviewSystem(pm, key);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding ReviewSystem instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the ReviewSystem instance
     * @return First ReviewSystem instance matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid ReviewSystem record
     */
    public ReviewSystem getReviewSystem(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            ReviewSystem defaultReviewSystem = new ReviewSystem();
            defaultReviewSystem.setEmail("review-sytem-program@anothersocialeconomy.com");
            defaultReviewSystem.setName("AnotherSocialEconomy.com");
            defaultReviewSystem.setUrl("http://anothersocialeconomy.com/");
            return defaultReviewSystem;
        }
        try {
            return pm.getObjectById(ReviewSystem.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving ReviewSystem instance for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param reviewSystem Resource to update
     * @return Updated resource
     *
     * @see ReviewSystemOperations#updateReviewSystem(PersistenceManager, ReviewSystem)
     */
    public ReviewSystem updateReviewSystem(ReviewSystem reviewSystem) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updateReviewSystem(pm, reviewSystem);
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
     * @param ReviewSystem Resource to update
     * @return Updated resource
     */
    public ReviewSystem updateReviewSystem(PersistenceManager pm, ReviewSystem reviewSystem) {
        return pm.makePersistent(reviewSystem);
    }
}
