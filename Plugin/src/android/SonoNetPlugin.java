package com.sonobeacon.cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import com.sonobeacon.system.sonolib.core.SonoNet;
import com.sonobeacon.system.sonolib.models.SonoNetCredentials;
import com.sonobeacon.system.sonolib.models.WebLink;
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
	static CallbackContext eventCallBackContext;
	boolean isDebugging = true;
	boolean notifyMe = true;
	boolean bluetoothOnly = false;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.e("ACTION", action);
		switch (action) {
			case "initialize":
				String apiKey = args.getString(0);
				isDebugging = args.getBoolean(1);
				notifyMe = args.getBoolean(2);
				bluetoothOnly = args.getBoolean(3);
				this.initialize(apiKey, callbackContext);
				return true;
			case "beaconCallback":
				this.beaconCallback(callbackContext);
				return true;
			case "eventCallback":
				this.eventCallBack(callbackContext);
				return true;
		}
		return false;
	}

	private void initialize(String apiKey, CallbackContext callbackContext) {
		initCallbackContext = callbackContext;
		context = cordova.getActivity().getWindow().getContext();

		final String recordAudio = Manifest.permission.RECORD_AUDIO;
		final String coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
		final String fineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
		String [] permissions = { recordAudio, coarseLocation, fineLocation };
		final int SEARCH_REQ_CODE = 0;

		SonoNetCredentials credentials = new SonoNetCredentials(apiKey);

		SonoNet.Companion.initialize(context, credentials);

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
		SonoNet.Control control = new SonoNet.Control(
		context,
		null,			//contentView
		false,			//with menu
		isDebugging,	//debugMode
		notifyMe,		//notifications
		bluetoothOnly,	//bluetoothOnlyMode
		true,			// showMenuItemOnlyOnce - not applicable here
		14f,				// menuFontSize - not applicable here
		"000000");			//MenuTextColor - not applicable here

		control.bind(this);
		PluginResult result = new PluginResult(PluginResult.Status.OK, "bindSuccess");
		initCallbackContext.sendPluginResult(result);
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions,
		int[] grantResults) {
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
			Log.i("JSON", e.getMessage());
		}
		PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
		result.setKeepCallback(true);
		beaconCallbackContext.sendPluginResult(result);
	}

	private void beaconCallback(CallbackContext beaconCallbackContext) {
		this.beaconCallbackContext = beaconCallbackContext;
	}

	private void eventCallBack(CallbackContext eventCallBackContext) {
		this.eventCallBackContext = eventCallBackContext;
	}
}