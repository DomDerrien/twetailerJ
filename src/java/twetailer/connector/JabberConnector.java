package twetailer.connector;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

public class JabberConnector {
    public static Message getInstantMessage(HttpServletRequest request) throws IOException {
        // Extract the incoming message
        XMPPService xmpp = XMPPServiceFactory.getXMPPService();
        Message instantMessage = xmpp.parseMessage(request);
        return instantMessage;
    }

    public static void sendInstantMessage(String receiverId, String message) {
        Message instantMessage = new MessageBuilder()
        .withRecipientJids(new JID(receiverId))
        .withBody(message)
        .build();

        XMPPService xmpp = XMPPServiceFactory.getXMPPService();
        xmpp.sendMessage(instantMessage);
    }
}
