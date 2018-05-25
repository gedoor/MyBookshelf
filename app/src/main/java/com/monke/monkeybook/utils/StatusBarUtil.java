package com.monke.monkeybook.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

public class StatusBarUtil {

    private static final int INVALID_VAL = -1;
    private static final int COLOR_DEFAULT = Color.parseColor("#20000000");

    public static void hideNavigationBar(Activity activity, Boolean navigationBar,Boolean layout) {
        View decorView = activity.getWindow().getDecorView();
        if (decorView != null) {
            int ui = decorView.getSystemUiVisibility();
            if (navigationBar) {
                ui |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
                if (!layout){
                    ui &= ~(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
            } else {
                if (layout){
                    ui &= ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                }
                ui &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            decorView.setSystemUiVisibility(ui);
        }
    }

}
