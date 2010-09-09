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
                    <img
                        alt="<%= LabelExtractor.get("product_name", locale) %>"
                        id="logo"
                        src="/images/logo/twitter-bird-and-cart-toLeft.png"
                        title="<%= LabelExtractor.get("product_name", locale) %>"
                    />
                    <a
                        href="http://anothersocialeconomy.com/"
                        title="<%= LabelExtractor.get("product_name", locale) %>"
                    ><span class="bang">!</span><span class="tw">tw</span><span class="etailer">etailer</span></a>
                </h1>
                <span id="mantra"><%= LabelExtractor.get("product_mantra", locale) %></span>
