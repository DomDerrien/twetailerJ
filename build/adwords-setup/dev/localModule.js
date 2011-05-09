var localModule = {};

(function() { // ** To limit the scope of the private variables

    var startDate = new Date(), reportHost = 'https://anothersocialeconomy.appspot.com/3rdParty/', reportDelay = 1000, reportId = '-', debugMode = false, reportOrder = 3, userKnown = true;

    dojo.require('dojo.cookie');
    dojo.require('dojo.io.script');
    dojo.require('dijit.Dialog');
    dojo.require('dojo.data.ItemFileReadStore');
    dojo.require('dijit.form.Button');
    dojo.require('dijit.form.CheckBox');
    dojo.require('dijit.form.DateTextBox');
    dojo.require('dijit.form.FilteringSelect');
    dojo.require('dijit.form.Form');
    dojo.require('dijit.form.NumberSpinner');
    dojo.require('dijit.form.Textarea');
    dojo.require('dijit.form.TextBox');
    dojo.require('dijit.Tooltip');
    dojo.require('dijit.form.ValidationTextBox');
    dojo.require('dojox.analytics.Urchin');

    dojo.addOnLoad(function() {
        var dueDate = new Date();
        dueDate.setMonth(dueDate.getMonth() + 1);

        var city = '${CITY}',
            maker = '${MAKE}' == 'Cars' || '${MAKE}' == 'Automobile' ? 'BMW' : '${MAKE}',
            model = '${MODEL}',
            qualifier = "${PRINTED_QUALIFIER}", // ** Use the double-quotes as delimiters because of D'occasion, for example
            postalCode = '${POSTAL_CODE}',
            checkCookie = true,
            info = '';
        
        setSocialLinks(localizedBundle.pageTitle, window.location.toString());

        if (window.location.search) {
            // ** Step 1: get the keywords
            var urlParams = dojo.queryToObject(window.location.search.slice(1)), skw = urlParams.kw;
            debugMode = urlParams.debugMode;
            reportHost = urlParams.host || reportHost;
            checkCookie = !urlParams.skipCookie;
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

        var lB = localizedBundle, makers = new dojo.data.ItemFileReadStore({ data : options });
        new dijit.form.Form({ id : 'requestForm' }, 'requestForm');
        new dijit.form.TextBox({ name : 'model', value : model, placeHolder : lB.modelFieldPlaceHolder, style : lB.inputFieldStyle }, 'model');
        new dijit.form.NumberSpinner({ name : 'range', value : 25, constraints : { min : 5, max : 100 }, style : lB.inputFieldStyle }, 'range');
        new dijit.form.ValidationTextBox({ name : 'postalCode', value : postalCode, placeHolder : lB.postalCodeFieldPlaceHolder, regExp : '[a-zA-Z][0-9][a-zA-Z] *[0-9][a-zA-Z][0-9]', required : true, trim : true, style : lB.inputFieldStyle }, 'postalCode');
        new dijit.form.DateTextBox({ name : 'dueDate', value : dueDate, constraints : { datePattern : 'EEE d MMM yyyy' }, style : lB.inputFieldStyle, required : true }, 'dueDate');
        new dijit.form.FilteringSelect({ name : 'make', value : maker, searchAttr : 'value', store : makers, style : lB.inputFieldStyle }, 'make');
        new dijit.form.Textarea({ name : 'info', value : info, style : lB.textareaFieldStyle }, 'info');
        new dijit.form.ValidationTextBox({ name : 'email', placeHolder : lB.emailFieldPlaceHolder, regExp : '[a-zA-Z0-9\.\_\%\-]+\@[a-zA-Z0-9\.\-]+\.[a-zA-Z]{2,4}', required : true, style: lB.inputFieldStyle, trim : true }, 'email');
        new dijit.form.CheckBox({ name : 'demoMode' }, 'demoMode');
        var lock = dojo.byId('emailLock');
        new dijit.Tooltip({label:lock.title, connectId:[lock]});
        lock.title = '';
        if (!debugMode) {
            if (checkCookie) {
                reportId = dojo.cookie('reportId') || reportId;
            }
            setTimeout(reportUsage, reportDelay);
            new dojox.analytics.Urchin({ acct : 'UA-11910037-5' });
        }
        if (lB.focusEmailField) {
            dijit.byId('email').focus();
        }
    });
    
    var setSocialLinks = function(title, url) {
        url = encodeURIComponent(url);
        title = encodeURIComponent(title);
        var placeHolder = dojo.byId('socialLinks'), anchor, image;
        // ** mailto:
        anchor = dojo.create('a', { href: 'mailto:?subject=' + title + '&body=' + url, rel: 'nofollow', style: 'float:left;clear:left;', target: 'socialLink' }, placeHolder);
        image = dojo.create('img', { height: '16px', src: 'http://anothersocialeconomy.appspot.com/images/icons/Email.png', title: 'Share by e-mail / envoyer par courriel', width: '16px' }, anchor);
        // ** Facebook
        anchor = dojo.create('a', { href: 'http://www.facebook.com/share.php?u=' + url + '&t=' + title, rel: 'nofollow', style: 'float:left;clear:left;', target: 'socialLink' }, placeHolder);
        image = dojo.create('img', { height: '16px', src: 'http://anothersocialeconomy.appspot.com/images/icons/FaceBook-32.png', title: 'Share on Facebook / partager sur Facebook', width: '16px' }, anchor);
        // ** Twitter
        anchor = dojo.create('a', { href: 'http://twitter.com/home?status=' + title + ' ' + url, rel: 'nofollow', style: 'float:left;clear:left;', target: 'socialLink' }, placeHolder);
        image = dojo.create('img', { height: '16px', src: 'http://anothersocialeconomy.appspot.com/images/icons/Twitter-32.png', title: 'Share on Twitter / partager sur Twitter', width: '16px' }, anchor);
        // ** Google Bookmarks
        anchor = dojo.create('a', { href: 'http://www.google.com/bookmarks/mark?op=edit&bkmk=' + url + '&title=' + title, rel: 'nofollow', style: 'float:left;clear:left;', target: 'socialLink' }, placeHolder);
        image = dojo.create('img', { height: '16px', src: 'http://anothersocialeconomy.appspot.com/images/icons/Google-32.png', title: 'Bookmark it with Google / Sauver le signet avec Google', width: '16px' }, anchor);
        // ** Internet Explorer Favorite
        if (dojo.isIE && window.externals && window.externals.AddFavorite) {
            anchor = dojo.create('a', { href: 'javascript:window.external.AddFavorite("' + url + '","' + title +'")', srel: 'nofollow', tyle: 'float:left;clear:left;', target: 'socialLink' }, placeHolder);
            image = dojo.create('img', { height: '16px', src: 'http://anothersocialeconomy.appspot.com/images/icons/IE-32.png', title: 'Bookmark it with Google / Sauver le signet avec Google', width: '16px' }, anchor);
        }
    };

    var reportUsage = function() {
        var data = getFormData(), dc = document;
        data.reportId = reportId;
        if (reportId.length == 1) {
            if (dc.referrer && 0 < dc.referrer.length) {
                data.referrerUrl = dc.referrer;
            }
        }
        else {
            if (dc.cookie && 0 < dc.cookie.length) {
                data.cookie = dc.cookie;
            }
        }
        data.reporterUrl =  window.location.toString();
        data.reporterTitle =  localizedBundle.pageTitle;
        dojo.io.script.get({
            callbackParamName : 'callback',
            content : data,
            error : function(dataBack) { },
            load : function(dataBack) {
                if (dataBack && dataBack.success) {
                    reportId = dataBack.reportId;
                    dojo.cookie('reportId', reportId, {'max-age': 10 * 60}); // ** 10 minutes
                    reportDelay = dataBack.reportDelay || 5 * reportDelay;
                    setTimeout(reportUsage, reportDelay);
                }
            },
            url : reportHost + 'Report'
        });
        -- reportOrder;
        if (reportOrder == 0) {
            new Image().src = localizedBundle.visitTrackerURL;
        }
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
            demoMode: false,
            exceptions : []
        }, exs = dataIn.exceptions, dc = document;
        if (dijit.byId('demoMode') && dijit.byId('demoMode').get('value') == 'on') { try { dataIn.demoMode = true; dataIn.hashTags.push('demo'); } catch (ex) { exs.push('hashTags - ' + ex); } }
        try { dataIn.email = dijit.byId('email').get('value'); } catch (ex) { exs.push('email - ' + ex); }
        try { dataIn.postalCode = dijit.byId('postalCode').get('value'); } catch (ex) { exs.push('postalCode - ' + ex); }
        try { dataIn.range = dijit.byId('range').get('value'); } catch (ex) { exs.push('range - ' + ex); }
        try { dataIn.dueDate = toISOString(dijit.byId('dueDate').get('value')); } catch (ex) { exs.push('dueDate - ' + ex); }
        try { dataIn.content = dojo.byId('makeFieldLabel').innerHTML + ' ' + dijit.byId('make').get('value'); } catch (ex) { exs.push('make - ' + ex); }
        try { dataIn.content += ', ' + dojo.byId('modelFieldLabel').innerHTML + ' ' + dijit.byId('model').get('value'); } catch (ex) { exs.push('model - ' + ex); }
        try { dataIn.metadata = '{\'make\':\'' + dijit.byId('make').get('value') + '\',\'model\':\'' + dijit.byId('model').get('value') + '\'}';  } catch (ex) { exs.push('metadata - ' + ex); }
        try {
            var info = dijit.byId('info').get('value');
            if (info && 0 < info.length) {
                dataIn.content += ', ' + dojo.byId('infoFieldLabel').innerHTML + ' ' + info;
            }
        } catch (ex) { exs.push('info - ' + ex); }
        return dataIn;
    };

    localModule.showReviewPane = function() {
        var lB = localizedBundle, dc = document;
        // ** Validate the input fields content
        if (!dijit.byId('requestForm').validate()) {
            return;
        }
        // ** Create the dialog box
        var reviewPane = dijit.byId('reviewPane') || new dijit.Dialog({
            execute: localModule.sendRequest,
            id : 'reviewPane',
            title : lB.reviewPaneTitle
        });
        reviewPane.setContent(
            '<div class="dijitDialogPaneContentArea">' +
            '<table class="reviewPaneTable"><tbody>' +
            '<tr><th>' + dojo.byId('emailFieldLabel').innerHTML + '</th><td>' + dijit.byId('email').get('value') + '</td><td rowspan="6">' + lB.emailCheckInitialMessage + '</td></tr>' +
            '<tr><th>' + dojo.byId('makeFieldLabel').innerHTML + '</th><td>' + dijit.byId('make').get('value') + '</td></tr>' +
            '<tr><th>' + dojo.byId('modelFieldLabel').innerHTML + '</th><td>' + dijit.byId('model').get('value') + '</td></tr>' +
            '<tr><th>' + dojo.byId('postalCodeFieldLabel').innerHTML + '</th><td>' + dijit.byId('postalCode').get('value') + '</td></tr>' +
            '<tr><th>' + dojo.byId('rangeFieldLabel').innerHTML + '</th><td>' + dojo.byId('range').value + ' </td></tr>' + // ** Use dojo.byId().value instead of dijit.byId().get('value') to keep the comma as a separator in French
            '<tr><th>' + dojo.byId('dueDateFieldLabel').innerHTML + '</th><td>' + dojo.byId('dueDate').value + '</td></tr>' + // ** Use dojo.byId().value instead of dijit.byId().get('value') to keep the formatted date
            '<tr><th>' + dojo.byId('infoFieldLabel').innerHTML + '</th><td colspan="2">' + dijit.byId('info').get('value') + '</td></tr>' +
            '</tbody></table>' +
            '</div>' +
            '<div class="dijitDialogPaneActionBar">' +
            '<button dojoType="dijit.form.Button" type="submit">' + dojo.byId('submitButton').innerHTML + '</button>' +
            '<div style="float:left;padding:4px 0;"><a href="javascript:dijit.byId(\'reviewPane\').hide();">' + lB.cancelButton + '</a></div>' +
            '</div>'
        );
        // ** Display the dialog box
        reviewPane.show();
        // ** Call the server for an email check
        dojo.io.script.get({
            callbackParamName : 'callback',
            content : {
                referralId : 0, // ** By ASE itself
                hashTags : [ 'cardealer' ],
                reportId : reportId,
                reporterTitle: lB.pageTitle,
                reporterUrl: window.location.toString(),
                language : localizedBundle.locale,
                email : dijit.byId('email').get('value')
            },
            error : function(dataBack) {
                alert(lB.emailCheckErrorMessage);
            },
            load : function(dataBack) {
                if (dataBack && dataBack.success) {
                    userKnown = dataBack.status;
                }
                else {
                    alert(lB.emailCheckErrorMessage);
                }
            },
            url : reportHost + 'Consumer'
        });
    };

    localModule.sendRequest = function() {
        var lB = localizedBundle,
            dialog = dijit.byId('confirmationPane') || new dijit.Dialog({
                id: 'confirmationPane',
                title : lB.sendRequestPaneTitle,
            }),
            data = getFormData();
        dialog.setContent(lB.sendRequestInitialMessage);
        dialog.startup();
        dialog.show();
        data.reportId = reportId;
        data.referralId = 0; // ** By ASE itself
        dojo.io.script.get({
            callbackParamName : 'callback',
            content : data,
            error : function(dataBack) {
                alert(lB.sendRequestErrorMessage);
            },
            load : function(dataBack) {
                if (dataBack && dataBack.success) {
                    if (dijit.byId('closeDialog')) {
                        dijit.byId('closeDialog').destroyRecursive();
                    }
                    dialog.hide();
                    dialog.set('content', userKnown ? lB.sendRequestSuccessMessageKnownUser : lB.sendRequestSuccessMessageUnknownUser);
                    new dijit.form.Button({
                        name : 'closeDialog',
                        type : 'submit',
                        onclick : function() {
                            dialog.hide();
                        }
                    }, 'closeDialog');
                    if (!data.demoMode) {
                        new Image().src = lB.demandTrackerURL;
                    }
                    new Image().src = lB.interestTrackerURL;
                    dojo.cookie('reportId', null, { expires: -1 }); // ** To delete the cookie
                    dialog.show();
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
})(); // ** End of the function limiting the scope of the private variables
