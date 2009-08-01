package org.domderrien.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LabelExtractor
{
	static public final String ERROR_MESSAGE_PREFIX = "errMsg_";
	
    /**
     * Return the message associated to the given error code.
     * 
     * @param errorCode Error code
     * @param locale    Optional locale instance, use to determine in which
     *                  resource files the label should be found. If the
     *                  reference is <code>null</code>, the root resource
     *                  bundle is used (written in English).
     * @return A localized label associated to the given error code. If no
     *         association is found, the message identifier is returned.
     */
    public static String get(int errorCode, Locale locale) {
        return get(errorCode, null, locale);
    }
    
    /**
     * Return the message associated to the given error code.
     * 
     * @param errorCode Error code
     * @param parameters Array of parameters, each one used to replace a
     *                   pattern made of a number between curly braces.
     * @param locale    Optional locale instance, use to determine in which
     *                  resource files the label should be found. If the
     *                  reference is <code>null</code>, the root resource
     *                  bundle is used (written in English).
     * @return A localized label associated to the given error code. If no
     *         association is found, the message identifier is returned.
     */
    public static String get(int errorCode, Object[] parameters,  Locale locale) {
        String prefix = ERROR_MESSAGE_PREFIX;
        String label = get(prefix + errorCode, parameters, locale);
        if (label.startsWith(prefix)) {
            return String.valueOf(errorCode);
        }
        return label;
    }
    
    /**
     * Return the message associated to the given identifier.
     * 
     * @param messageId Identifier used to retrieve the localized label.
     * @param locale    Optional locale instance, use to determine in which
     *                  resource files the label should be found. If the
     *                  reference is <code>null</code>, the root resource
     *                  bundle is used (written in English).
     * @return A localized label associated to the given error code. If no
     *         association is found, the message identifier is returned.
     */
    public static String get(String messageId, Locale locale) {
        return get(messageId, null, locale);
    }

    /**
     * Return the message associated to the given identifier.
     * 
     * @param messageId  Identifier used to retrieve the localized label.
     * @param parameters Array of parameters, each one used to replace a
     *                   pattern made of a number between curly braces.
     * @param locale     Optional locale instance, use to determine in which
     *                   resource files the label should be found. If the
     *                   reference is <code>null</code>, the root resource
     *                   bundle is used (written in English).
     * @return A localized label associated to the given error code. If no
     *         association is found, the message identifier is returned.
     */
    public static String get(String messageId, Object[] parameters, Locale locale) {
        String label = messageId;
        if (messageId != null && 0 < messageId.length()) {
            try {
                ResourceBundle labels = getResourceBundle(locale);
                label = labels.getString(messageId);
            }
            catch (MissingResourceException ex) {
                // nothing
            }
        }
        return insertParameters(label, parameters);
    }
    
    /**
     * Utility method inserting the parameters into the given string
     * 
     * @param label      Text to process
     * @param parameters Array of parameters, each one used to replace a
     *                   pattern made of a number between curly braces.
     * @return Text where the place holders have been replaced by the
     *         toString() of the objects passed in the array. 
     *         
     * @see java.text.MessageFormat#format(String, Object[])
     */
    public static String insertParameters(String label, Object[] parameters) {
        if (label != null && parameters != null) {
            // Note Message.format(label, parameters) does NOT work.
            int paramNb = parameters.length;
            for (int i=0; i<paramNb; ++i) {
                String pattern = "\\{" + i + "\\}"; //$NON-NLS-1$ //$NON-NLS-2$
                label = label.replaceAll(pattern, parameters[i].toString());
            }
        }
        return label;
    }
    
    /*------------------------------------------------------------------*/
    /*------------------------------------------------------------------*/
    
    protected static HashMap<String, ResourceBundle> resourceBundles = new HashMap<String, ResourceBundle>();
    
    /**
     * Provides a reset mechanism for the unit test suite
     */
    protected static void resetResourceBundleList() {
    	resourceBundles.clear();
    }
    
    /**
     * Gives the string representing the locale or fall-back on the default one.
     * Made protected to be available for unit testing.
     * 
     * @param locale locale to use
     * @return String identifying the locale
     */
    protected static String getResourceBundleId(Locale locale) {
        return locale == null ?
                "root" : //$NON-NLS-1$
                locale.toString(); // Composes language + country (if country available)
    }
    
    /**
     * Protected setter made available for the unit testing
     * @param rb Mock resource bundle
     * @param locale Locale associated to the mock resource bundle
     */
    protected static void setResourceBundle(ResourceBundle rb, Locale locale) {
        String rbId = getResourceBundleId(locale);
        resourceBundles.put(rbId, rb);
    }
    
    /**
     * Retrieve the application resource bundle with the list of supported languages.
     * Specified protected only to ease the unit testing (IOP).
     * 
     * @param locale locale to use when getting the resource bundle
     * @return The already resolved/set resource bundle or the one expected at runtime
     * @throws MissingResourceException
     */
    protected static ResourceBundle getResourceBundle(Locale locale) throws MissingResourceException {
        String rbId = getResourceBundleId(locale);
        ResourceBundle rb = (ResourceBundle) resourceBundles.get(rbId);
        if (rb == null) {
        	// Get the resource bundle filename from the application settings and return the identified file
            ResourceBundle applicationSettings = ResourceBundle.getBundle("domderrien-i18n", locale); //$NON-NLS-1$
        	rb = ResourceBundle.getBundle(applicationSettings.getString("localizedLabelFilename"), locale);
            resourceBundles.put(rbId, rb);
        }
        return rb;
    }
}
