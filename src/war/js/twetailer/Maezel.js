if (!dojo._hasResource["twetailer.Maezel"]) {
	dojo._hasResource["twetailer.Maezel"] = true;

	( function() { // To limit the scope of the private variables

		var module = dojo.provide("twetailer.Maezel");

		dojo.require("domderrien.i18n.LabelExtractor");

		var _useMockFiles = true,
			_labelExtractor;

		module.init = function() {
		};
		
		module.processDMs = function() {
			dojo.xhrGet({
				content: null, 
				handleAs: "json",
				load: function(response, request) {
					displayResponse(response);
				},
				preventCache: true,
				url: "/API/maezel/processDMs"
			});
		};
		
		module.processPubDemands = function() {
			alert("Not yet implemented!");
			/*
			dojo.xhrGet({
				content: null, 
				handleAs: "json",
				load: function(response, request) {
					displayResponse(response);
				},
				preventCache: true,
				url: "/API/maezel/processPublishedDemands"
			});
			*/
		};
		
		module.processProposals = function() {
			alert("Not yet implemented!");
			/*
			dojo.xhrGet({
				content: null, 
				handleAs: "json",
				load: function(response, request) {
					displayResponse(response);
				},
				preventCache: true,
				url: "/API/maezel/processProposals"
			});
			*/
		};
		
		var displayResponse = function(response) {
			var feedbackArea = dojo.byId("feedbackArea");
			feedbackArea.innerHTML =
				"<br/>----- " + (new Date()) + " -----<br/>" +
				dojo.toJson(response, true) + "<br/>" +
				feedbackArea.innerHTML;
		}
	})(); // End of the function limiting the scope of the private variables
}
