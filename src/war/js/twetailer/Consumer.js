(function() { // To limit the scope of the private variables

    var module = dojo.provide('twetailer.Consumer');

    dojo.require('twetailer.Common');

    module._labelExtractor = null; // Made accessible only for test purposes
    var _common = twetailer.Common,
        _masterBundleName = 'master',
        _consoleBundleName = 'console',
        _getLabel,
        _consumer,
        _queryPointOfView = _common.POINT_OF_VIEWS.CONSUMER;

    /**
     * Module initializer
     *
     * @param {String} ISO locale identifier.
     */
    module.init = function(locale) {
        // Get the localized resource bundle
        domderrien.i18n.LabelExtractor.init('twetailer', _masterBundleName, locale);
        module._labelExtractor = domderrien.i18n.LabelExtractor.init('twetailer', _consoleBundleName, locale);
        _getLabel = module._labelExtractor.getFrom;

        // Get debug parameters
        // var requestParameters = dojo.queryToObject(window.location.search.slice(1));
        // _debugMode = requestParameters["debugMode"];

        // TODO:
        // - hide the sale associate link if the consumer is not a sale associate
    };

    /**
     * Error message handler
     *
     * @param {String} message As reported by the JavaScript or the Dojo system.
     * @param {Object} XHR object as composed to send a request server-side.
     *
     * @private
     */
    module._reportClientError = function(message, ioArgs) {
        alert(_getLabel(_consoleBundleName, 'error_client_side_communication_failed'));
        // dojo.analytics.addData("ClientError", "[" + message + "][" + ioArgs.url + "]");
    };

    /**
     * Error message handler
     *
     * @param {Object} response As reported by the server-side business logic.
     * @param {Object} XHR object as composed to send a request server-side.
     *
     * @private
     */
    module._reportServerError = function(response, ioArgs) {
        var message = 'Unexpected failure while communicating over the URL: ' + ioArgs.url + '.\n';
        if (response) {
            if (response.isException === true) {
                message = response.exceptionMessage; // Replace the local message by the one generated server-side
            }
            else if (response.status) {
                switch (response.status) {
                case 404: message += 'Address for this request no more available;'; break;
                case 500: message += 'Web service not accessible. Failure might be due to a misconfiguration'; break;
                default: message += 'Http Error Code: ' + response.status;
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
     * @param {String} topic Identifier for the third-party channel.
     * @param {String} fieldId Identifier of the field containing the user name for the third-party provider.
     */
    module.verifyConsumerCode = function(topic, fieldId) {
        // Prepare the code field
        var codeField = dijit.byId(fieldId + 'Code');
        var codeNode = codeField.domNode;
        var waitForCode = dojo.style(codeNode, 'display') == 'none';
        // Check for the update
        var field = dijit.byId(fieldId);
        var value = dojo.trim(field.attr('value'));
        if (value == '' && !_consumer[topic] || value == _consumer[topic]) {
            if (!waitForCode) {
                dojo.style(codeNode, 'display', 'none');
            }
            return;
        }
        if (value == '') {
            module.resetConsumerCode(topic, fieldId);
            return;
        }
        dojo.style(codeNode, 'display', '');
        // Prepare the data to be sent
        var data = dojo.formToObject('consumerInformation');
        data.topic = topic;
        data.waitForCode = waitForCode;
        data.emailCode = isNaN(parseInt(data.emailCode)) ? 0 : parseInt(data.emailCode);
        data.jabberIdCode = isNaN(parseInt(data.jabberIdCode)) ? 0 : parseInt(data.jabberIdCode);
        data.twitterIdCode = isNaN(parseInt(data.twitterIdCode)) ? 0 : parseInt(data.twitterIdCode);
        // Send the request
        dojo.xhrPost({
            headers: { 'content-type': 'application/json' },
            postData: dojo.toJson(data),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response && response.success) {
                    if (response.codeValidity === true) {
                        dijit.byId(fieldId + 'VerifyButton').attr('disabled', true);
                        var handle = dojo.connect(
                                field,
                                'onKeyPress',
                                function() {
                                    dijit.byId(fieldId + 'Button').attr('disabled', false);
                                    dojo.disconnect(handle);
                                }
                        );
                    }
                    else {
                        var mechanism = _getLabel(_consoleBundleName, 'consumer_info_verification_alert_' + topic);
                        var message = _getLabel(_consoleBundleName, 'consumer_info_verification_alert', [mechanism, value]);
                        alert(message);
                    }
                }
                else {
                    module._reportServerError(response, ioArgs);
                }
            },
            error: module._reportClientError,
            url: '/API/maelzel/processVerificationCode'
        });
    };

    module.resetConsumerCode = function(topic, fieldId) {
        // Reset the value
        var field = dijit.byId(fieldId);
        field.attr('value', '');
        // Prepare the code field
        var codeField = dijit.byId(fieldId + 'Code');
        if (!_consumer[topic]) {
            codeField.attr('value', '');
        }
        else {
            codeField.attr('value', 9999999999);
        }
        dojo.style(codeField.domNode, 'display', 'none');
        // Block the Verify & Reset buttons
        dijit.byId(fieldId + 'VerifyButton').attr('disabled', true);
        dijit.byId(fieldId + 'ResetButton').attr('disabled', true);
    };

    module.controlVerifyButtonState = function(topic, fieldId) {
        // Enable the Verify button only if the value if different from the current one
        dijit.byId(fieldId + 'VerifyButton').attr('disabled', dijit.byId(fieldId).attr('value') == _consumer[topic]);
        dijit.byId(fieldId + 'Code').attr('value', null);
    };

    module.updateConsumerInformation = function() {
        // Prepare the data to be sent
        var data = dojo.formToObject('consumerInformation');
        data.emailCode = isNaN(parseInt(data.emailCode)) ? 0 : parseInt(data.emailCode);
        data.jabberIdCode = isNaN(parseInt(data.jabberIdCode)) ? 0 : parseInt(data.jabberIdCode);
        data.twitterIdCode = isNaN(parseInt(data.twitterIdCode)) ? 0 : parseInt(data.twitterIdCode);
        // Send the request
        dojo.xhrPut({
            headers: { 'content-type': 'application/json' },
            putData: dojo.toJson(data),
            handleAs: 'json',
            load: function(response, ioArgs) {
                var form = document.forms['consumerInformation'];
                if (response && response.success) {
                    // Replace the _consumer instance with the updated copy
                    _consumer = response.resource;
                }
                _setFormElementValue(form, _consumer);
            },
            error: module._reportClientError,
            url: '/API/Consumer/' + _consumer.key
        });
    };

    var _setFormElementValue = function(form, object) {
        for (var attr in object) {
            var field = form.elements[attr];
            if (field) {
                // field.value = response.resource[attr];
                var widget = dijit.getEnclosingWidget(field);
                widget.attr('value', object[attr]);
                var id = widget.attr('id');
                var button = dijit.byId(id + 'VerifyButton');
                if (button) {
                    button.attr('disabled', true);
                }
                var code = dijit.byId(id + 'Code');
                if (code) {
                    dojo.style(code.domNode, 'display', 'none');
                    code.attr('value', null);
                }
            }
        }
    }

})(); // End of the function limiting the scope of the private variables
