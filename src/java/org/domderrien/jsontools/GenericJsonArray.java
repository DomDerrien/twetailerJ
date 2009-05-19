package org.domderrien.jsontools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: Provide documentation
 */
public class GenericJsonArray implements JsonArray {
    protected List<Object> arrayList = null;

    /**
     * Default constructor, useful to generate the array to be sent to the browser
     */
    public GenericJsonArray() {
        arrayList = new ArrayList<Object>();
    }

    /**
     * Constructor made of an existing list
     */
    public GenericJsonArray(List<Object> list) {
        arrayList = list;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    /**
     * Local  toString() providing a nicely indented view of the object, with one attribute per line
     * @param index Depth of the recursive call
     * @return Nicely formatted string
     */
    protected String toString(int index) {
        StringBuilder offset = new StringBuilder("  ");
        for (int i = 0; i < index; ++ i) {
            offset.append("  ");
        }
        StringBuilder output = new StringBuilder();
        output.append("JsonArray: ").append("[");
        int length = size();
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                output.append(", ");
            }
            output.append("\n").append(offset);
            Object value = arrayList.get(i);
            if (value instanceof GenericJsonObject) {
                output.append(((GenericJsonObject) value).toString(index + 1));
            }
            else if (value instanceof GenericJsonArray) {
                output.append(((GenericJsonArray) value).toString(index + 1));
            }
            else if (value instanceof String) {
                output.append("\"").append(value).append("\"");
            }
            else {
                output.append(value);
            }
        }
        output.append("\n").append(offset.substring(index * 2)).append("]");
        return output.toString();
    }

    public int size() {
        return arrayList.size();
    }

    public List<Object> getList() {
        return arrayList;
    }

    /** @see java.util.List#get(int)  */
    protected Object getObject(int index) {
        return arrayList.get(index);
    }

    public boolean getBoolean(int index) {
        Boolean value = (Boolean) getObject(index);
        return Boolean.TRUE.equals(value);
    }

    public long getLong(int index) {
        return ((Double) getObject(index)).longValue();
    }

    public double getDouble(int index) {
        return ((Double) getObject(index));
    }

    public String getString(int index) {
        return (String) getObject(index);
    }

    public JsonObject getJsonObject(int index) {
        return (JsonObject) getObject(index);
    }

    public JsonArray getJsonArray(int index) {
        return (JsonArray) getObject(index);
    }

    public JsonException getJsonException(int index) {
        return (JsonException) getObject(index);
    }

    /** @see java.util.List#add  */
    protected void add(Object object) {
        arrayList.add(object);
    }

    public void add(boolean value) {
        add(value ? Boolean.TRUE : Boolean.FALSE);
    }

    public void add(long value) {
        add(new Double(value));
    }

    public void add(double value) {
        add(new Double(value));
    }

    public void add(String value) {
        add((Object) value);
    }

    public void add(JsonObject value) {
        add((Object) value);
    }

    public void add(JsonArray value) {
        add((Object) value);
    }

    public void add(JsonException value) {
        add((Object) value);
    }

    /** @see java.util.List#set */
    protected void set(int index, Object object) {
        if (index == arrayList.size()) {
            arrayList.add(object);
            return;
        }
        arrayList.set(index, object);
    }

    public void set(int index, boolean value) {
        set(index, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public void set(int index, long value) {
        set(index, new Double(value));
    }

    public void set(int index, double value) {
        set(index, new Double(value));
    }

    public void set(int index, String value) {
        set(index, (Object) value);
    }

    public void set(int index, JsonObject value) {
        set(index, (Object) value);
    }

    public void set(int index, JsonArray value) {
        set(index, (Object) value);
    }

    public void set(int index, JsonException value) {
        set(index, (Object) value);
    }

    public void remove(int index) {
        arrayList.remove(index);
    }

    public void remove(Object value) {
        arrayList.remove(value);
    }

    public void removeAll() {
        arrayList.clear();
    }

    public void append(JsonArray additionalValues) {
        arrayList.addAll(additionalValues.getList());
    }

	public void toStream(OutputStream out, boolean isFollowed) throws IOException {
		Iterator<Object> it = arrayList.iterator();
        JsonSerializer.startArray(out);
		while (it.hasNext()) {
			Object value = it.next();
			if (value instanceof Boolean) {
				JsonSerializer.toStream(((Boolean) value).booleanValue(), out, it.hasNext());
			}
			else if (value instanceof Long) {
				JsonSerializer.toStream(((Long) value).longValue(), out, it.hasNext());
			}
			else if (value instanceof Double) {
				JsonSerializer.toStream(((Double) value).doubleValue(), out, it.hasNext());
			}
			else if (value instanceof String) {
				JsonSerializer.toStream((String) value, out, it.hasNext());
			}
			else if (value instanceof JsonObject) {
				((JsonObject) value).toStream(out, it.hasNext());
			}
			else { // if (value instanceof JsonArray) {
				((JsonArray) value).toStream(out, it.hasNext());
			}
		}
        JsonSerializer.endArray(out, isFollowed);
	}
}
