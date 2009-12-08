(function() { // To limit the scope of the private variables

    /**
     * @author dom.derrien
     * @maintainer dom.derrien
     */
    var module = dojo.provide("domderrien.i18n.LabelExtractor");

    dojo.require("dojo.string");

    var _dictionary = {};
    var _defaultDictionary = null;

    /**
     * Initialize the library for the specified resource bundle
     *
     * @param {String} namespace JavaScript path containing a <code>nls</code>
     *                 folder with the localized resource bundles
     * @param {String} filename Base name of the resource bundles, with one
     *                 JavaScript file in <code>nls\&lt;iso&gt;</code> folder
     * @param {String} locale ISO code of the locale, used to load the right
     *                 resource bundles (dojo implements a fallback mechanism
     *                 if the corresponding localized bundle cannot be loaded)
     */
    module.init = function(/*String*/ namespace, /*String*/ filename, /*String*/ locale) {
        // Dojo uses dash-separated (e.g en-US not en_US) and uses lower case names (e.g en-us not en_US)
        locale = (locale || dojo.locale).replace('_','-').toLowerCase();

        // Load the bundle
        try {
            // Notes:
            // - Cannot use the notation <dojo>.<requireLocalization> because dojo parser
            //   will try to load the bundle when this file is interpreted, instead of
            //   waiting for a call with meaningful <namespace> and <filename> values
            dojo["requireLocalization"](namespace, filename, locale); // Blocking call getting the file per XHR or <iframe/>

            _dictionary[filename] = dojo.i18n.getLocalization(namespace, filename, locale);
            if (_defaultDictionary == null) {
                _defaultDictionary = filename;
            }
        }
        catch(ex) {
            module._reportError("Deployment issue:" +
                    "\nCannot get localized bundle " + namespace + "." + filename + " for the locale " + locale +
                    "\nMessage: " + ex
                );
        }

        return module;
    };

    /**
     * Return the message associated to the given identifier after a lookup
     * in the first initialized dictionary.
     *
     * @param {String} key  Identifier used to retrieve the localized label.
     * @param {String} args Array of parameters, each one used to replace a
     *                 pattern made of a number between curly braces.
     * @return A localized label associated to the given identifier. If no
     *         association is found, the message identifier is returned.
     */
    module.get = function(key, args) {
        return module.getFrom(_defaultDictionary, key, args);
    };

    /**
     * Return the message associated to the given identifier after a lookup
     * in the specified dictionary.
     *
     * @param {String} name Dictionary name.
     * @param {String} key  Identifier used to retrieve the localized label.
     * @param {String} args Array of parameters, each one used to replace a
     *                 pattern made of a number between curly braces.
     * @return A localized label associated to the given identifier. If no
     *         association is found, the message identifier is returned.
     */
    module.getFrom = function(name, key, args) {
        if (_dictionary[name] == null) {
            return key;
        }
        var message = _dictionary[name][key] || key;
        if (args != null) {
            message = dojo.string.substitute(message, args);
        }
        return message;
    };

    // Just provided to be able to control the environment during the unit tests
    module._resetDictionary = function(message) {
        _dictionary = {};
    };

    // Just provided to be able to control the error reporting during the unit tests
    module._reportError = function(message) {
        alert(message);
    };

})(); // End of the function limiting the scope of the private variables
