<!doctype html>
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
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.StringUtils"
    import="twetailer.connector.BaseConnector.Source"
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Location"
    import="twetailer.dto.SaleAssociate"
    import="twetailer.dto.Store"
    import="twetailer.j2ee.BaseRestlet"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.task.CommandProcessor"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String appVersion = appSettings.getProductVersion();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    cdnBaseURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.6"; // TODO: change at the application level

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <title>Monitoring Console</title>
    <meta http-equiv="X-UA-Compatible" content="chrome=1" />
    <meta http-equiv="content-type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <% if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/claro/claro.css";
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><link href="/js/release/<%= appVersion %>/dojo/resources/dojo.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dijit/themes/claro/claro.css" rel="stylesheet" type="text/css" />
    <link href="/css/console.css" rel="stylesheet" type="text/css" /><%
    } // endif (useCDN)
    %>
    <style type="text/css">
        .entityInformation>form>table>tbody>tr>td {
            height: 24px;
            vertical-align: middle;
        }
        .entityInformation>form>table>tbody>tr>td:nth-child(1), #locationFilterTable>tbody>tr>td:nth-child(1) {
            text-align: right;
        }
        .entityInformation>form>table>tbody>tr>td:nth-child(2), #locationFilterTable>tbody>tr>td:nth-child(2) {
            padding-left: 10px;
        }
        .shortField {
            width: 8em;
        }
        .longField {
            width: 12em;
        }
        textarea.longField {
            width: 12.2em;
            font-size: 12px;
        }
        .autoField {
            width: auto;
        }
    </style>
</head>
<body class="claro">

    <div id="introFlash">
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>

    <%
    if (useCDN) {
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: true, useXDomain: true, baseUrl: './', modulePaths: { dojo: '<%= cdnBaseURL %>/dojo', dijit: '<%= cdnBaseURL %>/dijit', dojox: '<%= cdnBaseURL %>/dojox', twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/_includes/dojo_blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    }
    else { // elif (!useCDN)
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: false, useXDomain: false, baseUrl: '/js/release/<%= appVersion %>/dojo/', dojoBlankHtmlUrl: '/_includes/dojo_blank.html', locale: '<%= localeId %>'"
        src="/js/release/<%= appVersion %>/dojo/dojo.js"
        type="text/javascript"
    ></script>
    <script
        src="/js/release/<%= appVersion %>/ase/_admin.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" data-dojo-type="dijit.layout.BorderContainer" data-dojo-props="gutters: false" style="height: 100%;">
        <jsp:include page="/_includes/banner_protected.jsp">
            <jsp:param name="pageForAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="isLoggedUserAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="consumerName" value="Administrator" />
        </jsp:include>
        <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="gutters: false, region: 'center'" id="centerZone" style="height: 100%; margin-top: 5px; margin-bottom: 10px;">
            <fieldset class="entityInformation" id="queryFieldset" style="">
                <legend>
                    Search
                </legend>
                Consumer by e-mail:
                <input
                    id="queryConsumer"
                    data-dojo-type="dijit.form.ValidationTextBox"
                    data-dojo-props="'class': 'shortField', invalidMessage: 'Must be at least 3 characters long', placeHolder: 'e-mail (starts with)', regExp: '(\\w|\\d|\\@){3}.*', required: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.searchEntityKey('queryConsumer', 'email', 'Consumer', 'consumer.key'); } }"
                />
                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.searchEntityKey('queryConsumer', 'email', 'Consumer', 'consumer.key'); }" type="button">Search</button>

                Location by postal code (in Canada):
                <input
                    id="queryLocation"
                    data-dojo-type="dijit.form.ValidationTextBox"
                    data-dojo-props="'class': 'shortField', invalidMessage: '<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>', placeHolder: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_default_CA", locale) %>', regExp: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_regExp_CA", locale) %>', required: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.searchEntityKey('queryLocation', 'postalCode', 'Location', 'location.key', { 'countryCode': 'CA', 'centerOnly': true }); } }"
                />
                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.searchEntityKey('queryLocation', 'postalCode', 'Location', 'location.key', { 'countryCode': 'CA', 'centerOnly': true }); }" type="button">Search</button>

                Store by name:
                <input
                    id="queryStore"
                    data-dojo-type="dijit.form.ValidationTextBox"
                    data-dojo-props="'class': 'shortField', invalidMessage: 'Must be at least 3 characters long', placeHolder: 'name (starts with)', regExp: '(\\w|\\d|\\s){3}.*', required: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.searchEntityKey('queryStore', 'name', 'Store', 'store.key'); } }"
                />
                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.searchEntityKey('queryStore', 'name', 'Store', 'store.key'); }" type="button">Search</button>

                RawCommand after date:
                <input
                    id="queryDueDate"
                    data-dojo-type="dijit.form.DateTextBox"
                    data-dojo-props="'class': 'shortField', required: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.searchEntityKey('queryDueDate', 'creationDate', 'RawCommand', 'rawcommand.key'); } }"
                />
                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.searchEntityKey('queryDueDate', 'creationDate', 'RawCommand', 'rawcommand.key'); }" type="button">Search</button>
            </fieldset>
            <div id="expandedBoxes"></div>
            <fieldset class="entityInformation" id="consumerFieldset" style="float:left;margin:5px;">
                <legend>
                    Consumer
                    <a href="javascript:localModule.expandBox('consumer');" id="consumerExpand">[+]</a>
                </legend>
                <form data-dojo-type="dijit.form.Form" id="consumerBox">
                    <table>
                        <tr>
                            <td><label for="consumer.key">Key:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('consumer.key', 'Consumer'); } }" id="consumer.key" />
                            <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('consumer.key', 'Consumer'); }" type="button">Fetch</button></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="consumer.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'locationKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('consumer.locationKey', 'Location'); } }" id="consumer.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('consumer.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input data-dojo-type="dijit.form.CheckBox" data-dojo-props="name: 'markedForDeletion', readOnly: true" id="consumer.markedForDeletion" type="checkbox" />
                            </td>
                            <td><label for="consumer.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="consumer.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="consumer._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="consumer.address">Address:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'address'" id="consumer.address" /></td>
                        </tr>
                        <tr>
                            <td>
                                <input data-dojo-type="dijit.form.CheckBox" data-dojo-props="name: 'automaticLocaleUpdate'" id="consumer.automaticLocaleUpdate" type="checkbox" />
                            </td>
                            <td><label for="consumer.automaticLocaleUpdate">Automatic locale update</label></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.autonomy">Autonomy:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'autonomy'"
                                    id="consumer.autonomy"
                                >
                                    <option value="BLOCKED">Blocked</option>
                                    <option value="UNCONFIRMED">Unconfirmed</option>
                                    <option value="MODERATED" selected="selected">Moderated</option>
                                    <option value="AUTONOMOUS">Autonomous</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="consumer.closedDemandNb">Closed demand #:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'closedDemandNb'" id="consumer.closedDemandNb" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.email">E-mail:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'email'" id="consumer.email" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.facebookId">Facebook identifier:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'facebookId'" id="consumer.facebookId" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.jabberId">Jabber identifier:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'jabberId'" id="consumer.jabberId" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.language">Language:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'language'"
                                    id="consumer.language"
                                >
                                    <option value="en" selected="selected">English</option>
                                    <option value="fr">French</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="consumer.name">Name:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'name'" id="consumer.name" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.openID">OpenID:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'openID'" id="consumer.openID" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.phoneNb">Phone number:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'phoneNb'" id="consumer.phoneNb" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.preferredConnection">Preferred connection:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'preferredConnection'"
                                    id="consumer.preferredConnection"
                                >
                                    <option value="mail" selected="selected">Mail</option>
                                    <option value="jabber">Jabber / XMPP</option>
                                    <option value="twitter">Twitter</option>
                                    <option value="facebook">Facebook (not implemented yet)</option>
                                    <option value="simlated">Simulated (for test purposes)</option>
                                    <option value="robot">Robot (for the #demo mode)</option>
                                    <option value="api">API (through the REST API)</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="consumer.publishedDemandNb">Published demand #:</label></td>
                            <td><input  data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'publishedDemandNb'" id="consumer.publishedDemandNb" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.saleAssociateKey">Sale Associate key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'saleAssociateKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('consumer.saleAssociateKey', 'SaleAssociate'); } }" id="consumer.saleAssociateKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('consumer.saleAssociateKey', 'SaleAssociate'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="consumer.twitterId">Twitter name:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'twitterId'" id="consumer.twitterId" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('Consumer'); }" type="button">Update</button>
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.getDemandKeys(); }" type="button">Get demand keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="demandFieldset" style="float:left;margin:5px;">
                <legend>
                    Demand
                    <a href="javascript:localModule.expandBox('demand');" id="demandExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="demandBox">
                    <table>
                        <tr>
                            <td style="font-weight:bold;text-align:right;padding-right:10px;color:orange;">Point of view:</td>
                            <td id="demand.pointOfView" style="font-weight:bold;color:orange;"></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="demand.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.key', 'Demand'); } }" id="demand.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.key', 'Demand'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="demand.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'locationKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.locationKey', 'Location'); } }" id="demand.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.locationKey', 'Location');}" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="demand.markedForDeletion" type="checkbox" /></td>
                            <td><label for="demand.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="demand.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="demand.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="demand._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="demand.action">Action:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="demand.action" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.cancelerKey">Canceler key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'cancelerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.cancelerKey', 'Consumer'); } }" id="demand.cancelerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.cancelerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.cc">CC-ed:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'cc'" id="demand.cc"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="demand.content">Content:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'content'" id="demand.content"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="demand.dueDate">Due date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', name: 'dueDate'" id="demand.dueDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.hashTags">Hash tags:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'hashTags'" id="demand.hashTags"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="demand.metadata">Metadata:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'metadata'" id="demand.metadata"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="demand.ownerKey">Owner key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'ownerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.ownerKey', 'Consumer'); } }" id="demand.ownerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.ownerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.quantity">Quantity:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:0,space:0}, name: 'quantity'" id="demand.quantity" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.rawCommandId">RawCommand key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="demand.rawCommandId" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.rawCommandId', 'RawCommand'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.source">Source:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, readOnly: true"
                                    id="demand.source"
                                >
                                    <option value="mail" selected="selected">E-mail</option>
                                    <option value="jabber">Jabber/XMPP</option>
                                    <option value="twitter">Twitter</option>
                                    <option value="simulated">Simulated</option>
                                    <option value="robot">Robot</option>
                                    <option value="facebook">Facebook</option>
                                    <option value="api">REST API</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.state">State:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'state'"
                                    id="demand.state"
                                >
                                    <option value="opened" selected="selected">Open</option>
                                    <option value="invalid">Invalid</option>
                                    <option value="published">Published</option>
                                    <option value="confirmed">Confirmed</option>
                                    <option value="cancelled">Cancelled</option>
                                    <option value="closed">Closed</option>
                                    <option value="declined">Declined</option>
                                    <option value="markedForDeletion">Marked for deletion</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="demand.stateCmdList" type="checkbox" /></td>
                            <td><label for="demand.stateCmdList">State for command: !list</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="demand.expirationDate">Expiration date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', name: 'expirationDate'" id="demand.expirationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.influencerKey">Influencer key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'influencerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.influencerKey', 'Influencer'); } }" id="demand.influencerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.influencerKey', 'Influencer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.proposalKeys">Proposal keys:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true"
                                    id="demand.proposalKeys"
                                >
                                    <option value="none" selected="selected">None</option>
                                </select>
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.proposalKeys', 'Proposal', 'CONSUMER'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.range">Range:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:5,places:0}, name: 'range'" id="demand.range" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.rangeUnit">Range unit:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'rangeUnit'"
                                    id="demand.rangeUnit"
                                >
                                    <option value="km" selected="selected">Kilometers</option>
                                    <option value="mi">Miles</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.reportKey">Report key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'reportKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.reportKey', 'Report'); } }" id="demand.reportKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.reportKey', 'Report'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.saleAssociateKeys">Sale associate keys:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true"
                                    id="demand.saleAssociateKeys"
                                >
                                    <option value="none" selected="selected">None</option>
                                </select>
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('demand.saleAssociateKeys', 'SaleAssociate'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" id="demand.updateButton" data-dojo-props="onClick: function() { localModule.saveEntity('Demand'); }" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="locationFieldset" style="float:left;margin:5px;">
                <legend>
                    Location
                    <a href="javascript:localModule.expandBox('location');" id="locationExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="locationBox">
                    <table id="locationAttributeTable">
                        <tr>
                            <td><label for="location.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('location.key', 'Location'); } }" id="location.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('location.key', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="location.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="location.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="location.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, readOnly: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('location.locationKey', 'Location'); } }" id="location.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="disabled: true, onClick: function() { localModule.fetchEntity('location.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="location.markedForDeletion" type="checkbox" /></td>
                            <td><label for="location.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="location.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="location.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="location._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="location._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="location.countryCode">Country code:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, onChange: function() { twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'location.postalCode'); }"
                                    id="location.countryCode"
                                >
                                    <option value="CA" selected="selected">Canada</option>
                                    <option value="US">United States of America</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="location.hasStore" type="checkbox" /></td>
                            <td><label for="location.hasStore">Has store</label></td>
                        </tr>
                        <tr>
                            <td><label for="location.latitude">Latitude:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:-90.0,max:90.0}, name: 'latitude'" id="location.latitude" /></td>
                        </tr>
                        <tr>
                            <td><label for=location.longitude>Longitude:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:-180.0,max:180.0}, name: 'longitude'" id="location.longitude" /></td>
                        </tr>
                        <tr>
                            <td><label for="location.postalCode">Postal code:</label></td>
                            <td>
                                <input
                                    id="location.postalCode"
                                    data-dojo-type="dijit.form.ValidationTextBox"
                                    data-dojo-props="'class': 'shortField', invalidMessage: '<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>', name: 'postalCode', placeHolder: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_default_CA", locale) %>', regExp: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_regExp_CA", locale) %>', required: true"
                                />
                            </td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('Location'); }" type="button">Update</button>
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.resolveLocation(); }" type="button">Resolve</button>
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { twetailer.Common.showMap(dijit.byId('location.postalCode').get('value'), dijit.byId('location.countryCode').get('value'), { geocoordinates: 'geoCoordinatesAvailable' }); }" type="button">View map</button>
                                <br />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.openLocationFilterDialog(); }" type="button">Get location keys</button>
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.openStoreFilterDialog(); }" type="button">Get store keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="reportFieldset" style="float:left;margin:5px;">
                <legend>
                    Report
                    <a href="javascript:localModule.expandBox('report');" id="reportExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="reportBox">
                    <table id="reportAttributeTable">
                        <tr>
                            <td><label for="report.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('report.key', 'Report'); } }" id="report.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('report.key', 'Report'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="report.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="report.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="report.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, readOnly: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('report.locationKey', 'Location'); } }" id="report.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('report.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="report.markedForDeletion" type="checkbox" /></td>
                            <td><label for="report.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="report.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="report.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="report._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="report._tracking" ></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="report.consumerKey">Consumer key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, readOnly: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('report.consumerKey', 'Consumer'); } }" id="report.consumerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('report.consumerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="report.content">Content:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', readOnly: true" id="report.content"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="report.demandKey">Demand key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, readOnly: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('report.demandKey', 'Demand'); } }" id="report.demandKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('report.demandKey', 'Demand'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="report.hashTags">Hash tags:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', readOnly: true" id="report.hashTags"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="report.ipAddress">IP address:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="report.ipAddress" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { window.open('http://www.iplocationfinder.com/' + dijit.byId('report.ipAddress').get('value'), 'ipLocator'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="report.language">Language:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, readOnly: true"
                                    id="report.language"
                                >
                                    <option value="en" selected="selected">English</option>
                                    <option value="fr">French</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="report.metadata">Metadata:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', readOnly: true" id="report.metadata"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="report.referrerUrl">Referrer URL:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', readOnly: true" id="report.referrerUrl"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="report.reporterTitle">Reporter title:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', readOnly: true" id="report.reporterTitle"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="report.reporterUrl">Reporter URL:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', readOnly: true" id="report.reporterUrl"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="report.userAgent">userAgent:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', readOnly: true" id="report.userAgent"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="report.range">Range:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:5,places:0}, readOnly: true" id="report.range" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('Report'); }" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="saleassociateFieldset" style="float:left;margin:5px;">
                <legend>
                    Sale Associate
                    <a href="javascript:localModule.expandBox('saleassociate');" id="saleassociateExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="saleassociateBox">
                    <table>
                        <tr>
                            <td><label for="saleassociate.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.key', 'SaleAssociate'); } }" id="saleassociate.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('saleassociate.key', 'SaleAssociate'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="saleassociate.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.locationKey', 'Location'); } }" id="saleassociate.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('saleassociate.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="saleassociate.markedForDeletion" type="checkbox" />
                            </td>
                            <td><label for="saleassociate.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="saleassociate.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="saleassociate._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="saleassociate.closedProposalNb">Closed proposal #:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}" id="saleassociate.closedProposalNb" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.consumerKey">Consumer key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'consumerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.consumerKey', 'Consumer'); } }" id="saleassociate.consumerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('saleassociate.consumerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.creatorKey">Creator key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'creatorKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.creatorKey', 'Consumer'); } }" id="saleassociate.creatorKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('saleassociate.creatorKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.criteria">Criteria:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'criteria'" id="saleassociate.criteria"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.hashTags">Hash tags:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'hashTags'" id="saleassociate.hashTags"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.publishedProposalNb">Published proposal #:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}" id="saleassociate.publishedProposalNb" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.score">Req. match score:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'shortField', name: 'score'" id="saleassociate.score" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.storeKey">Store key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'storeKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.storeKey', 'Store'); } }" id="saleassociate.storeKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('saleassociate.storeKey', 'Store'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="name: 'isStoreAdmin'" id="saleassociate.isStoreAdmin" type="checkbox" /></td>
                            <td><label for="saleassociate.isStoreAdmin">Store administrator</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('SaleAssociate'); }" type="button">Update</button>
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.getProposalKeys(); }" type="button">Get proposal keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="storeFieldset" style="float:left;margin:5px;">
                <legend>
                    Store
                    <a href="javascript:localModule.expandBox('store');" id="storeExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="storeBox">
                    <table>
                        <tr>
                            <td><label for="store.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('store.key', 'Store'); } }" id="store.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('store.key', 'Store'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="store.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="store.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('store.locationKey', 'Location'); } }" id="store.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('store.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="store.markedForDeletion" type="checkbox" /></td>
                            <td><label for="store.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="store.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="store.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="store._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="store._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="store.address">Address:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'address'" id="store.address" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.closedProposalNb">Closed proposal #:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}" id="store.closedProposalNb" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.email">E-mail:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'email'" id="store.email" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.latitude">Latitude:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:-90.0,max:90.0}, name: 'latitude'" id="store.latitude" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.longitude">Longitude:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:-180.0,max:180.0}, name: 'longitude'" id="store.longitude" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.name">Name:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'name'" id="store.name" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.phoneNb">Phone number:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'phoneNb'" id="store.phoneNb" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.publishedProposalNb">Published proposal #:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}" id="store.publishedProposalNb" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.registrarKey">Registrar key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'registratrKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('store.registrarKey', 'Registrar'); } }" id="store.registrarKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('store.registrarKey', 'Registrar'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="store.reviewSystemKey">Review system key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'reviewSystemKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('store.reviewSystemKey', 'ReviewSystem'); } }" id="store.reviewSystemKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('store.reviewSystemKey', 'ReviewSystem'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="store.state">State:</label></td>
                            <td>
                                <select data-dojo-type="dijit.form.Select" data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'state'" id="store.state">
                                    <option value="referenced" selected="selected">Referenced (minimal)</option>
                                    <option value="inProgress">Needs follow-up</option>
                                    <option value="waiting">Waiting activation</option>
                                    <option value="active">Activated</option>
                                    <option value="excluded">Excluded</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="store.url">URL:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'url'" id="store.url" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('Store'); }" type="button">Update</button>
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.getSaleAssociateKeys(); }" type="button">Get sale associate keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="proposalFieldset" style="float:left;margin:5px;">
                <legend>
                    Proposal
                    <a href="javascript:localModule.expandBox('proposal');" id="proposalExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="proposalBox">
                    <table>
                        <tr>
                            <td style="font-weight:bold;text-align:right;padding-right:10px;color:orange;">Point of view:</td>
                            <td id="proposal.pointOfView" style="font-weight:bold;color:orange;"></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="proposal.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.key', 'Proposal'); } }" id="proposal.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('proposal.key', 'Proposal'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="proposal.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.locationKey', 'Location'); } }" id="proposal.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('proposal.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="proposal.markedForDeletion" type="checkbox" /></td>
                            <td><label for="proposal.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="proposal.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="proposal._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="proposal.action">Action:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="proposal.action" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.cancelerKey">Canceler key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'cancelerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.cancelerKey', 'Consumer'); } }" id="proposal.cancelerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('proposal.cancelerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.cc">CC-ed:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'cc'" id="proposal.cc"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.content">Content:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'content'" id="proposal.content"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.dueDate">Due date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', name: 'dueDate'" id="proposal.dueDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.hashTags">Hash tags:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'hashTags'" id="proposal.hashTags"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.metadata">Metadata:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'metadåta'" id="proposal.metadata"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.ownerKey">Owner key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'ownerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.ownerKey', 'SaleAssociate'); } }" id="proposal.ownerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('proposal.ownerKey', 'SaleAssociate'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.quantity">Quantity:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:1,places:0}, name: 'quantity'" id="proposal.quantity" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.rawCommandId">RawCommand key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, readOnly: true" id="proposal.rawCommandId" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="disabled: true" data-dojo-props="onClick: function() { localModule.fetchEntity('proposal.rawCommandId', 'RawCommand'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.source">Source:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, readOnly: true"
                                    id="proposal.source"
                                >
                                    <option value="mail" selected="selected">E-mail</option>
                                    <option value="jabber">Jabber/XMPP</option>
                                    <option value="twitter">Twitter</option>
                                    <option value="simulated">Simulated</option>
                                    <option value="robot">Robot</option>
                                    <option value="facebook">Facebook</option>
                                    <option value="api">REST API</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.state">State:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'state'"
                                    id="proposal.state"
                                >
                                    <option value="opened" selected="selected">Open</option>
                                    <option value="invalid">Invalid</option>
                                    <option value="published">Published</option>
                                    <option value="confirmed">Confirmed</option>
                                    <option value="cancelled">Cancelled</option>
                                    <option value="closed">Closed</option>
                                    <option value="declined">Declined</option>
                                    <option value="markedForDeletion">Marked for deletion</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="proposal.stateCmdList" type="checkbox" /></td>
                            <td><label for="proposal.stateCmdList">State for command: !list</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="proposal.AWSCBUIURL">Co-branded service URL:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="proposal.AWSCBUIURL" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.consumerKey">Consumer key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'consumerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.consumerKey', 'Consumer'); } }" id="proposal.consumerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('proposal.consumerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.comment">Comment:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'comment'" id="proposal.comment"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.currencyCode">Currency:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'currencyCode'"
                                    id="proposal.currencyCode"
                                >
                                    <option value="USD" selected="selected">$ / USD</option>
                                    <option value="CAD">$ / CAD</option>
                                    <option value="EUR">€ / EUR</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.demandKey">Demand key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'demandKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.demandKey', 'Demand', 'SALE_ASSOCIATE'); } }" id="proposal.demandKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('proposal.demandKey', 'Demand', 'SALE_ASSOCIATE'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.price">Price:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:2}, name: 'price'" id="proposal.price" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.score">Score:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, name: 'score'"
                                    id="proposal.score"
                                >
                                    <option value="0" selected="selected">Not rated</option>
                                    <option value="1">:-(</option>
                                    <option value="2">Between :-( and :-|</option>
                                    <option value="3">:-|</option>
                                    <option value="4">Between :-| and :-)</option>
                                    <option value="5">:-)</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.storeKey">Store key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'storeKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.storeKey', 'Store'); } }" id="proposal.storeKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('proposal.storeKey', 'Store'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.total">Total:</label></td>
                            <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:2}, name: 'total'" id="proposal.total" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" id="proposal.updateButton" data-dojo-props="onClick: function() { localModule.saveEntity('Proposal'); }" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="rawcommandFieldset" style="float:left;margin:5px;">
                <legend>
                    Raw Command
                    <a href="javascript:localModule.expandBox('rawcommand');" id="rawcommandExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="rawcommandBox">
                    <table>
                        <tr>
                            <td><label for="rawcommand.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('rawcommand.key', 'RawCommand'); } }" id="rawcommand.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('rawcommand.key', 'RawCommand'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="rawcommand.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('rawcommand.locationKey', 'Location'); } }" id="rawcommand.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="disabled: true" data-dojo-props="onClick: function() { localModule.fetchEntity('rawcommand.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="rawcommand.markedForDeletion" type="checkbox" /></td>
                            <td><label for="rawcommand.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="rawcommand.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="rawcommand._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="rawcommand.command">Command:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="rawcommand.command" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.commandId">Command Id:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="rawcommand.commandId" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.emitterId">Emitter Id:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="rawcommand.emitterId" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.errorMessage">Error msg:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: 'errorMessage'" id="rawcommand.errorMessage"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.source">Source:</label></td>
                            <td>
                                <select
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="'class': 'autoField', hasDownArrow: true, readOnly: true"
                                    id="rawcommand.source"
                                >
                                    <option value="mail" selected="selected">E-mail</option>
                                    <option value="jabber">Jabber/XMPP</option>
                                    <option value="twitter">Twitter</option>
                                    <option value="simulated">Simulated</option>
                                    <option value="robot">Robot</option>
                                    <option value="facebook">Facebook</option>
                                    <option value="api">REST API</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.subject">Subject:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'subject'" id="rawcommand.subject" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.toId">To Id:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="rawcommand.toId" /></td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="paymentFieldset" style="float:left;margin:5px;">
                <legend>
                    Payment
                    <a href="javascript:localModule.expandBox('payment');" id="paymentExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="paymentBox">
                    <table style="display:none;">
                        <tr>
                            <td><label for="payment.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('payment.key', 'Payment'); } }" id="payment.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('payment.key', 'Payment'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="payment.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="payment.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('payment.locationKey', 'Location'); } }" id="payment.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="disabled: true" data-dojo-props="onClick: function() { localModule.fetchEntity('payment.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="payment.markedForDeletion" type="checkbox" /></td>
                            <td><label for="payment.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="payment.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="payment.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="payment._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="payment.authorizationId">Authorization identifier:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="payment.authorizationId" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.reference">Reference:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="payment.reference" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.requestId">Request identifier:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="payment.requestId" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.transactionId">Transaction identifier:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="payment.transactionId" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.status">Status:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', readOnly: true" id="payment.status" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="disabled: true" data-dojo-props="onClick: function() { localModule.saveEntity('Payment'); }" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="influencerFieldset" style="float:left;margin:5px;">
                <legend>
                    Influencer
                    <a href="javascript:localModule.expandBox('influencer');" id="influencerExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="influencerBox">
                    <table id="influencerAttributeTable" style="display:none;">
                        <tr>
                            <td><label for="influencer.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('influencer.key', 'Influencer'); } }" id="influencer.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('influencer.key', 'Influencer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="influencer.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="influencer.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('influencer.locationKey', 'Location'); } }" id="influencer.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('influencer.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="influencer.markedForDeletion" type="checkbox" /></td>
                            <td><label for="influencer.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="influencer.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="influencer._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="influencer.consumerKey">Consumer key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'consumerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('influencer.consumerKey', 'Consumer'); } }" id="influencer.consumerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('influencer.consumerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="influencer.email">E-mail:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'email'" id="influencer.email" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.name">Name:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'name'" id="influencer.name" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.referralId">Referral Id:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'referralId'" id="influencer.referralId" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.url">URL:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'url'" id="influencer.url" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('Influencer'); }" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="registrarFieldset" style="float:left;margin:5px;">
                <legend>
                    Registrar
                    <a href="javascript:localModule.expandBox('registrar');" id="registrarExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="registrarBox">
                    <table id="registrarAttributeTable" style="display:none;">
                        <tr>
                            <td><label for="registrar.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('registrar.key', 'Registrar'); } }" id="registrar.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('registrar.key', 'Registrar'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="registrar.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="registrar.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="registrar.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('registrar.locationKey', 'Location'); } }" id="registrar.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('registrar.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="registrar.markedForDeletion" type="checkbox" /></td>
                            <td><label for="registrar.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="registrar.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="registrar.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="registrar._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="registrar._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="registrar.consumerKey">Consumer key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'consumerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('registrar.consumerKey', 'Consumer'); } }" id="registrar.consumerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('registrar.consumerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="registrar.email">E-mail:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'email'" id="registrar.email" /></td>
                        </tr>
                        <tr>
                            <td><label for="registrar.name">Name:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'name'" id="registrar.name" /></td>
                        </tr>
                        <tr>
                            <td><label for="registrar.url">URL:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'url'" id="registrar.url" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('Registrar'); }" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="resellerFieldset" style="float:left;margin:5px;">
                <legend>
                    Reseller
                    <a href="javascript:localModule.expandBox('reseller');" id="resellerExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="resellerBox">
                    <table id="resellerAttributeTable" style="display:none;">
                        <tr>
                            <td><label for="reseller.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reseller.key', 'Reseller'); } }" id="reseller.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('reseller.key', 'Reseller'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="reseller.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="reseller.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="reseller.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reseller.locationKey', 'Location'); } }" id="reseller.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('reseller.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="reseller.markedForDeletion" type="checkbox" /></td>
                            <td><label for="reseller.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="reseller.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="reseller.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="reseller._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="reseller._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="reseller.consumerKey">Consumer key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'consumerKey', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reseller.consumerKey', 'Consumer'); } }" id="reseller.consumerKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('reseller.consumerKey', 'Consumer'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="reseller.tokenNb">Tokens:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'tokenNb'" id="reseller.tokenNb" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('Reseller'); }" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="reviewsystemFieldset" style="float:left;margin:5px;">
                <legend>
                    Review System
                    <a href="javascript:localModule.expandBox('reviewsystem');" id="reviewsystemExpand">[+]</a>
                </legend>
                <form  data-dojo-type="dijit.form.Form" id="reviewsystemBox">
                    <table id="reviewsystemAttributeTable" style="display:none;">
                        <tr>
                            <td><label for="reviewsystem.key">Key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, name: 'key', onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reviewsystem.key', 'ReviewSystem'); } }" id="reviewsystem.key" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.fetchEntity('reviewsystem.key', 'ReviewSystem'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.creationDate">Creation date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="reviewsystem.creationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.locationKey">Location key:</label></td>
                            <td>
                                <input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:0,places:0}, readOnly: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reviewsystem.locationKey', 'Location'); } }" id="reviewsystem.locationKey" />
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="disabled: true" data-dojo-props="onClick: function() { localModule.fetchEntity('reviewsystem.locationKey', 'Location'); }" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input data-dojo-type="dijit.form.CheckBox" data-dojo-props="readOnly: true" id="reviewsystem.markedForDeletion" type="checkbox" /></td>
                            <td><label for="reviewsystem.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.modificationDate">Modification date:</label></td>
                            <td><input data-dojo-type="dijit.form.DateTextBox" data-dojo-props="'class': 'shortField', readOnly: true" id="reviewsystem.modificationDate" /></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea data-dojo-type="dijit.form.Textarea" data-dojo-props="'class': 'longField', name: '_tracking'" id="reviewsystem._tracking"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="reviewsystem.email">E-mail:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'email'" id="reviewsystem.email" /></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.name">Name:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'name'" id="reviewsystem.name" /></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.url">URL:</label></td>
                            <td><input data-dojo-type="dijit.form.TextBox" data-dojo-props="'class': 'longField', name: 'url'" id="reviewsystem.url" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.saveEntity('ReviewSystem'); }" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
        </div>
        <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region: 'bottom'" id="footerZone">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <div
        data-dojo-type="dijit.Dialog"
        data-dojo-props="style: 'min-width: 260px'"
        id="entityKeysDialog"
    >
        <div id="keyZone" style="min-height:60px;"></div>
        <div class="dijitDialogPaneActionBar" style="text-align:right;padding-top:5px;">
            <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { dijit.byId('entityKeysDialog').hide(); }" type="button">Close</button>
        </div>
    </div>

    <div
        data-dojo-type="dijit.Dialog"
        id="locationFilterDialog"
    >
        <div class="dijitDialogPaneContentArea">
            <table id="locationFilterTable">
                <tr>
                    <td colspan="2" style="color:lightgrey;">Specify either {postalCode;countryCode} or {latitude;longitude} and click 'Search'<br/>&nbsp;</td>
                </tr>
                <tr>
                    <td><label for="locationFilter.postalCode">Postal code:</label></td>
                    <td>
                        <input
                            data-dojo-type="dijit.form.ValidationTextBox"
                            data-dojo-props="'class': 'shortField', invalidMessage: '<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>', placeHolder: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_default_CA", locale) %>', regExp: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_regExp_CA", locale) %>', required: true, onKeyUp: function() { if (event.keyCode == dojo.keys.ENTER) { var dialog = dijit.byId('locationFilterDialog'); dialog.execute(); dialog.hide(); } }"
                            id="locationFilter.postalCode"
                        />
                    </td>
                </tr>
                <tr>
                    <td><label for="locationFilter.countryCode">Country code:</label></td>
                    <td>
                        <select
                            data-dojo-type="dijit.form.Select"
                            data-dojo-props="'class': 'autoField', hasDownArrow: true, readOnly: true, onChange: function() { twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'locationFilter.postalCode'); }"
                            id="locationFilter.countryCode"
                        >
                            <option value="CA" selected="selected">Canada</option>
                            <option value="US">United States of America</option>
                        </select>
                    </td>
                </tr>
                <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                <tr>
                    <td><label for="locationFilter.latitude">Latitude:</label></td>
                    <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:-90.0,max:90.0}, value: -1000" id="locationFilter.latitude" /></td>
                </tr>
                <tr>
                    <td><label for=locationFilter.longitude>Longitude:</label></td>
                    <td><input data-dojo-type="dijit.form.NumberTextBox" data-dojo-props="'class': 'shortField', constraints: {min:-180.0,max:180.0}, value: -1000" id="locationFilter.longitude" /></td>
                </tr>
                <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                <tr>
                    <td><label for="locationFilter.range">Range:</label></td>
                    <td>
                        <input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="'class': 'shortField', constraints: {min:25,places:0}, value: 25" id="locationFilter.range" />
                        <select data-dojo-type="dijit.form.Select" data-dojo-props="'class': 'autoField', hasDownArrow: true" id="locationFilter.rangeUnit">
                            <option value="km" selected="selected">km</option>
                            <option value="mi">miles</option>
                        </select>
                    </td>
                </tr>
                <tr id="hasStoreRow">
                    <td><input data-dojo-type="dijit.form.CheckBox" id="locationFilter.hasStore" type="checkbox" /></td>
                    <td><label for="locationFilter.hasStore">Has store</label></td>
                </tr>
            </table>
        </div>
        <div class="dijitDialogPaneActionBar" style="text-align:right;margin-top:10px;">
            <button data-dojo-type="dijit.form.Button" data-dojo-props="type: 'submit'">Search</button>
            <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { dijit.byId('locationFilterDialog').hide(); }" type="button">Cancel</button>
        </div>
    </div>

    <div
        data-dojo-type="dijit.Dialog"
        id="locationMapDialog"
        title="<%= LabelExtractor.get(ResourceFileId.third, "shared_map_preview_dialog_title", locale) %>"
    >
        <div style="width:600px;height:400px;"><div id='mapPlaceHolder' style='width:100%;height:100%;'></div></div>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dojo.data.ItemFileWriteStore');
        dojo.require('dojo.parser');
        dojo.require('dijit.Dialog');
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        dojo.require('dijit.form.Button');
        dojo.require('dijit.form.CheckBox');
        dojo.require('dijit.form.ComboBox');
        dojo.require('dijit.form.DateTextBox');
        dojo.require('dijit.form.Form');
        dojo.require('dijit.form.NumberTextBox');
        dojo.require('dijit.form.Select');
        dojo.require('dijit.form.NumberSpinner');
        dojo.require('dijit.form.Textarea');
        dojo.require('dijit.form.TextBox');
        dojo.require('dojox.analytics.Urchin');
        dojo.require('twetailer.Common');
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            dojo.fadeOut({
                node: 'introFlash',
                delay: 50,
                onEnd: function() {
                    dojo.style('introFlash', 'display', 'none');
                }
            }).play();<%
            if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName()) && !"10.0.2.2".equals(request.getServerName())) { %>
            new dojox.analytics.Urchin({ acct: 'UA-11910037-2' });<%
            } %>
            dojo.byId('logoutLink').href = '<%= com.google.appengine.api.users.UserServiceFactory.getUserService().createLogoutURL(request.getRequestURI()) %>';
            twetailer.Common.init('en', null, null);
            localModule.init();
        });
    });

    var localModule = new Object();
    localModule.init = function() {
        dojo.query('.entityInformation>form>table').style('display','none');
        if (window.location.search) {
            var params = dojo.queryToObject(window.location.search.slice(1));
            if (params.type && params.key) {
                if (!dojo.isArray(params.type)) {
                    params.type = [params.type];
                    params.key = [params.key];
                }
                var types = params.type, idx = 0, limit = types.length, keys = params.key;
                while (idx < limit) {
                    var type = types[idx], key = keys[idx], id = type.toLowerCase() + '.key';
                    dijit.byId(id).set('value', key);
                    localModule.fetchEntity(id, type);
                    ++ idx;
                }
            }
            if (params.queryLocation) {
                dijit.byId('queryLocation').set('value', params.queryLocation);
                localModule.searchEntityKey('queryLocation', 'postalCode', 'Location', 'location.key', { 'countryCode': 'CA', 'centerOnly': true });
            }
        }
        dijit.byId('topContainer').resize();

        dojo.subscribe('geoCoordinatesAvailable', function(location) {
            var dataOK = true;
            if (location.lat) { dijit.byId('location.latitude').set('value', location.lat()); }
            else if (location.Ca) { dijit.byId('location.latitude').set('value', location.Ca); }
            else if (location.Da) { dijit.byId('location.latitude').set('value', location.Da); }
            else { dataOK = false; }
            if (location.lng) { dijit.byId('location.longitude').set('value', location.lng()); }
            else if (location.Ea) { dijit.byId('location.longitude').set('value', location.Ea); }
            else if (location.Fa) { dijit.byId('location.longitude').set('value', location.Fa); }
            else { dataOK = false; }
            if (!dataOK) {
                alert('Cannot extract the geo-coordinates...');
            }
        });
    };
    localModule.expandBox = function(boxId) {
        var plus = dojo.byId(boxId + 'Expand');
        if (plus) {
            plus.parentNode.removeChild(plus);
            dojo.byId('expandedBoxes').appendChild(dojo.byId(boxId + 'Fieldset'));
            dojo.query('#' + boxId + 'Box>table').style('display','');
        }
    };
    localModule.fetchEntity = function(keyFieldId, entityName, pointOfView) {
        var key = dijit.byId(keyFieldId).get('value');
        if (isNaN(key)) {
            alert('The key in the field \'' + keyFieldId + '\' is not a number!');
            return;
        }
        pointOfView = pointOfView || ((entityName == 'Proposal' || entityName == 'Store') ? 'SALE_ASSOCIATE' : 'CONSUMER');
        var prefix = entityName.toLowerCase();
        var pointOfViewField = dojo.byId(prefix + '.pointOfView');
        if (pointOfViewField) {
            pointOfViewField.innerHTML = pointOfView;
        }
        localModule.expandBox(prefix);
        dojo.animateProperty({
            node: prefix + 'Fieldset',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: {
                'pointOfView': pointOfView,
                '<%= BaseRestlet.ON_BEHALF_CONSUMER_KEY %>': dijit.byId('consumer.key').get('value'),
                '<%= BaseRestlet.ON_BEHALF_ASSOCIATE_KEY %>': dijit.byId('saleassociate.key').get('value'),
                '<%= CommandProcessor.DEBUG_MODE_PARAM %>': 'yes'
            },
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    localModule.displayEntityAttributes(prefix, response.resource);
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: prefix + 'Fieldset',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/' + entityName + '/' + key
        });
    };
    localModule.displayEntityAttributes = function(prefix, resource) {
        dijit.byId(prefix + 'Box').reset();
        for (var attr in resource) {
            try {
                var value = resource[attr];
                if (attr.indexOf('Date') != -1) {
                    value = dojo.date.stamp.fromISOString(value);
                }
                if (attr == '_tracking' || attr == 'content' || attr == 'comment') {
                    value = value.replace(/\\n/g, '\n');
                }
                if (attr == 'criteria' || attr == 'hashTags' || attr == 'cc') {
                    value = value.join('\n');
                }
                if (attr == 'proposalKeys' || attr == 'saleAssociateKeys') {
                    var options = [], limit = value.length, idx;
                    for (idx = 0; idx < limit; idx++) {
                        options.push({ label: value[idx], value: value[idx] });
                    }
                    dijit.byId(prefix + '.' + attr).set('options', options);
                    value = value[0];
                }
                var field = dijit.byId(prefix + '.' + attr)
                if (field) {
                    field.set('value', value);
                }
                else if (attr != 'criteria') {
                    alert('Field "' + prefix + '.' + attr + '" is missing!');
                }
                /*
                if (attr == 'state' && (entityName == 'Demand' || entityName == 'Proposal')) {
                    var isNonModifiable = value == 'closed' || value == 'cancelled' || value == 'markedForDeletion';
                    dijit.byId(prefix + '.updateButton').set('disabled', isNonModifiable);
                }
                */
            }
            catch (ex) {
                alert('Error while processing attribute "' + attr + '" for an instance of class "' + prefix + '".\nError: ' + ex);
            }
        }
    };
    localModule.saveEntity = function(entityName) {
        var prefix = entityName.toLowerCase();
        var key = dijit.byId(prefix + '.key').get('value');
        if (isNaN(key)) {
            alert('The key in the field \'' + prefix + '.key\' is not a number!');
            return;
        }
        dojo.animateProperty({
            node: prefix + 'Fieldset',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        var pointOfView = (entityName == 'Proposal' || entityName == 'Store') ? 'SALE_ASSOCIATE' : 'CONSUMER';
        var prefix = entityName.toLowerCase();
        var pointOfViewField = dojo.byId(prefix + '.pointOfView');
        if (pointOfViewField) {
            pointOfViewField.innerHTML = pointOfView;
        }
        var data = localModule.formToObject(prefix + 'Box');
        if (data.cc != null) { data.cc = data.cc.split('\n'); }
        if (data.criteria != null) { data.criteria = data.criteria.split('\n'); }
        if (data.hashTags != null) { data.hashTags = data.hashTags.split('\n'); }
        if (data.proposalKeys != null) { delete data.proposalKeys; } // Neutralized server-side, just removed for the bandwidth
        if (data.saleAssociateKeys != null) { delete data.saleAssociateKeys; } // Neutralized server-side, just removed for the bandwidth
        data['<%= CommandProcessor.DEBUG_MODE_PARAM %>'] = 'yes';
        data['pointOfView'] = pointOfView;
        data['<%= BaseRestlet.ON_BEHALF_CONSUMER_KEY %>'] = parseInt(dijit.byId('consumer.key').get('value') || 0);
        data['<%= BaseRestlet.ON_BEHALF_ASSOCIATE_KEY %>'] = parseInt(dijit.byId('saleassociate.key').get('value') || 0);
        dojo.xhrPut({
            headers: { 'content-type': 'application/json; charset=UTF-8' },
            putData: dojo.toJson(data),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    localModule.displayEntityAttributes(prefix, response.resource);
                }
                else {
                    alert(response.exceptionMessage+'\nurl: '+ioArgs.url+'\n\n'+response.originalExceptionMessage);
                }
                dojo.animateProperty({
                    node: prefix + 'Fieldset',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/' + entityName + '/' + key
        });
    };
    localModule.formToObject = function(/*DOMNode||String*/formNode) {
        var data = {};
        dojo.forEach(dojo.byId(formNode).elements, function(item){
            var item = dijit.getEnclosingWidget(item);
            if (item != null) {
                var _in = item.get('name');
                if (item.get('checked') === true) {
                    data[_in] = true;
                }
                else {
                    var _iv = item.get('value');
                    if (_in && _iv) {
                        if (_iv instanceof Date) {
                            _iv = dojo.date.stamp.toISOString(_iv, {});
                        }
                        data[_in] = _iv;
                    }
                }
            }
        });
        return data;
    };
    localModule.decorationOfEntityLinks = [
                '<a href="javascript:dijit.byId(\'entityKeysDialog\').hide();dijit.byId(\'', // entity class name, lower case
                '.key\').set(\'value\',',      // entity key
                ');localModule.fetchEntity(\'', // entity class name, lower case
                '.key\',\'',                    // entity class name
                '\');" title="Get the ',        // entity class name
                ': ',                           // entity key
                '">',                           // entity key
                '</a>, '
            ];
    localModule.loadEntityKeys = function(entityName, parameters) {
        var dialog = dijit.byId('entityKeysDialog');
        dialog.set('title', entityName + ' identifiers');
        dojo.byId('keyZone').innerHTML = '&nbsp;Loading...';
        dialog.show();

        dojo.animateProperty({
            node: 'keyZone',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: dojo.mixin({ 'anyState': true, 'onlyKeys': true, '<%= CommandProcessor.DEBUG_MODE_PARAM %>': 'yes' }, (parameters || {})),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var keys = response.resources;
                    var keyNb = keys.length;
                    var out = [];
                    if (keyNb == 0) {
                        out.push('None');
                    }
                    else {
                        var deco = localModule.decorationOfEntityLinks;
                        for(var i=0; i<keyNb; i++) {
                            var key = keys[i];
                            out.push(deco[0]); out.push(entityName.toLowerCase());
                            out.push(deco[1]); out.push(key);
                            out.push(deco[2]); out.push(entityName.toLowerCase());
                            out.push(deco[3]); out.push(entityName);
                            out.push(deco[4]); out.push(entityName);
                            out.push(deco[5]); out.push(key);
                            out.push(deco[6]); out.push(key);
                            out.push(deco[7]);
                        }
                    }
                    dojo.byId('keyZone').innerHTML = out.join('');
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: 'keyZone',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/' + entityName
        });
    };
    localModule.getDemandKeys = function() {
        var parameters = {
            'pointOfView': 'CONSUMER',
            '<%= BaseRestlet.ON_BEHALF_CONSUMER_KEY %>': dijit.byId('consumer.key').get('value')
        };
        localModule.loadEntityKeys('Demand', parameters);
     };
     localModule.getProposalKeys = function() {
         var parameters = {
            'pointOfView': 'SALE_ASSOCIATE',
            '<%= BaseRestlet.ON_BEHALF_CONSUMER_KEY %>': dijit.byId('saleassociate.consumerKey').get('value'),
            '<%= BaseRestlet.ON_BEHALF_ASSOCIATE_KEY %>': dijit.byId('saleassociate.key').get('value')
         };
         localModule.loadEntityKeys('Proposal', parameters);
     };
     localModule.getSaleAssociateKeys = function() {
         var parameters = {
            'storeKey': dijit.byId('store.key').get('value'),
            '<%= BaseRestlet.ON_BEHALF_CONSUMER_KEY %>': dijit.byId('saleassociate.consumerKey').get('value'),
            '<%= BaseRestlet.ON_BEHALF_ASSOCIATE_KEY %>': dijit.byId('saleassociate.key').get('value')
         };
         localModule.loadEntityKeys('SaleAssociate', parameters);
     };
     localModule.openLocationFilterDialog = function() {
         dojo.query('#hasStoreRow').style('display', '');
         var dialog = dijit.byId('locationFilterDialog');
         dialog.set('execute', localModule.getLocationKeys);
         dialog.set('title', 'Get Location keys');
         dialog.show();
     };
     localModule.openStoreFilterDialog = function() {
         dijit.byId('locationFilter.hasStore').set('value', 'on');
         dojo.query('#hasStoreRow').style('display', 'none');
         var dialog = dijit.byId('locationFilterDialog');
         dialog.set('execute', localModule.getStoreKeys);
         dialog.set('title', 'Get Store keys');
         dialog.show();
     };
     localModule.prepareLocationParameters = function() {
         var parameters = {
            'maximumResults': 0,
            'hasStore': (dijit.byId('locationFilter.hasStore').get('value') == 'on'),
            'countryCode': dijit.byId('locationFilter.countryCode').get('value'),
            'range': dijit.byId('locationFilter.range').get('value'),
            'rangeUnit': dijit.byId('locationFilter.rangeUnit').get('value')
        };
        var postalCode = dijit.byId('locationFilter.postalCode').get('value');
        if (0 < postalCode.length) {
            parameters['postalCode'] = postalCode;
        }
        else {
            parameters['latitude'] = dijit.byId('locationFilter.latitude').get('value');
            parameters['longitude'] = dijit.byId('locationFilter.longitude').get('value');
        }
        return parameters;
    };
    localModule.getLocationKeys = function() {
        localModule.loadEntityKeys('Location', localModule.prepareLocationParameters());
    };
    localModule.getStoreKeys = function() {
        localModule.loadEntityKeys('Store', localModule.prepareLocationParameters());
    };
    localModule.resolveLocation = function() {
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: {
                'countryCode': dijit.byId('location.countryCode').get('value'),
                'postalCode': dijit.byId('location.postalCode').get('value'),
                'consumerKey': 0,
                'key': 0,
                '<%= CommandProcessor.DEBUG_MODE_PARAM %>': 'yes'
            },
            handleAs: 'json',
            load: function(response, ioArgs) {
                localModule.fetchEntity('location.key', 'Location');
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/_tasks/validateLocation'
        });
    };
    localModule._earlyHourTime = new Date(2010,0,1,0,0,0,0);
    localModule.searchEntityKey = function(filterId, filterName, entityName, targetId, parameters) {
        var filterField = dijit.byId(filterId);
        if (filterField.validate && !filterField.validate()) {
            alert('Filter has an invalid value');
            return;
        }

        var dialog = dijit.byId('entityKeysDialog');
        dialog.set('title', entityName + ' identifiers');
        dojo.byId('keyZone').innerHTML = '&nbsp;Loading...';
        dialog.show();

        dojo.animateProperty({
            node: 'keyZone',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        var data = {
            'onlyKeys': true,
            '<%= CommandProcessor.DEBUG_MODE_PARAM %>': 'yes'
        };
        if (filterName.indexOf('Date') != -1) {
            data[filterName] = twetailer.Common.toISOString(filterField.get('value'), localModule._earlyHourTime);
        }
        else if (entityName == 'Location') {
            data[filterName] = filterField.get('value');
        }
        else {
            data[filterName] = '*' + filterField.get('value');
        }
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: dojo.mixin(data, parameters || {}),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var resources = response.resources;
                    var keys = response.resources;
                    var keyNb = keys.length;
                    var out = [];
                    if (keyNb == 0) {
                        out.push('None');
                    }
                    else {
                        var deco = localModule.decorationOfEntityLinks;
                        for(var i=0; i<keyNb; i++) {
                            var key = keys[i];
                            out.push(deco[0]); out.push(entityName.toLowerCase());
                            out.push(deco[1]); out.push(key);
                            out.push(deco[2]); out.push(entityName.toLowerCase());
                            out.push(deco[3]); out.push(entityName);
                            out.push(deco[4]); out.push(entityName);
                            out.push(deco[5]); out.push(key);
                            out.push(deco[6]); out.push(key);
                            out.push(deco[7]);
                        }
                    }
                    dojo.byId('keyZone').innerHTML = out.join('');
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: 'keyZone',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/' + entityName
        });
    };
    </script>

    <script src="https://maps-api-ssl.google.com/maps/api/js?v=3&sensor=false&language=<%= localeId %>" type="text/javascript"></script>
</body>
</html>
