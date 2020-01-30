package com.sonobeacon.cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import com.sonobeacon.system.sonolib.SonoNet;
import com.sonobeacon.system.sonolib.SonoNetCredentials;
import com.sonobeacon.system.sonolib.WebLink;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import org.apache.cordova.PluginResult;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SonoNetPlugin extends CordovaPlugin implements SonoNet.BeaconInfoDelegate {

    Context context;
    CallbackContext initCallbackContext;
    CallbackContext beaconCallbackContext;
    boolean isDebugging;
    boolean notifyMe;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initialize")) {
            String apiKey = args.getString(0);
            String locationId = args.getString(1);
            isDebugging = args.getBoolean(2);
            notifyMe = args.getBoolean(3);
            if (locationId.equals("null")) locationId = null;
            this.initialize(apiKey, locationId, callbackContext);
            return true;
        } else if (action.equals("beaconCallback")) {
            this.beaconCallback(callbackContext);
            return true;
        }
        return false;
    }

    private void initialize(String apiKey, String locationId, CallbackContext callbackContext) {
        initCallbackContext = callbackContext;
        context = cordova.getActivity().getWindow().getContext();
        
        final String recordAudio = Manifest.permission.RECORD_AUDIO;
        final String coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
        final String fineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String [] permissions = { recordAudio, coarseLocation, fineLocation };
        final int SEARCH_REQ_CODE = 0;
        
        SonoNetCredentials credentials = new SonoNetCredentials(apiKey, locationId);
        SonoNet.initialize(context, credentials);
        
        if (apiKey == null || apiKey.length() <= 0) {
            initCallbackContext.error("ApiKey must be provided");
            return;
        }

        if(!cordova.hasPermission(recordAudio)) {
            cordova.requestPermissions(this, SEARCH_REQ_CODE, permissions);
        } else {
            bind();
        }
    }

    private void bind() {
        SonoNet.Control control = new SonoNet.Control.Builder(context)
                .isDebugging(isDebugging)
                .notifyMe(notifyMe)
                .build();

        control.bind(this);
        PluginResult result = new PluginResult(PluginResult.Status.OK, "bindSuccess");
        initCallbackContext.sendPluginResult(result);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
       int[] grantResults) throws JSONException {
        for(int r : grantResults) {
            if(r == PackageManager.PERMISSION_DENIED) {
                this.initCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                return;
            }
        }
        bind();
    }

    @Override
    public void onBeaconReceivedLinkPayload(WebLink webLink) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", webLink.getId());
            jsonObject.put("url", webLink.getUrl());
            jsonObject.put("title", webLink.getTitle());
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
        result.setKeepCallback(true);
        beaconCallbackContext.sendPluginResult(result);
    }

    private void beaconCallback(CallbackContext beaconCallbackContext) {
        this.beaconCallbackContext = beaconCallbackContext;
    }

}

