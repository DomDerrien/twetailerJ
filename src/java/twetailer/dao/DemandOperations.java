package twetailer.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.task.CommandProcessor;
import domderrien.jsontools.JsonObject;

public class DemandOperations extends BaseOperations {
    private static Logger log = Logger.getLogger(DemandOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Demand instance with the given parameters
     *
     * @param parameters HTTP demand parameters
     * @param ownerKey Identifier of the demand owner
     * @return Just created resource
     *
     * @throws ClientException If the data given by the client are incorrect
     *
     * @see DemandOperations#createDemand(Demand)
     */
    public Demand createDemand(JsonObject parameters, Long ownerKey) throws ClientException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createDemand(pm, parameters, ownerKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Demand instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters HTTP demand parameters
     * @param ownerKey Identifier of the demand owner
     * @return Just created resource
     *
     * @throws ClientException If the data given by the client are incorrect
     *
     * @see DemandOperations#createDemand(PersistenceManager, Demand)
     */
    public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long ownerKey) throws ClientException {
        getLogger().warning("Create demand for owner id: " + ownerKey + " with: " + parameters.toString());
        // Creates new demand record and persist it
        Demand newDemand = new Demand(parameters);
        // Updates the identifier of the creator owner
        Long ownerId = newDemand.getOwnerKey();
        if (ownerId == null || ownerId == 0L) {
            newDemand.setOwnerKey(ownerKey);
        }
        else if (!ownerKey.equals(ownerId)) {
            throw new ClientException("Mismatch of owner identifiers [" + ownerId + "/" + ownerKey + "]");
        }
        // Persist it
        return createDemand(pm, newDemand);
    }

    /**
     * Create the Demand instance with the given parameters
     *
     * @param demand Resource to persist
     * @return Just created resource
     */
    public Demand createDemand(Demand demand) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createDemand(pm, demand);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Demand instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demand Resource to persist
     * @return Just created resource
     */
    public Demand createDemand(PersistenceManager pm, Demand demand) {
        pm.makePersistent(demand);
        return demand;
    }

    /**
     * Use the given reference to get the corresponding Demand instance for the identified consumer
     *
     * @param key Identifier of the demand
     * @param ownerKey Identifier of the demand owner
     * @return First demand matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved demand does not belong to the specified user
     *
     * @see DemandOperations#getDemand(PersistenceManager, Long, Long)
     */
    public Demand getDemand(Long key, Long ownerKey) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getDemand(pm, key, ownerKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given reference to get the corresponding Demand instance for the identified consumer while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the demand
     * @param ownerKey Identifier of the demand owner
     * @return First demand matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved demand does not belong to the specified user
     */
    public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new IllegalArgumentException("Invalid key; cannot retrieve the Demand instance");
        }
        getLogger().warning("Get Demand instance with id: " + key);
        try {
            Demand demand = pm.getObjectById(Demand.class, key);
            if (ownerKey != null && !ownerKey.equals(demand.getOwnerKey())) {
                throw new DataSourceException("Mismatch of owner identifiers [" + ownerKey + "/" + demand.getOwnerKey() + "]");
            }
            demand.getCriteria().size(); // FIXME: remove workaround for a bug in DataNucleus
            return demand;
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving demand for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instances
     *
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of demands matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see DemandOperations#getDemands(PersistenceManager, String, Object)
     */
    public List<Demand> getDemands(String attribute, Object value, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getDemands(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of demands matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Demand> getDemands(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Demand.class);
        value = prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select demand(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Demand> demands = (List<Demand>) queryObj.execute(value);
        demands.size(); // FIXME: remove workaround for a bug in DataNucleus
        return demands;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of demand identifiers matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getDemandKeys(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery("select " + Demand.KEY + " from " + Demand.class.getName());
        value = prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select demand(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Long> demandKeys = (List<Long>) queryObj.execute(value);
        demandKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
        return demandKeys;
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Demand instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of demands matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Demand.class);
        Object[] values = prepareQuery(query, parameters, limit);
        getLogger().warning("Select demand(s) with: " + query.toString());
        // Select the corresponding resources
        List<Demand> demands = (List<Demand>) query.executeWithArray(values);
        demands.size(); // FIXME: remove workaround for a bug in DataNucleus
        return demands;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instances
     *
     * @param locations list of locations where expected demands should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of demands matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see DemandOperations#getDemands(PersistenceManager, String, Object)
     */
    public List<Demand> getDemands(List<Location> locations, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getDemands(pm, locations, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instances while leaving the given persistence manager open for future updates
     *
     * Note that this command only return Demand not canceled, not marked-for-deletion, not closed (see Demand.stateCmdList attribute and Demand.setState() method).
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param locations list of locations where expected demands should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of demands matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public List<Demand> getDemands(PersistenceManager pm, List<Location> locations, int limit) throws DataSourceException {
        List<Demand> selection = new ArrayList<Demand>();
        for (Location location: locations) {
            // Select the corresponding resources
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(Demand.LOCATION_KEY, location.getKey());
            parameters.put(Demand.STATE_COMMAND_LIST, Boolean.TRUE);
            List<Demand> demands = CommandProcessor.demandOperations.getDemands(pm, parameters, limit);
            // Copy into the list to be returned
            selection.addAll(demands);
            if (limit != 0) {
                if (limit <= selection.size()) {
                    break;
                }
                limit = limit - selection.size();
            }
        }
        return selection;
    }

    /**
     * Load the demand matching the given parameters and persist the result of the merge
     *
     * @param parameters List of updated attributes, plus the resource identifier (cannot be changed)
     * @param ownerKey Identifier of the owner issuing the operation
     * @return Updated resource
     *
     * @throws DataSourceException If the identified resource does not belong to the issuing owner
     *
     * @see DemandOperations#updateDemand(PersistenceManager, Demand)
     */
    public Demand updateDemand(JsonObject parameters, Long ownerKey) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            Demand updatedDemand = getDemand(pm, parameters.getLong(Demand.KEY), ownerKey);
            updatedDemand.fromJson(parameters);
            // Persist updated demand
            return updateDemand(pm, updatedDemand);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param demand Resource to update
     * @return Updated resource
     *
     * @see DemandOperations#updateDemand(PersistenceManager, Demand)
     */
    public Demand updateDemand(Demand demand) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated demand
            return updateDemand(pm, demand);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demand Resource to update
     * @return Updated resource
     */
    public Demand updateDemand(PersistenceManager pm, Demand demand) {
        getLogger().warning("Updating demand with id: " + demand.getKey());
        pm.makePersistent(demand);
        return demand;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance and to delete it
     *
     * @param key Identifier of the demand
     * @param ownerKey Identifier of the demand owner
     *
     * @throws DataSourceException If the retrieved demand does not belong to the specified user
     *
     * @see DemandOperations#deleteDemand(PersistenceManager, Long)
     */
    public void deleteDemand(Long key, Long ownerKey) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            deleteDemand(pm, key, ownerKey);
        }
        finally {
            pm.close();
        }
    }


    /**
     * Use the given pair {attribute; value} to get the corresponding Demand instance and to delete it
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the demand
     * @param ownerKey Identifier of the demand owner
     *
     * @throws DataSourceException If the retrieved demand does not belong to the specified user
     *
     * @see DemandOperations#getDemands(PersistenceManager, Long, Long)
     * @see DemandOperations#deleteDemand(PersistenceManager, Demand)
     */
    public void deleteDemand(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
        Demand demand = getDemand(pm, key, ownerKey);
        deleteDemand(pm, demand);
    }

    /**
     * Delete the given demand while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the demand
     */
    public void deleteDemand(PersistenceManager pm, Demand demand) {
        getLogger().warning("Delete demand with id: " + demand.getKey());
        pm.deletePersistent(demand);
    }
}
