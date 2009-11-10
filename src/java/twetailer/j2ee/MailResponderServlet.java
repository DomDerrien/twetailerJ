package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ClientException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.validator.ApplicationSettings;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;

@SuppressWarnings("serial")
public class MailResponderServlet extends HttpServlet {

    protected BaseOperations _baseOperations = new BaseOperations();
    protected RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Consumer consumer = null;

        // Prepare the message to persist
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.mail);

        try {
            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);
            if (mailMessage.getFrom() == null) {
                throw new MessagingException("Incorrect message (no FROM!)");
            }

            // Creation only occurs if the corresponding Consumer instance is not retrieved
            consumer = consumerOperations.createConsumer((InternetAddress) (mailMessage.getFrom()[0]));

            // Fill up the message to persist
            rawCommand.setEmitterId(consumer.getEmail());
            rawCommand.setCommand(MailConnector.getText(mailMessage));
        }
        catch (MessagingException ex) {
            rawCommand.setErrorMessage("Error while parsing the mail message -- ex: " + ex.getMessage());
        }

        // Persist message
        rawCommandOperations.createRawCommand(rawCommand);

        if (rawCommand.getErrorMessage() != null) {
            try {
                BaseConnector.communicateToConsumer(
                        Source.mail,
                        consumer,
                        LabelExtractor.get(
                                "cp_unexpected_error",
                                new Object[] { rawCommand.getKey(), rawCommand.getErrorMessage()},
                                Locale.ENGLISH)
                );
            }
            catch (ClientException e) {
                // Ignored because we can't do much now
            }
        }
        else {
            // Create a task for that command
            Queue queue = QueueFactory.getDefaultQueue();
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/processCommand").
                        param(Command.KEY, rawCommand.getKey().toString()).
                        method(Method.GET)
            );
        }
    }
}
