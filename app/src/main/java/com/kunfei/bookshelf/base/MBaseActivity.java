//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.base;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.kunfei.basemvplib.BaseActivity;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.Theme.ToolbarContentTintHelper;
import com.kunfei.bookshelf.utils.barUtil.ImmersionBar;

import java.lang.reflect.Method;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    public final SharedPreferences preferences = MApplication.getInstance().getConfigPreferences();
    protected ImmersionBar mImmersionBar;
    private Snackbar snackbar;
    private Toolbar toolbar;

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
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        this.toolbar = toolbar;
        super.setSupportActionBar(toolbar);
    }

    /**
     * 设置MENU图标颜色
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar toolbar = getATHToolbar();
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(this, toolbar, menu, getToolbarBackgroundColor(toolbar));
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.setColorFilter(getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(this, getATHToolbar());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            //展开菜单显示图标
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
            if (ImmersionBar.canNavigationBarDarkFont()) {
                mImmersionBar.navigationBarColor(R.color.background);
                if (isNightTheme()) {
                    mImmersionBar.navigationBarDarkFont(false);
                } else {
                    mImmersionBar.navigationBarDarkFont(true);
                }
            }
            if (!preferences.getBoolean("navigationBarColorChange", false)) {
                mImmersionBar.navigationBarColor(R.color.black);
                mImmersionBar.navigationBarDarkFont(false);
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

    protected Toolbar getATHToolbar() {
        return toolbar;
    }

    public static int getToolbarBackgroundColor(Toolbar toolbar) {
        if (toolbar != null) {
            if (toolbar.getBackground() instanceof ColorDrawable) {
                return ((ColorDrawable) toolbar.getBackground()).getColor();
            }
        }
        return 0;
    }

    public void initNightTheme() {
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
