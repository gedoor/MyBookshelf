package com.monke.monkeybook.help;

import android.util.Log;
import com.monke.monkeybook.MApplication;

/**
 * Created by PureDark on 2016/9/24.
 */

public class Logger {

    public static void d(String tag, String message) {
        if (MApplication.DEBUG)
            Log.d(tag, message);
    }

    public static void e(String tag, String message, Throwable e) {
        if (MApplication.DEBUG)
            Log.e(tag, message, e);
    }
}
