<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html>
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "ui_application_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="shortcut icon" href="/images/logo/favicon.ico" />
    <link rel="icon" href="/images/logo/favicon.ico" type="image/x-icon"/>
    <%
    if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><style type="text/css">
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/tundra/tundra.css";
        @import "/css/console.css";
    </style><%
    } // endif (useCDN)
    %>
</head>
<body class="tundra">

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "ui_splash_screen_message", locale) %></span></div>
    </div>

    <%
    if (useCDN) {
    %><script
        djConfig="parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/sep/blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    }
    else { // elif (!useCDN)
    %><script
        djConfig="parseOnLoad: false, isDebug: false, baseUrl: '/js/dojo/dojo/', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/sep/blank.html'"
        src="/js/dojo/dojo/dojo.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>
    <script type="text/javascript">
    dojo.require("dijit.Dialog");
    dojo.require("dijit.layout.BorderContainer");
    dojo.require("dijit.layout.ContentPane");
    dojo.require("dijit.form.Form");
    dojo.require("dijit.form.Button");
    dojo.require("dijit.form.TextBox");
    dojo.require("twetailer.Console");
    dojo.require("dojo.parser");
    dojo.addOnLoad(function(){
        dojo.parser.parse();
        var userLocale = "<%= localeId %>";
        twetailer.Console.init(userLocale, true);
        dojo.fadeOut({
            node: "introFlash",
            delay: 50,
            onEnd: function() {
                dojo.style("introFlash", "display", "none");
            }
        }).play();
        loginLogic.init();
    });
    </script>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <div dojoType="dijit.layout.ContentPane" id="headerZone" region="top">
            <div id="brand">
                <h1>
                    <img
                        alt="<%= LabelExtractor.get("product_ascii_logo", locale) %>"
                        id="logo"
                        src="/images/logo/twitter-bird-and-cart-toLeft.png"
                        title="<%= LabelExtractor.get("product_name", locale) %> <%= LabelExtractor.get("product_ascii_logo", locale) %>"
                    />
                    <a
                        href="http://www.twetailer.com/"
                        title="<%= LabelExtractor.get("product_name", locale) %> <%= LabelExtractor.get("product_ascii_logo", locale) %>"
                    ><span class="bang">!</span><span class="tw">tw</span><span class="etailer">etailer</span></a>
                </h1>
                <span id="mantra"><%= LabelExtractor.get("product_mantra", locale) %></span>
            </div>
            <div id="navigation">
                <ul>
                    <!--  Normal order because they are left aligned -->
                    <li><a name="justForStyle1"><%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer", locale) %></a></li>
                    <li><a name="justForStyle2"><%= LabelExtractor.get(ResourceFileId.third, "navigation_sale_associate", locale) %></a></li>
                    <!--  Reverse order because they are right aligned -->
                    <li class="subItem"><a href="#" onclick="dijit.byId('aboutPopup').show();" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %></a></li>
                    <li class="subItem">
                        <input id="languageSelector" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_language_selector", locale) %>" />
                        <script type="text/javascript">
                        dojo.require("domderrien.i18n.LanguageSelector");
                        dojo.addOnLoad(function() { domderrien.i18n.LanguageSelector.createSelector("languageSelector", null, [<%
                            ResourceBundle languageList = LocaleController.getLanguageListRB();
                            Enumeration<String> keys = languageList.getKeys();
                            while(keys.hasMoreElements()) {
                                String key = keys.nextElement();
                                %>{value:"<%= key %>",label:"<%= languageList.getString(key) %>"}<%
                                if (keys.hasMoreElements()) {
                                    %>,<%
                                }
                            }
                            %>], "<%= localeId %>", "globalCommand", null)});
                        </script>
                    </li>
                </ul>
            </div>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="centerZone" region="center">
            <table style="width: 100%; height: 100%;">
                <tr>
                    <td>&nbsp;</td>
                    <td valign="middle" style="width: 40em;">
                        <div id="signInForm">
                            <div style="color:#888; text-align: justify;">
                                 <%= LabelExtractor.get(ResourceFileId.third, "login_introduction_message", locale) %>
                            </div>
                            <br/>
                            <form action="/control/login" dojoType="dijit.form.Form" method="post" onsubmit="dijit.byId('signInButton').attr('disabled', true);">
                                <label for="openid_identifier"><%= LabelExtractor.get(ResourceFileId.third, "login_open_id_label", locale) %></label><br/>
                                <center><input dojoType="dijit.form.TextBox" id="openid_identifier" name="openid_identifier" style="width:30em;" type="text" /></center>
                                <center><button dojoType="dijit.form.Button" id="signInButton" type="submit" iconClass="openidSignInButton"><%= LabelExtractor.get(ResourceFileId.third, "login_sign_in_button", locale) %></button></center>
                            </form>
                            <br/>
                            <%= LabelExtractor.get(ResourceFileId.third, "login_provider_list_message", locale) %>
                            <br/>
                            <div id="openIdProviderList">
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="window.location='/control/login?loginWith=google'"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_google", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/google.ico" width="16" height="16" /> </button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="window.location='/control/login?loginWith=yahoo'"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_yahoo", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/yahoo.ico" width="16" height="16" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="loginLogic.cookOpenId('http://www.myspace.com/', '');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_myspace", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/myspace.ico" width="16" height="16" /> </button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="loginLogic.cookOpenId('http://openid.aol.com/', '');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_aol", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/aol.ico" width="16" height="16" /> </button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="loginLogic.cookOpenId('http://', '.wordpress.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_wordpress", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/wordpress.ico" width="16" height="16" /> </button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="loginLogic.cookOpenId('http://', '.blogspot.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_blogger", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/blogger.ico" width="16" height="16" /> </button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="loginLogic.cookOpenId('http://', '.mp');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_chimp", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/chimp.gif" width="16" height="16" /> </button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="loginLogic.cookOpenId('http://', '.myopenid.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_myopenid", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/myopenid.ico" width="16" height="16" /> </button>
                            </div>
                        </div>
                    </td>
                    <td>&nbsp;</td>
                </tr>
            </table>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_copyright", locale) %>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="aboutPopup"
        title="About"
        _href="about.jsp"
    >
        <%= LabelExtractor.get(ResourceFileId.third, "about_text", locale) %>
        --<br/>
        <img
            alt="<%= LabelExtractor.get(ResourceFileId.third, "about_powered_by_appengine", locale) %>"
            height="30"
            src="http://code.google.com/appengine/images/appengine-noborder-120x30.gif"
            width="120"
        />
    </div>

    <div
        dojoType="dijit.Dialog"
        id="openIdResolver"
        title="Additional information required"
        execute="loginLogic.reportCookedOpenId();"
    >
        <%= LabelExtractor.get(ResourceFileId.third, "login_dialog_custom_info_prefix", locale) %>
        <ul>
            <li>
                <span id="openIdPrefix"></span>
                <input dojoType="dijit.form.TextBox" id="openIdCustom" >
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
    var loginLogic = {};
    loginLogic.init = function() {
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
    };
    loginLogic.cookOpenId = function(prefix, suffix) {
        dojo.byId('openIdPrefix').innerHTML = prefix;
        dojo.byId('openIdSuffix').innerHTML = suffix;
        dijit.byId('openIdResolver').show();
        dijit.byId('openIdCustom').focus();
    };
    loginLogic.reportCookedOpenId = function() {
        var prefix = dojo.byId('openIdPrefix').innerHTML;
        var custom = dijit.byId('openIdCustom').attr("value");
        var suffix = dojo.byId('openIdSuffix').innerHTML;
        dijit.byId("openid_identifier").attr("value", prefix + custom + suffix);
        dojo.byId("signInButton").click();
    };
    </script>

    <script type="text/javascript">
    var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-11910037-2']);
    _gaq.push(['_trackPageview']);
    (function() {
        var ga = document.createElement('script');
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        ga.setAttribute('async', 'true');
        document.documentElement.firstChild.appendChild(ga);
    })();
    </script>
</body>
</html>
