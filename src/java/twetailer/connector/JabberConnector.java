package twetailer.connector;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import twetailer.ClientException;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.MockXMPPService;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

/**
 * Definition of the methods specific to communication over XMPP/Jabber
 *
 * @author Dom Derrien
 */
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
     *
     * @throws ClientException If it's not possible to communicate with the receiver
     */
    public static void sendInstantMessage(String receiverId, String message) throws ClientException {
        XMPPService xmpp = getXMPPService();
        JID jabberId = new JID(receiverId);
        Message instantMessage = new MessageBuilder().withRecipientJids(jabberId).withBody(message).build();

        if (xmpp.getPresence(jabberId).isAvailable()) {
            SendResponse status = xmpp.sendMessage(instantMessage);
            if (!SendResponse.Status.SUCCESS.equals(status.getStatusMap().get(jabberId))) {
                // TODO: Report error!
                throw new ClientException("Need to report an error while sending an IM to " + receiverId + " -- status: " + status.getStatusMap().get(jabberId));
            }
        }
        else {
            // TODO: Report error!
            throw new ClientException("Need to report the IM recipient " + receiverId + " is not listening!");
        }
    }

    private static XMPPService mockService;

    /**
     * Made available only for test purposes
     */
    public static void injectMockXMPPService(MockXMPPService mock) {
        mockService = mock;
    }

    protected static XMPPService getXMPPService() {
        if (mockService != null) {
            return mockService;
        }
        return XMPPServiceFactory.getXMPPService();
    }
}
