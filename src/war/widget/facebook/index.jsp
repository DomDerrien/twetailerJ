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
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Demand"
    import="twetailer.dto.HashTag.RegisteredHashTag"
    import="twetailer.dto.Location"
    import="twetailer.j2ee.AuthVerifierFilter"
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
        JsonObject requestParams = twetailer.connector.FacebookConnector.processSignedRequest(request);
        String facebookId = requestParams.getString(FacebookConnector.ATTR_USER_ID);
        if (facebookId != null) {
            List<Consumer> consumers = BaseSteps.getConsumerOperations().getConsumers(Consumer.FACEBOOK_ID, facebookId, 1);
            if (0 < consumers.size()) {
                consumer = consumers.get(0);
            }
        }
        String oauthToken = requestParams.getString(FacebookConnector.ATTR_OAUTH_TOKEN);
        if (consumer == null && oauthToken != null)  {
            JsonObject userInfo = FacebookConnector.getUserInfo(oauthToken);
            consumer = BaseSteps.getConsumerOperations().createConsumer(userInfo);
        }
        request.getSession(true).setAttribute(OpenIdUser.ATTR_NAME, AuthVerifierFilter.prepareOpenIdRecord(consumer));
    }
    catch (Exception ex) {
        capturedEx = ex;
        // ex.printStackTrace();
    }

    String userName = consumer.getName();
    String userEmail = consumer.getEmail();
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
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/Grid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/tundraGrid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/enhanced/resources/tundraEnhancedGrid.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/FloatingPane.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/ExpandoPane.css";<%
        }
        else { // elif (!useCDN)
        %>
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/tundra/tundra.css";
        @import "/js/dojo/dojox/grid/resources/Grid.css";
        @import "/js/dojo/dojox/grid/resources/tundraGrid.css";
        @import "/js/dojo/dojox/grid/enhanced/resources/tundraEnhancedGrid.css";
        @import "/js/dojo/dojox/layout/resources/FloatingPane.css";
        @import "/js/dojo/dojox/layout/resources/ExpandoPane.css";<%
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
        }
        #navigation>ul>li .countValue{
            float: right;
            background-color: #D8DFEA;
            border-radius: 2px;
            -moz-border-radius: 2px;
            font-weight: bold;
            padding: 0 4px;
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
        <% if (userName != null) { %><div id="welcome"><%= LabelExtractor.get(ResourceFileId.third, "fbc_welcome_loggedUser", new Object[] { userName }, locale) %></div><% } %>
        <% if (userName == null) { %><div id="errorMsg"><%= LabelExtractor.get(ResourceFileId.third, "fbc_welcome_unknownUser", new Object[] { FacebookConnector.bootstrapAuthUrl(request), FacebookConnector.FB_MAIN_APP_URL }, locale) %></div><% } %>
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
        <div id="info">
            More information on <a href="http://anothersocialeconomy.com/">AnotherSocialEconomy.com</a>.
        </div>
    </div>

    <div class="dataZone fbListZone" id="demandZone" style="display:none;"><div>
        <h2><img src="/images/page_white_go_list.png" /> <%= LabelExtractor.get(ResourceFileId.third, "fbc_demandZone_title", locale) %></h2>
        <div id="demandList"></div>
    </div></div>


    <div class="dataZone fbListZone" id="wishZone" style="display:none;"><div>
        <h2><img src="/images/page_white_love_list.png" /> <%= LabelExtractor.get(ResourceFileId.third, "fbc_wishZone_title", locale) %></h2>
    </div></div>

    <jsp:include page="/_includes/widget_ase_body.jsp" />

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        dojo.require('dojox.grid.EnhancedGrid');
        dojo.addOnLoad(function(){
            localModule.secondInit();
        });
    });

    var localModule = localModule || {};
    localModule.secondInit = function() {
        localModule.showZone('widgetZone');

        <% if (userEmail != null) { %>dijit.byId('email0').set('value', '<%= userEmail %>');<% } %>

        // Fetch demands
        var pov = twetailer.Common.POINT_OF_VIEWS.CONSUMER;
        var dfd = twetailer.Common.loadRemoteDemands(null /* lastModificationDate */, null /* overlayId */, pov, null /* hashtags */); // No modificationDate means "load all active Demands"
        dfd.addCallback(function(response) { localModule.updateDemandCounter(response.resources); });

        // Fetch wishes
        // var dfd = twetailer.Common.loadRemoteWishes(null /* lastModificationDate */, null /* overlayId */, pov, null /* hashtags */); // No modificationDate means "load all active Demands"
        // dfd.addCallback(function(response) { localModule.updateWishCounter(response.resources); });
        localModule.updateWishCounter([]);
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
            case 'demandZone': localModule.fetchDemandGrid(); break;
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
    localModule.fetchDemandGrid = function() {
        var placeHolder = dojo.byId("demandList");
        if (placeHolder.firstChild != null) {
            return;
        }
        placeHolder.appendChild(dojo.doc.createTextNode("demand"));
    };
    </script>
</body>
</html>
