package twetailer.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Registrar;

/**
 * Controller defining various methods used for the CRUD operations on Registrar entities
 *
 * @author Dom Derrien
 */
public class RegistrarOperations extends BaseOperations {

    /**
     * Create the Registrar instance with the given parameters
     *
     * @param registrar Resource to persist
     * @return Just created resource
     *
     * @see RegistrarOperations#createRegistrar(PersistenceManager, Registrar)
     */
    public Registrar createRegistrar(Registrar registrar) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createRegistrar(pm, registrar);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Registrar instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param registrar Resource to persist
     * @return Just created resource
     */
    public Registrar createRegistrar(PersistenceManager pm, Registrar registrar) {
        return pm.makePersistent(registrar);
    }

    /**
     * Use the given key to get the corresponding Registrar instance
     *
     * @param key Identifier of the Registrar instance
     * @return First Registrar instance matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Registrar record
     *
     * @see RegistrarOperations#getRegistrar(PersistenceManager, Long)
     */
    public Registrar getRegistrar(Long key) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getRegistrar(pm, key);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Registrar instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the Registrar instance
     * @return First Registrar instance matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Registrar record
     */
    public Registrar getRegistrar(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            Registrar defaultRegistrar = new Registrar();
            defaultRegistrar.setEmail("registrar-program@anothersocialeconomy.com");
            defaultRegistrar.setName("AnotherSocialEconomy.com");
            defaultRegistrar.setUrl("http://anothersocialeconomy.com/");
            return defaultRegistrar;
        }
        try {
            return pm.getObjectById(Registrar.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving Registrar instance for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Get the identified Registrar instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param registrarKeys list of Registrar instance identifiers
     * @return Collection of registrars matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Registrar> getRegistrars(PersistenceManager pm, List<Long> registrarKeys) throws DataSourceException {
        List<Registrar> registrars;
        boolean defaultRequired = registrarKeys.remove(0L);
        if (registrarKeys.size() == 0) {
            registrars = new ArrayList<Registrar>();
        }
        else {
            // Select the corresponding resources
            Query query = pm.newQuery(Registrar.class, ":p.contains(key)"); // Reported as being more efficient than pm.getObjectsById()
            try {
                List<Registrar> results = (List<Registrar>) query.execute(registrarKeys);
                results.size(); // FIXME: remove workaround for a bug in DataNucleus
                // Copies the non-modifiable registrar list if it has to be updated later
                registrars = defaultRequired ? new ArrayList<Registrar>(results) : results;
            }
            finally {
                query.closeAll();
            }
        }
        if (defaultRequired) {
            try {
                registrars.add(getRegistrar(pm, 0L));
            }
            catch (InvalidIdentifierException e) {} // Impossible Exception is this case as Registrar(0L) is hardcoded!
        }
        return registrars;
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param registrar Resource to update
     * @return Updated resource
     *
     * @see RegistrarOperations#updateRegistrar(PersistenceManager, Registrar)
     */
    public Registrar updateRegistrar(Registrar registrar) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updateRegistrar(pm, registrar);
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
     * @param registrar Resource to update
     * @return Updated resource
     */
    public Registrar updateRegistrar(PersistenceManager pm, Registrar registrar) {
        return pm.makePersistent(registrar);
    }
}
