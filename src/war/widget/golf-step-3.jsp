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
            <td id="callToAction"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_3_title", locale) %></td>
        </tr>
        <tr>
            <td>
                ezToff confirms your tee-off request has been received.
                A confirmation of the operation has been sent to your email address '[given-email-address]'.
            </td>
        </tr>
        <tr>
            <td>
                Up to now, 4 golf courses have been notified.
                Once they issue proposals, they'll be forwarded to you by e-mail.
            </td>
        </tr>
        <tr>
            <td>
                For the one you'll confirm, you'll receive an invitation to pay the deposit via PayPal.
                Once the deposit is registered, your reservation will be OK.
            </td>
        </tr>
        <tr>
            <td>
                Thanks for booking your rounds with ezToff.
            </td>
        </tr>
        <tr>
            <td id="continueLink"><a href="javascript:history.go(-2);"><%= LabelExtractor.get(ResourceFileId.third, "gw_back_link", locale) %></a></td>
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
