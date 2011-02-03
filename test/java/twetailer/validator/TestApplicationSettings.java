package twetailer.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javamocks.util.logging.MockLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestApplicationSettings {

    @BeforeClass
    public static void setUpBeforeClass() {
        ApplicationSettings.setMockLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() throws Exception {
        ApplicationSettings.setResourceBundle(null);
        ApplicationSettings.reset();
    }

    @Test
    public void testConstructor() {
        assertNotNull(ApplicationSettings.get());
    }

    @Test
    public void testUseCDN() {
        assertTrue(ApplicationSettings.get().isUseCDN());
    }

    @Test
    public void testGetCdnBaseURL() {
        if (ApplicationSettings.get().isUseCDN()) {
            assertNotNull(ApplicationSettings.get().getCdnBaseURL());
            assertNotSame(0, ApplicationSettings.get().getCdnBaseURL().length());
        }
        else {
            assertEquals(ApplicationSettings.DEFAULT_BASE_URL, ApplicationSettings.get().getCdnBaseURL());
        }
    }

    @Test
    public void testGetLoginPageURL() {
        assertNotNull(ApplicationSettings.get().getLoginPageURL());
    }

    @Test
    public void testGetMainPageURL() {
        assertNotNull(ApplicationSettings.get().getMainPageURL());
    }

    final String cdnURL = "http://";
    final String loginURL = "login";
    final String mainURL = "main";
    final String logoURL = "http://";
    final String productName = "name";
    final String productWebsite = "http://";
    final String servletApiPath = "/api/path";

    class MockResourceBundle extends ListResourceBundle {
        private Object[][] contents = new Object[][]{
            {ApplicationSettings.USE_CDN_KEY, Boolean.TRUE.toString()},
            {ApplicationSettings.CDN_BASE_URL_KEY, cdnURL},
            {ApplicationSettings.LOGIN_PAGE_URL_KEY, loginURL},
            {ApplicationSettings.MAIN_PAGE_URL_KEY, mainURL},
            {ApplicationSettings.LOGO_URL_KEY, logoURL},
            {ApplicationSettings.PRODUCT_NAME_KEY, productName},
            {ApplicationSettings.PRODUCT_WEBSITE_KEY, productWebsite},
        };
        protected Object[][] getContents() {
            return contents;
        }
        protected void setContent(int index, Object[] value) {
            contents[index] = value;
        }
    }

    @Test
    @SuppressWarnings("static-access")
    public void testUnableToLoadRB() {
        ApplicationSettings settings = new ApplicationSettings() {
            @Override
            protected ResourceBundle getResourceBundle() throws MissingResourceException {
                throw new MissingResourceException("Done in purpose", "one", "two");
            }
        };
        ApplicationSettings.setResourceBundle(new MockResourceBundle());

        assertFalse(settings.get().isUseCDN());
    }

    @Test
    public void testVerifyModifiedValues() {
        ApplicationSettings.setResourceBundle(new MockResourceBundle());

        assertTrue(ApplicationSettings.get().isUseCDN());
        assertEquals(cdnURL, ApplicationSettings.get().getCdnBaseURL());
        assertEquals(loginURL, ApplicationSettings.get().getLoginPageURL());
        assertEquals(mainURL, ApplicationSettings.get().getMainPageURL());
        assertEquals(logoURL, ApplicationSettings.get().getLogoURL());
        assertEquals(productName, ApplicationSettings.get().getProductName());
        assertEquals(productWebsite, ApplicationSettings.get().getProductWebsite());
    }

    @Test
    public void testUseCDNNotSet() {
        MockResourceBundle mRB = new MockResourceBundle();
        mRB.contents[0] = new Object[] { ApplicationSettings.USE_CDN_KEY, 12345L };
        ApplicationSettings.setResourceBundle(mRB);

        assertFalse(ApplicationSettings.get().isUseCDN());
        assertEquals(cdnURL, ApplicationSettings.get().getCdnBaseURL());
        assertEquals(loginURL, ApplicationSettings.get().getLoginPageURL());
        assertEquals(mainURL, ApplicationSettings.get().getMainPageURL());
        assertEquals(logoURL, ApplicationSettings.get().getLogoURL());
        assertEquals(productName, ApplicationSettings.get().getProductName());
        assertEquals(productWebsite, ApplicationSettings.get().getProductWebsite());
    }

    @Test
    public void testCdnURLNotSet() {
        MockResourceBundle mRB = new MockResourceBundle();
        mRB.contents[1] = new Object[] { ApplicationSettings.CDN_BASE_URL_KEY, 12345L };
        ApplicationSettings.setResourceBundle(mRB);

        assertTrue(ApplicationSettings.get().isUseCDN());
        assertEquals(ApplicationSettings.DEFAULT_BASE_URL, ApplicationSettings.get().getCdnBaseURL());
        assertEquals(loginURL, ApplicationSettings.get().getLoginPageURL());
        assertEquals(mainURL, ApplicationSettings.get().getMainPageURL());
        assertEquals(logoURL, ApplicationSettings.get().getLogoURL());
        assertEquals(productName, ApplicationSettings.get().getProductName());
        assertEquals(productWebsite, ApplicationSettings.get().getProductWebsite());
    }

    @Test
    public void testLoginPageURLNotSet() {
        MockResourceBundle mRB = new MockResourceBundle();
        mRB.contents[2] = new Object[] { ApplicationSettings.LOGIN_PAGE_URL_KEY, 12345L };
        ApplicationSettings.setResourceBundle(mRB);

        assertTrue(ApplicationSettings.get().isUseCDN());
        assertEquals(cdnURL, ApplicationSettings.get().getCdnBaseURL());
        assertEquals(ApplicationSettings.DEFAULT_LOGIN_PAGE_URL, ApplicationSettings.get().getLoginPageURL());
        assertEquals(mainURL, ApplicationSettings.get().getMainPageURL());
        assertEquals(logoURL, ApplicationSettings.get().getLogoURL());
        assertEquals(productName, ApplicationSettings.get().getProductName());
        assertEquals(productWebsite, ApplicationSettings.get().getProductWebsite());
    }

    @Test
    public void testMainPageURLNotSet() {
        MockResourceBundle mRB = new MockResourceBundle();
        mRB.contents[3] = new Object[] { ApplicationSettings.MAIN_PAGE_URL_KEY, 12345L };
        ApplicationSettings.setResourceBundle(mRB);

        assertTrue(ApplicationSettings.get().isUseCDN());
        assertEquals(cdnURL, ApplicationSettings.get().getCdnBaseURL());
        assertEquals(loginURL, ApplicationSettings.get().getLoginPageURL());
        assertEquals(ApplicationSettings.DEFAULT_MAIN_PAGE_URL, ApplicationSettings.get().getMainPageURL());
        assertEquals(logoURL, ApplicationSettings.get().getLogoURL());
        assertEquals(productName, ApplicationSettings.get().getProductName());
        assertEquals(productWebsite, ApplicationSettings.get().getProductWebsite());
    }

    @Test
    public void testLogoURLNotSet() {
        MockResourceBundle mRB = new MockResourceBundle();
        mRB.contents[4] = new Object[] { ApplicationSettings.LOGO_URL_KEY, 12345L };
        ApplicationSettings.setResourceBundle(mRB);

        assertTrue(ApplicationSettings.get().isUseCDN());
        assertEquals(cdnURL, ApplicationSettings.get().getCdnBaseURL());
        assertEquals(loginURL, ApplicationSettings.get().getLoginPageURL());
        assertEquals(mainURL, ApplicationSettings.get().getMainPageURL());
        assertEquals(ApplicationSettings.DEFAULT_LOGO_URL, ApplicationSettings.get().getLogoURL());
        assertEquals(productName, ApplicationSettings.get().getProductName());
        assertEquals(productWebsite, ApplicationSettings.get().getProductWebsite());
    }

    @Test
    public void testProductNameNotSet() {
        MockResourceBundle mRB = new MockResourceBundle();
        mRB.contents[5] = new Object[] { ApplicationSettings.PRODUCT_NAME_KEY, 12345L };
        ApplicationSettings.setResourceBundle(mRB);

        assertTrue(ApplicationSettings.get().isUseCDN());
        assertEquals(cdnURL, ApplicationSettings.get().getCdnBaseURL());
        assertEquals(loginURL, ApplicationSettings.get().getLoginPageURL());
        assertEquals(mainURL, ApplicationSettings.get().getMainPageURL());
        assertEquals(logoURL, ApplicationSettings.get().getLogoURL());
        assertEquals(ApplicationSettings.DEFAULT_PRODUCT_NAME, ApplicationSettings.get().getProductName());
        assertEquals(productWebsite, ApplicationSettings.get().getProductWebsite());
    }

    @Test
    public void testProductWebsiteNotSet() {
        MockResourceBundle mRB = new MockResourceBundle();
        mRB.contents[6] = new Object[] { ApplicationSettings.PRODUCT_WEBSITE_KEY, 12345L };
        ApplicationSettings.setResourceBundle(mRB);

        assertTrue(ApplicationSettings.get().isUseCDN());
        assertEquals(cdnURL, ApplicationSettings.get().getCdnBaseURL());
        assertEquals(loginURL, ApplicationSettings.get().getLoginPageURL());
        assertEquals(mainURL, ApplicationSettings.get().getMainPageURL());
        assertEquals(logoURL, ApplicationSettings.get().getLogoURL());
        assertEquals(productName, ApplicationSettings.get().getProductName());
        assertEquals(ApplicationSettings.DEFAULT_PRODUCT_WEBSITE, ApplicationSettings.get().getProductWebsite());
    }
}
