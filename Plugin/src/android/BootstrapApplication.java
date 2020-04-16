package com.sonobeacon.cordova.plugin;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.sonobeacon.system.sonolib.SonoNet;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class BootstrapApplication extends Application implements BootstrapNotifier {

    @Override
    public void onCreate() {
        super.onCreate();
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
        Region region = new Region("region", null, null, null);

        new RegionBootstrap(this, region);
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void didEnterRegion(Region region) {
        SonoNet.getInstance().sendNotification(region.getUniqueId());
        Log.d("RegionBootstrap", "didEnterRegion: " + region.getUniqueId());
    }

    @Override
    public void didExitRegion(Region region) {
        Log.d("RegionBootstrap", "didExitRegion: " + region.getUniqueId());
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        Log.d("RegionBootstrap", "didDetermineStateForRegion: " + region.getUniqueId());
    }
}
