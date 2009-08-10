package com.twetailer.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import domderrien.jsontools.JsonObject;

import com.twetailer.dto.Demand;

public class LocaleValidator {

    public static void getGeoCoordinates(JsonObject command) {
        String countryCode = command.getString(Demand.POSTAL_CODE);
        String postalCode = command.getString(Demand.COUNTRY_CODE);
        Double[] coordinates = getGeoCoordinates(postalCode, countryCode);
        command.put(Demand.LATITUDE, coordinates[0]);
        command.put(Demand.LONGITUDE, coordinates[1]);
    }
    
    public static void getGeoCoordinates(Demand demand) {
        String countryCode = demand.getPostalCode();
        String postalCode = demand.getCountryCode();
        Double[] coordinates = getGeoCoordinates(postalCode, countryCode);
        demand.setLatitude(coordinates[0]);
        demand.setLongitude(coordinates[1]);
    }
    
    protected static Double[] getGeoCoordinates(String postalCode, String countryCode) {
        Double[] coordinates = new Double[] {Demand.INVALID_COORDINATE, Demand.INVALID_COORDINATE};
        if (countryCode == "US") {
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
        }
        else if (countryCode == "CA") {
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
                    coordinates[0] = coordinates[1] = Demand.INVALID_COORDINATE;
                }
                reader.close();
    
            }
            catch (MalformedURLException e) { }
            catch (IOException e) { }
        }
        return coordinates;
    }

}
