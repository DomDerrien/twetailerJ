package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

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
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.LocaleValidator;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;

@SuppressWarnings("serial")
public class MailResponderServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MailResponderServlet.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warning("Path Info: " + request.getPathInfo());

        processMailedRequest(request, response);
    }

    protected static void processMailedRequest(HttpServletRequest request, HttpServletResponse response) {
        Consumer consumer = null;
        String name = null;
        String email = null;
        String subject = null;
        String language = LocaleValidator.DEFAULT_LANGUAGE;
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

            // Extract information about the sender
            InternetAddress address = (InternetAddress) (mailMessage.getFrom()[0]);
            name = address.getPersonal();
            email = address.getAddress();
            subject = mailMessage.getSubject();
            if (mailMessage.getContentLanguage() != null) {
                language = mailMessage.getContentLanguage()[0];
            }

            // Fill up the message to persist
            rawCommand.setEmitterId(email);
            rawCommand.setSubject(subject);
            command = extractFirstLine(MailConnector.getText(mailMessage));
            rawCommand.setCommand(command);

            log.warning("Message sent by: " + email + " with the subject: " + subject + "\nWith the command: " + command);

            // Creation only occurs if the corresponding Consumer instance is not retrieved
            consumer = consumerOperations.createConsumer(address);

            // Persist message
            rawCommandOperations.createRawCommand(rawCommand);

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
            rawCommand.setErrorMessage(LabelExtractor.get("error_mail_messaging", new Locale(language)));
        }
        catch (DatastoreTimeoutException ex) {
            exception = ex;
            rawCommand.setErrorMessage(LabelExtractor.get("error_datastore_timeout", new Locale(language)));
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
                            new Locale(language)
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
                            email,
                            name,
                            subject,
                            rawCommand.getErrorMessage(),
                            new Locale(language)
                    );
                }
                catch (UnsupportedEncodingException e) {
                    // Ignored because we can't do much now

                    // Note for the testers:
                    //   Don't know how to generate a UnsupportedEncodingException by just
                    //   injecting a corrupted UTF-8 sequence and/or a wrong character set
                }
                catch (MessagingException e) {
                    // Ignored because we can't do much now
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
