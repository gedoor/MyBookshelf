package com.monke.monkeybook.utils;

import android.graphics.drawable.Drawable;

import com.monke.monkeybook.MApplication;

public class Resources {

    public static String getString(int id) {
        return MApplication.getInstance().getString(id);
    }

    public static int getColor(int id) {
        return MApplication.getInstance().getResources().getColor(id);
    }

    public static Drawable getDrawable(int id) {
        return MApplication.getInstance().getResources().getDrawable(id);
    }
}
