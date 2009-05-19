package org.domderrien.jsontools;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.domderrien.MockLogger;
import org.domderrien.MockOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestGenericJsonArray {

	MockLogger logger = new MockLogger("test", null);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@SuppressWarnings("unchecked")
    public void testConstructorI() throws JsonException {
        new GenericJsonArray(new ArrayList());
    }

	@Test
    public void testExtractFromEmptyObjectI() throws JsonException {
        JsonParser p = new JsonParser("[]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 0, o1.size());
    }

	@Test
    public void testExtractFromEmptyObjectII() throws JsonException {
        JsonParser p = new JsonParser(" []", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 0, o1.size());
    }

	@Test
    public void testExtractFromEmptyObjectIII() throws JsonException {
        JsonParser p = new JsonParser("[] ", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 0, o1.size());
    }

    @Test
    public void testExtractFromEmptyObjectIV() throws JsonException {
        JsonParser p = new JsonParser("[ ]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 0, o1.size());
    }

    @Test
    public void testExtractFromEmptyObjectV() throws JsonException {
        JsonParser p = new JsonParser(" \t [] \n ", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 0, o1.size());
    }

    @Test
    public void testExtractFromOneBooleanI() throws JsonException {
        JsonParser p = new JsonParser("[true]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", true, o1.getBoolean(0));
    }

    @Test
    public void testExtractFromOneBooleanII() throws JsonException {
        JsonParser p = new JsonParser("[false]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", false, o1.getBoolean(0));
    }

    @Test
    public void testExtractFromOneNumberI() throws JsonException {
        JsonParser p = new JsonParser("[0]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", 0, o1.getLong(0));
    }

    @Test
    public void testExtractFromOneNumberII() throws JsonException {
        JsonParser p = new JsonParser("[12345]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", 12345, o1.getLong(0));
    }

    @Test
    public void testExtractFromOneNumberIII() throws JsonException {
        JsonParser p = new JsonParser("[-153]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", -153, o1.getLong(0));
    }

    @Test
    public void testExtractFromOneNumberIW() throws JsonException {
        JsonParser p = new JsonParser("[-1.323E4]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", -13230, o1.getLong(0));
    }

    @Test
    public void testExtractFromOneStringI() throws JsonException {
        JsonParser p = new JsonParser("[\"value\"]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", "value", o1.getString(0));
    }

    @Test
    public void testExtractFromOneStringII() throws JsonException {
        JsonParser p = new JsonParser("[\"value1\\\"value2\"]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", "value1\"value2", o1.getString(0));
    }

    @Test
    public void testExtractFromOneStringIII() throws JsonException {
        JsonParser p = new JsonParser("[\"{key1: 1, key2: 2.0 , key3: true \t }\"]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());
        assertEquals("Attribute value", "{key1: 1, key2: 2.0 , key3: true \t }", o1.getString(0));
    }

    @Test
    public void testExtractObjectFromArray() throws JsonException {
        JsonParser p = new JsonParser("[{\"a\":\"a\",\"b\":\"b\"}]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());

        JsonObject o2 = o1.getJsonObject(0);
        assertNotNull("GenericJsonObject expected", o2);
        assertEquals("Attribute #", 2, o2.size());
        assertEquals("Attribute value", "a", o2.getString("a"));
        assertEquals("Attribute value", "b", o2.getString("b"));
    }

    @Test
    public void testExtractNestedArraysI() throws JsonException {
        JsonParser p = new JsonParser("[\"a\",[\"b\"],[\"c\",[\"d\"]]]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 3, o1.size());

        String o2 = o1.getString(0);
        assertNotNull("String expected", o2);
        assertEquals("Attribute value", "a", o2);
        JsonArray o3 = o1.getJsonArray(1);
        assertNotNull("GenericJsonObject expected", o3);
        assertEquals("Attribute #", 1, o3.size());
        assertEquals("Attribute value", "b", o3.getString(0));
        JsonArray o4 = o1.getJsonArray(2);
        assertNotNull("GenericJsonObject expected", o4);
        assertEquals("Attribute #", 2, o4.size());
        assertEquals("Attribute value", "c", o4.getString(0));
    }

    @Test
    public void testExtractNestedArraysII() throws JsonException {
        JsonParser p = new JsonParser("[\"[business]\"]", logger);
        JsonArray o1 = p.getJsonArray();
        assertEquals("Attribute #", 1, o1.size());

        try {
            JsonArray o2 = o1.getJsonArray(0);
            fail("ClassCastException should have been thrown");
            o2.size();
        }
        catch (ClassCastException e) {
        }
        catch (Exception ex) {
            fail("ClassCastException should have been thrown: " + ex);
        }

        String o3 = o1.getString(0);
        assertEquals("Attribute value", "[business]", o3);
    }

    @Test
    public void testJsonExceptionIntertion() throws JsonException {
        JsonArray o = new GenericJsonArray();
        JsonException o1 = new JsonException("Authentication failure");
        o.set(0, o1);
        o.add(o1);
        assertEquals(o.getJsonException(0), o1);
        assertEquals(o.getJsonException(1), o1);
    }

	@Test
    public void testGettersAndSetters() throws JsonException {
        JsonArray o = new GenericJsonArray();
        GenericJsonObject o1 = new GenericJsonObject();
        JsonArray o2 = new GenericJsonArray();

        // First series of tests to verify the set() methods
        int idx = 0;
        o.set(idx, true); idx++;
        o.set(idx, 1L); idx++;
        o.set(idx, 1.0D); idx++;
        o.set(idx, "test"); idx++;
        o.set(idx, o1); idx++;
        o.set(idx, o2); // idx++;
        idx = 0;
        assertEquals("Attribute " + idx + " should be " + true, true, o.getBoolean(idx)); idx++;
        assertEquals("Attribute " + idx + " should be " + 1L, 1L, o.getLong(idx)); idx++;
        assertEquals(1.0D, o.getDouble(idx), 0); idx++;
        assertEquals("Attribute " + idx + " should be " + "test", "test", o.getString(idx)); idx++;
        assertEquals("Attribute " + idx + " should be " + o1.toString(), o1, o.getJsonObject(idx)); idx++;
        assertEquals("Attribute " + idx + " should be " + o2.toString(), o2, o.getJsonArray(idx)); idx++;

        // First series of tests to verify the add() methods
        o.add(true);
        o.add(1L);
        o.add(1.0D);
        o.add("test");
        o.add(o1);
        o.add(o2);
        assertEquals("Attribute " + idx + " should be " + true, true, o.getBoolean(idx)); idx++;
        assertEquals("Attribute " + idx + " should be " + 1L, 1L, o.getLong(idx)); idx++;
        assertEquals(1.0D, o.getDouble(idx), 0); idx++;
        assertEquals("Attribute " + idx + " should be " + "test", "test", o.getString(idx)); idx++;
        assertEquals("Attribute " + idx + " should be " + o1.toString(), o1, o.getJsonObject(idx)); idx++;
        assertEquals("Attribute " + idx + " should be " + o2.toString(), o2, o.getJsonArray(idx)); // idx++;
    }

    @Test
    public void testJsonArray() throws JsonException {
        JsonArray o = new GenericJsonArray();
        assertEquals("Object should be empty", 0, o.size());
    }

    @Test
    public void testResetSameElement() throws JsonException {
        JsonArray o = new GenericJsonArray();
        o.set(0, true);
        assertEquals("Attribute 0 should be " + true, true, o.getBoolean(0));
        o.set(0, false);
        assertEquals("Attribute 0 should be " + false, false, o.getBoolean(0));
        o.set(0, 1);
        assertEquals("Attribute 0 should be " + 1, 1, o.getLong(0));
    }

    @Test
    public void testToString() throws JsonException {
        JsonArray o = new GenericJsonArray();
        GenericJsonObject o1 = new GenericJsonObject();
        o1.put("a", "Internal GenericJsonObject test string");
        JsonArray o2 = new GenericJsonArray();
        o2.add("Internal GenericJsonArray test string");
        o.add(o1);
        o.add(o2);
        o.add(+1.485687e125);
        o.add(true);
        o.add("test string");
        o.toString();
    }

    @Test
    public void testRemove() throws JsonException {
        JsonArray o = new GenericJsonArray();
        o.add(true);
        o.remove(0);
        assertEquals("Array should be empty", 0, o.size());
        o.add(false);
        o.remove(Boolean.FALSE);
        assertEquals("Array should be empty", 0, o.size());
    }

    @Test
    public void testRemoveAll() throws JsonException {
        JsonArray o = new GenericJsonArray();
        o.add(true);
        o.add(false);
        o.add(1);
        assertEquals("Array should have 3 elements", 3, o.size());
        o.removeAll();
        assertEquals("Array should be empty", 0, o.size());
    }

    @Test
    public void testAppendI() throws JsonException {
        JsonArray o1 = new GenericJsonArray();
        o1.add(true);
        o1.add(false);
        o1.add(1);
        assertEquals("Array should have 3 elements", 3, o1.size());
        JsonArray o2 = new GenericJsonArray();
        o1.append(o2);
        assertEquals("Array should have 3 elements", 3, o1.size());
        assertEquals("Third element should be 1", 1, o1.getLong(2));
    }

    @Test
    public void testAppendII() throws JsonException {
        JsonArray o1 = new GenericJsonArray();
        assertEquals("Array should have 0 element", 0, o1.size());
        JsonArray o2 = new GenericJsonArray();
        o2.add(true);
        o2.add(false);
        o2.add(1);
        o1.append(o2);
        assertEquals("Array should have 3 elements", 3, o1.size());
        assertEquals("Third element should be 1", 1, o1.getLong(2));
    }

    @Test
    public void testAppendIII() throws JsonException {
        JsonArray o1 = new GenericJsonArray();
        o1.add(true);
        o1.add(false);
        o1.add(1);
        assertEquals("Array should have 3 elements", 3, o1.size());
        JsonArray o2 = new GenericJsonArray();
        o2.add(2);
        JsonArray o3 = new GenericJsonArray();
        o2.add(o3);
        o1.append(o2);
        assertEquals("Array should have 5 elements", 5, o1.size());
        assertEquals("First element should be true", true, o1.getBoolean(0));
        assertEquals("Third element should be 1", 1, o1.getLong(2));
        assertEquals("Fourth element should be 2", 2, o1.getLong(3));
        assertEquals("Fifth element should be a JsonArrayInstance", o3, o1.getJsonArray(4));
    }
    
	@Test
    @SuppressWarnings("unchecked")
    public void testToStream() throws IOException {
    	List list = new ArrayList();
    	list.add(Boolean.TRUE);
    	list.add("test");
    	list.add(Long.valueOf(1l));
    	list.add(Double.valueOf(1f));
    	list.add(new GenericJsonObject());
    	list.add(new GenericJsonArray());
    	MockOutputStream stream = new MockOutputStream();
    	new GenericJsonArray(list).toStream(stream, true);
    	assertTrue(stream.getStream().contains("true"));
    	assertTrue(stream.getStream().contains("'test'"));
    	assertTrue(stream.getStream().contains("1"));
    	assertTrue(stream.getStream().contains("1.0"));
    	assertTrue(stream.getStream().contains("{}"));
    	assertTrue(stream.getStream().contains("[]"));
    }

    // See JsonObjectTranferTest.java for collection of {key; value} pairs tokenizing
}
