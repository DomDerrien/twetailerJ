package twetailer.connector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Consumer;
import twetailer.task.step.BaseSteps;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonObject;

public class ChannelConnector {

    protected static String getUniqueChannelId(Consumer consumer) {
        return consumer.getEmail();
        // Maybe an indirection will be required to select one user in one browser...
        // It's important to vary the token often to avoid someone having maliciously gain the token to listen to messages posted there!
        // Store the id in memcache and track the user's presence -- note token expires after 2 hours.
    }

    protected static String retrieveUniqueChannelId(Consumer consumer) {
        return getUniqueChannelId(consumer);
        // Temporary hack -> will look-up in memcache first and return null if the user is not connected
    }

    public static String getUserToken(Consumer consumer) {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        return channelService.createChannel(getUniqueChannelId(consumer));
    }

    @SuppressWarnings("unchecked")
    public static void sendMessage(Consumer consumer, JsonObject data) {
        Map<Long, Long> activeChannels = (Map<Long, Long>) BaseSteps.getSettingsOperations().getFromCache(MEMCACHE_IDENTIFIER);
        Long expirationDate = activeChannels.get(consumer.getKey());
        if (expirationDate != null && DateUtils.getNowCalendar().getTimeInMillis() - 2*60*60*1000 < expirationDate) {
            MockOutputStream buffer = new MockOutputStream();
            try {
                data.toStream(buffer, false);
            }
            catch (IOException ex) {
                Logger.getLogger(ChannelConnector.class.getName()).severe("Cannot prepare JsonObject for sending over Channel: " + data.toString() + " -- ex: " + ex.getMessage());
            }
            ChannelService channelService = ChannelServiceFactory.getChannelService();
            try {
                String channelId = retrieveUniqueChannelId(consumer);
                if (channelId != null) {
                    channelService.sendMessage(new ChannelMessage(channelId, buffer.getStream().toString()));
                }
            }
            catch(ChannelFailureException ex) {
                Logger.getLogger(ChannelConnector.class.getName()).severe("Cannot send data to: " + consumer.getName() + " over Channel: " + data.toString() + " -- ex: " + ex.getMessage());
            }
        }
    }

    public final static String MEMCACHE_IDENTIFIER = "activeChannels";

    @SuppressWarnings("unchecked")
    public static void register(Consumer consumer) {
        SettingsOperations ops = BaseSteps.getSettingsOperations();
        Map<Long, Long> activeChannels = (Map<Long, Long>) ops.getFromCache(MEMCACHE_IDENTIFIER);
        if (activeChannels == null) {
            activeChannels = new HashMap<Long, Long>();
            ops.setInCache(MEMCACHE_IDENTIFIER, activeChannels);
        }
        activeChannels.put(consumer.getKey(), DateUtils.getNowCalendar().getTimeInMillis());
        ops.setInCache(MEMCACHE_IDENTIFIER, activeChannels);
    }

    @SuppressWarnings("unchecked")
    public static void unregister(Consumer consumer) {
        Map<Long, Long> activeChannels = (Map<Long, Long>) BaseSteps.getSettingsOperations().getFromCache(MEMCACHE_IDENTIFIER);
        if (activeChannels != null) {
            activeChannels.remove(consumer.getKey());
        }
    }
}
