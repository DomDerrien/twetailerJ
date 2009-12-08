/////////////// Required header - beginning //////////////////////////
//
// WARNING:
//   When you add, update, or remove a test function, don't forget
//   to update accordingly the exposeTestFunctionNames() function!
//
function exposeTestFunctionNames() {
    return [
            "testCreationWithDefaultHandler",
            "testCreationWithCustomHandler",
            "testReplaceLanguageI",
            "testReplaceLanguageII",
            "testReplaceLanguageIII",
            "testReplaceLanguageIV"
    ];
}

function jscoverageRunner() {
    var testTitle = "TestLanguageSelector.js";
    var libDeps = ["domderrien.i18n.LanguageSelector", "dijit._Widget"];
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

function testCreationWithDefaultHandler() {
    // Definitions
    var id = "testI";
    var name = "formField";
    var options = [
           {abbreviation: "fr", name: "Français"},
           {abbreviation: "en", name: "English"}
    ];
    var cssClassName = "test";

    // Setup with the DOM node that will receive the <FilterSelect/> box
    var anchorNode = dojo.doc.createElement("input");
    anchorNode.id = id;
    dojo.doc.body.appendChild(anchorNode);

    // Execution
    var selector = domderrien.i18n.LanguageSelector.createSelector(
            id,
            name,
            options,
            "fr",
            cssClassName,
            null
    );

    // Verification
    var selectBox = dijit.byId(id);
    assertEquals(selector, selectBox);
    assertEquals("fr", selectBox.attr("value"));
    assertEquals("Français", selectBox.attr("displayedValue"));

    // Clean-up
    selector.destroyRecursive();
}

function testCreationWithCustomHandler() {
    // Definitions
    var id = "testI";
    var name = "formField";
    var options = [
           {abbreviation: "fr", name: "Français"},
           {abbreviation: "en", name: "English"}
    ];
    var cssClassName = "test";
    var selectedKey = "fr";
    var eventHandler = function(selection) {
        selectedKey = selection;
    }

    // Setup with the DOM node that will receive the <FilterSelect/> box
    var anchorNode = dojo.doc.createElement("input");
    anchorNode.id = id;
    dojo.doc.body.appendChild(anchorNode);

    // Execution
    var selector = domderrien.i18n.LanguageSelector.createSelector(
            id,
            name,
            options,
            "fr",
            cssClassName,
            eventHandler
    );
    selector.attr("value", "en");

    // Verification
    assertEquals("en", selectedKey);

    // Clean-up
    selector.destroyRecursive();
}

function testReplaceLanguageI() {
    var urlParams = "";

    var newParams = domderrien.i18n.LanguageSelector._replaceLanguage(urlParams, "en");

    assertEquals("?lang=en", newParams);
}

function testReplaceLanguageII() {
    var urlParams = "?test";

    var newParams = domderrien.i18n.LanguageSelector._replaceLanguage(urlParams, "en");

    assertEquals("?test&lang=en", newParams);
}

function testReplaceLanguageIII() {
    var urlParams = "?lang=fr";

    var newParams = domderrien.i18n.LanguageSelector._replaceLanguage(urlParams, "en");

    assertEquals("?lang=en", newParams);
}

function testReplaceLanguageIV() {
    var urlParams = "?test=1&lang=fr_CA&test=2";

    var newParams = domderrien.i18n.LanguageSelector._replaceLanguage(urlParams, "en");

    assertEquals("?test=1&lang=en&test=2", newParams);
}
