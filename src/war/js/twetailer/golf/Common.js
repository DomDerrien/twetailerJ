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

    /**
     * Formatter for the list of attached proposal keys.
     *
     * @param {Number[]} proposalKeys List of proposal keys.
     * @param {Number} rowIndex index of the data in the grid, used by the trigger launching the Proposal properties pane.
     * @param {Array} decoration Link definition wrapping a proposal key with:
     *                   ${0}: place holder for the proposalKey
     *                   ${1}: place holder for the rowIndex.
     * @return {String} Formatter list of one link per proposal key, a link opening a dialog with the proposal detail.
     */
    module.displayProposalKeys = function(proposalKeys, rowIndex, decoration) {
        if (!proposalKeys) {
            return '';
        }
        if (dojo.isArray(proposalKeys)) {
            var value = [], pK;
            var limit = proposalKeys.length;
            for (var idx = 0; idx < limit; idx++) {
                pK = proposalKeys[idx];
                value.push(dojo.string.substitute(decoration || '${0}', [pK, rowIndex]));
            }
            return value.join(' ');
        }
        return "<span class='invalidData' title='" + _getLabel('console', 'error_invalid_array') + "'>" + _getLabel('console', 'error_invalid_data') + '</span>';
    };
})(); // End of the function limiting the scope of the private variables
