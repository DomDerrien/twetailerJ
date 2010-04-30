<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
    import="twetailer.j2ee.BaseRestlet"
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
    OpenIdUser user = BaseRestlet.getLoggedUser(request);
    Long consumerKey = (Long) user.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID);
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
%><html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="<%= localeId %>">
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "ui_application_name", locale) %></title>
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.third, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <%
    if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/Grid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/tundraGrid.css";
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
        @import "/js/dojo/dojox/layout/resources/FloatingPane.css";
        @import "/js/dojo/dojox/layout/resources/ExpandoPane.css";
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
        djConfig="parseOnLoad: false, isDebug: true, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    }
    else { // elif (!useCDN)
    %><script
        djConfig="parseOnLoad: false, isDebug: false, baseUrl: '/js/dojo/dojo/', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="/js/dojo/dojo/dojo.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/jsp_includes/banner_protected.jsp"></jsp:include>
        <div
            dojoType="dijit.layout.TabContainer"
            id="centerZone"
            region="center"
            tabstrip="true"
        >
            <script type="dojo/connect" event="selectChild">
            switch(this.selectedChildWidget.id) {
            case "consumerProfilePane":
                dijit.byId("consumerName").focus();
                break;
            }
            </script>
            <div
                dojoType="dijit.layout.ContentPane"
                id="consumerCreateDemandPane"
                iconClass="silkIcon silkIconDemand"
                selected="true"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer_createDemand", locale) %>"
            >
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="consumerListDemandPane"
                iconClass="silkIcon silkIconDemandList"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer_listDemand", locale) %>"
            >
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="consumerListProposalPane"
                iconClass="silkIcon silkIconProposalList"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer_listProposal", locale) %>"
            >
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="consumerProfilePane"
                iconClass="silkIcon silkIconProfile"
                title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer_profile", locale) %>"
            >
                <form id="consumerInformation" onsubmit="return false;">
                    <fieldset class="entityInformation">
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
                                <option value="US"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
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
                    <fieldset class="entityInformation">
                        <legend><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_second_field_group", locale) %></legend>
                        <div>
                            <label for="consumerEmail"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_email", locale) %></label><br/>
                            <input
                                dojoType="dijit.form.ValidationTextBox"
                                id="consumerEmail"
                                invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "consumer_info_invalid_email_message", locale) %>"
                                name="<%= Consumer.EMAIL %>"
                                onkeyup="twetailer.Console.controlVerifyButtonState('<%= Consumer.EMAIL %>','consumerEmail');"
                                regExp="[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}"
                                style="width:30em;"
                                type="text"
                                value="<%= email %>"
                            />
                            <button
                                disabled="true"
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconVerify"
                                id="consumerEmailVerifyButton"
                                onclick="twetailer.Console.verifyConsumerCode('<%= Consumer.EMAIL %>','consumerEmail');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "verify_button", locale) %></button>
                            <input dojoType="dijit.form.NumberTextBox" id="consumerEmailCode" name="<%= Consumer.EMAIL %>Code" style="width:10em;display:none;" type="text" />
                            <button<% if (email == null || "".equals(email)) { %>
                                disabled="true"<% } %>
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconCancel"
                                id="consumerEmailResetButton"
                                onclick="twetailer.Console.resetConsumerCode('<%= Consumer.EMAIL %>','consumerEmail');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "clear_button", locale) %></button><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_email", locale) %></span>
                        </div>
                        <div>
                            <label for="consumerJabberId"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_jabber", locale) %></label><br/>
                            <input
                                dojoType="dijit.form.TextBox"
                                id="consumerJabberId"
                                name="<%= Consumer.JABBER_ID %>"
                                onkeyup="twetailer.Console.controlVerifyButtonState('<%= Consumer.JABBER_ID %>','consumerJabberId');"
                                style="width:30em;"
                                type="text"
                                value="<%= jabberId %>"
                            />
                            <button
                                disabled="true"
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconVerify"
                                id="consumerJabberIdVerifyButton"
                                onclick="twetailer.Console.verifyConsumerCode('<%= Consumer.JABBER_ID %>','consumerJabberId');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "verify_button", locale) %></button>
                            <input dojoType="dijit.form.NumberTextBox" id="consumerJabberIdCode" name="<%= Consumer.JABBER_ID %>Code" style="width:10em;display:none;" type="text" />
                            <button<% if (jabberId == null || "".equals(jabberId)) { %>
                                disabled="true"<% } %>
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconCancel"
                                id="consumerJabberIdResetButton"
                                onclick="twetailer.Console.resetConsumerCode('<%= Consumer.JABBER_ID %>','consumerJabberId');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "clear_button", locale) %></button><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_jabber", locale) %></span>
                        </div>
                        <div>
                            <label for="consumerTwitterId"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_label_twitter", locale) %></label><br/>
                            <input
                                dojoType="dijit.form.ValidationTextBox"
                                id="consumerTwitterId"
                                invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "consumer_info_invalid_twitter_message", locale) %>"
                                name="<%= Consumer.TWITTER_ID %>"
                                onkeyup="twetailer.Console.controlVerifyButtonState('<%= Consumer.TWITTER_ID %>','consumerTwitterId');"
                                regExp="[A-Za-z0-9_]+"
                                style="width:30em;"
                                trim="true"
                                type="text"
                                value="<%= twitterId %>"
                            />
                            <button
                                disabled="true"
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconVerify"
                                id="consumerTwitterIdVerifyButton"
                                onclick="twetailer.Console.verifyConsumerCode('<%= Consumer.TWITTER_ID %>','consumerTwitterId');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "verify_button", locale) %></button>
                            <input dojoType="dijit.form.NumberTextBox" id="consumerTwitterIdCode" name="<%= Consumer.TWITTER_ID %>Code" style="width:10em;display:none;" type="text" />
                            <button<% if (twitterId == null || "".equals(twitterId)) { %>
                                disabled="true"<% } %>
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconCancel"
                                id="consumerTwitterIdResetButton"
                                onclick="twetailer.Console.resetConsumerCode('<%= Consumer.TWITTER_ID %>','consumerTwitterId');"
                            ><%= LabelExtractor.get(ResourceFileId.third, "clear_button", locale) %></button><br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "consumer_info_hint_twitter", locale) %></span>
                        </div>
                    </fieldset>
                    <div style="text-align:center;">
                        <button
                            dojoType="dijit.form.Button"
                            iconClass="silkIcon silkIconUpdate"
                            id="consumerInfoUpdateButton"
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
                iconClass="silkIcon silkIconHelp"
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
        title="<%= LabelExtractor.get(ResourceFileId.third, "about_dialog_title", locale) %>"
        href="/jsp_includes/about.jsp"
    >
    </div>

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
            dojo.require("dijit.form.ValidationTextBox");
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