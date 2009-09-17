package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.dao.BaseOperations;
import twetailer.dao.MockAppEngineEnvironment;

public class TestSettings {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();

        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testConstructorI() {
        Settings object = new Settings();
        assertNull(object.getKey());
    }

    Long key = 12345L;
    String name = "settings";
    Long lastProcessDirectMessageId = 67890L;
    Long lastRobotDirectMessageId = 54321L;

    @Test
    public void testAccessors() {
        Settings object = new Settings();

        object.setKey(key);
        object.setName(name);
        object.setLastProcessDirectMessageId(lastProcessDirectMessageId);
        object.setLastRobotDirectMessageId(lastRobotDirectMessageId);

        assertEquals(key, object.getKey());
        assertEquals(name, object.getName());
        assertEquals(lastProcessDirectMessageId, object.getLastProcessDirectMessageId());
        assertEquals(lastRobotDirectMessageId, object.getLastRobotDirectMessageId());
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

    @Test(expected=IllegalArgumentException.class)
    public void testSetLastRobotDirectMessageIdI() {
        new Settings().setLastRobotDirectMessageId(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLastRobotDirectMessageIdII() {
        new Settings().setLastRobotDirectMessageId(0L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLastRobotDirectMessageIdIII() {
        new Settings().setLastRobotDirectMessageId(-3243L);
    }

    @Test
    public void testJsonCommandsI() {
        Settings object = new Settings();

        object.setKey(key);
        object.setName(name);
        object.setLastProcessDirectMessageId(lastProcessDirectMessageId);
        object.setLastRobotDirectMessageId(lastRobotDirectMessageId);

        Settings clone = new Settings(object.toJson());

        assertEquals(key, clone.getKey());
        assertEquals(name, clone.getName());
        assertEquals(lastProcessDirectMessageId, clone.getLastProcessDirectMessageId());
        assertEquals(lastRobotDirectMessageId, clone.getLastRobotDirectMessageId());
    }

    @Test
    public void testJsonCommandsII() {
        Settings object = new Settings();

        assertNull(object.getKey());

        Settings clone = new Settings(object.toJson());

        assertNull(clone.getKey());
    }
}
