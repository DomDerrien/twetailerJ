<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LocaleController"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html>
<head>
    <title><%= LabelExtractor.get("ui_application_name", locale) %></title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF8">
    <link rel="shortcut icon" href="/images/logo/favicon.ico" />
    <link rel="icon" href="/images/logo/favicon.ico" type="image/x-icon"/>
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
</head>
<body class="tundra">

    <div id="introFlash" style="display:none;">
        <div><span><%= LabelExtractor.get("ui_splash_screen_message", locale) %></span></div>
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
    <script type="text/javascript">
    /*
    dojo.require("dijit.layout.AccordionContainer");
    dojo.require("dijit.layout.TabContainer");
    dojo.require("dojox.grid.DataGrid");
    dojo.require("dojox.grid.cells");
    dojo.require("dijit.form.Form");
    dojo.require("dijit.form.Button");
    dojo.require("dijit.form.DateTextBox");
    dojo.require("dijit.form.TimeTextBox");
    dojo.require("dijit.form.NumberTextBox");
    dojo.require("dijit.form.NumberSpinner");
    dojo.require("dijit.form.CurrencyTextBox");
    dojo.require("dijit.form.ValidationTextBox");
    dojo.require("dijit.form.FilteringSelect");
    dojo.require("dijit.form.CheckBox");
    dojo.require("dijit.form.MultiSelect");
    dojo.require("dijit.form.Textarea");
    // dojo.require("dijit.Editor");
    */
    dojo.require("dijit.Dialog");
    dojo.require("dijit.layout.BorderContainer");
    dojo.require("dijit.layout.ContentPane");
    dojo.require("dijit.layout.TabContainer");
    dojo.require("dijit.form.Form");
    dojo.require("dijit.form.Button");
    dojo.require("dijit.form.TextBox");
    dojo.require("twetailer.Console");
    dojo.require("domderrien.i18n.LanguageSelector");
    dojo.require("dojo.parser");
    dojo.addOnLoad(function(){
        dojo.parser.parse();
        var uiTMXName = "master"; // @rwa.masterTMXfilename@
        var userLocale = "en"; // "<%= localeId %>"
        twetailer.Console.init(uiTMXName, userLocale);
        dojo.fadeOut({
            node: "introFlash",
            delay: 50,
            onEnd: function() {
                dojo.style("introFlash", "display", "none");
            }
        }).play();
    });
    </script>

    <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="false" style="height: 100%;">
        <div dojoType="dijit.layout.ContentPane" id="headerZone" region="top">
            <div id="brand">
                <h1>
                    <img id="logo" src="/images/logo/logo-48x48.png" alt="<('v')>" title="Twetailer logo" />
                    <a href="http://www.twetailer.com/">Twetailer:</a>
                </h1>
                <span id="mantra">Another Social Economy</span>
            </div>
            <div id="navigation">
                <ul>
                    <!--  Normal order because they are left aligned -->
                    <li><a href="javascript:twetailer.Console.showModule('consumer');">Consumer</a></li>
                    <li><a href="javascript:twetailer.Console.showModule('retailer');">Retailer</a></li>
                    <!--  Reverse order because they are right aligned -->
                    <li class="subItem"><a href="javascript:dijit.byId('aboutPopup').show();" title="About Twetailer">About</a></li>
                    <li class="subItem"><a href="/control/logout" title="Release your authentified session">Sign Out</a></li>
                </ul>
            </div>
        </div>
        <div dojoType="dijit.layout.ContentPane" id="centerZone" region="center">
            <!-- Place holder for the console content -->
        </div>
        <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
            Copyright &copy; 2009 <a href="http://www.milstein-assoc.com/">Milstein & associates inc.</a>
        </div>
    </div>

    <div
        dojoType="dijit.Dialog"
        id="aboutPopup"
        title="About"
        _href="about.jsp"
    >
        About box: To be completed.<br/>
        <br/>
        --<br/>
        <img alt="Powered by Google App Engine" height="30" src="http://code.google.com/appengine/images/appengine-noborder-120x30.gif" width="120"/>
    </div>
</body>
</html>
