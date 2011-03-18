package twetailer.connector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;
import twetailer.dao.CacheHandler;
import twetailer.dto.Consumer;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonObject;

public class ChannelConnector {

    private static Logger log = Logger.getLogger(ChannelConnector.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    /**
     * Generates a unique identifier for the consumer. Note that
     * this should be strong enough to avoid anyone guessing someone else
     * token and using it to listen message sent asynchronously!
     *
     * @param consumer owner of the identifier to create
     * @return unique identifier for the Google Push Channel
     */
    protected static String getUniqueChannelId(Consumer consumer) {
        return consumer.getEmail();
        // Maybe an indirection will be required to select one user in one browser...
        // It's important to vary the token often to avoid someone having maliciously gain the token to listen to messages posted there!
        // Store the id in cache and track the user's presence -- note token expires after 2 hours.
    }

    /**
     * Uses the cache to get the token computed for the user, or
     * create a new one.
     *
     * @param consumer owner of the identifier to create
     * @return unique identifier for the Google Push Channel
     *
     * @see twetailer.connector.ChannelConnector#getUniqueChannelId(Consumer)
     */
    protected static String retrieveUniqueChannelId(Consumer consumer) {
        return getUniqueChannelId(consumer);
        // Temporary hack -> will look-up in the cache first and return null if the user is not connected
    }

    /**
     * Forwards the Google Push Channel token created for the user.
     * This information should be conveyed to the client which will
     * use it to open a Channel instance. Be careful about not exposing
     * this token as it can be used by someone else to listen messages!
     *
     * @param consumer owner of the token to create
     * @return unique token for the Google Push Channel
     *
     * @see twetailer.connector.ChannelConnector#retrieveUniqueChannelId(Consumer)
     */
    public static String getUserToken(Consumer consumer) {
        ChannelService channelService = getService();
        return channelService.createChannel(retrieveUniqueChannelId(consumer));
    }

    private static ChannelService mockService;

    /// For unit test purposes
    public static void injectMockChannelService(ChannelService service) {
        ChannelConnector.mockService = service;
    }

    /// For unit test purposes
    protected static ChannelService getService() {
        if (mockService != null) {
            return mockService;
        }
        return ChannelServiceFactory.getChannelService();
    }

    /**
     * Helper using the user associated Channel to push the
     * given JSON object client-side. Note that if the
     * user's client has not yet been registered, or if the
     * session has expired, nothing is sent.
     *
     * @param consumer Identifies the channel to use
     * @param data Data to send client-side
     *
     * @see twetailer.connector.ChannelConnector#register(Consumer)
     */
    @SuppressWarnings("unchecked")
    public static void sendMessage(Consumer consumer, JsonObject data) {
        Map<Long, Long> activeChannels = (Map<Long, Long>) CacheHandler.getFromCache(MEMCACHE_IDENTIFIER);
        Long expirationDate = activeChannels == null ? null: activeChannels.get(consumer.getKey());
        if (expirationDate != null && DateUtils.getNowCalendar().getTimeInMillis() - 2*60*60*1000 < expirationDate) {
            MockOutputStream buffer = new MockOutputStream();
            try {
                data.toStream(buffer, false);
                getService().sendMessage(new ChannelMessage(retrieveUniqueChannelId(consumer), buffer.getStream().toString()));
            }
            catch (IOException ex) {
                getLogger().severe("Cannot prepare JsonObject for sending over Channel: " + data.toString() + " -- ex: " + ex.getMessage());
            }
            catch(ChannelFailureException ex) {
                getLogger().severe("Cannot send data to: " + consumer.getName() + " over Channel: " + data.toString() + " -- ex: " + ex.getMessage());
            }
            catch(Exception ex) {
                getLogger().severe("Unexpected error when sending data to: " + consumer.getName() + " over Channel: " + data.toString() + " -- ex: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    protected final static String MEMCACHE_IDENTIFIER = "_jsChannel";

    /**
     * Start tracking the user so messages can now be sent over his associated Channel
     *
     * @param consumer Owner of the channel to register
     *
     * @see twetailer.connector.ChannelConnector#unregister(Consumer)
     */
    @SuppressWarnings("unchecked")
    public static void register(Consumer consumer) {
        Map<Long, Long> activeChannels = (Map<Long, Long>) CacheHandler.getFromCache(MEMCACHE_IDENTIFIER);
        if (activeChannels == null) {
            activeChannels = new HashMap<Long, Long>();
        }
        activeChannels.put(consumer.getKey(), DateUtils.getNowCalendar().getTimeInMillis());
        CacheHandler.setInCache(MEMCACHE_IDENTIFIER, activeChannels);
    }

    /**
     * Stop tracking the user, so not more message will be sent over his Channel
     *
     * @param consumer Owner of the channel to unregister
     *
     * @see twetailer.connector.ChannelConnector#register(Consumer)
     */
    @SuppressWarnings("unchecked")
    public static void unregister(Consumer consumer) {
        Map<Long, Long> activeChannels = (Map<Long, Long>) CacheHandler.getFromCache(MEMCACHE_IDENTIFIER);
        if (activeChannels != null) {
            activeChannels.remove(consumer.getKey());
        }
    }
}
