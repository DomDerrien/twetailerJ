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
        "testIsDigitIII",
        "testIsDigitIV",
        "testIsDigitV",
        "testIsHexaDigitI",
        "testIsHexaDigitII",
        "testIsHexaDigitIII",
        "testIsHexaDigitIV",
        "testIsNonAlphaI",
        "testIsNonAlphaII",
        "testIsNonAlphaIII",
        "testIsNonAlphaIV",
        "testConvertToHexaI",
        "testConvertToHexaII",
        "testConvertToHexaIII",
        "testStartsWithI",
        "testStartsWithII",
        "testStartsWithIII",
        "testStartsWithIV",
        "testStartsWithV",
        "testStartsWithVI"
    ];
}

function jscoverageRunner() {
    var testTitle = "TestStringUtils.js";
    var libDeps = ["domderrien.utils.StringUtils"];
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
    assertFalse(domderrien.utils.StringUtils.isDigit(null));
}

function testIsDigitII() {
    var samples = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertTrue(domderrien.utils.StringUtils.isDigit(samples[idx]));
    }
}

function testIsDigitIII() {
    var samples = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertTrue(domderrien.utils.StringUtils.isDigit(samples[idx]));
    }
}

function testIsDigitIV() {
    var samples = ["a", "Z", "�", "%", true, [0, 1, 2, 3], {1: 1, 2: 2}];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertFalse(domderrien.utils.StringUtils.isDigit(samples[idx]));
    }
}

function testIsDigitV() {
    assertFalse(domderrien.utils.StringUtils.isDigit("more than one character"));
}

function testIsHexaDigitI() {
    assertFalse(domderrien.utils.StringUtils.isHexaDigit(null));
}

function testIsHexaDigitII() {
    assertFalse(domderrien.utils.StringUtils.isHexaDigit("more that one character"));
}

function testIsHexaDigitIII() {
    var samples = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "A", "B", "C", "D", "E", "F"];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertTrue(domderrien.utils.StringUtils.isHexaDigit(samples[idx]));
    }
}

function testIsHexaDigitIV() {
    var samples = ["g", "Z", "�", "%", true, [0, 1, 2, 3], {1: 1, 2: 2}];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertFalse(domderrien.utils.StringUtils.isHexaDigit(samples[idx]));
    }
}

function testIsNonAlphaI() {
    assertTrue(domderrien.utils.StringUtils.isNonAlpha(null));
}

function testIsNonAlphaII() {
    assertTrue(domderrien.utils.StringUtils.isNonAlpha("more that one character"));
}

function testIsNonAlphaIII() {
    var samples = [".", " ", "�", "%", true, [0, 1, 2, 3], {1: 1, 2: 2}];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertTrue(domderrien.utils.StringUtils.isNonAlpha(samples[idx]));
    }
}

function testIsNonAlphaIV() {
    var samples = ["a", "b", "c", "h", "u", "z", "A", "B", "C", "H", "U", "Z"];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertFalse(domderrien.utils.StringUtils.isNonAlpha(samples[idx]));
    }
}

function testConvertToHexaI() {
    assertEquals("", domderrien.utils.StringUtils.convertToHexa("any string"));
}

function testConvertToHexaII() {
    var samples = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];
    var expected = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertEquals(expected[idx], domderrien.utils.StringUtils.convertToHexa(samples[idx]));
    }
}

function testConvertToHexaIII() {
    var samples = [10, 11, 12, 13, 14, 15, 16, 32, 139, 65535];
    var expected = ["a", "b", "c", "d", "e", "f", "10", "20", "8b", "ffff"];
    var idx = samples.length;
    while (0 < idx) {
        -- idx;
        assertEquals(expected[idx], domderrien.utils.StringUtils.convertToHexa(samples[idx]));
    }
}

function testStartsWithI() {
    // Nothing to compare
    assertFalse(domderrien.utils.StringUtils.startsWith(null, null, false));
}

function testStartsWithII() {
    // Nothing looked for
    assertTrue(domderrien.utils.StringUtils.startsWith("test", null, false));
}

function testStartsWithIII() {
    // Same start, same case
    assertTrue(domderrien.utils.StringUtils.startsWith("test", "te", false));
}

function testStartsWithIV() {
    // Same start, mixed case
    assertFalse(domderrien.utils.StringUtils.startsWith("test", "tE", false));
}

function testStartsWithV() {
    // Same start, mixed case
    assertTrue(domderrien.utils.StringUtils.startsWith("test", "tE", true));
}

function testStartsWithVI() {
    // Longer pattern
    assertFalse(domderrien.utils.StringUtils.startsWith("te", "test", false));
}
