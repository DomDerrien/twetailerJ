package twetailer.j2ee;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class OAuthVerifierServlet extends HttpServlet {

    private final static String TWETAILER_KEY = "twetailer.appspot.com";
    private final static String TWETAILER_SECRET = "gZctFL7avK9O_RoMkEx0MVO8";

    private final static String ASE_KEY = "anothersocialeconomy.appspot.com";
    private final static String ASE_SECRET = "Taxd-BtrbP8ISGStJUdvVPZ_";

    public static String getOAuthKey(String appId) {
        return "twetailer".equals(appId) ? TWETAILER_KEY : ASE_KEY;
    }

    public static String getOAuthSecret(String appId) {
        return "twetailer".equals(appId) ? TWETAILER_SECRET : ASE_SECRET;
    }

    public static String getRequestTokenUrl(String appId) {
        return "https://" + appId + ".appspot.com/_ah/OAuthGetRequestToken";
    }

    public static String getAuthorizeUrl(String appId) {
        return "https://" + appId + ".appspot.com/_ah/OAuthAuthorizeToken";
    }

    public static String getAccessTokenUrl(String appId) {
        return "https://" + appId + ".appspot.com/_ah/OAuthGetAccessToken";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Source: http://ikaisays.com/2011/05/26/setting-up-an-oauth-provider-on-google-app-engine/

        ServletUtils.configureHttpParameters(request, response);

        response.setStatus(200); // OK
        JsonObject out = new GenericJsonObject();

        try {
            OAuthService oauth = OAuthServiceFactory.getOAuthService();
            User user = oauth.getCurrentUser();

            out.put("success", true);
            out.put("email", user.getEmail());
            out.put("entryPoint", request.getRequestURI());

            out.put("params", new GenericJsonObject(request.getParameterMap()));

            JsonObject headers = new GenericJsonObject();
            Enumeration<?> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                headers.put((String) headerName, (String) request.getHeader(headerName));
            }
            out.put("headers", headers);
        }
        catch (OAuthRequestException ex) {
            response.setStatus(401); // Unauthorized

            out.put("success", false);
            out.put("reason", "Not authenticated: " + ex.getMessage());
        }
        catch (Exception ex) {
            response.setStatus(500); // Server error
        }

        out.toStream(response.getOutputStream(), false);
    }

}
