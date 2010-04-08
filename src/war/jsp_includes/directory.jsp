<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="java.net.URL"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.jsontools.JsonArray"
    import="domderrien.jsontools.JsonObject"
    import="domderrien.jsontools.JsonParser"
    import="domderrien.i18n.LocaleController"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Get the data prepared by the servlet code
    JsonParser dataParser = new JsonParser(LocaleValidator.toUnicode((String) session.getAttribute("data")));
    session.setAttribute("data", null);
    JsonObject data = dataParser.getJsonObject();
    String cityName = data.getString("city_label");
    String cityUrl = data.getString("city_url");
    JsonArray cityTags = data.getJsonArray("city_tags");
    JsonArray citiesNearby = data.getJsonArray("cities_nearby");
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
        @import "<%= cdnBaseURL %>/dojox/grid/resources/Grid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/tundraGrid.css";
        @import "<%= cdnBaseURL %>/dojox/widget/Calendar/Calendar.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/FloatingPane.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/ExpandoPane.css";
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><style type="text/css">
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/tundra/tundra.css";
        @import "/js/dojo/dojox/grid/resources/Grid.css";
        @import "/js/dojo/dojox/grid/resources/tundraGrid.css";
        @import "/js/dojox/widget/Calendar/Calendar.css";
        @import "/js/dojo/dojox/layout/resources/FloatingPane.css";
        @import "/js/dojo/dojox/layout/resources/ExpandoPane.css";
        @import "/css/console.css";
    </style><%
    } // endif (useCDN)
    %>
    <style type="text/css" rel="stylesheet">
    .icon16x16 {
        width: 16px;
        height: 16px;
    }
    .iconTwetailer { background: url('/images/icons/twetailerToRight.png') no-repeat scroll 0 0 transparent; }
    .iconGTalk { background: url('/images/icons/GTalk.png') no-repeat scroll 0 0 transparent; }
    .iconSMS { background: url('/images/icons/SMS.png') no-repeat scroll 0 0 transparent; }
    .iconMail { background: url('/images/icons/Email.png') no-repeat scroll 0 0 transparent; }
    .iconTwitter { background: url('/images/icons/Twitter.png') no-repeat scroll 0 0 transparent; }

    .fisheyedTagCloud { font-size: 24pt; }
    </style>
</head>
<body class="tundra">

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "ui_splash_screen_message", locale) %></span></div>
    </div>

    <% if (useCDN) { %><script
        djConfig="parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    } else { %><script
        djConfig="parseOnLoad: false, isDebug: false, baseUrl: '/js/dojo/dojo/', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="/js/dojo/dojo/dojo.js"
        type="text/javascript"
    ></script><% } %>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/jsp_includes/banner_open.jsp"></jsp:include>
        <div
            design="sidebar"
            dojoType="dijit.layout.BorderContainer"
            id="centerZone"
            gutters="false"
            region="center"
        >
            <div
                dojoType="dijit.layout.ContentPane"
                region="top"
                style="padding:10px;"
            >
                <div
                    iconClass="silkIcon silkIconHelp"
                    dojoType="dijit.form.DropDownButton"
                    style="float:right;"
                >
                    <span><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_samples_button", locale) %></span>
                    <div
                        dojoType="dijit.TooltipDialog"
                        title="<%= LabelExtractor.get(ResourceFileId.third, "sep_demand_samples_button", locale) %>"
                    ><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_samples_list", locale) %></div>
                </div>
                <center>
                    <table>
                        <tbody>
                            <tr style="height:35px;">
                                <td colspan="2" style="font-size:x-large;"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_form_invite", locale) %></td>
                            </tr>
                            <tr>
                                <td style="text-align:right;"> <%= LabelExtractor.get(ResourceFileId.third, "sep_demand_create_invite", locale) %> </td>
                                <td>
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="completeDemand"
                                        name="completeDemand"
                                        style="width:100%;font-size:14pt;"
                                        type="text"
                                        value=""
                                    />
                                </td>
                            </tr>
                            <tr style="height:25px;">
                                <td></td>
                                <td style="text-align:center;vertical-align:top;">
                                    <a href="#" onclick="dijit.byId('advancedForm').show();return false;">
                                        <%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_button", locale) %>
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td style="text-align:right;"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_invite", locale) %></td>
                                <td>
                                    <!--
                                    Provide the links
                                    -->
                                    <button dojoType="dijit.form.Button" iconClass="icon16x16 iconTwitter" onclick="postThruTwitter();"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_Twitter", locale) %></button>
                                    <button dojoType="dijit.form.Button" iconClass="icon16x16 iconGTalk" onclick="alert('gtalk:chat?jid=twetailer@appspot.com');"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_GTalk", locale) %></button>
                                    <button disabled="true" dojoType="dijit.form.Button" iconClass="icon16x16 iconSMS" onclick="alert('--country dependent number--');"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_SMS", locale) %></button>
                                    <button dojoType="dijit.form.Button" iconClass="icon16x16 iconMail" onclick="alert('mailto:maezel@twetailer.appspotmail.com');"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_EMail", locale) %></button>
                                    <button disabled="true" dojoType="dijit.form.Button" iconClass="icon16x16 iconTwetailer" onclick="alert('http://twetailer.appspot.com/html/console.jsp');"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_Console", locale) %></button>
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td>
                                    <a href="http://help.twitter.com/portal">Twitter support</a>
                                    <a href="http://www.google.com/support/talk/?hl=<%= localeId %>">GTalk support</a>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </center>
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                region="right"
                style="width:240px;margin: 10px 0;background-color:lightgrey;"
                title="<%= LabelExtractor.get(ResourceFileId.third, "sep_other_locations_title", locale) %>"
            >
                <div style="color:white;background-color:black;font-weight:bold;padding:2px 5px;">
                    <%= LabelExtractor.get(ResourceFileId.third, "sep_other_locations_title", locale) %>
                </div>
                <div style="border:1px lightgrey solid;padding:10px;">
                    <%= LabelExtractor.get(ResourceFileId.third, "sep_other_locations_introduction", new Object[] { cityName, cityUrl }, locale) %>
                    <ul>
                        <% for(int cityIdx=0; cityIdx < citiesNearby.size(); cityIdx ++) {
                            JsonObject city = citiesNearby.getJsonObject(cityIdx);
                        %><li><a href="<%= city.getString("url") %>"><%= city.getString("label") %></a></li><% } %>
                    </ul>
                </div>
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                region="center"
                style="border-top:1px solid lightgrey;margin-right:10px;padding:10px 0;"
            >
                <div style="font-size:x-large;">
                    <%= LabelExtractor.get(ResourceFileId.third, "sep_tag_cloud_introduction", new Object[] { cityName, cityUrl }, locale) %>
                </div>
                <div style="padding-top:20px; text-align:center;font-size:larger">
                    <% for(int tagIdx = 0; tagIdx < cityTags.size(); tagIdx ++) {
                    %><%= cityTags.getString(tagIdx) %> <% } %>
                </div>
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                region="bottom"
                style="font-style:italic;padding:10px 0;"
            ><%= LabelExtractor.get(ResourceFileId.third, "sep_tag_cloud_disclaimer", new Object[] { cityName, cityUrl }, locale) %></div>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_copyright", locale) %>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="advancedForm"
        title="<%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_button", locale) %>"
        execute="alert('submitted w/args:\n' + dojo.toJson(arguments[0], true));"
    >
        <table class="demandForm">
            <tr>
                <td><label for="criteria"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_tags_label", locale) %></label></td>
                <td><input dojoType=dijit.form.TextBox type="text" name="criteria" id="criteria" style="width:25em;font-size:larger;"></td>
            </tr>
            <tr>
                <td></td>
                <td><span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_tags_hint", locale) %></span></td>
            </tr>
            <tr>
                <td><label for="postalCode"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_locale_label", locale) %></label></td>
                <td>
                    <input dojoType=dijit.form.TextBox type="text" name="postalCode" id="postalCode" style="width:6em;font-size:larger;">
                    <select dojoType=dijit.form.FilteringSelect name="countryCode" id="countryCode" hasDownArrow="true" style="width:8em;font-size:larger;">
                        <option value="CA" selected="true"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                        <option value="US"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                    </select>
                </td>
            </tr>
            <tr>
                <td></td>
                <td><span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_locale_hint", locale) %></span></td>
            </tr>
            <tr>
                <td><label for="criteria"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_expiration_label", locale) %></label></td>
                <td>
                    <input
                        constraints="{min:'2010-03-25',max:'2010-12-31'}"
                        dojoType="dijit.form.DateTextBox"
                        id="expiration"
                        invalidMessage="Invalid date. Use mm/dd/yyyy format."
                        lang="en-us"
                        name="expiration"
                        promptMessage="mm/dd/yyyy"
                        required="true"
                        style="width:8em;font-size:larger;"
                        type="text"
                    >
                </td>
            </tr>
            <tr>
                <td><label for="criteria"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_quantity_label", locale) %></label></td>
                <td><input dojoType=dijit.form.TextBox type="text" name="quantity" id="quantity" value="1" style="width:3em;font-size:larger;"></td>
            </tr>
            <tr>
                <td colspan="2" align="center">
                    <button dojoType=dijit.form.Button type="submit" name="submit"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_update_button", locale) %></button>
                </td>
            </tr>
        </table>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="aboutPopup"
        title="About"
        href="/jsp_includes/about.jsp"
    >
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dijit.Dialog");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.ContentPane");
        dojo.require("dijit.layout.TabContainer");
        dojo.require("dojox.layout.ExpandoPane");
        dojo.require("dijit.form.Form");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.form.NumberTextBox");
        dojo.require("dijit.form.ValidationTextBox");
        dojo.require("dijit.form.FilteringSelect");
        dojo.require("dojox.widget.Calendar");
        // dojo.require("twetailer.Console");
        dojo.require("dojo.parser");
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            var userLocale = "<%= localeId %>";
            // twetailer.TagClouds.init(userLocale);
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
    // http://twitter.com/home/?status=d twetailer tag%3Awii mario 2010 olympics range%3A25 mi locale%3A91210 us expires%3A2010-05-06 %23demo
    var localModule = {};
    localModule.init = function() {
    };
    localModule.postThruTwitter = function() {
        var demand = dijit.byId("completeDemand").attr("value");
        if (demand != null & 0 < demand.length) {
            demand = dojo.trim(demand);
        }
        if (demand != null & 0 < demand.length) {
            window.open("http://twitter.com/home/?status=d twetailer " + escape(demand), "twitter_window");
        }
        else {
            alert("ddd You need to write your demand!");
        }
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
