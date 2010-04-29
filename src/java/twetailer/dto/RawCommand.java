package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import twetailer.connector.BaseConnector.Source;
import twetailer.task.TweetLoader;

import com.google.appengine.api.datastore.Text;

/**
 * @author Dom Derrien
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class RawCommand extends Entity {

    @Persistent
    private String command;

    @Persistent
    private String emitterId;

    @Persistent
    private Text errorMessage;

    @Persistent
    private Long messageId;

    @Persistent
    private Source source;

    @Persistent
    private String subject;

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
            if (text.startsWith(TweetLoader.HARMFULL_D_TWETAILER_PREFIX)) {
                text = text.substring(TweetLoader.HARMFULL_D_TWETAILER_PREFIX.length()).trim();
            }
        }
        command = text;
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
}
