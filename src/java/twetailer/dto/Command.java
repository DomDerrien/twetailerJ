package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

import twetailer.validator.CommandSettings;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Command extends Entity {

    @Persistent
    private CommandSettings.Action action;

    public static final String ACTION = "action";

    @Persistent
    private Long consumerKey;

    public static final String CONSUMER_KEY = "consumerKey";

    public static final String NEED_HELP = "needHelp";

    @Persistent
    private CommandSettings.State state = CommandSettings.State.open;

    public static final String STATE = "state";

    @Persistent
    private Long tweetId;

    public static final String TWEET_ID = "tweetId";

    /** Default constructor */
    public Command() {
        super();
    }

    /**
     * Creates a consumer
     *
     * @param in HTTP request parameters
     */
    public Command(JsonObject in) {
        this();
        fromJson(in);
    }

    public CommandSettings.Action getAction() {
        return action;
    }

    public void setAction(CommandSettings.Action action) {
        this.action = action;
    }

    public void setAction(String action) {
        this.action = CommandSettings.Action.valueOf(action);
    }

    public Long getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(Long consumerId) {
        this.consumerKey = consumerId;
    }

    public CommandSettings.State getState() {
        return state;
    }

    public void setState(CommandSettings.State state) {
        this.state = state;
    }

    public void setState(String state) {
        this.state = CommandSettings.State.valueOf(state);
    }

    public Long getTweetId() {
        return tweetId;
    }

    public void setTweetId(Long tweetId) {
        this.tweetId = tweetId;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getAction() != null) { out.put(ACTION, getAction().toString()); }
        if (getConsumerKey() != null) { out.put(CONSUMER_KEY, getConsumerKey()); }
        out.put(STATE, getState().toString());
        if (getTweetId() != null) { out.put(TWEET_ID, getTweetId()); }
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        super.fromJson(in);
        if (in.containsKey(ACTION)) { setAction(in.getString(ACTION)); }
        if (in.containsKey(CONSUMER_KEY)) { setConsumerKey(in.getLong(CONSUMER_KEY)); }
        if (in.containsKey(STATE)) { setState(in.getString(STATE)); }
        if (in.containsKey(TWEET_ID)) { setTweetId(in.getLong(TWEET_ID)); }
        return this;
    }
}
