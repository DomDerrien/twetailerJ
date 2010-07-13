(function() { // To limit the scope of the private variables

    var module = dojo.provide("twetailer.GolfConsumer");

    dojo.require("twetailer.GolfCommon");

    /* Set of local variables */
    var _common = twetailer.GolfCommon,
        _getLabel,
        _grid,
        _gridCellNode,
        _gridRowIndex
        _queryPointOfView = _common.POINT_OF_VIEWS.CONSUMER;

    /**
     * Initializer
     *
     * @param {String} locale Identifier of the chosen locale
     */
    module.init = function(locale) {
        _getLabel = _common.init(locale);

        // Attach the contextual menu to the DataGrid instance
        // Note: initialization code grabbed in the dojo test file: test_grid_tooltip_menu.html
        _grid = dijit.byId("demandList");
        dijit.byId("demandListCellMenu").bindDomNode(_grid.domNode);
        _grid.onCellContextMenu = function(e) {
            _gridCellNode = e.cellNode;
            _gridRowIndex = e.rowIndex;
        };
        // _grid.setSortIndex(10, false); // 10 == position of the column 'modificationDate'

        // Fetch
        var dfd = _common.loadRemoteDemands(null, _queryPointOfView); // No modificationDate means "load all active Demands"
        dfd.addCallback(function(response) { _common.processDemandList(response.resources, _grid); });
    };

    var _demandUpdateDecoration = "<a href='#' onclick='twetailer.GolfConsumer.displayDemandForm(false,${0});return false;' title='${1}'><span class='dijitReset dijitInline silkIcon silkIconDemandUpdate'></span>${2}</a>";

    /**
     * Override of the formatter to be able to place the "Update Demand" link around the demand key
     *
     * @param {Number[]} demandKey identifier of the demand
     * @param {Number} rowIndex index of the data in the grid, used by the trigger launching the Proposal properties pane
     * @return {String} Formatter link with the Demand icon to open the Demand editor
     */
    module.displayDemandKey = function(demandKey, rowIndex) {
        // TODO: check the demand state in order to use the classname silkIconDemandConfirmed
        var updateLabel = dojo.string.substitute(
            _demandUpdateDecoration,
            [rowIndex, _getLabel("console", "ga_cmenu_updateDemand", [demandKey]), demandKey]
        );
        return updateLabel;
    };

    var _proposalViewDecoration = "<a href='#' onclick='twetailer.GolfConsumer.displayProposalForm(${1},${0});return false;' title='${2}'><span class='dijitReset dijitInline silkIcon silkIconProposalView'></span>${0}</a>";

    /**
     * Override of the formatter to be able to place the "Create Proposal" link in front of the proposal key list
     *
     * @param {Number[]} proposalKeys List of proposal keys
     * @param {Number} rowIndex index of the data in the grid, used by the trigger launching the Proposal properties pane
     * @return {String} Formatter list of one link per proposal key, a link opening a dialog with the proposal detail
     */
    module.displayProposalKeys = function(proposalKeys, rowIndex) {
        // TODO: check the demand state in order to use the classname silkIconProposalConfirmed
        if (proposalKeys == null || proposalKeys.length == 0) {
            return "";
        }
        var viewLabel = dojo.string.substitute(
            _proposalViewDecoration,
            ["${0}", "${1}", _getLabel("console", "ga_cmenu_viewProposal")]
        );
        return _common.displayProposalKeys(proposalKeys, rowIndex, viewLabel);
    };

    /**
     * Open a dialog box with the attributes of the identified proposal.
     *
     * @param {Boolean} freshForm should be <code>true</code> if the form is for a new Demand
     * @param {Number} proposedRowIndex (Optional) index given by the contextual icon link
     */
    module.displayDemandForm = function(freshForm, proposedRowIndex) {
        var demandForm = dijit.byId("demandForm");
        demandForm.reset();

        if (freshForm) {
            var now = new Date();
            var tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            dijit.byId("demand.date").attr("value", tomorrow);
            dijit.byId("demand.date").constraints.min = now;
            var lastDemand = _common.getLastDemand();
            if (lastDemand != null) {
                _common.setLocation(lastDemand.locationKey[0], dijit.byId("demand.postalCode"), dijit.byId("demand.countryCode"));
                dijit.byId("demand.range").attr("value", lastDemand.range[0]);
                dijit.byId("demand.rangeUnit").attr("value", lastDemand.rangeUnit[0]);
            }
            dijit.byId("demandFormSubmitButton").attr("label", _getLabel("console", "ga_cmenu_createDemand"));

            dojo.query(".updateButton").style("display", "");
            dojo.query(".existingAttribute").style("display", "none");
            dojo.query(".closeButton").style("display", "none");
        }
        else {
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

            dijit.byId("demand.key").attr("value", item.key[0]);
            dijit.byId("demand.state").attr("value", _getLabel("master", "cl_state_" + item.state[0]));
            if (dojo.isArray(item.criteria)) {
                dijit.byId("demand.criteria").attr("value", item.criteria.join(" "));
            }
            if (dojo.isArray(item.cc)) {
                dijit.byId("demand.cc").attr("value", item.cc.join("\n"));
            }
            dijit.byId("demand.quantity").attr("value", item.quantity[0]);
            var dueDate = dojo.date.stamp.fromISOString(item.dueDate[0]);
            dijit.byId("demand.date").attr("value", dueDate);
            dijit.byId("demand.date").constraints.min = new Date();
            dijit.byId("demand.time").attr("value", dueDate);
            _common.setLocation(item.locationKey[0], dijit.byId("demand.postalCode"), dijit.byId("demand.countryCode"));
            dijit.byId("demand.range").attr("value", item.range[0]);
            dijit.byId("demand.rangeUnit").attr("value", item.rangeUnit[0]);
            dijit.byId("demand.modificationDate").attr("value", _common.displayDateTime(item.modificationDate));
            dijit.byId("demandFormSubmitButton").attr("label", _getLabel("console", "ga_cmenu_updateDemand", [item.key]));
            dijit.byId("demandFormCancelButton").attr("label", _getLabel("console", "ga_cmenu_cancelDemand", [item.key]));
            dijit.byId("demandFormCloseButton").attr("label", _getLabel("console", "ga_cmenu_closeDemand", [item.key]));

            dojo.query(".existingAttribute").style("display", "");
            var closeableState = item.state == _common.STATES.CONFIRMED;
            if (closeableState) {
                dojo.query(".updateButton").style("display", "none");
                dojo.query(".closeButton").style("display", "");
            }
            else {
                dojo.query(".updateButton").style("display", "");
                dojo.query(".closeButton").style("display", "none");
            }
            dijit.byId("demandFormSubmitButton").attr("disabled", item.state == _common.STATES.DECLINED);
        }

        demandForm.show();
        dijit.byId("demand.postalCode").focus();
    };

    /**
     * Call the back-end to create or update a Demand with the given attribute
     *
     * @param {Object} data Set of attributes built from the <code>form</code> embedded in the dialog box
     */
    module.updateDemand = function(data) {
        if (isNaN(data.key)) {
            delete data.key;
        }
        data.criteria = data.criteria.split(" ");
        data.cc = data.cc.split("\n");
        var month = (data.date.getMonth() + 1);
        var day = data.date.getDate();
        var hours = data.time.getHours();
        var minutes = data.time.getMinutes();
        data.dueDate =
              data.date.getFullYear() +
              (month < 10 ? "-0" : "-") + month +
              (day < 10 ? "-0" : "-") + day +
              (hours < 10 ? "T0" : "T") + hours +
              (minutes < 10 ? ":0" : ":") + minutes +
              ":00";
        data.hashTags = ["golf"]; // TODO: offer a checkbox to allow the #demo mode

        var dfd = _common.updateRemoteDemand(data, data.key);
        dfd.addCallback(function(response) { module.loadNewDemands() });
    };

    /**
     * Call the back-end to cancel the demand displayed in the property pane
     *
     * @param {Object} formId Identifier of the dialog box to close
     * @param {Object} keyFieldId Identifier of the field, in that dialog box, containing the demand key
     */
    module.cancelDemand = function(formId, keyFieldId) {
        dijit.byId(formId).hide();

        var demandKey = dijit.byId(keyFieldId).attr("value");
        var demand = _common.getCachedDemand(demandKey);

        if(demand.state == _common.STATES.CONFIRMED) {
            var messageId = "ga_alert_cancelConfirmedDemand";

            var proposalKey = demand.proposalKeys[0];
            if (!confirm(_getLabel("console", messageId, [demandKey, proposalKey]))) {
                return;
            }
        }

        var data = { state: _common.STATES.CANCELLED };

        var dfd = _common.updateRemoteDemand(data, demandKey);
        dfd.addCallback(function(response) { module.loadNewDemands() });
    }

    /**
     * Call the back-end to close a demand displayed in the property pane
     *
     * @param {Object} formId Identifier of the dialog box to close
     * @param {Object} keyFieldId Identifier of the field, in that dialog box, containing the demand key
     */
    module.closeDemand = function(formId, keyFieldId) {
        dijit.byId(formId).hide();

        var demandKey = dijit.byId(keyFieldId).attr("value");
        var data = { state: _common.STATES.CLOSED };

        var dfd = _common.updateRemoteDemand(data, demandKey);
        dfd.addCallback(function(response) { module.loadNewDemands() });
    }

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

        var proposalForm = dijit.byId("proposalForm");
        proposalForm.reset();

        dijit.byId("associatedDemand.key").attr("value", item.key);
        dijit.byId("associatedDemand.modificationDate").attr("value", _common.displayDateTime(item.modificationDate));

        dijit.byId("proposal.key").attr("value", proposalKey);

        dijit.byId("proposalFormConfirmButton").attr("label", _getLabel("console", "ga_cmenu_confirmProposal", [proposalKey]));
        dijit.byId("proposalFormDeclineButton").attr("label", _getLabel("console", "ga_cmenu_declineProposal", [proposalKey]));
        dijit.byId("proposalFormCancelButton").attr("label", _getLabel("console", "ga_cmenu_cancelDemand", [item.key]));
        dijit.byId("proposalFormCloseButton").attr("label", _getLabel("console", "ga_cmenu_closeDemand", [item.key]));

        var closeableState = item.state == _common.STATES.CONFIRMED;
        if (closeableState) {
            dojo.query(".updateButton").style("display", "none");
            dojo.query(".closeButton").style("display", "");
        }
        else {
            dojo.query(".updateButton").style("display", "");
            dojo.query(".closeButton").style("display", "none");
        }

        _loadProposal(proposalKey);

        proposalForm.show();
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
            var dfd = _common.loadRemoteProposal(proposalKey, _queryPointOfView);
            dfd.addCallback(function(response) { _fetchProposal(_common.getCachedProposal(proposalKey)); });
        }
    };

    /**
     * Use the given Proposal to fetch the corresponding dialog box
     *
     * @param {Proposal} proposal Object to represent
     */
    var _fetchProposal = function(proposal) {
        dijit.byId("proposal.state").attr("value", _getLabel("master", "cl_state_" + proposal.state));
        dijit.byId("proposal.price").attr("value", proposal.price);
        dijit.byId("proposal.total").attr("value", proposal.total);
        var dateObject = dojo.date.stamp.fromISOString(proposal.dueDate);
        dijit.byId("proposal.date").attr("value", dateObject);
        dijit.byId("proposal.time").attr("value", dateObject);
        if (dojo.isArray(proposal.criteria)) {
            dijit.byId("proposal.criteria").attr("value", proposal.criteria.join(" "));
        }
        dijit.byId("proposal.modificationDate").attr("value", _common.displayDateTime(proposal.modificationDate));

        var modifiableState = proposal.state == _common.STATES.PUBLISHED;
        var closeableState = proposal.state == _common.STATES.CONFIRMED;
        dijit.byId("proposalFormConfirmButton").attr("disabled", !modifiableState);
        dijit.byId("proposalFormDeclineButton").attr("disabled", !modifiableState);

        var store = proposal.related.Store;
        dijit.byId("store.key").attr("value", store.key);
        dijit.byId("store.name").attr("value", store.name);
        dijit.byId("store.address").attr("value", store.address);
        dijit.byId("store.phoneNb").attr("value", store.phoneNb);
        dijit.byId("store.email").attr("value", store.email);
        dijit.byId("store.url").attr("value", store.url);
    };

    /**
     * Call the back-end to confirm the proposal displayed in the property pane
     */
    module.confirmProposal = function() {
        dijit.byId("proposalForm").hide();

        var proposalKey = dijit.byId("proposal.key").attr("value");
        var data = {
            state: _common.STATES.CONFIRMED,
            pointOfView: _queryPointOfView
        };

        var dfd = _common.updateRemoteProposal(data, proposalKey);
        dfd.addCallback(function(response) { module.loadNewDemands() });
    }

    /**
     * Call the back-end to decline the proposal displayed in the property pane
     */
    module.declineProposal = function() {
        dijit.byId("proposalForm").hide();

        var proposalKey = dijit.byId("proposal.key").attr("value");
        var data = {
            state: _common.STATES.DECLINED,
            pointOfView: _queryPointOfView
        };

        var dfd = _common.updateRemoteProposal(data, proposalKey);
        dfd.addCallback(function(response) { module.loadNewDemands() });
    }

    /**
     * Call the back-end to get the new Demands
     */
    module.loadNewDemands = function() {
        var lastDemand = _common.getLastDemand();
        var lastModificationDate = lastDemand == null ? null : lastDemand.modificationDate;
        var dfd = _common.loadRemoteDemands(lastModificationDate, _queryPointOfView);
        dfd.addCallback(function(response) { dijit.byId("refreshButton").resetTimeout(); _common.processDemandList(response.resources, _grid); });
    };
})(); // End of the function limiting the scope of the private variables
