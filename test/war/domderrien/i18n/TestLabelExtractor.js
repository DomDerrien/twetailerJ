/////////////// Required header - beginning //////////////////////////
//
// WARNING:
//   When you add, update, or remove a test function, don't forget
//   to update accordingly the exposeTestFunctionNames() function!
//
function exposeTestFunctionNames() {
	return [
	        "testExtractorGetI",
	        "testExtractorGetII",
	        "testExtractorGetIII",
	        "testExtractorGetIV"
    ];
}

function jscoverageRunner() {
	var testTitle = "TestLabelExtractor.js";
	var libDeps = ["domderrien.i18n.LabelExtractor"];
	var testFunctions = exposeTestFunctionNames();
	
	jscoverageProcessor(testTitle, libDeps, testFunctions);
}

// JSUnit framework function
function setUp() {
	// NOP
}

// JSUnit framework function
function tearDown() {
	// NOP
}
////////////// Required header - end////////////////////////////////

function testExtractorGetI() {
	var lblExtr = domderrien.i18n.LabelExtractor.init("domderrien.i18n", "labels", "uu_UU"); // Unsupported locale
	assertEquals("English", lblExtr.get("bundle_language"));
	assertEquals("N/A", lblExtr.get("unit_test_sample"));
}
function testExtractorGetII() {
	var lblExtr = domderrien.i18n.LabelExtractor.init("domderrien.i18n", "labels", "fr"); // Supported locale
	assertEquals("Français", lblExtr.get("bundle_language"));
	assertEquals("N/A", lblExtr.get("unit_test_sample"));
}
function testExtractorGetIII() {
	var lblExtr = domderrien.i18n.LabelExtractor.init("domderrien.i18n", "labels", "fr_CA"); // Supported locale
	assertEquals("Français Canadien", lblExtr.get("bundle_language"));
	assertEquals("N/A", lblExtr.get("unit_test_sample"));
}
function testExtractorGetIV() {
	var lblExtr = domderrien.i18n.LabelExtractor.init("domderrien.i18n", "labels", "fr_BE"); // Unsupported locale
	assertEquals("Français", lblExtr.get("bundle_language"));
	assertEquals("N/A", lblExtr.get("unit_test_sample"));
}
