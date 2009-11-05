package twetailer.validator;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ApplicationSettings {

    private boolean useCDN = false;

    protected final static String USE_CDN_KEY = "useCDN";

    protected final static String DEFAULT_BASE_URL = "";

    private String cdnBaseURL = DEFAULT_BASE_URL;

    protected final static String CDN_BASE_URL_KEY = "cdnBaseURL";

    protected final static String DEFAULT_LOGIN_PAGE_URL = "/html/login.jsp";

    public String loginPageURL = DEFAULT_LOGIN_PAGE_URL;

    protected final static String LOGIN_PAGE_URL_KEY = "loginPageURL";

    public final static String DEFAULT_MAIN_PAGE_URL = "/html/console.jsp";

    private String mainPageURL = DEFAULT_MAIN_PAGE_URL;

    protected final static String MAIN_PAGE_URL_KEY = "mainPageURL";

    public final static String DEFAULT_LOGO_URL = "http://twetailer.appspot.com/images/logo/logo-48x48.png";

    private String logoURL = DEFAULT_LOGO_URL;

    protected final static String LOGO_URL_KEY = "logoURL";

    public final static String DEFAULT_PRODUCT_NAME = "";

    private String productName = DEFAULT_PRODUCT_NAME;

    protected final static String PRODUCT_NAME_KEY = "productName";

    public final static String DEFAULT_PRODUCT_WEBSITE = "";

    private String productWebsite = DEFAULT_PRODUCT_WEBSITE;

    protected final static String PRODUCT_WEBSITE_KEY = "productWebsite";

    /**
     * Boolean used in the JSP file to decide if the Dojo library
     * (JavaScript, CSS, images) should be used from a distributed Content
     * Delivery Network
     *
     * @return Info about the usage of a CDN
     */
    public boolean isUseCDN() {
        return useCDN;
    }

    /**
     * Get the URL for the Dojo library hosted on a public Content Delivery Network.
     *
     * Can be an address like: <code>http://ajax.googleapis.com/ajax/libs/dojo/1.3</code>
     *
     * @return Fully qualified URL
     */
    public String getCdnBaseURL() {
        return cdnBaseURL;
    }

    /**
     * Get the relative login page URL
     *
     * @return Relative URL
     */
    public String getLoginPageURL() {
        return loginPageURL;
    }

    /**
     * Get the relative main page URL
     *
     * @return Relative URL
     */

    public String getMainPageURL() {
        return mainPageURL;
    }


    /**
     * Get the URL for the product logo
     *
     * @return Relative or fully qualified URL
     */
    public String getLogoURL() {
        return logoURL;
    }

    /**
     * Get the product name
     *
     * @return Name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Get the product website URL
     *
     * @return URL
     */
    public String getProductWebsite() {
        return productWebsite;
    }

    /**
     * Singleton accessor
     * @return Container for the application settings
     */
    public static ApplicationSettings get() {
        if (settings == null) {
            settings = new ApplicationSettings();
        }
        return settings;
    }

    private static ApplicationSettings settings;

    protected static void reset() {
        settings = null;
    }

    private static final Logger log = Logger.getLogger(ApplicationSettings.class.getName());

    protected ApplicationSettings() {
        settings = this;
        try {
            ResourceBundle appSettings = getResourceBundle();
            try {
                useCDN = "true".equalsIgnoreCase(appSettings.getString(USE_CDN_KEY));
            }
            catch(Exception ex) {
                cdnBaseURL = DEFAULT_BASE_URL;
            }
            try {
                cdnBaseURL = appSettings.getString(CDN_BASE_URL_KEY);
            }
            catch(Exception ex) {
                cdnBaseURL = DEFAULT_BASE_URL;
            }
            try {
                loginPageURL = appSettings.getString(LOGIN_PAGE_URL_KEY);
            }
            catch(Exception ex) {
                loginPageURL = DEFAULT_LOGIN_PAGE_URL;
            }
            try {
                mainPageURL = appSettings.getString(MAIN_PAGE_URL_KEY);
            }
            catch(Exception ex) {
                mainPageURL = DEFAULT_MAIN_PAGE_URL;
            }
            try {
                logoURL = appSettings.getString(LOGO_URL_KEY);
            }
            catch(Exception ex) {
                logoURL = DEFAULT_LOGO_URL;
            }
            try {
                productName = appSettings.getString(PRODUCT_NAME_KEY);
            }
            catch(Exception ex) {
                productName = DEFAULT_PRODUCT_NAME;
            }
            try {
                productWebsite = appSettings.getString(PRODUCT_WEBSITE_KEY);
            }
            catch(Exception ex) {
                productWebsite = DEFAULT_PRODUCT_WEBSITE;
            }
        }
        catch(Exception ex) {
            log.warning("Application settings cannot be loaded. Relying on default ones");
        }
    }

    private static ResourceBundle mockResourceBundle;

    protected static void setResourceBundle(ResourceBundle rb) {
        mockResourceBundle = rb;
    }

    protected ResourceBundle getResourceBundle() throws MissingResourceException {
        if (mockResourceBundle != null) {
            return mockResourceBundle;
        }
        return ResourceBundle.getBundle("applicationSettings", Locale.getDefault());
    }
}
