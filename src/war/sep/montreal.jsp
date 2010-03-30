<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // useCDN = false; // To be included for runs in offline mode ++ begin/end

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
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
        @import "<%= cdnBaseURL %>/dojox/grid/resources/Grid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/tundraGrid.css";
        @import "<%= cdnBaseURL %>/dojox/widget/Calendar/Calendar.css";
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

    <%
    if (useCDN) {
    %><script
        djConfig="parseOnLoad: false, isDebug: true, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/sep/blank.html'"
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
    dojo.addOnLoad(function(){
        dojo.addOnLoad(function(){
            dojo.require("dijit.Dialog");
            dojo.require("dijit.layout.BorderContainer");
            dojo.require("dijit.layout.ContentPane");
            dojo.require("dijit.layout.TabContainer");
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
            });
        });
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
                    <li><a name="justForStyle1" class="active"><%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer", locale) %></a></li>
                    <li><a name="justForStyle2"><%= LabelExtractor.get(ResourceFileId.third, "navigation_sale_associate", locale) %></a></li>
                    <!--  Reverse order because they are right aligned -->
                    <li class="subItem"><a href="javascript:dijit.byId('aboutPopup').show();" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %></a></li>
                    <li class="subItem"><a href="/control/logout" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_sign_out", locale) %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_sign_out", locale) %></a></li>
                </ul>
            </div>
        </div>
        <div
            dojoType="dijit.layout.ContentPane"
            id="centerZone"
            region="center"
        >
            <div>
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
                    <table style="padding: 10px 0;">
                        <tbody>
                            <tr>
                                <td style="text-align:right;">
                                    <%= LabelExtractor.get(ResourceFileId.third, "sep_demand_create_invite", locale) %>
                                </td>
                                <td>
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="completeDemand"
                                        name="completeDemand"
                                        style="width:30em;font-size:14pt;"
                                        type="text"
                                        value=""
                                    />
                                    <a href="#" onclick="dijit.byId('advancedForm').show();return false;" >
                                        <span><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_button", locale) %></span>
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td style="text-align:right;">
                                    <%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_invite", locale) %>
                                </td>
                                <td>
                                    <button
                                        dojoType="dijit.form.Button"
                                        iconClass="icon16x16 iconTwitter"
                                        onclick="alert('http://twitter.com');"
                                            ><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_Twitter", locale) %></button>
                                    <button
                                        dojoType="dijit.form.Button"
                                        iconClass="icon16x16 iconGTalk"
                                        onclick="alert('gtalk:chat?jid=twetailer@appspot.com');"
                                            ><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_GTalk", locale) %></button>
                                    <button
                                        disabled="true"
                                        dojoType="dijit.form.Button"
                                        iconClass="icon16x16 iconSMS"
                                        onclick="alert('--country dependent number--');"
                                            ><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_SMS", locale) %></button>
                                    <button
                                        dojoType="dijit.form.Button"
                                        iconClass="icon16x16 iconMail"
                                        onclick="alert('mailto:maezel@twetailer.appspotmail.com');"
                                            ><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_EMail", locale) %></button>
                                    <button
                                        disabled="true"
                                        dojoType="dijit.form.Button"
                                        iconClass="icon16x16 iconTwetailer"
                                        onclick="alert('http://twetailer.appspot.com/html/console.jsp');"
                                    ><%= LabelExtractor.get(ResourceFileId.third, "sep_demand_publish_with_Console", locale) %></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </center>
            </div>
            <div style="vertical-align:middle;border-top:2px solid black;margin-top:10px;padding-top:10px;text-align:left;height:30px;font-size:20pt;">
                <%= LabelExtractor.get(ResourceFileId.third, "sep_tag_cloud_introduction", new Object[] { "Montreal, Quebec, CA" }, locale) %>
            </div>
            <div style="padding-top:10px; text-align:center;">
                VW
                VolksWagen
                Audi
                car
                rent<br />
                livre
                book
            </div>
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
                <td>
                    <input dojoType=dijit.form.TextBox type="text" name="criteria" id="criteria" style="width:25em;"><br/>
                    <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_tags_hint", locale) %></span>
                </td>
            </tr>
            <tr>
                <td><label for="postalCode"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_locale_label", locale) %></label></td>
                <td>
                    <input dojoType=dijit.form.TextBox type="text" name="postalCode" id="postalCode" style="width:6em;">
                    <select dojoType=dijit.form.FilteringSelect name="countryCode" id="countryCode" hasDownArrow="true" style="width:8em;">
                        <option value="CA" selected="true"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                        <option value="US"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                    </select><br/>
                    <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_locale_hint", locale) %></span>
                </td>
            </tr>
            <tr>
                <td><label for="criteria"><%= LabelExtractor.get(ResourceFileId.third, "sep_advanced_form_quantity_label", locale) %></label></td>
                <td><input dojoType=dijit.form.TextBox type="text" name="quantity" id="quantity" value="1" style="width:3em;"></td>
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
                        style="width:8em;"
                        type="text"
                    >
                </td>
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
        _href="about.jsp"
    >
        <%= LabelExtractor.get(ResourceFileId.third, "about_text", locale) %>
        --<br/>
        <img alt="<%= LabelExtractor.get(ResourceFileId.third, "about_powered_by_appengine", locale) %>" height="30" src="http://code.google.com/appengine/images/appengine-noborder-120x30.gif" width="120"/>
    </div>

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
