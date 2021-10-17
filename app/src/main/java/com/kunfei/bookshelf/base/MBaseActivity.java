//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.BaseActivity;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.utils.ActivityExtensionsKt;
import com.kunfei.bookshelf.utils.ColorUtils;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.theme.MaterialValueHelper;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    private static final String TAG = MBaseActivity.class.getSimpleName();
    public final SharedPreferences preferences = MApplication.getConfigPreferences();
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initTheme();
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
        initImmersionBar();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        int primaryTextColor = MaterialValueHelper.getPrimaryTextColor(this, ColorUtils.isColorLight(ThemeStore.primaryColor(this)));
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(primaryTextColor, PorterDuff.Mode.SRC_ATOP);
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
                                drawable.mutate();
                                drawable.setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
                            }
                        }
                    }
                } catch (Exception ignored) {
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
            ActivityExtensionsKt.fullScreen(this);
            if (isImmersionBarEnabled()) {
                boolean isTransparent = getSupportActionBar() != null && actionBar != null && actionBar.getVisibility() == View.VISIBLE;
                ActivityExtensionsKt.setStatusBarColorAuto(this, ThemeStore.primaryColor(this), isTransparent, isTransparent);
            } else {
                if (getSupportActionBar() != null && actionBar != null && actionBar.getVisibility() == View.VISIBLE) {
                    ActivityExtensionsKt.setStatusBarColorAuto(this, ThemeStore.statusBarColor(this), false, false);
                } else {
                    ActivityExtensionsKt.setStatusBarColorAuto(this, getResources().getColor(R.color.status_bar_bag), false, false);
                }
            }
        } catch (Exception ignored) {
        }
        try {
            if (!preferences.getBoolean("navigationBarColorChange", false)) {
                ActivityExtensionsKt.setNavigationBarColorAuto(this, getResources().getColor(R.color.black));
            } else {
                ActivityExtensionsKt.setNavigationBarColorAuto(this, ThemeStore.primaryColorDark(this));
            }
        } catch (Exception ignored) {
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
    @SuppressLint("SourceLockedOrientationActivity")
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
        return MApplication.getInstance().isNightTheme();
    }

    protected void setNightTheme(boolean isNightTheme) {
        preferences.edit()
                .putBoolean("nightTheme", isNightTheme)
                .apply();
        MApplication.getInstance().initNightTheme();
        MApplication.getInstance().upThemeStore();
        RxBus.get().post(RxBusTag.RECREATE, true);
    }

    protected void initTheme() {
        if (ColorUtils.isColorLight(ThemeStore.primaryColor(this))) {
            setTheme(R.style.CAppTheme);
        } else {
            setTheme(R.style.CAppThemeBarDark);
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

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        if (MApplication.isEInkMode) {
            overridePendingTransition(R.anim.anim_none, R.anim.anim_none);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        if (MApplication.isEInkMode) {
            overridePendingTransition(R.anim.anim_none, R.anim.anim_none);
        }
    }

    @Override
    public void finish() {
        SoftInputUtil.hideIMM(getCurrentFocus());
        super.finish();
        if (MApplication.isEInkMode) {
            overridePendingTransition(R.anim.anim_none, R.anim.anim_none);
        }
    }
}
