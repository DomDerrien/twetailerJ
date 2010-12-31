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
    <title><%= LabelExtractor.get(ResourceFileId.third, "coreConsu_localized_page_name", locale) %></title>
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
        <jsp:include page="/_includes/widget_css_parameters.jsp" />
        .dijitMenuItem { color: black; }
    </style>

</head>
<body class="tundra">
    <div style="position:absolute;z-index:10;top:30px;right:5px;font-size:8pt;color:orange !important;font-weight:bold;">beta</div>
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

    <div class="dataZone" id="widgetZone"><div id="widgetInnerZone">
        <div id="pane1">
            <div dojoType="dijit.form.Form" id="form1" class="content" onsubmit="return false;">
                <% boolean showBrand = request.getParameter("hideBrand") == null;
                String brand = request.getParameter("brand");
                if (brand == null) {
                    brand = LabelExtractor.get(ResourceFileId.third, "cw_brand", locale);
                }
                if (showBrand) {
                %><div class="brand"><%= brand %></div><% }
                %><div class="title"><%= LabelExtractor.get(ResourceFileId.third, "cdw_step_1_title", locale) %></div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tr class="firstRow oddRow">
                        <td class="firstCell"><label for="metadata.make"><%= LabelExtractor.get(ResourceFileId.third, "cdw_label_make", locale) %></label></td>
                        <td class="lastCell"><%
                            String carMaker = request.getParameter("metadata.make");
                            if (carMaker == null) {
                                carMaker = "";
                            }
                            String[] carMakers = LabelExtractor.get(ResourceFileId.third, "cdw_carMakers", locale).split(",");
                            %>
                            <select class="selectbox" dojoType="dijit.form.FilteringSelect" id="metadata.make" name="metadata.make" style="width:auto;"><%
                            for (int i=0, carMakerNb = carMakers.length; i < carMakerNb; i++) { %>
                                <option value="<%= carMakers[i] %>"<% if (carMakers[i].equals(carMaker)) { out.write(" selected=true"); } %>><%= carMakers[i] %></option><%
                            } %></select>
                        </td>
                    </tr>
                    <tr class="evenRow"><%
                        String model = request.getParameter("metadata.model");
                        if (model == null) {
                            model = "";
                        }
                        String modelPlaceHolder = request.getParameter("metadata.model.placeHolder");
                        if (modelPlaceHolder == null) {
                            modelPlaceHolder = LabelExtractor.get(ResourceFileId.third, "cdw_placeholder_model", locale);
                        }
                        %>
                        <td class="firstCell"><label for="metadata.model"><%= LabelExtractor.get(ResourceFileId.third, "cdw_label_model", locale) %></label></td>
                        <td class="lastCell"><input dojoType="dijit.form.TextBox" id="metadata.model" name="metadata.model" placeholder="<%= modelPlaceHolder %>" style="width:100%;" type="text" value="<%= model %>" /></td>
                    </tr>
                    <tr class="oddRow"><%
                        String transmission = request.getParameter("metadata.transmission");
                        if (transmission == null) {
                          transmission = "";
                        }
                        %>
                        <td class="firstCell"><label for="metadata.transmission"><%= LabelExtractor.get(ResourceFileId.third, "cdw_label_transmission", locale) %></label></td>
                        <td class="lastCell"><input dojoType="dijit.form.TextBox" id="metadata.transmission" name="metadata.transmission" placeholder="<%= LabelExtractor.get(ResourceFileId.third, "cdw_placeholder_transmission", locale) %>" style="width:100%;" type="text" value="<%= transmission %>" /></td>
                    </tr>
                    <tr class="lastRow evenRow">
                        <td class="firstCell" style="vertical-align:top; padding-top:7px;"><label for="tags"><%= LabelExtractor.get(ResourceFileId.third, "cdw_label_criteria", locale) %></label></td>
                        <td class="lastCell">
                            <textarea
                                dojoType="dijit.form.Textarea"
                                id="tags"
                                name="tags"
                                placeholder="3 doors"
                                rows="3"
                                style="width:100%;min-height:48px;"
                            ><%
                            String criteria = request.getParameter("criteria");
                            if (criteria != null) {
                                out.write(criteria);
                            }
                            %></textarea><br/>
                            <div
                                iconClass="silkIcon silkIconHelp"
                                dojoType="dijit.form.DropDownButton"
                                style="float:right;margin-right:-1px;"
                            >
                                <span><%= LabelExtractor.get(ResourceFileId.third, "cw_helper_title", locale) %></span>
                                <div
                                    dojoType="dijit.TooltipDialog"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "cw_helper_title", locale) %>"
                                ><%= LabelExtractor.get(ResourceFileId.third, "cdw_helper_text", locale) %></div>
                            </div>
                        </td>
                    </tr>
                </table>
                <div class="comment"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_contextualInfo", locale) %></div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                id="next1"
                                onclick="localModule.switchPane(1, 2);"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "cw_action_next_toStep2_hint", locale) %>"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "cw_action_next", locale) %></button>
                            <br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_next_toStep2_hint", locale) %></span>
                        </td>
                    </tr>
                    <tr>
                        <td style="text-align:center;">
                            <a href="<%= ApplicationSettings.get().getProductWebsite() %>cardealer/?countryCode=<%= countryCode %>&postalCode=<%= postalCode %>&lang=<%= localeId %>#map" target="aseCarDealerPage"><img src="http://maps.google.com/maps/api/staticmap?sensor=false&language=<%= localeId %>&format=png8&center=<%= postalCode %>,<%= countryLabel %>&zoom=7&size=96x96" style="vertical-align: middle; border: 2px solid #A5BEDA; border-color: #A5BEDA #A5BEDA #5C7590 #5C7590;" /></a>
                            <div connectId="storeMapOpener" dojoType="dijit.Tooltip" position="above"><%= LabelExtractor.get(ResourceFileId.third, "cdw_open_storeMap", locale) %></div>
                            <br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "cdw_open_storeMap", locale) %></span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step active"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_2_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_3_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_4_progressBarItem", locale) %></div>
                </div>
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
        </div>
        <div id="pane2" style="display:none;">
            <div dojoType="dijit.form.Form" id="form2" class="content" onsubmit="return false;">
                <% if (showBrand) {
                %><div class="brand"><%= brand %></div><% }
                %><div class="title"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_2_title", locale) %></div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tr class="firstRow oddRow">
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
                    <tr class="evenRow">
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
                    <tr class="lastRow oddRow">
                        <td class="firstCell"><label for="range"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_range", locale) %></label></td>
                        <td class="lastCell" colspan="2">
                            <input constraints="{min:5,max:100,places:0}" dojoType="dijit.form.NumberSpinner" id="range" name="range" style="width:5em;" type="text" value="25" />
                        </td>
                    </tr>
                </table>
                <div class="comment"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_contextualInfo", locale) %></div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(2, 1);"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_previous", locale) %></a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                id="next2"
                                onclick="localModule.switchPane(2, 3);"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "cw_action_next_toStep3_hint", locale) %>"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "cw_action_next", locale) %></button>
                            <br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_next_toStep3_hint", locale) %></span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_progressBarItem", locale) %></div>
                    <div class="step active"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_2_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_3_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_4_progressBarItem", locale) %></div>
                </div>
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
        </div>
        <div id="pane3" style="display:none;">
            <div dojoType="dijit.form.Form" id="form3" class="content" onsubmit="return false;">
                <% if (showBrand) {
                %><div class="brand"><%= brand %></div><% }
                %><div class="title"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_3_title", locale) %></div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tr class="firstRow lastRow oddRow">
                        <td class="firstCell"><label for="date"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_dueDate_date", locale) %></label></td>
                        <td class="lastCell"><input constraints="{datePattern:'EEE, MMMM dd yyyy'}" dojoType="dijit.form.DateTextBox" id="date" name="date" required="true" style="width:100%;" type="text" /></td>
                    </tr>
                    <!--
                    <tr class="evenRow">
                        <td class="firstCell"><label for="time"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_dueDate_time", locale) %></label></td>
                        <td class="lastCell"><input constraints="{visibleIncrement:'T00:30:00',visibleRange:'T02:00:00'}" dojoType="dijit.form.TimeTextBox" id="time" name="time" required="true" style="width:100%;" type="text" value="T00:00" /></td>
                    </tr>
                    -->
                </table>
                <div class="comment"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_contextualInfo", locale) %></div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(3, 2);"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_previous", locale) %></a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                id="next3"
                                onclick="localModule.switchPane(3, 4);"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "cw_action_next_toStep4_hint", locale) %>"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "cw_action_next", locale) %></button>
                            <br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_next_toStep4_hint", locale) %></span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_2_progressBarItem", locale) %></div>
                    <div class="step active"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_3_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_4_progressBarItem", locale) %></div>
                </div>
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
        </div>
        <div id="pane4" style="display:none;">
            <div dojoType="dijit.form.Form" id="form4" class="content" onsubmit="return false;">
                <% if (showBrand) {
                %><div class="brand"><%= brand %></div><% }
                %><div class="title"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_4_title", locale) %></div>
                <table cellpadding="0" cellspacing="0" class="form">
                    <tbody id="friendList">
                        <tr class="firstRow oddRow" id="friendRow0">
                            <td class="firstCell" style="vertical-align:top;"><label for="email0"><%= LabelExtractor.get(ResourceFileId.third, "cw_label_owner_email", locale) %></label></td>
                            <td class="lastCell" style="text-align: right;" colspan="2">
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
                        <tr class="lastRow evenRow" id="friendRow1">
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
                            <td class="lastCell" style="width:20px;padding-right:0px !important;">
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
                <div class="comment"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_contextualInfo", locale) %></div>
                <table cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align:left;"><a href="#" onclick="localModule.switchPane(4, 3);"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_previous", locale) %></a></td>
                        <td style="text-align:right;">
                            <button
                                dojoType="dijit.form.Button"
                                id="next4"
                                onclick="localModule.sendRequest();"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "cw_action_send_hint", locale) %>"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "cw_action_send", locale) %></button>
                            <br/>
                            <span class="hint"><%= LabelExtractor.get(ResourceFileId.third, "cw_action_send_hint", locale) %></span>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_2_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_3_progressBarItem", locale) %></div>
                    <div class="step active"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_4_progressBarItem", locale) %></div>
                </div>
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
        </div>
        <div id="pane5" style="display:none;">
            <div class="content">
                <% if (showBrand) {
                %><div class="brand"><%= brand %></div><% }
                %><div class="title"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_5_title", locale) %></div>
                <div class="comment"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_5_contextualInfo", new Object[] { brand }, locale) %></div>
                <table cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td style="text-align:center;">
                            <button
                                dojoType="dijit.form.Button"
                                onclick="localModule.switchPane(5, 1);"
                                style="color:black;"
                                title="<%= LabelExtractor.get(ResourceFileId.third, "cw_action_reset_hint", locale) %>"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "cw_action_reset", locale) %></button>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="footer">
                <div class="progressBar">
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_1_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_2_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_3_progressBarItem", locale) %></div>
                    <div class="step inactive"><%= LabelExtractor.get(ResourceFileId.third, "cw_step_4_progressBarItem", locale) %></div>
                </div>
                <div class="poweredBy"><%= LabelExtractor.get(ResourceFileId.third, "cw_poweredBy", locale) %></div>
            </div>
        </div>
    </div></div>

    <div
       color="darkgrey"
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
        dojo.require("dijit.form.FilteringSelect");
        dojo.require('dijit.form.Form');
        dojo.require('dijit.form.NumberSpinner');
        // dojo.require('dijit.form.NumberTextBox');
        dojo.require("dijit.form.Select");
        dojo.require('dijit.form.Textarea');
        dojo.require('dijit.form.TextBox');
        dojo.require('dijit.form.TimeTextBox');
        dojo.require('dijit.form.ValidationTextBox');
        dojo.require('dijit.TooltipDialog');
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
                delay: 2000,
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
        var inOneMonth = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        inOneMonth.setMonth(inOneMonth.getMonth() + 1);
        var dateField = dijit.byId('date');
        dateField.set('value', inOneMonth);
        dateField.constraints.min = yesterday; // ??? why is reported as an invalid date?

        // dijit.byId('tags').focus();

        <%
        if (postalCode != null && 0 < postalCode.length()) {
        %>dijit.byId('postalCode').set('value', '<%= postalCode %>');
        <% }
        if (countryCode != null && 0 < countryCode.length()) {
        %>dijit.byId('countryCode').set('value', '<%= countryCode %>');<%
        } %>
    };
    localModule.controlTagField = function() {
        var tagField = dijit.byId('tags');
        var criteria = dojo.trim(tagField.get('value'));
        if (0 < criteria.length || 0 < dojo.trim(dijit.byId('metadata.model').get('value')).length) {
            return true;
        }
        var ttId = 'tooltipId';
        var tooltip = dijit.byId(ttId);
        if (!tooltip) {
            tooltip = new dijit.Tooltip({
                id: ttId,
                connectId: ['tags'],
                label: localModule._getLabel('console', 'cw_alert_tag_missing'),
                position: ['below', 'above'],
                showDelay: 0
            });
        }
        dojo.style(tooltip.domNode, 'visibility', 'visible');
        dojo.addClass(tagField.domNode, 'dijitError');
        var handle = dojo.connect(
            tagField,
            'onKeyPress',
            null, // no context required
            function() {
                var tooltip = dijit.byId(ttId);
                if (tooltip) {
                    tooltip.destroy();
                }
                dojo.removeClass(tagField.domNode, 'dijitError');
                dojo.disconnect(handle);
            }
        );
        tagField.focus();
        return false;
    };
    localModule.switchPane = function(sourceIdx, targetIdx) {
        if (sourceIdx < targetIdx) {
            var form = dijit.byId('form' + sourceIdx);
            if (form != null) {
                if (!form.validate()) {
                    return false;
                }
                if (sourceIdx == 1 && !localModule.controlTagField()) {
                    return false;
                }
            }
        }
        // create two animations
        var anim1 = dojo.fx.wipeOut({ node: 'pane' + sourceIdx, easing: dojo.fx.easing.expoOut });
        var anim2 = dojo.fx.wipeIn({ node: 'pane' + targetIdx, easing: dojo.fx.easing.expoIn });
        // and play them at the same moment
        dojo.fx.chain([anim1, anim2]).play();
        switch(targetIdx) {
        case 1: dijit.byId('tags').focus(); break;
        case 2: dijit.byId('postalCode').focus(); break;
        case 3: dijit.byId('next3').focus(); break;
        case 4: dijit.byId('email0').focus(); break;
        }
        return false;
    };
    localModule.sendRequest = function() {
        // Last form validation
        var form = dijit.byId('form4');
        if (!form.validate()) {
            return;
        }
        // Request preparation
        var criteria = [];
        if (0 < dojo.trim(dijit.byId('metadata.make').get('value')).length) {
            criteria.push('<%= LabelExtractor.get(ResourceFileId.third, "cdw_label_make", locale) %>');
            criteria.push(dojo.trim(dijit.byId('metadata.make').get('value')));
        }
        if (0 < dojo.trim(dijit.byId('metadata.model').get('value')).length) {
            criteria.push('<%= LabelExtractor.get(ResourceFileId.third, "cdw_label_model", locale) %>');
            criteria.push(dojo.trim(dijit.byId('metadata.model').get('value')));
        }
        if (0 < dojo.trim(dijit.byId('metadata.transmission').get('value')).length) {
            criteria.push('<%= LabelExtractor.get(ResourceFileId.third, "cdw_label_transmission", locale) %>');
            criteria.push(dojo.trim(dijit.byId('metadata.transmission').get('value')));
        }
        if (0 < dojo.trim(dijit.byId('tags').get('value')).length) {
            criteria = criteria.concat(dojo.trim(dijit.byId('tags').get('value')).split(/\s+/));
        }
        <% String referralId = request.getParameter("referralId");
        %>var parameters = {
            referralId: '<%= referralId == null || referralId.length() == 0 ? '0' : referralId %>',
            <%= Consumer.LANGUAGE %>: '<%= localeId %>',
            <%= Consumer.EMAIL %>: dijit.byId('email0').get('value'),
            <%= Location.POSTAL_CODE %>: dijit.byId('postalCode').get('value'),
            <%= Location.COUNTRY_CODE %>: dijit.byId('countryCode').get('value'),
            <%= Demand.DUE_DATE %>: twetailer.Common.toISOString(dijit.byId('date').get('value'), null), // dijit.byId('time').get('value')),
            <%= Demand.RANGE %>: dijit.byId('range').get('value'),
            <%= Demand.RANGE_UNIT %>: '<%= LocaleValidator.DEFAULT_RANGE_UNIT %>',
            <%= Demand.QUANTITY %>: 1, // Fixed to 1
            <%= Demand.HASH_TAGS %>: ['<%= RegisteredHashTag.cardealer.toString() %>'],
            // <%= Demand.META_DATA %>: '{}', // No metadata to communicate
            <%= Demand.CRITERIA %>: criteria
        };
        var cc = twetailer.Common.getFriendCoordinates();
        if (0 < cc.length) {
            parameters.<%= Demand.CC %> = cc;
        }
        dijit.byId('widgetOverlay').show();
        var dfd = dojo.xhrPost({
            headers: { 'content-type': 'application/json; charset=<%= StringUtils.HTML_UTF8_CHARSET %>' },
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
                    localModule.switchPane(4, 5);
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
