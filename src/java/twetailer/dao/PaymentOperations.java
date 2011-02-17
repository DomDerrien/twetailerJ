package twetailer.dao;

import javax.jdo.PersistenceManager;

import twetailer.InvalidIdentifierException;
import twetailer.dto.Payment;

/**
 * Controller defining various methods used for the CRUD operations on Payment entities
 *
 * @author Dom Derrien
 */
public class PaymentOperations extends BaseOperations {

    /**
     * Create the Payment instance with the given parameters
     *
     * @param payment Resource to persist
     * @return Just created resource
     *
     * @see PaymentOperations#createPayment(PersistenceManager, Payment)
     */
    public Payment createPayment(Payment payment) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createPayment(pm, payment);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Payment instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param payment Resource to persist
     * @return Just created resource
     */
    public Payment createPayment(PersistenceManager pm, Payment payment) {
        return pm.makePersistent(payment);
    }

    /**
     * Use the given key to get the corresponding Payment instance
     *
     * @param key Identifier of the payment
     * @return First payment matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Payment record
     *
     * @see PaymentOperations#getPayment(PersistenceManager, Long)
     */
    public Payment getPayment(Long key) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getPayment(pm, key);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Payment instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the payment
     * @return First payment matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Payment record
     */
    public Payment getPayment(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the Payment instance");
        }
        try {
            return pm.getObjectById(Payment.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving payment for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param payment Resource to update
     * @return Updated resource
     *
     * @see PaymentOperations#updatePayment(PersistenceManager, Payment)
     */
    public Payment updatePayment(Payment payment) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updatePayment(pm, payment);
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
     * @param payment Resource to update
     * @return Updated resource
     */
    public Payment updatePayment(PersistenceManager pm, Payment payment) {
        return pm.makePersistent(payment);
    }
}
