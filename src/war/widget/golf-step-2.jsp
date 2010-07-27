<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.Map"
    import="java.util.ResourceBundle"
    import="com.dyuproject.openid.OpenIdUser"
    import="com.dyuproject.openid.RelyingParty"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.i18n.LocaleController"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    String localeId = request.getParameter("lang");
    Locale locale = LocaleController.getLocale(localeId);

    String postalCode = request.getParameter("postalCode");
%><html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="<%= localeId %>">
<head>
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
        @import "/css/golf/widget.css";
    </style>
</head>
<body class="tundra"><div>
    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "widget_splash_screen_message", locale) %></span></div>
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

    <table id="form" height="100%" width="100%">
        <tr>
            <td id="callToAction"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_2_title", locale) %></td>
        </tr>
        <tr>
            <td>
                <label for="range"><%= LabelExtractor.get(ResourceFileId.third, "gw_your_email_label", locale) %></label><br/>
                <input dojoType="dijit.form.TextBox" id="email0" name="email0" required="true" style="width:100%;" type="text" /><br/>
                <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "gw_your_email_hint", locale) %></label></span>
            </td>
        </tr>
        <tr>
            <td>
                <label for="range"><%= LabelExtractor.get(ResourceFileId.third, "gw_email_buddy_1_label", locale) %></label><br/>
                <input dojoType="dijit.form.TextBox" id="email1" name="email1" required="true" style="width:100%;" type="text" /><br/>
                <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "gw_email_buddy_hint", locale) %></label></span>
            </td>
        </tr>
        <tr>
            <td>
                <label for="range"><%= LabelExtractor.get(ResourceFileId.third, "gw_email_buddy_2_label", locale) %></label><br/>
                <input dojoType="dijit.form.TextBox" id="email2" name="email2" required="true" style="width:100%;" type="text" />
            </td>
        </tr>
        <tr>
            <td>
                <label for="range"><%= LabelExtractor.get(ResourceFileId.third, "gw_email_buddy_3_label", locale) %></label><br/>
                <input dojoType="dijit.form.TextBox" id="email3" name="email3" required="true" style="width:100%;" type="text" />
            </td>
        </tr>
        <tr>
            <td id="continueLink">
                <a href="javascript:location.back();"><%= LabelExtractor.get(ResourceFileId.third, "gw_back_link", locale) %></a>
                <a href="/widget/golf-step-3.jsp"><%= LabelExtractor.get(ResourceFileId.third, "gw_send_link", locale) %></a>
            </td>
        </tr>
        <tr>
            <td id="notice"><%= LabelExtractor.get(ResourceFileId.third, "gw_notice", locale) %></td>
        </tr>
    </table>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dojo.parser");
        dojo.require("dijit.form.TextBox");
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            // twetailer.GolfAssociate.init("<%= localeId %>");
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

    var localModule = {};
    localModule.init = function() {
    }
    </script>

    <% if (!"localhost".equals(request.getServerName())) { %><script type="text/javascript">
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

</div></body>
</html>
