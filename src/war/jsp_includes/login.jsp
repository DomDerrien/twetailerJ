<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
    import="domderrien.jsontools.JsonArray"
    import="domderrien.jsontools.JsonObject"
    import="domderrien.jsontools.JsonParser"
    import="twetailer.dao.BaseOperations"
    import="twetailer.dao.SettingsOperations"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.validator.ApplicationSettings"
%><%!
    protected BaseOperations _baseOperations = new BaseOperations();
    protected SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Try to get the seed city list
    String seedCityList = (String) settingsOperations.getFromCache("/suppliesTagCloud/seedCityList");
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
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
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
    <style type="text/css">
    .dijitButtonText>img {
        width:24px;
        height:24px;
    }
    </style>
</head>
<body class="tundra">

    <div id="topBar"></div>

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "ui_splash_screen_message", locale) %></span></div>
    </div>

    <%
    if (useCDN) {
    %><script
        djConfig="parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    }
    else { // elif (!useCDN)
    %><script
        djConfig="parseOnLoad: false, isDebug: false, baseUrl: '/js/dojo/dojo/', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="/js/dojo/dojo/dojo.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">

        <jsp:include page="/jsp_includes/banner_open.jsp"></jsp:include>
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
                            <%= LabelExtractor.get(ResourceFileId.third, "login_provider_list_message", locale) %>
                            <br/>
                            <div id="openIdProviderList">
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="window.location='/login?loginWith=google&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + dojo.byId('fromPageURL').value"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_google", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/google.ico" width="16" height="16" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="window.location='/login?loginWith=yahoo&<%= LoginServlet.FROM_PAGE_URL_KEY %>=' + dojo.byId('fromPageURL').value"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_yahoo", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/yahoo.ico" width="16" height="16" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://www.myspace.com/', '');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_myspace", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/myspace.ico" width="16" height="16" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://openid.aol.com/', '');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_aol", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/aol.ico" width="16" height="16" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://', '.wordpress.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_wordpress", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/wordpress.ico" width="16" height="16" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://', '.blogspot.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_blogger", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/blogger.ico" width="16" height="16" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://', '.mp');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_chimp", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/chimp.gif" width="16" height="16" /></button>
                                <button
                                    class="shortcutButton"
                                    dojoType="dijit.form.Button"
                                    onclick="localModule.cookOpenId('http://', '.myopenid.com');"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "login_provider_shortcut_myopenid", locale) %>"
                                ><img src="http://domderrien.github.com/images/icons/myopenid.ico" width="16" height="16" /></button>
                            </div>
                            <br/>
                            <form action="/login" dojoType="dijit.form.Form" method="post" onsubmit="dijit.byId('signInButton').attr('disabled', true);">
                                <input id="fromPageURL" name="<%= LoginServlet.FROM_PAGE_URL_KEY %>" type="hidden" />
                                <label for="openid_identifier"><%= LabelExtractor.get(ResourceFileId.third, "login_open_id_label", locale) %></label><br/>
                                <center><input dojoType="dijit.form.TextBox" id="openid_identifier" name="openid_identifier" style="width:30em;font-size:larger" type="text" /></center>
                                <center><button dojoType="dijit.form.Button" id="signInButton" type="submit" iconClass="openidSignInButton"><%= LabelExtractor.get(ResourceFileId.third, "login_sign_in_button", locale) %></button></center>
                            </form>
                        </div>
                    </td>
                    <td>&nbsp;</td>
                </tr>
            </table>
        </div>
        <%
        if (seedCityList != null && 0 < seedCityList.length()) {
            JsonArray citiesNearby = new JsonParser(seedCityList).getJsonArray();
        %><div
            dojoType="dijit.layout.ContentPane"
            region="right"
            style="width:240px;margin:10px;background-color:lightgrey;"
            title="<%= LabelExtractor.get(ResourceFileId.third, "login_other_locations_title", locale) %>"
        >
            <div style="color:white;background-color:black;font-weight:bold;padding:2px 5px;">
                <%= LabelExtractor.get(ResourceFileId.third, "login_other_locations_title", locale) %>
            </div>
            <div style="border:1px lightgrey solid;padding:10px;">
                <%= LabelExtractor.get(ResourceFileId.third, "login_other_locations_introduction", locale) %>
                <ul>
                    <% for(int cityIdx=0; cityIdx < citiesNearby.size(); cityIdx ++) {
                        JsonObject city = citiesNearby.getJsonObject(cityIdx);
                    %><li><a href="/directory<%= city.getString("key") + (request.getQueryString() == null ? "" : "?" + request.getQueryString()) %>"><%= city.getString("label") %></a></li><% } %>
                </ul>
            </div>
        </div>
        <% } %><div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_copyright", locale) %>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="aboutPopup"
        title="<%= LabelExtractor.get(ResourceFileId.third, "about_dialog_title", locale) %>"
        href="/jsp_includes/about.jsp"
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
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            dojo.fadeOut({
                node: "introFlash",
                delay: 50,
                onEnd: function() {
                    dojo.style("introFlash", "display", "none");
                }
            }).play();
            localModule.init();
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

    <% if (!"localhost".equals(request.getServerName())) { %><script type="text/javascript">
    var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-11910037-2']);
    _gaq.push(['_trackPageview']);
    (function() {
        var ga = document.createElement('script');
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        ga.setAttribute('async', 'true');
        document.documentElement.firstChild.appendChild(ga);
    })();
    </script><% } %>
</body>
</html>
