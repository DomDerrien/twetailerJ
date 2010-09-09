package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

import javax.jdo.PersistenceManager;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.connector.BaseConnector;
import twetailer.connector.JabberConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.LocaleValidator;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;

import domderrien.i18n.LabelExtractor;

/**
 * Entry point processing XMPP messages.
 * Received information are stored in a RawCommand instance
 * that the task "/maelzel/processCommand" will process
 * asynchronously.
 *
 * @see twetailer.dto.RawCommand
 * @see twetailer.j2ee.MaelzelServlet
 *
 * @author Dom Derrien
 *
 */
@SuppressWarnings("serial")
public class JabberResponderServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(JabberResponderServlet.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Consumer consumer = null;
        String jabberId = null;
        String language = LocaleValidator.DEFAULT_LANGUAGE;
        Exception exception = null;
        String command = null;

        // Prepare the message to persist
        RawCommand rawCommand = new RawCommand(Source.jabber);

        try {
            // Extract the incoming message
            Message instantMessage = JabberConnector.getInstantMessage(request);

            // Extract information about the sender
            JID address = instantMessage.getFromJid();
            jabberId = address.getId().toLowerCase();
            command = instantMessage.getBody();

            // Prepare the message to persist
            rawCommand.setEmitterId(jabberId);
            rawCommand.setCommand(command);

            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                // Creation only occurs if the corresponding Consumer instance is not retrieved
                consumer = BaseSteps.getConsumerOperations().createConsumer(address);
                language = consumer.getLanguage();

                // Persist message
                rawCommand = BaseSteps.getRawCommandOperations().createRawCommand(rawCommand);
            }
            finally {
                pm.close();
            }

            // Create a task for to process that new command
            Queue queue = BaseSteps.getBaseOperations().getQueue();
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maelzel/processCommand").
                        param(Command.KEY, rawCommand.getKey().toString()).
                        method(Method.GET)
            );
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
                BaseSteps.getRawCommandOperations().createRawCommand(rawCommand);
            }
            else {
                BaseSteps.getRawCommandOperations().updateRawCommand(rawCommand);
            }
            // Communicate the error to the communication initiator
            if (consumer != null) {
                try {
                    // Useful because it has a fallback mechanism on e-mail
                    BaseConnector.communicateToConsumer(
                            rawCommand,
                            consumer,
                            new String[] { rawCommand.getErrorMessage() }
                    );
                }
                catch (ClientException e) {
                    // Ignored because we can't do much now
                }
            }
            else {
                try {
                    JabberConnector.sendInstantMessage(jabberId, rawCommand.getErrorMessage());
                }
                catch (ClientException e) {
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
                        "Unexpected error caught in " + this.getClass().getName() + ".doPost()",
                        "IM sender: " + jabberId + "\nIM content: " + command + "\n\n--\n\n" + stackTrace.toString()
                );
            }
            catch (MessagingException e) {
                log.severe("Failure while trying to report an unexpected by IM!");

                // Note for the testers:
                //   Don't know how to generate a MessagingException by just
                //   injecting a corrupted UTF-8 sequence and/or a wrong character set
            }
        }
    }
}
