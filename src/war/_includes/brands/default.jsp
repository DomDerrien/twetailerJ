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
    import="twetailer.validator.ApplicationSettings"
%><%
// Locale detection
String localeId = request.getParameter("localeId");
Locale locale = LocaleController.getLocale(localeId);
%>
                <div style="position:absolute;top:50px;left:480px;z-index:10;font-size:8pt;padding:2px;color:white;font-weight:bold;-moz-border-radius:4px;border-radius:4px;background-color:red;border:2px solid orange;">beta</div>
                <h1>
                    <img
                        alt="<%= LabelExtractor.get("product_name", locale) %>"
                        id="logo"
                        src="/images/logo/network-48.jpg"
                        title="<%= LabelExtractor.get("product_name", locale) %>"
                    />
                    <a
                        href="<%= ApplicationSettings.get().getProductWebsite() %>"
                        title="<%= LabelExtractor.get("product_name", locale) %>"
                    ><%= LabelExtractor.get("product_name", locale) %></a>
                </h1>
