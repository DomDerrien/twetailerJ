package twetailer.j2ee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.MockServletInputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import twetailer.j2ee.restlet.ConsumerRestlet;
import twetailer.j2ee.restlet.DemandRestlet;
import twetailer.j2ee.restlet.LocationRestlet;
import twetailer.j2ee.restlet.ProposalRestlet;
import twetailer.j2ee.restlet.SaleAssociateRestlet;
import twetailer.j2ee.restlet.StoreRestlet;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

/**
 * Temporary entry point for weakly authenticated requests.
 * Once the consumer identifier is determined, the requests
 * are processed by the normal entry points (the classes
 * deriving from twetailer.j2ee.BaseRestlet).
 *
 * @see twetailer.j2ee.restlet.ConsumerRestlet
 * @see twetailer.j2ee.restlet.DemandRestlet
 * @see twetailer.j2ee.restlet.LocationRestlet
 * @see twetailer.j2ee.restlet.ProposalRestlet
 * @see twetailer.j2ee.restlet.SaleAssociateRestlet
 * @see twetailer.j2ee.restlet.StoreRestlet
 *
 * @author Dom Derrien
 *
 */
@SuppressWarnings("serial")
public class ShortcutServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MaezelServlet.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    protected static OpenIdUser buildMockOpenIdUser(Long consumerKey) {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );

        Map<String, Object> json = new HashMap<String, Object>();

        json.put("a", "http://shortcut.twetailer.com/" + consumerKey);

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", new HashMap<String, String>());
        json.put("g", attributes);

        user.fromJSON(json);

        user.setAttribute(LoginServlet.AUTHENTICATED_CONSUMER_ID, consumerKey);

        return user;
    }

    protected long setMockOpenIdUser(HttpServletRequest request) {
        // Process the given parameters
        Long consumerKey = null;
        if (request.getParameter("shortId") != null) {
            consumerKey = Long.valueOf(request.getParameter("shortId"));
        }
        if (consumerKey == null || consumerKey == 0L) {
            throw new IllegalArgumentException("Invalid owner Key");
        }
        if (request.getAttribute(OpenIdUser.ATTR_NAME) != null) {
            OpenIdUser existingUser = (OpenIdUser) request.getAttribute(OpenIdUser.ATTR_NAME);
            if (LoginServlet.getConsumerKey(existingUser) != consumerKey) {
                throw new IllegalArgumentException("Cannot switch user");
            }
        }
        else {
            OpenIdUser user = buildMockOpenIdUser(consumerKey);
            request.setAttribute(OpenIdUser.ATTR_NAME, user);
        }
        return consumerKey;
    }

    private final static String CURRENT_ID = "/current";
    private final static String NUMERICAL_ID = "/\\w+";
    private final static String CONSUMER_PREFIX = "/Consumer";
    private final static String SALE_ASSOCIATE_PREFIX = "/SaleAssociate";
    private final static String DEMAND_PREFIX = "/Demand";
    private final static String PROPOSAL_PREFIX = "/Proposal";
    private final static String LOCATION_PREFIX = "/Location";
    private final static String STORE_PREFIX = "/Store";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        setMockOpenIdUser(request);

        String pathInfo = request.getPathInfo();
        getLogger().warning("Path Info: " + pathInfo);

        if ((CONSUMER_PREFIX + CURRENT_ID).equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper(CURRENT_ID, request);
            new ConsumerRestlet().doGet(alteredRequest, response);
        }
        else if ((SALE_ASSOCIATE_PREFIX + CURRENT_ID).equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper(CURRENT_ID, request);
            new SaleAssociateRestlet().doGet(alteredRequest, response);
        }
        else if (DEMAND_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new DemandRestlet().doGet(alteredRequest, response);
        }
        else if (Pattern.matches(DEMAND_PREFIX + NUMERICAL_ID, pathInfo)) {
            String alteredPathInfo = pathInfo.substring(DEMAND_PREFIX.length());
            HttpServletRequest alteredRequest = new HttpRequestWrapper(alteredPathInfo, request);
            new DemandRestlet().doGet(alteredRequest, response);
        }
        else if (PROPOSAL_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new ProposalRestlet().doGet(alteredRequest, response);
        }
        else if (Pattern.matches(PROPOSAL_PREFIX + NUMERICAL_ID, pathInfo)) {
            String alteredPathInfo = pathInfo.substring(PROPOSAL_PREFIX.length());
            HttpServletRequest alteredRequest = new HttpRequestWrapper(alteredPathInfo, request);
            new ProposalRestlet().doGet(alteredRequest, response);
        }
        else if (LOCATION_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new LocationRestlet().doGet(alteredRequest, response);
        }
        else if (Pattern.matches(LOCATION_PREFIX + NUMERICAL_ID, pathInfo)) {
            String alteredPathInfo = pathInfo.substring(LOCATION_PREFIX.length());
            HttpServletRequest alteredRequest = new HttpRequestWrapper(alteredPathInfo, request);
            new LocationRestlet().doGet(alteredRequest, response);
        }
        else if (STORE_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new StoreRestlet().doGet(alteredRequest, response);
        }
        else if (Pattern.matches(STORE_PREFIX + NUMERICAL_ID, pathInfo)) {
            String alteredPathInfo = pathInfo.substring(STORE_PREFIX.length());
            HttpServletRequest alteredRequest = new HttpRequestWrapper(alteredPathInfo, request);
            new StoreRestlet().doGet(alteredRequest, response);
        }
        else {
            response.setStatus(404); // Not Found
            JsonObject out = new GenericJsonObject();
            out.put("success", false);
            out.put("reason", "URL not supported");
            out.toStream(response.getOutputStream(), false);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        setMockOpenIdUser(request);

        String pathInfo = request.getPathInfo();
        getLogger().warning("Path Info: " + pathInfo);

        if (CONSUMER_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new ConsumerRestlet().doPost(alteredRequest, response);
        }
        else if (SALE_ASSOCIATE_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new SaleAssociateRestlet().doPost(alteredRequest, response);
        }
        else if (DEMAND_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new DemandRestlet().doPost(alteredRequest, response);
        }
        else if (PROPOSAL_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new ProposalRestlet().doPost(alteredRequest, response);
        }
        else if (LOCATION_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new LocationRestlet().doPost(alteredRequest, response);
        }
        else if (STORE_PREFIX.equals(pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper("", request);
            new StoreRestlet().doPost(alteredRequest, response);
        }
        else {
            response.setStatus(404); // Not Found
            JsonObject out = new GenericJsonObject();
            out.put("success", false);
            out.put("reason", "URL not supported");
            out.toStream(response.getOutputStream(), false);
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        setMockOpenIdUser(request);

        String pathInfo = request.getPathInfo();
        getLogger().warning("Path Info: " + pathInfo);

        if (pathInfo == null || pathInfo.length() == 0) {
            JsonObject out = new GenericJsonObject();
            response.setStatus(404); // Not Found
            out.put("success", false);
            out.put("reason", "URL not supported");
            out.toStream(response.getOutputStream(), false);
        }
        else if (Pattern.matches(CONSUMER_PREFIX + CURRENT_ID, pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper(CURRENT_ID, request);
            new ConsumerRestlet().doPut(alteredRequest, response);
        }
        else if (Pattern.matches(SALE_ASSOCIATE_PREFIX + CURRENT_ID, pathInfo)) {
            HttpServletRequest alteredRequest = new HttpRequestWrapper(CURRENT_ID, request);
            new SaleAssociateRestlet().doPut(alteredRequest, response);
        }
        else if (Pattern.matches(DEMAND_PREFIX + NUMERICAL_ID, pathInfo)) {
            String alteredPathInfo = pathInfo.substring(DEMAND_PREFIX.length());
            HttpServletRequest alteredRequest = new HttpRequestWrapper(alteredPathInfo, request);
            new DemandRestlet().doPut(alteredRequest, response);
        }
        else if (Pattern.matches(PROPOSAL_PREFIX + NUMERICAL_ID, pathInfo)) {
            String alteredPathInfo = pathInfo.substring(PROPOSAL_PREFIX.length());
            HttpServletRequest alteredRequest = new HttpRequestWrapper(alteredPathInfo, request);
            new ProposalRestlet().doPut(alteredRequest, response);
        }
        else if (Pattern.matches(STORE_PREFIX + NUMERICAL_ID, pathInfo)) {
            String alteredPathInfo = pathInfo.substring(STORE_PREFIX.length());
            HttpServletRequest alteredRequest = new HttpRequestWrapper(alteredPathInfo, request);
            new StoreRestlet().doPut(alteredRequest, response);
        }
        else {
            JsonObject out = new GenericJsonObject();
            response.setStatus(404); // Not Found
            out.put("success", false);
            out.put("reason", "URL not supported");
            out.toStream(response.getOutputStream(), false);
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        setMockOpenIdUser(request);

        String pathInfo = request.getPathInfo();
        getLogger().warning("Path Info: " + pathInfo);

        if (pathInfo == null || pathInfo.length() == 0) {
            JsonObject out = new GenericJsonObject();
            response.setStatus(404); // Not Found
            out.put("success", false);
            out.put("reason", "URL not supported");
            out.toStream(response.getOutputStream(), false);
        }
        else if (Pattern.matches(DEMAND_PREFIX + NUMERICAL_ID, pathInfo)) {
            String alteredPathInfo = pathInfo.substring(DEMAND_PREFIX.length());
            HttpServletRequest alteredRequest = new HttpRequestWrapper(alteredPathInfo, request);
            new DemandRestlet().doDelete(alteredRequest, response);
        }
        else {
            JsonObject out = new GenericJsonObject();
            response.setStatus(404); // Not Found
            out.put("success", false);
            out.put("reason", "URL not supported");
            out.toStream(response.getOutputStream(), false);
        }
    }

    @SuppressWarnings({"unchecked","deprecation"})
    protected class HttpRequestWrapper implements HttpServletRequest {

        private String alteredPathInfo;
        private HttpServletRequest request;

        public HttpRequestWrapper(String alteredPathInfo, HttpServletRequest request) {
            this.alteredPathInfo = alteredPathInfo;
            this.request = request;
        }

        @Override public String getAuthType() { return request.getAuthType(); }
        @Override public String getContextPath() { return request.getContextPath(); }
        @Override public Cookie[] getCookies() { return request.getCookies(); }
        @Override public long getDateHeader(String arg0) { return request.getDateHeader(arg0); }
        @Override public String getHeader(String arg0) { return request.getHeader(arg0); }
        @Override public Enumeration getHeaderNames() { return request.getHeaderNames(); }
        @Override public Enumeration getHeaders(String arg0) { return request.getHeaders(arg0); }
        @Override public int getIntHeader(String arg0) { return request.getIntHeader(arg0); }
        @Override public String getMethod() { return request.getMethod(); }
        @Override public String getPathTranslated() { return request.getPathTranslated(); }
        @Override public String getQueryString() { return request.getQueryString(); }
        @Override public String getRemoteUser() { return request.getRemoteUser(); }
        @Override public String getRequestURI() { return request.getRequestURI(); }
        @Override public StringBuffer getRequestURL() { return request.getRequestURL(); }
        @Override public String getRequestedSessionId() { return request.getRequestedSessionId(); }
        @Override public String getServletPath() { return request.getServletPath(); }
        @Override public HttpSession getSession() { return request.getSession(); }
        @Override public HttpSession getSession(boolean arg0) { return request.getSession(arg0); }
        @Override public Principal getUserPrincipal() { return request.getUserPrincipal(); }
        @Override public boolean isRequestedSessionIdFromCookie() { return request.isRequestedSessionIdFromCookie(); }
        @Override public boolean isRequestedSessionIdFromURL() { return request.isRequestedSessionIdFromURL(); }
        @Override public boolean isRequestedSessionIdFromUrl() { return request.isRequestedSessionIdFromUrl(); }
        @Override public boolean isRequestedSessionIdValid() { return request.isRequestedSessionIdValid(); }
        @Override public boolean isUserInRole(String arg0) { return request.isUserInRole(arg0); }
        @Override public Object getAttribute(String arg0) { return request.getAttribute(arg0); }
        @Override public Enumeration getAttributeNames() { return request.getAttributeNames(); }
        @Override public String getCharacterEncoding() { return request.getCharacterEncoding(); }
        @Override public int getContentLength() { return request.getContentLength(); }
        @Override public String getContentType() { return request.getContentType(); }
        @Override public String getLocalAddr() { return request.getLocalAddr(); }
        @Override public String getLocalName() { return request.getLocalName(); }
        @Override public int getLocalPort() { return request.getLocalPort(); }
        @Override public Locale getLocale() { return request.getLocale(); }
        @Override public Enumeration getLocales() { return request.getLocales(); }
        @Override public String getParameter(String arg0) { return request.getParameter(arg0); }
        @Override public Map getParameterMap() { return request.getParameterMap(); }
        @Override public Enumeration getParameterNames() { return request.getParameterNames(); }
        @Override public String[] getParameterValues(String arg0) { return request.getParameterValues(arg0); }
        @Override public String getProtocol() { return request.getProtocol(); }
        @Override public BufferedReader getReader() throws IOException { return request.getReader(); }
        @Override public String getRealPath(String arg0) { return request.getRealPath(arg0); }
        @Override public String getRemoteAddr() { return request.getRemoteAddr(); }
        @Override public String getRemoteHost() { return request.getRemoteHost(); }
        @Override public int getRemotePort() { return request.getRemotePort(); }
        @Override public RequestDispatcher getRequestDispatcher(String arg0) { return request.getRequestDispatcher(arg0); }
        @Override public String getScheme() { return request.getScheme(); }
        @Override public String getServerName() { return request.getServerName(); }
        @Override public int getServerPort() { return request.getServerPort(); }
        @Override public boolean isSecure() { return request.isSecure(); }
        @Override public void removeAttribute(String arg0) { request.removeAttribute(arg0); }
        @Override public void setAttribute(String arg0, Object arg1) { request.setAttribute(arg0, arg1); }
        @Override public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException { request.setCharacterEncoding(arg0); }

        // Updated methods
        @Override public String getPathInfo() { return alteredPathInfo; }

        private StringBuilder extractedString = null;
        @Override public ServletInputStream getInputStream() throws IOException {
            if (extractedString == null) {
                InputStream stream = request.getInputStream();
                extractedString = new StringBuilder();
                try {
                    while (true) {
                        int character = stream.read();
                        if(character == -1) break;
                        extractedString.append((char) character);
                    }
                    System.err.println("### Input stream: " + extractedString.toString());
                }
                catch (IOException ex) {
                    throw new RuntimeException("Cannot extract the input stream...", ex);
                }
            }
            return new MockServletInputStream(extractedString.toString());
        }
    }
}
