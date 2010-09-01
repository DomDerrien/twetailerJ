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
    <style type="text/css">
        @import url(http://fonts.googleapis.com/css?family=Droid+Sans);<%
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
        @import "/css/ase/widget.css";
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
            <div dojoType="dijit.form.Form" id="form1" class="content">
                <div class="brand">Community Buying Network</div>
                <div class="title">What are you going to buy?</div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tr>
                        <td style="vertical-align:top; padding-top:7px;"><label for="tags">Details:</label></td>
                        <td>
                            <textarea dojoType="dijit.form.Textarea" id="tags" name="tags" rows="3" style="width:100%;min-height:48px;font-family:'Droid Sans', arial, serif;font-size:12px;"></textarea><br/>
                            <div
                                iconClass="silkIcon silkIconHelp"
                                dojoType="dijit.form.DropDownButton"
                                style="float:right;margin-right:-1px;"
                            >
                                <span>Samples</span>
                                <div
                                    dojoType="dijit.TooltipDialog"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "sep_demand_samples_button", locale) %>"
                                >
                                    <li>Samsung HDTV UN46C9000</li>
                                    <li>Jonas Brothers CD album </li>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="quantity">Quantity:</label></td>
                        <td><input constraints="{min:1,places:0}" dojoType="dijit.form.NumberSpinner" id="quantity" name="quantity" style="width:5em;" required="true" type="text" value="1" /></td>
                    </tr>
                </table>
                <div class="comment">
                    Tell us What you want, Where abouts you would like to find it, When you need it for and Who you are so we can communicate with you once we find it.
                </div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                id="next1"
                                onclick="javascript:localModule.switchPane(1, 2);"
                                style="color:black;"
                                title="Where abouts?"
                            >Next &raquo;</button>
                            <br/>
                            <span class="hint">Where abouts?</span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step active">What</div>
                    <div class="step inactive">Where</div>
                    <div class="step inactive">When</div>
                    <div class="step inactive">Who</div>
                </div>
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
        </div>
        <div id="pane2" style="display:none;">
            <div dojoType="dijit.form.Form" id="form2" class="content">
                <div class="brand">Community Buying Network</div>
                <div class="title">Where abouts?</div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tr>
                        <td><label for="postalCode">Postal code:</label></td>
                        <td>
                            <input
                                dojoType="dijit.form.ValidationTextBox"
                                id="postalCode"
                                name="postalCode"
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
                    <tr>
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
                        <td><label for="countryCode">Within:</label></td>
                        <td colspan="2">
                            <input constraints="{min:5,max:100,places:0}" dojoType="dijit.form.NumberSpinner" id="range" name="range" style="width:5em;" type="text" value="25" /> km
                        </td>
                    </tr>
                </table>
                <div class="comment">
                    Tell us What you want, Where abouts you would like to find it, When you need it for and Who you are so we can communicate with you once we find it.
                </div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="javascript:localModule.switchPane(2, 1);">&laquo; back</a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                id="next2"
                                onclick="javascript:localModule.switchPane(2, 3);"
                                style="color:black;"
                                title="When would you like it by?"
                            >Next &raquo;</button>
                            <br/>
                            <span class="hint">When would you like it by?</span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step inactive">What</div>
                    <div class="step active">Where</div>
                    <div class="step inactive">When</div>
                    <div class="step inactive">Who</div>
                </div>
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
        </div>
        <div id="pane3" style="display:none;">
            <div dojoType="dijit.form.Form" id="form3" class="content">
                <div class="brand">Community Buying Network</div>
                <div class="title">When would you like it by?</div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tr>
                        <td><label for="expirationDate">Expires:</label></td>
                        <td colspan="2"><input constraints="{datePattern:'EEE, MMMM dd yyyy'}" dojoType="dijit.form.DateTextBox" id="expirationDate" name="expirationDate" required="true" style="width:100%;" type="text" /></td>
                    </tr>
                    <tr>
                        <td><label for="expirationTime">Time:</label></td>
                        <td colspan="2"><input constraints="{visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}" dojoType="dijit.form.TimeTextBox" id="expirationTime" name="expirationTime" required="true" style="width:100%;" type="text" value="T00:00" /></td>
                    </tr>
                </table>
                <div class="comment">
                    Tell us What you want, Where abouts you would like to find it, When you need it for and Who you are so we can communicate with you once we find it.
                </div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="javascript:localModule.switchPane(3, 2);">&laquo; back</a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                id="next3"
                                onclick="javascript:localModule.switchPane(3, 4);"
                                style="color:black;"
                                title="Who should we contact?"
                            >Next &raquo;</button>
                            <br/>
                            <span class="hint">Who should we contact?</span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step inactive">What</div>
                    <div class="step inactive">Where</div>
                    <div class="step active">When</div>
                    <div class="step inactive">Who</div>
                </div>
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
        </div>
        <div id="pane4" style="display:none;">
            <div dojoType="dijit.form.Form" id="form4" class="content">
                <div class="brand">Community Buying Network</div>
                <div class="title">Who should we contact?</div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tbody id="friendList">
                        <tr id="friendRow0">
                            <td style="vertical-align:top;"><label for="email0">Email address:</label></td>
                            <td style="text-align: right;" colspan="2">
                                <input
                                    dojoType="dijit.form.ValidationTextBox"
                                    id="email0"
                                    invalidMessage="Invalid Email Address"
                                    name="email0"
                                    placeHolder="email@example.com"
                                    regExp="<%= emailRegExp %>"
                                    required="true"
                                    style="width:100%;"
                                    trim="true"
                                    type="text"
                                />
                            </td>
                        </tr>
                        <tr id="friendRow1">
                            <td><label for="email1">CC others:</label></td>
                            <td style="text-align: right;">
                                <input
                                    dojoType="dijit.form.ValidationTextBox"
                                    id="email1"
                                    invalidMessage="Invalid Email Address"
                                    name="email1"
                                    placeHolder="email@example.com"
                                    regExp="<%= emailRegExp %>"
                                    required="false"
                                    style="width:100%;"
                                    trim="true"
                                    type="text"
                                />
                            </td>
                            <td style="width:20px;padding-right:0px !important;">
                                <button
                                    dojoType="dijit.form.Button"
                                    iconClass="silkIcon silkIconAdd"
                                    id="friendButton1"
                                    onclick="localModule.manageFriendRow(1);"
                                    showLabel="false"
                                    title="Add another email address"
                                ></button>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div class="comment">
                    Tell us What you want, Where abouts you would like to find it, When you need it for and Who you are so we can communicate with you once we find it.
                </div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="javascript:localModule.switchPane(4, 3);">&laquo; back</a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                id="next4"
                                onclick="javascript:localModule.switchPane(4, 5);"
                                style="color:black;"
                                title="Post request"
                            >Next &raquo;</button>
                            <br/>
                            <span class="hint">Post request</span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step inactive">What</div>
                    <div class="step inactive">Where</div>
                    <div class="step inactive">When</div>
                    <div class="step active">Who</div>
                </div>
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
        </div>
        <div id="pane5" style="display:none;">
            <div class="content">
                <div class="brand">Community Buying Network</div>
                <div class="title">Check your email</div>
                <div class="comment">
                    Thanks for shopping on the Community Buying Network.
                    We'll be sending you an email shortly to confirm your request and
                    hope to follow-up shortly with Proposals from local retailers.
                </div>
                <table cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td style="text-align:center;">
                            <button
                                dojoType="dijit.form.Button"
                                onclick="javascript:localModule.switchPane(5, 1);"
                                style="color:black;"
                                title="Make another request"
                            >&laquo; Make another request</button>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step inactive">What</div>
                    <div class="step inactive">Where</div>
                    <div class="step inactive">When</div>
                    <div class="step inactive">Who</div>
                </div>
                <div class="poweredBy">Powered by <a href="http://AnotherSocialEconomy.com" target="_blank">AnotherSocialEconomy.com</a></div>
            </div>
        </div>
    </div>

    <div
       color="darkgrey"
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
        dojo.require("dijit.TooltipDialog");
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
        var inOneMonth = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        inOneMonth.setMonth(inOneMonth.getMonth() + 1);
        var dateField = dijit.byId("expirationDate");
        dateField.set("value", inOneMonth);
        dateField.constraints.min = yesterday; // ??? why is reported as an invalid date?

        dijit.byId("tags").focus();
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
        case 1: dijit.byId("tags").focus(); break;
        case 2: dijit.byId("postalCode").focus(); break;
        case 3: dijit.byId("next3").focus(); break;
        case 4: dijit.byId("email0").focus(); break;
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
    localModule._friendRowNb = 1;
    localModule.manageFriendRow = function(rowIdx) {
        if (rowIdx == localModule._friendRowNb) {
            var button = dijit.byId("friendButton" + rowIdx);
            button.set("iconClass", "silkIcon silkIconDelete");
            button.set("title", "Remove this email address");
            localModule._friendRowNb ++;
            var fIdx = localModule._friendRowNb;
            var row = dojo.create("tr", { id: "friendRow" + fIdx }, dojo.byId("friendList"));
            dojo.create("label", { forAttr: "email" + fIdx, innerHTML: "Email:" }, dojo.create("td", null, row));
            dojo.create("td", null, row).appendChild(new dijit.form.ValidationTextBox({ id: "email" + fIdx, invalidMessage: "Invalid Email Address", name: "email" + fIdx, placeHolder: "email@example.com", required: false, style: "width:100%", trim: true }).domNode);
            dojo.create("td", { style: "width:20px;padding-right:0px !important;" }, row).appendChild(new dijit.form.Button({ iconClass: "silkIcon silkIconAdd", id: "friendButton" + fIdx, onClick: function() { localModule.manageFriendRow(fIdx); }, showLabel: false, title: "Add another email address" }).domNode);
            dijit.byId("email" + fIdx).set("regExp", dijit.byId('email1').get("regExp")); // Workaroud: otherwise, it seems the regExp value is missinterpreted!
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
            <%= Demand.META_DATA %>:"{pullCart:" + dijit.byId("pullCart").get("value") + ",golfCart:" + dijit.byId("motorCart").get("value") + "}"
        };
        var cc = [];
        for (var i=1; i<=localModule._friendRowNb; i++) {
            var email = dijit.byId("email" + i);
            if (0 < email.length) {
                cc.push(email);
            }
        }
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
                    localModule.switchPane(4, 5);
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

    <script src="http://maps.google.com/maps/api/js?sensor=false&language=<%= localeId %>" type="text/javascript"></script>

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
