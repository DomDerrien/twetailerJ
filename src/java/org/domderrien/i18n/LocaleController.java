package org.domderrien.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class LocaleController
{
    /**
     * Default locale
     */
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    
    /**
     * Identifier of the default language
     */
    public static final String DEFAULT_LANGUAGE_ID = DEFAULT_LOCALE.getLanguage();
    
    /**
     * Identifier of the language parameter received with the request 
     */
    public static final String REQUEST_LOCALE_KEY = "lang"; //$NON-NLS-1$

    /**
     * Identifier of the language parameter saved in the HTTP session 
     */
    public static final String SESSION_LOCALE_ID_KEY = "localeId"; //$NON-NLS-1$
    
    private static ResourceBundle languageListRB = null;
    
    /**
     * Protected setter made available for the unit testing
     * @param rb Mock resource bundle
     */
    protected static void setLanguageListRB(ResourceBundle rb) {
        languageListRB = rb;
    }

    /**
     * Retrieve the application resource bundle with the list of supported languages.
     * Specified protected only to ease the unit testing (IOP).
     * 
     * @return The already resolved/set resource bundle or the one expected at runtime
     * @throws MissingResourceException
     */
    public static ResourceBundle getLanguageListRB() {
        if (languageListRB != null) {
        	return languageListRB;
        }
    	// Get the resource bundle filename from the application settings and return the identified file
        ResourceBundle applicationSettings = ResourceBundle.getBundle("applicationSettings", DEFAULT_LOCALE); //$NON-NLS-1$
    	return ResourceBundle.getBundle(applicationSettings.getString("languageListFilename"), DEFAULT_LOCALE); //$NON-NLS-1$

    }

    /**
     * Use the information contained by the HTTP request to get the current
     * language identifier. If the language identifier corresponds to one
     * entry in the resource bundle defining the list of supported languages,
     * the identifier is saved in the HTTP session for future usage.
     * 
     * @param request
     *            Http request, used to access the information stored into the
     *            Http session, and to save the verified information into it too.
     * @return One of the supported locale as documented in the file name with {applicationSettings[languageListFilename]}
     */
    public static Locale detectLocale(HttpServletRequest request) {
        // Locale retrieval
    	String localeId = getLocaleId(request, true);
    	Locale locale = getLocale(localeId);
        
        // Saving of the verified locale for the rest of the application
        HttpSession session = null; // FIXME: request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_LOCALE_ID_KEY, localeId);
        }
        
        return locale;
    }
    
    /**
     * Get the locale identifier from the session or fallback on the request
     * setting.
     * 
     * @param request
     *            Http request, used to access the information stored into the
     *            Http session
     * @return Valid language identifier
     */
    public static String getLocaleId(HttpServletRequest request) {
    	return getLocaleId(request, false);
    }
    
    /**
     * Get the locale identifier from the session or fallback on the request
     * setting.
     * 
     * @param request
     *            Http request, used to access the information stored into the
     *            Http session
     * @param skipSession to bypass possible value still stored into the HttpSession
     * @return Valid language identifier
     */
    protected static String getLocaleId(HttpServletRequest request, boolean skipSession) {
    	String localeId = null;

    	// Get the localeId from the request
        if (request != null) {
            HttpSession session = null; // FIXME: request.getSession(false);
            if (!skipSession && session != null) {
                // Get the registered information
                localeId = (String) session.getAttribute(SESSION_LOCALE_ID_KEY);
            }
            if (localeId == null || localeId.length() == 0) {
            	// Get the specified information
            	localeId = request.getParameter(REQUEST_LOCALE_KEY);
            }
            if (localeId == null || localeId.length() == 0) {
                // Fall back on the preferred locale, as specified in the request headers
                Locale locale = request.getLocale();
                if (locale != null) {
                    localeId = request.getLocale().getLanguage();
                    String countryId = request.getLocale().getCountry();
                    if (countryId != null && 0 < countryId.length()) {
                    	localeId += "_" + countryId;
                    }
                }
            }
        }
        
        // Verification that the locale is supported
	    String[] localeIdParts = localeId == null ? new String[] {DEFAULT_LANGUAGE_ID} : localeId.split("_");
	    String languageId = localeIdParts[0];
	    String countryId = 1 < localeIdParts.length ? localeIdParts[1] : null;
        ResourceBundle languageList = getLanguageListRB();
        try {
        	localeId = languageId + "_" + countryId;
            languageList.getString(localeId); // Will throw an exception if the identified language/country is not in the language list file
        }
        catch (java.util.MissingResourceException ex0) {
    	    try {
    	    	localeId = languageId;
    	        languageList.getString(localeId); // Will throw an exception if the identified language is not in the language list file
    	    }
    	    catch (java.util.MissingResourceException ex1) {
    			localeId = DEFAULT_LANGUAGE_ID;
    		}
    	}

        return localeId;
    }

    /**
     * Generate a Locale instance from the language identifier saved in the
     * session. The language identifier is resolved by the code into the
     * <code>login.jsp</code> page. If the language identifier is not found or
     * invalid, an English locale is generated.
     * 
     * @param request
     *            Http request, used to access the information stored into the
     *            Http session
     * @return Valid locale
     */
    public static Locale getLocale(HttpServletRequest request) {
        return getLocale(getLocaleId(request));
    }
    
    /**
     * Generate a Locale instance from the given language identifier.
     * If the language identifier is emtpy or null, an English locale is
     * generated. The validation, if any, of the language identifier is made
     * by the <code>Locale</code> constructor. The format of the language
     * identifier can be: <code>ll</code> / <code>ll-CC</code> /
     * <code>ll_CC</code>, with <code>ll</code> the two letters for the
     * language and <code>CC</code> the two letters for the country.
     *  
     * @param localeId
     *            Identifier of the language to interpret
     * @return Valid locale
     * @see java.util.Locale
     */
    public static Locale getLocale(String localeId) {
        Locale locale = DEFAULT_LOCALE;

        if (localeId != null) {
            if (2 < localeId.length()) {
                locale = new Locale(localeId.substring(0, 2), localeId.substring(3));
            }
            else {
                locale = new Locale(localeId);
            }
        }
        
        return locale;
    }
}
