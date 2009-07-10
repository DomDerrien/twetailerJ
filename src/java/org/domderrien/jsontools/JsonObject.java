package org.domderrien.jsontools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Interface of a serializable collection  of key/value pairs.
 * <p>
 * This is the pending definition of the JavaScript object.
 * Many things can be inserted into the object (like in a {@link Map} object),
 * To limit the insertion to serializable objects only, the class provided a
 * set of typed getters and setters.
 */
public interface JsonObject {
	
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     * @see java.util.Map#size
     */
    int size();

    /** Accessor */
    Map<String, Object> getMap();

    /**
     * Returns <code>true</code> if this object points a to non null value.
     *
     * @param key Data identifier
     * @return <code>true</code> if the value is non null
     */
    boolean isNonNull(String key);

    /**
     * Returns <code>true</code> if this object maps one or more keys to the specified value.
     *
     * @param key Data identifier
     * @return <code>true</code> if the value is in the map
     * @see java.util.Map#containsKey
     */
    boolean containsKey(String key);

    /**
     * Return the identified <code>boolean</code> value
     *
     * @param key Data identifier
     * @return <code>boolean</code> value located at the specified place
     * @see java.util.Map#get(Object)
     */
    boolean getBoolean(String key) throws ClassCastException;

    /**
     * Return the identified <code>long</code> value
     *
     * @param key Data identifier
     * @return <code>long</code> value located at the specified place
     * @see java.util.Map#get
     */
    long getLong(String key) throws ClassCastException;

    /**
     * Return the identified <code>double</code> value
     *
     * @param key Data identifier
     * @return <code>double</code> value located at the specified place
     * @see java.util.Map#get
     */
    double getDouble(String key) throws ClassCastException, NumberFormatException;

    /**
     * Return the identified <code>Sting</code> value
     *
     * @param key Data identifier
     * @return <code>String</code> value located at the specified place
     * @see java.util.Map#get
     */
    String getString(String key) throws ClassCastException;

    /**
     * Return the identified <code>JsonObject</code> value
     *
     * @param key Data identifier
     * @return <code>JsonObject</code> reference located at the specified place
     * @see java.util.Map#get
     * @see java.util.Map#get
     */
    JsonObject getJsonObject(String key) throws ClassCastException;

    /**
     * Return the identified <code>JsonArray</code> value
     *
     * @param key Data identifier
     * @return <code>JsonArray</code> reference located at the specified place
     * @see java.util.Map#get
     */
    JsonArray getJsonArray(String key) throws ClassCastException;

    /**
     * Return the identified <code>JsonException</code> value
     *
     * @param key Data identifier
     * @return <code>JsonException</code> reference located at the specified place
     * @see java.util.Map#get
     */
    JsonException getJsonException(String key) throws ClassCastException;

    /**
     * Store the <code>boolean</code> value
     *
     * @param key   Data identifier
     * @param value <code>boolean</code> value to store
     * @see java.util.Map#put
     */
    void put(String key, boolean value);

    /**
     * Store the <code>boolean</code> value
     *
     * @param key   Data identifier
     * @param value <code>boolean</code> value to store
     * @see java.util.Map#put
     */
    void put(String key, Boolean value);

    /**
     * Store the <code>long</code> value
     *
     * @param key   Data identifier
     * @param value <code>long</code> value to store
     * @see java.util.Map#put
     */
    void put(String key, Long value);
    
    /**
     * Store the <code>long</code> value
     *
     * @param key   Data identifier
     * @param value <code>long</code> value to store
     * @see java.util.Map#put
     */
    void put(String key, long value);
    
    /**
     * Store the <code>double</code> value
     *
     * @param key   Data identifier
     * @param value <code>double</code> value to store
     * @see java.util.Map#put
     */
    void put(String key, Double value);

    /**
     * Store the <code>double</code> value
     *
     * @param key   Data identifier
     * @param value <code>double</code> value to store
     * @see java.util.Map#put
     */
    void put(String key, double value);

    /**
     * Store the String instance
     *
     * @param key   Data identifier
     * @param value String instance to store
     * @see java.util.Map#put
     */
    void put(String key, String value);

    /**
     * Store the JsonObject instance
     *
     * @param key   Data identifier
     * @param value JsonObject instance to store
     * @see java.util.Map#put
     */
    void put(String key, JsonObject value);

    /**
     * Store the JsonArray instance
     *
     * @param key   Data identifier
     * @param value JsonArray instance to store
     * @see java.util.Map#put
     */
    void put(String key, JsonArray value);

    /**
     * Store the JsonException instance
     *
     * @param key   Data identifier
     * @param value JsonException instance to store
     * @see java.util.Map#put
     */
    void put(String key, JsonException value);

    /**
     * Removes the mapping for this key from this map if it is present
     *
     * @param key   Data identifier
     * @see java.util.Map#remove
     */
    void remove(String key);

    /**
     * Removes all mappings from this JsonObject
     *
     * @see java.util.Map#clear
     */
    void removeAll();

    /**
     * Append the values of the given list to the current one. Note that
     * existing values in the current list are overriden.
     *
     * @param additionalValues values to add to the current list
     * @see java.util.Map#putAll
     */
    void append(JsonObject additionalValues);
	
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
