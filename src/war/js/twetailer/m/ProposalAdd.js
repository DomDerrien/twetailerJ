dojo.provide('twetailer.m.ProposalAdd');

dojo.require('dojo.string');
dojo.require('dojox.mobile.ScrollableView');

dojo.declare('twetailer.m.ProposalAdd', [dojox.mobile.ScrollableView], {

    demand: null,

    iconDemandUrl: dojo.moduleUrl('twetailer.m', 'resources/images/cart.png').path,
    iconProposalUrl: dojo.moduleUrl('twetailer.m', 'resources/images/database.png').path,
    iconAddProposalUrl: dojo.moduleUrl('twetailer.m', 'resources/images/database_add.png').path,

    setDemand: function(demand) {
        this.demand = demand;
        var set = function(id,data) { var w = dojo.byId(id); w.innerHTML = ''; w.appendChild(document.createTextNode(data)); };
        set('demand.key', demand.key);
        set('demand.content', demand.content);
        set('demand.dueDate', demand.dueDate);
//        dojo.byId('demand.key').appendChild(document.createTextNode(demand.key));
//        dojo.byId('demand.content').appendChild(document.createTextNode(demand.content));
//        dojo.byId('demand.dueDate').appendChild(document.createTextNode(demand.dueDate));
    },

    startup: function() {
        this.inherited(arguments);

        var controller = this.controller;

        dojo.connect(dijit.byId('proposalCreate'), 'onClick', function() {
            // Not yet implemented
            console.log('Not yet implemented!');
        });
    },
});
