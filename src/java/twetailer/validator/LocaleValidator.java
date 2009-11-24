package twetailer.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Logger;

import javamocks.io.MockInputStream;

import twetailer.dto.Location;
import twetailer.task.RobotResponder;
import domderrien.jsontools.JsonObject;

public class LocaleValidator {

    private static final Logger log = Logger.getLogger(LocaleValidator.class.getName());

    public static final String KILOMETER_UNIT = "km";
    public static final String MILE_UNIT = "mi";
    public static final String ALTERNATE_MILE_UNIT = "miles";
    public static final String DEFAULT_RANGE_UNIT = KILOMETER_UNIT;
    public static final Double DEFAULT_RANGE = 25.0D;

    public static void getGeoCoordinates(JsonObject command) {
        String postalCode = command.getString(Location.POSTAL_CODE);
        String countryCode = command.getString(Location.COUNTRY_CODE);
        Double[] coordinates = getGeoCoordinates(postalCode, countryCode);
        command.put(Location.LATITUDE, coordinates[0]);
        command.put(Location.LONGITUDE, coordinates[1]);
    }

    public static Location getGeoCoordinates(Location location) {
        Double[] coordinates = getGeoCoordinates(location.getPostalCode(), location.getCountryCode());
        location.setLatitude(coordinates[0]);
        location.setLongitude(coordinates[1]);
        return location;
    }

    protected static Double[] getGeoCoordinates(String postalCode, String countryCode) {
        Double[] coordinates = new Double[] {Location.INVALID_COORDINATE, Location.INVALID_COORDINATE};
        log.warning("Try to resolve: " + postalCode + " " + countryCode);
        // Test case
        if (RobotResponder.ROBOT_POSTAL_CODE.equals(postalCode)) {
            coordinates[0] = 90.0D;
            coordinates[1] = 0.0D;
        }
        // Postal code in USA
        else if (Locale.US.getCountry().equals(countryCode)) {
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
        else if (Locale.CANADA.getCountry().equals(countryCode)) {
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

    public static void setValidatorStream(InputStream stream) {
        testValidatorStream = stream;
    }

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

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static final String DEFAULT_LANGUAGE = DEFAULT_LOCALE.getLanguage();

    private static final String FRENCH_LANGUAGE = Locale.FRENCH.getLanguage();
    private static final String ENGLISH_LANGUAGE = Locale.ENGLISH.getLanguage();

    public static String checkLanguage(String language) {
        //
        // It's expected that the language code respected the ISO 2-letters format
        //
        if (FRENCH_LANGUAGE.equalsIgnoreCase(language)) { return FRENCH_LANGUAGE; }
        if (ENGLISH_LANGUAGE.equalsIgnoreCase(language)) { return ENGLISH_LANGUAGE; }
        return DEFAULT_LANGUAGE; // Default language
    }

    public static Locale getLocale(String language) {
        //
        // It's expected that the language code respected the ISO 2-letters format
        //
        if (FRENCH_LANGUAGE.equalsIgnoreCase(language)) { return Locale.FRENCH; }
        else if (ENGLISH_LANGUAGE.equalsIgnoreCase(language)) { return Locale.ENGLISH; }
        return DEFAULT_LOCALE; // Default language
    }

    public static final String DEFAULT_COUNTRY_CODE = Locale.US.getCountry();

    public static String checkCountryCode(String countryCode) {
        //
        // It's expected that the country code respected the ISO 2-letters format
        //
        if (Locale.CANADA.getCountry().equalsIgnoreCase(countryCode)) { return Locale.CANADA.getCountry(); }
        if (Locale.US.getCountry().equalsIgnoreCase(countryCode)) { return Locale.US.getCountry(); }
        return DEFAULT_COUNTRY_CODE; // Default country code
    }

}
