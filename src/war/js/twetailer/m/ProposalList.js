dojo.provide('twetailer.m.ProposalList');

dojo.require('dojo.string');
dojo.require('dojox.mobile.ScrollableView');

dojo.declare('twetailer.m.ProposalList', [dojox.mobile.ScrollableView], {

    demand: null,
    proposalList: {},

    proposalTemplateString: '<div class="itemIcon"><img src="${_iconProposal}"/></div>' +
    '<div class="itemActions"><a href="javascript:dijit.byId(\'proposalList\').removeProposal(${key})"><img src="${_iconRemoveProposal}"/></a></div>' +
    '<div class="itemDetails"> ' +
        '<div class="itemContent">${content}</div>' +
        '<div class="itemOthers">Unit price: <span>$${_price}</span>, Total cost: <span>$${total}</span><br/>' +
        'Due: <span>${dueDate}</span>, State: <span>${state}</span>, Hash tags: ${_hashTags}</div>' +
    '</div><div class="itemClear"></div>',

    iconRefresh: dojo.moduleUrl('twetailer.m', 'resources/images/refresh.png'),
    iconLoading: dojo.moduleUrl('twetailer.m', 'resources/images/loading.gif'),
    iconProposal: dojo.moduleUrl('twetailer.m', 'resources/images/database.png'),
    iconAddProposal: dojo.moduleUrl('twetailer.m', 'resources/images/database_add.png'),
    iconRemoveProposal: dojo.moduleUrl('twetailer.m', 'resources/images/database_delete.png'),

    setDemand: function(demand) {
        this.demand = demand;
        var message = dojo.byId('messageProposalList');
        if (demand) {
            message.innerHTML = 'This demand ' + demand.key + ' has ' + (demand.proposalKeys ? demand.proposalKeys.length : 0) + ' associated proposal(s). ' +
                'Press <span style="border: 1px inset #9CACC0;border-radius:5px;-webkit-border-radius:5px;padding:6px 3px 0 3px;vertical-align:middle;"><a href="javascript:dijit.byId(\'proposalList\').addProposal()"><img src="' + this.iconAddProposal + '" style="width:24px;height:24px;"/></a></span> to create a new Proposal for this Demand.';
            dojo.style(message, 'display', '');
        }
        else {
            dojo.style(message, 'display', 'none');
        }
        this.refresh();
    },

    startup: function() {
        this.inherited(arguments);

        this.listNode = dijit.byId(dojo.query('.viewList', this.domNode)[0].id);
        this.refreshButton = dijit.byId(dojo.query('.viewRefresh', this.domNode)[0].id);
        this.refreshButton.iconNode.src = this.iconRefresh;

        dojo.connect(this.refreshButton, 'onClick', this, 'refresh');
    },

    refresh: function() {
        this.refreshButton.iconNode.src = this.iconLoading;
        this.refreshButton.select(); // Button has been 'pressed'

        // Get the proposal list
        var deferred = dojo.xhrGet({
            content: { demandKey: this.demand ? this.demand.key : null, pointOfView: 'SALE_ASSOCIATE', related: ['Location'] },
            handleAs: 'json',
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            preventCache: true,
            url: '/API/Proposal/'
        });

        deferred.then(dojo.hitch(this, this.displayProposals), dojo.hitch(this, function(err) {
            // This shouldn't occur, but it's defined just in case
            alert('An error occurred: ' + err);
        }));
    },

    displayProposals: function(response) {
        this.refreshButton.iconNode.src = this.iconRefresh;
        this.refreshButton.select(true);

        this.listNode.domNode.innerHTML = '';
        if (response && response.success && 0 < response.resources.length) {
            dojo.forEach(response.resources, dojo.hitch(this, function(proposal) {
                this.proposalList['p' + proposal.key] = proposal;
                var placeHolder = new dojox.mobile.ListItem({
                    'class': 'viewListItem'
                }).placeAt(this.listNode,'first');
                proposal._hashTags = proposal.hashTags ? proposal.hashTags : 'none';
                proposal._price = proposal.price ? proposal.price : '-';
                proposal._iconProposal = this.iconProposal;
                proposal._iconRemoveProposal = this.iconRemoveProposal;
                try {
                    placeHolder.containerNode.innerHTML = dojo.string.substitute(this.proposalTemplateString, proposal);
                }
                catch(ex) {
                    placeHolder.containerNode.innerHTML = 'Could not render: ' + dojo.toJson(proposal);
                }
            }));
        }
    },

    addProposal: function(demandKey) {
        dijit.byId('proposalAdd').setDemand(this.demand);
        dijit.byId('proposalList').performTransition('proposalAdd', 1, 'slide', null, null);
    },

    removeProposal: function(demandKey) {
        // Not yet implemented
        console.log('Not yet implemented!');
    }
});
