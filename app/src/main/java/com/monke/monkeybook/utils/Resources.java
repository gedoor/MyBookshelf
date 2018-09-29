package com.monke.monkeybook.utils;

import com.monke.monkeybook.MApplication;

public class Resources {

    public static String getString(int resId) {
        return MApplication.getInstance().getString(resId);
    }

    public static int getColor(int resId) {
        return MApplication.getInstance().getResources().getColor(resId);
    }
}
