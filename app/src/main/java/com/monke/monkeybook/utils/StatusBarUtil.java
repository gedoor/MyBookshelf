package com.monke.monkeybook.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarUtil {

    private static final int INVALID_VAL = -1;
    private static final int COLOR_DEFAULT = Color.parseColor("#20000000");

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void compat(Activity activity, int statusColor)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (statusColor != INVALID_VAL)
            {
                window.setStatusBarColor(statusColor);
            }
            /*if (!hasSoftKeys(activity.getWindowManager()))
            {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }*/

            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                View decorView = activity.getWindow().getDecorView();
                if(decorView != null){
                    int ui = decorView.getSystemUiVisibility();
                    ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    decorView.setSystemUiVisibility(ui);
                }
            }*/

            //解决7.0沉浸式状态栏蒙灰问题
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    Class decorViewClass = Class.forName("com.android.internal.policy.DecorView");
                    Field field = decorViewClass.getDeclaredField("mSemiTransparentStatusBarColor");
                    field.setAccessible(true);
                    field.setInt(window.getDecorView(), Color.TRANSPARENT);  //改为透明
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int color = COLOR_DEFAULT;
            ViewGroup contentView = activity.findViewById(android.R.id.content);
            if (statusColor != INVALID_VAL)
            {
                color = statusColor;
            }
            View statusBarView = contentView.getChildAt(0);
            //改变颜色时避免重复添加statusBarView
            if (statusBarView != null && statusBarView.getMeasuredHeight() == getStatusBarHeight(activity))
            {
                statusBarView.setBackgroundColor(color);
                return;
            }
            statusBarView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(activity));
            statusBarView.setBackgroundColor(color);
            contentView.addView(statusBarView, lp);
        }
    }

    /**
     * 判断底部navigator是否已经显示
     * @param windowManager
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean hasSoftKeys(WindowManager windowManager){
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    public static void setFitsSystem (Activity activity){
        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
            mChildView.setFitsSystemWindows(true);
        }
    }

    //设置深色状态栏图标
    public static void setStatusBarIcon(Activity activity, Boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (OSUtil.getSystem().equals("sys_miui")) {
                MIUISetStatusBarLightMode(activity,dark);
            } else if (OSUtil.getSystem().equals("sys_flyme")) {
                FlymeSetStatusBarLightMode(activity,dark);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = activity.getWindow().getDecorView();
                if(decorView != null){
                    int ui = decorView.getSystemUiVisibility();
                    if(dark){
                        ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    } else{
                        ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    }
                    decorView.setSystemUiVisibility(ui);
                }
            }
        }
    }

    //获取状态栏高度
    public static int getStatusBarHeight(Context context)
    {
        int statusBarHeight  = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            statusBarHeight  = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight ;
    }

    //获取导航栏高度
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        int resourceId = context.getResources().getIdentifier(
                context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = context.getResources().getDimensionPixelOffset(resourceId);
        }

        return navigationBarHeight;
    }

    //获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavigationBar;
    }

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

    /*ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
                mChildView.setFitsSystemWindows(true);
            }*/


    /**
     * 需要MIUIV6以上
     *
     * @param activity
     * @param dark     是否把状态栏文字及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    private static boolean MIUISetStatusBarLightMode(Activity activity, boolean dark) {
        boolean result = false;
        Window window = activity.getWindow();
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    if (dark) {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param activity 需要设置窗口的Activity
     * @param dark   是否把状态栏文字及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    private static boolean FlymeSetStatusBarLightMode(Activity activity, boolean dark) {
        boolean result = false;
        Window window = activity.getWindow();
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
