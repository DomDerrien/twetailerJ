<!doctype html>
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.List"
    import="java.util.Locale"
    import="java.util.Map"
    import="java.util.ResourceBundle"
    import="com.dyuproject.openid.OpenIdUser"
    import="com.dyuproject.openid.RelyingParty"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.jsontools.JsonObject"
    import="org.apache.commons.codec.binary.Base64"
    import="twetailer.connector.FacebookConnector"
    import="twetailer.dto.Command"
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Demand"
    import="twetailer.dto.Entity"
    import="twetailer.dto.HashTag.RegisteredHashTag"
    import="twetailer.dto.Location"
    import="twetailer.j2ee.AuthVerifierFilter"
    import="twetailer.j2ee.BaseRestlet"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.task.step.BaseSteps"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    String localeId = request.getParameter("lg");
    if (localeId == null) {
        localeId = LocaleValidator.DEFAULT_LANGUAGE;
    }
    Locale locale = LocaleValidator.getLocale(localeId);

    // Url to go
    String urlToGo = request.getParameter("urlToGo");
    if (urlToGo != null) {
        response.sendRedirect(urlToGo);
        return;
    }

    // Check the page parameters
    Exception capturedEx = null;
    Consumer consumer = null;
    try {
        OpenIdUser user = BaseRestlet.getLoggedUser(request);
        if (user != null) {
            // Get the already logged-in user
            consumer = LoginServlet.getConsumer(user);
        }
        else {
            // Log the Facebook user in
            // 1. Get the logged in user information
            JsonObject requestParams = twetailer.connector.FacebookConnector.processSignedRequest(request);
            String facebookId = requestParams.getString(FacebookConnector.ATTR_USER_ID);
            // 2. Get the user if already registered
            if (facebookId != null) {
                List<Consumer> consumers = BaseSteps.getConsumerOperations().getConsumers(Consumer.FACEBOOK_ID, facebookId, 1);
                if (0 < consumers.size()) {
                    consumer = consumers.get(0);
                }
            }
            // 3. Create the user if not already registered
            String oauthToken = requestParams.getString(FacebookConnector.ATTR_OAUTH_TOKEN);
            if (consumer == null && oauthToken != null)  {
                JsonObject userInfo = FacebookConnector.getUserInfo(oauthToken);
                consumer = BaseSteps.getConsumerOperations().createConsumer(userInfo);
            }
            // 4. Propose the OpenId user record to the OpenId servlet filter process control, which authenticates the Facebook user
            RelyingParty.getInstance().getOpenIdUserManager().saveUser(AuthVerifierFilter.prepareOpenIdRecord(consumer), request, response);
        }
    }
    catch (Exception ex) {
        capturedEx = ex;
        // ex.printStackTrace();
    }

    String userName = consumer == null ? null : consumer.getName();
    String userEmail = consumer == null ? null : consumer.getEmail();
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <title><%= LabelExtractor.get(ResourceFileId.third, "coreConsu_localized_page_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <style type="text/css"><%
        if (useCDN) {
        %>
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";<%
        }
        else { // elif (!useCDN)
        %>
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/tundra/tundra.css";<%
        } // endif (useCDN)
        %>
        @import "/css/widget.css";
        <jsp:include page="/_includes/widget_css_parameters.jsp" />
        .bookmarklet-container {margin:1em 0;text-align:center}
        .bookmarklet-container .bookmarklet-link{background-color:#ccc;border:1px solid #999;border-color:#bbb #999 #777;border-radius:5px;-moz-border-radius:5px;-webkit-border-radius:5px;color:#333;font-weight:bold;padding:2px 10px;text-decoration:none}
        .bookmarklet-container .bookmarklet-callout{background:url(/images/bookmarklet-callout-arrow.gif) no-repeat center left;color:#7f7f66;margin-left:-3px;padding-left:6px}
        .bookmarklet-container .bookmarklet-callout-inner{background:#ffc;border:1px solid #fff1a8;border-left:0;padding:1px 5px}

        .fbSidebar {
            float:left;
            width: 179px;
            height: 100%;
            font-family: 'lucida grande',tahoma,verdana,arial,sans-serif;
            font-size: 11px;
            line-height: 13px;
            margin-top: 10px;
        }
        .fbSidebar .divider {
            font-size: 1px;
            margin: 8px 5px 0 0;
            border-top: 1px solid #eee;
        }
        #welcome, #errorMsg, #navigation, #info {
            padding-right: 5px;
        }
        #welcome {
           padding-bottom: 20px;
           color: #3B5998;
           font-weight: bold;
        }
        #navigation>ul{
            list-style-type: none;
            margin: 0;
            padding: 0;
        }
        #navigation>ul>li{
            border-bottom: 1px solid white;
            padding: 3px 2px 2px 3px;
        }
        #navigation>ul>li .countValue{
            font-size: smaller;
            float: right;
            color: #3B5998;
            background-color: #D8DFEA;
            border-radius: 2px;
            -moz-border-radius: 2px;
            font-weight: bold;
            padding: 0 3px;
        }
        #navigation>ul>li>img{
            vertical-align: middle;
        }
        #navigation>ul>li.selected {
            background-color: #D8DFEA;
            font-weight: bold;
        }
        #navigation>ul>li.enabled{
            cursor: pointer;
        }
        #navigation>ul>li.disabled {
            color: gray;
        }
        #navigation>ul>li:hover {
            background-color: #EFF2F7;
            text-decoration: none;
        }
        #info {
            color: gray;
        }
        .dataZone {
            left: 179px;
            border-left: 1px solid lightgrey;
            padding-left: 10px;
        }
        .dataZone>div {
            min-height: 0;
        }
        .dataZone>div h2 {
            line-height: 13px;
            vertical-align: bottom;
            font-size: 16px;
            font-weight: bold;
            color: #1C2A47;
            margin-top: 0;
            padding-top: 0;
        }
         .fbListZone, .fbListZone>div {
            font-family: 'lucida grande',tahoma,verdana,arial,sans-serif;
            background-color: transparent;
            border-radius: 0;
            -moz-border-radius: 0;
         }
    </style>

</head>
<body class="tundra">
    <div class="fbSidebar">
        <% if (userName != null) {
        %><div id="welcome"><%= LabelExtractor.get(ResourceFileId.third, "fbc_welcome_loggedUser", new Object[] { userName }, locale) %></div><%
        } else {
        %><div id="errorMsg"><%= LabelExtractor.get(ResourceFileId.third, "fbc_welcome_unknownUser", new Object[] { FacebookConnector.bootstrapAuthUrl(request), FacebookConnector.FB_MAIN_APP_URL }, locale) %></div><%
        } %>
        <div class="divider">&nbsp;</div>
        <div id="navigation">
            <ul>
                <li id="widgetZoneMI" onclick="localModule.showZone('widgetZone');">
                    <img src="/images/page_white_go_add.png" />
                    <%= LabelExtractor.get(ResourceFileId.third, "fbc_menu_newDemand", locale) %>
                </li><% if (userName != null) { %>
                <li id="demandZoneMI" onclick="localModule.showZone('demandZone');">
                    <span id="openDemandCount" class="countValue">?</span>
                    <img src="/images/page_white_go_list.png" />
                    <%= LabelExtractor.get(ResourceFileId.third, "fbc_menu_manageDemands", locale) %>
                </li>
                <li class="disabled" id="wishZoneMI">
                    <span id="openWishCount" class="countValue">?</span>
                    <img src="/images/page_white_love_list.png" />
                    <%= LabelExtractor.get(ResourceFileId.third, "fbc_menu_manageWishes", locale) %>
                </li><% } %>
            </ul>
        </div>
        <div class="divider">&nbsp;</div>
        <div id="info"><%= LabelExtractor.get(ResourceFileId.third, "fbc_menu_footer", locale) %></div>
        <div class="divider">&nbsp;</div>
        <div id="info" style="text-align:center;"><button dojoType="dijit.form.Button" onclick="window.location.reload();">Reload page</button></div>
    </div>

    <div class="dataZone fbListZone" id="demandZone" style="display:none;"><div>
        <h2><img src="/images/page_white_go_list.png" /> <%= LabelExtractor.get(ResourceFileId.third, "fbc_demandZone_title", locale) %></h2>
        <ul id="demandList" class="fbList"></ul>
    </div></div>


    <div class="dataZone fbListZone" id="wishZone" style="display:none;"><div>
        <h2><img src="/images/page_white_love_list.png" /> <%= LabelExtractor.get(ResourceFileId.third, "fbc_wishZone_title", locale) %></h2>
        <ul id="wishList" class="fbList"></ul>
    </div></div>

    <jsp:include page="/_includes/widget_ase_body.jsp" />

    <div
        dojoType="dijit.Dialog"
        id="locationMapDialog"
        title="<%= LabelExtractor.get(ResourceFileId.third, "shared_map_preview_dialog_title", locale) %>"
    >
        <div style="width:600px;height:400px;"><div id='mapPlaceHolder' style='width:100%;height:100%;'></div></div>
    </div>

    <script type="text/javascript"><!--
    dojo.addOnLoad(function(){
        dojo.require('dijit.Dialog');
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        dojo.addOnLoad(function(){
            localModule.secondInit();
        });
    });

    var localModule = localModule || {};
    localModule.secondInit = function() {
        localModule.showZone('widgetZone');<%
        if (userEmail != null) {
        %>
        // Initialize the field of the sender email address
        dijit.byId('email0').set('value', '<%= userEmail %>');

        // Fetch demands
        var pov = twetailer.Common.POINT_OF_VIEWS.CONSUMER;
        var dfd = twetailer.Common.loadRemoteDemands(null /* lastModificationDate */, null /* overlayId */, pov, null /* hashtags */); // No modificationDate means "load all active Demands"
        dfd.addCallback(function(response) { localModule.updateDemandCounter(response.resources); });

        // Fetch wishes
        // var dfd = twetailer.Common.loadRemoteWishes(null /* lastModificationDate */, null /* overlayId */, pov, null /* hashtags */); // No modificationDate means "load all active Demands"
        // dfd.addCallback(function(response) { localModule.updateWishCounter(response.resources); });
        localModule.updateWishCounter([]);<%
        } %>
    };
    localModule.showZone = function(selectedId) {
        var zoneIds = ['widgetZone', 'demandZone'], idx = zoneIds.length, zoneId;
        while (0 < idx) {
            -- idx;
            zoneId = zoneIds[idx];
            if (zoneId != selectedId) {
                dojo.query('#' + zoneId).style('display', 'none');
                dojo.query('#' + zoneId + 'MI').removeClass('selected').addClass('enabled');
            }
        }
        dojo.query('#' + selectedId).style('display', '');
        dojo.query('#' + selectedId + 'MI').removeClass('enabled').addClass('selected');

        switch(selectedId) {
            case 'widgetZone': break;
            case 'demandZone': localModule.fetchDemandList(); break;
            case 'wishZone': break;
        }
    };
    localModule.updateDemandCounter = function(resources) {
        var count = 0, idx = resources.length, resource, states = twetailer.Common.STATES;
        while (0 < idx) {
            --idx;
            resource = resources[idx];
            if (resource.state == states.PUBLISHED) {
                count ++;
            }
        }
        var counter = dojo.byId('openDemandCount');
        counter.innerHTML = '';
        counter.appendChild(dojo.doc.createTextNode(count));
    };
    localModule.updateWishCounter = function(resources) {
        var count = 0, idx = resources.length, resource, states = twetailer.Common.STATES;
        while (0 < idx) {
            --idx;
            resource = resources[idx];
            if (resource.state == states.PUBLISHED) {
                count ++;
            }
        }
        var counter = dojo.byId('openWishCount');
        counter.innerHTML = '';
        counter.appendChild(dojo.doc.createTextNode(count));
    };
    localModule._demandItem =
        '<div class="fbEntityIntro">' +
            '<div class="fbActionButtons fbTopActions" style="display:none;">' +
                '<a href="#" onclick="localModule.editDemand(${0});return false;"><img src="/images/page_white_go_edit.png" title="Edit demand" /></a>' +
                '<a href="#" onclick="localModule.cancelDemand(${0});return false;"><img src="/images/page_white_go_cancel.png" title="Cancel demand" /></a>' +
            '</div>' +
            '<span class="fbCriteria">${2}</span>' +
            '<span class="fbHashTags">${5}</span>' +
        '</div>' +
        '<div class="fbEntityDetails">' +
            '<span class="fbEntitylabel">Quantity:</span> ${9}<br />' +
            '<span class="fbEntitylabel">Due date:</span> ${3}<br />' +
            '<span class="fbEntitylabel">Within:</span> ${10} ${11} of ${6}<br />' +
        '</div>' +
        '<ul class="fbResponses">' +
            '<li class="fbResponseTopMarker"><i></i></li>' +
            '<li class="fbListItem"><span class="">Number of received proposals:</span> ${14}</li>' +
        '</ul>';
    localModule.fetchDemandList = function() {
        var placeHolder = dojo.byId('demandList');
        if (placeHolder.firstChild != null) {
            return;
        }
        var cache = twetailer.Common.getOrderedDemands(), demand, limit = cache.length, idx = 0, ul = dojo.byId('demandList'), li;
        while (idx < limit) {
            demand = cache[idx];
            li = dojo.create(
                    'li',
                    {
                        id: 'demand' + demand.key,
                        'class': 'fbListItem',
                        onmouseover: 'dojo.query(\'#demand' + demand.key + ' .fbTopActions\').style(\'display\', \'\');',
                        onmouseout: 'dojo.query(\'#demand' + demand.key + ' .fbTopActions\').style(\'display\', \'none\');'
                    },
                    ul
            );
            li.innerHTML = dojo.string.substitute(localModule._demandItem, [
                demand.key,              // 0
                demand.cc ? demand.cc.join(', ') : '',                    // 1
                demand.criteria ? demand.criteria.join(' ') : '',         // 2
                demand.dueDate,          // 3
                demand.expirationDate,   // 4
                demand.hashTags ? '#' + demand.hashTags.join(', #') : '', // 5
                twetailer.Common.displayLocale(demand.locationKey),       // 6
                demand.metadata,         // 7
                demand.modificationDate, // 8
                demand.quantity,         // 9
                demand.range,            // 10
                demand.rangeUnit,        // 11
                demand.source,           // 12
                demand.state,            // 13
                dojo.isArray(demand.proposalKeys) ? demand.proposalKeys.length : 0,     // 14
                '' // last, just to prevent the error reported because of a trailing comma
            ]);
            if (dojo.isArray(demand.proposalKeys)) {
                setTimeout(localModule._getDetachedFuction(demand.key, demand.proposalKeys), 100);
            }
            idx++;
        }
    };
    localModule._getDetachedFuction = function(demandKey, proposalKeys) {
        return function() {
            if (0 < proposalKeys.length) {
                var requiredProposalKeys = [], limit = proposalKeys.length, idx = 0;
                while (idx < limit) {
                    if (twetailer.Common.getCachedProposal(proposalKeys[idx]) == null) {
                        requiredProposalKeys.push(proposalKeys[idx]);
                    }
                    idx ++;
                }
                if (requiredProposalKeys.length == 0) {
                    localModule.insertProposals(demandKey);
                }
                else {
                    var dfd = twetailer.Common.loadRemoteProposals(requiredProposalKeys, null, twetailer.Common.POINT_OF_VIEWS.CONSUMER);
                    dfd.addCallback(function(response) { localModule.insertProposals(demandKey); });
                }
            }
        };
    };
    localModule._proposalResponse =
        '<div class="fbResponseIntro">' +
            '<div class="fbActionButtons fbSubActions" style="display:none;">' +
                '<a href="#" onclick="localModule.confirmProposal(${0});return false;"><img src="/images/page_white_come_accept.png" title="Confirm proposal" /></a>' +
                '<a href="#" onclick="localModule.declineProposal(${0});return false;"><img src="/images/page_white_come_cancel.png" title="Decline proposal" /></a>' +
            '</div>' +
            '<span class="fbCriteria">${1}</span>' +
        '</div>' +
        '<div class="fbResponseDetails">' +
            '<span class="fbEntitylabel">Quantity:</span> ${7}<br />' +
            '<span class="fbEntitylabel">Unit price:</span> ${2}${6}<br />' +
            '<span class="fbEntitylabel">Total cost:</span> ${2}${11}<br />' +
            '<span class="fbEntitylabel">Due date:</span> ${3}<br />' +
            '<span class="fbEntitylabel">Store:</span> ${10}<br />' +
            '<span class="fbEntitylabel">Store closing rate:</span> ${10}<br />' +
            '<span class="fbEntitylabel">Store registered by:</span> ${10}<br />' +
        '</div>';
    localModule.insertProposals = function(demandKey) {
        var demand = twetailer.Common.getCachedDemand(demandKey), proposalKeys = demand.proposalKeys, limit = proposalKeys.length, idx = 0, proposal, ul = dojo.query('#demand' + demandKey + ' .fbResponses')[0], li;
        while (idx < limit) {
            proposal = twetailer.Common.getCachedProposal(proposalKeys[idx]);
            li = dojo.create(
                    'li',
                    {
                        id: 'proposal' + proposal.key,
                        'class': 'fbListItem',
                        onmouseover: 'dojo.query(\'#proposal' + proposal.key + ' .fbSubActions\').style(\'display\', \'\');',
                        onmouseout: 'dojo.query(\'#proposal' + proposal.key + ' .fbSubActions\').style(\'display\', \'none\');'
                    },
                    ul
            );
            var currencySymbol = localModule._getLabel('master', 'currencySymbol_' + proposal.currencyCode);
            li.innerHTML = dojo.string.substitute(localModule._proposalResponse, [
                proposal.key,      // 0
                proposal.criteria ? proposal.criteria.join(' ') : '',         // 1
                currencySymbol,    // 2
                proposal.dueDate,  // 3
                proposal.hashTags ? '#' + proposal.hashTags.join(', #') : '', // 3
                proposal.metadata, // 4
                proposal.price,    // 6
                proposal.quantity, // 7
                proposal.source,   // 8
                proposal.state,    // 9
                proposal.storeKey, // 10
                proposal.total,    // 11
                '' // last, just to prevent the error reported because of a trailing comma
            ]);
            idx++;
        }
    };
    localModule.editDemand = function(demandKey) { alert('Not yet implemented ;)'); };
    localModule.cancelDemand = function(demandKey) {
        var demand = twetailer.Common.getCachedDemand(demandKey);

        if (demand.state == twetailer.Common.STATES.CONFIRMED) {
            var messageId = 'alert_cancelConfirmedDemand';

            var proposalKey = demand.proposalKeys[0];
            if (!confirm(_getLabel('console', messageId, [demandKey, proposalKey]))) {
                return;
            }
        }

        var data = { state: twetailer.Common.STATES.CANCELLED };

        var dfd = twetailer.Common.updateRemoteDemand(data, demandKey, null /* overlayId */);
        dfd.addCallback(function(response) {
            twetailer.Common.removeDemandFromCache(demandKey);
            // Fetch demands
            var pov = twetailer.Common.POINT_OF_VIEWS.CONSUMER;
            var dfd = twetailer.Common.loadRemoteDemands(null /* lastModificationDate */, null /* overlayId */, pov, null /* hashtags */); // No modificationDate means "load all active Demands"
            dfd.addCallback(function(response) { dojo.byId('demandList').innerHTML = ''; localModule.fetchDemandList(); });
        });
    };
    localModule.confirmProposal = function(proposalKey) {
        var data = {
            state: twetailer.Common.STATES.CONFIRMED,
            pointOfView: twetailer.Common.POINT_OF_VIEWS.CONSUMER
        };

        var dfd = twetailer.Common.updateRemoteProposal(data, proposalKey, null /* overlayId */);
        dfd.addCallback(function(response) { 
            var proposal = twetailer.Common.getCachedProposal(proposalKey), demand = twetailer.Common.getCachedDemand(proposal.demandKey), limit = demand.proposalKeys.length, idx = 0;
            while (idx < limit) {
                var movingProposalKey = demand.proposalKeys[idx];
                if (movingProposalKey != proposalKey) {
                    dojo.query('#proposal' + movingProposalKey).style('display', 'none');
                }
                idx ++;
            }
        });
    };
    localModule.declineProposal = function(proposalKey) {
        var proposal = twetailer.Common.getCachedProposal(proposalKey);

        if (proposal.state == twetailer.Common.STATES.CONFIRMED) {
            var messageId = 'alert_cancelConfirmedProposal';

            var demandKey = proposal.demandKey;
            if (!confirm(_getLabel('console', messageId, [demandKey, proposalKey]))) {
                return;
            }
        }

        var data = { state: twetailer.Common.STATES.CANCELLED };

        var dfd = twetailer.Common.updateRemoteProposal(data, proposalKey, null /* overlayId */);
        dfd.addCallback(function(response) {
            twetailer.Common.removeProposalFromCache(proposalKey);
            // Fetch demands
            var pov = twetailer.Common.POINT_OF_VIEWS.CONSUMER;
            var dfd = twetailer.Common.loadRemoteDemands(null /* lastModificationDate */, null /* overlayId */, pov, null /* hashtags */); // No modificationDate means "load all active Demands"
            dfd.addCallback(function(response) { 
                dojo.byId('demandList').innerHTML = ''; 
                localModule.fetchDemandList();
            });
        });
    };
    --></script>
    <style type="text/css">
        .fbList {
            list-style-type: none;
            margin: 0;
            padding: 0;
        }
        .fbListItem {
            border-bottom: 1px solid lightgrey;
            line-height: 24px;
        }
        .fbEntityIntro {
        }
        .fbEntityDetails {
            font-size: 11px;
            line-height: 16px;
            margin-left: 40px;
        }
        .fbEntitylabel {
            color: gray;
        }
        .fbActionButtons {
            float: right;
            padding: 3px 2px 0 10px;
        }
        .fbCriteria, .fbHashTags {
            font-size: 13px;
            font-weight: bold;
        }
        .fbHashTags {
            padding-left: 10px;
        }
        .fbResponses {
            list-style-type: none;
            margin: 0 40px 10px 40px;
            padding: 0;
        }
        .fbResponses .fbListItem {
            line-height: 16px;
            font-size: 11px;
            background-color: #EDEFF4;
            padding: 2px 5px;
            margin-bottom: 1px;
        }
        .fbResponseTopMarker {
            background-image: url(/images/facebook/response-intro.png);
            background-repeat: no-repeat;
            height: 5px;
            margin-left: 17px;
        }
    </style>
</body>
</html>
