package twetailer.validator;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ApplicationSettings {
    
    /**
     * Boolean used in the JSP file to decide if the 3rd party libraries
     * (JavaScript, CSS, images) should be used from distributed Content
     * Delivery Networks
     */
    boolean useCDN = false;
    
    /**
     * URL of such a Content Delivery Network
     */
    String cdnBaseURL = null;

    /**
     * Application main page URL
     */
    String mainPageURL = "/html/console.jsp";
    
    /**
     * Login page URL
     */
    String loginPageURL = "/html/login.jsp";
    
    public boolean isUseCDN() {
        return useCDN;
    }

    public String getCdnBaseURL() {
        return cdnBaseURL;
    }

    public String getMainPageURL() {
        return mainPageURL;
    }

    public String getLoginPageURL() {
        return loginPageURL;
    }

    /**
     * Singleton accessor
     * @return Container for the application settings
     */
    public static ApplicationSettings get() {
        return settings;
    }

    private static ApplicationSettings settings = new ApplicationSettings();
    
    private static final Logger log = Logger.getLogger(ApplicationSettings.class.getName());

    private ApplicationSettings() {
        try {
            ResourceBundle appSettings = ResourceBundle.getBundle("applicationSettings", Locale.getDefault());
            useCDN = "y".equals(appSettings.getString("useCDN"));
            cdnBaseURL = appSettings.getString("cdnBaseURL");
            String url = appSettings.getString("mainPageURL");
            if (url != null && 0 < url.length()) {
                mainPageURL = url;
            }
            url = appSettings.getString("loginPageURL");
            if (url != null && 0 < url.length()) {
                loginPageURL = url;
            }
        }
        catch(Exception ex) {
            log.warning("Application settings cannot be loaded. Relying on default ones");
        }
    }
}