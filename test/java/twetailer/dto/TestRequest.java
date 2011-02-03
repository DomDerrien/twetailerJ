package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestRequest {

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
        Request object = new Request();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Request object = new Request(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Long key = 76554L;

    List<String> cc = Arrays.asList(new String[] {"cc1", "cc2"});
    List<String> criteria = Arrays.asList(new String[] {"first", "second"});
    Date expirationDate = new Date(new Date().getTime() + 65536L);
    Long influencerKey = 654645232L;
    Long locationKey = 67890L;
    Long quantity = 15L;
    Double range = 25.52D;
    String rangeUnit = LocaleValidator.MILE_UNIT;

    @Test
    public void testAccessors() {
        Request object = new Request();

        // Request
        object.setCriteria(criteria);
        object.setExpirationDate(expirationDate);
        object.setInfluencerKey(influencerKey);
        object.setLocationKey(locationKey);
        object.setQuantity(quantity);
        object.setRange(range);
        object.setRangeUnit(rangeUnit);

        // Request
        assertEquals(criteria, object.getCriteria());
        assertEquals(expirationDate, object.getExpirationDate());
        assertEquals(influencerKey, object.getInfluencerKey());
        assertEquals(locationKey, object.getLocationKey());
        assertEquals(quantity, object.getQuantity());
        assertEquals(range, object.getRange());
        assertEquals(rangeUnit, object.getRangeUnit());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetCriteriaI() {
        Request object = new Request();

        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("first"); // Add it twice
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("second");
        assertEquals(2, object.getCriteria().size());

        object.removeCriterion("first"); // Remove first
        assertEquals(1, object.getCriteria().size());

        object.resetCriteria(); // Reset all
        assertEquals(0, object.getCriteria().size());

        object.setCriteria(null); // Failure!
    }

    @Test
    public void testResetCriteriaII() {
        Request object = new Request();

        object.resetLists(); // To force the criteria list creation
        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.resetLists(); // To be sure there's no error
        object.removeCriterion("first"); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetCriteria(); // Reset all
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetAction() {
        Request object = new Request();

        object.setAction((Action) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetExpirationDate() {
        Request object = new Request();

        object.setExpirationDate(null);
    }

    /** Relaxed validation because user's can give invalid dates up-front!
    @Test(expected=IllegalArgumentException.class)
    public void testSetExpirationDateInPast() {
        Request object = new Request();

        object.setExpirationDate(new Date(12345L));
    }
    */

    @Test
    public void testSetRangeUnit() {
        Request object = new Request();

        object.setRangeUnit(LocaleValidator.MILE_UNIT.toLowerCase(Locale.ENGLISH));
        assertEquals(LocaleValidator.MILE_UNIT, object.getRangeUnit());

        object.setRangeUnit(LocaleValidator.MILE_UNIT.toUpperCase(Locale.ENGLISH));
        assertEquals(LocaleValidator.MILE_UNIT, object.getRangeUnit());

        object.setRangeUnit(LocaleValidator.ALTERNATE_MILE_UNIT.toUpperCase(Locale.ENGLISH));
        assertEquals(LocaleValidator.MILE_UNIT, object.getRangeUnit());

        object.setRangeUnit("zzz");
        assertEquals(LocaleValidator.KILOMETER_UNIT, object.getRangeUnit());

        object.setRangeUnit(null);
        assertEquals(LocaleValidator.KILOMETER_UNIT, object.getRangeUnit());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetState() {
        Request object = new Request();

        object.setState((State) null);
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        Request object = new Request();

        object.setKey(key);

        object.setCC(cc);
        object.setCriteria(criteria);
        object.setExpirationDate(expirationDate);
        object.setInfluencerKey(influencerKey);
        object.setLocationKey(locationKey);
        object.setQuantity(quantity);
        object.setRange(range);
        object.setRangeUnit(rangeUnit);

        Request clone = new Request();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, object.getKey());

        assertEquals(DateUtils.dateToISO(expirationDate), DateUtils.dateToISO(clone.getExpirationDate()));
        assertEquals(influencerKey, clone.getInfluencerKey());
        assertEquals(range, clone.getRange());
        assertEquals(rangeUnit, clone.getRangeUnit());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        Request object = new Request();

        Request clone = new Request();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(clone.getDueDate(), clone.getExpirationDate());
        assertNull(clone.getInfluencerKey());
        assertEquals(LocaleValidator.DEFAULT_RANGE, clone.getRange());
        assertEquals(LocaleValidator.DEFAULT_RANGE_UNIT, clone.getRangeUnit());
    }

    @Test
    public void testJsonCommandsIII() {
        //
        // Admin update (middle)
        //
        Request object = new Request();

        object.setKey(key);

        object.setCC(cc);
        object.setCriteria(criteria);
        object.setExpirationDate(expirationDate);
        object.setInfluencerKey(influencerKey);
        object.setLocationKey(locationKey);
        object.setQuantity(quantity);
        object.setRange(range);
        object.setRangeUnit(rangeUnit);

        Request clone = new Request();
        clone.fromJson(object.toJson(), true, false);

        assertEquals(key, object.getKey());

        assertEquals(DateUtils.dateToISO(expirationDate), DateUtils.dateToISO(clone.getExpirationDate()));
        assertNull(clone.getInfluencerKey());
        assertEquals(range, clone.getRange());
        assertEquals(rangeUnit, clone.getRangeUnit());
    }

    @Test
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        Request object = new Request();

        // object.setKey(key);

        object.setCC(cc);
        object.setCriteria(criteria);
        object.setExpirationDate(expirationDate);
        object.setInfluencerKey(influencerKey);
        object.setLocationKey(locationKey);
        object.setQuantity(quantity);
        object.setRange(range);
        object.setRangeUnit(rangeUnit);

        Request clone = new Request();
        clone.fromJson(object.toJson());

        // assertEquals(key, object.getKey());

        assertEquals(DateUtils.dateToISO(expirationDate), DateUtils.dateToISO(clone.getExpirationDate()));
        assertNull(clone.getInfluencerKey());
        assertEquals(range, clone.getRange());
        assertEquals(rangeUnit, clone.getRangeUnit());
    }

    @Test
    public void testJsonCommandsV() {
        Request object = new Request();
        object.fromJson(new GenericJsonObject());
        assertEquals(object.getDueDate(), object.getExpirationDate());
    }

    @Test
    public void testInvalidDateFormat() throws JsonException {
        Request object = new Request();
        Date date = object.getExpirationDate();

        object.fromJson(new JsonParser("{'" + Request.EXPIRATION_DATE + "':'2009-0A-01Tzzz'}").getJsonObject());

        assertEquals(date.getTime() / 1000, object.getExpirationDate().getTime() / 1000); // Round up the milli-seconds
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRange() {
        new Request().setRange((Double) null);
    }

    @Test
    public void testFromJsonI() {
        Calendar now = DateUtils.getNowCalendar();
        now.set(Calendar.MILLISECOND, 0); // as milliseconds are ignored by the ISO format

        JsonObject parameters = new GenericJsonObject();
        parameters.put(Request.EXPIRATION_DATE, DateUtils.dateToISO(now.getTime()));

        assertEquals(now.getTime(), new Request(parameters).getDueDate());
    }

    @Test
    public void testFromJsonII() {
        Long key = 12345L;
        Calendar now = DateUtils.getNowCalendar();
        now.set(Calendar.MILLISECOND, 0); // as milliseconds are ignored by the ISO format

        Request object = new Request();
        Date dueDate = object.getDueDate();

        JsonObject parameters = new GenericJsonObject();
        parameters.put(Entity.KEY, key);
        parameters.put(Request.EXPIRATION_DATE, DateUtils.dateToISO(now.getTime()));

        object.fromJson(parameters);
        assertEquals(dueDate, object.getDueDate());
    }

    @Test
    public void testFromJsonIII() {
        Calendar now = DateUtils.getNowCalendar();
        now.set(Calendar.MILLISECOND, 0); // as milliseconds are ignored by the ISO format

        JsonObject parameters = new GenericJsonObject();
        parameters.put(Command.DUE_DATE, DateUtils.dateToISO(now.getTime()));

        assertEquals(now.getTime(), new Request(parameters).getExpirationDate());
    }

    @Test
    public void testFromJsonIV() {
        Long key = 12345L;

        Request object = new Request();
        Date expirationDate = object.getExpirationDate();
        object.setDueDate(null);

        JsonObject parameters = new GenericJsonObject();
        parameters.put(Entity.KEY, key);

        object.fromJson(parameters);
        assertEquals(expirationDate, object.getExpirationDate());
    }

    @Test
    public void testGetDueDate() {
        Request object = new Request();
        object.setDueDate(null);
        assertEquals(object.getExpirationDate(), object.getDueDate());
    }
}
