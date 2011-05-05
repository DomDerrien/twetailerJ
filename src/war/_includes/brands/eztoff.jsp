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
                <div style="position:absolute;top:10px;left:160px;z-index:10;font-size:8pt;padding:2px;color:white;font-weight:bold;-moz-border-radius:4px;border-radius:4px;background-color:red;border:2px solid orange;">beta</div>
                <h1>
                    <a
                        href="http://ezToff.com/"
                        title="<%= LabelExtractor.get(ResourceFileId.master, "golf_product_name", locale) %>"
                    ><img src="/images/golf/EZTOFF-logo.png" width="147" height="42" /></a>&nbsp;
                </h1>
                <span id="mantra"><%= LabelExtractor.get("product_mantra", locale) %></span>
