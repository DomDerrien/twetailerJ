package org.domderrien;

import java.io.IOException;
import java.io.OutputStream;

public class MockOutputStream extends OutputStream {
	private StringBuilder stream = new StringBuilder();

	public MockOutputStream() {
	    this(0);
	}
	
	private int limit;
	
	public MockOutputStream(int limit) {
	    this.limit = limit; 
	}
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
			index = 0;
		}
	}

	private int index = 0;
	
	@Override
	public void write(int b) throws IOException {
	    if (index < limit) {
	        stream.append((char) b);
	        ++ index;
	    }
	}
}
