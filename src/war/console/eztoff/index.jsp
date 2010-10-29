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
    import="twetailer.connector.BaseConnector.Source"
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Demand"
    import="twetailer.dto.Location"
    import="twetailer.dto.Store"
    import="twetailer.dto.SaleAssociate"
    import="twetailer.j2ee.BaseRestlet"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.CommandSettings.State"
    import="twetailer.validator.LocaleValidator"
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
    Long saleAssociateKey = consumer.getSaleAssociateKey();

    // Regular expression for e-mail address validation
    String emailRegExp = Consumer.EMAIL_REGEXP_VALIDATOR;
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <title><%= LabelExtractor.get(ResourceFileId.third, "golfConsu_localized_page_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <style type="text/css"><%
        if (useCDN) {
        %>
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/Grid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/resources/tundraGrid.css";
        @import "<%= cdnBaseURL %>/dojox/grid/enhanced/resources/tundraEnhancedGrid.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/FloatingPane.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/ExpandoPane.css";<%
        }
        else { // elif (!useCDN)
        %>
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/tundra/tundra.css";
        @import "/js/dojo/dojox/grid/resources/Grid.css";
        @import "/js/dojo/dojox/grid/resources/tundraGrid.css";
        @import "/js/dojo/dojox/grid/enhanced/resources/tundraEnhancedGrid.css";
        @import "/js/dojo/dojox/layout/resources/FloatingPane.css";
        @import "/js/dojo/dojox/layout/resources/ExpandoPane.css";<%
        } // endif (useCDN)
        %>
        @import "/css/console.css";
        @import "/css/eztoff/console.css";
    </style>
</head>
<body class="tundra">

    <div id="topBar"></div>

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>

    <%
    if (useCDN) {
    %><script type="text/javascript">
    var djConfig = {
        parseOnLoad: false,
        isDebug: false,
        useXDomain: true,
        baseUrl: './',
        modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' },
        dojoBlankHtmlUrl: '/blank.html',
        locale: '<%= localeId %>'
    };
    </script>
    <script src="<%= cdnBaseURL %>/dojo/dojo.xd.js" type="text/javascript"></script><%
    }
    else { // elif (!useCDN)
    %><script type="text/javascript">
    var djConfig = {
        parseOnLoad: false,
        isDebug: false,
        useXDomain: true,
        baseUrl: '/js/dojo/dojo/',
        modulePaths: { twetailer: '/js/twetailer', domderrien: '/js/domderrien' },
        dojoBlankHtmlUrl: '/blank.html',
        locale: '<%= localeId %>'
    };
    </script>
    <script src="/js/dojo/dojo/dojo.js" type="text/javascript"></script><%
    } // endif (useCDN)
    %>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/_includes/banner_protected.jsp">
            <jsp:param name="verticalId" value="eztoff" />
            <jsp:param name="localeId" value="<%= localeId %>" />
            <jsp:param name="pageForAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="isLoggedUserAssociate" value="<%= Boolean.toString(saleAssociateKey != null) %>" />
            <jsp:param name="consumerName" value="<%= consumer.getName() %>" />
        </jsp:include>
        <div dojoType="dijit.layout.BorderContainer" gutters="false" id="centerZone" region="center">
            <div dojoType="dijit.layout.ContentPane" region="top" style="margin:10px 10px 0 10px;">
                <div style="float:left;">
                    <button
                        dojoType="dijit.form.Button"
                        iconClass="silkIcon silkIconDemandAdd"
                        id="createButton"
                        onclick="twetailer.golf.Consumer.displayDemandForm(true);"
                    ><%= LabelExtractor.get(ResourceFileId.third, "golf_cmenu_createDemand", locale) %></button>
                </div>
                <div style="float:right;">
                    <select dojoType="dijit.form.Select" onchange="dijit.byId('demandList').filter({<%= Demand.STATE %>:this.value});" style="">
                        <option value="*" selected="true"><%= LabelExtractor.get(ResourceFileId.third, "core_stateSelector_anyState", locale) %></option>
                        <option value="<%= State.opened %>"><%= LabelExtractor.get(ResourceFileId.master, "cl_state_opened", locale) %></option>
                        <option value="<%= State.invalid %>"><%= LabelExtractor.get(ResourceFileId.master, "cl_state_invalid", locale) %></option>
                        <option value="<%= State.published %>"><%= LabelExtractor.get(ResourceFileId.master, "cl_state_published", locale) %></option>
                        <option value="<%= State.confirmed %>"><%= LabelExtractor.get(ResourceFileId.master, "cl_state_confirmed", locale) %></option>
                    </select>
                    <button
                        busyLabel="<%= LabelExtractor.get(ResourceFileId.third, "refreshing_button_state", locale) %>"
                        dojoType="dojox.form.BusyButton"
                        iconClass="silkIcon silkIconRefresh"
                        id="refreshButton"
                        onclick="twetailer.golf.Consumer.loadNewDemands();"
                    ><%= LabelExtractor.get(ResourceFileId.third, "refresh_button", locale) %></button>
                </div>
            </div>
            <div dojoType="dijit.Menu" id="demandListCellMenu" style="display: none;">
                <div dojoType="dijit.MenuItem" iconClass="silkIcon silkIconDemandUpdate" onClick="twetailer.golf.Consumer.displayDemandForm(false);"><%= LabelExtractor.get(ResourceFileId.third, "golf_cmenu_updateDemand", new String[] { "" }, locale) %></div>
                <div disabled="true" dojoType="dijit.MenuItem" iconClass="silkIcon silkIconProposalView" onClick="twetailer.golf.Consumer.displayProposalForm();"><%= LabelExtractor.get(ResourceFileId.third, "golf_cmenu_viewProposal", new String[] { "" }, locale) %></div>
                <div disabled="true" dojoType="dijit.MenuItem" iconClass="silkIcon silkIconDemandCancel"  onClick="twetailer.golf.Consumer.cancelDemand();"><%= LabelExtractor.get(ResourceFileId.third, "golf_cmenu_cancelDemand", new String[] { "" }, locale) %></div>
            </div>
            <table
                dojoType="dojox.grid.EnhancedGrid"
                errorMessage="&lt;span class='dojoxGridError'&gt;<%= LabelExtractor.get(ResourceFileId.third, "core_dataGrid_loadingError", locale) %>&lt;/span&gt;"
                id="demandList"
                query="{<%= Demand.STATE %>:'*'}"
                region="center"
                rowMenu="cellMenu"
                rowsPerPage="20"
                sortFields="[{attribute:'<%= Demand.MODIFICATION_DATE %>',descending:true}]"
                style="font-size:larger;margin:0 10px 10px 10px;border:1px solid lightgrey;"
            >
                <thead>
                    <tr>
                           <th field="<%= Demand.KEY %>" formatter="twetailer.golf.Consumer.displayDemandKey" styles="padding:2px 5px;"><%= LabelExtractor.get(ResourceFileId.third, "core_theader_demandKey", locale) %></th>
                           <th field="<%= Demand.LOCATION_KEY %>" formatter="twetailer.Common.displayLocale" styles="padding:2px 5px;"><%= LabelExtractor.get(ResourceFileId.third, "core_theader_locale", locale) %></th>
                           <th field="<%= Demand.DUE_DATE %>" formatter="twetailer.Common.displayDateTime" styles="padding:2px 5px;text-align:right;" width="140px"><%= LabelExtractor.get(ResourceFileId.third, "golf_theader_dueDate", locale) %></th>
                           <%--th field="<%= Demand.EXPIRATION_DATE %>" formatter="twetailer.Common.displayDateTime" styles="padding:2px 5px;text-align:right;" width="140px"><%= LabelExtractor.get(ResourceFileId.third, "core_theader_expirationDate", locale) %></th--%>
                           <th fields="<%= Demand.PROPOSAL_KEYS %>" formatter="twetailer.golf.Consumer.displayProposalKeys" styles="padding:2px 5px;" width="200px"><%= LabelExtractor.get(ResourceFileId.third, "core_theader_proposalKeys", locale) %></th>
                           <th field="<%= Demand.QUANTITY %>" styles="padding:2px 5px;text-align:right;"><%= LabelExtractor.get(ResourceFileId.third, "golf_theader_quantity", locale) %></th>
                           <th field="<%= Demand.META_DATA %>" formatter="twetailer.golf.Common.displayPullCartNb" styles="padding:2px 5px;text-align:right;"><%= LabelExtractor.get(ResourceFileId.third, "golf_theader_metadata_pullCart", locale) %></th>
                           <th field="<%= Demand.META_DATA %>" formatter="twetailer.golf.Common.displayGolfCartNb" styles="padding:2px 5px;text-align:right;"><%= LabelExtractor.get(ResourceFileId.third, "golf_theader_metadata_golfCart", locale) %></th>
                           <th
                               fields="<%= Demand.CRITERIA %>"
                               formatter="twetailer.Common.displayCriteria"
                               styles="padding:2px 5px;"
                               width="30%"
                           ><%= LabelExtractor.get(ResourceFileId.third, "golf_theader_criteria", locale) %></th>
                           <th fields="<%= Demand.CC %>" formatter="twetailer.Common.displayCCList" styles="padding:2px 5px;" width="30%"><%= LabelExtractor.get(ResourceFileId.third, "core_theader_ccList", locale) %></th>
                           <th field="<%= Demand.STATE %>" styles="padding:2px 5px;"><%= LabelExtractor.get(ResourceFileId.third, "core_theader_state", locale) %></th>
                           <th field="<%= Demand.MODIFICATION_DATE %>" formatter="twetailer.Common.displayDateTime" styles="padding:2px 5px;text-align:right;" width="140px"><%= LabelExtractor.get(ResourceFileId.third, "core_theader_modificationDate", locale) %></th>
                           <%--th field="<%= Demand.CREATION_DATE %>" formatter="twetailer.Common.displayDateTime" styles="padding:2px 5px;text-align:right;" width="140px"><%= LabelExtractor.get(ResourceFileId.third, "core_theader_creationDate", locale) %></th--%>
                    </tr>
                </thead>
            </table>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get(ResourceFileId.master, "product_rich_copyright", locale) %>
        </div>
    </div>


    <div
        dojoType="dijit.Dialog"
        execute="twetailer.golf.Consumer.updateDemand"
        id="demandForm"
        title="<%= LabelExtractor.get(ResourceFileId.third, "golf_demandForm_formTitle_creation", locale) %>"
    >
        <fieldset class="entityInformation">
            <legend><%= LabelExtractor.get(ResourceFileId.third, "golf_demandInfo", locale) %></legend>
            <table class="demandForm">
                <tr class="existingAttribute">
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandKey", locale) %></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="demand.key" name="key" readonly="true" style="width:6em;" type="text" /> </td>
                </tr>
                <tr class="existingAttribute">
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandState", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="demand.state" readonly="true" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="demand.quantity"><%= LabelExtractor.get(ResourceFileId.third, "golf_demandForm_demandQuantity", locale) %></label></td>
                    <td><input constraints="{min:1,places:0}" dojoType="dijit.form.NumberSpinner" id="demand.quantity" name="quantity" style="width:5em;" type="text" value="4" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="demand.date"><%= LabelExtractor.get(ResourceFileId.third, "golf_demandForm_demandDueDate", locale) %></label></td>
                    <td>
                        <input constraints="{datePattern:'EEE, MMMM dd yyyy'}" dojoType="dijit.form.DateTextBox" id="demand.date" name="date" required="true" type="text" />
                        <input constraints="{visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}" dojoType="dijit.form.TimeTextBox" id="demand.time" name="time" required="true" type="text" value="T07:00:00" />
                    </td>
                </tr>
                <tr>
                    <td align="right"><label for="demand.postalCode"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandRegion", locale) %></label></td>
                    <td>
                        <table>
                            <tr>
                                <td align="right"><label for="demand.postalCode"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandPostalCode", locale) %></label></td>
                                <td>
                                    <input
                                        dojoType="dijit.form.ValidationTextBox"
                                        id="demand.postalCode"
                                        invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>"
                                        name="postalCode"
                                        placeholder="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_placeHolder_CA", locale) %>"
                                        regExp="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_regExp_CA", locale) %>"
                                        required="true"
                                        style="width:7em;"
                                        type="text"
                                    />
                                    <button
                                        dojoType="dijit.form.Button"
                                        iconClass="silkIcon silkIconGPS"
                                        id="detectLocationButton"
                                        onclick="twetailer.Common.fetchBrowserLocation('demand.postalCode', 'demand.countryCode', 'demandFormOverlay');"
                                        showLabel="false"
                                        title="<%= LabelExtractor.get(ResourceFileId.third, "core_cmenu_detectLocale", locale) %>"
                                    ></button>
                                </td>
                            </tr>
                            <tr>
                                <td align="right"><label for="demand.countryCode"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandCountryCode", locale) %></label></td>
                                <td>
                                    <select
                                        dojoType="dijit.form.Select"
                                        id="demand.countryCode"
                                        name="countryCode"
                                        onchange="twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'demand.postalCode');"
                                        style=""
                                    >
                                            <option value="CA" selected="true"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                                            <option value="US"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td>
                                    <button
                                        dojoType="dijit.form.Button"
                                        iconClass="silkIcon silkIconGMaps"
                                        onclick="twetailer.Common.showMap(dijit.byId('demand.postalCode').get('value'), dijit.byId('demand.countryCode').get('value'));"
                                    ><%= LabelExtractor.get(ResourceFileId.third, "shared_locale_view_map_link", locale) %></button>
                                </td>
                            </tr>
                            <tr>
                                <td align="right"><label for="demand.range"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandRange", locale) %></label></td>
                                <td>
                                    <input constraints="{min:5,max:100,places:2}" dojoType="dijit.form.NumberSpinner" id="demand.range" name="range" required="true" style="width:7em;" type="text" value="25" />
                                    <select dojoType="dijit.form.Select" id="demand.rangeUnit" name="rangeUnit" required="true" style="width:5em;">
                                        <option value="<%= LocaleValidator.KILOMETER_UNIT %>" selected="true"><%= LocaleValidator.KILOMETER_UNIT %></option>
                                        <option value="<%= LocaleValidator.MILE_UNIT %>"><%= LocaleValidator.MILE_UNIT %></option>
                                    </select>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td align="right"><label for="demand.metadata.pullCart"><%= LabelExtractor.get(ResourceFileId.third, "golf_demandForm_demandPullCart", locale) %></label></td>
                    <td><input constraints="{min:0,places:0}" dojoType="dijit.form.NumberSpinner" id="demand.metadata.pullCart" name="pullCart" style="width:3em;" type="text" value="0" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="demand.metadata.golfCart"><%= LabelExtractor.get(ResourceFileId.third, "golf_demandForm_demandGolfCart", locale) %></label></td>
                    <td><input constraints="{min:0,places:0}" dojoType="dijit.form.NumberSpinner" id="demand.metadata.golfCart" name="golfCart" style="width:3em;" type="text" value="0" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="demand.criteria"><%= LabelExtractor.get(ResourceFileId.third, "golf_demandForm_demandCriteria", locale) %></label></td>
                    <td><input dojoType="dijit.form.TextBox" id="demand.criteria" name="criteria" style="width:25em;" type="text" /></td>
                </tr>
                <tr>
                    <td align="right" style="padding-top:7px;"><label for="demand.cc"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandCC", locale) %></label></td>
                    <td>
                        <table style="width:100%;">
                            <tbody id="friendList">
                                <tr>
                                    <td>
                                        <input
                                            dojoType="dijit.form.ValidationTextBox"
                                            id="email1"
                                            invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_ccInvalidMessage", locale) %>"
                                            name="email1"
                                            placeHolder="<%= LabelExtractor.get(ResourceFileId.third, "shared_email_sample", locale) %>"
                                            regExp="<%= emailRegExp %>"
                                            required="false"
                                            style="width:100%;"
                                            trim="true"
                                            type="text"
                                      />
                                    </td>
                                    <td style="width:20px;padding-right:0px !important;">
                                        <button
                                            dojoType="dijit.form.Button"
                                            iconClass="silkIcon silkIconAdd"
                                            id="friendButton1"
                                            onclick="twetailer.Common.manageFriendRow(1);"
                                            showLabel="false"
                                            title="<%= LabelExtractor.get(ResourceFileId.third, "add_ccInfo_button", locale) %>"
                                        ></button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr class="existingAttribute">
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandModificationDate", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="demand.modificationDate" readonly="true" style="width:10em;" type="text" /> </td>
                </tr>
            </table>
        </fieldset>
        <div style="text-align:center;">
            <button class="updateButton" dojoType="dijit.form.Button" iconClass="silkIcon silkIconDemandAccept" id="demandFormSubmitButton" onclick="return dijit.byId('demandForm').validate();" type="submit"></button>
            <button class="existingAttribute" dojoType="dijit.form.Button" iconClass="silkIcon silkIconDemandCancel" id="demandFormCancelButton" onclick="twetailer.golf.Consumer.cancelDemand('demandForm', 'demand.key');"></button>
            <button class="existingAttribute closeButton" dojoType="dijit.form.Button" iconClass="silkIcon silkIconProposalAccept" id="demandFormCloseButton" onclick="twetailer.golf.Consumer.closeDemand('demandForm', 'demand.key');"></button>
            <button dojoType="dijit.form.Button" iconClass="silkIcon silkIconClose" onclick="dijit.byId('demandForm').hide();"><%= LabelExtractor.get(ResourceFileId.third, "closeDialog_button", locale) %></button>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="proposalForm"
        title="<%= LabelExtractor.get(ResourceFileId.third, "golf_proposalForm_formTitle_view", locale) %>"
    >
        <input dojoType="dijit.form.TextBox" type="hidden" id="proposal.demandKey" />
        <fieldset class="entityInformation">
            <legend><%= LabelExtractor.get(ResourceFileId.third, "golf_proposalInfo", locale) %></legend>
            <table class="demandForm" width="100%">
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalKey", locale) %></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="proposal.key" readonly="true" style="width:6em;" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalState", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.state" readonly="true" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.time"><%= LabelExtractor.get(ResourceFileId.third, "golf_proposalForm_proposalDueDate", locale) %></label></td>
                    <td>
                        <input dojoType="dijit.form.DateTextBox" id="proposal.date" readonly="true" type="text" />
                        <input dojoType="dijit.form.TimeTextBox" id="proposal.time" readonly="true" type="text" />
                    </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.quantity"><%= LabelExtractor.get(ResourceFileId.third, "golf_proposalForm_proposalQuantity", locale) %></label></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.quantity" readonly="true" style="width:5em;" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.metadata.pullCart"><%= LabelExtractor.get(ResourceFileId.third, "golf_proposalForm_proposalPullCart", locale) %></label></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.metadata.pullCart" readonly="true" style="width:3em;" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.metadata.golfCart"><%= LabelExtractor.get(ResourceFileId.third, "golf_proposalForm_proposalGolfCart", locale) %></label></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.metadata.golfCart" readonly="true" style="width:3em;" type="text"" /> </td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.price"><%= LabelExtractor.get(ResourceFileId.third, "golf_proposalForm_proposalPrice", locale) %></label></td>
                    <td>$<input dojoType="dijit.form.TextBox" id="proposal.price" readonly="true" style="width:7em;" type="text" /></td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.total"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalTotal", locale) %></label></td>
                    <td>$<input dojoType="dijit.form.TextBox" id="proposal.total" readonly="true" style="width:7em;" type="text" /></td>
                </tr>
                <tr>
                    <td align="right"><label for="proposal.criteria"><%= LabelExtractor.get(ResourceFileId.third, "golf_proposalForm_proposalCriteria", locale) %></label></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.criteria" readonly="true" style="width:25em;" type="text" /></td>
                </tr>
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_proposalForm_proposalModificationDate", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="proposal.modificationDate" readonly="true" style="width:10em;" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"></td>
                    <td><a href="#" id="proposal.viewStoreInfo" onclick="dijit.byId('storeForm').show();return false;"><%= LabelExtractor.get(ResourceFileId.third, "golf_storeInfo", locale) %></a></td>
                </tr>
            </table>
        </fieldset>
        <div style="text-align:center;">
            <button class="updateButton" dojoType="dijit.form.Button" iconClass="silkIcon silkIconProposalAccept" id="proposalFormConfirmButton" onclick="twetailer.golf.Consumer.confirmProposal();"></button>
            <button class="updateButton" dojoType="dijit.form.Button" iconClass="silkIcon silkIconProposalCancel" id="proposalFormDeclineButton" onclick="twetailer.golf.Consumer.declineProposal();"></button>
            <button class="closeButton" dojoType="dijit.form.Button" iconClass="silkIcon silkIconDemandAccept" id="proposalFormCloseButton" onclick="twetailer.golf.Consumer.closeDemand('proposalForm', 'proposal.demandKey');"></button>
            <button dojoType="dijit.form.Button" iconClass="silkIcon silkIconClose" onclick="dijit.byId('proposalForm').hide();" ><%= LabelExtractor.get(ResourceFileId.third, "closeDialog_button", locale) %></button>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="storeForm"
        title="<%= LabelExtractor.get(ResourceFileId.third, "golf_proposalForm_formTitle_view", locale) %>"
    >
        <fieldset class="entityInformation">
            <legend><%= LabelExtractor.get(ResourceFileId.third, "golf_storeInfo", locale) %></legend>
            <table class="demandForm" width="100%">
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_storeForm_storeName", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="store.name" readonly="true" style="width:10em;" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_storeForm_storeAddress", locale) %></td>
                    <td>
                        <textarea dojoType="dijit.form.Textarea" id="store.address" name="criteria" readonly="true" style="width:20em;"></textarea><br/>
                        <button
                            disabled="true"
                            dojoType="dijit.form.Button"
                            iconClass="silkIcon silkIconGMaps"
                            onclick="twetailer.Common.showMap(dijit.byId('store.postalCode').get('value'), dijit.byId('store.countryCode').get('value'));"
                        ><%= LabelExtractor.get(ResourceFileId.third, "shared_locale_view_map_link", locale) %></button>
                    </td>
                </tr>
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_storeForm_storePhoneNumber", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="store.phoneNb" readonly="true" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_storeForm_storeEmail", locale) %></td>
                    <td><input dojoType="dijit.form.TextBox" id="store.email" readonly="true" style="width:10em;" type="text" /> </td>
                </tr>
                <tr>
                    <td align="right"><%= LabelExtractor.get(ResourceFileId.third, "core_storeForm_storeUrl", locale) %></td>
                    <td>
                        <input dojoType="dijit.form.TextBox" id="store.url" readonly="true" style="width:16em;" type="text" /><br/>
                        <button
                            disabled="true"
                            dojoType="dijit.form.Button"
                            iconClass="silkIcon silkIconLinkOpen"
                            onclick=""
                        ><%= LabelExtractor.get(ResourceFileId.third, "openLink_button", locale) %></button>
                    </td>
                </tr>
            </table>
        </fieldset>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="aboutPopup"
        title="<%= LabelExtractor.get(ResourceFileId.third, "about_dialog_title", locale) %>"
        href="/_includes/about.jsp"
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
       id="demandFormOverlay"
       target="demandForm"
    ></div>

    <div
       color="darkgreen"
       dojoType="dojox.widget.Standby"
       id="proposalFormOverlay"
       target="proposalForm"
    ></div>

    <div
        dojoType="dijit.Dialog"
        id="locationMapDialog"
        title="<%= LabelExtractor.get(ResourceFileId.third, "shared_map_preview_dialog_title", locale) %>"
    >
        <div style="width:600px;height:400px;"><div id='mapPlaceHolder' style='width:100%;height:100%;'></div></div>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dojo.data.ItemFileWriteStore');
        dojo.require('dojo.date.locale');
        // dojo.require('dojo.number');
        dojo.require('dojo.parser');
        dojo.require('dijit.Dialog');
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        dojo.require('dijit.layout.TabContainer');
        // dojo.require('dijit.form.CheckBox');
        // dojo.require('dijit.form.ComboBox');
        dojo.require('dijit.form.DateTextBox');
        // dojo.require('dijit.form.FilteringSelect');
        dojo.require('dijit.form.NumberSpinner');
        // dojo.require('dijit.form.NumberTextBox');
        dojo.require('dijit.form.Select');
        dojo.require('dijit.form.Textarea');
        dojo.require('dijit.form.TextBox');
        dojo.require('dijit.form.TimeTextBox');
        dojo.require('dijit.form.ValidationTextBox');
        dojo.require('dojox.analytics.Urchin');
        dojo.require('dojox.form.BusyButton');
        // dojo.require('dojox.form.Rating');
        dojo.require('dojox.grid.EnhancedGrid');
        // dojo.require('dojox.grid.enhanced.plugins.NestedSorting');
        // dojo.require('dojox.layout.ExpandoPane');
        // dojo.require('dojox.secure');
        // dojo.require('dojox.widget.Portlet');
        dojo.require('dojox.widget.Standby');
        dojo.require('twetailer.Consumer');
        dojo.require('twetailer.golf.Consumer');
        dojo.addOnLoad(function() {
            dojo.parser.parse();
            twetailer.golf.Consumer.init('<%= localeId %>');
            dojo.fadeOut({
                node: 'introFlash',
                delay: 50,
                onEnd: function() {
                    dojo.style('introFlash', 'display', 'none');
                }
            }).play();<%
            if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName())) { %>
            new dojox.analytics.Urchin({ acct: 'UA-11910037-2' });<%
            } %>
        });
    });
    </script>

    <script src="http://maps.google.com/maps/api/js?sensor=false&language=<%= localeId %>" type="text/javascript"></script>
</body>
</html>
