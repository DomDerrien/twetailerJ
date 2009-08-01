package com.twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

import com.twetailer.validator.CommandSettings;
import com.twetailer.validator.CommandSettings.State;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Command extends Entity {

    @Persistent
    private CommandSettings.Action action;
    
    public static final String ACTION = "action";
    
    @Persistent
    private Long consumerKey = 0L;

    public static final String CONSUMER_KEY = "consumerKey";
    
    @Persistent
    private State state = State.open;
    
    public static final String STATE = "state";

    @Persistent
    private Long tweetId;
    
    public static final String TWEET_ID = "tweetId";
    
    public Command() {
        super();
    }

    public Command(JsonObject parameters) {
        this();
        fromJson(parameters);
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
    
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setState(String state) {
        this.state = State.valueOf(state);
    }
    
    public Long getTweetId() {
        return tweetId;
    }

    public void setTweetId(Long tweetId) {
        this.tweetId = tweetId;
    }

    public JsonObject toJson() {
        JsonObject out = super.toJson();
        out.put(ACTION, getAction().toString());
        out.put(CONSUMER_KEY, getConsumerKey());
        out.put(STATE, getState().toString());
        out.put(TWEET_ID, getTweetId());
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
