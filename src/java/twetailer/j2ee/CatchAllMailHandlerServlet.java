package twetailer.j2ee;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.connector.MailConnector;

@SuppressWarnings("serial")
public class CatchAllMailHandlerServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(CatchAllMailHandlerServlet.class.getName());

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warning("Path Info: " + request.getPathInfo());

        dispatchMailMessage(request, response);
    }

    protected void dispatchMailMessage(HttpServletRequest request, HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        if ("/twitter@twetailer.appspotmail.com".equals(pathInfo)) {
            log.warning("Forwarding to: TwitterMailNotificationHandlerServlet.processTwitterNotification()");
            TwitterMailNotificationHandlerServlet.processTwitterNotification(request, response);
            return;
        }

        if ("/maezel@twetailer.appspotmail.com".equals(pathInfo)) {
            log.warning("Forwarding to: MailResponderServlet.processMailedRequest()");
            MailResponderServlet.processMailedRequest(request, response);
            return;
        }

        forwardUnexpectedMailMessage(request, response);
    }

    protected void forwardUnexpectedMailMessage(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);

            Address[] from = mailMessage.getFrom();
            String subject = mailMessage.getSubject();
            String body = MailConnector.getText(mailMessage);

            composeAndPostMailMessage((from == null || from.length == 0 ? "unknown" : from[0].toString()), subject, body);
        }
        catch (MessagingException ex) {
            // Too bad! Should we tweet the issue?
            // Dom: I don't think so because e-mail is probably the most robust communication mechanism (with native auto-retry, for example)
            log.severe("Error while processing e-mail from");
        }
        catch (IOException ex) {
            // Too bad! Should we tweet the issue?
            // Dom: I don't think so because e-mail is probably the most robust communication mechanism (with native auto-retry, for example)
            log.severe("Error while processing e-mail from");
        }
    }

    /**
     * Send the specified message to the recipients of the "catch-all" list
     *
     * @param from Message initiator
     * @param subject Message subject
     * @param body Message content
     *
     * @throws MessagingException If the message sending fails
     */
    public static void composeAndPostMailMessage(String from, String subject, String body) throws MessagingException {
        if (foolMessagePost) {
            foolMessagePost = false;
            throw new MessagingException("Done in purpose!");
        }

        log.warning("Message to be sent to: " + from + " with the subject: " + subject);

        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage messageToForward = new MimeMessage(session);
        messageToForward.setFrom(MailConnector.twetailer);
        messageToForward.setRecipient(Message.RecipientType.TO, new InternetAddress("catch-all@twetailer.com"));
        messageToForward.setSubject("Fwd: (" + from + ") " + subject);
        MailConnector.setContentAsPlainTextAndHtml(messageToForward, body);
        Transport.send(messageToForward);
    }

    private static boolean foolMessagePost = false;

    /**
     * Made available for unit tests
     */
    public static void foolNextMessagePost() {
        foolMessagePost = true;
    }
}
