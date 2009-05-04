if (!dojo._hasResource["twetailer.UIControl"]) {
	dojo._hasResource["twetailer.UIControl"] = true;

	( function() { // To limit the scope of the private variables

		var module = dojo.provide("twetailer.Console");

		module.activatePage = function(pageId) {
			var isAuthenticated = module.isSessionAuthenticated();
			/*
			if (pageId == "Consumer") {
				// Attach a selection processor to change the postal code validator to match the selected country
				dojo.connect(dijit.byId("consumerCountry"), "onChange", function(event){
					var validator = null;
					switch(event) {
					case "ca": validator = dojox.validate.ca.isPostalCode; break;
					case "us": validator = dojox.validate.us.isZipCode; break;
					default: validator = function() { return true; };
					}
					dijit.byId("consumerPostalCode").validator = validator;
				});

				// Set the default location
				dijit.byId("consumerCountry").attr("value", "ca");
			}
			*/
			// Put the inital focus into the consumer search field
			if (pageId == "Consumer-All Products") {
				dijit.byId("consumerAllProductsSearchCriteria").focus();
				dojo.query("#consumerAllProductsSearchCriteria").onkeypress(function(event) {
					if (event.keyCode == dojo.keys.ENTER) {
						dijit.byId("consumerAllProductsSearchSubmit").focus();
						dijit.byId("consumerAllProductsSearchSubmit").onClick();
					}
		        });
				var grid = dijit.byId("consumerProductTable");
			    dijit.byId("consumerProductTableMenu").bindDomNode(grid.domNode);
				grid.onCellContextMenu = function(e) {
					cellNode = e.cellNode;
					rowIndex = e.rowIndex;
				};
			}
			else if (pageId == "Consumer-Your Requests") {
				var searchButton = dijit.byId("consumerRequestsSearchSubmit");
				if(!isAuthenticated) {
					searchButton.attr("disabled", true);
				}
				else {
					searchButton.focus();
				}
				var grid = dijit.byId("consumerRequestTable");
			    dijit.byId("consumerRequestTableMenu").bindDomNode(grid.domNode);
				grid.onCellContextMenu = function(e) {
					cellNode = e.cellNode;
					rowIndex = e.rowIndex;
				};
			}
			else if (pageId == "Consumer-Your Account") {
				if (window.google && google.gears) {
					dojo.byId("adviceToInstallGears").style.display = "none";
				}
				// Attach a selection processor to change the postal code validator to match the selected country
				dojo.connect(dijit.byId("consumerCountry"), "onChange", function(event){
					var validator = null;
					switch(event) {
					case "CA": validator = dojox.validate.ca.isPostalCode; break;
					case "US": validator = dojox.validate.us.isZipCode; break;
					default: validator = function() { return true; };
					}
					dijit.byId("consumerPostalCode").validator = validator;
				});
				if(!isAuthenticated) {
					return;
				}
			}
		};

		module.updateConsumerAccountAddress = function(position) {
			dijit.byId("consumerGeoLatitude").attr("value", position.latitude);
			dijit.byId("consumerGeoLongitude").attr("value", position.longitude);
			if (position.gearsAddress) {
				dijit.byId("consumerProfileStreet1").attr("value", position.gearsAddress.streetNumber + ", " + position.gearsAddress.street);
				dijit.byId("consumerProfileStreet2").attr("value", "");
				dijit.byId("consumerProfileCity").attr("value", position.gearsAddress.city);
				dijit.byId("consumerProfilePostalCode").attr("value", position.gearsAddress.postalCode);
				dijit.byId("consumerProfileCountry").attr("value", position.gearsAddress.countryCode);
			}
		};

		module.getConsumerRequestLocation = function(inRowItem, inItem) {
			if (inItem != null) {
				return "<div ondblclick=\"alert('Not yet implemented');\">Dom</div>";
			}
		};

		module.getRetailerRequestLocation = function(inRowItem, inItem) {
			module.getConsumerRequestLocation(inRowItem, InItem);
		};

		/*
		var _adjustCountrySelectBoxWidth = function() {
			var pane = dojo.byId("consumerRightPane");
			// console.log("width:" + (parseInt(pane.style.width) - 130) + "px")
			dijit.byId("consumerCountry").attr("style", "width:" + (parseInt(pane.style.width) - 130) + "px");
		};
		*/

	})(); // End of the function limiting the scope of the private variables
}
