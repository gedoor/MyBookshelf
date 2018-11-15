//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.base;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.gyf.barlibrary.ImmersionBar;

import java.lang.reflect.Method;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    public final SharedPreferences preferences = MApplication.getInstance().getConfigPreferences();
    protected ImmersionBar mImmersionBar;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initNightTheme();
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        mImmersionBar = ImmersionBar.with(this);
        initImmersionBar();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 如果你的app可以横竖屏切换，并且适配4.4或者emui3手机请务必在onConfigurationChanged方法里添加这句话
        ImmersionBar.with(this).init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    @SuppressLint("PrivateApi")
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * 设置MENU图标颜色
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.setColorFilter(getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    /**
     * 沉浸状态栏
     */
    protected void initImmersionBar() {
        try {
            if (isImmersionBarEnabled()) {
                if (getSupportActionBar() != null && isNightTheme() && findViewById(R.id.action_bar) != null) {
                    mImmersionBar.statusBarColor(R.color.colorPrimary);
                } else {
                    mImmersionBar.transparentStatusBar();
                }
            } else {
                if (getSupportActionBar() != null && isNightTheme())
                    mImmersionBar.statusBarColor(R.color.colorPrimaryDark);
                else
                    mImmersionBar.statusBarColor(R.color.status_bar_bag);
            }
        } catch (Exception e) {
            Log.e("MonkBook", e.getLocalizedMessage());
        }
        try {
            if (isImmersionBarEnabled() && !isNightTheme()) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }
            changeNavigationBarColor(R.color.background);
            mImmersionBar.init();
        } catch (Exception e) {
            Log.e("MonkBook", e.getLocalizedMessage());
        }
    }

    /**
     * 导航栏变色
     */
    protected void changeNavigationBarColor(int navigationBarColor){
        changeNavigationBarColorInt(Build.VERSION.SDK_INT >= 23 ? getColor(navigationBarColor) : getResources().getColor(navigationBarColor));
    }

    protected void changeNavigationBarColorInt(int navigationBarColor){
        String navBarColorConfig = preferences.getString(getString(R.string.pk_navbar_color), "0");
        switch (navBarColorConfig) {
            case "1":
                mImmersionBar.navigationBarColorInt(Color.BLACK);
                mImmersionBar.navigationBarDarkIcon(false);
                break;
            case "2":
                mImmersionBar.navigationBarColorInt(Color.WHITE);
                mImmersionBar.navigationBarDarkIcon(true);
                break;
            default: //使用传入的颜色, 保持底部与界面一致
                mImmersionBar.navigationBarColorInt(navigationBarColor);
                mImmersionBar.navigationBarDarkIcon(!isColorDark(navigationBarColor));
                break;
        }
    }

    private boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false;
        }else{
            return true;
        }
    }

    /**
     * 在阅读文章时, 底部有一个颜色分明的分割线, 实在是让我有点看不过去, 所以将底部分割线去除掉
     */
    protected void changeNavigationBarColorInt(int navigationBarColor, boolean inReading) {
        if(inReading && Build.VERSION.SDK_INT >= 28){
            getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
        }
        changeNavigationBarColorInt(navigationBarColor);
    }

    /**
     * @return 是否沉浸
     */
    protected boolean isImmersionBarEnabled() {
        return preferences.getBoolean("immersionStatusBar", false);
    }

    /**
     * @return 是否夜间模式
     */
    protected boolean isNightTheme() {
        return preferences.getBoolean("nightTheme", false);
    }

    protected void setNightTheme(boolean isNightTheme) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("nightTheme", isNightTheme);
        editor.apply();
        initNightTheme();
    }

    public void setOrientation(int screenDirection) {
        switch (screenDirection) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 3:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
        }
    }

    public void initNightTheme() {
        if (isNightTheme()) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    public void showSnackBar(String msg) {
        showSnackBar(getCurrentFocus(), msg);
    }

    public void showSnackBar(String msg, int length) {
        showSnackBar(getCurrentFocus(), msg, length);
    }

    public void showSnackBar(View view, String msg) {
        showSnackBar(view, msg, Snackbar.LENGTH_SHORT);
    }

    public void showSnackBar(View view, String msg, int length) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, msg, length);
        } else {
            snackbar.setText(msg);
            snackbar.setDuration(length);
        }
        snackbar.show();
    }

    public void hideSnackBar() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }
}
