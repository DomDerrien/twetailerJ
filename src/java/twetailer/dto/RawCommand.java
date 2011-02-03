package twetailer.dto;

import java.util.Locale;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.connector.BaseConnector.Source;
import twetailer.task.TweetLoader;

import com.google.appengine.api.datastore.Text;

import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Collect the attributes produced by asynchronous connectors, an
 * object that will process by the task "/_tasks/processCommand"
 *
 * @see twetailer.task.CommandProcessor
 * @see twetailer.j2ee.MaelzelServlet
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class RawCommand extends Entity {

    @Persistent
    private String command;

    public static final String COMMAND = "command";

    @Persistent
    private String commandId;

    public static final String COMMAND_ID = "commandId";

    @Persistent
    private String emitterId;

    public static final String EMITTER_ID = "emitterId";

    @Persistent
    private Text errorMessage;

    public static final String ERROR_MESSAGE = "errorMessage";

    @Persistent
    private Long messageId;

    public static final String MESSAGE_ID = "messageId";

    @Persistent
    private Source source;

    public static final String SOURCE = "source";

    @Persistent
    private String subject;

    public static final String SUBJECT = "subject";

    @Persistent
    private String toId;

    public static final String TO_ID = "toId";

    /** Default constructor */
    public RawCommand() {
        super();
    }

    public RawCommand(Source source) {
        this();
        setSource(source);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String text) {
        if (text != null) {
            text = text.trim();
            String copy = text.toLowerCase(Locale.ENGLISH);
            if (copy.startsWith(TweetLoader.HARMFULL_D_TWETAILER_PREFIX)) {
                text = text.substring(TweetLoader.HARMFULL_D_TWETAILER_PREFIX.length()).trim();
            }
        }
        command = text;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getEmitterId() {
        return emitterId;
    }

    public void setEmitterId(String emitterId) {
        if (emitterId == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'emitterId'");
        }
        this.emitterId = emitterId;
    }

    public String getErrorMessage() {
        return errorMessage == null ? null : errorMessage.getValue();
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null || errorMessage.length() == 0 ? null : new Text(errorMessage);
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'messageId'");
        }
        this.messageId = messageId;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'source'");
        }
        this.source = source;
    }

    public void setSource(String source) {
        setSource(Source.valueOf(source));
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject == null || subject.length() == 0 ? null : subject;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = super.toJson();
        if (getCommand() != null) { out.put(COMMAND, getCommand()); }
        if (getCommandId() != null) { out.put(COMMAND_ID, getCommandId()); }
        if (getEmitterId() != null) { out.put(EMITTER_ID, getEmitterId()); }
        if (getErrorMessage() != null) { out.put(ERROR_MESSAGE, getErrorMessage()); }
        if (getMessageId() != null) { out.put(MESSAGE_ID, getMessageId()); }
        if (getSource() != null) { out.put(SOURCE, getSource().toString()); }
        if (getSubject() != null) { out.put(SUBJECT, getSubject()); }
        if (getToId() != null) { out.put(TO_ID, getToId()); }
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        return fromJson(in, false, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin, boolean isCacheRelated) {
        if (isCacheRelated) { isUserAdmin = isCacheRelated; }

        if (!isUserAdmin) {
            throw new IllegalArgumentException("Reserved operation");
        }

        super.fromJson(in, isUserAdmin, isCacheRelated);

        if (in.containsKey(COMMAND)) { setCommand(in.getString(COMMAND)); }
        if (in.containsKey(COMMAND_ID)) { setCommandId(in.getString(COMMAND_ID)); }
        if (in.containsKey(EMITTER_ID)) { setEmitterId(in.getString(EMITTER_ID)); }
        if (in.containsKey(ERROR_MESSAGE)) { setErrorMessage(in.getString(ERROR_MESSAGE)); }
        if (in.containsKey(MESSAGE_ID)) { setMessageId(in.getLong(MESSAGE_ID)); }
        if (in.containsKey(SOURCE)) { setSource(in.getString(SOURCE)); }
        if (in.containsKey(SUBJECT)) { setSubject(in.getString(SUBJECT)); }
        if (in.containsKey(TO_ID)) { setToId(in.getString(TO_ID)); }
        return this;
    }
}
