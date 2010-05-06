<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Enumeration"
    import="java.util.Locale"
    import="java.util.ResourceBundle"
    import="java.net.URL"
    import="java.net.URLEncoder"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LocaleController"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.jsontools.JsonArray"
    import="domderrien.jsontools.JsonObject"
    import="domderrien.jsontools.JsonParser"
    import="domderrien.i18n.LocaleController"
    import="twetailer.j2ee.LoginServlet"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Locale detection
    Locale locale = LocaleController.getLocale(request);

    // Get the current page url
    String queryString = request.getQueryString();
    String fromPageURL = request.getRequestURI();
    int defaultPageIdx = fromPageURL.lastIndexOf("index.jsp");
    if (defaultPageIdx == fromPageURL.length() - "index.jsp".length()) {
        fromPageURL = fromPageURL.substring(0, defaultPageIdx);
    }
    if (queryString != null) {
        fromPageURL += "?" + queryString;
    }
    fromPageURL = URLEncoder.encode(fromPageURL, "UTF-8");

    // Get the parameters passed to this template
    boolean pageForAssociate = Boolean.valueOf(request.getParameter("pageForAssociate"));
    boolean isLoggedUserAssociate = Boolean.valueOf(request.getParameter("isLoggedUserAssociate"));
    String consumerName = request.getParameter("consumerName");
%>
        <div dojoType="dijit.layout.ContentPane" id="headerZone" region="top">
            <div id="brand">
                <h1>
                    <img
                        alt="<%= LabelExtractor.get("product_ascii_logo", locale) %>"
                        id="logo"
                        src="/images/logo/twitter-bird-and-cart-toLeft.png"
                        title="<%= LabelExtractor.get("product_name", locale) %> <%= LabelExtractor.get("product_ascii_logo", locale) %>"
                    />
                    <a
                        href="http://www.twetailer.com/"
                        title="<%= LabelExtractor.get("product_name", locale) %> <%= LabelExtractor.get("product_ascii_logo", locale) %>"
                    ><span class="bang">!</span><span class="tw">tw</span><span class="etailer">etailer</span></a>
                </h1>
                <span id="mantra"><%= LabelExtractor.get("product_mantra", locale) %></span>
            </div>
            <div id="navigation">
                <ul>
                    <!--  Normal order because they are left aligned -->
                    <li><a href="./" class="<%= pageForAssociate ? "" : "active" %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_consumer", locale) %></a></li>
                    <% if(isLoggedUserAssociate) {
                    %><li><a href="./associate.jsp" class="<%= pageForAssociate ? "active" : "" %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_sale_associate", locale) %></a></li><%
                    } %>
                    <!--  Reverse order because they are right aligned -->
                    <li class="subItem"><a href="javascript:dijit.byId('aboutPopup').show();" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %></a></li>
                    <li class="subItem"><a href="/logout?<%= LoginServlet.FROM_PAGE_URL_KEY %>=<%= fromPageURL %>" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_sign_out", locale) %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_sign_out", locale) %></a></li>
                    <li class="subItem" style="color: orange; font-weight: bold; padding: 0 20px;">
                        <a href="/console/#profile" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_username_editIt", locale) %>">
                            <span style="color:orange;"><%= LabelExtractor.get(ResourceFileId.third, "navigation_welcome_user", new Object[] { consumerName}, locale) %></span>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
