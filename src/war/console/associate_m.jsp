<!doctype html>
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.List"
    import="java.util.Locale"
    import="java.util.Map"
    import="java.util.ResourceBundle"
    import="com.dyuproject.openid.OpenIdUser"
    import="com.dyuproject.openid.RelyingParty"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.StringUtils"
    import="javamocks.io.MockOutputStream"
    import="twetailer.connector.BaseConnector.Source"
    import="twetailer.connector.ChannelConnector"
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Demand"
    import="twetailer.dto.Location"
    import="twetailer.dto.Store"
    import="twetailer.dto.SaleAssociate"
    import="twetailer.j2ee.BaseRestlet"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.CommandSettings.State"
    import="twetailer.validator.MobileBrowserDetector"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String appVersion = appSettings.getProductVersion();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    cdnBaseURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.6"; // TODO: change at the application level
    useCDN = true;

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Get the logged user record
    OpenIdUser loggedUser = BaseRestlet.getLoggedUser(request);
    Consumer consumer = LoginServlet.getConsumer(loggedUser);
    Long saleAssociateKey = consumer.getSaleAssociateKey();

    // Redirects non sale associates
    if (saleAssociateKey == null) {
        response.sendRedirect("./");
    }

    SaleAssociate saleAssociate = LoginServlet.getSaleAssociate(loggedUser);

    // Prepare logged user information
    MockOutputStream serializedConsumer = new MockOutputStream();
    consumer.toJson().toStream(serializedConsumer, false);
    MockOutputStream serializedAssociate = new MockOutputStream();
    saleAssociate.toJson().toStream(serializedAssociate, false);

    boolean isAndroid = new MobileBrowserDetector(request).isAndroid();
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "coreAssoc_localized_page_name", locale) %></title>
    <meta http-equiv="content-type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,minimum-scale=1,user-scalable=no"/>
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-touch-fullscreen" content="yes" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>

    <%
    if (useCDN) {
    %><link href="<%= cdnBaseURL %>/dojox/mobile/themes/<%= isAndroid ? "android/android" : "iphone/iphone" %>.css" rel="stylesheet"></link>
    <link href="/css/console.css" rel="stylesheet" type="text/css" /><%
    }
    else { // elif (!useCDN)
    %><link href="/js/release/<%= appVersion %>/dojox/mobile/themes/<%= isAndroid ? "android/android" : "iphone/iphone" %>.css" rel="stylesheet" type="text/css" />
    <link href="/css/console.css" rel="stylesheet" type="text/css" /><%
    } // endif (useCDN)
    %>

    <%
    if (useCDN) {
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', modulePaths: { dojo: '<%= cdnBaseURL %>/dojo', dijit: '<%= cdnBaseURL %>/dijit', dojox: '<%= cdnBaseURL %>/dojox', twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/_includes/dojo_blank.html', locale: '<%= localeId %>'"
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
        src="/js/release/<%= appVersion %>/ase/mobile.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <script type="text/javascript">
        dojo.require('dojox.mobile.parser');
        dojo.require('dojox.mobile');
        dojo.require('dojox.mobile.ScrollableView');
        dojo.require('dojox.mobile.TabBar');
        dojo.require('dojox.mobile.app.TextBox');
        dojo.requireIf(!dojo.isWebKit, 'dojox.mobile.compat');
        dojo.require('twetailer.m.DemandList');
        dojo.require('twetailer.m.ProposalList');
        dojo.require('twetailer.m.ProposalAdd');
        dojo.ready(function() {
            dojox.mobile.parser.parse();
        });
    </script>

    <style type="text/css">
        .viewRefresh {
            float: right;
        }
        .viewRefresh img   {
            margin-top: 6px;
        }
        .android .viewRefresh img   {
            margin-top: 3px;
        }

        .mblScrollableViewContainer {
            padding-bottom: 40px !important;
        }

        .viewList li {
            min-height: 55px;
            padding-top: 8px;
        }

        .viewListItem { /* special formatting to allow 2 lines of content */
            line-height: 18px;
            height: auto;
        }

        .itemIcon, .itemActions {
            float: left;
            margin: 0 5px;
        }
        .itemActions {
            float: right;
            margin-right: 10px;
            text-align:center;
            border: 1px inset #9CACC0;
            border-radius: 5px;
            -webkit-border-radius: 5px;
        }
        .itemIcon>img, .itemActions>a>img {
            width: 24px;
            height: 24px;
            padding: 3px;
        }
        .itemActions>span {
            font-size: 50%;
            padding: 2px;
        }

        .itemDetails {
        }

        .itemOthers {
            color:#777;
            font-size:65%;
            font-weight:normal;
        }
        .android .itemOthers {
            color:#bbb;
        }

        .itemOthers span {
            font-weight: bold;
        }
        .android .itemOthers span {
            color:#ddd;
        }
    </style>
</head>
<body>

    <div id="demandList" dojoType="twetailer.m.DemandList" selected="true">
        <h1 dojoType="dojox.mobile.Heading">
            <div dojoType="dojox.mobile.ToolBarButton" class="mblDomButton viewRefresh" style="float:right;" icon="/js/twetailer/m/resources/images/refresh.png"></div>
            Published Demands
        </h1>
        <ul dojoType="dojox.mobile.RoundRectList" class="viewList"></ul>
    </div>

    <div id="proposalList" dojoType="twetailer.m.ProposalList">
        <h1 dojoType="dojox.mobile.Heading" back="Published Demands" moveTo="demandList">
            <div dojoType="dojox.mobile.ToolBarButton" class="mblDomButton viewRefresh" style="float:right;" icon="/js/twetailer/m/resources/images/refresh.png"></div>
            Associated Proposals
        </h1>
        <h3 id="messageProposalList" style="padding:0 5px"></h3>
        <ul dojoType="dojox.mobile.RoundRectList" class="viewList"></ul>
    </div>

    <div id="proposalAdd" dojoType="twetailer.m.ProposalAdd">
        <h1 dojoType="dojox.mobile.Heading" back="Associated Proposals" moveTo="proposalList">Create Proposal</h1>
        <div dojoType="dojox.mobile.RoundRect" shadow="true">
            <table width="100%">
                <tr><th colspan="2"><img src="/js/twetailer/m/resources/images/cart.png" style="width:24px;height:24px;vertical-align:middle;"/> Demand summary</th></tr>
                <tr><td>Demand Key:</td><td id="demand.key"></td></tr>
                <tr><td>Description:</td><td id="demand.content"></td></tr>
                <tr><td>Due date:</th><td id="demand.dueDate"></td></tr>
            </table>
        </div>
        <div dojoType="dojox.mobile.RoundRect" shadow="true">
            <table width="100%">
                <tr><th colspan="2"><img src="/js/twetailer/m/resources/images/database.png" style="width:24px;height:24px;vertical-align:middle;"/> Proposal attributes</th></tr>
                <tr><td>Quantity:</td><td><input dojoType="dojox.mobile.app.TextBox" trim="true" id="proposal.quantity" value="1" style="width:3em"></td></tr>
                <tr><td>Unit price ($):</td><td><input dojoType="dojox.mobile.app.TextBox" trim="true" placeHolder="10.00" id="proposal.price" value="" style="width:7em"> <span style="color:#aaa;font-size:65%">(Optional)</span></td></tr>
                <tr><td>Total cost ($):</td><td><input dojoType="dojox.mobile.app.TextBox" trim="true" placeHolder="11.57" id="proposal.total" value="" style="width:7em"></td></tr>
                <tr><td>Description:</td><td><input dojoType="dojox.mobile.app.TextBox" trim="true" placeHolder="Describe your proposal" id="proposal.content" value="" style="width:100%""></td></tr>
            </table>
        </div>
        <div dojoType="dojox.mobile.RoundRect" shadow="true" style="text-align:center">
            <button dojoType="dojox.mobile.Button" id="proposalCreate"><img src="/js/twetailer/m/resources/images/database_add.png"/> Create</button>
        </div>
    </div>

    <!--
    <ul dojoType="dojox.mobile.TabBar" iconBase="images/iconStrip.png" style="margin-top:-49px;">
        < ! - - All iconPos values specify: top left width height - - >
        <li dojoType="dojox.mobile.TabBarButton" iconPos1="0,0,29,30" iconPos2="29,0,29,30" selected="true" moveTo="demands">Demands</li>
        <li dojoType="dojox.mobile.TabBarButton" iconPos1="0,29,29,30" iconPos2="29,29,29,30" moveTo="mentions">Proposals</li>
        <li dojoType="dojox.mobile.TabBarButton" iconPos1="0,58,29,30" iconPos2="29,58,29,30" moveTo="settings">Settings</li>
    </ul>
    -->
</body>
</html>
