<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%><%
    String color = request.getParameter("color");
    String colorOddRow = request.getParameter("color-odd-row");
    String colorLink = request.getParameter("color-link");
    String colorTitle = request.getParameter("color-title");%>
        body, #centerZone, .hint, .comment, label {<%
        if (color != null && 0 < color.length()) { %>color:<%= color.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        #centerZone .title, #centerZone .brand, .progressBar .active, .poweredBy a, .poweredBy a:hover, .poweredBy a:visited {<%
        if (colorTitle != null && 0 < colorTitle.length()) { %>color:<%= colorTitle.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        a, a:hover, a:visited {<%
        if (colorLink != null && 0 < colorLink.length()) { %>color:<%= colorLink.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        .form>tbody>tr:nth-child(2n+1) label {<%
        if (colorOddRow != null && 0 < colorOddRow.length()) { %>color:<%= colorOddRow.replaceAll(";|<|>|\\:", "") %>;<% } %> }
<%
    String backgroundImage = request.getParameter("background-image");
    String backgroundColor = request.getParameter("background-color");
    String backgroundColorOddRow = request.getParameter("background-color-odd-row");
%>
        #centerZone {<%
        if (backgroundImage != null && 0 < backgroundImage.length()) { %>background-image:url(<%= backgroundImage.replaceAll(";|<|>", "") %>);<% } %> }
        #centerZone, .form>tbody>tr:nth-child(2n) {<%
        if (backgroundColor != null && 0 < backgroundColor.length()) { %>background-color:<%= backgroundColor.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        .form>tbody>tr:nth-child(2n+1) {<%
        if (backgroundColorOddRow != null && 0 < backgroundColorOddRow.length()) { %>background-color:<%= backgroundColorOddRow.replaceAll(";|<|>|\\:", "") %>;<% } %> }
<%
    String fontSize = request.getParameter("font-size");
    String fontSizeTitle = request.getParameter("font-size-title");
    String fontFamily = request.getParameter("font-family");
%>
        body, th, td, textarea {<%
        if (fontFamily != null && 0 < fontFamily.length()) { %>font-family:<%= fontFamily.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        #centerZone, textarea {<%
        if (fontSize != null && 0 < fontSize.length()) { %>font-size:<%= fontSize.replaceAll(";|<|>|\\:", "") %>;<% } %> }
        .brand, .title {<%
        if (fontSizeTitle != null && 0 < fontSizeTitle.length()) { %>font-size:<%= fontSizeTitle.replaceAll(";|<|>|\\:", "") %>;<% } %> }
