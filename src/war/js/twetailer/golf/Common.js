(function() { // To limit the scope of the private variables

    var module = dojo.provide('twetailer.golf.Common');

    dojo.require('twetailer.Common');

    /* Set of local variables */
    var _globalCommon = twetailer.Common,
        _getLabel;

    /**
     * Initializer.
     *
     * @param {String} locale Identifier of the chosen locale.
     * @return {Function} Shortcut on the local function getting localized labels.
     */
    module.init = function(locale) {
        _getLabel = _globalCommon.init(locale, 'detectLocationButton');

        // Return the shortcut on domderrien.i18n.LabelExtractor.getFrom()
        return _getLabel;
    };

    /**
     * Formatter extracting the number of pull cart from the metadata conveyed with the #golf demand.
     *
     * @param {Object} metadata Serialized JSON bag
     * return {Number} The number of pull carts stored in the JSON bag, or 0.
     */
    module.displayPullCartNb = function(metadata) {
        try {
            if (metadata) {
                return dojo.fromJson(metadata).pullCart;
            }
        }
        catch (ex) { alert(ex); }
        return 0;
    }

    /**
     * Formatter extracting the number of golf cart from the metadata conveyed with the #golf demand.
     *
     * @param {Object} metadata Serialized JSON bag
     * return {Number} The number of golf carts stored in the JSON bag, or 0.
     */
    module.displayGolfCartNb = function(metadata) {
        try {
            if (metadata) {
                return dojo.fromJson(metadata).golfCart;
            }
        }
        catch (ex) { alert(ex); }
        return 0;
    }
})(); // End of the function limiting the scope of the private variables
