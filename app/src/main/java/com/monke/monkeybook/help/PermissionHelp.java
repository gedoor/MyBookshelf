package com.monke.monkeybook.help;

import android.app.Activity;

import com.monke.monkeybook.R;

import pub.devrel.easypermissions.EasyPermissions;

import static com.monke.monkeybook.MApplication.PerList;

public class PermissionHelp {

    public static void checkPermission(Activity activity) {
        if (!EasyPermissions.hasPermissions(activity, PerList)) {
            EasyPermissions.requestPermissions(activity, activity.getString(R.string.restore_permission),
                    981293, PerList);
        }
    }

}
