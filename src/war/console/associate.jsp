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
    // Redirect mobile browsers
    if (new MobileBrowserDetector(request).isMobileBrowser()) {
        request.getRequestDispatcher("associate_m.jsp").forward(request, response);
        return;
    }

    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String appVersion = appSettings.getProductVersion();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    cdnBaseURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.6"; // TODO: change at the application level

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

%><html dir="ltr" lang="<%= localeId %>">
<head>
    <meta http-equiv="X-UA-Compatible" content="chrome=1" />
    <title><%= LabelExtractor.get(ResourceFileId.third, "coreAssoc_localized_page_name", locale) %></title>
    <meta http-equiv="content-type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <% if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/claro/claro.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/Grid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/claroGrid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/enhanced/resources/claro/Common.css";
        @import "<%= cdnBaseURL %>/dojox/grid/enhanced/resources/claro/EnhancedGrid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/enhanced/resources/claro/Filter.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/FloatingPane.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/ExpandoPane.css";
        @import "<%= cdnBaseURL %>/dojox/widget/SortList/SortList.css";
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><link href="/js/release/<%= appVersion %>/dojo/resources/dojo.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dijit/themes/claro/claro.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/grid/resources/Grid.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/grid/resources/claroGrid.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/grid/enhanced/resources/claro/Common.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/grid/enhanced/resources/claro/EnhancedGrid.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/grid/enhanced/resources/claro/Filter.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/layout/resources/FloatingPane.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/layout/resources/ExpandoPane.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/widget/SortList/SortList.css" rel="stylesheet" type="text/css" />
    <link href="/css/console.css" rel="stylesheet" type="text/css" /><%
    } // endif (useCDN)
    %>
</head>
<body class="claro">

    <div id="topBar"></div>

    <div id="introFlash">
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>

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
        src="/js/release/<%= appVersion %>/ase/associate.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" data-dojo-type="dijit.layout.BorderContainer" data-dojo-props="gutters: false, style: 'height: 100%;'">
        <jsp:include page="/_includes/banner_protected.jsp">
            <jsp:param name="verticalId" value="" />
            <jsp:param name="localeId" value="<%= localeId %>" />
            <jsp:param name="pageForAssociate" value="<%= Boolean.TRUE.toString() %>" />
            <jsp:param name="isLoggedUserAssociate" value="<%= Boolean.toString(saleAssociateKey != null) %>" />
            <jsp:param name="consumerName" value="<%= consumer.getName() %>" />
            <jsp:param name="profilePageURL" value="javascript:twetailer.Associate.showProfile();" />
        </jsp:include>
        <div data-dojo-type="dijit.layout.BorderContainer" id="centerZone" data-dojo-props="gutters: false, region: 'center'">
            <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region: 'top', style: 'margin:10px 10px 0 10px;'">
                <div style="float:right;">
                    <span id="automaticUpdateState" style="padding: 6px; vertical-align: middle;"></span>
                    <select data-dojo-type="dijit.form.Select" data-dojo-props="onChange: function() { dijit.byId('demandList').filter({ <%= Demand.STATE %>: this.value }); }">
                        <option value="*" selected="selected"><%= LabelExtractor.get(ResourceFileId.third, "core_stateSelector_anyState", locale) %></option>
                        <option value="<%= State.opened %>"><%= LabelExtractor.get(ResourceFileId.master, "cl_state_opened", locale) %></option>
                        <option value="<%= State.invalid %>"><%= LabelExtractor.get(ResourceFileId.master, "cl_state_invalid", locale) %></option>
                        <option value="<%= State.published %>"><%= LabelExtractor.get(ResourceFileId.master, "cl_state_published", locale) %></option>
                        <option value="<%= State.confirmed %>"><%= LabelExtractor.get(ResourceFileId.master, "cl_state_confirmed", locale) %></option>
                    </select>
                    <button
                        data-dojo-props="busyLabel: '<%= LabelExtractor.get(ResourceFileId.third, "refreshing_button_state", locale) %>', iconClass: 'silkIcon silkIconRefresh', onClick: function() { twetailer.Associate.loadNewDemands(); }"
                        data-dojo-type="dojox.form.BusyButton"
                        id="refreshButton"
                    ><%= LabelExtractor.get(ResourceFileId.third, "refresh_button", locale) %></button>
                </div>
            </div>
            <div id="demandList"></div>
        </div>
        <div data-dojo-type="dijit.layout.ContentPane" id="footerZone" data-dojo-props="region: 'bottom'">
            <%= LabelExtractor.get(ResourceFileId.master, "product_rich_copyright", locale) %>
        </div>
    </div>

    <div
        id="proposalForm"
        data-dojo-type="dijit.Dialog"
        data-dojo-props="execute: function() { try { twetailer.Associate.updateProposal(arguments[0]); } catch(ex) { alert('ex: '+ex); } }, title: '<%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_formTitle_creation", locale) %>'"
    >
        <input data-dojo-type="dijit.form.TextBox" data-dojo-props="type: 'hidden', name: 'demandKey'" id="demand.key" />
        <input data-dojo-type="dijit.form.TextBox" data-dojo-props="type: 'hidden', name: 'hashTags'" id="demand.hashTags" />
        <fieldset class="entityInformation">
            <legend><%= LabelExtractor.get(ResourceFileId.third, "core_demandInfo", locale) %></legend>
            <table style="width: 100%">
                <tr id="proposalForm.demand.hashTags">
                    <td align="right"><label for="demand.hashTags"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandHashTags", locale) %></label></td>
                    <td><input data-dojo-type="dijit.form.TextBox" id="demand.visibleHashTags" data-dojo-props="readOnly: true,  style: 'width:25em;'" /></td>
                </tr>
                <tr id="proposalForm.demand.content">
                    <td align="right"><label for="demand.content"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandCriteria", locale) %></label></td>
                    <td><textarea data-dojo-type="dijit.form.Textarea" id="demand.content" data-dojo-props="readOnly: true, rows: 3, style: 'width:100%;min-height:48px;font-family:\'Droid Sans\', arial, serif;font-size:12px;'"></textarea></td>
                </tr>
                <tr id="proposalForm.demand.metadata">
                    <td align="right"><label for="demand.metadata"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandMetadata", locale) %></label></td>
                    <td><input data-dojo-type="dijit.form.TextBox" id="demand.metadata" data-dojo-props="readOnly: true, style: 'width:25em;'" /></td>
                </tr>
            </table>
        </fieldset>
        <fieldset class="entityInformation">
            <legend><%= LabelExtractor.get(ResourceFileId.third, "core_proposalInfo", locale) %></legend>
            <table class="demandForm" style="width: 100%">
                <tr class="existingAttribute">
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalKey", locale) %></td>
                    <td><input data-dojo-type="dijit.form.TextBox" id="proposal.key" data-dojo-props="name: 'key', readOnly: true, style: 'width:6em;'" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.quantity"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalQuantity", locale) %></label></td>
                    <td><input data-dojo-type="dijit.form.NumberSpinner" id="proposal.quantity" data-dojo-props="constraints: {min:1,places:0}, name: 'quantity', style: 'width:3em;'" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.time"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalDueDate", locale) %></label></td>
                    <td>
                        <input data-dojo-type="dijit.form.DateTextBox" id="proposal.date" data-dojo-props="name: 'date', required: true" />
                        <input data-dojo-type="dijit.form.TimeTextBox" id="proposal.time" data-dojo-props="constraints: {visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}, name: 'time', required: true, value: 'T07:00:00'" />
                    </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.price"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalPrice", locale) %></label></td>
                    <td>$<input data-dojo-type="dijit.form.NumberSpinner" id="proposal.price" data-dojo-props="constraints: {min:0,places:2}, name: 'price', placeHolder: '10.00', style: 'width:7em;'" /></td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.total"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalTotal", locale) %></label></td>
                    <td>$<input data-dojo-type="dijit.form.NumberSpinner" id="proposal.total" data-dojo-props="constraints: {min:5.00,places:2}, name: 'total', placeHolder: '11.57', required: true, style: 'width:7em;'" /></td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.content"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalCriteria", locale) %></label></td>
                    <td>
                        <textarea
                            id="proposal.content"
                            data-dojo-type="dijit.form.Textarea"
                            data-dojo-props="name: 'content', rows: 3, style: 'width:100%;min-height:48px;font-family:\'Droid Sans\', arial, serif;font-size:12px;'"
                        ></textarea><br/>
                    </td>
                </tr>
                <tr id="proposalForm.proposal.metadata">
                    <td align="right"><label for="proposal.metadata"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalMetadata", locale) %></label></td>
                    <td>
                        <textarea
                            id="proposal.metadata"
                            data-dojo-type="dijit.form.Textarea"
                            data-dojo-props="name: 'metadata', rows: 3, style: 'width:100%;min-height:48px;font-family:\'Droid Sans\', arial, serif;font-size:12px;'"
                        ></textarea><br/>
                    </td>
                </tr>
                <tr class="existingAttribute">
                    <td align="right"><label for="proposal.score"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalScore", locale) %></label></td>
                    <td><input data-dojo-type="dijit.form.TextBox" id="proposal.score" data-dojo-props="readOnly: true, style: 'width:100%;'" /></td>
                </tr>
                <tr class="existingAttribute">
                    <td align="right"><label for="proposal.comment"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalComment", locale) %></label></td>
                    <td><textarea data-dojo-type="dijit.form.Textarea" id="proposal.comment" data-dojo-props="readOnly: true, rows: 3, style: 'width:100%;min-height:48px;font-family:\'Droid Sans\', arial, serif;font-size:12px;'"></textarea></td>
                </tr>
                <tr class="existingAttribute">
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalModificationDate", locale) %></td>
                    <td><input data-dojo-type="dijit.form.TextBox" id="proposal.modificationDate" data-dojo-props="readOnly: true, style: 'width:10em;'"" /> </td>
                </tr>
            </table>
        </fieldset>
        <div style="text-align:center;">
            <button data-dojo-type="dijit.form.Button" id="proposalFormSubmitButton" data-dojo-props="class: 'updateButton', iconClass: 'silkIcon silkIconProposalAccept', onClick: function() { return twetailer.Associate.validateMetadata('proposal.metadata') && dijit.byId('proposalForm').validate(); }, type: 'submit'" type="submit"></button>
            <button data-dojo-type="dijit.form.Button" id="proposalFormCancelButton" data-dojo-props="class: 'existingAttribute', iconClass: 'silkIcon silkIconProposalCancel', onClick: twetailer.Associate.cancelProposal"></button>
            <button data-dojo-type="dijit.form.Button" id="proposalFormCloseButton" data-dojo-props="class: 'existingAttribute closeButton', iconClass:' silkIcon silkIconProposalAccept', onClick: twetailer.Associate.closeProposal"></button>
            <button data-dojo-type="dijit.form.Button" data-dojo-props="iconClass: 'silkIcon silkIconClose', onClick: function() { dijit.byId('proposalForm').hide(); }"><%= LabelExtractor.get(ResourceFileId.third, "closeDialog_button", locale) %></button>
        </div>
    </div>

    <div
        id="aboutPopup"
        data-dojo-type="dijit.Dialog"
        data-dojo-props="title: '<%= LabelExtractor.get(ResourceFileId.third, "about_dialog_title", locale) %>', href: '/_includes/about.jsp'"
    >
    </div>

    <div
       id="demandListOverlay"
       data-dojo-type="dojox.widget.Standby"
       data-dojo-props="color: 'darkgreen', target: 'demandList'"
    ></div>

    <div
       id="proposalFormOverlay"
       data-dojo-type="dojox.widget.Standby"
       data-dojo-props="color: 'darkgreen', target: 'proposalForm'"
    ></div>

    <div
        id="locationMapDialog"
        data-dojo-type="dijit.Dialog"
        data-dojo-props="title: '<%= LabelExtractor.get(ResourceFileId.third, "shared_map_preview_dialog_title", locale) %>'"
    >
        <div style="width:600px;height:400px;"><div id='mapPlaceHolder' style='width:100%;height:100%;'></div></div>
    </div>

    <div
        data-dojo-type="dijit.Dialog"
        id="userProfile"
        title="<%= LabelExtractor.get(ResourceFileId.third, "user_profile_dialogTitle", locale) %>"
    >
        <form data-dojo-type="dijit.form.Form" id="userProfileForm">
            <div class="dijitDialogPaneContentArea" id="profileForms"></div>
            <div class="dijitDialogPaneActionBar" style="text-align: right; border-bottom: 1px solid #ccc; border-left: 1px solid #ccc; border-right: 1px solid #ccc; background-color: #f2f2f2;">
                <button data-dojo-type="dijit.form.Button" data-dojo-props="type: 'submit'">OK</button>
                <button data-dojo-type="dijit.form.Button" data-dojo-props="onClick: function() { dijit.byId('userProfile').hide(); }">Cancel</button>
            </div>
        </form>
    </div>

    <div
       id="userProfileFormOverlay"
       data-dojo-type="dojox.widget.Standby"
       data-dojo-props="color: 'darkgreen', target: 'userProfileForm'"
    ></div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dojo.data.ItemFileWriteStore');
        dojo.require('dojo.date.locale');
        dojo.require('dojo.number');
        dojo.require('dojo.parser');
        dojo.require('dijit.Dialog');
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        // dojo.require('dijit.layout.TabContainer');
        dojo.require('dijit.form.Button');
        // dojo.require('dijit.form.CheckBox');
        // dojo.require('dijit.form.ComboBox');
        dojo.require('dijit.form.DateTextBox');
        dojo.require('dijit.form.Form');
        dojo.require('dijit.form.NumberSpinner');
        // dojo.require('dijit.form.NumberTextBox');
        dojo.require('dijit.form.Select');
        dojo.require('dijit.form.Textarea');
        dojo.require('dijit.form.TextBox');
        dojo.require('dijit.form.TimeTextBox');
        // dojo.require('dijit.Menu');
        // dojo.require('dijit.MenuItem');
        dojo.require('dojox.analytics.Urchin');
        dojo.require('dojox.form.BusyButton');
        // dojo.require('dojox.form.Rating');
        dojo.require('dojox.grid.EnhancedGrid');
        // dojo.require('dojox.layout.ExpandoPane');
        // dojo.require('dojox.secure');
        // dojo.require('dojox.widget.Portlet');
        dojo.require('dojox.widget.Standby');
        dojo.require('twetailer.Associate');
        dojo.addOnLoad(function() {
            dojo.parser.parse();
            // Grid setup -- beginning -- Cf. http://trac.dojotoolkit.org/ticket/12820
            var grid = new dojox.grid.EnhancedGrid({
                    errorMessage: '&lt;span class="dojoxGridError"&gt;<%= LabelExtractor.get(ResourceFileId.third, "core_dataGrid_loadingError", locale) %>&lt;/span&gt;',
                    region: 'center',
                    rowsPerPage: 20,
                    sortFields: [{attribute: '<%= Demand.MODIFICATION_DATE %>', descending: true }],
                    structure: [
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_demandKey", locale) %>',        field:  '<%= Demand.KEY %>',               formatter: twetailer.Associate.displayDemandKey },
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_dueDate", locale) %>',          field:  '<%= Demand.DUE_DATE %>',          formatter: twetailer.Common.displayDateTime,        styles: 'text-align: right;', width: '120px' },
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_proposalKeys", locale) %>',     fields: ['<%= Demand.PROPOSAL_KEYS %>'],   formatter: twetailer.Associate.displayProposalKeys, width: '120px' },
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_quantity", locale) %>',         field:  '<%= Demand.QUANTITY %>',          styles: 'text-align: right;',                       width: '80px' },
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_hashTags", locale) %>',         fields: ['<%= Demand.HASH_TAGS %>'],       formatter: twetailer.Common.displayHashTags },
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_criteria", locale) %>',         field:  '<%= Demand.CONTENT %>',           width:  '30%' },
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_metadata", locale) %>',         field:  '<%= Demand.META_DATA %>',         formatter: twetailer.Common.displayMetadata,        width:  '30%' },
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_state", locale) %>',            field:  '<%= Demand.STATE %>' },
                        { name: '<%= LabelExtractor.get(ResourceFileId.third, "core_theader_modificationDate", locale) %>', field:  '<%= Demand.MODIFICATION_DATE %>', formatter: twetailer.Common.displayDateTime,        styles: 'text-align: right;', width: '120px' }
                    ]
                },
                dojo.byId('demandList')
            );
            grid.startup();
            // Grid setup -- end
            twetailer.Associate.init('<%= localeId %>', <%= LocaleController.getJsonOfLanguageList() %>, 'automaticUpdateState');
            twetailer.Common.registerConsumer(<%= serializedConsumer.getStream() %>);
            twetailer.Common.registerSaleAssociate(<%= serializedAssociate.getStream() %>);
            dojo.fadeOut({
                node: 'introFlash',
                delay: 50,
                onEnd: function() {
                    dojo.style('introFlash', 'display', 'none');
                    twetailer.Associate.readyToProcessParameters = true;
                }
            }).play();<%
            if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName()) && !"10.0.2.2".equals(request.getServerName())) { %>
            new dojox.analytics.Urchin({ acct: 'UA-11910037-2' });<%
            } %>
            dijit.byId('topContainer').resize();
        });
    });
    </script>

    <script src='/_ah/channel/jsapi'></script>
    <script src="https://maps-api-ssl.google.com/maps/api/js?v=3&sensor=false&language=<%= localeId %>" type="text/javascript"></script>
</body>
</html>
