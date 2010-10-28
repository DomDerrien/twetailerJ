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
        System.err.println("&&&&&&&& ready to stop the process");
        return;
    }
    System.err.println("&&&&&&&& process continuing");

    // Check the page parameters
    Exception capturedEx = null;
    String userName = null;
    try {
        JsonObject requestParams = twetailer.connector.FacebookConnector.processSignedRequest(request);
        String facebookId = requestParams.getString(FacebookConnector.ATTR_USER_ID);
        if (facebookId != null) {
            List<Consumer> consumers = BaseSteps.getConsumerOperations().getConsumers(Consumer.FACEBOOK_ID, facebookId, 1);
            System.err.println("^^^^^^^^^ look up for ASE consumer with facebookId: " + facebookId);
            if (0 < consumers.size()) {
                userName = "1. " + consumers.get(0).getName();
            }
        }
        String oauthToken = requestParams.getString(FacebookConnector.ATTR_OAUTH_TOKEN);
        if (userName == null && oauthToken != null)  {
            JsonObject userInfo = FacebookConnector.getUserInfo(oauthToken);
            userName = "2. " + userInfo.getString(FacebookConnector.ATTR_NAME);
        }
    }
    catch (Exception ex) {
        capturedEx = ex;
        ex.printStackTrace();
    }
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

        #sidebar {
            float:left;
            width: 179px;
            height: 100%;
            font-family: 'lucida grande',tahoma,verdana,arial,sans-serif;
            font-size: 11px;
            line-height: 13px;
            margin-top: 10px;
        }
        #sidebar .divider {
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
        #centerZone {
            left: 179px;
        }
    </style>

</head>
<body class="tundra">
    <div id="sidebar">
        <% if (userName != null) { %><div id="welcome">
            Welcome <%= userName %>
        </div><% } %>
        <% if (userName == null) { %><div id="errorMsg">
            Hi, Facebook has not been able to tell us who you are.<br/><br/>
            You might be logged out from Facebook or you has not yet
            <a href="<%= FacebookConnector.bootstrapAuthUrl(request) %>" onclick="this.href += escape(window.location + '&urlToGo=<%= FacebookConnector.FB_MAIN_APP_URL %>');" target="_blank">authorize</a>
            our application.<br/><br/>
            You can still use the <a href="http://anothersocialeconomy.com/">AnotherSocialEconomy.com</a>
            widget with your personal email address. Confirmations of your operations and notifications
            of proposals made by local retailers will be sent to your email account.
        </div><% } %>
        <div class="divider">&nbsp;</div>
        <div id="navigation">
            <ul>
                <li class="selected"><img src="/images/page_white_go_add.png" /> New Demand...</li><% if (userName != null) { %>
                <li class="disabled"><span id="openDemandCount" class="countValue">1</span><img src="/images/page_white_go_list.png" /> Opened Demands</li>
                <li class="disabled"><span id="openWishCount" class="countValue">0</span><img src="/images/page_white_go_list.png" /> Opened Wishes</li><% } %>
            </ul>
        </div>
        <div class="divider">&nbsp;</div>
        <div id="info">
            More information on <a href="http://anothersocialeconomy.com/">AnotherSocialEconomy.com</a>.
        </div>
    </div>

    <jsp:include page="/_includes/widget_ase_body.jsp" />

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        // dojo.require("dojo.fx");
        dojo.addOnLoad(function(){
            localModule.secondInit();
        });
    });

    var localModule = localModule || {};
    localModule.secondInit = function() {
    };
    </script>
</body>
</html>
