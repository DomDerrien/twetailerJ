package twetailer.connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import twetailer.validator.ApplicationSettings;

public class MailConnector {
    //
    // Mail properties (transparently handled by App Engine)
    //
    // Common mail properties
    //   mail.transport.protocol: smtp/pop3/imap
    //   mail.host: appspotmail.com
    //   mail.user: twetailer
    //   mail.password: ???
    //

    public static MimeMessage getMailMessage(HttpServletRequest request) throws IOException, MessagingException {
        // Extract the incoming message
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);
        MimeMessage mailMessage = new MimeMessage(session, request.getInputStream());
        return mailMessage;
    }

    public static void sendMailMessage(String receiverId, String message) throws UnsupportedEncodingException, MessagingException {
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage mailMessage = new MimeMessage(session);
        mailMessage.setFrom(new InternetAddress("command@twetailer.appspomaol.com", ApplicationSettings.get().getProductName()));
        mailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(receiverId, ""));
        mailMessage.setSubject("Twetailer notification");
        // setContentAsPlainText(mailMessage, message);
        // setContentAsHTML(mailMessage, message);
        setContentAsPlainTextAndHtml(mailMessage, message);
        Transport.send(mailMessage);
    }

    protected static void setContentAsPlainText(MimeMessage message, String content) throws MessagingException {
        message.setText(content);
    }

    protected static void setContentAsHTML(MimeMessage message, String content) throws MessagingException {
        message.setContent(content, "text/html; charset=UTF-8");
    }

    protected static void setContentAsPlainTextAndHtml(MimeMessage message, String content) throws MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();
        MimeBodyPart htmlPart = new MimeBodyPart();

        textPart.setContent(content, "text/plain; charset=UTF-8");
        htmlPart.setContent(content, "text/html; charset=UTF-8");

        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);

        message.setContent(multipart);
    }

    /**
     * Study the given MIME message part to extract the first text content
     *
     * @param message MimeMessage or MimeBodyPart to study
     * @return Extracted piece of text (can be text/plain or text/HTML, on one or many lines)
     *
     * @throws MessagingException When an error occurs while parsing a piece of the message
     * @throws IOException When an error occurs while reading the message stream
     */
    public static String getText(Part message) throws MessagingException, IOException {
        if (message.isMimeType("text/*")) {
            return (String) message.getContent();
        }
        if (message != null && message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for(int i = 0; i < multipart.getCount(); i++) {
                Part part = multipart.getBodyPart(i);
                String text = getText(part);
                if (!"".equals(text)) {
                    return text;
                }
            }
        }
        return "";
    }
}
