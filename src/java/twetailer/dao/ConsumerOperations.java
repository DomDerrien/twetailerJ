package twetailer.dao;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Consumer;
import twetailer.validator.LocaleValidator;
import domderrien.i18n.LabelExtractor;

/**
 * Controller defining various methods used for the CRUD operations on Consumer entities
 *
 * @author Dom Derrien
 */
public class ConsumerOperations extends BaseOperations {
    private static Logger log = Logger.getLogger(ConsumerOperations.class.getName());

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
     * @see ConsumerOperations#createConsumer(PersistenceManager, com.google.appengine.api.users.User)
     */
    public Consumer createConsumer(com.google.appengine.api.users.User loggedUser) {
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
    public Consumer createConsumer(PersistenceManager pm, com.google.appengine.api.users.User loggedUser) {
        // Return consumer if it already exists
        String address = loggedUser.getEmail().toLowerCase();
        try {
            // Try to retrieve the same consumer
            List<Consumer> consumers = getConsumers(pm, Consumer.EMAIL, address, 1);
            if (0 < consumers.size()) {
                return consumers.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Creates new consumer record and persist it
        Consumer newConsumer = new Consumer();
        newConsumer.setName(loggedUser.getNickname());
        newConsumer.setEmail(address);
        return createConsumer(pm, newConsumer);
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     *
     * @param loggedUser System entity to attach with the just created user
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, com.google.appengine.api.xmpp.JID)
     */
    public Consumer createConsumer(com.google.appengine.api.xmpp.JID jabberId) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createConsumer(pm, jabberId);
        }
        finally {
            pm.close();
        }
    }

    protected static String getSimplifiedJabberId(String jabberId) {
        String simplifiedJabberId = jabberId;
        int clientInformationSeparator = simplifiedJabberId.indexOf('/');
        if (clientInformationSeparator != -1) {
            simplifiedJabberId = simplifiedJabberId.substring(0, clientInformationSeparator);
        }
        return simplifiedJabberId;
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param jabberId Identifier of a XMPP user
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, Consumer)
     */
    public Consumer createConsumer(PersistenceManager pm, com.google.appengine.api.xmpp.JID jabberId) {
        // Return consumer if it already exists
        String identifier = getSimplifiedJabberId(jabberId.getId()).toLowerCase();
        try {
            // Try to retrieve the same consumer
            List<Consumer> consumers = getConsumers(pm, Consumer.JABBER_ID, identifier, 1);
            if (0 < consumers.size()) {
                return consumers.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Creates new consumer record and persist it
        Consumer newConsumer = new Consumer();
        newConsumer.setName(identifier);
        newConsumer.setJabberId(identifier);
        return createConsumer(pm, newConsumer);
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
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, Consumer)
     */
    public Consumer createConsumer(PersistenceManager pm, twitter4j.User twitterUser) throws DataSourceException {
        // Return consumer if it already exists
        String identifier = twitterUser.getScreenName();
        try {
            // Try to retrieve the same consumer
            List<Consumer> consumers = getConsumers(pm, Consumer.TWITTER_ID, identifier, 1);
            if (0 < consumers.size()) {
                return consumers.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Creates new consumer record and persist it
        Consumer newConsumer = new Consumer();
        newConsumer.setName(twitterUser.getName());
        newConsumer.setAddress(twitterUser.getLocation());
        newConsumer.setTwitterId(identifier);
        return createConsumer(pm, newConsumer);
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     *
     * @param senderAddress Mail address of the sender
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, javax.mail.internet.InternetAddress)
     */
    public Consumer createConsumer(javax.mail.internet.InternetAddress senderAddress) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createConsumer(pm, senderAddress);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param senderAddress Mail address of the sender
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, Consumer)
     */
    public Consumer createConsumer(PersistenceManager pm, javax.mail.internet.InternetAddress senderAddress) {
        // Return consumer if it already exists
        String email = senderAddress.getAddress().toLowerCase();
        try {
            // Try to retrieve the same consumer
            List<Consumer> consumers = getConsumers(pm, Consumer.EMAIL, email, 1);
            if (0 < consumers.size()) {
                return consumers.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Creates new consumer record and persist it
        Consumer newConsumer = new Consumer();
        newConsumer.setName(senderAddress.getPersonal());
        newConsumer.setEmail(email);
        return createConsumer(pm, newConsumer);
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     *
     * @param authenticatedUser user with his information has communicated by the OpenID provider
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, com.dyuproject.openid.OpenIdUser)
     */
    public Consumer createConsumer(com.dyuproject.openid.OpenIdUser authenticatedUser) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createConsumer(pm, authenticatedUser);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param authenticatedUser user with his information has communicated by the OpenID provider
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, Consumer)
     */
    @SuppressWarnings("unchecked")
    public Consumer createConsumer(PersistenceManager pm, com.dyuproject.openid.OpenIdUser authenticatedUser) {
        // Return consumer if it already exists
        String openID = authenticatedUser.getClaimedId();
        try {
            // Try to retrieve the same consumer
            List<Consumer> consumers = getConsumers(pm, Consumer.OPEN_ID, openID, 1);
            if (0 < consumers.size()) {
                return consumers.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Get user information
        String email = "", language = LocaleValidator.DEFAULT_LANGUAGE, name = ""; //, country = LocaleValidator.DEFAULT_COUNTRY_CODE;
        Map<String, String> info = (Map<String, String>) authenticatedUser.getAttribute("info");
        if (info != null) {
            if (info.get("language") != null) { language = LocaleValidator.checkLanguage(info.get("language")); }
            if (info.get("nickname") != null) { name =  info.get("nickname"); }
            if (name.length() == 0) {
                String firstname = info.get("firstname") == null ? "" : info.get("firstname");
                String lastname = info.get("lastname") == null ? "" : info.get("lastname");
                name = LabelExtractor.get("display_name_pattern", new Object[] { firstname, lastname }, new Locale(language)).trim();
            }
            if (info.get("email") != null) { email = info.get("email"); }
            // if (info.get("country") == null) { country = info.get("country"); }
        }

        // Return consumer if one has the same e-mail address after its update
        if (0 < email.length()) {
            // Try to retrieve the same consumer
            List<Consumer> consumers;
            try {
                consumers = getConsumers(pm, Consumer.EMAIL, email, 1);
                if (0 < consumers.size()) {
                    Consumer existingConsumer = consumers.get(0);
                    existingConsumer.setOpenID(openID);
                    String existingName = existingConsumer.getName();
                    if (existingName == null || existingName.length() == 0) {
                        existingConsumer.setName(name);
                    }
                    existingConsumer = updateConsumer(pm, existingConsumer);
                    return existingConsumer;
                }
            }
            catch (DataSourceException e) { }
        }

        // Creates new consumer record and persist it
        Consumer newConsumer = new Consumer();
        newConsumer.setName(name);
        newConsumer.setEmail(email);
        newConsumer.setOpenID(openID);
        newConsumer.setLanguage(language);
        return createConsumer(pm, newConsumer);
    }

    /**
     * Create the Consumer instance with the given parameters
     *
     * @param consumer Resource to persist
     * @return Just created resource
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, Consumer)
     */
    public Consumer createConsumer(Consumer consumer) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createConsumer(pm, consumer);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Consumer instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer Resource to persist
     * @return Just created resource
     */
    public Consumer createConsumer(PersistenceManager pm, Consumer consumer) {
        return pm.makePersistent(consumer);
    }

    /**
     * Use the given key to get the corresponding Consumer instance
     *
     * @param key Identifier of the consumer
     * @return First consumer matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Consumer record
     *
     * @see ConsumerOperations#getConsumer(PersistenceManager, Long)
     */
    public Consumer getConsumer(Long key) throws InvalidIdentifierException {
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
     * @throws InvalidIdentifierException If the given identifier does not match a valid Consumer record
     */
    public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the Consumer instance");
        }
        getLogger().warning("Get Consumer instance with id: " + key);
        try {
            return pm.getObjectById(Consumer.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving consumer for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
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
        if (Consumer.JABBER_ID.equals(attribute)) {
            value = getSimplifiedJabberId((String) value);
        }
        value = prepareQuery(queryObj, attribute, value, 0);
        getLogger().warning("Select consumer(s) with: " + queryObj.toString());
        // Select the corresponding consumers
        List<Consumer> consumers = (List<Consumer>) queryObj.execute(value);
        consumers.size(); // FIXME: remove workaround for a bug in DataNucleus
        return consumers;
    }

    /**
     * Get the identified Consumer instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumerKeys list of Consumer instance identifiers
     * @return Collection of consumers matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Consumer> getConsumers(PersistenceManager pm, List<Long> consumerKeys) throws DataSourceException {
        // Select the corresponding resources
        Query query = pm.newQuery(Consumer.class, ":p.contains(key)"); // Reported as being more efficient than pm.getObjectsById()
        List<Consumer> consumers = (List<Consumer>) query.execute(consumerKeys);
        consumers.size(); // FIXME: remove workaround for a bug in DataNucleus
        return consumers;
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Consumer instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of consumers matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Consumer> getConsumers(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Consumer.class);
        Object[] values = prepareQuery(query, parameters, limit);
        getLogger().warning("Select consumer(s) with: " + query.toString());
        // Select the corresponding resources
        List<Consumer> consumers = (List<Consumer>) query.executeWithArray(values);
        consumers.size(); // FIXME: remove workaround for a bug in DataNucleus
        return consumers;
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Consumer identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of consumer keys matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getConsumerKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery("select " + Consumer.KEY + " from " + Consumer.class.getName());
        Object[] values = prepareQuery(queryObj, parameters, limit);
        getLogger().warning("Select consumer(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Long> consumerKeys = (List<Long>) queryObj.executeWithArray(values);
        consumerKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
        return consumerKeys;
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
        return pm.makePersistent(consumer);
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Consumer instance and to delete it
     *
     * @param consumerKey Identifier of the consumer
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Consumer record
     *
     * @see ConsumerOperations#deleteConsumer(PersistenceManager, Long)
     */
    public void deleteConsumer(Long consumerKey) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            deleteConsumer(pm, consumerKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Consumer instance and to delete it
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumerKey Identifier of the consumer
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Consumer record
     *
     * @see ConsumerOperations#getConsumers(PersistenceManager, Long)
     * @see ConsumerOperations#deleteConsumer(PersistenceManager, Consumer)
     */
    public void deleteConsumer(PersistenceManager pm, Long consumerKey) throws InvalidIdentifierException {
        Consumer consumer = getConsumer(pm, consumerKey);
        deleteConsumer(pm, consumer);
    }

    /**
     * Delete the given Consumer while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer Object to delete
     */

    public void deleteConsumer(PersistenceManager pm, Consumer consumer) {
        getLogger().warning("Delete consumer with id: " + consumer.getKey());
        pm.deletePersistent(consumer);
    }
}
