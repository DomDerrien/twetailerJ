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
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Demand"
    import="twetailer.dto.HashTag.RegisteredHashTag"
    import="twetailer.dto.Location"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    String localeId = request.getParameter("languageId");
    if (localeId == null) {
        localeId = LocaleValidator.DEFAULT_LANGUAGE;
    }
    Locale locale = LocaleValidator.getLocale(localeId);

    // Get the widget parameters
    String referralId = request.getParameter("referralId");
    String postalCode = request.getParameter("postalCode");
    String countryCode = request.getParameter("countryCode");
    if (countryCode == null || countryCode.length() == 0) {
        countryCode = LocaleValidator.DEFAULT_COUNTRY_CODE;
    }
    String color = request.getParameter("color");
    String colorTitle = request.getParameter("color-title");
    String backgroundColor = request.getParameter("background-color");
    String backgroundImage = request.getParameter("background-image");
    String fontSize = request.getParameter("font-size");
    String fontSizeTitle = request.getParameter("font-size-title");
    String fontFamily = request.getParameter("font-family");

    // Regular expression for e-mail address validation
    String emailRegExp = Consumer.EMAIL_REGEXP_VALIDATOR;
%><html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="<%= localeId %>">
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "golfConsu_localized_page_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
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
        @import "/js/dojo/dijit/themes/tundra/tundra.css";<%
        } // endif (useCDN)
        %>
        @import "/css/eztoff/widget.css";
        body { <%
        if (color != null && 0 < color.length()) { %>color:<%= color.replaceAll(";|<|>|\\:", "") %>;<% } %>
        label, .title, .comment { <%
        if (fontFamily != null && 0 < fontFamily.length()) { %>font-family:<%= fontFamily.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        #content { <%
        if (fontSize != null && 0 < fontSize.length()) { %>font-size:<%= fontSize.replaceAll(";|<|>|\\:", "") %>;<% }
        if (backgroundColor != null && 0 < backgroundColor.length()) { %>background-color:<%= backgroundColor.replaceAll(";|<|>|\\:", "") %>;<% }
        if (backgroundImage != null && 0 < backgroundImage.length()) { %>background-image: <%= backgroundImage.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        #content .title { <%
        if (colorTitle != null && 0 < colorTitle.length()) { %>color:<%= colorTitle.replaceAll(";|<|>|\\:", "") %>;<% }
        if (fontSizeTitle != null && 0 < fontSizeTitle.length()) { %>font-size:<%= fontSizeTitle.replaceAll(";|<|>|\\:", "") %>;<% } %> }
    </style>
</head>
<body class="tundra">
    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "widget_splash_screen_message", locale) %></span></div>
    </div>

    <%
    if (useCDN) {
    %><script
        djConfig="parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/html/blank.html'"
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

    <div id="centerZone">
        <div id="pane1">
            <div class="progressBar">
                <div class="step active">1</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive">2</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive">3</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive goal">&nbsp;</div>
            </div>
            <div dojoType="dijit.form.Form" id="form1" class="content">
                <div class="title">When do you want to play?</div>
                <table cellpadding="0" cellspacing="0">
                    <tr id="postalCodeRow" style="display:none;">
                        <td><label for="postalCode">Postal code:</label></td>
                        <td>
                            <input
                                dojoType="dijit.form.ValidationTextBox"
                                id="postalCode"
                                name="postalCode"
                                placeholder="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_placeHolder_CA", locale) %>"
                                regExp="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_regExp_CA", locale) %>"
                                required="true"
                                style="width:100%;"
                                trim="true"
                                type="text"
                            />
                        </td>
                        <td style="width:20px;padding-right:0px !important;">
                            <button
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconGPS"
                                id="detectLocationButton"
                                onclick="localModule.getBrowserLocation();"
                                showLabel="false"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "ga_cmenu_detectLocale", locale) %>"
                            ></button>
                        </td>
                    </tr>
                    <tr id="countryCodeRow" style="display:none;">
                        <td><label for="countryCode">Country:</label></td>
                        <td colspan="2">
                            <select
                                dojoType="dojox.form.DropDownSelect"
                                id="countryCode"
                                hasDownArrow="true"
                                name="countryCode"
                                onchange="twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'postalCode');"
                                required="true"
                                style="width:100%;"
                            >
                                <option value="<%= Locale.CANADA.getCountry() %>"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                                <option value="<%= Locale.US.getCountry() %>"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="date">Date:</label></td>
                        <td colspan="2"><input constraints="{datePattern:'EEE, MMMM dd yyyy'}" dojoType="dijit.form.DateTextBox" id="date" name="date" required="true" style="width:100%;" type="text" /></td>
                    </tr>
                    <tr>
                        <td><label for="time">Time:</label></td>
                        <td colspan="2"><input constraints="{visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}" dojoType="dijit.form.TimeTextBox" id="time" name="time" required="true" style="width:100%;" type="text" value="T07:00"/></td>
                    </tr>
                    <tr>
                        <td><label for="quantity">For:</label></td>
                        <td colspan="2" style="vertical-align:top;">
                            <input constraints="{min:1,places:0}" dojoType="dijit.form.NumberSpinner" id="quantity" name="quantity" style="width:5em;" required="true" type="text" value="4" />
                            <label for="quantity">players</label>
                        </td>
                    </tr>
                </table>
                <div class="comment">Look for local golf courses within <input constraints="{min:5,max:100,places:0}" dojoType="dijit.form.NumberSpinner" id="range" name="range" style="width:5em;" type="text" value="25" /> km.</div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                onclick="localModule.switchPane(1, 2);"
                                style="color:black;"
                                title="Share your contact details"
                            >Next &raquo;</button>
                            <br/>
                            <span class="hint">Your account details</span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
        </div>
        <div id="pane2" style="display:none;">
            <div class="progressBar">
                <div class="step inactive">1</div>
                <div class="step transition">&nbsp;</div>
                <div class="step active">2</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive">3</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive goal">&nbsp;</div>
            </div>
            <div dojoType="dijit.form.Form" id="form2" class="content">
                <div class="title">20 golf courses will be contacted on your behalf. How do we reach you?</div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="vertical-align:top;"><label for="email0">Your email:</label></td>
                        <td style="text-align: right;">
                            <input
                                dojoType="dijit.form.ValidationTextBox"
                                id="email0"
                                invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_ccInvalidMessage", locale) %>"
                                name="email0"
                                placeHolder="<%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_ccPlaceHolder", locale) %>"
                                regExp="<%= emailRegExp %>"
                                required="true"
                                style="width:100%;"
                                trim="true"
                                type="text"
                            />
                            <br />
                            <span class="hint"><i>e.g.</i> yourname@gmail.com</span>
                        </td>
                    </tr>
                </table>
                <div class="comment">We can also automatically notify your golf buddies, friends, etc.</div>
                <table>
                    <tbody id="friendList">
                        <tr id="friendRow1">
                            <td><label for="email1">Email:</label></td>
                            <td style="text-align: right;">
                                <input
                                    dojoType="dijit.form.ValidationTextBox"
                                    id="email1"
                                    invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_ccInvalidMessage", locale) %>"
                                    name="email1"
                                    placeHolder="<%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_ccPlaceHolder", locale) %>"
                                    regExp="<%= emailRegExp %>"
                                    required="false"
                                    style="width:100%;"
                                    trim="true"
                                    type="text"
                                />
                            </td>
                            <td style="width:20px;padding-left:0px !important">
                                <button
                                    dojoType="dijit.form.Button"
                                    iconClass="silkIcon silkIconAdd"
                                    id="friendButton1"
                                    onclick="twetailer.Common.manageFriendRow(1, 'Email:');"
                                    showLabel="false"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_addCCButtonLabel", locale) %>"
                                ></button>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div class="comment">
                    <a href="http://eztoff.com/">ezToff</a> values your's & others' <a href="http://eztoff.com/privacy-<%= localeId %>">privacy</a>,
                    will not share your email addresses with others & only use them to communicate between you & us.
                </div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(2, 1);">&laquo; back</a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                onclick="localModule.switchPane(2, 3);"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "shared_locale_view_map_link", locale) %>"
                            >Next &raquo;</button>
                            <br/>
                            <span class="hint">Optional extras</span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
        </div>
        <div id="pane3" style="display:none;">
            <div class="progressBar">
                <div class="step inactive">1</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive">2</div>
                <div class="step transition">&nbsp;</div>
                <div class="step active">3</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive goal">&nbsp;</div>
            </div>
            <div dojoType="dijit.form.Form" id="form3" class="content">
                <div class="title">Extras:</div>
                <div style="margin-left:20px;">
                    <div>
                        <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberSpinner" id="pullCart" name="pullCart" style="width:6em;" type="text" value="0" />
                        <label for="pullCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_pullCartOptions_label", locale) %></label>
                    </div>
                    <div>
                        <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberSpinner" id="motorCart" name="motorCart" style="width:6em;" type="text" value="0" />
                        <label for="motorCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_motorCartOptions_label", locale) %></label>
                    </div>
                </div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(3, 2);">&laquo; back</a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconAccept"
                                onclick="localModule.sendRequest();"
                                style="color:black;"
                                title="Send tee-off reservation request"
                            >Send request</button>
                            <br />
                            <span class="hint">Broadcast your request</span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
        </div>
        <div id="pane4" style="display:none;">
            <div class="progressBar">
                <div class="step inactive">1</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive">2</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive">3</div>
                <div class="step transition">&nbsp;</div>
                <div class="step active goal">&nbsp;</div>
            </div>
            <div class="content">
                <div class="title">Thank you!</div>
                <div class="comment">You should receive an email at <span id="sender.email" style="font-weight:bold;">[tbd]</span> shortly.</div>
                <div class="comment">If you have not receive an email from ezToff after 20 minutes, please check our <a href="http://ezToff.com/faq-<%= localeId %>">FAQ</a> or contact <a href="mailto:support@eztoff.com">support@eztoff.com</a>.</div>
                <table cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(4, 1);">&laquo; Schedule another tee-off</a></td>
                    </tr>
                </table>
            </div>
        </div>
            <div class="footer">
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
    </div>

    <div
       color="darkgreen"
       dojoType="dojox.widget.Standby"
       id="widgetOverlay"
       target="centerZone"
    ></div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dojo.fx");
        dojo.require("dojo.fx.easing");
        dojo.require("dojo.parser");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.form.Form");
        dojo.require("dijit.form.NumberSpinner");
        // dojo.require("dijit.form.NumberTextBox");
        dojo.require("dijit.form.Textarea");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.form.TimeTextBox");
        dojo.require("dijit.form.ValidationTextBox");
        dojo.require("dojox.form.DropDownSelect");
        dojo.require("dojox.widget.Standby");
        dojo.require("twetailer.Common");
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

    var localModule = {};
    localModule._getLabel = null;
    localModule.init = function() {
        localModule._getLabel = twetailer.Common.init("<%= localeId %>", "detectLocationButton");

        var yesterday = new Date();
        var tomorrow = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        tomorrow.setDate(tomorrow.getDate() + 1);
        var dateField = dijit.byId("date");
        dateField.set("value", tomorrow);
        dateField.constraints.min = yesterday; // ??? why is reported as an invalid date?

        <% if (postalCode == null || postalCode.length() == 0) {
        %>dojo.style("postalCodeRow", "display", "");
        dijit.byId("postalCode").focus();
        dojo.style("countryCodeRow", "display", "");<%
        }
        else {
        %>dijit.byId("postalCode").set("value", "<%= postalCode %>");
        dijit.byId("quantity").focus();<%
        } %>
        dijit.byId("countryCode").set("value", "<%= countryCode %>");
    };
    localModule.switchPane = function(sourceIdx, targetIdx) {
        if (sourceIdx < targetIdx) {
            var form = dijit.byId("form" + sourceIdx);
            if (!form.isValid()) {
                return;
            }
        }
        // create two animations
        var anim1 = dojo.fx.wipeOut({ node: "pane" + sourceIdx, easing: dojo.fx.easing.expoOut });
        var anim2 = dojo.fx.wipeIn({ node: "pane" + targetIdx, easing: dojo.fx.easing.expoIn });
        // and play them at the same moment
        dojo.fx.chain([anim1, anim2]).play();
        switch(targetIdx) {
        case 1: <% if (postalCode == null || postalCode.length() == 0) {
                    %>dijit.byId("postalCode").focus();<%
                }
                else {
                    %>dijit.byId("quantity").focus();<%
                } %> break;
        case 2: dijit.byId("email0").focus(); break;
        case 3: dijit.byId("pullCart").focus(); break;
        }
    }
    localModule.getBrowserLocation = function() {
        var eventName = "browserLocationCodeAvailable";
        var handle = dojo.subscribe(eventName, function(postalCode, countryCode) {
            dijit.byId("postalCode").set("value", postalCode);
            dijit.byId("countryCode").set("value", countryCode);
            dijit.byId("postalCode").focus();
            dojo.unsubscribe(handle);
        })
        twetailer.Common.getBrowserLocation(eventName, "widgetOverlay");
    }
    localModule.sendRequest = function() {
        // Last form validation
        var form = dijit.byId("form3");
        if (!form.isValid()) {
            return;
        }
        // Request preparation
        var parameters = {
            referralId: "<%= referralId %>",
            <%= Consumer.LANGUAGE %>: "<%= localeId %>",
            <%= Consumer.EMAIL %>: dijit.byId("email0").get("value"),
            <%= Location.POSTAL_CODE %>: dijit.byId("postalCode").get("value"),
            <%= Location.COUNTRY_CODE %>: dijit.byId("countryCode").get("value"),
            <%= Demand.DUE_DATE %>: twetailer.Common.toISOString(dijit.byId("date").get("value"), dijit.byId("time").get("value")),
            <%= Demand.RANGE %>: dijit.byId("range").get("value"),
            <%= Demand.RANGE_UNIT %>: "<%= LocaleValidator.DEFAULT_RANGE_UNIT %>",
            <%= Demand.QUANTITY %>: dijit.byId("quantity").get("value"),
            <%= Demand.HASH_TAGS %>: ["<%= RegisteredHashTag.golf.toString() %>"],
            <%= Demand.META_DATA %>:"{'pullCart':" + dijit.byId("pullCart").get("value") + ",'golfCart':" + dijit.byId("motorCart").get("value") + "}"
        };
        var cc = twetailer.Common.getFriendCoordinates();
        if (0 < cc.length) {
            parameters.<%= Demand.CC %> = cc;
        }
        dijit.byId("widgetOverlay").show();
        var dfd = dojo.xhrPost({
            headers: { "content-type": "application/json; charset=utf-8" },
            postData: dojo.toJson(parameters),
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    // Update pane content
                    var placeHolder = dojo.byId("sender.email");
                    placeHolder.innerHTML = ""; // Reset content
                    placeHolder.appendChild(dojo.doc.createTextNode(dijit.byId("email0").get("value"))); // Safe insert
                    // Switch to pane
                    localModule.switchPane(3, 4);
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
                dijit.byId('widgetOverlay').hide();
            },
            error: function(message, ioArgs) {
                dijit.byId("widgetOverlay").hide();
                twetailer.Common.handleError(message, ioArgs);
            },
            url: "/3rdParty/Demand"
        });
    }
    </script>

    <% if (postalCode == null || postalCode.length() == 0) {
    %><script src="http://maps.google.com/maps/api/js?sensor=false&language=<%= localeId %>" type="text/javascript"></script>
    <% } %>
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
