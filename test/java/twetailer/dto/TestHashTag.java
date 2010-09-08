package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestHashTag {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testConstructor() {
        new HashTag();
    }

    @Test
    public void testIsSupportedHashTag() {
        assertTrue(HashTag.isSupportedHashTag("golf"));
        assertTrue(HashTag.isSupportedHashTag("eztoff"));
        assertFalse(HashTag.isSupportedHashTag("non-sense"));
    }

    @Test
    public void testGetSupportedHashTag() {
        assertEquals("golf", HashTag.getSupportedHashTag("golf"));
        assertEquals("golf", HashTag.getSupportedHashTag("eztoff"));
        assertNull(HashTag.getSupportedHashTag("non-sense"));
    }

    @Test
    public void testGetSupportedHashTags() {
        assertNotNull(HashTag.getSupportedHashTags());
    }
}
