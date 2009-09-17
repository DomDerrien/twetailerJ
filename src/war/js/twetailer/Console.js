if (!dojo._hasResource["twetailer.Console"]) {
    dojo._hasResource["twetailer.Console"] = true;

    ( function() { // To limit the scope of the private variables

        var module = dojo.provide("twetailer.Console");

        dojo.require("dojo.number");
        dojo.require("dojo.date.locale");
        dojo.require("dojo.data.ItemFileReadStore");
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dojox.validate.ca");
        dojo.require("dojox.validate.us");

        dojo.require("domderrien.i18n.LabelExtractor");

        // Set of additional twetailer.Console methods
        dojo.require("twetailer.ModelControl", true);
        dojo.require("twetailer.UIControl", true);

        var _useMockFiles = true,
            _labelExtractor;

        /**
         * Parse the given sequence and select matching selectors accordingly.
         * @param {String} selectorSequence page selector identifiers comma-separated
         */
        var _preSelectActivePageSelector = function(selectorSequence) {
            // TODO: use a loop to select pages according to the sequence length, not just limited to the depth of 3 levels
            if (selectorSequence != null && 0 < selectorSequence.length) {
                var ids = selectorSequence.substr(1).split("-"); // Remove the '#' and get the menu identifiers
                var topItem = dijit.byId("pageSelector-" + ids[0]);
                if (topItem != null) {
                    dijit.byId("pageSelectorSystem").selectChild(topItem);
                    var subItem = ids.length == 1 ? null : dijit.byId("pageSelector-" + ids[0] + "-" + ids[1]);
                    if (subItem != null) {
                        dijit.byId("pageSelectorSystem-" + ids[0]).selectChild(subItem);
                        var subSubItem = ids.length == 2 ? null : dijit.byId("pageSelector-" + ids[0] + "-" + ids[1] + "-" + ids[2]);
                        if (subSubItem != null) {
                            dijit.byId("pageSelectorSystem-" + ids[0] + "-" + ids[1]).selectChild(subItem);
                        }
                    }
                }
            }
        };

        /**
         * Private variable exposed for unit test purpose
         * @private
         */
        module._appConfig = { isAuthenticated: false };

        /**
         * Return the authentication state of the application
         * @return {Boolean} <code>true</code> if the application settings report the user is authenticated, <code>false</code> otherwise
         */
        module.isSessionAuthenticated = function() {
            return module._appConfig.isAuthenticated;
        };

        module.getGeoLocFromGears = function() {
            if (window.google && google.gears) {
                var geo = google.gears.factory.create('beta.geolocation');
                geo.getCurrentPosition(
                        function(position) {
                            module._appConfig.geo = position;
                            dojo.publish("gears/geoLoc", [position]);
                        },
                        function(positionError) {
                            // TODO: display positionError.message in the right location
                            alert(positionError.message);
                        },
                        {
                            gearsRequestAddress: true,
                            gearsAddressLanguage: 'en_US',
                            maximumAge: Infinity
                        }
                );
            }
        }

        /**
         * Module initializer:<ul>
         *  <li>Load the localized bundle</li>
         *  <li>Get information about the logged user</li>
         *  <li>Attach event handlers on the content selectors</li>
         *  <li>Activate the content of the current one</li></ul>
         *
         * @param {String} baseFilename part of the localized resource bundle name
         *
         * @param {String} ISO locale identifier
         *
         * @param {Boolean} isUserAuthenticated <code>true</code> if the user is
         * authenticated, <code>false</code> otherwise
         */
        module.init = function(baseFilename, locale, isUserAuthenticated) {
            // Detection of the running mode (stand-alone or connected)
            _useMockFiles = window.location.protocol == "file:";

            // Get the localized resource bundle
            _labelExtractor = domderrien.i18n.LabelExtractor.init(
                    "twetailer",
                    baseFilename,
                    locale
                );

            // Get information on the logged user
            if (isUserAuthenticated) {
                dojo.xhrGet({
                    handleAs: "json",
                    load: function(response, request) {
                        module._appConfig.loggedUser = response;
                    },
                    preventCache: true,
                    sync: true,
                    url: "/API/users/current"
                });
            }
            return; // FIXME

            // Get the geographical location
            if (!module.isSessionAuthenticated()) {
                module.getGeoLocFromGears();
            }
            dojo.subscribe("gears/geoLoc", module.updateConsumerAccountAddress);

            // Change the predefined menu selection according to the URL hash information
            _preSelectActivePageSelector(location.hash);

            // Attach handlers to active pages when the corresponding tab is selected
            // and get the title path for the active menu path
            var activeSelectorPath = [];
            var tempClosure = function(topTitle, subTitle) {
                return function() {
                    var pageId = topTitle + (subTitle == null ? "" : "-" + subTitle);
                    location.hash = pageId;
                    module.activatePage(pageId);
                };
            };
            var selectorSystem = dijit.byId("pageSelectorSystem");
            var topSelectors = selectorSystem.getChildren();
            var iIndex = topSelectors.length, jIndex;
            while (0 < iIndex) {
                --iIndex;
                var topSelector = topSelectors[iIndex];
                if (topSelector.selected) {
                    activeSelectorPath[0] = topSelector.title;
                }
                // Note: 1 level of indirection because intermediate ContentPane used for the presentation
                if (topSelector.getChildren()[0].declaredClass == "dijit.layout.TabContainer") {
                    var subSelectors = topSelector.getChildren()[0].getChildren();
                    jIndex = subSelectors.length;
                    while (0 < jIndex) {
                        --jIndex;
                        var subSelector = subSelectors[jIndex];
                        if (subSelector.selected && activeSelectorPath.length == 1) {
                            activeSelectorPath[1] = subSelector.title;
                        }
                        dojo.connect(subSelector, "onShow", tempClosure(topSelector.title, subSelector.title));
                    }
                }
                else {
                    dojo.connect(topSelector, "onShow", tempClosure(topSelector.title));
                }
            }

            // Enable elements if the user is authenticated
            if (module.isSessionAuthenticated()) {
                dojo.query(".hiddenToNonAuthenticated").removeClass("hiddenToNonAuthenticated");
                dojo.query(".hiddenToAuthenticated").addClass("hiddenToNonAuthenticated");
            }

            // Update the content of the selected dashboard page
            module.activatePage(activeSelectorPath.join("-"));
        };

        /**
         * Verify the product search parameters and call the server with them
         */
        module.searchConsumerProducts = function() {
            var criteriaField = dijit.byId("consumerAllProductsSearchCriteria");
            if (!criteriaField.isValid()) {
                return;
            }
            var postalCodeField = dijit.byId("consumerProfilePostalCode");
            if (!postalCodeField.isValid()) {
                return;
            }
            module.Product.select({
                    country: dijit.byId("consumerProfileCountry").attr("value"),
                    distance: dijit.byId("consumerProfileDistance").attr("value"),
                    distanceUnit: dijit.byId("consumerProfileDistanceUnit").attr("value"),
                    keywords: criteriaField.attr("value"),
                    postalCode: postalCodeField.attr("value")
                },
                function(response) {
                    if (response == null || response.products == null || response.products.length === 0) {
                        if (!module.isSessionAuthenticated()) {
                            alert("No product matches your request. If you want to be notified when a corresponding product is made available, please login or create an account.");
                            // Nothing more can be done
                        }
                        else {
                            if (response == null || response.requests == null || response.requests.length == 0) {
                                dijit.byId("consumerConfirmRequestSaving").show();
                            }
                            else {
                                var viewSimilarRequests = confirm("No product matches your request. Do you want to see similar requests that have been served successfully?");
                                if (viewSimilarRequests) {
                                    dijit.byId("pageSelectorSystem").selectChild(dijit.byId("pageSelector-Consumer"));
                                    dijit.byId("pageSelectorSystem-Consumer").selectChild(dijit.byId("pageSelector-Consumer-Your Requests"));
                                    _consumerSimilarRequestFetch(response.requests);
                                }
                                else {
                                    dijit.byId("consumerConfirmRequestSaving").show();
                                }
                            }
                        }
                    }
                    else {
                        _updateConsumerProductTable(response.products);
                    }
                }
            );
        };

        /**
         * Send request parameters to the server for their saving. Once
         * the saving is confirmed, the table of the pending requests is displayed.
         */
        module.saveConsumerRequest = function() {
            module.Request.create({
                    // author: "automatically set!",
                    countryCode: dijit.byId("consumerProfileCountry").attr("value"),
                    // creationDate: "automatically set!",
                    distance: dijit.byId("consumerProfileDistance").attr("value"),
                    distanceUnit: dijit.byId("consumerProfileDistanceUnit").attr("value"),
                    expirationDelay: dojo.byId("consumerRequestExpirationDelay").value,
                    keywords: dijit.byId("consumerAllProductsSearchCriteria").attr("value"),
                    postalCode: dijit.byId("consumerProfilePostalCode").attr("value")
                },
                function() {
                    alert("Your request has been saved. Switch to the â€œYour Requestsâ€? tab to update it.");
                }
            );
        };

        /**
         * Create and update product table
         * @param {Array} productList array of JSON objects, each one describing a product
         */
        var _updateConsumerProductTable = function(productList){
            var grid = dijit.byId("consumerProductTable");
            var store = new dojo.data.ItemFileReadStore({
                data : {
                    identifier: "key",
                    items: productList == null ? [] : productList
                }
            });
            store.fetch( {
                onComplete : function(items, request) {
                    grid.setStore(store);
                },
                onError : function(errorData, request) {
                    alert("other troubles"); // FIXME
                }
            });
        };

        /**
         * Create and update the request table. The pending request list is retreived
         * dynamically from the server.
         */
        module.searchConsumerRequests = function(){
            var grid = dijit.byId("consumerRequestTable");
            module.Request.select({
                    scope: dijit.byId("consumerRequestsSelector").attr("value")
                },
                function(response) {
                    var store = new dojo.data.ItemFileWriteStore({
                        data : {
                            identifier: "key",
                            items: response == null ? [] : response
                        }
                    });
                    store.fetch( {
                        onComplete : function(items, request) {
                            grid.setStore(store);
                        },
                        onError : function(errorData, request) {
                            alert("other troubles") // FIXME
                        }
                    });
                },
                function() { alert("Error during xhrGet(/API/requests)"); }
            );
        };

        /**
         * Menu handler deleting the request rendered in the row where the contextual menu has been opened
         */
        module.deleteConsumerRequest = function() {
            var grid = dijit.byId("consumerRequestTable");
            var item = grid.getItem(rowIndex);
            var store = grid.store;
            store.deleteItem(item);
            store._saveCustom = function(saveSuccessCallbback, saveFailureCallback) {
                module.Request.remove(item.key, saveSuccessCallbback, saveFailureCallback);
            };
            store.save({
                onComplete : function() {
                    // Nothing
                },
                onError : function(errorData, request) {
                    alert("Request deletion failed!");
                }
            });
        };

        module.searchRetailerRequests = function() {
            var criteriaField = dijit.byId("retailerAllRequestCriteria");
            if (!criteriaField.isValid()) {
                return;
            }
            // TODO: if the retailer is authenticated, use information attached to the store
            var postalCodeField = dijit.byId("consumerProfilePostalCode");
            if (!postalCodeField.isValid()) {
                return;
            }
            var grid = dijit.byId("retailerRequestTable");
            module.Request.select({
                    country: dijit.byId("consumerProfileCountry").attr("value"),
                    distance: dijit.byId("consumerProfileDistance").attr("value"),
                    distanceUnit: dijit.byId("consumerProfileDistanceUnit").attr("value"),
                    keywords: criteriaField.attr("value"),
                    postalCode: postalCodeField.attr("value")
                },
                function(response) {
                    var store = new dojo.data.ItemFileWriteStore({
                        data : {
                            identifier: "key",
                            items: response == null ? [] : response
                        }
                    });
                    store.fetch( {
                        onComplete : function(items, request) {
                            grid.setStore(store);
                        },
                        onError : function(errorData, request) {
                            alert("other troubles") // FIXME
                        }
                    });
                },
                function() { alert("Error during xhrGet(/API/requests)"); }
            );
        };

    })(); // End of the function limiting the scope of the private variables
}
