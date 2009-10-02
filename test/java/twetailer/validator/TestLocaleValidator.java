package twetailer.validator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import twetailer.dto.Location;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;
import domderrien.mocks.MockInputStream;

public class TestLocaleValidator {

    @Before
    public void setUp() throws Exception {
        LocaleValidator.setValidatorStream(null);
    }

    @Test
    public void testConstructor() {
        new LocaleValidator();
    }

    @Test
    public void testGetPublicGeoCoordinatesI() {
        JsonObject command = new GenericJsonObject();
        command.put(Location.POSTAL_CODE, "H0H0H0");
        command.put(Location.COUNTRY_CODE, "CA");
        LocaleValidator.getGeoCoordinates(command);
        assertEquals(90.0D, command.getDouble(Location.LATITUDE), 0.0D); // Latitude
        assertEquals(0.0D, command.getDouble(Location.LONGITUDE), 0.0D); // Longitude
    }

    @Test
    public void testGetPublicGeoCoordinatesII() {
        Location location = new Location();
        location.setPostalCode("H0H0H0");
        location.setCountryCode("CA");
        location = LocaleValidator.getGeoCoordinates(location);
        assertEquals(90.0D, location.getLatitude(), 0.0D); // Latitude
        assertEquals(0.0D, location.getLongitude(), 0.0D); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesI() {
        Double[] coords = LocaleValidator.getGeoCoordinates("H0H0H0", "CA");
        assertEquals(90.0D, coords[0].doubleValue(), 0.0D); // Latitude
        assertEquals(0.0D, coords[1].doubleValue(), 0.0D); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIIa() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("22.5, -120.5, Somewhere, US, 95432"));

        Double[] coords = LocaleValidator.getGeoCoordinates("95432", "US");
        assertEquals(22.5D, coords[0].doubleValue(), 0.0D); // Latitude
        assertEquals(-120.5D, coords[1].doubleValue(), 0.0D); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIIb() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream(""));

        Double[] coords = LocaleValidator.getGeoCoordinates("95432", "US");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIIc() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                throw new IOException("Done in purpose");
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("95432", "US");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIVa() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<latt>45.45</latt>\n\t<longt>-75.5</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "CA");
        assertEquals(45.45D, coords[0].doubleValue(), 0.0D); // Latitude
        assertEquals(-75.5D, coords[1].doubleValue(), 0.0D); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIVb() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<error>\n\t\t<code>105</code>\n\t\t<message>Bad format</code>\n\t</code><latt></latt>\n\t<longt>-</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "CA");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIVc() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream(""));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "CA");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIVd() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("") {
            @Override
            public int read() throws IOException {
                throw new IOException("Done in purpose");
            }
        });

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "CA");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesVII() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<latt>100.000000</latt>\n\t<longt>-75.5</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "CA");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesVIII() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<latt>-100.000000</latt>\n\t<longt>-75.5</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "CA");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesIX() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<latt>45.5</latt>\n\t<longt>200.000000</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "CA");
        assertEquals(Location.INVALID_COORDINATE, coords[0]); // Latitude
        assertEquals(Location.INVALID_COORDINATE, coords[1]); // Longitude
    }

    @Test
    public void testGetGeoCoordinatesX() throws IOException {
        LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<latt>45.5</latt>\n\t<longt>-200.000000</longt>\n</geodata>"));

        Double[] coords = LocaleValidator.getGeoCoordinates("A1B2C3", "CA");
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
        InputStream is = new MockInputStream("");
        LocaleValidator.setValidatorStream(is);
        assertEquals(is, LocaleValidator.getValidatorStream(null, null));
    }

    @Test
    public void testGetValidatorInputStreamII() throws IOException {
        try {
            LocaleValidator.getValidatorStream(null, "US");
        }
        catch(java.net.UnknownHostException ex) {
            // This exception can be thrown in disconnected mode
            // This exception is just ignored to not stop test runs in offline mode
        }
    }

    @Test
    public void testGetValidatorInputStreamIII() throws IOException {
        try {
            LocaleValidator.getValidatorStream(null, "CA");
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
        assertEquals(Locale.ENGLISH.getLanguage(), LocaleValidator.checkLanguage("EN"));
    }

    @Test
    public void testCheckLanguageII() {
        assertEquals(Locale.FRENCH.getLanguage(), LocaleValidator.checkLanguage("FR"));
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
        assertEquals(Locale.ENGLISH, LocaleValidator.getLocale("EN"));
    }

    @Test
    public void testCheckLocaleII() {
        assertEquals(Locale.FRENCH, LocaleValidator.getLocale("FR"));
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
        assertEquals(Locale.US.getCountry(), LocaleValidator.checkCountryCode("US"));
    }

    @Test
    public void testCheckCountryCodeII() {
        assertEquals(Locale.CANADA.getCountry(), LocaleValidator.checkCountryCode("CA"));
    }

    @Test
    public void testCheckCountryCodeIII() {
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, LocaleValidator.checkCountryCode(null));
    }

    @Test
    public void testCheckCountryCodeIV() {
        assertEquals(LocaleValidator.DEFAULT_COUNTRY_CODE, LocaleValidator.checkCountryCode("zzz"));
    }
}
