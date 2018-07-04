package com.monke.monkeybook.utils;

import android.content.Context;
import android.provider.Settings;

public class SystemUtil {

    public static int getScreenOffTime(Context context) {
        int screenOffTime = 0;
        try {
            screenOffTime = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenOffTime;
    }
}
