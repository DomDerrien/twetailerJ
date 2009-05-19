package org.domderrien;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a mock representation of InputStream to be used to test
 * the WAC code (JSONParser, WACAJAXService, etc.).
 */
public class MockInputStream extends InputStream {
    private StringBuilder stream;
    private int cursor = 0;
    private int limit;

    /** Default constructor. Use <code>reset(String)</code> to set the stream content. */
    public MockInputStream() {
        this("");
    }
    /** Constructor with the initial stream content */
    public MockInputStream(String data) {
        stream = new StringBuilder(data);
        limit = stream.length();
    }

    /** Clear stream content */
    public void clearActualBuffer() {
        stream.delete(0, stream.length());
    }
    /** Replace the stream content with the provided one */
    public void resetActualContent(String data) {
        clearActualBuffer();
        stream.append(data);
        cursor = 0;
        limit = stream.length();
    }

    /** Return one by one the stream characters */
    @Override
    public int read() throws IOException {
        if(cursor < limit) {
            char c = stream.charAt(cursor);
            ++ cursor;
            return (int) c;
        }
        return -1;
    }

    /** Return one by one the stream characters */
    @Override
    public int available() throws IOException {
        return limit - cursor;
    }

    /** Return the initial stream content */
    public String getContents() {
        return stream.toString();
    }
    /** Return the yet processed stream content */
    public String getProcessedContents() {
        return stream.substring(0, cursor - 1);
    }
    /** Return the non yet processed stream content */
    public String getNotProcessedContents() {
        return stream.substring(cursor);
    }
}
