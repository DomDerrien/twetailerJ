package twetailer.j2ee;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;

@SuppressWarnings("serial")
public class MockLoginServlet extends LoginServlet {

    public static final String DEFAULT_OPEN_ID = "http://unit.test";
    public static final Long DEFAULT_CONSUMER_KEY = 8045232434334L;
    public static final Long DEFAULT_SALE_ASSOCIATE_KEY = 560909864524L;

    /**
     * Create a Mock OpenIdUser instance
     *
     * @return Fake OpenID user
     */
    public static OpenIdUser buildMockOpenIdUser() {
        return buildMockOpenIdUser(DEFAULT_CONSUMER_KEY);
    }

    /**
     * Create a Mock OpenIdUser instance
     *
     * @param consumerKey Reference of the Consumer instance to mock
     * @return Fake OpenID user
     */
    public static OpenIdUser buildMockOpenIdUser(Long consumerKey) {
        return buildMockOpenIdUser(DEFAULT_OPEN_ID, consumerKey);
    }

    /**
     * Create a Mock OpenIdUser instance
     *
     * @param openId OpenID identifier of the Consumer instance to mock
     * @param consumerKey Reference of the Consumer instance to mock
     * @return Fake OpenID user
     */
    public static OpenIdUser buildMockOpenIdUser(String openId, Long consumerKey) {
        OpenIdUser user = OpenIdUser.populate(
                "http://www.yahoo.com",
                YadisDiscovery.IDENTIFIER_SELECT,
                LoginServlet.YAHOO_OPENID_SERVER_URL
        );

        //  {
        //      a: "claimId",
        //      b: "identity",
        //      c: "assocHandle",
        //      d: associationData,
        //      e: "openIdServer",
        //      f: "openIdDelegate",
        //      g: attributes,
        //      h: "identifier"
        //  }
        Map<String, Object> json = new HashMap<String, Object>();

        json.put("a", openId);

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("info", new HashMap<String, String>());
        json.put("g", attributes);

        user.fromJSON(json);

        user.setAttribute(LoginServlet.AUTHENTICATED_CONSUMER_ID, consumerKey);

        return user;
    }

    /**
     * Allow the dynamic update of the attached consumerKey
     *
     * @param user OpenID record to update
     * @param consumerKey New value
     */
    public static void updateConsumerKey(OpenIdUser user, Long consumerKey) {
        user.setAttribute(LoginServlet.AUTHENTICATED_CONSUMER_ID, consumerKey);
    }

    /**
     * Inject in the request a mock OpenUser instance built with values found among the request parameters
     * with fallback on default values
     *
     * @param parameters HTTP request parameters
     */
    protected static void injectMockOpenUser(HttpServletRequest request) {
        // Get the consumer information
        String proposeConsumerKey = request.getParameter("debugConsumerKey");
        Long consumerKey = proposeConsumerKey == null ? 1L : Long.valueOf(proposeConsumerKey);
        String proposedOpenId = request.getParameter("debugConsumerOpenId");
        String openId = proposedOpenId == null ? "http://open.id" : proposedOpenId;

        // Inject the data in an OpenIdUser and inject this one in the request
        injectMockOpenUser(request, buildMockOpenIdUser(openId, consumerKey));
    }

    /**
     * Inject in the request a mock OpenUser instance built with values found among the request parameters
     * with fallback on default values
     *
     * @param parameters HTTP request parameters
     * @param user Fetched fake user record
     */
    protected static void injectMockOpenUser(HttpServletRequest request, OpenIdUser user) {
        // Save the fake user as the request attribute
        request.setAttribute(OpenIdUser.ATTR_NAME, user);
    }

}
