/////////////// Required header - beginning //////////////////////////
//
// WARNING:
//   When you add, update, or remove a test function, don't forget
//   to update accordingly the exposeTestFunctionNames() function!
//
function exposeTestFunctionNames() {
    return [
            "testInitialization",
            "testReportClientError"
    ];
}

function jscoverageRunner() {
    var testTitle = "TestConsole.js";
    var libDeps = ["twetailer.Console"];
    var testFunctions = exposeTestFunctionNames();

    jscoverageProcessor(testTitle, libDeps, testFunctions);
}

var _mockAlertBuffer = "";
var nativeAlertFunction = window.alert;

// JSUnit framework function
function setUp() {
    _mockAlertBuffer = "";
    window.alert = function(message) {
        _mockAlertBuffer = message;
    }
}

// JSUnit framework function
function tearDown() {
    window.alert = nativeAlertFunction;
}
////////////// Required header - end////////////////////////////////

function testInitialization() {
    var module = twetailer.Console;
    module.init("en", true);
    assertNotNull(module._labelExtractor);
}

function testReportClientError() {
    var module = twetailer.Console;

    assertEquals("", _mockAlertBuffer);

    var message = "unit test";
    var ioArgs = { url: "unit test" };
    module._reportClientError(message, ioArgs);

    // assertNotEquals("", _mockAlertBuffer); // assertNotEquals miss-interpreted by JsCoverage!
    assertEquals(domderrien.i18n.LabelExtractor.getFrom("console", "error_client_side_communication_failed"), _mockAlertBuffer);
}
