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

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <title>Monitoring Console</title>
    <meta http-equiv="content-type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>" />
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
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
        .entityInformation>form>table>tbody>tr>td:nth-child(1), #locationFilterTable>tbody>tr>td:nth-child(1) {
            text-align: right;
        }
        .entityInformation>form>table>tbody>tr>td:nth-child(2), #locationFilterTable>tbody>tr>td:nth-child(2) {
            padding-left: 10px;
        }
    </style>
</head>
<body class="tundra">

    <div id="introFlash">
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
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
        <jsp:include page="/_includes/banner_protected.jsp">
            <jsp:param name="pageForAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="isLoggedUserAssociate" value="<%= Boolean.FALSE.toString() %>" />
            <jsp:param name="consumerName" value="Administrator" />
        </jsp:include>
        <div
            dojoType="dijit.layout.ContentPane"
            id="centerZone"
            region="center"
            style="margin-top: 5px; margin-bottom: 10px;"
        >
            <fieldset class="entityInformation" style="float:right;margin-left:10px;text-align:center;">
                <legend>Resources</legend>
                GAE console:
                    <a href="https://appengine.google.com/dashboard?&app_id=anothersocialeconomy" title="AnotherSocialEconomy">A</a> /
                    <a href="https://appengine.google.com/dashboard?&app_id=twetailer" title="Twetailer">T</a> /
                    <a href="http://localhost:9999/_ah/admin" title="Local development environment">D</a>
                <br />
                Registration:
                    <a href="https://anothersocialeconomy.appspot.com/_admin/registration.jsp" title="AnotherSocialEconomy">A</a> /
                    <a href="https://twetailer.appspot.com/_admin/registration.jsp" title="Twetailer">T</a> /
                    <a href="http://localhost:9999/_admin/registration.jsp" title="Local development environment">D</a>
                <br />
                Monitoring:
                    <a href="https://anothersocialeconomy.appspot.com/_admin/monitoring.jsp" title="AnotherSocialEconomy">A</a> /
                    <a href="https://twetailer.appspot.com/_admin/monitoring.jsp" title="Twetailer">T</a> /
                    <a href="http://localhost:9999/_admin/monitoring.jsp" title="Local development environment">D</a>
                <br />
                Associate:
                    <a href="https://anothersocialeconomy.appspot.com/console/associate.jsp" title="AnotherSocialEconomy">A</a> /
                    <a href="https://twetailer.appspot.com/console/associate.jsp" title="Twetailer">T</a> /
                    <a href="http://localhost:9999/console/associate.jsp" title="Local development environment">D</a>
                <br />
                <div style="color:grey;font-size:smaller;">ASE / Twetailer / Dev</div>
            </fieldset>
            <div style="background-color:red;color:white;font-weight:bold;padding:5px;float:left;border-radius:4px;">Warning: To avoid conflicts regarding the ownership of the commands to process, you MUST logout from all non <i>admin</i> console!</div>
            <br clear="left"/>
            <fieldset class="entityInformation" id="queryFieldset" style="">
                <legend>
                    Search
                </legend>

                Consumer by e-mail:
                <input
                    dojoType="dijit.form.ValidationTextBox"
                    id="queryConsumer"
                    invalidMessage="Must be at least 3 characters long, used to select the Consumers with an e-mail starting with the field value"
                    onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.searchEntityKey('queryConsumer', 'email', 'Consumer', 'consumer.key'); }"
                    placeHolder="e-mail address (starts with)"
                    regExp="\S\S[^\s|\*].*"
                    required="true"
                    type="text"
                />
                <button dojoType="dijit.form.Button" onclick="localModule.searchEntityKey('queryConsumer', 'email', 'Consumer', 'consumer.key');" type="button">Search</button>

                Location by postal code (in Canada):
                <input
                    dojoType="dijit.form.ValidationTextBox"
                    id="queryLocation"
                    invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>"
                    onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.searchEntityKey('queryLocation', 'postalCode', 'Location', 'location.key', { 'countryCode': 'CA', 'centerOnly': true }); }"
                    placeholder="<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_default_CA", locale) %>"
                    regExp="<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_regExp_CA", locale) %>"
                    required="true"
                    style="width:6em;"
                    type="text"
                />
                <button dojoType="dijit.form.Button" onclick="localModule.searchEntityKey('queryLocation', 'postalCode', 'Location', 'location.key', { 'countryCode': 'CA', 'centerOnly': true });" type="button">Search</button>

                Store by name:
                <input
                    id="queryStore"
                    dojoType="dijit.form.ValidationTextBox"
                    invalidMessage="Must be at least 3 characters long, used to select the Stores with a name starting with the field value"
                    onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.searchEntityKey('queryStore', 'name', 'Store', 'store.key'); }"
                    placeHolder="name (starts with)"
                    regExp="\S\S[^\s|\*].*"
                    required="true"
                    type="text"
                />
                <button dojoType="dijit.form.Button" onclick="localModule.searchEntityKey('queryStore', 'name', 'Store', 'store.key');" type="button">Search</button>

                RawCommand after date:
                <input
                    id="queyDueDate"
                    dojoType="dijit.form.DateTextBox"
                    onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.searchEntityKey('queyDueDate', 'creationDate', 'RawCommand', 'rawcommand.key'); }"
                    required="true"
                    style="width:8em;"
                    type="text"
                />
                <button dojoType="dijit.form.Button" onclick="localModule.searchEntityKey('queyDueDate', 'creationDate', 'RawCommand', 'rawcommand.key');" type="button">Search</button>
            </fieldset>
            <div style="float:left;">
               <a id="turnOffRow1" href="javascript:dojo.query('#turnOffRow1').style('display', 'none');dojo.query('#turnOnRow1').style('display', '');dojo.query('#consumerInformation>table').style('display','none');dojo.query('#saleassociateInformation>table').style('display','none');dojo.query('#storeInformation>table').style('display','none');dojo.query('#locationInformation>table').style('display','none');">[&ndash;]</a>
               <a id="turnOnRow1" href="javascript:dojo.query('#turnOnRow1').style('display', 'none');dojo.query('#turnOffRow1').style('display', '');dojo.query('#consumerInformation>table').style('display','');dojo.query('#saleassociateInformation>table').style('display','');dojo.query('#storeInformation>table').style('display','');dojo.query('#locationInformation>table').style('display','');" style="display:none;">[+]</a>
            </div>
            <fieldset class="entityInformation" id="consumerInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Consumer Information
                    <a href="javascript:dojo.query('#consumerInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#consumerInformation>table').style('display','');">[+]</a>
                </legend>
                <form dojoType="dijit.form.Form" id="consumerInformation">
                    <table>
                        <tr>
                            <td><label for="consumer.key">Key:</label></td>
                            <td><input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="consumer.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('consumer.key', 'Consumer'); }" style="width:8em;" type="text" />
                            <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('consumer.key', 'Consumer');" type="button">Fetch</button></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="consumer.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="consumer.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('consumer.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('consumer.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input dojoType="dijit.form.CheckBox" id="consumer.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" />
                            </td>
                            <td><label for="consumer.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="consumer.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="consumer._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="consumer.address">Address:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.address" name="address" type="text" /></td>
                        </tr>
                        <tr>
                            <td>
                                <input dojoType="dijit.form.CheckBox" id="consumer.automaticLocaleUpdate" name="automaticLocaleUpdate" type="checkbox" />
                            </td>
                            <td><label for="consumer.automaticLocaleUpdate">Automatic locale update</label></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.autonomy">Autonomy:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="consumer.autonomy"
                                    name="autonomy"
                                    style="width:8em;"
                                >
                                    <option value="BLOCKED">Blocked</option>
                                    <option value="UNCONFIRMED">Unconfirmed</option>
                                    <option value="MODERATED" selected="true">Moderated</option>
                                    <option value="AUTONOMOUS">Autonomous</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="consumer.closedDemandNb">Closed demand #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.closedDemandNb" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.facebookId">Facebook identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.facebookId" name="facebookId" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.jabberId">Jabber identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.jabberId" name="jabberId" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.language">Language:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="consumer.language"
                                    name="language"
                                    style="width:8em;"
                                >
                                    <option value="en" selected="true">English</option>
                                    <option value="fr">French</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="consumer.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.openID">OpenID:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.openID" name="openID" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.phoneNb">Phone number:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.phoneNb" name="phoneNb" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.preferredConnection">Preferred connection:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="consumer.preferredConnection"
                                    name="preferredConnection"
                                    style="width:10em;"
                                >
                                    <option value="mail" selected="mail">Mail</option>
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
                            <td><input dojoType="dijit.form.TextBox" id="consumer.publishedDemandNb" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="consumer.twitterId">Sale Associate key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="consumer.saleAssociateKey" name="saleAssociateKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('consumer.saleAssociateKey', 'SaleAssociate'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('consumer.saleAssociateKey', 'SaleAssociate');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="consumer.twitterId">Twitter name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.twitterId" name="twitterId" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Consumer');" type="button">Update</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.getDemandKeys();" type="button">Get demand keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="saleassociateInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Sale Associate Information
                    <a href="javascript:dojo.query('#saleassociateInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#saleassociateInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="saleassociateInformation">
                    <table>
                        <tr>
                            <td><label for="saleassociate.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="saleassociate.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.key', 'SaleAssociate'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.key', 'SaleAssociate');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="saleassociate.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="saleassociate.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input dojoType="dijit.form.CheckBox" id="saleassociate.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" />
                            </td>
                            <td><label for="saleassociate.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="saleassociate.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="saleassociate._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="saleassociate.closedProposalNb">Closed proposal #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="saleassociate.closedProposalNb" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.consumerKey">Consumer key:</label></td>
                            <td>
                                <input dojoType="dijit.form.NumberTextBox" id="saleassociate.consumerKey" name="consumerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.consumerKey', 'Consumer'); }" style="width:5em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.consumerKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.creatorKey">Creator key:</label></td>
                            <td>
                                <input dojoType="dijit.form.NumberTextBox" id="saleassociate.creatorKey" name="creatorKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.creatorKey', 'Consumer'); }" style="width:5em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.creatorKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.criteria">Criteria:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="saleassociate.criteria" name="criteria" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.hashTags">Hash tags:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="saleassociate.hashTags" name="hashTags" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.publishedProposalNb">Published proposal #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="saleassociate.publishedProposalNb" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.score">Req. match score:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="saleassociate.score" name="score" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="saleassociate.storeKey">Store key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="saleassociate.storeKey" name="storeKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('saleassociate.storeKey', 'Store'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.storeKey', 'Store');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="saleassociate.isStoreAdmin" name="isStoreAdmin" type="checkbox" /></td>
                            <td><label for="saleassociate.isStoreAdmin">Store administrator</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('SaleAssociate');" type="button">Update</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.getProposalKeys();" type="button">Get proposal keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="storeInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Store Information
                    <a href="javascript:dojo.query('#storeInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#storeInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="storeInformation">
                    <table>
                        <tr>
                            <td><label for="store.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="store.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('store.key', 'Store'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('store.key', 'Store');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="store.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="store.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="store.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('store.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('store.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="store.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="store.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="store.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="store.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="store._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="store.address">Address:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.address" name="address" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.closedProposalNb">Closed proposal #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.closedProposalNb" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.latitude">Latitude:</label></td>
                            <td><input dojoType="dijit.form.NumberTextBox" id="store.latitude" name="latitude" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.longitude">Longitude:</label></td>
                            <td><input dojoType="dijit.form.NumberTextBox" id="store.longitude" name="longitude" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.phoneNb">Phone number:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.phoneNb" name="phoneNb" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.publishedProposalNb">Published proposal #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.publishedProposalNb" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="store.registrarKey">Registrar key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="store.registrarKey" name="registrarKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('store.registrarKey', 'Registrar'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('store.registrarKey', 'Registrar');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="store.reviewSystemKey">Review system key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="store.reviewSystemKey" name="reviewSystemKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('store.reviewSystemKey', 'ReviewSystem'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('store.reviewSystemKey', 'ReviewSystem');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="store.state">State:</label></td>
                            <td><select dojoType="dijit.form.Select" id="store.state" name="state"><option value="referenced" selected="true">Referenced (minimal)</option><option value="inProgress">Needs follow-up</option><option value="waiting">Waiting activation</option><option value="active">Activated</option><option value="excluded">Excluded</option></select></td>
                        </tr>
                        <tr>
                            <td><label for="store.url">URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.url" name="url" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Store');" type="button">Update</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.getSaleAssociateKeys();" type="button">Get sale associate keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="locationInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Location Information
                    <a href="javascript:dojo.query('#locationInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#locationInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="locationInformation">
                    <table id="locationAttributeTable">
                        <tr>
                            <td><label for="location.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="location.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('location.key', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('location.key', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="location.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="location.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="location.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="location.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('location.locationKey', 'Location'); }" readonly="true" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('location.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="location.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="location.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="location.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="location.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="location._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="location._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="location.countryCode">Country code:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    id="location.countryCode"
                                    onchange="twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'location.postalCode');"
                                >
                                    <option value="CA" selected="true">Canada</option>
                                    <option value="US">United States of America</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="location.hasStore" name="hasStore" readonly="true" type="checkbox" /></td>
                            <td><label for="location.hasStore">Has store</label></td>
                        </tr>
                        <tr>
                            <td><label for="location.latitude">Latitude:</label></td>
                            <td><input dojoType="dijit.form.NumberTextBox" id="location.latitude" name="latitude" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for=location.longitude>Longitude:</label></td>
                            <td><input dojoType="dijit.form.NumberTextBox" id="location.longitude" name="longitude" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="location.postalCode">Postal code:</label></td>
                            <td>
                                <input
                                    dojoType="dijit.form.ValidationTextBox"
                                    id="location.postalCode"
                                    invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>"
                                    name="postalCode"
                                    placeholder="<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_default_CA", locale) %>"
                                    regExp="<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_regExp_CA", locale) %>"
                                    required="true"
                                    style="width:6em;"
                                    type="text"
                                />
                            </td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Location');" type="button">Update</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.resolveLocation();" type="button">Resolve</button>
                                <button disabled="true" dojoType="dijit.form.Button" type="button">View map</button>
                                <br />
                                <button dojoType="dijit.form.Button" onclick="localModule.openLocationFilterDialog();" type="button">Get location keys</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.openStoreFilterDialog();" type="button">Get store keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <br clear="both" />
            <div style="float:left;">
               <a id="turnOffRow2" href="javascript:dojo.query('#turnOffRow2').style('display', 'none');dojo.query('#turnOnRow2').style('display', '');dojo.query('#demandInformation>table').style('display','none');dojo.query('#proposalInformation>table').style('display','none');dojo.query('#rawcommandInformation>table').style('display','none');dojo.query('#paymentInformation>table').style('display','none');">[&ndash;]</a>
               <a id="turnOnRow2" href="javascript:dojo.query('#turnOnRow2').style('display', 'none');dojo.query('#turnOffRow2').style('display', '');dojo.query('#demandInformation>table').style('display','');dojo.query('#proposalInformation>table').style('display','');dojo.query('#rawcommandInformation>table').style('display','');dojo.query('#paymentInformation>table').style('display','');" style="display:none;">[+]</a>
            </div>
            <fieldset class="entityInformation" id="demandInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Demand Information
                    <a href="javascript:dojo.query('#demandInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#demandInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="demandInformation">
                    <table>
                        <tr>
                            <td style="font-weight:bold;text-align:right;padding-right:10px;color:orange;">Point of view:</td>
                            <td id="demand.pointOfView" style="font-weight:bold;color:orange;"></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="demand.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="demand.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.key', 'Demand'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.key', 'Demand');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="demand.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="demand.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="demand.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="demand.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="demand.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="demand.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="demand._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="demand.action">Action:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="demand.action" name="action" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.cancelerKey">Canceler key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="demand.cancelerKey" name="cancelerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.cancelerKey', 'Consumer'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.cancelerKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.cc">CC-ed:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="demand.cc" name="cc" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="demand.content">Content:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="demand.content" name="content" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="demand.dueDate">Due date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="demand.dueDate" name="dueDate" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.hashTags">Hash tags:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="demand.hashTags" name="hashTags" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="demand.metadata">Metadata:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="demand.metadata" name="metadata" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="demand.ownerKey">Owner key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="demand.ownerKey" name="ownerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.ownerKey', 'Consumer'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.ownerKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.rawCommandId">RawCommand key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="demand.rawCommandId" name="rawCommandId" readonly="true" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.rawCommandId', 'RawCommand');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.source">Source:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="demand.source"
                                    name="source"
                                    readonly="true"
                                    style="width:10em;"
                                >
                                    <option value="mail" selected="true">E-mail</option>
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
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="demand.state"
                                    name="state"
                                    style="width:10em;"
                                >
                                    <option value="opened" selected="true">Open</option>
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
                            <td><input dojoType="dijit.form.CheckBox" id="demand.stateCmdList" name="stateCmdList" readonly="true" type="checkbox" /></td>
                            <td><label for="demand.stateCmdList">State for command: !list</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="demand.expirationDate">Expiration date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="demand.expirationDate" name="expirationDate" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.influencerKey">Influencer key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="demand.influencerKey" name="influencerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.influencerKey', 'Influencer'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.influencerKey', 'Influencer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.proposalKeys">Proposal keys:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.ComboBox"
                                    hasDownArrow="true"
                                    id="demand.proposalKeys"
                                    onchange="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.proposalKeys', 'Proposal', 'CONSUMER'); }"
                                    style="width:8em;"
                                >
                                    <option value="none" selected="true">None</option>
                                </select>
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.proposalKeys', 'Proposal', 'CONSUMER');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.quantity">Quantity:</label></td>
                            <td><input constraints="{min:0,space:0}" dojoType="dijit.form.NumberTextBox" id="demand.quantity" name="quantity" style="width:3em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.range">Range:</label></td>
                            <td><input constraints="{min:0,space:0}" dojoType="dijit.form.NumberTextBox" id="demand.range" name="range" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="demand.rangeUnit">Range unit:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="demand.rangeUnit"
                                    name="rangeUnit"
                                    style="width:9em;"
                                >
                                    <option value="km" selected="true">Kilometers</option>
                                    <option value="mi">Miles</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="demand.saleAssociateKeys">Sale associate keys:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.ComboBox"
                                    hasDownArrow="true"
                                    id="demand.saleAssociateKeys"
                                    onchange="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('demand.saleAssociateKeys', 'SaleAssociate'); }"
                                    style="width:8em;"
                                >
                                    <option value="none" selected="true">None</option>
                                </select>
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.saleAssociateKeys', 'SaleAssociate');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" id="demand.updateButton" onclick="localModule.saveEntity('Demand');" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="proposalInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Proposal Information
                    <a href="javascript:dojo.query('#proposalInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#proposalInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="proposalInformation">
                    <table>
                        <tr>
                            <td style="font-weight:bold;text-align:right;padding-right:10px;color:orange;">Point of view:</td>
                            <td id="proposal.pointOfView" style="font-weight:bold;color:orange;"></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="proposal.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="proposal.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.key', 'Proposal'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.key', 'Proposal');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="proposal.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="proposal.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="proposal.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="proposal.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="proposal.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="proposal._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="proposal.action">Action:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="proposal.action" name="action" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.cancelerKey">Canceler key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.cancelerKey" name="cancelerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.cancelerKey', 'Consumer'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.cancelerKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.content">Content:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="proposal.content" name="content" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.dueDate">Due date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="proposal.dueDate" name="dueDate" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.hashTags">Hash tags:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="proposal.hashTags" name="hashTags" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.metadata">Metadata:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="proposal.metadata" name="metadata" style="width:10em;"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.ownerKey">Owner key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.ownerKey" name="ownerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.ownerKey', 'SaleAssociate'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.ownerKey', 'SaleAssociate');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.rawCommandId">RawCommand key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.rawCommandId" name="rawCommandId" readonly="true" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.rawCommandId', 'RawCommand');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.source">Source:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="proposal.source"
                                    name="source"
                                    readonly="true"
                                    style="width:10em;"
                                >
                                    <option value="mail" selected="true">E-mail</option>
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
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="proposal.state"
                                    name="state"
                                    style="width:10em;"
                                >
                                    <option value="opened" selected="true">Open</option>
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
                            <td><input dojoType="dijit.form.CheckBox" id="proposal.stateCmdList" name="stateCmdList" readonly="true" type="checkbox" /></td>
                            <td><label for="proposal.stateCmdList">State for command: !list</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="proposal.AWSCBUIURL">Co-branded service URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="proposal.AWSCBUIURL" name="AWSCBUIURL" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.consumerKey">Consumer key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.consumerKey" name="consumerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.consumerKey', 'Consumer'); }" readonly="true" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.consumerKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.comment">Comment:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="proposal.comment" name="comment"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.currencyCode">Currency:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="proposal.currencyCode"
                                    name="currencyCode"
                                    style="width:10em;"
                                >
                                    <option value="USD" selected="true">$ / USD</option>
                                    <option value="CAD">$ / CAD</option>
                                    <option value="EUR">€ / EUR</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.demandKey">Demand key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.demandKey" name="demandKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.demandKey', 'Demand', 'SALE_ASSOCIATE'); }" readonly="true" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.demandKey', 'Demand', 'SALE_ASSOCIATE');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.historic">Historic:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="proposal.historic" name="historic"></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.price">Price:</label></td>
                            <td><input constraints="{min:0,space:2}" dojoType="dijit.form.NumberTextBox" id="proposal.price" name="price" style="width:7em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.quantity">Quantity:</label></td>
                            <td><input constraints="{min:0,space:0}" dojoType="dijit.form.NumberTextBox" id="proposal.quantity" name="quantity" style="width:3em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="proposal.score">Score:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="proposal.score"
                                    name="score"
                                    style="width:10em;"
                                >
                                    <option value="0" selected="true">Not rated</option>
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
                                <input dojoType="dijit.form.TextBox" id="proposal.storeKey" name="storeKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('proposal.storeKey', 'Store'); }" readonly="true" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.storeKey', 'Store');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="proposal.total">Total:</label></td>
                            <td><input constraints="{min:0,space:2}" dojoType="dijit.form.NumberTextBox" id="proposal.total" name="total" style="width:7em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" id="proposal.updateButton" onclick="localModule.saveEntity('Proposal');" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="rawcommandInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Raw Command Information
                    <a href="javascript:dojo.query('#rawcommandInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#rawcommandInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="rawcommandInformation">
                    <table>
                        <tr>
                            <td><label for="rawcommand.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="rawcommand.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('rawcommand.key', 'RawCommand'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('rawcommand.key', 'RawCommand');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="rawcommand.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="rawcommand.locationKey" name="locationKey" readonly="true" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('rawcommand.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('rawcommand.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="rawcommand.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="rawcommand.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="rawcommand.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="rawcommand._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="rawcommand.command">Command:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="rawcommand.command" name="command" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.commandId">Command Id:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="rawcommand.commandId" name="commandId" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.emitterId">Emitter Id:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="rawcommand.emitterId" name="emitterId" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.errorMessage">Error msg:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="rawcommand.errorMessage" name="errorMessage" ></textarea></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.source">Source:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="rawcommand.source"
                                    name="source"
                                    readonly="true"
                                    style="width:10em;"
                                >
                                    <option value="mail" selected="true">E-mail</option>
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
                            <td><input dojoType="dijit.form.TextBox" id="rawcommand.subject" name="subject" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="rawcommand.toId">To Id:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="rawcommand.toId" name="toId" type="text" /></td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="paymentInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Payment Information
                    <a href="javascript:dojo.query('#paymentInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#paymentInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="paymentInformation">
                    <table style="display:none;">
                        <tr>
                            <td><label for="payment.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="payment.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('payment.key', 'Payment'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('payment.key', 'Payment');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="payment.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="payment.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="payment.locationKey" name="locationKey" readonly="true" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('payment.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('payment.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="payment.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="payment.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="payment.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="payment.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="payment._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="payment.authorizationId">Authorization identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.authorizationId" name="authorizationId" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.reference">Reference:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.reference" name="reference" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.requestId">Request identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.requestId" name="requestId" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.transactionId">Transaction identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.transactionId" name="transactionId" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="payment.status">Status:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.status" name="status" readonly="true" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.saveEntity('Payment');" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <br clear="both" />
            <div style="float:left;">
               <a id="turnOffRow3" href="javascript:dojo.query('#turnOffRow3').style('display', 'none');dojo.query('#turnOnRow3').style('display', '');dojo.query('#influencerInformation>table').style('display','none');dojo.query('#registrarInformation>table').style('display','none');dojo.query('#resellerInformation>table').style('display','none');dojo.query('#reviewsystemInformation>table').style('display','none');">[&ndash;]</a>
               <a id="turnOnRow3" href="javascript:dojo.query('#turnOnRow3').style('display', 'none');dojo.query('#turnOffRow3').style('display', '');dojo.query('#influencerInformation>table').style('display','');dojo.query('#registrarInformation>table').style('display','');dojo.query('#resellerInformation>table').style('display','');dojo.query('#reviewsystemInformation>table').style('display','');" style="display:none;">[+]</a>
            </div>
            <fieldset class="entityInformation" id="influencerInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Influencer Information
                    <a href="javascript:dojo.query('#influencerInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#influencerInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="influencerInformation">
                    <table id="influencerAttributeTable" style="display:none;">
                        <tr>
                            <td><label for="influencer.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="influencer.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('influencer.key', 'Influencer'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('influencer.key', 'Influencer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="influencer.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="influencer.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="influencer.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('influencer.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('influencer.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="influencer.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="influencer.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="influencer.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="influencer._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="influencer.consumerKey">Consumer key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="influencer.consumerKey" name="consumerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('influencer.consumerKey', 'Consumer'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('influencer.consumerKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="influencer.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="influencer.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="influencer.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.referralId">Referral Id:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="influencer.referralId" name="referralId" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="influencer.url">URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="influencer.url" name="url" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Influencer');" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="registrarInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Registrar Information
                    <a href="javascript:dojo.query('#registrarInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#registrarInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="registrarInformation">
                    <table id="registrarAttributeTable" style="display:none;">
                        <tr>
                            <td><label for="registrar.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="registrar.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('registrar.key', 'Registrar'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('registrar.key', 'Registrar');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="registrar.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="registrar.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="registrar.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="registrar.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('registrar.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('registrar.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="registrar.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="registrar.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="registrar.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="registrar.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="registrar._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="registrar._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="registrar.consumerKey">Consumer key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="registrar.consumerKey" name="consumerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('registrar.consumerKey', 'Consumer'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('registrar.consumerKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="registrar.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="registrar.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="registrar.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="registrar.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="registrar.url">URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="registrar.url" name="url" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Registrar');" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="resellerInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Reseller Information
                    <a href="javascript:dojo.query('#resellerInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#resellerInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="resellerInformation">
                    <table id="resellerAttributeTable" style="display:none;">
                        <tr>
                            <td><label for="reseller.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reseller.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reseller.key', 'Reseller'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reseller.key', 'Reseller');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="reseller.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="reseller.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="reseller.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reseller.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reseller.locationKey', 'Location'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reseller.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="reseller.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="reseller.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="reseller.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="reseller.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="reseller._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="reseller._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="reseller.consumerKey">Consumer key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reseller.consumerKey" name="consumerKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reseller.consumerKey', 'Consumer'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reseller.consumerKey', 'Consumer');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="reseller.tokenNb">Tokens:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reseller.tokenNb" name="tokenNb" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Reseller');" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="reviewsystemInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Review System Information
                    <a href="javascript:dojo.query('#reviewsystemInformation>table').style('display','none');">[&ndash;]</a> /
                    <a href="javascript:dojo.query('#reviewsystemInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="reviewsystemInformation">
                    <table id="reviewsystemAttributeTable" style="display:none;">
                        <tr>
                            <td><label for="reviewsystem.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reviewsystem.key" name="key" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reviewsystem.key', 'ReviewSystem'); }" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reviewsystem.key', 'ReviewSystem');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="reviewsystem.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reviewsystem.locationKey" name="locationKey" onkeyup="if (event.keyCode == dojo.keys.ENTER) { localModule.fetchEntity('reviewsystem.locationKey', 'Location'); }" readonly="true" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reviewsystem.locationKey', 'Location');" type="button">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td><input dojoType="dijit.form.CheckBox" id="reviewsystem.markedForDeletion" name="markedForDeletion" readonly="true" type="checkbox" /></td>
                            <td><label for="reviewsystem.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="reviewsystem.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem._tracking" style="font-style: italic;">Tracking:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="reviewsystem._tracking" name="_tracking" style="width:10em;"></textarea></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td><label for="reviewsystem.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reviewsystem.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reviewsystem.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td><label for="reviewsystem.url">URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reviewsystem.url" name="url" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('ReviewSystem');" type="button">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="entityKeysDialog"
        style="min-width:260px;"
    >
        <div id="keyZone" style="min-height:60px;"></div>
        <div class="dijitDialogPaneActionBar" style="text-align:right;padding-top:5px;">
            <button dojoType="dijit.form.Button" onclick="dijit.byId('entityKeysDialog').hide();" type="button">Close</button>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        execute="localModule.getLocationKeys();"
        id="locationFilterDialog"
        title="Location filters"
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
                                    dojoType="dijit.form.ValidationTextBox"
                                    id="locationFilter.postalCode"
                                    invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>"
                                    name="postalCode"
                                    placeholder="<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_default_CA", locale) %>"
                                    regExp="<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_regExp_CA", locale) %>"
                                    required="true"
                                    style="width:6em;"
                                    type="text"
                                />
                    </td>
                </tr>
                <tr>
                    <td><label for="locationFilter.countryCode">Country code:</label></td>
                    <td>
                        <select
                            dojoType="dijit.form.Select"
                            id="locationFilter.countryCode"
                            onchange="twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'locationFilter.postalCode');"
                        >
                            <option value="CA" selected="true">Canada</option>
                            <option value="US">United States of America</option>
                        </select>
                    </td>
                </tr>
                <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                <tr>
                    <td><label for="locationFilter.latitude">Latitude:</label></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="locationFilter.latitude" type="text" value="-1000" /></td>
                </tr>
                <tr>
                    <td><label for=locationFilter.longitude>Longitude:</label></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="locationFilter.longitude" type="text" value="-1000" /></td>
                </tr>
                <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                <tr>
                    <td><label for="locationFilter.range">Range:</label></td>
                    <td>
                        <input dojoType="dijit.form.TextBox" id="locationFilter.range" style="width:4em;" type="text" value="100" />
                        <select dojoType="dijit.form.Select" id="locationFilter.rangeUnit"><option value="km" selected="true">km</option><option value="mi">miles</option></select>
                    </td>
                </tr>
                <tr id="hasStoreRow">
                    <td><input dojoType="dijit.form.CheckBox" id="locationFilter.hasStore" type="checkbox" /></td>
                    <td><label for="locationFilter.hasStore">Has store</label></td>
                </tr>
            </table>
        </div>
        <div class="dijitDialogPaneActionBar" style="text-align:right;margin-top:10px;">
            <button dojoType="dijit.form.Button" id="ok" type="submit">Search</button>
            <button dojoType="dijit.form.Button" onClick="dijit.byId('locationFilterDialog').hide();" type="button">Cancel</button>
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
        dojo.animateProperty({
            node: prefix + 'InformationFieldset',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        dojo.xhrGet({
            headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            content: {
                'pointOfView': pointOfView,
                '<%= BaseRestlet.ON_BEHALF_CONSUMER_KEY %>': dijit.byId('consumer.key').get('value'),
                '<%= BaseRestlet.ON_BEHALF_ASSOCIATE_KEY %>': dijit.byId('saleassociate.key').get('value'),
                '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes'
            },
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    dijit.byId(prefix + 'Information').reset();
                    var resource = response.resource;
                    for (var attr in resource) {
                        try {
                            var value = resource[attr];
                            if (attr.indexOf('Date') != -1) {
                                value = dojo.date.stamp.fromISOString(value);
                            }
                            if (attr == 'criteria' || attr == 'hashTags' || attr == 'cc') {
                                value = value.join('\n');
                            }
                            if (attr == 'proposalKeys' || attr == 'saleAssociateKeys') {
                                var options = new dojo.data.ItemFileWriteStore({ data: { identifier: 'name', items: [] } })
                                var limit = value.length;
                                for (var idx = 0; idx < limit; idx++) {
                                    options.newItem({ name: value [idx] });
                                }
                                dijit.byId(prefix + '.' + attr).set('store', options);
                                dijit.byId(prefix + '.' + attr).set('value', value[0]);
                            }
                            else {
                                var field = dijit.byId(prefix + '.' + attr)
                                if (field) {
                                    field.set('value', value);
                                }
                                else if (attr != 'criteria') {
                                    alert('Field "' + prefix + '.' + attr + '" is missing!');
                                }
                            }
                            if (attr == 'state' && (entityName == 'Demand' || entityName == 'Proposal')) {
                                var isNonModifiable = value == 'closed' || value == 'cancelled' || value == 'markedForDeletion';
                                dijit.byId(prefix + '.updateButton').set('disabled', isNonModifiable);
                            }
                        }
                        catch (ex) {
                            alert('Error while processing attribute "' + attr + '" for an instance of class "' + entityName + '".\nError: ' + ex);
                        }
                    }
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dojo.animateProperty({
                    node: prefix + 'InformationFieldset',
                    properties: { backgroundColor: { end: 'transparent' } }
                }).play();
            },
            error: function(message, ioArgs) { twetailer.Common.handleError(message, ioArgs, true); },
            url: '/API/' + entityName + '/' + key
        });
    };
    localModule.saveEntity = function(entityName) {
        var prefix = entityName.toLowerCase();
        var key = dijit.byId(prefix + '.key').get('value');
        if (isNaN(key)) {
            alert('The key in the field \'' + keyFieldId + '\' is not a number!');
            return;
        }
        dojo.animateProperty({
            node: prefix + 'InformationFieldset',
            properties: { backgroundColor: { end: 'yellow' } }
        }).play();
        var pointOfView = (entityName == 'Proposal' || entityName == 'Store') ? 'SALE_ASSOCIATE' : 'CONSUMER';
        var prefix = entityName.toLowerCase();
        var pointOfViewField = dojo.byId(prefix + '.pointOfView');
        if (pointOfViewField) {
            pointOfViewField.innerHTML = pointOfView;
        }
        var data = localModule.formToObject(prefix + 'Information');
        if (data.cc != null) { data.cc = data.cc.split('\n'); }
        if (data.criteria != null) { data.criteria = data.criteria.split('\n'); }
        if (data.hashTags != null) { data.hashTags = data.hashTags.split('\n'); }
        if (data.proposalKeys != null) { delete data.proposalKeys; } // Neutralized server-side, just removed for the bandwidth
        if (data.saleAssociateKeys != null) { delete data.saleAssociateKeys; } // Neutralized server-side, just removed for the bandwidth
        data['<%= CommandProcessor.DEBUG_INFO_SWITCH %>'] = 'yes';
        data['pointOfView'] = pointOfView;
        data['<%= BaseRestlet.ON_BEHALF_CONSUMER_KEY %>'] = parseInt(dijit.byId('consumer.key').get('value'));
        data['<%= BaseRestlet.ON_BEHALF_ASSOCIATE_KEY %>'] = parseInt(dijit.byId('saleassociate.key').get('value') || 0);
        dojo.xhrPut({
            headers: { 'content-type': 'application/json; charset=UTF-8' },
            putData: dojo.toJson(data),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    // No visual feedback
                }
                else {
                    alert(response.exceptionMessage+'\nurl: '+ioArgs.url+'\n\n'+response.originalExceptionMessage);
                }
                dojo.animateProperty({
                    node: prefix + 'InformationFieldset',
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
                var _in = item.name;
                if (item.checked === true) {
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
            content: dojo.mixin({ 'anyState': true, 'onlyKeys': true, '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes' }, (parameters || {})),
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
                '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes'
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
            '<%= CommandProcessor.DEBUG_INFO_SWITCH %>': 'yes'
        };
        if (filterName.indexOf('Date') != -1) {
            data[filterName] = twetailer.Common.toISOString(filterField.get('value'), localModule._earlyHourTime);
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
</body>
</html>
