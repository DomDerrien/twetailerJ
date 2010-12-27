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
    import="domderrien.i18n.StringUtils"
    import="twetailer.dto.Consumer"
    import="twetailer.dto.Demand"
    import="twetailer.dto.HashTag.RegisteredHashTag"
    import="twetailer.dto.Location"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    String localeId = request.getParameter("lg");
    if (localeId == null) {
        localeId = LocaleValidator.DEFAULT_LANGUAGE;
    }
    Locale locale = LocaleValidator.getLocale(localeId);

    // Regular expression for e-mail address validation
    String emailRegExp = Consumer.EMAIL_REGEXP_VALIDATOR;

    // Parameter reading
    String postalCode = request.getParameter("postalCode");
    if (postalCode == null) {
        postalCode = "H3S 1A1";
    }
    String countryCode = LocaleValidator.checkCountryCode(request.getParameter("countryCode"));
    String countryLabel = LocaleValidator.getCountryLabel(countryCode, locale);
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <title><%= LabelExtractor.get(ResourceFileId.third, "golfConsu_localized_page_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>">
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <style type="text/css"><%
        if (useCDN) {
        %>
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";<%
        }
        else { // elif (!useCDN)
        %>
        @import "/js/dojo/dojo/resources/dojo.css";
        @import "/js/dojo/dijit/themes/tundra/tundra.css";<%
        } // endif (useCDN)
        %>
        @import "/css/widget.css";
        @import "/css/eztoff/widget.css";
        <jsp:include page="/_includes/widget_css_parameters.jsp" />
    </style>
</head>
<body class="tundra">
    <div id="introFlash">
        <!--[if lt IE 8]>
        <div id="incompatibleIEWarning" style='border: 1px solid #F7941D; background: #FEEFDA; text-align: center; clear: both; position: relative; margin-bottom: 10px; z-index:1000;'>
            <div style='margin: 0 auto; text-align: left; padding: 0; overflow: hidden; color: black;'>
                <div style='float: left; padding-left: 5px;'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-warning.jpg' alt='!'/></div>
                <div style='font-family: Arial, sans-serif; padding-top: 5px; padding-left: 5px;'>
                    <div style='font-size: 14px; font-weight: bold; margin-top: 12px;'><%= LabelExtractor.get(ResourceFileId.third, "login_call_to_ie6_users", locale) %></div>
                    <div style='font-size: 12px; margin-top: 6px; line-height: 12px;'><%= LabelExtractor.get(ResourceFileId.third, "login_info_to_ie6_users", locale) %></div>
                </div>
                <div style='float: right; padding-left: 5px; overflow: auto; height: 75px;'>
                    <a href='http://www.firefox.com' target='_blank'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-firefox.jpg' style='border: none;' alt='Mozilla Firefox'/></a>
                    <a href='http://www.microsoft.com/windows/internet-explorer/' target='_blank'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-ie8.jpg' style='border: none;' alt='Microsoft Internet Explorer'/></a>
                    <a href='http://www.apple.com/safari/download/' target='_blank'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-safari.jpg' style='border: none;' alt='Apple Safari'/></a>
                    <a href='http://www.google.com/chrome' target='_blank'><img src='http://www.ie6nomore.com/files/theme/ie6nomore-chrome.jpg' style='border: none;' alt='Google Chrome'/></a>
                </div>
            </div>
        </div>
        <![endif]-->
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "widget_splash_screen_message", locale) %></span></div>
        <div id="introFlashInfo"><%= LabelExtractor.get(ResourceFileId.third, "cw_redirection_information", locale) %></div>
    </div>

    <%
    if (useCDN) {
    %><script type="text/javascript">
    var djConfig = {
        parseOnLoad: false,
        isDebug: false,
        useXDomain: true,
        baseUrl: './',
        modulePaths: {
            dojo: '<%= cdnBaseURL %>/dojo',
            dijit: '<%= cdnBaseURL %>/dijit',
            dojox: '<%= cdnBaseURL %>/dojox',
            twetailer: '/js/twetailer',
            domderrien: '/js/domderrien'
        },
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

    <div class="dataZone" id="widgetZone">
    <div style="position:absolute;bottom:20px;right:10px;min-height:14px;z-index:10;font-size:8pt;padding:2px;color:white;font-weight:bold;-moz-border-radius:4px;border-radius:4px;background-color:red;border:2px solid orange;">beta</div>
    <div>
        <div id="pane1">
            <div class="progressBar">
                <div class="step active"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_1_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_2_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_3_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive goal"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_4_progressBarItem", locale) %></div>
            </div>
            <div dojoType="dijit.form.Form" id="form1" class="content" onsubmit="return false;">
                <div class="title"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_1_title", locale) %></div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tr class="firstRow oddRow" id="postalCodeRow" style="display:none;">
                        <td class="firstCell"><label for="postalCode"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_postalCode", locale) %></label></td>
                        <td>
                            <input
                                dojoType="dijit.form.ValidationTextBox"
                                id="postalCode"
                                name="postalCode"
                                placeholder="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_placeHolder_CA", locale) %>"
                                regExp="<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_regExp_CA", locale) %>"
                                required="true"
                                style="width:100%;"
                                trim="true"
                                type="text"
                            />
                        </td>
                        <td class="lastCell" style="width:20px;padding-right:0px !important;">
                            <button
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconGPS"
                                id="detectLocationButton"
                                onclick="twetailer.Common.fetchBrowserLocation('postalCode', 'countryCode', 'widgetOverlay');"
                                showLabel="false"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "core_cmenu_detectLocale", locale) %>"
                                type="button"
                            ></button>
                        </td>
                    </tr>
                    <tr class="evenRow" id="countryCodeRow" style="display:none;">
                        <td class="firstCell"><label for="countryCode"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_countryCode", locale) %></label></td>
                        <td class="lastCell" colspan="2">
                            <select
                                dojoType="dijit.form.Select"
                                id="countryCode"
                                hasDownArrow="true"
                                name="countryCode"
                                onchange="twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'postalCode');"
                                required="true"
                                style="width:100%;"
                            >
                                <option value="<%= Locale.CANADA.getCountry() %>"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                                <option value="<%= Locale.US.getCountry() %>"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                            </select>
                        </td>
                    </tr>
                    <tr class="firstRow oddRow">
                        <td class="firstCell"><label for="date"><%= LabelExtractor.get(ResourceFileId.third, "gw_label_dueDate_date", locale) %></label></td>
                        <td class="lastCell" colspan="2"><input constraints="{datePattern:'EEE, MMMM dd yyyy'}" dojoType="dijit.form.DateTextBox" id="date" name="date" required="true" style="width:100%;" type="text" /></td>
                    </tr>
                    <tr class="evenRow">
                        <td class="firstCell"><label for="time"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_dueDate_time", locale) %></label></td>
                        <td class="lastCell" colspan="2"><input constraints="{visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}" dojoType="dijit.form.TimeTextBox" id="time" name="time" required="true" style="width:100%;" type="text" value="T07:00"/></td>
                    </tr>
                    <tr class="oddRow">
                        <td class="firstCell"><label for="quantity"><%= LabelExtractor.get(ResourceFileId.third, "gw_label_quantity", locale) %></label></td>
                        <td class="lastCell" colspan="2" style="vertical-align:top;">
                            <input constraints="{min:1,places:0}" dojoType="dijit.form.NumberSpinner" id="quantity" name="quantity" style="width:5em;" required="true" type="text" value="4" />
                            <label for="quantity"><%= LabelExtractor.get(ResourceFileId.third, "gw_label_quantity_extra", locale) %></label>
                        </td>
                    </tr>
                    <tr class="lastRow evenRow">
                        <td class="firstCell"><label for="demoMode" style="font-style:italic;"><%= LabelExtractor.get(ResourceFileId.third, "demoMode_checkbox", locale) %></label></td>
                        <td class="lastCell" colspan="2" style="vertical-align:top;">
                            <input dojoType="dijit.form.CheckBox" id="demoMode" type="checkbox" />
                        </td>
                    </tr>
                </table>
                <div class="comment">
                    <%= LabelExtractor.get(ResourceFileId.third, "gw_label_range", locale) %>
                    <input constraints="{min:5,max:100,places:0}" dojoType="dijit.form.NumberSpinner" id="range" name="range" style="width:5em;" type="text" value="25" />
                    km.
                </div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                onclick="localModule.switchPane(1, 2);"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "gw_action_next_toStep2_hint", locale) %>"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "cw_action_next", locale) %></button>
                            <br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "gw_action_next_toStep2_hint", locale) %></span>
                        </td>
                    </tr>
                    <tr>
                        <td style="text-align:center;">
                            <a href="<%= ApplicationSettings.get().getProductWebsite() %>golf/?countryCode=<%= countryCode %>&postalCode=<%= postalCode %>&lang=<%= localeId %>" target="aseGolfPartsPage"><img src="http://maps.google.com/maps/api/staticmap?sensor=false&language=<%= localeId %>&format=png8&center=<%= postalCode %>,<%= countryLabel %>&zoom=7&size=96x96" style="vertical-align: middle; border: 2px solid #A5BEDA; border-color: #A5BEDA #A5BEDA #5C7590 #5C7590;" /></a>
                            <div connectId="storeMapOpener" dojoType="dijit.Tooltip" position="above"><%= LabelExtractor.get(ResourceFileId.third, "gw_open_storeMap", locale) %></div>
                            <br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "cdw_open_storeMap", locale) %></span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
        </div>
        <div id="pane2" style="display:none;">
            <div class="progressBar">
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_1_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step active"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_2_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_3_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive goal"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_4_progressBarItem", locale) %></div>
            </div>
            <div dojoType="dijit.form.Form" id="form2" class="content" onsubmit="return false;">
                <div class="title"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_2_title", locale) %></div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tr class="firstRow lastRow oddRow">
                        <td class="firstCell" style="vertical-align:top;"><label for="email0"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_owner_email", locale) %></label></td>
                        <td class="lastCell" style="text-align: right;">
                            <input
                                dojoType="dijit.form.ValidationTextBox"
                                id="email0"
                                invalidMessage="<%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_ccInvalidMessage", locale) %>"
                                name="email0"
                                placeHolder="<%= LabelExtractor.get(ResourceFileId.third, "shared_email_sample", locale) %>"
                                regExp="<%= emailRegExp %>"
                                required="true"
                                style="width:100%;"
                                trim="true"
                                type="text"
                            />
                        </td>
                    </tr>
                </table>
                <div class="comment"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_2_contextualInfo", locale) %></div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tbody id="friendList">
                        <tr class="firstRow lastRow oddRow" id="friendRow1">
                            <td class="firstCell"><label for="email1"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_cced_email", locale) %></label></td>
                            <td style="text-align: right;">
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
                            <td class="lastCell" style="width:20px;padding-left:0px !important">
                                <button
                                    dojoType="dijit.form.Button"
                                    iconClass="silkIcon silkIconAdd"
                                    id="friendButton1"
                                    onclick="twetailer.Common.manageFriendRow(1, '<%= LabelExtractor.get(ResourceFileId.third, "cw_label_cced_email", locale).replaceAll("\\'", "\\\\'") %>');"
                                    showLabel="false"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "add_ccInfo_button", locale) %>"
                                    type="button"
                                ></button>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div class="comment"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_2_contextualInfo_2", locale) %></div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(2, 1);"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_previous", locale) %></a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                onclick="localModule.switchPane(2, 3);"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "gw_action_next_toStep3_hint", locale) %>"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "cw_action_next", locale) %></button>
                            <br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "gw_action_next_toStep3_hint", locale) %></span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
        </div>
        <div id="pane3" style="display:none;">
            <div class="progressBar">
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_1_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_2_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step active"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_3_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive goal"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_4_progressBarItem", locale) %></div>
            </div>
            <div dojoType="dijit.form.Form" id="form3" onsubmit="return false;">
                <div class="title"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_3_title", locale) %></div>
                <div class="form" style="border: 1px solid black; margin-bottom: 10px;">
                    <div class="oddRow" style="padding: 5px 0 5px 20px; border-bottom: 1px solid black;">
                        <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberSpinner" id="pullCart" name="pullCart" style="width:6em;" type="text" value="0" />
                        <label for="pullCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_label_metadata_pullCart", locale) %></label>
                    </div>
                    <div class="evenRow" style="padding: 5px 0 5px 20px;">
                        <input constraints="{min:0,places:0}" dojoType="dijit.form.NumberSpinner" id="motorCart" name="motorCart" style="width:6em;" type="text" value="0" />
                        <label for="motorCart"><%= LabelExtractor.get(ResourceFileId.third, "gw_label_metadata_golfCart", locale) %></label>
                    </div>
                </div>
                <table cellpadding="0" cellspacing="0" border="0" width="100%" style="border: 0 none !important;">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(3, 2);"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_previous", locale) %></a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                iconClass="silkIcon silkIconAccept"
                                onclick="localModule.sendRequest();"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "cw_action_send_hint", locale) %>"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "cw_action_send", locale) %></button>
                            <br />
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_send_hint", locale) %></span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
        </div>
        <div id="pane4" style="display:none;">
            <div class="progressBar">
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_1_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_2_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_3_progressBarItem", locale) %></div> <div class="step transition">&nbsp;</div>
                <div class="step active goal"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_4_progressBarItem", locale) %></div>
            </div>
            <div class="content">
                <div class="title"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_4_title", locale) %></div>
                <div class="comment"><%= LabelExtractor.get(ResourceFileId.third, "gw_step_4_contextualInfo", locale) %></div>
                <div class="comment"></div>
                <table cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(4, 1);"><%= LabelExtractor.get(ResourceFileId.third, "gw_action_reset", locale) %></a></td>
                    </tr>
                </table>
            </div>
        </div>
            <div class="footer">
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
    </div></div>

    <div
       color="darkgreen"
       dojoType="dojox.widget.Standby"
       id="widgetOverlay"
       target="widgetZone"
       zIndex="1000"
    ></div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dojo.fx');
        dojo.require('dojo.fx.easing');
        dojo.require('dojo.parser');
        dojo.require('dijit.form.Button');
        dojo.require('dijit.form.CheckBox');
        dojo.require('dijit.form.DateTextBox');
        dojo.require('dijit.form.Form');
        dojo.require('dijit.form.NumberSpinner');
        // dojo.require('dijit.form.NumberTextBox');
        dojo.require("dijit.form.Select");
        dojo.require('dijit.form.Textarea');
        dojo.require('dijit.form.TextBox');
        dojo.require('dijit.form.TimeTextBox');
        dojo.require('dijit.form.ValidationTextBox');
        dojo.require('dojox.analytics.Urchin');
        dojo.require('dojox.widget.Standby');
        dojo.require('twetailer.Common');
        dojo.addOnLoad(function(){
            if (dojo.byId('incompatibleIEWarning') != null) {
                dojo.style('widgetZone', 'display', 'none');
                dojo.style('introFlashWait', 'display', 'none');
                dojo.style('introFlashInfo', 'display', 'none');
                dojo.style('introFlash', 'backgroundImage', 'none');
                return;
            }
            dojo.extend(dijit._TimePicker,{
                visibleRange: 'T02:00:00',
            });
            dojo.parser.parse();
            dojo.fadeOut({
                node: 'introFlash',
                delay: 50,
                onEnd: function() {
                    dojo.style('introFlash', 'display', 'none');
                }
            }).play();
            localModule.init();<%
            if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName()) && !"10.0.2.2".equals(request.getServerName())) { %>
            new dojox.analytics.Urchin({ acct: 'UA-11910037-2' });<%
            } %>
        });
    });

    var localModule = {};
    localModule._getLabel = null;
    localModule.init = function() {
        localModule._getLabel = twetailer.Common.init('<%= localeId %>', null, 'detectLocationButton');

        var yesterday = new Date();
        var tomorrow = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        tomorrow.setDate(tomorrow.getDate() + 1);
        var dateField = dijit.byId('date');
        dateField.set('value', tomorrow);
        dateField.constraints.min = yesterday; // ??? why is reported as an invalid date?

        <%
        if (postalCode == null || postalCode.length() == 0) {
        %>dojo.style('postalCodeRow', 'display', '');
        dijit.byId('postalCode').focus();
        dojo.style('countryCodeRow', 'display', '');
        <% }
        else {
        %>dijit.byId('postalCode').set('value', '<%= postalCode %>');
        dijit.byId('quantity').focus();
        <% }
        if (countryCode != null && 0 < countryCode.length()) {
        %>dijit.byId('countryCode').set('value', '<%= countryCode %>');<%
        } %>
    };
    localModule.switchPane = function(sourceIdx, targetIdx) {
        if (sourceIdx < targetIdx) {
            var form = dijit.byId('form' + sourceIdx);
            if (!form.validate()) {
                return;
            }
        }
        // create two animations
        var anim1 = dojo.fx.wipeOut({ node: 'pane' + sourceIdx, easing: dojo.fx.easing.expoOut });
        var anim2 = dojo.fx.wipeIn({ node: 'pane' + targetIdx, easing: dojo.fx.easing.expoIn });
        // and play them at the same moment
        dojo.fx.chain([anim1, anim2]).play();
        switch(targetIdx) {
        case 1: <% if (postalCode == null || postalCode.length() == 0) {
                    %>dijit.byId('postalCode').focus();<%
                }
                else {
                    %>dijit.byId('quantity').focus();<%
                } %> break;
        case 2: dijit.byId('email0').focus(); break;
        case 3: dijit.byId('pullCart').focus(); break;
        }
    };
    localModule.sendRequest = function() {
        // Last form validation
        var form = dijit.byId('form3');
        if (!form.validate()) {
            return;
        }
        // Request preparation
        <% String referralId = request.getParameter("referralId");
        %>var parameters = {
            referralId: '<%= referralId == null || referralId.length() == 0 ? '0' : referralId %>',
            <%= Consumer.LANGUAGE %>: '<%= localeId %>',
            <%= Consumer.EMAIL %>: dijit.byId('email0').get('value'),
            <%= Location.POSTAL_CODE %>: dijit.byId('postalCode').get('value'),
            <%= Location.COUNTRY_CODE %>: dijit.byId('countryCode').get('value'),
            <%= Demand.DUE_DATE %>: twetailer.Common.toISOString(dijit.byId('date').get('value'), dijit.byId('time').get('value')),
            <%= Demand.RANGE %>: dijit.byId('range').get('value'),
            <%= Demand.RANGE_UNIT %>: '<%= LocaleValidator.DEFAULT_RANGE_UNIT %>',
            <%= Demand.QUANTITY %>: dijit.byId('quantity').get('value'),
            <%= Demand.HASH_TAGS %>: ['<%= RegisteredHashTag.golf.toString() %>'],
            <%= Demand.META_DATA %>:'{\'pullCart\':' + dijit.byId('pullCart').get('value') + ',\'golfCart\':' + dijit.byId('motorCart').get('value') + '}'
        };
        if (dijit.byId('demoMode').get('value') !== false) {
            parameters.<%= Demand.HASH_TAGS %>.push('<%= RegisteredHashTag.demo %>');
        }
        var cc = twetailer.Common.getFriendCoordinates();
        if (0 < cc.length) {
            parameters.<%= Demand.CC %> = cc;
        }
        dijit.byId('widgetOverlay').show();
        var dfd = dojo.xhrPost({
            headers: { 'content-type': 'application/json; charset=UTF-8' },
            postData: dojo.toJson(parameters),
            handleAs: 'json',
            load: function(response, ioArgs) {
                if (response !== null && response.success) {
                    // Update pane content
                    var placeHolder = dojo.byId('sender.email');
                    if (placeHolder) {
                        placeHolder.innerHTML = ''; // Reset content
                        placeHolder.appendChild(dojo.doc.createTextNode(dijit.byId('email0').get('value'))); // Safe insert
                    }
                    // Switch to pane
                    localModule.switchPane(3, 4);
                }
                else {
                    alert(response.message+'\nurl: '+ioArgs.url);
                }
                dijit.byId('widgetOverlay').hide();
            },
            error: function(message, ioArgs) {
                dijit.byId('widgetOverlay').hide();
                twetailer.Common.handleError(message, ioArgs);
            },
            url: '/3rdParty/Demand'
        });
    };
    </script>

    <script src="http://maps.google.com/maps/api/js?sensor=false&language=<%= localeId %>" type="text/javascript"></script>
</body>
</html>
