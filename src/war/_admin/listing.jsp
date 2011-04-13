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
        data-dojo-config="parseOnLoad: false, isDebug: true, useXDomain: true, baseUrl: './', modulePaths: { dojo: '<%= cdnBaseURL %>/dojo', dijit: '<%= cdnBaseURL %>/dijit', dojox: '<%= cdnBaseURL %>/dojox', twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
        src="<%= cdnBaseURL %>/dojo/dojo.xd.js"
        type="text/javascript"
    ></script><%
    }
    else { // elif (!useCDN)
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: true, baseUrl: '/js/dojo/dojo/', modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/blank.html'"
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
        <div data-dojo-type="dijit.layout.BorderContainer" data-dojo-props="gutters: false, region: 'center'" id="centerZone" style="height: 100%;">
            <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region: 'top'">
                <div style="float: right">
                    Date filter:
                    <select
                        data-dojo-type="dijit.form.Select"
                        data-dojo-propos="hasDownArrow: true"
                        id="dateFilter"
                    >
                        <option value="modificationDate">Modification</option>
                        <option value="creationDate" selected="selected">Creation</option>
                    </select>
                    <input data-dojo-type="dijit.form.DateTextBox" id="dateLimit" type="text" />
                    <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: localModule.loadGrid">Load</button>
                </div>
                <span style="float:left;width:10em">Pending transfer:</span>
                <span id="counts.transfer">none</span>.
                <br/>
                <span style="float:left;width:10em">Consumers:</span>
                total: <span id="counts.consumers.total">0</span>,
                <b>moderated: <span id="counts.consumers.moderated">0</span></b>,
                unconfirmed: <span id="counts.consumers.unconfirmed">0</span>,
                blocked: <span id="counts.consumers.blocked">0</span>.
                <br/>
                <span style="float:left;width:10em">Demands:</span>
                total: <span id="counts.demands.total">0</span>,
                open: <span id="counts.demands.opened">0</span>,
                <b>published: <span id="counts.demands.published">0</span></b>,
                invalid: <span id="counts.demands.invalid">0</span>,
                cancelled: <span id="counts.demands.cancelled">0</span>,
                expired: <span id="counts.demands.expired">0</span>,
                confirmed: <span id="counts.demands.confirmed">0</span>,
                closed: <span id="counts.demands.closed">0</span>.
                <br/>
                <span style="float:left;width:10em">Proposals:</span>
                total: <span id="counts.proposals.total">0</span>,
                rated: <span id="counts.proposals.rated">0</span>.
            </div>
            <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region: 'center'" style="padding: 0">
                <div id="consumerGrid"></div>
            </div>
        </div>
        <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region: 'bottom'" id="footerZone">
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
        dojo.require('dojox.grid.TreeGrid');
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
        var limit = new Date();
        limit.setDate(limit.getDate() - 7); // Last 7 days
        dijit.byId('dateLimit').set('value', limit);

        dijit.byId('topContainer').resize();
    };
    localModule.loadGrid = function() {
        var dateFilter = dijit.byId('dateFilter').get('value');
        var dateLimit = dijit.byId('dateLimit').get('value');
        dateLimit = twetailer.Common.toISOString(dateLimit, null);

        localModule.initializeCounters();
        localModule.fetchConsumers(dateFilter, dateLimit);
    };
    localModule.instanciateTreeGrid = function(model) {
        var grid = dijit.byId('consumerList');
        if (grid) {
            grid.setModel(model);
            return grid;
        }
        var layout = [
            { name: 'Summary', get: localModule.displayInfo , width: 'auto' },
            { name: 'State', field: 'state', width: '10em' },
            { name: 'Content', get: localModule.displayContent, width: 'auto' },
            { name: 'Creation Date', field: 'creationDate', styles: 'text-align: center;', width: '12em' },
            { name: 'Modification Date', field: 'modificationDate', styles: 'text-align: center;', width: '12em' },
            { name: 'Tracking', get: localModule.displayTracking, width: 'auto' }
        ];
        grid = new dojox.grid.TreeGrid({
            id: 'consumerList',
            treeModel: model,
            structure: layout,
            defaultOpen: true
        }, 'consumerGrid');
        grid.startup();
        dojo.connect(window, 'onresize', grid, 'resize');
        return grid;
    };
    localModule.displayInfo = function(rowItem, item) {
        try {
            if (item) {
                var type = item._type[0], key = item.key[0];
                if (type == 'Consumer') {
                    var name = item.name[0], email = item.email[0];
                    return '<a href="monitoring.jsp?type=' + type + '&key=' + key + '" target="_monitoring">' + type + ' ' + key + '</a>' +
                        '<br/><div style="float:left;width:18px">&nbsp;</div>' + name +
                        (name != email ? ('<br/><div style="float:left;width:18px">&nbsp;</div>' + email) : '');
                }
                return type + ' ' + key;
            }
        }
        catch(ex) {
            return '<span style="color:red;">Error info on row: ' + rowItem + '</span>';
        }
    };
    localModule.displayTracking = function(rowItem, item) {
        try {
            if (item) {
                if (item._tracking && 0 < item._tracking.length) {
                    return item._tracking[0].replace(/\\n/g, '<br/>');
                }
                return '';
            }
        }
        catch(ex) {
            return '<span style="color:red;">Error tracking on row: ' + rowItem + '</span>';
        }
    };
    localModule.displayContent = function(rowItem, item) {
        try {
            if (item) {
                // TODO: return #hashTags
                // TODO: return metadata
                if (item.content && 0 < item.content.length) {
                    var content = item.content[0].replace(/\\n/g, '<br/>');
                    if (item.hashTags && item.hashTags.length == 2) {
                        return '<span style="color:red;">' + content + '</span>';
                    }
                    return content;
                }
                return '';
            }
        }
        catch(ex) {
            return '<span style="color:red;">Error content on row: ' + rowItem + '</span>';
        }
    };
    localModule.initializeCounters = function() {
        localModule.counts = { transfer: 0, consumers: { total: 0, moderated: 0, unconfirmed: 0, blocked: 0 }, demands: { total: 0, opened: 0, published: 0, invalid: 0, cancelled: 0, expired: 0, confirmed: 0, closed: 0 }, proposals: { total: 0, rated: 0 }};
    };
    localModule.updateCounters = function() {
        dojo.byId('counts.transfer').innerHTML = localModule.counts.transfer;
        dojo.byId('counts.consumers.total').innerHTML = localModule.counts.consumers.total;
        dojo.byId('counts.consumers.moderated').innerHTML = localModule.counts.consumers.moderated;
        dojo.byId('counts.consumers.unconfirmed').innerHTML = localModule.counts.consumers.unconfirmed;
        dojo.byId('counts.consumers.blocked').innerHTML = localModule.counts.consumers.blocked;
        dojo.byId('counts.demands.total').innerHTML = localModule.counts.demands.total;
        dojo.byId('counts.demands.total').innerHTML = localModule.counts.demands.total;
        dojo.byId('counts.demands.opened').innerHTML = localModule.counts.demands.opened;
        dojo.byId('counts.demands.published').innerHTML = localModule.counts.demands.published;
        dojo.byId('counts.demands.invalid').innerHTML = localModule.counts.demands.invalid;
        dojo.byId('counts.demands.cancelled').innerHTML = localModule.counts.demands.cancelled;
        dojo.byId('counts.demands.expired').innerHTML = localModule.counts.demands.expired;
        dojo.byId('counts.demands.confirmed').innerHTML = localModule.counts.demands.confirmed;
        dojo.byId('counts.demands.closed').innerHTML = localModule.counts.demands.closed;
        dojo.byId('counts.proposals.total').innerHTML = localModule.counts.proposals.total;
        dojo.byId('counts.proposals.rated').innerHTML = localModule.counts.proposals.rated;
    };
    localModule.fetchConsumers = function(dateFilter, dateLimit) {
        ++ localModule.counts.transfer;
        localModule.updateCounters();
        var data = { '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes' };
        data[dateFilter] = dateLimit;
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: data,
            handleAs: 'json',
            load: function(response, ioArgs) {
                -- localModule.counts.transfer;
                if (response !== null && response.success) {
                    // Build the array of Consumer instances for the grid
                    var resources = response.resources, idx, limit = resources.length, resource, keys = [];
                    for (idx = 0; idx < limit; idx++) {
                        resource = resources[idx];
                        resource.key = '' + resource.key;
                        resource._type = 'Consumer';
                        switch(resource.autonomy) {
                        case 'UNCONFIRMED': resource.state = 'unconfirmed'; ++ localModule.counts.consumers.unconfirmed; break;
                        case 'BLOCKED': resource.state = 'blocked'; ++ localModule.counts.consumers.blocked; break;
                        default: resource.state = 'moderated'; ++ localModule.counts.consumers.moderated;
                        }
                        keys.push(resource.key);
                    }
                    localModule.counts.consumers.total += limit;
                    // Build the store and the model for the grid
                    localModule.jsonStore = new dojo.data.ItemFileWriteStore({ data: { identifier: 'key', label: 'key', items: resources, numRows: limit } });
                    localModule.treeModel = new dijit.tree.ForestStoreModel({
                        store: localModule.jsonStore,
                        query: { _type: 'Consumer' },
                        rootId: 'consumerBase',
                        rootLabel: 'ASE Consumer Base',
                        deferItemLoadingUntilExpand: true,
                        start: 0,
                        count: 10,
                        sort: {
                            attribute: 'creationDate',
                            descending: false
                        }
                    });
                    // Fetch the grid
                    localModule.instanciateTreeGrid(localModule.treeModel);
                    // Get the related Demand instances
                    if (0 < keys.length) {
                        localModule.fetchDemands(keys, dateFilter, dateLimit);
                    }
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                localModule.updateCounters();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/Consumer'
        });
    };
    localModule.fetchDemands = function(keys, dateFilter, dateLimit) {
        var idx, limit = keys.length, consumerKey, data;
        for (idx = 0; idx < limit; idx++) {
            consumerKey = keys[idx];
            ++ localModule.counts.transfer;
            localModule.updateCounters();
            data = {
                'pointOfView': 'CONSUMER',
                'onBehalfConsumerKey': consumerKey,
                '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes'
            };
            data[dateFilter] = dateLimit;
            dojo.xhrGet({
                headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                content: data,
                handleAs: 'json',
                load: function(response, ioArgs) {
                    -- localModule.counts.transfer;
                    if (response !== null && response.success) {
                        var resources = response.resources, jdx, jimit = resources.length, resource, anyProposal = false;
                        for (jdx = 0; jdx < jimit; jdx++) {
                            resource = resources[jdx];
                            resource.key = '' + resource.key;
                            resource.ownerKey = '' + resource.ownerKey;
                            resource._type = 'Demand';
                            switch(resource.state) {
                            case 'opened': ++ localModule.counts.demands.opened; break;
                            case 'published': ++ localModule.counts.demands.published; break;
                            case 'invalid': ++ localModule.counts.demands.invalid; break;
                            case 'cancelled': ++ localModule.counts.demands.cancelled; break;
                            case 'expired': ++ localModule.counts.demands.expired; break;
                            case 'confirmed': ++ localModule.counts.demands.confirmed; break;
                            case 'closed': ++ localModule.counts.demands.closed; break;
                            default: alert('Unexpected demand state: ' + resource.state);
                            }
                            localModule.addChild(resource.ownerKey, resource);
                            anyProposal = anyProposal || resource.proposalKeys;
                        }
                        localModule.counts.demands.total += jimit;
                        if (anyProposal) {
                            localModule.fetchProposals(resource.ownerKey, dateFilter, dateLimit);
                        }
                    }
                    else {
                        alert(response.message+'\nurl: '+ioArgs.url);
                    }
                    localModule.updateCounters();
                },
                error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
                url: '/API/Demand'
            });
        }
    };
    localModule.fetchProposals = function(consumerKey, dateFilter, dateLimit) {
        ++ localModule.counts.transfer;
        localModule.updateCounters();
        var data = {
            'pointOfView': 'CONSUMER',
            'onBehalfConsumerKey': consumerKey,
            '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes'
        };
        data[dateFilter] = dateLimit;
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: data,
            handleAs: 'json',
            load: function(response, ioArgs) {
                -- localModule.counts.transfer;
                if (response !== null && response.success) {
                    var resources = response.resources, jdx, jimit = resources.length, resource, keys = [];
                    for (jdx = 0; jdx < jimit; jdx++) {
                        resource = resources[jdx];
                        resource.key = '' + resource.key;
                        resource.demandKey = '' + resource.demandKey;
                        resource._type = 'Proposal';
                        if (resource.rated) {
                            ++ localModule.counts.proposals.rated;
                        }
                        localModule.addChild(resource.demandKey, resource);
                    }
                    localModule.counts.proposals.total += jimit;
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                localModule.updateCounters();
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

    <script src="https://maps-api-ssl.google.com/maps/api/js?v=3&sensor=false&language=<%= localeId %>" type="text/javascript"></script>
</body>
</html>
