package twetailer.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.FacebookConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Consumer;
import twetailer.dto.Entity;
import twetailer.validator.LocaleValidator;
import domderrien.i18n.LabelExtractor;

/**
 * Controller defining various methods used for the CRUD operations on Consumer entities
 *
 * @author Dom Derrien
 */
public class ConsumerOperations extends BaseOperations {

    // Add entries for JabberId & TwitterId when these connectors start to be heavily used
    private static final CacheHandler<Consumer> cacheHandler = new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { Entity.KEY, Consumer.EMAIL, Consumer.OPEN_ID });

    private static Consumer cacheConsumer(Consumer consumer) {
        return cacheHandler.cacheInstance(consumer);
    }

    private static Consumer decacheConsumer(Consumer consumer) {
        return cacheHandler.decacheInstance(consumer);
    }

    private static Consumer getCachedConsumer(Long key) {
        return cacheHandler.getCachedInstance(Consumer.KEY, key);
    }

    private static List<Consumer> getCachedConsumers(String key, Object value) {
        Consumer consumer = cacheHandler.getCachedInstance(key, value);
        if (consumer != null) {
            List<Consumer> consumers = new ArrayList<Consumer>();
            consumers.add(consumer);
            return consumers;
        }
        return null;
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
        newConsumer.setPreferredConnection(Source.mail);
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
        newConsumer.setPreferredConnection(Source.jabber);
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
        if (newConsumer.getName() == null) {
            newConsumer.setName(newConsumer.getTwitterId());
        }
        newConsumer.setPreferredConnection(Source.twitter);
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
        if (newConsumer.getName() == null) {
            newConsumer.setName(newConsumer.getEmail());
        }
        newConsumer.setPreferredConnection(Source.mail);
        return createConsumer(pm, newConsumer);
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     *
     * @param authenticatedUser user with his information has been communicated by the OpenID provider
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
     * @param authenticatedUser user with his information has been communicated by the OpenID provider
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
                    if (existingName == null) { // setName("") reset the field to <code>null</code>
                        existingConsumer.setName(name.length() == 0 ? email : name);
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
        if (newConsumer.getName() == null) {
            if (newConsumer.getEmail() != null) {
                newConsumer.setName(newConsumer.getEmail());
            }
            else {
                newConsumer.setName(openID);
            }
        }
        newConsumer.setPreferredConnection(Source.mail);
        return createConsumer(pm, newConsumer);
    }

    /**
     * Create the Consumer instance if it does not yet exist, or get the existing one
     *
     * @param authenticatedUser user with his information has been communicated by Facebook or Twitter
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, domderrien.jsontools.JsonObject)
     */
    public Consumer createConsumer(domderrien.jsontools.JsonObject authenticatedUser) {
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
     * @param authenticatedUser user with his information has been communicated by Facebook or Twitter
     * @return The just created Consumer instance, or the corresponding one loaded from the data source
     *
     * @see ConsumerOperations#createConsumer(PersistenceManager, Consumer)
     */
    public Consumer createConsumer(PersistenceManager pm, domderrien.jsontools.JsonObject authenticatedUser) {
        // Return consumer if it already exists
        String facebookId = authenticatedUser.getString(FacebookConnector.ATTR_UID);
        try {
            // Try to retrieve the same consumer
            List<Consumer> consumers = getConsumers(pm, Consumer.FACEBOOK_ID, facebookId, 1);
            if (0 < consumers.size()) {
                return consumers.get(0);
            }
        }
        catch (DataSourceException ex) {}

        // Get user information
        String name = authenticatedUser.getString(FacebookConnector.ATTR_NAME);
        String email = authenticatedUser.getString(FacebookConnector.ATTR_EMAIL);
        String language = LocaleValidator.getLocale(authenticatedUser.getString(FacebookConnector.ATTR_LOCALE)).getLanguage();

        // Return consumer if one has the same e-mail address after its update
        if (email != null && 0 < email.length()) {
            // Try to retrieve the same consumer
            List<Consumer> consumers;
            try {
                consumers = getConsumers(pm, Consumer.EMAIL, email, 1);
                if (0 < consumers.size()) {
                    Consumer existingConsumer = consumers.get(0);
                    existingConsumer.setFacebookId(facebookId);
                    String existingName = existingConsumer.getName();
                    if (existingName == null) { // setName("") reset the field to <code>null</code>
                        existingConsumer.setName(name == null || name.length() == 0 ? email : name);
                    }
                    return updateConsumer(pm, existingConsumer);
                }
            }
            catch (DataSourceException e) { }
        }

        // Creates new consumer record and persist it
        Consumer newConsumer = new Consumer();
        newConsumer.setName(name);
        newConsumer.setEmail(email);
        newConsumer.setFacebookId(facebookId);
        newConsumer.setLanguage(language);
        newConsumer.setPreferredConnection(Source.facebook);
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
        // Persist new consumer
        consumer = pm.makePersistent(consumer);
        // Cache the new instance
        cacheConsumer(consumer);
        return consumer;
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
        return getConsumer(pm, key, true);
    }

    /**
     * Use the given key to get the corresponding Consumer instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the consumer
     * @param useCache If <code>true</code> the Consumer record might come from the cache, otherwise it's loaded from the data store
     * @return First consumer matching the given criteria or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Consumer record
     */
    protected Consumer getConsumer(PersistenceManager pm, Long key, boolean useCache) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the Consumer instance");
        }
        // Try to get a copy from the cache
        Consumer consumer = useCache ? getCachedConsumer(key) : null;
        if (consumer != null) {
            return consumer;
        }
        try {
            // Get it from the data store
            consumer = pm.getObjectById(Consumer.class, key);
            // Cache the instance
            if (useCache) {
                cacheConsumer(consumer);
            }
            return consumer;
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
        Query query = pm.newQuery(Consumer.class);
        try {
            if (Consumer.JABBER_ID.equals(attribute)) {
                value = getSimplifiedJabberId((String) value);
            }
            // Try to get a copy from the cache
            List<Consumer> consumers = getCachedConsumers(attribute, value);
            if (consumers != null) {
                return consumers;
            }
            // Select the corresponding consumers
            value = prepareQuery(query, attribute, value, 0);
            consumers = (List<Consumer>) query.execute(value);
            // Cache the data if only one instance is returned
            if (consumers.size() == 1) {
                cacheConsumer(consumers.get(0));
            }
            return consumers;
        }
        finally {
            query.closeAll();
        }
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
        try {
            // TODO: lookup in the cache and only query the ones not cached
            List<Consumer> consumers = (List<Consumer>) query.execute(consumerKeys);
            // Cache the data if only one instance is returned
            if (consumers.size() == 1) {
                cacheConsumer(consumers.get(0));
            }
            return consumers;
        }
        finally {
            query.closeAll();
        }
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
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<Consumer> consumers = (List<Consumer>) query.executeWithArray(values);
            // Cache the data if only one instance is returned
            if (consumers.size() == 1) {
                cacheConsumer(consumers.get(0));
            }
            return consumers;
        }
        finally {
            query.closeAll();
        }
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
        Query query = pm.newQuery("select " + Consumer.KEY + " from " + Consumer.class.getName());
        try {
            Object[] values = prepareQuery(query, parameters, limit);
            // Select the corresponding resources
            List<Long> consumerKeys = (List<Long>) query.executeWithArray(values);
            consumerKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
            return consumerKeys;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param consumer Resource to update
     * @return Updated resource
     *
     * @throws DataSourceException If the data management failed data store side
     *
     * @see ConsumerOperations#updateConsumer(PersistenceManager, Consumer)
     */
    public Consumer updateConsumer(Consumer consumer) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
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
     *
     * @throws DataSourceException If the data management failed data store side
     */
    public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) throws DataSourceException {
        ObjectState state = JDOHelper.getObjectState(consumer);
        if (ObjectState.TRANSIENT.equals(state)) {
            // Get a fresh user copy from the data store
            Consumer transientConsumer = consumer;
            try {
                consumer = getConsumer(pm, consumer.getKey(), false);
            }
            catch (InvalidIdentifierException ex) {
                throw new DataSourceException("Cannot retreive a fresh copy of the consumer key:" + consumer.getKey(), ex);
            }
            // Remove the previous copy from the cache
            decacheConsumer(transientConsumer); // To handle the possibility of an attribute used as a cache key being updated and leaving a wrong entry into the cache
            // Merge the attribute of the old copy into the fresh one
            consumer.fromJson(transientConsumer.toJson(), true, true);
        }
        // Persist updated consumer
        consumer = pm.makePersistent(consumer);
        // Update the cached instance
        cacheConsumer(consumer);
        return consumer;
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
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid SaleAssociate record
     */

    public void deleteConsumer(PersistenceManager pm, Consumer consumer) throws InvalidIdentifierException {
        ObjectState state = JDOHelper.getObjectState(consumer);
        if (ObjectState.TRANSIENT.equals(state)) {
            consumer = getConsumer(pm, consumer.getKey(), false);
        }
        decacheConsumer(consumer);
        pm.deletePersistent(consumer);
    }
}
