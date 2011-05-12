dependencies = {

    layers: [
             {
                 name: "../ase/_admin.js",
                 resourceName: "ase._admin",
                 layerDependencies: [
                 ],
                 dependencies: [
                     'dojo.data.ItemFileWriteStore',
                     'dojo.parser',
                     'dijit.Dialog',
                     'dijit.layout.BorderContainer',
                     'dijit.layout.ContentPane',
                     'dijit.layout.StackContainer',
                     'dijit.form.Button',
                     'dijit.form.CheckBox',
                     'dijit.form.ComboBox',
                     'dijit.form.DateTextBox',
                     'dijit.form.Form',
                     'dijit.form.NumberTextBox',
                     'dijit.form.Select',
                     'dijit.form.Textarea',
                     'dijit.form.TextBox',
                     'dijit.tree.ForestStoreModel',
                     'dojox.analytics.Urchin',
                     'dojox.data.JsonRestStore',
                     'dojox.grid.TreeGrid',
                     'twetailer.Common'
                 ]
             },
             {
                 name: "../ase/login.js",
                 resourceName: "ase.login",
                 layerDependencies: [
                 ],
                 dependencies: [
                     'dojo.parser',
                     'dijit.Dialog',
                     'dijit.layout.BorderContainer',
                     'dijit.layout.ContentPane',
                     'dijit.form.Button',
                     'dijit.form.Form',
                     'dijit.form.Select',
                     'dijit.form.TextBox',
                     'dojox.analytics.Urchin',
                     'domderrien.i18n.LanguageSelector'
                 ]
             },
        {
            name: "../ase/stores.js",
            resourceName: "ase.stores",
            layerDependencies: [
            ],
            dependencies: [
                'dojo.fx',
                'dojo.fx.easing',
                'dojo.parser',
                'dijit.layout.BorderContainer',
                'dijit.layout.ContentPane',
                'dijit.form.Button',
                'dijit.form.Form',
                'dijit.form.NumberSpinner',
                'dijit.form.Select',
                'dijit.form.TimeTextBox',
                'dijit.form.ValidationTextBox',
                'dojox.analytics.Urchin',
                'dojox.layout.ExpandoPane',
                'dojox.widget.Standby',
                'twetailer.Common'
            ]
        }
    ],

    prefixes: [
        [ "dijit", "../dijit" ],
        [ "dojox", "../dojox" ],
        [ "domderrien", "../../domderrien" ],
        [ "twetailer", "../../twetailer" ]
    ]
}
