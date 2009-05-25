package org.domderrien.jsontools;

import static org.junit.Assert.*;

import java.io.IOException;

import org.domderrien.MockInputStream;
import org.domderrien.MockLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJsonParser {

	MockLogger mockLogger = new MockLogger("test", null);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
    public void testJsonParserCharStreamI() {
		try {
			new JsonParser("normal");
		}
		catch (JsonException ex) {
			fail("no exception expected -- " + ex);
		}
	}

	@Test(expected=JsonException.class)
    public void testJsonParserCharStreamII() throws JsonException {
		new JsonParser("");
	}

	@Test(expected=JsonException.class)
    public void testJsonParserCharStreamIII() throws JsonException {
		new JsonParser((String) null);
	}

	@Test
    public void testJsonParserCharStreamIV() throws JsonException {
		JsonParser t0 = new JsonParser("normal");
		t0.getStream().peek(true);
	}

	@Test
    public void testJsonParserCharStreamV() throws JsonException {
		JsonParser t0 = new JsonParser("      ");
		t0.getStream().peek(true);
	}

	@Test(expected=JsonException.class)
    public void testTokenizeFromAnInputStreamI() throws JsonException {
        new JsonParser(new MockInputStream());
    }

	@Test
    public void testTokenizeFromAnInputStreamII() throws JsonException {
        JsonParser t0 = new JsonParser(new MockInputStream("\"Hi\""));
        assertEquals("Extracted value", "Hi", t0.getString());
    }

	@Test
    public void testTokenizeFromAnInputStreamIII() throws JsonException {
        JsonParser t1 = new JsonParser(new MockInputStream("1"));
        assertEquals("Extracted value", new Double(1), t1.getNumber());
    }

	@Test
    public void testTokenizeFromAnInputStreamIV() throws JsonException {
        JsonParser t2 = new JsonParser(new MockInputStream("true"));
        assertEquals("Extracted value", Boolean.TRUE, t2.getBoolean());
    }

	@Test
    public void testTokenizeFromAnInputStreamV() throws JsonException {
        JsonParser t3 = new JsonParser(new MockInputStream("null"));
        assertNull("Extracted value should be null", t3.getObject());
    }

	@Test
    public void testTokenizeFromAnInputStreamVI() throws JsonException {
        JsonParser t4 = new JsonParser(new MockInputStream("{\"key\":\"Hi\"}"));
        JsonObject o0 = t4.getJsonObject();
        assertEquals("Extracted value", "Hi", o0.getString("key"));
    }

	@Test
    public void testTokenizeFromAnInputStreamVII() throws JsonException {
        JsonParser t4 = new JsonParser(new MockInputStream("[\"Hi\"]"));
        JsonArray o0 = t4.getJsonArray();
        assertEquals("Extracted value", "Hi", o0.getString(0));
    }

	@Test
    public void testTokenizeAStringI() throws JsonException {
        JsonParser t1 = new JsonParser("\"Hello\"", mockLogger);
        assertEquals("Extracted value", "Hello", t1.getString());
    }

	@Test
    public void testTokenizeAStringII() throws JsonException {
        JsonParser t2 = new JsonParser("\"\"", mockLogger);
        assertEquals("Extracted value", "", t2.getString());
    }

	@Test
    public void testTokenizeAStringIII() throws JsonException {
        JsonParser t3 = new JsonParser("\"\\\\\"", mockLogger);
        assertEquals("Extracted value", "\\", t3.getString());
    }

	@Test
    public void testTokenizeAStringIV() throws JsonException {
        JsonParser t4 = new JsonParser("\"\\/\"", mockLogger);
        assertEquals("Extracted value", "/", t4.getString());
    }

	@Test
    public void testTokenizeAStringV() throws JsonException {
        JsonParser t5 = new JsonParser("\"\\r\"", mockLogger);
        assertEquals("Extracted value", "\r", t5.getString());
    }

	@Test
    public void testTokenizeAStringVI() throws JsonException {
        JsonParser t6 = new JsonParser("\"\\n\"", mockLogger);
        assertEquals("Extracted value", "\n", t6.getString());
    }

	@Test
    public void testTokenizeAStringVII() throws JsonException {
        JsonParser t7 = new JsonParser("\"\\b\"", mockLogger);
        assertEquals("Extracted value", "\b", t7.getString());
    }

	@Test
    public void testTokenizeAStringVIII() throws JsonException {
        JsonParser t8 = new JsonParser("\"\\f\"", mockLogger);
        assertEquals("Extracted value", "\f", t8.getString());
    }

	@Test
    public void testTokenizeAStringIX() throws JsonException {
        JsonParser t9 = new JsonParser("\"\\t\"", mockLogger);
        assertEquals("Extracted value", "\t", t9.getString());
    }

	@Test
    public void testTokenizeAStringX() throws JsonException {
        JsonParser t10 = new JsonParser("\"Hello World\"", mockLogger);
        assertEquals("Extracted value", "Hello World", t10.getString());
    }

	@Test
    public void testTokenizeAStringXI() throws JsonException {
        JsonParser t11 = new JsonParser("\" Hello World \"", mockLogger);
        assertEquals("Extracted value", " Hello World ", t11.getString());
    }

	@Test
    public void testTokenizeANumberI() throws JsonException {
        JsonParser t0 = new JsonParser("12", mockLogger);
        assertEquals("Extracted value", new Double(12), t0.getNumber());
    }

	@Test
    public void testTokenizeANumberII() throws JsonException {
        JsonParser t1 = new JsonParser("12.21", mockLogger);
        assertEquals("Extracted value", new Double(12.21), t1.getNumber());
    }

	@Test
    public void testTokenizeANumberIII() throws JsonException {
        JsonParser t2 = new JsonParser("-1.2345678E90", mockLogger);
        assertEquals("Extracted value", new Double(-1.2345678E90), t2.getNumber());
    }

	@Test
    public void testTokenizeABooleanI() throws JsonException {
        JsonParser t0 = new JsonParser("true", mockLogger);
        assertEquals("Extracted value", Boolean.TRUE, t0.getBoolean());
    }

	@Test
    public void testTokenizeABooleanII() throws JsonException {
        JsonParser t1 = new JsonParser("false", mockLogger);
        assertEquals("Extracted value", Boolean.FALSE, t1.getBoolean());
    }

	@Test
    public void testTokenizeNull() throws JsonException {
        JsonParser t0 = new JsonParser("null", mockLogger);
        assertNull("Extracted value", t0.getObject());
    }

	@Test(expected=JsonException.class)
    public void testUnbalancedDelimitersI() throws JsonException {
        JsonParser t1 = new JsonParser("\"Hello", mockLogger);
        t1.getString();
    }

	@Test(expected=JsonException.class)
    public void testUnbalancedDelimitersII() throws JsonException {
        JsonParser t1 = new JsonParser("[true", mockLogger);
        t1.getJsonArray();
    }

	@Test(expected=JsonException.class)
    public void testUnbalancedDelimitersIII() throws JsonException {
        JsonParser t1 = new JsonParser("{\"key\":true", mockLogger);
        t1.getJsonObject();
    }

	@Test(expected=JsonException.class)
    public void testUnbalancedDelimitersIV() throws JsonException {
        JsonParser t1 = new JsonParser("[true,]", mockLogger);
        t1.getJsonArray();
    }

	@Test(expected=JsonException.class)
    public void testUnbalancedDelimitersV() throws JsonException {
        JsonParser t1 = new JsonParser("{\"key\":true,}", mockLogger);
        t1.getJsonObject();
    }

	@Test
    public void testUnbalancedDelimitersVI() throws JsonException {
        JsonParser t1 = new JsonParser("[]", mockLogger);
        t1.getJsonArray();
    }

	@Test
    public void testUnbalancedDelimitersVII() throws JsonException {
        JsonParser t1 = new JsonParser("{}", mockLogger);
        t1.getJsonObject();
    }

	@Test(expected=JsonException.class)
    public void testUnbalancedDelimitersVIII() throws JsonException {
        JsonParser t1 = new JsonParser("[", mockLogger);
        t1.getJsonArray();
    }

	@Test(expected=JsonException.class)
    public void testUnbalancedDelimitersIX() throws JsonException {
        JsonParser t1 = new JsonParser("{", mockLogger);
        t1.getJsonObject();
    }

	@Test
    public void testUnicodeCharactersI() throws JsonException {
        // Unicode character can in the stream
        JsonParser t1 = new JsonParser("\"key\u1234\"", mockLogger);
        t1.getString();
    }

	@Test(expected=JsonException.class)
    public void testUnicodeCharactersII() throws JsonException {
        // Escaped unicode characters are not yet processed
        JsonParser t1 = new JsonParser("\"key\\u1234\"", mockLogger);
        t1.getString();
    }

	@Test(expected=JsonException.class)
    public void testTokenizeFromCorruptedInputStreamI() throws JsonException {
        new JsonParser(new MockInputStream("---") {
            @Override
            public int read() throws IOException {
                throw new IOException("done in purpose");
            }
        });
    }

	@Test
	public void testMatchI() throws JsonException {
		// To cover that a small string will be totally reproduced in the JsonException message
		JsonParser t0 = new JsonParser("short string", mockLogger);
		t0.match('s');
	}

	@Test(expected=JsonException.class)
	public void testMatchII() throws JsonException {
		// To cover that only the last 32 characters will be reproduced in the JsonException message
		JsonParser t0 = new JsonParser("{\"key1:\":\"long long long long long long long long long long string\",\"key2\":1111", mockLogger);
		t0.getJsonObject();
	}

	@Test(expected=JsonException.class)
	public void testMatchIII() throws JsonException {
		// To cover that only the first 32 characters will be reproduced in the JsonException message
		JsonParser t0 = new JsonParser("{\"key1\":1111 \"key2:\":\"long long long long long long long long long long string\"}", mockLogger);
		t0.getJsonObject();
	}

	@Test(expected=JsonException.class)
	public void testMatchIV() throws JsonException {
		// To cover that only a part of 32 characters will be reproduced in the JsonException message
		JsonParser t0 = new JsonParser("{\"key1:\":\"long long long long long long long long long long string\",\"key2\":1111 \"key3:\":\"long long long long long long long long long long string\"}", mockLogger);
		t0.getJsonObject();
	}

	@Test
	public void testParseValueI() throws JsonException {
        JsonParser t1 = new JsonParser("[\'quote\']", mockLogger);
        t1.getJsonArray();
        JsonParser t2 = new JsonParser("[\"double-quote\"]", mockLogger);
        t2.getJsonArray();
	}

	@Test
	public void testParseValueII() throws JsonException {
        JsonParser t1 = new JsonParser("[{\"nested\":\"object\"}]", mockLogger);
        t1.getJsonArray();
	}

	@Test
	public void testParseValueIII() throws JsonException {
        JsonParser t1 = new JsonParser("[[\"nested array\"]]", mockLogger);
        t1.getJsonArray();
	}

	@Test
	public void testParseValueIV() throws JsonException {
        JsonParser t1 = new JsonParser("[true]", mockLogger);
        t1.getJsonArray();
        JsonParser t2 = new JsonParser("[false]", mockLogger);
        t2.getJsonArray();
	}

	@Test
	public void testParseValueV() throws JsonException {
        JsonParser t1 = new JsonParser("[null]", mockLogger);
        assertNull(t1.getJsonArray().getString(0));
	}

	@Test
	public void testParseValueVI() throws JsonException {
        JsonParser t1 = new JsonParser("[undefined]", mockLogger);
        assertNull(t1.getJsonArray().getString(0));
	}

	@Test
	public void testParseValueVII() throws JsonException {
        JsonParser t1 = new JsonParser("[0]", mockLogger);
        assertEquals(0, t1.getJsonArray().getLong(0));
        JsonParser t2 = new JsonParser("[256]", mockLogger);
        assertEquals(256, t2.getJsonArray().getLong(0));
        JsonParser t3 = new JsonParser("[3.1415]", mockLogger);
        assertEquals(3.1415, t3.getJsonArray().getDouble(0), 0);
        JsonParser t4 = new JsonParser("[1.1223e-234]", mockLogger);
        assertEquals(1.1223e-234, t4.getJsonArray().getDouble(0), 0);
	}

	@Test
	public void testParseStringI() throws JsonException {
        JsonParser t1 = new JsonParser("['\\\"']", mockLogger);
        assertEquals("\"", t1.getJsonArray().getString(0));
        JsonParser t2 = new JsonParser("['\\'']", mockLogger);
        assertEquals("'", t2.getJsonArray().getString(0));
	}

	@Test
	public void testParseStringII() throws JsonException {
        JsonParser t1 = new JsonParser("['\\b']", mockLogger);
        assertEquals("\b", t1.getJsonArray().getString(0));
        JsonParser t2 = new JsonParser("['\\f']", mockLogger);
        assertEquals("\f", t2.getJsonArray().getString(0));
	}

	@Test
	public void testParseStringIII() throws JsonException {
        JsonParser t1 = new JsonParser("['\\r']", mockLogger);
        assertEquals("\r", t1.getJsonArray().getString(0));
        JsonParser t2 = new JsonParser("['\\n']", mockLogger);
        assertEquals("\n", t2.getJsonArray().getString(0));
        JsonParser t3 = new JsonParser("['\\t']", mockLogger);
        assertEquals("\t", t3.getJsonArray().getString(0));
	}

	@Test
	public void testParseStringIV() throws JsonException {
        JsonParser t1 = new JsonParser("['\\\\']", mockLogger);
        assertEquals("\\", t1.getJsonArray().getString(0));
        JsonParser t2 = new JsonParser("['\\/']", mockLogger);
        assertEquals("/", t2.getJsonArray().getString(0));
	}

	@Test(expected=JsonException.class)
	public void testParseStringV() throws JsonException {
        JsonParser t1 = new JsonParser("['\\u0020']", mockLogger);
        t1.getJsonArray();
	}

	@Test(expected=JsonException.class)
	public void testParseStringVI() throws JsonException {
        JsonParser t1 = new JsonParser("['\\.']", mockLogger);
        t1.getJsonArray();
	}

	@Test
	public void testIsSpearatorI() throws JsonException {
		assertTrue(JsonParser.isSeparatorChar(' '));
	}

	@Test
	public void testIsSpearatorII() throws JsonException {
		assertTrue(JsonParser.isSeparatorChar('\t'));
	}

	@Test
	public void testIsSpearatorIII() throws JsonException {
		assertTrue(JsonParser.isSeparatorChar('\r'));
	}

	@Test
	public void testIsSpearatorIV() throws JsonException {
		assertTrue(JsonParser.isSeparatorChar('\n'));
	}

	@Test
	public void testIsNumberI() throws JsonException {
		assertTrue(JsonParser.isNumberChar('-'));
		assertTrue(JsonParser.isNumberChar('+'));
		assertTrue(JsonParser.isNumberChar('0'));
		assertTrue(JsonParser.isNumberChar('.'));
		assertTrue(JsonParser.isNumberChar('1'));
		assertTrue(JsonParser.isNumberChar('2'));
		assertTrue(JsonParser.isNumberChar('3'));
		assertTrue(JsonParser.isNumberChar('4'));
		assertTrue(JsonParser.isNumberChar('5'));
		assertTrue(JsonParser.isNumberChar('6'));
		assertTrue(JsonParser.isNumberChar('7'));
		assertTrue(JsonParser.isNumberChar('8'));
		assertTrue(JsonParser.isNumberChar('9'));
		assertTrue(JsonParser.isNumberChar('e'));
		assertTrue(JsonParser.isNumberChar('E'));
	}

	// See GenericJsonArrayTest.java for ordered list of values tokenizing
    // See JsonObjectTranferTest.java for collection of {key; value} pairs tokenizing
}
