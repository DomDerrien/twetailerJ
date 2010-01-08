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
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.dto.Location"
    import="twetailer.dto.Store"
    import="twetailer.dto.SaleAssociate"
    import="twetailer.connector.BaseConnector.Source"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // useCDN = false; // To be included for runs in offline mode ++ begin/end

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%>
<%@page import="twetailer.dto.SaleAssociate"%>
<%@page import="twetailer.dto.Location"%>
<%@page import="twetailer.dto.Store"%><html>
<head>
    <title>Sale Associate Registration Page</title>
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
        dojo.require("dijit.Dialog");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.StackContainer");
        dojo.require("dijit.layout.ContentPane");
        dojo.require("dijit.form.Form");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.form.FilteringSelect");
        dojo.require("twetailer.Console");
        dojo.require("dojo.parser");
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            var userLocale = "<%= localeId %>";
            twetailer.Console.init(userLocale, true);
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
                    <li><a name="notImportantJustForStyle1"><%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer", locale) %></a></li>
                    <li><a name="notImportantJustForStyle2"><%= LabelExtractor.get(ResourceFileId.third, "navigation_sale_associate", locale) %></a></li>
                    <!--  Reverse order because they are right aligned -->
                    <li class="subItem"><a name="notImportantJustForStyle3"><%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %></a></li>
                    <li class="subItem"><a id="logoutLnk" href="/control/logout" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_sign_out", locale) %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_sign_out", locale) %></a></li>
                </ul>
            </div>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="centerZone" region="center">
            <div dojoType="dijit.layout.StackContainer" id="wizard" jsId="wizard" style="margin-left:25%;margin-right:25%;width:50%;">
                <div dojoType="dijit.layout.ContentPane" jsId="step0">
                    <fieldset class="consumerInformation">
                        <legend>Action Selection</legend>
                        <p>
                            <button disabled="true" dojoType="dijit.form.Button"><< Previous</button>
                            <button dojoType="dijit.form.Button" onclick="wizard.selectChild(step1);dijit.byId('<%= Location.POSTAL_CODE %>').focus();">New Location >></button>
                            <button dojoType="dijit.form.Button" onclick="wizard.selectChild(step2);dijit.byId('<%= Store.LOCATION_KEY %>').focus();">New Store >></button>
                            <button dojoType="dijit.form.Button" onclick="wizard.selectChild(step3);dijit.byId('<%= SaleAssociate.STORE_KEY %>').focus();">New Sale Associate >></button>
                        </p>
                    </fieldset>
                </div>
                <div dojoType="dijit.layout.ContentPane" jsId="step1" style="display:hidden;">
                    <fieldset class="consumerInformation">
                        <legend>Location Creation/Retrieval</legend>
                        <form id="locationInformation">
                            <div>
                                <label for="<%= Location.POSTAL_CODE %>">Postal Code</label><br/>
                                <input dojoType="dijit.form.TextBox" id="<%= Location.POSTAL_CODE %>" name="<%= Location.POSTAL_CODE %>" style="width:10em;" type="text" value="" />
                            </div>
                            <div>
                                <label for="<%= Location.COUNTRY_CODE %>">Country Code</label><br/>
                                <select dojoType="dijit.form.FilteringSelect" name="countryCode">
                                    <option value="CA" selected="true">Canada</option>
                                    <!--option value="US">United States of America</option-->
                                </select>
                            </div>
                        </form>
                        <p>
                            <button dojoType="dijit.form.Button" onclick="wizard.back();"><< Previous</button>
                            <button dojoType="dijit.form.Button" onclick="wizard.forward();registration.createLocation();dijit.byId('<%= Store.LOCATION_KEY %>').focus();">Next >></button>
                        </p>
                    </fieldset>
                </div>
                <div dojoType="dijit.layout.ContentPane" jsId="step2" style="display:hidden;">
                    <fieldset class="consumerInformation">
                        <legend>Store Creation</legend>
                        <form id="storeInformation">
                            <div>
                                <label for="<%= Store.LOCATION_KEY %>">Location Key</label><br/>
                                <input dojoType="dijit.form.TextBox" id="<%= Store.LOCATION_KEY %>" name="<%= Store.LOCATION_KEY %>" style="width:10em;" type="text" value="" />
                            </div>
                            <div>
                                <label for="<%= Store.NAME %>">Store Name</label><br/>
                                <input dojoType="dijit.form.TextBox" name="<%= Store.NAME %>" style="width:20em;" type="text" value="" />
                            </div>
                            <div>
                                <label for="<%= Store.ADDRESS %>">Address</label><br/>
                                <input dojoType="dijit.form.TextBox" name="<%= Store.ADDRESS %>" style="width:30em;" type="text" value="" />
                            </div>
                            <div>
                                <label for="<%= Store.PHONE_NUMBER %>">Phone Number</label><br/>
                                <input dojoType="dijit.form.TextBox" name="<%= Store.PHONE_NUMBER %>" style="width:10em;" type="text" value="" />
                            </div>
                        </form>
                        <p>
                            <button dojoType="dijit.form.Button" onclick="wizard.back();dijit.byId('<%= Location.POSTAL_CODE %>').focus();"><< Previous</button>
                            <button dojoType="dijit.form.Button" onclick="wizard.forward();registration.createStore();dijit.byId('<%= SaleAssociate.STORE_KEY %>').focus();">Next >></button>
                        </p>
                    </fieldset>
                    <fieldset class="consumerInformation">
                        <legend>Store Retreival</legend>
                        <ul id="storeList">
                        </ul>
                        <p>
                            <button dojoType="dijit.form.Button" onclick="registration.getStores();">Get Stores</button>
                        </p>
                    </fieldset>
                </div>
                <div dojoType="dijit.layout.ContentPane" jsId="step3" style="display:hidden;">
                    <fieldset class="consumerInformation">
                        <legend>Sale Associate Creation</legend>
                        <form id="saleAssociateInformation">
                            <div>
                                <label for="<%= SaleAssociate.STORE_KEY %>">Store Key</label><br/>
                                <input dojoType="dijit.form.TextBox" id="<%= SaleAssociate.STORE_KEY %>" name="<%= SaleAssociate.STORE_KEY %>" style="width:10em;" type="text" value="" />
                            </div>
                            <div>
                                <label for="<%= SaleAssociate.NAME %>">Associate Name</label><br/>
                                <input dojoType="dijit.form.TextBox" name="<%= SaleAssociate.NAME %>" style="width:20em;" type="text" value="" />
                            </div>
                            <div>
                                <label for="<%= SaleAssociate.EMAIL %>">E-mail Sddress</label><br/>
                                <input dojoType="dijit.form.TextBox" name="<%= SaleAssociate.EMAIL %>" style="width:30em;" type="text" value="" />
                            </div>
                            <div>
                                <label for="<%= SaleAssociate.TWITTER_ID %>">Twitter Name</label><br/>
                                <input dojoType="dijit.form.TextBox" name="<%= SaleAssociate.TWITTER_ID %>" style="width:10em;" type="text" value="" />
                            </div>
                            <div>
                                <label for="<%= SaleAssociate.PREFERRED_CONNECTION %>">Preferred Connection</label><br/>
                                <select dojoType="dijit.form.FilteringSelect" name="<%= SaleAssociate.PREFERRED_CONNECTION %>">
                                    <option value="<%= Source.twitter %>" selected="true">Twitter</option>
                                    <option value="<%= Source.jabber %>">Jabber/XMPP</option>
                                    <option value="<%= Source.mail %>">E-Mail</option>
                                </select>
                            </div>
                            <div>
                                <label for="<%= SaleAssociate.LANGUAGE %>">Language</label><br/>
                                <select dojoType="dijit.form.FilteringSelect" name="<%= SaleAssociate.LANGUAGE %>">
                                    <option value="en" selected="true">English</option>
                                    <!--option value="fr">French</option-->
                                </select>
                            </div>
                        </form>
                        <p>
                            <button dojoType="dijit.form.Button" onclick="wizard.back();dijit.byId('<%= Store.LOCATION_KEY %>').focus();"><< Previous</button>
                            <button dojoType="dijit.form.Button" onclick="wizard.forward();registration.createSaleAssociate();">Next >></button>
                        </p>
                    </fieldset>
                    <fieldset class="consumerInformation">
                        <legend>Sale Associate Retreival</legend>
                        <ul id="saleAssociateList">
                        </ul>
                        <p>
                            <button dojoType="dijit.form.Button" onclick="registration.getSaleAssociates();">Get Sale Associates</button>
                        </p>
                    </fieldset>
                </div>
                <div dojoType="dijit.layout.ContentPane" jsId="step4" style="display:hidden;">
                    <fieldset class="consumerInformation">
                        <legend>Repeat Again ;)</legend>
                        <p>The Sale Associate can now tweet to @twetailer to supply his/her own tags.</p>
                        <p>
                            <button dojoType="dijit.form.Button" onclick="wizard.selectChild(step1);dijit.byId('<%= Location.POSTAL_CODE %>').focus();"><< Another Location</button>
                            <button dojoType="dijit.form.Button" onclick="wizard.selectChild(step2);dijit.byId('<%= Store.LOCATION_KEY %>').focus();"><< Another Store</button>
                            <button dojoType="dijit.form.Button" onclick="wizard.selectChild(step3);dijit.byId('<%= SaleAssociate.STORE_KEY %>').focus();"><< Another Sale Associate</button>
                            <button disabled="true" dojoType="dijit.form.Button">Next >></button>
                        </p>
                    </fieldset>
                </div>
            </div>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_copyright", locale) %>
        </div>
    </div>
    <script type="text/javascript">
    var registration = new Object();
    registration.createLocation = function() {
        dojo.xhrPost({
            headers: { "content-type": "application/json" },
            putData: dojo.formToJson("locationInformation"),
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    dijit.byId("<%= Store.LOCATION_KEY %>").attr("value", response.resource.<%= Location.KEY %>);
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/API/Location"
        });
    },
    registration.createStore = function() {
        var data = dojo.formToObject("storeInformation");
        data.locationKey = parseInt(data.locationKey); // Otherwise it's passed as a String
        dojo.xhrPost({
            headers: { "content-type": "application/json" },
            putData: dojo.toJson(data),
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    dijit.byId("<%= SaleAssociate.STORE_KEY %>").attr("value", response.resource.<%= Store.KEY %>);
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/API/Store"
        });
    },
    registration.getStores = function() {
        var locationKey = parseInt(dijit.byId("<%= Store.LOCATION_KEY %>").attr("value"));
        if (locationKey.length == 0 || isNaN(locationKey)) {
            alert("You need to specify a valid Location key");
            dijit.byId("<%= Store.LOCATION_KEY %>").focus();
            return;
        }
        dojo.xhrGet({
            content: { <%= Store.LOCATION_KEY %>: locationKey },
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var placeHolder = dojo.byId("storeList");
                    placeHolder.innerHTML = "";
                    dojo.forEach(response.resources, function(store, i) {
                        var listItem = dojo.doc.createElement("li");
                        var onclickHandler =
                            "var saKeyField = dijit.byId(\"<%= SaleAssociate.STORE_KEY %>\");" +
                            "saKeyField.attr(\"value\", \"" + store.<%= Store.KEY %> + "\");" +
                            "saKeyField.focus();" +
                            "wizard.forward();" +
                            "return false;";
                        listItem.innerHTML =
                            "Name: <a href='#' onclick='" + onclickHandler + "'>" + store.<%= Store.NAME %> + "</a>, " +
                            "Address: " + store.<%= Store.ADDRESS %> + ", " +
                            "Phone Number: " + store.<%= Store.PHONE_NUMBER %>;
                        placeHolder.appendChild(listItem);
                    });
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/API/Store"
        });
    };
    registration.createSaleAssociate = function() {
        var data = dojo.formToObject("saleAssociateInformation");
        data.storeKey = parseInt(data.storeKey); // Otherwise it's passed as a String
        dojo.xhrPost({
            headers: { "content-type": "application/json" },
            putData: dojo.toJson(data),
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    // No visual feedback
                }
                else {
                    alert(response.exceptionMessage+"\nurl: "+ioArgs.url+"\n\n"+response.originalExceptionMessage);
                    wizard.back();
                }
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/API/SaleAssociate"
        });
    };
    registration.getSaleAssociates = function() {
        var storeKey = parseInt(dijit.byId("<%= SaleAssociate.STORE_KEY %>").attr("value"));
        if (storeKey.length == 0 || isNaN(storeKey)) {
            alert("You need to specify a valid Store key");
            dijit.byId("<%= SaleAssociate.STORE_KEY %>").focus();
            return;
        }
        dojo.xhrGet({
            content: { <%= SaleAssociate.STORE_KEY %>: storeKey },
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var placeHolder = dojo.byId("saleAssociateList");
                    placeHolder.innerHTML = "";
                    dojo.forEach(response.resources, function(saleAssociate, i) {
                        var listItem = dojo.doc.createElement("li");
                        listItem.innerHTML =
                            "Name: " + saleAssociate.<%= SaleAssociate.NAME %> + "</a>, " +
                            "E-mail Address: <a href='mailto:" + saleAssociate.<%= SaleAssociate.EMAIL %> + "'>" + saleAssociate.<%= SaleAssociate.EMAIL %> + "</a>, " +
                            "Twitter Name: <a href='http://twitter.com/" + saleAssociate.<%= SaleAssociate.TWITTER_ID %> +"' target='_blank'>" + saleAssociate.<%= SaleAssociate.TWITTER_ID %> + "</a>";
                        placeHolder.appendChild(listItem);
                    });
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/API/SaleAssociate"
        });
    };
    </script>

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
