if (!dojo._hasResource["domderrien.i18n.LabelExtractor"]) {
	dojo._hasResource["domderrien.i18n.LabelExtractor"] = true;

	(function() { // To limit the scope of the private variables

		/**
		 * @author dom.derrien
		 * @maintainer dom.derrien 
		 */
		var module = dojo.provide("domderrien.i18n.LabelExtractor");
		
		var _dictionnary = null;

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
			   	
				_dictionary = dojo.i18n.getLocalization(namespace, filename, locale);
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
			if (_dictionary == null) {
				return key;
			}
		    var message = _dictionary[key] || key;
		    if (args != null) {
		        dojo.string.substituteParams(message, args);
		    }
		    return message;
		};

	})(); // End of the function limiting the scope of the private variables
}
