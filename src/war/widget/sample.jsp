<!doctype html>
<%@page
    language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.Locale"
    import="domderrien.i18n.LabelExtractor"
    import="domderrien.i18n.LabelExtractor.ResourceFileId"
    import="domderrien.i18n.LocaleController"
    import="twetailer.validator.ApplicationSettings"
    import="twetailer.validator.LocaleValidator"
%><%
    // Application settings
    ApplicationSettings appSettings = ApplicationSettings.get();
    boolean useCDN = appSettings.isUseCDN();
    String cdnBaseURL = appSettings.getCdnBaseURL();

    // Locale detection
    Locale locale = LocaleController.detectLocale(request);

    // Getting parameters
    String localeId = request.getParameter("lg");
    String width = request.getParameter("w");
    String height = request.getParameter("h");
    if (localeId == null) { localeId = "en"; }
    if (width == null) { width = "200"; }
    if (height == null) { height = "400"; }
%>
<html dir="ltr" lang="<%= localeId %>">
<head>
    <title>AnotherSocialEconomy.com - HowTo embed the ASE widget</title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>
    <style type="text/css">
        @import "<%= cdnBaseURL %>/dojo/resources/dojo.css";
        @import "<%= cdnBaseURL %>/dijit/themes/tundra/tundra.css";

        body { margin: 10px 60px; font-size: 10pt; font-family: tahoma, verdana, sans-serif; }
        h1 { font-size: 14pt; color: white; background-color: black; font-weight: bold; padding: 3px 5px; }
        .name, .n, code { color: green; }
        .value, .v { color: blue; }

        .params { border: 1px solid black;  }
        .params>thead>tr>th { background-color: black; color: white; font-weight: bold; text-align: center; padding: 3px 5px; }
        .params>tbody>tr>td { border: 1px solid; black; padding: 3px 5px; }
        .params>tbody>tr>td:first-child { color: green; }
        .params>tbody>tr>td>input { color: blue; border: 0 none; width: 20em; }
        .params>tbody>tr>td>input:hover { background-color: #fff000; }
        .params>tbody>tr>td:last-child { color: brown; }
   </style>
</head>
<body class="tundra">
    <div style="float: right; padding: 5px; background: white; text-align: center;">
        <div>
            Select your widget: <select onchange="document.getElementById('aseWidget').src=this.value;">
                <option value="/widget/eztoff.jsp?lg=<%= localeId %>&postalCode=H3C2N6&countryCode=CA&referralId=0">ezToff</option>
                <option value="/widget/ase.jsp?lg=<%= localeId %>&referralId=0" selected>Generic ASE</option>
            </select>
        </div>
        <iframe
            id="aseWidget"
            src="/widget/ase.jsp?lg=<%= localeId %>&referralId=0"
            style="width:<%= width %>px;height:<%= height %>px;border:0 none;"
            frameborder="0" width="<%= width %>" height="<%= height %>" border="0"
        ></iframe>
    </div>
    <h1 style="background-color: white; color: black; font-weight: bold; font-size: 30px; line-height: 36px; font-family: 'Helvetica Neue', Arial, Helvetica, 'Nimbus Sans L', sans-serif;">AnotherSocialEconomy</h1>
    <p><a href="http://anothersocialeconomy.com"><img src="http://anothersocialeconomy.com/wp-content/uploads/2010/08/cropped-iStock_000006943675Small.jpg" style="border-top: 4px solid black; border-bottom: 4px solid black;"/></a></p>
    <h1>Widget context</h1>
    <p>
        <a href="http://anothersocialeconomy.com/">AnotherSocialEconomy.com</a> (ASE) provides a multi-channel engine (e-mail, Twitter, Facebook, SMS, Android, iPhone, Web, etc.).
        <a href="http://eztoff.com/">ezToff</a> is a specific implementation of ASE to the golf world!
        <b>ezToff</b> offers its widget to help players initiating their requests.
        The widget can be embedded on any Web page of the participating golf courses or associations, being given the right referral identifier is used.
        When golfers have successfully created their requests with the <b>ezToff</b> widget, all communications continue trough e-mail.
        No golfer registration is required as the system is widely open.
        And no system requirement on the golf course-side other than inserting the code snippet below into the golf course Web site.
    </p>
    <h1>Widget parameters</h1>
    <p>
        The following table presents the various parameters integrators can tuned to embed seamlessly the widget in third-party Web sites.
        The following sections offer operators to generate the HTML code to be inserted in these Web sites and a bookmark-let for stand-alone usage.
        Don't hesitate to click on the <span style="color:blue;">blue text</span> to update the corresponding field before the generation of the URLs.
    </p>
    <table class="params" border="0" cellspacing="0" cellpadding="0">
        <thead>
            <tr>
                <th>Parameter name</th>
                <th>Value</th>
                <th>Comment</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>lg</td>
                <td><input id="lg" type="text" value="<%= LocaleValidator.DEFAULT_LANGUAGE %>"/></td>
                <td>ISO code of the interface language</td>
            </tr>
            <tr>
                <td>referralId</td>
                <td><input id="referralId" type="text" value="0"/></td>
                <td>Influencer identifier (mandatory for tracking purposes). The pattern is 'ddddd-ddddd-dd' with 'd' being a digit. Note that the last 2 digits can be updated by the influencer to track different entry points or marketing campaigns.</td>
            </tr>
            <tr>
                <td>hideBrand</td>
                <td><input id="hideBrand" type="text" value=""/></td>
                <td>If present, the brand part is hidden (just printed in the confirmation message, on the last wizard pane)</td>
            </tr>
            <tr>
                <td>brand</td>
                <td><input id="brand" type="text" value="<%= LabelExtractor.get(ResourceFileId.third, "cw_brand", locale) %>"/></td>
                <td>Text appearing as the brand banner</td>
            </tr>
            <tr>
                <td>criteria</td>
                <td><input id="criteria" type="text" value=""/></td>
                <td>Default search criteria; note that the selected text is automatically transmitted by the bookmarklet via this parameter.</td>
            </tr>
            <tr>
                <td>postalCode</td>
                <td><input id="postalCode" type="text" value=""/></td>
                <td>Default postal code</td>
            </tr>
            <tr>
                <td>countryCode</td>
                <td><input id="countryCode" type="text" value="<%= LocaleValidator.DEFAULT_COUNTRY_CODE %>"/></td>
                <td>Default country code; must be an 2-letters ISO code</td>
            </tr>
            <tr>
                <td>font-family</td>
                <td><input id="font-family" type="text" value="arial, helvetica, sans-serif"/></td>
                <td>Default font families, <a href="http://www.w3.org/TR/REC-CSS1/#font-family">separated by a comma</a>; goes with 'font-size' and 'color'</td>
            </tr>
            <tr>
                <td>font-family-brand</td>
                <td><input id="font-family-brand" type="text" value="'arial black', gadget, sans-serif"/></td>
                <td>Font families for the brand; goes with 'font-size-brand' and 'color-brand'; see 'font-family' for the constraints</td>
            </tr>
            <tr>
                <td>font-family-title</td>
                <td><input id="font-family-title" type="text" value="'arial black', gadget, sans-serif"/></td>
                <td>Font families for the titles; goes with 'font-size-title' and 'color-title'; see 'font-family' for the constraints</td>
            </tr>
            <tr>
                <td>font-size</td>
                <td><input id="font-size" type="text" value="11px"/></td>
                <td>Default font families, <a href="http://www.w3.org/TR/REC-CSS1/#font-size">separated by a comma</a>; goes with 'font-size' and 'color'</td>
            </tr>
            <tr>
                <td>font-size-brand</td>
                <td><input id="font-size-brand" type="text" value="14px"/></td>
                <td>Font families for the brand; goes with 'font-size-brand' and 'color-brand'; see 'font-size' for the constraints</td>
            </tr>
            <tr>
                <td>font-size-title</td>
                <td><input id="font-size-title" type="text" value="11px"/></td>
                <td>Font families for the titles; goes with 'font-size-title' and 'color-title'; see 'font-size' for the constraints</td>
            </tr>
            <tr>
                <td>color</td>
                <td><input id="color" type="text" value="#222"/></td>
                <td>Default text color; can be a <a href="http://www.w3schools.com/HTML/html_colornames.asp">textual value or an hexadecimal starting with '#'</a>; note that the '#' must be escaped in '%23' if inserted manually in a URL</td>
            </tr>
            <tr>
                <td>color-odd-row</td>
                <td><input id="color-odd-row" type="text" value="darkred"/></td>
                <td>Color of the text in the odd rows in the table; goes in pair with 'background-color-odd-row'; see 'color' for constraints on the value</td>
            </tr>
            <tr>
                <td>color-brand</td>
                <td><input id="color-brand" type="text" value="darkred"/></td>
                <td>Color of the brand; goes in pair with 'font-size-brand' and 'font-family-brand'; see 'color' for constraints on the value</td>
            </tr>
            <tr>
                <td>color-title</td>
                <td><input id="color-title" type="text" value="#222"/></td>
                <td>Color of the titles; goes in pair with 'font-size-title' and 'font-family-title'; see 'color' for constraints on the value</td>
            </tr>
            <tr>
                <td>color-link</td>
                <td><input id="color-link" type="text" value="darkred"/></td>
                <td>Color of the links; see 'color' for constraints on the value</td>
            </tr>
            <tr>
                <td>background-image</td>
                <td><input id="background-image" type="text" value=""/></td>
                <td>URL of the image to used in the background</td>
            </tr>
            <tr>
                <td>background-color</td>
                <td><input id="background-color" type="text" value="lightgrey"/></td>
                <td>Default background color; see 'color' for constraints on the value</td>
            </tr>
            <tr>
                <td>background-color-odd-row</td>
                <td><input id="background-color-odd-row" type="text" value="white"/></td>
                <td>Color of the background in the odd rows in the table; goes in pair with 'color-odd-row'; see 'color' for constraints on the value</td>
            </tr>
        </tbody>
    </table>
    <h1>Code snippets</h1>
    <p>
        The following piece of code shows how flexible is the system.
        By default, the &lt;iframe/&gt; content shows a background colored and with rounded corners.
        In the example below, the &lt;iframe/&gt; background color is set to 'transparent' and color is applied to the &lt;iframe/&gt; itself, with drop-shadow.
    </p>
    <textarea id="cdsnppt" rows="5" style="border: 1px solid gray; width: 100%;">&nbsp;&nbsp;&lt;iframe
&nbsp;&nbsp;&nbsp;&nbsp;src="https://anothersocialeconomy.appspot.com/widget/ase.jsp?background-color=transparent"
&nbsp;&nbsp;&nbsp;&nbsp;style="width: 250px; height: 400px; border: 0 none; background-color: #fff000; -moz-box-shadow: 5px 5px 5px #aaa; -webkit-box-shadow: 5px 5px 5px #aaa;"
&nbsp;&nbsp;&nbsp;&nbsp;frameborder="0" width="250" height="400" border="0"
&nbsp;&nbsp;&gt;&lt;/iframe&gt;</textarea>
    <p>
        You can also <a href="javascript:generateCodeSnippet()">update the code snippet</a> with parameters loaded from the table above
        (<a href="javascript:setupParametersForYPG();">shortcut for a YPG setup</a>).
        Reminder: you can update each <span style="color: blue;">blue value</span> with a simple click to edit them.
    </p>
    <p>
        The following piece of code shows the line to add to your webpages in order to have the ASE Floating Tab installed on your site.
        When the visitors click on the Tab, the widget is loaded and displayed over the content of your page.
        This is a light process as it does not use space on your pages and it does not slowdown the page rendering as the widget itself can do.
        The code snippet is about a command to load a &lt;script/&gt; file from the ASE servers.
    </p>
    <textarea rows="1" style="border: 1px solid gray; width: 100%;">&nbsp;&nbsp;&lt;script type="text/javascript" src="/widget/widget-loader.js"&gt;&lt;/script&gt;</textarea>
    <h1>Widget alternatives</h1>
    <p>
        The widget is a nice tool to be embedded on retailers' website.
        But what happens to a customer visiting a website which does not provide the widget?
        Our solution is to offer a general purpose <a href="http://en.wikipedia.org/wiki/Bookmarklet">bookmarklet</a>!
    </p>
    <p>
        To activate the widget with the parameters from the table above
        (<a href="javascript:setupParametersForYPG();">shortcut for a YPG setup</a>),
        you have <b>first</b> to <b><a href="javascript:compressRawCode();">trigger the bookmarklet code generation</a></b>.
        Then you can 'drag and drop' the updated link on your bookmark toolbar.
    </p>
    <ul>
        <li>
            <a id="bkmklt" style="border:1px solid grey;background-color:lightgrey;padding:3px 5px;">ASE bookmarklet</a>
            &mdash; trigger the bookmarklet code generator first and drag'n'drop the generated link on your bookmark toolbar
        </li>
    </ul>
    <ul>
        <li>
            <a
                href="javascript:(function(){var dc=document,js=dc.createElement('script');js.src='https://anothersocialeconomy.appspot.com/widget/widget-loader.js';js.type='text/javascript';dc.getElementsByTagName('head')[0].appendChild(js);})()"
                style="border:1px solid grey;background-color:#fff000;padding:3px 5px;"
            >ASE floating tab</a>
            &mdash; ready to be dropped on your bookmark toolbar
        </li>
    </ul>
    <p>
        The widget can be also added to popular portal as <a href="http://www.google.com/ig" target="ig">iGoogle</a> , for example, with standard buttons as
        <a
            href="http://fusion.google.com/add?source=atgs&amp;moduleurl=http%3A//domderrien.github.com/ase-ypg-igoogle.xml"
            onclick="javascript:pageTracker._trackPageview('/outbound/article/fusion.google.com');"
        >
            <img src="http://gmodules.com/ig/images/plus_google.gif" border="0" alt="Add to Google" />
        </a>.
    </p>

    <script type="text/javascript">
    var setupParametersForYPG = function() {
        document.getElementById('lg').value = 'en';
        document.getElementById('referralId').value = '223001-68747561-00';
        document.getElementById('hideBrand').value = '';
        document.getElementById('brand').value = 'YellowPages.ca Buying Network';
        document.getElementById('criteria').value = '';
        document.getElementById('postalCode').value = 'H3C2N6';
        document.getElementById('countryCode').value = 'CA';
        document.getElementById('font-family').value = 'arial,helvetica, sans-serif';
        document.getElementById('font-family-brand').value = 'arial,helvetica,sans-serif';
        document.getElementById('font-family-title').value = 'arial,helvetica,sans-serif';
        document.getElementById('font-size').value = '12px';
        document.getElementById('font-size-brand').value = '18px';
        document.getElementById('font-size-title').value = '14px';
        document.getElementById('color').value = '#252525';
        document.getElementById('color-odd-row').value = 'white';
        document.getElementById('color-brand').value = '#252525';
        document.getElementById('color-title').value = '#252525';
        document.getElementById('color-link').value = '#005e9d';
        document.getElementById('background-image').value = '';
        document.getElementById('background-color').value = '#fff000';
        document.getElementById('background-color-odd-row').value = '#7f8082';
    };
    var addParameterSequence = function(parameters, name) {
        var value = document.getElementById(name).value;
        if (0 < value.length) {
            parameters.push(name + '=' + escape(escape(value)));
        }
        return parameters;
    };
    var addParameterSequences = function(parameters, conveyCriteria) {
        addParameterSequence(parameters, 'lg');
        addParameterSequence(parameters, 'referralId');
        addParameterSequence(parameters, 'hideBrand');
        addParameterSequence(parameters, 'brand');
        addParameterSequence(parameters, 'postalCode');
        addParameterSequence(parameters, 'countryCode');
        addParameterSequence(parameters, 'font-family');
        addParameterSequence(parameters, 'font-family-brand');
        addParameterSequence(parameters, 'font-family-title');
        addParameterSequence(parameters, 'font-size');
        addParameterSequence(parameters, 'font-size-brand');
        addParameterSequence(parameters, 'font-size-title');
        addParameterSequence(parameters, 'color');
        addParameterSequence(parameters, 'color-odd-row');
        addParameterSequence(parameters, 'color-brand');
        addParameterSequence(parameters, 'color-title');
        addParameterSequence(parameters, 'color-link');
        addParameterSequence(parameters, 'background-image');
        addParameterSequence(parameters, 'background-color');
        addParameterSequence(parameters, 'background-color-odd-row');

        if (conveyCriteria) {
            addParameterSequence(parameters, 'criteria');
        }
        else {
            // Expected to be setup dynamically by the bookmarklet code
        }

        return parameters;
    };
    var generateCodeSnippet = function() {
        var code = '&nbsp;&nbsp;&lt;iframe\n' +
                   '&nbsp;&nbsp;&nbsp;&nbsp;src="https://anothersocialeconomy.appspot.com/widget/ase.jsp?[plchldr]"\n'+
                   '&nbsp;&nbsp;&nbsp;&nbsp;style="width: 250px; height: 400px; border: 0 none; background-color: #fff000; -moz-box-shadow: 5px 5px 5px #aaa; -webkit-box-shadow: 5px 5px 5px #aaa;"\n' +
                   '&nbsp;&nbsp;frameborder="0" width="250" height="400" border="0"\n' +
                   '&nbsp;&nbsp;&gt;&lt;/iframe&gt;';

        var parameters = addParameterSequences([], false);
        code = code.replace('[plchldr]', parameters.join('&'));

        document.getElementById('cdsnppt').innerHTML = code;
    };
    var aseBkmklt = function() {
        var id = '__ASE_wdgt',
            dc = document,
            dv = dc.getElementById(id),
            txt = window.getSelection ? window.getSelection() : dc.getSelection ? dc.getSelection() : dc.selection ? dc.selection.createRange().text : '',
            src = 'https://anothersocialeconomy.appspot.com/widget/ase.jsp?[plchldr]&criteria=' + escape(txt),
            ifr;
        if (dv) {
            dv.style.display = '';
            ifr = dc.getElementById(id + '_ifr');
            ifr.src = src;
        }
        else {
            var cpc = '100%',
                rad = 'border-radius',
                px = '30px',
                hd,
                st;
            dv = dc.createElement('div');
            dv.id = id;
            st = dv.style;
            st.position = 'fixed';
            st.top = px;
            st.right = px;
            st.bottom = px;
            st.width = '250px';
            st.border = '0 none';
            st.zIndex = 999;
            st.backgroundColor = 'transparent';
            st.textAlign = 'left';
            dc.getElementsByTagName('body')[0].appendChild(dv);
            hd = dc.createElement('span');
            hd.innerHTML = '<a href="#" onclick="document.getElementById(\''+id+'\').style.display=\'none\';return false;" style="color:white;font-weight:bold;">Close [X]<a>';
            st = hd.style;
            st.backgroundColor = '#7f8082';
            st.fontFamily = 'arial,sans-serif';
            st.fontSize = 'smaller';
            st.color = '#fff';
            px = '5px';
            st.padding = px;
            st[rad] = px;
            dv.appendChild (hd);
            st['-moz-' + rad] = px;
            ifr = dc.createElement('iframe');
            ifr.id = id + '_ifr';
            ifr.width = cpc;
            ifr.height = cpc;
            ifr.border = '0';
            st = ifr.style;
            st.width = cpc;
            st.height = cpc;
            st.border = '0 none';
            ifr.src = src;
            dv.appendChild(ifr);
        }
    };
    var compressAseBkmkltCode = function() {
        var code = '' + aseBkmklt; // transform the Function as a String
        code = code.replace(/\n\s+/g, '');
        code = code.replace(/\s+=\s+/g, '=');
        code = code.replace(/\s+\?\s+/g, '?');
        code = code.replace(/\s+\:\s+/g, ':');
        code = code.replace(/\s+\+\s+/g, '+');
        code = code.replace(/\s+\(/g, '(');
        code = code.replace(/\s+\{/g, '{');

        var parameters = addParameterSequences([], false);
        code = code.replace('[plchldr]', parameters.join('&'));

        return code;
    };
    var compressRawCode = function() {
        var code = compressAseBkmkltCode();
        // if (prompt('Here is the compressed code (size: ' + code.length + ').\n\nIf you agree, it will be injected in the bookmarklet [href] attribute and you will be able to use it standalone.', code)) {
            document.getElementById('bkmklt').href = 'javascript:(' + code + ')();';
        // }
    };
    </script>

    <h1>Live widgets &amp; associated consoles</h1>

    <ul>
        <li>This documentation on <a href="https://anothersocialeconomy.appspot.com/widget/sample.jsp">appspot.com</a> or <a href="http://127.0.0.1:9999/widget/sample.jsp">dev server</a>.</li>
        <li>ASE widget alone: <a href="/widget/ase.jsp">normal</a> -- <a href="/widget/ase.jsp?color=yellow&color-title=green&color-link=yellow&color-odd-row=yellow&background-color=white&background-color-odd-row=white&font-family=courier&font-size=22pt&font-family-title=courier&font-family-brand=courier&font-size-title=42pt&background-image=http://www.dvd-ppt-slideshow.com/images/ppt-background/background-5.jpg">custom</a>.</li>
        <li>ezToff page alone: <a href="/widget/eztoff.jsp">normal</a> -- <a href="/widget/eztoff.jsp?color=yellow&color-title=green&color-link=yellow&color-odd-row=yellow&background-color=white&background-color-odd-row=white&font-family=courier&font-size=22pt&font-family-title=courier&font-family-brand=courier&font-size-title=42pt&background-image=http://www.dvd-ppt-slideshow.com/images/ppt-background/background-5.jpg">custom</a>.</li>
        <li>ASE console: <a href="/console/">Consumer</a> -- <a href="/console/associate.jsp">Associate</a>.</li>
        <li>ezToff console: <a href="/console/eztoff/">Consumer</a> -- <a href="/console/eztoff/associate.jsp">Associate</a>.</li>
    </ul>

   <script type="text/javascript" src="/widget/widget-loader.js"></script>

    <% if (!"localhost".equals(request.getServerName()) && !"127.0.0.1".equals(request.getServerName())) { %><script type="text/javascript">
    var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-11910037-2']);
    _gaq.push(['_trackPageview']);
    (function() {
        var ga = document.createElement('script');
        ga.type = 'text/javascript';
        ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(ga, s);
    })();
    </script><% } %>
</body>
</html>
