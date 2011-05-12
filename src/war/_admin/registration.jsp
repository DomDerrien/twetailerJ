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
    <title>Sale Associate Registration Page</title>
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
        div.action {
            float: left;
            text-align: center;
            padding: 3px 30px;
            margin: 1px;
            border-radius: 5px;
            background-color: #dfd;
        }
        div.action.current {
            background-color: #afa;
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
        data-dojo-config="parseOnLoad: false, isDebug: true, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/_includes/dojo_blank.html'"
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
        <div
            data-dojo-type="dijit.layout.ContentPane"
            data-dojo-props="region: 'left', style: 'width:20%'"
            style="margin-top: 5px; margin-bottom: 10px;"
        ></div>
        <div
            data-dojo-type="dijit.layout.ContentPane"
            data-dojo-props="region: 'right', style: 'width:20%'"
            style="margin-top: 5px; margin-bottom: 10px;"
        ></div>
        <div
            id="centerZone"
            data-dojo-type="dijit.layout.ContentPane"
            data-dojo-props="region: 'center'"
        >
            <div data-dojo-type="dijit.layout.StackContainer" id="wizard" data-dojo-id="wizard">
                <div data-dojo-type="dijit.layout.ContentPane" data-dojo-id="step1">
                    <fieldset class="entityInformation" id="innerStep1">
                        <legend>Location Creation/Retrieval</legend>
                        <form data-dojo-type="dijit.form.Form" id="locationInformation" data-dojo-props="onSubmit: function() { localModule.createLocation(); return false; }">
                            <div>
                                <label for="<%= Location.POSTAL_CODE %>">Postal Code</label><br/>
                                <input
                                    id="<%= Location.POSTAL_CODE %>"
                                    data-dojo-type="dijit.form.ValidationTextBox"
                                    data-dojo-props="name: '<%= Location.POSTAL_CODE %>', invalidMessage: '<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>', placeHolder: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_default_CA", locale) %>', regExp: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_regExp_CA", locale) %>', required: true, style: 'width: 6em'"
                                    type="text"
                                />
                            </div>
                            <div>
                                <label for="<%= Location.COUNTRY_CODE %>">Country Code</label><br/>
                                <select
                                    id="<%= Location.COUNTRY_CODE %>"
                                    data-dojo-type="dijit.form.Select"
                                    data-dojo-props="name: '<%= Location.COUNTRY_CODE %>', onChange: function() { twetailer.Common.updatePostalCodeFieldConstraints(this.value, '<%= Location.POSTAL_CODE %>'); }"
                                >
                                    <option value="CA" selected="selected">Canada</option>
                                    <option value="US">United States of America</option>
                                </select>
                            </div>
                            <br/>
                            <div class="action current"><button data-dojo-type="dijit.form.Button" data-dojo-props="type: 'submit'">Create Location &gt;&gt;</button><br/>(with values from above)</div>
                            <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.selectChild(step2);dijit.byId('<%= Store.LOCATION_KEY %>').focus(); }" type="button">New Store &gt;&gt;</button><br/>(need valid Location key)</div>
                            <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.selectChild(step3);dijit.byId('<%= SaleAssociate.STORE_KEY %>').focus(); }" type="button">New Sale Associate &gt;&gt;</button><br/>(need valid Store key)</div>
                        </form>
                    </fieldset>
                </div>
                <div data-dojo-type="dijit.layout.ContentPane" data-dojo-id="step2" style="display:hidden;">
                    <fieldset class="entityInformation" id="innerStep2">
                        <legend>Store Creation</legend>
                        <form data-dojo-type="dijit.form.Form" id="storeInformation" data-dojo-props="onSubmit: function() { localModule.createStore(); return false; }">
                            <div>
                                <label for="<%= Store.LOCATION_KEY %>">Location Key</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= Store.LOCATION_KEY %>" data-dojo-props="name: '<%= Location.LOCATION_KEY %>', required: true, style: 'width:8em;'" type="text" />
                            </div>
                            <div>
                                <label for="<%= Store.NAME %>">Store Name</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= Store.NAME %>" data-dojo-props="name: '<%= Store.NAME %>', required: true, style: 'width:30em;'" type="text" />
                            </div>
                            <div>
                                <label for="<%= Store.ADDRESS %>">Address</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= Store.ADDRESS %>" data-dojo-props="name: '<%= Store.ADDRESS %>', required: true, style: 'width:30em;'" type="text" />
                            </div>
                            <div>
                                <label for="<%= Store.EMAIL %>">Email</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= Store.EMAIL %>" data-dojo-props="name: '<%= Store.EMAIL %>', required: true, style: 'width:30em;'" type="text" />
                            </div>
                            <div>
                                <label for="<%= Store.PHONE_NUMBER %>">Phone Number</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= Store.PHONE_NUMBER %>" data-dojo-props="name: '<%= Store.PHONE_NUMBER %>', required: true, style: 'width:12em;', value: 0" type="text" />
                            </div>
                            <div>
                                <label for="<%= Store.REGISTRAR_KEY %>">Registrar Key</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= Store.REGISTRAR_KEY %>" data-dojo-props="name: '<%= Store.REGISTRAR_KEY %>', required: true, style: 'width:8em;', value: 0" type="text" />
                            </div>
                            <div>
                                <label for="<%= Store.REVIEW_SYSTEM_KEY %>">Review System Key</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= Store.REVIEW_SYSTEM_KEY %>" data-dojo-props="name: '<%= Store.REVIEW_SYSTEM_KEY %>', required: true, style: 'width:8em;', value: 0" type="text" />
                            </div>
                            <div>
                                <label for="<%= Store.STATE %>">State</label><br/>
                                <select data-dojo-type="dijit.form.Select" id="<%= Store.STATE %>" data-dojo-props="name: '<%= Store.STATE %>'">
                                    <option value="referenced" selected="selected">referenced</option>
                                    <option value="declined">declined</option>
                                    <option value="inProgress">inProgress</option>
                                    <option value="waiting">waiting</option>
                                    <option value="active">active</option>
                                    <option value="excluded">excluded</option>
                                </select>
                            </div>
                            <div>
                                <label for="<%= Store.URL %>">Website URL</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= Store.URL %>" data-dojo-props="name: '<%= Store.URL %>', required: true, style: 'width:30em;'" type="text" />
                            </div>
                            <br/>
                            <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.back();dijit.byId('<%= Location.POSTAL_CODE %>').focus(); }" type="button">&lt;&lt; New Location</button><br/>(create or retrieve)</div>
                            <div class="action current"><button data-dojo-type="dijit.form.Button" data-dojo-props="type: 'submit'">Create Store &gt;&gt;</button><br/>(with values from above)</div>
                            <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.selectChild(step3);dijit.byId('<%= SaleAssociate.STORE_KEY %>').focus(); }" type="button">New Sale Associate &gt;&gt;</button><br/>(need valid Store key)</div>
                        </form>
                    </fieldset>
                    <fieldset class="entityInformation" id="innerStep2.1">
                        <legend>Store Retrieval</legend>
                        <ul id="storeList"></ul>
                        <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.getStores(); }" type="button">Get Stores</button><br/>(for the specified location)</div>
                    </fieldset>
                </div>
                <div data-dojo-type="dijit.layout.ContentPane" data-dojo-id="step3" style="display:hidden;">
                    <fieldset class="entityInformation" id="innerStep3">
                        <legend>Sale Associate Creation</legend>
                        <form data-dojo-type="dijit.form.Form" id="saleAssociateInformation" data-dojo-props="onSubmit: function() { localModule.createSaleAssociate(); return false; }">
                            <div>
                                <label for="<%= SaleAssociate.STORE_KEY %>">Store Key</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= SaleAssociate.STORE_KEY %>" data-dojo-props="name: '<%= SaleAssociate.STORE_KEY %>', required: true, style: 'width:8em;'" type="text" />
                            </div>
                            <div>
                                <label for="<%= SaleAssociate.CONSUMER_KEY %>">Consumer Key</label><br/>
                                <input data-dojo-type="dijit.form.ValidationTextBox" id="<%= SaleAssociate.CONSUMER_KEY %>" data-dojo-props="name: '<%= SaleAssociate.CONSUMER_KEY %>', required: true, style: 'width:8em;'" type="text" />
                            </div>
                            <br/>
                            <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.selectChild(step1);;dijit.byId('<%= Location.POSTAL_CODE %>').focus(); }" type="button">&lt;&lt; New Location</button><br/>(create or retrieve)</div>
                            <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.selectChild(step2);dijit.byId('<%= Store.LOCATION_KEY %>').focus(); }" type="button">&lt;&lt; New Store</button><br/>(need valid Location key)</div>
                            <div class="action current"><button data-dojo-type="dijit.form.Button" data-dojo-props="type: 'submit'">Create SaleAssociate &gt;&gt;</button><br/>(with values from above)</div>
                        </form>
                    </fieldset>
                    <fieldset class="entityInformation" id="innerStep3.1">
                        <legend>Sale Associate Retrieval</legend>
                        <ul id="saleAssociateList"></ul>
                        <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.getSaleAssociates(); }" type="button">Get Sale Associates</button><br/>(for the specified store)</div>
                    </fieldset>
                    <fieldset class="entityInformation" id="innerStep3.2">
                        <legend>Consumer Retrieval</legend>
                        <ul id="consumerList"></ul>
                        <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { localModule.getConsumer(); }" type="button">Get Consumer</button><br/>(for the specified consumer</div>
                    </fieldset>
                </div>
                <div data-dojo-type="dijit.layout.ContentPane" data-dojo-id="step5" style="display:hidden;">
                    <fieldset class="entityInformation" id="innerStep5">
                        <legend>Repeat Again ;)</legend>
                        <p>The Sale Associate can now tweet to @twetailer to supply his/her own tags.</p>
                        <br/>
                        <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.selectChild(step1);dijit.byId('<%= Location.POSTAL_CODE %>').focus(); }" type="button">&lt;&lt; Another Location</button></div>
                        <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.selectChild(step2);dijit.byId('<%= Store.LOCATION_KEY %>').focus(); }" type="button">&lt;&lt; Another Store</button></div>
                        <div class="action"><button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { wizard.selectChild(step3);dijit.byId('<%= SaleAssociate.STORE_KEY %>').focus(); }" type="button">&lt;&lt; Another Sale Associate</button></div>
                    </fieldset>
                </div>
            </div>
        </div>
        <div data-dojo-type="dijit.layout.ContentPane" id="footerZone" data-dojo-props="region: 'bottom'">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dojo.parser');
        dojo.require('dijit.Dialog');
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        dojo.require('dijit.layout.StackContainer');
        dojo.require('dijit.form.Button');
        dojo.require('dijit.form.Form');
        dojo.require('dijit.form.Select');
        dojo.require('dijit.form.Textarea');
        dojo.require('dijit.form.TextBox');
        dojo.require('dijit.form.ValidationTextBox');
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
        dijit.byId('topContainer').resize();
        dijit.byId('<%= Location.POSTAL_CODE %>').focus();
    };
    localModule.createLocation = function() {
        var form = dijit.byId('locationInformation');
        if (!form.validate()) {
            alert('Postal code has an invalid value');
            return;
        }
        dojo.animateProperty({
            node: 'innerStep1',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        if (!dijit.byId('locationInformation').validate()) {
            return;
        }
        dojo.xhrPost({
            headers: { 'content-type': 'application/json; charset=<%= StringUtils.HTML_UTF8_CHARSET %>' },
            putData: dojo.formToJson('locationInformation'),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    dijit.byId('<%= Store.LOCATION_KEY %>').focus();
                    dijit.byId('<%= Store.LOCATION_KEY %>').set('value', response.resource.<%= Location.KEY %>);
                    wizard.forward();
                    dijit.byId('<%= Store.NAME %>').focus();
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: 'innerStep1',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/Location/'
        });
    };
    localModule.createStore = function() {
        var form = dijit.byId('storeInformation');
        if (!form.validate()) {
            alert('Some form fields have an invalid value');
            return;
        }
        dojo.animateProperty({
            node: 'innerStep2',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        if (!dijit.byId('storeInformation').validate()) {
            return;
        }
        var data = dojo.formToObject('storeInformation');
        data.locationKey = parseInt(data.locationKey); // Otherwise it's passed as a String
        data.registrarKey = parseInt(data.registrarKey); // Otherwise it's passed as a String
        data.reviewSystemKey = parseInt(data.reviewSystemKey); // Otherwise it's passed as a String
        data.onBehalfAssociateKey = 0; // Because we assume the sale associate account creation
        dojo.xhrPost({
            headers: { 'content-type': 'application/json; charset-<%= StringUtils.HTML_UTF8_CHARSET %>' },
            putData: dojo.toJson(data),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    dijit.byId('<%= SaleAssociate.STORE_KEY %>').focus();
                    dijit.byId('<%= SaleAssociate.STORE_KEY %>').set('value', response.resource.<%= Store.KEY %>);
                    wizard.forward();
                    dijit.byId('<%= SaleAssociate.CONSUMER_KEY %>').focus();
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: 'innerStep2',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/Store/'
        });
    };
    localModule.getStores = function() {
        var locationKey = parseInt(dijit.byId('<%= Store.LOCATION_KEY %>').get('value'));
        if (locationKey.length == 0 || isNaN(locationKey)) {
            alert('You need to specify a valid Location key');
            dijit.byId('<%= Store.LOCATION_KEY %>').focus();
            return;
        }
        dojo.animateProperty({
            node: 'innerStep2.1',
            properties: { backgroundColor: { end: 'green' } }
        }).play();
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=<%= StringUtils.HTML_UTF8_CHARSET %>' },
            content: { <%= Store.LOCATION_KEY %>: locationKey },
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var placeHolder = dojo.byId('storeList');
                    placeHolder.innerHTML = '';
                    dojo.forEach(response.resources, function(store, i) {
                        var listItem = dojo.doc.createElement('li');
                        var onclickHandler =
                            'var saKeyField = dijit.byId(\'<%= SaleAssociate.STORE_KEY %>\');' +
                            'saKeyField.set(\'value\', \'' + store.<%= Store.KEY %> + '\');' +
                            'saKeyField.focus();' +
                            'wizard.forward();' +
                            'return false;';
                        listItem.innerHTML =
                            'Name: <a href="#" onclick="' + onclickHandler + '">' + store.<%= Store.NAME %> + '</a>, ' +
                            'Address: ' + store.<%= Store.ADDRESS %> + ', ' +
                            'Phone Number: ' + store.<%= Store.PHONE_NUMBER %>;
                        placeHolder.appendChild(listItem);
                    });
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: 'innerStep2.1',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/Store/'
        });
    };
    localModule.createSaleAssociate = function() {
        var form = dijit.byId('saleAssociateInformation');
        if (!form.validate()) {
            alert('Some form fields have an invalid value');
            return;
        }
        dojo.animateProperty({
            node: 'innerStep3',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        if (!dijit.byId('saleAssociateInformation').validate()) {
            return;
        }
        var data = dojo.formToObject('saleAssociateInformation');
        data.storeKey = parseInt(data.storeKey); // Otherwise it's passed as a String
        if (data.consumerKey == null || isNaN(data.consumerKey) || data.consumerKey.length == 0) {
            delete data.consumerKey;
        }
        else {
            data.consumerKey = parseInt(data.consumerKey); // Otherwise it's passed as a String
        }
        data.onBehalfAssociateKey = 0; // Because we assume the sale associate account creation
        dojo.xhrPost({
            headers: { 'content-type': 'application/json; charset=<%= StringUtils.HTML_UTF8_CHARSET %>' },
            putData: dojo.toJson(data),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    // No visual feedback
                    wizard.forward();
                }
                else {
                    alert(response.exceptionMessage+'\nurl: '+ioArgs.url+'\n\n'+response.originalExceptionMessage);
                }
                dojo.animateProperty({
                    node: 'innerStep3',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/SaleAssociate/'
        });
    };
    localModule.getSaleAssociates = function() {
        var storeKey = parseInt(dijit.byId('<%= SaleAssociate.STORE_KEY %>').get('value'));
        if (storeKey.length == 0 || isNaN(storeKey)) {
            alert('You need to specify a valid Store key');
            dijit.byId('<%= SaleAssociate.STORE_KEY %>').focus();
            return;
        }
        dojo.animateProperty({
            node: 'innerStep3.1',
            properties: { backgroundColor: { end: 'green' } }
        }).play();
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=<%= StringUtils.HTML_UTF8_CHARSET %>' },
            content: { <%= SaleAssociate.STORE_KEY %>: storeKey },
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var placeHolder = dojo.byId('saleAssociateList');
                    placeHolder.innerHTML = '';
                    dojo.forEach(response.resources, function(saleAssociate, i) {
                        var listItem = dojo.doc.createElement('li');
                        listItem.innerHTML =
                            'Consumer key: <a href="javascript:localModule.getConsumer(' + saleAssociate.<%= SaleAssociate.CONSUMER_KEY %> + ');">' + saleAssociate.<%= SaleAssociate.CONSUMER_KEY %> + '</a>, ' +
                            'Tags: [' + (saleAssociate.<%= SaleAssociate.CRITERIA %> || []) + '], ' +
                            'Hash tags: [' + (saleAssociate.<%= SaleAssociate.HASH_TAGS %> || []) + '], ' +
                            'Rates: ' + saleAssociate.<%= SaleAssociate.CLOSED_PROPOSAL_NB %> + ' / ' + saleAssociate.<%= SaleAssociate.PUBLISHED_PROPOSAL_NB %> + ', ' +
                            'Score: ' + saleAssociate.<%= SaleAssociate.SCORE %>;
                        placeHolder.appendChild(listItem);
                    });
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: 'innerStep3.1',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/SaleAssociate/'
        });
    };
    localModule.getConsumer = function(consumerKey) {
        consumerKey = consumerKey || dijit.byId('<%= SaleAssociate.CONSUMER_KEY %>').get('value');
        if (consumerKey.length == 0) {
            alert('You need to specify a consumer key!');
            dijit.byId('<%= SaleAssociate.CONSUMER_KEY %>').focus();
            return;
        }
        var parameters = { <%= SaleAssociate.CONSUMER_KEY %>: consumerKey };
        dojo.animateProperty({
            node: 'innerStep3.2',
            properties: { backgroundColor: { end: 'green' } }
        }).play();
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=<%= StringUtils.HTML_UTF8_CHARSET %>' },
            content: parameters,
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var placeHolder = dojo.byId('consumerList');
                    placeHolder.innerHTML = '';
                    var consumer = response.resource;
                    var listItem = dojo.doc.createElement('li');
                    listItem.innerHTML =
                        'Key: <a href="#" onclick="javascript:dijit.byId(\'<%= SaleAssociate.CONSUMER_KEY %>\').set(\'value\',' + consumer.<%= Consumer.KEY %> + ');return false;">' + consumer.<%= Consumer.KEY %> + '</a>, ' +
                        'Name: ' + consumer.<%= Consumer.NAME %> + ', ' +
                        'E-mail Address: <a href="mailto:' + consumer.<%= Consumer.EMAIL %> + '">' + consumer.<%= Consumer.EMAIL %> + '</a>, ' +
                        'Jabber Id: <a href="xmpp:' + consumer.<%= Consumer.JABBER_ID %> + '">' + consumer.<%= Consumer.JABBER_ID %> + '</a>, ' +
                        'Twitter Name: <a href="https://twitter.com/' + consumer.<%= Consumer.TWITTER_ID %> +'" target="_blank">' + consumer.<%= Consumer.TWITTER_ID %> + '</a>';
                    placeHolder.appendChild(listItem);
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: 'innerStep3.2',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/Consumer/' + consumerKey
        });
    };
    </script>
</body>
</html>
