/////////////// Required header - beginning //////////////////////////
//
// WARNING:
//   When you add, update, or remove a test function, don't forget
//   to update accordingly the exposeTestFunctionNames() function!
//
function exposeTestFunctionNames() {
    return [
            "testExtractorInitI",
            "testExtractorGetI",
            "testExtractorGetII",
            "testExtractorGetIII",
            "testExtractorGetIV",
            "testStupidMethodI",
            "testGetterI",
            "testGetterII",
            "testGetterIII"
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
    domderrien.i18n.LabelExtractor._resetDictionary();
}

// JSUnit framework function
function tearDown() {
    // NOP
}
////////////// Required header - end////////////////////////////////

function testExtractorInitI() {
    var reportedError = null;
    var initialErrorReporter = domderrien.i18n.LabelExtractor._reportError;
    domderrien.i18n.LabelExtractor._reportError = function(message) { reportedError = message; };

    domderrien.i18n.LabelExtractor.init("anywhere", "anyfile", "uu_UU"); // Unsupported locale

    assertNotNull(reportedError);

    domderrien.i18n.LabelExtractor._reportError = initialErrorReporter;
}

function testExtractorGetI() {
    var lblExtr = domderrien.i18n.LabelExtractor.init("domderrien.i18n", "domderrien-labels", "uu_UU"); // Unsupported locale
    assertEquals("English", lblExtr.get("bundle_language"));
    assertEquals("N/A", lblExtr.get("unit_test_sample"));
}

function testExtractorGetII() {
    var lblExtr = domderrien.i18n.LabelExtractor.init("domderrien.i18n", "domderrien-labels", "fr"); // Supported locale
    assertEquals("Français", lblExtr.get("bundle_language"));
    assertEquals("N/A", lblExtr.get("unit_test_sample"));
}

function testExtractorGetIII() {
    var lblExtr = domderrien.i18n.LabelExtractor.init("domderrien.i18n", "domderrien-labels", "fr_CA"); // Supported locale
    assertEquals("Français Canadien", lblExtr.get("bundle_language"));
    assertEquals("N/A", lblExtr.get("unit_test_sample"));
}

function testExtractorGetIV() {
    var lblExtr = domderrien.i18n.LabelExtractor.init("domderrien.i18n", "domderrien-labels", "fr_BE"); // Unsupported locale
    assertEquals("Français", lblExtr.get("bundle_language"));
    assertEquals("N/A", lblExtr.get("unit_test_sample"));
}

function testStupidMethodI() {
    var nativeAlert = alert;
    var reportedError = null;
    alert = function(message) { reportedError = message; };

    domderrien.i18n.LabelExtractor._reportError("what?");

    assertNotNull(reportedError);
    assertEquals("what?", reportedError);

    window.alert = nativeAlert;
}

function testGetterI() {
    // No dictionary
    assertEquals("key-test", domderrien.i18n.LabelExtractor.get("key-test"));
}

function testGetterII() {
    // Unknown key
    domderrien.i18n.LabelExtractor.init("domderrien.i18n", "domderrien-labels", "en");
    assertEquals("key-test", domderrien.i18n.LabelExtractor.get("key-test"));
}

function testGetterIII() {
    domderrien.i18n.LabelExtractor.init("domderrien.i18n", "domderrien-labels", "en");
    var label = domderrien.i18n.LabelExtractor.get("unit_test_sample_with_parameters", ["one", "two"]);
    assertTrue(label.indexOf("one") != -1);
    assertTrue(label.indexOf("two") != -1);
}
