package twetailer.dao;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.RawCommand;

/**
 * Controller defining various methods used for the CRUD operations on RawCommand entities
 *
 * @author Dom Derrien
 */
public class RawCommandOperations extends BaseOperations {

    /**
     * Create the RawCommand instance with the given parameters
     *
     * @param rawCommand Resource to persist
     * @return Just created resource
     *
     * @see RawCommandsOperations#createRawCommand(PersistenceManager, RawCommand)
     */
    public RawCommand createRawCommand(RawCommand rawCommand) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createRawCommand(pm, rawCommand);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the RawCommand instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand Resource to persist
     * @return Just created resource
     */
    public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
        return pm.makePersistent(rawCommand);
    }

    /**
     * Use the given key to get the corresponding RawCommand instance
     *
     * @param key Identifier of the rawCommand
     * @return First rawCommand matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid RawCommand record
     *
     * @see RawCommandOperations#getRawCommand(PersistenceManager, Long)
     */
    public RawCommand getRawCommand(Long key) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getRawCommand(pm, key);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding RawCommand instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the rawCommand
     * @return First rawCommand matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid RawCommand record
     */
    public RawCommand getRawCommand(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the RawCommand instance");
        }
        try {
            return pm.getObjectById(RawCommand.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving rawCommand for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding RawCommand identifiers while leaving the given persistence manager open for future updates
     *
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of rawCommand keys matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data rawCommand type
     *
     * @see RawCommandOperations#getRawCommandKeys(PersistenceManager, Map<String, Object>, int)
     */
    public List<Long> getRawCommandKeys(Map<String, Object> parameters, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getRawCommandKeys(pm, parameters, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding RawCommand identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of rawCommand keys matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data rawCommand type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getRawCommandKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery("select " + RawCommand.KEY + " from " + RawCommand.class.getName());
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<Long> rawCommandKeys = (List<Long>) query.executeWithArray(values);
            rawCommandKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
            return rawCommandKeys;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param rawCommand Resource to update
     * @return Updated resource
     *
     * @see RawCommandOperations#updateRawCommand(PersistenceManager, RawCommand)
     */
    public RawCommand updateRawCommand(RawCommand rawCommand) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated rawCommand
            return updateRawCommand(pm, rawCommand);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand Resource to update
     * @return Updated resource
     */
    public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
        return pm.makePersistent(rawCommand);
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance and to delete it
     *
     * @param rawCommandKey Identifier of the raw command
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid RawCommand record
     *
     * @see RawCommandOperations#deleteRawCommand(PersistenceManager, Long)
     */
    public void deleteRawCommand(Long rawCommandKey) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            deleteRawCommand(pm, rawCommandKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance and to delete it
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommandKey Identifier of the raw command
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid RawCommand record
     *
     * @see RawCommandOperations#getRawCommands(PersistenceManager, Long)
     * @see RawCommandOperations#deleteRawCommand(PersistenceManager, RawCommand)
     */
    public void deleteRawCommand(PersistenceManager pm, Long rawCommandKey) throws InvalidIdentifierException {
        RawCommand rawCommand = getRawCommand(pm, rawCommandKey);
        deleteRawCommand(pm, rawCommand);
    }

    /**
     * Delete the given demand while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand Object to delete
     */

    public void deleteRawCommand(PersistenceManager pm, RawCommand rawCommand) {
        pm.deletePersistent(rawCommand);
    }
}
