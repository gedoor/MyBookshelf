//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.monke.monkeybook.service.DownloadService;

public class MApplication extends Application {
    private static MApplication instance;
    private static String versionName;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.IS_RELEASE) {
            String channel = "debug";
            try {
                ApplicationInfo appInfo = getPackageManager()
                        .getApplicationInfo(getPackageName(),
                                PackageManager.GET_META_DATA);
                channel = appInfo.metaData.getString("UMENG_CHANNEL_VALUE");
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                versionName = "0.0.0";
                e.printStackTrace();
            }
        }
        instance = this;
        startService(new Intent(this, DownloadService.class));
    }

    public static MApplication getInstance() {
        return instance;
    }

    public static String getVersionName() {
        return versionName;
    }

}
