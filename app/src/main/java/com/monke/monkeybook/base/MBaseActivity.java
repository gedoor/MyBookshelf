//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.base;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.View;

import com.monke.basemvplib.BaseActivity;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;

import java.lang.reflect.Method;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    public SharedPreferences preferences;
    protected ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        initNightTheme();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        mImmersionBar = ImmersionBar.with(this);
        initImmersionBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null) {
            mImmersionBar.destroy();  //在BaseActivity里销毁}
        }
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
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    /**
     * 沉浸状态栏
     */
    protected void initImmersionBar() {
        if (isImmersionBarEnabled()) {
            mImmersionBar.transparentStatusBar();
        } else {
            mImmersionBar.statusBarColor(R.color.status_bar_bag);
        }
        if (isImmersionBarEnabled() && !isNightTheme()) {
            mImmersionBar.statusBarDarkFont(true, 0.2f);
        } else {
            mImmersionBar.statusBarDarkFont(false);
        }
        if (ImmersionBar.canNavigationBarDarkFont()) {
            mImmersionBar.navigationBarColor(R.color.background);
            if (isNightTheme()) {
                mImmersionBar.navigationBarDarkFont(false);
            } else {
                mImmersionBar.navigationBarDarkFont(true);
            }
        }
        mImmersionBar.init();

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

    public void setOrientation() {
        switch (preferences.getString(getString(R.string.pk_screen_direction), "0")) {
            case "0":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case "1":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case "2":
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

}
