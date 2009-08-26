if (!dojo._hasResource["twetailer.Maezel"]) {
	dojo._hasResource["twetailer.Maezel"] = true;

	( function() { // To limit the scope of the private variables

		var module = dojo.provide("twetailer.Maezel");

		dojo.require("domderrien.i18n.LabelExtractor");

		var _useMockFiles = true,
			_labelExtractor;

		module.init = function() {
		};
		
		module.delegateProcess = function(buttonId, processor) {
			dijit.byId(buttonId).attr('disabled', true);
			displayResponse(null);
			dojo.xhrGet({
				content: null, 
				handleAs: "json",
				load: function(response, request) {
					dijit.byId(buttonId).attr('disabled', false);
					displayResponse(response);
				},
				error: function(error, request) {
					dijit.byId(buttonId).attr('disabled', false);
					displayResponse(response);
				},
				preventCache: true,
				url: "/API/maezel/" + processor
			});
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
