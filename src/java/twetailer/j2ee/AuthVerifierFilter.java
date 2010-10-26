package twetailer.j2ee;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.connector.FacebookConnector;
import twetailer.dto.Consumer;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;

import domderrien.jsontools.JsonObject;

/**
 *
 * @author Dom Derrien
 */
public class AuthVerifierFilter implements Filter {
    private static Logger log = Logger.getLogger(AuthVerifierFilter.class.getName());

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // Is Facebook calling us back?
            //
            // Source: http://developers.facebook.com/docs/authentication/
            //
            String code = httpRequest.getParameter("code");
            if (code != null && 0 < code.length()) {
                // Get the OAuth access token for this user
                JsonObject produced = FacebookConnector.getAccessToken(httpRequest.getRequestURL().toString(), code);
                String accessToken = produced.getString(FacebookConnector.ATTR_ACCESS_TOKEN);
                if (accessToken != null && 0 < accessToken.length()) {
                    // Get user info
                    JsonObject userInfo = FacebookConnector.getUserInfo(accessToken);

                    // Create only if does not yet exist, otherwise return the existing instance
                    Consumer consumer = BaseSteps.getConsumerOperations().createConsumer(userInfo);

                    httpRequest.getSession(true).setAttribute(OpenIdUser.ATTR_NAME, prepareOpenIdUser(consumer));

                    // Redirect to clean the url
                    ((HttpServletResponse) response).sendRedirect(httpRequest.getRequestURL().toString());
                }
                else {
                    log.warning("The Facebook user refused to give credentials to ASE.com -- reason: " + produced.toString());

                    // Redirect to get a new token
                    String facebookAuthVerifURL = "https://graph.facebook.com/oauth/authorize?client_id=" + FacebookConnector.ASE_FACEBOOK_APP_ID;
                    facebookAuthVerifURL += "&redirect_uri=" + httpRequest.getRequestURL();
                    ((HttpServletResponse) response).sendRedirect(facebookAuthVerifURL);
                }
            }
            else {
                filterChain.doFilter(request, response);
            }
        }
        catch (IOException ex) {
            log.severe("Unexpected exception while processing the request -- ex: " + ex.getMessage() + "\n" + dumpResponse(httpRequest));
            throw ex;
        }
    }

    protected static OpenIdUser prepareOpenIdUser(Consumer consumer) {
        // Get initial OpenId record
        OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate(
                "http://www.facebook.com",        // Identifier
                YadisDiscovery.IDENTIFIER_SELECT, // ClaimedId
                "http://www.facebook.com"         // OpenId server
        );

        // Inject the Facebook identifier as the OpenId user identity
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("b", "facebook://" + consumer.getFacebookId() + "/" + consumer.getName()); // Means user.isAuthenticated() == true
        user.fromJSON(json);

        // Attach the user consumer key to save future lookups
        user.setAttribute(LoginServlet.AUTHENTICATED_CONSUMER_ID, consumer.getKey());

        return user;
    }

    protected static String dumpResponse(HttpServletRequest request) {
        StringBuilder out = new StringBuilder();
        out.append("******\n");
        out.append("remote address: ").append(request.getRemoteAddr()).append("\n");
        out.append("remote host: ").append(request.getRemoteHost()).append("\n");
        out.append("remote port: ").append(request.getRemotePort()).append("\n");
        out.append("remote user: ").append(request.getRemoteUser()).append("\n");
        out.append("******\n");
        out.append("protocol: ").append(request.getProtocol()).append("\n");
        out.append("server name: ").append(request.getServerName()).append("\n");
        out.append("server port: ").append(request.getServerPort()).append("\n");
        out.append("request url: ").append(request.getRequestURL()).append("\n");
        out.append("query string: ").append(request.getQueryString()).append("\n");
        Enumeration<?> names = request.getAttributeNames();
        out.append("******\n");
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            out.append("attr '").append(name).append("': ").append(request.getAttribute(name)).append("\n");
        }
        names = request.getHeaderNames();
        out.append("******\n");
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            out.append("header '").append(name).append("': ").append(request.getHeader(name)).append("\n");
        }
        names = request.getParameterNames();
        out.append("******\n");
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            out.append("param '").append(name).append("': ").append(request.getParameter(name)).append("\n");
        }
        out.append("******\n");
        return out.toString();
    }
}
