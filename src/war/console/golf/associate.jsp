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
    import="twetailer.connector.BaseConnector.Source"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><head>
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
    </style>
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

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/jsp_includes/banner_protected.jsp"></jsp:include>
        <div dojoType="dijit.Menu" id="cellMenu" style="display: none;">
            <div dojoType="dijit.MenuItem" iconClass="silkIcon silkAddIcon" onClick="localModule.displayProposalForm();"><%= LabelExtractor.get(ResourceFileId.third, "ga_cmenu_viewProposal", locale) %></div>
            <div disabled="true" dojoType="dijit.MenuItem" iconClass="silkIcon silkRemoveIcon" onClick="localModule.nYI();"><%= LabelExtractor.get(ResourceFileId.third, "ga_cmenu_declineDemand", locale) %></div>
        </div>
        <table
            dojoType="dojox.grid.DataGrid"
            id="demandList"
            region="center"
            rowMenu="cellMenu"
            rowsPerPage="20"
            style="font-size:larger;margin:10px;border:1px solid lightgrey;"
        >
            <thead>
                <tr>
                       <th field="<%= Demand.KEY %>"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_key", locale) %></th>
                       <th field="<%= Demand.EXPIRATION_DATE %>" formatter="localModule.displayDate"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_expirationDate", locale) %></th>
                       <th field="<%= Demand.CRITERIA %>" width="60%"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_criteria", locale) %></th>
                       <th field="<%= Demand.QUANTITY %>"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_quantity", locale) %></th>
                       <th field="state"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_state", locale) %></th>
                       <th field="<%= Demand.CREATION_DATE %>" formatter="localModule.displayDate"><%= LabelExtractor.get(ResourceFileId.third, "ga_theader_creationDate", locale) %></th>
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
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_copyright", locale) %>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        execute="localModule.updateProposal"
        id="proposalForm"
        title="<%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_dialogTitle", locale) %>"
    >
        <input id="demand.key" name="demand.key" type="hidden" />
        <input id="proposal.key" name="proposal.key" type="hidden" />
        <fieldset class="entityInformation">
            <legend><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_demandInfo", locale) %></legend>
            <table class="demandForm">
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
                <tr>
                    <td align="right"><label for="proposal.time"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalTime", locale) %></label></td>
                    <td><input dojoType="dijit.form.TimeTextBox" id="proposal.time" required="true" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.price"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalPrice", locale) %></label></td>
                    <td><input constraints="{min:1,max:999,places:0}" dojoType="dijit.form.NumberSpinner" id="proposal.price" name="price" required="true" style="width:5em;" type="text" value="60" /></td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.total"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalTotal", locale) %></label></td>
                    <td><input constraints="{min:1,max:999,places:0}" dojoType="dijit.form.NumberSpinner" id="proposal.total" name="total" style="width:5em;" type="text" /></td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.criteria"><%= LabelExtractor.get(ResourceFileId.third, "ga_proposalForm_proposalCriteria", locale) %></label></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.criteria" name="criteria" required="true" style="width:25em;" type="text" /></td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <button dojoType="dijit.form.Button" iconClass="silkIcon silkIconAccept" onclick="return dijit.byId('advancedForm').isValid();" type="submit"><%= LabelExtractor.get(ResourceFileId.third, "update_button", locale) %></button>
                        <button dojoType="dijit.form.Button" iconClass="silkIcon silkIconCancel" onclick="dijit.byId('proposalForm').hide();" ><%= LabelExtractor.get(ResourceFileId.third, "close_button", locale) %></button>
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

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dijit.Dialog");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.TabContainer");
        dojo.require("dijit.layout.ContentPane");
        dojo.require("dojox.grid.DataGrid");
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dojo.date.locale");
        dojo.require("dojo.number");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.form.TimeTextBox");
        dojo.require("dijit.form.NumberTextBox");
        dojo.require("dijit.form.NumberSpinner");
        /*
        dojo.require("dijit.form.Form");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.form.CheckBox");
        dojo.require("dijit.form.TextBox");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.form.NumberTextBox");
        dojo.require("dijit.form.FilteringSelect");
        dojo.require("dijit.form.ComboBox");
        dojo.require("dijit.form.Textarea");
        */
        dojo.require("dojo.parser");
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            dojo.fadeOut({
                node: "introFlash",
                delay: 50,
                onEnd: function() {
                    dojo.style("introFlash", "display", "none");
                }
            }).play();
            localModule.init();
        });
    });
    </script>

    <script type="text/javascript">
    var localModule = new Object();
    localModule.init = function() {
        // Note: initialization code grabbed in the dojo test file: test_grid_tooltip_menu.html
        var grid = dijit.byId("demandList");
        dijit.byId("cellMenu").bindDomNode(grid.domNode);
        grid.onCellContextMenu = function(e) {
            cellNode = e.cellNode;
            rowIndex = e.rowIndex;
        };

        localModule.loadDemands();
    };
    localModule.nYI = function() {
        alert("Not yet implemented");
    };
    localModule.loadDemands = function() {
        var url = "/API/Demand/";
        url = "demandSet.json";
        var store = new dojo.data.ItemFileWriteStore({
            url: url,
            urlPreventCache: true,
            typeMap: localModule.typeMap
        });
        var errMsg = "Error occured while retreiving Received Requests' Detail.";
        store.fetch( {
            query : {},
            onComplete : function(items, request) {
                if (localModule.itemListWithErrors(items, "Error while getting Demands")) {
                    return;
                }
                var grid = dijit.byId("demandList");
                if (grid.selection !== null) {
                    grid.selection.clear();
                }
                grid.setStore(store);
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); }
        });
    };
    localModule.itemListWithErrors = function(items, message) {
        if (items !== null && items.length == 1 && items[0].isException != null && items[0].isException[0] === true) {
            alert(response.exceptionMessage+"\nurl: "+ioArgs.url+"\n\n"+response.originalExceptionMessage);
            _reportUpdateFailure(items[0], message);
            return true;
        }
        return false;
    };
    localModule.typeMap = {
        "Date" : {
            type: Date,
            deserialize : function(value) {
                return dojo.date.stamp.fromISOString(value);
            },
            serialize : function(value) {
                return dojo.date.stamp.toISOString(value, {});
            }
        }
    };
    localModule.displayDate = function(dateObject) {
        return dojo.date.locale.format(dateObject, {selector: "date"});
    };
    localModule.displayCriteria = function(criteria) {
        return criteria.join(" ");
    };
    localModule.displayProposalForm = function() {
        // rowIndex bind to the handler
        if (rowIndex === null) {
            return;
        }
        var grid = dijit.byId("demandList");
        var item = grid.getItem(rowIndex);
        if (item === null) {
            return;
        }
        dojo.byId("demand.key").value = item.key;
        dijit.byId("demand.criteria").attr("value", item.criteria);
        dijit.byId("demand.quantity").attr("value", item.quantity);
        dijit.byId("proposal.time").focus();
        dijit.byId("proposalForm").show();
    };
    localModule.updateProposal = function() {
        dijit.byId("proposalForm").hide();
    }
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
