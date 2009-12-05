if (!dojo._hasResource["domderrien.i18n.LanguageSelector"]) {
    dojo._hasResource["domderrien.i18n.LanguageSelector"] = true;

    (function() { // To limit the scope of the private variables

        dojo.require("dojo.data.ItemFileReadStore");
        dojo.require("dijit.form.FilteringSelect");

        /**
         * @author dom.derrien
         * @maintainer dom.derrien
         */
        var module = dojo.provide("domderrien.i18n.LanguageSelector");

        /**
         * Attach a <code>FilteringSelect</code> widget to the identified
         * DOM node. The widget <code>onChange</code> is connected to a
         * function refreshing the page with the identifier of the newly
         * selected language.
         *
         * @param {String} placeHolderId Identifier of the DOM that will be
         * replaced by the <code>FilteringSelect</code> widget.
         *
         * @param {String} formFieldName Value of the attribute <code>name</code>
         * for the underneath HTML element <code>&lt;select/&gt;</code>.
         *
         * @param {Array} options Array of objects, each object being composed of
         * the attributes {<code>abbreviation</code>, <name>name</code>} which
         * identify the language ISO code and the localized language label.
         *
         * @param {String} currentLanguageId ISO code of the language
         * to be initially selected
         *
         * @param {String} className Optional CSS class name for the widget to be created
         *
         * @param {Function} onChangeHandler Optional event handler attached
         * to the <code>onChange</code> event that the selector can generate. If
         * the handler is missing, the default behavior attached is the page reload
         * with the attribute <code>lang=&ltselected-value&gt;</code>.
         *
         */
        module.createSelector = function(placeHolderId, formFieldName, options, currentLanguageId, className, onChangeHandler){
            var languageStore = new dojo.data.ItemFileReadStore({
                data: {
                    identifier: "abbreviation",
                    label: "name",
                    items: options
                }
            });
            var _selector = new dijit.form.FilteringSelect({
                    autoComplete: false,
                    'class': className || "",
                    id: placeHolderId,
                    // onChange: _switchLanguage, // Should be postponed to avoid automatic URL update when the initial value is set
                    name: formFieldName || placeHolderId,
                    store: languageStore
                },
                placeHolderId
            );
            _selector.attr("value", currentLanguageId);
            dojo.connect(_selector, "onChange", _selector, onChangeHandler || _switchLanguage);
            return _selector;
        };

        /**
         * Event handler attach to the selector language and which is updating the
         * page URL with the identifier of the selected language ISO code
         *
         * @private
         */
        var _switchLanguage = function() {
            var urlParams = window.location.search;
            var newLanguage = this.attr("value");
            if (urlParams.indexOf("lang=") == -1) {
                urlParams += (urlParams.length == 0 ? "" : "&") + "lang=" + newLanguage;
            }
            else {
                urlParams = urlParams.replace(/lang=((\w\w_\w\w)|(\w\w))/g, "lang=" + newLanguage);
            }
            window.location.search = urlParams;
        };

    })(); // End of the function limiting the scope of the private variables
}
