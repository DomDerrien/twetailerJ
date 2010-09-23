<!doctype html>
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
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Consumer attributes
    OpenIdUser loggedUser = BaseRestlet.getLoggedUser(request);
    Consumer consumer = LoginServlet.getConsumer(loggedUser);

    Exception issue = null;
%><html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="<%= localeId %>">
<head>
    <title>Twitter Access Console</title>
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <%
    if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/Grid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/tundraGrid.css";
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><style type="text/css">
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/tundra/tundra.css";
        @import "/js/dojo/dojox/grid/resources/Grid.css";
        @import "/js/dojo/dojox/grid/resources/tundraGrid.css";
        @import "/css/console.css";
    </style><%
    } // endif (useCDN)
    %>
    <style type="text/css">
        .entityInformation td {
            height: 24px;
            vertical-align: middle;
        }
    </style>
</head>
<body class="tundra">

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>

    <%
    if (useCDN) {
    %><script
        djConfig="parseOnLoad: false, isDebug: true, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/html/blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    }
    else { // elif (!useCDN)
    %><script
        djConfig="parseOnLoad: false, isDebug: false, baseUrl: '/js/dojo/dojo/', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/html/blank.html'"
        src="/js/dojo/dojo/dojo.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/_includes/banner_protected.jsp">
            <jsp:param name="pageForAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="isLoggedUserAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="consumerName" value="<%= consumer.getName() %>" />
        </jsp:include>
        <div
            dojoType="dijit.layout.ContentPane"
            id="centerZone"
            region="center"
        >
            <fieldset class="entityInformation" style="margin:5px;">
                <legend>Twetailer Account</legend>
                <button dojoType="dijit.form.Button" style="float:right;">Get Application OAuth Token</button>
                Consumer Key: <%= TwitterConnector.TWETAILER_TWITTER_CONSUMER_KEY %><br />
                Consumer Secret: <%= TwitterConnector.TWETAILER_TWITTER_CONSUMER_SECRET %><br />
                <br />
                <%@page
                    import="twitter4j.Twitter"
                    import="twitter4j.TwitterException"
                    import="twitter4j.TwitterFactory"
                    import="twitter4j.conf.ConfigurationContext"
                    import="twitter4j.http.AccessToken"
                    import="twitter4j.http.OAuthAuthorization"
                    import="twitter4j.http.RequestToken"
                %>
                <% if (request.getParameter("getOAuthToken") != null) {
                    try {
                        Twitter twitter = new TwitterFactory().getInstance();
                        twitter.setOAuthConsumer(TwitterConnector.TWETAILER_TWITTER_CONSUMER_KEY, TwitterConnector.TWETAILER_TWITTER_CONSUMER_SECRET);

                        RequestToken requestToken = null;
                        try {
                            // Step 1: Retrieve a request token
                            boolean inSession = session.getAttribute("requestToken") != null; // && request.getParameter("ignoreSession") != null;
                            if (inSession) {
                                requestToken = (RequestToken) session.getAttribute("requestToken");
                            }
                            else {
                                String fromPageURL = request.getRequestURI();
                                String queryString = request.getQueryString();
                                if (queryString != null) {
                                    fromPageURL += "?" + queryString;
                                }

                                requestToken = twitter.getOAuthRequestToken(fromPageURL + "&source=twitterForRequestToken");
                                session.setAttribute("requestToken", requestToken);
                            }
                            out.write(inSession ? "From session:<br />" : "Live:<br />");
                            out.write("Request token key: " + requestToken.getToken() + "<br />");
                            out.write("Request token secret: " + requestToken.getTokenSecret() + "<br />");
                        }
                        catch (TwitterException ex) {
                            out.write("<br /><span style='color:red'>");
                            out.write("Attempt to get the request token failed! Message: '" + ex.getMessage() + "'");
                            out.write("</span><br />");
                            issue = ex;
                        }
                        out.write("<br />");
                        AccessToken accessToken = null;
                        if (issue == null) {
                            try {
                                // Step 2: Request user authorization
                                boolean inSession = session.getAttribute("accessToken") != null; // && request.getParameter("ignoreSession") != null;
                                if (inSession) {
                                    accessToken = (AccessToken) session.getAttribute("accessToken");
                                }
                                else {
                                    String verifier = request.getParameter("oauth_verifier");
                                    if (verifier != null) {
                                        accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                                    }
                                    else {
                                        accessToken = twitter.getOAuthAccessToken(requestToken);
                                    }
                                    session.setAttribute("accessToken", accessToken);
                                }
                                out.write(inSession ? "From session:<br />" : "Live:<br />");
                                out.write("Access token key: " + accessToken.getToken() + "<br />");
                                out.write("Access token secret: " + accessToken.getTokenSecret() + "<br />");
                            }
                            catch (TwitterException ex) {
                                out.write("<br /><span style='color:red'>");
                                out.write("Attempt to get the access token failed! Message: '" + ex.getMessage() + "'");
                                out.write("</span><br />");
                                out.write("Authorization URL: <a href='" + requestToken.getAuthorizationURL() + "'>" + requestToken.getAuthorizationURL() + "</a><br />");
                                issue = ex;
                            }
                        }
                        out.write("<br />");
                        if (issue == null) {
                            try {
                                // 3. Get the user information
                                twitter.setOAuthAccessToken(accessToken);
                                out.write("Account id: " + accessToken.getUserId() + " / " + twitter.getId() + "<br />");
                                twitter4j.User twetailer = twitter.showUser("twetailer");
                                out.write("Status: <span style='color:green;'>" + twetailer.getStatus().getText() + "</span<<br />");
                                out.write("Followers #: " + twetailer.getFollowersCount() + " -- ");
                                out.write("Friends #: " + twetailer.getFriendsCount() + " -- ");
                                out.write("DMs #: " + twitter.getDirectMessages().size() + "<br />");
                                out.write("Last DM: <span style='color:green;'>" + twitter.getDirectMessages().get(0).getText() + "</span><br />");
                            }
                            catch (TwitterException ex) {
                                out.write("<br /><span style='color:red'>");
                                out.write("Attempt to get data for the authorized application failed! Message: '" + ex.getMessage() + "'");
                                out.write("</span><br />");
                                issue = ex;
                            }
                        }
                    }
                    catch (Exception ex) {
                        out.write("<br /><span style='color:red'>");
                        out.write("Unexpected error while dealing with Twitter! Message: '" + ex.getMessage() + "'");
                        out.write("</span><br />");
                        issue = ex;
                    }
                }
                else {
                    try {
                        out.write("Acces Key: " + TwitterConnector.TWETAILER_TWITTER_ACCESS_KEY + "<br />");
                        out.write("Access Secret: " + TwitterConnector.TWETAILER_TWITTER_ACCESS_SECRET + "<br />");
                        out.write("<br />");
                        // 1. Build the Twitter accessor
                        Twitter twitter = new TwitterFactory().getInstance(
                                new OAuthAuthorization(
                                        ConfigurationContext.getInstance(), //Configuration conf,
                                        TwitterConnector.TWETAILER_TWITTER_CONSUMER_KEY,
                                        TwitterConnector.TWETAILER_TWITTER_CONSUMER_SECRET,
                                        new AccessToken(
                                                TwitterConnector.TWETAILER_TWITTER_ACCESS_KEY,
                                                TwitterConnector.TWETAILER_TWITTER_ACCESS_SECRET
                                        )
                                )
                        );
                        try {
                            // 2. Get the user information
                            out.write("Account id: " + twitter.getId() + "<br />");
                            twitter4j.User twetailer = twitter.showUser("twetailer");
                            out.write("Status: <span style='color:green;'>" + twetailer.getStatus().getText() + "</span><br />");
                            out.write("Followers #: " + twetailer.getFollowersCount() + " -- ");
                            out.write("Friends #: " + twetailer.getFriendsCount() + " -- ");
                            out.write("DMs #: " + twitter.getDirectMessages().size() + "<br />");
                            out.write("Last DM: <span style='color:green;'>" + twitter.getDirectMessages().get(0).getText() + "</span><br />");
                        }
                        catch (TwitterException ex) {
                            out.write("<br /><span style='color:red'>");
                            out.write("Attempt to get data for the authorized application failed! Message: '" + ex.getMessage() + "'");
                            out.write("</span><br />");
                            issue = ex;
                        }
                    }
                    catch (Exception ex) {
                        out.write("<br /><span style='color:red'>");
                        out.write("Unexpected error while dealing with Twitter! Message: '" + ex.getMessage() + "'");
                        out.write("</span><br />");
                        issue = ex;
                    }
                } %>
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
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        // dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dojo.parser");
        dojo.require("dijit.Dialog");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.ContentPane");
        // dojo.require("dijit.layout.TabContainer");
        // dojo.require("dijit.form.Form");
        dojo.require("dijit.form.Button");
        // dojo.require("dijit.form.CheckBox");
        // dojo.require("dijit.form.ComboBox");
        // dojo.require("dijit.form.DateTextBox");
        // dojo.require("dijit.form.FilteringSelect");
        // dojo.require("dijit.form.NumberTextBox");
        // dojo.require("dijit.form.Textarea");
        dojo.require("dijit.form.TextBox");
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            dojo.fadeOut({
                node: "introFlash",
                delay: 50,
                onEnd: function() {
                    dojo.style("introFlash", "display", "none");
                }
            }).play();
        });
    });
    </script>

    <script type="text/javascript">
    var localModule = new Object();
    </script>

    <% if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName())) { %><script type="text/javascript">
    var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-11910037-2']);
    _gaq.push(['_trackPageview']);
    (function() {
        var ga = document.createElement('script');
        ga.type = 'text/javascript';
        ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(ga, s);
    })();
    </script><% } %>
</body>
</html>
