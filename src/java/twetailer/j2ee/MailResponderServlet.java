package twetailer.j2ee;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

import javax.jdo.PersistenceManager;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.connector.MailConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.CacheHandler;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.HashTag;
import twetailer.dto.RawCommand;
import twetailer.task.CommandLineParser;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Prefix;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

/**
 * Entry point processing IMAP messages.
 * Received information are stored in a RawCommand instance
 * that the task "/_tasks/processCommand" will process
 * asynchronously.
 *
 * @see twetailer.dto.RawCommand
 * @see twetailer.j2ee.MaelzelServlet
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class MailResponderServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MailResponderServlet.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processMailedRequest(request, response);
    }

    protected static List<String> responderEndpoints = new ArrayList<String>();

    public static List<String> getResponderEndpoints() {
        if (responderEndpoints.size() == 0) {
            String emailDomain = ApplicationSettings.get().getProductEmailDomain();

            responderEndpoints.add("assistant@" + emailDomain);

            List<String> hashTags = HashTag.getSupportedHashTags();
            for (String hashTag: hashTags) {
                responderEndpoints.add(hashTag + "@" + emailDomain);
            }

            responderEndpoints.add("eztoff@" + emailDomain);
        }
        return responderEndpoints;
    }

    protected static final String MESSAGE_ID_LIST = "_mailMessageIds";

    @SuppressWarnings("unchecked")
    protected static void processMailedRequest(HttpServletRequest request, HttpServletResponse response) {
        String to = null;
        String name = null;
        String email = null;
        String subject = null;
        String language = null;
        Exception exception = null;
        String command = "";

        // Prepare the message to persist
        RawCommand rawCommand = new RawCommand(Source.mail);

        try {
            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);
            if (mailMessage.getFrom() == null) {
                throw new MessagingException("Incorrect message (no FROM!)");
            }

            // Check if the message has already been processed
            String messageId = mailMessage.getMessageID();
            if (messageId != null && 0 < messageId.length()) {
                List<String> lastMailMessageIds = (List<String>) CacheHandler.getFromCache(MESSAGE_ID_LIST);
                if (lastMailMessageIds != null) {
                    if (lastMailMessageIds.contains(messageId)) {
                        getLogger().warning("Email '" + messageId + "' already processed");
                        return;
                    }
                }
                else {
                    lastMailMessageIds = new ArrayList<String>();
                }
                lastMailMessageIds.add(messageId);
                CacheHandler.setInCache(MESSAGE_ID_LIST, lastMailMessageIds);
            }

            // Fill up the message to persist
            rawCommand.setCommandId(messageId);

            // Extract information about the sender
            InternetAddress address = (InternetAddress) (mailMessage.getFrom()[0]);
            name = address.getPersonal();
            email = address.getAddress();
            subject = mailMessage.getSubject();
            if (mailMessage.getContentLanguage() != null) {
                language = LocaleValidator.checkLanguage(mailMessage.getContentLanguage()[0]);
            }

            // Fill up the message to persist
            rawCommand.setEmitterId(email.toLowerCase());
            rawCommand.setSubject(subject);

            // Extract information about a supported receiver
            StringBuilder log = new StringBuilder();
            InternetAddress[] recipients = (InternetAddress []) (mailMessage.getRecipients(Message.RecipientType.TO));
            for (int idx = 0; to == null && idx < recipients.length; idx ++) {
                to = recipients[idx].getAddress();
                log.append("\"").append(recipients[idx].getPersonal()).append("\" <").append(recipients[idx].getAddress()).append(">, ");
                if (!getResponderEndpoints().contains(to)) {
                    to = null;
                }
            }
            if (to == null) {
                String messageContent;
                try {
                    messageContent = MailConnector.getText(mailMessage);
                }
                catch(IOException ex) {
                    messageContent = MailConnector.alternateGetText(mailMessage);
                }
                getLogger().warning(
                        "Email '" + messageId + "' not addressed to Twetailer!\n" +
                        "From: \"" + name + "\" <" + email + ">\n" +
                        "To: " + log.toString() + "\n" +
                        "Subject: " + subject + "\n--\n" + messageContent
                );
                throw new RuntimeException("Message received without the To: field! Sent by: \"" + name + "\" <" + email + ">");
            }
            StringBuilder ccList = new StringBuilder();
            recipients = (InternetAddress []) (mailMessage.getRecipients(Message.RecipientType.CC));
            if (recipients != null) {
                if (0 < recipients.length) {
                    // TODO: push this code below to fall back on the consumer locale
                    Locale locale = language == null ? Locale.ENGLISH : new Locale(language);
                    CommandLineParser.loadLocalizedSettings(locale);
                    JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
                    String ccPrefix = prefixes.getJsonArray(Prefix.cc.toString()).getString(0);
                    for (int idx = 0; idx < recipients.length; idx ++) {
                        String ccAddress = recipients[idx].getAddress();
                        if (!getResponderEndpoints().contains(ccAddress)) {
                            ccList.append(Command.SPACE).append(ccPrefix).append(CommandLineParser.PREFIX_SEPARATOR).append(ccAddress);
                        }
                    }
                    command = ccList.insert(0, command).toString();
                }
            }

            // Fill up the message to persist
            rawCommand.setToId(to.toLowerCase());

            // Add vertical information
            String toBase = to.substring(0, to.indexOf('@'));
            if (HashTag.isSupportedHashTag(toBase)) {
                command += " #" + HashTag.getSupportedHashTag(toBase); // To change 'eztoff' in 'golf', for example
            }

            String messageContent;
            try {
                messageContent = MailConnector.getText(mailMessage);
            }
            catch(IOException ex) {
                messageContent = MailConnector.alternateGetText(mailMessage);
            }
            command = extractFirstLine(messageContent) + command;

            // Fill up the message to persist
            rawCommand.setCommand(command);

            getLogger().warning("Message sent by: " + name + " <" + email + ">\nWith the identifier: " + messageId + "\nWith the subject: " + subject + "\nWith the command: " + command);

            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                // Creation only occurs if the corresponding Consumer instance is not retrieved
                Consumer consumer = BaseSteps.getConsumerOperations().createConsumer(pm, address, true);
                if (language == null) {
                    language = consumer.getLanguage();
                }

                // Persist message
                rawCommand = BaseSteps.getRawCommandOperations().createRawCommand(pm, rawCommand);
            }
            finally {
                pm.close();
            }

            // Create a task for to process that new command
            Queue queue = BaseSteps.getBaseOperations().getQueue();
            queue.add(
                    withUrl("/_tasks/processCommand").
                        param(Command.KEY, rawCommand.getKey().toString()).
                        method(Method.GET).
                        countdownMillis(2000)
            );
        }
        catch (MessagingException ex) {
            exception = ex;
            getLogger().warning("During the message composition -- message " + ex.getMessage());
            rawCommand.setErrorMessage(LabelExtractor.get("error_mail_messaging", language == null ? LocaleValidator.DEFAULT_LOCALE : new Locale(language)));
        }
        catch (DatastoreTimeoutException ex) {
            exception = ex;
            getLogger().warning("During trying to manage data with the back store -- message " + ex.getMessage());
            rawCommand.setErrorMessage(LabelExtractor.get("error_datastore_timeout", language == null ? LocaleValidator.DEFAULT_LOCALE : new Locale(language)));
        }
        catch (Exception ex) {
            exception = ex;
            getLogger().warning("Unexpected issue -- message " + ex.getMessage());
            rawCommand.setErrorMessage(
                    LabelExtractor.get(
                            "error_unexpected",
                            new Object[] {
                                    rawCommand.getKey() == null ? 0L : rawCommand.getKey(),
                                    "" // No contextual information exposed, they'll in the mail message to the "catch-all" list
                            },
                            language == null ? LocaleValidator.DEFAULT_LOCALE : new Locale(language)
                    )
            );
        }

        if (rawCommand.getErrorMessage() != null) {
            // Persist the error message
            if (rawCommand.getKey() == null) {
                BaseSteps.getRawCommandOperations().createRawCommand(rawCommand);
            }
            else {
                BaseSteps.getRawCommandOperations().updateRawCommand(rawCommand);
            }
            // Communicate the error to the communication initiator directly with the MailConnector (no fallback possible)
            if (email != null) {
                try {
                    MailConnector.sendMailMessage(
                            false,
                            false,
                            email,
                            name,
                            subject,
                            rawCommand.getErrorMessage(),
                            language == null ? LocaleValidator.DEFAULT_LOCALE : new Locale(language)
                    );
                }
                // catch (MessagingException e) {
                // catch (UnsupportedEncodingException e) {
                catch (Exception ex) {
                    // Ignored because we can't do much now
                    getLogger().warning("Unexpected error -- message " + ex.getMessage());

                    // Note for the testers:
                    //   Don't know how to generate a UnsupportedEncodingException by just
                    //   injecting a corrupted UTF-8 sequence and/or a wrong character set
                }
            }
        }

        // Report initial exception to the "catch-all" list
        if (exception != null) {
            // Send an e-mail to out catch-all list
            MockOutputStream stackTrace = new MockOutputStream();
            exception.printStackTrace(new PrintStream(stackTrace));
            try {
                MailConnector.reportErrorToAdmins(
                        "Unexpected error caught in " + MailResponderServlet.class.getName() + ".doPost()",
                        "Mail sender: " + name + "<" + email + ">" + "\nMail subject: " + subject + "\nMail filtered content: " + command + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException ex) {
                getLogger().severe("Failure while trying to report an unexpected by e-mail! -- message: " + ex.getMessage());

                // Note for the testers:
                //   Don't know how to generate a MessagingException by just
                //   injecting a corrupted UTF-8 sequence and/or a wrong character set
            }
        }
    }

    /**
     * Return the first line with significant characters
     *
     * @param in Buffer to parse
     * @return series of significant characters
     */
    protected static String extractFirstLine(String in) {
        /* Sample of a text/plain message with: format=flowed

        !supply this is just a simple test to propose a command line going over 78
        characters, which it's going to be wrapped on many lines separated by a
        soft line break.

        Read http://tools.ietf.org/html/rfc3676 for instructions
        on text/plain message. Keep in mind that a "soft line break"
        is a SP CRLF sequence, while a "hard line break" is just CRLF.
        */

        // Warning: GMail, at least, send text/plain messages with delsp=yes,
        // the SP is automatically remove before the CRLF! It's then not possible
        // to detect the soft line breaks...

        // Solution: our policy is to consider the following cases as hard line breaks
        // 1) <text | SP>+ <CRLF>* EOM => end of message
        // 2) <text | SP>+ CRLF CRLF => one empty line detected
        // 3) <text | SP>+ CRLF -- <SP>*   => signature separator detected

        if (in == null || in.length() == 0) {
            return in;
        }
        // Trim leading separators
        int begin = 0;
        char cursor = in.charAt(begin);
        while (cursor == ' ' || cursor == '\t' || cursor == '\r' || cursor == '\n') {
            begin ++;
            cursor = in.charAt(begin);
        }
        // Detect the hard line break
        int end = in.indexOf('\n', begin);
        in += "     "; // Just to be sure the buffer is long enough for the detection of the footer separator
        int limit = in.length();
        while (true) {
            if (end == -1) {
                end = limit;
                break;
            }
            if(in.charAt(end + 1) == '\n') {
                break;
            }
            if(in.charAt(end + 1) == '\r' && in.charAt(end + 2) == '\n') {
                break;
            }
            if(in.charAt(end + 1) == '-' && in.charAt(end + 2) == '-' && in.charAt(end + 3) == '\n') {
                break;
            }
            if(in.charAt(end + 1) == '-' && in.charAt(end + 2) == '-' && in.charAt(end + 3) == ' ' && in.charAt(end + 4) == '\n') {
                break;
            }
            if(in.charAt(end + 1) == '-' && in.charAt(end + 2) == '-' && in.charAt(end + 3) == '\r' && in.charAt(end + 4) == '\n') {
                break;
            }
            if(in.charAt(end + 1) == '-' && in.charAt(end + 2) == '-' && in.charAt(end + 3) == ' ' && in.charAt(end + 4) == '\r' && in.charAt(end + 5) == '\n') {
                break;
            }
            // Jump to the next return-to-line
            end = in.indexOf('\n', end + 1);
        }
        // Trim trailing separators
        cursor = in.charAt(end - 1);
        while (cursor == ' ' || cursor == '\t' || cursor == '\r' || cursor == '\n') {
            end --;
            cursor = in.charAt(end - 1);
        }
        // Return the short message
        return in.substring(begin, end).replaceAll("\r", "").replaceAll("\n", " ");
    }
}
