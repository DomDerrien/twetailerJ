<!doctype html>
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.StringUtils"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <title><%= LabelExtractor.get(ResourceFileId.third, "sm_localized_page_name", locale) %></title>
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
        @import "/css/console.css";
    </style>

</head>
<body class="tundra">

    <div id="introFlash">
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
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

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <jsp:include page="/_includes/banner_open.jsp">
            <jsp:param name="localeId" value="<%= localeId %>" />
        </jsp:include>
        <div dojoType="dijit.layout.BorderContainer" id="centerZone" region="center">
            <div dojoType="dijit.layout.AccordionContainer" splitter="true" style="width: 300px;" region="left">
                <div dojoType="dijit.layout.ContentPane" id="formPane" title="<%= LabelExtractor.get(ResourceFileId.third, "sm_commands_sectionTitle", locale) %>">
                    <table>
                        <tr>
                            <td align="right"><label for="postalCode"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandPostalCode", locale) %></label></td>
                            <td>
                                <input
                                    dojoType="dijit.form.ValidationTextBox"
                                    id="postalCode"
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
                                    onclick="twetailer.Common.fetchBrowserLocation('postalCode', 'countryCode', 'formPaneOverlay');"
                                    showLabel="false"
                                    title="<%= LabelExtractor.get(ResourceFileId.third, "core_cmenu_detectLocale", locale) %>"
                                    type="button"
                                ></button>
                            </td>
                        </tr>
                        <tr>
                            <td align="right"><label for="countryCode"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandCountryCode", locale) %></label></td>
                            <td>
                                <select
                                    dojoType="dijit.form.Select"
                                    id="countryCode"
                                    name="countryCode"
                                    onchange="twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'postalCode');"
                                    style=""
                                >
                                        <option value="CA" selected="true"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                                        <option value="US"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td align="right"><label for="range"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandRange", locale) %></label></td>
                            <td>
                                <input constraints="{min:1,max:50,places:0}" dojoType="dijit.form.NumberSpinner" id="range" name="range" required="true" style="width:7em;" type="text" value="10" />
                                <select dojoType="dijit.form.Select" id="rangeUnit" name="rangeUnit" required="true" style="width:5em;">
                                    <option value="<%= LocaleValidator.KILOMETER_UNIT %>" selected="true"><%= LocaleValidator.KILOMETER_UNIT %></option>
                                    <option value="<%= LocaleValidator.MILE_UNIT %>"><%= LocaleValidator.MILE_UNIT %></option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <button
                                    dojoType="dijit.form.Button"
                                    iconClass="silkIcon silkIconGMaps"
                                    onclick="twetailer.Common.showMap(dijit.byId('postalCode').get('value'), dijit.byId('countryCode').get('value'), {zoom: 11, notification: 'mapReady', _iconOnDragEnd: 'iconDragged'});"
                                    type="button"
                                ><%= LabelExtractor.get(ResourceFileId.third, "shared_locale_view_map_link", locale) %></button>
                            </td>
                        </tr>
                    </table>
                </div>
                <div dojoType="dijit.layout.ContentPane" title="<%= LabelExtractor.get(ResourceFileId.third, "sm_statistics_sectionTitle", locale) %>"><div id="statPane"></div></div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom">
                <span style="font-size: larger;"><%= LabelExtractor.get(ResourceFileId.third, "sm_legend_title", locale) %></span>
                <img src="/images/mini_red_dot.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_referenced", locale) %> &mdash;
                <img src="/images/mini_black_dot.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_declined", locale) %> / <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_excluded", locale) %>&mdash;
                <img src="/images/mini_blue_pin.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_inProgress", locale) %> &mdash;
                <img src="/images/mini_orange_pin.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_waiting", locale) %> &mdash;
                <img src="/images/mini_green_pin.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_active", locale) %>.
            </div>
            <div dojoType="dijit.layout.ContentPane" region="center">
                <div id='mapPlaceHolder' style='width:100%;height:100%;'></div>
            </div>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>

    <div
       color="yellow"
       dojoType="dojox.widget.Standby"
       id="formPaneOverlay"
       target="formPane"
    ></div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dojo.fx');
        dojo.require('dojo.fx.easing');
        dojo.require('dojo.parser');
        dojo.require("dijit.layout.AccordionContainer");
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        dojo.require('dijit.form.Button');
        dojo.require('dijit.form.CheckBox');
        dojo.require('dijit.form.DateTextBox');
        dojo.require("dijit.form.FilteringSelect");
        dojo.require('dijit.form.Form');
        dojo.require('dijit.form.NumberSpinner');
        // dojo.require('dijit.form.NumberTextBox');
        dojo.require('dijit.form.Textarea');
        dojo.require('dijit.form.TextBox');
        dojo.require('dijit.form.TimeTextBox');
        dojo.require('dijit.form.ValidationTextBox');
        dojo.require('dijit.TooltipDialog');
        dojo.require('dojox.analytics.Urchin');
        dojo.require('dojox.form.DropDownSelect');
        dojo.require('dojox.widget.Standby');
        dojo.require('twetailer.Common');
        dojo.addOnLoad(function(){
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
    localModule._stores;
    localModule._markers;
    localModule._infoWindows;
    localModule._markerImages;
    localModule._lastInfoWindow = null;

    localModule.init = function() {
        localModule._getLabel = twetailer.Common.init('<%= localeId %>', 'detectLocationButton');
        localModule.getMarkerImage('shadow'); // To bootstrap the process with the shadow marker

        dojo.subscribe('mapReady', function(map) {
            dijit.byId('formPaneOverlay').show();
            dojo.xhrGet({
                headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                content: {
                    postalCode: dijit.byId('postalCode').get('value'),
                    countryCode: dijit.byId('countryCode').get('value'),
                    range: dijit.byId('range').get('value'),
                    rangeUnit: dijit.byId('rangeUnit').get('value'),
                    referralId: 0
                },
                handleAs: 'json',
                load: function(response, ioArgs) {
                    if (response && response.success) {
                        // 1. Update the map zoom
                        var bounds = response.bounds;
                        map.fitBounds(new google.maps.LatLngBounds(new google.maps.LatLng(bounds.bottom, bounds.left), new google.maps.LatLng(bounds.top, bounds.right)));
                        // 2. Highlight the covered zone
                        new google.maps.Polygon({
                            map: map,
                            paths: [
                                new google.maps.LatLng(bounds.top, bounds.left),
                                new google.maps.LatLng(bounds.bottom, bounds.left),
                                new google.maps.LatLng(bounds.bottom, bounds.right),
                                new google.maps.LatLng(bounds.top, bounds.right)
                            ],
                            strokeColor: "#FF0000",
                            strokeOpacity: 0.6,
                            strokeWeight: 2,
                            fillColor: "#FF0000",
                            fillOpacity: 0.1
                        });
                        // 3. Place a marker per store
                        var stores = response.resources, a = 0, w = 0, ip = 0, de = 0;
                        localModule._stores = stores;
                        localModule._markers = [];
                        localModule._infoWindows = [];
                        for (var idx = 0, limit = stores.length; idx < limit; idx++) {
                            var store = stores[idx], color = 'red';
                            switch(store.state) {
                            // case 'referenced': color = 'red'; break;
                            case 'declined': color = 'black'; de++; break;
                            case 'inProgress': color = 'blue'; ip++; break;
                            case 'waiting': color = 'orange'; w++; break;
                            case 'active': color = 'green'; a++; break;
                            case 'excluded': color = 'black'; de++; break;
                            default: color = 'red';
                            }
                            var marker = new google.maps.Marker({
                                map: map,
                                icon: localModule.getMarkerImage(color),
                                position: new google.maps.LatLng(store.latitude, store.longitude),
                                shadow: localModule.getMarkerImage('shadow'),
                                title: store.name
                            });
                            google.maps.event.addListener(marker, 'click', localModule.getInfoWindowHandler(map, idx));
                            localModule._markers.push(marker);
                        }
                        // 4. Report the statistics
                        var params = {
                            total: stores.length,
                            active: a,
                            waiting: w,
                            inProgress: ip,
                            declinedExcluded: de
                        };
                        dojo.byId('statPane').innerHTML = '<div>' + localModule._getLabel('console', 'sm_statistics_sectionBody', params) + '</div>';
                        // 5. Restore the transparent background
                        dijit.byId('formPaneOverlay').hide();
                    }
                    else {
                        alert(response.message + '\nurl: '+ ioArgs.url);
                    }
                    return response;
                },
                error: function(message, ioArgs) {
                    twetailer.Common.handleError(message, ioArgs);
                },
                preventCache: true,
                url: '/3rdParty/Store'
            });
        });

        dojo.subscribe('iconDragged', function(mouseEvent, marker) {
            console.log(dojo.toJson(marker.getPosition()));
        });
    };
    localModule.getInfoWindowHandler = function(map, storeIdx) {
        return function(event) { localModule.showInfoWindow(map, storeIdx); }
    };
    localModule.showInfoWindow = function(map, storeIdx) {
        if (localModule._lastInfoWindow) {
            localModule._lastInfoWindow.close();
        }
        var store = localModule._stores[storeIdx];
        var marker = localModule._markers[storeIdx];
        var infoWindow = localModule._infoWindows[storeIdx];
        if (!infoWindow) {
            var msgId = store.state == 'active' ? 'sm_infoBubble_activeStore' : 'sm_infoBubble_publicStore';
            var params = {
                'store>name': store.name == null ? '' : store.name,
                'store>address': store.address == null ? '' : store.address,
                'store>phoneNb': store.phoneNb == null ? '' : store.phoneNb,
                'store>url': store.url == null ? '' : store.url
            };
            if (store.state == 'active') {
                params['store>memberDate'] = twetailer.Common.displayDateTime(store.creationDate);
                params['store>closedProposalNb'] = store.closedProposalNb;
                params['store>publishedProposalNb'] = store.publishedProposalNb;
                params['store>closedProposalPercentage'] = store.publishedProposalNb == 0 ? 0 : (store.closedProposalNb * 100 / store.publishedProposalNb);
            }
            console.log(dojo.toJson(params));
            console.log(localModule._getLabel('console', msgId));
            var content = '<div>' + localModule._getLabel('console', msgId, params) + '</div>';
            infoWindow = new google.maps.InfoWindow({
                content: content
            });
            localModule._infoWindows[storeIdx] = infoWindow;
        }
        infoWindow.open(map, marker);
        localModule._lastInfoWindow = infoWindow;
    };
    localModule.getMarkerImage = function(color) {
        var list = localModule._markerImages;
        if (list && list[color]) {
            return list[color];
        }
        var ext = '_pin.png', width = 12, height = 20, x = 3, y = 20;
        switch (color) {
        case 'red':
        case 'black':
            ext = '_dot.png';
            width = 14;
            height = 14;
            x = 6;
            y = 6;
            break;
        case 'shadow':
            width = 22;
            break;
        };
        var image = new google.maps.MarkerImage(
            '/images/mini_' + color + ext,
            new google.maps.Size(width, height),
            new google.maps.Point(0, 0),
            new google.maps.Point(x, y)
        );
        if (list == null) {
            localModule._markerImages = {};
        }
        localModule._markerImages[color] = image;
        return image;
    };
    </script>

    <script src="http://maps.google.com/maps/api/js?sensor=false&language=<%= localeId %>" type="text/javascript"></script>
</body>
</html>
