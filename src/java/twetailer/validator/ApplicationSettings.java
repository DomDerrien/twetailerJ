package twetailer.validator;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Read the application settings from a local properties file.
 *
 * @author Dom Derrien
 */
public class ApplicationSettings {

    private static Logger log = Logger.getLogger(ApplicationSettings.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    private boolean useCDN = false;

    protected final static String USE_CDN_KEY = "useCDN";

    protected final static String DEFAULT_BASE_URL = "";

    private String cdnBaseURL = DEFAULT_BASE_URL;

    protected final static String CDN_BASE_URL_KEY = "cdnBaseURL";

    protected final static String DEFAULT_LOGIN_PAGE_URL = "/_includes/login.jsp";

    public String loginPageURL = DEFAULT_LOGIN_PAGE_URL;

    protected final static String LOGIN_PAGE_URL_KEY = "loginPageURL";

    public final static String DEFAULT_MAIN_PAGE_URL = "/console/";

    private String mainPageURL = DEFAULT_MAIN_PAGE_URL;

    protected final static String MAIN_PAGE_URL_KEY = "mainPageURL";

    public final static String DEFAULT_LOGO_URL = "/images/logo/logo-48x48.png";

    private String logoURL = DEFAULT_LOGO_URL;

    protected final static String LOGO_URL_KEY = "logoURL";

    public final static String DEFAULT_PRODUCT_EMAIL_DOMAIN = "anothersocialeconomy.appspotmail.com";

    private String productEmailDomain = DEFAULT_PRODUCT_EMAIL_DOMAIN;

    protected final static String PRODUCT_EMAIL_DOMAIN_KEY = "productEmailDomain";

    public final static String DEFAULT_PRODUCT_NAME = "";

    private String productName = DEFAULT_PRODUCT_NAME;

    protected final static String PRODUCT_NAME_KEY = "productName";

    public final static String DEFAULT_PRODUCT_VERSION = "0";

    private String productVersion = DEFAULT_PRODUCT_VERSION;

    protected final static String PRODUCT_VERSION_KEY = "productVersion";

    public final static String DEFAULT_PRODUCT_WEBSITE = "http://anothersocialeconomy.com/";

    private String productWebsite = DEFAULT_PRODUCT_WEBSITE;

    protected final static String PRODUCT_WEBSITE_KEY = "productWebsite";

    public final static String DEFAULT_APPLICATION_WEBSITE = "https://anothersocialeconomy.appspot.com/";

    private String applicationWebsite = DEFAULT_APPLICATION_WEBSITE;

    protected final static String APPLICATION_WEBSITE_KEY = "applicationWebsite";

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
     * Get the product email domain
     *
     * @return Name
     */
    public String getProductEmailDomain() {
        return productEmailDomain;
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
     * Get the product version
     *
     * @return Version
     */
    public String getProductVersion() {
        return productVersion;
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
     * Get the application website URL
     *
     * @return URL
     */
    public String getApplicationWebsite() {
        return applicationWebsite;
    }

    /**
     * Singleton accessor
     * @return Container for the application settings
     */
    public static ApplicationSettings get() {
        if (settings == null) {
            // Facing a race condition here is not an issue
            settings = new ApplicationSettings();
        }
        return settings;
    }

    protected static ApplicationSettings settings;

    protected static void reset() {
        settings = null;
    }

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
                productEmailDomain = appSettings.getString(PRODUCT_EMAIL_DOMAIN_KEY);
            }
            catch(Exception ex) {
                productEmailDomain = DEFAULT_PRODUCT_EMAIL_DOMAIN;
            }
            try {
                productName = appSettings.getString(PRODUCT_NAME_KEY);
            }
            catch(Exception ex) {
                productName = DEFAULT_PRODUCT_NAME;
            }
            try {
                productVersion = appSettings.getString(PRODUCT_VERSION_KEY);
            }
            catch(Exception ex) {
                productVersion = DEFAULT_PRODUCT_VERSION;
            }
            try {
                productWebsite = appSettings.getString(PRODUCT_WEBSITE_KEY);
            }
            catch(Exception ex) {
                productWebsite = DEFAULT_PRODUCT_WEBSITE;
            }
            try {
                applicationWebsite = appSettings.getString(APPLICATION_WEBSITE_KEY);
            }
            catch(Exception ex) {
                applicationWebsite = DEFAULT_APPLICATION_WEBSITE;
            }
        }
        catch(Exception ex) {
            getLogger().severe("Application settings cannot be loaded. Relying on default ones");
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
