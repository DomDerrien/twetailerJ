package twetailer.connector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

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

import twetailer.CommunicationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Consumer;
import twetailer.j2ee.MailResponderServlet;
import twetailer.validator.ApplicationSettings;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.StringUtils;
import domderrien.i18n.LabelExtractor.ResourceFileId;

/**
 * Definition of the methods specific to communication over IMAP
 *
 * @author Dom Derrien
 */
public class MailConnector {
    private static Logger log = Logger.getLogger(MailConnector.class.getName());

    //
    // Mail properties (transparently handled by App Engine)
    //
    // Common mail properties
    //   mail.transport.protocol: smtp/pop3/imap
    //   mail.host: appspotmail.com
    //   mail.user: anothersocialeconomy
    //   mail.password: ???
    //

    /**
     * Use the Google App Engine API to get the Mail message carried by the HTTP request
     *
     * @param request Request parameters submitted by the Google App Engine in response to the reception of an mail message sent to a valid engine entry point
     * @return Extracted mail message information
     *
     * @throws IOException If the HTTP request stream parsing fails
     */
    public static MimeMessage getMailMessage(HttpServletRequest request) throws IOException, MessagingException {
        // Extract the incoming message
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);
        MimeMessage mailMessage = new MimeMessage(session, request.getInputStream());
        return mailMessage;
    }

    public static InternetAddress twetailer;
    public static InternetAddress twetailer_cc;
    static {
        twetailer = prepareInternetAddress(
                StringUtils.JAVA_UTF8_CHARSET,
                ApplicationSettings.get().getProductName(),
                MailResponderServlet.getResponderEndpoints().get(0)
        );
        twetailer_cc = prepareInternetAddress(
                StringUtils.JAVA_UTF8_CHARSET,
                ApplicationSettings.get().getProductName(), // TODO: Change the label by "noreply
                MailResponderServlet.getResponderEndpoints().get(0).replace("@", "-noreply@")
        );
    }

    /**
     * Helper setting up an InternetAddress instance with the given parameters
     *
     * @param name Display name of the e-mail address
     * @param email E-mail address
     * @return address Fetched InternetAddress instance
     */
    public static InternetAddress prepareInternetAddress(String charsetEncoding, String name, String email) {
        InternetAddress address = new InternetAddress();
        address.setAddress(email);
        if (name != null && 0 < name.length()) {
            try {
                address.setPersonal(name, charsetEncoding);
            }
            catch (UnsupportedEncodingException ex) {
                // Too bad! The recipient will only see the e-mail address
                Logger.getLogger(MailConnector.class.getName()).warning("Invalid email user name: " + name + " -- message: " + ex.getMessage());

                // Note for the testers:
                //   Don't know how to generate a UnsupportedEncodingException by just
                //   injecting a corrupted UTF-8 sequence and/or a wrong character set

            }
        }
        return address;
    }

    /**
     * Helper appending the conventional "re:" prefix is not detected
     *
     * @param subject Information to check
     * @param locale In order to load the right prefix
     * @return Prefixed information or a default one
     */
    public static String prepareSubjectAsResponse(String subject, Locale locale) {
        if (subject != null) {
            subject = subject.trim();
        }
        if (subject == null || subject.length() == 0) {
            return LabelExtractor.get(ResourceFileId.fourth, "common_message_subject_default", locale);
        }
        String responsePrefix = LabelExtractor.get(ResourceFileId.fourth, "common_message_subject_response_prefix", locale);
        if (!subject.startsWith(responsePrefix.substring(0,3))) {
            return LabelExtractor.get(ResourceFileId.fourth, "common_message_subject_response_prefix", new Object[] { subject }, locale);
        }
        return subject;
    }

    /**
     * Helper appending the conventional "fwd:" prefix is not detected
     *
     * @param subject Information to check
     * @param locale In order to load the right prefix
     * @return Prefixed information or a default one
     */
    public static String prepareSubjectAsForward(String subject, Locale locale) {
        if (subject != null) {
            subject = subject.trim();
        }
        if (subject == null || subject.length() == 0) {
            return LabelExtractor.get(ResourceFileId.fourth, "common_message_subject_default", locale);
        }
        String responsePrefix = LabelExtractor.get(ResourceFileId.fourth, "common_message_subject_forward_prefix", locale);
        if (!subject.startsWith(responsePrefix.substring(0,3))) {
            return LabelExtractor.get(ResourceFileId.fourth, "common_message_subject_forward_prefix", new Object[] { subject }, locale);
        }
        return subject;
    }

    /**
     * Use the Google App Engine API to send an mail message to the identified e-mail address
     *
     * @param useCcAccount Indicates that the message should be sent from a CC account, which is going to ignore unexpected replies
     * @param receiverId E-mail address of the recipient
     * @param recipientName recipient display name
     * @param subject Subject of the message that triggered this response
     * @param message Message to send
     * @param locale recipient's locale
     *
     * @throws MessagingException If one of the message attribute is incorrect
     * @throws UnsupportedEncodingException if the e-mail address is invalid
     */
    public static void sendMailMessage(boolean useCcAccount, String receiverId, String recipientName, String subject, String message, Locale locale) throws MessagingException, UnsupportedEncodingException {
        InternetAddress recipient = new InternetAddress(receiverId, recipientName, StringUtils.JAVA_UTF8_CHARSET);

        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage mailMessage = new MimeMessage(session);
        mailMessage.setFrom(useCcAccount ? twetailer_cc : twetailer);
        mailMessage.setRecipient(Message.RecipientType.TO, recipient);
        mailMessage.setSubject(subject, StringUtils.JAVA_UTF8_CHARSET);
        setContentAsPlainTextAndHtml(mailMessage, message);
        Transport.send(mailMessage);
    }

    /**
     * Utility inserting the given text into the message as a multi-part segment with a plain text and a plain HTML version
     *
     * @param message Message container
     * @param content Text to send
     *
     * @throws MessagingException If the submitted text is not accepted
     */
    public static void setContentAsPlainTextAndHtml(MimeMessage message, String content) throws MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();
        MimeBodyPart htmlPart = new MimeBodyPart();

        // TODO: send a default message in case "content" is null or empty

        textPart.setContent(content, "text/plain; charset=" + StringUtils.HTML_UTF8_CHARSET);
        if (content != null && 0 < content.length() && content.charAt(0) != '<') {
            content = content.replaceAll(BaseConnector.MESSAGE_SEPARATOR, "<br/>");
        }
        htmlPart.setContent(content, "text/html; charset=" + StringUtils.HTML_UTF8_CHARSET);

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
    public static String getText(MimeMessage message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return (String) message.getContent();
            // return StringUtils.toUTF8((String)plain message.getContent());
            // return convertToString((InputStream) message.getContent());
        }
        if (message.isMimeType("text/html")) {
            return (String) message.getContent();
            // return StringUtils.toUTF8((String) message.getContent());
            // return convertToString((InputStream) message.getContent());
        }
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = new MimeMultipart(message.getDataHandler().getDataSource());
            return getText(multipart);
        }
        return "";
    }

    /**
     * Study the given MIME part collection to extract the first text content
     *
     * @param multipart Collection to study
     * @return Extracted text or empty string
     *
     * @throws MessagingException When an error occurs while parsing a piece of the message
     * @throws IOException When an error occurs while reading the message stream
     */
    protected static String getText(Multipart multipart) throws MessagingException, IOException {
        int count = multipart.getCount();
        for(int i = 0; i < count; i++) {
            Part part = multipart.getBodyPart(i);
            String partText = getText(part);
            if (!"".equals(partText)) {
                return partText;
            }
        }
        throw new MessagingException("No text found in this message");
    }

    /**
     * Study the given MIME part to extract the first text content.
     * Note that the algorithm excludes "text/*" attachment.
     * Note also that the algorithm does not parse recursively the "multipart/*" elements.
     *
     * @param part Piece of content to study
     * @return Extracted text or empty string
     *
     * @throws MessagingException When an error occurs while parsing a piece of the message
     * @throws IOException When an error occurs while reading the message stream
     */
    protected static String getText(Part part) throws MessagingException, IOException {
        String filename = part.getFileName();
        if (filename == null && part.isMimeType("text/plain")) {
            return (String) part.getContent();
            // return StringUtils.toUTF8((String) part.getContent());
            // return convertToString((InputStream) part.getContent());
        }
        if (filename == null && part.isMimeType("text/html")) {
            return (String) part.getContent();
            // return StringUtils.toUTF8((String) part.getContent());
            // return convertToString((InputStream) part.getContent());
        }
        // We don't want to go deeper because this part is probably an attachment or a reply!
        // if (part.isMimeType("multipart/*")) {
            // return getText(new MimeMultipart(part.getDataHandler().getDataSource()));
        // }
        return "";
    }

    /**
     * Utility method dumping the content of an InputStream into a String buffer
     *
     * @param in InputStream to process
     * @return String containing all characters extracted from the InputStream
     *
     * @throws IOException If the InputStream process fails
     */
    /*
    //
    // Not used anymore since 1.2.8 because getContent() return String instance instead of InputStream
    //
    protected static String convertToString(InputStream in) throws IOException {
        int character = in.read();
        StringBuilder out = new StringBuilder();
        while (character != -1) {
            out.append((char) character);
            character = in.read();
        }
        return LocaleValidator.toUTF8(out.toString());
    }
    */

    public static boolean foolMessagePost = false;

    /**
     * Made available for unit tests
     */
    public static void foolNextMessagePost() {
        foolMessagePost = true;
    }

    /**
     * Send the specified message to the recipients of the "administrators" list
     *
     * @param subject Message subject
     * @param body Message content
     *
     * @throws MessagingException If the message sending fails
     */
    public static void reportErrorToAdmins(String subject, String body) throws MessagingException {
        reportErrorToAdmins(null, subject, body);
    }

    /**
     * Send the specified message to the recipients of the "administrators" list
     *
     * @param from Message initiator
     * @param subject Message subject
     * @param body Message content
     *
     * @throws MessagingException If the message sending fails
     */
    public static void reportErrorToAdmins(String from, String subject, String body) throws MessagingException {
        if (foolMessagePost) {
            foolMessagePost = false;
            throw new MessagingException("Done in purpose!");
        }

        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage messageToForward = new MimeMessage(session);
        try {
            messageToForward.setFrom(new InternetAddress("admin-notifier@" + ApplicationSettings.get().getProductEmailDomain(), "ASE admin notifier"));
        }
        catch (UnsupportedEncodingException ex) {
            log.warning("Cannot encode 'ASE admin notifier' -- ex: " + ex.getMessage());
            messageToForward.setFrom(new InternetAddress("admin-notifier@" + ApplicationSettings.get().getProductEmailDomain()));
        }
        messageToForward.setRecipient(Message.RecipientType.TO, new InternetAddress("admins"));
        messageToForward.setSubject((from == null ? "" : "Fwd: (" + from + ") ") + subject);
        setContentAsPlainTextAndHtml(messageToForward, body);

        // log.warning("Reporting to 'admins' (medium: mail) -- subject: [" + messageToForward.getSubject() + "] -- message: [" + body + "]");

        Transport.send(messageToForward);
    }

    private final static String[] tableParts = new String[] {
        "<tr><th style=\"border:1px solid black;vertical-align:top;\">",
        "</th><td style=\"border:1px solid black;\">",
        "</td></tr>"
    };

    public static void sendCopyToAdmins(Source source, Consumer consumer, String subject, String[] messages) throws CommunicationException {
        if (foolMessagePost) {
            foolMessagePost = false;
            throw new CommunicationException("Done in purpose!");
        }

        try {
            Properties properties = new Properties();
            Session session = Session.getDefaultInstance(properties, null);

            MimeMessage messageToForward = new MimeMessage(session);
            try {
                messageToForward.setFrom(new InternetAddress("twetailer@gmail.com", "ASE admin notifier"));
            }
            catch (UnsupportedEncodingException ex) {
                log.warning("Cannot encode 'ASE admin notifier' -- ex: " + ex.getMessage());
                messageToForward.setFrom(new InternetAddress("twetailer@gmail.com"));
            }
            messageToForward.setRecipient(Message.RecipientType.TO, new InternetAddress("admins"));
            messageToForward.setSubject("Silent copy");

            StringBuilder body = new StringBuilder();
            body.append("<table border=\"1\" style=\"border:1px solid black;\">");
            body.append(tableParts[0]).append("Recipient").append(tableParts[1]).append(consumer.getName()).append(tableParts[2]);
            body.append(tableParts[0]).append("Subject").append(tableParts[1]).append(subject).append(tableParts[2]);
            body.append(tableParts[0]).append("Date").append(tableParts[1]).append(DateUtils.dateToISO(DateUtils.getNowDate())).append(tableParts[2]);
            body.append(tableParts[0]).append("Message").append(tableParts[1]);
            for (int idx = 0; idx < messages.length; idx ++) {
                body.append(messages[idx]);
            }
            body.append(tableParts[2]);
            body.append(tableParts[0]).append("Recipient info").append(tableParts[1]).append("<pre>").append(consumer.toJson().toString()).append("</pre>").append(tableParts[2]);
            body.append("</table>");
            setContentAsPlainTextAndHtml(messageToForward, body.toString());

            // log.warning("Copying 'admins' (medium: mail) -- subject: [" + messageToForward.getSubject() + "] -- message: [" + body.toString() + "]");

            Transport.send(messageToForward);
        }
        catch (MessagingException ex) {
            throw new CommunicationException("Cannot communicate a copy to the adminis", ex);
        }
    }

    /**
     * Helper extracting the content of a badly quoted-printable message
     *
     * @param message Message the system cannot decode
     * @return Decoded message
     *
     * @throws MessagingException If the system cannot the message InputStream
     * @throws IOException If the message InputStream cannot be read one character at a time
     */
    public static String alternateGetText(MimeMessage message) throws MessagingException, IOException {
        InputStream in = message.getRawInputStream();
        if (in == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());
        byte[] buffer = new byte[2];
        int status;
        while ((status = in.read(buffer, 0, 1)) != -1) {
            if (buffer[0] == '=') {
                // Get the next controlled character
                status = in.read(buffer, 0, 1);
                if (status == -1) {
                    // Ignore trailing '='
                }
                else if (buffer[0] == '\r') {
                    // Get next '\n' and ignore sequence '=\r\n'
                    in.read(buffer, 0, 1);
                }
                else if (buffer[0] == '\n') {
                    // Ignore sequence '=\n'
                }
                else {
                    // Convert encoded character
                    in.read(buffer, 1, 1);
                    out.write((byte) ((Character.digit(buffer[0], 16) << 4) + Character.digit(buffer[1], 16)));
                }
            }
            else {
                out.write(buffer, 0, 1);
            }
        }

        String contentType = message.getContentType(), charset = null;
        int charsetPartIdx = contentType.indexOf("charset");
        if (charsetPartIdx != -1) {
            charset = contentType.substring(contentType.indexOf('=', charsetPartIdx + 1) + 1).trim();
        }

        return new String(out.toByteArray(), charset);
    }
}
