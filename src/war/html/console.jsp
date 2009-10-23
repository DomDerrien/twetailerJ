<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LocaleController"
    import="com.google.appengine.api.users.User"
    import="com.google.appengine.api.users.UserService"
    import="com.google.appengine.api.users.UserServiceFactory"
%><%
    // Application settings
    ResourceBundle appSettings = ResourceBundle.getBundle("applicationSettings", Locale.getDefault());
    boolean useCDN = "y".equals(appSettings.getString("useCDN"));
    String cdnBaseURL = appSettings.getString("cdnBaseURL");

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Authenticated user detection
    UserService userService = UserServiceFactory.getUserService();
    User loggedUser = userService.getCurrentUser();
    String uri = request.getRequestURI(); // TODO: verify that the parameters are kept
%><html>
<head>
    <title><%= LabelExtractor.get("ui_application_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF8">
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
        <div><span><%= LabelExtractor.get("ui_splash_screen_message", locale) %></span></div>
    </div>

    <script type="text/javascript" src="/js/gears_init.js"></script>
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
    dojo.require("dijit.layout.BorderContainer");
    dojo.require("dijit.layout.AccordionContainer");
    dojo.require("dijit.layout.TabContainer");
    dojo.require("dijit.layout.ContentPane");
    dojo.require("dojox.grid.DataGrid");
    dojo.require("dojox.grid.cells");
    dojo.require("dijit.Dialog");
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
    dojo.require("twetailer.Console");
    dojo.require("domderrien.i18n.LanguageSelector");
    dojo.require("dojo.parser");
    dojo.addOnLoad(function(){
        dojo.parser.parse();
        twetailer.Console.init("labels", "<%= localeId %>", <%= loggedUser != null %>); // FIXME: ("@rwa.localizedLabelBaseFilename@", "<%= localeId %>", <%= loggedUser != null %>);
        dojo.fadeOut({
            node: "introFlash",
            delay: 500,
            onEnd: function() {
                dojo.style("introFlash", "display", "none");
            }
        }).play();
    });
    </script>

    <div
        dojoType="dijit.layout.BorderContainer"
        gutters="false"
        style="width: 100%; height: 100%;"
    >
        <div
            dojoType="dijit.layout.ContentPane"
            id="header"
            region="top"
        >
            <!-- Need to be in reverse order because this <div /> is "float: right;" -->
            <input id="languageSelector" />
            <script type="text/javascript">
            domderrien.i18n.LanguageSelector.createSelector("languageSelector", "globalCommand", [<%
                ResourceBundle languageList = LocaleController.getLanguageListRB();
                Enumeration<String> keys = languageList.getKeys();
                while(keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    %>{abbreviation:"<%= key %>",name:"<%= languageList.getString(key) %>"}<%
                    if (keys.hasMoreElements()) {
                        %>,<%
                    }
                }
                %>], "<%= localeId %>");
            </script>
            <a
                 class="globalCommand"
                 href="help-standalone.html"
                 onClick="dijit.byId('helpSystemDialog').show(); return false;"
                 target="_blank"
                 title="<%= LabelExtractor.get("ui_global_command_help", locale) %>"
            ><img src="/images/help.png" width="16" height="16" /></a>
            <%
            if (loggedUser == null) {
            %><a
                 class="globalCommand"
                 href="javascript:dijit.byId('checkPoint').show();"
                 title="<%= LabelExtractor.get("ui_global_command_login", locale) %>"
            ><img src="/images/key.png" width="16" height="16" /></a><%
            }
            else {
            %><a
                 class="globalCommand"
                 href="<%= userService.createLogoutURL(uri) %>"
                 title="<%= LabelExtractor.get("ui_global_command_logout", locale) %>"
            ><img src="/images/door_out.png" width="16" height="16" /></a><%
            }
            %><img height="48" src="/images/logo/logo-48x48.png" title="<%= LabelExtractor.get("ui_application_name", locale) %>" width="48" />
            <%= LabelExtractor.get("ui_application_name", locale) %>
        </div>

        <div
            dojoType="dijit.layout.TabContainer"
            id="pageSelectorSystem"
            region="center"
        >
            <div
                dojoType="dijit.layout.ContentPane"
                id="pageSelector-Consumer"
                title="<%= LabelExtractor.get("pageSelectorConsumer", locale) %>"
            >
                <div dojoType="dijit.layout.TabContainer" id="pageSelectorSystem-Consumer">
                    <div
                        dojoType="dijit.layout.BorderContainer"
                        gutters="false"
                        id="pageSelector-Consumer-All Products"
                        title="<%= LabelExtractor.get("pageSelectorConsumerAllProducts", locale) %>"
                    >
                        <div dojoType="dijit.layout.ContentPane" region="top" style="text-align: center; padding-top: 10px;">
                                <label class="a11y" for="consumerAllProductsCriteria"><%= LabelExtractor.get("consumerSearchCriteriaFieldLabel", locale) %></label>
                                <input
                                    class="searchField"
                                    dojoType="dijit.form.ValidationTextBox"
                                    id="consumerAllProductsSearchCriteria"
                                    invalidMessage="<%= LabelExtractor.get("consumerSearchCriteriaFieldInvalidMsg", new Object[] {3}, locale) %>"
                                    regExp=".{3,}"
                                    required="true"
                                    title="<%= LabelExtractor.get("consumerSearchCriteriaFieldLabel", locale) %>"
                                    type="text"
                                />
                                <button
                                    class="searchField"
                                    dojoType="dijit.form.Button"
                                    iconClass="silkIcon silkSearchIcon"
                                    id="consumerAllProductsSearchSubmit"
                                    onClick="twetailer.Console.searchConsumerProducts();"
                                ><%= LabelExtractor.get("consumerSearchButtonLabel", locale) %></button><br/>
                                <%= LabelExtractor.get("consumerSearchLocationInfoMsg", new Object[] {"<tbd", "FIXME select with JavaScript"}, locale) %>
                        </div>
                        <table
                            dojoType="dojox.grid.DataGrid"
                            elasticView="1"
                            id="consumerProductTable"
                            region="center"
                            rowSelector="20px"
                            rowsPerPage="10"
                            selectionMode="single"
                        >
                            <thead>
                                <tr>
                                    <th cellType="dojox.grid.cells.RowIndex" hidden="true" width="60px"><%= LabelExtractor.get("tableColumnIndex", locale) %></th>
                                    <th field="name" width="100px"><%= LabelExtractor.get("tableColumnName", locale) %></th>
                                    <th field="description" width="auto"><%= LabelExtractor.get("tableColumnDescription", locale) %></th>
                                    <th formatter="" width="200px"><%= LabelExtractor.get("tableColumnLocation", locale) %></th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <div
                        dojoType="dijit.layout.BorderContainer"
                        gutters="false"
                        id="pageSelector-Consumer-Your Requests"
                        title="<%= LabelExtractor.get("pageSelectorConsumerYourRequests", locale) %>"
                    >
                        <div dojoType="dijit.layout.ContentPane" region="top" style="text-align: center; padding-top: 10px;">
                            <p class="hiddenToAuthenticated">
                                You need to <a href="javascript:dijit.byId('checkPoint').show();">log in</a>
                                or you can create a <a href="javascript:alert('Not yet implemented');">new account</a>.
                            </p>
                            <span class="hiddenToNonAuthenticated">
                                <label class="searchField" for="consumerRequestsSelector">Selector:</label>
                                <select class="searchField" dojoType="dijit.form.FilteringSelect" id="consumerRequestsSelector">
                                    <option value="ALL">All Requests</option>
                                    <option value="PENDING" selected="true">Pending Requests</option>
                                    <option value="CANCELLED">Cancelled Requests</option>
                                    <option value="DELIVERED">Delivered Requests</option>
                                </select>
                                <button
                                    class="searchField"
                                    dojoType="dijit.form.Button"
                                    iconClass="silkIcon silkSearchIcon"
                                    id="consumerRequestsSearchSubmit"
                                    onClick="twetailer.Console.searchConsumerRequests();"
                                >Get Your Requests</button>
                            </span>
                        </div>
                        <table
                            dojoType="dojox.grid.DataGrid"
                            elasticView="1"
                            id="consumerRequestTable"
                            region="center"
                            rowSelector="20px"
                            rowsPerPage="10"
                            selectionMode="single"
                        >
                            <thead>
                                <tr>
                                    <th cellType="dojox.grid.cells.RowIndex" hidden="true" width="60px"><%= LabelExtractor.get("tableColumnIndex", locale) %></th>
                                    <th field="creationDate" width="100px"><%= LabelExtractor.get("tableColumnCreationDate", locale) %></th>
                                    <th editable="true" field="keywords" width="auto"><%= LabelExtractor.get("tableColumnCriteria", locale) %></th>
                                    <th get="twetailer.Console.getConsumerRequestLocation" width="200px"><%= LabelExtractor.get("tableColumnLocation", locale) %></th>
                                    <th
                                        cellType="dojox.grid.cells.Select"
                                        editable="true"
                                        field="expirationDelay"
                                        formatter="cpwr.ConsoleLogic.displayParameterStatus"
                                        options="1,2,3,4,5"
                                        values="1,2,3,4,5"
                                        width="100px"
                                    ><%= LabelExtractor.get("tableColumnExpiration", locale) %></th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <div
                        dojoType="dijit.layout.ContentPane"
                        id="pageSelector-Consumer-Your Account"
                        title="<%= LabelExtractor.get("pageSelectorConsumerYourAccount", locale) %>"
                    >
                        <p class="hiddenToAuthenticated">
                            Being not authenticated, your location is guessed by the system.
                            You need to <a href="javascript:dijit.byId('checkPoint').show();">log in</a>
                            or you can create a <a href="javascript:alert('Not yet implemented');">new account</a>
                            to be able to specify another location.
                        </p>
                        <p id="adviceToInstallGears">
                            Twetailer can use <a href="http://gears.google.com/?action=install&message=To%20improve%20your%20experience%20on%20Twetailer&return=http:%2f%2ftwetailer.appspot.com">Gears</a>,
                            a free browser plug-in that extends your browser, to guess your geographical location and to save your data for offline usage. We suggest you to install
                            <a href="http://gears.google.com/?action=install&message=To%20improve%20your%20experience%20on%20Twetailer&return=http:%2f%2ftwetailer.appspot.com">Gears</a>
                            from Google website.
                        </p>
                        <table class="attributeValueList" cellspacing="0" cellpadding="0">
                            <tr class="hiddenToNonAuthenticated">
                                <td class="title" colspan="2">Identification</td>
                            </tr>
                            <tr class="hiddenToNonAuthenticated">
                                <td class="attribute"><label class="label" for="consumerNickname">Name:</label></td>
                                <td class="value">
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="consumerProfileNickname"
                                        readonly="true"
                                        title="Name"
                                    />
                                </td>
                            </tr>
                            <tr class="hiddenToNonAuthenticated">
                                <td class="attribute"><label class="label" for="consumerEmail">E-mail:</label></td>
                                <td class="value">
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="consumerProfileEmail"
                                        readonly="true"
                                        title="Name"
                                    />
                                </td>
                            </tr>
                            <tr class="hiddenToNonAuthenticated">
                                <td class="attribute"><label class="label" for="consumerPhoneCell">Cellphone #:</label></td>
                                <td class="value">
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="consumerProfilePhoneCell"
                                        title="Name"
                                    />
                                </td>
                            </tr>
                            <tr class="hiddenToNonAuthenticated">
                                <td class="attribute"><label class="label" for="consumerPhoneHome">Home phone #:</label></td>
                                <td class="value">
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="consumerProfilePhoneHome"
                                        title="Name"
                                    />
                                </td>
                            </tr>
                            <tr>
                                <td class="title" colspan="2">Location</td>
                            </tr>
                            <tr class="hiddenToNonAuthenticated">
                                <td class="attribute"><label for="consumerStreet">Street:</label></td>
                                <td class="value">
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="consumerProfileStreet1"
                                        type="Text"
                                    /><br/>
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="consumerProfileStreet2"
                                        type="Text"
                                    />
                                </td>
                            </tr>
                            <tr>
                                <td class="attribute"><label for="consumerCity">City:</label></td>
                                <td class="value">
                                    <input
                                        dojoType="dijit.form.TextBox"
                                        id="consumerProfileCity"
                                        type="Text"
                                    />
                                </td>
                            </tr>
                            <tr>
                                <td class="attribute"><label class="label" for="consumerPostalCode">Postal code:</label></td>
                                <td class="value">
                                    <input
                                        class="postalCodeField"
                                        dojoType="dijit.form.ValidationTextBox"
                                        id="consumerProfilePostalCode"
                                    />
                                </td>
                            </tr>
                            <tr>
                                <td class="attribute"><label for="consumerCountry">Country:</label></td>
                                <td class="value">
                                    <select
                                        class="countryField"
                                        dojoType="dijit.form.FilteringSelect"
                                        id="consumerProfileCountry"
                                        style="width: 170px;"
                                        title="Country"
                                        value="_"
                                    ><!-- value set to "_" to be sure it will initialized with a different value later by the init() process -->
                                        <option value="CA">Canada</option>
                                        <option value="US">United States</option>
                                    </select><br/>
                                </td>
                            </tr>
                            <tr>
                                <td class="attribute"><label class="label" for="consumerGeoLatitude">Latitude:</label></td>
                                <td class="value">
                                    <input
                                        class="postalCodeField"
                                        dojoType="dijit.form.ValidationTextBox"
                                        id="consumerGeoLatitude"
                                    />
                                </td>
                            </tr>
                            <tr>
                                <td class="attribute"><label class="label" for="consumerGeoLongitude">Longitude:</label></td>
                                <td class="value">
                                    <input
                                        class="postalCodeField"
                                        dojoType="dijit.form.ValidationTextBox"
                                        id="consumerGeoLongitude"
                                    />
                                </td>
                            </tr>
                            <tr>
                                <td class="attribute"><label class="label" for="consumerDistance">Distance:</label></td>
                                <td class="value">
                                    <input
                                        class="distanceField"
                                        constraints="{min:5,max:100,places:0}"
                                        dojoType="dijit.form.NumberSpinner"
                                        id="consumerProfileDistance"
                                        title="Distance around"
                                        value="10"
                                    />
                                    <select
                                        class="distanceField"
                                        dojoType="dijit.form.FilteringSelect"
                                        id="consumerProfileDistanceUnit"
                                        title="Distance unit"
                                        value="km"
                                    >
                                        <option value="mi">mi</option>
                                        <option value="km">km</option>
                                    </select>
                                </td>
                            </tr>
                            <tr class="hiddenToNonAuthenticated">
                                <td colspan="2" style="text-align: center; padding: 10px 0;">
                                    <button
                                        dojoType="dijit.form.Button"
                                        iconClass="silkIcon silkAcceptIcon"
                                        onClick="alert('Not yet implemented!');"
                                    >Update record</button>
                                    <button
                                        dojoType="dijit.form.Button"
                                        iconClass="silkIcon silkSearchIcon"
                                        onClick="twetailer.Console.getGeoLocFromGears();"
                                    >Detect Location</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
            <div
                dojoType="dijit.layout.ContentPane"
                id="pageSelector-Retailer"
                title="<%= LabelExtractor.get("pageSelectorRetailer", locale) %>"
            >
                <div dojoType="dijit.layout.TabContainer" id="pageSelectorSystem-Retailer">
                    <div
                        dojoType="dijit.layout.BorderContainer"
                        gutters="false"
                        id="pageSelector-Retailer-All Requests"
                        title="<%= LabelExtractor.get("pageSelectorRetailerAllRequests", locale) %>"
                    >
                        <div dojoType="dijit.layout.ContentPane" region="top" style="text-align: center; padding-top: 10px;">
                            <label class="a11y" for="retailerAllRequestCriteria">Search criteria: </label>
                            <input
                                class="searchField"
                                dojoType="dijit.form.ValidationTextBox"
                                id="retailerAllRequestCriteria"
                                invalidMessage="At least one keyword of 3 characters is required."
                                regExp=".{3,}"
                                required="true"
                                title="Search criteria"
                                type="text"
                            />
                            <button
                                class="searchField"
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkSearchIcon"
                                onClick="twetailer.Console.searchRetailerRequests();"
                            >Scan requests</button>
                        </div>
                        <table
                            dojoType="dojox.grid.DataGrid"
                            elasticView="1"
                            id="retailerRequestTable"
                            region="center"
                            rowSelector="20px"
                            rowsPerPage="10"
                            selectionMode="single"
                        >
                            <thead>
                                <tr>
                                    <th cellType="dojox.grid.cells.RowIndex" hidden="true" width="60px"><%= LabelExtractor.get("tableColumnIndex", locale) %></th>
                                    <th field="creationDate" width="100px"><%= LabelExtractor.get("tableColumnCreationDate", locale) %></th>
                                    <th field="keywords" width="auto"><%= LabelExtractor.get("tableColumnCriteria", locale) %></th>
                                    <th get="twetailer.Console.getRetailerRequestLocation" width="200px"><%= LabelExtractor.get("tableColumnLocation", locale) %></th>
                                    <th
                                        cellType="dojox.grid.cells.Select"
                                        field="expirationDelay"
                                        formatter="cpwr.ConsoleLogic.displayParameterStatus"
                                        options="1,2,3,4,5"
                                        values="1,2,3,4,5"
                                        width="100px"
                                    ><%= LabelExtractor.get("tableColumnExpiration", locale) %></th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <div
                        dojoType="dijit.layout.ContentPane"
                        id="pageSelector-Retailer-Your Requests"
                        title="<%= LabelExtractor.get("pageSelectorRetailerYourRequests", locale) %>"
                    >
                        <p class="hiddenToAuthenticated">
                            You need to <a href="javascript:dijit.byId('checkPoint').show();">log in</a>
                            or you can create a <a href="javascript:alert('Not yet implemented');">new account</a>.
                        </p>
                    </div>
                    <div
                        dojoType="dijit.layout.ContentPane"
                        id="pageSelector-Retailer-Your Products"
                        title="<%= LabelExtractor.get("pageSelectorRetailerYourProducts", locale) %>"
                    >
                        <p class="hiddenToAuthenticated">
                            You need to <a href="javascript:dijit.byId('checkPoint').show();">log in</a>
                            or you can create a <a href="javascript:alert('Not yet implemented');">new account</a>.
                        </p>
                    </div>
                    <div
                        dojoType="dijit.layout.ContentPane"
                        id="pageSelector-Retailer-Your Store"
                        title="<%= LabelExtractor.get("pageSelectorRetailerYourStore", locale) %>"
                    >
                        <p class="hiddenToAuthenticated">
                            You need to <a href="javascript:dijit.byId('checkPoint').show();">log in</a>
                            or you can create a <a href="javascript:alert('Not yet implemented');">new account</a>.
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <div
            dojoType="dijit.layout.ContentPane"
            id="footer"
            region="bottom"
            style="height: 30px;"
        >
            Created by <a href="mailto://dominique.derrien@gmail.com/">Dom Derrien</a> --
            <img alt="Powered by Google App Engine" height="30" src="http://code.google.com/appengine/images/appengine-noborder-120x30.gif" width="120"/>
        </div>

    </div>

    <div
        dojoType="dijit.Dialog"
        href="help-popup.html"
        id="helpSystemDialog"
        title="Help System"
    ></div>

    <div
        dojoType="dijit.Dialog"
        id="consumerConfirmRequestSaving"
        title="Request Confirmation"
        style="width: 400px;"
    >
        <p style="padding-top: 0; margin-top: 0;">
            No product matches your request.
        </p>
        <p>
            Do you want to save the request in order to be notified when a corresponding product is made available?
            If a retailer can serve it in the coming
            <select id="consumerRequestExpirationDelay">
                <option>1</option>
                <option>2</option>
                <option selected="true">3</option>
                <option>4</option>
                <option>5</option>
                <option>6</option>
                <option>7</option>
            </select> day(s), you'll be notified.
        </p>
        <p style="text-align: center;">
            <button
                dojoType="dijit.form.Button"
                iconClass="silkIcon silkAcceptIcon"
                type="submit""
                onClick="twetailer.Console.saveConsumerRequest(); dijit.byId('consumerConfirmRequestSaving').hide();"
            >Save Request</button>
            <button
                dojoType="dijit.form.Button"
                iconClass="silkIcon silkCancelIcon"
                type="reset"
                onClick="dijit.byId('consumerConfirmRequestSaving').hide();"
            >Ignore Request</button>
        </p>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="retailerProductDetailPanel"
        style="width: 500px;"
        title="Product Detail"
    >
        <table class="attributeValueList">
            <tr>
                <td class="attribute"><label for="productName">Name</label></td>
                <td class="value">
                    <input
                        class=""
                        dojoType="dijit.form.ValidationTextBox"
                        id="productName"
                        regExp=".{3,}"
                        style="width: 300px;"
                        title="Name of the product"
                        type="text"
                    />
                </td>
            </tr>
            <tr>
                <td class="attribute"><label for="productDescription">Description</label></td>
                <td class="value">
                    <!--
                    <div
                        class=""
                        dojoType="dijit.Editor"
                        id="productDescription"
                        plugins="['bold','italic','underline','|','insertOrderedList','insertUnorderedList','indent','outdent','|','createLink']"
                        style=""
                        title="Name of the product"
                        type="text"
                    ></div>
                    -->
                    <textarea
                        id="productDescription"
                        style="width: 300px;"
                    ></textarea>
                </td>
            </tr>
            <tr>
                <td class="attribute"><label for="productKeywords">Keywords</label></td>
                <td class="value">
                    <input
                        class=""
                        dojoType="dijit.form.TextBox"
                        id="productKeywords"
                        style="width: 300px;"
                        title="Keywords matching the product, to make it searchable"
                        type="text"
                    /><br />
                    <span class="hint">Accurate keywords help finding your product easily.</span>
                </td>
            </tr>
            <tr>
                <td class="attribute"><label for="productUnitPrice">Unit price</label></td>
                <td class="value">
                    <input
                        class=""
                        dojoType="dijit.form.CurrencyTextBox"
                        id="productUnitPrice"
                        style="width: 100px;"
                        title="Product unit price"
                        type="text"
                    />
                    <select
                        dojoType="dijit.form.FilteringSelect"
                        id="productCurrency"
                        title="Price currency"
                        value="CAD"
                    >
                        <option value="CAD">Canadian Dollars</option>
                        <option value="USD">United States of America Dollars</option>
                    </select>
                </td>
            </tr>
        </table>
    </div>

    <div dojoType="dijit.Menu" id="consumerProductTableMenu" style="display: none;">
        <div disabled="true" dojoType="dijit.MenuItem" iconClass="silkIcon silkViewIcon">View details</div>
        <div disabled="true" dojoType="dijit.MenuItem" iconClass="silkIcon silkSelectIcon">Select</div>
    </div>

    <div dojoType="dijit.Menu" id="consumerRequestTableMenu" style="display: none;">
        <div disabled="true" dojoType="dijit.MenuItem" iconClass="silkIcon silkEditIcon">View details</div>
        <div dojoType="dijit.MenuItem" iconClass="silkIcon silkRemoveIcon" onClick="twetailer.Console.deleteConsumerRequest">Delete</div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="checkPoint"
        title="<%= LabelExtractor.get("ui_login_form_pane_title", locale) %>"
        htref="<%= userService.createLoginURL(uri) %>"
    >
        <form
            action="<%= userService.createLoginURL(uri) %>"
            method="post"
            style="text-align:center; font:13px sans-serif"
        >
            <input type="hidden" name="continue" value="/html/console.jsp">
            <div style="width: 20em; margin: 1em auto; text-align: left; padding: 0 2em 1.25em 2em; background-color: #d6e9f8; border: 2px solid #67a7e3">
                <h3><%= LabelExtractor.get("ui_login_form_pane_message", locale) %></h3>
                <p style="padding: 0; margin: 0">
                    <label for="email" style="width: 3em"><%= LabelExtractor.get("ui_login_form_pane_username_label", locale) %></label>
                    <input type="text" name="email">
                </p>
                <p style="padding: 0; margin: 0">
                    <label for="password" style="width: 3em"><%= LabelExtractor.get("ui_login_form_pane_password_label", locale) %></label>
                    <input type="text" name="password" disabled="true">
                </p>
                <p style="margin: .5em 0 0 3em; font-size:12px">
                    <input type="checkbox" name="isAdmin">
                    <label for="isAdmin"><%= LabelExtractor.get("ui_login_form_pane_administrative_state_label", locale) %></label>
                </p>
                <p style="margin-left: 3em;">
                    <input type="submit" value="<%= LabelExtractor.get("ui_login_form_pane_action_label", locale) %>">
                </p>
            </div>
        </form>
    </div>
</body>
</html>
