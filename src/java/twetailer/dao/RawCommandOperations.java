package twetailer.dao;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.InvalidIdentifierException;
import twetailer.dto.RawCommand;

/**
 * Controller defining various methods used for the CRUD operations on RawCommand entities
 *
 * @author Dom Derrien
 */
public class RawCommandOperations extends BaseOperations {
    private static Logger log = Logger.getLogger(RawCommandOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

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
        getLogger().warning("Get RawCommand instance with id: " + key);
        try {
            return pm.getObjectById(RawCommand.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving rawCommand for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
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
        getLogger().warning("Delete raw command with id: " + rawCommand.getKey());
        pm.deletePersistent(rawCommand);
    }
}
