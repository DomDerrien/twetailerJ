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
    String appVersion = appSettings.getProductVersion();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    cdnBaseURL = "https://ajax.googleapis.com/ajax/libs/dojo/1.6"; // TODO: change at the application level

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);

    // Parameter reading
    String postalCode = request.getParameter("postalCode");
    String countryCode = LocaleValidator.checkCountryCode(request.getParameter("countryCode"));
    String rangeUnit = LocaleValidator.checkRangeUnit(request.getParameter("rangeUnit"));
    long range = 10;
    try {
        if (request.getParameter("range") != null) {
            range = Math.round(Double.valueOf(request.getParameter("range")));
        }
    }
    catch(NumberFormatException ex) {} // Just ignore the malformed value!
    boolean autoload = request.getParameter("autoload") != null;
    String serializedHashTags = request.getParameter("hashTags");
    boolean normalChrome = !"min".equals(request.getParameter("chrome"));
%><html dir="ltr" lang="<%= localeId %>">
<head>
    <meta http-equiv="X-UA-Compatible" content="chrome=1" />
    <title><%= LabelExtractor.get(ResourceFileId.third, "sm_localized_page_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=<%= StringUtils.HTML_UTF8_CHARSET %>">
    <meta http-equiv="content-language" content="<%= localeId %>" />
    <meta name="copyright" content="<%= LabelExtractor.get(ResourceFileId.master, "product_copyright", locale) %>" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <% if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/claro/claro.css";
        @import "<%= cdnBaseURL %>/dojox/layout/resources/ExpandoPane.css";
        @import "/css/console.css";
    </style><%
    }
    else { // elif (!useCDN)
    %><link href="/js/release/<%= appVersion %>/dojo/resources/dojo.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dijit/themes/claro/claro.css" rel="stylesheet" type="text/css" />
    <link href="/js/release/<%= appVersion %>/dojox/layout/resources/ExpandoPane.css" rel="stylesheet" type="text/css" />
    <link href="/css/console.css" rel="stylesheet" type="text/css" /><%
    } // endif (useCDN)
    %>
    <style type="text/css">
        .dojoxExpandoWrapper {
            border-left: 1px solid #CCC;
            border-right: 1px solid #CCC;
        }
        .claro .dojoxExpandoIcon {
            background-image: url('/images/spriteRoundedIconsSmall.gif');
        }
    </style>

</head>
<body class="claro">

    <% if (normalChrome) { %>
    <div id="introFlash">
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>
    <% } else { %>
    <div id="introFlash" style="top: 0; right: 0; bottom: 0; left: 0;">
        <div id="introFlashWait"><span><%= LabelExtractor.get(ResourceFileId.third, "console_splash_screen_message", locale) %></span></div>
    </div>
    <% } %>

    <%
    if (useCDN) {
    %><script
        data-dojo-config="parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', modulePaths: { dojo: '<%= cdnBaseURL %>/dojo', dijit: '<%= cdnBaseURL %>/dijit', dojox: '<%= cdnBaseURL %>/dojox', twetailer: '/js/twetailer', domderrien: '/js/domderrien' }, dojoBlankHtmlUrl: '/_includes/dojo_blank.html', locale: '<%= localeId %>'",
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
        src="/js/release/<%= appVersion %>/ase/stores.js"
        type="text/javascript"
    ></script><%
    } // endif (useCDN)
    %>

    <% if (normalChrome) { %>
    <div id="topContainer" data-dojo-type="dijit.layout.BorderContainer" data-dojo-props="gutters: false" style="height: 100%;">
        <jsp:include page="/_includes/banner_open.jsp">
            <jsp:param name="localeId" value="<%= localeId %>" />
        </jsp:include>
        <div data-dojo-type="dijit.layout.BorderContainer" id="centerZone" data-dojo-props="region: 'center'">
    <% } else { %>
        <div data-dojo-type="dijit.layout.BorderContainer" id="centerZone" data-dojo-props="region: 'center'" style="margin: 0; height: 100%; background-color: transparent;">
    <% } %>
            <div data-dojo-type="dojox.layout.ExpandoPane" id="formPane" data-dojo-props="splitter: true, region: 'left'" style="width: 260px; border-bottom: 1px solid #CCC !important; background-color: #FFF;" title="<%= LabelExtractor.get(ResourceFileId.third, "sm_commands_sectionTitle", locale) %>">
                <form data-dojo-type="dijit.form.Form" data-dojo-props="onSubmit: function() { localModule.loadMapForPostalCode(); return false;}" id="formEntity">
                <table>
                    <tr>
                        <td align="right"><label for="postalCode"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandPostalCode", locale) %></label></td>
                        <td>
                            <input
                                id="postalCode"
                                data-dojo-type="dijit.form.ValidationTextBox"
                                data-dojo-props="name: 'postalCode', invalidMessage: '<%= LabelExtractor.get(ResourceFileId.third, "location_postalCode_invalid_CA", locale) %>', placeHolder: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_default_CA", locale) %>', regExp: '<%= LabelExtractor.get(ResourceFileId.master, "location_postalCode_regExp_CA", locale) %>', required: true, style: 'width:6em;'"
                            />
                            <button
                                id="detectLocationButton"
                                data-dojo-type="dijit.form.Button"
                                data-dojo-props="iconClass: 'silkIcon silkIconGPS', showLabel: false, title: '<%= LabelExtractor.get(ResourceFileId.third, "core_cmenu_detectLocale", locale) %>', onClick: localModule.fetchLocation"
                                type="button"
                            ></button>
                        </td>
                    </tr>
                    <tr>
                        <td align="right"><label for="countryCode"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandCountryCode", locale) %></label></td>
                        <td>
                            <select
                                id="countryCode"
                                data-dojo-type="dijit.form.Select"
                                data-dojo-props="name: 'countryCode', onChange: localModule.updateValidationRule"
                            >
                                    <option value="CA" selected="selected"><%= LabelExtractor.get(ResourceFileId.master, "country_CA", locale) %></option>
                                    <option value="US"><%= LabelExtractor.get(ResourceFileId.master, "country_US", locale) %></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="right"><label for="range"><%= LabelExtractor.get(ResourceFileId.third, "core_demandForm_demandRange", locale) %></label></td>
                        <td>
                            <input data-dojo-type="dijit.form.NumberSpinner" data-dojo-props="constraints: {min:1,max:100,places:0}, required: true, name: 'range', style: 'width:4em;', value: 10" id="range" />
                            <select data-dojo-type="dijit.form.Select" id="rangeUnit" data-dojo-props="name: 'rangeUnit', required: true">
                                <option value="<%= LocaleValidator.KILOMETER_UNIT %>" selected="selected"><%= LocaleValidator.KILOMETER_UNIT %></option>
                                <option value="<%= LocaleValidator.MILE_UNIT %>"><%= LocaleValidator.MILE_UNIT %></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td>
                            <button
                                data-dojo-type="dijit.form.Button"
                                data-dojo-props="iconClass: 'silkIcon silkIconGMaps', type: 'submit'"
                                id="showMapButton"
                                type="button"
                            ><%= LabelExtractor.get(ResourceFileId.third, "shared_locale_view_map_link", locale) %></button>
                        </td>
                    </tr>
                </table>
                </form>
                <div style="display: none;">
                    <p style="border-top: 1px solid lightgrey;"><%= LabelExtractor.get(ResourceFileId.third, "sm_statistics_sectionTitle", locale) %></p>
                    <div id="statPane"></div>
                </div>
            </div>
            <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region: 'bottom'">
                <span style="font-size: larger;"><%= LabelExtractor.get(ResourceFileId.third, "sm_legend_title", locale) %></span>
                <img src="/images/mini_red_dot.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_referenced", locale) %> &mdash;
                <img src="/images/mini_black_dot.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_declined", locale) %> / <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_excluded", locale) %>&mdash;
                <img src="/images/mini_blue_pin.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_inProgress", locale) %> &mdash;
                <img src="/images/mini_orange_pin.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_waiting", locale) %> &mdash;
                <img src="/images/mini_green_pin.png" style="vertical-align: middle;" /> <%= LabelExtractor.get(ResourceFileId.third, "sm_legend_active", locale) %>.
            </div>
            <div data-dojo-type="dijit.layout.ContentPane" data-dojo-props="region: 'center'">
                <div id='mapPlaceHolder' style='width:100%;height:100%;'></div>
            </div>
    <% if (!normalChrome) { %>
        </div>
    <% } else { %>
        </div>
        <div data-dojo-type="dijit.layout.ContentPane" id="footerZone" data-dojo-props="region: 'bottom'">
            <%= LabelExtractor.get("product_rich_copyright", locale) %>
        </div>
    </div>
    <% } %>

    <div
       data-dojo-props="color: 'yellow', target: 'formPane'"
       data-dojo-type="dojox.widget.Standby"
       id="formPaneOverlay"
    ></div>

    <script type="text/javascript">
    dojo.addOnLoad(function(){
        dojo.require('dojo.parser');
        dojo.require('dijit.layout.BorderContainer');
        dojo.require('dijit.layout.ContentPane');
        dojo.require('dijit.form.Button');
        dojo.require('dijit.form.Form');
        dojo.require('dijit.form.NumberSpinner');
        dojo.require('dijit.form.Select');
        dojo.require('dijit.form.TimeTextBox');
        dojo.require('dijit.form.ValidationTextBox');
        dojo.require('dojox.analytics.Urchin');
        dojo.require('dojox.layout.ExpandoPane');
        dojo.require('dojox.widget.Standby');
        dojo.require('twetailer.Common');
        dojo.addOnLoad(function(){
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
    localModule._stores;
    localModule._markers;
    localModule._infoWindows;
    localModule._markerImages;
    localModule._lastInfoWindow = null;
    localModule._mapFetchedWithData = false;

    localModule.init = function() {
        localModule._getLabel = twetailer.Common.init('<%= localeId %>', null, 'detectLocationButton');
        localModule.getMarkerImage('shadow'); // To bootstrap the process with the shadow marker

        dojo.subscribe('mapReady', function(map) {
            dijit.byId('formPaneOverlay').show();
            var postalCode = dijit.byId('postalCode').get('value'), countryCode = dijit.byId('countryCode').get('value'), position = twetailer.Common.getCachedGeoCoordinates(postalCode, countryCode);
            dojo.xhrGet({
                headers: { 'content-type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                content: {
                    postalCode: postalCode,
                    countryCode: countryCode,
                    latitude: position.lat(),
                    longitude: position.lng(),
                    range: dijit.byId('range').get('value'),
                    rangeUnit: dijit.byId('rangeUnit').get('value'),
                    hasStore: true,
                    referralId: 0<%
                    if (serializedHashTags != null) { %>,
                    hashTags: ['<%= serializedHashTags.replaceAll(",", "','") %>']<% } %>
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
                            var store = stores[idx], color = 'red', zIndex = 199, needShadow = false;
                            switch(store.state) {
                            // case 'referenced': color = 'red'; break;
                            case 'declined': color = 'black'; de++; break;
                            case 'inProgress': color = 'blue'; ip++; zIndex += 20; needShadow = true; break;
                            case 'waiting': color = 'orange'; w++; zIndex += 30; needShadow = true; break;
                            case 'active': color = 'green'; a++; zIndex += 50; needShadow = true; break;
                            case 'excluded': color = 'black'; de++; break;
                            default: color = 'red';
                            }
                            var marker = new google.maps.Marker({
                                map: map,
                                icon: localModule.getMarkerImage(color),
                                position: new google.maps.LatLng(store.latitude, store.longitude),
                                shadow: needShadow ? localModule.getMarkerImage('shadow') : null,
                                animation: google.maps.Animation.DROP,
                                title: store.name,
                                zIndex: zIndex
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
                        localModule._mapFetchedWithData = true
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
            twetailer.Common.getCursorOnMapLocation(
                marker,
                'postalCode',
                'countryCode',
                'formPaneOverlay'
            );
            // https://maps-api-ssl.google.com/maps/api/geocode/json?latlng=40.714224,-73.961452&sensor=false
        });

        dijit.byId('countryCode').set('value', '<%= countryCode %>');
        <% if (postalCode != null) { %>dijit.byId('postalCode').set('value', '<%= postalCode %>');<% } %>
        dijit.byId('range').set('value', '<%= range %>');
        dijit.byId('rangeUnit').set('value', '<%= rangeUnit %>');
        <% if (autoload && postalCode != null) { %>setTimeout(function(){ dijit.byId('showMapButton').onClick(); }, 0);<% } %>

        var topContainer = dijit.byId('topContainer');
        if (topContainer) {
            topContainer.resize();
        }

        dijit.byId('postalCode').focus();
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
    localModule.fetchLocation = function() { twetailer.Common.fetchBrowserLocation('postalCode', 'countryCode', 'formPaneOverlay'); };
    localModule.updateValidationRule = function() { twetailer.Common.updatePostalCodeFieldConstraints(this.value, 'postalCode'); };
    localModule.loadMapForPostalCode = function() {
        localModule._mapFetchedWithData = false;
        if (dijit.byId('formEntity').validate()) {
            twetailer.Common.showMap(dijit.byId('postalCode').get('value'), dijit.byId('countryCode').get('value'), {zoom: 11, notification: 'mapReady', iconOnDragEnd: 'iconDragged'});
        }
    };
    </script>

    <script src="https://maps-api-ssl.google.com/maps/api/js?v=3&sensor=false&language=<%= localeId %>" type="text/javascript"></script>
</body>
</html>
