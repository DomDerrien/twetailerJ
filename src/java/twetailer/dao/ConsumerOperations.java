package twetailer.dao;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.users.User;
import twetailer.DataSourceException;
import twetailer.dto.Consumer;

public class ConsumerOperations extends BaseOperations {
    private static final Logger log = Logger.getLogger(ConsumerOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     * 
     * @param loggedUser System entity to attach with the just created user
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     * 
     * @see ConsumerOperations#createConsumer(PersistenceManager, User)
     */
    public Consumer createConsumer(User loggedUser) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createConsumer(pm, loggedUser);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param loggedUser System entity to attach with the just created user
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     */
    public Consumer createConsumer(PersistenceManager pm, User loggedUser) {
        try {
            // Try to retrieve the same location
            List<Consumer> consumers = getConsumers(pm, Consumer.EMAIL, loggedUser.getEmail(), 1);
            if (0 < consumers.size()) {
                return consumers.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Creates new consumer record and persist it
        Consumer newConsumer = new Consumer();
        newConsumer.setName(loggedUser.getNickname());
        newConsumer.setEmail(loggedUser.getEmail());
        pm.makePersistent(newConsumer);
        return newConsumer;
    }

    /**
     * Create the Consumer instance
     * 
     * @param twitterId Twitter identifier to be used to identify the new consumer account
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     * 
     * @throws DataSourceException Forward error reported when trying to get a consumer record
     * 
     * @see ConsumerOperations#createConsumer(PersistenceManager, twitter4j.User)
     */
    public Consumer createConsumer(twitter4j.User twitterUser) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createConsumer(pm, twitterUser);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Consumer instance
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param twitterId Twitter identifier to be used to identify the new consumer account
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     * 
     * @throws DataSourceException Forward error reported when trying to get a consumer record
     */
    public Consumer createConsumer(PersistenceManager pm, twitter4j.User twitterUser) throws DataSourceException {
        try {
            // Try to retrieve the same location
            List<Consumer> consumers = getConsumers(pm, Consumer.TWITTER_ID, twitterUser.getId(), 1);
            if (0 < consumers.size()) {
                return consumers.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Creates new consumer record and persist it
        Consumer newConsumer = new Consumer();
        newConsumer.setName(twitterUser.getName());
        newConsumer.setAddress(twitterUser.getLocation());
        newConsumer.setTwitterId(Long.valueOf(twitterUser.getId()));
        pm.makePersistent(newConsumer);
        return newConsumer;
    }

    /**
     * Use the given key to get the corresponding Consumer instance
     * 
     * @param key Identifier of the consumer
     * @return First consumer matching the given criteria or <code>null</code>
     * 
     * @throws DataSourceException If the retrieved consumer does not belong to the specified user
     * 
     * @see ConsumerOperations#getConsumer(PersistenceManager, Long)
     */
    public Consumer getConsumer(Long key) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getConsumer(pm, key);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Consumer instance
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the consumer
     * @return First consumer matching the given criteria or <code>null</code>
     * 
     * @throws DataSourceException If the retrieved consumer does not belong to the specified user
     */
    public Consumer getConsumer(PersistenceManager pm, Long key) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new InvalidParameterException("Invalid key; cannot retrieve the Consumer instance");
        }
        getLogger().warning("Get Consumer instance with id: " + key);
        try {
            Consumer consumer = pm.getObjectById(Consumer.class, key);
            return consumer;
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving consumer for identifier: " + key + " -- ex: " + ex.getMessage());
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Consumer instances
     * 
     * @param attribute Name of the consumer attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of consumers matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     * 
     * @see ConsumerOperations#getConsumers(PersistenceManager, String, Object)
     */
    public List<Consumer> getConsumers(String attribute, Object value, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getConsumers(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }
        
    /**
     * Use the given pair {attribute; value} to get the corresponding Consumer instances
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the consumer attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of consumers matching the given criteria
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Consumer> getConsumers(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Consumer.class);
        value = prepareQuery(queryObj, attribute, value, 0);
        getLogger().warning("Select consumer(s) with: " + queryObj.toString());
        // Select the corresponding consumers
        List<Consumer> consumers = (List<Consumer>) queryObj.execute(value);
        consumers.size(); // FIXME: remove workaround for a bug in DataNucleus
        return consumers;
    }
    
    /**
     * Persist the given (probably updated) resource
     * 
     * @param consumer Resource to update
     * @return Updated resource
     * 
     * @see ConsumerOperations#updateConsumer(PersistenceManager, Consumer)
     */
    public Consumer updateConsumer(Consumer consumer) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated consumer
            return updateConsumer(pm, consumer);
        }
        finally {
            pm.close();
        }
    }
    
    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     * 
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer Resource to update
     * @return Updated resource
     */
    public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
        getLogger().warning("Updating consumer with id: " + consumer.getKey());
        pm.makePersistent(consumer);
        return consumer;
    }
}