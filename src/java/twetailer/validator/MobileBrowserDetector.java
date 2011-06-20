package twetailer.validator;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

public class MobileBrowserDetector {

    String userAgent;
    private final static String EMPTY_STRING = "";
    private final static String IPAD = "ipad";
    private final static String IPOD = "ipod";
    private final static String IPHONE = "iphone";
    private final static String ANDROID = "android";

    public MobileBrowserDetector(HttpServletRequest request) {
        userAgent = request.getParameter("customUserAgent");
        if (userAgent == null) {
            userAgent = request.getHeader("User-Agent");
        }
        if (userAgent == null) {
            userAgent = request.getHeader("user-agent");
        }
        if (userAgent == null) {
            userAgent = EMPTY_STRING;
        }
        userAgent = userAgent.toLowerCase(Locale.ENGLISH);
    }

    public boolean isIPad() { return -1 < userAgent.indexOf(IPAD); }
    public boolean isIPod() { return -1 < userAgent.indexOf(IPOD); }
    public boolean isIPhone() { return -1 < userAgent.indexOf(IPHONE); }
    public boolean isAndroid() { return -1 < userAgent.indexOf(ANDROID); }

    public boolean isMobileBrowser() { return isAndroid() || isIPhone() || isIPad() || isIPod(); }
}
