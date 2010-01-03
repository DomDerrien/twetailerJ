<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" version="4.0" encoding="utf-8" indent="no" />
	<xsl:template match="tmx/header" />
	<xsl:template match="tmx/body">
		<html>
			<head>
				<title>TMX representation</title>
			    <style type="text/css">
			        @import "http://ajax.googleapis.com/ajax/libs/dojo/1.4/dojo/resources/dojo.css";
			        @import "http://ajax.googleapis.com/ajax/libs/dojo/1.4/dijit/themes/tundra/tundra.css";
			        @import "http://ajax.googleapis.com/ajax/libs/dojo/1.4/dijit/themes/tundra/tundra.css";
			        @import "http://ajax.googleapis.com/ajax/libs/dojo/1.4/dojox/grid/resources/Grid.css";
			        @import "http://ajax.googleapis.com/ajax/libs/dojo/1.4/dojox/grid/resources/tundraGrid.css";
                </style>
                <style type="text/css">
					#introFlash {
					    width: 100%;
					    height: 100%;
					    margin: 0;
					    border: 0 none;
					    padding: 0;
					    position: absolute;
					    background-color: white;
					    z-index: 999;
					}
					#introFlash>div {
					    position: absolute;
					    top: 40%;
					    width: 100%;
					    text-align: center;
					}
					#introFlash>div>span {
					    background-color: #F9F9F9;
					    border: 1px solid #BFBFBF;
					    padding: 4px;
					    line-height: 30px;
					}
			    </style>
            </head>
            <body class="tundra">
			    <div id="introFlash">
			        <div><span>Please, wait while loading...</span></div>
			    </div>
                <script
                    djConfig="parseOnLoad: false, isDebug: false, useXDomain: true, baseUrl: './', dojoBlankHtmlUrl: '../../src/WebContent/blank.html'"
                    src="http://ajax.googleapis.com/ajax/libs/dojo/1.4/dojo/dojo.xd.js"
                    type="text/javascript"
                ></script>
                <script type="text/javascript">
                dojo.require("dijit.Dialog");
                dojo.require("dijit.layout.BorderContainer");
                dojo.require("dijit.layout.ContentPane");
		        dojo.require("dojox.grid.DataGrid");
                dojo.require("dojo.data.ItemFileWriteStore");
                dojo.require("dijit.form.Button");
                dojo.require("dojo.parser");
                var tmxContentStore = null;
                var backendURL = null;
                dojo.addOnLoad(function(){
                    tmxContentStore = new dojo.data.ItemFileWriteStore({ data: {
                        identifier: "tuid",
                        items: [
                        <xsl:for-each select="tu">
                            <xsl:sort select="@tuid" />
                            <xsl:variable name="segment">
                                <xsl:call-template name="globalReplace" >
                                    <xsl:with-param name="origin" select="tuv/seg"/>
                                    <xsl:with-param name="target" select="'&#x22;'"/>
                                    <xsl:with-param name="replacement" select="'\u0022'"/>
                                </xsl:call-template>
                            </xsl:variable>
                            {
                                tuid:"<xsl:value-of select="@tuid" />",
                                tuv:"<xsl:value-of select="normalize-space($segment)" />",
                                version:"<xsl:value-of select="prop[@type='x-version']" />"
                            },
                        </xsl:for-each>
                            {tuid:0,tuv:'last item',version:0}
                        ]
                    }});
                    dojo.connect(tmxContentStore, "onSet", function() { dijit.byId("submitBtn").attr("disabled", false); });
	                tmxContentStore._saveEverything = function(saveCompleteCallback, saveFailedCallback, newFileContentString) {
    	                if (backendURL == null) {
    	                   alert("The JavaScript variable 'backendURL' need to be provisioned to let the page knowing where to send the updates\n\n" + newFileContentString);
    	                }
    	                else {
		                    dojo.xhrPost( {
		                        content: {
		                            dataString: newFileContentString
		                        },
		                        handleAs: "json",
		                        load: function(response) {
		                            if (response !== null &amp;&amp; response.success) {
		                                saveCompleteCallback();
		                            }
		                            else {
		                                saveFailedCallback(response);
		                            }
		                        },
		                        error: saveFailedCallback,
		                        url: backendURL
		                    });
	                    }
	                };
                    dojo.parser.parse();
                    dojo.fadeOut({
                        node: "introFlash",
                        delay: 50,
                        onEnd: function() {
                            dojo.style("introFlash", "display", "none");
                        }
                    }).play();
                });
                </script>
                <div id="topContainer" dojoType="dijit.layout.BorderContainer" gutters="true" style="height: 100%;">
                    <div dojoType="dijit.layout.ContentPane" id="headerZone" region="top">

                        <button
                            disabled="true"
                            dojoType="dijit.form.Button"
                            id="submitBtn"
                            onClick="tmxContentStore.save(function() {{ location.reload(); }}, function(err, req) {{ alert(err); }});"
                            style="float: right;"
                        >Submit Updates</button>

                        <span style="float: left; width: 7em;">Language:</span>
                        <input dojotType="dijit.form.Text" style="width: 30em;" readonly="true" value="{tu[@tuid='bundle_language']/tuv/seg} ({tu[@tuid='bundle_language']/tuv/@xml:lang})" /><br/>

                        <span style="float: left; width: 7em;">Contributor:</span>
                        <input dojotType="dijit.form.Text" id="contributorId" type="text" style="width: 30em;" value="Your identifier" /><br/>

                        <span style="float: left; width: 7em;">Comment:</span>
                        <input dojotType="dijit.form.Text" id="comment" type="text" style="width: 60em;" value="Your comment" />
                    </div>
					<table
                        dojoType="dojox.grid.DataGrid"
					    id="tmxContent"
					    region="center"
					    store="tmxContentStore"
					    style="font-size: 10pt;"
				    >
					    <thead>
					        <tr>
						        <th field="tuid" width="30%">Translation Unit Identifier</th>
						        <th field="tuv" width="auto" editable="true">Translation Unit Value</th>
                                <th field="version" width="60px" align="right">Version</th>
					        </tr>
					    </thead>
					</table>
                    <div dojoType="dijit.layout.ContentPane" id="footerZone" region="bottom">
                        Copyright (c) 2006-2010, Dom Derrien.
                        <span style="float:right;">Available under the terms of the <a href="javascript:dijit.byId('aboutPopup').show();">modified BSD license</a>.</span>
                    </div>
                </div>
                <!--script type="text/javascript">
				<xsl:for-each select="tu">
					<xsl:sort select="@tuid" />
					<dl id="{@tuid}">
						<dt>
							<xsl:variable
								name="lowerTUID"
								select="translate(@tuid,'ABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÂÄÃÈÉÊËÌÍÎÏÒÓÔÖÕÙÚÛÜÝŸÑ','abcdefghijklmnopqrstuvwxyzàáâäãèéêëìíîïòóôöõùúûüýÿñ')"
							/>
							<a name="{$lowerTUID}"></a><xsl:value-of select="@tuid" />
						</dt>
                        <dd class="note">
                            <xsl:call-template name="globalReplace">
                                <xsl:with-param name="origin" select="note"/>
                                <xsl:with-param name="target" select="'|'"/>
                                <xsl:with-param name="replacement" select="'[br/]'"/>
                            </xsl:call-template>
                        </dd>
                        <dd class="version">Version:
                            <xsl:for-each select="prop[@type='x-version']">
                                <xsl:value-of select="." />.
                            </xsl:for-each>
                        </dd>
                        <xsl:variable name="segment">
                            <xsl:call-template name="globalReplace" >
                                <xsl:with-param name="origin" select="tuv/seg"/>
                                <xsl:with-param name="target" select="'|'"/>
                                <xsl:with-param name="replacement" select="'[br/]'"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <dd class="current">
                            <b><xsl:value-of select="$segment" /></b>
                        </dd>
                        <dd class="alternative">
                            <input
                                dojoType="dijit.form.text"
                                name="{@tuid}"
                                value="{$segment}"
                                style="width:100%;"
                            />
                        </dd>
					</dl>
				</xsl:for-each>
				</script-->
                <div
                    dojoType="dijit.Dialog"
                    id="aboutPopup"
                    title="About"
                    style="max-width:600px;"
                >
                    <p>The text of the modified BSD license covering my work:</p>
                    <hr/>
                    <p><u>The "New" BSD License:</u></p>
                    <p>Copyright (c) 2006-2010, Dom Derrien<br/>
                    All rights reserved.</p>
                    <p>Redistribution and use in source and binary forms, with or without
                    modification, are permitted provided that the following conditions are met:</p>
                    <ul>
                        <li>Redistributions of source code must retain the above copyright notice, this
                        list of conditions and the following disclaimer.</li>
                        <li>Redistributions in binary form must reproduce the above copyright notice,
                        this list of conditions and the following disclaimer in the documentation
                        and/or other materials provided with the distribution.</li>
                        <li>Neither the name of the Dom Derrien nor the names of other contributors
                        may be used to endorse or promote products derived from this software
                        without specific prior written permission.</li>
                    </ul>
                    <p>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
                    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
                    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
                    DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
                    FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
                    DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
                    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
                    CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
                    OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
                    OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</p>
                </div>
			</body>
		</html>
	</xsl:template>
	<xsl:template name="globalReplace">
		<xsl:param name="origin" />
		<xsl:param name="target" />
		<xsl:param name="replacement" />
		<xsl:variable name="output" select="translate($origin,'\','')" />
		<xsl:choose>
			<xsl:when test="contains($output,$target)">
				<xsl:value-of select="substring-before($output,$target)" />
				<xsl:choose>
					<xsl:when test="contains($replacement,'[br/]')">
						<br/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$replacement" />
					</xsl:otherwise>
				</xsl:choose>
				<xsl:call-template name="globalReplace">
					<xsl:with-param name="origin" select="substring-after($output,$target)"/>
					<xsl:with-param name="target" select="$target"/>
					<xsl:with-param name="replacement" select="$replacement"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$output" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>