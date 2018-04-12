
package com.monke.monkeybook.utils;


import android.content.Context;
import android.util.DisplayMetrics;

import android.view.WindowManager;


import com.monke.monkeybook.MApplication;

import kotlin.TypeCastException;

public class ScreenUtils {

    private static int tempWidth;
    private static int tempHeight;
    private static float tempDensity;


    private static void getCalWidthAndHeight() {
        if (tempWidth == 0) {
            WindowManager windowManager = MApplication.context!= null ?
                    (WindowManager)MApplication.context.getSystemService(Context.WINDOW_SERVICE) : null;

            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            tempWidth = metrics.widthPixels;
            tempHeight = metrics.heightPixels;
            tempDensity = metrics.density;
        }

    }

    public static float getDensity() {
        getCalWidthAndHeight();
        return tempDensity;
    }

    public static int getHeight() {
        getCalWidthAndHeight();
        return tempHeight;
    }

    public static int getWidth() {
        getCalWidthAndHeight();
        return tempWidth;
    }
}
