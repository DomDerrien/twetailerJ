<!doctype html>
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.StringUtils"
    import="twetailer.validator.ApplicationSettings"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html>
<head>
    <title><%= LabelExtractor.get(ResourceFileId.third, "ui_application_name", locale) %></title>
    <meta name="google-site-verification" content="WY7P9S7-YK1ZBPjxlVz1h7kd0Ex1Sc74hcab8zXy1d4" />
    <meta http-equiv="content-type" content="text/html; charset=<%= StringUtils.HTML_UTF8_CHARSET %>">
    <link rel="icon" href="/favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <meta http-equiv="refresh" content="0; URL=/console/"/>
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
    <h3 style="text-align:center;"><%= LabelExtractor.get(ResourceFileId.third, "ui_page_redirection_message", new Object[] {"/console/"},locale) %></h3>
</body>
</html>
