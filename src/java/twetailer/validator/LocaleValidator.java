package twetailer.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Collator;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javamocks.io.MockInputStream;
import twetailer.connector.MailConnector;
import twetailer.dto.Location;
import twetailer.dto.Store;
import twetailer.task.RobotResponder;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.StringUtils;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

/**
 * Control the access to third party services used to
 * validate location coordinates.
 *
 * @author Dom Derrien
 */
public class LocaleValidator {

    public static final String KILOMETER_UNIT = "km";
    public static final String MILE_UNIT = "mi";
    public static final String ALTERNATE_MILE_UNIT = "miles";
    public static final String DEFAULT_RANGE_UNIT = KILOMETER_UNIT;
    public static final Double DEFAULT_RANGE = 25.0D;

    /**
     * Use 3rd party service to resolve the geocoordinates of the given location
     *
     * @param command Parameters as received by the REST API
     * @return Pair of coordinates {latitude, longitude}
     */
    public static void getGeoCoordinates(JsonObject command) {
        String postalCode = command.getString(Location.POSTAL_CODE);
        String countryCode = command.getString(Location.COUNTRY_CODE);
        Double[] coordinates = getGeoCoordinates(postalCode, countryCode, null);
        command.put(Location.LATITUDE, coordinates[0]);
        command.put(Location.LONGITUDE, coordinates[1]);
    }

    /**
     * Use 3rd party service to resolve the geocoordinates of the given location
     *
     * @param location Information as received from the data store
     * @return Given parameter for operation chaining
     */
    public static Location getGeoCoordinates(Location location) {
        Double[] coordinates = getGeoCoordinates(location.getPostalCode(), location.getCountryCode(), location.getKey());
        location.setLatitude(coordinates[0]);
        location.setLongitude(coordinates[1]);
        return location;
    }

    /**
     * Use 3rd party service to resolve the geo-coordinates of the given location
     *
     * @param store Entity to localized
     * @return Given parameter for operation chaining
     */
    public static Store getGeoCoordinates(Store store) {
        Double[] coordinates = getGeoCoordinates(store.getAddress(), store.getKey());
        store.setLatitude(coordinates[0]);
        store.setLongitude(coordinates[1]);
        return store;
    }

    protected static Pattern CANADIAN_POSTAL_CODE_PATTERN = Pattern.compile("^\\w\\d\\w(?:\\s|-)?\\d\\w\\d$", Pattern.CASE_INSENSITIVE);
    protected static Pattern US_POSTAL_CODE_PATTERN = Pattern.compile("^\\d\\d\\d\\d\\d(?:-\\d\\d\\d\\d(?:\\d\\d)?:)?$", Pattern.CASE_INSENSITIVE);

    /**
     * Use 3rd party service to resolve the geocoordinates of the given location
     *
     * @param postalCode Postal code
     * @param countryCode Code of the country to consider
     * @param locationKey TODO
     * @return Pair of coordinates {latitude, longitude}
     */
    protected static Double[] getGeoCoordinates(String postalCode, String countryCode, Long locationKey) {
        Double[] coordinates = new Double[] {Location.INVALID_COORDINATE, Location.INVALID_COORDINATE};
        StringBuilder buffer = new StringBuilder();
        Exception ex = null;
        // Test case
        if (RobotResponder.ROBOT_POSTAL_CODE.equals(postalCode)) {
            coordinates[0] = 90.0D;
            coordinates[1] = 0.0D;
        }
        // Postal code in USA
        else if (Locale.US.getCountry().equals(countryCode) && US_POSTAL_CODE_PATTERN.matcher(postalCode).find()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getValidatorStream(postalCode, countryCode)));
                String line = reader.readLine();
                while (line != null) {
                    buffer.append(line);
                    line = reader.readLine();
                }
                reader.close();

                JsonObject info = new JsonParser(buffer.toString()).getJsonObject();
                info = info.getJsonArray("results").getJsonObject(0).getJsonObject("geometry").getJsonObject("location");
                coordinates[0] = info.getDouble("lat");
                coordinates[1] = info.getDouble("lng");
            }
            catch (Exception e) { ex = e; }
        }
        // Postal code in Canada
        else if (Locale.CANADA.getCountry().equals(countryCode) && CANADIAN_POSTAL_CODE_PATTERN.matcher(postalCode).find()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getValidatorStream(postalCode, countryCode)));
                String line = reader.readLine();
                while (line != null) {
                    buffer.append(line);
                    line = reader.readLine();
                }
                reader.close();

                JsonObject info = new JsonParser(buffer.toString()).getJsonObject();
                info = info.getJsonArray("results").getJsonObject(0).getJsonObject("geometry").getJsonObject("location");
                coordinates[0] = info.getDouble("lat");
                coordinates[1] = info.getDouble("lng");

            }
            catch (Exception e) { ex = e; }
        }
        if (coordinates[0] < -90.0d || 90.0d < coordinates[0] || coordinates[1] < -180.0d || 180.0d < coordinates[1]) {
            // Reset
            ex = new  IllegalArgumentException("Invalid coordinates: [" + coordinates[0] + "; " + coordinates[1] + "]") ;
            coordinates[0] = coordinates[1] = Location.INVALID_COORDINATE;
        }

        // TODO: remove this information logging
        try {
            MailConnector.reportErrorToAdmins(
                    null, // No From
                    "Retrieved coordinates for: " + postalCode + " " + countryCode,
                    "<p>*** Coordinates: {" + coordinates[0] + "; " + coordinates[1] + "} ***<br/>"
                    + "*** Update: http://anothersocialeconomy.appspot.com/_admin/monitoring.jsp?type=Location&key=" + locationKey + "<br/>"
                    + (ex == null ? "*** No exception, retrieval was OK ***<p>" : "*** Exception: " + ex.getMessage() + " ***<br/>*** Google maps response:\n" + buffer + "</p>")
            );
            if (ex != null) {
                ex.printStackTrace();
            }
        }
        catch (Exception ez) { } // Too bad...

        return coordinates;
    }

    private static MockInputStream testValidatorStream;

    /** Just for unit test purposes */
    public static void setMockValidatorStream(MockInputStream stream) {
        testValidatorStream = stream;
    }

    /**
     * Use 3rd party service to resolve the geocoordinates of the given location
     *
     * @param address To be localized
     * @param storeKey TODO
     * @return Pair of coordinates {latitude, longitude}
     */
    protected static Double[] getGeoCoordinates(String address, Long storeKey) {
        Double[] coordinates = new Double[] { Location.INVALID_COORDINATE, Location.INVALID_COORDINATE };
        StringBuilder buffer = new StringBuilder();
        Exception ex = null;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getValidatorStream(address)));
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                line = reader.readLine();
            }
            reader.close();

            JsonObject info = new JsonParser(buffer.toString()).getJsonObject();
            info = info.getJsonArray("results").getJsonObject(0).getJsonObject("geometry").getJsonObject("location");
            coordinates[0] = info.getDouble("lat");
            coordinates[1] = info.getDouble("lng");
        }
        catch (Exception e) { ex = e; }

        // TODO: remove this information logging
        try {
            MailConnector.reportErrorToAdmins(
                    null, // No From
                    "Retrieved coordinates for address: " + address,
                    "<p>*** Coordinates: {" + coordinates[0] + "; " + coordinates[1] + "} ***<br/>"
                    + "*** Update: http://anothersocialeconomy.appspot.com/_admin/monitoring.jsp?type=Store&key=" + storeKey + "<br/>"
                    + (ex == null ? "*** No exception, retrieval was OK ***<p>" : "*** Exception: " + ex.getMessage() + " ***<br/>*** Google maps response:\n" + buffer + "</p>")
            );
            if (ex != null) {
                ex.printStackTrace();
            }
        }
        catch (Exception ez) { } // Too bad...
        return coordinates;
    }

    /**
     * Contact 3rd party services to get information about the specified location
     *
     * @param postalCode Postal code
     * @param countryCode Code of the country to consider
     * @return Live input stream with information on the related location
     *
     * @throws IOException If the 3rd party service is not available or if there's no 3rd party service for the specified country
     */
    protected static InputStream getValidatorStream(String postalCode, String countryCode) throws IOException {
        if (testValidatorStream != null) {
            return testValidatorStream;
        }
        if (Locale.CANADA.getCountry().equals(countryCode)) {
            return new URL("https://maps-api-ssl.google.com/maps/api/geocode/json?v=3&sensor=false&language=en&address=" + postalCode + ",%20Canada").openStream();
            // return new URL("http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=" + postalCode + ",%20Canada").openStream();
            // return new URL("http://geocoder.ca/?geoit=xml&postal=" + postalCode).openStream();
        }
        if (Locale.US.getCountry().equals(countryCode)) {
            return new URL("https://maps-api-ssl.google.com/maps/api/geocode/json?v=3&sensor=false&language=en&address=" + postalCode + ",%20USA").openStream();
            // return new URL("http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=" + postalCode + ",%20USA").openStream();
            // return new URL("http://geocoder.us/service/csv/geocode?zip=" + postalCode).openStream();
        }
        throw new MalformedURLException("Unsupported coutry code: " + countryCode);
    }

    /**
     * Contact 3rd party services to get information about the specified location
     *
     * @param address To be geo-localized
     * @return Live input stream with information on the related location
     *
     * @throws IOException If the 3rd party service is not available or if there's no 3rd party service for the specified country
     * @throws URISyntaxException If the syntax of the URL is invalid
     */
    protected static InputStream getValidatorStream(String address) throws IOException, URISyntaxException {
        if (testValidatorStream != null) {
            return testValidatorStream;
        }
        address = URLEncoder.encode(address, StringUtils.JAVA_UTF8_CHARSET);
        Logger.getLogger(LocaleValidator.class.getName()).warning("Address to lookup: " + address);
        return new URL("https://maps-api-ssl.google.com/maps/api/geocode/json?v=3&sensor=false&language=fr&address=" + address).openStream();
        // return new URL("http://maps.google.com/maps/api/geocode/json?sensor=false&address=" + address).openStream();
    }

    public static final Locale DEFAULT_LOCALE = Locale.CANADA;
    public static final String DEFAULT_LANGUAGE = DEFAULT_LOCALE.getLanguage();
    public static final String DEFAULT_DISPLAY_LANGUAGE = DEFAULT_LOCALE.getDisplayLanguage(DEFAULT_LOCALE);
    public static final String DEFAULT_CURRENCY_CODE = DecimalFormatSymbols.getInstance(DEFAULT_LOCALE).getInternationalCurrencySymbol();

    private static final String FRENCH_LANGUAGE = Locale.FRENCH.getLanguage();
    private static final String ENGLISH_LANGUAGE = Locale.ENGLISH.getLanguage();

    /**
     * Verify that the given language is supported by the system
     *
     * @param language Language defined with the ISO 2-letters format
     * @return The given language if it's valid or the default one
     *
     * @see LocaleValidator#getLocale(String)
     */
    public static String checkLanguage(String language) {
        if (language != null && 2 < language.length()) {
            language = language.substring(0, 2);
        }
        if (FRENCH_LANGUAGE.equalsIgnoreCase(language)) { return FRENCH_LANGUAGE; }
        if (ENGLISH_LANGUAGE.equalsIgnoreCase(language)) { return ENGLISH_LANGUAGE; }
        return DEFAULT_LANGUAGE; // Default language
    }

    /**
     * Get the Locale instance matching the validated language
     *
     * @param language Language defined with the ISO 2-letters format
     * @return The Locale instance matching the given language if this one is valid, or the default one
     *
     * @see LocaleValidator#checkLanguage(String)
     */
    public static Locale getLocale(String language) {
        if (FRENCH_LANGUAGE.equalsIgnoreCase(language)) { return Locale.FRENCH; }
        else if (ENGLISH_LANGUAGE.equalsIgnoreCase(language)) { return Locale.ENGLISH; }
        return DEFAULT_LOCALE; // Default language
    }

    public static final String DEFAULT_COUNTRY_CODE = DEFAULT_LOCALE.getCountry();

    public static final String DEFAULT_POSTAL_CODE_CA = "H0H0H0";
    public static final String DEFAULT_POSTAL_CODE_US = "00000";
    public static final String DEFAULT_POSTAL_CODE_ALT_US = "00000-0000";
    /**
     * Clean-up the postal code format
     *
     * @param postalCode user input
     * @param countryCode used to guess which regular postal code format to apply
     * @return format for the back-end storage
     */
    public static String standardizePostalCode(String postalCode, String countryCode) {
        postalCode = postalCode.replaceAll("\\s", "").toUpperCase();
        if (Locale.CANADA.getCountry().equalsIgnoreCase(countryCode)) {
            postalCode = postalCode.replaceAll("\\-", "");
            int size = postalCode.length();
            if (size != 6) {
                return DEFAULT_POSTAL_CODE_CA;
            }
            char letter1 = postalCode.charAt(0);
            char letter2 = postalCode.charAt(1);
            char letter3 = postalCode.charAt(2);
            char letter4 = postalCode.charAt(3);
            char letter5 = postalCode.charAt(4);
            char letter6 = postalCode.charAt(5);
            if (letter1 < 'A' || 'Z' < letter1 ||
                letter2 < '0' || '9' < letter2 ||
                letter3 < 'A' || 'Z' < letter3 ||
                letter4 < '0' || '9' < letter4 ||
                letter5 < 'A' || 'Z' < letter5 ||
                letter6 < '0' || '9' < letter6
            ) {
                return DEFAULT_POSTAL_CODE_CA;
            }
            return postalCode;
        }
        else if (Locale.US.getCountry().equalsIgnoreCase(countryCode)) {
            int size = postalCode.length();
            while(0 < size && postalCode.charAt(size - 1) == '-') {
                postalCode = postalCode.substring(0, size -1);
                size --;
            }
            if (size != 5 && size != 10) {
                if (5 < size) {
                    return DEFAULT_POSTAL_CODE_ALT_US;
                }
                return DEFAULT_POSTAL_CODE_US;
            }
            char letter1 = postalCode.charAt(0);
            char letter2 = postalCode.charAt(1);
            char letter3 = postalCode.charAt(2);
            char letter4 = postalCode.charAt(3);
            char letter5 = postalCode.charAt(4);
            if (letter1 < '0' || '9' < letter1 ||
                letter2 < '0' || '9' < letter2 ||
                letter3 < '0' || '9' < letter3 ||
                letter4 < '0' || '9' < letter4 ||
                letter5 < '0' || '9' < letter5
            ) {
                if (size == 10) {
                    char letter6 = postalCode.charAt(5);
                    char letter7 = postalCode.charAt(6);
                    char letter8 = postalCode.charAt(7);
                    char letter9 = postalCode.charAt(8);
                    char letterA = postalCode.charAt(9);
                    if (letter6 != '-' ||
                        letter7 < '0' || '9' < letter7 ||
                        letter8 < '0' || '9' < letter8 ||
                        letter9 < '0' || '9' < letter9 ||
                        letterA < '0' || '9' < letterA
                    ) {
                        return DEFAULT_POSTAL_CODE_ALT_US;
                    }
                }
                return DEFAULT_POSTAL_CODE_US;
            }
            return postalCode;
        }
        char letter1 = postalCode.charAt(0);
        if (letter1 < 'A' || 'Z' < letter1) {
            postalCode = postalCode.replaceAll("\\-", "");
        }
        return postalCode;
    }

    /**
     * Verify that the given country code is supported by the system
     *
     * @param countryCode Country code to validate
     * @return The given country code if it's valid or the default one
     */
    public static String checkCountryCode(String countryCode) {
        //
        // It's expected that the country code respected the ISO 2-letters format
        //
        if (Locale.CANADA.getCountry().equalsIgnoreCase(countryCode)) { return Locale.CANADA.getCountry(); }
        if (Locale.US.getCountry().equalsIgnoreCase(countryCode)) { return Locale.US.getCountry(); }
        return DEFAULT_COUNTRY_CODE; // Default country code
    }

    /**
     * Return the localized country name for the given country code
     *
     * @param countryCode code, to be normalized, used to select the label to return
     * @param locale used to select in which localized resource bundle to load the label
     * @return Country name in the expected locale
     *
     * @see twetailer.validator.LocaleValidator#checkCountryCode(String)
     */
    public static String getCountryLabel(String countryCode, Locale locale) {
        // Normalization
        countryCode = checkCountryCode(countryCode);
        return LabelExtractor.get("country_" + countryCode, locale);
    }

    /**
     * Verify that the given unit is supported by the system
     *
     * @param rangeUnit Unit to validate
     * @return The given unit if it's valid or the default one
     */
    public static String checkRangeUnit(String rangeUnit) {
        if (LocaleValidator.MILE_UNIT.equalsIgnoreCase(rangeUnit) || LocaleValidator.ALTERNATE_MILE_UNIT.equalsIgnoreCase(rangeUnit)) {
            return LocaleValidator.MILE_UNIT;
        }
        if (LocaleValidator.KILOMETER_UNIT.equalsIgnoreCase(rangeUnit)) { return LocaleValidator.KILOMETER_UNIT; }
        return LocaleValidator.DEFAULT_RANGE_UNIT; // Default range unit
    }

    /**
     * Create a Collator instance for the given locale information.
     * This object can be used for locale dependent comparisons.
     *
     * @param locale Consumer's locale
     * @return Collator instance
     */
    public static Collator getCollator(Locale locale) {
        //
        // TODO: cache the value by user's locale
        //
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY);
        return collator;
    }

    private static Map<Locale, NumberFormat> numberFormats = new HashMap<Locale, NumberFormat>();

    /**
     * Helper formatting the given floating point number into a string with
     * two fractional digits
     *
     * @param number Entity to generate
     * @param locale Recipient's locale
     * @return Number with two fractional digits and separator in the user's locale
     */
    public static String formatFloatWith2Digits(double number, Locale locale) {
        NumberFormat numberFormat = numberFormats.get(locale);
        if (numberFormat == null) {
            numberFormat = NumberFormat.getNumberInstance(locale);
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setMinimumFractionDigits(2);
            numberFormats.put(locale, numberFormat);
        }
        return numberFormat.format(number);
    }
}
