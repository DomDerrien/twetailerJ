if (!dojo._hasResource["twetailer.Maezel"]) {
	dojo._hasResource["twetailer.Maezel"] = true;

	( function() { // To limit the scope of the private variables

		var module = dojo.provide("twetailer.Maezel");

		dojo.require("domderrien.i18n.LabelExtractor");

		var _useMockFiles = true,
			_labelExtractor;

		module.init = function() {
		};

		module.checkId = function(twitterId) {
			dojo.xhrGet({
				content: { twitterId: twitterId }, 
				handleAs: "json",
				load: function(response, request) {
					if (response.success) {
						dijit.byId("consumerKey").attr("value", response.resource.key); 
					}
				},
				preventCache: true,
				url: "/API/maezel/checkId"
			});
		};
		
		module.extractAttributes = function(tweet) {
			tweet = tweet.replace(/\@\w+/, '');
			var expiration = /expires:(\d\d\d\d-\d\d-\d\d)/.exec(tweet);
			dijit.byId('expirationDate').attr('value', expiration[1]);
			tweet = tweet.replace(expiration[0], "");
			var range = /range:(\w+)\s?(miles|km)/.exec(tweet);
			dijit.byId('range').attr('value', range[1]);
			if (range[2] == 'miles') {
				dijit.byId('rangeUnit').attr('value', 'mi');
			}
			tweet = tweet.replace(range[0], "");
			var city = /locale:(\w+)\s?(US|CA)/.exec(tweet);
			dijit.byId('postalCode').attr('value', city[1]);
			if (city[2] == 'US') {
				dijit.byId('countryCode').attr('value', 'US');
			}
			tweet = tweet.replace(city[0], "");
			dijit.byId('criteria').attr('value', dojo.trim(tweet));
		};
		
		module.submitRequest = function() {
			var content = {
				twitterId: dijit.byId('twitterId').attr('value'),
				consumerKey: dijit.byId('consumerKey').attr('value'),
				criteria: dijit.byId('criteria').attr('value'),
				expirationDate: dijit.byId('expirationDate').attr('value') + "T20:00:00",
				range: dijit.byId('range').attr('value'),
				rangeUnit: dijit.byId('rangeUnit').attr('value'),
				postalCode: dijit.byId('postalCode').attr('value'),
				countryCode: dijit.byId('countryCode').attr('value')
			};
			dojo.xhrGet({
				content: content, 
				handleAs: "json",
				load: function(response, request) {
					if (response.success) {
						dijit.byId("requestKey").attr("value", response.resourceId); 
					}
				},
				preventCache: true,
				url: "/API/maezel/createRequest"
			});
		};

		module.openTwitterConsole = function() {
			window.open(
					'http://twitter.com/home/?status=D ' + dijit.byId('twitterId').attr('value') +
					' Your request has been saved with the ID: ' + dijit.byId('requestKey').attr('value'),
					"twitter_window"
			);
		};
	})(); // End of the function limiting the scope of the private variables
}
