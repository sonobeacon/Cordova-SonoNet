#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');

// here add/remove permissions you need for Android app
let permissions = [
'android.permission.RECORD_AUDIO',
'android.permission.INTERNET',
'android.permission.BLUETOOTH',
'android.permission.BLUETOOTH_ADMIN',
'android.permission.ACCESS_COARSE_LOCATION',
'android.permission.ACCESS_FINE_LOCATION'
];


module.exports = function (context) {
  var platformRoot = path.join(context.opts.projectRoot, 'platforms/android');
  var manifestFile = path.join(platformRoot, 'app/src/main/AndroidManifest.xml');

  if(fs.existsSync(manifestFile)) {
    fs.readFile(manifestFile, 'utf8', function (err, data) {
      if (err) {
        throw new Error('unable to read AndroidManifest.xml: ' + err);
      }
      var result;
      var stringOfPermissions = "";
      permissions.forEach(function(permission) {
        if (data.search(permission) == -1) {
          stringOfPermissions += '<uses-permission android:name=' + '"' + permission + '" ' + '/>\n';
        }
      });
      var index = data.indexOf('<uses-permission');
      if (index == -1) {
        index = data.indexOf('<application');
      }
      result = data.splice(index, 0, stringOfPermissions);
      fs.writeFile(manifestFile, result, 'utf8', function (err) {
        if (err) throw new Error('Unable to write to AndroidManifest.xml: ' + err);
      });
    });
  }
};


if (!String.prototype.splice) {
    /**
     * {JSDoc}
     *
     * The splice() method changes the content of a string by removing a range of
     * characters and/or adding new characters.
     *
     * @this {String}
     * @param {number} start Index at which to start changing the string.
     * @param {number} delCount An integer indicating the number of old chars to remove.
     * @param {string} newSubStr The String that is spliced in.
     * @return {string} A new string with the spliced substring.
     */
     String.prototype.splice = function(start, delCount, newSubStr) {
      return this.slice(0, start) + newSubStr + this.slice(start + Math.abs(delCount));
    };
  }