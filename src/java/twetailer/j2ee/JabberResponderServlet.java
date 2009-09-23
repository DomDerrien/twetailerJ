package twetailer.j2ee;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.connector.JabberConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.RawCommand;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.appengine.api.xmpp.Message;

@SuppressWarnings("serial")
public class JabberResponderServlet extends HttpServlet {

  protected BaseOperations _baseOperations = new BaseOperations();
  protected RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Extract the incoming message
    Message instantMessage = JabberConnector.getInstantMessage(request);

    // Prepare the message to persist
    RawCommand rawCommand = new RawCommand();
    rawCommand.setSource(Source.jabber);
    rawCommand.setEmitterId(instantMessage.getFromJid().getId());
    rawCommand.setCommand(instantMessage.getBody());

    // Persist message
    rawCommandOperations.createRawCommand(rawCommand);

    // Create a task for that command
    Queue queue = QueueFactory.getDefaultQueue();
    queue.add(url("/tasks/Maezel/processCommand").param("key", rawCommand.getKey().toString()).method(Method.GET));
  }
}
