<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%><%
    String color = request.getParameter("color");
    String colorOddRow = request.getParameter("color-odd-row");
    String colorLink = request.getParameter("color-link");
    String colorTitle = request.getParameter("color-title");
    String colorBrand = request.getParameter("color-brand");%>
        body, .dataZone, .hint, .comment, label {<%
        if (color != null && 0 < color.length()) { %>color:<%= color.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        #introFlashInfo, .dataZone .title, .progressBar .active, .poweredBy a, .poweredBy a:hover, .poweredBy a:visited {<%
        if (colorTitle != null && 0 < colorTitle.length()) { %>color:<%= colorTitle.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        .dataZone .brand {<%
        if (colorBrand != null && 0 < colorBrand.length()) { %>color:<%= colorBrand.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        a, a:hover, a:visited {<%
        if (colorLink != null && 0 < colorLink.length()) { %>color:<%= colorLink.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        #introFlashWait>span, .oddRow label {<%
        if (colorOddRow != null && 0 < colorOddRow.length()) { %>color:<%= colorOddRow.replaceAll(";|<|>|\\:", "") %>;<% } %> }
<%
    String backgroundImage = request.getParameter("background-image");
    String backgroundColor = request.getParameter("background-color");
    String backgroundColorOddRow = request.getParameter("background-color-odd-row");
%>
        .dataZone {<%
        if (backgroundImage != null && 0 < backgroundImage.length()) { %>background-image:url(<%= backgroundImage.replaceAll(";|<|>", "") %>);<% } %> }
        #introFlash, .dataZone, .dataZone>div, .evenRow, .content, .footer:hover {<%
        if (backgroundColor != null && 0 < backgroundColor.length()) { %>background-color:<%= backgroundColor.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        #introFlashWait>span, .oddRow {<%
        if (backgroundColorOddRow != null && 0 < backgroundColorOddRow.length()) { %>background-color:<%= backgroundColorOddRow.replaceAll(";|<|>|\\:", "") %>;<% } %> }
<%
    String fontSize = request.getParameter("font-size");
    String fontSizeTitle = request.getParameter("font-size-title");
    String fontSizeBrand = request.getParameter("font-size-brand");
    String fontFamily = request.getParameter("font-family");
    String fontFamilyTitle = request.getParameter("font-family-title");
    String fontFamilyBrand = request.getParameter("font-family-brand");
%>
        body, th, td, textarea {<%
        if (fontFamily != null && 0 < fontFamily.length()) { %>font-family:<%= fontFamily.replaceAll(";|<|>|\\:", "") %>;<% }
        if (fontSize != null && 0 < fontSize.length()) { %>font-size:<%= fontSize.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        #introFlashInfo, .title {<%
        if (fontFamilyTitle != null && 0 < fontFamilyTitle.length()) { %>font-family:<%= fontFamilyTitle.replaceAll(";|<|>|\\:", "") %>;<% }
        if (fontSizeTitle != null && 0 < fontSizeTitle.length()) { %>font-size:<%= fontSizeTitle.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        .dataZone .brand {<%
        if (fontFamilyBrand != null && 0 < fontFamilyBrand.length()) { %>font-family:<%= fontFamilyBrand.replaceAll(";|<|>|\\:", "") %>;<% }
        if (fontSizeBrand != null && 0 < fontSizeBrand.length()) { %>font-size:<%= fontSizeBrand.replaceAll(";|<|>|\\:", "") %>;<% } %> }
