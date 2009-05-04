package org.domderrien.jsontools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TODO: Provide documentation
 */
public class GenericJsonObject implements JsonObject {
    protected Map<String, Object> hashMap;

    /**
     * Default constructor, useful to generate the object to be sent to the browser
     */
    public GenericJsonObject() {
        hashMap = new HashMap<String, Object>();
    }

    /**
     * Constructor made of an existing map
     */
    public GenericJsonObject(Map<String, Object> map) {
        hashMap = map;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    /**
     * Local toString() providing a nicely indented view of the object, with one attribute per line
     *
     * @param index Depth of the recursive call
     * @return Nicely formatted string
     */
    protected String toString(int index) {
        StringBuilder offset = new StringBuilder("  ");
        for (int i = 0; i < index; ++ i) {
            offset.append("  ");
        }
        StringBuilder output = new StringBuilder();
        output.append("JsonObject: ").append("{");
        boolean firstItem = true;
        for (String key : hashMap.keySet()) {
            if (!firstItem) {
                output.append(", ");
            }
            else {
                firstItem = false;
            }
            output.append("\n").append(offset).append(key).append(": ");
            Object value = hashMap.get(key);
            if (value instanceof GenericJsonObject) {
                output.append(((GenericJsonObject) value).toString(index + 1));
            }
            else if (value instanceof GenericJsonArray) {
                output.append(((GenericJsonArray) value).toString(index + 1));
            }
            else if (value instanceof String) {
            	output.append("{String}");
                output.append("\"").append(value).append("\"");
            }
            else if (value instanceof String[]) {
            	output.append("{String[]}");
            	String[] arrayOfStrings = (String[]) value;
            	int limit = arrayOfStrings.length;
            	if (limit == 0) {
            		output.append("null");
            	}
            	else if (limit == 1) {
            		output.append("\"").append(arrayOfStrings[0]).append("\"");
            	}
            	else {
    				output.append("[");
            		for (int i=0; i<limit; i++) {
            			output.append("\"").append(arrayOfStrings[0]).append("\"");
            			if (i != limit) {
            				output.append(",");
            			}
            		}
    				output.append("]");
            	}
            }
            else {
            	output.append("{other}");
                output.append(value);
            }
        }
        output.append("\n").append(offset.substring((index) * 2)).append("}");
        return output.toString();
    }

    public int size() {
        return hashMap.size();
    }

    public Map<String, Object> getMap() {
        return hashMap;
    }

    public boolean isNonNull(String key) {
        return hashMap.get(key) != null;
    }

    public boolean containsKey(String key) {
        return hashMap.containsKey(key);
    }

    /** @see java.util.Map#get */
    protected Object getObject(String key) {
        return hashMap.get(key);
    }

    public boolean getBoolean(String key) throws ClassCastException {
        Object value = getObject(key);
        Boolean typedValue = Boolean.FALSE;
        if (value instanceof String[]) {
        	String[] arrayOfStrings = (String[]) value;
        	if (arrayOfStrings.length == 0) {
        		typedValue = Boolean.parseBoolean(arrayOfStrings[0]);
        	}
        }
        else {
        	typedValue = (Boolean) value; 
        }
        return Boolean.TRUE.equals(typedValue);
    }

    public long getLong(String key) throws ClassCastException {
        Object value = getObject(key);
        Long typedValue = 0L;
        if (value instanceof String[]) {
        	String[] arrayOfStrings = (String[]) value;
        	if (arrayOfStrings.length == 0) {
        		typedValue = Long.parseLong(arrayOfStrings[0]);
        	}
        }
        else {
        	typedValue = value == null ? 0L : (Long) value; 
        }
        return typedValue.longValue();
    }

    public double getDouble(String key) throws ClassCastException, NumberFormatException {
        Object value = getObject(key);
        Double typedValue = 0.0D;
        if (value instanceof String[]) {
        	String[] arrayOfStrings = (String[]) value;
        	if (arrayOfStrings.length == 0) {
        		typedValue = Double.parseDouble(arrayOfStrings[0]);
        	}
        }
        else {
        	typedValue = value == null ? 0.0D : (Double) value; 
        }
        return typedValue.doubleValue();
    }

    public String getString(String key) throws ClassCastException {
        Object value = getObject(key);
        String typedValue = null;
        if (value instanceof String[]) {
        	String[] arrayOfStrings = (String[]) value;
        	typedValue = arrayOfStrings.length == 0 ? null : arrayOfStrings[0];
        }
        else {
        	typedValue = (String) value;
        }
        return typedValue;
    }

    public JsonObject getJsonObject(String key) throws ClassCastException {
        Object value = getObject(key);
        if (value instanceof String[]) {
        	throw new ClassCastException("Cannot extract a JsonObject from an array of String instance");
        }
        return (JsonObject) value;
    }

    public JsonArray getJsonArray(String key) throws ClassCastException {
        Object value = getObject(key);
        JsonArray typedValue = null;
        if (value instanceof String[]) {
        	String[] arrayOfStrings = (String[]) value;
        	typedValue = new GenericJsonArray();
        	for (int i=0; i<arrayOfStrings.length; i++) {
        		typedValue.add(arrayOfStrings[i]);
        	}
        }
        else {
        	typedValue = (JsonArray) value; 
        }
        return typedValue;
    }

    public JsonException getJsonException(String key) throws ClassCastException {
        return (JsonException) getObject(key);
    }

    /** @see java.util.Map#put */
    protected void putArbitrary(String key, Object object) {
        hashMap.put(key, object);
    }

    public void put(String key, boolean value) {
        putArbitrary(key, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public void put(String key, long value) {
        putArbitrary(key, new Long(value));
    }

    public void put(String key, double value) {
        putArbitrary(key, new Double(value));
    }

    public void put(String key, String value) {
        putArbitrary(key, (Object) value);
    }

    public void put(String key, JsonObject value) {
        putArbitrary(key, (Object) value);
    }

    public void put(String key, JsonArray value) {
        putArbitrary(key, (Object) value);
    }

    public void put(String key, JsonException value) {
        putArbitrary(key, (Object) value);
    }

    public void remove(String key) {
        this.hashMap.remove(key);
    }

    public void removeAll() {
        this.hashMap.clear();
    }

    public void append(JsonObject additionalValues) {
        hashMap.putAll(additionalValues.getMap());
    }

	public void toStream(OutputStream out, boolean isFollowed) throws IOException {
		Iterator<String> it = hashMap.keySet().iterator();
        JsonSerializer.startObject(out);
		while (it.hasNext()) {
			String key = it.next();
			Object value = hashMap.get(key);
			if (value instanceof Boolean) {
				JsonSerializer.toStream(key, (Boolean) value, out, it.hasNext());
			}
			else if (value instanceof Long) {
				JsonSerializer.toStream(key, (Long) value, out, it.hasNext());
			}
			else if (value instanceof String) {
				JsonSerializer.toStream(key, (String) value, out, it.hasNext());
			}
			else if (value instanceof JsonObject) {
				JsonSerializer.introduceComplexValue(key, out);
				((JsonObject) value).toStream(out, isFollowed);
			}
			else if (value instanceof JsonArray) {
				JsonSerializer.introduceComplexValue(key, out);
				((JsonArray) value).toStream(out, isFollowed);
			}
		}
        JsonSerializer.endObject(out, isFollowed);
	}
}
