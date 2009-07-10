package org.domderrien.jsontools;

import java.io.IOException;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for data extraction from a JSON string.
 * Format of JSON string is defined on <a href="http://json.org">json.org</a>.
 * <p>
 * JSON grammar very simple. It is built only two structures:<ul>
 *   <li>A collection of {key: value} pairs (separated by a comma, delimited by braces);
 *   <li>An ordered list of values (separated by a comma, delimited by square brackets).</ul>
 * A value can be a <code>number</code>, a <code>string</code>, a
 * <code>boolean</code>, <code>null</code> or missing, <code>collection</code>
 * or an <code>ordered list</code>.
 * <p>
 * In Java, <code>numbers</code> are mapped to <code>Double</code> instances and
 * the tokenizer offers accessor to manipulate the primitives <code>int</code>,
 * <code>long</code>, <code>float</code>, and  <code>double</code>. <code>Booleans
 * </code> are mapped to <code>Boolean</code> instances and their is an accessor to
 * get a <code>boolean</code> primitive. <code>Collections</code> are mapped to <code>
 * {@link GenericJsonObject}</code> instances. <code>Ordered lists</code> are mapped to <code>
 * {@link GenericJsonArray}</code> instances.
 * <p>
 * The algorithm is usable as a recursive descent parser, but instead of discovering the
 * delimiters while the string is parsed, there is a preliminary parsing of the list
 * identifying the delimiters and normalizing the string (controls '\t', '\r', and '\n'
 * characters and detects JavaScript comments). This first parsing is iterative, and
 * many syntax errors can be reported very quickly (that is before the heavier recursive
 * parsing). This first parsing is done automatically by the constructor.
 * @author Dom Derrien
 * @version 1.03
 */
public class JsonParser {

    private static Logger s_logger;
    private CharStream stream;

    /** Simple stream-like class implementing peek() and get() methods */
    protected class CharStream {
        public CharStream(String stream) throws JsonException {
            string = new StringBuilder(stream == null ? "" : stream);
            index = 0;
            limit = string.length();
            if (limit == 0) {
                throw new JsonException("INVALID_JSON_STREAM", "JSON bag cannot be empty");
            }
        }
        public CharStream(InputStream stream) throws JsonException {
            string = new StringBuilder();
            try {
                while (true) {
                    int character = stream.read();
                    if(character == -1) break;
                    string.append((char) character);
                }
            }
            catch (IOException e) {
                throw new JsonException("INVALID_JSON_STREAM", "Context: " + string.toString());
            }
            index = 0;
            limit = string.length();
            if (limit == 0) {
                throw new JsonException("INVALID_JSON_STREAM", "JSON bag cannot be empty");
            }
        }
        private int index;
        private int limit;
        private StringBuilder string;

        public char get(boolean skipSeparators) {
            char c = peek(skipSeparators);
            index++; // No limit check required because peek() takes care of it
            return c;
        }
        public char peek(boolean skipSeparators) {
            if (skipSeparators) {
                while (index < limit && isSeparatorChar(string.charAt(index))) {
                    ++ index;
                }
            }
            if(index >= limit) return JsonDelimiters.END_OF_STRING;
            return string.charAt(index);
        }
    }

    /**
     * Load the data from the input stream. Made available to read
     * the JSON bag from the servlet input stream
     * @param json InputStream to be provided by a servlet
     * @throws JsonException If the stream reading fails or if the buffer is empty
     */
    public JsonParser(InputStream json) throws JsonException {
        stream = new CharStream(json);
        s_logger = Logger.getLogger(JsonParser.class.getName());
    }

    /**
     * Use the given string as an input stream. Made available to
     * read the JSON bag from a servlet parameter.
     * @param json String with a serialized JSON bag
     * @throws JsonException If the buffer is empty
     */
    public JsonParser(String json) throws JsonException {
        this(json, Logger.getLogger(JsonParser.class.getName()));
    }

    /**
     * Use the given string as an input stream. Conveniently provided for the unit testing.
     * @param json String with a serialized JSON bag
     * @param logger Logger to use internally -- To be replaced by a mock object during unit testing
     * @throws JsonException If the buffer is empty
     */
    protected JsonParser(String json, Logger logger) throws JsonException {
        stream = new CharStream(json);
        s_logger = logger;
    }

    /**
     * Return the loaded stream
     * @return Serialized stream
     */
    public CharStream getStream() {
    	return stream;
    }

    /**
     * Return the value from the stream if it is delimited by quotes
     * @return Value without the quotes
     * @throws JsonException If the format is not correct
     */
    public String getString() throws JsonException {
        String obj = parseString();
        match(JsonDelimiters.END_OF_STRING);
        return obj;
    }

    /**
     * Return the number from the stream if it has a valid format
     * @return Value converted in a <code>Double</code> instance
     * @throws JsonException If the format is not correct
     */
    public Double getNumber() throws JsonException {
        Double obj = parseNumber();
        match(JsonDelimiters.END_OF_STRING);
        return obj;
    }

    /**
     * Return the value from the stream if it is <code>true</code> or <code>false</code>
     * @return Value converted in a <code>Boolean</code> instance
     * @throws JsonException If the format is not correct
     */
    public Boolean getBoolean() throws JsonException {
        Boolean obj = parseBoolean();
        match(JsonDelimiters.END_OF_STRING);
        return obj;
    }

    /**
     * Return the value from the stream if it is delimited by braces, and if the format "key:value" is repected.
     * The parser being recursive, the values in the JsonObject can be {string, boolean, number, JsonObject, JsonArray}.
     * @return Value converted in a <code>JsonObject</code> instance
     * @throws JsonException If the format is not correct
     */
    public JsonObject getJsonObject() throws JsonException {
        JsonObject obj = parseJsonObject();
        match(JsonDelimiters.END_OF_STRING);
        return obj;
    }

    /**
     * Return the value from the stream if it is delimited by brackets, and contained values are separated by a comma.
     * The parser being recursive, the values in the JsonArray can be {string, boolean, number, JsonObject, JsonArray}.
     * @return Value converted in a <code>JsonArray</code> instance
     * @throws JsonException If the format is not correct
     */
    public JsonArray getJsonArray() throws JsonException {
        JsonArray obj = parseJsonArray();
        match(JsonDelimiters.END_OF_STRING);
        return obj;
    }

    protected Object getObject() throws JsonException {
        Object obj = parseValue();
        match(JsonDelimiters.END_OF_STRING);
        return obj;
    }

    protected JsonObject parseJsonObject() throws JsonException {
        GenericJsonObject jsonObject = new GenericJsonObject();

        match(JsonDelimiters.OPENING_BRACE);
        while(stream.peek(true)!=JsonDelimiters.CLOSING_BRACE && stream.peek(true)!=JsonDelimiters.END_OF_STRING) {
            // Get key
            String key = parseString();
            match(JsonDelimiters.COLONS);

            // Get value & store
            jsonObject.putObject(key, parseValue());

            // Skip comma, if present
            if(stream.peek(true)!=JsonDelimiters.CLOSING_BRACE) {
                match(JsonDelimiters.COMMA);
                if(stream.peek(true)==JsonDelimiters.CLOSING_BRACE) reportError("Trailing comma in object");
            }
        }
        match(JsonDelimiters.CLOSING_BRACE);

        return jsonObject;
    }

    protected JsonArray parseJsonArray() throws JsonException {
        GenericJsonArray transferArray = new GenericJsonArray();

        match(JsonDelimiters.OPENING_SQUARE_BRACKET);
        while(stream.peek(true)!=JsonDelimiters.CLOSING_SQUARE_BRACKET && stream.peek(true)!=JsonDelimiters.END_OF_STRING) {
            // Get value & store
            transferArray.addObject( parseValue() );

            // Skip comma, if present
            if(stream.peek(true)!=JsonDelimiters.CLOSING_SQUARE_BRACKET) {
                match(JsonDelimiters.COMMA);
                if(stream.peek(true)==JsonDelimiters.CLOSING_SQUARE_BRACKET) reportError("Trailing comma in array");
            }
        }
        match(JsonDelimiters.CLOSING_SQUARE_BRACKET);

        return transferArray;
    }

    protected Object parseValue() throws JsonException {
        Object obj = null;
        switch(stream.peek(true)) {
            case JsonDelimiters.QUOTE:
            case JsonDelimiters.DOUBLE_QUOTES:
                obj = parseString();
                s_logger.finest("String extracted: " + obj);
                break;
            case JsonDelimiters.OPENING_BRACE:
                obj = parseJsonObject();
                s_logger.finest("<GenericJsonObject> extracted");
                break;
            case JsonDelimiters.OPENING_SQUARE_BRACKET:
                obj = parseJsonArray();
                s_logger.finest("<GenericJsonArray> extracted");
                break;
            case JsonDelimiters.TRUE_LABEL_FIRST_CHAR:
            case JsonDelimiters.FALSE_LABEL_FIRST_CHAR:
                obj = parseBoolean();
                s_logger.finest("Boolean extracted: " + obj);
                break;
            case JsonDelimiters.NULL_LABEL_FIRST_CHAR:
                for (int i = 0; i < JsonDelimiters.NULL_LABEL.length(); ++i) {
                    match(JsonDelimiters.NULL_LABEL.charAt(i));
                }
                s_logger.finest("<null> extracted");
                break;
            case JsonDelimiters.UNDEFINED_LABEL_FIRST_CHAR:
                for (int i = 0; i < JsonDelimiters.UNDEFINED_LABEL.length(); ++i) {
                    match(JsonDelimiters.UNDEFINED_LABEL.charAt(i));
                }
                s_logger.finest("<undefined> extracted");
                break;
            default: // assume it's a number
                obj = parseNumber();
                s_logger.finest("Number extracted: " + obj);
        }
        return obj;
    }

    protected String parseString() throws JsonException {
        StringBuilder str = new StringBuilder();
        char stringDelimiter = stream.peek(false);
        match(stringDelimiter);
        while(stream.peek(false)!=stringDelimiter && stream.peek(false)!=JsonDelimiters.END_OF_STRING) {
            char c = stream.get(false);
            if(c==JsonDelimiters.BACK_SLASH) {
                char next = stream.get(false);
                char unescaped;
                switch(next) {
                    case JsonDelimiters.QUOTE:              unescaped=JsonDelimiters.QUOTE; break;
                    case JsonDelimiters.DOUBLE_QUOTES:      unescaped=JsonDelimiters.DOUBLE_QUOTES; break;
                    case JsonDelimiters.BACK_SLASH:         unescaped=JsonDelimiters.BACK_SLASH; break;
                    case JsonDelimiters.SLASH:              unescaped=JsonDelimiters.SLASH; break;
                    case JsonDelimiters.BELL_ID:            unescaped=JsonDelimiters.BELL; break;
                    case JsonDelimiters.FORM_FEED_ID:       unescaped=JsonDelimiters.FORM_FEED; break;
                    case JsonDelimiters.NEW_LINE_ID:        unescaped=JsonDelimiters.NEW_LINE; break;
                    case JsonDelimiters.CARRIAGE_RETURN_ID: unescaped=JsonDelimiters.CARRIAGE_RETURN; break;
                    case JsonDelimiters.TABULATION_ID:      unescaped=JsonDelimiters.TABULATION; break;
                    case JsonDelimiters.UNICODE_ID:   //TODO
                        // (for now, fall through to an error)
                    default:
                        unescaped = JsonDelimiters.END_OF_STRING; //makes javac happy
                        reportError("After " + JsonDelimiters.BACK_SLASH + ", unexpected character " + next + "(" + ((int) next) + ")");
                }
                str.append(unescaped);
            } else {
                str.append(c);
            }
        }
        match(stringDelimiter);

        return str.toString();
    }

    protected Double parseNumber() throws JsonException {
        StringBuilder str = new StringBuilder();
        while(true) {
            char c = stream.peek(false);
            if (!isNumberChar(c)) {
                break;
            }
            str.append(c);
            stream.get(false); // advance the cursor to peek the next character
        }

        Double obj = null;
        try {
            obj = Double.parseDouble(str.toString());
        }
        catch(NumberFormatException e) {
            reportError("Unable to parse number: "+str);
        }
        return obj;
    }

    protected Boolean parseBoolean() throws JsonException {
        Boolean obj = null;
        char c = stream.peek(false);
        switch(c){
            case JsonDelimiters.TRUE_LABEL_FIRST_CHAR:
                for (int i = 0; i < JsonDelimiters.TRUE_LABEL.length(); ++i) {
                    match(JsonDelimiters.TRUE_LABEL.charAt(i));
                }
                obj = Boolean.TRUE;
                break;
            case JsonDelimiters.FALSE_LABEL_FIRST_CHAR:
                for (int i = 0; i < JsonDelimiters.FALSE_LABEL.length(); ++i) {
                    match(JsonDelimiters.FALSE_LABEL.charAt(i));
                }
                obj = Boolean.FALSE;
                break;
        }
        return obj;
    }

    protected void match(char next) throws JsonException {
        String message = "Expected " + next;
        if (s_logger.isLoggable(Level.FINEST)) {
            message += ". Context: cursor being at position "+stream.index+" in the stream : ";
            int lowestLimit = 0 < stream.index - 32 ? stream.index - 32 : 0;
            int highestLimit = stream.index + 32 < stream.limit ? stream.index + 32 : stream.limit;
            message += stream.string.substring(lowestLimit, highestLimit);
        }
        if(stream.get(true)!=next) reportError(message);
    }

    protected static boolean isSeparatorChar(char c) {
        return c==JsonDelimiters.SPACE || c==JsonDelimiters.TABULATION || c==JsonDelimiters.CARRIAGE_RETURN || c==JsonDelimiters.NEW_LINE;
    }

    protected static boolean isNumberChar(char c) {
        return Character.isDigit(c) || c=='.' || c=='-' || c=='+' || c=='e' || c=='E';
    }

    protected static void reportError(String message) throws JsonException {
        throw new JsonException("INVALID_JSON_STREAM", message);
    }
}
