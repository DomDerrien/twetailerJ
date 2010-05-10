(function() { // To limit the scope of the private variables

    var module = dojo.provide("twetailer.GolfCommon");

    dojo.require("domderrien.i18n.LabelExtractor");

    /* Set of local variables */
    var _getLabel,
        _proposals = {};

    /**
     * Initializer
     *
     * @param {String} locale Identifier of the chosen locale
     * @return {Function} Shortcut on the local function getting localized labels
     */
    module.init = function(locale) {

        // Get the localized resource bundle
        domderrien.i18n.LabelExtractor.init("twetailer", "master", locale);
        domderrien.i18n.LabelExtractor.init("twetailer", "console", locale);
        _getLabel = domderrien.i18n.LabelExtractor.getFrom;

        // Return the shortcut on domderrien.i18n.LabelExtractor.getFrom()
        return _getLabel;
    };

    /**
     * Date formatter
     *
     * @param {String} ISO representation of a Date, as generated by the back-end
     * @return {String} Simply formatted date
     */
    module.displayDate = function(serializedDate) {
        try {
            var dateObject = dojo.date.stamp.fromISOString(serializedDate);
            return dojo.date.locale.format(dateObject, {selector: "date"});
        }
        catch(ex) {
            console.log("displayDate('" + serializedDate + "') -- ex: " + ex.message);
            return "<span class='invalidData' title='" + _getLabel("console", "error_invalid_date", [serializedDate]) + "'>" + _getLabel("console", "error_invalid_data") + "</span>";
        }
    };

    /**
     * Date & Time formatter
     *
     * @param {String} ISO representation of a Date, as generated by the back-end
     * @return {String} Simply formatted date
     */
    module.displayDateTime = function(serializedDate) {
        try {
            dateObject = dojo.date.stamp.fromISOString(serializedDate);
            return dojo.date.locale.format(dateObject, {selector: "dateTime"});
        }
        catch(ex) {
            console.log("displayDateTime('" + serializedDate + "') -- ex: " + ex.message);
            return "<span class='invalidData' title='" + _getLabel("console", "error_invalid_date", [serializedDate]) + "'>" + _getLabel("console", "error_invalid_data") + "</span>";
        }
    };

    /**
     * Criteria formatter
     *
     * @param {String[]} List of criteria, as generated by the back-end
     * @return {String} Serialized criteria list
     */
    module.displayCriteria = function(criteria) {
        if (criteria == null) {
            return "";
        }
        if (dojo.isArray(criteria)) {
            return criteria.join(" ");
        }
        console.log("displayCriteria(" + criteria + ") is not an Array");
        return "<span class='invalidData' title='" + _getLabel("console", "error_invalid_array") + "'>" + _getLabel("console", "error_invalid_data") + "</span>";
    };

    var _proposalKeyDecoration = ["<a href='#' onclick='twetailer.GolfAssociate.displayProposalForm(", ",", ");return false;' title='", "'>", "</a> "];

    /**
     * Formatter for the list of attached proposal keys
     *
     * @param {Number[]} List of proposal keys
     * @return {String} Formatter list of one link per proposal key, a link opening a dialog with the proposal detail
     */
    module.displayProposalKeys = function(proposalKeys, rowIndex) {
        if (proposalKeys == null) {
            return "";
        }
        if (dojo.isArray(proposalKeys)) {
            var value = [];
            var limit = proposalKeys.length;
            for (var idx = 0; idx < limit; idx ++) {
                var pK = proposalKeys[idx];
                value.push(_proposalKeyDecoration[0]);
                value.push(rowIndex);
                value.push(_proposalKeyDecoration[1]);
                value.push(pK);
                value.push(_proposalKeyDecoration[2]);
                value.push(_getLabel("console", "ga_cmenu_viewProposal", [pK]));
                value.push(_proposalKeyDecoration[3]);
                value.push(pK);
                value.push(_proposalKeyDecoration[4]);
            }
            return value.join("");
        }
        console.log("displayProposalKeys(" + proposalKeys + ") is not an Array");
        return "<span class='invalidData' title='" + _getLabel("console", "error_invalid_array") + "'>" + _getLabel("console", "error_invalid_data") + "</span>";
    };

    /**
     * Load the demands modified after the given date from the back-end
     *
     * @param {String} pointOfView Identifier of the caller type, to be able to get the corresponding Demand view
     * @param {Date} lastModificationDate (Optional) Date to considered before returning the demands
     * @return {dojo.Deferred} Object that callers can use to attach callbacks and errbacks
     */
    module.loadRemoteDemands = function(pointOfView, lastModificationDate) {
        dijit.byId("demandListOverlay").show();
        var dfd = dojo.xhrGet({
            content: {
                pointOfView: pointOfView,
                lastModificationDate: lastModificationDate == null ? null : lastModificationDate
            },
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    // Deferred callback will process the list
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
                dijit.byId("demandListOverlay").hide();
                return response;
            },
            error: function(message, ioArgs) {
                if (ioArgs.xhr.status == 403) { // 403 == Forbidden
                    dijit.byId("demandListOverlay").hide();
                    alert("This is a page for Associates you can't do anything with. You're about to be redirected to the corresponding page for Consumers.");
                    window.location = "./";
                }
                else {
                    alert(message+"\nurl: "+ioArgs.url);
                    dijit.byId("demandListOverlay").hide();
                }
            },
            preventCache: true,
            url: "/API/Demand/"
        });
        return dfd;
    };

    /**
     * Callback processing a list of demands that should replace the current grid content
     *
     * @param {Demand[]} List of demands to insert into the grid
     * @param {DataGrid} Reference on the grid to fetch
     */
    module.processDemandList = function(resources, grid) {
        // Prepare the data store
        var demandStore = new dojo.data.ItemFileWriteStore({
            data: { identifier: 'key', items: resources }
        });
        // Fetch the grid with the data
        demandStore.fetch( {
            query : {},
            onComplete : function(items, request) {
                if (grid.selection !== null) {
                    grid.selection.clear();
                }
                grid.setStore(demandStore);
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); }
        });
    };

    /**
     * Tell of the specified proposal has been cached.
     *
     * @param {String} proposalKey Identifier of the proposal to load
     * @return {Boolean} <code>true</code> if the proposal is cached, <code>false</code> otherwise
     */
    module.isProposalCached = function(proposalKey) {
        return _proposals[proposalKey] != null;
    };

    /**
     * Get the specified proposal from the cache.
     *
     * @param {String} proposalKey Identifier of the proposal to load
     * @return {Proposal} Identified proposal if it exists, <code>null</code> otherwise;
     */
    module.getCachedProposal = function(proposalKey) {
        return _proposals[proposalKey];
    };

    /**
     * Load the identified proposal by its key from the remote back-end.
     *
     * @param {String} proposalKey Identifier of the proposal to load
     * @return {dojo.Deferred} Object that callers can use to attach callbacks and errbacks
     */
    module.loadRemoteProposal = function(proposalKey) {
        dijit.byId("proposalFormOverlay").show();
        var dfd = dojo.xhrGet({
            content: null,
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var resource = response.resource;
                    _proposals[proposalKey] = resource;
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
                dijit.byId("proposalFormOverlay").hide();
                return response;
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); dijit.byId("proposalFormOverlay").hide(); },
            url: "/API/Proposal/" + proposalKey
        });
        return dfd;
    };

    /**
     * Call the back-end to create or update a Proposal with the given attribute
     *
     * @param {Object} data Set of attributes built from the <code>form</code> embedded in the dialog box
     * @return {dojo.Deferred} Object that callers can use to attach callbacks and errbacks
     */
    module.updateRemoteProposal = function(data) {
        dijit.byId('demandListOverlay').show();
        var dfd = (data.key == null ? dojo.xhrPost : dojo.xhrPut)({
            headers: { "content-type": "application/json; charset=utf-8" },
            postData: dojo.toJson(data),
            putData: dojo.toJson(data),
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var proposal = response.resource;
                    _proposals[proposal.key] = proposal;
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
                dijit.byId('demandListOverlay').hide();
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); dijit.byId("demandListOverlay").hide(); },
            url: "/API/Proposal/" + (data.key == null ? "" : data.key)
        });
        return dfd;
    };
})(); // End of the function limiting the scope of the private variables
