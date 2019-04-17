package com.kunfei.bookshelf.utils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.help.ReadBookControl;

public class Prefs {

    public static final String PREFS_KEY_E_INK_MODE = "prefs_key_e_ink_mode";
    public static boolean isEInkMode = false;

    public static void init() {
        isEInkMode = MApplication.getConfigPreferences().getBoolean(PREFS_KEY_E_INK_MODE, false);
    }

    public static void setEInkModeStatus(boolean enable) {
        isEInkMode = enable;
        MApplication.getConfigPreferences().edit().putBoolean(PREFS_KEY_E_INK_MODE, enable).apply();
        ReadBookControl.getInstance().setPageMode(4);
        ReadBookControl.getInstance().setTextColor(1,0xFF000000);
        ReadBookControl.getInstance().setBgColor(1,0xFFFFFFFF);
        ReadBookControl.getInstance().setTextDrawableIndex(0);
        ReadBookControl.getInstance().initTextDrawableIndex();
    }

}
