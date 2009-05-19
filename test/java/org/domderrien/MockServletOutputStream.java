package org.domderrien;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class MockServletOutputStream extends ServletOutputStream {
    private StringBuffer stream = new StringBuffer();

    public boolean contains(String excerpt) {
        return stream.indexOf(excerpt) != -1;
    }

    public String getStream() {
        return stream.toString();
    }

    public int length() {
        return stream.length();
    }

    public void clear() {
        if (0 < stream.length()) {
            stream.delete(0, stream.length() - 1);
        }
    }

    @Override
    public void write(int b) throws IOException {
        stream.append((char) b);
    }
}
