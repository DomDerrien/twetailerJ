(function() { // To limit the scope of the private variables

    var module = dojo.provide('twetailer.golf.Consumer');

    dojo.require('twetailer.golf.Common');

    /* Set of local variables */
    var _globalCommon = twetailer.Common,
        _localCommon = twetailer.golf.Common,
        _getLabel,
        _grid,
        _gridCellNode,
        _gridRowIndex;
        _queryPointOfView = _globalCommon.POINT_OF_VIEWS.CONSUMER;

    /**
     * Initializer.
     *
     * @param {String} locale Identifier of the chosen locale.
     */
    module.init = function(locale) {
        _getLabel = _localCommon.init(locale);

        // Attach the contextual menu to the DataGrid instance
        // Note: initialization code grabbed in the dojo test file: test_grid_tooltip_menu.html
        _grid = dijit.byId('demandList');
        dijit.byId('demandListCellMenu').bindDomNode(_grid.domNode);
        _grid.onCellContextMenu = function(e) {
            _gridCellNode = e.cellNode;
            _gridRowIndex = e.rowIndex;
        };

        // Fetch
        var dfd = _globalCommon.loadRemoteDemands(null, 'demandListOverlay', _queryPointOfView, 'golf'); // No modificationDate means "load all active Demands"
        dfd.addCallback(function(response) { _globalCommon.processDemandList(response.resources, _grid); });
    };

    var _demandUpdateDecoration =
           "<span style='white-space:nowrap;'>" +
           "<a href='#' onclick='twetailer.golf.Consumer.displayDemandForm(false,${0});return false;' title='${2}'><span class='dijitReset dijitInline silkIcon silkIconDemandUpdate'></span>${1}</a>" +
           " (<a href='#' onclick='if (confirm(\"${3}\")){twetailer.golf.Consumer.cancelDemand(null,${1});}return false;' title='${3}'><span class='dijitReset dijitInline silkIcon silkIconDemandCancel'></span></a>)" +
           "</span>";

    /**
     * Override of the formatter to be able to place the "Update Demand" link around the demand key.
     *
     * @param {Number[]} demandKey identifier of the demand.
     * @param {Number} rowIndex index of the data in the grid, used by the trigger launching the Proposal properties pane.
     * @return {String} Formatter link with the Demand icon to open the Demand editor.
     */
    module.displayDemandKey = function(demandKey, rowIndex) {
        // TODO: check the demand state in order to use the classname silkIconDemandConfirmed
        var updateLabel = dojo.string.substitute(
            _demandUpdateDecoration,
            [rowIndex, demandKey, _getLabel('console', 'golf_cmenu_updateDemand', [demandKey]), _getLabel('console', 'core_cmenu_cancelDemand', [demandKey])]
        );
        return updateLabel;
    };

    var _proposalViewDecoration = "<a href='#' onclick='twetailer.golf.Consumer.displayProposalForm(${1},${0});return false;' title='${2}'><span class='dijitReset dijitInline silkIcon silkIconProposalView'></span>${0}</a>";

    /**
     * Override of the formatter to be able to place the "Create Proposal" link in front of the proposal key list.
     *
     * @param {Number[]} proposalKeys List of proposal keys.
     * @param {Number} rowIndex index of the data in the grid, used by the trigger launching the Proposal properties pane.
     * @return {String} Formatter list of one link per proposal key, a link opening a dialog with the proposal detail.
     */
    module.displayProposalKeys = function(proposalKeys, rowIndex) {
        // TODO: check the demand state in order to use the classname silkIconProposalConfirmed
        if (!proposalKeys || proposalKeys.length == 0) {
            return '';
        }
        var viewLabel = dojo.string.substitute(
            _proposalViewDecoration,
            ['${0}', '${1}', _getLabel('console', 'golf_cmenu_viewProposal')]
        );
        return _globalCommon.displayProposalKeys(proposalKeys, rowIndex, viewLabel);
    };

    /**
     * Open a dialog box with the attributes of the identified proposal.
     *
     * @param {Boolean} freshForm should be <code>true</code> if the form is for a new Demand.
     * @param {Number} proposedRowIndex (Optional) index given by the contextual icon link.
     */
    module.displayDemandForm = function(freshForm, proposedRowIndex) {
        var demandForm = dijit.byId('demandForm');
        demandForm.reset();

        if (freshForm) {
            var yesterday = new Date();
            var tomorrow = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            tomorrow.setDate(tomorrow.getDate() + 1);
            var dateField = dijit.byId('demand.date');
            dateField.set('value', tomorrow);
            dateField.constraints.min = yesterday;

            var lastDemand = _globalCommon.getLastDemand();
            if (lastDemand) {
                _globalCommon.setLocation(lastDemand.locationKey[0], dijit.byId('demand.postalCode'), dijit.byId('demand.countryCode'));
                dijit.byId('demand.range').set('value', lastDemand.range[0]);
                dijit.byId('demand.rangeUnit').set('value', lastDemand.rangeUnit[0]);
            }
            dijit.byId('demandFormSubmitButton').set('label', _getLabel('console', 'golf_cmenu_createDemand'));

            demandForm.set('title', _getLabel('console', 'golf_demandForm_formTitle_creation'));

            dojo.query('.updateButton').style('display', '');
            dojo.query('.existingAttribute').style('display', 'none');
            dojo.query('.closeButton').style('display', 'none');
        }
        else {
            // rowIndex bind to the handler
            if (proposedRowIndex == null) {
                if (_gridRowIndex == null) {
                    return;
                }
                proposedRowIndex = _gridRowIndex;
            }

            var item = _grid.getItem(proposedRowIndex);
            if (!item) {
                return;
            }

            dijit.byId('demand.key').set('value', item.key[0]);
            dijit.byId('demand.state').set('value', _getLabel('master', 'cl_state_' + item.state[0]));
            if (dojo.isArray(item.criteria)) {
                dijit.byId('demand.criteria').set('value', item.criteria.join(' '));
            }
            if (dojo.isArray(item.cc)) {
                var idx = 0, limit = item.cc.length, coordinate;
                while (idx < limit) {
                    coordinate = item.cc[idx]; // 0-based index
                    idx++;
                    twetailer.Common.getFriendInputField(idx).set('value', coordinate); // 1-based index
                }
            }
            dijit.byId('demand.quantity').set('value', item.quantity[0]);
            if (item.metadata) {
                var metadata = dojo.fromJson(item.metadata[0]);
                if (metadata.pullCart) {
                    dijit.byId('demand.metadata.pullCart').set('value', metadata.pullCart);
                }
                if (metadata.golfCart) {
                    dijit.byId('demand.metadata.golfCart').set('value', metadata.golfCart);
                }
            }
            var dueDate = dojo.date.stamp.fromISOString(item.dueDate[0]);
            dijit.byId('demand.date').set('value', dueDate);
            dijit.byId('demand.date').constraints.min = new Date();
            dijit.byId('demand.time').set('value', dueDate);
            _globalCommon.setLocation(item.locationKey[0], dijit.byId('demand.postalCode'), dijit.byId('demand.countryCode'));
            dijit.byId('demand.range').set('value', item.range[0]);
            dijit.byId('demand.rangeUnit').set('value', item.rangeUnit[0]);
            dijit.byId('demand.modificationDate').set('value', _globalCommon.displayDateTime(item.modificationDate));
            dijit.byId('demandFormSubmitButton').set('label', _getLabel('console', 'golf_cmenu_updateDemand', [item.key]));
            dijit.byId('demandFormCancelButton').set('label', _getLabel('console', 'golf_cmenu_cancelDemand', [item.key]));
            dijit.byId('demandFormCloseButton').set('label', _getLabel('console', 'golf_cmenu_closeDemand', [item.key]));

            demandForm.set('title', _getLabel('console', 'golf_demandForm_formTitle_edition', [item.key[0]]));

            dojo.query('.existingAttribute').style('display', '');
            var closeableState = item.state == _globalCommon.STATES.CONFIRMED;
            if (closeableState) {
                dojo.query('.updateButton').style('display', 'none');
                dojo.query('.closeButton').style('display', '');
            }
            else {
                dojo.query('.updateButton').style('display', '');
                dojo.query('.closeButton').style('display', 'none');
            }
            dijit.byId('demandFormSubmitButton').set('disabled', item.state == _globalCommon.STATES.DECLINED);
        }

        demandForm.show();
    };

    /**
     * Call the back-end to create or update a Demand with the given attribute.
     *
     * @param {Object} data Set of attributes built from the <code>form</code> embedded in the dialog box.
     */
    module.updateDemand = function(data) {
        if (isNaN(data.key)) {
            delete data.key;

            var cachedDemand = _globalCommon.getCachedDemand(data.key);
            if (cachedDemand) {
                var now = new Date();
                var expirationDate = dojo.date.stamp.fromISOString(cachedDemand.expirationDate);
                if (expirationDate.getTime() < now.getTime()) {
                    data.expirationDate = data.dueDate;
                }
            }
        }
        data.criteria = data.criteria.split(/(?:\s|\n|,|;)+/);
        var cc = twetailer.Common.getFriendCoordinates();
        if (0 < cc.length) {
            data.cc = cc;
        }
        data.dueDate = _globalCommon.toISOString(data.date, data.time);
        data.metadata = dojo.toJson({pullCart: parseInt(data.pullCart), golfCart: parseInt(data.golfCart)});
        data.hashTags = ['golf']; // TODO: offer a checkbox to allow the #demo mode

        var dfd = _globalCommon.updateRemoteDemand(data, data.key, 'demandListOverlay');
        dfd.addCallback(function(response) { module.loadNewDemands() });
    };

    /**
     * Call the back-end to cancel the demand displayed in the property pane.
     *
     * @param {Object} formId Identifier of the dialog box to close.
     * @param {Object} keyFieldId Identifier of the field, in that dialog box, containing the demand key.
     */
    module.cancelDemand = function(formId, keyFieldId) {
        if (formId) {
            dijit.byId(formId).hide();
        }

        var demandKey = isNaN(keyFieldId) ? dijit.byId(keyFieldId).get('value') : keyFieldId;
        var demand = _globalCommon.getCachedDemand(demandKey);

        if (demand.state == _globalCommon.STATES.CONFIRMED) {
            var messageId = 'golf_alert_cancelConfirmedDemand';

            var proposalKey = demand.proposalKeys[0];
            if (!confirm(_getLabel('console', messageId, [demandKey, proposalKey]))) {
                return;
            }
        }

        var data = { state: _globalCommon.STATES.CANCELLED };

        var dfd = _globalCommon.updateRemoteDemand(data, demandKey, 'demandListOverlay');
        dfd.addCallback(function(response) { module.loadNewDemands() });
    }

    /**
     * Call the back-end to close a demand displayed in the property pane.
     *
     * @param {Object} formId Identifier of the dialog box to close.
     * @param {Object} keyFieldId Identifier of the field, in that dialog box, containing the demand key.
     */
    module.closeDemand = function(formId, keyFieldId) {
        if (formId) {
            dijit.byId(formId).hide();
        }

        var demandKey = isNaN(keyFieldId) ? dijit.byId(keyFieldId).get('value') : keyFieldId;
        var data = { state: _globalCommon.STATES.CLOSED };

        var dfd = _globalCommon.updateRemoteDemand(data, demandKey, 'demandListOverlay');
        dfd.addCallback(function(response) { module.loadNewDemands() });
    }

    /**
     * Open a dialog box with the attributes of the identified proposal.
     *
     * @param {Number} proposedRowIndex (Optional) index given when a link on the proposal key is activated.
     * @param {Number} proposalKey (Optional) index given when a link on the proposal key is activated.
     */
    module.displayProposalForm = function(proposedRowIndex, proposalKey) {
        // rowIndex bind to the handler
        if (proposedRowIndex == null) {
            if (_gridRowIndex == null) {
                return;
            }
            proposedRowIndex = _gridRowIndex;
        }

        var item = _grid.getItem(proposedRowIndex);
        if (!item) {
            return;
        }

        var proposalForm = dijit.byId('proposalForm');
        proposalForm.reset();

        proposalForm.set('title', _getLabel('console', 'golf_proposalForm_formTitle_view', [proposalKey]));

        dijit.byId('proposal.key').set('value', proposalKey);

        dijit.byId('proposalFormConfirmButton').set('label', _getLabel('console', 'golf_cmenu_confirmProposal', [proposalKey]));
        dijit.byId('proposalFormDeclineButton').set('label', _getLabel('console', 'golf_cmenu_declineProposal', [proposalKey]));
        dijit.byId('proposalFormCloseButton').set('label', _getLabel('console', 'golf_cmenu_closeDemand', [item.key]));

        var closeableState = item.state == _globalCommon.STATES.CONFIRMED;
        if (closeableState) {
            dojo.query('.updateButton').style('display', 'none');
            dojo.query('.closeButton').style('display', '');
        }
        else {
            dojo.query('.updateButton').style('display', '');
            dojo.query('.closeButton').style('display', 'none');
        }

        if (_globalCommon.getCachedProposal(proposalKey)) {
            _fetchProposal(_globalCommon.getCachedProposal(proposalKey));
        }
        else {
            var dfd = _globalCommon.loadRemoteProposal(proposalKey, 'proposalFormOverlay', _queryPointOfView);
            dfd.addCallback(function(response) { _fetchProposal(_globalCommon.getCachedProposal(proposalKey)); });
        }

        proposalForm.show();
    };

    /**
     * Use the given Proposal to fetch the corresponding dialog box.
     *
     * @param {Proposal} proposal Object to represent.
     */
    var _fetchProposal = function(proposal) {
        dijit.byId('proposal.demandKey').set('value', proposal.demandKey);
        dijit.byId('proposal.state').set('value', _getLabel('master', 'cl_state_' + proposal.state));
        dijit.byId('proposal.quantity').set('value', proposal.quantity);
        if (proposal.metadata) {
            var metadata = dojo.fromJson(proposal.metadata);
            if (metadata.pullCart) {
                dijit.byId('proposal.metadata.pullCart').set('value', metadata.pullCart);
            }
            if (metadata.golfCart) {
                dijit.byId('proposal.metadata.golfCart').set('value', metadata.golfCart);
            }
        }
        dijit.byId('proposal.price').set('value', proposal.price);
        dijit.byId('proposal.total').set('value', proposal.total);
        var dateObject = dojo.date.stamp.fromISOString(proposal.dueDate);
        dijit.byId('proposal.date').set('value', dateObject);
        dijit.byId('proposal.time').set('value', dateObject);
        if (dojo.isArray(proposal.criteria)) {
            dijit.byId('proposal.criteria').set('value', proposal.criteria.join(' '));
        }
        dijit.byId('proposal.modificationDate').set('value', _globalCommon.displayDateTime(proposal.modificationDate));

        var modifiableState = proposal.state == _globalCommon.STATES.PUBLISHED;
        var closeableState = proposal.state == _globalCommon.STATES.CONFIRMED;
        dijit.byId('proposalFormConfirmButton').set('disabled', !modifiableState);
        dijit.byId('proposalFormDeclineButton').set('disabled', !modifiableState);

        var store = proposal.related.Store;
        dijit.byId('store.name').set('value', store.name);
        dijit.byId('store.address').set('value', store.address);
        dijit.byId('store.phoneNb').set('value', store.phoneNb);
        dijit.byId('store.email').set('value', store.email);
        dijit.byId('store.url').set('value', store.url);

        var storeFormTitle = _getLabel('console', 'golf_storeForm_formTitle_view', [store.key]);
        dijit.byId('storeForm').set('title', storeFormTitle);
        dojo.byId('proposal.viewStoreInfo').innerHTML = storeFormTitle;
    };

    /**
     * Call the back-end to confirm the proposal displayed in the property pane.
     */
    module.confirmProposal = function() {
        dijit.byId('proposalForm').hide();

        var proposalKey = dijit.byId('proposal.key').get('value');
        var data = {
            state: _globalCommon.STATES.CONFIRMED,
            pointOfView: _queryPointOfView
        };

        var dfd = _globalCommon.updateRemoteProposal(data, proposalKey, 'demandListOverlay');
        dfd.addCallback(function(response) { module.loadNewDemands() });
    }

    /**
     * Call the back-end to decline the proposal displayed in the property pane.
     */
    module.declineProposal = function() {
        dijit.byId('proposalForm').hide();

        var proposalKey = dijit.byId('proposal.key').get('value');
        var data = {
            state: _globalCommon.STATES.DECLINED,
            pointOfView: _queryPointOfView
        };

        var dfd = _globalCommon.updateRemoteProposal(data, proposalKey, 'demandListOverlay');
        dfd.addCallback(function(response) { module.loadNewDemands() });
    }

    /**
     * Call the back-end to get the new Demands.
     */
    module.loadNewDemands = function() {
        var lastDemand = _globalCommon.getLastDemand();
        var lastModificationDate = lastDemand ? lastDemand.modificationDate : null;
        var dfd = _globalCommon.loadRemoteDemands(lastModificationDate, 'demandListOverlay', _queryPointOfView, 'golf');
        dfd.addCallback(function(response) { dijit.byId('refreshButton').resetTimeout(); _globalCommon.processDemandList(response.resources, _grid); });
    };
})(); // End of the function limiting the scope of the private variables
