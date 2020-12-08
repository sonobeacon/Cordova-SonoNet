package com.sonobeacon.cordova.plugin;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.sonobeacon.system.sonolib.core.OnCompletionListener;
import com.sonobeacon.system.sonolib.core.SonoNet;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public class BootstrapApplication extends Application implements BootstrapNotifier, OnCompletionListener {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fireEvent(intent.getAction().toString(), intent.getStringExtra("reminder_id"));
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction("ENTER");
        filter.addAction("EXIT");
        registerReceiver(receiver, filter);

        BeaconManager beaconmanager = BeaconManager.getInstanceForApplication(this);
        beaconmanager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Background beacon monitoring is activated");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }

        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
        RunningAverageRssiFilter.setSampleExpirationMilliseconds(5000L);
        beaconmanager.enableForegroundServiceScanning(builder.build(),456);
        beaconmanager.setEnableScheduledScanJobs(false);
        beaconmanager.setBackgroundBetweenScanPeriod(0);
        beaconmanager.setBackgroundScanPeriod(1100);
        Region region = new Region("default region", null, null, null);
        new RegionBootstrap(this, region);
    }

    void fireEvent(String enterAction, String id) {
        SonoNet.Companion.regionEvent(this, enterAction, id);
        SonoNet.Companion.formatRegionInformationToJson(this, id, enterAction, this);
    }

    @Override
    public void onComplete(String resultJson) {
        sendCallback(resultJson);
    }

    void sendCallback(String json) {
        CallbackContext context = SonoNetPlugin.eventCallBackContext;
        if (context != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, json);
            result.setKeepCallback(true);
            context.sendPluginResult(result);
        }
    }

    @Override
    public void didEnterRegion(Region region) {
        if (!region.getUniqueId().equals("default region")) {
            Log.d("RegionBootstrap", "didEnterRegion: " + region.getUniqueId());
            SonoNet.Companion.regionEvent(this, "ENTER", region.getUniqueId());
            SonoNet.Companion.formatRegionInformationToJson(this, region.getUniqueId(), "ENTER", this);
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if (!region.getUniqueId().equals("default region")) {
            Log.d("RegionBootstrap", "didExitRegion: " + region.getUniqueId());
            SonoNet.Companion.regionEvent(this, "EXIT", region.getUniqueId());
            SonoNet.Companion.formatRegionInformationToJson(this, region.getUniqueId(), "EXIT", this);
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        Log.d("RegionBootstrap", "didDetermineStateForRegion: " + region.getUniqueId());
    }
}