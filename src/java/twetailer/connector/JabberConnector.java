package twetailer.connector;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

public class JabberConnector {
    /**
     * Use the Google App Engine API to get the Jabber message carried by the HTTP request
     *
     * @param request Request parameters submitted by the Google App Engine in response to the reception of an instant message sent to twetailer@appspot.com
     * @return Extracted instant message information
     *
     * @throws IOException If the HTTP request stream parsing fails
     */
    public static Message getInstantMessage(HttpServletRequest request) throws IOException {
        // Extract the incoming message
        XMPPService xmpp = XMPPServiceFactory.getXMPPService();
        Message instantMessage = xmpp.parseMessage(request);
        return instantMessage;
    }

    /**
     * Use the Google App Engine API to send an instant message to the identified Jabber user
     *
     * @param receiverId Identifier of the instant message recipient
     * @param message Message to send
     */
    public static void sendInstantMessage(String receiverId, String message) {
        Message instantMessage = new MessageBuilder().withRecipientJids(new JID(receiverId)).withBody(message).build();

        XMPPService xmpp = XMPPServiceFactory.getXMPPService();
        xmpp.sendMessage(instantMessage);
    }
}
