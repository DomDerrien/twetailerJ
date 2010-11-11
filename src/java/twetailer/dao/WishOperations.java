package twetailer.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Location;
import twetailer.dto.Wish;
import twetailer.validator.CommandSettings.State;
import domderrien.jsontools.JsonObject;

/**
 * Controller defining various methods used for the CRUD operations on Wish entities
 *
 * @author Dom Derrien
 */
public class WishOperations extends BaseOperations {

    /**
     * Create the Wish instance with the given parameters
     *
     * @param parameters HTTP wish parameters
     * @param ownerKey Identifier of the wish owner
     * @return Just created resource
     *
     * @throws ClientException If the data given by the client are incorrect
     *
     * @see WishOperations#createWish(Wish)
     */
    public Wish createWish(JsonObject parameters, Long ownerKey) throws ClientException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createWish(pm, parameters, ownerKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Wish instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters HTTP wish parameters
     * @param ownerKey Identifier of the wish owner
     * @return Just created resource
     *
     * @throws ClientException If the data given by the client are incorrect
     *
     * @see WishOperations#createWish(PersistenceManager, Wish)
     */
    public Wish createWish(PersistenceManager pm, JsonObject parameters, Long ownerKey) throws ClientException {
        // Creates new wish record and persist it
        Wish newWish = new Wish(parameters);
        // Updates the identifier of the creator owner
        Long ownerId = newWish.getOwnerKey();
        if (ownerId == null || ownerId == 0L) {
            newWish.setOwnerKey(ownerKey);
        }
        else if (!ownerKey.equals(ownerId)) {
            throw new ClientException("Mismatch of owner identifiers [" + ownerId + "/" + ownerKey + "]");
        }
        // Persist it
        return createWish(pm, newWish);
    }

    /**
     * Create the Wish instance with the given parameters
     *
     * @param wish Resource to persist
     * @return Just created resource
     */
    public Wish createWish(Wish wish) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createWish(pm, wish);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Wish instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param wish Resource to persist
     * @return Just created resource
     */
    public Wish createWish(PersistenceManager pm, Wish wish) {
        return pm.makePersistent(wish);
    }

    /**
     * Use the given reference to get the corresponding Wish instance for the identified consumer
     *
     * @param key Identifier of the wish
     * @param ownerKey Identifier of the wish owner
     * @return First wish matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Wish record
     *
     * @see WishOperations#getWish(PersistenceManager, Long, Long)
     */
    public Wish getWish(Long key, Long ownerKey) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getWish(pm, key, ownerKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given reference to get the corresponding Wish instance for the identified consumer while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the wish
     * @param ownerKey Identifier of the wish owner
     * @return First wish matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Wish record
     */
    public Wish getWish(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the Wish instance");
        }
        try {
            Wish wish = pm.getObjectById(Wish.class, key);
            if (ownerKey != null && !ownerKey.equals(wish.getOwnerKey())) {
                throw new InvalidIdentifierException("Mismatch of owner identifiers [" + ownerKey + "/" + wish.getOwnerKey() + "]");
            }
            if (State.markedForDeletion.equals(wish.getState())) {
                throw new InvalidIdentifierException("Invalid key; entity marked for deletion.");
            }
            wish.getCriteria().size(); // FIXME: remove workaround for a bug in DataNucleus
            return wish;
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving wish for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Wish instances
     *
     * @param attribute Name of the wish attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of wishes matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see WishOperations#getWishes(PersistenceManager, String, Object)
     */
    public List<Wish> getWishes(String attribute, Object value, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getWishes(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Wish instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the wish attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of wishes matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Wish> getWishes(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Wish.class);
        try {
            value = prepareQuery(query, attribute, value, limit);
            // Select the corresponding resources
            List<Wish> wishes = (List<Wish>) query.execute(value);
            wishes.size(); // FIXME: remove workaround for a bug in DataNucleus
            return wishes;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Wish identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the wish attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of wish identifiers matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getWishKeys(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery("select " + Wish.KEY + " from " + Wish.class.getName());
        try {
            value = prepareQuery(query, attribute, value, limit);
            // Select the corresponding resources
            List<Long> wishKeys = (List<Long>) query.execute(value);
            wishKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
            return wishKeys;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Wish instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of wishes matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Wish> getWishes(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Wish.class);
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<Wish> wishes = (List<Wish>) query.executeWithArray(values);
            wishes.size(); // FIXME: remove workaround for a bug in DataNucleus
            return wishes;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Wish identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of wish keys matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getWishKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery("select " + Wish.KEY + " from " + Wish.class.getName());
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<Long> wishKeys = (List<Long>) query.executeWithArray(values);
            wishKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
            return wishKeys;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Wish instances
     *
     * @param locations list of locations where expected wishes should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of wishes matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see WishOperations#getWishes(PersistenceManager, String, Object)
     */
    public List<Wish> getWishes(List<Location> locations, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getWishes(pm, new HashMap<String, Object>(), locations, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Wish instances while leaving the given persistence manager open for future updates
     *
     * Note that this command only return Wish not cancelled, not marked-for-deletion, not closed (see Wish.stateCmdList attribute and Wish.setState() method).
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param queryParameters Map of attributes and values to match
     * @param locations list of locations where expected wishes should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of wishes matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public List<Wish> getWishes(PersistenceManager pm, Map<String, Object> queryParameters, List<Location> locations, int limit) throws DataSourceException {
        List<Wish> selection = new ArrayList<Wish>();
        for (Location location: locations) {
            // Select the corresponding resources
            queryParameters.put(Wish.LOCATION_KEY, location.getKey());
            List<Wish> wishes = getWishes(pm, queryParameters, limit);
            // Copy into the list to be returned
            selection.addAll(wishes);
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
     * Get the identified Wish instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param wishKeys list of Wish instance identifiers
     * @return Collection of wishes matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Wish> getWishes(PersistenceManager pm, List<Long> wishKeys) throws DataSourceException {
        // Select the corresponding resources
        Query query = pm.newQuery(Wish.class, ":p.contains(key)"); // Reported as being more efficient than pm.getObjectsById()
        try {
            List<Wish> wishes = (List<Wish>) query.execute(wishKeys);
            wishes.size(); // FIXME: remove workaround for a bug in DataNucleus
            return wishes;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Load the wish matching the given parameters and persist the result of the merge
     *
     * @param parameters List of updated attributes, plus the resource identifier (cannot be changed)
     * @param ownerKey Identifier of the owner issuing the operation
     * @return Updated resource
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Wish record
     *
     * @see WishOperations#updateWish(PersistenceManager, Wish)
     */
    public Wish updateWish(JsonObject parameters, Long ownerKey) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updateWish(pm, parameters, ownerKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Load the wish matching the given parameters and persist the result of the merge
     * while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters List of updated attributes, plus the resource identifier (cannot be changed)
     * @param ownerKey Identifier of the owner issuing the operation
     * @return Updated resource
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Wish record
     *
     * @see WishOperations#updateWish(PersistenceManager, Wish)
     */
    public Wish updateWish(PersistenceManager pm, JsonObject parameters, Long ownerKey) throws InvalidIdentifierException {
        // Get the original wish
        Wish updatedWish = getWish(pm, parameters.getLong(Wish.KEY), ownerKey);
        // Merge with the updates
        updatedWish.fromJson(parameters);
        // Persist updated wish
        return updateWish(pm, updatedWish);
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param wish Resource to update
     * @return Updated resource
     *
     * @see WishOperations#updateWish(PersistenceManager, Wish)
     */
    public Wish updateWish(Wish wish) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated wish
            return updateWish(pm, wish);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param wish Resource to update
     * @return Updated resource
     */
    public Wish updateWish(PersistenceManager pm, Wish wish) {
        return pm.makePersistent(wish);
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Wish instance and to delete it
     *
     * @param key Identifier of the wish
     * @param ownerKey Identifier of the wish owner
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Wish record
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Wish record
     * @see WishOperations#deleteWish(PersistenceManager, Long)
     */
    public void deleteWish(Long key, Long ownerKey) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            deleteWish(pm, key, ownerKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Wish instance and to delete it
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the wish
     * @param ownerKey Identifier of the wish owner
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Wish record
     *
     * @see WishOperations#getWishes(PersistenceManager, Long, Long)
     * @see WishOperations#deleteWish(PersistenceManager, Wish)
     */
    public void deleteWish(PersistenceManager pm, Long key, Long ownerKey) throws InvalidIdentifierException {
        Wish wish = getWish(pm, key, ownerKey);
        deleteWish(pm, wish);
    }

    /**
     * Delete the given wish while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the wish
     */
    public void deleteWish(PersistenceManager pm, Wish wish) {
        pm.deletePersistent(wish);
    }
}
