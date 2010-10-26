package twetailer.connector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
 * Definition of the methods specific to communication via Twitter
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

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    public static final String ATTR_ACCESS_TOKEN = "access_token";
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
    public static final String ATTR_NAME = "name";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_TZ = "timezone";
    public static final String ATTR_VERIFIED = "verified";
    public static final String ATTR_LOCALE = "locale";

    public static final String getTwetailerRequestedFields() {
        return ATTR_UID +
        "," + ATTR_NAME +
        "," + ATTR_EMAIL +
        "," + ATTR_LOCALE +
        "," + ATTR_TZ +
        "," + ATTR_VERIFIED +
        "";
    }

    /* Supported scopes:
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

    public static JsonObject getAccessToken(String requestSource, String code) throws MalformedURLException, IOException {
        // Get the OAuth access token for this user
        code = URLEncoder.encode(code, "UTF-8");
        String tokenURL = "https://graph.facebook.com/oauth/access_token" +
            "?client_id=" + FacebookConnector.ASE_FACEBOOK_APP_ID +
            "&client_secret=" + FacebookConnector.ASE_FACEBOOK_APP_SECRET +
            "&redirect_uri=" + URLEncoder.encode(requestSource, "UTF-8") +
            "&code=" + code;
        log.warning("Calling Facebook to get an access token for the logged user -- url: " + tokenURL);
        HTTPResponse produced = getURLFetchService().fetch(getRequest(tokenURL));
        // log.warning("Generated response: " + dumpResponse(produced));

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

    public static JsonObject getUserInfo(String accessToken) throws MalformedURLException, IOException {
        String infoURL = "https://graph.facebook.com/me" +
            "?access_token=" + URLEncoder.encode(accessToken, "UTF-8") +
            "&fields=" + getTwetailerRequestedFields();
        log.warning("Calling Facebook to the logged user info -- url: " + infoURL);
        HTTPResponse produced = getURLFetchService().fetch(getRequest(infoURL));
        // log.warning("Generated response: " + dumpResponse(produced));

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

    protected static URLFetchService getURLFetchService() {
        if (urlFS == null) {
            urlFS = URLFetchServiceFactory.getURLFetchService();
        }
        return urlFS;
    }

    protected static HTTPRequest getRequest(String url) throws MalformedURLException {
        return new HTTPRequest(
                new URL(url),
                HTTPMethod.GET,
                FetchOptions.Builder.disallowTruncate().followRedirects()
        );
    }

    protected static String getContentType(HTTPResponse response) {
        for (HTTPHeader header: response.getHeaders()) {
            if ("Content-Type".equals(header.getName())) {
                return header.getValue();
            }
        }
        return "";
    }

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

    protected static JsonObject processJsonResponse(HTTPResponse response) throws JsonException {
        return new JsonParser(StringUtils.convertUnicodeChars(new String(response.getContent()))).getJsonObject();
    }

    protected static String dumpResponse(HTTPResponse response) {
        StringBuilder out = new StringBuilder();
        for (HTTPHeader header: response.getHeaders()) {
            out.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        out.append("\n").append(new String(response.getContent()));
        return out.toString();
    }
}
