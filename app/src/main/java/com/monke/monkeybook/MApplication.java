//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.monke.monkeybook.help.FileHelp;

import pub.devrel.easypermissions.EasyPermissions;

public class MApplication extends Application {
    public final static boolean DEBUG = BuildConfig.DEBUG;
    public final static String channelIdDownload = "channel_download";
    public final static String channelIdReadAloud = "channel_read_aloud";
    public final static String[] PerList = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public final static int RESULT__PERMS = 263;
    private static MApplication instance;
    private static String versionName;
    private static int versionCode;
    public static String downloadPath;

    public static MApplication getInstance() {
        return instance;
    }

    public static int getVersionCode() {
        return versionCode;
    }

    public static String getVersionName() {
        return versionName;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = 0;
            versionName = "0.0.0";
            e.printStackTrace();
        }
        instance = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIdDownload();
            createChannelIdReadAloud();
        }
        SharedPreferences sharedPreferences = getSharedPreferences("CONFIG", 0);
        if (sharedPreferences.getString(getString(R.string.pk_download_path), "").equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pk_download_path), FileHelp.getCachePath());
            editor.apply();
        }
        downloadPath = sharedPreferences.getString(getString(R.string.pk_download_path), FileHelp.getCachePath());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannelIdDownload() {
        //用唯一的ID创建渠道对象
        NotificationChannel firstChannel = new NotificationChannel(channelIdDownload,
                getString(R.string.download_offline),
                NotificationManager.IMPORTANCE_LOW);
        //初始化channel
        firstChannel.enableLights(false);
        firstChannel.enableVibration(false);
        firstChannel.setSound(null, null);
        //向notification manager 提交channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(firstChannel);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannelIdReadAloud() {
        //用唯一的ID创建渠道对象
        NotificationChannel firstChannel = new NotificationChannel(channelIdReadAloud,
                getString(R.string.read_aloud),
                NotificationManager.IMPORTANCE_LOW);
        //初始化channel
        firstChannel.enableLights(false);
        firstChannel.enableVibration(false);
        firstChannel.setSound(null, null);
        //向notification manager 提交channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(firstChannel);
        }
    }
}
