package org.domderrien.jsontools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface of a serializable ordered list of values.
 * <p>
 * This is the pending definition of the JavaScript array.
 * Many things can be inserted into the array (like in a {@link List} object),
 * To limit the insertion to serializable objects only, the class provided a
 * set of typed getters and setters.
 */
public interface JsonArray {
    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     * @see java.util.List#size
     */
    int size();

    /** Accessor */
    List<Object> getList();

    /**
     * Return the identified <code>boolean</code> value
     *
     * @param index Index should be in the range [0; length() -1]
     * @return <code>boolean</code> value located at the specified place
     * @see java.util.List#get
     */
    boolean getBoolean(int index);

    /**
     * Return the identified <code>long</code> value
     *
     * @param index Index should be in the range [0; length() -1]
     * @return <code>long</code> value located at the specified place
     * @see java.util.List#get
     */
    long getLong(int index);

    /**
     * Return the identified <code>double</code> value
     *
     * @param index Index should be in the range [0; length() -1]
     * @return <code>double</code> value located at the specified place
     * @see java.util.List#get
     */
    double getDouble(int index);

    /**
     * Return the identified <code>String</code> value
     *
     * @param index Index should be in the range [0; length() -1]
     * @return <code>String</code> value located at the specified place
     * @see java.util.List#get
     */
    String getString(int index);

    /**
     * Return the identified <code>JsonObject</code> value
     *
     * @param index Index should be in the range [0; length() -1]
     * @return <code>JsonObject</code> reference located at the specified place
     * @see java.util.List#get
     */
    JsonObject getJsonObject(int index);

    /**
     * Return the identified <code>JsonArray</code> value
     *
     * @param index Index should be in the range [0; length() -1]
     * @return <code>JsonArray</code> reference located at the specified place
     * @see java.util.List#get
     */
    JsonArray getJsonArray(int index);

    /**
     * Return the identified <code>JsonException</code> value
     *
     * @param index Index should be in the range [0; length() -1]
     * @return <code>JsonException</code> reference located at the specified place
     * @see java.util.List#get
     */
    JsonException getJsonException(int index);

    /**
     * Store the identified <code>boolean</code>
     *
     * @param value <code>boolean</code> value to store
     * @see java.util.List#add
     */
    void add(boolean value);

    /**
     * Store the identified <code>boolean</code>
     *
     * @param value <code>boolean</code> value to store
     * @see java.util.List#add
     */
    void add(Boolean value);

    /**
     * Store the identified <code>long</code>
     *
     * @param value <code>long</code> value to store
     * @see java.util.List#add
     */
    void add(long value);

    /**
     * Store the identified <code>long</code>
     *
     * @param value <code>long</code> value to store
     * @see java.util.List#add
     */
    void add(Long value);

    /**
     * Store the identified <code>double</code>
     *
     * @param value <code>double</code> value to store
     * @see java.util.List#add
     */
    void add(double value);

    /**
     * Store the identified <code>double</code>
     *
     * @param value <code>double</code> value to store
     * @see java.util.List#add
     */
    void add(Double value);

    /**
     * Store the identified String instance.
     *
     * @param value String to store
     * @see java.util.List#add
     */
    void add(String value);

    /**
     * Store the identified JsonObject instance.
     *
     * @param value Object to store
     * @see java.util.List#add
     */
    void add(JsonObject value);

    /**
     * Store the identified JsonArray instance.
     *
     * @param value Object to store
     * @see java.util.List#add
     */
    void add(JsonArray value);

    /**
     * Store the identified JsonException instance.
     *
     * @param value Object to store
     * @see java.util.List#add
     */
    void add(JsonException value);

    /**
     * Store the identified <code>boolean</code>
     *
     * @param index Index where the object should be stored
     * @param value <code>boolean</code> value to store
     * @see java.util.List#set
     */
    void set(int index, boolean value);

    /**
     * Store the identified <code>boolean</code>
     *
     * @param index Index where the object should be stored
     * @param value <code>boolean</code> value to store
     * @see java.util.List#set
     */
    void set(int index, Boolean value);

    /**
     * Store the identified <code>long</code>
     *
     * @param index Index where the object should be stored
     * @param value <code>long</code> value to store
     * @see java.util.List#set
     */
    void set(int index, long value);

    /**
     * Store the identified <code>long</code>
     *
     * @param index Index where the object should be stored
     * @param value <code>long</code> value to store
     * @see java.util.List#set
     */
    void set(int index, Long value);

    /**
     * Store the identified <code>double</code>
     *
     * @param index Index where the object should be stored
     * @param value <code>double</code> value to store
     * @see java.util.List#set
     */
    void set(int index, double value);

    /**
     * Store the identified <code>double</code>
     *
     * @param index Index where the object should be stored
     * @param value <code>double</code> value to store
     * @see java.util.List#set
     */
    void set(int index, Double value);

    /**
     * Store the identified String instance.
     *
     * @param index Index where the object should be stored
     * @param value String to store
     * @see java.util.List#set
     */
    void set(int index, String value);

    /**
     * Store the identified JsonObject instance.
     *
     * @param index Index where the object should be stored
     * @param value Object to store
     * @see java.util.List#set
     */
    void set(int index, JsonObject value);

    /**
     * Store the identified JsonArray instance.
     *
     * @param index Index where the object should be stored
     * @param value Object to store
     * @see java.util.List#set
     */
    void set(int index, JsonArray value);

    /**
     * Store the identified JsonException instance.
     *
     * @param index Index where the object should be stored
     * @param value Object to store
     * @see java.util.List#set
     */
    void set(int index, JsonException value);

    /**
     * Remove the identified value
     *
     * @param index Index where the object is stored
     * @see java.util.List#remove
     */
    void remove(int index);

    /**
     * Remove the identified value
     *
     * @param value <code>double</code> value to remove
     * @see java.util.List#remove
     */
    void remove(Object value);

    /**
     * Removes all mappings from this JsonArray
     *
     * @see java.util.List#clear
     */
    void removeAll();

    /**
     * Append the values of the given array to the current one
     * @param additionalValues values to add to the current array
     */
    void append(JsonArray additionalValues);
	
	/**
	 * Serialize the object on the output stream
	 * 
	 * @param out Output stream
	 * @param isFollowed <code>true</code> if the object is not the last of its set
	 * 
	 * @throws IOException If there is a problem during the serialization
	 */
	void toStream(OutputStream out, boolean isFollowed) throws IOException;
}
