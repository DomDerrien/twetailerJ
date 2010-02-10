package twetailer.dao;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.dto.RawCommand;

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
        pm.makePersistent(rawCommand);
        return rawCommand;
    }

    /**
     * Use the given key to get the corresponding RawCommand instance
     *
     * @param key Identifier of the rawCommand
     * @return First rawCommand matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved rawCommand does not belong to the specified user
     *
     * @see RawCommandOperations#getRawCommand(PersistenceManager, Long)
     */
    public RawCommand getRawCommand(Long key) throws DataSourceException {
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
     * @throws DataSourceException If the retrieved rawCommand does not belong to the specified user
     */
    public RawCommand getRawCommand(PersistenceManager pm, Long key) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new IllegalArgumentException("Invalid key; cannot retrieve the RawCommand instance");
        }
        getLogger().warning("Get RawCommand instance with id: " + key);
        try {
            RawCommand rawCommand = pm.getObjectById(RawCommand.class, key);
            return rawCommand;
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving rawCommand for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
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
        getLogger().warning("Updating rawCommand with id: " + rawCommand.getKey());
        pm.makePersistent(rawCommand);
        return rawCommand;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance and to delete it
     *
     * @param rawCommandKey Identifier of the raw command
     *
     * @throws DataSourceException If the raw command record retrieval fails
     *
     * @see RawCommandOperations#deleteRawCommand(PersistenceManager, Long)
     */
    public void deleteRawCommand(Long rawCommandKey) throws DataSourceException {
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
     * @throws DataSourceException If the raw command record retrieval fails
     *
     * @see RawCommandOperations#getRawCommands(PersistenceManager, Long)
     * @see RawCommandOperations#deleteRawCommand(PersistenceManager, RawCommand)
     */
    public void deleteRawCommand(PersistenceManager pm, Long rawCommandKey) throws DataSourceException {
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
