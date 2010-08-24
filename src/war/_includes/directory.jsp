<!doctype html>
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
    import="twetailer.dto.Location"
    import="twetailer.dto.SaleAssociate"
    import="twetailer.dto.Seed"
    import="twetailer.j2ee.DirectoryServlet"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Get the data prepared by the servlet code
    JsonParser dataParser = new JsonParser((String) session.getAttribute(DirectoryServlet.QUERIED_CITY_ID));
    JsonObject data = dataParser.getJsonObject();
    String cityName = data.getString(Seed.LABEL);
    String cityUrl = data.getString(Seed.KEY);
    JsonArray cityTags = data.getJsonArray(SaleAssociate.CRITERIA);
    session.setAttribute(DirectoryServlet.QUERIED_CITY_ID, null);

    dataParser = new JsonParser((String) session.getAttribute(DirectoryServlet.SEED_CITY_LIST_ID));
    JsonArray citiesNearby = dataParser.getJsonArray();
    session.setAttribute(DirectoryServlet.SEED_CITY_LIST_ID, null);
%><html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="<%= localeId %>">
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "sep_localized_page_name", new Object[] { cityName }, locale) %></title>
    <meta name="google-site-verification" content="WY7P9S7-YK1ZBPjxlVz1h7kd0Ex1Sc74hcab8zXy1d4" />
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="description" content="<%= LabelExtractor.get(ResourceFileId.third, "sep_localized_page_description", new Object[] { cityName }, locale) %>" />
    <meta name="keywords" content="<%= cityName %>, <% for(int tagIdx = 0; tagIdx < (cityTags == null ? 0 : cityTags.size()); tagIdx ++) { %><%= cityTags.getString(tagIdx) %>, <% } %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.third, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <%
    if (useCDN) {
    %><style rel="stylesheet" type="text/css">
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
    %><style rel="stylesheet" type="text/css">
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
    <style rel="stylesheet" type="text/css">
        .icon16x16 {
            width: 16px;
            height: 16px;
        }
        .iconTwetailer { background: url('/images/icons/twetailerToRight.png') no-repeat scroll 0 0 transparent; }
        .iconGTalk { background: url('/images/icons/GTalk.png') no-repeat scroll 0 0 transparent; }
        .iconSMS { background: url('/images/icons/SMS.png') no-repeat scroll 0 0 transparent; }
        .iconMail { background: url('/images/icons/Email.png') no-repeat scroll 0 0 transparent; }
        .iconTwitter { background: url('/images/icons/Twitter.png') no-repeat scroll 0 0 transparent; }
        .iconGMaps { background: url('/images/icons/GMaps.png') no-repeat scroll 0 0 transparent; }

        .fisheyedTagCloud { font-size: 24pt; }
    </style>
</head>
<body class="tundra">

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>

    <% if (useCDN) { %><script
        djConfig="locale: '<%= localeId %>',parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    } else { %><script
        djConfig="locale: '<%= localeId %>', parseOnLoad: false, isDebug: false, baseUrl: '/js/dojo/dojo/', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="/js/dojo/dojo/dojo.js"
        type="text/javascript"
    ></script><% } %>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/_includes/banner_open.jsp">
            <jsp:param name="verticalId" value="" />
            <jsp:param name="localeId" value="<%= localeId %>" />
        </jsp:include>
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
                                    <a href="javascript:twetailer.Directory.showAdvancedForm();">
                                        <%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_button", locale) %>
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td style="text-align:right;"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_invite", locale) %></td>
                                <td>
                                    <button dojoType="dijit.form.Button" iconClass="icon16x16 iconTwitter" onclick="twetailer.Directory.postThruTwitter();"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_Twitter", locale) %></button>
                                    <button dojoType="dijit.form.Button" iconClass="icon16x16 iconSMS" onclick="twetailer.Directory.postThruSMS();"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_SMS", locale) %></button>
                                    <button dojoType="dijit.form.Button" iconClass="icon16x16 iconMail" onclick="twetailer.Directory.postThruEMail();"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_EMail", locale) %></button>
                                    <button dojoType="dijit.form.Button" iconClass="icon16x16 iconGTalk" onclick="twetailer.Directory.postThruGTalk();"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_GTalk", locale) %></button>
                                    <button disabled="true" dojoType="dijit.form.Button" iconClass="icon16x16 iconTwetailer" onclick="alert('http://twetailer.appspot.com/');"><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_Console", locale) %></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </center>
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                region="right"
                style="width:240px;margin:10px 0;background-color:lightgrey;"
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
                        %><li><a href="/directory<%= city.getString(Seed.KEY) + (request.getQueryString() == null ? "" : "?" + request.getQueryString()) %>"><%= city.getString(Seed.LABEL) %></a></li><% } %>
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
                    <% for(int tagIdx = 0; tagIdx < (cityTags == null ? 0 : cityTags.size()); tagIdx ++) {
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
        execute="twetailer.Directory.closeAdvancedForm();"
        id="advancedForm"
        title="<%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_button", locale) %>"
    >
        <table class="demandForm">
            <tr>
                <td align="right"><label for="criteria"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_tags_label", locale) %></label></td>
                <td>
                    <input
                        dojoType="dijit.form.TextBox"
                        id="criteria"
                        name="criteria"
                        required="true"
                        style="width:25em;font-size:larger;"
                        type="text"
                    />
                </td>
            </tr>
            <tr>
                <td align="right"></td>
                <td><span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_tags_hint", locale) %><br/>&nbsp;</span></td>
            </tr>
            <tr>
                <td align="right"><label for="postalCode"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_locale_label", locale) %></label></td>
                <td>
                    <input
                        dojoType="dijit.form.TextBox"
                        id="postalCode"
                        name="postalCode"
                        required="true"
                        style="width:6em;font-size:larger;"
                        type="text"
                    />
                    <select dojoType="dojox.form.DropDownSelect" name="countryCode" id="countryCode" hasDownArrow="true" style="width:14em;font-size:larger;">
                        <option value="<%= Locale.CANADA.getCountry() %>"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                        <option value="<%= Locale.US.getCountry() %>"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                    </select>
                    <button dojoType="dijit.form.Button" onclick="twetailer.Directory.showMap();" title="<%= LabelExtractor.get(ResourceFileId.third, "shared_locale_view_map_link", locale) %>">
                        <img src="/images/icons/GMaps.png" width="16" height="16" />
                    </button>
                </td>
            </tr>
            <tr>
                <td align="right"></td>
                <td><span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_locale_hint", locale) %><br/>&nbsp;</span></td>
            </tr>
            <tr>
                <td align="right"><label for="range"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_range_label", locale) %></label></td>
                <td>
                    <input
                        constraints="{min:5,max:100,places:0}"
                        dojoType="dijit.form.NumberTextBox"
                        id="range"
                        name="range"
                        onchange="dijit.byId('rangeSlider').attr('value',this.value);"
                        required="true"
                        style="width:3em;font-size:larger;"
                        type="text"
                        value="10"
                    />
                    <select
                        dojoType="dojox.form.DropDownSelect"
                        hasDownArrow="true"
                        id="rangeUnit"
                        name="rangeUnit"
                        onchange="twetailer.Directory.onRangeUnitChange();"
                        style="width:5em;font-size:larger;"
                    >
                        <option value="<%= LocaleValidator.KILOMETER_UNIT %>" selected="true"><%= LocaleValidator.KILOMETER_UNIT %></option>
                        <option value="<%= LocaleValidator.MILE_UNIT %>"><%= LocaleValidator.MILE_UNIT %></option>
                    </select>
                </td>
            </tr>
            <tr>
                <td align="right"></td>
                <td><div
                        id="rangeSlider"
                        dojoType="dijit.form.HorizontalSlider"
                        value="10"
                        minimum="5"
                        maximum="100"
                        discreteValues="20"
                        intermediateChanges="true"
                        showButtons="true"
                        style="width:12em;"
                    >
                        <script type="dojo/connect" event="onChange">
                            dijit.byId("range").attr("value", this.attr("value"));
                        </script>
                    </div>
                </td>
            </tr>
            <tr>
                <td align="right"></td>
                <td><span class="hint">&nbsp;</span></td>
            </tr>
            <tr>
                <td align="right"><label for="criteria"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_expiration_label", locale) %></label></td>
                <td>
                    <input
                        constraints="{min:'2010-03-25',max:'2010-12-31'}"
                        dojoType="dijit.form.DateTextBox"
                        id="expiration"
                        invalidMessage=<%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_expiration_date_invalid_message", new Object[] { LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_expiration_date_format", locale)}, locale) %>
                        lang="<%= localeId %>"
                        name="expiration"
                        promptMessage="<%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_expiration_date_format", locale) %>"
                        style="width:8em;font-size:larger;"
                        type="text"
                    />
                </td>
            </tr>
            <tr>
                <td align="right"></td>
                <td><span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_expiration_hint", new Object[] { LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_expiration_date_format", locale)}, locale) %><br/>&nbsp;</span></td>
            </tr>
            <tr>
                <td align="right"><label for="criteria"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_quantity_label", locale) %></label></td>
                <td>
                    <input
                        constraints="{min:1,max:999,places:0}"
                        dojoType="dijit.form.NumberSpinner"
                        id="quantity"
                        name="quantity"
                        style="width:5em;font-size:larger;"
                        type="text"
                        value="1"
                    />
                </td>
            </tr>
            <tr>
                <td align="right"></td>
                <td><span class="hint">&nbsp;</span></td>
            </tr>
            <tr>
                <td colspan="2" align="center">
                    <button
                        dojoType="dijit.form.Button"
                        onclick="return dijit.byId('advancedForm').isValid();"
                        type="submit"
                    ><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_update_button", locale) %></button>
                </td>
            </tr>
        </table>
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
        id="postThruSMSInfo"
        title="<%= LabelExtractor.get(ResourceFileId.third, "sep_sms_info_dialog_title", locale) %>"
    >
        <%= LabelExtractor.get(ResourceFileId.third, "sep_sms_info_dialog_content", locale) %>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="postThruGTalkInfo"
        title="<%= LabelExtractor.get(ResourceFileId.third, "sep_gtalk_info_dialog_title", locale) %>"
    >
        <%= LabelExtractor.get(ResourceFileId.third, "sep_gtalk_info_dialog_content", locale) %>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="locationMapDialog"
        title="<%= LabelExtractor.get(ResourceFileId.third, "shared_map_preview_dialog_title", locale) %>"
    >
        <div style="width:600px;height:400px;"><div id='mapPlaceHolder' style='width:100%;height:100%;'></div></div>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dojo.io.script");
        dojo.require("dojo.parser");
        dojo.require("dijit.Dialog");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.form.Form");
        dojo.require("dijit.form.HorizontalSlider");
        dojo.require("dijit.form.NumberSpinner");
        dojo.require("dijit.form.NumberTextBox");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.form.ValidationTextBox");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.ContentPane");
        // dojo.require("dojox.layout.ExpandoPane");
        dojo.require("dijit.layout.TabContainer");
        dojo.require("dojox.form.DropDownSelect");
        dojo.require("twetailer.Directory");
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            twetailer.Directory.init("<%= localeId %>", "<%= cityName %>", "<%= data.getString(Location.POSTAL_CODE) %>", "<%= data.getString(Location.COUNTRY_CODE) %>");
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

    <script async="true" defer="true" src="http://maps.google.com/maps/api/js?sensor=false&language=<%= localeId %>" type="text/javascript"></script>

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
</body>
</html>
