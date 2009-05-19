package org.domderrien.jsontools;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.domderrien.MockOutputStream;

public class TestJsonException {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetExceptionId() {
        JsonException ex1 = new JsonException("Unexpected error");
        JsonException ex2 = new JsonException("Unexpected error");
        assertTrue("Following exception index should be greater", ex1.getExceptionId() < ex2.getExceptionId());
    }

	@Test
    public void testJsonExceptionI() {
        String exceptionType = "Unexpected error";
        String message = "test message";
        JsonException o = new JsonException(exceptionType, message);
        assertEquals("Exception key must be '" + exceptionType + "'", exceptionType, o.getExceptionType());
        assertEquals("Message should be the given message", message, o.getMessage());
    }

	@Test
    public void testJsonExceptionII() {
        String exceptionType = "Unexpected error";
        String message = null;
        JsonException o = new JsonException(exceptionType, message);
        assertEquals("Exception key must be '" + exceptionType + "'", exceptionType, o.getExceptionType());
        assertEquals("Message should be the given type", exceptionType, o.getMessage());
    }

	@Test
    public void testJsonExceptionIII() {
        String exceptionType = "Unexpected error";
        String message = "test message";
        JsonException o = new JsonException(exceptionType, new RuntimeException(message));
        assertEquals("Exception key must be '" + exceptionType + "'", exceptionType, o.getExceptionType());
        assertTrue("Message should be the given message", o.getMessage().contains(message));
    }

	@Test
    public void testToStringI() {
		new JsonException("type", "message", new RuntimeException("exception")).toString();
    }

	@Test
    public void testGetMapI() {
		new JsonException("type", "message", new RuntimeException("exception")).getMap();
    }

	@Test
    public void testIsNonNullI() {
		JsonException o = new JsonException("type", "message", new RuntimeException("exception"));
		assertTrue(o.isNonNull("exceptionType"));
    }

	@Test
    public void testJsonObjectGettersSettersI() {
        JsonException o = new JsonException("Unexpected error");
        o.put("a", true);
        assertEquals(true, o.getBoolean("a"));
        o.put("aa", false);
        assertEquals(false, o.getBoolean("aa"));
    }

	@Test
    public void testJsonObjectGettersSettersII() {
        JsonException o = new JsonException("Unexpected error");
        o.put("a", 1235);
        assertEquals(1235, o.getLong("a"));
        o.put("aa", -785613212);
        assertEquals(-785613212, o.getLong("aa"));
    }

	@Test
    public void testJsonObjectGettersSettersIII() {
        JsonException o = new JsonException("Unexpected error");
        o.put("a", 12.35);
        assertEquals(12.35, o.getDouble("a"), 0);
        o.put("aa", -1.454E248);
        assertEquals(-1.454E248, o.getDouble("aa"), 0);
    }

	@Test
    public void testJsonObjectGettersSettersIV() {
        JsonObject o1 = new GenericJsonObject();
        JsonObject o2 = new GenericJsonObject();
        JsonException o = new JsonException("Unexpected error");
        o.put("a", o1);
        assertEquals(o1, o.getJsonObject("a"));
        o.put("aa", o2);
        assertEquals(o2, o.getJsonObject("aa"));
    }

	@Test
    public void testJsonObjectGettersSettersV() {
        JsonArray o1 = new GenericJsonArray();
        JsonArray o2 = new GenericJsonArray();
        JsonException o = new JsonException("Unexpected error");
        o.put("a", o1);
        assertEquals(o1, o.getJsonArray("a"));
        o.put("aa", o2);
        assertEquals(o2, o.getJsonArray("aa"));
    }

	@Test
    public void testJsonObjectGettersSettersVI() {
        JsonException o = new JsonException("Unexpected error");
        o.put("a", o);
        assertEquals(o, o.getJsonException("a"));
        o.put("aa", o);
        assertEquals(o, o.getJsonException("aa"));
    }

	@Test
    public void testJsonObjectRemoveI() {
        JsonException o = new JsonException("Unexpected error");
        int initialSize = o.size();
        o.put("a", "test");
        assertEquals(initialSize + 1, o.size());
        o.remove("a");
        assertEquals(initialSize, o.size());
        assertFalse(o.containsKey("a"));
    }

	@Test
    public void testJsonObjectRemoveII() {
        JsonException o = new JsonException("Unexpected error");
        int initialSize = o.size();
        o.put("aa", "test");
        assertEquals(initialSize + 1, o.size());
        o.remove("aa");
        assertEquals(initialSize, o.size());
        assertFalse(o.containsKey("aa"));
    }

	@Test
    public void testJsonObjectRemoveIII() {
        JsonException o = new JsonException("Unexpected error");
        try {
            o.removeAll();
        }
        catch(UnsupportedOperationException ex) {
            // Expected exception
        }
        catch(Exception ex) {
            fail("No other Exception expected");
        }
    }

	@Test
    public void testJsonObjectAppend() {
        JsonException o = new JsonException("Unexpected error");
        try {
            o.append(new GenericJsonObject());
        }
        catch(UnsupportedOperationException ex) {
            // Expected exception
        }
        catch(Exception ex) {
            fail("No other Exception expected");
        }
    }
	
	@Test
	public void testToStreamI() {
		MockOutputStream out = new MockOutputStream();
        String exceptionType = "Unexpected error";
        String message = "test message";
        JsonException o = new JsonException(exceptionType, message);
        try {
			o.toStream(out, false);
		}
        catch (IOException e) {
			fail("No expected exception");
		}
        assertTrue(out.getStream().contains("isException"));
        assertTrue(out.getStream().contains("true"));
        assertTrue(out.getStream().contains("exceptionId"));
        assertTrue(out.getStream().contains("exceptionType"));
        assertTrue(out.getStream().contains(exceptionType));
        assertTrue(out.getStream().contains("exceptionMessage"));
        assertTrue(out.getStream().contains(message));
        assertTrue(out.getStream().contains("orginalException"));
        assertTrue(out.getStream().contains("<nop>"));
    }
	
	@Test
	public void testToStreamII() {
		MockOutputStream out = new MockOutputStream();
        String exceptionType = "Unexpected error";
        String message = "test message";
        JsonException o = new JsonException(exceptionType, new RuntimeException(message));
        try {
			o.toStream(out, false);
		}
        catch (IOException e) {
			fail("No expected exception");
		}
        assertTrue(out.getStream().contains("isException"));
        assertTrue(out.getStream().contains("true"));
        assertTrue(out.getStream().contains("exceptionId"));
        assertTrue(out.getStream().contains("exceptionType"));
        assertTrue(out.getStream().contains(exceptionType));
        assertTrue(out.getStream().contains("exceptionMessage"));
        assertTrue(out.getStream().contains(exceptionType));
        assertTrue(out.getStream().contains("orginalException"));
        assertTrue(out.getStream().contains(message));
    }
	
	@Test
	public void testToStreamIII() {
		MockOutputStream out = new MockOutputStream();
        String exceptionType = "Unexpected error";
        String message = null;
        JsonException o = new JsonException(exceptionType, new RuntimeException(message));
        try {
			o.toStream(out, false);
		}
        catch (IOException e) {
			fail("No expected exception");
		}
        assertTrue(out.getStream().contains("isException"));
        assertTrue(out.getStream().contains("true"));
        assertTrue(out.getStream().contains("exceptionId"));
        assertTrue(out.getStream().contains("exceptionType"));
        assertTrue(out.getStream().contains(exceptionType));
        assertTrue(out.getStream().contains("exceptionMessage"));
        assertTrue(out.getStream().contains("null"));
        assertTrue(out.getStream().contains(exceptionType));
        assertTrue(out.getStream().contains("orginalException"));
        assertTrue(out.getStream().contains("<nop>"));
	}
}
