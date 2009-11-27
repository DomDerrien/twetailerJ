(function() { // To limit the scope of the private variables

    var module = dojo.provide("twetailer.Console");

    dojo.require("domderrien.i18n.LabelExtractor");

    var _labelExtractor;

    /**
     * Module initializer
     *
     * @param {String} baseFilename part of the localized resource bundle name
     * @param {String} ISO locale identifier
     * @param {Boolean} Indicator of the login page
     */
    module.init = function(baseFilename, locale, isLoginPage) {
        // Get the localized resource bundle
        _labelExtractor = domderrien.i18n.LabelExtractor.init(
                "twetailer",
                baseFilename,
                locale
            );

        // Show default module
        if (isLoginPage !== true) {
            // Identifies the sub module to load
            var moduleName = "";
            if (window.location.search != null) {
                var queryParams = dojo.queryToObject(window.location.search.slice(1)); // Ignore the leading question mark
                moduleName = queryParams["module"];
            }
            module.showModule(moduleName);
        }
    };

    module.showModule = function(moduleName) {
        switch (moduleName) {
        case "sale-associate":
            moduleName = "console-saleAssociate.jsp";
            break;
        case "consumer":
        default:
            moduleName = "console-consumer.jsp";
        }
        dijit.byId("centerZone").attr("href", "/html/" + moduleName);
    };

    // TODO:
    // - hide the sale associate link if the consumer is not a sale associate
    // - block showModule if the user is not a sale associate

})(); // End of the function limiting the scope of the private variables
