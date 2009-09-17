// Stubs of JSUnit functions
function fail() {};
function assertTrue() {};
function assertFalse() {};
function assertEquals() {};
function assertNull() {};
function assertNotNull() {};

// Generic function exercizing all given functions
function jscoverageProcessor(title, deps, functions) {
    document.write("<h1>" + title + "</h1><p>");

    for(var i=0, limit=deps.length; i<limit; i++)   {
        dojo.require(deps[i]);
    }

    for (var i=0, limit=functions.length; i<limit; i++) {
        setUp();
        window[functions[i]]();
        tearDown();
        document.write(i + ".");
    }
    document.write("</p>");
}

function jscoverageOpenSummary() {
    /* Not activate because the access from docs/index/html have nasty side-effects.
    var elem = window.top.document.getElementById("summaryTab");
    if (elem == null) {
        document.getElementById('openReport').click();
    }
    else if (window.top.tab_click != null) {
        window.top.tab_click({target:{id:'summaryTab'}});
    }
    */
}
