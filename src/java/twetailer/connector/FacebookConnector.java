package twetailer.connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import domderrien.i18n.StringUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

/**
 * Definition of the methods specific to communication via Facebook
 *
 * @author Dom Derrien
 */
public class FacebookConnector {
    private static Logger log = Logger.getLogger(FacebookConnector.class.getName());

    /* Information for OAuth authentication
     * Application Id:        161355780552042
     * Application key:       ead60783729d9df1b84ed3eec87547bf
     * Application secret:    a48c963252a56949b052f09af72e967c
     *
     * Access token key:      <valid for the length of the Facebook session/>
     */
    public static final String ASE_FACEBOOK_APP_ID = "161355780552042";
    public static final String ASE_FACEBOOK_APP_KEY = "ead60783729d9df1b84ed3eec87547bf";
    public static final String ASE_FACEBOOK_APP_SECRET = "a48c963252a56949b052f09af72e967c";

    public static final String DEV_LCLHOST_FACEBOOK_APP_ID = "164780320208648";
    public static final String DEV_LCLHOST_FACEBOOK_APP_KEY = "cc9933d240f333508cee8490711ce4c4";
    public static final String DEV_LCLHOST_FACEBOOK_APP_SECRET = "2213d315b21fa7d74e0ede71f4567f19";

    public static final String DEV_VRTBOX_FACEBOOK_APP_ID = "166483420039607";
    public static final String DEV_VRTBOX_FACEBOOK_APP_KEY = "494d72d784a39564207a581437459a28";
    public static final String DEV_VRTBOX_FACEBOOK_APP_SECRET = "aa54e00527e5d60dcff95c32edd12d7b";

    public static final String FB_GRAPH_AUTH_URL = "https://graph.facebook.com/oauth/authorize";

    protected static boolean inLocalHost = false;
    protected static boolean inVirtualBox = false;

    public static String bootstrapAuthUrl(HttpServletRequest request) throws UnsupportedEncodingException {
        inLocalHost = "localhost".equals(request.getServerName()) || "127.0.0.1".equals(request.getServerName());
        inVirtualBox = "10.0.2.2".equals(request.getServerName());
        return FB_GRAPH_AUTH_URL +
            "?client_id=" + (inLocalHost ? DEV_LCLHOST_FACEBOOK_APP_ID : inVirtualBox ? DEV_VRTBOX_FACEBOOK_APP_ID : ASE_FACEBOOK_APP_ID) +
            "&scope=" + getTwetailerScope() +
            "&display=page" +
            "&redirect_uri=";
    }

    public static String getAppUrl(HttpServletRequest request) throws UnsupportedEncodingException {
        inLocalHost = "localhost".equals(request.getServerName()) || "127.0.0.1".equals(request.getServerName());
        inVirtualBox = "10.0.2.2".equals(request.getServerName());
        return inLocalHost ? FB_LCLHOST_APP_URL : inVirtualBox ? FB_VRTBOX_APP_URL : FB_ASE_APP_URL;
    }

    public static final String FB_BUS_PAGE_URL = "http://www.facebook.com/pages/AnotherSocialEconomy/156908804326834";
    public static final String FB_GROUP_URL = "http://www.facebook.com/group.php?gid=165456116817105";

    public static final String FB_ASE_APP_URL = "http://apps.facebook.com/anothersocialeconomy/";
    public static final String FB_LCLHOST_APP_URL = "http://apps.facebook.com/ase-localhost/";
    public static final String FB_VRTBOX_APP_URL = "http://apps.facebook.com/ase-virtualbox/";

    public static final String ASE_MAIN_APP_URL = "http://anothersocialeconomy.appspot.com/widget/facebook/";
    public static final String LCLHOST_MAIN_APP_URL = "http://localhost:9999/widget/facebook/";
    public static final String VRTBOX_MAIN_APP_URL = "http://10.0.2.2:9999/widget/facebook/";

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    public static final String ATTR_ACCESS_TOKEN = "access_token";
    public static final String ATTR_OAUTH_TOKEN = "oauth_token";
    public static final String ATTR_ALGORITHM = "algorithm";
    public static final String ATTR_USER_ID = "user_id";
    public static final String ATTR_ERROR_REASON = "error_reason";

    /* User info: JsonObject: {
     *  id: String: "620001321",
     *  first_name: String: "Dom",
     *  timezone: -4.0,
     *  hometown: JsonObject: {
     *    id: String: "116069268406981",
     *    name: String: "Montreal, Quebec, Canada"
     *  },
     *  verified: true,
     *  locale: String: "en_US",
     *  link: String: "http://www.facebook.com/dom.derrien",
     *  name: String: "Dom Derrien",
     *  last_name: String: "Derrien",
     *  gender: String: "male",
     *  updated_time: String: "2009-09-07T02:42:18+0000"
     * }
     */
    public static final String ATTR_UID = "id";
    public static final String ATTR_NAME = "name"; // == Consumer.NAME
    public static final String ATTR_EMAIL = "email"; // == Consumer.EMAIL
    public static final String ATTR_TZ = "timezone";
    public static final String ATTR_VERIFIED = "verified";
    public static final String ATTR_LOCALE = "locale";

    /**
     * Return a list of attributes (separated by a comma) to be
     * passed to the call of the Facebook Graph API '/me'
     *
     * @return Serialized list of field names
     */
    public static final String getTwetailerRequestedFields() {
        return ATTR_UID +
        "," + ATTR_NAME +
        "," + ATTR_EMAIL +
        "," + ATTR_LOCALE +
        "," + ATTR_TZ +
        "," + ATTR_VERIFIED +
        "";
    }

    /* Facebook supported scopes:
     * ads_management create_event create_note email export_stream friends_about_me friends_activities
     * friends_birthday friends_checkins friends_education_history friends_events friends_groups friends_hometown
     * friends_interests friends_likes friends_location friends_notes friends_online_presence friends_photo_video_tags
     * friends_photos friends_relationship_details friends_relationships friends_religion_politics friends_status
     * friends_videos friends_website friends_work_history manage_friendlists manage_pages offline_access photo_upload
     * physical_login publish_stream read_friendlists read_insights read_mailbox read_requests read_stream rsvp_event
     * share_item sms status_update user_about_me user_activities user_birthday user_checkins user_education_history
     * user_events user_groups user_hometown user_interests user_likes user_location user_notes user_online_presence
     * user_photo_video_tags user_photos user_relationship_details user_relationships user_religion_politics user_status
     * user_videos user_website user_work_history video_upload xmpp_login
     */
    public static final String SCOPE_ADS = "ads_management";
    public static final String SCOPE_CRT_EVT = "create_event";
    public static final String SCOPE_CRT_NOT = "create_note";
    public static final String SCOPE_EMAIL = "email";
    public static final String SCOPE_EXP_STR = "export_stream";
    public static final String SCOPE_FRDS_ABOUT = "friends_about_me";
    public static final String SCOPE_FRDS_ACTVT = "friends_activities";
    public static final String SCOPE_FRDS_BRTHD = "friends_birthday";
    public static final String SCOPE_FRDS_CHCKN = "friends_checkins";
    public static final String SCOPE_FRDS_EDUC = "friends_education_history";
    public static final String SCOPE_FRDS_EVT = "friends_events";
    public static final String SCOPE_FRDS_GRP = "friends_groups";
    public static final String SCOPE_FRDS_TOWN = "friends_hometown";
    public static final String SCOPE_FRDS_INTRST = "friends_interests";
    public static final String SCOPE_FRDS_LIKES = "friends_likes";
    public static final String SCOPE_FRDS_LOC = "friends_location";
    public static final String SCOPE_FRDS_NOT = "friends_notes";
    public static final String SCOPE_FRDS_PRES = "friends_online_presence";
    public static final String SCOPE_FRDS_VIDTAG = "friends_photo_video_tags";
    public static final String SCOPE_FRDS_PICT = "friends_photos";
    public static final String SCOPE_FRDS_RELDET = "friends_relationship_details";
    public static final String SCOPE_FRDS_REL = "friends_relationships";
    public static final String SCOPE_FRDS_RLGPLT = "friends_religion_politics";
    public static final String SCOPE_FRDS_STATUS = "friends_status";
    public static final String SCOPE_FRDS_VIDEO = "friends_videos";
    public static final String SCOPE_FRDS_WEBSITE = "friends_website";
    public static final String SCOPE_FRDS_WORK = "friends_work_history";
    public static final String SCOPE_MNG_FRDLST = "manage_friendlists";
    public static final String SCOPE_MNG_PAGE = "manage_pages";
    public static final String SCOPE_OFF_ACC = "offline_access";
    public static final String SCOPE_PICT_UP = "photo_upload";
    public static final String SCOPE_PHYS_LOG = "physical_login";
    public static final String SCOPE_PUB_STR = "publish_stream";
    public static final String SCOPE_RD_FRDLST = "read_friendlists";
    public static final String SCOPE_RD_INSGHT = "read_insights";
    public static final String SCOPE_RD_MAILBX = "read_mailbox";
    public static final String SCOPE_RD_REQ = "read_requests";
    public static final String SCOPE_RD_STR = "read_stream";
    public static final String SCOPE_RSVP = "rsvp_event";
    public static final String SCOPE_SHARE = "share_item";
    public static final String SCOPE_SMS = "sms";
    public static final String SCOPE_STATUS = "status_update";
    public static final String SCOPE_USR_ABOUT = "user_about_me";
    public static final String SCOPE_USR_ACTVT = "user_activities";
    public static final String SCOPE_USR_BRTHD = "user_birthday";
    public static final String SCOPE_USR_CHCKN = "user_checkins";
    public static final String SCOPE_USR_EDUC = "user_education_history";
    public static final String SCOPE_USR_EVT = "user_events";
    public static final String SCOPE_USR_GRP = "user_groups";
    public static final String SCOPE_USR_TOWN = "user_hometown";
    public static final String SCOPE_USR_INTRST = "user_interests";
    public static final String SCOPE_USR_LIKES = "user_likes";
    public static final String SCOPE_USR_LOC = "user_location";
    public static final String SCOPE_USR_NOT = "user_notes";
    public static final String SCOPE_USR_PRES = "user_online_presence";
    public static final String SCOPE_USR_VIDTAG = "user_photo_video_tags";
    public static final String SCOPE_USR_PICT = "user_photos";
    public static final String SCOPE_USR_RELDET = "user_relationship_details";
    public static final String SCOPE_USR_REL = "user_relationships";
    public static final String SCOPE_USR_RLGPLT = "user_religion_politics";
    public static final String SCOPE_USR_STATUS = "user_status";
    public static final String SCOPE_USR_VIDEO = "user_videos";
    public static final String SCOPE_USR_WEBSITE = "user_website";
    public static final String SCOPE_USER_WORK = "user_work_history";
    public static final String SCOPE_VIDEO_UP = "video_upload";
    public static final String SCOPE_XMPP_LOG = "xmpp_login";

    /**
     * Return a list of access rights (separated by a comma) to be
     * passed to the call of the Facebook Graph API '/authorize'
     *
     * @return Serialized list of access rights
     */
    public static final String getTwetailerScope() {
        return SCOPE_EMAIL +
        // "," + SCOPE_OFF_ACC +
        // "," + SCOPE_PHYS_LOG +
        // "," + SCOPE_PUB_STR +
        "," + SCOPE_RD_FRDLST +
        // "," + SCOPE_STATUS +
        // "," + SCOPE_USR_ABOUT +
        // "," + SCOPE_USR_BRTHD +
        // "," + SCOPE_USR_TOWN +
        // "," + SCOPE_USR_LOC +
        // "," + SCOPE_USR_PRES +
        // "," + SCOPE_USR_STATUS +
        "";
    }

    /**
     * Use the URLFetch service to get an access token for the given
     * code (code produced by an initial call to the Facebook Graph
     * API '/authorize')
     *
     * @param requestSource URL of the page initiating the call, used by
     *                      the Facebook Graph API to verify that URL called
     *                      to get this code is consistent.
     * @param code          Code generated by the Facebook Graph API '/authorize'
     * @return An access to be used whenever we want to act on behalf of the logged user
     *
     * @throws MalformedURLException If the URL to call the Facebook Graph API is mal-formed
     * @throws IOException If the data retrieval fails
     */
    public static JsonObject getAccessToken(String requestSource, String code) throws MalformedURLException, IOException {
        // Get the OAuth access token for this user
        code = URLEncoder.encode(code, StringUtils.JAVA_UTF8_CHARSET);
        String tokenURL = "https://graph.facebook.com/oauth/access_token" +
            "?client_id=" + (inLocalHost ? DEV_LCLHOST_FACEBOOK_APP_ID : inVirtualBox ? DEV_VRTBOX_FACEBOOK_APP_ID : ASE_FACEBOOK_APP_ID) +
            "&client_secret=" + (inLocalHost ? DEV_LCLHOST_FACEBOOK_APP_SECRET : inVirtualBox ? DEV_VRTBOX_FACEBOOK_APP_SECRET : ASE_FACEBOOK_APP_SECRET) +
            "&redirect_uri=" + URLEncoder.encode(requestSource, StringUtils.JAVA_UTF8_CHARSET) +
            "&code=" + code;
        log.warning("Calling Facebook to get an access token for the logged user -- url: " + tokenURL);
        HTTPResponse produced = getURLFetchService().fetch(getRequest(tokenURL));
        log.fine("Generated response: " + dumpResponse(produced));

        try {
            if (getContentType(produced).contains("text/plain")) {
                return new GenericJsonObject(processURLEncodedResponse(produced));
            }
            return processJsonResponse(produced);
        }
        catch (JsonException ex) {
            String dump = dumpResponse(produced);
            log.severe("The Facebook Json bag is malformed --ex: " + ex.getMessage() + " -- original: " + dump);
            throw new IOException("The Facebook Json bag with the logged user information is malformed! -- ex: " + ex.getMessage() + " -- original: " + dump);
        }
    }

    /**
     * Use the URLFetch service to get information about the
     * Facebook user (call to the Facebook Graph API '/me')
     *
     * @param accessToken OAuth token required to get the data
     * @return Json bag with user attribute
     *
     * @throws MalformedURLException If the URL to call the Facebook Graph API is mal-formed
     * @throws IOException If the data retrieval fails
     */
    public static JsonObject getUserInfo(String accessToken) throws MalformedURLException, IOException {
        String infoURL = "https://graph.facebook.com/me" +
            "?access_token=" + URLEncoder.encode(accessToken, StringUtils.JAVA_UTF8_CHARSET) +
            "&fields=" + getTwetailerRequestedFields();
        log.warning("Calling Facebook to the logged user info -- url: " + infoURL);
        HTTPResponse produced = getURLFetchService().fetch(getRequest(infoURL));
        log.fine("Generated response: " + dumpResponse(produced));

        try {
            if (getContentType(produced).contains("text/plain")) {
                return new GenericJsonObject(processURLEncodedResponse(produced));
            }
            return processJsonResponse(produced);
        }
        catch (JsonException ex) {
            String dump = dumpResponse(produced);
            log.severe("The Facebook Json bag is malformed --ex: " + ex.getMessage() + " -- original: " + dump);
            throw new IOException("The Facebook Json bag with the logged user information is malformed! -- ex: " + ex.getMessage() + " -- original: " + dump);
        }
    }

    private static URLFetchService urlFS;

    /**
     * Behavior injector for the unit tests
     *
     * @return Instance of the App Engine service
     */
    protected static URLFetchService getURLFetchService() {
        if (urlFS == null) {
            urlFS = URLFetchServiceFactory.getURLFetchService();
        }
        return urlFS;
    }

    /**
     * Prepare a request for the URL Fetch Service
     *
     * @param url Address to reach
     * @return Request instance ready to be fetched
     *
     * @throws MalformedURLException If the given URL is malformed
     */
    protected static HTTPRequest getRequest(String url) throws MalformedURLException {
        return new HTTPRequest(
                new URL(url),
                HTTPMethod.GET,
                FetchOptions.Builder.disallowTruncate().followRedirects()
        );
    }

    /**
     * Extract the response content-type from the response produced by the URL Fetch Service
     *
     * @param response Result of a URL fetch() call
     * @return Value of the header field "Content-Type" or empty string
     */
    protected static String getContentType(HTTPResponse response) {
        for (HTTPHeader header: response.getHeaders()) {
            if ("content-type".equals(header.getName().toLowerCase())) {
                return header.getValue();
            }
        }
        return "";
    }

    /**
     * Extract the URL encoded parameters from the response buffer.
     * Note that only one value is returned per parameter name!
     *
     * @param response Result of a URL fetch() call
     * @return Json bag with one entry per parameter in the URL encoded string
     */
    protected static Map<String, Object> processURLEncodedResponse(HTTPResponse response) {
        String content = new String(response.getContent());
        Map<String, Object> parameters = new HashMap<String, Object>();
        String[] pairs = content.split("\\&");
        int idx = pairs.length;
        while (0 < idx) {
            --idx;
            String[] parts = pairs[idx].split("\\=");
            parameters.put(parts[0], parts.length == 2 ? parts[1] : null);
        }
        return parameters;
    }

    /**
     * Extract the Json bag from the response buffer
     *
     * @param response Result of a URL fetch() call
     * @return Json bag produced from the serialized version read in the response buffer
     *
     * @throws JsonException If the serialized buffer is mal-formed
     */
    protected static JsonObject processJsonResponse(HTTPResponse response) throws JsonException {
        return new JsonParser(StringUtils.convertUnicodeChars(new String(response.getContent()))).getJsonObject();
    }

    /**
     * Serialize the response header fields and its content
     *
     * @param response Result of a URL fetch() call
     * @return String for logging purposes
     */
    protected static String dumpResponse(HTTPResponse response) {
        StringBuilder out = new StringBuilder();
        for (HTTPHeader header: response.getHeaders()) {
            out.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        out.append("\n").append(new String(response.getContent()));
        return out.toString();
    }

    protected static final String ENCRYPTION_ALGORITHM_FACEBOOK_NAME = "HMAC-SHA256";
    protected static final String ENCRYPTION_ALGORITHM_STANDARD_NAME = "HMACSHA256";

    /**
     * Study the passed 'signed_request' parameter, verify it's not been tampered, and
     * extract the passed logged in user identifiers.
     *
     * @param request HTTP request instance containing the request information
     * @return Json bag with the logged in user identifiers
     *
     * @throws ServletException If the content of the 'signed_request' parameter is corrupted
     *
     * @see http://developers.facebook.com/docs/authentication/canvas for the process details
     * @see http://www.hammersoft.de/blog/?p=87 for the original implementation
     */
    public static JsonObject processSignedRequest(HttpServletRequest request) throws ServletException {
        try {
            String signedRequest = request.getParameter("signed_request");
            String[] signedRequestParts = signedRequest.split("\\.");
            String signature = signedRequestParts[0];

            String encodedPayload = signedRequestParts[1];
            String rawPayload = new String(new Base64(true).decode(encodedPayload.getBytes()));
            JsonObject payload = new JsonParser(rawPayload).getJsonObject();
            log.fine("Extracted payload: " + payload.toString());

            if (!ENCRYPTION_ALGORITHM_FACEBOOK_NAME.equals(payload.getString(ATTR_ALGORITHM))) {
                throw new ServletException("Unexpected encryption algorithm: " + payload.getString(ATTR_ALGORITHM));
            }

            try {
                inLocalHost = "localhost".equals(request.getServerName()) || "127.0.0.1".equals(request.getServerName());
                inVirtualBox = "10.0.2.2".equals(request.getServerName());
                SecretKeySpec secretKeySpec = new SecretKeySpec((inLocalHost ? DEV_LCLHOST_FACEBOOK_APP_SECRET : inVirtualBox ? DEV_VRTBOX_FACEBOOK_APP_SECRET : ASE_FACEBOOK_APP_SECRET).getBytes(), ENCRYPTION_ALGORITHM_STANDARD_NAME);
                Mac messageAuthenticationCode = Mac.getInstance(ENCRYPTION_ALGORITHM_STANDARD_NAME);
                messageAuthenticationCode.init(secretKeySpec);
                if (!Arrays.equals(new Base64(true).decode(signature.getBytes()), messageAuthenticationCode.doFinal(encodedPayload.getBytes()))) {
                    throw new ServletException("Non-matching signature for request");
                }
            }
            catch (NoSuchAlgorithmException ex) {
                throw new ServletException("Unknown hash algorithm " + ENCRYPTION_ALGORITHM_STANDARD_NAME, ex);
            }
            catch (InvalidKeyException ex) {
                throw new ServletException("Wrong key for " + ENCRYPTION_ALGORITHM_STANDARD_NAME, ex);
            }

            return payload;
        }
        catch (JsonException ex) {
            throw new ServletException("Invalid JsonObject in the signed request payload", ex);
        }
    }
}
