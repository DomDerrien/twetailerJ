(function() { // To limit the scope of the private variables

    var module = dojo.provide("twetailer.Console");

    dojo.require("domderrien.i18n.LabelExtractor");

    module._labelExtractor = null; // Made accessible only for test purposes
    var  _masterBundleName = "master",
        _consoleBundleName = "console",
        _getLabel,
        _consumer;

    /**
     * Module initializer
     *
     * @param {String} ISO locale identifier
     * @param {Boolean} Indicator of the login page
     */
    module.init = function(locale, isLoginPage) {
        // Get the localized resource bundle
        domderrien.i18n.LabelExtractor.init("twetailer", _masterBundleName, locale);
        module._labelExtractor = domderrien.i18n.LabelExtractor.init("twetailer", _consoleBundleName, locale);
        _getLabel = module._labelExtractor.getFrom;

        // TODO:
        // - hide the sale associate link if the consumer is not a sale associate
    };

    /**
     * Error message handler
     *
     * @param {String} message As reported by the JavaScript or the Dojo system
     * @param {Object} XHR object as composed to send a request server-side
     *
     * @private
     */
    module._reportClientError = function(message, ioArgs) {
        alert(_getLabel("console", "error_client_side_communication_failed"));
        // alert(message + "\nurl: " + ioArgs.url);
        // dojo.analytics.addData("ClientError", "[" + message + "][" + ioArgs.url + "]");
    };

    /**
     * Error message handler
     *
     * @param {Object} response As reported by the server-side business logic
     * @param {Object} XHR object as composed to send a request server-side
     *
     * @private
     */
    module._reportServerError = function(response, ioArgs) {
        message = "Unexpected failure while communicating over the URL: " + ioArgs.url + ".\n";
        if (response != null) {
            if (response.isException === true) {
                message += response.message;
            }
            else if (response.status !== null) {
                switch(response.status) {
                case 404: message += "Address for this request no more available;"; break;
                case 500: message += "Web service not accessible. Failure might be due to a misconfiguration"; break;
                default: message += "Http Error Code: " + response.status;
                }
            }
        }
        alert(message);
    };

    /**
     * Hook for the server-code (induced by the JSP processor) to store the logged user information
     */
    module.registerConsumer = function(json) {
        _consumer = json;
    };

    /**
     * Invoke the server to 1) initiate a verification sending via the specified channel
     * and 2) to validate the code if the user has specified any.
     *
     * @param {String} topic Identifier for the third-party channel
     * @param {String} fieldId Identifier of the field containing the user name for the third-party provider
     */
    module.verifyConsumerCode = function(topic, fieldId) {
        // Check for the update
        var field = dijit.byId(fieldId);
        var value = dojo.trim(field.attr("value"));
        if (value == "" && _consumer[topic] == null || value == _consumer[topic]) {
            return;
        }
        // Prepare the code field
        var codeField = dijit.byId(fieldId + "Code");
        var codeNode = codeField.domNode;
        var waitForCode = dojo.style(codeNode, "display") == "none";
        dojo.style(codeNode, "display", "");
        // Prepare the data to be sent
        var data = dojo.formToObject("consumerInformation");
        data.topic = topic;
        data.waitForCode = waitForCode;
        data.emailCode = isNaN(parseInt(data.emailCode)) ? 0 : parseInt(data.emailCode);
        data.jabberIdCode = isNaN(parseInt(data.jabberIdCode)) ? 0 : parseInt(data.jabberIdCode);
        data.twitterIdCode = isNaN(parseInt(data.twitterIdCode)) ? 0 : parseInt(data.twitterIdCode);
        // Send the request
        dojo.xhrPost({
            headers: { "content-type": "application/json" },
            postData: dojo.toJson(data),
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    if (response.codeValidity === true) {
                        dijit.byId(fieldId + "Button").attr("disabled", true);
                        var handle = dojo.connect(
                                field,
                                "onKeyPress",
                                function() {
                                    dijit.byId(fieldId + "Button").attr("disabled", false);
                                    dojo.disconnect(handle);
                                }
                        );
                    }
                    else {
                        var mechanism = _getLabel(_consoleBundleName, "consumer_info_verification_alert_" + topic);
                        var message = _getLabel(_consoleBundleName, "consumer_info_verification_alert", [mechanism, value]);
                        alert(message);
                    }
                }
                else {
                    module._reportServerError(response, ioArgs);
                }
            },
            error: module._reportClientError,
            url: "/API/maezel/processVerificationCode"
        });
    };

    module.updateConsumerInformation = function() {
        // Prepare the data to be sent
        var data = dojo.formToObject("consumerInformation");
        data.emailCode = isNaN(parseInt(data.emailCode)) ? 0 : parseInt(data.emailCode);
        data.jabberIdCode = isNaN(parseInt(data.jabberIdCode)) ? 0 : parseInt(data.jabberIdCode);
        data.twitterIdCode = isNaN(parseInt(data.twitterIdCode)) ? 0 : parseInt(data.twitterIdCode);
        // Send the request
        dojo.xhrPut({
            headers: { "content-type": "application/json" },
            putData: dojo.toJson(data),
            handleAs: "json",
            load: function(response, ioArgs) {
                alert(response.success);
            },
            error: module._reportClientError,
            url: "/API/Consumer/" + _consumer.key
        });
    };

})(); // End of the function limiting the scope of the private variables
