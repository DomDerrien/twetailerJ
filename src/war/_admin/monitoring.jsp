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
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Consumer attributes
    OpenIdUser loggedUser = BaseRestlet.getLoggedUser(request);
    Consumer consumer = LoginServlet.getConsumer(loggedUser);
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <title>Monitoring Console</title>
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
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
    </style>
</head>
<body class="tundra">

    <div id="introFlash">
        <div><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
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
            <jsp:param name="consumerName" value="<%= consumer.getName() %>" />
        </jsp:include>
        <div
            dojoType="dijit.layout.ContentPane"
            id="centerZone"
            region="center"
        >
            <p align="center">
                Administrative console:
                <a href="http://appengine.google.com/">hosted</a> --
                <a href="http://127.0.0.1:9999/_ah/admin">local</a>;
                Registration console:
                <a href="http://anothersocialeconomy.appspot.com/_admin/registration.jsp">hosted</a> --
                <a href="http://127.0.0.1:9999/_admin/registration.jsp">local</a>;
                Monitoring console:
                <a href="http://anothersocialeconomy.appspot.com/_admin/monitoring.jsp">hosted</a> --
                <a href="http://127.0.0.1:9999/_admin/monitoring.jsp">local</a>.
            </p>
            <fieldset class="entityInformation" id="consumerInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Consumer Information
                    <a href="javascript:dojo.query('#consumerInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#consumerInformation>table').style('display','');">[+]</a>
                </legend>
                <form dojoType="dijit.form.Form" id="consumerInformation">
                    <table>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.key">Key:</label></td>
                            <td><input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="consumer.key" name="key" style="width:8em;" type="text" />
                            <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('consumer.key', 'Consumer');">Fetch</button></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="consumer.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="consumer.locationKey" name="locationKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('consumer.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;">
                                <input dojoType="dijit.form.CheckBox" id="consumer.markedForDeletion" name="markedForDeletion" type="checkbox" />
                            </td>
                            <td><label for="consumer.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="consumer.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.address">Address:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.address" name="address" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;">
                                <input dojoType="dijit.form.CheckBox" id="consumer.automaticLocaleUpdate" name="automaticLocaleUpdate" type="checkbox" />
                            </td>
                            <td><label for="consumer.automaticLocaleUpdate">Automatic locale update</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.closedDemandNb">Closed demand #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.closedDemandNb" readonly="true" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.jabberId">Jabber identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.jabberId" name="jabberId" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.language">Language:</label></td>
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
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.openID">OpenID:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.openID" name="openID" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.phoneNb">Phone number:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.phoneNb" name="phoneNb" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.preferredConnection">Phone number:</label></td>
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
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.publishedDemandNb">Published demand #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.publishedDemandNb" readonly="true" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.twitterId">Sale Associate key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="consumer.saleAssociateKey" name="saleAssociateKey" readonly="true" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('consumer.saleAssociateKey', 'SaleAssociate');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="consumer.twitterId">Twitter name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="consumer.twitterId" name="twitterId" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Consumer');">Update</button>
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.deleteEntity('Consumer');">Delete</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.loadEntityKeys('consumer.key','Demand');">Get demand keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="saleassociateInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Sale Associate Information
                    <a href="javascript:dojo.query('#saleassociateInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#saleassociateInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="saleassociateInformation">
                    <table>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="saleassociate.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.key', 'SaleAssociate');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="saleassociate.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="saleassociate.locationKey" name="locationKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;">
                                <input dojoType="dijit.form.CheckBox" id="saleassociate.markedForDeletion" name="markedForDeletion" type="checkbox" />
                            </td>
                            <td><label for="saleassociate.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="saleassociate.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.closedProposalNb">Closed proposal #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="saleassociate.closedProposalNb" readonly="true" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.consumerKey">Consumer key:</label></td>
                            <td>
                                <input dojoType="dijit.form.NumberTextBox" id="saleassociate.consumerKey" name="consumerKey" readonly="true" style="width:5em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.consumerKey', 'Consumer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.creatorKey">Creator key:</label></td>
                            <td>
                                <input dojoType="dijit.form.NumberTextBox" id="saleassociate.creatorKey" name="creatorKey" readonly="true" style="width:5em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.creatorKey', 'Consumer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.criteria">Criteria:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="saleassociate.criteria" name="criteria" style="width:10em;">None</textarea></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.hashTags">Hash tags:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="saleassociate.hashTags" name="hashTags" style="width:10em;">None</textarea></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.publishedProposalNb">Published proposal #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="saleassociate.publishedProposalNb" readonly="true" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.score">Score:</label></td>
                            <td><input dojoType="dijit.form.NumberTextBox" id="saleassociate.score" name="score" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="saleassociate.storeKey">Store key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="saleassociate.storeKey" name="storeKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('saleassociate.storeKey', 'Store');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="saleassociate.isStoreAdmin" name="isStoreAdmin" type="checkbox" /></td>
                            <td><label for="saleassociate.isStoreAdmin">Store administrator</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('SaleAssociate');">Update</button>
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.deleteEntity('SaleAssociate');">Delete</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.loadEntityKeys('consumer.key','Proposal');">Get proposal keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="storeInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Store Information
                    <a href="javascript:dojo.query('#storeInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#storeInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="storeInformation">
                    <table>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="store.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('store.key', 'Store');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="store.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="store.locationKey" name="locationKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('store.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="store.markedForDeletion" name="markedForDeletion" type="checkbox" /></td>
                            <td><label for="store.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="store.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.address">Address:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.address" name="address" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.closedProposalNb">Closed proposal #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.closedProposalNb" readonly="true" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.phoneNb">Phone number:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.phoneNb" name="phoneNb" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.publishedProposalNb">Published proposal #:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.publishedProposalNb" readonly="true" style="width:6em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.resellerKey">Reseller key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="store.resellerKey" name="resellerKey" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('store.resellerKey', 'Reseller');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.reviewSystemKey">Review system key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="store.reviewSystemKey" name="reviewSystemKey" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('store.reviewSystemKey', 'ReviewSystem');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="store.url">URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="store.url" name="url" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Store');">Update</button>
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.saveEntity('Store');">Get sale associate keys</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.loadEntityKeys('consumer.key','Store');">Get store keys</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="locationInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Location Information
                    <a href="javascript:dojo.query('#locationInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#locationInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="locationInformation">
                    <table id="locationAttributeTable">
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="location.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="location.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('location.key', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="location.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="location.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="location.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="location.locationKey" name="locationKey" readonly="true" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('location.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="location.markedForDeletion" name="markedForDeletion" type="checkbox" /></td>
                            <td><label for="location.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="location.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="location.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="location.countryCode">Country code:</label></td>
                            <td><select dojoType="dijit.form.Select" id="location.countryCode" name="countryCode"><option value="CA" selected="true">Canada</option><option value="US">United States of America</option></select></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="location.hasStore" name="hasStore" readonly="true" type="checkbox" /></td>
                            <td><label for="location.hasStore">Has store</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="location.latitude">Latitude:</label></td>
                            <td><input dojoType="dijit.form.NumberTextBox" id="location.latitude" name="latitude" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for=location.longitude>Longitude:</label></td>
                            <td><input dojoType="dijit.form.NumberTextBox" id="location.longitude" name="longitude" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="location.postalCode">Postal code:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="location.postalCode" name="postalCode" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.saveEntity('Location');">Update</button>
                                <button dojoType="dijit.form.Button" onclick="dijit.byId('locationFilterDialog').show();">Get location keys</button>
                                <button dojoType="dijit.form.Button" onclick="localModule.resolveLocation();">Resolve</button>
                                <button disabled="true" dojoType="dijit.form.Button">View Map</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <br clear="both" />
            <fieldset class="entityInformation" id="influencerInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Influencer Information
                    <a href="javascript:dojo.query('#influencerInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#influencerInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="influencerInformation">
                    <table id="influencerAttributeTable">
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="influencer.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('influencer.key', 'Influencer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="influencer.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="influencer.locationKey" name="locationKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('influencer.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="influencer.markedForDeletion" name="markedForDeletion" type="checkbox" /></td>
                            <td><label for="influencer.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="influencer.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.consumerKey">Consumer key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="influencer.consumerKey" name="consumerKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('influencer.consumerKey', 'Consumer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="influencer.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="influencer.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.referralId">Referral Id:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="influencer.referralId" name="referralId" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="influencer.url">URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="influencer.url" name="url" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Influencer');">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="resellerInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Reseller Information
                    <a href="javascript:dojo.query('#resellerInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#resellerInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="resellerInformation">
                    <table id="resellerAttributeTable">
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reseller.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reseller.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reseller.key', 'Reseller');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reseller.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="reseller.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reseller.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reseller.locationKey" name="locationKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reseller.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="reseller.markedForDeletion" name="markedForDeletion" type="checkbox" /></td>
                            <td><label for="reseller.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reseller.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="reseller.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reseller.consumerKey">Consumer key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reseller.consumerKey" name="consumerKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reseller.consumerKey', 'Consumer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reseller.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reseller.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reseller.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reseller.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reseller.url">URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reseller.url" name="url" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('Reseller');">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="reviewSystemInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Review System Information
                    <a href="javascript:dojo.query('#reviewSystemInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#reviewSystemInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="reviewSystemInformation">
                    <table id="reviewSystemAttributeTable">
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reviewSystem.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reviewSystem.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reviewSystem.key', 'ReviewSystem');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reviewSystem.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="reviewSystem.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reviewSystem.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="reviewSystem.locationKey" name="locationKey" readonly="true" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('reviewSystem.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="reviewSystem.markedForDeletion" name="markedForDeletion" type="checkbox" /></td>
                            <td><label for="reviewSystem.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reviewSystem.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="reviewSystem.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reviewSystem.email">E-mail:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reviewSystem.email" name="email" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reviewSystem.name">Name:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reviewSystem.name" name="name" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="reviewSystem.url">URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="reviewSystem.url" name="url" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" onclick="localModule.saveEntity('ReviewSystem');">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <br clear="both" />
            <fieldset class="entityInformation" id="demandInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Demand Information
                    <a href="javascript:dojo.query('#demandInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#demandInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="demandInformation">
                    <table>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="demand.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.key', 'Demand');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="demand.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="demand.locationKey" name="locationKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="demand.markedForDeletion" name="markedForDeletion" type="checkbox" /></td>
                            <td><label for="demand.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="demand.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.action">Action:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="demand.action" name="action" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.cancelerKey">Canceler key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="demand.cancelerKey" name="cancelerKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.cancelerKey', 'Consumer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.cc">CC-ed:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="demand.cc" name="cc" style="width:10em;">None</textarea></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.criteria">Criteria:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="demand.criteria" name="criteria" style="width:10em;">None</textarea></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.dueDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="demand.dueDate" name="dueDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.hashTags">Hash tags:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="demand.hashTags" name="hashTags" style="width:10em;">None</textarea></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.ownerKey">Owner key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="demand.ownerKey" name="ownerKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.ownerKey', 'Consumer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.rawCommandId">RawCommand identifier:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="demand.rawCommandId" name="rawCommandId" readonly="true" style="width:5em;" type="text" />
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.source">Source:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="demand.source"
                                    name="source"
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
                            <td style="text-align:right;padding-right:10px;"><label for="demand.state">State:</label></td>
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
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="demand.stateCmdList" name="stateCmdList" type="checkbox" /></td>
                            <td><label for="demand.stateCmdList">State for command: !list</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.expirationDate">Expiration date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="demand.expirationDate" name="expirationDate" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.influencerKey">Influencer key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="demand.influencerKey" name="influencerKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.influencerKey', 'Influencer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.proposalKeys">Proposal keys:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.ComboBox"
                                    hasDownArrow="true"
                                    id="demand.proposalKeys"
                                    name="proposalKeys"
                                    style="width:8em;"
                                >
                                    <option value="none" selected="true">None</option>
                                </select>
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.proposalKeys', 'Proposal');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.quantity">Quantity:</label></td>
                            <td><input constraints="{min:0,space:0}" dojoType="dijit.form.NumberTextBox" id="demand.quantity" name="quantity" style="width:3em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.range">Range:</label></td>
                            <td><input constraints="{min:0,space:0}" dojoType="dijit.form.NumberTextBox" id="demand.range" name="range" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="demand.rangeUnit">Range unit:</label></td>
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
                            <td style="text-align:right;padding-right:10px;"><label for="demand.saleAssociateKeys">Sale associate keys:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.ComboBox"
                                    hasDownArrow="true"
                                    id="demand.saleAssociateKeys"
                                    name="saleAssociateKeys"
                                    style="width:8em;"
                                >
                                    <option value="none" selected="true">None</option>
                                </select>
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('demand.saleAssociateKeys', 'SaleAssociate');">Fetch</button>
                            </td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" id="demand.updateButton" onclick="localModule.saveEntity('Demand');">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="proposalInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Proposal Information
                    <a href="javascript:dojo.query('#proposalInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#proposalInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="proposalInformation">
                    <table>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="proposal.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.key', 'Proposal');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="proposal.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="proposal.locationKey" name="locationKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="proposal.markedForDeletion" name="markedForDeletion" type="checkbox" /></td>
                            <td><label for="proposal.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="proposal.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.action">Action:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="proposal.action" name="action" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.cancelerKey">Canceler key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.cancelerKey" name="cancelerKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.cancelerKey', 'Consumer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.criteria">Criteria:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="proposal.criteria" name="criteria" style="width:10em;">None</textarea></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.dueDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="proposal.dueDate" name="dueDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.hashTags">Hash tags:</label></td>
                            <td><textarea dojoType="dijit.form.Textarea" id="proposal.hashTags" name="hashTags" style="width:10em;">None</textarea></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.ownerKey">Owner key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.ownerKey" name="ownerKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.ownerKey', 'SaleAssociate');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.rawCommandId">RawCommand identifier:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.rawCommandId" name="rawCommandId" readonly="true" style="width:5em;" type="text" />
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.source">Source:</label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    hasDownArrow="true"
                                    id="proposal.source"
                                    name="source"
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
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.state">State:</label></td>
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
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="proposal.stateCmdList" name="stateCmdList" type="checkbox" /></td>
                            <td><label for="proposal.stateCmdList">State for command: !list</label></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.AWSCBUIURL">Co-branded service URL:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="proposal.AWSCBUIURL" name="AWSCBUIURL" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.consumerKey">Consumer key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.consumerKey" name="consumerKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.consumerKey', 'Consumer');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.demandKey">Demand key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.demandKey" name="demandKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.demandKey', 'Demand');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.price">Price:</label></td>
                            <td><input constraints="{min:0,space:2}" dojoType="dijit.form.NumberTextBox" id="proposal.price" name="price" style="width:7em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.quantity">Quantity:</label></td>
                            <td><input constraints="{min:0,space:0}" dojoType="dijit.form.NumberTextBox" id="proposal.quantity" name="quantity" style="width:3em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.storeKey">Store key:</label></td>
                            <td>
                                <input dojoType="dijit.form.TextBox" id="proposal.storeKey" name="storeKey" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('proposal.storeKey', 'Store');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.total">Total:</label></td>
                            <td><input constraints="{min:0,space:2}" dojoType="dijit.form.NumberTextBox" id="proposal.total" name="total" style="width:7em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button dojoType="dijit.form.Button" id="proposal.updateButton" onclick="localModule.saveEntity('Proposal');">Update</button>
                            </td>
                        </tr>
                    </table>
                </form>
            </fieldset>
            <fieldset class="entityInformation" id="paymentInformationFieldset" style="float:left;margin:5px;">
                <legend>
                    Payment Information
                    <a href="javascript:dojo.query('#paymentInformation>table').style('display','none');">[--]</a> /
                    <a href="javascript:dojo.query('#paymentInformation>table').style('display','');">[+]</a>
                </legend>
                <form  dojoType="dijit.form.Form" id="paymentInformation">
                    <table>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="payment.key">Key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="payment.key" name="key" style="width:8em;" type="text" />
                                <button dojoType="dijit.form.Button" onclick="localModule.fetchEntity('payment.key', 'Payment');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="payment.creationDate">Creation date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="payment.creationDate" name="creationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="proposal.locationKey">Location key:</label></td>
                            <td>
                                <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberTextBox" id="payment.locationKey" name="locationKey" readonly="true" style="width:8em;" type="text" />
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.fetchEntity('payment.locationKey', 'Location');">Fetch</button>
                            </td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="payment.markedForDeletion" name="markedForDeletion" type="checkbox" /></td>
                            <td><label for="payment.markedForDeletion">Marked for deletion</label></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="payment.modificationDate">Modification date:</label></td>
                            <td><input dojoType="dijit.form.DateTextBox" id="payment.modificationDate" name="modificationDate" readonly="true" style="width:8em;" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="payment.authorizationId">Authorization identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.authorizationId" name="authorizationId" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="payment.reference">Reference:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.reference" name="reference" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="payment.requestId">Request identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.requestId" name="requestId" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="payment.transactionId">Transaction identifier:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.transactionId" name="transactionId" readonly="true" type="text" /></td>
                        </tr>
                        <tr>
                            <td style="text-align:right;padding-right:10px;"><label for="payment.status">Status:</label></td>
                            <td><input dojoType="dijit.form.TextBox" id="payment.status" name="status" readonly="true" type="text" /></td>
                        </tr>
                        <tr><td colspan="2" style="height:1px !important;background-color:lightgrey;"></td></tr>
                        <tr>
                            <td colspan="2" style="text-align:center;">
                                <button disabled="true" dojoType="dijit.form.Button" onclick="localModule.saveEntity('Payment');">Update</button>
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
    >
        <div id="keyZone"></div>
    </div>

    <div
        dojoType="dijit.Dialog"
        execute="localModule.getLocationKeys();"
        id="locationFilterDialog"
        title="Location filters"
    >
        <div class="dijitDialogPaneContentArea">
            <table id="locationAttributeTable">
                <tr>
                    <td style="text-align:right;padding-right:10px;"><label for="locationFilter.countryCode">Country code:</label></td>
                    <td><select dojoType="dijit.form.Select" id="locationFilter.countryCode"><option value="CA" selected="true">Canada</option><option value="US">United States of America</option></select></td>
                </tr>
                <tr>
                    <td style="text-align:right;padding-right:10px;"><input dojoType="dijit.form.CheckBox" id="locationFilter.hasStore" type="checkbox" /></td>
                    <td><label for="locationFilter.hasStore">Has store</label></td>
                </tr>
                <tr>
                    <td style="text-align:right;padding-right:10px;"><label for="locationFilter.latitude">Latitude:</label></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="locationFilter.latitude" type="text" value="-1000" /></td>
                </tr>
                <tr>
                    <td style="text-align:right;padding-right:10px;"><label for=locationFilter.longitude>Longitude:</label></td>
                    <td><input dojoType="dijit.form.NumberTextBox" id="locationFilter.longitude" type="text" value="-1000" /></td>
                </tr>
                <tr>
                    <td style="text-align:right;padding-right:10px;"><label for="locationFilter.postalCode">Postal code:</label></td>
                    <td><input dojoType="dijit.form.TextBox" id="locationFilter.postalCode" type="text" /></td>
                </tr>
                <tr>
                    <td style="text-align:right;padding-right:10px;"><label for="locationFilter.range">Range:</label></td>
                    <td>
                        <input dojoType="dijit.form.TextBox" id="locationFilter.range" style="width:4em;" type="text" value="100" />
                        <select dojoType="dijit.form.Select" id="locationFilter.rangeUnit"><option value="km" selected="true">km</option><option value="mi">miles</option></select>
                    </td>
                </tr>
            </table>
        </div>
        <div class="dijitDialogPaneActionBar">
            <button dojoType="dijit.form.Button" type="submit" id="ok">Get location keys</button>
            <button dojoType="dijit.form.Button" type="button" onClick="dijit.byId('locationFilterDialog').onCancel();">Cancel</button>
        </div>
    </div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require("dojo.data.ItemFileWriteStore");
        dojo.require("dojo.parser");
        dojo.require("dijit.Dialog");
        dojo.require("dijit.layout.BorderContainer");
        dojo.require("dijit.layout.ContentPane");
        dojo.require("dijit.form.Button");
        dojo.require("dijit.form.CheckBox");
        dojo.require("dijit.form.ComboBox");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.form.Form");
        dojo.require("dijit.form.NumberTextBox");
        dojo.require("dijit.form.Select");
        dojo.require("dijit.form.Textarea");
        dojo.require("dijit.form.TextBox");
        dojo.require("dojox.analytics.Urchin");
        dojo.addOnLoad(function(){
            dojo.parser.parse();
            dojo.fadeOut({
                node: "introFlash",
                delay: 50,
                onEnd: function() {
                    dojo.style("introFlash", "display", "none");
                }
            }).play();<%
            if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName())) { %>
            new dojox.analytics.Urchin({ acct: 'UA-11910037-2' });<%
            } %>
        });
    });
    </script>

    <script type="text/javascript">
    var localModule = new Object();
    localModule.fetchEntity = function(keyFieldId, entityName, ownerKey) {
        var key = dijit.byId(keyFieldId).get("value");
        if (isNaN(key)) {
            alert("The key in the field '" + keyFieldId + "' is not a number!");
            return;
        }
        if (entityName == "Consumer" || entityName == "SaleAssociate") {
            key = "current";
        }
        var ownrKey = dijit.byId("consumer.key").get("value");
        if (isNaN(ownrKey)) {
            alert("The key in the field 'consumer.key' is not a number!");
            return;
        }
        var pointOfView = "CONSUMER";
        if (entityName == "Proposal" || entityName == "Store") {
            pointOfView = "SALE_ASSOCIATE";
        }
        var prefix = entityName.toLowerCase();
        dojo.animateProperty({
            node: prefix + "InformationFieldset",
            properties: { backgroundColor: { end: "yellow" } }
        }).play();
        dojo.xhrGet({
            content: null,
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    dijit.byId(prefix + "Information").reset();
                    var resource = response.resource;
                    for (var attr in resource) {
                        var value = resource[attr];
                        if (attr.indexOf("Date") != -1) {
                            value = dojo.date.stamp.fromISOString(value);
                        }
                        if (attr == "criteria" || attr == "hashTags" || attr == "cc") {
                            value = value.join('\n');
                        }
                        if (attr == "proposalKeys" || attr == "saleAssociateKeys") {
                            var options = new dojo.data.ItemFileWriteStore({ data: { identifier: 'name', items: [] } })
                            var limit = value.length;
                            for (var idx = 0; idx < limit; idx++) {
                                options.newItem({ name: value [idx] });
                            }
                            dijit.byId(prefix + "." + attr).set("store", options);
                            dijit.byId(prefix + "." + attr).set("value", value[0]);
                        }
                        else {
                            dijit.byId(prefix + "." + attr).set("value", value);
                        }
                        if (attr == "state" && (entityName == "Demand" || entityName == "Proposal")) {
                            var isNonModifiable = value == "closed" || value == "cancelled" || value == "markedForDeletion";
                            dijit.byId(prefix + ".updateButton").set("disabled", isNonModifiable);
                        }
                    }
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
                dojo.animateProperty({
                    node: prefix + "InformationFieldset",
                    properties: { backgroundColor: { end: "transparent" } }
                }).play();
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/shortcut/" + entityName + "/" + key + "?shortId=" + ownrKey + "&pointOfView=" + pointOfView
        });
    };
    localModule.saveEntity = function(entityName) {
        var prefix = entityName.toLowerCase();
        var key = dijit.byId(prefix + ".key").get("value");
        if (isNaN(key)) {
            alert("The key in the field '" + keyFieldId + "' is not a number!");
            return;
        }
        if (entityName == "Consumer" || entityName == "SaleAssociate") {
            key = "current";
        }
        var ownrKey = dijit.byId("consumer.key").get("value");
        if (isNaN(ownrKey)) {
            alert("The key in the field 'consumer.key' is not a number!");
            return;
        }
        dojo.animateProperty({
            node: prefix + "InformationFieldset",
            properties: { backgroundColor: { end: "yellow" } }
        }).play();
        var data = localModule.formToObject(prefix + "Information");
        if (data.cc != null) { data.cc = data.cc.split("\n"); }
        if (data.criteria != null) { data.criteria = data.criteria.split("\n"); }
        if (data.hashTags != null) { data.hashTags = data.hashTags.split("\n"); }
        if (data.proposalKeys != null) { delete data.proposalKeys; } // Neutralized server-side, just removed for the bandwidth
        if (data.saleAssociateKeys != null) { delete data.saleAssociateKeys; } // Neutralized server-side, just removed for the bandwidth
        dojo.xhrPut({
            headers: { "content-type": "application/json" },
            putData: dojo.toJson(data),
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    // No visual feedback
                }
                else {
                    alert(response.exceptionMessage+"\nurl: "+ioArgs.url+"\n\n"+response.originalExceptionMessage);
                }
                dojo.animateProperty({
                    node: prefix + "InformationFieldset",
                    properties: { backgroundColor: { end: "transparent" } }
                }).play();
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/shortcut/" + entityName + "/" + key + "?shortId=" + ownrKey
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
                    var _iv = item.get("value");
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
                "<a href='javascript:dijit.byId(\"entityKeysDialog\").hide();dijit.byId(\"", // entity class name, lower case
                ".key\").set(\"value\",",      // entity key
                ");localModule.fetchEntity(\"", // entity class name, lower case
                ".key\",\"",                    // entity class name
                "\");' title='Get the ",        // entity class name
                ": ",                           // entity key
                "'>",                           // entity key
                "</a>, "
            ];
    localModule.loadEntityKeys = function(keyFieldId, entityName, parameters) {
        var ownerKey = dijit.byId(keyFieldId).get("value");
        if (isNaN(ownerKey)) {
            alert("The key in the field '" + keyFieldId + "' is not a number!");
            return;
        }
        var dialog = dijit.byId("entityKeysDialog");
        dialog.set("title", entityName + " identifiers");
        dialog.show();

        dojo.animateProperty({
            node: "keyZone",
            properties: { backgroundColor: { end: "yellow" } }
        }).play();
        dojo.xhrGet({
            content: null,
            handleAs: "json",
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    var keys = response.resources;
                    var keyNb = keys.length;
                    var deco = localModule.decorationOfEntityLinks;
                    var out = [];
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
                    dojo.byId("keyZone").innerHTML = out.join("");
                }
                else {
                    alert(response.message+"\nurl: "+ioArgs.url);
                }
                dojo.animateProperty({
                    node: "keyZone",
                    properties: { backgroundColor: { end: "transparent" } }
                }).play();
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/shortcut/" + entityName + "?shortId=" + ownerKey + "&anyState=true&onlyKeys=true&" + parameters
        });
    };
    localModule.resolveLocation = function() {
        dojo.xhrGet({
            content: null,
            handleAs: "json",
            load: function(response, ioArgs) {
                localModule.fetchEntity('location.key', 'Location');
            },
            error: function(message, ioArgs) { alert(message+"\nurl: "+ioArgs.url); },
            url: "/API/maelzel/validateLocation?countryCode=" + dijit.byId("location.countryCode").get("value") + "&postalCode=" + dijit.byId("location.postalCode").get("value") + "&consumerKey=0&key=0"
        });
    };
    localModule.getLocationKeys = function() {
        var parameters =
            "maximumResults=0" +
            "&hasStore=" + (dijit.byId("locationFilter.hasStore").get("value") == "on") +
            "&countryCode=" + dijit.byId("locationFilter.countryCode").get("value") +
            "&range=" + dijit.byId("locationFilter.range").get("value") +
            "&rangeUnit=" + dijit.byId("locationFilter.rangeUnit").get("value");
        var postalCode = dijit.byId("locationFilter.postalCode").get("value");
        if (0 < postalCode.length) {
            parameters += "&postalCode=" + postalCode;
        }
        else {
            parameters += "&latitude=" + dijit.byId("locationFilter.latitude").get("value") + "&longitude=" + dijit.byId("locationFilter.longitude").get("value");
        }
        localModule.loadEntityKeys('consumer.key','Location', parameters);
    };
    </script>
</body>
</html>
