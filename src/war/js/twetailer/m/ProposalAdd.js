dojo.provide('twetailer.m.ProposalAdd');

dojo.require('dojo.string');
dojo.require('dojox.mobile.ScrollableView');

dojo.declare('twetailer.m.ProposalAdd', [dojox.mobile.ScrollableView], {

    demand: null,

    iconDemand: dojo.moduleUrl('twetailer.m', 'resources/images/cart.png'),
    iconProposal: dojo.moduleUrl('twetailer.m', 'resources/images/database.png'),
    iconAddProposal: dojo.moduleUrl('twetailer.m', 'resources/images/database_add.png'),

    setDemand: function(demand) {
        this.demand = demand;
        dojo.byId('demand.key').appendChild(document.createTextNode(demand.key));
        dojo.byId('demand.content').appendChild(document.createTextNode(demand.content));
        dojo.byId('demand.dueDate').appendChild(document.createTextNode(demand.dueDate));
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
