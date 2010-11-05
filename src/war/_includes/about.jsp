<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="java.net.URL"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
%><%
    // Locale detection
    Locale locale = LocaleController.getLocale(request);
%><html>
    <body>
       <%= LabelExtractor.get(ResourceFileId.third, "about_text", locale) %>
        --<br/>
        <img alt="<%= LabelExtractor.get(ResourceFileId.third, "about_powered_by_appengine", locale) %>" height="30" src="http://code.google.com/appengine/images/appengine-noborder-120x30.gif" width="120"/>
    </body>
</html>
