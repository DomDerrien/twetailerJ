(function() { // To limit the scope of the private variables

    var module = dojo.provide('twetailer.Associate');

    dojo.require('twetailer.Common');

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
     */
    module.init = function(locale) {
        _getLabel = _globalCommon.init(locale);

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
        return cellContent + _localCommon.displayProposalKeys(proposalKeys, rowIndex, updateLabel);
    };

    /**
     * Call the back-end to get the new Demands.
     */
    module.loadNewDemands = function() {
        var lastDemand = _globalCommon.getLastDemand();
        var lastModificationDate = lastDemand ? lastDemand.modificationDate : null;
        var dfd = _globalCommon.loadRemoteDemands(lastModificationDate, 'demandListOverlay', _queryPointOfView, null);
        dfd.addCallback(function(response) { dijit.byId('refreshButton').resetTimeout(); _globalCommon.processDemandList(response.resources, _grid); });
    };
})(); // End of the function limiting the scope of the private variables
