package com.kunfei.bookshelf.help;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;

/**
 * Created by GKF on 2018/2/27.
 * 更换图标
 */

public class LauncherIcon {
    private static PackageManager packageManager = MApplication.getInstance().getPackageManager();
    private static ComponentName componentNameMain = new ComponentName(MApplication.getInstance(), "com.kunfei.bookshelf.view.activity.WelcomeActivity");
    private static ComponentName componentNameBookMain = new ComponentName(MApplication.getInstance(), "com.kunfei.bookshelf.view.activity.WelcomeBookActivity");

    public static void ChangeIcon(String icon) {

        if (icon.equals(MApplication.getInstance().getString(R.string.icon_book))) {
            if (packageManager.getComponentEnabledSetting(componentNameBookMain) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                //启用
                packageManager.setComponentEnabledSetting(componentNameBookMain,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                //禁用
                packageManager.setComponentEnabledSetting(componentNameMain,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        } else {
            if (packageManager.getComponentEnabledSetting(componentNameMain) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                //启用
                packageManager.setComponentEnabledSetting(componentNameMain,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                //禁用
                packageManager.setComponentEnabledSetting(componentNameBookMain,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }
    }

    public static String getInUseIcon() {
        if (packageManager.getComponentEnabledSetting(componentNameBookMain) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return MApplication.getInstance().getString(R.string.icon_book);
        }
        return MApplication.getInstance().getString(R.string.icon_main);
    }
}
