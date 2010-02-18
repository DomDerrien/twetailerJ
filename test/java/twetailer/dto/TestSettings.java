package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestSettings {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
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
    public void testConstructorI() {
        Settings object = new Settings();
        assertNull(object.getKey());
    }

    Long key = 12345L;
    String name = "settings";
    Long lastProcessDirectMessageId = 67890L;
    Long robotConsumerKey = 67876L;
    Long robotSaleAssociateKey = 12321L;

    @Test
    public void testAccessors() {
        Settings object = new Settings();

        object.setKey(key);
        object.setName(name);
        object.setLastProcessDirectMessageId(lastProcessDirectMessageId);
        object.setRobotConsumerKey(robotConsumerKey);
        object.setRobotSaleAssociateKey(robotSaleAssociateKey);

        assertEquals(key, object.getKey());
        assertEquals(name, object.getName());
        assertEquals(lastProcessDirectMessageId, object.getLastProcessDirectMessageId());
        assertEquals(robotConsumerKey, object.getRobotConsumerKey());
        assertEquals(robotSaleAssociateKey, object.getRobotSaleAssociateKey());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetName() {
        new Settings().setName(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLastProcessDirectMessageIdI() {
        new Settings().setLastProcessDirectMessageId(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLastProcessDirectMessageIdII() {
        new Settings().setLastProcessDirectMessageId(0L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLastProcessDirectMessageIdIII() {
        new Settings().setLastProcessDirectMessageId(-32L);
    }

    @Test
    public void testJsonCommandsI() {
        Settings object = new Settings();

        object.setKey(key);
        object.setName(name);
        object.setLastProcessDirectMessageId(lastProcessDirectMessageId);

        Settings clone = new Settings(object.toJson());

        assertEquals(key, clone.getKey());
        assertEquals(name, clone.getName());
        assertEquals(lastProcessDirectMessageId, clone.getLastProcessDirectMessageId());
    }

    @Test
    public void testJsonCommandsII() {
        Settings object = new Settings();

        assertNull(object.getKey());

        Settings clone = new Settings(object.toJson());

        assertNull(clone.getKey());
    }
}
