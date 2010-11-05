package twetailer.j2ee;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import domderrien.i18n.StringUtils;

/**
 * Utility class with shared methods for the servlet entry points
 *
 * @author Dom Derrien
 */
public class ServletUtils {

    /**
     * Fixed pattern for the regular expression extracting the resource keys from the URIs
     */
    public static final Pattern uriKeyPattern = Pattern.compile("/(\\w+)");

    /**
     * Set default parameters for the HTTP request/response objects
     *
     * @param request Container of the HTTP request parameters
     * @param response Container for the request output stream
     */
    public static void configureHttpParameters(HttpServletRequest request, HttpServletResponse response) {
        // Set httpRequest encoding
        try {
            request.setCharacterEncoding(StringUtils.HTML_UTF8_CHARSET);
        }
        catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ServletUtils.class.getName()).warning("Cannot set the encoding of the request! -- message " + ex.getMessage());
            // Ignore the exception
        }

        // Set httpResponse format
        response.setCharacterEncoding(StringUtils.HTML_UTF8_CHARSET);
        response.setContentType("text/javascript; charset=" + StringUtils.HTML_UTF8_CHARSET);

        // FIXME: introduce a strategy that is going to detect future expiration dates
        response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
        response.setHeader("Pragma","no-cache"); // HTTP 1.0
        response.setDateHeader ("Expires", 0); // prevents caching at the proxy server
    }
}
