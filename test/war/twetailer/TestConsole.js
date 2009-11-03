/////////////// Required header - beginning //////////////////////////
//
// WARNING:
//   When you add, update, or remove a test function, don't forget
//   to update accordingly the exposeTestFunctionNames() function!
//
function exposeTestFunctionNames() {
    return [
            "testInitialization"
    ];
}

function jscoverageRunner() {
    var testTitle = "TestConsole.js";
    var libDeps = ["twetailer.Console"];
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

function testInitialization() {
    var module = twetailer.Console;
    var tempConfig = module._appConfig;
    module.init("master", "en", true);
}
