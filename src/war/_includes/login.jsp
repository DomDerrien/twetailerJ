<!doctype html>
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.List"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.i18n.LocaleController"
    import="domderrien.jsontools.JsonArray"
    import="domderrien.jsontools.JsonObject"
    import="domderrien.jsontools.JsonParser"
    import="twetailer.dao.BaseOperations"
    import="twetailer.dao.SettingsOperations"
    import="twetailer.dto.HashTag"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.task.step.BaseSteps"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Detects the vertical context
    boolean useVertical = false;
    String verticalId = null;
    String forwardedUriAttribute = (String) request.getAttribute("javax.servlet.forward.servlet_path");
    if (forwardedUriAttribute != null) {
        // Extract the hash tag
        if (forwardedUriAttribute.startsWith("/console/")) {
            verticalId = forwardedUriAttribute.substring("/console/".length());
            if (verticalId.indexOf('/') != -1) {
                verticalId = verticalId.substring(0, verticalId.indexOf('/'));
            }
            useVertical = HashTag.isSupportedHashTag(verticalId);
        }
    }
    if (!useVertical) {
        verticalId = "";
    }
%><html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="<%= localeId %>">
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "ui_application_name", locale) %></title>
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta http-equiv="cache-control" content="no-cache" />
    <meta http-equiv="pragma" content="no-cache" />
    <meta http-equiv="expires" content="0" />
    <meta name="description" content="<%= LabelExtractor.get(ResourceFileId.third, "login_localized_page_description", locale) %>" />
    <meta name="keywords" content="<%= LabelExtractor.get(ResourceFileId.third, "login_localized_page_keywords", locale) %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.third, "product_copyright", locale) %>" />
    <meta name="google-site-verification" content="WY7P9S7-YK1ZBPjxlVz1h7kd0Ex1Sc74hcab8zXy1d4" />
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
        @import "/js/dojo/dijit/themes/tundra/tundra.css";><%
        } // endif (useCDN)
        %>
        @import "/css/console.css";<%
        if (useVertical) {
        %>
        @import "/css/<%= verticalId %>/console.css";<%
        } // endif (useVertical)
        %>

    .dijitButtonText>img {
        width:32px;
        height:32px;
    }
    </style>
</head>
<body class="tundra">

    <div id="topBar"></div>

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>

    <%
    if (useCDN) {
    %><script type="text/javascript">
    var djConfig = {
        parseOnLoad: false,
        isDebug: false,
        useXDomain: true,
        baseUrl: './',
        modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' },
        dojoBlankHtmlUrl: '/blank.html',
        locale: '<%= localeId %>'
    };
    </script>
    <script src="<%= cdnBaseURL %>/dojo/dojo.xd.js" type="text/javascript"></script><%
    }
    else { // elif (!useCDN)
    %><script type="text/javascript">
    var djConfig = {
        parseOnLoad: false,
        isDebug: false,
        useXDomain: true,
        baseUrl: '/js/dojo/dojo/',
        modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' },
        dojoBlankHtmlUrl: '/blank.html',
        locale: '<%= localeId %>'
    };
    </script>
    <script src="/js/dojo/dojo/dojo.js" type="text/javascript"></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/_includes/banner_open.jsp">
            <jsp:param name="verticalId" value="<%= verticalId %>" />
            <jsp:param name="localeId" value="<%= localeId %>" />
        </jsp:include>
        <div dojoType="dijit.layout.ContentPane" id="centerZone" region="center" class="loginBG">
            <table style="width: 100%; height: 100%; background-color: transparent;">
                <tr>
                    <td>&nbsp;</td>
                    <td valign="middle" style="width: 40em;">
                        <!--[if lt IE 7]>
                        <div style='border: 1px solid #F7941D; background: #FEEFDA; text-align: center; clear: both; height: 75px; position: relative;'>
                            <div style='width: 640px; margin: 0 auto; text-align: left; padding: 0; overflow: hidden; color: black;'>
                                <div style='width: 75px; float: left;'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-warning.jpg' alt='!'/></div>
                                <div style='width: 275px; float: left; font-family: Arial, sans-serif;'>
                                    <div style='font-size: 14px; font-weight: bold; margin-top: 12px;'><%= LabelExtractor.get(ResourceFileId.third, "login_call_to_ie6_users", locale) %></div>
                                    <div style='font-size: 12px; margin-top: 6px; line-height: 12px;'><%= LabelExtractor.get(ResourceFileId.third, "login_info_to_ie6_users", locale) %></div>
                                </div>
                                <div style='width: 75px; float: left;'><a href='http://www.firefox.com' target='_blank'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-firefox.jpg' style='border: none;' alt='Mozilla Firefox'/></a></div>
                                <div style='width: 75px; float: left;'><a href='http://www.microsoft.com/windows/internet-explorer/' target='_blank'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-ie8.jpg' style='border: none;' alt='Microsoft Internet Explorer'/></a></div>
                                <div style='width: 73px; float: left;'><a href='http://www.apple.com/safari/download/' target='_blank'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-safari.jpg' style='border: none;' alt='Apple Safari'/></a></div>
                                <div style='float: left;'><a href='http://www.google.com/chrome' target='_blank'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-chrome.jpg' style='border: none;' alt='Google Chrome'/></a></div>
                            </div>
                        </div>
                        <![endif]-->

                        <![if !IE]>
                        <div id="signInForm">
                            <div style="color:#888; text-align: justify;">
                                 <%= LabelExtractor.get(ResourceFileId.third, "login_introduction_message", locale) %>
                            </div>
                            <br/>
                            <%= LabelExtractor.get(ResourceFileId.third, "login_provider_list_message", locale) %>
                            <br/>
                            <div id="openIdProviderList">
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="window.location='/login?loginWith=google&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + dojo.byId('fromPageURL').value"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_google", locale) %>"
                                ><img src="/images/icons/Google-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="window.location='/login?loginWith=yahoo&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + dojo.byId('fromPageURL').value"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_yahoo", locale) %>"
                                ><img src="/images/icons/Yahoo-red-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://www.myspace.com/', '');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_myspace", locale) %>"
                                ><img src="/images/icons/MySpace-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://openid.aol.com/', '');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_aol", locale) %>"
                                ><img src="/images/icons/Aol-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://', '.wordpress.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_wordpress", locale) %>"
                                ><img src="/images/icons/Wordpress-blue-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://', '.blogspot.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_blogger", locale) %>"
                                ><img src="/images/icons/Blogger-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://', '.mp');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_chimp", locale) %>"
                                ><img src="/images/icons/Chimp-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://', '.myopenid.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_myopenid", locale) %>"
                                ><img src="/images/icons/MyOpenId-32.png" width="32" height="32" /></button>
                            </div>
                            <br/>
                            <form action="/login" dojoType="dijit.form.Form" method="post" onsubmit="dijit.byId('signInButton').attr('disabled', true);">
                                <input id="fromPageURL" name="<%= LoginServlet.FROM_PAGE_URL_KEY %>" type="hidden" />
                                <label for="openid_identifier"><%= LabelExtractor.get(ResourceFileId.third, "login_open_id_label", locale) %></label><br/>
                                <center><input dojoType="dijit.form.TextBox" id="openid_identifier" name="openid_identifier" style="width:30em;font-size:larger" type="text" /></center>
                                <center><button dojoType="dijit.form.Button" id="signInButton" type="submit" iconClass="openidSignInButton"><%= LabelExtractor.get(ResourceFileId.third, "login_sign_in_button", locale) %></button></center>
                            </form>
                        </div>
                        <![endif]>
                    </td>
                    <td>&nbsp;</td>
                </tr>
            </table>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="aboutPopup"
        title="<%= LabelExtractor.get(ResourceFileId.third, "about_dialog_title", locale) %>"
        href="/_includes/about.jsp"
    >
    </div>

    <div
        dojoType="dijit.Dialog"
        id="openIdResolver"
        title="Additional information required"
        execute="localModule.reportCookedOpenId();"
    >
        <%= LabelExtractor.get(ResourceFileId.third, "login_dialog_custom_info_prefix", locale) %>
        <ul>
            <li>
                <span id="openIdPrefix"></span>
                <input dojoType="dijit.form.TextBox" id="openIdCustom" />
                <span id="openIdSuffix"></span>
            </li>
        </ul>
        <%= LabelExtractor.get(ResourceFileId.third, "login_dialog_custom_info_suffix", locale) %>
        <center>
            <button dojoType="dijit.form.Button" id="useAdditionalInfoButton" type="submit" iconClass="openidSignInButton"><%= LabelExtractor.get(ResourceFileId.third, "login_sign_in_button", locale) %></button>
            <button dojoType="dijit.form.Button" type="reset" iconClass="silkIcon silkIconCancel" onclick="dijit.byId('openIdResolver').hide();dijit.byId('openid_identifier').focus();"><%= LabelExtractor.get(ResourceFileId.third, "login_dialog_close_button", locale) %></button>
        </center>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dojo.parser");
        dojo.require("dijit.Dialog");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.form.Form");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.ContentPane");
        dojo.require("dojox.analytics.Urchin");
        dojo.addOnLoad(function(){
            if (1280 < parseInt(dojo.style("centerZone", "width"))) {
                var currentBgImg = dojo.style("centerZone", "backgroundImage");
                if (currentBgImg != null && 15 < currentBgImg.length) {
                    var suffix = currentBgImg.substr(currentBgImg.length - 15);
                    if (suffix.indexOf("-1024.png") != -1) {
                        dojo.style("centerZone", "backgroundImage", currentBgImg.replace("-1024.png", "-2048.png"));
                    }
                }
            }
            dojo.parser.parse();
            dojo.fadeOut({
                node: "introFlash",
                delay: 50,
                onEnd: function() {
                    dojo.style("introFlash", "display", "none");
                }
            }).play();
            localModule.init();<%
            if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName())) { %>
            new dojox.analytics.Urchin({ acct: 'UA-11910037-2' });<%
            } %>
        });
    });
    </script>

    <script type="text/javascript">
    var localModule = {};
    localModule.init = function() {
        dijit.byId("openid_identifier").focus();
        dojo.query("#signInButton").onclick(function(evt) {
            dojo.query(".shortcutButton").forEach(function(node, index, arr){
                // dojo.fadeOut({ node: dijit.getEnclosingWidget(node).domNode, duration: 2000 }).play();
                dijit.getEnclosingWidget(node).attr("disabled", true);
            });
        });
        dojo.query("#openIdCustom").onkeypress(function(evt) {
            if (evt.keyCode == dojo.keys.ENTER) {
                dojo.byId("useAdditionalInfoButton").click();
            }
        });
        dojo.byId("fromPageURL").value = encodeURI(window.location);
        dijit.byId("signInButton").attr("disabled", false);
    };
    localModule.cookOpenId = function(prefix, suffix) {
        dojo.byId('openIdPrefix').innerHTML = prefix;
        dojo.byId('openIdSuffix').innerHTML = suffix;
        dijit.byId('openIdResolver').show();
        dijit.byId('openIdCustom').focus();
    };
    localModule.reportCookedOpenId = function() {
        var prefix = dojo.byId('openIdPrefix').innerHTML;
        var custom = dijit.byId('openIdCustom').attr("value");
        var suffix = dojo.byId('openIdSuffix').innerHTML;
        dijit.byId("openid_identifier").attr("value", prefix + custom + suffix);
        dojo.byId("signInButton").click();
    };
    </script>
</body>
</html>
