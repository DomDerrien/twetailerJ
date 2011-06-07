<!doctype html>
<%@page import="domderrien.jsontools.JsonParser"%>
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.io.PrintWriter"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.Map"
    import="java.util.ResourceBundle"
    import="com.dyuproject.openid.OpenIdUser"
    import="com.dyuproject.openid.RelyingParty"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.StringUtils"
    import="twetailer.connector.BaseConnector.Source"
    import="twetailer.connector.TwitterConnector"
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Location"
    import="twetailer.dto.Store"
    import="twetailer.dto.SaleAssociate"
    import="twetailer.j2ee.BaseRestlet"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    String appId = appSettings.getAppEngineId();
    String appVersion = appSettings.getProductVersion();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    useCDN = true;
    cdnBaseURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.6"; // TODO: change at the application level

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    Exception issue = null;
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <title>OAuth Access Console</title>
    <meta http-equiv="content-type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <% if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/claro/claro.css";
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><link href="/js/release/<%= appVersion %>/dojo/resources/dojo.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dijit/themes/claro/claro.css" rel="stylesheet" type="text/css" />
    <link href="/css/console.css" rel="stylesheet" type="text/css" /><%
    } // endif (useCDN)
    %>
</head>
<body class="claro">

    <div id="introFlash">
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>

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
        src="/js/release/<%= appVersion %>/ase/_admin.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" data-dojo-type="dijit.layout.BorderContainer" data-dojo-props="gutters: false" style="height: 100%;">
        <jsp:include page="/_includes/banner_protected.jsp">
            <jsp:param name="pageForAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="isLoggedUserAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="consumerName" value="Administrator" />
        </jsp:include>
        <div
            id="centerZone"
            data-dojo-type="dijit.layout.ContentPane"
            data-dojo-props="region: 'center'"
        >
            <fieldset class="entityInformation" style="margin:5px;">
                <legend><%= appSettings.getAppEngineId() %> Account</legend>
                <p>OAuth source Key: <%= OAuthVerifierServlet.getOAuthKey(appId) %><br />
                OAuth source Secret: <%= OAuthVerifierServlet.getOAuthSecret(appId) %></p>
            </fieldset>
            <fieldset class="entityInformation" style="margin:5px;">
                <legend>Authentication with return URL for the secret token generation</legend>
                <%@page
                    import="oauth.signpost.OAuth"
                    import="oauth.signpost.OAuthConsumer"
                    import="oauth.signpost.OAuthProvider"
                    import="oauth.signpost.basic.DefaultOAuthConsumer"
                    import="oauth.signpost.basic.DefaultOAuthProvider"
                    import="oauth.signpost.exception.OAuthCommunicationException"

                    import="twetailer.j2ee.OAuthVerifierServlet"
                %>
                <%
                /*
                    import="oauth.signpost.jetty.JettyOAuthConsumer"
                    import="oauth.signpost.commonshttp.CommonsHttpOAuthConsumer"
                    import="oauth.signpost.commonshttp.CommonsHttpOAuthProvider"
                */

                // 1. Entity setup
                OAuthConsumer consumer = new DefaultOAuthConsumer(
                // OAuthConsumer consumer = new JettyOAuthConsumer(
                // OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
                        OAuthVerifierServlet.getOAuthKey(appId),
                        OAuthVerifierServlet.getOAuthSecret(appId));

                OAuthProvider provider = new DefaultOAuthProvider(
                // OAuthProvider provider = new CommonsHttpOAuthProvider(
                        OAuthVerifierServlet.getRequestTokenUrl(appId),
                        OAuthVerifierServlet.getAccessTokenUrl(appId),
                        OAuthVerifierServlet.getAuthorizeUrl(appId));

                // 2. Request token & authorization URL build up
                String requestTokenUrl = provider.retrieveRequestToken(consumer, OAuthVerifierServlet.getAccessTokenUrl(appId));

                out.write("<p>Generated request token URL: <a href='" + requestTokenUrl + "' target='_blank'>" + requestTokenUrl + "</a></p>");

                out.write("<p>The user should be invited to click and he'll be directed to a page where he can grant access rights to the application.<br/>");
                out.write("Once the rights are granted, the browser is redirected to the following URL which produces a payload:</p>");
                out.write("<ul><li>https://twetailer.appspot.com/_ah/OAuthGetAccessToken?oauth_verifier=LKgKi44OcGMXOeke1tMb2zz_&oauth_token=4%2FgIktmsDlTvAzmGO6mVvjs13OK5To</li>");
                out.write("<li>oauth_token=1%2FdX7EdE6S7cNuVBWlIBYsl9C3r080ntR7l5VYrRr6wy0&oauth_token_secret=M_Bx1sNlG53qhTSD2A_aG23V</li></ul>");

                // 3. Signed access to a OAuth protected resource
                out.write("<p>The consumer descriptor is set with the retreived pair {key; secret} and a <b>signed</b> call to https://" + appId + ".appspot.com/oauth is issued.");
                consumer.setTokenWithSecret("1/dX7EdE6S7cNuVBWlIBYsl9C3r080ntR7l5VYrRr6wy0", "M_Bx1sNlG53qhTSD2A_aG23V");

                java.net.URL url = new java.net.URL("http://" + appId + ".appspot.com/oauth");
                java.net.HttpURLConnection twetailerRequest = (java.net.HttpURLConnection) url.openConnection();
                consumer.sign(twetailerRequest);
                twetailerRequest.connect();

                int statusCode = twetailerRequest.getResponseCode();
                out.write("<ul><li>Response code: " + statusCode + "</li>");
                out.write("<li>Content type: " + twetailerRequest.getContentType() + "</li>");
                domderrien.jsontools.JsonObject json = new domderrien.jsontools.JsonParser(twetailerRequest.getInputStream()).getJsonObject();
                out.write("<li>Parsed content: " + json.toString() + "</li>");
                java.util.Map<String, java.util.List<String>> headerFields = twetailerRequest.getHeaderFields();
                for (String fieldKey: headerFields.keySet()) {
                    out.write("<li>Header field: " + fieldKey);
                    out.write(" -- ");
                    for (int i=0; i < headerFields.get(fieldKey).size(); i++) {
                        out.write(headerFields.get(fieldKey).get(i));
                    }
                    out.write("</li>");
                }
                out.write("</ul>");
                %>
            </fieldset>
            <fieldset class="entityInformation" style="margin:5px;">
                <legend>Authentication with return URL on the localhost</legend>
                <%
                requestTokenUrl = provider.retrieveRequestToken(consumer, "http://localhost:8080/oauth");

                out.write("<p>Generated request token URL: <a href='" + requestTokenUrl + "' target='_blank'>" + requestTokenUrl + "</a></p>");

                out.write("<p>The user should be invited to click and he'll be directed to a page where he can grant access rights to the application.<br/>");
                out.write("Once the rights are granted, the browser is redirected to the following URL with a message (note the produced token changes everytime):</p>");
                out.write("<ul><li>http://localhost:8080/oauth?oauth_verifier=UP0uPM3g1L2NnCvYhsmtl12T&oauth_token=4%2Fke1Akg1sltcT7LFYY7b2l750So8f</li>");
                out.write("<li>{<br/>&nbsp;&nbsp;'headers':{<br/>&nbsp;&nbsp;&nbsp;&nbsp;'Accept-Language':'en-US,en;q=0.8',<br/>&nbsp;&nbsp;&nbsp;&nbsp;'Cookie':'CP=null*; dev_appserver_login=test@example.com:true:18580476422013912411; JSESSIONID=euqgggw8t8wm',<br/>&nbsp;&nbsp;&nbsp;&nbsp;'Host':'localhost:8080',<br/>&nbsp;&nbsp;&nbsp;&nbsp;'Accept-Charset':'ISO-8859-1,utf-8;q=0.7,*;q=0.3',<br/>&nbsp;&nbsp;&nbsp;&nbsp;'Accept-Encoding':'gzip,deflate,sdch',<br/>&nbsp;&nbsp;&nbsp;&nbsp;'User-Agent':'Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.71 Safari/534.24',<br/>&nbsp;&nbsp;&nbsp;&nbsp;'Accept':'application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5',<br/>&nbsp;&nbsp;&nbsp;&nbsp;'Connection':'keep-alive',<br/>&nbsp;&nbsp;&nbsp;&nbsp;'Cache-Control':'max-age=0'<br/>&nbsp;&nbsp;},<br/>&nbsp;&nbsp;'email':'example@example.com',<br/>&nbsp;&nbsp;'entryPoint':'/oauth',<br/>&nbsp;&nbsp;'params':{},<br/>&nbsp;&nbsp;'success':true<br/>}</li></ul>");

                String verificationCode = "5A60pMJgg7jTU9uUW-Riesa3";

                try {
                    provider.retrieveAccessToken(consumer, verificationCode);
                }
                catch(oauth.signpost.exception.OAuthCommunicationException ex) {
                    out.write("<p><span style='color:red'>");
                    out.write("Cannot get the access token! Message: '" + ex.getMessage() + "'");
                    out.write("</span><p>");
                    issue = ex;
                }

                %>
            </fieldset>
            <fieldset class="entityInformation" style="margin:5px;">
                <legend>Authentication with NO return URL</legend>
                <%
                requestTokenUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);

                out.write("<p>Generated request token URL: <a href='" + requestTokenUrl + "' target='_blank'>" + requestTokenUrl + "</a></p>");

                out.write("<p>The user should be invited to click and he'll be directed to a page where he can grant access rights to the application.<br/>");
                out.write("Once the rights are granted, the browser is redirected to the following URL with a message (note the produced token changes everytime):</p>");
                out.write("<ul><li>https://www.google.com/accounts/OAuthAuthorizeToken</li>");
                out.write("<li>You have successfully granted twetailer.appspot.com access to your Google Account. You can revoke access at any time under 'My Account'.<br/>To complete the process, please provide twetailer.appspot.com with this verification code: <b>W5vcc3-7A6GtjwHOXQMa-YBx</b></li></ul>");

                %>
            </fieldset>
            <fieldset class="entityInformation" style="margin:5px;">
                <legend>Request Parameters</legend>
                <% for (Object name: request.getParameterMap().keySet()) {
                    out.write((String) name);
                    out.write(": ");
                    out.write((String) request.getParameter((String) name));
                    out.write("<br />");
                } %>
            </fieldset>
            <fieldset class="entityInformation" style="margin:5px;">
                <legend>Request Headers</legend>
                <% Enumeration<?> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = (String) headerNames.nextElement();
                    out.write((String) headerName);
                    out.write(": ");
                    out.write((String) request.getHeader(headerName));
                    out.write("<br />");
                } %>
            </fieldset>
            <fieldset class="entityInformation" style="margin:5px;">
                <legend>Issue Stack Trace</legend>
                <% if (issue != null) {
                    out.write("<pre>");
                    issue.printStackTrace(new PrintWriter(out));
                    out.write("</pre>");
                } %>
            </fieldset>
        </div>
        <div data-dojo-type="dijit.layout.ContentPane" id="footerZone" data-dojo-props="region: 'bottom'">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        // dojo.require('dojo.data.ItemFileWriteStore');
        dojo.require('dojo.parser');
        dojo.require('dijit.Dialog');
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        // dojo.require('dijit.layout.TabContainer');
        // dojo.require('dijit.form.Form');
        // dojo.require('dijit.form.Button');
        // dojo.require('dijit.form.CheckBox');
        // dojo.require('dijit.form.ComboBox');
        // dojo.require('dijit.form.DateTextBox');
        // dojo.require('dijit.form.FilteringSelect');
        // dojo.require('dijit.form.NumberTextBox');
        // dojo.require('dijit.form.Textarea');
        // dojo.require('dijit.form.TextBox');
        dojo.require('dojox.analytics.Urchin');
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            dojo.fadeOut({
                node: 'introFlash',
                delay: 50,
                onEnd: function() {
                    dojo.style('introFlash', 'display', 'none');
                }
            }).play();<%
            if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName()) && !"10.0.2.2".equals(request.getServerName())) { %>
            new dojox.analytics.Urchin({ acct: 'UA-11910037-2' });<%
            } %>
            dojo.byId('logoutLink').href = '<%= com.google.appengine.api.users.UserServiceFactory.getUserService().createLogoutURL(request.getRequestURI()) %>';
        });
    });

    var localModule = new Object();
    </script>
</body>
</html>
