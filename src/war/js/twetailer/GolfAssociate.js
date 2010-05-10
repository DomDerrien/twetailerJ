(function() { // To limit the scope of the private variables

    var module = dojo.provide("twetailer.GolfAssociate");

    dojo.require("twetailer.GolfCommon");

    /* Set of local variables */
    var _common,
        _getLabel,
        _grid,
        _gridCellNode,
        _gridRowIndex;

    /**
     * Initializer
     *
     * @param {String} locale Identifier of the chosen locale
     */
    module.init = function(locale) {
        _common = twetailer.GolfCommon;
        _getLabel = _common.init(locale);

        // Attach the contextual menu to the DataGrid instance
        // Note: initialization code grabbed in the dojo test file: test_grid_tooltip_menu.html
        _grid = dijit.byId("demandList");
        dijit.byId("demandListCellMenu").bindDomNode(_grid.domNode);
        _grid.onCellContextMenu = function(e) {
            _gridCellNode = e.cellNode;
            _gridRowIndex = e.rowIndex;
        };

        // Fetch
        var dfd = _common.loadRemoteDemands("SA"); // No modificationDate means "load all active Demands"
        dfd.addCallback(function(response) { _common.processDemandList(response.resources, _grid); });
    };

    /**
     * Open a dialog box with the attributes of the identified proposal. If there's no
     * proposalKey, the variable set by the contextual menu handler for the Demand grid
     * is used to identified a selected grid row and to propose a dialog for a new
     * proposal creation.
     *
     * @param {Number} proposedRowIndex (Optional) index given when a link on the proposal key is activated
     * @param {Number} proposalKey (Optional) index given when a link on the proposal key is activated
     */
    module.displayProposalForm = function(proposedRowIndex, proposalKey) {
        // rowIndex bind to the handler
        if (proposedRowIndex == null) {
            if (_gridRowIndex === null) {
                return;
            }
            proposedRowIndex = _gridRowIndex;
        }

        var item = _grid.getItem(proposedRowIndex);
        if (item === null) {
            return;
        }

        var proposalForm = dijit.byId("proposalForm");
        proposalForm.reset();

        dijit.byId("demand.key").attr("value", item.key);
        dijit.byId("demand.criteria").attr("value", item.criteria.join(" "));
        dijit.byId("demand.quantity").attr("value", item.quantity);

        if (proposalKey == null) {
            proposalForm.attr("title", _getLabel("console", "ga_cmenu_createProposal"));
            dijit.byId("proposalFormSubmitButton").attr("label", _getLabel("console", "create_button"));
            dijit.byId("proposalFormCancelButton").attr("disabled", true);
            dojo.query(".existingProposalAttribute").style("display", "none");
        }
        else {
            _loadProposal(proposalKey);
            proposalForm.attr("title", _getLabel("console", "ga_cmenu_viewProposal", [proposalKey]));
            dijit.byId("proposalFormSubmitButton").attr("label", _getLabel("console", "update_button"));
            dijit.byId("proposalFormCancelButton").attr("disabled", false);
            dojo.query(".existingProposalAttribute").style("display", "");
            dijit.byId("proposal.key").attr("value", proposalKey);
        }
        proposalForm.show();
        dijit.byId('proposal.price').focus();
    };

    /**
     * Load the identified proposal by its key from a local cache or from the remote back-end.
     * The control is passed to the <code>_fetchProposal()</code> for the update of dialog box
     * with the Proposal attributes.
     *
     * @param {String} proposalKey Identifier of the proposal to load
     */
    var _loadProposal = function(proposalKey) {
        if (_common.isProposalCached(proposalKey)) {
            _fetchProposal(_common.getCachedProposal(proposalKey));
        }
        else {
            var dfd = _common.loadRemoteProposal(proposalKey);
            dfd.addCallback(function(response) { _fetchProposal(_common.getCachedProposal(proposalKey)); });
        }
    };

    /**
     * Use the given Proposal to fetch the corresponding dialog box
     *
     * @param {Proposal} proposal Object to represent
     */
    var _fetchProposal = function(proposal) {
        var time = null;
        var additionalInformation = [];
        var criteria = proposal.criteria;
        var limit = criteria.length;
        for (var idx = 0; idx < limit; idx++) {
            var criterion = criteria[idx];
            if (criterion == "time:") {
                idx ++;
                time = _getTime(criteria[idx]);
            }
            else if (criterion.substr(0, "time:".length) == "time:") {
                time = _getTime(criterion.substring("time:".length, criterion.length));
            }
            else {
                additionalInformation.push(criterion);
            }
        }
        if (time != null) {
            dijit.byId("proposal.time").attr("value", time);
        }
        dijit.byId("proposal.state").attr("value", proposal.state);
        dijit.byId("proposal.price").attr("value", proposal.price);
        dijit.byId("proposal.total").attr("value", proposal.total);
        // TODO: skip the date part ;)
        dijit.byId("proposal.criteria").attr("value", additionalInformation.join(" "));
        dijit.byId("proposal.modificationDate").attr("value", _common.displayDateTime(proposal.modificationDate));
    };

    /**
     * Helper extracting the time in 24 or AM/PM
     */
    var _getTime = function(time) {
        var hour = parseInt(time.substr(0, 2));
        var minute = parseInt(time.substr("00:".length, 2));
        if (isNaN(hour) || isNaN(minute)) {
            return null;
        }
        if (time.charAt("00:00".length) != ':') {
            if (time.charAt("00:00".length) == 'p') {
                hour += 12;
            }
            if (time.charAt("00:00".length) == ' ' && time.charAt("00:00 ".length) == 'p') {
                hour += 12;
            }
        }
        return new Date(1970,0,1,parseInt(hour),parseInt(minute),0,0);
    };

    /**
     * Call the back-end to create or update a Proposal with the given attribute
     *
     * @param {Object} data Set of attributes built from the <code>form</code> embedded in the dialog box
     */
    module.updateProposal = function(data) {
        data.key = isNaN(data.key) ? null : data.key;
        data.total = isNaN(data.total) ? null : data.total;
        data.criteria = data.criteria.split(" ");
        data.criteria.push("time:" + data.time.toString().replace(/.*1970\s(\S+).*/,"$1"));

        var dfd = _common.updateRemoteProposal(data);
        dfd.addCallback(function(response) { module.loadNewDemands() });
    };

    /**
     * Call the back-end to create or update a Proposal with the given attribute
     *
     * @param {Object} data Set of attributes built from the <code>form</code> embedded in the dialog box
     */
    module.cancelProposal = function() {
        var pK = dijit.byId("proposal.key").attr("value");
        var dK = dijit.byId("demand.key").attr("value");
        if (!confirm(_getLabel("console", "ga_alert_askConfirmationOfProposalCancelling", [pK, dK]))) {
            return;
        }

        dijit.byId("proposalFormOverlay").show();
        var dfd = _common.updateRemoteProposal({
            key: dijit.byId("proposal.key").attr("value"),
            state: "cancelled"
        });
        dfd.addCallback(function(response) { dijit.byId("proposalFormOverlay").hide(); dijit.byId("proposalForm").hide(); module.loadNewDemands() });
    };

    /**
     * Call the back-end to get the new Demands
     */
    module.loadNewDemands = function() {
        var dfd = _common.loadRemoteDemands("SA");
        dfd.addCallback(function(response) { dijit.byId("refreshButton").resetTimeout(); _common.processDemandList(response.resources, _grid); });
    };
})(); // End of the function limiting the scope of the private variables
