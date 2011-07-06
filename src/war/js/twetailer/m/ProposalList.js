dojo.provide('twetailer.m.ProposalList');

dojo.require('dojo.string');
dojo.require('dojox.mobile.ScrollableView');

dojo.declare('twetailer.m.ProposalList', [dojox.mobile.ScrollableView], {

    demand: null,
    proposalList: {},

    proposalTemplateString: '<div class="itemIcon"><img src="${_iconProposal}"/></div>' +
    '<div class="itemActions"><a href="javascript:dijit.byId(\'proposalList\').removeProposal(${key})"><img src="${_iconRemoveProposal}"/></a></div>' +
    '<div class="itemDetails"> ' +
        '<div class="itemContent">${_content}</div>' +
        '<div class="itemOthers">' +
            '<div>Unit price: <span>$${_price}</span>, Total cost: <span>$${total}</span></div>' +
            '<div>Due: <span>${dueDate}</span>, State: <span>${state}</span>, Hash tags: ${_hashTags}</div>' +
            '<div>Review: <span>${_review}</span></div>' +
        '</div>' +
    '</div><div class="itemClear"></div>',

    iconRefreshUrl: dojo.moduleUrl('twetailer.m', 'resources/images/refresh.png').path,
    iconLoadingUrl: dojo.moduleUrl('twetailer.m', 'resources/images/loading.gif').path,
    iconProposalUrl: dojo.moduleUrl('twetailer.m', 'resources/images/database.png').path,
    iconAddProposalUrl: dojo.moduleUrl('twetailer.m', 'resources/images/database_add.png').path,
    iconRemoveProposalUrl: dojo.moduleUrl('twetailer.m', 'resources/images/database_delete.png').path,

    setDemand: function(demand) {
        this.demand = demand;
        var message = dojo.byId('messageProposalList');
        if (demand) {
            message.innerHTML = 'This demand ' + demand.key + ' has ' + (demand.proposalKeys ? demand.proposalKeys.length : 0) + ' associated proposal(s). ' +
                'Press <span style="border: 1px inset #9CACC0;border-radius:5px;-webkit-border-radius:5px;padding:6px 3px 0 3px;vertical-align:middle;"><a href="javascript:dijit.byId(\'proposalList\').addProposal()"><img src="' + this.iconAddProposalUrl + '" style="width:24px;height:24px;"/></a></span> to create a new Proposal for this Demand.';
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
        this.refreshButton.iconNode.src = this.iconRefreshUrl;

        dojo.connect(this.refreshButton, 'onClick', this, 'refresh');
    },

    refresh: function() {
        var demand = this.demand;
        if (!demand.proposalKeys || demand.proposalKeys.length == 0) {
            return;
        }

        this.refreshButton.iconNode.src = this.iconLoadingUrl;
        this.refreshButton.select(); // Button has been 'pressed'

        // Get the proposal list
        var deferred = dojo.xhrGet({
            content: { demandKey: demand.key, pointOfView: 'SALE_ASSOCIATE', related: ['Location'] },
            handleAs: 'json',
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            preventCache: true,
            url: '/API/Proposal/'
        });

        this.listNode.domNode.innerHTML = '';
        deferred.then(dojo.hitch(this, this.displayProposals), dojo.hitch(this, function(err) {
            // This shouldn't occur, but it's defined just in case
            alert('An error occurred: ' + err);
        }));
    },

    displayProposals: function(response) {
        this.refreshButton.iconNode.src = this.iconRefreshUrl;
        this.refreshButton.select(true);

        if (response && response.success && 0 < response.resources.length) {
            dojo.forEach(response.resources, dojo.hitch(this, function(proposal) {
                this.proposalList['p' + proposal.key] = proposal;
                var placeHolder = new dojox.mobile.ListItem({
                    'class': 'viewListItem'
                }).placeAt(this.listNode,'first');
                proposal._content = proposal.content ? proposal.content : 'no details';
                proposal._hashTags = proposal.hashTags ? proposal.hashTags : 'none';
                proposal._price = proposal.price ? proposal.price : '-';
                proposal._review = proposal.score == 0 ? 'not yet reviewed' : (proposal.score == 3 ? ':-(' : proposal.score < 5 ? ':-|' : ':-)') + (proposal.comment || '');
                proposal._iconProposal = this.iconProposalUrl;
                proposal._iconRemoveProposal = this.iconRemoveProposalUrl;
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
