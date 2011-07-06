dojo.provide('twetailer.m.DemandList');

dojo.require('dojo.string');
dojo.require('dojox.mobile.ScrollableView');

dojo.declare('twetailer.m.DemandList', [dojox.mobile.ScrollableView], {

    demandList: {},

    demandTemplateString: '<div onClick="dijit.byId(\'demandList\').switchToProposalList(${key})">' +
    '<div class="itemIcon"><img src="${_iconDemand}"/></div>' +
    '<div class="itemActions"><a href="javascript:dijit.byId(\'demandList\').switchToProposalList(${key})"><img src="${_iconManageProposals}"/></a><span>${_proposalNb}</span></div>' +
    '<div class="itemDetails"> ' +
        '<div class="itemContent">${_content}</div>' +
        '<div class="itemOthers">Due: <span>${dueDate}</span>, State: <span>${state}</span>, Hash tags: <span>${_hashTags}</span></div>' +
    '</div><div class="itemClear"></div>' +
    '</div>',

    iconRefreshUrl: dojo.moduleUrl('twetailer.m', 'resources/images/refresh.png').path,
    iconLoadingUrl: dojo.moduleUrl('twetailer.m', 'resources/images/loading.gif').path,
    iconDemandUrl: dojo.moduleUrl('twetailer.m', 'resources/images/cart.png').path,
    iconManageProposalsUrl: dojo.moduleUrl('twetailer.m', 'resources/images/database_go.png').path,

    startup: function() {
        this.inherited(arguments);

        this.listNode = dijit.byId(dojo.query('.viewList', this.domNode)[0].id);
        this.refreshButton = dijit.byId(dojo.query('.viewRefresh', this.domNode)[0].id);
        this.refreshButton.iconNode.src = this.iconRefreshUrl;

        dojo.connect(this.refreshButton, 'onClick', this, 'refresh');
        this.refresh();
    },

    refresh: function() {
        this.refreshButton.iconNode.src = this.iconLoadingUrl;
        this.refreshButton.select(); // Button has been 'pressed'

        // Get the demand list
        var deferred = dojo.xhrGet({
            content: { pointOfView: 'SALE_ASSOCIATE', related: ['Location'] },
            handleAs: 'json',
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            preventCache: true,
            url: '/API/Demand/'
        });

        this.listNode.domNode.innerHTML = '';
        deferred.then(dojo.hitch(this, this.displayDemands), dojo.hitch(this, function(err) {
            // This shouldn't occur, but it's defined just in case
            alert('An error occurred: ' + err);
        }));
    },

    displayDemands: function(response) {
        this.refreshButton.iconNode.src = this.iconRefreshUrl;
        this.refreshButton.select(true);

        if (response && response.success && 0 < response.resources.length) {
            dojo.forEach(response.resources, dojo.hitch(this, function(demand) {
                this.demandList['d' + demand.key] = demand;
                var placeHolder = new dojox.mobile.ListItem({
                    'class': 'viewListItem'
                }).placeAt(this.listNode,'first');
                demand._content = demand.content ? demand.content : 'no details';
                demand._hashTags = demand.hashTags ? demand.hashTags : 'none';
                demand._proposalNb = demand.proposalKeys ? demand.proposalKeys.length : 0;
                demand._iconDemand = this.iconDemandUrl;
                demand._iconManageProposals = this.iconManageProposalsUrl;
                try {
                    placeHolder.containerNode.innerHTML = dojo.string.substitute(this.demandTemplateString, demand);
                }
                catch(ex) {
                    placeHolder.containerNode.innerHTML = 'Could not render: ' + dojo.toJson(demand);
                }
            }));
        }
    },

    switchToProposalList: function(demandKey) {
        dijit.byId('proposalList').setDemand(this.demandList['d' + demandKey]);
        dijit.byId('demandList').performTransition('proposalList', 1, 'slide', null, null);
    }
});
