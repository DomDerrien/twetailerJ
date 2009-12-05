(function() { // To limit the scope of the private variables

    /**
     * @author dom.derrien
     * @maintainer dom.derrien
     */
    var module = dojo.provide("domderrien.i18n.LabelExtractor");

    var _dictionary = {};
    var _defaultDictionary = null;

    module.init = function(namespace, filename, locale) {
        // Dojo uses dash-separated (e.g en-US not en_US) and uses lower case names (e.g en-us not en_US)
        locale = (locale || dojo.locale).replace('_','-').toLowerCase();

        // Load the bundle
        try {
            // Notes:
            // - Cannot use the notation <dojo>.<requireLocalization> because dojo parser
            //   will try to load the bundle when this file is interpreted, instead of
            //   waiting for a call which meaningful <namespace> and <filename> values
            dojo["requireLocalization"](namespace, filename, locale); // Blocking call getting the file per XHR or <iframe/>

            _dictionary[filename] = dojo.i18n.getLocalization(namespace, filename, locale);
            if (_defaultDictionary == null) {
                _defaultDictionary = filename;
            }
        }
        catch(ex) {
            alert("Deployment issue:" +
                    "\nCannot get localized bundle " + namespace + "." + filename + " for the locale " + locale +
                    "\nMessage: " + ex
                );
        }

        return module;
    };

    module.get = function(key, args) {
        return module.getFrom(_defaultDictionary);
    };

    module.getFrom = function(bundleName, key, args) {
        if (_dictionary[bundleName] == null) {
            return key;
        }
        var message = _dictionary[bundleName][key] || key;
        if (args != null) {
            message = dojo.string.substitute(message, args);
        }
        return message;
    };

})(); // End of the function limiting the scope of the private variables
