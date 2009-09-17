<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="org.domderrien.i18n.LabelExtractor"
    import="org.domderrien.i18n.LocaleController"
%><%
    // Application settings
    ResourceBundle appSettings = ResourceBundle.getBundle("applicationSettings", Locale.ROOT);
    boolean useCDN = "y".equals(appSettings.getString("useCDN"));
    String cdnBaseURL = appSettings.getString("cdnBaseURL");
    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html>
<head>
    <title><%= LabelExtractor.get("applicationName", locale) %></title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link rel="icon" href="/images/favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="/images/favicon.ico" />
    <meta http-equiv="refresh" content="0; URL=/html/console.jsp"/>
    <%
    if (useCDN) {
    %><style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";
        @import "/css/console.css";
    </style><%
    }
    else { // if (!useCDN)
    %><style type="text/css">
        @import "/js/dojo/resources/dojo.css";
        @import "/js/dijit/themes/tundra/tundra.css";
        @import "/css/console.css";
    </style><%
    } // endif (useCDN)
    %>
</head>
<body class="tundra">
    <h3 style="text-align:center;"><%= LabelExtractor.get("redirectionMsg", new Object[] {"/html/console.jsp"},locale) %></h3>
</body>
</html>
