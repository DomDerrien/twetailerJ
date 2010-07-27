<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%><%
    String localeId = request.getParameter("lg");
    String width = request.getParameter("w");
    String height = request.getParameter("h");
    if (localeId == null) { localeId = "en"; }
    if (width == null) { width = "300"; }
    if (height == null) { height = "400"; }
%><html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="<%= localeId %>">
<head>
    <body>
        <iframe
            id="ezToffWidget"
            src="http://localhost:9999/widget/golf-step-1.jsp?lg=<%= localeId %>&postalCode=H8P 3R8"
            style="width: <%= width %>px; height:<%= height %>px; float:right;border:0 none;"
            frameborder="0"
            type="text/html"
        ></iframe>
<pre
    style="border:1px solid gray; padding:15px;float:left;"
>&lt;!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"&gt;
&lt;%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%&gt;&lt;%
    String localeId = request.getParameter("lg");
    String width = request.getParameter("w");
    String height = request.getParameter("h");
    if (localeId == null) { localeId = "en"; }
    if (width == null) { width = "300"; }
    if (height == null) { height = "400"; }
%&gt;&lt;html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="&lt;%= localeId %&gt;&postalCode=H8P 3R8"&gt;
&lt;head&gt;
    &lt;body&gt;
        &lt;iframe
            id="ezToffWidget"
            src="http://localhost:9999/widget/golf.jsp?lg=&lt;%= localeId %&gt;"
            style="width: &lt;%= width %&gt;px; height:&lt;%= height %&gt;px; float:right;border:0 none;"
            frameborder="0"
            type="text/html"
        &gt;&lt;/iframe&gt;
    &lt;/body&gt;
&lt;/html&gt;</pre>
    </body>
</html>
