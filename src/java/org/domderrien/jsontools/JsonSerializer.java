package org.domderrien.jsontools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class JsonSerializer {

	private static final String QUOTE_BACKSLASH_PATTERN_LITERAL = "([\\''\\\\])";
	private static final String QUOTE_BACKSLASH_REPLACEMENT_LITERAL = "\\\\$1";
	private static final Pattern QUOTE_BACKSLASH_PATTERN = Pattern.compile(QUOTE_BACKSLASH_PATTERN_LITERAL);
	
	protected static String escape(String data) {
		return QUOTE_BACKSLASH_PATTERN.matcher(data).replaceAll(QUOTE_BACKSLASH_REPLACEMENT_LITERAL);
	}

	private final static String UTF8 = "utf-8";
	
	protected static byte[] getBytes(String data) throws IOException {
		return data.getBytes(UTF8);
	}

	protected static void startObject(OutputStream out) throws IOException {
		out.write(JsonDelimiters.OPENING_BRACE_STR.getBytes(UTF8));
	}

	protected static void endObject(OutputStream out, boolean isFollowed) throws IOException {
		out.write((isFollowed ? JsonDelimiters.CLOSING_BRACE_STR + JsonDelimiters.COMMA_STR : JsonDelimiters.CLOSING_BRACE_STR).getBytes(UTF8));
	}

	public static void startArray(OutputStream out) throws IOException {
		out.write(JsonDelimiters.OPENING_SQUARE_BRACKET_STR.getBytes(UTF8));
	}

	public static void endArray(OutputStream out, boolean isFollowed) throws IOException {
		out.write((isFollowed ? JsonDelimiters.CLOSING_SQUARE_BRACKET_STR + JsonDelimiters.COMMA_STR : JsonDelimiters.CLOSING_SQUARE_BRACKET_STR).getBytes(UTF8));
	}

	public static void startObject(String key, String value, OutputStream out, boolean isFollowed) throws IOException {
		startObject(out);
		toStream(key, value, out, isFollowed);
	}

	public static void startObject(String key, long value, OutputStream out, boolean isFollowed) throws IOException {
		startObject(out);
		toStream(key, value, out, isFollowed);
	}

	public static void startObject(String key, boolean value, OutputStream out, boolean isFollowed) throws IOException {
		startObject(out);
		toStream(key, value, out, isFollowed);
	}

	public static void endObject(String key, String value, OutputStream out, boolean isFollowed) throws IOException {
		toStream(key, value, out, false);
		endObject(out, isFollowed);
	}

	public static void endObject(String key, int value, OutputStream out, boolean isFollowed) throws IOException {
		toStream(key, value, out, false);
		endObject(out, isFollowed);
	}

	private final static String KEY_STRING_VALUE_SEPARATOR = JsonDelimiters.QUOTE_STR + JsonDelimiters.COLONS_STR + JsonDelimiters.QUOTE_STR;
	private final static String KEY_OTHER_VALUE_SEPARATOR = JsonDelimiters.QUOTE_STR + JsonDelimiters.COLONS_STR;

	public static void introduceComplexValue(String key, OutputStream out) throws IOException {
        out.write(getBytes(
				JsonDelimiters.QUOTE_STR + 
				escape(key) + 
				KEY_OTHER_VALUE_SEPARATOR
		));
	}

	public static void toStream(String key, String value, OutputStream out, boolean isFollowed) throws IOException {
		out.write(getBytes(
				JsonDelimiters.QUOTE_STR + 
				escape(key) + 
				KEY_STRING_VALUE_SEPARATOR + 
				escape(value) + 
				JsonDelimiters.QUOTE_STR +
				(isFollowed ? JsonDelimiters.COMMA_STR : JsonDelimiters.EMPTY_STR)
		));
	}

	public static void toStream(String key, long value, OutputStream out, boolean isFollowed) throws IOException {
		out.write(getBytes(
				JsonDelimiters.QUOTE_STR + 
				escape(key) + 
				KEY_OTHER_VALUE_SEPARATOR + 
				value + 
				(isFollowed ? JsonDelimiters.COMMA_STR : JsonDelimiters.EMPTY_STR)
		));
	}

	public static void toStream(String key, boolean value, OutputStream out, boolean isFollowed) throws IOException {
		out.write(getBytes(
				JsonDelimiters.QUOTE_STR + 
				escape(key) + 
				KEY_OTHER_VALUE_SEPARATOR + 
				(value ? "true" : "false") + 
				(isFollowed ? JsonDelimiters.COMMA_STR : JsonDelimiters.EMPTY_STR)
		));
	}

	public static void toStream(String value, OutputStream out, boolean isFollowed) throws IOException {
		out.write(getBytes(
				JsonDelimiters.QUOTE_STR + 
				escape(value) + 
				JsonDelimiters.QUOTE_STR +
				(isFollowed ? JsonDelimiters.COMMA_STR : JsonDelimiters.EMPTY_STR)
		));
	}

	public static void toStream(long value, OutputStream out, boolean isFollowed) throws IOException {
		out.write(getBytes(
				value + 
				(isFollowed ? JsonDelimiters.COMMA_STR : JsonDelimiters.EMPTY_STR)
		));
	}

	public static void toStream(boolean value, OutputStream out, boolean isFollowed) throws IOException {
		out.write(getBytes(
				(value ? "true" : "false") + 
				(isFollowed ? JsonDelimiters.COMMA_STR : JsonDelimiters.EMPTY_STR)
		));
	}
}
