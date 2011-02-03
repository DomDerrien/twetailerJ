(function() { // To limit the scope of the private variables

    var module = dojo.provide('twetailer.Associate');

    dojo.require('twetailer.Common');
    dojo.require('dijit.Tooltip');
    dojo.require('dijit.form.MultiSelect');
    dojo.require('dojox.widget.SortList');

    /* Set of local variables */
    var _globalCommon = twetailer.Common,
        _getLabel,
        _grid,
        _gridCellNode,
        _gridRowIndex,
        _queryPointOfView = _globalCommon.POINT_OF_VIEWS.SALE_ASSOCIATE;

    /**
     * Initializer.
     *
     * @param {String} locale Identifier of the chosen locale.
     * @param {Array} supportedLanguages List of supported languages, in a format to be used for a dijit.form.Select widget.
     * @param {String} statusPlaceHolderId identifier of the DOM node to display the notification related to the GAE Channel
     */
    module.init = function(locale, supportedLanguages, statusPlaceHolderId) {
        _getLabel = _globalCommon.init(locale, supportedLanguages);

        // Attach the contextual menu to the DataGrid instance
        // Note: initialization code grabbed in the dojo test file: test_grid_tooltip_menu.html
        _grid = dijit.byId('demandList');
        dijit.byId('demandListCellMenu').bindDomNode(_grid.domNode);
        _grid.onCellContextMenu = function(e) {
            _gridCellNode = e.cellNode;
            _gridRowIndex = e.rowIndex;
        };

        // Fetch
        var dfd = _globalCommon.loadRemoteDemands(null, 'demandListOverlay', _queryPointOfView, null); // No modificationDate means "load all active Demands" & no filter on hash tag
        dfd.addCallback(function(response) { _globalCommon.processDemandList(response.resources, _grid); });
        dfd.addCallback(function(response) { setTimeout(_processConsoleParameters, 1000); });

        // Register callbacks for the GAE Channel and initialize it
        var placeHolder = dojo.byId(statusPlaceHolderId);
        var genericMessage = 'Connected for automatic updates...';
        dojo.subscribe(_globalCommon.getOnOpenNotificationId(), function() { placeHolder.innerHTML = genericMessage; });
        dojo.subscribe(_globalCommon.getOnMessageNotificationId(), function(message) {
            dojo.animateProperty({
                node: placeHolder,
                properties: { backgroundColor: { end: 'yellow' } }
            }).play();
            placeHolder.innerHTML = '1 message received and being processed ;)';
            module.loadNewDemands(); // TODO: use the data in the received message to update the grid without another roundtrip
            setTimeout(function() {
                dojo.animateProperty({
                    node: placeHolder,
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
                placeHolder.innerHTML = genericMessage;
            }, 2500); // Restore the default message in 2.4 seconds
        });
        _globalCommon.openGAEChannel({
            statusPlaceHolder: placeHolder
        });
    };

    module.readyToProcessParameters = false;

    /**
     * Helper trying to display the Proposal create pane for the specified demand.
     * Can reschedule the pane openning if the console state is not yet stable.
     *
     * TODO: fix a limit in the number of retries
     */
    var _processConsoleParameters = function() {
        var parameters = location.search;
        if (parameters == null) { return; }
        if (parameters.length == 0) { return; }
        if (!module.readyToProcessParameters) { setTimeout(_processConsoleParameters, 1000); }
        if (parameters.indexOf('?') == 0) { parameters = parameters.substr(1); }
        parameters = dojo.queryToObject(parameters);
        if (parameters.demand == null) { return; }
        if (_grid.store == null) { setTimeout(_processConsoleParameters, 1000); }
        module.displayProposalForm(_grid.store._getItemByIdentity(parameters.demand)._0, null);
    };

    var _demandViewDecoration = "<span class='dijitReset dijitInline silkIcon silkIconDemandView'></span>${0}";

    /**
     * Override of the formatter to be able to place the Demand icon before demand key.
     *
     * @param {Number[]} demandKey identifier of the demand.
     * @param {Number} rowIndex index of the data in the grid, used by the trigger launching the Proposal properties pane.
     * @return {String} Formatter with the Demand icon before the demand key.
     */
    module.displayDemandKey = function(demandKey, rowIndex) {
        // TODO: check the demand state in order to use the classname silkIconDemandConfirmed
        try {
            return dojo.string.substitute(_demandViewDecoration, [demandKey]);
        }
        catch (ex) { alert(ex); }
        return demandKey;
    };

    var _proposalCreateDecoration = "<a href='#' onclick='twetailer.Associate.displayProposalForm(${0},null);return false;' title='${1}'><span class='dijitReset dijitInline silkIcon silkIconProposalAdd'></span>${1}</a>";
    var _proposalUpdateDecoration = "<a href='#' onclick='twetailer.Associate.displayProposalForm(${1},${0});return false;' title='${2}'><span class='dijitReset dijitInline silkIcon silkIconProposalUpdate'></span>${0}</a>";
    var _proposalViewDecoration = "<a href='#' onclick='twetailer.Associate.displayProposalForm(${1},${0});return false;' title='${2}'><span class='dijitReset dijitInline silkIcon silkIconProposalView'></span>${0}</a>";

    /**
     * Formatter to be able to place the "Create Proposal" link in front of the proposal key list.
     *
     * @param {Number[]} proposalKeys List of proposal keys.
     * @param {Number} rowIndex index of the data in the grid, used by the trigger launching the Proposal properties pane.
     * @return {String} Formatter list of one link per proposal key, a link opening a dialog with the proposal detail.
     */
    module.displayProposalKeys = function(proposalKeys, rowIndex) {
        // TODO: check the demand state in order to use the class name silkIconProposalConfirmed
        var item = _grid.getItem(rowIndex);
        if (!item) {
            return;
        }
        var cellContent = '';
        var modifiableDemand = item.state == _globalCommon.STATES.PUBLISHED;
        if (modifiableDemand) {
            var createLabel = dojo.string.substitute(
                _proposalCreateDecoration,
                [rowIndex, _getLabel('console', 'core_cmenu_createProposal')]
            );
            cellContent = createLabel;
        }
        if (!proposalKeys || proposalKeys.length == 0) {
            return cellContent;
        }
        var updateLabel = dojo.string.substitute(
            modifiableDemand ? _proposalUpdateDecoration : _proposalViewDecoration,
            ['${0}', '${1}', _getLabel('console', modifiableDemand ? 'core_cmenu_updateProposal' : 'core_cmenu_viewProposal')]
        );
        if (modifiableDemand) {
            cellContent += '<br/>';
        }
        return cellContent + _globalCommon.displayProposalKeys(proposalKeys, rowIndex, updateLabel);
    };

    /**
     * Open a dialog box with the attributes of the identified proposal. If there's no
     * proposalKey, the variable set by the contextual menu handler for the Demand grid
     * is used to identified a selected grid row and to propose a dialog for a new
     * proposal creation.
     *
     * @param {Number} proposedRowIndex (Optional) index given when a link on the proposal key is activated.
     * @param {Number} proposalKey (Optional) index given when a link on the proposal key is activated.
     */
    module.displayProposalForm = function(proposedRowIndex, proposalKey){
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

        dijit.byId('demand.key').set('value', item.key[0]); // hidden field generating "proposal.demandKey"
        dijit.byId('demand.hashTags').set('value', item.hashTags); // hidden field generating "proposal.hashTags"

        var dueDate = dojo.date.stamp.fromISOString(item.dueDate[0]);
        dijit.byId('proposal.date').set('value', dueDate);
        dijit.byId('proposal.date').constraints.min = new Date();
        dijit.byId('proposal.time').set('value', dueDate);
        var showDemandField = false;
        if (item.hashTags) {
            // Regular hash tags placed in a hidden field
            dijit.byId('demand.visibleHashTags').set('value', _globalCommon.displayHashTags(item.hashTags));
            showDemandField = 0 < item.hashTags.length;
        }
        dojo.byId('proposalForm.demand.hashTags').style.display = showDemandField ? '' : 'none';
        dojo.byId('proposalForm.proposal.metadata').style.display = showDemandField ? '' : 'none'; // No hash tag means no metadata support out of the box
        showDemandField = false;
        if (dojo.isArray(item.criteria)) {
            dijit.byId('demand.criteria').set('value', item.criteria.join(' '));
            showDemandField = 0 < item.criteria.length;
        }
        dojo.byId('proposalForm.demand.criteria').style.display = showDemandField ? '' : 'none';
        showDemandField = false;
        if (item.metadata && item.metadata[0]) {
            dijit.byId('demand.metadata').set('value', _globalCommon.displayMetadata(item.metadata[0]));
            showDemandField = 0 < item.metadata.length;
        }
        dojo.byId('proposalForm.demand.metadata').style.display = showDemandField ? '' : 'none';
        dijit.byId('proposal.quantity').set('value', item.quantity[0]);

        if (!proposalKey) {
            proposalForm.set('title', _getLabel('console', 'core_proposalForm_formTitle_creation', [item.key[0]]));
            dijit.byId('proposalFormSubmitButton').set('label', _getLabel('console', 'core_cmenu_createProposal'));

            dojo.query('.updateButton').style('display', '');
            dojo.query('.existingAttribute').style('display', 'none');
            dojo.query('.closeButton').style('display', 'none');
        }
        else {
            proposalForm.set('title', _getLabel('console', 'core_proposalForm_formTitle_edition', [proposalKey, item.key[0]]));
            dijit.byId('proposalFormSubmitButton').set('label', _getLabel('console', 'core_cmenu_updateProposal', [proposalKey]));
            dijit.byId('proposalFormCancelButton').set('label', _getLabel('console', 'core_cmenu_cancelProposal', [proposalKey]));
            dijit.byId('proposalFormCloseButton').set('label', _getLabel('console', 'core_cmenu_closeProposal', [proposalKey]));
            dojo.query('.existingAttribute').style('display', '');

            if (_globalCommon.getCachedProposal(proposalKey)) {
                _fetchProposal(_globalCommon.getCachedProposal(proposalKey));
            }
            else {
                var dfd = _globalCommon.loadRemoteProposal(proposalKey, 'proposalFormOverlay', _queryPointOfView);
                dfd.addCallback(function(response) { _fetchProposal(_globalCommon.getCachedProposal(proposalKey)); });
            }
        }

        proposalForm.show();
    };

    /**
     * Use the given Proposal to fetch the corresponding dialog box.
     *
     * @param {Proposal} proposal Object to represent.
     */
    var _fetchProposal = function(proposal) {
        dijit.byId('proposal.key').set('value', proposal.key);
        dijit.byId('proposal.price').set('value', proposal.price);
        dijit.byId('proposal.total').set('value', proposal.total);
        dijit.byId('proposal.quantity').set('value', proposal.quantity);
        if (proposal.metadata) {
            // dijit.byId('proposal.metadata').set('value', proposal.metadata);
            dijit.byId('proposal.metadata').set('value', dojo.toJson(dojo.fromJson(proposal.metadata), true));
        }
        var dateObject = dojo.date.stamp.fromISOString(proposal.dueDate);
        dijit.byId('proposal.date').set('value', dateObject);
        dijit.byId('proposal.time').set('value', dateObject);
        if (dojo.isArray(proposal.criteria)) {
            dijit.byId('proposal.criteria').set('value', proposal.criteria.join(' '));
        }
        dijit.byId('proposal.modificationDate').set('value', _globalCommon.displayDateTime(proposal.modificationDate));
        var scoreValue = _getLabel('console', 'core_proposalForm_proposalNoScoreYet');
        switch(proposal.score) {
            case 1: scoreValue = ':-('; break;
            case 2: scoreValue = ':-|'; break;
            case 3: scoreValue = ':-|'; break;
            case 4: scoreValue = ':-)'; break;
            case 5: scoreValue = ':-)'; break;
        }
        dijit.byId('proposal.score').set('value', scoreValue);
        var commentWidget = dijit.byId('proposal.comment');
        if (proposal.comment) {
            commentWidget.set('value', proposal.comment);
        }

        var closeableState = proposal.state == _globalCommon.STATES.CONFIRMED;
        if (closeableState) {
            dojo.query('.updateButton').style('display', 'none');
            dojo.query('.closeButton').style('display', '');
        }
        else {
            dojo.query('.updateButton').style('display', '');
            dojo.query('.closeButton').style('display', 'none');
        }
        dijit.byId('proposalFormSubmitButton').set('disabled', proposal.state == _globalCommon.STATES.DECLINED);
    };

    /**
     * Call the back-end to create or update a Proposal with the given attribute.
     *
     * @param {Object} data Set of attributes built from the <code>form</code> embedded in the dialog box.
     */
    module.updateProposal = function(data) {
        if (isNaN(data.key)) {
            delete data.key;

            var cachedProposal = _globalCommon.getCachedProposal(data.key);
            if (cachedProposal) {
                var now = new Date();
                var expirationDate = dojo.date.stamp.fromISOString(cachedProposal.expirationDate);
                if (expirationDate.getTime() < now.getTime()) {
                    data.expirationDate = data.dueDate;
                }
            }
        }
        if (isNaN(data.price)) {
            delete data.price;
        }
        if (isNaN(data.total)) {
            delete data.total;
        }
        data.demandKey = parseInt(data.demandKey);
        data.criteria = data.criteria.split(/(?:\s|\n|,|;)+/);
        data.quantity = parseInt(data.quantity);
        data.dueDate = _globalCommon.toISOString(data.date, data.time);
        data.metadata = dojo.trim(data.metadata);

        data.hashTags = data.hashTags.split(','); // Standard array delimiter
        if (data.demoMode) {
            data.hashTags.push('demo');
        }

        var dfd = _globalCommon.updateRemoteProposal(data, data.key, 'demandListOverlay');
        // dfd.addCallback(function(response) { setTimeout(function() { module.loadNewDemands(); }, 7000); }); // Now, updates come automatically via the Channel')
    };

    /**
     * Verify if the data hosted by the identified field is correctly JSON formatted.
     *
     * @param {String} metadataId Identifier of the field to check.
     * @param {Boolean} autoFormat (Optional) Indicates if the field content should be updated with clean JSON information.
     * @return {Boolean} <code>true</code> if the data represent a valid JSON bag (can be empty), <code>false</code> otherwise.
     */
    module.validateMetadata = function(metadataId, autoFormat) {
        if (!metadataId) {
            return true;
        }
        var metadataField = dijit.byId(metadataId);
        if (!metadataField) {
            return true;
        }
        var metadata = metadataField.get("value");
        if (!metadata) {
            return true;
        }
        try {
            metadata = dojo.toJson(dojo.fromJson(metadata), false);
            if (autoFormat === null || autoFormat === true) {
                metadataField.set("value", metadata);
            }
            return true;
        }
        catch(ex) {
            var ttId = metadataId + 'tootipId';
            var tooltip = dijit.byId(ttId);
            if (!tooltip) {
                tooltip = new dijit.Tooltip({
                    id: ttId,
                    connectId: [metadataId],
                    label: _getLabel('console', 'core_alert_invalidMetadata'),
                    position: ["above", "below"],
                    showDelay: 0
                });
            }
            dojo.style(tooltip.domNode, "visibility", "visible");
            dojo.addClass(metadataField.domNode, 'dijitError');
            var handle = dojo.connect(
                metadataField,
                "onKeyPress",
                null, // no context required
                function() {
                    dijit.byId(ttId).destroy();
                    dojo.removeClass(metadataField.domNode, 'dijitError');
                    dojo.disconnect(handle);
                }
            );
            metadataField.focus();
            return false;
        }
    };

    /**
     * Call the back-end to create or update a Proposal with the given attribute.
     *
     * @param {Object} data Set of attributes built from the <code>form</code> embedded in the dialog box.
     */
    module.cancelProposal = function() {
        dijit.byId('proposalForm').hide();

        var proposalKey = dijit.byId('proposal.key').get('value');
        var proposal = _globalCommon.getCachedProposal(proposalKey);

        var messageId = proposal.state == _globalCommon.STATES.CONFIRMED ?
              'core_alert_cancelConfirmedProposal' :
              'core_alert_cancelPublishedProposal';

        var demandKey = proposal.demandKey;
        if (!confirm(_getLabel('console', messageId, [proposalKey, demandKey]))) {
            return;
        }

        var data = { state: _globalCommon.STATES.CANCELLED };

        var dfd = _globalCommon.updateRemoteProposal(data, proposalKey, 'demandListOverlay');
        dfd.addCallback(function(response) { module.loadNewDemands(); });
    };

    /**
     * Call the back-end to close the proposal displayed in the property pane.
     */
    module.closeProposal = function() {
        dijit.byId('proposalForm').hide();

        var proposalKey = dijit.byId('proposal.key').get('value');
        var data = { state: _globalCommon.STATES.CLOSED };

        var dfd = _globalCommon.updateRemoteProposal(data, proposalKey, 'demandListOverlay');
        dfd.addCallback(function(response) { module.loadNewDemands(); });
    }

    /**
     * Call the back-end to get the new Demands.
     */
    module.loadNewDemands = function() {
        var lastDemand = _globalCommon.getLastDemand();
        var lastModificationDate = lastDemand ? lastDemand.modificationDate : null;
        var dfd = _globalCommon.loadRemoteDemands(lastModificationDate, 'demandListOverlay', _queryPointOfView, null);
        dfd.addCallback(function(response) { dijit.byId('refreshButton').resetTimeout(); _globalCommon.processDemandList(response.resources, _grid); });
    };

    /**
     * Helper building dynamically the pane with the user profile attributes
     */
    module.showProfile = function() {
        var createContent = dijit.byId('profileForms') == null
        if (createContent) {
            // Create the tab container for both the consumer and the associate profiles
            var tC = new dijit.layout.TabContainer({
                style: 'height: 380px; width: 600px;',
                tabStrip: true
            }, 'profileForms');

            // Create the consumer profile tab
            var consumerTab = new dijit.layout.ContentPane({
                content: _globalCommon.createConsumerProfilePane(_globalCommon.getLoggedConsumer()),
                title: _getLabel('console', 'profile_consumer_tabTitle')
            });
            tC.addChild(consumerTab);

            // Create the associate profile tab and select it
            var associateTab = new dijit.layout.ContentPane({
                content: _createSaleAssociateProfilePane(_globalCommon.getLoggedSaleAssociate()),
                title: _getLabel('console', 'profile_associate_tabTitle')
            });
            tC.addChild(associateTab);
            tC.selectChild(associateTab);

            var form = dijit.byId('userProfileForm');
            dojo.connect(form, 'onSubmit', module, 'updateProfile');
        }

        // Show the dialog
        var dialog = dijit.byId('userProfile');
        dialog.show();

        if (createContent) {
            // Initialize the tab container
            tC.startup();
        }
    };

    /**
     * Helper creating a HTML <code>table</code> with the <code>input</code> fields
     * displaying the associate profile attributes
     *
     * @param {Object} associate Copy of the logged Sale Associate record
     */
    var _createSaleAssociateProfilePane = function(associate) {
        var div = dojo.create('div', null),
            fieldSet = dojo.create('fieldset', { 'class': 'entityInformation' }, div),
            legend = dojo.create('legend', { innerHTML: _getLabel('console', 'profile_associate_hashTagGroup') }, fieldSet),
            table = dojo.create('table', { style: 'width: 100%'}, fieldSet),
            tbody = dojo.create('tbody', null, table),
            row,
            thresholds = associate['score'].substr(2).split('\.');

        // Row with a list of available hash tags and the associate's selected hash tags
        row = dojo.create('tr', null, tbody);
        dojo.create('label', { 'for': 'hashTagSource', innerHTML: _getLabel('console', 'profile_associate_hashTagLabel') }, dojo.create('td', { colspan: 4 }, row));

        var nativeListSource = dojo.create('ul'), nativeListTarget = dojo.create('ul');
        dojo.forEach(['cardealer', 'carparts', 'golf', 'taxi'], function(hashTag) {
            dojo.create('li', { key: hashTag, innerHTML: '#' + hashTag }, (dojo.indexOf(associate.hashTags || [], hashTag) == -1 ? nativeListSource : nativeListTarget));
        });

        row = dojo.create('tr', null, tbody);
        dojo.create('td', { innerHTML: '&nbsp;', style: 'width: 25%;' }, row)
        dojo.create('td', { style: 'width: 35%;' }, row).appendChild(nativeListSource);
        cell = dojo.create('td', { style: 'text-align: center;', style: 'width: 5%;' }, row);
        cell.appendChild(
            new dijit.form.Button({
                label: '&gt;',
                onClick: function() {
                    _switchSortList(dijit.byId("hashTagSource"), dijit.byId("hashTagTarget"));
                }
            }).domNode
        );
        cell.appendChild(
            dojo.create('br')
        );
        cell.appendChild(
            new dijit.form.Button({
                label: '&lt;',
                onClick: function() {
                    _switchSortList(dijit.byId("hashTagTarget"), dijit.byId("hashTagSource"));
                }
            }).domNode
        );
        dojo.create('td', { style: 'width: 35%;' }, row).appendChild(nativeListTarget);
        new dojox.widget.SortList({ id: 'hashTagSource', style: 'width: 100%; height: 100px; overflow: auto;', sortable: true, title: _getLabel('console', 'profile_associate_hashTagListSourceLabel') }, nativeListSource);
        new dojox.widget.SortList({ id: 'hashTagTarget', style: 'width: 100%; height: 100px; overflow: auto;', sortable: true, title: _getLabel('console', 'profile_associate_hashTagListTargetLabel') }, nativeListTarget);

        // Row with the associate's number of required hash tags
        row = dojo.create('tr', null, tbody);
        cell = dojo.create('td', { colspan: 4 }, row);
        dojo.create('label', { 'for': 'requiredHashTagNb', innerHTML: _getLabel('console', 'profile_associate_requiredHashTagNbLabel'), style: 'margin-right: 10px;'}, cell);
        cell.appendChild(
            new dijit.form.NumberSpinner({
                constraints: {min: 0, places: 0 },
                id: 'requiredHashTagNb',
                name: 'requiredHashTagNb',
                style: 'width: 3em;',
                value: thresholds[0]
            }).domNode
        );

        fieldSet = dojo.create('fieldset', { 'class': 'entityInformation' }, div);
        legend = dojo.create('legend', { innerHTML: _getLabel('console', 'profile_associate_criteriaGroup') }, fieldSet);
        table = dojo.create('table', { style: 'width: 100%'}, fieldSet);
        tbody = dojo.create('tbody', null, table);

        // Rows with the associates's criteria
        var row = dojo.create('tr', null, tbody);
        dojo.create('label', { 'for': 'criteria', innerHTML: _getLabel('console', 'profile_associate_criteriaLabel') }, dojo.create('td', { colspan: 4 }, row));

        row = dojo.create('tr', null, tbody);
        dojo.create('td', { innerHTML: '&nbsp;', style: 'width: 25%;' }, row);
        dojo.create('td', { colspan: 3 }, row).appendChild(
            new dijit.form.Textarea({
                id: 'criteria',
                name: 'criteria',
                placeHolder: _getLabel('console', 'profile_associate_criteriaPlaceHolder'),
                rows: 3,
                style: 'width: 100%; font-family: "Droid Sans", arial, serif; font-size: 12px;',
                trim: true,
                value: (associate.criteria || []).join('\n')
            }).domNode
        );

        // Row with the associate's number of required criteria
        row = dojo.create('tr', null, tbody);
        cell = dojo.create('td', { colspan: 4 }, row);
        dojo.create('label', { 'for': 'requiredCriteriaNb', innerHTML: _getLabel('console', 'profile_associate_requiredCriteriaNbLabel'), style: 'padding-right: 10px;' }, cell);
        cell.appendChild(
            new dijit.form.NumberSpinner({
                constraints: {min: 0, places: 0 },
                id: 'requiredCriteriaNb',
                name: 'requiredCriteriaNb',
                style: 'width: 3em;',
                value: thresholds[1]
            }).domNode
        );

        // Return DOM node with the form fields
        return div;
    };

    /**
     * Helper moving the select items from the SortList 'fromList' to the SortList 'toList'
     *
     * @param {Object} fromList Instance of the SortList to get the selected items from
     * @param {Object} toList Instance of the SortList to put the select items
     */
    var _switchSortList = function(fromList, toList){
        var selectedTags = fromList.get('selected');
        if (selectedTags == null || selectedTags.length == 0) {
            return;
        }
        var selectedTagIdx = 0, fromContainerNode = fromList.containerNode, child = fromContainerNode.firstChild, toContainerNode = toList.containerNode, nextSibling;
        while (child != null) {
            nextSibling = child.nextSibling;
            if (child.innerHTML == selectedTags[selectedTagIdx]) {
                fromContainerNode.removeChild(child);
                toContainerNode.appendChild(child);
                dojo.removeClass(child, 'sortListItemSelected');
                selectedTagIdx++;
            }
            child = nextSibling;
        }
        fromList.onSort();
        fromList.onSort();
        toList.onSort();
        toList.onSort();
    }

    /**
     * Event handler checking the content of the User Profile dialog and submitting updates
     *
     * @param {Object} event Event which triggered the form submission
     */
    module.updateProfile = function(event) {
        // Block the vent propagation to prevent the page change
        dojo.stopEvent(event);
        // Verify the required parameters are OK
        var form = dijit.byId('userProfileForm');
        if (!form.validate()) {
            return;
        }
        dijit.byId('userProfileFormOverlay').show();
        var filledData = form.get('value');
        _globalCommon.updateConsumerProfile(filledData, function() { _updateSaleAssociateProfile(filledData) });
    };


    /**
     * Helper collecting updated SaleAssociate attributes and submitting them to the server
     *
     * @param {Object} filledData Collection of attributes made from the SaleAssociate form
     * @param {Function} nextStep Function to invoke when the process if finished
     *
     * @see twetailer.Associate#createSaleAssociateProfilePane(Object)
     */
    var _updateSaleAssociateProfile = function(filledData) {
        var associate = _globalCommon.getLoggedSaleAssociate(), filteredData = {}, objectHasNewValue = false, fieldHasNewValue = false;
        // Check hash tags
        var source = associate['hashTags'] || [], target = [];
        for(var child = dijit.byId('hashTagTarget').containerNode.firstChild; child != null; child = child.nextSibling) {
            target.push(dojo.attr(child, 'key'));
        }
        if (source.length == 0 && target.length == 0) {
            // No update, both lists are empty
        }
        else if (source.length != target.length){
            // Different lists
            fieldHasNewValue = true;
        }
        else {
            // Look for the differences
            for(var idx = 0, iLimit = target.length; idx < iLimit; idx++) {
                if (dojo.indexOf(source, target[idx]) == -1) {
                    fieldHasNewValue = true;
                    break;
                }
            }
        }
        // Report the differences
        if (fieldHasNewValue == true) {
            objectHasNewValue = true;
            filteredData['hashTags'] = target;
        }
        // Check criteria (normal tags)
        source = associate['criteria'] || [];
        target = filledData['criteria'].split('\n');
        fieldHasNewValue = false;
        // Normalize the proposed entries
        for (var idx = 0, iLimit = target.length; idx < iLimit; idx++) {
            var newValue = dojo.trim(target[idx]);
            if (newValue.length == 0) {
                target.splice(idx, 1);
                iLimit--;
            }
            target[idx] = newValue.toLowerCase();
        }
        // Look for the differences
        for(var idx = 0, iLimit = target.length; idx < iLimit; idx++) {
            var newValue = dojo.trim(target[idx]).toLowerCase();
            if (dojo.indexOf(target, newValue, idx + 1) != -1) {
                target.splice(idx, 1);
                iLimit--;
                idx --;
            }
            else if (dojo.indexOf(source, newValue) == -1) {
                fieldHasNewValue = true;
                break;
            }
        }
        // Report the differences
        if (fieldHasNewValue == true || source.length != iLimit) {
            objectHasNewValue = true;
            filteredData['criteria'] = target;
        }
        // Check thresholds
        source = associate['score'];
        target = '1:' + dijit.byId('requiredHashTagNb').get('value') + '.' + dijit.byId('requiredCriteriaNb').get('value');
        if (source != target) {
            objectHasNewValue = true;
            filteredData['score'] = target;
        }
        // Propagate the updates server-side
        if (objectHasNewValue) {
            // alert('need Sale Associate field update\n'+dojo.toJson(filteredData));
            dojo.xhrPut({
                headers: { 'content-type': 'application/json; charset=UTF-8' },
                putData: dojo.toJson(filteredData),
                handleAs: 'json',
                load: function(response, ioArgs) {
                    if (response && response.success) {
                        _globalCommon.registerSaleAssociate(dojo.mixin(associate, response.resource));
                    }
                    else {
                        alert(response.message + '\nurl: '+ ioArgs.url);
                    }
                    dijit.byId('userProfileFormOverlay').hide();
                    dijit.byId('userProfile').hide();
                },
                error: function(message, ioArgs) {
                    dijit.byId('userProfileFormOverlay').hide();
                    module.handleError(message, ioArgs);
                },
                url: '/API/SaleAssociate/current'
            });
        }
        else {
            dijit.byId('userProfile').hide();
            dijit.byId('userProfileFormOverlay').hide();
        }
    }
})(); // End of the function limiting the scope of the private variables
