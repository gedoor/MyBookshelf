package com.kunfei.bookshelf.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.kunfei.bookshelf.help.ReadBookControl;

public class Prefs {

    public static final String PREFS_KEY_E_INK_MODE = "prefs_key_e_ink_mode";
    public static boolean isEInkMode = false;

    public static void init(Context context) {
        isEInkMode = getConfigStorage(context).getBoolean(PREFS_KEY_E_INK_MODE, false);
    }

    public static void setEInkModeStatus(Context context, boolean enable) {
        isEInkMode = enable;
        getConfigStorage(context).edit().putBoolean(PREFS_KEY_E_INK_MODE, enable).apply();
        ReadBookControl.getInstance().setPageMode(4);
        ReadBookControl.getInstance().setTextColor(1,0xFF000000);
        ReadBookControl.getInstance().setBgColor(1,0xFFFFFFFF);
        ReadBookControl.getInstance().setTextDrawableIndex(0);
        ReadBookControl.getInstance().initTextDrawableIndex();
    }

    public static SharedPreferences getConfigStorage(Context context) {
        return getStorage(context, "CONFIG");
    }

    public static SharedPreferences getStorage(Context context, String storage) {
        return context.getSharedPreferences(storage, Context.MODE_PRIVATE);
    }
}
