package com.twetailer.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import domderrien.jsontools.JsonObject;

import com.twetailer.dto.Location;

public class LocaleValidator {

    public static final String KILOMETER_UNIT = "km";
    public static final String MILE_UNIT = "mi";
    
    public static void getGeoCoordinates(JsonObject command) {
        String countryCode = command.getString(Location.POSTAL_CODE);
        String postalCode = command.getString(Location.COUNTRY_CODE);
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
        // Test case
        if ("H0H0H0".equals(postalCode)) {
            coordinates[0] = 90.0D;
            coordinates[1] = 0.0D;
            return coordinates;
        }
        // Postal code in USA
        if (Locale.US.equals(countryCode)) {
            // http://geocoder.us/service/csv/geocode?zip=95472
            try {
                URL url = new URL("http://geocoder.us/service/csv/geocode?zip=" + postalCode);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = reader.readLine(); // Only one line expected
                if (line != null && 0 < line.length()) {
                    String[] parts = line.split(",\\s*");
                    coordinates[0] = Double.valueOf(parts[0]);
                    coordinates[1] = Double.valueOf(parts[1]);
                }
                reader.close();
    
            }
            catch (MalformedURLException e) { }
            catch (IOException e) { }
            return coordinates;
        }
        // Postal code in Canada
        if (Locale.CANADA.equals(countryCode)) {
            // http://geocoder.ca/?geoit=xml&postal=h8p3r8
            try {
                URL url = new URL("http://geocoder.ca/?geoit=xml&postal=" + postalCode);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line; // Only one line expected
                while ((line = reader.readLine()) != null) {
                    if (line.indexOf("<latt>") != -1) {
                        coordinates[0] = Double.valueOf(line.substring(line.indexOf("<latt>") + "<latt>".length()));
                    }
                    if (line.indexOf("<longt>") != -1) {
                        coordinates[1] = Double.valueOf(line.substring(line.indexOf("<longt>") + "<longt>".length()));
                    }
                }
                if (90.0d < coordinates[1]) {
                    // Reset
                    coordinates[0] = coordinates[1] = Location.INVALID_COORDINATE;
                }
                reader.close();
    
            }
            catch (MalformedURLException e) { }
            catch (IOException e) { }
            return coordinates;
        }
        if (Locale.FRANCE.equals(countryCode)) {
        }
        return coordinates;
    }

}
