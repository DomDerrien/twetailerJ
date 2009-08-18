if (!dojo._hasResource["twetailer.Maezel"]) {
	dojo._hasResource["twetailer.Maezel"] = true;

	( function() { // To limit the scope of the private variables

		var module = dojo.provide("twetailer.Maezel");

		dojo.require("domderrien.i18n.LabelExtractor");

		var _useMockFiles = true,
			_labelExtractor;

		module.init = function() {
		};
		
		module.processDMs = function(buttonId) {
			dijit.byId(buttonId).setDisabled(true);
			displayResponse(null);
			dojo.xhrGet({
				content: null, 
				handleAs: "json",
				load: function(response, request) {
					dijit.byId(buttonId).setDisabled(false);
					displayResponse(response);
				},
				preventCache: true,
				url: "/API/maezel/processDMs"
			});
		};
		
		module.validateOpenDemands = function(buttonId) {
			dijit.byId(buttonId).setDisabled(true);
			displayResponse(null);
			dojo.xhrGet({
				content: null, 
				handleAs: "json",
				load: function(response, request) {
					dijit.byId(buttonId).setDisabled(false);
					displayResponse(response);
				},
				preventCache: true,
				url: "/API/maezel/validateOpenDemands"
			});
		};
		
		module.processPubDemands = function(buttonId) {
			dijit.byId(buttonId).setDisabled(true);
			displayResponse(null);
			dojo.xhrGet({
				content: null, 
				handleAs: "json",
				load: function(response, request) {
					dijit.byId(buttonId).setDisabled(false);
					displayResponse(response);
				},
				preventCache: true,
				url: "/API/maezel/processPubDemands"
			});
		};
		
		module.processProposals = function(buttonId) {
			dijit.byId(buttonId).setDisabled(true);
			alert("Not yet implemented!");
			/*
			displayResponse(null);
			dojo.xhrGet({
				content: null, 
				handleAs: "json",
				load: function(response, request) {
					dijit.byId(buttonId).setDisabled(false);
					displayResponse(response);
				},
				preventCache: true,
				url: "/API/maezel/processProposals"
			});
			*/
		};
		
		var displayResponse = function(response) {
			var feedbackArea = dojo.byId("feedbackArea");
			if (response != null) {
				feedbackArea.innerHTML =
					"<br/>----- " + (new Date()) + " -----<br/>" +
					dojo.toJson(response, true).replace(/</g, "&lt;").replace(/>/g, "&gt") + "<br/>" +
					feedbackArea.innerHTML;
			}
			else {
				feedbackArea.innerHTML = "<br/>..." + feedbackArea.innerHTML;
			}
		}
	})(); // End of the function limiting the scope of the private variables
}
