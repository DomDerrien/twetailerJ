package twetailer.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javamocks.io.MockInputStream;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

/**
 * Control the access to third party services used to
 * validate location coordinates.
 *
 * @author Dom Derrien
 */
public class UrlShortener {

    /**
     * Use 3rd party service to get a shortened URL
     *
     * @param url URL to process
     * @return Short Url
     *
     * @throws IOException
     * @throws JsonException
     */
    public static String getShortUrl(String url) throws IOException, JsonException {
        InputStream validatorStream = getValidatorStream(url);
        if (validatorStream == null) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(validatorStream));
        StringBuilder buffer = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            buffer.append(line);
            line = reader.readLine();
        }
        reader.close();

        JsonObject info = new JsonParser(buffer.toString()).getJsonObject();
        return info.getString("id");
    }

    private static MockInputStream testValidatorStream;

    /** Just for unit test purposes */
    public static void setMockValidatorStream(MockInputStream stream) {
        testValidatorStream = stream;
    }

    /**
     * Contact 3rd party services to get a shortened URL
     *
     * @return Live input stream with shortened URL
     *
     * @throws IOException If the 3rd party service is not available
     */
    protected static InputStream getValidatorStream(String longUrl) throws IOException {
        if (testValidatorStream != null) {
            return testValidatorStream;
        }
        URL url = new URL("https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyCKrAOrb0KI9gbCuVgTJFhlREYQSL9EIhg");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write("{\"longUrl\":\"" + longUrl + "\"}");
        writer.close();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return connection.getInputStream();
        }
        return null;
    }
}
