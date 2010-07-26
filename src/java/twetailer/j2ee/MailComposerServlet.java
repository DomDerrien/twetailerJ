package twetailer.j2ee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.connector.MailConnector;

/**
 * Entry point returning the content of the
 * received e-mail to the sender
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class MailComposerServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MailComposerServlet.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    public static List<String> responderEndpoints = new ArrayList<String>();
    static {
        responderEndpoints.add("composer@twetailer.appspotmail.com");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processMailedRequest(request, response);
    }

    protected static void processMailedRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);

            InternetAddress address = (InternetAddress) (mailMessage.getFrom()[0]);
            String name = address.getPersonal();
            String email = address.getAddress();
            String subject = mailMessage.getSubject();
            String body = MailConnector.getText(mailMessage);

            log.warning("Redirects email to: " + name);
            MailConnector.sendMailMessage(true, email, name, subject, body, Locale.ENGLISH);
        }
        catch (MessagingException ex) {
            // Nothing to do with a corrupted message...
        }
        catch (IOException ex) {
            // Nothing to do with a corrupted message...
        }
    }
}
