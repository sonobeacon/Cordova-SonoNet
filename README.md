# SonoNet-Cordova

## Information

This repository provides installation instructions for the Sonobeacon Cordova plugin and a sample app for more details on how to integrate the plugin in your app.

We will provide you with the apiKey that you need to use this plugin.

## DemoApp

The Demo app provides a simple template as an enviroment to test and troubleshoot the plugin.

### Usage

- Clone the repo
- create the platform you want to use (iOS / Android) with 'cordova platform add ios/android'
- follow the installation instructions below to add the plugin to the app.

The index.js file in the www folder has been set up for the plugin to start on app startup. Simply fill in your ApiKey.

## Content

Content is managed in [the Backend](https://www.admin.sonobeacon.com/). Here customers have the ability to set up geofences, bluetooth beacons and sonobeacons in an easy and straight forward way.

## Bluetooth and Geofence Events

Events regarding bluetooth and geofences are now passed back up to the js level as json. They have the following syntax:

```javascript
//BLE
{
  "enterOrExit" : "ENTER/EXIT",
  "bleId" : "BLEID"
}
//GEOFENCE
{
  "enterOrExit" : "ENTER/EXIT",
  "lat" : "LAT",
  "long" : "LONG"
}
```

Push-Notifications are shown when app is **not** in forground (iOS only, Android will show them even when app is in use), that is in background or terminated.

Entry- & Exit-Urls are called regardless of appState. The calls made are GET-request.

## Installation

After we've provided you with the plugin, simply add the plugin to your existing cordova app

```
cordova plugin add ../plugin/
```
where '../plugin/' is the relative path from your app to the plugin's location.

### Call the Plugin from your index.js
```javascript
//Example: cordova.plugins.SonoNetPlugin.initialize("1234", true, true, true, ...
cordova.plugins.SonoNetPlugin.initialize("APIKEY", true, true, true,
        function(response){
            console.log(response);
            if (response == "bindSuccess") {
                cordova.plugins.SonoNetPlugin.beaconCallback(function(response) {
                    console.log(JSON.stringify(response)); // use beacon data
                });
                cordova.plugins.SonoNetPlugin.eventCallback(function(response) {
                    console.log(JSON.stringify(response)); // use event data
                })
            }
        }, function(error){
            console.log(error);
        });
```

| Parameter            | type    | explanation                                                                   |
|----------------------|---------|-------------------------------------------------------------------------------|
| ApiKey               | String  | your apiKey (mandatory)                                                       |
| debugMode            | boolean | whether you want the api to log debugging information in the console          |
| reveiceNotifications | boolean | whether you want to receive notifications from geofences and bluetooth beacons|
| bluetoothOnly        | boolean | whether you want to use ultrasound detection or just bluetooth tracking       |

Within the *response* function, process the obtained data according to your needs. The object *response* is a JSON containing the id, title and url of the beacon that was just detected.

The *response* function only puts out data when ultrasound detection with a sonobeacon device is used. Functionalities like geofences and push notification do not get forwarded back up to the javascript level.

## Platform configuration to proper integrate the plugin

### iOS

in Xcode under Targets -> Signing & Capabilities, check 'Location Updates' under 'Background Modes'.

**Hint:** Background Mode may have to be added if not already in use by selecting '+ Capabilities'.

If under Build Settings -> Swift Compiler - Language, the *Swift Language Version* is set to **undefined**, select one of the drop down choices.

### Android

In your project-level build.gradle, add the kotlin verion number and classpath like below:
```gradle
buildscript {
  ext.kotlin_version = '1.3.30'
  repositories {
  	..
  }
  dependencies {
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
  	..
  }
}
```

and set the minmum sdk-version to 21:
```gradle
allprojects {
    repositories {
        ..
    }
    project.ext {
      ..
      defaultMinSdkVersion=21
      ..
    }
}
```

In your app-level build.gradle, add kotlin below *apply plugin: 'com.android.application'*
```gradle
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'
```
Still in your app-level build.gradle, add the following dependencies:
```gradle
dependencies {
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'com.google.android.material:material:1.2.1'
    implementation 'androidx.room:room-runtime:2.2.5'
    implementation 'com.google.android.gms:play-services-location:17.1.0'
    implementation "androidx.core:core-ktx:1.3.2"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0'
    implementation 'androidx.room:room-runtime:2.2.5'
    implementation 'androidx.room:room-ktx:2.2.5'
    implementation 'androidx.localbroadcastmanager:localbroadcastmanager:1.0.0'
    // SUB-PROJECT DEPENDENCIES START
    ..
    // SUB-PROJECT DEPENDENCIES END

}
```

In your gradle.properties, add these two lines:
```gradle
android.useAndroidX=true
android.enableJetifier=true
```

You also need to modify your AndroidManifest file by adding following service and receiver, v1.1.0 adds the needed permissions automatically. Below are the changes you need to do by hand:
```xml
<manifest>
	...
  <application>
    ...
    <service android:label="dacDetect" android:name="com.sonobeacon.system.sonolib.core.BeaconService" />
    <receiver
      android:name="com.sonobeacon.system.sonolib.location.GeofenceBroadcastReceiver"
      android:enabled="true"
      android:exported="true" />

    <service
      android:name="com.sonobeacon.system.sonolib.location.GeofenceTransitionsJobIntentService"
      android:exported="true"
      android:permission="android.permission.BIND_JOB_SERVICE" />
  </application>
</manifest>
```

If done correctly, the BootstrapApplication class should be registered in AndroidManifest.xml as the Application class. When encountering issues, first check that this is indeed the case.

If your app has its own Application class, copy and paste the contents of the [plugin's class](Plugin/src/android/BootstrapApplication.java) into yours.

If you are experiencing difficulties with the hook script adding the permissions, refer to the README from branch v1.0.0.
