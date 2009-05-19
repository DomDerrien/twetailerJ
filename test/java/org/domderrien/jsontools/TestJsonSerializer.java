package org.domderrien.jsontools;

import static org.junit.Assert.*;

import java.io.IOException;

import org.domderrien.MockLogger;
import org.domderrien.MockOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJsonSerializer {

	MockLogger logger = new MockLogger("test", null);
	MockOutputStream stream = new MockOutputStream();

	@Before
	public void setUp() throws Exception {
		stream.clear();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
    public void testConstructorI() throws IOException {
        new JsonSerializer();
    }

	@Test
    public void testGetBytesI() throws IOException {
        assertEquals(0, JsonSerializer.getBytes("").length);
    }

	@Test
    public void testGetBytesII() throws IOException {
        assertEquals(5, JsonSerializer.getBytes("abcde").length);
    }

	@Test
    public void testGetBytesIII() throws IOException {
        assertEquals(20, JsonSerializer.getBytes("àéìôü").length);
    }

	@Test
    public void testGetBytesIV() throws IOException {
        assertEquals(2, JsonSerializer.getBytes("\u0234").length);
    }

	@Test
    public void testEscapeI() throws IOException {
        assertEquals(4, JsonSerializer.escape(null).length());
        assertEquals("null", JsonSerializer.escape(null));
    }

	@Test
    public void testEscapeII() throws IOException {
        assertEquals(0, JsonSerializer.escape("").length());
    }

	@Test
    public void testEscapeIII() throws IOException {
        assertEquals(5, JsonSerializer.escape("abcde").length());
    }

	@Test
    public void testEscapeIV() throws IOException {
        assertEquals(6, JsonSerializer.escape("'\\\'").length());
        assertEquals("\\'\\\\\\'", JsonSerializer.escape("'\\\'") 	);
    }

	@Test(expected=IOException.class)
    public void testSerializerWithCorruptedInputStream() throws IOException {
        String data = "---";
        JsonSerializer.toStream(data, new MockOutputStream() {
            @Override
            public void write(int i) throws java.io.IOException {
                throw new IOException("done in purpose");
            }
        }, false);
    }

	@Test
    public void testToStreamBooleanI() throws IOException {
        JsonSerializer.toStream(Boolean.TRUE.booleanValue(), stream, false);
        assertEquals("true", stream.getStream());
    }

	@Test
    public void testToStreamBooleanII() throws IOException {
        JsonSerializer.toStream(Boolean.FALSE.booleanValue(), stream, false);
        assertEquals("false", stream.getStream());
    }
	
	@Test
    public void testToStreamBooleanIII() throws IOException {
        JsonSerializer.toStream(Boolean.TRUE.booleanValue(), stream, true);
        assertEquals("true,", stream.getStream());
    }

	@Test
    public void testToStreamBooleanIV() throws IOException {
        JsonSerializer.toStream(Boolean.FALSE.booleanValue(), stream, true);
        assertEquals("false,", stream.getStream());
    }

	@Test
    public void testToStreamKeyBooleanI() throws IOException {
        JsonSerializer.toStream("key", Boolean.TRUE.booleanValue(), stream, false);
        assertEquals("'key':true", stream.getStream());
    }

	@Test
    public void testToStreamKeyBooleanII() throws IOException {
        JsonSerializer.toStream("key", Boolean.FALSE.booleanValue(), stream, false);
        assertEquals("'key':false", stream.getStream());
    }
	
	@Test
    public void testToStreamKeyBooleanIII() throws IOException {
        JsonSerializer.toStream("key", Boolean.TRUE.booleanValue(), stream, true);
        assertEquals("'key':true,", stream.getStream());
    }

	@Test
    public void testToStreamKeyBooleanIV() throws IOException {
        JsonSerializer.toStream("key", Boolean.FALSE.booleanValue(), stream, true);
        assertEquals("'key':false,", stream.getStream());
    }

	@Test
    public void testToStreamNumberI() throws IOException {
        JsonSerializer.toStream(1l, stream, false);
        assertEquals("1", stream.getStream());
    }

	@Test
    public void testToStreamNumberII() throws IOException {
        JsonSerializer.toStream(12345l, stream, false);
        assertEquals("12345", stream.getStream());
    }

	@Test
    public void testToStreamNumberIII() throws IOException {
        JsonSerializer.toStream(12.345d, stream, false);
        assertEquals("12.345", stream.getStream());
    }

	@Test
    public void testToStreamNumberIV() throws IOException {
        JsonSerializer.toStream(1.2345E12d, stream, false);
        assertEquals("1.2345E12", stream.getStream());
    }

	@Test
    public void testToStreamNumberV() throws IOException {
        JsonSerializer.toStream(1l, stream, true);
        assertEquals("1,", stream.getStream());
    }

	@Test
    public void testToStreamNumberVI() throws IOException {
        JsonSerializer.toStream(12.345d, stream, true);
        assertEquals("12.345,", stream.getStream());
    }

	@Test
    public void testToStreamKeyNumberI() throws IOException {
        JsonSerializer.toStream("key", 1l, stream, false);
        assertEquals("'key':1", stream.getStream());
    }

	@Test
    public void testToStreamKeyNumberII() throws IOException {
        JsonSerializer.toStream("key", 12345l, stream, false);
        assertEquals("'key':12345", stream.getStream());
    }

	@Test
    public void testToStreamKeyNumberIII() throws IOException {
        JsonSerializer.toStream("key", 12.345d, stream, false);
        assertEquals("'key':12.345", stream.getStream());
    }

	@Test
    public void testToStreamKeyNumberIV() throws IOException {
        JsonSerializer.toStream("key", 1.2345E12d, stream, false);
        assertEquals("'key':1.2345E12", stream.getStream());
    }

	@Test
    public void testToStreamKeyNumberV() throws IOException {
        JsonSerializer.toStream("key", 1l, stream, true);
        assertEquals("'key':1,", stream.getStream());
    }

	@Test
    public void testToStreamKeyNumberVI() throws IOException {
        JsonSerializer.toStream("key", 12.345d, stream, true);
        assertEquals("'key':12.345,", stream.getStream());
    }

	@Test
    public void testToStreamStringI() throws IOException {
        JsonSerializer.toStream(null, stream, false);
        assertEquals("'null'", stream.getStream());
    }

	@Test
    public void testToStreamStringII() throws IOException {
        JsonSerializer.toStream("", stream, false);
        assertEquals("''", stream.getStream());
    }

	@Test
    public void testToStreamStringIII() throws IOException {
        JsonSerializer.toStream("test message", stream, false);
        assertEquals("'test message'", stream.getStream());
    }

	@Test
    public void testToStreamStringIV() throws IOException {
        JsonSerializer.toStream("test message", stream, true);
        assertEquals("'test message',", stream.getStream());
    }

	@Test
    public void testToStreamStringV() throws IOException {
        JsonSerializer.toStream("\"", stream, false);
        assertEquals("'\"'", stream.getStream());
    }

	@Test
    public void testToStreamKeyStringI() throws IOException {
        JsonSerializer.toStream("key", null, stream, false);
        assertEquals("'key':'null'", stream.getStream());
    }

	@Test
    public void testToStreamKeyStringII() throws IOException {
        JsonSerializer.toStream("key", "", stream, false);
        assertEquals("'key':''", stream.getStream());
    }

	@Test
    public void testToStreamKeyStringIII() throws IOException {
        JsonSerializer.toStream("key", "test message", stream, false);
        assertEquals("'key':'test message'", stream.getStream());
    }

	@Test
    public void testToStreamKeyStringIV() throws IOException {
        JsonSerializer.toStream("key", "test message", stream, true);
        assertEquals("'key':'test message',", stream.getStream());
    }

	@Test
    public void testToStreamKeyStringV() throws IOException {
        JsonSerializer.toStream("key", "\"", stream, false);
        assertEquals("'key':'\"'", stream.getStream());
    }

	@Test
    public void testStartObjectI() throws IOException {
        JsonSerializer.startObject(stream);
        assertEquals("{", stream.getStream());
    }

	@Test
    public void testStartObjectII() throws IOException {
        JsonSerializer.startObject("key", "value", stream, false);
        assertEquals("{'key':'value'", stream.getStream());
    }

	@Test
    public void testStartObjectIII() throws IOException {
        JsonSerializer.startObject("key", true, stream, false);
        assertEquals("{'key':true", stream.getStream());
    }

	@Test
    public void testStartObjectIV() throws IOException {
        JsonSerializer.startObject("key", 1l, stream, false);
        assertEquals("{'key':1", stream.getStream());
    }

	@Test
    public void testStartObjectV() throws IOException {
        JsonSerializer.startObject("key", 1d, stream, false);
        assertEquals("{'key':1.0", stream.getStream());
    }

	@Test
    public void testEndObjectI() throws IOException {
        JsonSerializer.endObject(stream, false);
        assertEquals("}", stream.getStream());
    }

	@Test
    public void testEndObjectII() throws IOException {
        JsonSerializer.endObject(stream, true);
        assertEquals("},", stream.getStream());
    }

	@Test
    public void testEndObjectIII() throws IOException {
        JsonSerializer.endObject("key", "value", stream, false);
        assertEquals("'key':'value'}", stream.getStream());
    }

	@Test
    public void testEndObjectIV() throws IOException {
        JsonSerializer.endObject("key", true, stream, false);
        assertEquals("'key':true}", stream.getStream());
    }

	@Test
    public void testEndObjectV() throws IOException {
        JsonSerializer.endObject("key", 1l, stream, false);
        assertEquals("'key':1}", stream.getStream());
    }

	@Test
    public void testEndObjectVI() throws IOException {
        JsonSerializer.endObject("key", 1d, stream, false);
        assertEquals("'key':1.0}", stream.getStream());
    }

	@Test
    public void testStartArrayI() throws IOException {
        JsonSerializer.startArray(stream);
        assertEquals("[", stream.getStream());
    }

	@Test
    public void testEndArrayI() throws IOException {
        JsonSerializer.endArray(stream, false);
        assertEquals("]", stream.getStream());
    }

	@Test
    public void testEndArrayII() throws IOException {
        JsonSerializer.endArray(stream, true);
        assertEquals("],", stream.getStream());
    }

	@Test
    public void testIntroduceComplexValueI() throws IOException {
        JsonSerializer.introduceComplexValue("key", stream);
        assertEquals("'key':", stream.getStream());
    }

	/*
	@Test
    public void testToStreamJsonObject() {
        try {
            stream.clearActualBuffer();
            JsonSerializer.toStream(new GenericJsonObject(), stream);
            assertEquals("Should be empty JSON Object", "{}", stream.getStream());

            stream.clearActualBuffer();
            GenericJsonObject o1 = new GenericJsonObject();
            o1.putArbitrary("a", true);
            o1.putArbitrary("b", 123);
            o1.putArbitrary("c", "test message");
            GenericJsonObject o2 = new GenericJsonObject();
            o2.putArbitrary("e", false);
            o1.putArbitrary("d", o2);
            GenericJsonArray o3 = new GenericJsonArray();
            o3.add(false);
            o1.putArbitrary("f", o3);
            JsonSerializer.toStream(o1, stream);
            String outputStream = stream.getStream();
            assertTrue("Should contain a boolean", outputStream.contains("\"a\":true"));
            assertTrue("Should contain a number", outputStream.contains("\"b\":123"));
            assertTrue("Should contain a string", outputStream.contains("\"c\":\"test message\""));
            assertTrue("Should contain a simple GenericJsonObject definition", outputStream.contains("\"d\":{\"e\":false}"));
            assertTrue("Should contain a simple GenericJsonArray definition", outputStream.contains("\"f\":[false]"));
        }
        catch(JsonException e) {
            fail("No JsonException should have been thrown");
        }
    }

	@Test
    public void testToStreamJsonArray() {
        try {
            stream.clearActualBuffer();
            JsonSerializer.toStream(new GenericJsonArray(), stream);
            assertEquals("Should be empty JSON Array", "[]", stream.getStream());

            stream.clearActualBuffer();
            GenericJsonArray o1 = new GenericJsonArray();
            o1.add(true);
            o1.add(123);
            o1.add("test message");
            GenericJsonObject o2 = new GenericJsonObject();
            o2.putArbitrary("e", false);
            o1.add(o2);
            GenericJsonArray o3 = new GenericJsonArray();
            o3.add(false);
            o1.add(o3);
            JsonSerializer.toStream(o1, stream);
            String outputStream = stream.getStream();
            String compactJSONStream = "[true,123.0,\"test message\",{\"e\":false},[false]]";
            assertEquals("Should contain the ordered list", compactJSONStream, outputStream);
        }
        catch(JsonException e) {
            fail("No JsonException should have been thrown");
        }
    }

	@Test
    public void testToStreamJsonException() throws IOException {
        JsonException o = new JsonException("Unexpected error");
        JsonSerializer.toStream(o, stream);
        String outputStream = stream.getStream();
        assertTrue(outputStream.contains("'isException':true"));
        assertTrue(outputStream.contains("'exceptionType':'Unexpected error'"));
        assertTrue(outputStream.contains("'exceptionId':" + o.getExceptionId()));
    }
	*/

}
