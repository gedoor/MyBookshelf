//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import com.kunfei.bookshelf.constant.AppConstant;
import com.kunfei.bookshelf.help.AppFrontBackHelper;
import com.kunfei.bookshelf.help.CrashHandler;
import com.kunfei.bookshelf.help.FileHelp;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.utils.Prefs;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import io.reactivex.internal.functions.Functions;
import io.reactivex.plugins.RxJavaPlugins;

public class MApplication extends Application {
    public final static String channelIdDownload = "channel_download";
    public final static String channelIdReadAloud = "channel_read_aloud";
    public final static String[] PerList = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public final static int RESULT__PERMS = 263;
    public static String downloadPath;
    private static MApplication instance;
    private static String versionName;
    private static int versionCode;
    private SharedPreferences configPreferences;
    private boolean donateHb;

    public static MApplication getInstance() {
        return instance;
    }

    public static int getVersionCode() {
        return versionCode;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static Resources getAppResources() {
        return getInstance().getResources();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler.getInstance().init(this);
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = 0;
            versionName = "0.0.0";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIdDownload();
            createChannelIdReadAloud();
        }
        configPreferences = getSharedPreferences("CONFIG", 0);
        downloadPath = configPreferences.getString(getString(R.string.pk_download_path), "");
        if (TextUtils.isEmpty(downloadPath) | Objects.equals(downloadPath, FileHelp.getCachePath())) {
            setDownloadPath(null);
        }
        initNightTheme();
        if (!ThemeStore.isConfigured(this, versionCode)) {
            upThemeStore();
        }
        AppFrontBackHelper.getInstance().register(this, new AppFrontBackHelper.OnAppStatusListener() {
            @Override
            public void onFront() {
                donateHb = System.currentTimeMillis() - configPreferences.getLong("DonateHb", 0) <= TimeUnit.DAYS.toMillis(3);
            }

            @Override
            public void onBack() {
                if (UpLastChapterModel.model != null) {
                    UpLastChapterModel.model.onDestroy();
                }
            }
        });
        Prefs.init();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void initNightTheme() {
        if (isNightTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * 初始化主题
     */
    public void upThemeStore() {
        if (isNightTheme()) {
            ThemeStore.editTheme(this)
                    .primaryColor(configPreferences.getInt("colorPrimaryNight", getResources().getColor(R.color.md_grey_800)))
                    .accentColor(configPreferences.getInt("colorAccentNight", getResources().getColor(R.color.md_pink_800)))
                    .backgroundColor(configPreferences.getInt("colorBackgroundNight", getResources().getColor(R.color.md_grey_800)))
                    .apply();
        } else {
            ThemeStore.editTheme(this)
                    .primaryColor(configPreferences.getInt("colorPrimary", getResources().getColor(R.color.md_grey_100)))
                    .accentColor(configPreferences.getInt("colorAccent", getResources().getColor(R.color.md_pink_600)))
                    .backgroundColor(configPreferences.getInt("colorBackground", getResources().getColor(R.color.md_grey_100)))
                    .apply();
        }
    }

    public boolean isNightTheme() {
        return configPreferences.getBoolean("nightTheme", false);
    }

    /**
     * 设置下载地址
     */
    public void setDownloadPath(String path) {
        if (TextUtils.isEmpty(path)) {
            downloadPath = FileHelp.getFilesPath();
        } else {
            downloadPath = path;
        }
        AppConstant.BOOK_CACHE_PATH = downloadPath + File.separator + "book_cache" + File.separator;
        configPreferences.edit()
                .putString(getString(R.string.pk_download_path), path)
                .apply();
    }

    public static SharedPreferences getConfigPreferences() {
        return getInstance().configPreferences;
    }

    public boolean getDonateHb() {
        return donateHb || BuildConfig.DEBUG;
    }

    public void upDonateHb() {
        configPreferences.edit()
                .putLong("DonateHb", System.currentTimeMillis())
                .apply();
        donateHb = true;
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
