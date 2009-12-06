package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.connector.JabberConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.validator.ApplicationSettings;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.appengine.api.xmpp.Message;

@SuppressWarnings("serial")
public class JabberResponderServlet extends HttpServlet {

    protected BaseOperations _baseOperations = new BaseOperations();
    protected RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Extract the incoming message
        Message instantMessage = JabberConnector.getInstantMessage(request);

        // Creation only occurs if the corresponding Consumer instance is not retrieved
        Consumer consumer = consumerOperations.createConsumer(instantMessage.getFromJid());

        // Prepare the message to persist
        RawCommand rawCommand = new RawCommand(Source.jabber);
        rawCommand.setEmitterId(consumer.getJabberId());
        rawCommand.setCommand(instantMessage.getBody());

        // Persist message
        rawCommandOperations.createRawCommand(rawCommand);

        // Create a task for that command
        Queue queue = _baseOperations.getQueue();
        queue.add(
                url(ApplicationSettings.get().getServletApiPath() + "/maezel/processCommand").
                    param(Command.KEY, rawCommand.getKey().toString()).
                    method(Method.GET)
        );
    }
}
