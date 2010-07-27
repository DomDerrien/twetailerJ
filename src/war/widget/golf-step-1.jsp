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
            <td colspan="2" id="callToAction"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_1_title", locale) %></td>
        </tr>
        <tr>
            <td><label for="range"><%= LabelExtractor.get(ResourceFileId.third, "gw_range_label", locale) %></label></td>
            <td><input constraints="{min:5,max:100,places:2}" dojoType="dijit.form.NumberSpinner" id="range" name="range" required="true" style="width:100%;" type="text" value="45" /></td>
        </tr>
        <tr>
            <td><label for="postalCode"><%= LabelExtractor.get(ResourceFileId.third, "gw_postalCode_label", locale) %></label></td>
            <td>
                <input
                    dojoType="dijit.form.ValidationTextBox"
                    id="postalCode"
                    invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>"
                    name="postalCode"
                    regExp="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_regExp_CA", locale) %>"
                    required="true"
                    style="width:100%;"
                    type="text"
                    value="<%= postalCode %>"
                />
            </td>
        </tr>
        <tr>
            <td><label for="date"><%= LabelExtractor.get(ResourceFileId.third, "gw_teeOffDate_label", locale) %></label></td>
            <td><input constraints="{datePattern:'EEE, MMMM dd'}" dojoType="dijit.form.DateTextBox" id="date" name="date" required="true" style="width:100%;" type="text" /></td>
        </tr>
        <tr>
            <td><label for="time"><%= LabelExtractor.get(ResourceFileId.third, "gw_teeOffTime_label", locale) %></label></td>
            <td><input constraints="{visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}" dojoType="dijit.form.TimeTextBox" id="time" name="time" required="true" style="width:100%;" type="text" value="T07:00:00" /></td>
        </tr>
        <tr>
            <td><label for="quantity"><%= LabelExtractor.get(ResourceFileId.third, "gw_playerNb_label", locale) %></label></td>
            <td><input constraints="{min:1,max:100,places:0}" dojoType="dijit.form.NumberSpinner" id="demand.range" name="range" required="true" style="width:100%;" type="text" value="4" /></td>
        </tr>
        <tr>
            <td><label for="motorCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_cartOptions_label", locale) %></label></td>
            <td>
                <input constraints="{min:0,max:8,places:0}" dojoType="dijit.form.NumberSpinner" id="motorCart" name="motorCart" style="width:3em;" type="text" value="0" />
                <label for="motorCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_motorCartOptions_label", locale) %></label>
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input constraints="{min:0,max:8,places:0}" dojoType="dijit.form.NumberSpinner" id="pullCart" name="pullCart" style="width:3em;" type="text" value="0" />
                <label for="pullCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_pullCartOptions_label", locale) %></label>
            </td>
        </tr>
        <tr>
            <td colspan="2" id="continueLink"><a href="/widget/golf-step-2.jsp"><%= LabelExtractor.get(ResourceFileId.third, "gw_continue_link", locale) %></a></td>
        </tr>
        <tr>
            <td colspan="2" id="notice"><%= LabelExtractor.get(ResourceFileId.third, "gw_notice", locale) %></td>
        </tr>
    </table>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dojo.parser");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.form.FilteringSelect");
        dojo.require("dijit.form.NumberSpinner");
        // dojo.require("dijit.form.NumberTextBox");
        dojo.require("dijit.form.Textarea");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.form.TimeTextBox");
        dojo.require("dijit.form.ValidationTextBox");
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
        var yesterday = new Date();
        var tomorrow = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        tomorrow.setDate(tomorrow.getDate() + 1);
        var dateField = dijit.byId("date");
        dateField.set("value", tomorrow);
        // dateField.constraints.min = yesterday; // ??? why is reported as an invalid date?
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
