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
        try {
            log.warning("Path Info: " + request.getPathInfo());

            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);

            Address[] from = mailMessage.getFrom();
            String subject = mailMessage.getSubject();
            String body = MailConnector.getText(mailMessage);

            composeAndPostMailMessage((from == null || from.length == 0 ? "unknown" : from[0].toString()), subject, body);
        }
        catch (MessagingException ex) {
            // Too bad! Should we tweet the issue?
            log.severe("Error while processing e-mail from");
        }
    }

    public static void composeAndPostMailMessage(String from, String subject, String body) throws MessagingException {
        log.warning("Message to be sent to: " + from + " with the subject: " + subject);

        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage messageToForward = new MimeMessage(session);
        messageToForward.setFrom(MailConnector.twetailer);
        messageToForward.setRecipient(Message.RecipientType.TO, new InternetAddress("dominique.derrien@gmail.com"));
        // messageToForward.setRecipient(Message.RecipientType.TO, new InternetAddress("steven.milstein@gmail.com"));
        messageToForward.setSubject("FW: (" + from + ")" + subject);
        MailConnector.setContentAsPlainTextAndHtml(messageToForward, body);
        Transport.send(messageToForward);
    }
}
