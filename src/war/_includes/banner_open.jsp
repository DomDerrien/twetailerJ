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
    import="domderrien.jsontools.JsonArray"
    import="domderrien.jsontools.JsonObject"
    import="domderrien.jsontools.JsonParser"
    import="domderrien.i18n.LocaleController"
    import="twetailer.dto.HashTag"
    import="twetailer.dto.HashTag.RegisteredHashTag"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Locale detection
    String localeId = request.getParameter("localeId");
    Locale locale = LocaleController.getLocale(localeId);

    String verticalId = request.getParameter("verticalId");
%>
        <div dojoType="dijit.layout.ContentPane" id="headerZone" region="top">
            <div id="brand"><%
                if (verticalId == null || verticalId.length() == 0) {
                    %><jsp:include page="/_includes/brands/default.jsp"><jsp:param name="localeId" value="<%= localeId %>" /></jsp:include><%
                }
                else if (RegisteredHashTag.golf.toString().equals(HashTag.getSupportedHashTag(verticalId))) {
                    %><jsp:include page="/_includes/brands/eztoff.jsp"><jsp:param name="localeId" value="<%= localeId %>" /></jsp:include><%
                }
            %></div>
            <div id="navigation">
                <ul>
                    <!--  Normal order because they are left aligned -->
                    <li>&nbsp;</li>
                    <!--  Reverse order because they are right aligned -->
                    <li class="subItem"><a href="#" onclick="dijit.byId('aboutPopup').show();" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %>"><%= LabelExtractor.get(ResourceFileId.third, "navigation_about", locale) %></a></li>
                    <li class="subItem">
                        <input id="languageSelector" title="<%= LabelExtractor.get(ResourceFileId.third, "navigation_language_selector", locale) %>" />
                        <script type="text/javascript">
                        dojo.require("domderrien.i18n.LanguageSelector");
                        dojo.addOnLoad(function() { domderrien.i18n.LanguageSelector.createSelector("languageSelector", null, [<%
                            ResourceBundle languageList = LocaleController.getLanguageListRB();
                            Enumeration<String> keys = languageList.getKeys();
                            while(keys.hasMoreElements()) {
                                String key = keys.nextElement();
                                %>{value:"<%= key %>",label:"<%= languageList.getString(key) %>"}<%
                                if (keys.hasMoreElements()) {
                                    %>,<%
                                }
                            }
                            %>], "<%= localeId %>", "globalCommand", null)});
                        </script>
                    </li>
                </ul>
            </div>
        </div>
