package twetailer.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.Locale;
import java.util.regex.Pattern;

import twetailer.dto.Location;
import twetailer.task.RobotResponder;
import domderrien.jsontools.JsonObject;

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
     * Use 3rd party service to resolve the geo-coordinates of the given location
     *
     * @param command Parameters as received by the REST API
     * @return Pair of coordinates {latitude, longitude}
     */
    public static void getGeoCoordinates(JsonObject command) {
        String postalCode = command.getString(Location.POSTAL_CODE);
        String countryCode = command.getString(Location.COUNTRY_CODE);
        Double[] coordinates = getGeoCoordinates(postalCode, countryCode);
        command.put(Location.LATITUDE, coordinates[0]);
        command.put(Location.LONGITUDE, coordinates[1]);
    }

    /**
     * Use 3rd party service to resolve the geo-coordinates of the given location
     *
     * @param location Information as received from the datastore
     * @return Pair of coordinates {latitude, longitude}
     */
    public static Location getGeoCoordinates(Location location) {
        Double[] coordinates = getGeoCoordinates(location.getPostalCode(), location.getCountryCode());
        location.setLatitude(coordinates[0]);
        location.setLongitude(coordinates[1]);
        return location;
    }

    protected static Pattern CANADIAN_POSTAL_CODE_PATTERN = Pattern.compile("^\\w\\d\\w(?:\\s|-)?\\d\\w\\d$", Pattern.CASE_INSENSITIVE);
    protected static Pattern US_POSTAL_CODE_PATTERN = Pattern.compile("^\\d\\d\\d\\d\\d(?:-\\d\\d\\d\\d(?:\\d\\d)?:)?$", Pattern.CASE_INSENSITIVE);

    /**
     * Use 3rd party service to resolve the geo-coordinates of the given location
     *
     * @param postalCode Postal code
     * @param countryCode Code of the country to consider
     * @return Pair of coordinates {latitude, longitude}
     */
    protected static Double[] getGeoCoordinates(String postalCode, String countryCode) {
        Double[] coordinates = new Double[] {Location.INVALID_COORDINATE, Location.INVALID_COORDINATE};
        // Test case
        if (RobotResponder.ROBOT_POSTAL_CODE.equals(postalCode)) {
            coordinates[0] = 90.0D;
            coordinates[1] = 0.0D;
        }
        // Postal code in USA
        else if (Locale.US.getCountry().equals(countryCode) && US_POSTAL_CODE_PATTERN.matcher(postalCode).find()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getValidatorStream(postalCode, countryCode)));
                String line = reader.readLine(); // Only one line expected
                if (line != null) {
                    String[] parts = line.split(",\\s*");
                    coordinates[0] = Double.valueOf(parts[0].trim());
                    coordinates[1] = Double.valueOf(parts[1].trim());
                }
                reader.close();

            }
            catch (IOException e) { }
        }
        // Postal code in Canada
        else if (Locale.CANADA.getCountry().equals(countryCode) && CANADIAN_POSTAL_CODE_PATTERN.matcher(postalCode).find()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getValidatorStream(postalCode, countryCode)));
                // Manual parsing
                String line = reader.readLine();
                while (line != null) {
                    if (line.indexOf("<error>") != -1) {
                        break;
                    }
                    if (line.indexOf("<latt>") != -1) {
                        coordinates[0] = Double.valueOf(line.substring(line.indexOf("<latt>") + "<latt>".length(), line.indexOf("</latt>")));
                    }
                    else if (line.indexOf("<longt>") != -1) {
                        coordinates[1] = Double.valueOf(line.substring(line.indexOf("<longt>") + "<longt>".length(), line.indexOf("</longt>")));
                    }
                    line = reader.readLine();
                }
                reader.close();
                /* Automated parsing
                import javax.xml.parsers.DocumentBuilder;
                import javax.xml.parsers.DocumentBuilderFactory;
                import javax.xml.parsers.ParserConfigurationException;
                import javax.xml.xpath.XPath;
                import javax.xml.xpath.XPathConstants;
                import javax.xml.xpath.XPathExpression;
                import javax.xml.xpath.XPathExpressionException;
                import javax.xml.xpath.XPathFactory;

                import org.w3c.dom.Document;
                import org.w3c.dom.NodeList;
                import org.xml.sax.InputSource;
                import org.xml.sax.SAXException;

                StringBuilder bufferedResponse = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    bufferedResponse.append(line);
                    line = reader.readLine();
                }
                reader.close();
                try {
                    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = builderFactory.newDocumentBuilder();
                    Document doc = builder.parse(new InputSource(new StringReader(bufferedResponse.toString())));

                    XPathFactory factory = XPathFactory.newInstance();
                    XPath xpath = factory.newXPath();
                    XPathExpression expr = xpath.compile("//geodata]/latt/text()");

                    Object result = expr.evaluate(doc, XPathConstants.NODESET);
                    NodeList nodes = (NodeList) result;

                    if (0 < nodes.getLength()) {
                        coordinates[0] = Double.valueOf(nodes.item(0).getNodeValue());
                    }

                    expr = xpath.compile("//geodata]/longt/text()");

                    result = expr.evaluate(doc, XPathConstants.NODESET);
                    nodes = (NodeList) result;

                    if (0 < nodes.getLength()) {
                        coordinates[1] = Double.valueOf(nodes.item(0).getNodeValue());
                    }
                }
                catch(ParserConfigurationException ex) { }
                catch(SAXException ex) { }
                catch(XPathExpressionException ex) { }
                */
            }
            catch (IOException e) { }
        }
        if (coordinates[0] < -90.0d || 90.0d < coordinates[0] || coordinates[1] < -180.0d || 180.0d < coordinates[1]) {
            // Reset
            coordinates[0] = coordinates[1] = Location.INVALID_COORDINATE;
        }
        return coordinates;
    }

    private static InputStream testValidatorStream;

    /** Just for unit test purposes */
    public static void setValidatorStream(InputStream stream) {
        testValidatorStream = stream;
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
            return new URL("http://geocoder.ca/?geoit=xml&postal=" + postalCode).openStream();
        }
        if (Locale.US.getCountry().equals(countryCode)) {
            return new URL("http://geocoder.us/service/csv/geocode?zip=" + postalCode).openStream();
        }
        throw new MalformedURLException("Unsupported coutry code: " + countryCode);
    }

    public static final Locale DEFAULT_LOCALE = Locale.US;
    public static final String DEFAULT_LANGUAGE = DEFAULT_LOCALE.getLanguage();
    public static final String DEFAULT_DISPLAY_LANGUAGE = DEFAULT_LOCALE.getDisplayLanguage(DEFAULT_LOCALE);

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

    public static final String DEFAULT_COUNTRY_CODE = Locale.US.getCountry();

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
     * Transform the UTF-8 string in its Unicode counterpart
     *
     * @param utf8Str Original string
     * @return Converted string if there's no error, the original value otherwise
     */
    public static String toUnicode(String utf8Str) {
        String out = utf8Str;
        try {
            out = new String(utf8Str.getBytes(), "UTF8");
        }
        catch (UnsupportedEncodingException e) {
            // Note for the testers:
            //   UnsupportedEncodingException can be generated if the character set would be invalid (instead of "UTF8")
            //   Not a possible use case here...
        }
        return out;
    }

    /**
     * Transform the Unicode string in its UTF-8 counterpart
     *
     * @param unicodeStr Original string
     * @return Converted string if there's no error, the original value otherwise
     */
    public static String toUTF8(String unicodeStr) {
        String out = unicodeStr;
        try {
            out = new String(unicodeStr.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            // Note for the testers:
            //   UnsupportedEncodingException can be generated if the character set would be invalid (instead of "UTF8")
            //   Not a possible use case here...
        }
        return out;
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
}
