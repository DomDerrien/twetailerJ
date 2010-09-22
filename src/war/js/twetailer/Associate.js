(function() { // To limit the scope of the private variables

    var module = dojo.provide('twetailer.Associate');

    dojo.require('twetailer.Common');

    /* Set of local variables */
    var _common = twetailer.Common,
        _getLabel,
        _queryPointOfView = _common.POINT_OF_VIEWS.SALE_ASSOCIATE;

    /**
     * Initializer
     *
     * @param {String} locale Identifier of the chosen locale.
     */
    module.init = function(locale) {
        _getLabel = _common.init(locale);
    };
})(); // End of the function limiting the scope of the private variables
