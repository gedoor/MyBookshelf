package com.kunfei.bookshelf.utils.Theme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;

import com.kunfei.bookshelf.R;

import java.lang.reflect.Field;
import java.util.ArrayList;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.appcompat.view.menu.BaseMenuPresenter;
import androidx.appcompat.view.menu.ListMenuItemView;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.view.menu.MenuPresenter;
import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.ToolbarWidgetWrapper;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class ToolbarContentTintHelper {

    public static void setToolbarContentColorBasedOnToolbarColor(@NonNull Context context, Toolbar toolbar, int toolbarColor) {
        setToolbarContentColorBasedOnToolbarColor(context, toolbar, null, toolbarColor);
    }

    public static void setToolbarContentColorBasedOnToolbarColor(@NonNull Context context, Toolbar toolbar, @Nullable Menu menu, int toolbarColor) {
        setToolbarContentColorBasedOnToolbarColor(context, toolbar, menu, toolbarColor, ThemeStore.accentColor(context));
    }

    public static void setToolbarContentColorBasedOnToolbarColor(@NonNull Context context, Toolbar toolbar, @Nullable Menu menu, int toolbarColor, final @ColorInt int menuWidgetColor) {
        setToolbarContentColor(context, toolbar, menu, toolbarContentColor(context, toolbarColor), toolbarTitleColor(context, toolbarColor), toolbarSubtitleColor(context, toolbarColor), menuWidgetColor);
    }

    public static void setToolbarContentColor(@NonNull Context context, Toolbar toolbar, final @ColorInt int toolbarContentColor, final @ColorInt int primaryTextColor, final @ColorInt int secondaryTextColor, final @ColorInt int menuWidgetColor) {
        setToolbarContentColor(context, toolbar, null, toolbarContentColor, primaryTextColor, secondaryTextColor, menuWidgetColor);
    }

    @SuppressLint("RestrictedApi")
    @SuppressWarnings("unchecked")
    public static void setToolbarContentColor(@NonNull Context context, Toolbar toolbar, @Nullable Menu menu, final @ColorInt int toolbarContentColor, final @ColorInt int titleTextColor, final @ColorInt int subtitleTextColor, final @ColorInt int menuWidgetColor) {
        if (toolbar == null) return;

        if (menu == null) {
            menu = toolbar.getMenu();
        }

        toolbar.setTitleTextColor(titleTextColor);
        toolbar.setSubtitleTextColor(subtitleTextColor);

        if (toolbar.getNavigationIcon() != null) {
            // Tint the toolbar navigation icon (e.g. back, drawer, etc.)
            toolbar.setNavigationIcon(TintHelper.createTintedDrawable(toolbar.getNavigationIcon(), toolbarContentColor));
        }

        InternalToolbarContentTintUtil.tintMenu(toolbar, menu, toolbarContentColor);
        InternalToolbarContentTintUtil.applyOverflowMenuTint(context, toolbar, menuWidgetColor);

        if (context instanceof Activity) {
            InternalToolbarContentTintUtil.setOverflowButtonColor((Activity) context, toolbarContentColor);
        }

        try {
            // Tint immediate overflow menu items
            final Field menuField = Toolbar.class.getDeclaredField("mMenuBuilderCallback");
            menuField.setAccessible(true);
            final Field presenterField = Toolbar.class.getDeclaredField("mActionMenuPresenterCallback");
            presenterField.setAccessible(true);
            final Field menuViewField = Toolbar.class.getDeclaredField("mMenuView");
            menuViewField.setAccessible(true);

            final MenuPresenter.Callback currentPresenterCb = (MenuPresenter.Callback) presenterField.get(toolbar);
            if (!(currentPresenterCb instanceof ATHMenuPresenterCallback)) {
                final ATHMenuPresenterCallback newPresenterCb = new ATHMenuPresenterCallback(context, menuWidgetColor, currentPresenterCb, toolbar);
                final MenuBuilder.Callback currentMenuCb = (MenuBuilder.Callback) menuField.get(toolbar);
                toolbar.setMenuCallbacks(newPresenterCb, currentMenuCb);
                ActionMenuView menuView = (ActionMenuView) menuViewField.get(toolbar);
                if (menuView != null)
                    menuView.setMenuCallbacks(newPresenterCb, currentMenuCb);
            }

            // OnMenuItemClickListener to tint submenu items
            final Field menuItemClickListener = Toolbar.class.getDeclaredField("mOnMenuItemClickListener");
            menuItemClickListener.setAccessible(true);
            Toolbar.OnMenuItemClickListener currentClickListener = (Toolbar.OnMenuItemClickListener) menuItemClickListener.get(toolbar);
            if (!(currentClickListener instanceof ATHOnMenuItemClickListener)) {
                final ATHOnMenuItemClickListener newClickListener = new ATHOnMenuItemClickListener(context, menuWidgetColor, currentClickListener, toolbar);
                toolbar.setOnMenuItemClickListener(newClickListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ATHMenuPresenterCallback implements MenuPresenter.Callback {

        public ATHMenuPresenterCallback(Context context, final @ColorInt int color, MenuPresenter.Callback parentCb, Toolbar toolbar) {
            mContext = context;
            mColor = color;
            mParentCb = parentCb;
            mToolbar = toolbar;
        }

        private Context mContext;
        private int mColor;
        private MenuPresenter.Callback mParentCb;
        private Toolbar mToolbar;

        @Override
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (mParentCb != null)
                mParentCb.onCloseMenu(menu, allMenusAreClosing);
        }

        @Override
        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            InternalToolbarContentTintUtil.applyOverflowMenuTint(mContext, mToolbar, mColor);
            return mParentCb != null && mParentCb.onOpenSubMenu(subMenu);
        }
    }

    private static class ATHOnMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

        private Context mContext;
        private int mColor;
        private Toolbar.OnMenuItemClickListener mParentListener;
        private Toolbar mToolbar;

        public ATHOnMenuItemClickListener(Context context, final @ColorInt int color, Toolbar.OnMenuItemClickListener parentCb, Toolbar toolbar) {
            mContext = context;
            mColor = color;
            mParentListener = parentCb;
            mToolbar = toolbar;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            InternalToolbarContentTintUtil.applyOverflowMenuTint(mContext, mToolbar, mColor);
            return mParentListener != null && mParentListener.onMenuItemClick(item);
        }
    }

    @Nullable
    public static Toolbar getSupportActionBarView(@Nullable ActionBar ab) {
        if (ab == null || !(ab instanceof WindowDecorActionBar)) return null;
        try {
            WindowDecorActionBar decorAb = (WindowDecorActionBar) ab;
            Field field = WindowDecorActionBar.class.getDeclaredField("mDecorToolbar");
            field.setAccessible(true);
            ToolbarWidgetWrapper wrapper = (ToolbarWidgetWrapper) field.get(decorAb);
            field = ToolbarWidgetWrapper.class.getDeclaredField("mToolbar");
            field.setAccessible(true);
            return (Toolbar) field.get(wrapper);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to retrieve Toolbar from AppCompat support ActionBar: " + t.getMessage(), t);
        }
    }

    public static void handleOnPrepareOptionsMenu(Activity activity, Toolbar toolbar) {
        handleOnPrepareOptionsMenu(activity, toolbar, ThemeStore.accentColor(activity));
    }

    public static void handleOnPrepareOptionsMenu(Activity activity, Toolbar toolbar, int widgetColor) {
        InternalToolbarContentTintUtil.applyOverflowMenuTint(activity, toolbar, widgetColor);
    }

    public static void handleOnCreateOptionsMenu(Context context, Toolbar toolbar, Menu menu, int toolbarColor) {
        setToolbarContentColorBasedOnToolbarColor(context, toolbar, menu, toolbarColor);
    }

    public static void handleOnCreateOptionsMenu(Context context, Toolbar toolbar, Menu menu, @ColorInt int toolbarContentColor, @ColorInt int titleTextColor, @ColorInt int subtitleTextColor, @ColorInt int menuWidgetColor) {
        setToolbarContentColor(context, toolbar, menu, toolbarContentColor, titleTextColor, subtitleTextColor, menuWidgetColor);
    }

    @CheckResult
    @ColorInt
    public static int toolbarContentColor(@NonNull Context context, @ColorInt int toolbarColor) {
        if (ColorUtil.isColorLight(toolbarColor)) {
            return toolbarSubtitleColor(context, toolbarColor);
        }
        return toolbarTitleColor(context, toolbarColor);
    }

    @CheckResult
    @ColorInt
    public static int toolbarSubtitleColor(@NonNull Context context, @ColorInt int toolbarColor) {
        return MaterialValueHelper.getSecondaryTextColor(context, ColorUtil.isColorLight(toolbarColor));
    }

    @CheckResult
    @ColorInt
    public static int toolbarTitleColor(@NonNull Context context, @ColorInt int toolbarColor) {
        return MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(toolbarColor));
    }

    private ToolbarContentTintHelper() {
    }

    public static class InternalToolbarContentTintUtil {

        @SuppressWarnings("unchecked")
        public static void tintMenu(@NonNull Toolbar toolbar, @Nullable Menu menu, final @ColorInt int color) {
            try {
                final Field field = Toolbar.class.getDeclaredField("mCollapseIcon");
                field.setAccessible(true);
                Drawable collapseIcon = (Drawable) field.get(toolbar);
                if (collapseIcon != null) {
                    field.set(toolbar, TintHelper.createTintedDrawable(collapseIcon, color));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (menu != null && menu.size() > 0) {
                for (int i = 0; i < menu.size(); i++) {
                    final MenuItem item = menu.getItem(i);
                    if (item.getIcon() != null) {
                        item.setIcon(TintHelper.createTintedDrawable(item.getIcon(), color));
                    }
                    // Search view theming
                    if (item.getActionView() != null && (item.getActionView() instanceof android.widget.SearchView || item.getActionView() instanceof SearchView)) {
                        SearchViewTintUtil.setSearchViewContentColor(item.getActionView(), color);
                    }
                }
            }
        }

        public static void applyOverflowMenuTint(final @NonNull Context context, final Toolbar toolbar, final @ColorInt int color) {
            if (toolbar == null) return;
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Field f1 = Toolbar.class.getDeclaredField("mMenuView");
                        f1.setAccessible(true);
                        ActionMenuView actionMenuView = (ActionMenuView) f1.get(toolbar);
                        Field f2 = ActionMenuView.class.getDeclaredField("mPresenter");
                        f2.setAccessible(true);

                        // Actually ActionMenuPresenter
                        BaseMenuPresenter presenter = (BaseMenuPresenter) f2.get(actionMenuView);
                        Field f3 = presenter.getClass().getDeclaredField("mOverflowPopup");
                        f3.setAccessible(true);
                        MenuPopupHelper overflowMenuPopupHelper = (MenuPopupHelper) f3.get(presenter);
                        setTintForMenuPopupHelper(context, overflowMenuPopupHelper, color);

                        Field f4 = presenter.getClass().getDeclaredField("mActionButtonPopup");
                        f4.setAccessible(true);
                        MenuPopupHelper subMenuPopupHelper = (MenuPopupHelper) f4.get(presenter);
                        setTintForMenuPopupHelper(context, subMenuPopupHelper, color);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public static void setTintForMenuPopupHelper(final @NonNull Context context, @Nullable MenuPopupHelper menuPopupHelper, final @ColorInt int color) {
            try {
                if (menuPopupHelper != null) {
                    @SuppressLint("RestrictedApi") final ListView listView = ((ShowableListMenu) menuPopupHelper.getPopup()).getListView();
                    listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            try {
                                Field checkboxField = ListMenuItemView.class.getDeclaredField("mCheckBox");
                                checkboxField.setAccessible(true);
                                Field radioButtonField = ListMenuItemView.class.getDeclaredField("mRadioButton");
                                radioButtonField.setAccessible(true);

                                final boolean isDark = !ColorUtil.isColorLight(ATHUtil.resolveColor(context, android.R.attr.windowBackground));

                                for (int i = 0; i < listView.getChildCount(); i++) {
                                    View v = listView.getChildAt(i);
                                    if (!(v instanceof ListMenuItemView)) continue;
                                    ListMenuItemView iv = (ListMenuItemView) v;

                                    CheckBox check = (CheckBox) checkboxField.get(iv);
                                    if (check != null) {
                                        TintHelper.setTint(check, color, isDark);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                            check.setBackground(null);
                                    }

                                    RadioButton radioButton = (RadioButton) radioButtonField.get(iv);
                                    if (radioButton != null) {
                                        TintHelper.setTint(radioButton, color, isDark);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                            radioButton.setBackground(null);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                //noinspection deprecation
                                listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void setOverflowButtonColor(@NonNull Activity activity, final @ColorInt int color) {
            final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final ArrayList<View> outViews = new ArrayList<>();
                    decorView.findViewsWithText(outViews, overflowDescription,
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                    if (outViews.isEmpty()) return;
                    final AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
                    overflow.setImageDrawable(TintHelper.createTintedDrawable(overflow.getDrawable(), color));
                    ViewUtil.removeOnGlobalLayoutListener(decorView, this);
                }
            });
        }

        public static final class SearchViewTintUtil {
            private static void tintImageView(Object target, Field field, final @ColorInt int color) throws Exception {
                field.setAccessible(true);
                final ImageView imageView = (ImageView) field.get(target);
                if (imageView.getDrawable() != null)
                    imageView.setImageDrawable(TintHelper.createTintedDrawable(imageView.getDrawable(), color));
            }

            public static void setSearchViewContentColor(View searchView, final @ColorInt int color) {
                if (searchView == null) return;
                final Class<?> cls = searchView.getClass();
                try {
                    final Field mSearchSrcTextViewField = cls.getDeclaredField("mSearchSrcTextView");
                    mSearchSrcTextViewField.setAccessible(true);
                    final EditText mSearchSrcTextView = (EditText) mSearchSrcTextViewField.get(searchView);
                    mSearchSrcTextView.setTextColor(color);
                    mSearchSrcTextView.setHintTextColor(ColorUtil.adjustAlpha(color, 0.5f));
                    TintHelper.setCursorTint(mSearchSrcTextView, color);

                    Field field = cls.getDeclaredField("mSearchButton");
                    tintImageView(searchView, field, color);
                    field = cls.getDeclaredField("mGoButton");
                    tintImageView(searchView, field, color);
                    field = cls.getDeclaredField("mCloseButton");
                    tintImageView(searchView, field, color);
                    field = cls.getDeclaredField("mVoiceButton");
                    tintImageView(searchView, field, color);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private SearchViewTintUtil() {
            }
        }

        private InternalToolbarContentTintUtil() {
        }
    }
}
