(function() { // To limit the scope of the private variables

    var module = dojo.provide("twetailer.Common");

    dojo.require("domderrien.i18n.LabelExtractor");

    /* Set of local variables */
    var _getLabel,
        _locale,
        _supportGeolocation,
        _geoCoder,
        _postalCode,
        _countryCode,
        _geoCache = {},
        _lastBrowserLocation,
        _browserLocationOverlayId,
        _getPostalCountryEventName,
        _friendRowNb = 1;

    /**
     * List of possible command states (command being a Demand or a Proposal).
     */
    module.STATES = {
        OPENED: "opened",
        INVALID: "invalid",
        PUBLISHED: "published",
        CONFIRMED: "confirmed",
        CLOSED: "closed",
        DECLINED: "declined",
        CANCELLED: "cancelled",
        DELETED: "markedForDeletion"
    }

    /**
     * List of operation point of view
     */
    module.POINT_OF_VIEWS = {
        CONSUMER: "CONSUMER",
        SALE_ASSOCIATE: "SALE_ASSOCIATE",
        ANONYMOUS: "ANONYMOUS"
    }

    /**
     * Initializer
     *
     * @param {String} locale Identifier of the chosen locale
     * @param {String} getGeoButtonId Identifier of the button allowing to query the browser location
     * @return {Function} Shortcut on the local function getting localized labels
     */
    module.init = function(locale, getGeoButtonId) {
        _locale = locale;

        // Get the localized resource bundle
        domderrien.i18n.LabelExtractor.init("twetailer", "master", locale);
        domderrien.i18n.LabelExtractor.init("twetailer", "console", locale);
        _getLabel = domderrien.i18n.LabelExtractor.getFrom;

        _supportGeoLocation = navigator.geolocation;
        var getCoordinatesButton = dijit.byId(getGeoButtonId);
        if (getCoordinatesButton != null) {
            getCoordinatesButton.set("disabled", !_supportGeoLocation);
        }

        return _getLabel;
    }

    /**
     * Helper to generate an ISO formatted date without the timezome
     *
     * @param {Date} date date part to be formatted
     * @param {Date} timee time part to be formatted
     * @return ISO representation of the given date
     */
    module.toISOString = function(date, time) {
        // return dojo.date.stamp.toISOString(date, {}); // Contains the timezone gap
        var month = date.getMonth() + 1;
        var day = date.getDate();
        var hours = time == null ? 0 : time.getHours();
        var minutes = time == null ? 0 : time.getMinutes();
        return date.getFullYear() +
              (month < 10 ? "-0" : "-") + month +
              (day < 10 ? "-0" : "-") + day +
              (hours < 10 ? "T0" : "T") + hours +
              (minutes < 10 ? ":0" : ":") + minutes +
              ":00";
    }

    /**
     * Helper verifying the HTTP status code and acting accordingly.
     * Worse case, the given message is displayed with the request URL.
     *
     * @param {Object} message
     * @param {Object} ioArgs
     */
    module.handleError = function(message, ioArgs) {
        if (ioArgs.xhr.status == 403) { // 403 == Forbidden
            _getLabel("console", "error_user_access_forbidden");
            window.location = "./";
            return;
        }
        if (ioArgs.xhr.status == 401) { // 401 == Unauthorized
            alert(_getLabel("console", "error_client_not_authenticated"));
            window.location = "/logout?fromPageURL=" + window.location;
            return;
        }
        if (ioArgs.xhr.status == 500) { // 500 == Server error
            alert(_getLabel("console", "error_client_side_communication_failed"));
            return;
        }
        alert(message+"\nurl: "+ioArgs.url);
    }

    var _previouslySelectedCountryCode = null;

    /**
     * Helper modifying on the fly the constraints for the postal code field
     *
     * @param {String} countryCode New country code
     * @param {String} postalCodeFieldId Identifier of the postal code field
     */
    module.updatePostalCodeFieldConstraints = function(countryCode, postalCodeFieldId) {
        if (_previouslySelectedCountryCode != countryCode) {
            var pcField = dijit.byId(postalCodeFieldId);
            if (pcField != null) {
                pcField.set("regExp", _getLabel("console", "location_postalCode_regExp_" + countryCode));
                pcField.set("placeHolder", _getLabel("console", "location_postalCode_placeHolder_" + countryCode));
                pcField.set("invalidMessage", _getLabel("console", "location_postalCode_invalid_" + countryCode));
                pcField.focus();
            }
            _previouslySelectedCountryCode = countryCode;
        }
    }

    /**
     * Invoke a third-party service (Google Maps) to get the geographical coordinate of the given location
     * and render the corresponding map in a dialog box with id "locationMapDialog"
     *
     * @param {String} postalCode postal code in "A1A1A1" for Canada, and "12345" for USA
     * @param {String} countryCode ISO of the country
     */
    module.showMap = function(postalCode, countryCode) {
        _postalCode = postalCode;
        _countryCode = countryCode;

        var cachedValue = _geoCache[_postalCode + "-" + countryCode];
        if (cachedValue != null) {
            _placeMap(cachedValue);
            return;
        }

        var countryShortLabel = _countryCode == "CA" ? "Cananda" : "USA";
        var geoCoderParameters = {
            language: _locale,
            address: _postalCode + "," + countryShortLabel,
            region: countryCode
        };
        if (_geoCoder == null) {
            _geoCoder = new google.maps.Geocoder();
        }
        _geoCoder.geocode(geoCoderParameters, _getGeoCoordinatesCallback);
    };

    /**
     * Callback dispatching the resolved {postal code, country code} or displaying an error message
     *
     * @param {Array} results Array of possible locations matching the given {postal code, country code}
     * @param {Number} status Status of the lookup operation
     *
     * @see Common#_placeMap(Object)
     */
    var _getGeoCoordinatesCallback = function(results, status) {
        if (google.maps.GeocoderStatus.OK == status) {
            var location = results[0].geometry.location;
            _geoCache[_postalCode + "-" + _countryCode] = location;
            _placeMap(location);
        }
        else {
            alert(_getLabel("console", "shared_invalid_locale_message", [ _postalCode, _countryCode ]));
        }
    };

    /**
     * Helper displaying a map (from Google) centered on the specified location
     *
     * @param {Object} location set of gographical coordinates
     *
     * @see Common#_getGeoCoordinatesCallback(Array, Number)
     */
    var _placeMap = function(location) {
        // Dialog should be displayed first for the map to appear correctly!
        dijit.byId('locationMapDialog').show();

        // Creating a map
        var map = new google.maps.Map(
            dojo.byId("mapPlaceHolder"), {
                center: location,
                language: _locale,
                mapTypeId: google.maps.MapTypeId.ROADMAP,
                zoom: 10
            }
        );

        // Image shadow made with: http://www.cycloloco.com/shadowmaker/shadowmaker.htm
        // Google Maps API for overlays: http://code.google.com/apis/maps/documentation/v3/overlays.html

        var image = new google.maps.MarkerImage(
            "/images/logo/marker9.png",
            new google.maps.Size(38,73),
            new google.maps.Point(0,0),
            new google.maps.Point(28,70)
        );
        var shadow = new google.maps.MarkerImage(
            "/images/logo/marker9-shadow.png",
            new google.maps.Size(75,73),
            new google.maps.Point(0,0),
            new google.maps.Point(20, 70)
        );
        // Creating a marker and positioning it on the map
        var marker = new google.maps.Marker({
            clickable: false,
            icon: image,
            shadow: shadow,
            map: map,
            position: location,
            title: _postalCode + " " + _countryCode
        });
    };

    /**
     * Utility method getting the geo-coordinates from the browser and then the postal and country code
     * for the corresponding geo-position in order to notify the listeners waiting on the specified
     * event the corresponding codes
     *
     * @param {String} eventName Name of the notification
     * @param {String} overlayId (Optional) Identifier of the overlay to show while the asynchronous operation is in progress
     *
     * @see Common#_successCallbackBrowserLocation(Array, Number)
     * @see Common#_errorCallbackBrowserLocation(Object)
     */
    module.getBrowserLocation = function(eventName, overlayId) {
        _browserLocationOverlayId = overlayId;
        _getPostalCountryEventName = eventName;

        if (_browserLocationOverlayId != null) {
            dijit.byId(_browserLocationOverlayId).show();
        }

        /* // Will trigger one immediate lookup in the browser cache, and if not cached the error handler will trigger a remote call => step not necessary
        navigator.geolocation.getCurrentPosition(
            _successCallbackBrowserLocation,
            _errorCallbackBrowserLocation,
            {
                maximumAge: Infinity,
                timeout: 0
            }
        );
        */
        navigator.geolocation.getCurrentPosition(_successCallbackBrowserLocation, _errorCallbackBrowserLocation);
    }

    /**
     * Helper calling a third party service (Google) to get the postal and country codes for a
     * the geo-position reported by the browser (HTML5 compliant browser)
     *
     * @param {Object} position Set of coordinates reported by the browser
     *
     * @see Common#_getPostalCountryCodesCallback(Array, Number)
     */
    var _successCallbackBrowserLocation = function(position) {
        var coords = position.coords; // coords: {latitude, longitude, altitude, accuracy, altitudeAccuracy, heading, speed}
        if (_lastBrowserLocation != null && _lastBrowserLocation.latitude == coords.latitude && _lastBrowserLocation.longitude == coords.longitude) {
            _notifyBrowserLocation(_lastBrowserLocation.postalCode, _lastBrowserLocation.countryCode);
        }
        else {
            var geoCoderParameters = {
                language: _locale,
                latLng: new google.maps.LatLng(coords.latitude, coords.longitude)
            };
            if (_geoCoder == null) {
                _geoCoder = new google.maps.Geocoder();
            }
            _lastBrowserLocation = {
                latitude: coords.latitude,
                longitude: coords.longitude
            };
            _geoCoder.geocode(geoCoderParameters, _getPostalCountryCodesCallback);
        }
    }

    /**
     * Helper reporting missing cached value and invoking a third party service or reporting a raw error
     *
     * @param {Object} error Error record
     */
    var _errorCallbackBrowserLocation = function(error) {
        // TODO: put the messages in the console.tmx
        switch (error.code) {
            case 3: // error.TIMEOUT:
                /* // Will trigger a remote call when the immediate browser cache lookup failed => not necessary
                navigator.geolocation.getCurrentPosition(_successCallbackBrowserLocation, _errorCallbackBrowserLocation);
                break;
                */
            case 2: // error.POSITION_UNAVAILABLE:
            case 1: // error.PERMISSION_DENIED:
            default: // error.UNKNOWN_ERROR == 0
                alert(_getLabel("console", "shared_cannot_get_geocoordinates_message"));
                _notifyBrowserLocation();
        }
    }

    /**
     * Helper parsing the returned address list to extract a postal code and a country code
     *
     * @param {Array} results Array of possible locations matching the given {postal code, country code}
     * @param {Number} status Status of the lookup operation
     *
     * @see Common#_getGeoCoordinatesCallback(Array, Number)
     */
    var _getPostalCountryCodesCallback = function(results, status) {
        if (google.maps.GeocoderStatus.OK == status) {
            var pC = null, cC = null, idx = results.length, jdx, COUNTRY_TAG = "country", POSTAL_CODE_TAG = "postal_code";
            while (0 < idx) {
                idx --;
                var parts = results[idx].address_components;
                jdx = parts.length;
                while (0 < jdx) {
                    jdx --;
                    var part = parts[jdx];
                    if (cC == null && part.types[0] == COUNTRY_TAG) {
                        cC = part.short_name;
                    }
                    else if (pC == null && part.types[0] == POSTAL_CODE_TAG && 3 < part.short_name.length) {
                        pC = part.short_name;
                    }
                    if (pC && cC) { break; }
                }
                if (pC && cC) { break; }
            }
            if (pC && cC) {
                var location = results[0].geometry.location;
                _lastBrowserLocation.postalCode = pC;
                _lastBrowserLocation.countryCode = cC;
               _notifyBrowserLocation(pC, cC);
               return;
            }
            alert(_getLabel("console", "shared_cannot_resolve_geocoordinates_message"));
            _lastBrowserLocation = null;
            _notifyBrowserLocation();
            return;
        }
        alert(_getLabel("console", "shared_invalid_locale_message", [_postalCode, _countryCode]));
        _lastBrowserLocation = null;
        _notifyBrowserLocation();
    }

    /**
     * Helper notifying listeners about the set of codes for the browser location
     *
     * @param {Object} postalCode
     * @param {Object} countryCode
     */
    var _notifyBrowserLocation = function(postalCode, countryCode) {
        dojo.publish(_getPostalCountryEventName, [postalCode, countryCode]);

        if (_browserLocationOverlayId != null) {
            dijit.byId(_browserLocationOverlayId).hide();
        }
    }

    /**
     * Handles a click on a [+] or [-] button placed near an &lt;input/&gt; which is going to receive
     * the coordinate of someone to be cc'ed for the corresponding demand.
     *
     * @param {Number} rowIdx index of the &lt;input/&gt; field (starts at 1)
     * @param {String} fieldLabel label of the field to be inserted in a cell before the &lt;input/&gt; field
     *
     * The HTML code should define the following tags. Note that the ellipsis (...)
     * replace labels or values that depend on the final implementation details.<pre>
     *   &lt;table style="..."&gt;
     *       &lt;tbody id="friendList"&gt;         // All new rows are appended as child to this element
     *           &lt;tr&gt;
     *               &lt;td&gt;                    // Optional raw with the label to be receive the &lt;input/&gt; label
     *                   &lt;label for="email1"&gt;
     *                       ...
     *                   &lt;/label&gt;
     *               &lt;/td&gt;
     *               &lt;td&gt;
     *                   &lt;input
     *                       dojoType="dijit.form.ValidationTextBox"
     *                       id="email1"           // This identifier allows to use this widget as the master copy to others
     *                       invalidMessage="..."  // Value copied in cloned rows
     *                       name="email1"         // This identifier is used when the form is submitted
     *                       placeHolder="..."     // Value copied in cloned rows
     *                       regExp="..."          // Value copied in cloned rows
     *                       required="false"
     *                       style="width:100%;"
     *                       trim="true"
     *                       type="text"
     *                 /&gt;
     *               &lt;/td&gt;
     *               &lt;td style="width:20px;padding-right:0px !important;"&gt;
     *                   &lt;button
     *                       dojoType="dijit.form.Button"
     *                       iconClass="silkIcon silkIconAdd"
     *                       id="friendButton1"    // This value is used to retrieve the button and to change its meaning if it's the last visible one
     *                       onclick="twetailer.Common.manageFriendRow(1);"
     *                       showLabel="false"
     *                       title="..."           // Value copied in cloned rows
     *                   &gt;&lt;/button&gt;
     *               &lt;/td&gt;
     *           &lt;/tr&gt;
     *       &lt;/tbody&gt;
     *   &lt;/table&gt;</pre>
     *
     */
    module.manageFriendRow = function(rowIdx, fieldLabel) {
        if (_friendRowNb == 1 && dijit.byId("email1") == null) {
            alert ("The HTML is misconfigured! Please check the documentation to verify the requirements.");
            return;
        }
        if (rowIdx == _friendRowNb) {
            // Add a row with a new <input/> field
            var button = dijit.byId("friendButton" + rowIdx);
            button.set("iconClass", "silkIcon silkIconDelete");
            button.set("title", _getLabel("console", "ga_demandForm_removeCCButtonLabel"));
            _friendRowNb ++;
            var fIdx = _friendRowNb;
            var firstField = dijit.byId('email1');
            var row = dojo.create("tr", { id: "friendRow" + fIdx }, dojo.byId("friendList"));
            if (fieldLabel != null) {
                dojo.create("label", { forAttr: "email" + fIdx, innerHTML: fieldLabel }, dojo.create("td", null, row));
            }
            dojo.create("td", null, row).appendChild(
                new dijit.form.ValidationTextBox({
                    id: "email" + fIdx,
                    invalidMessage: firstField.get("invalidMessage"),
                    name: "email" + fIdx,
                    placeHolder: firstField.get("placeHolder"),
                    required: false,
                    style: "width:100%",
                    trim: true }).domNode
            );
            dojo.create("td", { style: "width:20px;padding-right:0px !important;" }, row).appendChild(
                new dijit.form.Button({
                    iconClass: "silkIcon silkIconAdd",
                    id: "friendButton" + fIdx,
                    onClick: function() { module.manageFriendRow(fIdx, fieldLabel); },
                    showLabel: false,
                    title: firstField.get("title") }).domNode
            );
            dijit.byId("email" + fIdx).set("regExp", dijit.byId('email1').get("regExp")); // Work-around: otherwise, it seems the regExp value is misinterpreted!
        }
        else {
            // Remove the identified row
            var fIdx = _friendRowNb;
            for (var i=rowIdx; i < fIdx; i++) {
                dijit.byId("email" + i).set("value", dijit.byId("email" + (i + 1)).get("value"));
            }
            dijit.byId("email" + fIdx).destroy();
            dijit.byId("friendButton" + fIdx).destroy();
            dojo.destroy("friendRow" + fIdx);
            _friendRowNb --;
            fIdx --;
            var button = dijit.byId("friendButton" + fIdx);
            button.set("iconClass", "silkIcon silkIconAdd");
            button.set("title", _getLabel("console", "ga_demandForm_addCCButtonLabel"));
        }
    }

    /**
     * Return the Dijit field for the identified CC'ed friend field.
     * If the field does not yet exist, it will be created by simulating
     * user action on the [+] button up to have the corresponding field.
     *
     * @param {Number} rowIdx index of the &lt;input/&gt; field (starts at 1)
     * @param {String} fieldLabel label of the field to be inserted in a cell before the &lt;input/&gt; field
     * @return {Object} Dijit field instance
     *
     * @see twetailer.Console#manageFriendRow(Number, String)
     */
    module.getFriendInputField = function(rowIdx, fieldLabel){
        var field = dijit.byId("email" + rowIdx);
        if (field == null) {
            console.log("byId(rowIdx) == null")
            var i = _friendRowNb, limit =  rowIdx;
            while (i < limit) {
                console.log(i)
                module.manageFriendRow(i);
                i ++;
            }
            field = dijit.byId("email" + rowIdx);
        }
        return field;
    }

    /**
     * Scan the friend fields to stack their values in an array
     *
     * @return {Array} Documented friend coordinates
     *
     * @see twetailer.Console#manageFriendRow(Number, String)
     */
    module.getFriendCoordinates = function(){
        var coordinates = [], idx = 0;
        while (idx < _friendRowNb) {
            idx ++; // Increment now because the field index starts at 1
            var field = dijit.byId("email" + idx);
            if (field != null) {
                var coordinate = field.get("value");
                if (0 < coordinate.length) {
                    coordinates.push(coordinate);
                }
            }
        }
        return coordinates;
    }
})(); // End of the function limiting the scope of the private variables
