//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.base;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.kunfei.basemvplib.BaseActivity;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.ColorUtil;
import com.kunfei.bookshelf.utils.bar.ImmersionBar;
import com.kunfei.bookshelf.utils.theme.MaterialValueHelper;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import java.lang.reflect.Method;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    private static final String TAG = MBaseActivity.class.getSimpleName();
    public final SharedPreferences preferences = MApplication.getConfigPreferences();
    protected ImmersionBar mImmersionBar;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initTheme();
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        mImmersionBar = ImmersionBar.with(this);
        initImmersionBar();
    }

    @SuppressWarnings("NullableProblems")
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
    public void setSupportActionBar(@Nullable androidx.appcompat.widget.Toolbar toolbar) {
        if (toolbar != null) {
            toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        }
        super.setSupportActionBar(toolbar);
    }

    /**
     * 设置MENU图标颜色
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.setColorFilter(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(ThemeStore.primaryColor(this))), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("PrivateApi")
    @SuppressWarnings("unchecked")
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            //展开菜单显示图标
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                    method = menu.getClass().getDeclaredMethod("getNonActionItems");
                    ArrayList<MenuItem> menuItems = (ArrayList<MenuItem>) method.invoke(menu);
                    if (!menuItems.isEmpty()) {
                        for (MenuItem menuItem : menuItems) {
                            Drawable drawable = menuItem.getIcon();
                            if (drawable != null) {
                                drawable.setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * 沉浸状态栏
     */
    protected void initImmersionBar() {
        try {
            View actionBar = findViewById(R.id.action_bar);
            if (isImmersionBarEnabled()) {
                if (getSupportActionBar() != null && actionBar != null && actionBar.getVisibility() == View.VISIBLE) {
                    mImmersionBar.statusBarColorInt(ThemeStore.primaryColor(this));
                } else {
                    mImmersionBar.transparentStatusBar();
                }
            } else {
                if (getSupportActionBar() != null && actionBar != null && actionBar.getVisibility() == View.VISIBLE) {
                    mImmersionBar.statusBarColorInt(ThemeStore.statusBarColor(this));
                } else {
                    mImmersionBar.statusBarColor(R.color.status_bar_bag);
                }
            }
        } catch (Exception e) {
            Log.e("MonkBook", e.getLocalizedMessage());
        }
        try {
            if (isImmersionBarEnabled() && ColorUtil.isColorLight(ThemeStore.primaryColor(this))) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else if (ColorUtil.isColorLight(ThemeStore.primaryColorDark(this))) {
                mImmersionBar.statusBarDarkFont(true, 0.2f);
            } else {
                mImmersionBar.statusBarDarkFont(false);
            }
            if (!preferences.getBoolean("navigationBarColorChange", false)) {
                mImmersionBar.navigationBarColor(R.color.black);
                mImmersionBar.navigationBarDarkFont(false);
            } else if (ImmersionBar.canNavigationBarDarkFont()) {
                mImmersionBar.navigationBarColorInt(ThemeStore.primaryColorDark(this));
                if (ColorUtil.isColorLight(ThemeStore.primaryColor(this))) {
                    mImmersionBar.navigationBarDarkFont(true);
                } else {
                    mImmersionBar.navigationBarDarkFont(false);
                }
            }
            mImmersionBar.init();
        } catch (Exception e) {
            Log.e("MonkBook", e.getLocalizedMessage());
        }
    }

    /**
     * @return 是否沉浸
     */
    protected boolean isImmersionBarEnabled() {
        return preferences.getBoolean("immersionStatusBar", false);
    }

    /**
     * 设置屏幕方向
     */
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

    /**
     * @return 是否夜间模式
     */
    public boolean isNightTheme() {
        return preferences.getBoolean("nightTheme", false);
    }

    protected void setNightTheme(boolean isNightTheme) {
        preferences.edit()
                .putBoolean("nightTheme", isNightTheme)
                .apply();
        MApplication.getInstance().upThemeStore();
        initTheme();
    }

    protected void initTheme() {
        if (ColorUtil.isColorLight(ThemeStore.primaryColor(this))) {
            setTheme(R.style.CAppTheme);
        } else {
            setTheme(R.style.CAppThemeBarDark);
        }
        if (isNightTheme()) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
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
