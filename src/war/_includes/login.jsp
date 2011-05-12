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
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String appVersion = appSettings.getProductVersion();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    cdnBaseURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.6"; // TODO: change at the application level

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

    boolean onLocalHost = "localhost".equals(request.getServerName()) || "127.0.0.1".equals(request.getServerName()) || "10.0.2.2".equals(request.getServerName());

    String liveAppId = new WindowsLiveLogin("liveIdKeys-" + (onLocalHost ? "localhost" : appSettings.getAppEngineId()) + ".xml").getAppId();
    String liveControlUrl = (onLocalHost ? "http://" : "https://") + "login.live.com/controls/WebAuthButton.htm";
    String liveBaseUrl = liveControlUrl + "?appid=" + liveAppId + "&context=/_wll/liveWebAuthHandler";

%><html dir="ltr" lang="<%= localeId %>">
<head>
    <meta http-equiv="X-UA-Compatible" content="chrome=1" />
    <title><%= LabelExtractor.get(ResourceFileId.third, "ui_application_name", locale) %></title>
    <meta http-equiv="content-type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>" />
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
    <style type="text/css"><%
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
<body class="claro">

    <div id="topBar"></div>

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
        src="/js/release/<%= appVersion %>/ase/login.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" data-dojo-type="dijit.layout.BorderContainer" data-dojo-props="gutters: false" style="height: 100%;">
        <jsp:include page="/_includes/banner_open.jsp">
            <jsp:param name="verticalId" value="<%= verticalId %>" />
            <jsp:param name="localeId" value="<%= localeId %>" />
        </jsp:include>
        <div data-dojo-type="dijit.layout.ContentPane" id="centerZone" data-dojo-props="region: 'center'" class="loginBG">
            <table style="width: 100%; height: 100%; background-color: transparent;">
                <tr>
                    <td>&nbsp;</td>
                    <td valign="middle" style="width: 40em;">
                        <div id="signInForm">
                            <!--[if lt IE 8]>
                            <div id="incompatibleIEWarning" style='border: 1px solid #F7941D; background: #FEEFDA; text-align: center; clear: both; height: 75px; position: relative; margin-bottom: 10px;'>
                                <div style='width: 640px; margin: 0 auto; text-align: left; padding: 0; overflow: hidden; color: black;'>
                                    <div style='width: 75px; float: left; padding-left: 10px;'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-warning.jpg' alt='!'/></div>
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
                            <div style="color:#888; text-align: justify;">
                                 <%= LabelExtractor.get(ResourceFileId.third, "login_introduction_message", locale) %>
                            </div>
                            <br/>
                            <%= LabelExtractor.get(ResourceFileId.third, "login_provider_list_message", locale) %>
                            <br/>
                            <div class="openIdProviderList">
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { window.location='<%= FacebookConnector.bootstrapAuthUrl(request) %>'+escape(window.location); }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_facebook", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/FaceBook-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="disabled: true, onClick: function() { window.location='/login?loginWith=twitter&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + dojo.byId('fromPageURL').value; }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_twitter", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/Twitter-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { window.location='/login?loginWith=google&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + dojo.byId('fromPageURL').value; }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_google", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/Google-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { window.location='/login?loginWith=yahoo&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + dojo.byId('fromPageURL').value; }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_yahoo", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/Yahoo-red-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { localModule.cookOpenId('http://openid.aol.com/', ''); }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_aol", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/Aol-32.png" width="32" height="32" /></button>
                                <iframe
                                    src="<%= liveBaseUrl %>&style=font-size%3A+q0pt%3B+font-family%3A+verdana%3B+background%3A+transparent%3B"
                                    style="margin: 0; padding: 0; border: 0 none; height: 33px; width: 76px; vertical-align: middle;"
                                    scrolling="no"
                                    seamless="seamless">
                                </iframe>
                            </div>
                            <div style="text-align: right;">
                                <a
                                    href="#"
                                    id="moreProvider"
                                    onclick="dojo.byId('moreProvider').style.display = 'none'; dojo.byId('loginForm').style.display = ''; return false;"
                                >
                                    <%= LabelExtractor.get(ResourceFileId.third, "login_provider_more", locale) %>
                                </a>
                            </div>
                            <div class="openIdProviderList" id="loginForm" style="display: none;">
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { localModule.cookOpenId('http://', '.wordpress.com'); }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_wordpress", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/Wordpress-blue-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { localModule.cookOpenId('http://', '.blogspot.com'); }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_blogger", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/Blogger-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { localModule.cookOpenId('http://www.myspace.com/', ''); }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_myspace", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/MySpace-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { localModule.cookOpenId('http://', '.mp'); }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_chimp", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/Chimp-32.png" width="32" height="32" /></button>
                                <button
                                    class="shortcutButton"
                                    data-dojo-type="dijit.form.Button"
                                    data-dojo-props="onClick: function() { localModule.cookOpenId('http://', '.myopenid.com'); }, title: '<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_myopenid", locale) %>'"
                                    type="button"
                                ><img src="/images/icons/MyOpenId-32.png" width="32" height="32" /></button>
                                <form action="/login" data-dojo-type="dijit.form.Form" method="post" data-dojo-props="onSubmit: function() { dijit.byId('signInButton').set('disabled', true); }">
                                    <input id="fromPageURL" name="<%= LoginServlet.FROM_PAGE_URL_KEY %>" type="hidden" />
                                    <label for="openid_identifier"><%= LabelExtractor.get(ResourceFileId.third, "login_open_id_label", locale) %></label><br/>
                                    <div style="text-align: center"><input data-dojo-type="dijit.form.TextBox" id="openid_identifier" data-dojo-props="name: 'openid_identifier'" style="width:30em;font-size:larger" type="text" /></div>
                                    <div style="text-align: center"><button data-dojo-type="dijit.form.Button" data-dojo-props="iconClass: 'openidSignInButton', type: 'submit'" id="signInButton"><%= LabelExtractor.get(ResourceFileId.third, "login_sign_in_button", locale) %></button></div>
                                </form>
                            </div>
                        </div>
                    </td>
                    <td>&nbsp;</td>
                </tr>
            </table>
        </div>
        <div data-dojo-type="dijit.layout.ContentPane" id="footerZone" data-dojo-props="region: 'bottom'">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <div
        data-dojo-type="dijit.Dialog"
        id="aboutPopup"
        title="<%= LabelExtractor.get(ResourceFileId.third, "about_dialog_title", locale) %>"
        data-dojo-props="href: '/_includes/about.jsp'"
    >
    </div>

    <div
        data-dojo-type="dijit.Dialog"
        id="openIdResolver"
        title="Additional information required"
        data-dojo-type="execute: localModule.reportCookedOpenId"
    >
        <%= LabelExtractor.get(ResourceFileId.third, "login_dialog_custom_info_prefix", locale) %>
        <ul>
            <li>
                <span id="openIdPrefix"></span>
                <input data-dojo-type="dijit.form.TextBox" id="openIdCustom" />
                <span id="openIdSuffix"></span>
            </li>
        </ul>
        <%= LabelExtractor.get(ResourceFileId.third, "login_dialog_custom_info_suffix", locale) %>
        <div style="text-align: center">
            <button data-dojo-type="dijit.form.Button" data-dojo-props="iconClass: 'openidSignInButton', type: 'submit'" id="useAdditionalInfoButton"><%= LabelExtractor.get(ResourceFileId.third, "login_sign_in_button", locale) %></button>
            <button data-dojo-type="dijit.form.Button" data-dojo-props="iconClass: 'silkIcon silkIconCancel', onClick: function() { dijit.byId('openIdResolver').hide(); dijit.byId('openid_identifier').focus(); }, type: 'reset'"><%= LabelExtractor.get(ResourceFileId.third, "login_dialog_close_button", locale) %></button>
        </div>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dojo.parser');
        dojo.require('dijit.Dialog');
        dojo.require('dijit.form.Button');
        dojo.require('dijit.form.Form');
        dojo.require('dijit.form.TextBox');
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        dojo.require('dojox.analytics.Urchin');
        dojo.addOnLoad(function(){
            if (1280 < parseInt(dojo.style('centerZone', 'width'))) {
                var currentBgImg = dojo.style('centerZone', 'backgroundImage');
                if (currentBgImg != null && 15 < currentBgImg.length) {
                    var suffix = currentBgImg.substr(currentBgImg.length - 15);
                    if (suffix.indexOf('-1024.png') != -1) {
                        dojo.style('centerZone', 'backgroundImage', currentBgImg.replace('-1024.png', '-2048.png'));
                    }
                }
            }
            dojo.parser.parse();
            dojo.fadeOut({
                node: 'introFlash',
                delay: 50,
                onEnd: function() {
                    dojo.style('introFlash', 'display', 'none');
                }
            }).play();
            localModule.init();<%
            if (!onLocalHost) { %>
            new dojox.analytics.Urchin({ acct: 'UA-11910037-2' });<%
            } %>
        });
    });

    var localModule = {};
    localModule.init = function() {
        var warningBox = dojo.byId('incompatibleIEWarning');
        if (warningBox != null) {
            dojo.query('.shortcutButton').forEach(function(node, index, arr) {
                var widget = dijit.getEnclosingWidget(node)
                widget.set('disabled', true);
                widget.set('title', '');
            });
            dijit.byId('signInButton').set('disabled', true);
        }
        else {
            dijit.byId('openid_identifier').focus();
            dojo.query('#signInButton').onclick(function(evt) {
                dojo.query('.shortcutButton').forEach(function(node, index, arr) {
                    // dojo.fadeOut({ node: dijit.getEnclosingWidget(node).domNode, duration: 2000 }).play();
                    dijit.getEnclosingWidget(node).set('disabled', true);
                });
            });
            dojo.query('#openIdCustom').onkeypress(function(evt) {
                if (evt.keyCode == dojo.keys.ENTER) {
                    dojo.byId('useAdditionalInfoButton').click();
                }
            });
            dojo.byId('fromPageURL').value = escape(window.location); // encodeURI(window.location);
            dijit.byId('signInButton').set('disabled', false);
        }
    };
    localModule.cookOpenId = function(prefix, suffix) {
        dojo.byId('openIdPrefix').innerHTML = prefix;
        dojo.byId('openIdSuffix').innerHTML = suffix;
        dijit.byId('openIdResolver').show();
        dijit.byId('openIdCustom').focus();
    };
    localModule.reportCookedOpenId = function() {
        var prefix = dojo.byId('openIdPrefix').innerHTML;
        var custom = dijit.byId('openIdCustom').get('value');
        var suffix = dojo.byId('openIdSuffix').innerHTML;
        dijit.byId('openid_identifier').set('value', prefix + custom + suffix);
        dojo.byId('signInButton').click();
    };
    </script>
</body>
</html>
