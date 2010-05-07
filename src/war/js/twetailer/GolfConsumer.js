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
        _getLabel = _commom.init(locale);

        // Attach the contextual menu to the DataGrid instance
        // Note: initialization code grabbed in the dojo test file: test_grid_tooltip_menu.html
        _grid = dijit.byId("demandList");
        dijit.byId("cellMenu").bindDomNode(_grid.domNode);
        _grid.onCellContextMenu = function(e) {
            _gridCellNode = e.cellNode;
            _gridRowIndex = e.rowIndex;
        };

        // Fetch
        var dfd = _common.loadRemoteDemands("Consumer"); // No modificationDate means "load all active Demands"
        dfd.addCallback(function(response) { _common.processDemandList(response.resources, _grid); });
    };

    /**
     * Open a dialog box with the attributes of the identified proposal.
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

        // TODO: fetch the form
        /*
        var proposalForm = dijit.byId("proposalForm");
        proposalForm.reset();

        // ...
        _loadProposal(proposalKey);
        // ...

        proposalForm.show();
        dijit.byId('proposal.price').focus();
        */
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
        // TODO: fetch the form
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
        var dfd = _common.loadRemoteDemands();
        dfd.addCallback(function(response) { dijit.byId("refreshButton").resetTimeout(); _common.processDemandList(response.resources, _grid); });
    };
})(); // End of the function limiting the scope of the private variables
