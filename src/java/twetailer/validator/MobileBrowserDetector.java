package twetailer.validator;

import javax.servlet.http.HttpServletRequest;

public class MobileBrowserDetector {

    String userAgent;
    private final static String EMPTY_STRING = "";

    public MobileBrowserDetector(HttpServletRequest request) {
        userAgent = request.getParameter("customUserAgent");
        if (userAgent == null) {
            userAgent = request.getHeader("HTTP_USER_AGENT");
        }
        if (userAgent == null) {
            userAgent = EMPTY_STRING;
        }
    }

    public boolean isIPad() { return -1 < userAgent.indexOf("iPad"); }
    public boolean isIPod() { return -1 < userAgent.indexOf("iPod"); }
    public boolean isIPhone() { return -1 < userAgent.indexOf("iPhone"); }
    public boolean isAndroid() { return -1 < userAgent.indexOf("android"); }

    public boolean isMobileBrowser() { return isAndroid() || isIPhone() || isIPad() || isIPod(); }
}
