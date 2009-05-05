package org.domderrien.i18n;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestLabelExtractor {

	@Before
	public void setUp() throws Exception {
		LabelExtractor.resetResourceBundleList();
	}

	@After
	public void tearDown() throws Exception {
	}
    
    /************* Mock classes & local objects ***********************/
    class MockResourceBundle extends ListResourceBundle {
        public final static String LABEL_0 = "0";   //$NON-NLS-1$
        public final static String LABEL_1 = "1";   //$NON-NLS-1$ 
        public final static String LABEL_10 = "10"; //$NON-NLS-1$
        public final static String LABEL_11 = "11"; //$NON-NLS-1$
        public final static String LABEL_12 = "12"; //$NON-NLS-1$
        public final static String ERR_LABEL_0 = LabelExtractor.ERROR_MESSAGE_PREFIX + "0"; //$NON-NLS-1$
        public final static String ERR_LABEL_1 = LabelExtractor.ERROR_MESSAGE_PREFIX + "1"; //$NON-NLS-1$
        private Object[][] contents = new Object[][]{
            {LABEL_0, LABEL_0},
            {LABEL_1, LABEL_1},
            {LABEL_10, LABEL_0},
            {LABEL_11, LABEL_1},
            {ERR_LABEL_0, LABEL_0},
            {ERR_LABEL_1, LABEL_1}
        };
        protected Object[][] getContents() {
            return contents;
        }         
    }

    /************* Test Functions ***********************/
    @Test
    public void testConstructor() {
    	new LabelExtractor();
    }
    @Test
    public void testSetResourceBundleI() {
    	// Set a resource bundle with a null locale reference
        ResourceBundle mock = new MockResourceBundle();
        LabelExtractor.setResourceBundle(mock, null);
        assertEquals(mock, LabelExtractor.getResourceBundle(null));
    }

    @Test
    public void testSetResourceBundleII() {
    	// Set a resource bundle with a non null locale reference
        ResourceBundle mock = new MockResourceBundle();
        LabelExtractor.setResourceBundle(mock, Locale.US);
        assertEquals(mock, LabelExtractor.getResourceBundle(Locale.US));
    }

    @Test
    public void testSetResourceBundleIII() {
    	// Set a resource bundle with a composite locale reference
        ResourceBundle mock = new MockResourceBundle();
        LabelExtractor.setResourceBundle(mock, Locale.CANADA_FRENCH);
        assertEquals(mock, LabelExtractor.getResourceBundle(Locale.CANADA_FRENCH));
    }

    @Test
    public void testGetResourceBundleI() {
    	// Expected to read a English file for the locale en_US => fallback on en
        ResourceBundle rb = LabelExtractor.getResourceBundle(Locale.US);
        assertEquals("English", rb.getString("bundle_language"));
    }

    @Test
    public void testGetResourceBundleII() {
    	// Expected to read a English file for the locale fr_CA => fallback on fr
        ResourceBundle rb = LabelExtractor.getResourceBundle(Locale.CANADA_FRENCH);
        assertEquals("Fran\u00e7ais", rb.getString("bundle_language"));
    }

    @Test
    public void testGetResourceBundleIII() {
    	// Expected to read a English file for the locale cn => fallback on root
        ResourceBundle rb = LabelExtractor.getResourceBundle(Locale.TRADITIONAL_CHINESE);
        assertEquals("English", rb.getString("bundle_language"));
    }

    @Test
    public void testGetI() {
        MockResourceBundle mock = new MockResourceBundle();
        LabelExtractor.setResourceBundle(mock, Locale.US);
        // The label asked has an entry in the dictionary
        assertEquals(mock.getString(MockResourceBundle.LABEL_0), LabelExtractor.get(MockResourceBundle.LABEL_0, Locale.US));
        assertEquals(mock.getString(MockResourceBundle.LABEL_1), LabelExtractor.get(MockResourceBundle.LABEL_1, Locale.US));
        assertEquals(mock.getString(MockResourceBundle.LABEL_10), LabelExtractor.get(MockResourceBundle.LABEL_0, Locale.US));
        assertEquals(mock.getString(MockResourceBundle.LABEL_11), LabelExtractor.get(MockResourceBundle.LABEL_1, Locale.US));
    }
    
    @Test
    public void testGetII() {
        MockResourceBundle mock = new MockResourceBundle();
        LabelExtractor.setResourceBundle(mock, Locale.US);
        // The label asked has NO entry in the dictionary, the key is returned
        try {
            mock.getString(MockResourceBundle.LABEL_12);
            fail("MissingResourceException expected");
        }
        catch (MissingResourceException ex0) {
            // Expected exception
        }
        catch (Exception ex1) {
            fail("No other exception expected");
        }
        assertEquals(MockResourceBundle.LABEL_12, LabelExtractor.get(MockResourceBundle.LABEL_12, Locale.US)); 
    }

    @Test
    public void testGetIII() {
        MockResourceBundle mock = new MockResourceBundle();
        LabelExtractor.setResourceBundle(mock, Locale.US);
        // The label asked has an entry in the dictionary
        assertEquals(mock.getString(LabelExtractor.ERROR_MESSAGE_PREFIX + MockResourceBundle.LABEL_0), LabelExtractor.get(0, Locale.US));
        assertEquals(mock.getString(LabelExtractor.ERROR_MESSAGE_PREFIX + MockResourceBundle.LABEL_1), LabelExtractor.get(1, Locale.US));
    }

    @Test
    public void testGetIV() {
        MockResourceBundle mock = new MockResourceBundle();
        LabelExtractor.setResourceBundle(mock, Locale.US);
        // The label returned is the given error code because the entry is not in the dictionary
        assertEquals("33", LabelExtractor.get(33, Locale.US));
    }

    @Test
    public void testGetV() {
    	assertNull(LabelExtractor.get(null, Locale.US));
    }

    @Test
    public void testGetVI() {
    	assertEquals("", LabelExtractor.get("", Locale.US));
    }
    
    @Test
    public void testInsertParametersI() {
    	assertEquals("test", LabelExtractor.insertParameters("{0}", new Object[] { "test" } ));
    	assertEquals("test1 - test0", LabelExtractor.insertParameters("{1} - {0}", new Object[] { "test0", "test1" } ));
    	assertEquals("test1 - test0 - test1", LabelExtractor.insertParameters("{1} - {0} - {1}", new Object[] { "test0", "test1" } ));
    }

    @Test
    public void testInsertParametersII() {
    	assertNull(LabelExtractor.get(null, new Object[] { "test" }, Locale.US));
    }

    @Test
    public void testInsertParametersIII() {
    	assertEquals("{1} - {0}", LabelExtractor.get("{1} - {0}", null, Locale.US));
    }
}
