if (!dojo._hasResource["twetailer.Maezel"]) {
	dojo._hasResource["twetailer.Maezel"] = true;

	( function() { // To limit the scope of the private variables

		var module = dojo.provide("twetailer.Maezel");

		dojo.require("domderrien.i18n.LabelExtractor");

		var _useMockFiles = true,
			_labelExtractor;

		module.init = function() {
		};

		module.checkId = function(prefix) {
			var twitterId = dijit.byId(prefix + '.twitterId').attr('value');
			dojo.xhrGet({
				content: { twitterId: twitterId }, 
				handleAs: "json",
				load: function(response, request) {
					var key = 0;
					if (response.success) {
						key = response.resource.key; 
					}
					dijit.byId(prefix + ".consumerKey").attr("value", key);
				},
				preventCache: true,
				url: "/API/maezel/checkId"
			});
		};
		
		module.extractAttributes = function(tweet) {
			tweet = tweet.replace(/\@\w+/, '');
			var expiration = /expires:(\d\d\d\d-\d\d-\d\d)/.exec(tweet);
			dijit.byId('create.expirationDate').attr('value', expiration[1]);
			tweet = tweet.replace(expiration[0], "");
			var range = /range:(\w+)\s?(miles|km)/.exec(tweet);
			dijit.byId('create.range').attr('value', range[1]);
			if (range[2] == 'miles') {
				dijit.byId('create.rangeUnit').attr('value', 'mi');
			}
			tweet = tweet.replace(range[0], "");
			var city = /locale:(\w+\s?\w+)\s?(US|CA)/.exec(tweet);
			dijit.byId('create.postalCode').attr('value', city[1]);
			if (city[2] == 'US') {
				dijit.byId('create.countryCode').attr('value', 'US');
			}
			tweet = tweet.replace(city[0], "");
			dijit.byId('create.criteria').attr('value', dojo.trim(tweet));
		};
		
		module.createRequest = function() {
			var content = {
				twitterId: dijit.byId('create.twitterId').attr('value'),
				consumerKey: dijit.byId('create.consumerKey').attr('value'),
				criteria: dijit.byId('create.criteria').attr('value'),
				expirationDate: dijit.byId('create.expirationDate').attr('value') + "T20:00:00",
				range: dijit.byId('create.range').attr('value'),
				rangeUnit: dijit.byId('create.rangeUnit').attr('value'),
				postalCode: dijit.byId('create.postalCode').attr('value'),
				countryCode: dijit.byId('create.countryCode').attr('value')
			};
			dojo.xhrGet({
				content: content, 
				handleAs: "json",
				load: function(response, request) {
					var key = 0;
					if (response.success) {
						key = response.resourceId;
					}
					dijit.byId("create.response").attr("value", key); 
				},
				preventCache: true,
				url: "/API/maezel/createRequest"
			});
		};

		module.openTwitterConsole = function(prefix, responseIdx) {
			var message = 'unsupported situation';
			switch(prefix) {
			case 'create':
				message = 'Your demand has been saved with the ID: ' +  dijit.byId(prefix + '.response').attr('value');
				break;
			case 'list':
				message = 'Demand ' + responseIdx + ': ' +  dijit.byId(prefix + '.response' + responseIdx).attr('value');
				break;
			default:
				alert(message);
				return;
			}
			var twitterId = dijit.byId(prefix + '.twitterId').attr('value');
			window.open('http://twitter.com/home/?status=D ' + twitterId + ' ' + message, "twitter_window");
		};
		
		var _request2tweet = function(request) {
			// @twetailer wii console range:25 miles locale:H9B 1X9 CA expires:2009-07-10
			return request.criteria +
				" range:" + request.range + " " + (request.rangeUnit == "km" ? "km" : "miles") +
				" locale:" + request.postalCode + " " + request.countryCode +
				" expires:" + request.expirationDate.substring(0, 10);
		}
		module.listRequests = function() {
			var content = {
				qA: 'consumerKey',
				qV: dijit.byId('list.consumerKey').attr('value'),
			};
			dojo.xhrGet({
				content: content, 
				handleAs: "json",
				load: function(response, request) {
					var key = 0;
					if (response.success) {
						key = response.resourceId;
					}
					var i, limit = response.resources == null ? 0 : 10 < response.resources.length ? 10 : response.resources.length;
					for (i = 0; i < limit; ++i) {
						dijit.byId("list.response" + i).attr("value", _request2tweet(response.resources[i]));
						dojo.style(dojo.byId("list.bullet" + i), 'display', 'block');
					}
					for (i = limit; i <10; ++i) {
						dojo.style(dojo.byId("list.bullet" + i), 'display', 'none');
					}
				},
				preventCache: true,
				url: "/API/maezel/getRequests"
			});
		};
	})(); // End of the function limiting the scope of the private variables
}
