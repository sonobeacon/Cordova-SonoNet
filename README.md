# SonoNet-Cordova

## Information

This repository provides installation instructions for the Sonobeacon Cordova plugin and a sample app for more details on how to integrate the plugin in your app.

We will provide you with the apiKey that you need to use this plugin.

## DemoApp

The Demo app provides a simple template as an enviroment to test and troubleshoot the plugin.

### Usage:

- Clone the repo
- create the platform you want to use (iOS / Android) with 'cordova platform add ios/android'
- follow the installation instructions below to add the plugin to the app. 

The index.js file in the www folder has been set up for the plugin to start on app startup. Simply fill in your ApiKey.

## Installation


Add the plugin to your existing cordova app

via npm:
```
npm install sononetplugin //not working yet, coming soon
```
Or clone the this repository and add it locally:
```
cordova plugin add ../plugin/
```


### Call the Plugin from your index.js
```javascript
//Example: cordova.plugins.SonoNetPlugin.initialize("1234", null, true, true, ... 
cordova.plugins.SonoNetPlugin.initialize("ApiKey", "locationId", "debugMode", "receiveNotification", 
        function(response){
            console.log(response);
            if (response == "bindSuccess") {
                cordova.plugins.SonoNetPlugin.beaconCallback(function(response){
                    console.log(JSON.stringify(response)); // use beacon data
                });
            }
        }, function(error){
            console.log(error);
        });
```

| Parameter            | type    | explanation                                               |
|----------------------|---------|-----------------------------------------------------------|
| ApiKey               | String  | your apiKey (mandatory)                                   |
| locationId           | String  | your locationId (optional)                                |
| debugMode            | boolean | whether you want the api to put out debugging information |
| reveiceNotifications | boolean | whether you want to receive notifications from geofences  |

Within the *response* function, process the obtained data according to your needs. The object *response* is a JSON containing the id, title and url of the beacon that was just detected.

The *response* function only puts out data when ultrasound detection with a sonobeacon device is used. Functionalities like geofences and push notification do not get forwarded back up to the javascript level.

## Platform configuration to proper integrate the plugin

### iOS

in Xcode under Targets -> Signing & Capabilities, check 'Location Updates' under 'Background Modes'.

**Hint:** Background Mode may have to be added if not already in use by selecting '+ Capabilities'.

Then under Build Settings -> Swift Compiler - Language, set *Swift Language Version* to Swift 4.

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
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'com.google.android.material:material:1.0.0'
    implementation 'org.altbeacon:android-beacon-library:2.15.1'
    implementation 'androidx.room:room-runtime:2.2.0'
    implementation 'com.google.android.gms:play-services-location:17.0.0'
    implementation "androidx.core:core-ktx:1.0.1"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
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

You also need to modify your AndroidManifest file by adding following service and receiver, v1.1.0 adds the needed permissions automatically:
```xml
<manifest>
	...
  <application>
    ...
    <service android:label="dacDetect" android:name="com.sonobeacon.system.sonolib.BeaconInfoService" />
    <receiver
      android:name="com.sonobeacon.system.sonolib.GeofenceBroadcastReceiver"
      android:enabled="true"
      android:exported="true" />

    <service
      android:name="com.sonobeacon.system.sonolib.GeofenceTransitionsJobIntentService"
      android:exported="true"
      android:permission="android.permission.BIND_JOB_SERVICE" />
  </application>
</manifest>
```

If done correctly, the BootstrapApplication class should be registered in AndroidManifest.xml as the Application class. When encountering issues, first check that this is indeed the case.

If you are experiencing difficulties with the hook script adding the permissions, refer to the README from v1.0.0 branch.
