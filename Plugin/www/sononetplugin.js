var exec = require('cordova/exec');

exports.initialize = function (arg0, arg1, arg2, arg3, arg4, success, error) {
    exec(success, error, 'SonoNetPlugin', 'initialize', [arg0, arg1, arg2, arg3, arg4]);
};

exports.beaconCallback = function (success) {
    exec(success, null, 'SonoNetPlugin', 'beaconCallback', []);
};