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
%><%
// Locale detection
String localeId = request.getParameter("localeId");
Locale locale = LocaleController.getLocale(localeId);
%>
                <h1>
                    <a
                        href="http://ezToff.com/"
                        title="<%= LabelExtractor.get(ResourceFileId.fourth, "golf_product_name", locale) %>"
                    ><img src="/images/golf/EZTOFF-logo.png" /></a>&nbsp;
                </h1>
                <span id="mantra"><%= LabelExtractor.get("product_mantra", locale) %></span>
