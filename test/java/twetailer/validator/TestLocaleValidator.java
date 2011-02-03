package twetailer.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Locale;

import javamocks.io.MockInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.dto.Location;
import twetailer.task.RobotResponder;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestLocaleValidator {

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        LocaleValidator.setMockValidatorStream(null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        new LocaleValidator();
    }

    @Test
    public void testGetPublicGeoCoordinatesI() {
        JsonObject command = new GenericJsonObject();
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);
        LocaleValidator.getGeoCoordinates(command);
        assertEquals(90.0D, command.getDouble(Location.LATITUDE), 0.0D); // Latitude
        assertEquals(0.0D, command.getDouble(Location.LONGITUDE), 0.0D); // Longitude
    }

    @Test
    public void testGetPublicGeoCoordinatesII() {
        Location location = new Location();
        location.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
        location.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
        location = LocaleValidator.getGeoCoordinates(location);
        assertEquals(90.0D, location.getLatitude(), 0.0D); // Latitude
        assertEquals(0.0D, location.getLongitude(), 0.0D); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesI() {
        Double[] coords = LocaleValidator.getGeoCoordinates(RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(90.0D, coords[0].doubleValue(), 0.0D); // Latitude
        assertEquals(0.0D, coords[1].doubleValue(), 0.0D); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIIa() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("{'status':'OK','results':[{'geometry':{'location':{'lat':22.5,'lng':-120.5}}}]}"));
        // LocaleValidator.setValidatorStream(new MockInputStream("22.5, -120.5, Somewhere, US, 95432"));

        Double[] coords = LocaleValidator.getGeoCoordinates("95432", Locale.US.getCountry());
        assertEquals(22.5D, coords[0].doubleValue(), 0.0D); // Latitude
        assertEquals(-120.5D, coords[1].doubleValue(), 0.0D); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIIb() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream(""));

        Double[] coords = LocaleValidator.getGeoCoordinates("95432", Locale.US.getCountry());
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIIc() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                throw new IOException("Done in purpose");
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("95432", Locale.US.getCountry());
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIVa() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("{'status':'OK','results':[{'geometry':{'location':{'lat':45.45,'lng':-75.5}}}]}"));
        // LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<latt>45.45</latt>\n\t<longt>-75.5</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(45.45D, coords[0].doubleValue(), 0.0D); // Latitude
        assertEquals(-75.5D, coords[1].doubleValue(), 0.0D); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIVb() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("<geodata>\n\t<error>\n\t\t<code>105</code>\n\t\t<message>Bad format</code>\n\t</code><latt></latt>\n\t<longt>-</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIVc() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream(""));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIVd() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                throw new IOException("Done in purpose");
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesVII() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("<geodata>\n\t<latt>100.000000</latt>\n\t<longt>-75.5</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesVIII() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("<geodata>\n\t<latt>-100.000000</latt>\n\t<longt>-75.5</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIX() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("<geodata>\n\t<latt>45.5</latt>\n\t<longt>200.000000</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesX() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("<geodata>\n\t<latt>45.5</latt>\n\t<longt>-200.000000</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", RobotResponder.ROBOT_COUNTRY_CODE);
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesXI() throws IOException {
        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "zzz");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetValidatorInputStreamI() throws IOException {
        MockInputStream is = new MockInputStream("");
        LocaleValidator.setMockValidatorStream(is);
        assertEquals(is, LocaleValidator.getValidatorStream(null, null));
    }

    @Test
    public void testGetValidatorInputStreamII() throws IOException {
        try {
            LocaleValidator.getValidatorStream(null, Locale.US.getCountry());
        }
        catch(java.net.UnknownHostException ex) {
            // This exception can be thrown in disconnected mode
            // This exception is just ignored to not stop test runs in offline mode
        }
    }

    @Test
    public void testGetValidatorInputStreamIII() throws IOException {
        try {
            LocaleValidator.getValidatorStream(null, RobotResponder.ROBOT_COUNTRY_CODE);
        }
        catch(java.net.UnknownHostException ex) {
            // This exception can be thrown in disconnected mode
            // This exception is just ignored to not stop test runs in offline mode
        }
    }

    @Test(expected=IOException.class)
    public void testGetValidatorInputStreamIV() throws IOException {
        LocaleValidator.getValidatorStream(null, "zzz");
    }

    @Test
    public void testCheckLanguageI() {
        assertEquals(Locale.ENGLISH.getLanguage(), LocaleValidator.checkLanguage(Locale.ENGLISH.getLanguage()));
    }

    @Test
    public void testCheckLanguageII() {
        assertEquals(Locale.FRENCH.getLanguage(), LocaleValidator.checkLanguage(Locale.FRENCH.getLanguage()));
    }

    @Test
    public void testCheckLanguageIII() {
        assertEquals(LocaleValidator.DEFAULT_LANGUAGE, LocaleValidator.checkLanguage(null));
    }

    @Test
    public void testCheckLanguageIV() {
        assertEquals(LocaleValidator.DEFAULT_LANGUAGE, LocaleValidator.checkLanguage("zzz"));
    }

    @Test
    public void testCheckLocaleI() {
        assertEquals(Locale.ENGLISH, LocaleValidator.getLocale(Locale.UK.getLanguage()));
    }

    @Test
    public void testCheckLocaleII() {
        assertEquals(Locale.FRENCH, LocaleValidator.getLocale(Locale.FRANCE.getLanguage()));
    }

    @Test
    public void testCheckLocaleIII() {
        assertEquals(LocaleValidator.DEFAULT_LOCALE, LocaleValidator.getLocale(null));
    }

    @Test
    public void testCheckLocaleIV() {
        assertEquals(LocaleValidator.DEFAULT_LOCALE, LocaleValidator.getLocale("zzz"));
    }

    @Test
    public void testCheckCountryCodeI() {
        assertEquals(Locale.US.getCountry(), LocaleValidator.checkCountryCode(Locale.US.getCountry()));
    }

    @Test
    public void testCheckCountryCodeII() {
        assertEquals(Locale.CANADA.getCountry(), LocaleValidator.checkCountryCode(RobotResponder.ROBOT_COUNTRY_CODE));
    }

    @Test
    public void testCheckCountryCodeIII() {
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, LocaleValidator.checkCountryCode(null));
    }

    @Test
    public void testCheckCountryCodeIV() {
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, LocaleValidator.checkCountryCode("zzz"));
    }

    @Test
    public void testGetRegularExpressionFilterI() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                fail("Call not expected!");
                return -1;
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("95", Locale.US.getCountry());
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetRegularExpressionFilterII() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                fail("Call not expected!");
                return -1;
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("954344343", Locale.US.getCountry());
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetRegularExpressionFilterIII() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                fail("Call not expected!");
                return -1;
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("12345-67890", Locale.US.getCountry());
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetRegularExpressionFilterV() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                fail("Call not expected!");
                return -1;
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("hhhhhh", Locale.CANADA.getCountry());
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetRegularExpressionFilterVI() throws IOException {
        LocaleValidator.setMockValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                fail("Call not expected!");
                return -1;
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("h1x9", Locale.CANADA.getCountry());
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testCheckRangeUnitI() {
        assertEquals(LocaleValidator.MILE_UNIT, LocaleValidator.checkRangeUnit(LocaleValidator.MILE_UNIT));
    }

    @Test
    public void testCheckRangeUnitII() {
        assertEquals(LocaleValidator.MILE_UNIT, LocaleValidator.checkRangeUnit(LocaleValidator.ALTERNATE_MILE_UNIT));
    }

    @Test
    public void testCheckRangeUnitIII() {
        assertEquals(LocaleValidator.KILOMETER_UNIT, LocaleValidator.checkRangeUnit(LocaleValidator.KILOMETER_UNIT));
    }

    @Test
    public void testCheckRangeUnitIV() {
        assertEquals(LocaleValidator.KILOMETER_UNIT, LocaleValidator.checkRangeUnit(null));
    }

    @Test
    public void testCheckRangeUnitV() {
        assertEquals(LocaleValidator.KILOMETER_UNIT, LocaleValidator.checkRangeUnit("zzzzzzz"));
    }

    @Test
    public void testGetCollator() {
        assertNotNull(LocaleValidator.getCollator(LocaleValidator.DEFAULT_LOCALE));
    }
}
