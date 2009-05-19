package org.domderrien.jsontools;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.domderrien.MockLogger;
import org.domderrien.MockOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestGenericJsonObject {

	MockLogger logger = new MockLogger("test", null);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructor() {
		new GenericJsonObject(new HashMap());
	}

	@Test
	public void testExtractFromEmptyObjectI() throws JsonException {
		JsonParser p = new JsonParser("{}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 0, o0.size());
	}

	@Test
	public void testExtractFromEmptyObjectII() throws JsonException {
		JsonParser p = new JsonParser(" {}", logger);
		JsonObject o0 = p.getJsonObject();
		o0.size();
	}

	@Test
	public void testExtractFromEmptyObjectIII() throws JsonException {
		JsonParser p = new JsonParser("{} ", logger);
		JsonObject o0 = p.getJsonObject();
		o0.size();
	}

	@Test
	public void testExtractFromEmptyObjectIV() throws JsonException {
		JsonParser p = new JsonParser("{ }", logger);
		JsonObject o0 = p.getJsonObject();
		o0.size();
	}

	@Test
	public void testExtractFromEmptyObjectV() throws JsonException {
		JsonParser p = new JsonParser(" \t {} \n ", logger);
		JsonObject o0 = p.getJsonObject();
		o0.size();
	}

	@Test(expected=JsonException.class)
	public void testExtractFromOneKeyWithoutValueI() throws JsonException {
		JsonParser p = new JsonParser("{\"key\"}", logger);
		p.getJsonObject();
	}

	@Test(expected=JsonException.class)
	public void testExtractFromOneKeyWithoutValueII() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":}", logger);
		p.getJsonObject();
	}

	@Test(expected=JsonException.class)
	public void testExtractFromOneKeyWithoutValueIII() throws JsonException {
		JsonParser p = new JsonParser("{ \"key\" : }", logger);
		p.getJsonObject();
	}

	@Test
	public void testExtractFromOneKeyWithoutValueIV() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":null}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
	}

	@Test
	public void testExtractFromOneWeirdKeyI() throws JsonException {
		JsonParser p = new JsonParser("{\"k:ey\":1}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
	}

	@Test
	public void testExtractFromOneWeirdKeyII() throws JsonException {
		JsonParser p = new JsonParser("{\"k\\\"ey\":1}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertNotNull("Attribute value", o0.getDouble("k\"ey"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testIsNonNull() {
		Map map = new HashMap();
		map.put("key", new String[] { "1.0" });
		JsonObject o0 = new GenericJsonObject(map);
		assertTrue(o0.isNonNull("key"));
		assertFalse(o0.isNonNull("KEY"));
	}
	@Test
	public void testExtractFromOneBooleanI() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":true}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertTrue("Attribute value", o0.getBoolean("key"));
	}

	@Test
	public void testExtractFromOneBooleanII() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":false}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertFalse("Attribute value", o0.getBoolean("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractFromOneBooleanIII() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[] { "true" });
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals("Attribute #", 1, o0.size());
		assertTrue("Attribute value", o0.getBoolean("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractFromOneBooleanIV() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[0]);
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals("Attribute #", 1, o0.size());
		assertFalse("Default value", o0.getBoolean("key"));
	}

	@Test
	public void testExtractFromOneNumberI() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":0}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", 0l, o0.getLong("key"));
		assertEquals("Attribute value", 0d, o0.getDouble("key"), 0);
	}

	@Test
	public void testExtractFromOneNumberII() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":12345}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", 12345, o0.getLong("key"));
		assertEquals("Attribute value", 1.2345e4d, o0.getDouble("key"), 0);
	}

	@Test
	public void testExtractFromOneNumberIII() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":-153}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", -153, o0.getLong("key"));
		assertEquals("Attribute value", -1.53e2d, o0.getDouble("key"), 0);
	}

	@Test
	public void testExtractFromOneNumberIV() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":-1.323E4}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", -13230, o0.getLong("key"));
		assertEquals("Attribute value", -1.323e4d, o0.getDouble("key"), 0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractFromOneNumberV() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[] { "1" });
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", 1l, o0.getLong("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractFromOneNumberVI() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[] { "1.0" });
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", 1d, o0.getDouble("key"), 0);
	}

	@Test
	public void testExtractFromOneNumberVII() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":null}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", 0l, o0.getLong("key"));
	}

	@Test
	public void testExtractFromOneNumberVIII() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":null}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", 0d, o0.getDouble("key"), 0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractFromOneNumberIX() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[0]);
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Default value", 0l, o0.getLong("key"));
		assertEquals("Default value", 0d, o0.getDouble("key"), 0);
	}

	@Test
	public void testExtractFromOneStringI() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":\"value\"}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", "value", o0.getString("key"));
	}

	@Test
	public void testExtractFromOneStringII() throws JsonException {
		JsonParser p = new JsonParser("{\"key\":\"value1\\\"value2\"}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", "value1\"value2", o0.getString("key"));
	}

	@Test
	public void testExtractFromOneStringIII() throws JsonException {
		JsonParser p = new JsonParser("{\"key\": \"{key1: 1, key2: 2.0 , key3: true \t }\"}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", "{key1: 1, key2: 2.0 , key3: true \t }", o0.getString("key"));
	}

	@Test
	public void testExtractFromOneStringIV() throws JsonException {
		JsonParser p = new JsonParser("{ \"key\" : \"value\" }", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", "value", o0.getString("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractFromOneStringV() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[0]);
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals("Attribute #", 1, o0.size());
		assertNull("Attribute value", o0.getString("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractFromOneStringVI() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[] { "1.0", "2.0" });
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals("Attribute #", 1, o0.size());
		assertEquals("Attribute value", "1.0", o0.getString("key"));
	}

	@Test(expected=ClassCastException.class)
	@SuppressWarnings("unchecked")
	public void testExtractObject() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[0]);
		JsonObject o0 = new GenericJsonObject(map);
		o0.getJsonObject("key");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractArrayI() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[0]);
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals(0, o0.getJsonArray("key").size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExtractArrayII() throws JsonException {
		Map map = new HashMap();
		map.put("key", new String[] { "test1", "test2", "test3" });
		JsonObject o0 = new GenericJsonObject(map);
		assertEquals(3, o0.getJsonArray("key").size());
		assertEquals("test1", o0.getJsonArray("key").getString(0));
		assertEquals("test2", o0.getJsonArray("key").getString(1));
		assertEquals("test3", o0.getJsonArray("key").getString(2));
	}

	@Test
	public void testExtractArrayFromObject() throws JsonException {
		JsonParser p = new JsonParser("{\"addresses\":[\"a\",\"b\"]}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());

		JsonArray o1 = o0.getJsonArray("addresses");
		assertNotNull("GenericJsonArray expected", o1);
		assertEquals("Attribute #", 2, o1.size());
		assertEquals("Attribute value", "a", o1.getString(0));
		assertEquals("Attribute value", "b", o1.getString(1));
	}

	@Test
	public void testExtractNestedObjectsI() throws JsonException {
		JsonParser p = new JsonParser(
				"{\"addresses\":{\"business\":\"addr1\",\"home\":\"addr2\"}}",
				logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());

		JsonObject o1 = o0.getJsonObject("addresses");
		assertNotNull("GenericJsonObject expected", o1);
		assertEquals("Attribute #", 2, o1.size());
		assertEquals("Attribute value", "addr1", o1.getString("business"));
		assertEquals("Attribute value", "addr2", o1.getString("home"));
	}

	@Test
	public void testExtractNestedObjectsII() throws JsonException {
		JsonParser p = new JsonParser(
				"{\"addresses\":\"{business:addr1,home:addr2}\"}", logger);
		JsonObject o0 = p.getJsonObject();
		assertEquals("Attribute #", 1, o0.size());

		try {
			JsonObject o1 = o0.getJsonObject("addresses");
			fail("ClassCastException should have been thrown");
			o1.size();
		} catch (ClassCastException ex) {
		} catch (Exception ex) {
			fail("ClassCastException should have been thrown: " + ex);
		}
		String o2 = o0.getString("addresses");
		assertNotNull("GenericJsonObject not expected", o2);
		assertEquals("Attribute value", "{business:addr1,home:addr2}", o2);
	}

	@Test
	public void testExtractNestedObjectsAndArrays() throws JsonException {
		JsonParser p = new JsonParser(
				"{\"name\":\"test\",\"age\":25,\"addresses\":{\"business\":[\"bus_addr1\",\"bus_addr2\",\"bus_addr3\"],\"home\":[\"home_addr1\",\"home_addr2\"]}}",
				logger);
		JsonObject o1 = p.getJsonObject();
		assertEquals("Attribute #", 3, o1.size());

		String o2 = o1.getString("name");
		assertNotNull("String expected", o2);
		assertEquals("Attribute value", "test", o2);
		long o3 = o1.getLong("age");
		assertEquals("Attribute value", 25, o3);
		JsonObject o4 = o1.getJsonObject("addresses");
		assertNotNull("GenericJsonObject expected", o4);
		assertEquals("Attribute #", 2, o4.size());
		JsonArray o5 = o4.getJsonArray("business");
		assertNotNull("GenericJsonObject expected", o5);
		assertEquals("Attribute #", 3, o5.size());
		assertEquals("Attribute value", "bus_addr3", o5.getString(2));
		JsonArray o6 = o4.getJsonArray("home");
		assertNotNull("GenericJsonObject expected", o6);
		assertEquals("Attribute #", 2, o6.size());
		assertEquals("Attribute value", "home_addr2", o6.getString(1));
	}

	@Test
	public void testJsonExceptionInsertion() throws JsonException {
		JsonObject o = new GenericJsonObject();
		JsonException o1 = new JsonException("Authentication failire");
		o.put("a", o1);
		assertEquals(o.getJsonException("a"), o1);
	}

	@Test
	public void testGettersAndSetters() throws JsonException {
		JsonObject o = new GenericJsonObject();
		JsonObject o1 = new GenericJsonObject();
		GenericJsonArray o2 = new GenericJsonArray();

		o.put("a", true);
		o.put("c", 1L);
		o.put("e", 1.0D);
		o.put("f", "test");
		o.put("g", o1);
		o.put("h", o2);
		assertEquals("Attribute " + "a" + " should be " + true, true, o.getBoolean("a"));
		assertEquals("Attribute " + "c" + " should be " + 1L, 1L, o.getLong("c"));
		assertEquals(1.0D, o.getDouble("e"), 0);
		assertEquals("Attribute " + "f" + " should be " + "test", "test", o.getString("f"));
		assertEquals("Attribute " + "g" + " should be " + o1.toString(), o1, o.getJsonObject("g"));
		assertEquals("Attribute " + "h" + " should be " + o2.toString(), o2, o.getJsonArray("h"));
	}

	@Test
	public void testJsonObject() throws JsonException {
		JsonObject o = new GenericJsonObject();
		assertEquals("Object should be empty", 0, o.size());
	}

	@Test
	public void testContainsKey() throws JsonException {
		JsonObject o = new GenericJsonObject();
		assertFalse("No value stored at: addresses", o.containsKey("addresses"));
		o.put("addresses", "test");
		assertTrue("No value stored at: addresses", o.containsKey("addresses"));
		assertEquals("Value should be: test", "test", o.getString("addresses"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testToStringI() {
		Map map = new HashMap();
		map.put("k1", Boolean.TRUE);
		map.put("k2", "test");
		map.put("k3", Long.valueOf(1l));
		map.put("k4", Double.valueOf(1f));
		map.put("k5", new GenericJsonObject());
		map.put("k6", new GenericJsonArray());
		String out = new GenericJsonObject(map).toString();
		assertTrue(out.contains("k1: true"));
		assertTrue(out.contains("k2: String: \"test\""));
		assertTrue(out.contains("k3: 1"));
		assertTrue(out.contains("k4: 1.0"));
		assertTrue(out.contains("k5: JsonObject: {\n  }"));
		assertTrue(out.contains("k6: JsonArray: [\n  ]"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testToStringII() {
		Map map = new HashMap();
		map.put("key", new String[] { "test1", "test2", "test3" });
		String out = new GenericJsonObject(map).toString();
		assertTrue(out.contains("key: String[]: [\"test1\",\"test2\",\"test3\"]"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testToStringIII() {
		Map map = new HashMap();
		map.put("key", new String[] { "test1" });
		String out = new GenericJsonObject(map).toString();
		assertTrue(out.contains("key: String[]: \"test1\""));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testToStringIV() {
		Map map = new HashMap();
		map.put("key", new String[0]);
		String out = new GenericJsonObject(map).toString();
		assertTrue(out.contains("key: String[]: null"));
	}

	@Test
	public void testRemove() throws JsonException {
		JsonObject o = new GenericJsonObject();
		o.put("a", true);
		o.remove("a");
		assertEquals("Object should be empty", 0, o.size());
	}

	@Test
	public void testRemoveAll() throws JsonException {
		JsonObject o = new GenericJsonObject();
		o.put("a", true);
		o.put("b", true);
		o.put("c", true);
		o.removeAll();
		assertEquals("Object should be empty", 0, o.size());
	}

	@Test
	public void testAppendI() throws JsonException {
		JsonObject o1 = new GenericJsonObject();
		o1.put("a", true);
		o1.put("b", false);
		o1.put("c", 1);
		assertEquals("Object should have 3 elements", 3, o1.size());
		JsonObject o2 = new GenericJsonObject();
		o1.append(o2);
		assertEquals("Object should have 3 elements", 3, o1.size());
		assertEquals("Element \"c\" should be 1", 1, o1.getLong("c"));
	}

	@Test
	public void testAppendII() throws JsonException {
		JsonObject o1 = new GenericJsonObject();
		assertEquals("Object should have 0 element", 0, o1.size());
		JsonObject o2 = new GenericJsonObject();
		o2.put("a", true);
		o2.put("b", false);
		o2.put("c", 1);
		o1.append(o2);
		assertEquals("Object should have 3 elements", 3, o1.size());
		assertEquals("Element \"c\" should be 1", 1, o1.getLong("c"));
	}

	@Test
	public void testAppendIII() throws JsonException {
		JsonObject o1 = new GenericJsonObject();
		o1.put("a", true);
		o1.put("b", false);
		o1.put("c", 1);
		assertEquals("Object should have 3 elements", 3, o1.size());
		JsonObject o2 = new GenericJsonObject();
		o2.put("d", 2);
		JsonObject o3 = new GenericJsonObject();
		o2.put("e", o3);
		o1.append(o2);
		assertEquals("Object should have 5 elements", 5, o1.size());
		assertEquals("Element \"c\" should be 1", 1, o1.getLong("c"));
		assertEquals("Element \"d\" should be 2", 2, o1.getLong("d"));
		assertEquals("Element \"e\" should be a JsonObject instance", o3, o1.getJsonObject("e"));
	}

	@Test
	public void testAppendIV() throws JsonException {
		JsonObject o1 = new GenericJsonObject();
		o1.put("a", true);
		o1.put("b", false);
		o1.put("c", 1);
		assertEquals("Object should have 3 elements", 3, o1.size());
		JsonObject o2 = new GenericJsonObject();
		o2.put("c", 2);
		JsonObject o3 = new GenericJsonObject();
		o2.put("b", o3);
		o1.append(o2);
		assertEquals("Object should have 3 elements", 3, o1.size());
		assertEquals("Element \"c\" should be 2", 2, o1.getLong("c"));
		assertEquals("Element \"b\" should be a JsonObject instance", o3, o1.getJsonObject("b"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testToStream() throws IOException {
		Map map = new HashMap();
		map.put("k1", Boolean.TRUE);
		map.put("k2", "test");
		map.put("k3", Long.valueOf(1l));
		map.put("k4", Double.valueOf(1f));
		map.put("k5", new GenericJsonObject());
		map.put("k6", new GenericJsonArray());
		MockOutputStream stream = new MockOutputStream();
		new GenericJsonObject(map).toStream(stream, true);
		assertTrue(stream.getStream().contains("'k1':true"));
		assertTrue(stream.getStream().contains("'k2':'test'"));
		assertTrue(stream.getStream().contains("'k3':1"));
		assertTrue(stream.getStream().contains("'k4':1.0"));
		assertTrue(stream.getStream().contains("'k5':{}"));
		assertTrue(stream.getStream().contains("'k6':[]"));
	}
	
	// See GenericJsonArrayTest.java for ordered list of values tokenizing
}
