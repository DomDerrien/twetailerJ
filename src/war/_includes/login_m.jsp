<!doctype html>
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.List"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="com.live.login.WindowsLiveLogin"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.StringUtils"
    import="twetailer.connector.FacebookConnector"
    import="twetailer.dto.HashTag"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.MobileBrowserDetector"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String appVersion = appSettings.getProductVersion();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    cdnBaseURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.6"; // TODO: change at the application level
    useCDN = true;

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    boolean onLocalHost = "localhost".equals(request.getServerName()) || "127.0.0.1".equals(request.getServerName()) || "10.0.2.2".equals(request.getServerName());

    String liveAppId = new WindowsLiveLogin("liveIdKeys-" + (onLocalHost ? "localhost" : appSettings.getAppEngineId()) + ".xml").getAppId();
    String liveControlUrl = (onLocalHost ? "http://" : "https://") + "login.live.com/controls/WebAuthButton.htm";
    String liveBaseUrl = liveControlUrl + "?appid=" + liveAppId + "&context=/_wll/liveWebAuthHandler";

    boolean isAndroid = new MobileBrowserDetector(request).isAndroid();
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "ui_application_name", locale) %></title>
    <meta http-equiv="content-type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta http-equiv="cache-control" content="no-cache" />
    <meta http-equiv="pragma" content="no-cache" />
    <meta http-equiv="expires" content="0" />
    <meta name="description" content="<%= LabelExtractor.get(ResourceFileId.third, "login_localized_page_description", locale) %>" />
    <meta name="keywords" content="<%= LabelExtractor.get(ResourceFileId.third, "login_localized_page_keywords", locale) %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.third, "product_copyright", locale) %>" />
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,minimum-scale=1,user-scalable=no"/>
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>

    <%
    if (useCDN) {
    %><link href="<%= cdnBaseURL %>/dojox/mobile/themes/<%= isAndroid ? "android/android" : "iphone/iphone" %>.css" rel="stylesheet"></link>
    <link href="/css/console.css" rel="stylesheet" type="text/css" /><%
    }
    else { // elif (!useCDN)
    %><link href="/js/release/<%= appVersion %>/dojox/mobile/themes//<%= isAndroid ? "android/android" : "iphone/iphone" %>.css" rel="stylesheet" type="text/css" />
    <link href="/css/console.css" rel="stylesheet" type="text/css" /><%
    } // endif (useCDN)
    %>

    <%
    if (useCDN) {
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', modulePaths: { dojo: '<%= cdnBaseURL %>/dojo', dijit: '<%= cdnBaseURL %>/dijit', dojox: '<%= cdnBaseURL %>/dojox', twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/_includes/dojo_blank.html', locale: '<%= localeId %>'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    }
    else { // elif (!useCDN)
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: false, useXDomain: false, baseUrl: '/js/release/<%= appVersion %>/dojo/', dojoBlankHtmlUrl: '/_includes/dojo_blank.html', locale: '<%= localeId %>'"
        src="/js/release/<%= appVersion %>/dojo/dojo.js"
        type="text/javascript"
    ></script>
    <script
        src="/js/release/<%= appVersion %>/ase/login_m.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <script type="text/javascript">
        dojo.require('dojox.mobile.parser');
        dojo.require('dojox.mobile');
        dojo.require("dojox.mobile.ScrollableView");
        dojo.requireIf(!dojo.isWebKit, 'dojox.mobile.compat');
        dojo.ready(function() {
            dojox.mobile.parser.parse();
        });
    </script>

</head>
<body class="claro">

    <div dojoType="dojox.mobile.ScrollableView" selected="true">
        <h1 dojoType="dojox.mobile.Heading">Log in to AnotherSocialEconomy</h1>
        <ul dojoType="dojox.mobile.RoundRectList">
            <li
                clickable="true"
                dojoType="dojox.mobile.ListItem"
                icon="/images/icons/FaceBook-32.png" 
                onClick="window.location='<%= FacebookConnector.bootstrapAuthUrl(request) %>'+escape(window.location);"
                rightText="Go"
            ><%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_facebook", locale) %></li>
            <li
                clickable="false"
                dojoType="dojox.mobile.ListItem"
                icon="/images/icons/Twitter-32.png" 
                style="background-color:#aaa;"
            ><%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_twitter", locale) %></li>
            <li
                clickable="true"
                dojoType="dojox.mobile.ListItem"
                icon="/images/icons/Google-32.png" 
                onClick="window.location='/login?loginWith=google&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + escape(window.location);"
                rightText="Go"
            ><%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_google", locale) %></li>
            <li
                clickable="true"
                dojoType="dojox.mobile.ListItem"
                icon="/images/icons/Yahoo-red-32.png" 
                onClick="window.location='/login?loginWith=yahoo&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + escape(window.location);"
                rightText="Go"
            ><%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_yahoo", locale) %></li>
        </ul>
        <div dojoType="dojox.mobile.RoundRect" shadow="true">
            <%= LabelExtractor.get(ResourceFileId.third, "login_introduction_message", locale) %>
        </div>
    </div>

</body>
</html>
