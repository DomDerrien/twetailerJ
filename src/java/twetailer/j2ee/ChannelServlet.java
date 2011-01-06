package twetailer.j2ee;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.ReservedOperationException;
import twetailer.connector.ChannelConnector;

import com.dyuproject.openid.OpenIdUser;

import domderrien.i18n.StringUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

/**
 * Entry point processing XMPP messages.
 * Received information are stored in a RawCommand instance
 * that the task "/_tasks/processCommand" will process
 * asynchronously.
 *
 * @see twetailer.dto.RawCommand
 * @see twetailer.j2ee.MaelzelServlet
 *
 * @author Dom Derrien
 *
 */
@SuppressWarnings("serial")
public class ChannelServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(ChannelServlet.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.configureHttpParameters(request, response);

        String pathInfo = request.getPathInfo();

        JsonObject out = new GenericJsonObject();
        out.put("success", true);
        JsonObject in = null;

        try {
            // TODO: verify Content-type == "application/json"
            in = new JsonParser(request.getInputStream(), StringUtils.JAVA_UTF8_CHARSET).getJsonObject();

            OpenIdUser loggedUser = BaseRestlet.getLoggedUser(request);
            getLogger().finest("*** JSessionId: " + (request.getSession(false) == null ? "no session" : request.getSession(false).getId()) + " -- identity: " + (loggedUser == null ? "no record!" : loggedUser.getIdentity()));

            if (loggedUser == null) {
                response.setStatus(401); // Unauthorized
                out.put("success", false);
                out.put("reason", "Unauthorized");
            }
            else if ("getToken".equals(in.getString("action"))) {
                out.put("token", ChannelConnector.getUserToken(LoginServlet.getConsumer(loggedUser)));
            }
            else if ("register".equals(in.getString("action"))) {
                ChannelConnector.register(LoginServlet.getConsumer(loggedUser));
            }
            else if ("unregister".equals(in.getString("action"))) {
                ChannelConnector.unregister(LoginServlet.getConsumer(loggedUser));
            }
            else {
                response.setStatus(400); // Unavailable
                out.put("success", false);
                out.put("reason", "Unauthorized");
            }
        }
        catch (ReservedOperationException ex) {
            response.setStatus(403); // Forbidden
            out.put("success", false);
            out.put("reason", ex.getMessage());
        }
        catch (Exception ex) {
            response.setStatus(500); // Internal Server Error
            out = BaseRestlet.processException(ex, "doPost", pathInfo, BaseRestlet.debugModeDetected(request) || BaseRestlet.debugModeDetected(in));
        }

        out.toStream(response.getOutputStream(), false);
    }
}
