if (!dojo._hasResource["twetailer.ModelControl"]) {
	dojo._hasResource["twetailer.ModelControl"] = true;

	( function() { // To limit the scope of the private variables

		var module = dojo.provide("twetailer.Console");

		module.Request = {
			select: function(data, onSuccessCallback, onFailureCallback) {
				dojo.xhrGet({
					content: data,
					handleAs: "json",
					load: onSuccessCallback,
					error: onFailureCallback,
					url: "/API/requests"
				});
			},
			get: function(key, data, onSuccessCallback, onFailureCallback) {
				dojo.xhrGet({
					content: data,
					handleAs: "json",
					load: onSuccessCallback,
					error: onFailureCallback,
					url: "/API/requests/" + key
				});
			},
			create: function(data, onSuccessCallback, onFailureCallback) {
				dojo.xhrPost({ // Should be put but Python/WSGI self.request.get() can only extract parameters for GET and POST!!!
					content: data,
					handleAs: "json",
					load: onSuccessCallback,
					error: onFailureCallback,
					url: "/API/requests"
				});
			},
			update: function(key, data, onSuccessCallback, onFailureCallback) {
				dojo.xhrPost({
					content: data,
					handleAs: "json",
					load: onSuccessCallback,
					error: onFailureCallback,
					url: "/API/requests/" + key
				});
			},
			remove: function(key, onSuccessCallback, onFailureCallback) {
				dojo.xhrDelete({
					handleAs: "json",
					load: onSuccessCallback,
					error: onFailureCallback,
					url: "/API/requests/" + key
				});
			}
		};
		
		module.Product = {
			select: function(data, onSuccessCallback, onFailureCallback) {
				dojo.xhrGet({
					content: data,
					handleAs: "json",
					load: onSuccessCallback,
					error: onFailureCallback,
					url: "/API/products"
				});
			},
			get: function(data, onSuccessCallback, onFailureCallback) {
				alert("Not yet implemented!");
			},
			create: function(data, onSuccessCallback, onFailureCallback) {
				alert("Not yet implemented!");
			},
			update: function(data, onSuccessCallback, onFailureCallback) {
				alert("Not yet implemented!");
			},
			remove: function(data, onSuccessCallback, onFailureCallback) {
				alert("Not yet implemented!");
			}
		};

	})(); // End of the function limiting the scope of the private variables
}
