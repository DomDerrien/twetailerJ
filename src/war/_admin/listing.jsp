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
    String cdnBaseURL = appSettings.getCdnBaseURL();
    cdnBaseURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.6"; // TODO: change at the application level

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <title>Listing Console</title>
    <meta http-equiv="content-type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <%
    if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/claro/claro.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/Grid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/claroGrid.css";
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><style type="text/css">
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/claro/claro.css";
        @import "/js/dojo/dojox/grid/resources/Grid.css";
        @import "/js/dojo/dojox/grid/resources/claroGrid.css";
        @import "/css/console.css";
    </style><%
    } // endif (useCDN)
    %>
    <style type="text/css">
    </style>
</head>
<body class="claro">

    <!--
    class="tundra" => class="claro"
    djConfig => data-dojo-config
    dojoType => data-dojo-type
    all widget properties into a single attribute => data-dojo-props
     -->

    <div id="introFlash">
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>

    <%
    if (useCDN) {
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: true, useXDomain: true, baseUrl: './', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/html/blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    }
    else { // elif (!useCDN)
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: true, baseUrl: '/js/dojo/dojo/', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/html/blank.html'"
        src="/js/dojo/dojo/dojo.js"
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
        <div data-dojo-type="dijit.layout.ContentPane" id="centerZone" data-dojo-props="region:'center'"><div id="consumerGrid"></div></div>
        <div data-dojo-type="dijit.layout.ContentPane" id="footerZone" data-dojo-props="region: 'bottom'">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <div
        data-dojo-type="dijit.Dialog"
        id="entityKeysDialog"
        style="min-width:260px;"
    >
        <div id="keyZone" style="min-height:60px;"></div>
        <div class="dijitDialogPaneActionBar" style="text-align:right;margin-top:5px;">
            <button data-dojo-type="dijit.form.Button" onclick="dijit.byId('entityKeysDialog').hide();" type="button">Close</button>
        </div>
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
        dojo.require('dijit.form.Textarea');
        dojo.require('dijit.form.TextBox');
        dojo.require('dijit.tree.ForestStoreModel');
        dojo.require('dojox.analytics.Urchin');
        dojo.require('dojox.data.JsonRestStore');
        dojo.require("dojox.grid.TreeGrid");
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
        // 1. Prepare local variables
        // dojo.date.stamp.fromISOString(value)
        var limit = new Date();
        limit.setMonth(limit.getMonth() - 1);

        // 2. Start to get live data
        localModule.fetchConsumers(limit);

        dijit.byId('topContainer').resize();
    };
    localModule.instanciateTreeGrid = function(model) {
        var layout = [
            { name: "Type", field: "_type", width: "auto" },
            { name: "Key", field: "key", width: "auto" },
            { name: "State", field: "state", width: "auto" },
            { name: "Name", field: "name", width: "auto" },
            { name: "Content", field: "content", width: "auto" },
            { name: "Expiration Date", field: "expirationDate", width: "auto" }
        ];
        var grid = new dojox.grid.TreeGrid({
            id: 'consumerList',
            treeModel: model,
            structure: layout,
            defaultOpen: true
        }, 'consumerGrid');
        grid.startup();
        dojo.connect(window, "onresize", grid, "resize");
        return grid;
    };
    localModule.fetchConsumers = function(modificationDate) {
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: {
                '>modificationDate': twetailer.Common.toISOString(modificationDate, null),
                '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes'
            },
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    // Build the array of Consumer instances for the grid
                    var resources = response.resources, idx, limit = resources.length, resource, keys = [];
                    for (idx = 0; idx < limit; idx++) {
                        resource = resources[idx];
                        resource._type = 'Consumer';
                        resource.state = resource.autonomy;
                        keys.push(resource.key);
                    }
                    // Build the store and the model for the grid
                    localModule.jsonStore = new dojo.data.ItemFileWriteStore({ data: { identifier: 'key', label: 'key', items: resources } });
                    localModule.treeModel = new dijit.tree.ForestStoreModel({
                        store: localModule.jsonStore,
                        query: { _type: 'Consumer' },
                        rootId: 'consumerBase',
                        rootLabel: 'ASE Consumer Base',
                        childrenAttrs: [ '_children' ]
                    });
                    // Fetch the grid
                    localModule.instanciateTreeGrid(localModule.treeModel);
                    // Get the related Demand instances
                    if (0 < keys.length) {
                        localModule.fetchDemands(keys, modificationDate);
                    }
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/Consumer'
        });
    };
    localModule.fetchDemands = function(keys, modificationDate) {
        var idx, limit = keys.length, consumerKey;
        for (idx = 0; idx < limit; idx++) {
            consumerKey = keys[idx];
            dojo.xhrGet({
                headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                content: {
                    'pointOfView': 'CONSUMER',
                    'onBehalfConsumerKey': consumerKey,
                    '>modificationDate': twetailer.Common.toISOString(modificationDate, null),
                    '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes'
                },
                handleAs: 'json',
                load: function(response, ioArgs) {
                    if (response !== null && response.success) {
                        var resources = response.resources, jdx, jimit = resources.length, resource;
                        for (jdx = 0; jdx < jimit; jdx++) {
                            resource = resources[jdx];
                            resource._type = 'Demand';
                            localModule.addChild(resource.ownerKey, resource);
                        }
                        localModule.fetchProposals(consumerKey, modificationDate);
                    }
                    else {
                        alert(response.message+'\nurl: '+ioArgs.url);
                    }
                },
                error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
                url: '/API/Demand'
            });
        }
    };
    localModule.fetchProposals = function(consumerKey, modificationDate) {
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: {
                'pointOfView': 'CONSUMER',
                'onBehalfConsumerKey': consumerKey,
                '>modificationDate': twetailer.Common.toISOString(modificationDate, null),
                '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes'
            },
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var resources = response.resources, jdx, jimit = resources.length, resource, keys = [];
                    for (jdx = 0; jdx < jimit; jdx++) {
                        resource = resources[jdx];
                        resource._type = 'Proposal';
                        localModule.addChild(resource.demandKey, resource);
                    }
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/Proposal'
        });
    };
    localModule.addChild = function(parentId, child) {
        localModule.jsonStore.fetchItemByIdentity({
            identity: parentId,
            onItem: function(item){
                if (item) {
                    localModule.treeModel.newItem(child, item);
                }
            }
        });
    };
    </script>
</body>
</html>
