<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.0 Strict//EN" />
	<xsl:template match="/">
		<html>
			<head>
				<title>JSUnit results</title>
				<style>
					body { font-family: verdana, arial; font-size: 68%; }
					h1 { font-size: 165%; }
					h2 { font-size: 125%; }
					table { width: 95%; }
					table.details th { background-color: #a6caf0; padding: 4px 5px; }
					table.details td { background-color: #eeeee0; padding: 4px 5px; }
				</style>
			</head>
			<body>
				<h1>JSUnit Test Results</h1>
				<hr />
				<h2>Summary</h2>
				<table class="details">
					<thead><tr><th>Test</th><th>Browser</th><th>Test suite</th><th>Success rate</th><th>Time</th></tr></thead>
					<tbody>
						<tr>
							<td><xsl:value-of select="count(browserResult/testCases/testCase)" /></td>
							<td><xsl:value-of select="browserResult/properties/property[@name='browserFileName']/@value" /></td>
							<td><xsl:value-of select="substring-after(browserResult/properties/property[@name='url']/@value, 'TeamMember/')" /></td>
							<td>100%</td>
							<td><xsl:value-of select="browserResult/@time" /></td>
						</tr>
					</tbody>
				</table>
				<table>
					<tbody>
						<tr>
							<td>Note: success rate always equals 100% because the source report has been generated correctly (use the browser command "view source" to see the original report).</td>
						</tr>
					</tbody>
				</table>
				<h2>Test cases</h2>
				<table class="details">
					<thead><tr><th>Suite</th><th>Test name</th><th>Time</th></tr></thead>
					<tbody>
						<xsl:for-each select="browserResult/testCases/testCase">
							<tr>
								<td><xsl:value-of select="substring-before(substring-after(@name, 'TeamMember/'), ':')" /></td>
								<td><xsl:value-of select="substring-after(@name, '.html:')" /></td>
								<td><xsl:value-of select="@time" /></td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>