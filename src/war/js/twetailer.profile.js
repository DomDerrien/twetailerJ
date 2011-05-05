dependencies = {

    layers: [
        {
            name: "../ase/listing.js",
            resourceName: "ase.listing",
            layerDependencies: [
            ],
            dependencies: [
                'dojo.data.ItemFileWriteStore',
                'dojo.parser',
                'dijit.Dialog',
                'dijit.layout.BorderContainer',
                'dijit.layout.ContentPane',
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
        }
    ],

    prefixes: [
        [ "dijit", "../dijit" ],
        [ "dojox", "../dojox" ],
        [ "domderrien", "../../domderrien" ],
        [ "twetailer", "../../twetailer" ]
    ]
}
