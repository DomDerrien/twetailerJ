package twetailer.j2ee;

import java.io.IOException;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.connector.MailConnector;

/**
 * Servlet receiving all unexpected e-mails. Do triage them in
 * order to dispatch the useful ones, and forward the remaining
 * ones to "admins".
 *
 * The ability to compose e-mails to "admins" is also used by the
 * system to forward information about unexpected errors (with
 * stack traces).
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
        if (pathInfo != null && 0 < pathInfo.length()) {
            pathInfo = pathInfo.substring(1); // To remove the leading '/'
        }

        if (TwitterMailNotificationHandlerServlet.getResponderEndpoints().contains(pathInfo)) {
            TwitterMailNotificationHandlerServlet.processTwitterNotification(request, response);
            return;
        }

        if (MailResponderServlet.getResponderEndpoints().contains(pathInfo)) {
            MailResponderServlet.processMailedRequest(request, response);
            return;
        }

        if (MailComposerServlet.getResponderEndpoints().contains(pathInfo)) {
            MailComposerServlet.processMailedRequest(request, response);
            return;
        }

        forwardUnexpectedMailMessage(request, response);
    }

    protected void forwardUnexpectedMailMessage(HttpServletRequest request, HttpServletResponse response) {
        String fromName = null;
        try {
            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);

            Address from = mailMessage.getSender();
            if (mailMessage.getSender() == null) {
                from = mailMessage.getFrom()[0];
            }
            fromName = from.toString();
            Address[] to = mailMessage.getRecipients(Message.RecipientType.TO);
            Address[] cc = mailMessage.getRecipients(Message.RecipientType.CC);
            String subject = mailMessage.getSubject();
            StringBuilder body = new StringBuilder();

            body.append("From: ").append(from.toString()).append('\n');
            for (int idx = 0; idx < to.length; idx ++) {
                body.append("To: ").append(to[idx].toString()).append('\n');
            }
            for (int idx = 0; idx < cc.length; idx ++) {
                body.append("Cc: ").append(cc[idx].toString()).append('\n');
            }
            body.append("Subject: ").append(subject).append('\n');
            body.append('\n').append(MailConnector.getText(mailMessage));

            MailConnector.reportErrorToAdmins(from.toString(), "Unexpected e-mail!", body.toString());
        }
        // catch (MessagingException ex) {
        // catch (IOException ex) {
        catch (Exception ex) {
            log.severe("Error while processing e-mail from: " + fromName + " -- message: " + ex.getMessage());
        }
    }
}
