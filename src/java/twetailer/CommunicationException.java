package twetailer;

import twetailer.connector.BaseConnector.Source;

/**
 * To report issues related to broken communications
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class CommunicationException extends ClientException {

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Exception ex) {
        super(message, ex);
    }

    private Source source;

    public CommunicationException(String message, Source source) {
        super(message);
        setSource(source);
    }

    public CommunicationException(String message, Source source, Exception ex) {
        super(message, ex);
        setSource(source);
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}
