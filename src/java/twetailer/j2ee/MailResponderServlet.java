package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

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
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.HashTag;
import twetailer.dto.RawCommand;
import twetailer.task.CommandLineParser;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Prefix;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class MailResponderServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MailResponderServlet.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();
    protected static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processMailedRequest(request, response);
    }

    public static List<String> responderEndpoints = new ArrayList<String>();
    static {
        responderEndpoints.add("assistant@twetailer.appspotmail.com");
        responderEndpoints.add("maezel@twetailer.appspotmail.com");
        responderEndpoints.add("hub@twetailer.appspotmail.com");
        responderEndpoints.add("thehub@twetailer.appspotmail.com");

        String[] hashTags = HashTag.getHashTagsArray();
        for (int idx=0; idx<hashTags.length; idx ++) {
            responderEndpoints.add(hashTags[idx] + "@twetailer.appspotmail.com");
        }
    }

    private static final String MESSAGE_ID_LIST = "mailMessageIds";

    @SuppressWarnings("unchecked")
    protected static void processMailedRequest(HttpServletRequest request, HttpServletResponse response) {
        String to = null;
        String name = null;
        String email = null;
        String subject = null;
        String language = null;
        Exception exception = null;
        String command = null;

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
                List<String> lastMailMessageIds = (List<String>) settingsOperations.getFromCache(MESSAGE_ID_LIST);
                if (lastMailMessageIds != null && lastMailMessageIds.contains(messageId)) {
                    log.warning("**** Email '" + messageId + "' already processed");
                    return;
                }
                if (lastMailMessageIds == null) {
                    lastMailMessageIds = new ArrayList<String>();
                    log.warning("**** Create list in MemCache to store the last mail message identifiers.");
                }
                lastMailMessageIds.add(messageId);
                settingsOperations.setInCache(MESSAGE_ID_LIST, lastMailMessageIds);
            }

            // Extract information about the sender
            InternetAddress address = (InternetAddress) (mailMessage.getFrom()[0]);
            name = address.getPersonal();
            email = address.getAddress();
            subject = mailMessage.getSubject();
            if (mailMessage.getContentLanguage() != null) {
                language = LocaleValidator.checkLanguage(mailMessage.getContentLanguage()[0]);
            }

            // Extract information about a supported receiver
            InternetAddress[] recipients = (InternetAddress []) (mailMessage.getRecipients(Message.RecipientType.TO));
            for (int idx = 0; to == null && idx < recipients.length; idx ++) {
                to = recipients[idx].getAddress();
                if (!responderEndpoints.contains(to)) {
                    to = null;
                }
            }
            if (to == null) {
                log.warning("**** Email '" + messageId + "' not addressed to Twetailer!");
                return;
            }
            StringBuilder ccList = new StringBuilder();
            recipients = (InternetAddress []) (mailMessage.getRecipients(Message.RecipientType.CC));
            if (recipients != null && 0 < recipients.length) {
                // TODO: push this code below to fallback on the consumer locale
                Locale locale = language == null ? Locale.ENGLISH : new Locale(language);
                log.warning("Language for the CC prefix: " + language);
                CommandLineParser.loadLocalizedSettings(locale);
                JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
                String ccPrefix = prefixes.getJsonArray(Prefix.cc.toString()).getString(0);
                for (int idx = 0; idx < recipients.length; idx ++) {
                    String ccAddress = recipients[idx].getAddress();
                    if (!responderEndpoints.contains(ccAddress)) {
                        ccList.append(Command.SPACE).append(ccPrefix).append(CommandLineParser.PREFIX_SEPARATOR).append(ccAddress);
                    }
                }
                command = ccList.insert(0, command).toString();
            }

            // Add vertical information
            String toBase = to.substring(0, to.indexOf('@'));
            if (HashTag.getHashTagsList().contains(toBase)) {
                command += " #" + toBase;
            }

            // Fill up the message to persist
            rawCommand.setCommandId(messageId);
            rawCommand.setEmitterId(email.toLowerCase());
            rawCommand.setToId(to.toLowerCase());
            rawCommand.setSubject(subject);
            command = extractFirstLine(MailConnector.getText(mailMessage));
            rawCommand.setCommand(command);

            log.warning("Message sent by: " + name + " <" + email + ">\nWith the identifier: " + messageId + "\nWith the subject: " + subject + "\nWith the command: " + command);

            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                // Creation only occurs if the corresponding Consumer instance is not retrieved
                Consumer consumer = consumerOperations.createConsumer(pm, address);
                if (language == null) {
                    language = consumer.getLanguage();
                }

                // Persist message
                rawCommand = rawCommandOperations.createRawCommand(pm, rawCommand);
            }
            finally {
                pm.close();
            }

            // Create a task for to process that new command
            Queue queue = _baseOperations.getQueue();
            log.warning("Preparing the task: /maezel/processCommand?key=" + rawCommand.getKey().toString());
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/processCommand").
                        param(Command.KEY, rawCommand.getKey().toString()).
                        method(Method.GET)
            );
        }
        catch (MessagingException ex) {
            exception = ex;
            rawCommand.setErrorMessage(LabelExtractor.get("error_mail_messaging", language == null ? LocaleValidator.DEFAULT_LOCALE : new Locale(language)));
        }
        catch (DatastoreTimeoutException ex) {
            exception = ex;
            rawCommand.setErrorMessage(LabelExtractor.get("error_datastore_timeout", language == null ? LocaleValidator.DEFAULT_LOCALE : new Locale(language)));
        }
        catch (Exception ex) {
            exception = ex;
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
                rawCommandOperations.createRawCommand(rawCommand);
            }
            else {
                rawCommandOperations.updateRawCommand(rawCommand);
            }
            // Communicate the error to the communication initiator directly with the MailConnector (no fallback possible)
            if (email != null) {
                try {
                    MailConnector.sendMailMessage(
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
                catch (Exception e) {
                    // Ignored because we can't do much now

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
                CatchAllMailHandlerServlet.composeAndPostMailMessage(
                        "error-notifier",
                        "Unexpected error caught in " + MailResponderServlet.class.getName() + ".doPost()",
                        "Mail sender: " + name + "<" + email + ">" + "\nMail subject: " + subject + "\nMail filtered content: " + command + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                log.severe("Failure while trying to report an unexpected by e-mail!");

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
        // Detect the end-of-line
        int end = in.indexOf('\n', begin);
        if (end == -1) {
            end = in.length();
        }
        // Trim trailing separators
        cursor = in.charAt(end - 1);
        while (cursor == ' ' || cursor == '\t' || cursor == '\r') { // || cursor == '\n') { // \n excluded from the list because this the searched separator for the end of the command line!
            end --;
            cursor = in.charAt(end - 1);
        }
        // Return the short message
        return in.substring(begin, end);
    }
}
