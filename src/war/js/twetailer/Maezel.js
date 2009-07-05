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
					var key = 0;
					if (response.success) {
						alert("New index: " + response.newSinceId);
					}
				},
				preventCache: true,
				url: "/API/maezel/processDMs"
			});
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
		
		var _request2tweet = function(request) {
			// @twetailer wii console range:25 miles locale:H9B 1X9 CA expires:2009-07-10
			var message = request.criteria +
				" range:" + request.range + " " + (request.rangeUnit == "km" ? "km" : "miles") +
				" locale:" + request.postalCode + " " + request.countryCode +
				" expires:" + request.expirationDate.substring(0, 10);
			return request.key != 0 ? "reference:" + request.key + " " + message : message;
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
						dojo.style(dojo.byId("list.bullet" + i), 'visibility', 'visible');
					}
					for (i = limit; i <10; ++i) {
						dojo.style(dojo.byId("list.bullet" + i), 'visibility', 'hidden');
					}
				},
				preventCache: true,
				url: "/API/maezel/getRequests"
			});
		};

		module.getRequest = function(prefix) {
			var content = {
				consumerKey: dijit.byId(prefix + '.consumerKey').attr('value'),
				key: dijit.byId(prefix + '.requestId').attr('value')
			};
			dojo.xhrGet({
				content: content, 
				handleAs: "json",
				load: function(response, request) {
					var requestDetails = "null";
					if (response.success) {
						requestDetails = _request2tweet(response.resource);
					}
					dijit.byId(prefix + ".requestDetails").attr("value", requestDetails);
				},
				preventCache: true,
				url: "/API/maezel/getRequest"
			});
		}

		module.deleteRequest = function(prefix) {
			var content = {
				consumerKey: dijit.byId(prefix + '.consumerKey').attr('value'),
				key: dijit.byId(prefix + '.requestId').attr('value')
			};
			dojo.xhrGet({
				content: content, 
				handleAs: "json",
				load: function(response, request) {
					dijit.byId(prefix + ".response").attr("value", response.success);
				},
				preventCache: true,
				url: "/API/maezel/deleteRequest"
			});
		}

		module.openTwitterConsole = function(prefix, idx) {
			var message = 'unsupported situation';
			switch(prefix) {
			case 'create':
				message = 'Your demand has been saved with the reference: ' +  dijit.byId('create.response').attr('value');
				break;
			case 'list':
				message = 'Demand ' +  dijit.byId('list.response' + idx).attr('value');
				break;
			case 'delete':
				if (idx == 0) { // ask
					message = 'Confirm deletion of Demand reference:' + dijit.byId('delete.requestId').attr('value') + ' (Y/N)';
				}
				else { // if (idx == 1) { // confirm
					message = 'Demand reference:' + dijit.byId('delete.requestId').attr('value') + ' deleted';
				}
				break;
			default:
				alert(message);
				return;
			}
			var twitterId = dijit.byId(prefix + '.twitterId').attr('value');
			window.open('http://twitter.com/home/?status=D ' + twitterId + ' ' + message, "twitter_window");
		};
	})(); // End of the function limiting the scope of the private variables
}
