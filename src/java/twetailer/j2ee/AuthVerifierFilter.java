package twetailer.j2ee;

import java.io.IOException;
import java.net.URLEncoder;
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
import javax.servlet.http.HttpSession;

import twetailer.connector.FacebookConnector;
import twetailer.dto.Consumer;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;
import com.live.login.WindowsLiveLogin;

import domderrien.i18n.StringUtils;
import domderrien.jsontools.JsonObject;

/**
 *
 * @author Dom Derrien
 */
public class AuthVerifierFilter implements Filter {

    private static Logger log = Logger.getLogger(AuthVerifierFilter.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    private final static String LIVE_SESSION_TOKEN = "liveIdWebAuthToken";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            String action = httpRequest.getParameter("action");
            String code = httpRequest.getParameter("code");

            //
            // Is Windows Live Logging calling us back?
            // Source: http://msdn.microsoft.com/en-us/library/bb676640.aspx
            //
            if (action != null && 0 < action.length()) {
                ApplicationSettings appSettings = ApplicationSettings.get();
                boolean onLocalHost = "localhost".equals(request.getServerName()) || "127.0.0.1".equals(request.getServerName()) || "10.0.2.2".equals(request.getServerName());
                WindowsLiveLogin wll = new WindowsLiveLogin("liveIdKeys-" + (onLocalHost ? "localhost" : appSettings.getAppEngineId()) + ".xml");

                if (!"POST".equals(((HttpServletRequest) request).getMethod())) {
                    getLogger().severe("POST method expected! -- Was: " + ((HttpServletRequest) request).getMethod());
                }

                /*
                 * If action is 'logout', clear the session token and redirect to the logout page.
                 *
                 * If action is 'clearcookie', clear the session token and return a GIF as response to signify success.
                 *
                 * By default, try to process a login. If login was successful, cache the user token in the session and
                 * redirect to the site's main page.  If login failed, clear the cookie and redirect to the main page.
                 */
                HttpSession session = httpRequest.getSession(false);
                if ("logout".equals(action)) {
                    if (session != null) {
                        session.setAttribute(LIVE_SESSION_TOKEN, null);
                    }
                    ((HttpServletResponse) response).sendRedirect("/console");
                }
                else if ("clearcookie".equals(action)) {
                    if (session != null) {
                        session.setAttribute(LIVE_SESSION_TOKEN, null);
                    }
                    response.setContentType(wll.getClearCookieResponseType());
                    response.getOutputStream().write(wll.getClearCookieResponseBody());
                    response.flushBuffer();
                }
                else { // if ("login".equals(action)) {
                    @SuppressWarnings("unchecked")
                    WindowsLiveLogin.User user = wll.processLogin(request.getParameterMap());

                    if (user != null) {
                        if (session != null) {
                            session = httpRequest.getSession(true);
                        }
                        getLogger().warning("Live user authenticated -- Token: " + user.getToken());
                        session.setAttribute(LIVE_SESSION_TOKEN, user.getToken());
                        
                        // TODO: create a OpenID record for this user

                        if (user.usePersistentCookie()) {
                            session.setMaxInactiveInterval(-1);
                        }

                        ((HttpServletResponse) response).sendRedirect("/console");
                    }
                    else {
                        if (session != null) {
                            session.setAttribute(LIVE_SESSION_TOKEN, null);
                        }
                        ((HttpServletResponse) response).sendRedirect("/console");
                    }
                }
            }
            //
            // Is Facebook calling us back?
            // Source: http://developers.facebook.com/docs/authentication/
            //
            else if (code != null && 0 < code.length()) {
                // Get the OAuth access token for this user
                JsonObject produced = FacebookConnector.getAccessToken(httpRequest.getRequestURL().toString(), code);
                String accessToken = produced.getString(FacebookConnector.ATTR_ACCESS_TOKEN);
                if (accessToken != null && 0 < accessToken.length()) {
                    // Get user info
                    JsonObject userInfo = FacebookConnector.getUserInfo(accessToken);

                    // Create only if does not yet exist, otherwise return the existing instance
                    Consumer consumer = BaseSteps.getConsumerOperations().createConsumer(userInfo);

                    httpRequest.getSession(true).setAttribute(OpenIdUser.ATTR_NAME, prepareOpenIdRecord(consumer));

                    // Redirect to clean the url
                    ((HttpServletResponse) response).sendRedirect(httpRequest.getRequestURL().toString());
                }
                else {
                    getLogger().warning("The Facebook user refused to give credentials to ASE.com -- reason: " + produced.toString());

                    // Redirect to get a new token
                    String facebookAuthVerifURL = FacebookConnector.bootstrapAuthUrl(httpRequest) + URLEncoder.encode(httpRequest.getRequestURL().toString(), StringUtils.JAVA_UTF8_CHARSET);
                    ((HttpServletResponse) response).sendRedirect(facebookAuthVerifURL);
                }
            }
            //
            // Let the process continue
            //
            else {
                filterChain.doFilter(request, response);
            }
        }
        catch (IOException ex) {
            getLogger().severe("Unexpected exception while processing the request -- ex: " + ex.getMessage() + "\n" + dumpRequest(httpRequest));
            throw ex;
        }
    }

    public static OpenIdUser prepareOpenIdRecord(Consumer consumer) {
        // Get initial OpenId record
        OpenIdUser user = com.dyuproject.openid.OpenIdUser.populate(
                "http://www.facebook.com",        // Identifier
                YadisDiscovery.IDENTIFIER_SELECT, // ClaimedId
                "https://www.facebook.com/login"  // OpenId server
        );

        // Inject the Facebook identifier as the OpenId user identity
        Map<String, Object> json = new HashMap<String, Object>();
        // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
        json.put("b", "facebook://" + consumer.getFacebookId() + "/" + consumer.getName()); // Means user.isAuthenticated() == true
        json.put("a", json.get("b")); // Simple copy
        json.put("h", json.get("b")); // Simple copy
        user.fromJSON(json);

        // Attach the user consumer key to save future lookups
        user.setAttribute(LoginServlet.AUTHENTICATED_CONSUMER_ID, consumer.getKey());

        return user;
    }

    /**
     * Serialize the request fields (headers, attributes, parameters) and its content
     *
     * @param request Servlet request
     * @return String for logging purposes
     */
    public static String dumpRequest(HttpServletRequest request) {
        StringBuilder out = new StringBuilder();
        out.append("******\nGeneral information:\n---\n");
        out.append("authType: ").append(request.getAuthType()).append("\n");
        out.append("encoding: ").append(request.getCharacterEncoding()).append("\n");
        out.append("******\nRemote coordinates:\n---\n");
        out.append("address: ").append(request.getRemoteAddr()).append("\n");
        out.append("host: ").append(request.getRemoteHost()).append("\n");
        out.append("port: ").append(request.getRemotePort()).append("\n");
        out.append("user: ").append(request.getRemoteUser()).append("\n");
        out.append("******\nServer coordinates:\n---\n");
        out.append("protocol: ").append(request.getProtocol()).append("\n");
        out.append("name: ").append(request.getServerName()).append("\n");
        out.append("port: ").append(request.getServerPort()).append("\n");
        out.append("request url: ").append(request.getRequestURL()).append("\n");
        out.append("query string: ").append(request.getQueryString()).append("\n");
        Enumeration<?> names = request.getAttributeNames();
        out.append("******\nAttributes:\n---\n");
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            out.append(name).append(": ").append(request.getAttribute(name)).append("\n");
        }
        names = request.getHeaderNames();
        out.append("******\nHeaders:\n---\n");
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            out.append(name).append(": ").append(request.getHeader(name)).append("\n");
        }
        names = request.getParameterNames();
        out.append("******\nParameters:\n---\n");
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            out.append(name).append(": ");
            String[] values = request.getParameterValues(name);
            int limit = values == null ? 0 : values.length;
            for (int i = 0; i < limit; i++) {
                out.append(values[i]).append(" ");
            }
            out.append("\n");
        }
        out.append("******\n");
        return out.toString();
    }
}
