(function() { // To limit the scope of the private variables

    var module = dojo.provide("twetailer.Directory");

    dojo.require("twetailer.Common");

    /* Set of local variables */
    var _common,
        _locale,
        _getLabel,
        _dateInOneMonth,
        _cityName,
        _postalCode,
        _countryCode;

    /**
     * Module initializer
     *
     * @param {String} ISO locale identifier
     */
    module.init = function(locale, cityName, postalCode, countryCode) {
        _locale = locale;

        // Get the localized resource bundle
        _common = twetailer.Common;
        _getLabel = _common.init(locale);

        // Put the focus in the Demand field
        dijit.byId("completeDemand").focus();

        _dateInOneMonth = new Date();
        _dateInOneMonth.setMonth(_dateInOneMonth.getMonth() + 1);
        dijit.byId("expiration").attr("value", _dateInOneMonth);
        _dateInOneMonth = dijit.byId("expiration").attr("value");

        _cityName = cityName;
        _postalCode = postalCode;
        _countryCode = countryCode;
        dijit.byId("postalCode").attr("value", postalCode);
        dijit.byId("countryCode").attr("value", countryCode);
    };

    var _getValidDemand = function(silent) {
        var demand = dijit.byId("completeDemand").attr("value");
        if (demand != null & 0 < demand.length) {
            demand = dojo.trim(demand);
        }
        if (demand != null & 0 < demand.length) {
            var postalCode = dijit.byId("postalCode").attr("value");
            var countryCode = dijit.byId("countryCode").attr("value");
            if (true) { // postalCode != _postalCode || countryCode != _countryCode) { // To be sure a valid postal code is always submitted
                demand += " " + _getLabel("master", "cl_prefix_locale").substring(0,3) + ":" + postalCode.replace(/\s/g, "") + " " + countryCode;
            }
            var range = dijit.byId("range").attr("value");
            var rangeUnit = dijit.byId("rangeUnit").attr("value");
            if (range != 10 && rangeUnit == "km") { // LocaleValidator.KILOMETER_UNIT
                demand += " " + _getLabel("master", "cl_prefix_range").substring(0,3) + ":" + range + rangeUnit;
            }
            if (range != 6 && rangeUnit == "mi") { // LocaleValidator.MILE_UNIT
                demand += " " + _getLabel("master", "cl_prefix_range").substring(0,3) + ":" + range + rangeUnit;
            }
            var expiration = dijit.byId("expiration").attr("value");
            if (expiration.getFullYear() != _dateInOneMonth.getFullYear() ||
                expiration.getMonth() != _dateInOneMonth.getMonth() ||
                expiration.getDate() != _dateInOneMonth.getDate()
            ) {
                demand += " " + _getLabel("master", "cl_prefix_expiration").substring(0,3) + ":" + expiration.getFullYear() + expiration.getMonth() + expiration.getDate();
            }
            var quantity = dijit.byId("quantity").attr("value");
            if (quantity != 1) {
                demand += " " + _getLabel("master", "cl_prefix_quantity").substring(0,3) + ":" + quantity;
            }
            return demand;
        }
        if (silent !== true) {
            alert(_getLabel("console", "sep_need_non_empty_demand"));
        }
        return null;
    };

    module.showAdvancedForm = function() {
        var source = dijit.byId("completeDemand");
        var demand = source.attr("value");
        if (demand != null & 0 < demand.length) {
            demand = dojo.trim(demand);
        }
        var target = dijit.byId("criteria");
        target.attr("value", demand == null ? "" : demand);
        target.focus();

        dijit.byId("advancedForm").show();
    };

    module.closeAdvancedForm = function() {
        var source = dijit.byId("criteria");
        var demand = source.attr("value");
        if (demand != null & 0 < demand.length) {
            demand = dojo.trim(demand);
        }
        var target = dijit.byId("completeDemand");
        target.attr("value", demand == null ? "" : demand);
        target.focus();
    };

    module.onRangeUnitChange = function() {
        var rangeUnitField = dijit.byId("rangeUnit");
        var unit = rangeUnitField.attr("value");
        var rangeField = dijit.byId("range");
        var range = rangeField.attr("value");
        var min = 5, max = 100;
        if (unit == "km") { // LocaleValidator.KILOMETER_UNIT
            range = range * 1.609344;
        }
        else { // LocaleValidator.MILE_UNIT
            min = 5 * 0.621371192;
            max = 100 * 0.621371192;
            range = range * 0.621371192;
        }
        var rangeSlider = dijit.byId("rangeSlider");
        rangeSlider.attr("minimun", min);
        rangeSlider.attr("maximum", max);
        rangeSlider.attr("value", range);
        rangeField.attr("constraints", {min:min,max:max,places:0});
    };

    module.postThruTwitter = function() {
        var demand = _getValidDemand();
        if (demand == null) {
            return;
        }
        demand = "d twetailer " + escape(demand).replace(/\%20/g, " ");
        if (demand.length < 140) {
            window.open("http://twitter.com/home/?status=" + demand, "twitter_window");
            return;
        }
        var processThruEMail = window.confirm(_getLabel("console", "sep_tweet_too_long_propose_email_alternative"));
        if (processThruEMail) {
            module.postThruEMail();
        }
    };

    module.postThruSMS = function() {
        var demand = _getValidDemand();
        if (demand == null) {
            return;
        }
        demand = "d twetailer " + demand;
        if (demand.length < 140) {
            dojo.byId("demand_repetition_for_sms").appendChild(document.createTextNode(demand));
            dijit.byId('postThruSMSInfo').show();
            return;
        }
        var processThruEMail = window.confirm(_getLabel("console", "sep_tweet_too_long_propose_email_alternative"));
        if (processThruEMail) {
            module.postThruEMail();
        }
    };

    module.postThruEMail = function() {
        var demand = _getValidDemand();
        if (demand == null) {
            return;
        }
        var subject = _getLabel("console", "sep_email_subject", [ _cityName ]);
        window.open("mailto:maezel@twetailer.appspotmail.com?subject" + subject + "&body=" + escape(demand), "email_window");
    };

    module.postThruGTalk = function() {
        var demand = _getValidDemand();
        if (demand == null) {
            return;
        }
        dojo.byId("demand_repetition_for_gtalk").appendChild(document.createTextNode(demand));
        dijit.byId('postThruGTalkInfo').show();
    };

    module.showMap = function() {
        _common.showMap(dijit.byId("postalCode").attr("value"), dijit.byId("countryCode").attr("value"));
    };
})(); // End of the function limiting the scope of the private variables
