package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestEntity {

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
    Calendar date = DateUtils.getNowCalendar();
    Long locationKey = 543654L;
    Boolean markForDeletion = Boolean.TRUE;

    @Test
    public void testAccessors() {
        Entity object = new Entity();

        object.setKey(key);
        object.setCreationDate(date.getTime());
        object.setLocationKey(locationKey);
        object.setModificationDate(date.getTime());
        object.setMarkedForDeletion(markForDeletion);

        assertEquals(key, object.getKey());
        assertEquals(date.getTimeInMillis(), object.getCreationDate().getTime());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(date.getTimeInMillis(), object.getModificationDate().getTime());
        assertEquals(markForDeletion, object.getMarkedForDeletion());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetNullKey() {
        Entity object = new Entity();
        assertNull(object.getKey());

        // Cannot set it to 0
        object.setKey(null);
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

        object.setCreationDate(date.getTime());

        object.resetCoreDates();
        assertNotSame(date.getTimeInMillis(), object.getCreationDate().getTime());
        assertEquals(object.getCreationDate(), object.getModificationDate());
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Entity object = new Entity();

        object.setKey(key);

        object.setCreationDate(date.getTime());
        object.setLocationKey(locationKey);
        object.setModificationDate(date.getTime());
        object.setMarkedForDeletion(markForDeletion);

        Entity clone = new Entity();
        clone.fromJson(object.toJson(), true, true);

        // In the translation, the milliseconds are rounded!
        date.set(Calendar.MILLISECOND, 0);

        assertEquals(key, clone.getKey());

        assertEquals(date.getTimeInMillis(), clone.getCreationDate().getTime());
        assertEquals(locationKey, clone.getLocationKey());
        assertTrue(clone.getMarkedForDeletion());
        assertEquals(date.getTimeInMillis(), clone.getModificationDate().getTime());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Entity object = new Entity();
        object.resetLists();

        Entity clone = new Entity();
        clone.resetLists();
        clone.fromJson(object.toJson(), true, true);

        assertNull(clone.getKey());

        assertNull(clone.getCreationDate());
        assertNull(clone.getLocationKey());
        assertFalse(clone.getMarkedForDeletion());
        assertNull(clone.getModificationDate());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Entity object = new Entity();

        object.setKey(key);

        object.setCreationDate(date.getTime());
        object.setLocationKey(locationKey);
        object.setModificationDate(date.getTime());
        object.setMarkedForDeletion(markForDeletion);

        Entity clone = new Entity();
        clone.fromJson(object.toJson(), true, false);

        // In the translation, the milliseconds are rounded!
        date.set(Calendar.MILLISECOND, 0);

        assertEquals(key, clone.getKey());

        assertTrue(date.getTimeInMillis() <= clone.getCreationDate().getTime());
        assertEquals(locationKey, clone.getLocationKey());
        assertFalse(clone.getMarkedForDeletion());
        assertTrue(date.getTimeInMillis() <= clone.getModificationDate().getTime());
    }

    @Test
    public void testInvalidDateFormat() throws JsonException {
        Entity object = new Entity();
        Date date = object.getCreationDate();

        object.fromJson(new JsonParser("{'" + Entity.CREATION_DATE + "':'2009-01-01Tzzz'}").getJsonObject());

        assertEquals(date, object.getCreationDate()); // Corrupted date did not alter the original date
    }

    @Test
    public void testGetMarkedForDeletion() {
        Entity entity = new Entity();
        assertFalse(entity.getMarkedForDeletion());
        entity.setMarkedForDeletion(null);
        assertFalse(entity.getMarkedForDeletion());
        entity.setMarkedForDeletion(false);
        assertFalse(entity.getMarkedForDeletion());
        entity.setMarkedForDeletion(true);
        assertTrue(entity.getMarkedForDeletion());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAttemptsetLocationKey() {
        Entity object = new Entity();
        assertNull(object.getKey());

        object.setLocationKey(null);
    }

    @Test
    public void testToJsonI() {
        Entity object = new Entity();
        assertNull(object.getKey());
        assertNull(object.getLocationKey());

        JsonObject out = object.toJson();
        assertFalse(out.containsKey(Entity.KEY));
        assertFalse(out.containsKey(Entity.LOCATION_KEY));
    }

    @Test
    public void testToJsonII() {
        Entity object = new Entity();
        object.setKey(12345L);
        object.setLocationKey(54232L);

        JsonObject out = object.toJson();
        assertTrue(out.containsKey(Entity.KEY));
        assertTrue(out.containsKey(Entity.LOCATION_KEY));
    }

    @Test
    public void testGetModificationDate() {
        Entity object = new Entity();
        object.resetLists();
        assertNull(object.getModificationDate());
    }

    @Test
    public void testtoJsonCommandsIII() {
        JsonObject json = new GenericJsonObject();
        json.put(Entity.CREATION_DATE, "corrupted date");
        json.put(Entity.MODIFICATION_DATE, "corrupted date");

        Entity object = new Entity();
        object.fromJson(json, true, true);
    }

    @Test
    public void testtoJsonCommandsIV() {
        JsonObject json = new GenericJsonObject();
        Entity object = new Entity();

        json.put(Entity.KEY, 12345L);
        object.fromJson(json, true, false);
        assertEquals(12345L, object.getKey().longValue());
    }

    @Test
    public void testtoJsonCommandsV() {
        JsonObject json = new GenericJsonObject();
        Entity object = new Entity();

        object.setKey(23456L);
        json.put(Entity.KEY, 12345L);
        object.fromJson(json, true, true);
        assertEquals(12345L, object.getKey().longValue());
    }

    @Test
    public void testtoJsonCommandsVI() {
        JsonObject json = new GenericJsonObject();
        Entity object = new Entity();

        object.setKey(23456L);
        json.put(Entity.KEY, 12345L);
        object.fromJson(json, true, false);
        assertEquals(23456L, object.getKey().longValue());
    }
}
