cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "cordova-plugin-sononetplugin.SonoNetPlugin",
      "file": "plugins/cordova-plugin-sononetplugin/www/SonoNetPlugin.js",
      "pluginId": "cordova-plugin-sononetplugin",
      "clobbers": [
        "cordova.plugins.SonoNetPlugin"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.4",
    "cordova-plugin-sononetplugin": "0.0.1"
  };
});