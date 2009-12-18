<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="com.dyuproject.openid.OpenIdUser"
    import="com.dyuproject.openid.RelyingParty"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="javamocks.io.MockOutputStream"
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Location"
    import="twetailer.dao.BaseOperations"
    import="twetailer.dao.ConsumerOperations"
    import="twetailer.dao.LocationOperations"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Data access
    BaseOperations _baseOperations = new BaseOperations();
    ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    LocationOperations locationOperations = _baseOperations.getLocationOperations();

    // Consumer attributes
    OpenIdUser user = RelyingParty.getInstance().discover(request); // null; // FIXME:
    Long consumerKey = (Long) user.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID); // 336L; // FIXME:
    Consumer consumer = consumerOperations.getConsumer(consumerKey);
    String openId = consumer.getOpenID() == null ? "" : consumer.getOpenID();
    String name = consumer.getName() == null ? "" : consumer.getName();
    // Location location = locationOperations.getLocation(consumer.getLocationKey());
    // String country = location.getCountryCode();
    // String language = consumer.getLanguage();
    String email = consumer.getEmail() == null ? "" : consumer.getEmail();
    String jabberId = consumer.getJabberId() == null ? "" : consumer.getJabberId();
    String twitterId = consumer.getTwitterId() == null ? "" : consumer.getTwitterId();

    MockOutputStream serializedConsumer = new MockOutputStream();
    consumer.toJson().toStream(serializedConsumer, false);
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
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><style type="text/css">
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/tundra/tundra.css";
        @import "/js/dojo/dojox/grid/resources/Grid.css";
        @import "/js/dojo/dojox/grid/resources/tundraGrid.css";
        @import "/css/console.css";
    </style><%
    } // endif (useCDN)
    %>
</head>
<body class="tundra">

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "ui_splash_screen_message", locale) %></span></div>
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
    <script type="text/javascript">
    dojo.addOnLoad(function(){
        /*
        dojo.require("dijit.layout.AccordionContainer");
        dojo.require("dijit.layout.TabContainer");
        dojo.require("dojox.grid.DataGrid");
        dojo.require("dojox.grid.cells");
        dojo.require("dijit.form.Form");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.form.TimeTextBox");
        dojo.require("dijit.form.NumberTextBox");
        dojo.require("dijit.form.NumberSpinner");
        dojo.require("dijit.form.CurrencyTextBox");
        dojo.require("dijit.form.ValidationTextBox");
        dojo.require("dijit.form.FilteringSelect");
        dojo.require("dijit.form.CheckBox");
        dojo.require("dijit.form.MultiSelect");
        dojo.require("dijit.form.Textarea");
        // dojo.require("dijit.Editor");
        */
        dojo.addOnLoad(function(){
            dojo.require("dijit.Dialog");
            dojo.require("dijit.layout.BorderContainer");
            dojo.require("dijit.layout.ContentPane");
            dojo.require("dijit.layout.TabContainer");
            dojo.require("dijit.form.Form");
            dojo.require("dijit.form.Button");
            dojo.require("dijit.form.TextBox");
            dojo.require("dijit.form.NumberTextBox");
            dojo.require("dijit.form.FilteringSelect");
            dojo.require("twetailer.Console");
            dojo.require("dojo.parser");
            dojo.addOnLoad(function(){
                dojo.parser.parse();
                var userLocale = "en"; // "<%= localeId %>"
                twetailer.Console.init(userLocale);
                twetailer.Console.registerConsumer(<%= serializedConsumer.getStream() %>);
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
            dojoType="dijit.layout.TabContainer"
            id="centerZone"
            region="center"
            tabstrip="true"
        >
            <script type="dojo/connect" event="selectChild">
            switch(this.selectedChildWidget.id) {
            case "consumerProfilePane":
                var consumerNameField = dijit.byId("consumerName");
                var handle1 = dojo.connect(consumerNameField, "onChange", function() { dijit.byId("consumerInfoUpdateButton").attr("disabled", false); dojo.disconnect(handle1); });
                var handle2 = dojo.connect(dijit.byId("consumerCountry"), "onChange", function() { dijit.byId("consumerInfoUpdateButton").attr("disabled", false); dojo.disconnect(handle2); });
                var handle3 = dojo.connect(dijit.byId("consumerLanguage"), "onChange", function() { dijit.byId("consumerInfoUpdateButton").attr("disabled", false); dojo.disconnect(handle3); });
                consumerNameField.focus();
                break;
            }
        </script>
            <div
                dojoType="dijit.layout.ContentPane"
                id="consumerCreateDemandPane"
                selected="true"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer_createDemand", locale) %>"
            >
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="consumerListDemandPane"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer_listDemand", locale) %>"
            >
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="consumerListProposalPane"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer_listProposal", locale) %>"
            >
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="consumerProfilePane"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer_profile", locale) %>"
            >
                <form id="consumerInformation" onsubmit="return false;">
                    <fieldset class="consumerInformation">
                        <legend><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_first_field_group", locale) %></legend>
                        <div>
                            <label for="consumerOpenId"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_openid", locale) %></label><br/>
                            <input dojoType="dijit.form.TextBox" id="consumerOpenId" name="<%= Consumer.OPEN_ID %>" readonly="true" style="width:100%;" type="text" value="<%= openId %>" /><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_openid", locale) %></span>
                        </div>
                        <div>
                            <label for="consumerName"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_name", locale) %></label><br/>
                            <input dojoType="dijit.form.TextBox" id="consumerName" name="<%= Consumer.NAME %>" style="width:30em;" type="text" value="<%= name %>" /><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_name", locale) %></span>
                        </div>
                        <div style="float:left;margin-right:1em;">
                            <label for="consumerCountry"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_country", locale) %></label><br/>
                            <select dojoType="dijit.form.FilteringSelect" id="consumerCountry" name="<%= Location.COUNTRY_CODE %>">
                                <option value="CA" selected="true"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                                <!--option value="US"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option-->
                                <!--option value="FR"><%= LabelExtractor.get(ResourceFileId.master, "country_FR", locale) %></option-->
                            </select><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_country", locale) %></span>
                        </div>
                        <div style="float:left;">
                            <label for="consumerLanguage"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_language", locale) %></label><br/>
                            <input id="consumerLanguage" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_language_selector", locale) %>" />
                            <script type="text/javascript">
                            dojo.require("domderrien.i18n.LanguageSelector");
                            dojo.addOnLoad(function() { domderrien.i18n.LanguageSelector.createSelector("consumerLanguage", "<%= Consumer.LANGUAGE %>", [<%
                                ResourceBundle languageList = LocaleController.getLanguageListRB();
                                Enumeration<String> keys = languageList.getKeys();
                                while(keys.hasMoreElements()) {
                                    String key = keys.nextElement();
                                    %>{value:"<%= key %>",label:"<%= languageList.getString(key) %>"}<%
                                    if (keys.hasMoreElements()) {
                                        %>,<%
                                    }
                                }
                                %>], "<%= localeId %>", null, function() {}) });
                            </script><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_language", locale) %></span>
                        </div>
                        <br style="clear: both;" />
                    </fieldset>
                    <fieldset class="consumerInformation">
                        <legend><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_second_field_group", locale) %></legend>
                        <div>
                            <label for="consumerEmail"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_email", locale) %></label><br/>
                            <input dojoType="dijit.form.TextBox" id="consumerEmail" name="<%= Consumer.EMAIL %>" style="width:30em;" type="text" value="<%= email %>" />
                            <input dojoType="dijit.form.NumberTextBox" id="consumerEmailCode" name="<%= Consumer.EMAIL %>Code" style="width:10em;display:none;" type="text" />
                            <button
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconVerify"
                                id="consumerEmailButton"
                                onclick="twetailer.Console.verifyConsumerCode('<%= Consumer.EMAIL %>','consumerEmail');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "verify_button", locale) %></button><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_email", locale) %></span>
                        </div>
                        <div>
                            <label for="consumerJabberId"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_jabber", locale) %></label><br/>
                            <input dojoType="dijit.form.TextBox" id="consumerJabberId" name="<%= Consumer.JABBER_ID %>" style="width:30em;" type="text" value="<%= jabberId %>" />
                            <input dojoType="dijit.form.NumberTextBox" id="consumerJabberIdCode" name="<%= Consumer.JABBER_ID %>Code" style="width:10em;display:none;" type="text" />
                            <button
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconVerify"
                                id="consumerJabberIdButton"
                                onclick="twetailer.Console.verifyConsumerCode('<%= Consumer.JABBER_ID %>','consumerJabberId');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "verify_button", locale) %></button><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_jabber", locale) %></span>
                        </div>
                        <div>
                            <label for="consumerTwitterId"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_twitter", locale) %></label><br/>
                            <input dojoType="dijit.form.TextBox" id="consumerTwitterId" name="<%= Consumer.TWITTER_ID %>" style="width:30em;" type="text" value="<%= twitterId %>" />
                            <input dojoType="dijit.form.NumberTextBox" id="consumerTwitterIdCode" name="<%= Consumer.TWITTER_ID %>Code" style="width:10em;display:none;" type="text" />
                            <button
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconVerify"
                                id="consumerTwitterIdButton"
                                onclick="twetailer.Console.verifyConsumerCode('<%= Consumer.TWITTER_ID %>','consumerTwitterId');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "verify_button", locale) %></button><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_twitter", locale) %></span>
                        </div>
                    </fieldset>
                    <div style="text-align:center;">
                        <button
                            dojoType="dijit.form.Button"
                            iconClass="silkIcon silkIconUpdate"
                            onclick="twetailer.Console.updateConsumerInformation();"
                        ><%= LabelExtractor.get(ResourceFileId.third, "update_button", locale) %></button>
                    </div>
                </form>
            </div>
            <!--
            <div
                dojoType="dijit.layout.ContentPane"
                id="saleAssociateListDemandPane"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_saleAssociate_listDemand", locale) %>"
            >
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="saleAssociateListProposalPane"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_saleAssociate_listDemand", locale) %>"
            >
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="saleAssociateProfilePane"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_saleAssociate_listDemand", locale) %>"
            >
            </div>
            -->
            <div
                dojoType="dijit.layout.ContentPane"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_needHelp", locale) %>"
            >
            </div>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_copyright", locale) %>
        </div>
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
