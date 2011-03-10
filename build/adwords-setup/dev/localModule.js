var localModule = {};

(function() { // To limit the scope of the private variables

    var startDate = new Date(), reportHost = 'https://anothersocialeconomy.appspot.com/3rdParty/', reportDelay = 1000, reportId = "-", debugMode = false;

    dojo.require('dojo.io.script');
    dojo.require('dijit.Dialog'); // ** Use DialogSimple as soon as Dojo 1.6 is out
    dojo.require('dojo.data.ItemFileReadStore');
    dojo.require('dijit.form.Button');
    dojo.require('dijit.form.CheckBox');
    dojo.require('dijit.form.DateTextBox');
    dojo.require('dijit.form.FilteringSelect');
    dojo.require('dijit.form.Form');
    dojo.require('dijit.form.NumberSpinner');
    dojo.require('dijit.form.Textarea');
    dojo.require('dijit.form.TextBox');
    dojo.require('dijit.form.ValidationTextBox');
    dojo.require('dojox.analytics.Urchin');
    // ** dojo.require('dojox.widget.DialogSimple');

    dojo.addOnLoad(function() {
        var dueDate = new Date();
        dueDate.setMonth(dueDate.getMonth() + 1);

        var city = '${CITY}';
        var maker = '${MAKE}' == 'Cars' || '${MAKE}' == 'Automobile' ? 'BMW' : '${MAKE}';
        var model = '${MODEL}';
        var qualifier = "${PRINTED_QUALIFIER}"; // ** Use the double-quotes as delimiters because of D'occasion, for example
        var postalCode = '${POSTAL_CODE}';
        var info = '';

        if (window.location.search) {
            // ** Step 1: get the keywords
            var urlParams = dojo.queryToObject(window.location.search.slice(1));
            debugMode = urlParams.debugMode != null;
            reportHost = urlParams.host || reportHost;
            var skw = urlParams.kw;
            if (skw) {
                // ** Step 2: remove extra characters
                skw = skw.replace(/\"/g, '').replace(/\+/g, '');
                // ** Step 3: remove the duplicates (keep the first occurrence)
                skw = removeDups(skw, city, true);
                skw = removeDups(skw, qualifier, true);
                skw = removeDups(skw, maker, true);
                // ** Step 4: insert the cleaned up keywords in the page
                var field = dojo.byId('keywordAdTitle');
                field.innerHTML = '';
                field.appendChild(dojo.doc.createTextNode(skw));
                // ** Step 5: update the language switchers
                dojo.byId('switchLanguage1').href += '?kw=' + skw;
                dojo.byId('switchLanguage2').href += '?kw=' + skw;
                // ** Step 6: remove the last occurrence of the page keyword and place the remaining in the 'model' variable
                skw = removeDups(skw, city, false);
                skw = removeDups(skw, qualifier, false);
                skw = removeDups(skw, maker, false);
                // ** Step 7: remove the last occurrence of the page keyword and place the remaining in the 'model' variable
                skw = removeDups(skw, city.replace(/é/g, 'e'), false);
                skw = removeDups(skw, qualifier.replace(/é/g, 'e'), false);
                skw = removeDups(skw, maker.replace(/é/g, 'e'), false);
                model = dojo.trim(skw.replace(/\s+/g, ' ').replace(/\"/g, '').replace(/\+/g, ''));
            }
        }

        var options = {
            identifier : 'value',
            items : []
        }, items = options.items, idx, limit = makes.length;
        for (idx = 0; idx < limit; idx++) {
            items.push({
                value : makes[idx]
            });
        }

        var lB = localizedBundle;
        var makers = new dojo.data.ItemFileReadStore({ data : options });
        new dijit.form.Form({ id : 'requestForm' }, 'requestForm');
        new dijit.form.TextBox({ name : 'model', value : model, placeHolder : lB.modelFieldPlaceHolder, style : 'width:160px;' }, 'model');
        new dijit.form.NumberSpinner({ name : 'range', value : 25, constraints : { min : 5, max : 100 }, style : 'width:160px;' }, 'range');
        new dijit.form.ValidationTextBox({ name : 'postalCode', value : postalCode, placeHolder : lB.postalCodeFieldPlaceHolder, regExp : '[a-zA-Z][0-9][a-zA-Z] *[0-9][a-zA-Z][0-9]', required : true, trim : true, style : 'width:160px;' }, 'postalCode');
        new dijit.form.DateTextBox({ name : 'dueDate', value : dueDate, constraints : { datePattern : 'EEE d MMM yyyy' }, style : 'width:160px;', required : true }, 'dueDate');
        new dijit.form.FilteringSelect({ name : 'make', value : maker, searchAttr : 'value', store : makers, style : 'width:160px;' }, 'make');
        new dijit.form.Textarea({ name : 'info', value : info, style : 'min-height:80px;', style : 'width:100%; min-height: 80px;' }, 'info');
        new dijit.form.ValidationTextBox({ name : 'email', placeHolder : lB.emailFieldPlaceHolder, regExp : '[a-zA-Z0-9\.\_\%\-]+\@[a-zA-Z0-9\.\-]+\.[a-zA-Z]{2,4}', required : true, trim : true }, 'email').focus();
        new dijit.form.CheckBox({ name : 'demoMode' }, 'demoMode');
        if (!debugMode) {
            new dojox.analytics.Urchin({ acct : 'UA-11910037-5' });
            setTimeout(reportUsage, reportDelay);
        }
    });

    var reportUsage = function() {
        var data = getFormData();
        data.reportId = reportId;
        data.reportDelay = reportDelay;
        data.startDate = startDate;
        data.reportDate = new Date();
        data.referrer = document.referrer;
        data.location = "" + document.location;
        data.cookie = document.cookie;
        dojo.io.script.get({
            callbackParamName : 'callback',
            content : data,
            error : function(dataBack) { },
            load : function(dataBack) {
                if (dataBack && dataBack.success) {
                    reportId = dataBack.reportId;
                    reportDelay = dataBack.reportDelay || 5 * reportDelay;
                    setTimeout(reportUsage, reportDelay);
                }
            },
            url : reportHost + 'Report'
        });
    };

    var removeDups = function(source, pattern, keepFirst) {
        var idx = source.indexOf(pattern), length = pattern.length;
        while (idx != -1) {
            if (keepFirst) {
                keepFirst = false;
                idx += length;
            }
            else {
                source = source.substring(0, idx) + source.substring(idx + length);
            }
            idx = source.indexOf(pattern, idx);
        }
        return source;
    };

    var getFormData = function() {
        // ** Collect data
        var dataIn = {
            referralId : 0, // ** By ASE itself
            language : localizedBundle.locale,
            countryCode : 'CA',
            hashTags : [ 'cardealer' ],
            exceptions : []
        };
        if (dijit.byId('demoMode').get('value') == 'on') { try { dataIn.hashTags.push('demo'); } catch (ex) { dataIn.exceptions.push('hashTags - ' + ex); } }
        try { dataIn.email = document.getElementById('email').value; } catch (ex) { dataIn.exceptions.push('email - ' + ex); }
        try { dataIn.postalCode = document.getElementById('postalCode').value; } catch (ex) { dataIn.exceptions.push('postalCode - ' + ex); }
        try { dataIn.range = document.getElementById('range').value; } catch (ex) { dataIn.exceptions.push('range - ' + ex); }
        try { dataIn.dueDate = toISOString(dijit.byId('dueDate').get('value')); } catch (ex) { dataIn.exceptions.push('dueDate - ' + ex); }
        try { dataIn.content = 'Manufacturer: ' + document.getElementById('make').value; } catch (ex) { dataIn.exceptions.push('make - ' + ex); }
        try { dataIn.content += ' Model: ' + document.getElementById('model').value; } catch (ex) { dataIn.exceptions.push('model - ' + ex); }
        try { dataIn.content += ' Infos: ' + document.getElementById('info').value; } catch (ex) { dataIn.exceptions.push('info - ' + ex); }
        return dataIn;
    };

    localModule.showReviewPane = function() {
        var lB = localizedBundle;
        // ** Validate the input fields content
        if (!dijit.byId('requestForm').validate())
            return;
        // ** Create the dialog box
        var reviewPane = dijit.byId('reviewPane') || new dijit.Dialog({
            content :
                '<div class="dijitDialogPaneContentArea" id="profileForms">' +
                '<table class="reviewPaneTable"><tbody>' +
                '<tr><th>' + document.getElementById('emailFieldLabel').innerHTML + '</th><td id="emailReview"></td><td id="emailVerifStatus" rowspan="6"></td></tr>' +
                '<tr><th>' + document.getElementById('makeFieldLabel').innerHTML + '</th><td id="makeReview"></td></tr>' +
                '<tr><th>' + document.getElementById('modelFieldLabel').innerHTML + '</th><td id="modelReview"></td></tr>' +
                '<tr><th>' + document.getElementById('postalCodeFieldLabel').innerHTML + '</th><td id="postalCodeReview"></td></tr>' +
                '<tr><th>' + document.getElementById('rangeFieldLabel').innerHTML + '</th><td id="rangeReview"></td></tr>' +
                '<tr><th>' + document.getElementById('dueDateFieldLabel').innerHTML + '</th><td id="dueDateReview"></td></tr>' +
                '<tr><th>' + document.getElementById('infoFieldLabel').innerHTML + '</th><td id="infoReview" colspan="2"></td></tr>' +
                '</tbody></table>' +
                '</div>' +
                '<div class="dijitDialogPaneActionBar">' +
                '<button dojoType="dijit.form.Button" type="submit">' + document.getElementById('submitButton').innerHTML + '</button>' +
                '<div style="float:left;padding:4px 0;"><a href="javascript:dijit.byId(\'reviewPane\').hide();">' + lB.cancelButton + '</a></div>' +
                '</div>',
            execute: localModule.sendRequest,
            id : 'reviewPane',
            style : 'min-width:400px;max-width:800px;',
            title : "Request Review Step"
        });
        // ** Fill up the dialog box fields
        document.getElementById('emailReview').innerHTML = document.getElementById('email').value;
        document.getElementById('makeReview').innerHTML = document.getElementById('make').value;
        document.getElementById('modelReview').innerHTML = document.getElementById('model').value;
        document.getElementById('postalCodeReview').innerHTML = document.getElementById('postalCode').value;
        document.getElementById('rangeReview').innerHTML = document.getElementById('range').value + ' km';
        document.getElementById('dueDateReview').innerHTML = document.getElementById('dueDate').value;
        document.getElementById('infoReview').appendChild(document.createTextNode(document.getElementById('info').value));
        document.getElementById('emailVerifStatus').innerHTML = lB.emailCheckInitialMessage;
        // ** Display the dialog box
        reviewPane.show();
        // ** Call the server for an email check
        dojo.io.script.get({
            callbackParamName : 'callback',
            content : {
                referralId : 0, // ** By ASE itself
                hashTags : [ 'cardealer' ],
                reportId : reportId,
                email : document.getElementById('email').value
            },
            error : function(dataBack) {
                alert(lB.emailCheckErrorMessage);
            },
            load : function(dataBack) {
                if (dataBack && dataBack.success) {
                    if (dataBack.status) {
                        document.getElementById('emailVerifStatus').innerHTML = lB.emailCheckSuccessKnownMessage.replace('_name_', dataBack.name);
                    }
                    else {
                        var content = document.getElementById('makeFieldLabel').innerHTML + document.getElementById('make').value + ',';
                        content += document.getElementById('modelFieldLabel').innerHTML + document.getElementById('model').value + ',';
                        content += document.getElementById('infoFieldLabel').innerHTML + document.getElementById('info').value + '\n';
                        content += document.getElementById('postalCodeFieldLabel').innerHTML + document.getElementById('postalCode').value + ',';
                        content += document.getElementById('rangeFieldLabel').innerHTML + document.getElementById('range').value + ',';
                        content += document.getElementById('dueDateFieldLabel').innerHTML + document.getElementById('dueDate').value + ',';
                        document.getElementById('emailVerifStatus').innerHTML = lB.emailCheckSuccessUnknownMessage.replace('_content_', escape(content));
                    }
                }
                else {
                    alert(lB.emailCheckErrorMessage);
                }
            },
            url : reportHost + 'Consumer'
        });
    };

    localModule.sendRequest = function() {
        var lB = localizedBundle;
        // ** Submit data
        var dialog = new dijit.Dialog({ // ** Use DialogSimple as soon as Dojo 1.6 is out
        // ** var dialog = new dojox.widget.Dialog({
            content : lB.sendRequestInitialMessage,
            style : 'min-height: 100px; background-color: #fff; min-width: 400px; max-width: 800px;'
        });
        dialog.startup();
        dialog.show();
        dojo.io.script.get({
            callbackParamName : 'callback',
            content : getFormData(),
            error : function(dataBack) {
                alert(lB.sendRequestErrorMessage);
            },
            load : function(dataBack) {
                if (dataBack && dataBack.success) {
                    if (dijit.byId('closeDialog')) {
                        dijit.byId('closeDialog').destroyRecursive();
                    }
                    dialog.set('content', lB.sendRequestSuccessMessage);
                    new dijit.form.Button({
                        name : 'closeDialog',
                        type : 'submit',
                        onclick : function() {
                            dialog.hide();
                        }
                    }, 'closeDialog');
                    new Image().src = 'https://www.googleadservices.com/pagead/conversion/1019079067/?label=UgQfCMXjkQIQm9P35QM&amp;guid=ON&amp;script=0';
                }
                else {
                    alert(lB.sendRequestErrorMessage);
                }
            },
            url : reportHost + 'Demand'
        });
    };

    var toISOString = function(date, time) {
        // ** return dojo.date.stamp.toISOString(date, {}); // Contains the time zone gap
        var month = date.getMonth() + 1;
        var day = date.getDate();
        var hours = time ? time.getHours() : 23;
        var minutes = time ? time.getMinutes() : 59;
        return date.getFullYear() + (month < 10 ? '-0' : '-') + month + (day < 10 ? '-0' : '-') + day + (hours < 10 ? 'T0' : 'T') + hours + (minutes < 10 ? ':0' : ':') + minutes + ':00';
    };
})(); // End of the function limiting the scope of the private variables
