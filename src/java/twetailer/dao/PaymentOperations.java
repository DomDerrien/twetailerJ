package twetailer.dao;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.dto.Payment;

public class PaymentOperations extends BaseOperations {
    private static Logger log = Logger.getLogger(PaymentOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

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
     * @return First payment matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved payment does not belong to the specified user
     *
     * @see PaymentOperations#getPayment(PersistenceManager, Long)
     */
    public Payment getPayment(Long key) throws DataSourceException {
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
     * @return First payment matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved payment does not belong to the specified user
     */
    public Payment getPayment(PersistenceManager pm, Long key) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new IllegalArgumentException("Invalid key; cannot retrieve the Payment instance");
        }
        getLogger().warning("Get Payment instance with id: " + key);
        try {
            return pm.getObjectById(Payment.class, key);
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving payment for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
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
