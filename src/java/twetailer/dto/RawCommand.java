package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.datastore.Text;

import twetailer.connector.BaseConnector.Source;

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
        command = text;
    }

    public String getEmitterId() {
        return emitterId;
    }

    public void setEmitterId(String sId) {
        emitterId = sId;
    }

    public String getErrorMessage() {
        return errorMessage == null ? null : errorMessage.getValue();
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = new Text(errorMessage);
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long lId) {
        messageId = lId;
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
        this.subject = subject;
    }
}
