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

    <div id="centerZone">
        <div id="pane1">
            <div class="progressBar">
                <div class="step active">1</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive">2</div>
                <div class="step transition">&nbsp;</div>
                <div class="step inactive">3</div>
                <div class="step transition">&nbsp;</div>
                <div class="step goal">&nbsp;</div>
            </div>
            <div class="content">
                <div class="title">When do you want to play?</div>
                <table cellpadding="0" cellspacing="0">
                    <tr id="postalCodeRow" style="display:none;">
                        <td><label id="postalCode">Postal code:</label></td>
                        <td><input dojoType="dijit.form.TextBox" id="postalCode" name="postalCode" required="true" style="width:100%;" type="text" /></td>
                        <td width="24">
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
                        <td><label id="countryCode">Country:</label></td>
                        <td colspan="2">
                            <select dojoType="dojox.form.DropDownSelect" name="countryCode" id="countryCode" hasDownArrow="true" style="width:100%;">
                                <option value="<%= Locale.CANADA.getCountry() %>"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                                <option value="<%= Locale.US.getCountry() %>"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td><label id="date">Date:</label></td>
                        <td colspan="2"><input constraints="{datePattern:'EEE, MMMM dd'}" dojoType="dijit.form.DateTextBox" id="date" name="date" required="true" style="width:100%;" type="text" /></td>
                    </tr>
                    <tr>
                        <td><label id="time">Time:</label></td>
                        <td colspan="2"><input constraints="{visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}" dojoType="dijit.form.TimeTextBox" id="time" name="time" required="true" style="width:100%;" type="text" value="T07:00"/></td>
                    </tr>
                    <tr>
                        <td><label id="quantity">For:</label></td>
                        <td colspan="2" style="vertical-align:top;">
                            <input constraints="{min:1,places:0}" dojoType="dijit.form.NumberSpinner" id="quantity" name="quantity" style="width:5em;" type="text" value="4" />
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
                                onclick="javascript:localModule.switchPane('pane1','pane2');"
                                style="color:black;"
                                title="Share your contact details"
                            >Next &raquo;</button>
                            <br/>
                            <span class="hint">Your account details</span>
                        </td>
                    </tr>
                </table>
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
                <div class="step goal">&nbsp;</div>
            </div>
            <div class="content">
                <div class="title">20 golf courses will be contacted on your behalf. How do we reach you?</div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="vertical-align:top;"><label id="email0">Your email:</label></td>
                        <td style="text-align: right;">
                            <input constraints="{}" dojoType="dijit.form.TextBox" id="email0" name="email0" required="true" style="width:100%;" type="text" />
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
                            <td><input constraints="{}" dojoType="dijit.form.TextBox" id="email1" name="email1" required="true" style="width:100%;" type="text" /></td>
                            <td>
                                <button
                                    dojoType="dijit.form.Button"
                                    iconClass="silkIcon silkIconAdd"
                                    id="friendButton1"
                                    onclick="javascript:localModule.manageFriendRow(1);"
                                    showLabel="false"
                                    title="Add another email address"
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
                        <td style="text-align:left;"><a href="javascript:localModule.switchPane('pane2','pane1');">&laquo; back</a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                onclick="javascript:localModule.switchPane('pane2','pane3');"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "shared_locale_view_map_link", locale) %>"
                            >Next &raquo;</button>
                            <br/>
                            <span class="hint">Optional extras</span>
                        </td>
                    </tr>
                </table>
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
                <div class="step goal">&nbsp;</div>
            </div>
            <div class="content">
                <div class="title">Extras:</div>
                <div style="margin-left:20px;">
                    <div>
                        <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberSpinner" id="motorCart" name="motorCart" style="width:6em;" type="text" value="0" />
                        <label for="motorCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_motorCartOptions_label", locale) %></label>
                    </div>
                    <div>
                        <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberSpinner" id="pullCart" name="pullCart" style="width:6em;" type="text" value="0" />
                        <label for="pullCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_pullCartOptions_label", locale) %></label>
                    </div>
                </div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="javascript:localModule.switchPane('pane3','pane2');">&laquo; back</a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconAccept"
                                onclick="javascript:localModule.sendRequest();"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "shared_locale_view_map_link", locale) %>"
                            >Send request</button>
                            <br />
                            <span class="hint">Broadcast your request</span>
                        </td>
                    </tr>
                </table>
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
                <div class="step activeGoal">&nbsp;</div>
            </div>
            <div class="content">
                <div class="title">Thank you!</div>
                <div class="comment">You should receive an email at joe.blow@somewhere.moon shortly.</div>
                <div class="comment">If you have not receive an email from ezToff after 20 minutes, please check our <a href="http://ezToff.com/faq-<%= localeId %>">FAQ</a> or contact <a href="mailto:support@eztoff.com">support@eztoff.com</a>.</div>
                <table cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td style="text-align:left;"><a href="javascript:localModule.switchPane('pane4','pane1');">&laquo; Schedule another tee-off</a></td>
                    </tr>
                </table>
            </div>
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
        // dojo.require("dijit.form.FilteringSelect");
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
    localModule.init = function() {
        var yesterday = new Date();
        var tomorrow = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        tomorrow.setDate(tomorrow.getDate() + 1);
        var dateField = dijit.byId("date");
        dateField.set("value", tomorrow);
        // dateField.constraints.min = yesterday; // ??? why is reported as an invalid date?

        <% if (postalCode == null || postalCode.length() == 0) {
        %>dojo.style("postalCodeRow", "display", "");
        dojo.style("countryCodeRow", "display", "");<%
        }
        else {
        %>dijit.byId("postalCode").set("value", "<%= postalCode %>");<%
        } %>
        dijit.byId("countryCode").set("value", "<%= countryCode %>");

        var supportGeoLocation = navigator.geolocation;
        dijit.byId("detectLocationButton").set("disabled", !supportGeoLocation);
    };
    localModule.switchPane = function(source, target) {
        // create two animations
        var anim1 = dojo.fx.wipeOut({ node: source, easing: dojo.fx.easing.expoOut });
        var anim2 = dojo.fx.wipeIn({ node: target, easing: dojo.fx.easing.expoIn });
        // and play them at the same moment
        dojo.fx.combine([anim1, anim2]).play();
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
    localModule._friendRowNb = 1;
    localModule.manageFriendRow = function(rowIdx) {
        if (rowIdx == localModule._friendRowNb) {
            var button = dijit.byId("friendButton" + rowIdx);
            button.set("iconClass", "silkIcon silkIconDelete");
            button.set("title", "Remove this email address");
            localModule._friendRowNb ++;
            var fIdx = localModule._friendRowNb;
            // var n = dojo.create("tr", { id: "friendRow" + fIdx, innerHTML: localModule._friendRowDefinition.join(fIdx) }, dojo.byId("friendList"));
            var row = dojo.create("tr", { id: "friendRow" + fIdx }, dojo.byId("friendList"));
            dojo.create("label", { forAttr: "email" + fIdx, innerHTML: "Email:" }, dojo.create("td", null, row));
            dojo.create("td", null, row).appendChild(new dijit.form.TextBox({ id: "email" + fIdx, name: "email" + fIdx, required: false, style: "width:100%" }).domNode);
            dojo.create("td", null, row).appendChild(new dijit.form.Button({ iconClass: "silkIcon silkIconAdd", id: "friendButton" + fIdx, onClick: function() { localModule.manageFriendRow(fIdx); }, showLabel: false, title: "Add another email address" }).domNode);
        }
        else {
            var fIdx = localModule._friendRowNb;
            for (var i=rowIdx; i < fIdx; i++) {
                dijit.byId("email" + i).set("value", dijit.byId("email" + (i + 1)).get("value"));
            }
            dijit.byId("email" + fIdx).destroy();
            dijit.byId("friendButton" + fIdx).destroy();
            dojo.destroy("friendRow" + fIdx);
            localModule._friendRowNb --;
            fIdx --;
            var button = dijit.byId("friendButton" + fIdx);
            button.set("iconClass", "silkIcon silkIconAdd");
            button.set("title", "Add another email address");
        }
    }
    localModule.sendRequest = function() {
        dijit.byId("widgetOverlay").show();
        setTimeout(function() {
            localModule.switchPane('pane3','pane4');
            dijit.byId("widgetOverlay").hide();
        },
        2000);
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
