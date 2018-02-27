package com.monke.monkeybook.help;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.monke.monkeybook.MApplication;

/**
 * Created by GKF on 2018/2/27.
 * 更换图标
 */

public class LauncherIcon {
    public static void Change() {
        PackageManager packageManager = MApplication.getInstance().getPackageManager();
        ComponentName componentNameMain = new ComponentName(MApplication.getInstance(), "com.monke.monkeybook.view.activity.WelcomeActivity");
        ComponentName componentNameBook = new ComponentName(MApplication.getInstance(), "com.monke.monkeybook.BookIcon");

        if (packageManager.getComponentEnabledSetting(componentNameBook) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            packageManager.setComponentEnabledSetting(componentNameMain,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(componentNameBook,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(componentNameBook,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(componentNameMain,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
