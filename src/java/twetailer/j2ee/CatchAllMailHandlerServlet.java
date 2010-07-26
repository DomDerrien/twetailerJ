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

/**
 * Servlet receiving all unexpected e-mails. Do triage them in
 * order to dispatch the useful ones, and forward the remaining
 * ones to "catch-all@twetailer.com".
 *
 * The ability to compose e-mails to "catch-all@twetailer.com"
 * is also used by the system to forward information about unexpected
 * errors (with stack traces).
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class CatchAllMailHandlerServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(CatchAllMailHandlerServlet.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        dispatchMailMessage(request, response);
    }

    protected void dispatchMailMessage(HttpServletRequest request, HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);
        if (pathInfo != null && 0 < pathInfo.length()) {
            pathInfo = pathInfo.substring(1); // To remove the leading '/'
        }

        if (TwitterMailNotificationHandlerServlet.responderEndpoints.contains(pathInfo)) {
            log.warning("Forwarding to: TwitterMailNotificationHandlerServlet.processTwitterNotification()");
            TwitterMailNotificationHandlerServlet.processTwitterNotification(request, response);
            return;
        }

        if (MailResponderServlet.responderEndpoints.contains(pathInfo)) {
            log.warning("Forwarding to: MailResponderServlet.processMailedRequest()");
            MailResponderServlet.processMailedRequest(request, response);
            return;
        }

        if (MailComposerServlet.responderEndpoints.contains(pathInfo)) {
            log.warning("Forwarding to: MailComposerServlet.processMailedRequest()");
            MailComposerServlet.processMailedRequest(request, response);
            return;
        }

        forwardUnexpectedMailMessage(request, response);
    }

    protected void forwardUnexpectedMailMessage(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);

            Address from = mailMessage.getSender();
            if (mailMessage.getSender() == null) {
                from = mailMessage.getFrom()[0];
            }
            Address[] to = mailMessage.getRecipients(Message.RecipientType.TO);
            Address[] cc = mailMessage.getRecipients(Message.RecipientType.CC);
            String subject = mailMessage.getSubject();
            StringBuilder body = new StringBuilder();

            body.append("From: ").append(from.toString()).append("\n");
            for (int idx = 0; idx < to.length; idx ++) {
                body.append("To: ").append(to[idx].toString()).append("\n");
            }
            for (int idx = 0; idx < cc.length; idx ++) {
                body.append("Cc: ").append(cc[idx].toString()).append("\n");
            }
            body.append("Subject: ").append(subject).append("\n");
            body.append("\n").append(MailConnector.getText(mailMessage));

            composeAndPostMailMessage(from.toString(), "Unexpected e-mail!", body.toString());
        }
        // catch (MessagingException ex) {
        // catch (IOException ex) {
        catch (Exception ex) {
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

        log.warning("Message to be sent to: " + from + " with the subject: " + subject + "\n***\n" + body + "\n***");

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
