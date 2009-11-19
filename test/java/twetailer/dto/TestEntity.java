package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.dao.BaseOperations;
import twetailer.dao.MockAppEngineEnvironment;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonParser;

public class TestEntity {

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
        Entity object = new Entity();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Entity object = new Entity(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Long key = 12345L;
    Date date = new Date();
    Boolean markForDeletion = Boolean.TRUE;

    @Test
    public void testAccessors() {
        Entity object = new Entity();

        object.setKey(key);
        object.setCreationDate(date);
        object.setModificationDate(date);
        object.setMarkedForDeletion(markForDeletion);

        assertEquals(key, object.getKey());
        assertEquals(date, object.getCreationDate());
        assertEquals(date, object.getModificationDate());
        assertEquals(markForDeletion, object.getMarkedForDeletion());
    }

    @Test
    public void testSetNullKey() {
        Entity object = new Entity();
        assertNull(object.getKey());

        // Cannot set it to 0
        object.setKey(null);
        assertNull(object.getKey());
    }

    @Test
    public void testSetZeroKey() {
        Entity object = new Entity();
        assertNull(object.getKey());

        // Cannot set it to 0
        object.setKey(0L);
        assertNull(object.getKey());
    }

    @Test
    public void testSetKeyTwice() {
        Entity object = new Entity();
        assertNull(object.getKey());

        // Can switch from null to a good value
        object.setKey(key);
        assertEquals(key, object.getKey());

        // Can re-apply the good value
        object.setKey(key);
        assertEquals(key, object.getKey());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetKeyDifferent() {
        Entity object = new Entity();
        assertNull(object.getKey());

        // Can switch from null to a good value
        object.setKey(key);
        assertEquals(Long.valueOf(12345L), object.getKey());

        // Try to apply another value
        object.setKey(54321L);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAttemptResetCreationDate() {
        Entity object = new Entity();
        assertNull(object.getKey());

        object.setCreationDate(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAttemptResetModificationDate() {
        Entity object = new Entity();
        assertNull(object.getKey());

        object.setModificationDate(null);
    }

    @Test
    public void testResetDates() {
        Entity object = new Entity();
        assertNull(object.getKey());

        object.setCreationDate(date);

        object.resetCoreDates();
        assertNotSame(date.getTime(), object.getCreationDate().getTime());
        assertEquals(object.getCreationDate(), object.getModificationDate());
    }

    @Test
    public void testJsonCommands() {
        Entity object = new Entity();

        object.setKey(key);
        object.setCreationDate(date);
        object.setModificationDate(date);
        object.setMarkedForDeletion(markForDeletion);

        Entity clone = new Entity(object.toJson());

        assertEquals(key, clone.getKey());
        assertEquals(DateUtils.dateToISO(date), DateUtils.dateToISO(clone.getCreationDate()));
        assertTrue(date.getTime() <= clone.getModificationDate().getTime()); // Always adjusted to the time of the un-marshalling process
        assertEquals(markForDeletion, clone.getMarkedForDeletion());
    }

    @Test
    public void testInvalidDateFormat() throws JsonException {
        Entity object = new Entity();
        Date date = object.getCreationDate();

        object.fromJson(new JsonParser("{'" + Entity.CREATION_DATE + "':'2009-01-01Tzzz'}").getJsonObject());

        assertEquals(date, object.getCreationDate()); // Corrupted date did not alter the original date
    }
}
