dojo.provide('twetailer.m.DemandView');

dojo.require("dojo.string");
dojo.require('dojox.mobile.ScrollableView');

dojo.declare('twetailer.m.DemandView', [dojox.mobile.ScrollableView], {

    demandTemplateString: '<div class="demandActions"><a href="#" onclick="twetailer.m.DemandView.switchToProposalAdd(${key});return false"><img src="/images/page_white_come_add.png"></a></div>' +
    '<div class="demandDetails"> ' +
        '<div class="demandContent">${content}</div>' +
        '<div class="demandOthers">Due: <span>${dueDate}</span>, State: <span>${state}</span>, Hash tags: ${hashTags}</div>' +
    '</div><div class="listItemClear"></div>',

    iconRefresh: dojo.moduleUrl('twetailer.m', 'resources/images/refresh.png'),
    iconLoading: dojo.moduleUrl('twetailer.m', 'resources/images/loading.gif'),

    startup: function() {
        this.inherited(arguments);

        this.listNode = dijit.byId(dojo.query('.viewList', this.domNode)[0].id);
        this.refreshButton = dijit.byId(dojo.query('.viewRefresh', this.domNode)[0].id);
        this.refreshButton.iconNode.src = this.iconRefresh;

        dojo.connect(this.refreshButton, 'onClick', this, 'refresh');
    },

    refresh: function() {
        console.log("refresh called");
        this.refreshButton.iconNode.src = this.iconLoading;
        this.refreshButton.select(); // Button has been 'pressed'

        // Get the demand list
        var deferred = dojo.xhrGet({
            content: { pointOfView: 'SALE_ASSOCIATE', related: ['Location'] },
            handleAs: 'json',
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            preventCache: true,
            url: '/API/Demand/'
        });
        console.log("xhrGet issued");

        deferred.then(dojo.hitch(this, this.displayDemands), dojo.hitch(this, function(err) {
            // This shouldn't occur, but it's defined just in case
            alert('An error occurred: ' + err);
        }));
    },

    displayDemands: function(response) {
        this.refreshButton.iconNode.src = this.iconRefresh;
        this.refreshButton.select(true);

        console.log(response ? "response: " + dojo.toJson(response) : "response: null");
        if (response && response.success && 0 < response.resources.length) {
            dojo.forEach(response.resources, dojo.hitch(this, function(demand) {
                twetailer.m.DemandView.demandList['d' + demand.key] = demand;
                var placeHolder = new dojox.mobile.ListItem({
                    'class': 'viewListItem'
                }).placeAt(this.listNode,'first');
                placeHolder.containerNode.innerHTML = dojo.string.substitute(this.demandTemplateString, demand);
            }));
        }
    }
});

twetailer.m.DemandView.demandList = {};
twetailer.m.DemandView.switchToProposalAdd = function(demandKey) {
    var demand = twetailer.m.DemandView.demandList['d' + demandKey];
    dojo.byId('demand.key').appendChild(document.createTextNode(demand.key));
    dojo.byId('demand.content').appendChild(document.createTextNode(demand.content));
    dojo.byId('demand.dueDate').appendChild(document.createTextNode(demand.dueDate));
    dijit.byId('demands').performTransition('proposalAdd', 1, 'slide', null, null);
}
