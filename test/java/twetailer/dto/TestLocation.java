package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.task.RobotResponder;
import twetailer.validator.LocaleValidator;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestLocation {

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
        Location object = new Location();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Location object = new Location(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Long key = 4354L;
    String countryCode = "US";
    Boolean hasStore = Boolean.TRUE;
    Double latitude = 45.0D;
    Double longitude = -27.5D;
    String postalCode = "00000";

    @Test
    public void testAccessors() {
        Location object = new Location();

        object.setCountryCode(countryCode);
        object.setHasStore(hasStore);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        assertEquals(countryCode, object.getCountryCode());
        assertEquals(hasStore, object.hasStore());
        assertEquals(latitude, object.getLatitude());
        assertEquals(longitude, object.getLongitude());
        assertEquals(postalCode, object.getPostalCode());
    }

    @Test
    public void testSetCountryCode() {
        Location object = new Location();

        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, object.getCountryCode());

        object.setCountryCode(Locale.CANADA.getCountry());
        assertEquals(Locale.CANADA.getCountry(), object.getCountryCode());

        object.setCountryCode("zzz");
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, object.getCountryCode());
    }

    @Test
    public void testHasStore() {
        Location object = new Location();

        assertFalse(object.hasStore()); // Default value is false

        object.setHasStore(Boolean.FALSE);
        assertFalse(object.hasStore());

        object.setHasStore(Boolean.TRUE);
        assertTrue(object.hasStore());

        object.setHasStore(null);
        assertFalse(object.hasStore()); // Reset value is false
    }

    @Test
    public void testSetLatitude() {
        Location object = new Location();

        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());

        object.setLatitude(null);
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());

        object.setLatitude(90.00001);
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());

        object.setLatitude(-90.00001);
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());
    }

    @Test
    public void testSetLongitude() {
        Location object = new Location();

        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());

        object.setLongitude(null);
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());

        object.setLongitude(180.00001);
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());

        object.setLongitude(-180.00001);
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());
    }

    @Test
    public void testSetPostalCode() {
        Location object = new Location();

        assertNull(object.getPostalCode());

        String test = RobotResponder.ROBOT_POSTAL_CODE;
        object.setPostalCode(test);
        assertEquals(test, object.getPostalCode());

        String otherTest = "h0h 0h0";
        object.setPostalCode(otherTest);
        assertEquals(test, object.getPostalCode()); // Capitalized and without spaces

        test = "95423";
        object.setPostalCode(test);
        assertEquals(test, object.getPostalCode()); // Capitalized and without spaces

        otherTest = "95423-3242";
        object.setPostalCode(otherTest);
        assertEquals(test + "3242", object.getPostalCode()); // Capitalized and without spaces
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Location object = new Location();

        object.setKey(key);

        object.setCountryCode(countryCode);
        object.setHasStore(hasStore);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        Location clone = new Location();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(countryCode, clone.getCountryCode());
        assertEquals(hasStore, clone.hasStore());
        assertEquals(latitude, clone.getLatitude());
        assertEquals(longitude, clone.getLongitude());
        assertEquals(postalCode, clone.getPostalCode());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Location object = new Location();

        Location clone = new Location();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, clone.getCountryCode());
        assertFalse(clone.hasStore());
        assertEquals(Location.INVALID_COORDINATE, clone.getLatitude());
        assertEquals(Location.INVALID_COORDINATE, clone.getLongitude());
        assertNull(clone.getPostalCode());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Location object = new Location();

        object.setKey(key);

        object.setCountryCode(countryCode);
        object.setHasStore(hasStore);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        Location clone = new Location();
        clone.fromJson(object.toJson(), true, false);

        assertEquals(key, clone.getKey());

        assertEquals(countryCode, clone.getCountryCode());
        assertEquals(hasStore, clone.hasStore());
        assertEquals(latitude, clone.getLatitude());
        assertEquals(longitude, clone.getLongitude());
        assertEquals(postalCode, clone.getPostalCode());
    }

    @Test
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        Location object = new Location();

        // object.setKey(key);

        object.setCountryCode(countryCode);
        object.setHasStore(hasStore);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        Location clone = new Location();
        clone.fromJson(object.toJson());

        assertEquals(countryCode, clone.getCountryCode());
        assertEquals(hasStore, clone.hasStore());
        assertEquals(latitude, clone.getLatitude());
        assertEquals(longitude, clone.getLongitude());
        assertEquals(postalCode, clone.getPostalCode());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsV() {
        //
        // User update for an existing object (lowest)
        //
        Location object = new Location();

        object.setKey(key);

        object.setCountryCode(countryCode);
        object.setHasStore(hasStore);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        Location clone = new Location();
        clone.fromJson(object.toJson());
    }

    @Test
    public void testToJsonWithCoordinatesResetI() throws JsonException {
        Location object = new Location();

        object.setCountryCode(countryCode);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        object.fromJson(new JsonParser(
                "{'" + Location.COUNTRY_CODE + "':'" + countryCode + "'" +
                ",'" + Location.LATITUDE + "':" + latitude +
                ",'" + Location.LONGITUDE + "':" + longitude +
                ",'" + Location.POSTAL_CODE + "':'" + postalCode + "'" +
                "}").getJsonObject());

        assertEquals(countryCode, object.getCountryCode());
        assertEquals(latitude, object.getLatitude());
        assertEquals(longitude, object.getLongitude());
        assertEquals(postalCode, object.getPostalCode());
    }

    @Test
    public void testToJsonWithCoordinatesResetII() throws JsonException {
        Location object = new Location();

        object.setCountryCode(countryCode);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        object.fromJson(new JsonParser("{'" + Location.COUNTRY_CODE + "':'zzz'}").getJsonObject());

        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, object.getCountryCode());
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());
        assertNull(object.getPostalCode());
    }

    @Test
    public void testToJsonWithCoordinatesResetIII() throws JsonException {
        Location object = new Location();

        object.setCountryCode(countryCode);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        object.fromJson(new JsonParser("{'" + Location.LATITUDE + "':1.23456789}").getJsonObject());

        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, object.getCountryCode());
        assertEquals(Double.valueOf(1.23456789D), object.getLatitude());
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());
        assertNull(object.getPostalCode());
    }

    @Test
    public void testToJsonWithCoordinatesResetIV() throws JsonException {
        Location object = new Location();

        object.setCountryCode(countryCode);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        object.fromJson(new JsonParser("{'" + Location.LONGITUDE + "':1.23456789}").getJsonObject());

        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, object.getCountryCode());
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());
        assertEquals(Double.valueOf(1.23456789D), object.getLongitude());
        assertNull(object.getPostalCode());
    }

    @Test
    public void testToJsonWithCoordinatesResetV() throws JsonException {
        Location object = new Location();

        object.setCountryCode(countryCode);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        object.fromJson(new JsonParser("{'" + Location.POSTAL_CODE + "':'zzz'}").getJsonObject());

        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, object.getCountryCode());
        assertEquals(Location.INVALID_COORDINATE, object.getLatitude());
        assertEquals(Location.INVALID_COORDINATE, object.getLongitude());
        assertEquals("ZZZ", object.getPostalCode());
    }

    @Test
    public void testToJsonWithCoordinatesResetVI() throws JsonException {
        Location object = new Location();

        object.setCountryCode(countryCode);
        object.setLatitude(latitude);
        object.setLongitude(longitude);
        object.setPostalCode(postalCode);

        object.fromJson(new JsonParser(
                "{'" + Location.COUNTRY_CODE + "':'zzz'" +
                ",'" + Location.LATITUDE + "':1.23456789" +
                ",'" + Location.LONGITUDE + "':1.23456789" +
                ",'" + Location.POSTAL_CODE + "':'zzz'" +
                "}").getJsonObject());

        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, object.getCountryCode());
        assertEquals(Double.valueOf(1.23456789D), object.getLatitude());
        assertEquals(Double.valueOf(1.23456789D), object.getLongitude());
        assertEquals("ZZZ", object.getPostalCode());
    }

    @Test
    public void testHasAttributeForNewLocationI() {
        JsonObject parameters = new GenericJsonObject();

        assertFalse(new Location().hasNewAttributes(parameters));
    }

    @Test
    public void testHasAttributeForNewLocationII() {
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Location.COUNTRY_CODE, "zzz");

        assertTrue(new Location().hasNewAttributes(parameters));
    }

    @Test
    public void testHasAttributeForNewLocationIII() {
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Location.POSTAL_CODE, "zzz");

        assertTrue(new Location().hasNewAttributes(parameters));
    }

    @Test
    public void testHasAttributeForNewLocationIV() {
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Location.LATITUDE, 123.45D);

        assertTrue(new Location().hasNewAttributes(parameters));
    }

    @Test
    public void testHasAttributeForNewLocationV() {
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Location.LONGITUDE, 123.45D);

        assertTrue(new Location().hasNewAttributes(parameters));
    }

    @Test
    public void testHasAttributeForNewLocationVI() {
        JsonObject parameters = new GenericJsonObject();

        assertFalse(Location.hasAttributeForANewLocation(parameters));
    }

    @Test
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(Location.LOCATION_KEY, key);

        assertEquals(key, new Location(parameters).getKey());
    }
}
