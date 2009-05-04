/////////////// Required header - beginning //////////////////////////
//
// WARNING:
//   When you add, update, or remove a test function, don't forget
//   to update accordingly the exposeTestFunctionNames() function!
//
function exposeTestFunctionNames() {
	return [
        "testIsDigitI",
        "testIsDigitII",
        "testIsDigitIII"
    ];
}

function jscoverageRunner() {
	var testTitle = "TestStringUtils.js";
	var libDeps = ["domderrien.util.StringUtils"];
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
function testIsDigitI() {
	var samples = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];
	var idx = samples.length;
	while (0 < idx) {
		-- idx;
		assertTrue(domderrien.util.StringUtils.isDigit(samples[idx]));
	}
}

function testIsDigitII() {
	var samples = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];
	var idx = samples.length;
	while (0 < idx) {
		-- idx;
		assertTrue(domderrien.util.StringUtils.isDigit(samples[idx]));
	}
}

function testIsDigitIII() {
	var samples = ["a", "Z", "�", "%", true, [0, 1, 2, 3], {1: 1, 2: 2}];
	var idx = samples.length;
	while (0 < idx) {
		-- idx;
		assertFalse(domderrien.util.StringUtils.isDigit(samples[idx]));
	}
}
