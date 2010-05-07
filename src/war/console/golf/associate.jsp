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
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Demand"
    import="twetailer.dto.Location"
    import="twetailer.dto.Seed"
    import="twetailer.dto.Store"
    import="twetailer.dto.SaleAssociate"
    import="twetailer.j2ee.BaseRestlet"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.connector.BaseConnector.Source"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Get the logged user record
    OpenIdUser loggedUser = BaseRestlet.getLoggedUser(request);
    Consumer consumer = LoginServlet.getConsumer(loggedUser);
    Long saleAssociateKey = LoginServlet.getSaleAssociateKey(loggedUser);

    // Redirects non sale associates
    if (saleAssociateKey == null) {
        response.sendRedirect("./");
    }
%><html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="<%= localeId %>">
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "ga_localized_page_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
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
    <style type="text/css">
        .entityInformation td {
            height: 24px;
            vertical-align: middle;
        }
        .dijitTextBox .rightAlign {
            text-align: right;
        }
        #demandList .dojoxGridScrollbox {
            background: url("/images/golf/grass-border.png") repeat-x scroll center bottom #FFFFFF;
        }
        #proposalForm .dijitDialogPaneContent {
            background: url("/images/golf/18-hole-flag.png") no-repeat scroll right bottom #FFFFFF;
        }
    </style>
</head>
<body class="tundra">

    <div id="topBar"></div>

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

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/jsp_includes/banner_protected.jsp">
            <jsp:param name="pageForAssociate" value="<%= Boolean.TRUE.toString() %>" />
            <jsp:param name="isLoggedUserAssociate" value="<%= Boolean.toString(saleAssociateKey != null) %>" />
            <jsp:param name="consumerName" value="<%= consumer.getName() %>" />
        </jsp:include>
        <div dojoType="dijit.layout.BorderContainer" gutters="false" region="center">
            <div dojoType="dijit.layout.ContentPane" region="top" style="text-align:right;margin:10px 10px 0 10px;">
                <button
                    busyLabel="<%= LabelExtractor.get(ResourceFileId.third, "refreshing_button_state", locale) %>"
                    dojoType="dojox.form.BusyButton"
                    id="refreshButton"
                    onclick="twetailer.GolfAssociate.loadNewDemands();"
                ><%= LabelExtractor.get(ResourceFileId.third, "refresh_button", locale) %></button>
            </div>
            <div dojoType="dijit.Menu" id="cellMenu" style="display: none;">
                <div dojoType="dijit.MenuItem" iconClass="silkIcon silkAddIcon" onClick="twetailer.GolfAssociate.displayProposalForm();"><%= LabelExtractor.get(ResourceFileId.third, "ga_cmenu_createProposal", locale) %></div>
                <div disabled="true" dojoType="dijit.MenuItem" iconClass="silkIcon silkRemoveIcon"><%= LabelExtractor.get(ResourceFileId.third, "ga_cmenu_declineDemand", locale) %></div>
            </div>
            <table
                dojoType="dojox.grid.DataGrid"
                errorMessage="&lt;span class='dojoxGridError'&gt;<%= LabelExtractor.get(ResourceFileId.third, "ga_dataGrid_loadingError", locale) %>&lt;/span&gt;"
                id="demandList"
                errorMessage="<%= LabelExtractor.get(ResourceFileId.third, "ga_dataGrid_loading", locale) %>"
                region="center"
                rowMenu="cellMenu"
                rowsPerPage="20"
                style="font-size:larger;margin:10px;border:1px solid lightgrey;"
            >
                <thead>
                    <tr>
                           <th field="<%= Demand.KEY %>"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_demandKey", locale) %></th>
                           <th field="<%= Demand.DUE_DATE %>" formatter="twetailer.GolfCommon.displayDate"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_dueDate", locale) %></th>
                           <th fields="<%= Demand.CRITERIA %>" formatter="twetailer.GolfCommon.displayCriteria" width="60%"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_criteria", locale) %></th>
                           <th field="<%= Demand.QUANTITY %>" styles="text-align:right;"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_quantity", locale) %></th>
                           <th field="state"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_state", locale) %></th>
                           <th fields="<%= Demand.PROPOSAL_KEYS %>" formatter="twetailer.GolfCommon.displayProposalKeys"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_proposalKeys", locale) %></th>
                           <th field="<%= Demand.MODIFICATION_DATE %>" formatter="twetailer.GolfCommon.displayDateTime" styles="text-align:right;" width="180px"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_modificationDate", locale) %></th>
                           <th field="<%= Demand.CREATION_DATE %>" formatter="twetailer.GolfCommon.displayDate" hidden="true"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_creationDate", locale) %></th>
                           <th field="<%= Demand.EXPIRATION_DATE %>" formatter="twetailer.GolfCommon.displayDate"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_expirationDate", locale) %></th>
                           <!--th
                               cellType="dojox.grid.cells.Select"
                               editable="true"
                               field="status"
                               formatter="cpwr.ConsoleLogic.displayParameterStatus"
                               options="Not Set,Excluded,Always Included,Cache Bypasser"
                               values="NotSet,Excluded,Included,CacheBypasser"
                               width="20%"
                           >Status</th-->
                    </tr>
                </thead>
            </table>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_copyright", locale) %>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        execute="twetailer.GolfAssociate.updateProposal"
        id="proposalForm"
    >
        <fieldset class="entityInformation">
            <legend><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_demandInfo", locale) %></legend>
            <table class="demandForm">
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_demandKey", locale) %></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="demand.key" name="demandKey" readonly="true" style="width:6em;" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="demand.quantity"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_demandQuantity", locale) %></label></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="demand.quantity" readonly="true" style="width:3em;" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="demand.criteria"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_demandCriteria", locale) %></label></td>
                    <td><input dojoType="dijit.form.TextBox" id="demand.criteria" readonly="true" style="width:25em;" type="text" /></td>
                </tr>
            </table>
        </fieldset>
        <fieldset class="entityInformation">
            <legend><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalInfo", locale) %></legend>
            <table class="demandForm">
                <tr class="existingProposalAttribute">
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalKey", locale) %></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="proposal.key" name="key" readonly="true" style="width:6em;" type="text" /> </td>
                </tr>
                <tr class="existingProposalAttribute">
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalState", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.state" readonly="true" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.time"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalTime", locale) %></label></td>
                    <td><input constraints="{visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}" dojoType="dijit.form.TimeTextBox" id="proposal.time" name="time" required="true" type="text" value="T12:00:00" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.price"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalPrice", locale) %></label></td>
                    <td><input constraints="{min:0,max:999,places:2}" dojoType="dijit.form.NumberSpinner" id="proposal.price" name="price" required="true" style="width:7em;" type="text" /></td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.total"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalTotal", locale) %></label></td>
                    <td><input class="rightAlign" constraints="{min:0,max:999,places:2}" dojoType="dijit.form.NumberSpinner" id="proposal.total" name="total" style="width:7em;" type="text" /></td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.criteria"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalCriteria", locale) %></label></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.criteria" name="criteria" required="true" style="width:25em;" type="text" /></td>
                </tr>
                <tr class="existingProposalAttribute">
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalModificationDate", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.modificationDate" readonly="true" style="width:10em;" type="text" /> </td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <button dojoType="dijit.form.Button" iconClass="silkIcon silkIconAccept" id="proposalFormSubmitButton" onclick="return dijit.byId('proposalForm').isValid();" type="submit"><%= LabelExtractor.get(ResourceFileId.third, "update_button", locale) %></button>
                        <button dojoType="dijit.form.Button" iconClass="silkIcon silkIconCancel" id="proposalFormCancelButton" onclick="twetailer.GolfAssociate.cancelProposal();"><%= LabelExtractor.get(ResourceFileId.third, "cancel_button", locale) %></button>
                        <button dojoType="dijit.form.Button" iconClass="silkIcon silkIconClose" onclick="dijit.byId('proposalForm').hide();" ><%= LabelExtractor.get(ResourceFileId.third, "close_button", locale) %></button>
                    </td>
                </tr>
            </table>
        </fieldset>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="aboutPopup"
        title="<%= LabelExtractor.get(ResourceFileId.third, "about_dialog_title", locale) %>"
        href="/jsp_includes/about.jsp"
    >
    </div>

    <div
       color="darkgreen"
       dojoType="dojox.widget.Standby"
       id="demandListOverlay"
       target="demandList"
    ></div>

    <div
       color="darkgreen"
       dojoType="dojox.widget.Standby"
       id="proposalFormOverlay"
       target="proposalForm"
    ></div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dojo.date.locale");
        dojo.require("dojo.number");
        dojo.require("dojo.parser");
        dojo.require("dijit.Dialog");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.ContentPane");
        dojo.require("dijit.layout.TabContainer");
        // dojo.require("dijit.form.CheckBox");
        // dojo.require("dijit.form.ComboBox");
        // dojo.require("dijit.form.DateTextBox");
        // dojo.require("dijit.form.FilteringSelect");
        dojo.require("dijit.form.NumberSpinner");
        dojo.require("dijit.form.NumberTextBox");
        // dojo.require("dijit.form.Textarea");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.form.TimeTextBox");
        dojo.require("dojox.form.BusyButton");
        dojo.require("dojox.form.Rating");
        // dojo.require("dojox.grid.EnhancedDataGrid");
        dojo.require("dojox.grid.DataGrid");
        // dojo.require("dojox.layout.ExpandoPane");
        // dojo.require("dojox.secure");
        // dojo.require("dojox.widget.Portlet");
        dojo.require("dojox.widget.Standby");
        dojo.require("twetailer.GolfAssociate");
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            twetailer.GolfAssociate.init("<%= localeId %>");
            dojo.fadeOut({
                node: "introFlash",
                delay: 50,
                onEnd: function() {
                    dojo.style("introFlash", "display", "none");
                }
            }).play();
        });
        // http://archive.dojotoolkit.org/nightly/dojotoolkit/dojox/image/tests/test_SlideShow.html
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
