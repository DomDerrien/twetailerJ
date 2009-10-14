<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" version="4.0" encoding="utf-8" indent="no" />
	<xsl:template name="globalReplace">
		<xsl:param name="origin" />
		<xsl:param name="search" />
		<xsl:param name="replacement" />
		<xsl:variable name="output" select="translate($origin,'\','')" />
		<xsl:choose>
			<xsl:when test="contains($output,$search)">
				<xsl:value-of select="concat(substring-before($output,$search),$replacement)" />
				<br/>
				<xsl:call-template name="globalReplace">
					<xsl:with-param name="origin" select="substring-after($output,$search)"/>
					<xsl:with-param name="search" select="$search"/>
					<xsl:with-param name="replacement" select="$replacement"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$output" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="tmx/body">
		<html>
			<head>
				<title>Twetailer lexicon</title>
			    <link rel="shortcut icon" href="http://www.twetailer.com/images/logo/favicon.ico" />
			    <link rel="icon" href="http://www.twetailer.com/images/logo/favicon.ico" type="image/x-icon"/>
			    <style type="text/css">
			        @import "http://ajax.googleapis.com/ajax/libs/dojo/1.3/dojo/resources/dojo.css";
			        @import "http://ajax.googleapis.com/ajax/libs/dojo/1.3/dijit/themes/tundra/tundra.css";
			        @import "http://www.twetailer.com/styles/lexicon.css";
			    </style>
				<script language="JavaScript" type="text/javascript">
					function processSeeAlsoLinks() {
						var entries = document.getElementsByTagName("dl");
						var entryNb = entries == null ? 0 : entries.length;
						while(0 &lt; entryNb) {
							-- entryNb;
							var entry = entries[entryNb];
							var entryTitle = entry.id;
							var entryDef = entry.firstChild.nextSibling.innerHTML;
							var seeAlsoPos = entryDef == null ? -1 : entryDef.indexOf("(see also):");
							if (seeAlsoPos != -1) {
								// Get first part of the definition
								var before = entryDef.substring(0, seeAlsoPos + ("(see also):").length);
								var after = entryDef.substring(before.length);
								var eolPos = after.indexOf("<br/>");
								var equivalents = after.substring(0, eolPos - 1);
								after = after.substring(eolPos);
								equivalents = equivalents.split(",");
								var links = new Array();
								for (var i = 0; i &lt; equivalents.length; i++) {
									var equivalent = equivalents[i].trim();
								    links.push('<a href="#' + equivalent.toLowerCase() + '">' + equivalent + "</a>");
								}
								entry.firstChild.nextSibling.innerHTML = before + " " + links.join(", ") + after;
							}
						}
					}
				</script>
			</head>
			<body onload="processSeeAlsoLinks();">
				<xsl:for-each select="tu">
					<dl id="{@tuid}">
						<dt>
							<xsl:variable
								name="lowerTUID"
								select="translate(@tuid,'ABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÂÄÃÈÉÊËÌÍÎÏÒÓÔÖÕÙÚÛÜÝŸÑ','abcdefghijklmnopqrstuvwxyzàáâäãèéêëìíîïòóôöõùúûüýÿñ')"
							/>
							<a name="{$lowerTUID}"></a><xsl:value-of select="@tuid" />
						</dt>
						<dd>
							<xsl:call-template name="globalReplace">
								<xsl:with-param name="origin" select="tuv/seg"/>
								<xsl:with-param name="search" select="'|'"/>
								<xsl:with-param name="replacement" select="''"/>
							</xsl:call-template>
						</dd>
					</dl>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>