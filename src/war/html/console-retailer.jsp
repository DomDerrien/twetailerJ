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
%><%
    // Locale detection
    Locale locale = LocaleController.getLocale(request);
    String localeId = LocaleController.getLocaleId(request);
%><html>
<body class="tundra">

    <div
        dojoType="dijit.layout.TabContainer"
        region="center"
    >
        <div
            dojoType="dijit.layout.ContentPane"
            title="List received Demands"
        >
        </div>
        <div
            dojoType="dijit.layout.ContentPane"
            title="List your Proposals"
        >
        </div>
        <div
            dojoType="dijit.layout.ContentPane"
            title="Your Profile"
        >
        </div>
        <div
            dojoType="dijit.layout.ContentPane"
            title="Need Help?"
        >
        </div>
    </div>
</body>
</html>
